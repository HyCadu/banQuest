package br.com.trilhati.feature.session

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.trilhati.domain.exam.ExamRules
import br.com.trilhati.domain.model.AnswerKey
import br.com.trilhati.ui.components.ChunkyButton
import br.com.trilhati.ui.components.ChunkyStyle
import br.com.trilhati.ui.icons.GameIcon
import br.com.trilhati.ui.icons.GameIconView
import br.com.trilhati.ui.icons.IconColors
import br.com.trilhati.ui.mascot.Mascot
import br.com.trilhati.ui.mascot.MascotCharacter
import br.com.trilhati.ui.mascot.MascotPose
import br.com.trilhati.ui.theme.Spacing
import br.com.trilhati.ui.theme.Trilha

/** Ações da tela de aviso da prova anterior. */
class ExamIntroActions(
    val onPickPart: (br.com.trilhati.domain.exam.ExamPart) -> Unit = {},
    val onSilence: (Boolean) -> Unit = {},
    /** Abre a tela do Android para liberar o acesso ao Não perturbe. */
    val onOpenSilenceAccess: () -> Unit = {},
    val onBegin: () -> Unit = {},
)

/**
 * Aviso antes da prova anterior (pedido do dono): as regras ficam à vista antes de gastar o coração. Abrir gasta 1
 * coração, errar não tira coração e o XP só vem se o aluno responder todas as questões.
 */
@Composable
internal fun ExamIntroScreen(ui: SessionUi.ExamIntro, character: MascotCharacter, actions: ExamIntroActions, onExit: () -> Unit, animated: Boolean) {
    val c = Trilha.colors
    val time = ExamRules.durationLabel(ui.estimatedMs)
    val free = ui.costHalves == 0
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = Spacing.screen, vertical = Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).semantics { contentDescription = "Voltar"; role = Role.Button }.clickable(onClick = onExit),
                contentAlignment = Alignment.Center,
            ) { GameIconView(GameIcon.Close, size = 26.dp, colors = IconColors.flat(c.inkSoft, c.inkSoft)) }
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = Spacing.screen),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Mascot(character, MascotPose.Idle, Modifier.size(120.dp), animated = animated, decorative = true)
            Text(ui.title, style = MaterialTheme.typography.headlineLarge, color = c.ink, textAlign = TextAlign.Center)
            if (ui.cargo.isNotBlank()) {
                Text(ui.cargo, style = MaterialTheme.typography.bodyMedium, color = c.inkSoft, textAlign = TextAlign.Center)
            }
            if (ui.parts.size > 1) PartPicker(ui, actions.onPickPart)

            Column(
                Modifier.fillMaxWidth().background(c.surface, RoundedCornerShape(20.dp)).border(2.dp, c.outline, RoundedCornerShape(20.dp)).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Rule(GameIcon.Clock, "${ui.questionCount} questões · cerca de $time", "No ritmo de prova, com o cronômetro de cada questão.")
                Rule(
                    GameIcon.Heart,
                    if (free) "Modo sem vidas: começar não gasta coração" else "Começar gasta 1 coração",
                    "Errar não tira coração. Os erros vão para Praticar erros.",
                )
                Rule(
                    GameIcon.Star,
                    "O XP só vem se você responder todas",
                    if (free) "Se sair antes, não ganha nada." else "Se sair antes, não ganha nada e o coração não volta.",
                )
                when (ui.answerKey) {
                    AnswerKey.Final -> Unit
                    AnswerKey.Preliminary -> Rule(GameIcon.Info, "Gabarito preliminar", "Alguma resposta ainda pode mudar no gabarito definitivo.")
                    AnswerKey.Unconfirmed -> Rule(GameIcon.Info, "Gabarito não confirmado", "As respostas não foram conferidas com o gabarito oficial.")
                }
            }

            SilenceRow(ui, actions)
            if (!ui.canOpen) {
                Text(
                    "Você precisa de 1 coração inteiro para abrir a prova.",
                    style = MaterialTheme.typography.bodyMedium, color = c.errorText, textAlign = TextAlign.Center,
                )
            }
        }
        Column(Modifier.fillMaxWidth().padding(Spacing.screen), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ChunkyButton(
                if (free) "Começar prova" else "Começar prova (−1 coração)", actions.onBegin, Modifier.fillMaxWidth(), ChunkyStyle.Primary,
                enabled = !ui.starting,
            )
            ChunkyButton("Agora não", onExit, Modifier.fillMaxWidth(), ChunkyStyle.Neutral)
        }
    }
}

@Composable
private fun PartPicker(ui: SessionUi.ExamIntro, onPick: (br.com.trilhati.domain.exam.ExamPart) -> Unit) {
    val c = Trilha.colors
    Row(Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ui.parts.forEachIndexed { i, part ->
            val selected = part == ui.part
            val size = ui.partSizes.getOrNull(i) ?: 0
            Column(
                Modifier.weight(1f)
                    .background(if (selected) c.primarySoft else c.surface, RoundedCornerShape(16.dp))
                    .border(2.dp, if (selected) c.primary else c.outline, RoundedCornerShape(16.dp))
                    .selectable(selected = selected, role = Role.RadioButton, onClick = { onPick(part) })
                    .padding(vertical = 10.dp, horizontal = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(part.label, style = MaterialTheme.typography.titleSmall, color = if (selected) c.primaryText else c.ink, textAlign = TextAlign.Center)
                Text("$size questões", style = MaterialTheme.typography.labelMedium, color = c.inkSoft)
            }
        }
    }
}

@Composable
private fun Rule(icon: GameIcon, title: String, detail: String) {
    val c = Trilha.colors
    Row(Modifier.semantics(mergeDescendants = true) {}, horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
        GameIconView(icon, size = 28.dp)
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = c.ink)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = c.inkSoft)
        }
    }
}

/**
 * "Silenciar notificações durante a prova": liga o Não perturbe do Android ao começar e desliga no fim. Sem o acesso
 * liberado, ligar o interruptor abre a tela do Android para liberar.
 */
@Composable
private fun SilenceRow(ui: SessionUi.ExamIntro, actions: ExamIntroActions) {
    val c = Trilha.colors
    val onToggle: (Boolean) -> Unit = { on ->
        actions.onSilence(on)
        if (on && !ui.silenceAllowed) actions.onOpenSilenceAccess()
    }
    Row(
        Modifier.fillMaxWidth()
            .background(c.surface, RoundedCornerShape(20.dp)).border(2.dp, c.outline, RoundedCornerShape(20.dp))
            .toggleable(value = ui.silence, role = Role.Switch, onValueChange = onToggle)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text("Silenciar notificações durante a prova", style = MaterialTheme.typography.titleSmall, color = c.ink)
            Text(
                when {
                    ui.silence && !ui.silenceAllowed -> "Falta permitir o acesso ao Não perturbe nas configurações do Android."
                    ui.silenceAllowed -> "Liga o Não perturbe do Android e desliga quando a prova acabar."
                    else -> "Liga o Não perturbe do Android e desliga no fim. Na primeira vez, o Android pede permissão."
                },
                style = MaterialTheme.typography.bodySmall, color = if (ui.silence && !ui.silenceAllowed) c.warningText else c.inkSoft,
            )
        }
        // O Row inteiro é o interruptor (toggleable); o Switch só desenha.
        Switch(checked = ui.silence, onCheckedChange = null)
    }
    if (ui.silence && !ui.silenceAllowed) {
        ChunkyButton("Permitir acesso ao Não perturbe", actions.onOpenSilenceAccess, Modifier.fillMaxWidth(), ChunkyStyle.Secondary)
    }
}
