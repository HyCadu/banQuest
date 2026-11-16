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

    /** Questões da lição já respondidas alguma vez (para saber quais conceitos o aluno já viu). */
    @Query("SELECT questionId FROM question_stat WHERE lessonId = :lessonId AND times > 0")
    suspend fun answeredIds(lessonId: String): List<String>

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

    // ---- backup (exportar/importar)
    @Query("SELECT * FROM attempt ORDER BY id ASC")
    suspend fun allAttempts(): List<AttemptEntity>

    @Query("SELECT * FROM daily_log ORDER BY day ASC")
    suspend fun allDailyLogs(): List<DailyLogEntity>

    @Query("SELECT * FROM quest_state ORDER BY day ASC, questId ASC")
    suspend fun allQuests(): List<QuestStateEntity>

    @Query("SELECT * FROM question_stat")
    suspend fun allQuestionStats(): List<QuestionStatEntity>

    @Query("SELECT * FROM achievement")
    suspend fun allAchievements(): List<AchievementEntity>

    @Upsert suspend fun upsertAttempts(items: List<AttemptEntity>)
    @Upsert suspend fun upsertDailyLogs(items: List<DailyLogEntity>)
    @Upsert suspend fun upsertLessonProgressList(items: List<LessonProgressEntity>)
    @Upsert suspend fun upsertPackMetas(items: List<PackMetaEntity>)

    @Query("DELETE FROM user_profile") suspend fun deleteProfile()
    @Query("DELETE FROM stats") suspend fun deleteStats()
    @Query("DELETE FROM lesson_progress") suspend fun deleteLessonProgress()
    @Query("DELETE FROM question_stat") suspend fun deleteQuestionStats()
    @Query("DELETE FROM attempt") suspend fun deleteAttempts()
    @Query("DELETE FROM daily_log") suspend fun deleteDailyLogs()
    @Query("DELETE FROM quest_state") suspend fun deleteQuests()
    @Query("DELETE FROM achievement") suspend fun deleteAchievements()
    @Query("DELETE FROM shop_state") suspend fun deleteShop()
    @Query("DELETE FROM pack_meta") suspend fun deletePackMetas()

    // ---- fila de envio ao servidor
    @Insert
    suspend fun insertOutbox(item: OutboxSessionEntity)

    @Query("SELECT * FROM outbox_session ORDER BY createdAtMs ASC LIMIT :limit")
    suspend fun outbox(limit: Int): List<OutboxSessionEntity>

    @Query("SELECT COUNT(*) FROM outbox_session")
    fun observeOutboxCount(): Flow<Int>

    @Query("DELETE FROM outbox_session WHERE id = :id")
    suspend fun deleteOutbox(id: String)

    @Query("DELETE FROM outbox_session")
    suspend fun clearOutbox()
}
