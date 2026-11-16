package shot

import android.content.Context
import android.os.Looper
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.com.trilhati.data.content.ContentRepository
import br.com.trilhati.data.db.AppDatabase
import br.com.trilhati.data.prefs.SettingsRepository
import br.com.trilhati.data.repo.AppSnapshot
import br.com.trilhati.data.repo.ProgressRepository
import br.com.trilhati.domain.model.QuestionType
import br.com.trilhati.domain.progress.*
import br.com.trilhati.feature.session.*
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/** Muda de dia conforme o teste manda. */
class MutableClock(var instant: Instant) : Clock() {
    override fun getZone(): ZoneId = ZoneId.of("America/Sao_Paulo")
    override fun withZone(zone: ZoneId): Clock = this
    override fun instant(): Instant = instant
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class IntegrationTest {
    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var clock: MutableClock
    private lateinit var progress: ProgressRepository
    private lateinit var content: ContentRepository
    private lateinit var settings: SettingsRepository

    @Before fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        androidx.work.testing.WorkManagerTestInitHelper.initializeTestWorkManager(context)
        clock = MutableClock(Instant.parse("2026-11-18T15:00:00Z")) // quarta 12:00 em Brasília
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        progress = ProgressRepository(db, db.progressDao(), clock)
        content = ContentRepository(context)
        settings = SettingsRepository(context)
    }

    @After fun tearDown() = db.close()

    private fun today() = java.time.LocalDate.now(clock)
    private fun idle() = shadowOf(Looper.getMainLooper()).idle()
    private fun snap(): AppSnapshot = runBlocking { progress.snapshot(today()).first() }

    private fun <T> await(what: String, supplier: () -> T?): T {
        val end = System.currentTimeMillis() + 15_000
        while (System.currentTimeMillis() < end) {
            idle()
            supplier()?.let { return it }
            Thread.sleep(15)
        }
        fail("tempo esgotado esperando: $what"); throw IllegalStateException()
    }

    private fun newVm(kind: String, id: String = "-") =
        SessionViewModel(SavedStateHandle(mapOf("kind" to kind, "id" to id)), content, progress, settings, clock, br.com.trilhati.sound.SoundPlayer(context), br.com.trilhati.notify.Notifications(context),
            br.com.trilhati.data.social.SyncScheduler(context))

    /** Joga a sessão inteira. [wrongOnFirst] é quantas questões errar de propósito (as primeiras de múltipla escolha). */
    private fun play(vm: SessionViewModel, wrongOnFirst: Int = 0, slipOnMatch: Boolean = false, feedbacks: MutableList<Pair<QuestionType, Phase.Feedback>>? = null): SessionUi.Finished {
        var wrongLeft = wrongOnFirst
        var slip = slipOnMatch
        var guard = 0
        while (guard++ < 60) {
            val ui = await("próximo estado") { vm.ui.value.takeIf { it !is SessionUi.Loading && it !is SessionUi.Saving } }
            when (ui) {
                is SessionUi.Finished -> return ui
                is SessionUi.Playing -> {
                    if (ui.phase is Phase.Feedback) { vm.continueAfterFeedback(); idle(); continue }
                    val q = ui.question
                    when (q.type) {
                        QuestionType.Match -> {
                            val order = (ui.exercise as ExerciseState.Match).rightOrder
                            if (slip) { slip = false; vm.matchLeft(0); idle(); vm.matchRight(order.indexOf(1)); idle() } // um par errado
                            q.pairs.indices.forEach { i -> vm.matchLeft(i); idle(); vm.matchRight(order.indexOf(i)); idle() }
                        }
                        QuestionType.OrderCode -> {
                            q.lines.indices.forEach { vm.orderPlace(it); idle() }
                            vm.check()
                        }
                        else -> {
                            val goWrong = wrongLeft > 0 && !ui.retry
                            if (goWrong) wrongLeft--
                            vm.select(if (goWrong) (q.answer + 1) % q.options.size else q.answer); idle(); vm.check()
                        }
                    }
                    idle()
                    try { await("feedback") { (vm.ui.value as? SessionUi.Playing)?.phase as? Phase.Feedback }.let { feedbacks?.add(q.type to it) } }
                    catch (e: AssertionError) { val cur = vm.ui.value as? SessionUi.Playing; fail("sem feedback em ${q.id} (${q.type}) exercício=${cur?.exercise}") }
                }
                else -> fail("estado inesperado: $ui")
            }
        }
        fail("a sessão não terminou"); throw IllegalStateException()
    }

    @Test fun primeiraAbertura_criaTudoComPadroes() {
        runBlocking { progress.ensureReady(today()) }
        val s = snap()
        assertEquals("Estudante", s.profile.name); assertEquals(100, s.profile.dailyGoalXp)
        assertEquals(5, s.stats.hearts.hearts); assertEquals(0, s.stats.totalXp)
        assertEquals(3, runBlocking { progress.observeQuests(today()).first() }.size)
        // idempotente
        runBlocking { progress.ensureReady(today()); progress.ensureReady(today()) }
        assertEquals(3, runBlocking { progress.observeQuests(today()).first() }.size)
    }

    @Test fun conteudoCarregaDosAssets() {
        assertEquals(10, content.course.size)
        // 50 questões do pacote da unidade 1 + 26 de provas anteriores encaixadas nas lições dela (assets/provas).
        val u1 = content.course[0].lessons.flatMap { it.questions }
        assertEquals(76, u1.size)
        assertEquals(26, u1.count { it.isFromExam })
        assertNotNull(content.lesson("u1l3")); assertNotNull(content.question("u1l1q01"))
        assertEquals("FGV · Banestes 2021 · questão 48", content.question("fgv-banestes-2021-q48")?.ref?.label)
        // Unidade sem pacote continua "chegando", mesmo tendo questões de prova esperando por ela.
        assertFalse(content.lesson("u2l1")!!.hasContent)
    }

    @Test fun licaoPerfeitaDeTodosOsTiposGravaTudo() {
        runBlocking { progress.ensureReady(today()) }
        // u1l1 tem match e truefalse; joga com todos os tipos possíveis.
        val vm = newVm("lesson", "u1l1")
        val fin = play(vm)
        assertTrue(fin.run.perfect)
        val s = snap()
        assertTrue(s.lessonProgress.getValue("u1l1").perfect)
        assertEquals(1, s.stats.streak.current)
        assertEquals(8, s.stats.firstTryAnswered); assertEquals(8, s.stats.firstTryCorrect)
        assertEquals(fin.processed.summary.totalXp, s.stats.totalXp)
        assertTrue(s.stats.gems >= 10)
        // a sessão entrou na fila para o ranking (sai quando houver rede)
        val outbox = runBlocking { db.progressDao().outbox(10) }
        assertEquals(1, outbox.size)
        assertEquals("lesson", outbox[0].kind); assertEquals(s.stats.totalXp, outbox[0].xp); assertEquals(8, outbox[0].answers)
        assertEquals(today().toEpochDay(), outbox[0].day); assertEquals(1, outbox[0].streak)
        assertEquals(1, s.dailyLogs.getValue(today()).lessons)
        // desbloqueou a próxima
        val st = TrailEngine.statuses(content.course, s.lessonProgress)
        assertEquals(LessonStatus.Perfect, st["u1l1"]); assertEquals(LessonStatus.Available, st["u1l2"])
        assertEquals(LessonStatus.Locked, st["u1l3"])
        // missões avançaram
        val quests = runBlocking { progress.observeQuests(today()).first() }
        assertTrue(quests.any { it.progress > 0 })
    }

    @Test fun errosCustamCoracaoViramBancoDeErrosETreinoDevolveCoracao() {
        runBlocking { progress.ensureReady(today()) }
        val fin = play(newVm("lesson", "u1l2"), wrongOnFirst = 2)
        assertFalse(fin.run.perfect)
        var s = snap()
        assertEquals(3, s.stats.hearts.hearts) // 2 erros de primeira = 2 corações
        assertEquals(2, s.errorBankSize)
        assertEquals(0, s.dueCount) // revisão só amanhã
        // Treino de erros: usa o banco mesmo sem vencidas
        val practice = play(newVm("errors"))
        assertEquals(2, practice.run.records.size)
        s = snap()
        assertEquals(4, s.stats.hearts.hearts)
        assertEquals(1, s.stats.practiceSessions)
        // acertou no treino: a revisão sobe para o intervalo de 3 dias. Amanhã nada vence; em 3 dias, vence.
        clock.instant = clock.instant.plusSeconds(24 * 3600)
        runBlocking { progress.ensureReady(today()) }
        assertEquals(0, snap().dueCount)
        clock.instant = clock.instant.plusSeconds(2 * 24 * 3600)
        runBlocking { progress.ensureReady(today()) }
        assertEquals(2, snap().dueCount)
    }

    @Test fun semCoracoesNaoComecaLicaoMasTreinoFunciona() {
        runBlocking {
            progress.ensureReady(today())
            repeat(5) { progress.loseHeart() }
        }
        val vm = newVm("lesson", "u1l1")
        val ui = await("sem corações") { vm.ui.value as? SessionUi.NoHearts }
        assertEquals(false, ui.endedMidLesson)
        // sem erros guardados -> mensagem vazia
        val vm2 = newVm("errors")
        assertTrue(await("vazio") { vm2.ui.value as? SessionUi.Empty } is SessionUi.Empty)
    }

    @Test fun lojaMissoesBauDiario() {
        runBlocking { progress.ensureReady(today()) }
        assertEquals(PurchaseResult.NotEnoughGems(200), runBlocking { progress.buy(ShopItem.StreakFreeze) })
        assertEquals(PurchaseResult.AlreadyFull, runBlocking { progress.buy(ShopItem.RefillHearts) })
        val g = runBlocking { progress.claimDailyChest(today()) }
        assertNotNull(g); assertNull(runBlocking { progress.claimDailyChest(today()) })
        assertEquals(g, snap().stats.gems)
        // junta gemas jogando
        repeat(4) { i -> play(newVm("lesson", "u1l${i + 1}")) }
        val s = snap()
        assertEquals(4, s.stats.lessonsCompleted)
        assertTrue("gems=${s.stats.gems}", s.stats.gems in 60..300)
        runBlocking { db.progressDao().upsertStats(db.progressDao().stats()!!.copy(gems = 500)) }
        assertTrue(runBlocking { progress.buy(ShopItem.StreakFreeze) } is PurchaseResult.Success)
        assertEquals(300, snap().stats.gems)
        assertEquals(1, snap().stats.streak.freezes)
        // resgate de missão concluída
        val q = runBlocking { progress.observeQuests(today()).first() }
        val done = q.firstOrNull { br.com.trilhati.domain.quest.QuestEngine.byId(it.questId)?.let { d -> br.com.trilhati.domain.quest.QuestEngine.isComplete(d, it.progress) } == true }
        assertNotNull("alguma missão deveria estar completa após 4 lições", done)
        assertNotNull(runBlocking { progress.claimQuest(today(), done!!.questId) })
        assertNull(runBlocking { progress.claimQuest(today(), done!!.questId) })
    }

    @Test fun unidadeCompletaAbreBauEUmaVez() {
        runBlocking { progress.ensureReady(today()) }
        repeat(5) { i -> play(newVm("lesson", "u1l${i + 1}")) }
        val s = snap()
        assertTrue("unit1" in s.achievements)
        assertEquals(1, TrailEngine.completedUnitCount(content.course, s.lessonProgress))
        assertEquals(60, runBlocking { progress.claimUnitChest("u1") })
        assertNull(runBlocking { progress.claimUnitChest("u1") })
        assertEquals(LessonStatus.Locked, TrailEngine.statuses(content.course, s.lessonProgress)["u2l1"]) // u2 sem conteúdo
    }

    @Test fun ofensivaPerdidaEConsomeCongelamento() {
        runBlocking { progress.ensureReady(today()) }
        play(newVm("lesson", "u1l1"))
        assertEquals(1, snap().stats.streak.current)
        // compra congelamento via gemas fictícias: injeta direto
        runBlocking { db.progressDao().upsertStats(db.progressDao().stats()!!.copy(freezes = 1, gems = 500)) }
        clock.instant = clock.instant.plusSeconds(2 * 24 * 3600) // pula 1 dia inteiro (sexta)
        runBlocking { progress.ensureReady(today()) }
        val s = snap()
        assertEquals(1, s.stats.streak.current); assertEquals(0, s.stats.streak.freezes)
        assertTrue(s.dailyLogs.values.any { it.froze })
        // dois dias depois sem congelamento: perde
        clock.instant = clock.instant.plusSeconds(3 * 24 * 3600)
        runBlocking { progress.ensureReady(today()) }
        assertEquals(0, snap().stats.streak.current)
    }

    @Test fun associarParesComParErradoCorrigidoNaoCustaCoracaoMasVaiParaRevisao() {
        runBlocking { progress.ensureReady(today()) }
        val feedbacks = mutableListOf<Pair<QuestionType, Phase.Feedback>>()
        val fin = play(newVm("lesson", "u1l1"), slipOnMatch = true, feedbacks = feedbacks)
        val match = feedbacks.first { it.first == QuestionType.Match }.second
        assertTrue("deveria ser corrigida", match.corrected)
        assertEquals(1, match.slips); assertEquals(5, match.xp); assertNull(match.correctText)
        assertEquals("nada volta para o fim", 8, fin.run.records.size)
        assertFalse(fin.run.perfect)
        val s = snap()
        assertEquals("sem perder coração", 5, s.stats.hearts.hearts)
        assertEquals("vai para a revisão", 1, s.errorBankSize)
        assertFalse(s.lessonProgress.getValue("u1l1").perfect)
    }

    @Test fun treinoTambemEntraNaFilaComoPractice() {
        runBlocking { progress.ensureReady(today()) }
        play(newVm("lesson", "u1l2"), wrongOnFirst = 1)
        play(newVm("errors"))
        val kinds = runBlocking { db.progressDao().outbox(10) }.map { it.kind }.sorted()
        assertEquals(listOf("lesson", "practice"), kinds)
    }

    @Test fun apagarTudoRecomeca() {
        runBlocking { progress.ensureReady(today()) }
        play(newVm("lesson", "u1l1"))
        runBlocking { progress.eraseAll(); progress.ensureReady(today()) }
        val s = snap()
        assertEquals(0, s.stats.totalXp); assertTrue(s.lessonProgress.isEmpty())
    }

    @Test fun backupIdaEVoltaRestauraTudo() {
        runBlocking { progress.ensureReady(today()) }
        play(newVm("lesson", "u1l1"))
        play(newVm("lesson", "u1l2"), wrongOnFirst = 1)
        val before = snap()
        val text = br.com.trilhati.data.backup.BackupCodec.encode(runBlocking { progress.exportBackup() })
        runBlocking { progress.eraseAll(); progress.ensureReady(today()) }
        assertEquals(0, snap().stats.totalXp)
        val read = br.com.trilhati.data.backup.BackupCodec.decode(text) as br.com.trilhati.data.backup.BackupRead.Ok
        runBlocking { progress.importBackup(read.backup, today()) }
        val after = snap()
        assertEquals(before.stats, after.stats)
        assertEquals(before.lessonProgress, after.lessonProgress)
        assertEquals(before.questionStats, after.questionStats)
        assertEquals(before.dailyLogs, after.dailyLogs)
        assertEquals(before.achievements, after.achievements)
        assertEquals(before.errorBankSize, after.errorBankSize)
    }

    @Test fun importarPacoteDeConteudoEntraNaTrilha() {
        // Simula um pacote da Unidade 2 trocando os ids do pacote da Unidade 1.
        val u1 = java.io.File("../../../app/src/main/assets/content/u1-banco-de-dados.json").readText()
        val u2 = u1.replace("\"u1l", "\"u2l").replace("\"id\": \"u1\"", "\"id\": \"u2\"").replace("\"order\": 1,\n  \"title\": \"Banco de dados\"", "\"order\": 2,\n  \"title\": \"Programação (teste)\"")
            .replace("\"pack\": \"u1-banco-de-dados\"", "\"pack\": \"u2-teste\"")
        val result = content.importPack(u2)
        assertTrue("$result", result is br.com.trilhati.data.content.PackLoad.Ok)
        assertTrue(content.course[1].hasContent)
        assertEquals(1, content.importedPacks().size)
        assertTrue(content.importPack("{ quebrado") is br.com.trilhati.data.content.PackLoad.Invalid)
        content.removeImported(content.importedPacks().first().fileName)
        assertFalse(content.course[1].hasContent)
    }

    @Test fun notificacaoDeLembreteAparece() {
        org.robolectric.Shadows.shadowOf(context as android.app.Application).grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS)
        val n = br.com.trilhati.notify.Notifications(context)
        n.createChannels()
        n.post(br.com.trilhati.domain.notify.ReminderTexts.risk("Preguica", 0, 7))
        val manager = context.getSystemService(android.app.NotificationManager::class.java)
        val posted = shadowOf(manager).allNotifications
        assertEquals(1, posted.size)
        assertTrue(posted[0].extras.getString(android.app.Notification.EXTRA_TEXT)!!.contains("7 dias"))
        n.clear()
        assertEquals(0, shadowOf(manager).allNotifications.size)
    }

    @Test fun perfilPersiste() {
        runBlocking {
            progress.ensureReady(today())
            progress.setCharacter("Tartaruga"); progress.setName("  Carlos  "); progress.setDailyGoal(200)
        }
        val p = snap().profile
        assertEquals("Tartaruga", p.character); assertEquals("Carlos", p.name); assertEquals(200, p.dailyGoalXp)
    }
}
