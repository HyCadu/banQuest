package br.com.trilhati.notify

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import br.com.trilhati.domain.notify.ReminderPolicy
import br.com.trilhati.domain.time.APP_ZONE
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Clock
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Agenda o próximo lembrete com WorkManager (PLAN.md §8): um `OneTimeWork` recalculado a cada disparo, sem alarmes exatos.
 * O WorkManager já reagenda o trabalho pendente quando o aparelho reinicia.
 */
@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val clock: Clock,
) {
    fun schedule(enabled: Boolean, time: LocalTime) {
        val wm = WorkManager.getInstance(context)
        if (!enabled) {
            wm.cancelUniqueWork(WORK_NAME)
            return
        }
        val now = LocalDateTime.now(clock.withZone(APP_ZONE))
        val trigger = ReminderPolicy.next(now, time)
        val delay = Duration.between(now, trigger.at)
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay)
            .setInputData(workDataOf(ReminderWorker.KEY_KIND to trigger.kind.name, ReminderWorker.KEY_AT to trigger.at.toString()))
            .build()
        wm.enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    companion object {
        const val WORK_NAME = "trilha_lembrete"
    }
}
