package br.com.trilhati.domain.progress

import br.com.trilhati.domain.TestData
import br.com.trilhati.domain.hearts.HeartsState
import br.com.trilhati.domain.lesson.LessonRun
import br.com.trilhati.domain.lesson.LessonRunState
import br.com.trilhati.domain.quest.QuestEngine
import br.com.trilhati.domain.streak.StreakState
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressProcessorTest {
    private val today = LocalDate.of(2026, 11, 16)
    private val nowMs = 1_000_000L
    private val units = listOf(TestData.unit(1, 2))

    private fun freshState(goal: Int = 1, stats: Stats = Stats()): ProgressState {
        val quests = QuestEngine.questsFor(today).map { QuestProgress(today, it.id) }
        return ProgressState(
            stats = stats, lessonProgress = emptyMap(), questionStats = emptyMap(),
            todayLog = DailyLog(today), todayQuests = quests, unlockedAchievements = emptySet(),
            dailyGoalLessons = goal,
        )
    }

    private fun play(correctPattern: List<Boolean>, lessonId: String = "u1l1", timeMs: Long = 60_000): Pair<LessonRunState, SessionResult> {
        var run = LessonRun.start(TestData.questions(correctPattern.size, lessonId))
        correctPattern.forEach { run = LessonRun.answer(run, it, timeMs, 180_000) }
        while (!run.finished) run = LessonRun.answer(run, true, timeMs, 180_000)
        val result = SessionResult(SessionKind.Lesson, lessonId, "u1", run.records, run.bestCombo, run.perfect)
        return run to result
    }

    private fun apply(state: ProgressState, result: SessionResult) = ProgressProcessor.apply(
        state, result, today, nowMs,
        completedUnitsAfter = { TrailEngine.completedUnitCount(units, it) },
        completedUnitsBefore = TrailEngine.completedUnitCount(units, state.lessonProgress),
    )

    @Test fun `licao perfeita de 8 soma xp, gemas, ofensiva e desbloqueia`() {
        val (run, result) = play(List(8) { true })
        val out = apply(freshState(), result)
        val s = out.state
        // 8 respostas x 15 + 20 conclusão + 10 perfeita
        assertEquals(8 * 15 + 30, s.stats.totalXp)
        assertEquals(run.xp + 30, out.summary.totalXp)
        assertEquals(ShopEngine.LESSON_GEMS + ShopEngine.PERFECT_GEMS + 20 /* meta de 1 lição atingida */ + gemsFromAchievements(out), s.stats.gems)
        assertEquals(1, s.stats.streak.current)
        assertTrue(out.summary.streakIncreased)
        assertTrue(s.lessonProgress.getValue("u1l1").perfect)
        assertEquals(LessonStatus.Perfect, TrailEngine.statuses(units, s.lessonProgress)["u1l1"])
        assertEquals(LessonStatus.Available, TrailEngine.statuses(units, s.lessonProgress)["u1l2"])
        assertEquals(1, s.stats.lessonsCompleted)
        assertEquals(1, s.stats.perfectLessons)
        assertTrue(out.summary.goalJustMet)
        assertTrue(s.todayLog.goalMet)
    }

    private fun gemsFromAchievements(out: ProcessedSession) = out.summary.newAchievements.sumOf { it.rewardGems }

    @Test fun `erro cria cartao de revisao so com a primeira resposta`() {
        val (_, result) = play(listOf(true, false, true, true))
        val s = apply(freshState(), result).state
        val wrongId = TestData.questions(4)[1].id
        val card = s.questionStats.getValue(wrongId).card
        assertTrue(card.active)
        assertEquals(0, card.index) // a repetição correta não avançou o degrau
        assertEquals(today.plusDays(1), card.nextReview)
        assertFalse(s.questionStats.getValue(TestData.questions(4)[0].id).card.active)
    }

    @Test fun `licao com erro nao e perfeita e nao soma bonus de perfeicao`() {
        val (_, result) = play(listOf(true, false, true, true))
        val out = apply(freshState(), result)
        assertFalse(out.state.lessonProgress.getValue("u1l1").perfect)
        assertEquals(0, out.state.stats.perfectLessons)
        assertEquals(20, out.summary.xpBonus)
        assertEquals(75, out.state.lessonProgress.getValue("u1l1").bestAccuracy)
    }

    @Test fun `ritmo - media abaixo do alvo conta como licao no ritmo`() {
        val (_, fast) = play(List(8) { true }, timeMs = 100_000)
        assertTrue(apply(freshState(), fast).summary.paced)
        val (_, slow) = play(List(8) { true }, timeMs = 200_000)
        assertFalse(apply(freshState(), slow).summary.paced)
    }

    @Test fun `meta diaria so dispara uma vez e da gemas`() {
        val (_, r) = play(List(8) { true })
        val first = apply(freshState(goal = 2), r)
        assertFalse(first.summary.goalJustMet)
        val second = apply(first.state, play(List(8) { true }, "u1l2").second)
        assertTrue(second.summary.goalJustMet)
        assertTrue(second.state.todayLog.goalMet)
        assertEquals(ShopEngine.DAILY_GOAL_GEMS, second.summary.gemsEarned - ShopEngine.LESSON_GEMS - ShopEngine.PERFECT_GEMS - gemsFromAchievements(second))
        val third = apply(second.state, play(List(8) { true }, "u1l1").second)
        assertFalse(third.summary.goalJustMet)
    }

    @Test fun `meta nao atingida nao da gemas de meta`() {
        val (_, r) = play(List(8) { true })
        val out = apply(freshState(goal = 3), r)
        assertFalse(out.summary.goalJustMet)
        assertFalse(out.state.todayLog.goalMet)
    }

    @Test fun `ofensiva perdida com congelamento mantem e registra dias congelados`() {
        val stats = Stats(streak = StreakState(current = 4, longest = 4, lastDay = today.minusDays(2), freezes = 1))
        val (_, r) = play(List(8) { true })
        val out = apply(freshState(stats = stats), r)
        assertEquals(5, out.state.stats.streak.current)
        assertEquals(0, out.state.stats.streak.freezes)
        assertEquals(listOf(today.minusDays(1)), out.summary.frozenDays)
    }

    @Test fun `concluir as duas licoes da unidade marca unidade concluida e conquista`() {
        val first = apply(freshState(), play(List(8) { true }).second)
        assertFalse(first.summary.unitCompleted)
        val second = apply(first.state, play(List(8) { true }, "u1l2").second)
        assertTrue(second.summary.unitCompleted)
        assertTrue("unit1" in second.state.unlockedAchievements)
    }

    @Test fun `treino de erros vencidos da coracao, nao mexe na ofensiva e conta revisao correta`() {
        val (_, lesson) = play(listOf(false, true, true, true))
        val afterLesson = apply(freshState(), lesson).state
        val wrongQ = TestData.questions(4)[0]
        var run = LessonRun.start(listOf(wrongQ))
        run = LessonRun.answer(run, true, 30_000, 180_000)
        val practice = SessionResult(SessionKind.Practice, null, null, run.records, run.bestCombo, run.perfect)
        // O erro venceu (chegou o dia da revisão): é o que o app põe no treino de erros.
        val due = afterLesson.questionStats.getValue(wrongQ.id).let { it.copy(card = it.card.copy(nextReview = today)) }
        val low = afterLesson.copy(
            stats = afterLesson.stats.copy(hearts = HeartsState(0, nowMs)),
            questionStats = afterLesson.questionStats + (wrongQ.id to due),
        )
        val out = apply(low, practice)
        assertEquals(1, out.state.stats.hearts.hearts)
        assertEquals(afterLesson.stats.streak, out.state.stats.streak)
        assertEquals(1, out.state.stats.reviewCorrect)
        assertEquals(1, out.state.stats.practiceSessions)
        assertEquals(1, out.state.questionStats.getValue(wrongQ.id).card.index)
        assertEquals(0, out.summary.xpBonus)
    }

    @Test fun `missoes avancam com a sessao`() {
        val (_, r) = play(List(8) { true })
        val out = apply(freshState(), r)
        val ids = out.state.todayQuests.associateBy { it.questId }
        QuestEngine.questsFor(today).forEach { def ->
            val progress = ids.getValue(def.id).progress
            when (def.id) {
                "xp100" -> assertEquals(100, progress)
                "lessons2" -> assertEquals(1, progress)
                "perfect1", "paced1" -> assertEquals(1, progress)
                "correct20" -> assertEquals(8, progress)
                "combo6" -> assertEquals(6, progress)
                "practice1" -> assertEquals(0, progress)
            }
        }
    }

    @Test fun `estatisticas de precisao e tempo`() {
        val (_, r) = play(listOf(true, false, true, true), timeMs = 30_000)
        val s = apply(freshState(), r).state.stats
        assertEquals(4, s.firstTryAnswered)
        assertEquals(3, s.firstTryCorrect)
        assertEquals(75, s.accuracyPercent)
        assertEquals(5, s.answeredTotal) // 4 + 1 repetição
        assertEquals(30_000, s.averageTimeMs)
    }

    @Test fun `questao corrigida vai para a revisao como um erro`() {
        val qs = TestData.questions(3)
        var run = LessonRun.start(qs)
        run = LessonRun.answer(run, true, 60_000, 180_000)
        run = LessonRun.answer(run, false, 60_000, 180_000, corrected = true)
        run = LessonRun.answer(run, true, 60_000, 180_000)
        val result = SessionResult(SessionKind.Lesson, "u1l1", "u1", run.records, run.bestCombo, run.perfect)
        val s = apply(freshState(), result).state
        val card = s.questionStats.getValue(qs[1].id).card
        assertTrue(card.active)
        assertEquals(today.plusDays(1), card.nextReview)
        assertEquals(0, s.stats.perfectLessons)
        assertEquals(2, s.stats.firstTryCorrect)
    }

    /** Prova anterior: [correctPattern] na ordem, sem repetir erros (como no modo da aba Praticar). */
    private fun exam(correctPattern: List<Boolean>, timeMs: Long = 60_000): SessionResult {
        var run = LessonRun.start(TestData.questions(correctPattern.size, "u3l1"), repeatMistakes = false)
        correctPattern.forEach { run = LessonRun.answer(run, it, timeMs, 180_000) }
        assertTrue(run.finished)
        return SessionResult(SessionKind.Exam, null, null, run.records, run.bestCombo, run.perfect)
    }

    @Test fun `prova concluida da xp das respostas e de conclusao, conta como estudo e nao devolve coracao`() {
        val hearts = HeartsState(3, nowMs)
        val result = exam(List(30) { it % 3 != 0 }) // 20 acertos rápidos e 10 erros
        val out = apply(freshState(stats = Stats(hearts = hearts)), result)
        val s = out.state
        assertEquals(20 * 15 + 20, out.summary.totalXp) // sem o bônus de gabarito
        assertEquals(out.summary.totalXp, s.stats.totalXp)
        assertEquals(hearts, s.stats.hearts) // abrir já custou um coração; terminar não devolve
        assertEquals(1, s.stats.streak.current)
        assertTrue(out.summary.streakIncreased)
        assertEquals(1, s.todayLog.lessons) // conta como estudo do dia (lembretes e ofensiva em dupla)
        assertEquals(0, s.stats.lessonsCompleted)
        assertEquals(0, s.stats.practiceSessions)
        assertTrue(s.lessonProgress.isEmpty())
        assertEquals(ShopEngine.LESSON_GEMS + 20 /* meta de 100 XP */ + gemsFromAchievements(out), s.stats.gems)
        // os erros vão para a revisão espaçada (aparecem em "Praticar erros")
        assertEquals(10, s.questionStats.values.count { it.card.active })
    }

    @Test fun `prova gabaritada soma o bonus de perfeicao`() {
        val out = apply(freshState(), exam(List(5) { true }))
        assertEquals(5 * 15 + 30, out.summary.totalXp)
    }

    @Test fun `treino livre nao da xp, coracao, missao nem meta e nao adianta o cartao`() {
        val (_, lesson) = play(listOf(false, true, true, true))
        val afterLesson = apply(freshState(), lesson).state
        val wrongQ = TestData.questions(4)[0]
        val cardBefore = afterLesson.questionStats.getValue(wrongQ.id).card // vence amanhã
        var run = LessonRun.start(listOf(wrongQ), awardsXp = false)
        run = LessonRun.answer(run, true, 30_000, 180_000)
        assertEquals(0, run.xp)
        val free = SessionResult(SessionKind.FreeReview, null, null, run.records, run.bestCombo, run.perfect)
        val low = afterLesson.copy(stats = afterLesson.stats.copy(hearts = HeartsState(0, nowMs)))
        val out = apply(low, free)
        assertEquals(0, out.summary.totalXp)
        assertEquals(low.stats.totalXp, out.state.stats.totalXp)
        assertEquals(0, out.state.stats.hearts.hearts) // sem coração
        assertEquals(low.todayLog.xp, out.state.todayLog.xp) // meta não anda
        assertEquals(low.todayQuests, out.state.todayQuests) // missões não andam
        assertEquals(0, out.state.stats.reviewCorrect)
        assertEquals(0, out.state.stats.practiceSessions)
        assertEquals(cardBefore, out.state.questionStats.getValue(wrongQ.id).card) // acertar antes do dia não adianta
        assertEquals(low.stats.gems, out.state.stats.gems)
    }

    @Test fun `treino livre errado ainda volta o cartao para o comeco`() {
        val q = TestData.questions(1)[0]
        val stat = QuestionStat(q.id, q.lessonId, 3, 3, br.com.trilhati.domain.srs.SrsCard(2, today.plusDays(5)))
        var run = LessonRun.start(listOf(q), awardsXp = false)
        run = LessonRun.answer(run, false, 30_000, 180_000)
        while (!run.finished) run = LessonRun.answer(run, true, 30_000, 180_000)
        val out = apply(freshState().copy(questionStats = mapOf(q.id to stat)), SessionResult(SessionKind.FreeReview, null, null, run.records, run.bestCombo, run.perfect))
        assertEquals(br.com.trilhati.domain.srs.SrsCard(0, today.plusDays(1)), out.state.questionStats.getValue(q.id).card)
        assertEquals(0, out.summary.totalXp)
    }
}
