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
        SessionViewModel(SavedStateHandle(mapOf("kind" to kind, "id" to id)), content, progress, settings, clock)

    /** Joga a sessão inteira. [wrongOnFirst] é quantas questões errar de propósito (as primeiras de múltipla escolha). */
    private fun play(vm: SessionViewModel, wrongOnFirst: Int = 0): SessionUi.Finished {
        var wrongLeft = wrongOnFirst
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
                    try { await("feedback") { (vm.ui.value as? SessionUi.Playing)?.phase as? Phase.Feedback } }
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
        assertEquals(0, s.league.tier)
        // idempotente
        runBlocking { progress.ensureReady(today()); progress.ensureReady(today()) }
        assertEquals(3, runBlocking { progress.observeQuests(today()).first() }.size)
    }

    @Test fun conteudoCarregaDosAssets() {
        assertEquals(10, content.course.size)
        assertEquals(50, content.course[0].lessons.sumOf { it.questions.size })
        assertNotNull(content.lesson("u1l3")); assertNotNull(content.question("u1l1q01"))
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
        assertEquals(s.stats.totalXp, s.league.userXp)
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

    @Test fun ligaFechaNaSegundaEPromoveQuemEstudou() {
        runBlocking { progress.ensureReady(today()) }
        play(newVm("lesson", "u1l1"))
        val week1 = snap().league
        assertTrue(week1.userXp > 0)
        // próxima segunda-feira 00:05 em Brasília
        clock.instant = Instant.parse("2026-11-23T03:05:00Z")
        runBlocking { progress.ensureReady(today()) }
        val s = snap()
        assertEquals(java.time.LocalDate.of(2026, 11, 23), s.league.weekStart)
        assertEquals(0, s.league.userXp)
        val last = s.lastLeagueResult!!
        assertEquals(java.time.LocalDate.of(2026, 11, 16), last.weekStart)
        assertTrue("rank=${last.rank}", last.rank in 1..30)
        assertEquals(if (last.rank <= 10) 1 else 0, last.outcome)
        assertEquals(if (last.rank <= 10) 1 else 0, s.league.tier)
    }

    @Test fun apagarTudoRecomeca() {
        runBlocking { progress.ensureReady(today()) }
        play(newVm("lesson", "u1l1"))
        runBlocking { progress.eraseAll(); progress.ensureReady(today()) }
        val s = snap()
        assertEquals(0, s.stats.totalXp); assertTrue(s.lessonProgress.isEmpty())
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
