package br.com.trilhati.data.remote

import br.com.trilhati.data.social.AuthStore
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Login por código de e-mail e manutenção da sessão (renova sozinha antes de vencer). */
@Singleton
class AuthRepository @Inject constructor(
    private val api: SupabaseAuthApi,
    private val store: AuthStore,
    private val clock: Clock,
) {
    private val refreshLock = Mutex()

    val session: Flow<AuthSession?> = store.session

    suspend fun current(): AuthSession? = store.current()

    suspend fun sendCode(email: String): ApiResult<Unit> = api.sendCode(email.trim().lowercase())

    suspend fun verifyCode(email: String, code: String): ApiResult<AuthSession> {
        val result = api.verifyCode(email.trim().lowercase(), code.filter(Char::isDigit))
        if (result is ApiResult.Ok) store.save(result.value)
        return result
    }

    /**
     * Token válido para chamar o servidor. Renova se vence em menos de 1 minuto. Sem internet, devolve o atual
     * (a chamada vai falhar por rede, e a sincronização tenta de novo depois). Se a renovação for recusada, a sessão
     * acabou: o app volta para o login.
     */
    suspend fun validAccessToken(forceRefresh: Boolean = false): String? = refreshLock.withLock {
        val session = store.current() ?: return@withLock null
        val nowSec = clock.millis() / 1000
        if (!forceRefresh && session.expiresAtSec - nowSec > 60) return@withLock session.accessToken
        when (val refreshed = api.refresh(session)) {
            is ApiResult.Ok -> {
                store.save(refreshed.value)
                refreshed.value.accessToken
            }
            is ApiResult.Err -> if (refreshed.kind == ErrorKind.Network) {
                session.accessToken
            } else {
                store.clear()
                null
            }
        }
    }

    suspend fun signOut() {
        store.current()?.let { api.signOut(it.accessToken) }
        store.clear()
    }

    /** Usado depois de excluir a conta no servidor: só apaga a sessão local. */
    suspend fun forgetSession() = store.clear()
}
