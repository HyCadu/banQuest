package br.com.trilhati.feature.path

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.trilhati.domain.exam.TargetExam
import br.com.trilhati.ui.components.ChunkyButton
import br.com.trilhati.ui.icons.GameIcon
import br.com.trilhati.ui.icons.GameIconView
import br.com.trilhati.ui.mascot.Mascot
import br.com.trilhati.ui.mascot.MascotCharacter
import br.com.trilhati.ui.mascot.MascotPose
import br.com.trilhati.ui.theme.LocalReducedMotion
import br.com.trilhati.ui.theme.Spacing
import br.com.trilhati.ui.theme.Trilha
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Emblema de cada prova: código para o Analista de TI, prédio de banco para o Técnico Bancário. */
val TargetExam.emblem: GameIcon
    get() = when (this) {
        TargetExam.AnalistaTi -> GameIcon.ExamCode
        TargetExam.TecnicoBancario -> GameIcon.ExamBank
    }

private val DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy")

private fun daysText(days: Int): String = when (days) {
    0 -> "a prova é hoje"
    1 -> "falta 1 dia"
    else -> "faltam $days dias"
}

/**
 * Chip da prova no topo da Trilha (0.13.0): emblema da prova escolhida e os dias que faltam. Tocar abre o painel de
 * provas ([ExamPanel]); a seta vira para cima enquanto ele está aberto.
 */
@Composable
fun ExamChip(exam: TargetExam, daysLeft: Int, expanded: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = Trilha.colors
    val turn by animateFloatAsState(if (expanded) 180f else 0f, tween(if (LocalReducedMotion.current) 0 else 220), label = "examChevron")
    Surface(
        onClick = onClick,
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = "Prova: ${exam.title}, ${daysText(daysLeft)}. Trocar de prova"
            stateDescription = if (expanded) "aberto" else "fechado"
        },
        shape = RoundedCornerShape(50),
        color = c.surface,
        border = BorderStroke(2.dp, if (expanded) c.primary else c.outline),
    ) {
        Row(Modifier.padding(start = 5.dp, end = 10.dp, top = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            GameIconView(exam.emblem, size = 28.dp)
            Spacer(Modifier.width(6.dp))
            Text("$daysLeft d", style = MaterialTheme.typography.titleMedium, color = c.ink)
            Spacer(Modifier.width(4.dp))
            Chevron(Modifier.size(16.dp).rotate(turn))
        }
    }
}

@Composable
private fun Chevron(modifier: Modifier) {
    val color = Trilha.colors.inkSoft
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.18f
        drawLine(color, Offset(w * 0.2f, h * 0.38f), Offset(w * 0.5f, h * 0.66f), stroke, StrokeCap.Round)
        drawLine(color, Offset(w * 0.5f, h * 0.66f), Offset(w * 0.8f, h * 0.38f), stroke, StrokeCap.Round)
    }
}

/** Cartão de uma prova (painel e primeira entrada): emblema, nome, data com os dias que faltam, formato e o ✓. */
@Composable
fun ExamOptionCard(exam: TargetExam, selected: Boolean, today: LocalDate, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = Trilha.colors
    val shape = RoundedCornerShape(18.dp)
    val days = exam.daysLeft(today)
    Row(
        modifier
            .clip(shape)
            .background(if (selected) c.primarySoft else c.surface)
            .border(if (selected) 3.dp else 2.dp, if (selected) c.primary else c.outline, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        GameIconView(exam.emblem, size = 44.dp)
        Column(Modifier.weight(1f)) {
            Text(exam.title, style = MaterialTheme.typography.titleMedium, color = c.ink)
            Text("${exam.date.format(DATE)} · ${daysText(days)}", style = MaterialTheme.typography.bodySmall, color = c.inkSoft)
            Text(exam.formatLabel, style = MaterialTheme.typography.bodySmall, color = c.inkSoft)
        }
        if (selected) {
            Box(Modifier.size(28.dp).background(c.primary, RoundedCornerShape(50)), contentAlignment = Alignment.Center) {
                GameIconView(GameIcon.Check, size = 18.dp)
            }
        }
    }
}

/**
 * Painel de provas que desce do chip, com a trilha escurecida atrás: só os cartões das provas (data, dias e formato), sem
 * texto a mais (pedido do dono). Tocar numa prova troca a trilha e fecha; tocar fora ou voltar também fecha.
 */
@Composable
fun ExamPanel(
    visible: Boolean,
    selected: TargetExam,
    today: LocalDate,
    onPick: (TargetExam) -> Unit,
    onDismiss: () -> Unit,
    topOffset: Dp,
    modifier: Modifier = Modifier,
) {
    val c = Trilha.colors
    val reduced = LocalReducedMotion.current
    BackHandler(enabled = visible, onBack = onDismiss)
    AnimatedVisibility(
        visible, modifier.fillMaxSize(),
        enter = if (reduced) EnterTransition.None else fadeIn(tween(160)),
        exit = if (reduced) ExitTransition.None else fadeOut(tween(160)),
    ) {
        Box(Modifier.fillMaxSize()) {
            // Fundo escurecido: tocar fora fecha.
            Box(
                Modifier.fillMaxSize().padding(top = topOffset).background(c.ink.copy(alpha = 0.38f))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss)
                    .semantics { contentDescription = "Fechar a escolha de prova"; onClick("Fechar") { onDismiss(); true } },
            )
            Box(Modifier.padding(top = topOffset + 8.dp).padding(horizontal = 12.dp)) {
                Column(
                    Modifier.fillMaxWidth()
                        .shadow(14.dp, RoundedCornerShape(24.dp))
                        .background(c.surface, RoundedCornerShape(24.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("Qual prova você vai fazer?", Modifier.semantics { heading() }, style = MaterialTheme.typography.titleLarge, color = c.ink)
                    Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        TargetExam.entries.forEach { exam ->
                            ExamOptionCard(exam, exam == selected, today, { onPick(exam) }, Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
    }
}

/**
 * Pergunta da prova na primeira entrada, logo depois da escolha do tema (só para quem está começando; quem já usava o
 * app continua no Analista e troca pelo chip). O botão só libera depois de escolher.
 */
@Composable
fun ExamChoiceScreen(
    selected: TargetExam?,
    today: LocalDate,
    character: MascotCharacter,
    onPick: (TargetExam) -> Unit,
    onConfirm: () -> Unit,
    animated: Boolean = true,
) {
    val c = Trilha.colors
    Column(Modifier.fillMaxSize().background(c.background).statusBarsPadding().navigationBarsPadding()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = Spacing.screen, vertical = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Mascot(character, MascotPose.Happy, Modifier.size(96.dp), animated = animated, decorative = true)
            Text("Qual prova você vai fazer?", Modifier.semantics { heading() }, style = MaterialTheme.typography.headlineMedium, color = c.ink)
            Column(Modifier.fillMaxWidth().selectableGroup(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TargetExam.entries.forEach { exam ->
                    ExamOptionCard(exam, exam == selected, today, { onPick(exam) }, Modifier.fillMaxWidth())
                }
            }
            Text(
                "Os Conhecimentos Gerais são os mesmos nas duas provas. Dá para trocar quando quiser no topo da Trilha.",
                style = MaterialTheme.typography.bodyMedium, color = c.inkSoft,
            )
        }
        ChunkyButton("Começar", onConfirm, Modifier.fillMaxWidth().padding(Spacing.screen), enabled = selected != null)
    }
}
