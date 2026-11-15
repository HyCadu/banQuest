package br.com.trilhati.data.update

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.updateStore by preferencesDataStore(name = "updates")
private val json = Json { ignoreUnknownKeys = true }

/** Última versão vista no servidor e o que o aluno já viu dela. Não depende da conta (sobrevive ao sair). */
data class UpdateMeta(
    val latest: AppRelease?,
    val lastCheckMs: Long,
    val notifiedCode: Int,
    val dismissedCode: Int,
)

@Singleton
class UpdateStore @Inject constructor(@ApplicationContext private val context: Context) {
    private object K {
        val latest = stringPreferencesKey("latest_release")
        val lastCheck = longPreferencesKey("last_check_ms")
        val notified = intPreferencesKey("notified_code")
        val dismissed = intPreferencesKey("dismissed_code")
    }

    val meta: Flow<UpdateMeta> = context.updateStore.data.map { p ->
        UpdateMeta(
            latest = p[K.latest]?.let { runCatching { json.decodeFromString(AppRelease.serializer(), it) }.getOrNull() },
            lastCheckMs = p[K.lastCheck] ?: 0,
            notifiedCode = p[K.notified] ?: 0,
            dismissedCode = p[K.dismissed] ?: 0,
        )
    }

    suspend fun current(): UpdateMeta = meta.first()

    suspend fun setLatest(release: AppRelease?, atMs: Long) = context.updateStore.edit {
        if (release == null) it.remove(K.latest) else it[K.latest] = json.encodeToString(AppRelease.serializer(), release)
        it[K.lastCheck] = atMs
    }

    suspend fun setNotified(code: Int) = context.updateStore.edit { it[K.notified] = code }
    suspend fun setDismissed(code: Int) = context.updateStore.edit { it[K.dismissed] = code }
}
