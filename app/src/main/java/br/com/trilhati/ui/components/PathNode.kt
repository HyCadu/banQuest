package br.com.trilhati.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import br.com.trilhati.R
import br.com.trilhati.ui.icons.GameIcon
import br.com.trilhati.ui.icons.GameIconView
import br.com.trilhati.ui.icons.IconColors
import br.com.trilhati.ui.theme.Spacing
import br.com.trilhati.ui.theme.Trilha

enum class NodeState { Locked, Current, Done, Perfect }

/**
 * Nó da trilha (uma lição). O nó atual pulsa e mostra o balão "Começar".
 * [label] descreve a lição para o TalkBack (ex.: "Lição 2, Modelo ER").
 */
@Composable
fun PathNode(
    state: NodeState,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: GameIcon = GameIcon.Star,
    size: Dp = 76.dp,
) {
    val c = Trilha.colors
    val face = when (state) {
        NodeState.Locked -> c.locked
        NodeState.Current -> c.primary
        NodeState.Done -> c.secondary
        NodeState.Perfect -> c.warning
    }
    val edge = when (state) {
        NodeState.Locked -> c.lockedEdge
        NodeState.Current -> c.primaryEdge
        NodeState.Done -> c.secondaryEdge
        NodeState.Perfect -> c.warningEdge
    }
    val stateText = stringResource(
        when (state) {
            NodeState.Locked -> R.string.node_locked
            NodeState.Current -> R.string.node_current
            NodeState.Done -> R.string.node_done
            NodeState.Perfect -> R.string.node_perfect
        },
    )
    val transition = rememberInfiniteTransition(label = "node")
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.28f,
        animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "pulse",
    )
    val bob by transition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(tween(650, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bob",
    )
    val edgeHeight = 7.dp
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        if (state == NodeState.Current) {
            StartBubble(Modifier.offset { IntOffset(0, bob.dp.roundToPx()) })
        }
        Box(
            modifier = Modifier
                .size(size + 24.dp)
                .semantics(mergeDescendants = true) {
                    contentDescription = "$label, $stateText"
                    role = Role.Button
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (state == NodeState.Current) {
                Canvas(Modifier.size(size + 24.dp)) {
                    val ringAlpha = (1.28f - pulse) / 0.28f
                    drawCircle(
                        c.primary.copy(alpha = 0.45f * ringAlpha),
                        radius = (size.toPx() / 2f + 6.dp.toPx()) * pulse,
                        center = center,
                        style = Stroke(4.dp.toPx()),
                    )
                }
            }
            Box(Modifier.size(size, size + edgeHeight)) {
                Box(Modifier.size(size).offset(y = edgeHeight).background(edge, CircleShape))
                Box(
                    modifier = Modifier.size(size).background(face, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Canvas(Modifier.size(size)) {
                        drawArc(
                            Color.White.copy(alpha = if (state == NodeState.Locked) 0.25f else 0.3f),
                            startAngle = 200f,
                            sweepAngle = 60f,
                            useCenter = false,
                            topLeft = Offset(size.toPx() * 0.14f, size.toPx() * 0.14f),
                            size = androidx.compose.ui.geometry.Size(size.toPx() * 0.72f, size.toPx() * 0.72f),
                            style = Stroke(5.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round),
                        )
                    }
                    when (state) {
                        NodeState.Locked -> GameIconView(
                            GameIcon.Lock, size = size * 0.46f,
                            colors = IconColors(c.onLocked, c.locked, c.lockedEdge),
                        )
                        NodeState.Current -> GameIconView(icon, size = size * 0.5f)
                        NodeState.Done -> GameIconView(GameIcon.Check, size = size * 0.5f)
                        NodeState.Perfect -> GameIconView(GameIcon.Crown, size = size * 0.56f)
                    }
                }
            }
        }
    }
}

@Composable
private fun StartBubble(modifier: Modifier = Modifier) {
    val c = Trilha.colors
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = c.surface,
            border = BorderStroke(2.dp, c.primary),
        ) {
            Text(
                stringResource(R.string.node_start).uppercase(),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge,
                color = c.primaryText,
            )
        }
        Canvas(Modifier.size(14.dp, 8.dp).offset(y = (-2).dp)) {
            val p = Path().apply {
                moveTo(0f, 0f); lineTo(size.width, 0f); lineTo(size.width / 2f, size.height); close()
            }
            drawPath(p, c.surface)
            val edges = Path().apply { moveTo(0f, 0f); lineTo(size.width / 2f, size.height); lineTo(size.width, 0f) }
            drawPath(edges, c.primary, style = Stroke(2.dp.toPx()))
        }
    }
}

/** Cabeçalho de unidade da trilha ("Unidade 1 · Banco de dados"). Fica cinza quando a unidade ainda não tem conteúdo. */
@Composable
fun UnitBanner(
    unitNumber: Int,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    locked: Boolean = false,
) {
    val c = Trilha.colors
    val shape = RoundedCornerShape(22.dp)
    val face = if (locked) c.locked else c.primary
    val edge = if (locked) c.lockedEdge else c.primaryEdge
    val textColor = if (locked) c.onLocked else c.onPrimary
    Box(modifier.fillMaxWidth()) {
        Box(Modifier.matchParentSize().padding(top = Spacing.edge).background(edge, shape))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.edge)
                .background(face, shape)
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("UNIDADE $unitNumber", style = MaterialTheme.typography.labelMedium, color = textColor.copy(alpha = 0.85f))
                Text(title, style = MaterialTheme.typography.titleLarge, color = textColor)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = textColor.copy(alpha = 0.9f))
            }
            if (locked) {
                GameIconView(GameIcon.Lock, size = 36.dp, colors = IconColors(c.onLocked, c.locked, c.lockedEdge))
            } else {
                GameIconView(GameIcon.Book, size = 40.dp)
            }
        }
    }
}
