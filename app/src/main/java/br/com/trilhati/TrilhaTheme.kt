package br.com.trilhati

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/** Tema provisório (base azul-petróleo + âmbar). O design system completo entra na Fase 1, em `ui/theme`. */
@Composable
fun TrilhaTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}

private val LightColors = lightColorScheme()
private val DarkColors = darkColorScheme()
