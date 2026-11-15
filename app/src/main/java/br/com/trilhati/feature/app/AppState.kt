package br.com.trilhati.feature.app

import androidx.compose.runtime.Immutable
import br.com.trilhati.data.prefs.Settings
import br.com.trilhati.data.repo.AppSnapshot
import br.com.trilhati.domain.hearts.HeartsEngine
import br.com.trilhati.domain.model.CourseUnit
import br.com.trilhati.domain.progress.DailyLog
import br.com.trilhati.domain.progress.LessonStatus
import br.com.trilhati.domain.progress.QuestProgress
import br.com.trilhati.domain.progress.TrailEngine
import br.com.trilhati.domain.streak.StreakEngine
import br.com.trilhati.domain.streak.StreakStatus
import br.com.trilhati.domain.time.daysToExam
import java.time.LocalDate

/** Tudo que as abas precisam para se desenhar. Reemitido quando o banco, as preferências ou o dia mudam. */
@Immutable
data class AppState(
    val today: LocalDate,
    val course: List<CourseUnit>,
    val snapshot: AppSnapshot,
    val quests: List<QuestProgress>,
    val settings: Settings,
) {
    val statuses: Map<String, LessonStatus> = TrailEngine.statuses(course, snapshot.lessonProgress)
    val daysToExam: Int = daysToExam(today)
    val streakCount: Int = StreakEngine.displayCount(snapshot.stats.streak, today)
    val streakStatus: StreakStatus = StreakEngine.status(snapshot.stats.streak, today)
    val todayLog: DailyLog = snapshot.dailyLogs[today] ?: DailyLog(today)
    val completedUnitIds: Set<String> = course.filter { TrailEngine.isUnitComplete(it, snapshot.lessonProgress) }.map { it.id }.toSet()

    /** Lição que o aluno deve fazer agora (a primeira disponível), se houver. */
    val currentLessonId: String? = statuses.entries.firstOrNull { it.value == LessonStatus.Available }?.key

    /** Corações disponíveis em [nowMs], já contando a regeneração por tempo. */
    fun hearts(nowMs: Long): Int = HeartsEngine.regen(snapshot.stats.hearts, nowMs).hearts

    fun heartsMsUntilNext(nowMs: Long): Long? = HeartsEngine.msUntilNext(snapshot.stats.hearts, nowMs)

    val hasAnyCompletedLesson: Boolean get() = snapshot.lessonProgress.values.any { it.completed }

    /** Concluiu uma lição hoje neste aparelho (é o que conta para as ofensivas), mesmo antes de sincronizar. */
    val studiedToday: Boolean get() = todayLog.lessons > 0
}

/**
 * Lado social que as telas mostram: o último ranking baixado (cache, para ver offline) e o estado da sincronização.
 * [overview] nulo = ainda não baixou nada (primeira vez sem internet).
 */
@Immutable
data class SocialUi(
    val overview: br.com.trilhati.data.social.SocialOverview? = null,
    val email: String? = null,
    val pendingUploads: Int = 0,
    val lastSyncAtMs: Long = 0,
    val lastError: String? = null,
    val refreshing: Boolean = false,
    val conflict: br.com.trilhati.data.social.SyncConflict? = null,
) {
    val friendCode: String? get() = overview?.profile?.friendCode
    val showInGlobal: Boolean get() = overview?.profile?.showInGlobal ?: true
}

/** Ações que as abas disparam. Mantidas num objeto único para as telas serem fáceis de pré-visualizar. */
data class AppActions(
    val openLesson: (String) -> Unit = {},
    val openErrors: () -> Unit = {},
    val openReview: () -> Unit = {},
    val openSettings: () -> Unit = {},
    val buy: (br.com.trilhati.domain.progress.ShopItem) -> Unit = {},
    val claimQuest: (String) -> Unit = {},
    val claimDailyChest: () -> Unit = {},
    val claimUnitChest: (String) -> Unit = {},
    val setCharacter: (String) -> Unit = {},
    val setName: (String) -> Unit = {},
    val setGoal: (Int) -> Unit = {},
    val message: (String) -> Unit = {},
    val openShop: () -> Unit = {},
    val enableReminders: () -> Unit = {},
    val dismissReminderPrompt: () -> Unit = {},
    // Social (aba Ranking)
    val refreshSocial: () -> Unit = {},
    val requestFriend: (String) -> Unit = {},
    val respondRequest: (requestId: String, accept: Boolean) -> Unit = { _, _ -> },
    val addFriendByCode: (String) -> Unit = {},
    val removeFriend: (String) -> Unit = {},
    val shareFriendCode: (String) -> Unit = {},
    val openRanking: () -> Unit = {},
)
