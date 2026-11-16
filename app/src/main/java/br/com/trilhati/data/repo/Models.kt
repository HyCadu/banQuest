package br.com.trilhati.data.repo

import br.com.trilhati.data.db.AchievementEntity
import br.com.trilhati.data.db.DailyLogEntity
import br.com.trilhati.data.db.LessonProgressEntity
import br.com.trilhati.data.db.NO_DAY
import br.com.trilhati.data.db.QuestStateEntity
import br.com.trilhati.data.db.QuestionStatEntity
import br.com.trilhati.data.db.ShopStateEntity
import br.com.trilhati.data.db.StatsEntity
import br.com.trilhati.domain.hearts.HeartsState
import br.com.trilhati.domain.progress.DailyLog
import br.com.trilhati.domain.progress.LessonProgress
import br.com.trilhati.domain.progress.QuestProgress
import br.com.trilhati.domain.progress.QuestionStat
import br.com.trilhati.domain.progress.Stats
import br.com.trilhati.domain.srs.SrsCard
import br.com.trilhati.domain.streak.StreakState
import java.time.LocalDate

/** Dados do perfil do aluno. O personagem é guardado pelo nome (o desenho vive na camada de UI). */
data class Profile(val name: String, val character: String, val dailyGoalXp: Int) {
    companion object {
        const val DEFAULT_NAME = "Estudante"
        const val DEFAULT_CHARACTER = "Guaiamum"
        const val DEFAULT_GOAL_XP = 100
    }
}

data class ShopState(val lastDailyChestDay: LocalDate?, val claimedUnitChests: Set<String>)

/** Retrato de tudo que a interface precisa do progresso. Pequeno o bastante para ser recomposto inteiro. */
data class AppSnapshot(
    val profile: Profile,
    val stats: Stats,
    val lessonProgress: Map<String, LessonProgress>,
    val questionStats: Map<String, QuestionStat>,
    val dailyLogs: Map<LocalDate, DailyLog>,
    val achievements: Map<String, Long>,
    /** Sessões guardadas esperando envio ao servidor. */
    val pendingUploads: Int = 0,
    val shop: ShopState,
    val errorBankSize: Int,
    val dueCount: Int,
)

// ---- conversões banco <-> domínio

internal fun Long.toDayOrNull(): LocalDate? = if (this == NO_DAY) null else LocalDate.ofEpochDay(this)
internal fun LocalDate?.toDay(): Long = this?.toEpochDay() ?: NO_DAY

internal fun StatsEntity.toDomain() = Stats(
    totalXp = totalXp, gems = gems, hearts = HeartsState(hearts, heartsUpdatedAtMs, heartHalf),
    streak = StreakState(streakCurrent, streakLongest, streakLastDay.toDayOrNull(), freezes),
    lessonsCompleted = lessonsCompleted, perfectLessons = perfectLessons, pacedLessons = pacedLessons,
    practiceSessions = practiceSessions, reviewCorrect = reviewCorrect, answeredTotal = answeredTotal,
    totalTimeMs = totalTimeMs, firstTryAnswered = firstTryAnswered, firstTryCorrect = firstTryCorrect,
    podiums = podiums, help = br.com.trilhati.domain.progress.HelpState(helps, helpsDay),
)

internal fun Stats.toEntity() = StatsEntity(
    totalXp = totalXp, gems = gems, hearts = hearts.hearts, heartsUpdatedAtMs = hearts.updatedAtMs,
    streakCurrent = streak.current, streakLongest = streak.longest, streakLastDay = streak.lastDay.toDay(),
    freezes = streak.freezes, lessonsCompleted = lessonsCompleted, perfectLessons = perfectLessons,
    pacedLessons = pacedLessons, practiceSessions = practiceSessions, reviewCorrect = reviewCorrect,
    answeredTotal = answeredTotal, totalTimeMs = totalTimeMs, firstTryAnswered = firstTryAnswered,
    firstTryCorrect = firstTryCorrect, podiums = podiums, heartHalf = hearts.half, helps = help.count, helpsDay = help.grantedDay,
)

internal fun LessonProgressEntity.toDomain() = LessonProgress(lessonId, unitId, timesCompleted, bestAccuracy, perfectCount, completedAtMs)
internal fun LessonProgress.toEntity() = LessonProgressEntity(lessonId, unitId, timesCompleted, bestAccuracy, perfectCount, completedAtMs)

internal fun QuestionStatEntity.toDomain() = QuestionStat(
    questionId, lessonId, times, correct,
    if (srsIndex < 0) SrsCard() else SrsCard(srsIndex, nextReviewDay.toDayOrNull()),
)

internal fun QuestionStat.toEntity() = QuestionStatEntity(
    questionId, lessonId, times, correct, if (card.active) card.index else SrsCard.NO_CARD, card.nextReview.toDay(),
)

internal fun DailyLogEntity.toDomain() = DailyLog(LocalDate.ofEpochDay(day), xp, lessons, goalMet, froze)
internal fun DailyLog.toEntity() = DailyLogEntity(day.toEpochDay(), xp, lessons, goalMet, froze)

internal fun QuestStateEntity.toDomain() = QuestProgress(LocalDate.ofEpochDay(day), questId, progress, claimed)
internal fun QuestProgress.toEntity() = QuestStateEntity(day.toEpochDay(), questId, progress, claimed)

internal fun ShopStateEntity.toDomain() = ShopState(
    lastDailyChestDay.toDayOrNull(),
    claimedUnitChests.split(',').filter { it.isNotBlank() }.toSet(),
)

internal fun ShopState.toEntity() = ShopStateEntity(1, lastDailyChestDay.toDay(), claimedUnitChests.sorted().joinToString(","))

internal fun List<AchievementEntity>.toMap(): Map<String, Long> = associate { it.id to it.unlockedAtMs }
