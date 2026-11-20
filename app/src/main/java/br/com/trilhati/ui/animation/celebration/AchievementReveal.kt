package br.com.trilhati.ui.animation.celebration

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import br.com.trilhati.ui.icons.GameIcon
import br.com.trilhati.ui.icons.GameIconView
import br.com.trilhati.ui.theme.Trilha
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Cartão de conquista que entra "girando para o lugar", com raios de luz girando atrás do troféu. [p] guia a entrada
 * (0 a 1, com mola que passa do ponto); os raios só giram com a animação ligada e o cartão na tela.
 */
@Composable
internal fun AchievementReveal(item: AchievementItem, p: Animatable<Float, AnimationVector1D>, motion: Boolean) {
    val c = Trilha.colors
    val shape = RoundedCornerShape(20.dp)
    val spin = if (motion) {
        rememberInfiniteTransition(label = "rays").animateFloat(0f, 360f, infiniteRepeatable(tween(6_000, easing = LinearEasing)), label = "raysSpin")
    } else {
        null
    }
    Row(
        Modifier.fillMaxWidth()
            .graphicsLayer {
                val v = p.value
                alpha = v.coerceIn(0f, 1f)
                val s = 0.4f + 0.6f * v
                scaleX = s
                scaleY = s
                rotationZ = (1f - v) * -10f
            }
            .background(c.warningSoft, shape).border(2.dp, c.warning, shape).padding(12.dp)
            .semantics(mergeDescendants = true) { contentDescription = "Conquista: ${item.title}. ${item.description}" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(64.dp)) {
                val k = p.value.coerceIn(0f, 1f)
                val center = Offset(size.width / 2f, size.height / 2f)
                rotate(spin?.value ?: 0f, center) {
                    for (i in 0 until 10) {
                        val a = (i / 10f) * 2f * PI.toFloat()
                        val inner = size.minDimension * 0.28f
                        val outer = size.minDimension * 0.5f
                        drawLine(
                            c.warning.copy(alpha = 0.55f * k),
                            Offset(center.x + cos(a) * inner, center.y + sin(a) * inner),
                            Offset(center.x + cos(a) * outer, center.y + sin(a) * outer),
                            strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round,
                        )
                    }
                }
            }
            Box(Modifier.size(44.dp).background(c.surface, CircleShape), contentAlignment = Alignment.Center) {
                GameIconView(GameIcon.Trophy, size = 30.dp)
            }
        }
        Column(Modifier.weight(1f)) {
            Text("Conquista nova", style = MaterialTheme.typography.labelMedium, color = c.warningText)
            Text(item.title, style = MaterialTheme.typography.titleMedium, color = c.ink)
            Text(item.description, style = MaterialTheme.typography.bodySmall, color = c.inkSoft)
        }
    }
}
