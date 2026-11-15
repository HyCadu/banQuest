package br.com.trilhati

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/** Tela de diagnóstico da Fase 0: prova que Compose, Hilt, Clock e Room estão funcionando. */
@Composable
fun DiagnosticScreen(todayLabel: String, profileName: String?) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.safeDrawingPadding().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, androidx.compose.ui.Alignment.CenterVertically),
        ) {
            Text(stringResource(R.string.diag_title), style = MaterialTheme.typography.displaySmall)
            Text(stringResource(R.string.diag_phase), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.diag_date, todayLabel))
            Text(
                if (profileName == null) {
                    stringResource(R.string.diag_db_empty)
                } else {
                    stringResource(R.string.diag_db_profile, profileName)
                },
            )
            Text(stringResource(R.string.diag_hint), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DiagnosticScreenPreview() {
    TrilhaTheme { DiagnosticScreen(todayLabel = "15/11/2026", profileName = null) }
}
