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
import br.com.trilhati.domain.exam.ExamPart
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
        // O DataStore fica em memória durante toda a execução: um teste que liga uma opção não pode afetar os seguintes.
        runBlocking { settings.eraseAll() }
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
            br.com.trilhati.data.social.SyncScheduler(context), br.com.trilhati.notify.ExamFocus(context))

    /** Joga a sessão inteira. [wrongOnFirst] é quantas questões errar de propósito (as primeiras de múltipla escolha). */
    /**
     * Joga a sessão inteira. [wrongOnFirst] é quantas questões errar de propósito (as primeiras de múltipla escolha).
     * [dontKnowOn]: em cada exercício (fora a repetição do fim), decide se aperta "Não sei"; [helpSeen] recebe, para cada
     * exercício, se o botão estava disponível.
     */
    private fun play(
        vm: SessionViewModel,
        wrongOnFirst: Int = 0,
        wrongOnFirstExam: Int = 0, // erra as primeiras N questões de prova (as de ensino vêm antes na lição)
        slipOnMatch: Boolean = false,
        feedbacks: MutableList<Pair<QuestionType, Phase.Feedback>>? = null,
        dontKnowOn: (br.com.trilhati.domain.model.Question) -> Boolean = { false },
        helpSeen: MutableList<Pair<br.com.trilhati.domain.model.Question, Boolean>>? = null,
    ): SessionUi.Finished {
        var wrongLeft = wrongOnFirst
        var examWrongLeft = wrongOnFirstExam
        var slip = slipOnMatch
        var guard = 0
        while (guard++ < 80) {
            val ui = await("próximo estado") { vm.ui.value.takeIf { it !is SessionUi.Loading && it !is SessionUi.Saving } }
            when (ui) {
                is SessionUi.Finished -> return ui
                is SessionUi.Playing -> {
                    if (ui.phase is Phase.Feedback) { vm.continueAfterFeedback(); idle(); continue }
                    val q = ui.question
                    if (!ui.retry) helpSeen?.add(q to ui.canAskHelp)
                    if (!ui.retry && dontKnowOn(q)) {
                        vm.dontKnow(); idle()
                        val fb = await("feedback do Não sei") { (vm.ui.value as? SessionUi.Playing)?.phase as? Phase.Feedback }
                        feedbacks?.add(q.type to fb)
                        continue
                    }
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
                            val examWrong = examWrongLeft > 0 && q.level == br.com.trilhati.domain.model.QuestionLevel.Exam
                            val goWrong = !ui.retry && (wrongLeft > 0 || examWrong)
                            if (goWrong) { if (wrongLeft > 0) wrongLeft-- else examWrongLeft-- }
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

    @Test fun provaEscolhidaMudaTrilhaRitmoEProvasAnteriores() {
        val tb = br.com.trilhati.domain.exam.TargetExam.TecnicoBancario
        val ti = br.com.trilhati.domain.exam.TargetExam.AnalistaTi
        runBlocking {
            assertEquals(ti, settings.settings.first().targetExam)
            assertEquals(180_000L, settings.settings.first().paceConfig.perQuestionMs)
            settings.setTargetExam(tb)
            val s = settings.settings.first()
            assertEquals(tb, s.targetExam)
            assertEquals(205_000L, s.paceConfig.perQuestionMs) // 3:25
            // 2:30 escolhido nas Configurações vale nas duas provas; voltar ao padrão segue a prova de novo
            settings.setPaceTarget(br.com.trilhati.domain.pace.PaceConfig.TIGHT_MS)
            assertEquals(150_000L, settings.settings.first().paceConfig.perQuestionMs)
            settings.setPaceTarget(null)
            assertEquals(205_000L, settings.settings.first().paceConfig.perQuestionMs)
        }
        // Trilha do Técnico: Específicos dele ainda sem conteúdo, Gerais iguais aos do Analista (com o Português pronto)
        val course = content.course(tb)
        val specific = course.filter { it.track == br.com.trilhati.domain.model.Track.Specific }
        assertEquals(listOf("u16", "u17", "u15", "u18", "u19"), specific.map { it.id })
        assertTrue(specific.none { it.hasContent })
        val general = course.filter { it.track == br.com.trilhati.domain.model.Track.General }
        assertEquals(listOf("u11", "u12", "u13", "u14"), general.map { it.id })
        assertEquals(content.course(ti).first { it.id == "u11" }.lessons, general.first().lessons)
        // Provas anteriores: o Técnico vê só as do Banestes (as que têm Gerais), só a parte dos Gerais
        // Provas anteriores (0.14.0): as de Técnico Bancário vêm inteiras para o Técnico; as de TI, só com os Gerais.
        val forTb = content.exams(tb)
        val own = forTb.filter { br.com.trilhati.domain.exam.TargetExam.forCargo(it.cargo) == tb }
        assertEquals(listOf("fgv-banestes-2023", "fgv-banestes-2018-tecnico-bancario"), own.map { it.id })
        assertEquals(listOf(63, 72), own.map { it.size }) // sem anuladas, imagens, versões antigas, fora do edital e repetida
        assertEquals(listOf(ExamPart.Full, ExamPart.Specific, ExamPart.General), own.first().parts)
        val others = forTb - own.toSet()
        assertTrue(others.isNotEmpty() && others.all { it.specific.isEmpty() && "Banestes" in it.body })
        assertNull(content.exam("fcc-dpe-rr-2015", tb))
        // O Analista vê as de Técnico só com os Gerais dele, inclusive a matemática financeira (G5)
        val tbForTi = content.exam("fgv-banestes-2018-tecnico-bancario", ti)!!
        assertTrue(tbForTi.specific.isEmpty())
        assertTrue(tbForTi.general.any { it.lessonId.startsWith("u15") } && tbForTi.general.none { it.lessonId.startsWith("u16") })
    }

    @Test fun perguntaDaProvaSoParaQuemEstaComecando() = runBlocking {
        // Quem já usava (tema escolhido antes da 0.13.0) não vê a pergunta.
        assertFalse(settings.settings.first().examChoicePending)
        settings.confirmThemeChoice(br.com.trilhati.ui.theme.ThemePalette.Claro)
        assertTrue(settings.settings.first().examChoicePending)
        // trocar o tema depois não pergunta de novo
        settings.setTargetExam(br.com.trilhati.domain.exam.TargetExam.TecnicoBancario)
        assertFalse(settings.settings.first().examChoicePending)
        settings.confirmThemeChoice(br.com.trilhati.ui.theme.ThemePalette.Escuro)
        assertFalse(settings.settings.first().examChoicePending)
        // o 3:00 gravado pelas versões antigas vira "segue a prova"
        settings.eraseAll()
        settings.setPaceTarget(br.com.trilhati.domain.pace.PaceConfig.STANDARD_MS)
        assertNull(settings.settings.first().paceTargetMs)
    }

    @Test fun conteudoCarregaDosAssets() {
        assertEquals(15, content.course(br.com.trilhati.domain.exam.TargetExam.AnalistaTi).size) // 10 Específicos + 5 Gerais (G2 dividida)
        // 170 exercícios do pacote da unidade 1 (as 5 lições no formato por conceitos) + 36 de provas anteriores
        // (26 da FCC e da FGV, 6 da IDECAN 2012 e 4 do Instituto Access 2024).
        val u1 = content.course(br.com.trilhati.domain.exam.TargetExam.AnalistaTi)[0].lessons.flatMap { it.questions }
        assertEquals(206, u1.size)
        assertEquals(36, u1.count { it.isFromExam })
        // U2 (154 + 35 oficiais) e G1 (168 + 55 oficiais), com as provas de 2012 e 2024 da 0.11.0; e mais 30 oficiais
        // de Português das provas de Técnico Bancário de 2018 e 2023 (0.14.0).
        assertEquals(189, content.course(br.com.trilhati.domain.exam.TargetExam.AnalistaTi)[1].lessons.sumOf { it.questions.size })
        assertEquals(253, content.course(br.com.trilhati.domain.exam.TargetExam.AnalistaTi)[10].lessons.sumOf { it.questions.size })
        // Nas lições por conceito, toda questão de prova está ligada a um conceito da lição: sem isso o planejador
        // nunca a sorteia (aconteceu com as 20 oficiais das lições 2 a 5 na 0.8.1).
        content.course(br.com.trilhati.domain.exam.TargetExam.AnalistaTi).flatMap { it.lessons }.forEach { l ->
            val ids = l.concepts.map { it.id }.toSet()
            if (ids.isNotEmpty()) l.questions.filter { it.isFromExam }.forEach { q -> assertTrue("${q.id} sem conceito da ${l.id}", q.conceptId in ids) }
        }
        assertNotNull(content.lesson("u1l3")); assertNotNull(content.question("u1l1q01"))
        assertNotNull(content.question("u2l4q05")); assertNotNull(content.question("u11l2q01"))
        assertEquals("FGV · Banestes 2021 · questão 48", content.question("fgv-banestes-2021-q48")?.ref?.label)
        // Unidade sem pacote continua "chegando", mesmo tendo questões de prova esperando por ela.
        assertFalse(content.lesson("u3l1")!!.hasContent)
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
        // u1l1 está no formato por conceitos: 12 exercícios, do aprender para a prova.
        val n = br.com.trilhati.domain.lesson.LessonPlanner.SIZE
        assertEquals(n, s.stats.firstTryAnswered); assertEquals(n, s.stats.firstTryCorrect)
        // Primeira vez: blocos dos 3 primeiros conceitos da lição, na ordem, e a prova no fim.
        val levels = fin.run.records.map { it.question.level }
        assertTrue(levels.take(9).none { it == br.com.trilhati.domain.model.QuestionLevel.Exam })
        assertTrue(levels.drop(9).all { it == br.com.trilhati.domain.model.QuestionLevel.Exam })
        assertEquals(listOf("u1l1c1", "u1l1c2", "u1l1c3"), fin.run.records.take(9).map { it.question.conceptId }.distinct())
        assertEquals(fin.processed.summary.totalXp, s.stats.totalXp)
        assertTrue(s.stats.gems >= 10)
        // a sessão entrou na fila para o ranking (sai quando houver rede)
        val outbox = runBlocking { db.progressDao().outbox(10) }
        assertEquals(1, outbox.size)
        assertEquals("lesson", outbox[0].kind); assertEquals(s.stats.totalXp, outbox[0].xp); assertEquals(n, outbox[0].answers)
        assertEquals(today().toEpochDay(), outbox[0].day); assertEquals(1, outbox[0].streak)
        assertEquals(1, s.dailyLogs.getValue(today()).lessons)
        // desbloqueou a próxima
        val st = TrailEngine.statuses(content.course(br.com.trilhati.domain.exam.TargetExam.AnalistaTi), s.lessonProgress)
        assertEquals(LessonStatus.Perfect, st["u1l1"]); assertEquals(LessonStatus.Available, st["u1l2"])
        assertEquals(LessonStatus.Locked, st["u1l3"])
        // missões avançaram
        val quests = runBlocking { progress.observeQuests(today()).first() }
        assertTrue(quests.any { it.progress > 0 })
    }

    @Test fun errosCustamCoracaoViramBancoDeErrosETreinoDevolveCoracao() {
        runBlocking { progress.ensureReady(today()) }
        val fin = play(newVm("lesson", "u1l2"), wrongOnFirstExam = 2)
        assertFalse(fin.run.perfect)
        var s = snap()
        assertEquals(3, s.stats.hearts.hearts) // 2 erros de primeira em questão de prova = 2 corações
        assertEquals(2, s.errorBankSize)
        assertEquals(0, s.dueCount) // revisão só amanhã
        // Amanhã os 2 erros vencem: a revisão deles devolve 1 coração e dá XP.
        clock.instant = clock.instant.plusSeconds(24 * 3600)
        runBlocking { progress.ensureReady(today()); progress.loseHeart(4) } // em 24 h os corações voltaram; fica com 3
        assertEquals(2, snap().dueCount)
        val xpBefore = snap().stats.totalXp
        val practice = play(newVm("errors"))
        assertTrue(practice.rewarded)
        assertEquals(2, practice.run.records.size)
        s = snap()
        assertEquals(4, s.stats.hearts.hearts)
        assertEquals(1, s.stats.practiceSessions)
        assertTrue(s.stats.totalXp > xpBefore)
        // acertou na revisão: ela sobe para o intervalo de 3 dias. Amanhã nada vence; em 3 dias, vence.
        assertEquals(0, s.dueCount)
        assertEquals(2, s.errorBankSize) // continua guardado até acertar as 4 revisões
        clock.instant = clock.instant.plusSeconds(2 * 24 * 3600)
        runBlocking { progress.ensureReady(today()) }
        assertEquals(0, snap().dueCount)
        clock.instant = clock.instant.plusSeconds(24 * 3600)
        runBlocking { progress.ensureReady(today()) }
        assertEquals(2, snap().dueCount)
    }

    /** Bug relatado por um amigo do dono (0.10.1): dava para repetir o treino sem fim e ganhar XP e corações. */
    @Test fun treinoForaDoPrazoERevisaoLivreNaoDaoXpNemCoracaoENaoAdiantamARevisao() {
        runBlocking { progress.ensureReady(today()); progress.loseHeart(4) } // 3 corações
        play(newVm("lesson", "u1l2"), wrongOnFirstExam = 2) // 2 erros (vencem amanhã) = 1 coração
        val before = snap()
        val cards = before.questionStats.values.filter { it.card.active }.associate { it.questionId to it.card }
        assertEquals(2, cards.size)
        repeat(3) { round ->
            val free = play(newVm("errors")) // nada vencido: treino livre com os mais antigos
            assertFalse("rodada $round", free.rewarded)
            assertEquals("Treino livre", free.title)
            assertEquals(0, free.processed.summary.totalXp)
            assertTrue(free.run.records.all { it.xp == 0 })
        }
        val review = play(newVm("review"))
        assertFalse(review.rewarded)
        assertEquals(0, review.processed.summary.totalXp)
        val after = snap()
        assertEquals(before.stats.totalXp, after.stats.totalXp)
        assertEquals(before.stats.hearts.halves, after.stats.hearts.halves) // nenhum coração ganho
        assertEquals(before.stats.practiceSessions, after.stats.practiceSessions)
        assertEquals(cards, after.questionStats.filterKeys { it in cards }.mapValues { it.value.card }) // revisão não adiantou
        assertEquals(1, runBlocking { db.progressDao().outbox(10) }.size) // só a lição vai para o ranking
        // Sem erro vencido, a tela "sem corações" não oferece o treino que não devolveria coração.
        runBlocking { progress.loseHeart(10) }
        val blocked = newVm("lesson", "u1l1").let { vm -> await("sem corações") { vm.ui.value as? SessionUi.NoHearts } }
        assertEquals(0, blocked.dueErrors)
        clock.instant = clock.instant.plusSeconds(24 * 3600)
        runBlocking { progress.ensureReady(today()); progress.loseHeart(10) } // em 24 h os corações voltariam
        val tomorrow = newVm("lesson", "u1l1").let { vm -> await("sem corações") { vm.ui.value as? SessionUi.NoHearts } }
        assertEquals(2, tomorrow.dueErrors)
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
        assertEquals(1, TrailEngine.completedUnitCount(content.course(br.com.trilhati.domain.exam.TargetExam.AnalistaTi), s.lessonProgress))
        assertEquals(60, runBlocking { progress.claimUnitChest("u1") })
        assertNull(runBlocking { progress.claimUnitChest("u1") })
        val st = TrailEngine.statuses(content.course(br.com.trilhati.domain.exam.TargetExam.AnalistaTi), s.lessonProgress)
        assertEquals(LessonStatus.Available, st["u2l1"]) // a U2 tem pacote desde a 0.10.0
        assertEquals(LessonStatus.Locked, st["u3l1"]) // u3 sem conteúdo
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
        assertEquals("nada volta para o fim", br.com.trilhati.domain.lesson.LessonPlanner.SIZE, fin.run.records.size)
        assertFalse(fin.run.perfect)
        val s = snap()
        assertEquals("sem perder coração", 5, s.stats.hearts.hearts)
        assertEquals("vai para a revisão", 1, s.errorBankSize)
        assertFalse(s.lessonProgress.getValue("u1l1").perfect)
    }

    @Test fun naoSeiGastaUmUsoNaoCustaCoracaoEVoltaNoFim() {
        runBlocking { progress.ensureReady(today()) }
        assertEquals(3, snap().stats.help.count)
        val vm = newVm("lesson", "u1l1")
        val first = await("primeiro exercício") { vm.ui.value as? SessionUi.Playing }
        assertTrue(first.canAskHelp); assertEquals(3, first.helps)
        vm.dontKnow(); idle()
        val fb = await("feedback do Não sei") { (vm.ui.value as? SessionUi.Playing)?.phase as? Phase.Feedback }
        assertTrue(fb.skipped); assertEquals(0, fb.xp); assertNotNull(fb.correctText)
        val fin = play(vm)
        // O exercício voltou no fim: 12 + 1.
        assertEquals(br.com.trilhati.domain.lesson.LessonPlanner.SIZE + 1, fin.run.records.size)
        val s = snap()
        assertEquals(2, s.stats.help.count)
        assertEquals("sem perder coração", 10, s.stats.hearts.halves)
        assertEquals(1, s.errorBankSize)
    }

    @Test fun naoSeiFuncionaEmTodoTipoDeEnsinoENaoApareceNaProva() {
        runBlocking { progress.ensureReady(today()); db.progressDao().upsertStats(db.progressDao().stats()!!.copy(helps = 9)) }
        val seen = mutableListOf<Pair<br.com.trilhati.domain.model.Question, Boolean>>()
        val feedbacks = mutableListOf<Pair<QuestionType, Phase.Feedback>>()
        val teaching = { q: br.com.trilhati.domain.model.Question -> q.level != br.com.trilhati.domain.model.QuestionLevel.Exam }
        // Três sessões da u1l1 passam pelos 7 conceitos. Repõe o estoque antes de cada uma.
        repeat(3) {
            runBlocking { db.progressDao().upsertStats(db.progressDao().stats()!!.copy(helps = 9)) }
            val before = snap().stats.hearts.halves
            val fin = play(newVm("lesson", "u1l1"), feedbacks = feedbacks, dontKnowOn = teaching, helpSeen = seen)
            // Nenhum coração perdido, e cada "Não sei" voltou no fim (12 + 9 respostas).
            assertEquals(before, snap().stats.hearts.halves)
            assertEquals(br.com.trilhati.domain.lesson.LessonPlanner.SIZE + 9, fin.run.records.size)
            assertEquals(0, snap().stats.help.count)
        }
        // O botão aparece em todo exercício de aprender e praticar e em nenhuma questão de prova.
        assertTrue(seen.filter { teaching(it.first) }.all { it.second })
        assertTrue(seen.filterNot { teaching(it.first) }.none { it.second })
        assertTrue(seen.any { it.first.level == br.com.trilhati.domain.model.QuestionLevel.Learn })
        assertTrue(seen.any { it.first.level == br.com.trilhati.domain.model.QuestionLevel.Practice })
        // Funciona em todos os tipos de ensino da lição: escolha rápida, certo/errado, associar, completar e ordenar.
        val skippedTypes = feedbacks.filter { it.second.skipped }.map { it.first }.toSet()
        assertEquals(
            setOf(QuestionType.Quick, QuestionType.TrueFalse, QuestionType.Match, QuestionType.FillBlank, QuestionType.OrderCode),
            skippedTypes,
        )
        assertTrue(feedbacks.filter { it.second.skipped }.all { it.second.xp == 0 && it.second.correctText != null })
        // Os pulados foram para a revisão; no "Praticar erros" o botão também funciona.
        assertTrue(snap().errorBankSize > 0)
        runBlocking { db.progressDao().upsertStats(db.progressDao().stats()!!.copy(helps = 9)) }
        val practiceSeen = mutableListOf<Pair<br.com.trilhati.domain.model.Question, Boolean>>()
        play(newVm("errors"), dontKnowOn = { teaching(it) && practiceSeen.count { p -> p.second } <= 1 }, helpSeen = practiceSeen)
        assertTrue(practiceSeen.filter { teaching(it.first) }.all { it.second })
    }

    @Test fun naoSeiSemEstoqueNaoFazNada() {
        runBlocking { progress.ensureReady(today()); db.progressDao().upsertStats(db.progressDao().stats()!!.copy(helps = 0)) }
        val vm = newVm("lesson", "u1l1")
        val first = await("primeiro exercício") { vm.ui.value as? SessionUi.Playing }
        assertEquals(0, first.helps)
        vm.dontKnow(); idle()
        assertEquals(Phase.Answering, (vm.ui.value as SessionUi.Playing).phase)
        assertEquals(10, snap().stats.hearts.halves)
    }

    @Test fun erroDeEnsinoCustaMeioCoracaoEDeProvaUmInteiro() {
        runBlocking { progress.ensureReady(today()) }
        play(newVm("lesson", "u1l1"), wrongOnFirst = 1) // o primeiro da u1l1 é de aprender
        assertEquals(9, snap().stats.hearts.halves)
        assertEquals("4 corações e meio", br.com.trilhati.domain.hearts.HeartsEngine.description(snap().stats.hearts))
        assertEquals("4,5", br.com.trilhati.domain.hearts.HeartsEngine.display(snap().stats.hearts))
        play(newVm("lesson", "u1l2"), wrongOnFirstExam = 1) // erra a primeira questão de prova da u1l2
        assertEquals(7, snap().stats.hearts.halves)
    }

    @Test fun lojaVendeNaoSei() {
        runBlocking { progress.ensureReady(today()); db.progressDao().upsertStats(db.progressDao().stats()!!.copy(gems = 120)) }
        assertTrue(runBlocking { progress.buy(br.com.trilhati.domain.progress.ShopItem.Help) } is PurchaseResult.Success)
        assertEquals(4, snap().stats.help.count); assertEquals(70, snap().stats.gems)
    }

    // ------------------------------------------------------------------ provas anteriores (aba Praticar)

    private fun halves() = br.com.trilhati.domain.hearts.HeartsEngine.regen(snap().stats.hearts, clock.millis()).halves
    private fun intro(vm: SessionViewModel) = await("aviso da prova") { vm.ui.value as? SessionUi.ExamIntro }
    private val notificationManager get() = context.getSystemService(android.app.NotificationManager::class.java)

    @Test fun provasDoPacoteViramProvasAnterioresQueCabemNoServidor() {
        val exams = content.exams
        assertEquals(
            listOf(
                "instituto-access-banestes-2024", "fgv-banestes-2023", "fgv-banestes-2021", "fgv-banestes-2018",
                "fgv-banestes-2018-tecnico-bancario", "fcc-prefeitura-de-teresina-2016", "fcc-dpe-rr-2015", "idecan-banestes-2012",
            ),
            exams.map { it.id },
        )
        assertEquals(listOf(74, 63, 57, 87, 72, 35, 26, 47), exams.map { it.size })
        exams.forEach { e ->
            assertTrue("${e.id} cabe no limite do servidor", e.size <= br.com.trilhati.domain.social.SessionLimits.EXAM_MAX_ANSWERS)
            assertEquals(e.size, e.specific.size + e.general.size)
            assertTrue(e.questions(br.com.trilhati.domain.exam.ExamPart.Full).zipWithNext().all { (a, b) -> a.ref!!.number < b.ref!!.number })
        }
        fun byId(id: String) = exams.first { it.id == id }
        assertEquals(37 to 50, byId("fgv-banestes-2018").specific.size to byId("fgv-banestes-2018").general.size)
        assertEquals(29 to 45, exams[0].specific.size to exams[0].general.size) // Banestes 2024: 4 alternativas
        // Técnico Bancário: matemática financeira, informática, bancários e vendas são Específicos; Português,
        // Raciocínio e o Guia de Conduta Ética, Gerais.
        val tb18 = byId("fgv-banestes-2018-tecnico-bancario")
        assertTrue(tb18.specific.all { it.lessonId.substringBefore('l') in setOf("u15", "u16", "u17") })
        assertTrue(tb18.general.all { it.lessonId.substringBefore('l') in setOf("u11", "u12", "u14") })
        assertEquals(3, byId("fgv-banestes-2018").parts.size)
        assertEquals(1, byId("fcc-dpe-rr-2015").parts.size)
        assertTrue(exams[0].questions(br.com.trilhati.domain.exam.ExamPart.Full).all { it.options.size == 4 })
    }

    @Test fun provaAvisaAntesGastaUmCoracaoAoComecarNaoTiraCoracaoPorErroEDaXpSoAoTerminar() {
        runBlocking { progress.ensureReady(today()) }
        val exam = content.exam("fcc-dpe-rr-2015", br.com.trilhati.domain.exam.TargetExam.AnalistaTi)!!
        val vm = newVm("exam", exam.id)
        val ui = intro(vm)
        assertEquals(exam.size, ui.questionCount)
        assertEquals(2, ui.costHalves)
        assertTrue(ui.canOpen)
        assertEquals(exam.size * 180_000L, ui.estimatedMs)
        assertEquals(10, halves()) // o aviso não gasta nada
        vm.beginExam(); idle()
        val first = await("primeira questão") { vm.ui.value as? SessionUi.Playing }
        assertTrue(first.exam)
        assertNull(first.hearts) // sem contador de corações: errar não custa
        assertEquals(8, halves())
        val done = play(vm, wrongOnFirst = 5)
        assertEquals(exam.size, done.run.records.size) // os erros não voltam no fim
        assertEquals(8, halves()) // errar não tirou coração e terminar não devolveu
        val xp = (exam.size - 5) * 15 + 20 // relógio parado: todos os acertos são rápidos; sem o bônus de gabarito
        assertEquals(xp, done.processed.summary.totalXp)
        assertEquals(xp, snap().stats.totalXp)
        assertEquals(1, snap().stats.streak.current) // conta como estudo do dia
        val sent = runBlocking { db.progressDao().outbox(10) }.single()
        assertEquals("exam", sent.kind)
        assertEquals(exam.size, sent.answers)
        assertEquals(xp, sent.xp)
        // os erros da prova (de unidades ainda sem lição) vão para Praticar erros
        val practice = play(newVm("errors"))
        assertEquals(5, practice.run.firstTryRecords.size)
        assertTrue(practice.run.records.all { it.question.ref?.body == "DPE-RR" && it.question.ref?.year == 2015 })
    }

    @Test fun abandonarAProvaNaoDaXpEOCoracaoNaoVolta() {
        runBlocking { progress.ensureReady(today()) }
        val vm = newVm("exam", "fcc-dpe-rr-2015")
        intro(vm)
        vm.beginExam(); idle()
        repeat(3) {
            val ui = await("questão") { (vm.ui.value as? SessionUi.Playing)?.takeIf { it.phase == Phase.Answering } }
            vm.select(ui.question.answer); idle(); vm.check(); idle()
            await("feedback") { (vm.ui.value as? SessionUi.Playing)?.phase as? Phase.Feedback }
            vm.continueAfterFeedback(); idle()
        }
        // o aluno sai aqui: nada foi gravado além do coração gasto para abrir
        assertEquals(0, snap().stats.totalXp)
        assertEquals(8, halves())
        assertEquals(0, snap().stats.streak.current)
        assertTrue(runBlocking { db.progressDao().outbox(10) }.isEmpty())
    }

    @Test fun semCoracaoInteiroNaoAbreAProva() {
        runBlocking { progress.ensureReady(today()); progress.loseHeart(9) } // sobra meio coração
        val vm = newVm("exam", "fgv-banestes-2021")
        assertFalse(intro(vm).canOpen)
        vm.beginExam(); idle()
        val blocked = await("sem corações") { vm.ui.value as? SessionUi.NoHearts }
        assertTrue(blocked.forExam)
        assertEquals(1, halves()) // não gastou o meio coração
    }

    @Test fun modoSemVidasAbreAProvaDeGraca() {
        runBlocking { progress.ensureReady(today()); settings.setNoLives(true); progress.loseHeart(10) }
        val vm = newVm("exam", "fcc-dpe-rr-2015")
        val ui = intro(vm)
        assertEquals(0, ui.costHalves)
        assertTrue(ui.canOpen)
        vm.beginExam(); idle()
        await("primeira questão") { vm.ui.value as? SessionUi.Playing }
        assertEquals(0, halves())
    }

    @Test fun provaComGeraisDeixaEscolherAParte() {
        runBlocking { progress.ensureReady(today()) }
        val vm = newVm("exam", "fgv-banestes-2018")
        val ui = intro(vm)
        assertEquals(br.com.trilhati.domain.exam.ExamPart.entries.toList(), ui.parts)
        assertEquals(listOf(87, 37, 50), ui.partSizes)
        vm.pickExamPart(br.com.trilhati.domain.exam.ExamPart.Specific); idle()
        assertEquals(37, intro(vm).questionCount)
        vm.beginExam(); idle()
        val playing = await("primeira questão") { vm.ui.value as? SessionUi.Playing }
        assertEquals("FGV · Banestes 2018 · Específicos", playing.title)
        val done = play(vm)
        assertEquals(37, done.run.records.size)
        assertTrue(done.run.records.all { it.question.lessonId.removePrefix("u").substringBefore("l").toInt() <= 10 })
        assertEquals(37 * 15 + 30, done.processed.summary.totalXp) // gabaritou: + bônus de perfeição
    }

    @Test fun silenciarLigaONaoPerturbeSoDuranteAProva() {
        runBlocking { progress.ensureReady(today()) }
        val nm = notificationManager
        shadowOf(nm).setNotificationPolicyAccessGranted(true)
        nm.setInterruptionFilter(android.app.NotificationManager.INTERRUPTION_FILTER_ALL)
        val vm = newVm("exam", "fcc-dpe-rr-2015")
        assertTrue(intro(vm).silenceAllowed)
        vm.setExamSilence(true); idle()
        assertTrue(intro(vm).silence)
        vm.beginExam(); idle()
        await("primeira questão") { vm.ui.value as? SessionUi.Playing }
        assertEquals(android.app.NotificationManager.INTERRUPTION_FILTER_PRIORITY, nm.currentInterruptionFilter)
        play(vm)
        assertEquals(android.app.NotificationManager.INTERRUPTION_FILTER_ALL, nm.currentInterruptionFilter)
        // a escolha fica lembrada (a gravação no DataStore é assíncrona: espera ela terminar)
        await("interruptor gravado") { runBlocking { settings.settings.first() }.examSilence.takeIf { it } }
    }

    @Test fun silencioNaoMexeQuandoONaoPerturbeJaEstavaLigado() {
        runBlocking { progress.ensureReady(today()); settings.setExamSilence(true) }
        val nm = notificationManager
        shadowOf(nm).setNotificationPolicyAccessGranted(true)
        nm.setInterruptionFilter(android.app.NotificationManager.INTERRUPTION_FILTER_ALARMS)
        val vm = newVm("exam", "fcc-dpe-rr-2015")
        intro(vm)
        vm.beginExam(); idle()
        await("primeira questão") { vm.ui.value as? SessionUi.Playing }
        assertEquals(android.app.NotificationManager.INTERRUPTION_FILTER_ALARMS, nm.currentInterruptionFilter)
        play(vm)
        assertEquals(android.app.NotificationManager.INTERRUPTION_FILTER_ALARMS, nm.currentInterruptionFilter)
    }

    @Test fun naoPerturbeDeUmaProvaInterrompidaDesligaAoAbrirOApp() {
        val nm = notificationManager
        shadowOf(nm).setNotificationPolicyAccessGranted(true)
        nm.setInterruptionFilter(android.app.NotificationManager.INTERRUPTION_FILTER_ALL)
        assertTrue(br.com.trilhati.notify.ExamFocus(context).silence())
        assertEquals(android.app.NotificationManager.INTERRUPTION_FILTER_PRIORITY, nm.currentInterruptionFilter)
        // o app foi fechado no meio da prova; ao abrir de novo, o TrilhaApp chama restore()
        br.com.trilhati.notify.ExamFocus(context).restore()
        assertEquals(android.app.NotificationManager.INTERRUPTION_FILTER_ALL, nm.currentInterruptionFilter)
        br.com.trilhati.notify.ExamFocus(context).restore() // de novo não faz nada
        assertEquals(android.app.NotificationManager.INTERRUPTION_FILTER_ALL, nm.currentInterruptionFilter)
    }

    @Test fun treinoTambemEntraNaFilaComoPractice() {
        runBlocking { progress.ensureReady(today()) }
        play(newVm("lesson", "u1l2"), wrongOnFirst = 1)
        clock.instant = clock.instant.plusSeconds(24 * 3600) // o erro vence amanhã
        runBlocking { progress.ensureReady(today()) }
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
        // Simula um pacote da Unidade 3 (ainda sem pacote embutido) trocando os ids do pacote da Unidade 1.
        val u1 = java.io.File("../../../app/src/main/assets/content/u1-banco-de-dados.json").readText()
        val u3 = u1.replace("\"u1l", "\"u3l").replace("\"id\": \"u1\"", "\"id\": \"u3\"").replace("\"order\": 1,\n  \"title\": \"Banco de dados\"", "\"order\": 3,\n  \"title\": \"Engenharia (teste)\"")
            .replace("\"pack\": \"u1-banco-de-dados\"", "\"pack\": \"u3-teste\"")
        val result = content.importPack(u3)
        assertTrue("$result", result is br.com.trilhati.data.content.PackLoad.Ok)
        assertTrue(content.course(br.com.trilhati.domain.exam.TargetExam.AnalistaTi)[2].hasContent)
        assertEquals(1, content.importedPacks().size)
        assertTrue(content.importPack("{ quebrado") is br.com.trilhati.data.content.PackLoad.Invalid)
        content.removeImported(content.importedPacks().first().fileName)
        assertFalse(content.course(br.com.trilhati.domain.exam.TargetExam.AnalistaTi)[2].hasContent)
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
