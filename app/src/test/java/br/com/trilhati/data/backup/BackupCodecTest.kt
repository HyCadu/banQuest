package br.com.trilhati.data.backup

import br.com.trilhati.data.db.AchievementEntity
import br.com.trilhati.data.db.DailyLogEntity
import br.com.trilhati.data.db.LessonProgressEntity
import br.com.trilhati.data.db.StatsEntity
import br.com.trilhati.data.db.UserProfileEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupCodecTest {
    private val stats = StatsEntity(1, 640, 215, 3, 99, 3, 5, 20_000, 1, 2, 1, 1, 0, 0, 18, 2_520_000, 16, 13, 0)
    private val backup = Backup(
        exportedAtMs = 123,
        profile = UserProfileEntity(name = "Carlos", character = "Tartaruga", dailyGoalXp = 200, createdAtEpochMillis = 1),
        stats = stats,
        lessonProgress = listOf(LessonProgressEntity("u1l1", "u1", 1, 100, 1, 5)),
        dailyLogs = listOf(DailyLogEntity(20_000, 120, 1, true, false)),
        achievements = listOf(AchievementEntity("streak3", 7)),
    )

    @Test fun `ida e volta preserva tudo`() {
        val read = BackupCodec.decode(BackupCodec.encode(backup))
        assertEquals(BackupRead.Ok(backup), read)
    }

    @Test fun `json com o formato esperado`() {
        val text = BackupCodec.encode(backup)
        assertTrue(text.contains("\"format\": \"trilha-ti-backup\""))
        assertTrue(text.contains("\"Carlos\""))
    }

    @Test fun `recusa arquivo que nao e backup`() {
        assertTrue(BackupCodec.decode("{\"schema\":1,\"pack\":\"u1\"}") is BackupRead.Invalid)
        assertTrue(BackupCodec.decode("não é json") is BackupRead.Invalid)
        assertTrue(BackupCodec.decode("") is BackupRead.Invalid)
    }

    @Test fun `recusa backup de versao mais nova`() {
        val newer = BackupCodec.encode(backup).replace("\"version\": 1", "\"version\": 99")
        assertTrue(BackupCodec.decode(newer) is BackupRead.Invalid)
    }

    @Test fun `recusa backup sem estatisticas`() {
        assertTrue(BackupCodec.decode(BackupCodec.encode(backup.copy(stats = null))) is BackupRead.Invalid)
    }
}
