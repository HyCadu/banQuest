package br.com.trilhati.ui.animation.effects

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.max

/**
 * Brilho radial atrás do conteúdo. O pincel é criado uma vez por tamanho (drawWithCache) e a intensidade é lida na
 * fase de desenho, então o brilho pode pulsar a cada quadro sem recompor nem alocar.
 *
 * @param radius raio em fração do maior lado (1 = o brilho alcança as bordas).
 * @param center centro em fração do tamanho.
 */
fun Modifier.glow(
    color: Color,
    intensity: () -> Float,
    radius: Float = 0.75f,
    center: Offset = Offset(0.5f, 0.5f),
): Modifier = drawWithCache {
    val c = Offset(size.width * center.x, size.height * center.y)
    val r = max(size.width, size.height) * radius
    val brush = Brush.radialGradient(
        0f to color,
        0.45f to color.copy(alpha = color.alpha * 0.45f),
        1f to color.copy(alpha = 0f),
        center = c,
        radius = r.coerceAtLeast(1f),
    )
    onDrawBehind {
        val k = intensity().coerceIn(0f, 1f)
        if (k > 0f) drawCircle(brush, r, c, alpha = k)
    }
}
