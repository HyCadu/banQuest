package br.com.trilhati.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v2 -> v3 (versão social): fila de sessões para o servidor, coluna de pódios e fim da liga simulada.
 * Escrita à mão porque, a partir daqui, o aluno já tem progresso real e nada pode ser apagado na atualização.
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `outbox_session` (`id` TEXT NOT NULL, `day` INTEGER NOT NULL, `kind` TEXT NOT NULL, " +
                "`xp` INTEGER NOT NULL, `answers` INTEGER NOT NULL, `durationMs` INTEGER NOT NULL, `streak` INTEGER NOT NULL, " +
                "`streakLongest` INTEGER NOT NULL, `lastStudyDay` INTEGER NOT NULL, `createdAtMs` INTEGER NOT NULL, PRIMARY KEY(`id`))",
        )
        db.execSQL("ALTER TABLE `stats` ADD COLUMN `podiums` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("DROP TABLE IF EXISTS `league_week`")
    }
}

/** v3 -> v4 (0.7.1): meio coração e o estoque do botão "Não sei" (todo mundo começa com 3). */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `stats` ADD COLUMN `heartHalf` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `stats` ADD COLUMN `helps` INTEGER NOT NULL DEFAULT 3")
        db.execSQL("ALTER TABLE `stats` ADD COLUMN `helpsDay` INTEGER NOT NULL DEFAULT 0")
    }
}
