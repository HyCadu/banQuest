package br.com.trilhati.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** `true` quando o aluno desligou as animações do sistema (Acessibilidade > Remover animações). */
val LocalReducedMotion = staticCompositionLocalOf { false }

enum class ThemeMode { System, Light, Dark }

/** Acesso aos tokens: `Trilha.colors.primary`. */
object Trilha {
    val colors: TrilhaColors
        @Composable @ReadOnlyComposable get() = LocalTrilhaColors.current
}

@Composable
fun TrilhaTheme(mode: ThemeMode = ThemeMode.System, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    val c = if (dark) DarkTrilhaColors else LightTrilhaColors
    val scheme = if (dark) {
        darkColorScheme(
            primary = c.primary, onPrimary = c.onPrimary, primaryContainer = c.primarySoft, onPrimaryContainer = c.ink,
            secondary = c.secondary, onSecondary = c.onSecondary, secondaryContainer = c.secondarySoft, onSecondaryContainer = c.ink,
            background = c.background, onBackground = c.ink, surface = c.surface, onSurface = c.ink,
            surfaceVariant = c.surfaceAlt, onSurfaceVariant = c.inkSoft, outline = c.outline,
            error = c.error, onError = c.onError, errorContainer = c.errorSoft, onErrorContainer = c.errorText,
        )
    } else {
        lightColorScheme(
            primary = c.primary, onPrimary = c.onPrimary, primaryContainer = c.primarySoft, onPrimaryContainer = c.ink,
            secondary = c.secondary, onSecondary = c.onSecondary, secondaryContainer = c.secondarySoft, onSecondaryContainer = c.ink,
            background = c.background, onBackground = c.ink, surface = c.surface, onSurface = c.ink,
            surfaceVariant = c.surfaceAlt, onSurfaceVariant = c.inkSoft, outline = c.outline,
            error = c.error, onError = c.onError, errorContainer = c.errorSoft, onErrorContainer = c.errorText,
        )
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as? android.app.Activity)?.window?.let { window ->
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !dark
                    isAppearanceLightNavigationBars = !dark
                }
            }
        }
    }
    val context = LocalContext.current
    val reducedMotion = remember(context) {
        android.provider.Settings.Global.getFloat(context.contentResolver, android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
    CompositionLocalProvider(LocalTrilhaColors provides c, LocalReducedMotion provides reducedMotion) {
        MaterialTheme(colorScheme = scheme, typography = TrilhaTypography, shapes = TrilhaShapes, content = content)
    }
}
