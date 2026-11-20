package br.com.trilhati.ui.animation.effects

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** Campos de cada partícula no vetor de dados (sem objeto por partícula). */
private const val FIELDS = 6
private const val F_ANGLE = 0
private const val F_SPEED = 1
private const val F_SIZE = 2
private const val F_COLOR = 3
private const val F_SPIN = 4
private const val F_SHAPE = 5

/**
 * Rajada de partículas desenhada num Canvas, disparada a cada novo [trigger] (0 = nada). Leve de propósito: no
 * máximo [count] partículas, dados num único FloatArray reaproveitado e nenhum objeto criado por quadro; o progresso é
 * lido só na fase de desenho, então a rajada não recompõe a tela. Não tem pointerInput: não rouba toque de nada.
 *
 * @param origin ponto de saída, em fração do tamanho (0.5, 0.5 = centro).
 * @param power distância máxima que uma partícula percorre.
 * @param gravity puxa as partículas para baixo no fim do voo (0 = sem gravidade).
 * @param upward limita a saída ao leque de cima (faíscas de chama, confete do acerto).
 */
@Composable
fun ParticleBurst(
    trigger: Int,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    count: Int = 24,
    power: Dp = 120.dp,
    sizeRange: ClosedFloatingPointRange<Float> = 3f..7f,
    durationMs: Int = 900,
    gravity: Float = 0.5f,
    origin: Offset = Offset(0.5f, 0.5f),
    upward: Boolean = false,
) {
    val n = count.coerceIn(1, MAX_PARTICLES)
    val data = remember(n) { FloatArray(n * FIELDS) }
    val progress = remember { Animatable(1f) }
    LaunchedEffect(trigger) {
        if (trigger == 0) return@LaunchedEffect
        val rnd = Random(trigger * 7919 + n)
        for (i in 0 until n) {
            val base = i * FIELDS
            data[base + F_ANGLE] = if (upward) (-PI / 2 + (rnd.nextFloat() - 0.5f) * PI * 0.9).toFloat() else (rnd.nextFloat() * 2 * PI).toFloat()
            data[base + F_SPEED] = 0.45f + rnd.nextFloat() * 0.55f
            data[base + F_SIZE] = sizeRange.start + rnd.nextFloat() * (sizeRange.endInclusive - sizeRange.start)
            data[base + F_COLOR] = rnd.nextInt(colors.size.coerceAtLeast(1)).toFloat()
            data[base + F_SPIN] = (rnd.nextFloat() - 0.5f) * 720f
            data[base + F_SHAPE] = if (rnd.nextFloat() < 0.5f) 0f else 1f
        }
        progress.snapTo(0f)
        progress.animateTo(1f, tween(durationMs, easing = LinearEasing))
    }
    Canvas(modifier) {
        val t = progress.value
        if (t >= 1f || colors.isEmpty()) return@Canvas
        val reach = power.toPx()
        val ox = size.width * origin.x
        val oy = size.height * origin.y
        // Sai rápido e freia (ease-out), some no fim.
        val travel = 1f - (1f - t) * (1f - t) * (1f - t)
        val fade = if (t < 0.6f) 1f else 1f - (t - 0.6f) / 0.4f
        for (i in 0 until n) {
            val base = i * FIELDS
            val angle = data[base + F_ANGLE]
            val dist = reach * data[base + F_SPEED] * travel
            val x = ox + cos(angle) * dist
            val y = oy + sin(angle) * dist + gravity * reach * t * t
            val r = data[base + F_SIZE].dp.toPx() * (1f - 0.4f * t)
            val color = colors[data[base + F_COLOR].toInt()].copy(alpha = fade)
            if (data[base + F_SHAPE] == 0f) {
                drawCircle(color, r, Offset(x, y))
            } else {
                rotate(data[base + F_SPIN] * t, Offset(x, y)) {
                    drawRoundRect(color, Offset(x - r, y - r * 0.6f), Size(r * 2f, r * 1.2f), CornerRadius(r * 0.3f))
                }
            }
        }
    }
}

/** Teto de partículas de uma rajada, para manter os quadros leves em celulares intermediários. */
const val MAX_PARTICLES = 48

/**
 * Brasas subindo devagar, em laço, enquanto o componente está na tela (a transição infinita para sozinha quando ele
 * sai). Posições derivadas do tempo e de sementes fixas: nada é alocado por quadro.
 */
@Composable
fun EmberField(
    color: Color,
    modifier: Modifier = Modifier,
    count: Int = 10,
    intensity: () -> Float = { 1f },
) {
    val n = count.coerceIn(1, MAX_PARTICLES)
    val seeds = remember(n) {
        val rnd = Random(11)
        FloatArray(n * 3) { rnd.nextFloat() }
    }
    val time = rememberInfiniteTransition(label = "embers").animateFloat(
        0f, 1f, infiniteRepeatable(tween(4_800, easing = LinearEasing)), label = "emberTime",
    )
    Canvas(modifier) {
        val k = intensity().coerceIn(0f, 1f)
        if (k <= 0f) return@Canvas
        val tt = time.value
        for (i in 0 until n) {
            val phase = (tt * (0.8f + seeds[i * 3] * 0.6f) + seeds[i * 3 + 1]) % 1f
            val wobble = sin((phase * 2f + seeds[i * 3 + 2]) * 2f * PI.toFloat()) * size.width * 0.05f
            val x = size.width * (0.28f + 0.44f * seeds[i * 3 + 2]) + wobble
            val y = size.height * (0.9f - 0.8f * phase)
            val alpha = sin(phase * PI.toFloat()) * k
            val r = (1.6f + 2.2f * seeds[i * 3]).dp.toPx() * (1f - 0.5f * phase)
            drawCircle(color.copy(alpha = alpha.coerceIn(0f, 1f)), r, Offset(x, y))
        }
    }
}
