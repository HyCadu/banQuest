package br.com.trilhati.data.social

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import br.com.trilhati.data.remote.AuthSession
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val Context.authStore by preferencesDataStore(name = "auth")
private val Context.syncStore by preferencesDataStore(name = "sync")
private val json = Json { ignoreUnknownKeys = true }

/** Sessão de login (fica só neste aparelho, na área privada do app). */
@Singleton
class AuthStore @Inject constructor(@ApplicationContext private val context: Context) {
    private val key = stringPreferencesKey("session")

    val session: Flow<AuthSession?> = context.authStore.data.map { p ->
        p[key]?.let { runCatching { json.decodeFromString(AuthSession.serializer(), it) }.getOrNull() }
    }

    suspend fun current(): AuthSession? = session.first()
    suspend fun save(session: AuthSession) = context.authStore.edit { it[key] = json.encodeToString(AuthSession.serializer(), session) }
    suspend fun clear() = context.authStore.edit { it.clear() }
}

/** Conflito entre o progresso deste aparelho e o da nuvem, para o aluno decidir qual manter. */
@Serializable
data class SyncConflict(
    val serverVersion: Long,
    val serverDevice: String,
    val serverUpdatedAt: String,
    val serverXp: Int,
    val serverLessons: Int,
    val localXp: Int,
    val localLessons: Int,
)

/** Estado da sincronização e cache do social, para mostrar tudo mesmo offline. */
data class SyncMeta(
    val deviceId: String?,
    val profileReady: Boolean,
    val snapshotVersion: Long,
    val lastPushedHash: String?,
    val conflict: SyncConflict?,
    val overview: SocialOverview?,
    val lastSyncAtMs: Long,
    val lastError: String?,
    val seenRequestIds: Set<String>,
    val lastStreakSent: String?,
)

@Singleton
class SyncStore @Inject constructor(@ApplicationContext private val context: Context) {
    private object K {
        val deviceId = stringPreferencesKey("device_id")
        val profileReady = booleanPreferencesKey("profile_ready")
        val snapshotVersion = longPreferencesKey("snapshot_version")
        val lastPushedHash = stringPreferencesKey("last_pushed_hash")
        val conflict = stringPreferencesKey("conflict")
        val overview = stringPreferencesKey("overview")
        val lastSyncAt = longPreferencesKey("last_sync_at")
        val lastError = stringPreferencesKey("last_error")
        val seenRequests = stringPreferencesKey("seen_requests")
        val lastStreakSent = stringPreferencesKey("last_streak_sent")
    }

    val meta: Flow<SyncMeta> = context.syncStore.data.map { it.toMeta() }

    private fun Preferences.toMeta() = SyncMeta(
        deviceId = this[K.deviceId],
        profileReady = this[K.profileReady] ?: false,
        snapshotVersion = this[K.snapshotVersion] ?: 0,
        lastPushedHash = this[K.lastPushedHash],
        conflict = this[K.conflict]?.let { runCatching { json.decodeFromString(SyncConflict.serializer(), it) }.getOrNull() },
        overview = this[K.overview]?.let { runCatching { json.decodeFromString(SocialOverview.serializer(), it) }.getOrNull() },
        lastSyncAtMs = this[K.lastSyncAt] ?: 0,
        lastError = this[K.lastError],
        seenRequestIds = this[K.seenRequests]?.split(',')?.filter { it.isNotBlank() }?.toSet().orEmpty(),
        lastStreakSent = this[K.lastStreakSent],
    )

    suspend fun current(): SyncMeta = meta.first()

    /** Identificador deste aparelho (aparece no aviso de conflito como "outro aparelho"). Criado uma vez. */
    suspend fun deviceId(): String {
        current().deviceId?.let { return it }
        val id = "android-" + UUID.randomUUID().toString().take(8)
        context.syncStore.edit { it[K.deviceId] = id }
        return id
    }

    suspend fun setProfileReady(ready: Boolean) = context.syncStore.edit { it[K.profileReady] = ready }
    suspend fun setSnapshot(version: Long, hash: String?) = context.syncStore.edit {
        it[K.snapshotVersion] = version
        if (hash == null) it.remove(K.lastPushedHash) else it[K.lastPushedHash] = hash
    }
    suspend fun setConflict(conflict: SyncConflict?) = context.syncStore.edit {
        if (conflict == null) it.remove(K.conflict) else it[K.conflict] = json.encodeToString(SyncConflict.serializer(), conflict)
    }
    suspend fun setOverview(overview: SocialOverview, atMs: Long) = context.syncStore.edit {
        it[K.overview] = json.encodeToString(SocialOverview.serializer(), overview)
        it[K.lastSyncAt] = atMs
        it.remove(K.lastError)
    }
    suspend fun setError(message: String?) = context.syncStore.edit {
        if (message == null) it.remove(K.lastError) else it[K.lastError] = message
    }
    suspend fun markRequestsSeen(ids: Set<String>) = context.syncStore.edit { it[K.seenRequests] = ids.joinToString(",") }
    suspend fun setLastStreakSent(value: String) = context.syncStore.edit { it[K.lastStreakSent] = value }

    /** Ao sair da conta: esquece tudo, menos o id do aparelho. */
    suspend fun clearAccount() = context.syncStore.edit { prefs ->
        val device = prefs[K.deviceId]
        prefs.clear()
        device?.let { prefs[K.deviceId] = it }
    }
}
