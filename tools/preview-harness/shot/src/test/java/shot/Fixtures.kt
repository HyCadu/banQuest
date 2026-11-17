package shot

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onRoot
import br.com.trilhati.data.content.CourseAssembler
import br.com.trilhati.data.content.PackDto
import br.com.trilhati.data.content.toDomain
import br.com.trilhati.data.prefs.Settings
import br.com.trilhati.data.repo.AppSnapshot
import br.com.trilhati.data.repo.Profile
import br.com.trilhati.data.repo.ShopState
import br.com.trilhati.domain.hearts.HeartsState
import br.com.trilhati.domain.progress.*
import br.com.trilhati.domain.srs.SrsCard
import br.com.trilhati.domain.streak.StreakState
import br.com.trilhati.feature.app.AppState
import br.com.trilhati.feature.app.SocialUi
import br.com.trilhati.data.social.*
import br.com.trilhati.ui.theme.*
import java.io.File
import java.time.Instant
import java.time.LocalDate
import kotlinx.serialization.json.Json

val OUT: String = File("build/shots").also { it.mkdirs() }.absolutePath
val TODAY: LocalDate = LocalDate.of(2026, 11, 18) // quarta-feira
val NOW_MS: Long = Instant.parse("2026-11-18T15:20:00Z").toEpochMilli()

fun Bitmap.save(name: String) = File(OUT, name).outputStream().use { compress(Bitmap.CompressFormat.PNG, 100, it) }

/** Provas anteriores do pacote real, agrupadas como o app faz (seção "Provas anteriores" da aba Praticar). */
val pastExams by lazy {
    val questions = Json { ignoreUnknownKeys = true }
        .decodeFromString(br.com.trilhati.data.content.ExamPackDto.serializer(), File("../../../app/src/main/assets/provas/provas-anteriores.json").readText())
        .toDomain()
    br.com.trilhati.domain.exam.PastExams.from(questions) { it.lessonId.removePrefix("u").substringBefore("l").toInt() > 10 }
}

val course by lazy {
    val dto = Json { ignoreUnknownKeys = true }.decodeFromString(PackDto.serializer(), File("../../../app/src/main/assets/content/u1-banco-de-dados.json").readText())
    CourseAssembler.assemble(listOfNotNull(dto.toDomain()))
}

fun fakeState(progress: Boolean, character: String = "Guaiamum", noLives: Boolean = false): AppState {
    val lessons = if (progress) mapOf(
        "u1l1" to LessonProgress("u1l1", "u1", 1, 100, 1, 1),
        "u1l2" to LessonProgress("u1l2", "u1", 1, 75, 0, 1),
    ) else emptyMap()
    val stats = if (progress) Stats(
        totalXp = 640, gems = 215, hearts = HeartsState(3, NOW_MS - 10 * 60_000), streak = StreakState(3, 5, TODAY, 1),
        lessonsCompleted = 2, perfectLessons = 1, pacedLessons = 1, answeredTotal = 18, totalTimeMs = 18 * 140_000L,
        firstTryAnswered = 16, firstTryCorrect = 13,
    ) else Stats(hearts = HeartsState(5, NOW_MS))
    val q = course[0].lessons.flatMap { it.questions }
    val qstats = if (progress) q.take(6).mapIndexed { i, qq ->
        qq.id to QuestionStat(qq.id, qq.lessonId, 3, if (i < 2) 1 else 3, if (i < 2) SrsCard(0, TODAY) else SrsCard())
    }.toMap() else emptyMap()
    val logs = if (progress) (0..9).associate { TODAY.minusDays(it.toLong()) to DailyLog(TODAY.minusDays(it.toLong()), 120, if (it in listOf(0, 1, 2, 5, 6, 8)) 1 else 0, it == 0, it == 3) } else emptyMap()
    val snap = AppSnapshot(
        profile = Profile("Carlos", character, 100), stats = stats, lessonProgress = lessons, questionStats = qstats,
        dailyLogs = logs, achievements = if (progress) mapOf("streak3" to 1L, "perfect1" to 1L) else emptyMap(),
        shop = ShopState(null, emptySet()), errorBankSize = if (progress) 2 else 0, dueCount = if (progress) 2 else 0,
    )
    val quests = br.com.trilhati.domain.quest.QuestEngine.questsFor(TODAY).mapIndexed { i, d ->
        QuestProgress(TODAY, d.id, if (progress) (if (i == 0) d.target else d.target / 2) else 0, claimed = false)
    }
    return AppState(TODAY, course, snap, quests, Settings(noLives = noLives), pastExams)
}

/** Ranking, amigos e pedidos de exemplo (nomes fictícios), como se tivessem vindo do `social_overview`. */
fun fakeSocial(
    friends: Boolean = true,
    requests: Boolean = true,
    hidden: Boolean = false,
    lastPodium: Boolean = true,
    pending: Int = 0,
    error: String? = null,
): SocialUi {
    fun row(id: String, name: String, mascot: String, xp: Int, streak: Int, pos: Int, state: String) =
        RankingRow(id, name, mascot, xp, streak, pos, state)
    val global = listOf(
        row("u1", "Bia Nogueira", "Tartaruga", 820, 12, 1, if (friends) "friend" else "none"),
        row("me", "Carlos", "Guaiamum", 640, 3, 2, "me"),
        row("u2", "Rafa", "Preguica", 455, 0, 3, "outgoing"),
        row("u3", "Duda Lima", "Tamandua", 380, 7, 4, if (requests) "incoming" else "none"),
        row("u4", "Leo", "Guaiamum", 120, 1, 5, "none"),
        row("u5", "Marina Costa e Silva", "Tartaruga", 0, 0, 6, "none"),
    )
    val friendRows = if (friends) listOf(
        FriendRow("u1", "Bia Nogueira", "Tartaruga", 12, 820, pairStreak = 5, meToday = false, friendToday = true),
        FriendRow("u6", "Jô", "Preguica", 2, 90, pairStreak = 0, meToday = false, friendToday = false),
    ) else emptyList()
    val friendsRanking = if (friends) listOf(
        row("u1", "Bia Nogueira", "Tartaruga", 820, 12, 1, "friend"),
        row("me", "Carlos", "Guaiamum", 640, 3, 2, "me"),
        row("u6", "Jô", "Preguica", 90, 2, 3, "friend"),
    ) else emptyList()
    val overview = SocialOverview(
        profile = RemoteProfile("me", "Carlos", "Guaiamum", "TRI-K7M2Q", showInGlobal = !hidden, streak = 3, streakLongest = 5, lastStudyDay = TODAY.toString()),
        weekStart = "2026-11-16", today = TODAY.toString(),
        global = global, friendsRanking = friendsRanking, friends = friendRows,
        requests = if (requests) listOf(FriendRequestRow("r1", "u3", "Duda Lima", "Tamandua", "2026-11-18T10:00:00Z")) else emptyList(),
        lastWeek = if (lastPodium) LastWeekResult(2, 910, 6) else LastWeekResult(5, 120, 6),
    )
    return SocialUi(
        overview = overview, email = "carlos@exemplo.com", pendingUploads = pending, lastSyncAtMs = NOW_MS - 7 * 60_000,
        lastError = error,
    )
}

fun ComposeContentTestRule.shot(
    name: String,
    dark: Boolean = false,
    palette: br.com.trilhati.ui.theme.ThemePalette? = null,
    content: @Composable () -> Unit,
) {
    mainClock.autoAdvance = false
    setContent {
        TrilhaTheme(palette ?: if (dark) br.com.trilhati.ui.theme.ThemePalette.Escuro else br.com.trilhati.ui.theme.ThemePalette.Claro) { Box(Modifier.background(Trilha.colors.background)) { content() } }
    }
    mainClock.advanceTimeBy(300)
    onRoot().captureToImage().asAndroidBitmap().save(name)
}
