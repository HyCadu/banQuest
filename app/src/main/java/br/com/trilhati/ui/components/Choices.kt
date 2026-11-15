package br.com.trilhati.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import br.com.trilhati.ui.theme.Trilha

/** Opção de escolha única em formato de pílula (meta diária, tema, ritmo...). */
@Composable
fun PillChoice(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = Trilha.colors
    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .semantics { role = Role.RadioButton; this.selected = selected }
            .background(if (selected) c.primary else c.surface, shape)
            .border(2.dp, if (selected) c.primaryEdge else c.outline, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = if (selected) c.onPrimary else c.primaryText)
    }
}
