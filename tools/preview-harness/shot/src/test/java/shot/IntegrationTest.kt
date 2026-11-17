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

    @Test fun conteudoCarregaDosAssets() {
        assertEquals(10, content.course.size)
        // 170 exercícios do pacote da unidade 1 (as 5 lições no formato por conceitos) + 26 de provas anteriores.
        val u1 = content.course[0].lessons.flatMap { it.questions }
        assertEquals(196, u1.size)
        assertEquals(26, u1.count { it.isFromExam })
        // Nas lições por conceito, toda questão de prova está ligada a um conceito da lição: sem isso o planejador
        // nunca a sorteia (aconteceu com as 20 oficiais das lições 2 a 5 na 0.8.1).
        content.course[0].lessons.forEach { l ->
            val ids = l.concepts.map { it.id }.toSet()
            if (ids.isNotEmpty()) l.questions.filter { it.isFromExam }.forEach { q -> assertTrue("${q.id} sem conceito da ${l.id}", q.conceptId in ids) }
        }
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
        val st = TrailEngine.statuses(content.course, s.lessonProgress)
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
