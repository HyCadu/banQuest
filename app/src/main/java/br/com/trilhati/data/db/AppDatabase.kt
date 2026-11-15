package br.com.trilhati.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProfileEntity::class, StatsEntity::class, LessonProgressEntity::class, QuestionStatEntity::class,
        AttemptEntity::class, DailyLogEntity::class, QuestStateEntity::class, AchievementEntity::class,
        ShopStateEntity::class, PackMetaEntity::class, OutboxSessionEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun progressDao(): ProgressDao

    companion object {
        const val NAME = "trilha-ti.db"
    }
}
