package br.com.trilhati.notify

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import br.com.trilhati.data.prefs.SettingsRepository
import br.com.trilhati.data.repo.ProgressRepository
import br.com.trilhati.domain.notify.ReminderKind
import br.com.trilhati.domain.notify.ReminderPolicy
import br.com.trilhati.domain.streak.StreakEngine
import br.com.trilhati.domain.time.APP_ZONE
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import java.time.LocalDateTime
import kotlinx.coroutines.flow.first

/** Acesso às dependências do Hilt a partir do worker, sem precisar de biblioteca extra. */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface ReminderEntryPoint {
    fun progress(): ProgressRepository
    fun settings(): SettingsRepository
    fun scheduler(): ReminderScheduler
    fun notifications(): Notifications
    fun clock(): Clock
}

/** Dispara no horário agendado, decide se avisa (regra pura em [ReminderPolicy]) e agenda o próximo. */
class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val deps = EntryPointAccessors.fromApplication(applicationContext, ReminderEntryPoint::class.java)
        val clock = deps.clock()
        val settings = deps.settings().settings.first()
        val now = LocalDateTime.now(clock.withZone(APP_ZONE))
        val kind = inputData.getString(KEY_KIND)?.let { runCatching { ReminderKind.valueOf(it) }.getOrNull() } ?: ReminderKind.Daily
        val scheduledAt = inputData.getString(KEY_AT)?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() } ?: now
        val today = now.toLocalDate()
        val snapshot = deps.progress().snapshot(today).first()
        val streak = snapshot.stats.streak
        val message = ReminderPolicy.decide(
            kind = kind, now = now, scheduledAt = scheduledAt, enabled = settings.reminderEnabled,
            lessonsToday = snapshot.dailyLogs[today]?.lessons ?: 0,
            streakStatus = StreakEngine.status(streak, today), streakCount = StreakEngine.displayCount(streak, today),
            character = snapshot.profile.character,
        )
        message?.let { deps.notifications().post(it) }
        deps.scheduler().schedule(settings.reminderEnabled, settings.reminderTime)
        return Result.success()
    }

    companion object {
        const val KEY_KIND = "kind"
        const val KEY_AT = "at"
    }
}
