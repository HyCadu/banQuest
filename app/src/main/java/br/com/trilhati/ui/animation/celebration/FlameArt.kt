package br.com.trilhati.ui.animation.celebration

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.lerp
import kotlin.math.PI
import kotlin.math.sin

/** Cores da chama apagada (brasa escura) e acesa (as mesmas do ícone de ofensiva do app). */
internal object FlameColors {
    val bodyOff = Color(0xFF2F2A48)
    val outlineOff = Color(0xFF4C4570)
    val coreOff = Color(0xFFFFB238)
    val bodyOn = Color(0xFFFF7A1A)
    val coreOn = Color(0xFFFFD84D)
    val outlineOn = Color(0xFF2B1A5E)
    val glow = Color(0xFFFF9A2E)
}

/**
 * A chama da ofensiva desenhada em Canvas, na geometria do ícone do app, viva: a ponta balança, as laterais respiram e o
 * núcleo pulsa. Tudo lido na fase de desenho; os dois Path são reaproveitados (reset a cada quadro, sem alocar).
 *
 * @param lit 0 = apagada (brasa escura com núcleo âmbar fraco), 1 = acesa (laranja e amarelo).
 * @param heat 0 a 1: o núcleo cresce e a chama se ergue enquanto o aluno arrasta.
 * @param time 0 a 1 em laço, para o balanço (constante quando a animação está desligada).
 */
@Composable
internal fun FlameArt(
    modifier: Modifier,
    lit: () -> Float,
    heat: () -> Float,
    time: () -> Float,
) {
    val outer = remember { Path() }
    val inner = remember { Path() }
    Canvas(modifier) {
        val l = lit().coerceIn(0f, 1f)
        val h = heat().coerceIn(0f, 1f)
        val t = time()
        val tau = 2f * PI.toFloat()
        // Mais calor, mais movimento.
        val energy = 0.5f + 0.5f * maxOf(l, h)
        val sway = sin(t * tau) * 1.8f * energy
        val sway2 = sin(t * tau * 2f + 1.3f) * 0.9f * energy
        val breathe = sin(t * tau * 0.5f) * 0.9f
        val rise = h * 2.5f + l * 1.5f

        val s = size.minDimension / 48f
        val dx = (size.width - 48f * s) / 2f
        val dy = (size.height - 48f * s) / 2f
        scale(s, s, pivot = Offset.Zero) {
            val ox = dx / s
            val oy = dy / s
            outer.reset()
            outer.moveTo(ox + 24f + sway, oy + 3f - breathe - rise)
            outer.cubicTo(ox + 27f + sway * 0.6f, oy + 12f - rise * 0.5f, ox + 40f + breathe * 0.4f, oy + 18f, ox + 40f + sway2 * 0.2f, oy + 31f)
            outer.cubicTo(ox + 40f, oy + 40f, ox + 33f, oy + 45f, ox + 24f, oy + 45f)
            outer.cubicTo(ox + 15f, oy + 45f, ox + 8f, oy + 40f, ox + 8f - sway2 * 0.2f, oy + 31f)
            outer.cubicTo(ox + 8f, oy + 25f, ox + 12f - breathe * 0.3f, oy + 21f, ox + 15f + sway2 * 0.3f, oy + 18f - rise * 0.3f)
            outer.cubicTo(ox + 16f, oy + 23f, ox + 19f, oy + 24f, ox + 20f, oy + 23f)
            outer.cubicTo(ox + 19f + sway * 0.4f, oy + 15f, ox + 21f + sway * 0.7f, oy + 8f, ox + 24f + sway, oy + 3f - breathe - rise)
            outer.close()

            val body = lerp(FlameColors.bodyOff, FlameColors.bodyOn, l)
            val line = lerp(FlameColors.outlineOff, FlameColors.outlineOn, l)
            drawPath(outer, body)

            // Núcleo: brilho em camadas (sem pincel novo por quadro) e a gota interna.
            val coreColor = lerp(FlameColors.coreOff, FlameColors.coreOn, l)
            val pulse = 0.85f + 0.15f * sin(t * tau * 1.5f)
            val coreR = (4f + 5f * h + 3f * l) * pulse
            val coreCenter = Offset(ox + 24f + sway * 0.4f, oy + 35f - h * 2f)
            // O brilho fica dentro do contorno da chama (sem disco vazando para fora).
            clipPath(outer) {
                drawCircle(coreColor.copy(alpha = 0.22f + 0.14f * h), coreR * 2.2f, coreCenter)
                drawCircle(coreColor.copy(alpha = 0.38f + 0.2f * h), coreR * 1.5f, coreCenter)
            }

            val k = 0.55f + 0.45f * maxOf(l, h)
            val cx = ox + 24f + sway * 0.5f
            val top = oy + 35f - 11f * k - h * 2f
            inner.reset()
            inner.moveTo(cx, top)
            inner.cubicTo(cx + 3f * k, top + 4f * k, cx + 8f * k, top + 6f * k, cx + 8f * k, top + 11f * k)
            inner.cubicTo(cx + 8f * k, top + 16f * k, cx + 4f * k, top + 18f * k, cx, top + 18f * k)
            inner.cubicTo(cx - 4f * k, top + 18f * k, cx - 8f * k, top + 16f * k, cx - 8f * k, top + 11f * k)
            inner.cubicTo(cx - 8f * k, top + 7f * k, cx - 2f * k, top + 5f * k, cx, top)
            inner.close()
            drawPath(inner, coreColor.copy(alpha = 0.55f + 0.45f * maxOf(l, h)))

            drawPath(outer, line, style = Stroke(2.6f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}
