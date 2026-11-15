package br.com.trilhati.data.remote

import br.com.trilhati.data.social.AddByCodeResult
import br.com.trilhati.data.social.PushResult
import br.com.trilhati.data.social.RemoteProfile
import br.com.trilhati.data.social.RemoteSnapshot
import br.com.trilhati.data.social.SessionUpload
import br.com.trilhati.data.social.SocialOverview
import br.com.trilhati.data.social.SubmitResult
import javax.inject.Inject
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.serializer

/** Funções do servidor usadas pelo app (ver supabase/migrations/0001_social.sql). Interface para trocar nos testes. */
interface SocialApi {
    suspend fun myProfile(): ApiResult<RemoteProfile?>
    suspend fun upsertProfile(displayName: String, mascot: String): ApiResult<RemoteProfile>
    suspend fun setShowInGlobal(show: Boolean): ApiResult<RemoteProfile>
    suspend fun updateStreak(streak: Int, longest: Int, lastStudyDay: String?): ApiResult<Unit>
    suspend fun submitSession(session: SessionUpload): ApiResult<SubmitResult>
    suspend fun overview(): ApiResult<SocialOverview>
    suspend fun requestFriend(userId: String): ApiResult<String>
    suspend fun respondRequest(requestId: String, accept: Boolean): ApiResult<String>
    suspend fun addFriendByCode(code: String): ApiResult<AddByCodeResult>
    suspend fun removeFriend(userId: String): ApiResult<String>
    suspend fun pushSnapshot(expectedVersion: Long, deviceId: String, payload: JsonElement): ApiResult<PushResult>
    suspend fun pullSnapshot(): ApiResult<RemoteSnapshot?>
    suspend fun deleteAccount(): ApiResult<Unit>
}

/** Implementação via PostgREST: POST /rest/v1/rpc/<função>, com o token do usuário logado. */
class SupabaseSocialApi @Inject constructor(
    private val config: SupabaseConfig,
    private val http: HttpTransport,
    private val auth: AuthRepository,
) : SocialApi {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun myProfile() = rpc("my_profile", JsonObject(emptyMap())) { decodeNullable(it, RemoteProfile.serializer()) }

    override suspend fun upsertProfile(displayName: String, mascot: String) =
        rpc("upsert_my_profile", buildJsonObject { put("p_display_name", displayName); put("p_mascot", mascot) }) {
            json.decodeFromString(RemoteProfile.serializer(), it)
        }

    override suspend fun setShowInGlobal(show: Boolean) =
        rpc("set_show_in_global", buildJsonObject { put("p_show", show) }) { json.decodeFromString(RemoteProfile.serializer(), it) }

    override suspend fun updateStreak(streak: Int, longest: Int, lastStudyDay: String?) =
        rpc("update_my_streak", buildJsonObject {
            put("p_streak", streak); put("p_longest", longest); put("p_last_study_day", lastStudyDay)
        }) { }

    override suspend fun submitSession(session: SessionUpload) =
        rpc("submit_session", buildJsonObject {
            put("p_id", session.id); put("p_day", session.day); put("p_kind", session.kind); put("p_xp", session.xp)
            put("p_answers", session.answers); put("p_duration_ms", session.durationMs); put("p_streak", session.streak)
            put("p_streak_longest", session.streakLongest); put("p_last_study_day", session.lastStudyDay)
        }) { json.decodeFromString(SubmitResult.serializer(), it) }

    override suspend fun overview() = rpc("social_overview", JsonObject(emptyMap())) { json.decodeFromString(SocialOverview.serializer(), it) }

    override suspend fun requestFriend(userId: String) = rpc("request_friend", buildJsonObject { put("p_user", userId) }) { text(it) }

    override suspend fun respondRequest(requestId: String, accept: Boolean) =
        rpc("respond_friend_request", buildJsonObject { put("p_request", requestId); put("p_accept", accept) }) { text(it) }

    override suspend fun addFriendByCode(code: String) =
        rpc("add_friend_by_code", buildJsonObject { put("p_code", code) }) { json.decodeFromString(AddByCodeResult.serializer(), it) }

    override suspend fun removeFriend(userId: String) = rpc("remove_friend", buildJsonObject { put("p_user", userId) }) { text(it) }

    override suspend fun pushSnapshot(expectedVersion: Long, deviceId: String, payload: JsonElement) =
        rpc("push_snapshot", buildJsonObject {
            put("p_expected_version", expectedVersion); put("p_device_id", deviceId); put("p_payload", payload)
        }) { json.decodeFromString(PushResult.serializer(), it) }

    override suspend fun pullSnapshot() = rpc("pull_snapshot", JsonObject(emptyMap())) { decodeNullable(it, RemoteSnapshot.serializer()) }

    override suspend fun deleteAccount() = rpc("delete_my_account", JsonObject(emptyMap())) { }

    private fun <T> decodeNullable(body: String, serializer: KSerializer<T>): T? {
        val element = json.parseToJsonElement(body)
        return if (element is JsonNull) null else json.decodeFromJsonElement(serializer, element)
    }

    private fun text(body: String): String = json.decodeFromString(serializer<String>(), body)

    private suspend fun <T> rpc(function: String, args: JsonObject, parse: (String) -> T): ApiResult<T> {
        if (!config.isConfigured) return ApiResult.Err(ErrorKind.NotConfigured, "O servidor do app não está configurado.")
        var token = auth.validAccessToken() ?: return ApiResult.Err(ErrorKind.NotSignedIn, "Entre na sua conta de novo.")
        repeat(2) { attempt ->
            val response = try {
                http.post(
                    "${config.url}/rest/v1/rpc/$function",
                    mapOf(
                        "apikey" to config.publishableKey, "Authorization" to "Bearer $token",
                        "Content-Type" to "application/json", "Accept" to "application/json",
                    ),
                    args.toString(),
                )
            } catch (_: NetworkException) {
                return ApiResult.Err(ErrorKind.Network, "Sem conexão. O app tenta de novo sozinho.")
            }
            if (response.code in 200..299) {
                return try {
                    ApiResult.Ok(parse(response.body.ifBlank { "null" }))
                } catch (e: IllegalArgumentException) {
                    ApiResult.Err(ErrorKind.Server, "Resposta inesperada do servidor.")
                }
            }
            // Token vencido no meio do caminho: renova uma vez e tenta de novo.
            if (response.code == 401 && attempt == 0) {
                token = auth.validAccessToken(forceRefresh = true) ?: return ApiResult.Err(ErrorKind.NotSignedIn, "Entre na sua conta de novo.")
                return@repeat
            }
            return ApiResult.Err(classify(response), messageFor(response))
        }
        return ApiResult.Err(ErrorKind.NotSignedIn, "Entre na sua conta de novo.")
    }

    private fun pgCode(response: HttpResponse): String =
        runCatching { json.parseToJsonElement(response.body).jsonObject["code"]?.jsonPrimitive?.contentOrNull }.getOrNull().orEmpty()

    private fun classify(response: HttpResponse): ErrorKind = when (pgCode(response)) {
        "P0002" -> ErrorKind.ProfileMissing
        "28000" -> ErrorKind.NotSignedIn
        "PGRST202", "42883" -> ErrorKind.NotConfigured
        else -> if (response.code == 401 || response.code == 403) ErrorKind.NotSignedIn else ErrorKind.Server
    }

    private fun messageFor(response: HttpResponse): String = when (classify(response)) {
        ErrorKind.ProfileMissing -> "Crie seu perfil para continuar."
        ErrorKind.NotSignedIn -> "Entre na sua conta de novo."
        ErrorKind.NotConfigured -> "O banco do servidor ainda não foi configurado (falta rodar o SQL do app no Supabase)."
        else -> "O servidor respondeu com erro (${response.code}). Tente de novo mais tarde."
    }
}
