package br.com.trilhati.feature.update

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.trilhati.domain.update.ReleaseInfo
import br.com.trilhati.ui.components.ChunkyButton
import br.com.trilhati.ui.components.ChunkyStyle
import br.com.trilhati.ui.components.TrilhaDialog
import br.com.trilhati.ui.icons.GameIcon
import br.com.trilhati.ui.icons.GameIconView
import br.com.trilhati.ui.theme.Trilha

/** Aviso no topo da Trilha: versão nova disponível. */
@Composable
fun UpdateBanner(release: ReleaseInfo, onDownload: () -> Unit, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val c = Trilha.colors
    Column(
        modifier.fillMaxWidth().background(c.secondarySoft, RoundedCornerShape(16.dp))
            .border(2.dp, c.secondary, RoundedCornerShape(16.dp)).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GameIconView(GameIcon.Gem, size = 26.dp)
            Column(Modifier.weight(1f)) {
                Text("BanQuest ${release.versionName} disponível", style = MaterialTheme.typography.titleSmall, color = c.secondaryText)
                if (release.notes.isNotBlank()) {
                    Text(release.notes, style = MaterialTheme.typography.bodySmall, color = c.ink, maxLines = 3)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChunkyButton("Baixar", onDownload, Modifier.weight(1f), ChunkyStyle.Secondary, minHeight = 42.dp)
            ChunkyButton("Agora não", onDismiss, Modifier.weight(1f), ChunkyStyle.Neutral, minHeight = 42.dp)
        }
    }
}

/** Versão abaixo da mínima: não dá para fechar; só baixar a nova. */
@Composable
fun UpdateRequiredDialog(release: ReleaseInfo, onDownload: () -> Unit) {
    val c = Trilha.colors
    TrilhaDialog(
        onDismiss = { },
        title = "Atualize o BanQuest",
        content = {
            Text(
                "Esta versão não funciona mais com o servidor. Baixe a ${release.versionName} e instale por cima: " +
                    "seu progresso continua salvo na conta." + if (release.notes.isNotBlank()) "\n\n${release.notes}" else "",
                style = MaterialTheme.typography.bodyMedium, color = c.ink,
            )
        },
        actions = { ChunkyButton("Baixar a versão ${release.versionName}", onDownload, Modifier.fillMaxWidth()) },
    )
}
