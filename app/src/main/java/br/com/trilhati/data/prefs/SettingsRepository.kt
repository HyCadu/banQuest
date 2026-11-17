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
    val reminderEnabled: Boolean = true,
    val reminderMinutes: Int = 19 * 60,
    val reminderPromptDismissed: Boolean = false,
    val sounds: Boolean = true,
    /** Tema escolhido (Configurações › Tema); `null` enquanto não escolheu: segue o claro/escuro do aparelho. */
    val palette: br.com.trilhati.ui.theme.ThemePalette? = null,
    /** O aluno já escolheu o tema? Se não, a escolha aparece ao entrar. */
    val themeChosen: Boolean = false,
    /** Trilha aberta na aba Trilha (Específicos ou Gerais). */
    val trailTrack: br.com.trilhati.domain.model.Track = br.com.trilhati.domain.model.Track.Specific,
) {
    val reminderTime: java.time.LocalTime get() = java.time.LocalTime.of(reminderMinutes / 60, reminderMinutes % 60)

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
        /** Modo claro/escuro das versões até a 0.7.3 ("System", "Light" ou "Dark"); só é lido para migrar. */
        val theme = stringPreferencesKey("theme")
        val reminderEnabled = booleanPreferencesKey("reminder_enabled")
        val reminderMinutes = androidx.datastore.preferences.core.intPreferencesKey("reminder_minutes")
        val reminderPromptDismissed = booleanPreferencesKey("reminder_prompt_dismissed")
        val sounds = booleanPreferencesKey("sounds")
        val palette = stringPreferencesKey("palette")
        val themeChosen = booleanPreferencesKey("theme_chosen")
        val trailTrack = stringPreferencesKey("trail_track")
    }

    val settings: Flow<Settings> = context.settingsStore.data.map { p -> p.toSettings() }

    private fun Preferences.toSettings() = Settings(
        timerMode = this[Keys.timerMode]?.let { runCatching { TimerMode.valueOf(it) }.getOrNull() } ?: TimerMode.Pace,
        paceTargetMs = this[Keys.paceTarget]?.takeIf { it == PaceConfig.STANDARD_MS || it == PaceConfig.TIGHT_MS } ?: PaceConfig.STANDARD_MS,
        caseFourMinutes = this[Keys.caseFour] ?: false,
        noLives = this[Keys.noLives] ?: false,
        haptics = this[Keys.haptics] ?: true,
        reminderEnabled = this[Keys.reminderEnabled] ?: true,
        reminderMinutes = this[Keys.reminderMinutes]?.coerceIn(0, 24 * 60 - 1) ?: (19 * 60),
        reminderPromptDismissed = this[Keys.reminderPromptDismissed] ?: false,
        sounds = this[Keys.sounds] ?: true,
        palette = this[Keys.palette]?.let { runCatching { br.com.trilhati.ui.theme.ThemePalette.valueOf(it) }.getOrNull() }
            ?: legacyPalette(this[Keys.theme]),
        themeChosen = this[Keys.themeChosen] ?: false,
        trailTrack = this[Keys.trailTrack]?.let { runCatching { br.com.trilhati.domain.model.Track.valueOf(it) }.getOrNull() }
            ?: br.com.trilhati.domain.model.Track.Specific,
    )

    suspend fun setTimerMode(mode: TimerMode) = context.settingsStore.edit { it[Keys.timerMode] = mode.name }
    suspend fun setPaceTarget(ms: Long) = context.settingsStore.edit { it[Keys.paceTarget] = ms }
    suspend fun setCaseFourMinutes(on: Boolean) = context.settingsStore.edit { it[Keys.caseFour] = on }
    suspend fun setNoLives(on: Boolean) = context.settingsStore.edit { it[Keys.noLives] = on }
    suspend fun setHaptics(on: Boolean) = context.settingsStore.edit { it[Keys.haptics] = on }
    suspend fun setReminderEnabled(on: Boolean) = context.settingsStore.edit { it[Keys.reminderEnabled] = on }
    suspend fun setReminderMinutes(minutes: Int) = context.settingsStore.edit { it[Keys.reminderMinutes] = minutes.coerceIn(0, 24 * 60 - 1) }
    suspend fun dismissReminderPrompt() = context.settingsStore.edit { it[Keys.reminderPromptDismissed] = true }
    suspend fun setSounds(on: Boolean) = context.settingsStore.edit { it[Keys.sounds] = on }
    suspend fun setPalette(palette: br.com.trilhati.ui.theme.ThemePalette) = context.settingsStore.edit { it[Keys.palette] = palette.name }
    /** Grava o tema da primeira entrada (mesmo que o aluno só tenha confirmado o que já vinha marcado). */
    suspend fun confirmThemeChoice(palette: br.com.trilhati.ui.theme.ThemePalette) = context.settingsStore.edit {
        it[Keys.palette] = palette.name
        it[Keys.themeChosen] = true
    }
    suspend fun setTrailTrack(track: br.com.trilhati.domain.model.Track) = context.settingsStore.edit { it[Keys.trailTrack] = track.name }
    suspend fun eraseAll() = context.settingsStore.edit { it.clear() }
}

/** Converte o modo claro/escuro antigo no tema equivalente; "System" (ou nada) continua seguindo o aparelho. */
internal fun legacyPalette(mode: String?): br.com.trilhati.ui.theme.ThemePalette? = when (mode) {
    "Light" -> br.com.trilhati.ui.theme.ThemePalette.Claro
    "Dark" -> br.com.trilhati.ui.theme.ThemePalette.Escuro
    else -> null
}
