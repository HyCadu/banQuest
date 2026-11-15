package br.com.trilhati.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.trilhati.data.prefs.Settings
import br.com.trilhati.domain.pace.PaceConfig
import br.com.trilhati.domain.pace.TimerMode
import br.com.trilhati.feature.app.AppViewModel
import br.com.trilhati.ui.components.ChunkyButton
import br.com.trilhati.ui.components.ChunkyStyle
import br.com.trilhati.ui.components.PillChoice
import br.com.trilhati.ui.components.TrilhaDialog
import br.com.trilhati.ui.theme.Spacing
import br.com.trilhati.ui.theme.ThemeMode
import br.com.trilhati.ui.theme.Trilha

@Composable
fun SettingsScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val settings by viewModel.settings.collectAsState()
    val state by viewModel.state.collectAsState()
    val questionCount = state?.course?.sumOf { u -> u.lessons.sumOf { it.questions.size } } ?: 0
    val unreviewed = state?.course?.sumOf { u -> u.lessons.sumOf { l -> l.questions.count { it.showUnreviewedBadge } } } ?: 0
    SettingsContent(
        settings = settings, questionCount = questionCount, unreviewedCount = unreviewed, onBack = onBack,
        onTimerMode = viewModel::setTimerMode, onPaceTarget = viewModel::setPaceTarget, onCaseFour = viewModel::setCaseFourMinutes,
        onNoLives = viewModel::setNoLives, onHaptics = viewModel::setHaptics, onTheme = viewModel::setTheme, onErase = viewModel::eraseAll,
    )
}

@Composable
fun SettingsContent(
    settings: Settings,
    questionCount: Int,
    unreviewedCount: Int,
    onBack: () -> Unit,
    onTimerMode: (TimerMode) -> Unit,
    onPaceTarget: (Long) -> Unit,
    onCaseFour: (Boolean) -> Unit,
    onNoLives: (Boolean) -> Unit,
    onHaptics: (Boolean) -> Unit,
    onTheme: (ThemeMode) -> Unit,
    onErase: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = Trilha.colors
    var confirmErase by remember { mutableStateOf(false) }
    Column(
        modifier.fillMaxSize().background(c.background).statusBarsPadding().verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.screen, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ChunkyButton("Voltar", onBack, style = ChunkyStyle.Neutral, minHeight = 44.dp)
            Text("Configurações", style = MaterialTheme.typography.headlineMedium, color = c.ink)
        }

        Section("Cronômetro de ritmo", "O diferencial do app: treine no ritmo da prova. Ele nunca reprova a resposta.") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PillChoice("Desligado", settings.timerMode == TimerMode.Off, { onTimerMode(TimerMode.Off) })
                PillChoice("Discreto", settings.timerMode == TimerMode.Discreet, { onTimerMode(TimerMode.Discreet) })
                PillChoice("Ritmo", settings.timerMode == TimerMode.Pace, { onTimerMode(TimerMode.Pace) })
            }
            Text(
                when (settings.timerMode) {
                    TimerMode.Off -> "Sem relógio na tela. O tempo ainda é medido para o resumo."
                    TimerMode.Discreet -> "Sem relógio durante a questão; o tempo aparece só no resumo final."
                    TimerMode.Pace -> "Anel de tempo em cada questão, âmbar aos 75% e vermelho ao estourar."
                },
                style = MaterialTheme.typography.bodySmall, color = c.inkSoft,
            )
            Text("Tempo por questão", style = MaterialTheme.typography.titleSmall, color = c.ink)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PillChoice("3:00 (média da prova)", settings.paceTargetMs == PaceConfig.STANDARD_MS, { onPaceTarget(PaceConfig.STANDARD_MS) })
                PillChoice("2:30 (sobra tempo)", settings.paceTargetMs == PaceConfig.TIGHT_MS, { onPaceTarget(PaceConfig.TIGHT_MS) })
            }
            SwitchRow("Caso prático com 4:00", "Questões de caso prático ganham mais tempo.", settings.caseFourMinutes, onCaseFour)
        }

        Section("Estudo") {
            SwitchRow("Modo sem vidas", "Sem corações: estude sem punição por erro.", settings.noLives, onNoLives)
            SwitchRow("Vibração", "Vibra de leve ao acertar e ao errar.", settings.haptics, onHaptics)
        }

        Section("Tema") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PillChoice("Sistema", settings.themeMode == ThemeMode.System, { onTheme(ThemeMode.System) })
                PillChoice("Claro", settings.themeMode == ThemeMode.Light, { onTheme(ThemeMode.Light) })
                PillChoice("Escuro", settings.themeMode == ThemeMode.Dark, { onTheme(ThemeMode.Dark) })
            }
        }

        Section("Sobre o conteúdo") {
            Text(
                "$questionCount questões no app, $unreviewedCount ainda não revisadas. " +
                    "As questões geradas por IA mostram o selo \"não revisada\" até você conferi-las. " +
                    "Nenhuma é de prova real da banca.",
                style = MaterialTheme.typography.bodyMedium, color = c.ink,
            )
        }

        Section("Dados") {
            Text("Todo o progresso fica só neste aparelho. Nada é enviado para a internet.", style = MaterialTheme.typography.bodySmall, color = c.inkSoft)
            ChunkyButton("Apagar todos os dados", { confirmErase = true }, Modifier.fillMaxWidth(), ChunkyStyle.Error)
        }
    }

    if (confirmErase) {
        TrilhaDialog(
            onDismiss = { confirmErase = false },
            title = "Apagar tudo?",
            content = { Text("Isso apaga XP, ofensiva, gemas, progresso das lições e conquistas. Não dá para desfazer.", style = MaterialTheme.typography.bodyMedium, color = c.ink) },
            actions = {
                ChunkyButton("Sim, apagar", { onErase(); confirmErase = false; onBack() }, Modifier.fillMaxWidth(), ChunkyStyle.Error)
                ChunkyButton("Cancelar", { confirmErase = false }, Modifier.fillMaxWidth(), ChunkyStyle.Neutral)
            },
        )
    }
}

@Composable
private fun Section(title: String, subtitle: String? = null, content: @Composable () -> Unit) {
    val c = Trilha.colors
    Column(
        Modifier.fillMaxWidth().background(c.surface, RoundedCornerShape(22.dp)).border(2.dp, c.outline, RoundedCornerShape(22.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = c.primaryText)
        if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = c.inkSoft)
        content()
    }
}

@Composable
private fun SwitchRow(title: String, description: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    val c = Trilha.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = c.ink)
            Text(description, style = MaterialTheme.typography.bodySmall, color = c.inkSoft)
        }
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}
