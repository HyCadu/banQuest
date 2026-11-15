package br.com.trilhati.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {
    // ---- perfil
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun observeProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun profile(): UserProfileEntity?

    @Upsert
    suspend fun upsertProfile(profile: UserProfileEntity)

    // ---- estatísticas
    @Query("SELECT * FROM stats WHERE id = 1")
    fun observeStats(): Flow<StatsEntity?>

    @Query("SELECT * FROM stats WHERE id = 1")
    suspend fun stats(): StatsEntity?

    @Upsert
    suspend fun upsertStats(stats: StatsEntity)

    // ---- lições
    @Query("SELECT * FROM lesson_progress")
    fun observeLessonProgress(): Flow<List<LessonProgressEntity>>

    @Query("SELECT * FROM lesson_progress")
    suspend fun lessonProgress(): List<LessonProgressEntity>

    @Upsert
    suspend fun upsertLessonProgress(progress: LessonProgressEntity)

    // ---- questões / revisão espaçada
    @Query("SELECT * FROM question_stat")
    fun observeQuestionStats(): Flow<List<QuestionStatEntity>>

    @Query("SELECT * FROM question_stat WHERE questionId IN (:ids)")
    suspend fun questionStats(ids: List<String>): List<QuestionStatEntity>

    @Upsert
    suspend fun upsertQuestionStats(stats: List<QuestionStatEntity>)

    /** Questões já erradas e ainda não "formadas" que pertencem à lição (para priorizá-las no sorteio). */
    @Query("SELECT questionId FROM question_stat WHERE lessonId = :lessonId AND srsIndex >= 0")
    suspend fun missedIds(lessonId: String): List<String>

    @Query("SELECT questionId FROM question_stat WHERE srsIndex >= 0 AND nextReviewDay <= :today ORDER BY nextReviewDay ASC, questionId ASC LIMIT :limit")
    suspend fun dueIds(today: Long, limit: Int): List<String>

    @Query("SELECT questionId FROM question_stat WHERE srsIndex >= 0 ORDER BY nextReviewDay ASC, questionId ASC LIMIT :limit")
    suspend fun bankIds(limit: Int): List<String>

    @Query("SELECT COUNT(*) FROM question_stat WHERE srsIndex >= 0 AND nextReviewDay <= :today")
    fun observeDueCount(today: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM question_stat WHERE srsIndex >= 0")
    fun observeErrorBankCount(): Flow<Int>

    @Insert
    suspend fun insertAttempts(attempts: List<AttemptEntity>)

    // ---- dias, missões, conquistas
    @Query("SELECT * FROM daily_log WHERE day >= :from ORDER BY day ASC")
    fun observeDailyLogs(from: Long): Flow<List<DailyLogEntity>>

    @Query("SELECT * FROM daily_log WHERE day = :day")
    suspend fun dailyLog(day: Long): DailyLogEntity?

    @Upsert
    suspend fun upsertDailyLog(log: DailyLogEntity)

    @Query("SELECT * FROM quest_state WHERE day = :day ORDER BY questId ASC")
    fun observeQuests(day: Long): Flow<List<QuestStateEntity>>

    @Query("SELECT * FROM quest_state WHERE day = :day ORDER BY questId ASC")
    suspend fun quests(day: Long): List<QuestStateEntity>

    @Upsert
    suspend fun upsertQuests(quests: List<QuestStateEntity>)

    @Query("SELECT * FROM achievement")
    fun observeAchievements(): Flow<List<AchievementEntity>>

    @Query("SELECT id FROM achievement")
    suspend fun achievementIds(): List<String>

    @Upsert
    suspend fun upsertAchievements(achievements: List<AchievementEntity>)

    // ---- liga
    @Query("SELECT * FROM league_week ORDER BY weekStart DESC LIMIT 1")
    fun observeLatestLeague(): Flow<LeagueWeekEntity?>

    @Query("SELECT * FROM league_week ORDER BY weekStart DESC LIMIT 1")
    suspend fun latestLeague(): LeagueWeekEntity?

    @Query("SELECT * FROM league_week WHERE closed = 1 ORDER BY weekStart DESC LIMIT 1")
    fun observeLastClosedLeague(): Flow<LeagueWeekEntity?>

    @Upsert
    suspend fun upsertLeague(week: LeagueWeekEntity)

    // ---- loja e pacotes
    @Query("SELECT * FROM shop_state WHERE id = 1")
    fun observeShop(): Flow<ShopStateEntity?>

    @Query("SELECT * FROM shop_state WHERE id = 1")
    suspend fun shop(): ShopStateEntity?

    @Upsert
    suspend fun upsertShop(shop: ShopStateEntity)

    @Upsert
    suspend fun upsertPackMeta(meta: PackMetaEntity)

    @Query("SELECT * FROM pack_meta")
    suspend fun packMetas(): List<PackMetaEntity>
}
