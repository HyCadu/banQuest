package br.com.trilhati.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import br.com.trilhati.domain.pace.PaceConfig
import br.com.trilhati.domain.pace.TimerMode
import br.com.trilhati.ui.theme.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsStore by preferencesDataStore(name = "settings")

/** Preferências do aluno (PLAN.md §7, Configurações). */
data class Settings(
    val timerMode: TimerMode = TimerMode.Pace,
    val paceTargetMs: Long = PaceConfig.STANDARD_MS,
    val caseFourMinutes: Boolean = false,
    val noLives: Boolean = false,
    val haptics: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.System,
) {
    val paceConfig: PaceConfig get() = PaceConfig(timerMode, paceTargetMs, caseFourMinutes)
}

@Singleton
class SettingsRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private object Keys {
        val timerMode = stringPreferencesKey("timer_mode")
        val paceTarget = longPreferencesKey("pace_target_ms")
        val caseFour = booleanPreferencesKey("case_four_minutes")
        val noLives = booleanPreferencesKey("no_lives")
        val haptics = booleanPreferencesKey("haptics")
        val theme = stringPreferencesKey("theme")
    }

    val settings: Flow<Settings> = context.settingsStore.data.map { p -> p.toSettings() }

    private fun Preferences.toSettings() = Settings(
        timerMode = this[Keys.timerMode]?.let { runCatching { TimerMode.valueOf(it) }.getOrNull() } ?: TimerMode.Pace,
        paceTargetMs = this[Keys.paceTarget]?.takeIf { it == PaceConfig.STANDARD_MS || it == PaceConfig.TIGHT_MS } ?: PaceConfig.STANDARD_MS,
        caseFourMinutes = this[Keys.caseFour] ?: false,
        noLives = this[Keys.noLives] ?: false,
        haptics = this[Keys.haptics] ?: true,
        themeMode = this[Keys.theme]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.System,
    )

    suspend fun setTimerMode(mode: TimerMode) = context.settingsStore.edit { it[Keys.timerMode] = mode.name }
    suspend fun setPaceTarget(ms: Long) = context.settingsStore.edit { it[Keys.paceTarget] = ms }
    suspend fun setCaseFourMinutes(on: Boolean) = context.settingsStore.edit { it[Keys.caseFour] = on }
    suspend fun setNoLives(on: Boolean) = context.settingsStore.edit { it[Keys.noLives] = on }
    suspend fun setHaptics(on: Boolean) = context.settingsStore.edit { it[Keys.haptics] = on }
    suspend fun setTheme(mode: ThemeMode) = context.settingsStore.edit { it[Keys.theme] = mode.name }
    suspend fun eraseAll() = context.settingsStore.edit { it.clear() }
}
