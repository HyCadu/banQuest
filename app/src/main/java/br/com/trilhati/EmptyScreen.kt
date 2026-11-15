package br.com.trilhati

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview

/** Tela vazia da Fase 0. A trilha de verdade chega na Fase 4. */
@Composable
fun EmptyScreen() {
    val description = stringResource(R.string.empty_screen_description)
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(modifier = Modifier.fillMaxSize().semantics { contentDescription = description })
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyScreenPreview() {
    TrilhaTheme { EmptyScreen() }
}
