package br.com.trilhati.data.remote

import java.time.Clock
import javax.inject.Inject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/** Sessão de login guardada no aparelho. */
@Serializable
data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresAtSec: Long,
    val userId: String,
    val email: String,
)

enum class ErrorKind { Network, NotSignedIn, InvalidCode, RateLimited, InvalidEmail, NotConfigured, ProfileMissing, Server }

/** Resultado de uma chamada remota: valor ou erro já com a mensagem para o aluno. */
sealed interface ApiResult<out T> {
    data class Ok<T>(val value: T) : ApiResult<T>
    data class Err(val kind: ErrorKind, val message: String) : ApiResult<Nothing>
}

/**
 * Login por código de e-mail pela API REST do Supabase Auth (GoTrue):
 *  - POST /auth/v1/otp     envia o código de 6 dígitos (cria a conta se for o primeiro acesso);
 *  - POST /auth/v1/verify  troca e-mail + código por uma sessão;
 *  - POST /auth/v1/token?grant_type=refresh_token  renova a sessão.
 */
class SupabaseAuthApi @Inject constructor(
    private val config: SupabaseConfig,
    private val http: HttpTransport,
    private val clock: Clock,
) {
    private val json = Json { ignoreUnknownKeys = true }

    private fun headers(accessToken: String? = null) = buildMap {
        put("apikey", config.publishableKey)
        put("Content-Type", "application/json")
        put("Accept", "application/json")
        if (accessToken != null) put("Authorization", "Bearer $accessToken")
    }

    suspend fun sendCode(email: String): ApiResult<Unit> = call("/auth/v1/otp", buildJsonObject {
        put("email", email)
        put("create_user", true)
    }) { }

    suspend fun verifyCode(email: String, code: String): ApiResult<AuthSession> = call("/auth/v1/verify", buildJsonObject {
        put("type", "email")
        put("email", email)
        put("token", code)
    }) { body -> parseSession(body, email) }

    suspend fun refresh(session: AuthSession): ApiResult<AuthSession> =
        call("/auth/v1/token?grant_type=refresh_token", buildJsonObject { put("refresh_token", session.refreshToken) }) { body ->
            parseSession(body, session.email)
        }

    suspend fun signOut(accessToken: String) {
        try {
            http.post(config.url + "/auth/v1/logout", headers(accessToken), "{}")
        } catch (_: NetworkException) {
            // Sair funciona offline: a sessão local é apagada de qualquer jeito.
        }
    }

    private suspend fun <T> call(path: String, body: JsonObject, parse: (String) -> T): ApiResult<T> {
        if (!config.isConfigured) return ApiResult.Err(ErrorKind.NotConfigured, "O servidor do app não está configurado.")
        val response = try {
            http.post(config.url + path, headers(), body.toString())
        } catch (_: NetworkException) {
            return ApiResult.Err(ErrorKind.Network, "Sem conexão com a internet. Tente de novo.")
        }
        if (response.code in 200..299) {
            return try {
                ApiResult.Ok(parse(response.body))
            } catch (e: IllegalArgumentException) {
                ApiResult.Err(ErrorKind.Server, "Resposta inesperada do servidor.")
            }
        }
        return ApiResult.Err(classify(response), messageFor(response))
    }

    private fun parseSession(body: String, email: String): AuthSession {
        val dto = json.decodeFromString(TokenResponse.serializer(), body)
        val expiresAt = dto.expiresAt ?: (clock.millis() / 1000 + (dto.expiresIn ?: 3600))
        return AuthSession(dto.accessToken, dto.refreshToken, expiresAt, dto.user.id, dto.user.email ?: email)
    }

    private fun errorCode(response: HttpResponse): String {
        val obj = runCatching { json.parseToJsonElement(response.body).jsonObject }.getOrNull() ?: return ""
        return listOf("error_code", "error", "code").firstNotNullOfOrNull { key ->
            obj[key]?.jsonPrimitive?.contentOrNull?.takeIf { it.any(Char::isLetter) }
        }.orEmpty()
    }

    private fun classify(response: HttpResponse): ErrorKind {
        val code = errorCode(response)
        return when {
            response.code == 429 || code.contains("rate_limit") -> ErrorKind.RateLimited
            code.contains("otp_expired") || code.contains("invalid_grant") || code.contains("otp") -> ErrorKind.InvalidCode
            code.contains("email_address_invalid") || code.contains("validation_failed") -> ErrorKind.InvalidEmail
            response.code == 401 || response.code == 403 -> ErrorKind.InvalidCode
            else -> ErrorKind.Server
        }
    }

    private fun messageFor(response: HttpResponse): String {
        val code = errorCode(response)
        return when {
            response.code == 429 || code.contains("rate_limit") ->
                "Muitos pedidos de código seguidos. Espere alguns minutos e tente de novo."
            code.contains("email_address_not_authorized") ->
                "O servidor ainda não pode mandar e-mail para esse endereço. O dono do app precisa configurar o envio de e-mail (SMTP) no Supabase."
            code.contains("signup_disabled") -> "Cadastro de novas contas está desligado no servidor."
            code.contains("otp_expired") || code.contains("otp") || code.contains("invalid_grant") -> "Código inválido ou vencido. Peça um novo."
            code.contains("email_address_invalid") || code.contains("validation_failed") -> "E-mail inválido."
            response.code >= 500 -> "O servidor está indisponível agora. Tente de novo em instantes."
            else -> "Não deu certo (erro ${response.code}). Tente de novo."
        }
    }

    @Serializable
    private data class TokenResponse(
        @SerialName("access_token") val accessToken: String,
        @SerialName("refresh_token") val refreshToken: String,
        @SerialName("expires_in") val expiresIn: Long? = null,
        @SerialName("expires_at") val expiresAt: Long? = null,
        val user: TokenUser,
    )

    @Serializable
    private data class TokenUser(val id: String, val email: String? = null)
}
