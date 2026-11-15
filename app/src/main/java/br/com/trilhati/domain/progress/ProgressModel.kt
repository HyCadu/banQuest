package br.com.trilhati.domain.progress

import br.com.trilhati.domain.hearts.HeartsEngine
import br.com.trilhati.domain.hearts.HeartsState
import br.com.trilhati.domain.srs.SrsCard
import br.com.trilhati.domain.streak.StreakState
import java.time.LocalDate

/** Estatísticas gerais do aluno (linha única). */
data class Stats(
    val totalXp: Int = 0,
    val gems: Int = 0,
    val hearts: HeartsState = HeartsState(HeartsEngine.MAX, 0L),
    val streak: StreakState = StreakState(),
    val lessonsCompleted: Int = 0,
    val perfectLessons: Int = 0,
    val pacedLessons: Int = 0,
    val practiceSessions: Int = 0,
    val reviewCorrect: Int = 0,
    /** Todas as respostas dadas (inclusive repetições): base da média de tempo por questão. */
    val answeredTotal: Int = 0,
    val totalTimeMs: Long = 0,
    /** Só respostas de primeira: base da precisão. */
    val firstTryAnswered: Int = 0,
    val firstTryCorrect: Int = 0,
    /** Semanas terminadas entre os 3 primeiros do ranking geral (conquista "Pódio da semana"). */
    val podiums: Int = 0,
) {
    val accuracyPercent: Int get() = if (firstTryAnswered == 0) 0 else firstTryCorrect * 100 / firstTryAnswered
    val averageTimeMs: Long get() = if (answeredTotal == 0) 0 else totalTimeMs / answeredTotal
}

data class LessonProgress(
    val lessonId: String,
    val unitId: String,
    val timesCompleted: Int = 0,
    val bestAccuracy: Int = 0,
    val perfectCount: Int = 0,
    val completedAtMs: Long = 0,
) {
    val completed: Boolean get() = timesCompleted > 0
    val perfect: Boolean get() = perfectCount > 0
}

data class QuestionStat(
    val questionId: String,
    val lessonId: String,
    val times: Int = 0,
    val correct: Int = 0,
    val card: SrsCard = SrsCard(),
)

data class DailyLog(val day: LocalDate, val xp: Int = 0, val lessons: Int = 0, val goalMet: Boolean = false, val froze: Boolean = false)

data class QuestProgress(val day: LocalDate, val questId: String, val progress: Int = 0, val claimed: Boolean = false)


/** Tudo que uma sessão de estudo pode alterar. */
data class ProgressState(
    val stats: Stats,
    val lessonProgress: Map<String, LessonProgress>,
    val questionStats: Map<String, QuestionStat>,
    val todayLog: DailyLog,
    val todayQuests: List<QuestProgress>,
    val unlockedAchievements: Set<String>,
    val dailyGoalXp: Int,
)
