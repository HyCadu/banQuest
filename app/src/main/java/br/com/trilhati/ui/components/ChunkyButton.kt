package br.com.trilhati.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import br.com.trilhati.ui.icons.GameIcon
import br.com.trilhati.ui.icons.GameIconView
import br.com.trilhati.ui.theme.Spacing
import br.com.trilhati.ui.theme.Trilha
import java.util.Locale

enum class ChunkyStyle { Primary, Secondary, Success, Error, Warning, Neutral }

private class ButtonColors(val face: Color, val edge: Color, val content: Color)

@Composable
private fun ChunkyStyle.colors(enabled: Boolean): ButtonColors {
    val c = Trilha.colors
    if (!enabled) return ButtonColors(c.locked, c.lockedEdge, c.onLocked)
    return when (this) {
        ChunkyStyle.Primary -> ButtonColors(c.primary, c.primaryEdge, c.onPrimary)
        ChunkyStyle.Secondary -> ButtonColors(c.secondary, c.secondaryEdge, c.onSecondary)
        ChunkyStyle.Success -> ButtonColors(c.success, c.successEdge, c.onSuccess)
        ChunkyStyle.Error -> ButtonColors(c.error, c.errorEdge, c.onError)
        ChunkyStyle.Warning -> ButtonColors(c.warning, c.warningEdge, c.onWarning)
        ChunkyStyle.Neutral -> ButtonColors(c.surface, c.outline, c.primaryText)
    }
}

/**
 * Botão "gordinho" de jogo: tem uma lombada 3D embaixo e afunda ao ser tocado.
 * O rótulo vai em caixa alta, como em jogos.
 */
@Composable
fun ChunkyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ChunkyStyle = ChunkyStyle.Primary,
    enabled: Boolean = true,
    icon: GameIcon? = null,
    minHeight: Dp = 52.dp,
) {
    val colors = style.colors(enabled)
    val shape = RoundedCornerShape(18.dp)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val sink by animateDpAsState(
        targetValue = if (pressed && enabled) Spacing.edge else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "buttonSink",
    )
    Box(
        propagateMinConstraints = true,
        modifier = modifier
            .heightIn(min = Spacing.touchTarget)
            .semantics(mergeDescendants = true) { if (!enabled) disabled() }
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            ),
    ) {
        Box(Modifier.matchParentSize().padding(top = Spacing.edge).background(colors.edge, shape))
        Box(
            modifier = Modifier
                .padding(bottom = Spacing.edge)
                .offset { IntOffset(0, sink.roundToPx()) }
                .heightIn(min = minHeight)
                .background(colors.face, shape)
                .drawBehind {
                    if (enabled && style != ChunkyStyle.Neutral) {
                        drawRoundRect(
                            Color.White.copy(alpha = 0.22f),
                            topLeft = Offset(12.dp.toPx(), 5.dp.toPx()),
                            size = Size(size.width - 24.dp.toPx(), 5.dp.toPx()),
                            cornerRadius = CornerRadius(3.dp.toPx()),
                        )
                    }
                }
                .padding(horizontal = 20.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    GameIconView(icon, size = 22.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = text.uppercase(Locale.forLanguageTag("pt-BR")),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.content,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
