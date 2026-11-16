package br.com.trilhati.di

import android.content.Context
import androidx.room.Room
import br.com.trilhati.data.db.AppDatabase
import br.com.trilhati.data.db.ProgressDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            // v1 (Fase 0) só tinha uma tabela vazia: pode ser recriada. Da v2 em diante há progresso real,
            // então só migrações explícitas (docs/DECISIONS.md).
            .addMigrations(br.com.trilhati.data.db.MIGRATION_2_3, br.com.trilhati.data.db.MIGRATION_3_4)
            .fallbackToDestructiveMigrationFrom(true, 1)
            .build()

    @Provides
    fun provideProgressDao(db: AppDatabase): ProgressDao = db.progressDao()
}
