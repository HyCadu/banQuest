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

/**
 * Os quatro temas do app: [Claro] e [Escuro] (roxo e turquesa, os de sempre) e [Eletrico] e [Chiclete] (referências
 * 60/30/10 do dono, sempre com fundo claro).
 */
enum class ThemePalette(val displayName: String) {
    Claro("Claro"),
    Escuro("Escuro"),
    Eletrico("Elétrico"),
    Chiclete("Chiclete"),
}

/** Tokens de cor de cada tema. */
fun paletteColors(palette: ThemePalette): TrilhaColors = when (palette) {
    ThemePalette.Claro -> LightTrilhaColors
    ThemePalette.Escuro -> DarkTrilhaColors
    ThemePalette.Eletrico -> EletricoColors
    ThemePalette.Chiclete -> ChicleteColors
}

/** Tema em uso: o escolhido, ou, enquanto o aluno não escolheu, Claro ou Escuro conforme o aparelho. */
@Composable
fun resolvePalette(palette: ThemePalette?): ThemePalette =
    palette ?: if (isSystemInDarkTheme()) ThemePalette.Escuro else ThemePalette.Claro

/** Acesso aos tokens: `Trilha.colors.primary`. */
object Trilha {
    val colors: TrilhaColors
        @Composable @ReadOnlyComposable get() = LocalTrilhaColors.current
}

@Composable
fun TrilhaTheme(palette: ThemePalette? = null, content: @Composable () -> Unit) {
    val c = paletteColors(resolvePalette(palette))
    val dark = c.isDark
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
