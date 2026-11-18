package br.com.trilhati.feature.path

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.trilhati.domain.update.ReleaseInfo
import br.com.trilhati.feature.update.UpdateBanner
import br.com.trilhati.ui.components.GameProgressBar
import br.com.trilhati.ui.icons.GameIcon
import br.com.trilhati.ui.icons.GameIconView
import br.com.trilhati.ui.theme.LocalReducedMotion
import br.com.trilhati.ui.theme.Trilha
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * O que o sino do topo da Trilha mostra (0.14.1, pedido do dono): a meta do dia em lições, o aviso de ofensiva em risco
 * e a versão nova do app. A meta está sempre lá; o ponto vermelho do sino só acende para os avisos.
 */
@Immutable
data class Notices(
    val goalLessons: Int,
    val lessonsToday: Int,
    val examDate: LocalDate,
    /** Texto de [br.com.trilhati.domain.streak.StreakAlert]; nulo = nada em risco. */
    val streakAlert: String? = null,
    /** Há ofensiva em dupla no aviso (tocar nele abre o Ranking). */
    val pairsInAlert: Boolean = false,
    val update: ReleaseInfo? = null,
) {
    val alerts: Int get() = (if (streakAlert != null) 1 else 0) + (if (update != null) 1 else 0)
    val goalMet: Boolean get() = lessonsToday >= goalLessons
}

/** Botão do sino: só o contorno, com um ponto vermelho quando há aviso. */
@Composable
fun NoticeBell(alerts: Int, expanded: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = Trilha.colors
    val label = when (alerts) {
        0 -> "Avisos"
        1 -> "Avisos: 1 novo"
        else -> "Avisos: $alerts novos"
    }
    Box(
        modifier.size(width = 44.dp, height = 46.dp)
            .background(if (expanded) c.primarySoft else c.background, RoundedCornerShape(23.dp))
            .clickable(role = Role.Button, onClickLabel = if (expanded) "Fechar avisos" else "Abrir avisos", onClick = onClick)
            .semantics {
                contentDescription = label
                stateDescription = if (expanded) "Aberto" else "Fechado"
            },
        contentAlignment = Alignment.Center,
    ) {
        val ink = if (expanded) c.primaryText else c.inkSoft
        Canvas(Modifier.size(26.dp)) {
            val s = size.width / 24f
            val bell = Path().apply {
                moveTo(6f * s, 16f * s)
                lineTo(6f * s, 11f * s)
                cubicTo(6f * s, 3f * s, 18f * s, 3f * s, 18f * s, 11f * s)
                lineTo(18f * s, 16f * s)
                lineTo(19.5f * s, 18f * s)
                lineTo(4.5f * s, 18f * s)
                close()
            }
            val stroke = Stroke(2.2f * s, cap = StrokeCap.Round, join = StrokeJoin.Round)
            drawPath(bell, ink, style = stroke)
            val clapper = Path().apply {
                moveTo(10f * s, 20.5f * s)
                cubicTo(10.6f * s, 22.3f * s, 13.4f * s, 22.3f * s, 14f * s, 20.5f * s)
            }
            drawPath(clapper, ink, style = stroke)
        }
        if (alerts > 0) {
            Box(
                Modifier.align(Alignment.TopEnd).padding(top = 9.dp, end = 9.dp).size(11.dp)
                    .background(c.background, CircleShape).padding(2.dp).background(c.heart, CircleShape),
            )
        }
    }
}

/** Painel dos avisos, que desce do topo como o de provas. Tocar fora ou voltar fecha. */
@Composable
fun NoticesPanel(
    visible: Boolean,
    notices: Notices,
    onDownload: (ReleaseInfo) -> Unit,
    onDismissUpdate: (ReleaseInfo) -> Unit,
    onOpenRanking: () -> Unit,
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
            Box(
                Modifier.fillMaxSize().padding(top = topOffset).background(c.ink.copy(alpha = 0.38f))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss)
                    .semantics { contentDescription = "Fechar os avisos"; onClick("Fechar") { onDismiss(); true } },
            )
            Box(Modifier.padding(top = topOffset + 8.dp).padding(horizontal = 12.dp)) {
                Column(
                    Modifier.fillMaxWidth()
                        .shadow(14.dp, RoundedCornerShape(24.dp))
                        .background(c.surface, RoundedCornerShape(24.dp))
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("Avisos", Modifier.semantics { heading() }, style = MaterialTheme.typography.titleLarge, color = c.ink)
                    GoalCard(notices)
                    notices.streakAlert?.let { StreakCard(it, if (notices.pairsInAlert) onOpenRanking else null) }
                    notices.update?.let { release ->
                        UpdateBanner(release, onDownload = { onDownload(release) }, onDismiss = { onDismissUpdate(release) })
                    }
                }
            }
        }
    }
}

private val DAY_MONTH: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM")

@Composable
private fun GoalCard(n: Notices) {
    val c = Trilha.colors
    val done = n.lessonsToday.coerceAtMost(n.goalLessons)
    val title = if (n.goalMet) "Meta de hoje batida" else "Meta de hoje: ${n.goalLessons} ${if (n.goalLessons == 1) "lição" else "lições"}"
    Column(
        Modifier.fillMaxWidth().border(2.dp, c.outline, RoundedCornerShape(18.dp)).padding(14.dp)
            .semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GameIconView(GameIcon.Target, size = 30.dp)
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = if (n.goalMet) c.successText else c.ink)
                Text("Ritmo para terminar a trilha até ${n.examDate.format(DAY_MONTH)}", style = MaterialTheme.typography.bodySmall, color = c.inkSoft)
            }
            Text(
                "$done/${n.goalLessons}", style = MaterialTheme.typography.titleSmall, color = if (n.goalMet) c.successText else c.inkSoft,
                modifier = Modifier.semantics { contentDescription = "$done de ${n.goalLessons} feitas" },
            )
        }
        if (n.goalLessons <= SEGMENTS_MAX) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(n.goalLessons) { i ->
                    Box(Modifier.weight(1f).height(10.dp).background(if (i < done) c.success else c.surfaceAlt, RoundedCornerShape(5.dp)))
                }
            }
        } else {
            GameProgressBar(done.toFloat() / n.goalLessons, Modifier.fillMaxWidth(), color = c.success, height = 10.dp)
        }
    }
}

/** Com mais lições que isso, a meta vira uma barra contínua em vez de um bloco por lição. */
private const val SEGMENTS_MAX = 8

@Composable
private fun StreakCard(text: String, onOpen: (() -> Unit)?) {
    val c = Trilha.colors
    val shape = RoundedCornerShape(18.dp)
    Row(
        Modifier.fillMaxWidth().background(c.warningSoft, shape).border(2.dp, c.warning, shape)
            .then(if (onOpen != null) Modifier.clickable(onClickLabel = "Ver ofensivas em dupla", onClick = onOpen) else Modifier)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        GameIconView(GameIcon.Flame, size = 30.dp)
        Column(Modifier.weight(1f)) {
            Text("Ofensiva em risco", style = MaterialTheme.typography.titleSmall, color = c.warningText)
            Text(text, style = MaterialTheme.typography.bodyMedium, color = c.warningText)
        }
    }
}
