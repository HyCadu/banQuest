package shot

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.com.trilhati.data.backup.BackupCodec
import br.com.trilhati.data.content.ContentRepository
import br.com.trilhati.data.db.AppDatabase
import br.com.trilhati.data.remote.ApiResult
import br.com.trilhati.data.remote.AuthRepository
import br.com.trilhati.data.remote.ErrorKind
import br.com.trilhati.data.remote.SupabaseAuthApi
import br.com.trilhati.data.remote.SupabaseConfig
import br.com.trilhati.data.remote.SupabaseSocialApi
import br.com.trilhati.data.repo.ProgressRepository
import br.com.trilhati.data.social.AuthStore
import br.com.trilhati.data.social.SyncRepository
import br.com.trilhati.data.social.SyncStore
import br.com.trilhati.domain.lesson.LessonPlanner
import br.com.trilhati.domain.lesson.LessonRun
import br.com.trilhati.domain.progress.SessionKind
import br.com.trilhati.domain.progress.SessionResult
import br.com.trilhati.domain.social.InitialSync
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.random.Random
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/** Cliente social inteiro (login, fila, retrato, conflito, amigos) contra um Supabase falso em memória. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SocialSyncTest {
    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var clock: MutableClock
    private lateinit var progress: ProgressRepository
    private lateinit var content: ContentRepository
    private lateinit var server: FakeSupabase
    private lateinit var auth: AuthRepository
    private lateinit var store: SyncStore
    private lateinit var sync: SyncRepository
    private val email = "carlos@exemplo.com"

    @Before fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        clock = MutableClock(Instant.parse("2026-11-18T15:00:00Z"))
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        progress = ProgressRepository(db, db.progressDao(), clock)
        content = ContentRepository(context)
        server = FakeSupabase(clock)
        val config = SupabaseConfig("https://teste.supabase.co", "sb_publishable_teste")
        val authStore = AuthStore(context)
        store = SyncStore(context)
        runBlocking { authStore.clear(); store.clearAccount(); progress.ensureReady(today()) }
        auth = AuthRepository(SupabaseAuthApi(config, server, clock), authStore, clock)
        val notifications = br.com.trilhati.notify.Notifications(context).also { it.createChannels() }
        sync = SyncRepository(SupabaseSocialApi(config, server, auth), auth, store, progress, db.progressDao(), content, notifications, clock)
    }

    @After fun tearDown() = db.close()

    private fun today(): LocalDate = LocalDate.now(clock)

    /** Grava uma lição concluída direto no repositório (8 acertos, 60 s cada), como o fim de uma lição faz. */
    private fun studyLesson(lessonId: String = "u1l1") = runBlocking {
        val questions = content.lesson(lessonId)!!.questions.take(8)
        var run = LessonRun.start(questions)
        repeat(questions.size) { run = LessonRun.answer(run, true, 60_000, 180_000) }
        progress.applySession(SessionResult(SessionKind.Lesson, lessonId, "u1", run.records, run.bestCombo, run.perfect), today(), content.course)
    }

    private fun login(name: String = "Carlos", mascot: String = "Tartaruga"): InitialSync = runBlocking {
        assertTrue(auth.sendCode(" Carlos@Exemplo.com ") is ApiResult.Ok)
        assertEquals(listOf(email), server.emailsSent.takeLast(1))
        assertTrue(auth.verifyCode(email, "123456") is ApiResult.Ok)
        assertTrue(sync.saveProfile(name, mascot) is ApiResult.Ok)
        (sync.initialSync() as ApiResult.Ok).value
    }

    private fun meta() = runBlocking { store.current() }
    private fun uid() = server.userIdOf(email)!!

    @Test fun codigoErradoNaoEntra() = runBlocking {
        auth.sendCode(email)
        val wrong = auth.verifyCode(email, "000000")
        assertTrue(wrong is ApiResult.Err && wrong.kind == ErrorKind.InvalidCode)
        assertNull(auth.current())
        assertTrue(auth.verifyCode(email, "123456") is ApiResult.Ok)
        assertEquals(email, auth.current()?.email)
    }

    @Test fun entrarSincronizarETrocarDeAparelhoRestauraTudo() {
        studyLesson("u1l1") // estudou antes de criar a conta
        assertEquals(InitialSync.UploadLocal, login())
        val local = runBlocking { progress.snapshot(today()).first() }
        assertEquals("Carlos", local.profile.name); assertEquals("Tartaruga", local.profile.character)
        assertEquals(1L, server.snapshots.getValue(uid()).version)
        assertTrue(meta().profileReady)

        // primeira rodada: a sessão sai da fila e o ranking chega ao cache
        assertTrue(runBlocking { sync.syncNow() } is ApiResult.Ok)
        assertEquals(1, server.sessions.size)
        assertEquals(local.stats.totalXp, server.sessions[0].xp)
        assertEquals("lesson", server.sessions[0].kind)
        assertTrue(runBlocking { db.progressDao().outbox(10) }.isEmpty())
        val overview = meta().overview!!
        assertEquals(local.stats.totalXp, overview.global.single { it.isMe }.xp)
        assertNotNull(overview.profile?.friendCode)

        // nada mudou: não reenvia o retrato
        val pushes = server.pushes
        runBlocking { sync.syncNow() }
        assertEquals(pushes, server.pushes)

        // mais uma lição: sessão nova e retrato novo
        studyLesson("u1l2")
        runBlocking { sync.syncNow() }
        assertEquals(2, server.sessions.size)
        assertEquals(2L, server.snapshots.getValue(uid()).version)
        val before = runBlocking { progress.snapshot(today()).first() }

        // "celular novo": sai da conta (limpa o aparelho) e entra de novo
        runBlocking { sync.signOut() }
        assertNull(runBlocking { auth.current() })
        assertEquals(0, runBlocking { progress.snapshot(today()).first() }.stats.totalXp)
        assertTrue(!meta().profileReady)
        assertEquals(InitialSync.DownloadCloud, login())
        val after = runBlocking { progress.snapshot(today()).first() }
        assertEquals(before.stats, after.stats)
        assertEquals(before.lessonProgress, after.lessonProgress)
        assertEquals(before.achievements, after.achievements)
        // e continua sincronizando normalmente depois
        assertTrue(runBlocking { sync.syncNow() } is ApiResult.Ok)
        assertEquals(2, server.sessions.size)
    }

    @Test fun conflitoEntreAparelhosDeixaEscolher() {
        login()
        runBlocking { sync.syncNow() }
        // outro aparelho gravou mais progresso na nuvem
        val other = runBlocking { progress.exportBackup() }.let { b -> b.copy(stats = b.stats!!.copy(totalXp = 999, lessonsCompleted = 7)) }
        server.pushFromOtherDevice(uid(), Json.parseToJsonElement(BackupCodec.encode(other)))
        studyLesson()
        assertTrue(runBlocking { sync.syncNow() } is ApiResult.Ok)
        val conflict = meta().conflict!!
        assertEquals(999, conflict.serverXp); assertEquals(7, conflict.serverLessons)
        assertTrue(conflict.localXp in 1..150)
        // a sessão da lição foi para o ranking mesmo com o conflito
        assertEquals(1, server.sessions.size)

        // fica o da nuvem
        assertTrue(runBlocking { sync.resolveConflict(keepLocal = false) } is ApiResult.Ok)
        assertNull(meta().conflict)
        assertEquals(999, runBlocking { progress.snapshot(today()).first() }.stats.totalXp)

        // de novo, agora mantendo o deste aparelho (sobrescreve a nuvem)
        server.pushFromOtherDevice(uid(), Json.parseToJsonElement(BackupCodec.encode(other.copy(stats = other.stats!!.copy(totalXp = 5)))))
        studyLesson("u1l2")
        runBlocking { sync.syncNow() }
        assertNotNull(meta().conflict)
        assertTrue(runBlocking { sync.resolveConflict(keepLocal = true) } is ApiResult.Ok)
        assertNull(meta().conflict)
        val cloud = BackupCodec.decode(server.snapshots.getValue(uid()).payload.toString()) as br.com.trilhati.data.backup.BackupRead.Ok
        assertEquals(runBlocking { progress.snapshot(today()).first() }.stats.totalXp, cloud.backup.stats!!.totalXp)
    }

    @Test fun semInternetGuardaNaFilaEEnviaDepois() {
        login()
        server.offline = true
        studyLesson()
        val r = runBlocking { sync.syncNow() }
        assertTrue(r is ApiResult.Err && r.kind == ErrorKind.Network)
        assertEquals(1, runBlocking { db.progressDao().outbox(10) }.size)
        assertNotNull(meta().lastError)
        server.offline = false
        assertTrue(runBlocking { sync.syncNow() } is ApiResult.Ok)
        assertEquals(1, server.sessions.size)
        assertTrue(runBlocking { db.progressDao().outbox(10) }.isEmpty())
        assertNull(meta().lastError)
    }

    /** Lição por conceitos de verdade (12 exercícios, montada pelo LessonPlanner), feita sem erro e rápido. */
    private fun studyConceptLessonPerfectly(lessonId: String) = runBlocking {
        val lesson = content.lesson(lessonId)!!
        val questions = LessonPlanner.plan(lesson.questions, emptySet(), Random(7), lesson.concepts.map { it.id })
        assertEquals(LessonPlanner.SIZE, questions.size)
        var run = LessonRun.start(questions)
        while (run.current != null) run = LessonRun.answer(run, true, 30_000, 180_000)
        progress.applySession(SessionResult(SessionKind.Lesson, lessonId, "u1", run.records, run.bestCombo, run.perfect), today(), content.course)
            .summary.totalXp
    }

    @Test fun licaoPorConceitosPerfeitaEntraNoRanking() {
        login()
        val xp = studyConceptLessonPerfectly("u1l2")
        assertEquals(210, xp) // 12 × 15 + 20 + 10: passava do antigo limite de 150 e ficava fora do ranking
        assertTrue(runBlocking { sync.syncNow() } is ApiResult.Ok)
        assertEquals(listOf(210), server.sessions.map { it.xp })
        assertEquals(210, meta().overview!!.global.single { it.isMe }.xp)
        assertTrue(runBlocking { db.progressDao().outbox(10) }.isEmpty())
    }

    @Test fun provaAnteriorInteiraEntraNoRanking() {
        login()
        val xp = runBlocking {
            val qs = content.exams.first { it.id == "fgv-banestes-2018" }.questions(br.com.trilhati.domain.exam.ExamPart.Full)
            var run = LessonRun.start(qs, repeatMistakes = false)
            while (run.current != null) run = LessonRun.answer(run, true, 30_000, 180_000)
            progress.applySession(SessionResult(SessionKind.Exam, null, null, run.records, run.bestCombo, run.perfect), today(), content.course)
                .summary.totalXp
        }
        assertEquals(87 * 15 + 30, xp) // 87 questões: muito acima dos limites de lição (30 respostas, 210 XP)
        assertTrue(runBlocking { sync.syncNow() } is ApiResult.Ok)
        assertEquals(listOf("exam" to xp), server.sessions.map { it.kind to it.xp })
        assertEquals(xp, meta().overview!!.global.single { it.isMe }.xp)
        assertTrue(runBlocking { db.progressDao().outbox(10) }.isEmpty())
    }

    @Test fun sessaoForaDosLimitesSaiDaFilaSemTravar() {
        login()
        runBlocking {
            db.progressDao().insertOutbox(
                br.com.trilhati.data.db.OutboxSessionEntity("rapida", today().toEpochDay(), "lesson", 100, 8, 1_000, 1, 1, today().toEpochDay(), 0),
            )
        }
        studyLesson()
        assertTrue(runBlocking { sync.syncNow() } is ApiResult.Ok)
        assertEquals(1, server.sessions.size) // só a lição normal
        assertTrue(runBlocking { db.progressDao().outbox(10) }.isEmpty())
    }

    @Test fun tokenVencidoRenovaSozinho() {
        login()
        server.expireAllTokens()
        assertTrue(runBlocking { sync.syncNow() } is ApiResult.Ok)
        assertTrue(server.paths.contains("/auth/v1/token"))
        // e quando o relógio passa da validade, renova antes de chamar
        clock.instant = clock.instant.plusSeconds(2 * 3600)
        val before = server.paths.count { it == "/auth/v1/token" }
        assertTrue(runBlocking { sync.syncNow() } is ApiResult.Ok)
        assertEquals(before + 1, server.paths.count { it == "/auth/v1/token" })
    }

    @Test fun perfilApagadoNoServidorVoltaParaACriacaoDoPerfil() {
        login()
        server.profiles.remove(uid())
        val r = runBlocking { sync.syncNow() }
        assertTrue(r is ApiResult.Err && r.kind == ErrorKind.ProfileMissing)
        assertTrue(!meta().profileReady)
    }

    @Test fun amigosPorCodigoPedidoAvisadoUmaVezEPrivacidade() {
        shadowOf(context as android.app.Application).grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS)
        val bia = server.seedUser("Bia", "TRI-BIA22")
        val duda = server.seedUser("Duda", "TRI-DUD44", mascot = "Tamandua")
        val oculto = server.seedUser("Oculto", "TRI-OCU55", showInGlobal = false)
        login()
        server.requests += FakeSupabase.Request("r1", duda, uid())

        val added = runBlocking { sync.addFriendByCode("TRI-BIA22") }
        assertEquals("added", (added as ApiResult.Ok).value.result)
        assertEquals("Bia", added.value.displayName)
        val ov = meta().overview!!
        assertEquals(listOf("Bia"), ov.friends.map { it.displayName })
        assertEquals("incoming", ov.global.single { it.userId == duda }.friendState)
        assertTrue("oculto não aparece no geral", ov.global.none { it.userId == oculto })
        assertEquals(1, ov.requests.size)

        // o pedido gerou um aviso, e só uma vez
        val manager = context.getSystemService(android.app.NotificationManager::class.java)
        assertEquals(1, shadowOf(manager).allNotifications.count { it.extras.getString(android.app.Notification.EXTRA_TITLE) == "Pedido de amizade" })
        runBlocking { sync.refreshOverview() }
        assertEquals(1, shadowOf(manager).allNotifications.count { it.extras.getString(android.app.Notification.EXTRA_TITLE) == "Pedido de amizade" })

        // aceitar vira amizade; código inexistente e o próprio código respondem certo
        assertEquals("accepted", (runBlocking { sync.respondRequest("r1", true) } as ApiResult.Ok).value)
        assertEquals(setOf("Bia", "Duda"), meta().overview!!.friends.map { it.displayName }.toSet())
        assertEquals("not_found", (runBlocking { sync.addFriendByCode("TRI-ZZZZZ") } as ApiResult.Ok).value.result)
        val myCode = meta().overview!!.profile!!.friendCode
        assertEquals("self", (runBlocking { sync.addFriendByCode(myCode) } as ApiResult.Ok).value.result)

        // ficar oculto do geral
        runBlocking { sync.setShowInGlobal(false) }
        assertEquals(false, meta().overview!!.profile!!.showInGlobal)

        // desfazer amizade
        runBlocking { sync.removeFriend(bia) }
        assertEquals(listOf("Duda"), meta().overview!!.friends.map { it.displayName })
    }

    @Test fun excluirContaApagaNoServidorELimpaOAparelho() {
        studyLesson()
        login()
        runBlocking { sync.syncNow() }
        assertTrue(runBlocking { sync.deleteAccount() } is ApiResult.Ok)
        assertTrue(server.profiles.isEmpty()); assertTrue(server.snapshots.isEmpty()); assertTrue(server.sessions.isEmpty())
        assertNull(runBlocking { auth.current() })
        assertEquals(0, runBlocking { progress.snapshot(today()).first() }.stats.totalXp)
    }

    @Test fun avisaSeHaProgressoNaoEnviadoAntesDeSair() {
        login()
        runBlocking { sync.syncNow() }
        assertTrue(!runBlocking { sync.hasUnsyncedChanges() })
        server.offline = true
        studyLesson()
        runBlocking { sync.syncNow() }
        assertTrue(runBlocking { sync.hasUnsyncedChanges() })
    }
}
