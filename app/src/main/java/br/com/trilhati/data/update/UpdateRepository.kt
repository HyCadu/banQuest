package br.com.trilhati.data.update

import br.com.trilhati.data.remote.ApiResult
import br.com.trilhati.data.remote.SocialApi
import br.com.trilhati.domain.update.UpdateRule
import br.com.trilhati.domain.update.UpdateStatus
import br.com.trilhati.notify.Notifications
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Aviso de nova versão. Pergunta ao servidor qual é a versão mais nova (`latest_release`, sem precisar de login),
 * guarda a resposta (para mostrar offline) e avisa por notificação uma vez por versão. O download é só um link:
 * o app não instala nada sozinho (sem permissão extra).
 */
@Singleton
class UpdateRepository @Inject constructor(
    private val api: SocialApi,
    private val store: UpdateStore,
    private val notifications: Notifications,
    private val version: AppVersion,
    private val clock: Clock,
) {
    private val lock = Mutex()

    val ui: Flow<UpdateUi> = store.meta.map { meta ->
        val status = UpdateRule.status(version.code, meta.latest?.toInfo())
        UpdateUi(
            current = version, status = status, showBanner = UpdateRule.showBanner(status, meta.dismissedCode),
            lastCheckMs = meta.lastCheckMs,
        )
    }

    /** Confere o servidor. Sem [force], respeita o intervalo mínimo entre conferências. */
    suspend fun check(force: Boolean = false): ApiResult<UpdateStatus> = lock.withLock {
        val meta = store.current()
        val now = clock.millis()
        if (!force && !UpdateRule.shouldCheck(now, meta.lastCheckMs)) {
            return@withLock ApiResult.Ok(UpdateRule.status(version.code, meta.latest?.toInfo()))
        }
        when (val r = api.latestRelease()) {
            is ApiResult.Err -> r
            is ApiResult.Ok -> {
                store.setLatest(r.value, now)
                val status = UpdateRule.status(version.code, r.value?.toInfo())
                if (UpdateRule.shouldNotify(status, meta.notifiedCode)) {
                    status.releaseOrNull?.let { release ->
                        val required = status is UpdateStatus.Required
                        notifications.postUpdate(
                            title = if (required) "Atualize o BanQuest" else "BanQuest ${release.versionName} disponível",
                            text = release.notes.ifBlank { "Toque para baixar a versão nova." },
                            url = release.apkUrl,
                        )
                        store.setNotified(release.versionCode)
                    }
                }
                ApiResult.Ok(status)
            }
        }
    }

    /** "Agora não": esconde o aviso da Trilha até sair uma versão ainda mais nova. */
    suspend fun dismiss(versionCode: Int) = store.setDismissed(versionCode)
}
