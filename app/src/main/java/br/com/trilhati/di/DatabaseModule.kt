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
            // A versão 1 (Fase 0) só tinha uma tabela vazia de perfil. Antes de o aluno ter dados reais,
            // trocar o esquema recria o banco; depois disso, só migrações explícitas (docs/DECISIONS.md).
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideProgressDao(db: AppDatabase): ProgressDao = db.progressDao()
}
