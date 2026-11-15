package br.com.trilhati.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/** Datas são guardadas como "dia da época" (`LocalDate.toEpochDay()`); -1 significa "nenhum". */
const val NO_DAY = -1L

/** Perfil do usuário (PLAN.md §5.2). Linha única, `id = 1`. */
@Serializable
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = SINGLE_ID,
    val name: String,
    val character: String,
    val dailyGoalXp: Int,
    val createdAtEpochMillis: Long,
) {
    companion object {
        const val SINGLE_ID = 1
    }
}

@Serializable
@Entity(tableName = "stats")
data class StatsEntity(
    @PrimaryKey val id: Int = 1,
    val totalXp: Int,
    val gems: Int,
    val hearts: Int,
    val heartsUpdatedAtMs: Long,
    val streakCurrent: Int,
    val streakLongest: Int,
    val streakLastDay: Long,
    val freezes: Int,
    val lessonsCompleted: Int,
    val perfectLessons: Int,
    val pacedLessons: Int,
    val practiceSessions: Int,
    val reviewCorrect: Int,
    val answeredTotal: Int,
    val totalTimeMs: Long,
    val firstTryAnswered: Int,
    val firstTryCorrect: Int,
    val bestLeagueTier: Int,
)

@Serializable
@Entity(tableName = "lesson_progress")
data class LessonProgressEntity(
    @PrimaryKey val lessonId: String,
    val unitId: String,
    val timesCompleted: Int,
    val bestAccuracy: Int,
    val perfectCount: Int,
    val completedAtMs: Long,
)

@Serializable
@Entity(tableName = "question_stat")
data class QuestionStatEntity(
    @PrimaryKey val questionId: String,
    val lessonId: String,
    val times: Int,
    val correct: Int,
    /** Degrau da revisão espaçada; -1 = sem cartão. */
    val srsIndex: Int,
    val nextReviewDay: Long,
)

@Serializable
@Entity(tableName = "attempt")
data class AttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questionId: String,
    val correct: Boolean,
    val retry: Boolean,
    val timeMs: Long,
    val atMs: Long,
    val kind: String,
)

@Serializable
@Entity(tableName = "daily_log")
data class DailyLogEntity(
    @PrimaryKey val day: Long,
    val xp: Int,
    val lessons: Int,
    val goalMet: Boolean,
    val froze: Boolean,
)

@Serializable
@Entity(tableName = "quest_state", primaryKeys = ["day", "questId"])
data class QuestStateEntity(
    val day: Long,
    val questId: String,
    val progress: Int,
    val claimed: Boolean,
)

@Serializable
@Entity(tableName = "achievement")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val unlockedAtMs: Long,
)

@Serializable
@Entity(tableName = "league_week")
data class LeagueWeekEntity(
    @PrimaryKey val weekStart: Long,
    val tier: Int,
    val userXp: Int,
    val closed: Boolean,
    /** Posição final; -1 enquanto a semana está aberta. */
    val finalRank: Int,
    /** Resultado do fechamento: 0 = ficou, 1 = subiu, -1 = desceu. */
    val outcome: Int,
)

@Serializable
@Entity(tableName = "shop_state")
data class ShopStateEntity(
    @PrimaryKey val id: Int = 1,
    val lastDailyChestDay: Long,
    /** Ids das unidades cujo baú já foi aberto, separados por vírgula. */
    val claimedUnitChests: String,
)

@Serializable
@Entity(tableName = "pack_meta")
data class PackMetaEntity(
    @PrimaryKey val pack: String,
    val version: Int,
)
