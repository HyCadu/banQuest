package br.com.trilhati.data.social

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import br.com.trilhati.data.remote.ApiResult
import br.com.trilhati.data.remote.ErrorKind
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SyncEntryPoint {
    fun sync(): SyncRepository
    fun updates(): br.com.trilhati.data.update.UpdateRepository
}

/** Roda uma sincronização em segundo plano. Sem rede, o WorkManager tenta de novo mais tarde. */
class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val entry = EntryPointAccessors.fromApplication(applicationContext, SyncEntryPoint::class.java)
        // Aviso de versão nova chega mesmo com o app fechado (no máximo uma conferência a cada 30 min).
        entry.updates().check()
        return when (val result = entry.sync().syncNow()) {
            is ApiResult.Ok -> Result.success()
            is ApiResult.Err -> if (result.kind == ErrorKind.Network || result.kind == ErrorKind.Server) Result.retry() else Result.success()
        }
    }
}

/** Agenda a sincronização: a cada ~1 h (quando há rede) e "logo" depois de uma lição ou ao abrir o app. */
@Singleton
class SyncScheduler @Inject constructor(@ApplicationContext private val context: Context) {
    private val online = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    fun ensurePeriodic() {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(1, TimeUnit.HOURS).setConstraints(online).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(PERIODIC, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    fun syncSoon() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>().setConstraints(online).build()
        WorkManager.getInstance(context).enqueueUniqueWork(ONE_SHOT, ExistingWorkPolicy.REPLACE, request)
    }

    fun cancelAll() {
        WorkManager.getInstance(context).cancelUniqueWork(PERIODIC)
        WorkManager.getInstance(context).cancelUniqueWork(ONE_SHOT)
    }

    companion object {
        private const val PERIODIC = "trilha_sync_periodic"
        private const val ONE_SHOT = "trilha_sync_now"
    }
}
