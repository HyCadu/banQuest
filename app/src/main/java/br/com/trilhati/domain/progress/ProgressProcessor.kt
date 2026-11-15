package br.com.trilhati.domain.progress

import br.com.trilhati.domain.achievement.AchievementDef
import br.com.trilhati.domain.achievement.AchievementEngine
import br.com.trilhati.domain.achievement.AchievementInput
import br.com.trilhati.domain.hearts.HeartsEngine
import br.com.trilhati.domain.lesson.AnswerRecord
import br.com.trilhati.domain.lesson.XpEngine
import br.com.trilhati.domain.quest.QuestEngine
import br.com.trilhati.domain.quest.QuestEvent
import br.com.trilhati.domain.srs.SrsEngine
import br.com.trilhati.domain.streak.StreakEngine
import java.time.LocalDate

enum class SessionKind { Lesson, Practice }

/** Resumo de uma sessão terminada, vindo da tela de lição. */
data class SessionResult(
    val kind: SessionKind,
    /** Lição concluída (só para [SessionKind.Lesson]). */
    val lessonId: String?,
    val unitId: String?,
    val records: List<AnswerRecord>,
    val bestCombo: Int,
    val perfect: Boolean,
)

/** O que mostrar na tela de resultado. */
data class SessionSummary(
    val xpFromAnswers: Int,
    val xpBonus: Int,
    val gemsEarned: Int,
    val streakBefore: Int,
    val streakAfter: Int,
    val streakIncreased: Boolean,
    val goalJustMet: Boolean,
    val paced: Boolean,
    val newAchievements: List<AchievementDef>,
    val questsCompleted: List<String>,
    val frozenDays: List<LocalDate>,
    val unitCompleted: Boolean,
) {
    val totalXp: Int get() = xpFromAnswers + xpBonus
}

data class ProcessedSession(val state: ProgressState, val summary: SessionSummary)

/**
 * Aplica o resultado de uma sessão ao progresso inteiro, de uma vez e sem tocar em Android nem banco.
 * O repositório só carrega o [ProgressState], chama esta função e grava o resultado.
 */
object ProgressProcessor {
    fun apply(
        state: ProgressState,
        result: SessionResult,
        today: LocalDate,
        nowMs: Long,
        /** Unidades que existem na trilha (para contar "unidade concluída" nas conquistas). */
        completedUnitsAfter: (Map<String, LessonProgress>) -> Int,
        /** Ids das unidades já concluídas antes da sessão. */
        completedUnitsBefore: Int,
    ): ProcessedSession {
        val firstTry = result.records.filter { !it.retry }
        val isLesson = result.kind == SessionKind.Lesson
        val xpFromAnswers = result.records.sumOf { it.xp }
        val xpBonus = if (isLesson) XpEngine.completionXp(result.perfect) else 0
        val xpTotal = xpFromAnswers + xpBonus
        val totalTime = result.records.sumOf { it.timeMs }
        val targetsSum = result.records.sumOf { it.targetMs }
        val paced = isLesson && result.records.isNotEmpty() && totalTime <= targetsSum

        // Revisão espaçada: só a primeira resposta de cada questão mexe no cartão.
        val questionStats = state.questionStats.toMutableMap()
        var reviewCorrect = 0
        for (r in firstTry) {
            val q = r.question
            val before = questionStats[q.id] ?: QuestionStat(q.id, q.lessonId)
            if (result.kind == SessionKind.Practice && r.correct && before.card.active) reviewCorrect++
            questionStats[q.id] = before.copy(
                times = before.times + 1,
                correct = before.correct + if (r.correct) 1 else 0,
                card = SrsEngine.onAnswer(before.card, r.correct, today),
            )
        }

        // Progresso da lição.
        val lessonProgress = state.lessonProgress.toMutableMap()
        if (isLesson && result.lessonId != null && result.unitId != null) {
            val before = lessonProgress[result.lessonId] ?: LessonProgress(result.lessonId, result.unitId)
            val accuracy = if (firstTry.isEmpty()) 0 else firstTry.count { it.correct } * 100 / firstTry.size
            lessonProgress[result.lessonId] = before.copy(
                timesCompleted = before.timesCompleted + 1,
                bestAccuracy = maxOf(before.bestAccuracy, accuracy),
                perfectCount = before.perfectCount + if (result.perfect) 1 else 0,
                completedAtMs = nowMs,
            )
        }

        // Ofensiva: só lição conta; treino apenas reconcilia dias perdidos.
        val streakResult = if (isLesson) StreakEngine.onLessonCompleted(state.stats.streak, today)
        else StreakEngine.reconcile(state.stats.streak, today)
        val streakBefore = StreakEngine.displayCount(state.stats.streak, today)

        // Meta diária.
        val newDailyXp = state.todayLog.xp + xpTotal
        val goalJustMet = !state.todayLog.goalMet && newDailyXp >= state.dailyGoalXp
        val todayLog = state.todayLog.copy(
            xp = newDailyXp,
            lessons = state.todayLog.lessons + if (isLesson) 1 else 0,
            goalMet = state.todayLog.goalMet || goalJustMet,
        )

        // Missões do dia.
        val event = QuestEvent(
            xp = xpTotal,
            lessons = if (isLesson) 1 else 0,
            bestCombo = result.bestCombo,
            pacedLessons = if (paced) 1 else 0,
            perfectLessons = if (isLesson && result.perfect) 1 else 0,
            correctAnswers = result.records.count { it.correct },
            practiceSessions = if (isLesson) 0 else 1,
        )
        val completedQuests = mutableListOf<String>()
        val quests = state.todayQuests.map { qp ->
            val def = QuestEngine.byId(qp.questId) ?: return@map qp
            val next = QuestEngine.apply(def, qp.progress, event)
            if (!QuestEngine.isComplete(def, qp.progress) && QuestEngine.isComplete(def, next)) completedQuests += def.title
            qp.copy(progress = next)
        }

        // Estatísticas e recompensas.
        val gemsEarned = (if (isLesson) ShopEngine.LESSON_GEMS + (if (result.perfect) ShopEngine.PERFECT_GEMS else 0) else 0) +
            if (goalJustMet) ShopEngine.DAILY_GOAL_GEMS else 0
        val hearts = if (isLesson) state.stats.hearts else HeartsEngine.gain(state.stats.hearts, 1, nowMs)
        var stats = state.stats.copy(
            totalXp = state.stats.totalXp + xpTotal,
            gems = state.stats.gems + gemsEarned,
            hearts = hearts,
            streak = streakResult.state,
            lessonsCompleted = state.stats.lessonsCompleted + if (isLesson) 1 else 0,
            perfectLessons = state.stats.perfectLessons + if (isLesson && result.perfect) 1 else 0,
            pacedLessons = state.stats.pacedLessons + if (paced) 1 else 0,
            practiceSessions = state.stats.practiceSessions + if (isLesson) 0 else 1,
            reviewCorrect = state.stats.reviewCorrect + reviewCorrect,
            answeredTotal = state.stats.answeredTotal + result.records.size,
            totalTimeMs = state.stats.totalTimeMs + totalTime,
            firstTryAnswered = state.stats.firstTryAnswered + firstTry.size,
            firstTryCorrect = state.stats.firstTryCorrect + firstTry.count { it.correct },
        )

        // Liga da semana.
        val league = state.league.copy(userXp = state.league.userXp + xpTotal)

        // Conquistas (podem render gemas, que entram no total).
        val unitsAfter = completedUnitsAfter(lessonProgress)
        val input = AchievementInput(
            longestStreak = stats.streak.longest,
            totalXp = stats.totalXp,
            perfectLessons = stats.perfectLessons,
            unitsCompleted = unitsAfter,
            pacedLessons = stats.pacedLessons,
            reviewCorrect = stats.reviewCorrect,
            bestLeagueTier = stats.bestLeagueTier,
        )
        val newAchievements = AchievementEngine.newlyUnlocked(input, state.unlockedAchievements)
        stats = stats.copy(gems = stats.gems + newAchievements.sumOf { it.rewardGems })

        val summary = SessionSummary(
            xpFromAnswers = xpFromAnswers,
            xpBonus = xpBonus,
            gemsEarned = gemsEarned + newAchievements.sumOf { it.rewardGems },
            streakBefore = streakBefore,
            streakAfter = StreakEngine.displayCount(stats.streak, today),
            streakIncreased = isLesson && stats.streak.current > streakBefore,
            goalJustMet = goalJustMet,
            paced = paced,
            newAchievements = newAchievements,
            questsCompleted = completedQuests,
            frozenDays = streakResult.frozenDays,
            unitCompleted = unitsAfter > completedUnitsBefore,
        )
        val newState = state.copy(
            stats = stats,
            lessonProgress = lessonProgress,
            questionStats = questionStats,
            todayLog = todayLog,
            todayQuests = quests,
            unlockedAchievements = state.unlockedAchievements + newAchievements.map { it.id },
            league = league,
        )
        return ProcessedSession(newState, summary)
    }
}
