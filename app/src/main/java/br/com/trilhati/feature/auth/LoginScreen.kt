package br.com.trilhati.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import br.com.trilhati.ui.components.ChunkyButton
import br.com.trilhati.ui.components.ChunkyStyle
import br.com.trilhati.ui.mascot.Mascot
import br.com.trilhati.ui.mascot.MascotCharacter
import br.com.trilhati.ui.mascot.MascotPose
import br.com.trilhati.ui.theme.Spacing
import br.com.trilhati.ui.theme.Trilha

@Composable
fun LoginScreen(viewModel: AuthViewModel = hiltViewModel()) {
    val ui by viewModel.ui.collectAsState()
    LoginContent(
        ui = ui,
        onEmail = viewModel::onEmail, onSendCode = viewModel::sendCode, onCode = viewModel::onCode, onVerify = viewModel::verify,
        onChangeEmail = viewModel::changeEmail, onName = viewModel::onName, onMascot = viewModel::onMascot,
        onSaveProfile = viewModel::saveProfile, onSignOut = { viewModel.signOut() },
    )
}

/** Tela de entrada, sem ViewModel (dá para pré-visualizar cada passo). */
@Composable
fun LoginContent(
    ui: AuthUi,
    onEmail: (String) -> Unit,
    onSendCode: () -> Unit,
    onCode: (String) -> Unit,
    onVerify: () -> Unit,
    onChangeEmail: () -> Unit,
    onName: (String) -> Unit,
    onMascot: (String) -> Unit,
    onSaveProfile: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
    animated: Boolean = true,
) {
    val c = Trilha.colors
    val mascot = MascotCharacter.entries.firstOrNull { it.name == ui.mascot } ?: MascotCharacter.Guaiamum
    Column(
        modifier.fillMaxSize().background(c.background).statusBarsPadding().navigationBarsPadding().imePadding()
            .verticalScroll(rememberScrollState()).padding(horizontal = Spacing.screen, vertical = Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Mascot(
            mascot,
            when (ui.step) { AuthStep.Syncing -> MascotPose.Celebrate; AuthStep.Profile -> MascotPose.Happy; else -> MascotPose.Idle },
            Modifier.size(170.dp), animated = animated, decorative = true,
        )
        Text("Trilha TI", style = MaterialTheme.typography.displaySmall, color = c.primaryText)
        when (ui.step) {
            AuthStep.Email -> {
                Text(
                    "Entre com seu e-mail para estudar com seus amigos: ranking da semana, ofensiva em dupla e seu progresso salvo na nuvem.",
                    style = MaterialTheme.typography.bodyLarge, color = c.inkSoft, textAlign = TextAlign.Center,
                )
                OutlinedTextField(
                    ui.email, onEmail, Modifier.fillMaxWidth(), label = { Text("E-mail") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Send),
                )
                ErrorText(ui.error)
                ChunkyButton(if (ui.busy) "Enviando…" else "Receber código", onSendCode, Modifier.fillMaxWidth(), enabled = !ui.busy)
                Text("Sem senha: mandamos um código de 6 dígitos para o seu e-mail.", style = MaterialTheme.typography.bodySmall, color = c.inkSoft, textAlign = TextAlign.Center)
            }
            AuthStep.Code -> {
                Text("Digite o código que chegou em\n${ui.email}", style = MaterialTheme.typography.bodyLarge, color = c.ink, textAlign = TextAlign.Center)
                OutlinedTextField(
                    ui.code, onCode, Modifier.fillMaxWidth(), label = { Text("Código de 6 dígitos") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                    textStyle = MaterialTheme.typography.headlineSmall,
                )
                ErrorText(ui.error)
                ChunkyButton(if (ui.busy) "Conferindo…" else "Entrar", onVerify, Modifier.fillMaxWidth(), enabled = !ui.busy)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChunkyButton(
                        if (ui.resendInSec > 0) "Reenviar (${ui.resendInSec})" else "Reenviar código", onSendCode, Modifier.weight(1f),
                        ChunkyStyle.Neutral, enabled = ui.resendInSec == 0 && !ui.busy, minHeight = 44.dp,
                    )
                    ChunkyButton("Trocar e-mail", onChangeEmail, Modifier.weight(1f), ChunkyStyle.Neutral, minHeight = 44.dp)
                }
                Text("Não chegou? Olhe a caixa de spam.", style = MaterialTheme.typography.bodySmall, color = c.inkSoft)
            }
            AuthStep.Profile -> {
                Text(
                    if (ui.returning) "Bem-vindo de volta! Confira seu apelido e personagem." else "Como seus amigos vão te ver no ranking?",
                    style = MaterialTheme.typography.bodyLarge, color = c.ink, textAlign = TextAlign.Center,
                )
                OutlinedTextField(ui.name, onName, Modifier.fillMaxWidth(), label = { Text("Apelido") }, singleLine = true)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MascotCharacter.entries.forEach { option ->
                        val selected = option.name == ui.mascot
                        Column(
                            Modifier.weight(1f)
                                .semantics { role = Role.RadioButton; this.selected = selected }
                                .background(if (selected) c.primarySoft else c.surface, RoundedCornerShape(16.dp))
                                .border(2.dp, if (selected) c.primary else c.outline, RoundedCornerShape(16.dp))
                                .clickable { onMascot(option.name) }.padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Mascot(option, MascotPose.Idle, Modifier.size(52.dp), animated = false, decorative = true)
                            Text(option.displayName.substringBefore('-'), style = MaterialTheme.typography.labelSmall, color = c.inkSoft, maxLines = 1)
                        }
                    }
                }
                Text("Os outros veem só seu apelido, personagem, XP da semana e ofensiva. Nunca o e-mail.", style = MaterialTheme.typography.bodySmall, color = c.inkSoft, textAlign = TextAlign.Center)
                ErrorText(ui.error)
                ChunkyButton(if (ui.busy) "Salvando…" else "Começar", onSaveProfile, Modifier.fillMaxWidth(), enabled = !ui.busy)
                ChunkyButton("Sair e usar outro e-mail", onSignOut, Modifier.fillMaxWidth(), ChunkyStyle.Neutral, minHeight = 44.dp)
            }
            AuthStep.Syncing -> {
                Text("Preparando sua trilha e seu progresso…", style = MaterialTheme.typography.titleMedium, color = c.ink, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun ErrorText(message: String?) {
    if (message != null) {
        Text(
            message, Modifier.fillMaxWidth().background(Trilha.colors.errorSoft, RoundedCornerShape(12.dp)).padding(12.dp),
            style = MaterialTheme.typography.bodyMedium, color = Trilha.colors.errorText,
        )
    }
}
