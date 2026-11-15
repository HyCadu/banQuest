package br.com.trilhati.data.backup

import br.com.trilhati.data.db.AchievementEntity
import br.com.trilhati.data.db.AttemptEntity
import br.com.trilhati.data.db.DailyLogEntity
import br.com.trilhati.data.db.LessonProgressEntity
import br.com.trilhati.data.db.PackMetaEntity
import br.com.trilhati.data.db.QuestStateEntity
import br.com.trilhati.data.db.QuestionStatEntity
import br.com.trilhati.data.db.ShopStateEntity
import br.com.trilhati.data.db.StatsEntity
import br.com.trilhati.data.db.UserProfileEntity
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Cópia completa do progresso, em JSON, para o aluno guardar fora do aparelho (PLAN.md §11). */
@Serializable
data class Backup(
    val format: String = FORMAT,
    val version: Int = VERSION,
    val exportedAtMs: Long,
    val profile: UserProfileEntity? = null,
    val stats: StatsEntity? = null,
    val lessonProgress: List<LessonProgressEntity> = emptyList(),
    val questionStats: List<QuestionStatEntity> = emptyList(),
    val attempts: List<AttemptEntity> = emptyList(),
    val dailyLogs: List<DailyLogEntity> = emptyList(),
    val quests: List<QuestStateEntity> = emptyList(),
    val achievements: List<AchievementEntity> = emptyList(),
    val shop: ShopStateEntity? = null,
    val packs: List<PackMetaEntity> = emptyList(),
) {
    companion object {
        const val FORMAT = "trilha-ti-backup"
        const val VERSION = 1
    }
}

sealed interface BackupRead {
    data class Ok(val backup: Backup) : BackupRead
    data class Invalid(val reason: String) : BackupRead
}

object BackupCodec {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(backup: Backup): String = json.encodeToString(Backup.serializer(), backup)

    fun decode(text: String): BackupRead {
        val backup = try {
            json.decodeFromString(Backup.serializer(), text)
        } catch (e: SerializationException) {
            return BackupRead.Invalid("o arquivo não é um backup do BanQuest (${e.message?.take(80)})")
        } catch (e: IllegalArgumentException) {
            return BackupRead.Invalid("o arquivo não é um backup do BanQuest (${e.message?.take(80)})")
        }
        return when {
            backup.format != Backup.FORMAT -> BackupRead.Invalid("o arquivo não é um backup do BanQuest")
            backup.version > Backup.VERSION -> BackupRead.Invalid("o backup é de uma versão mais nova do app")
            backup.stats == null || backup.profile == null -> BackupRead.Invalid("o backup está incompleto")
            else -> BackupRead.Ok(backup)
        }
    }
}
