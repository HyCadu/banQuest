package br.com.trilhati.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import br.com.trilhati.ui.icons.GameIcon
import br.com.trilhati.ui.icons.GameIconView
import br.com.trilhati.ui.theme.Trilha

/**
 * Botão "ⓘ": guarda uma explicação num diálogo, para a tela não ficar cheia de parágrafos. O rótulo de acessibilidade
 * diz do que se trata ("Saiba mais: Ofensivas em dupla").
 */
@Composable
fun InfoButton(title: String, text: String, modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    Box(
        modifier
            .size(40.dp)
            .semantics(mergeDescendants = true) { role = Role.Button; contentDescription = "Saiba mais: $title" }
            .clickable { open = true },
        contentAlignment = Alignment.Center,
    ) { GameIconView(GameIcon.Info, size = 22.dp) }
    if (open) {
        TrilhaDialog(
            onDismiss = { open = false },
            title = title,
            content = { Text(text, style = MaterialTheme.typography.bodyMedium, color = Trilha.colors.ink) },
            actions = { ChunkyButton("Entendi", { open = false }, Modifier.fillMaxWidth()) },
        )
    }
}

/** Botão redondo pequeno com um símbolo (↻, ⋯) e um rótulo de acessibilidade completo. */
@Composable
fun RoundSymbolButton(symbol: String, description: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val c = Trilha.colors
    Surface(
        modifier = modifier
            .size(40.dp)
            .semantics(mergeDescendants = true) { role = Role.Button; contentDescription = description }
            .clickable(enabled = enabled, onClick = onClick),
        shape = CircleShape,
        color = c.surface,
        border = BorderStroke(2.dp, c.outline),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(symbol, style = MaterialTheme.typography.titleMedium, color = if (enabled) c.primaryText else c.inkSoft)
        }
    }
}
