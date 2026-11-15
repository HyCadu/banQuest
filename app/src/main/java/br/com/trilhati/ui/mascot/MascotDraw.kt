package br.com.trilhati.ui.mascot

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.util.lerp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** Contorno escuro "de desenho animado" comum a todos os mascotes. */
internal val Ink = Color(0xFF2B1A5E)
private val MouthInside = Color(0xFF5A1740)
private val Tongue = Color(0xFFFF7A9A)
private val Cheek = Color(0xFFFF6F91)
private val TearColor = Color(0xFF6CC8FF)
private val Gold = Color(0xFFFFD84D)

internal const val OUTLINE = 4f
private const val TWO_PI = 2f * PI.toFloat()

/** Sobe de 0 a 1 no começo do gesto, fica em 1 e desce no fim (sem trancos). */
internal fun envelope(t: Float, edge: Float = 0.18f): Float = min(1f, min(t / edge, (1f - t) / edge)).coerceIn(0f, 1f)

private fun smooth(t: Float): Float = t.coerceIn(0f, 1f).let { it * it * (3f - 2f * it) }

/**
 * Estado instantâneo do mascote em um quadro de animação. Os valores de pose (0 a 1) vêm animados por mola; [pop] é o
 * "squash & stretch" da troca de pose (-1 achatado, +1 esticado); [fidget]/[fidgetT] é o gesto de quem está parado.
 */
internal data class Frame(
    val seconds: Float,
    val smile: Float,
    val open: Float,
    val happyEyes: Float,
    val closedEyes: Float,
    val sadEyes: Float,
    val arms: Float,
    val blush: Float,
    val droop: Float,
    val sleepy: Float,
    val celebrate: Float,
    val bounce: Float,
    val think: Float = 0f,
    val nervous: Float = 0f,
    val surprise: Float = 0f,
    val waveArm: Float = 0f,
    val proud: Float = 0f,
    val idle: Float = 0f,
    val pop: Float = 0f,
    val fidget: Fidget? = null,
    val fidgetT: Float = 0f,
    /** Bocejo (a Preguiça usa no gesto próprio). */
    val yawn: Float = 0f,
) {
    /** 0 = olho aberto, 1 = piscando. Pisca a cada ~3,6 s (e duas vezes seguidas de vez em quando). */
    val blink: Float
        get() {
            val phase = seconds % 3.6f
            val one = if (phase > 3.3f) (1f - abs((phase - 3.45f) / 0.15f)).coerceIn(0f, 1f) else 0f
            val cycle = (seconds / 3.6f).toInt()
            val two = if (cycle % 3 == 2 && phase in 2.9f..3.2f) (1f - abs((phase - 3.05f) / 0.15f)).coerceIn(0f, 1f) else 0f
            return maxOf(one, two) * (1f - surprise)
        }

    /** Pequeno vai-e-vem dos braços/garras. */
    fun wave(speed: Float = 1f): Float = sin(seconds * TWO_PI * speed)

    /** Força do gesto próprio do personagem agora (0 a 1). */
    val signature: Float get() = if (fidget == Fidget.Signature) envelope(fidgetT) * idle else 0f

    /** Altura do pulinho espontâneo (0 a 1). */
    val fidgetHop: Float get() = if (fidget == Fidget.Hop) sin(fidgetT * PI.toFloat()) * idle else 0f

    /** Para onde os olhos apontam: -1 esquerda … +1 direita. */
    val lookX: Float
        get() {
            val around = if (fidget == Fidget.LookAround) {
                val t = fidgetT
                when {
                    t < 0.15f -> -smooth(t / 0.15f)
                    t < 0.45f -> -1f
                    t < 0.6f -> lerp(-1f, 1f, smooth((t - 0.45f) / 0.15f))
                    t < 0.85f -> 1f
                    else -> 1f - smooth((t - 0.85f) / 0.15f)
                } * idle
            } else 0f
            val dart = sin(seconds * 7f) * 0.6f * nervous
            return (around + 0.75f * think + dart).coerceIn(-1f, 1f)
        }

    /** -1 olhando para cima … +1 para baixo. */
    val lookY: Float get() = -0.9f * think

    /** Braço direito levantado (comemorar ou acenar). */
    val rightArm: Float get() = maxOf(arms, waveArm)

    /** Balanço do braço direito: suave ao comemorar, rápido ao acenar. */
    fun rightArmSwing(amplitude: Float): Float = wave(1.1f) * amplitude * arms + wave(2.4f) * amplitude * 1.8f * waveArm
}

// ---------- primitivas ----------

internal fun DrawScope.fillOval(
    cx: Float,
    cy: Float,
    rx: Float,
    ry: Float,
    color: Color,
    outline: Float = OUTLINE,
    degrees: Float = 0f,
) {
    rotate(degrees, Offset(cx, cy)) {
        val topLeft = Offset(cx - rx, cy - ry)
        val size = Size(rx * 2, ry * 2)
        drawOval(color, topLeft, size)
        if (outline > 0f) drawOval(Ink, topLeft, size, style = Stroke(outline))
    }
}

/**
 * Oval com volume: a cor base, uma meia-lua de sombra embaixo (luz vindo de cima à esquerda) e o contorno por cima.
 * Dá o aspecto de "adesivo gordinho" sem gradiente.
 */
internal fun DrawScope.fillOvalShaded(
    cx: Float,
    cy: Float,
    rx: Float,
    ry: Float,
    color: Color,
    outline: Float = OUTLINE,
    degrees: Float = 0f,
    shade: Float = 0.16f,
) {
    rotate(degrees, Offset(cx, cy)) {
        val rect = Rect(cx - rx, cy - ry, cx + rx, cy + ry)
        drawOval(color, rect.topLeft, rect.size)
        shadeInside(Path().apply { addOval(rect) }, rect, shade)
        if (outline > 0f) drawOval(Ink, rect.topLeft, rect.size, style = Stroke(outline))
    }
}

/** Meia-lua de sombra na parte de baixo de uma forma qualquer ([shape] com retângulo [bounds]). */
internal fun DrawScope.shadeInside(shape: Path, bounds: Rect, shade: Float = 0.16f) {
    val lit = Path().apply { addPath(shape, Offset(-bounds.width * 0.04f, -bounds.height * 0.13f)) }
    clipPath(shape) {
        clipPath(lit, ClipOp.Difference) {
            drawRect(Ink.copy(alpha = shade), bounds.topLeft, bounds.size)
        }
    }
}

internal fun DrawScope.fillPath(path: Path, color: Color, outline: Float = OUTLINE) {
    drawPath(path, color)
    if (outline > 0f) {
        drawPath(path, Ink, style = Stroke(outline, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/** Brilho suave no canto superior esquerdo, dá volume de "adesivo". */
internal fun DrawScope.shine(cx: Float, cy: Float, rx: Float, ry: Float, degrees: Float = -20f) {
    rotate(degrees, Offset(cx, cy)) {
        drawOval(Color.White.copy(alpha = 0.32f), Offset(cx - rx, cy - ry), Size(rx * 2, ry * 2))
        drawCircle(Color.White.copy(alpha = 0.45f), ry * 0.45f, Offset(cx + rx * 0.95f, cy + ry * 0.2f))
    }
}

/** Membro grosso com contorno: linha escura por baixo, cor por cima. */
internal fun DrawScope.limb(from: Offset, to: Offset, width: Float, color: Color) {
    drawLine(Ink, from, to, strokeWidth = width + OUTLINE * 2, cap = StrokeCap.Round)
    drawLine(color, from, to, strokeWidth = width, cap = StrokeCap.Round)
}

internal fun mix(a: Offset, b: Offset, t: Float) = Offset(lerp(a.x, b.x, t), lerp(a.y, b.y, t))

// ---------- rosto ----------

internal class FaceSpec(
    val eyeL: Offset,
    val eyeR: Offset,
    val eyeRx: Float,
    val eyeRy: Float,
    val mouth: Offset,
    val mouthWidth: Float,
    val blushL: Offset,
    val blushR: Offset,
    val whiteEyeBall: Boolean = false,
    val lineColor: Color = Ink,
)

internal fun DrawScope.drawFace(face: FaceSpec, f: Frame) {
    // bochechas rosadas
    val blushAlpha = 0.6f * f.blush
    if (blushAlpha > 0.01f) {
        for (c in listOf(face.blushL, face.blushR)) {
            drawOval(Cheek.copy(alpha = blushAlpha), Offset(c.x - 9f, c.y - 5.5f), Size(18f, 11f))
        }
    }
    drawEye(face, face.eyeL, f, left = true)
    drawEye(face, face.eyeR, f, left = false)
    drawBrows(face, f)
    drawMouth(face.mouth, face.mouthWidth, f)
}

private fun DrawScope.drawEye(face: FaceSpec, c: Offset, f: Frame, left: Boolean) {
    val rx = face.eyeRx
    val ry = face.eyeRy
    val line = Stroke(OUTLINE + 0.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val ballRadius = rx * 1.55f * (1f + 0.15f * f.surprise)
    if (face.whiteEyeBall) {
        drawCircle(Color.White, ballRadius, c)
        drawCircle(Ink, ballRadius, c, style = Stroke(OUTLINE))
    }
    val closed = maxOf(f.closedEyes, f.yawn)
    when {
        closed > 0.5f -> {
            val w = if (face.whiteEyeBall) rx * 0.9f else rx * 1.1f
            val p = Path().apply {
                moveTo(c.x - w, c.y - ry * 0.1f)
                quadraticTo(c.x, c.y + ry * 1.3f, c.x + w, c.y - ry * 0.1f)
            }
            drawPath(p, face.lineColor, style = line)
        }

        f.happyEyes > 0.5f -> {
            val w = if (face.whiteEyeBall) rx * 0.9f else rx * 1.1f
            val p = Path().apply {
                moveTo(c.x - w, c.y + ry * 0.6f)
                quadraticTo(c.x, c.y - ry * 1.5f, c.x + w, c.y + ry * 0.6f)
            }
            drawPath(p, face.lineColor, style = line)
        }

        else -> {
            val open = 1f - f.blink
            val wander = sin(f.seconds * 0.9f) * 0.4f * (1f - f.sleepy) * (1f - f.think)
            val lookX = (f.lookX + wander) * rx * 0.45f
            val lookY = f.lookY * ry * 0.3f
            // Surpreso: pupila maior. Nervoso: pupila menor.
            val size = 1f + 0.3f * f.surprise - 0.2f * f.nervous
            if (face.whiteEyeBall) {
                val prx = rx * 0.75f * size
                val pry = ry * open * size
                val px = c.x + lookX * 1.6f
                val py = c.y + 1.5f + lookY * 1.4f
                drawOval(Ink, Offset(px - prx, py - pry), Size(prx * 2f, pry * 2f))
                if (open > 0.4f) {
                    drawCircle(Color.White, rx * 0.3f * size, Offset(px - prx * 0.35f, py - pry * 0.45f))
                    drawCircle(Color.White, rx * 0.15f * size, Offset(px + prx * 0.4f, py + pry * 0.4f))
                }
            } else {
                val erx = rx * size
                val h = ry * open * size
                val px = c.x + lookX
                val py = c.y + lookY
                drawOval(Ink, Offset(px - erx, py - h), Size(erx * 2f, h * 2f))
                if (open > 0.4f) {
                    drawCircle(Color.White, erx * 0.36f, Offset(px - erx * 0.3f, py - h * 0.38f))
                    drawCircle(Color.White, erx * 0.18f, Offset(px + erx * 0.38f, py + h * 0.4f))
                }
            }
        }
    }
}

/** Sobrancelhas: tristes, preocupadas (nervoso), uma levantada (pensando) ou as duas altas (surpreso). */
private fun DrawScope.drawBrows(face: FaceSpec, f: Frame) {
    if (f.closedEyes > 0.5f || f.happyEyes > 0.5f || f.yawn > 0.5f) return
    val worried = maxOf(if (f.sadEyes > 0.5f) 1f else 0f, f.nervous)
    val raise = maxOf(f.surprise, f.think)
    if (worried < 0.3f && raise < 0.3f) return
    for (left in listOf(true, false)) {
        val c = if (left) face.eyeL else face.eyeR
        val sign = if (left) 1f else -1f
        val rx = face.eyeRx
        val top = if (face.whiteEyeBall) rx * 1.55f + 3f else face.eyeRy * 1.5f
        val (outer, inner) = when {
            worried >= 0.3f -> Offset(c.x - sign * rx * 1.5f, c.y - top) to Offset(c.x + sign * rx * 1.2f, c.y - top - face.eyeRy * 0.9f * worried)
            f.surprise >= 0.3f -> Offset(c.x - sign * rx * 1.3f, c.y - top - 4f * f.surprise) to Offset(c.x + sign * rx * 1.1f, c.y - top - 5f * f.surprise)
            else -> {
                // Pensando: só a sobrancelha direita sobe, um pouco torta.
                val lift = if (left) 0f else 4f * f.think
                Offset(c.x - sign * rx * 1.3f, c.y - top - lift) to Offset(c.x + sign * rx * 1.1f, c.y - top - lift - (if (left) 0f else 2f))
            }
        }
        val mid = Offset((outer.x + inner.x) / 2, minOf(outer.y, inner.y) - 2.5f)
        val p = Path().apply { moveTo(outer.x, outer.y); quadraticTo(mid.x, mid.y, inner.x, inner.y) }
        drawPath(p, face.lineColor, style = Stroke(OUTLINE, cap = StrokeCap.Round))
    }
}

private fun DrawScope.drawMouth(m: Offset, w: Float, f: Frame) {
    val stroke = Stroke(OUTLINE, cap = StrokeCap.Round, join = StrokeJoin.Round)
    when {
        f.yawn > 0.3f -> {
            // bocejo: boca alta e oval
            val h = 6f + 22f * f.yawn
            val rx = w * (0.5f + 0.25f * f.yawn)
            drawOval(MouthInside, Offset(m.x - rx, m.y - 3f), Size(rx * 2, h))
            clipPath(Path().apply { addOval(Rect(m.x - rx, m.y - 3f, m.x + rx, m.y - 3f + h)) }) {
                drawOval(Tongue, Offset(m.x - rx * 0.7f, m.y - 3f + h * 0.55f), Size(rx * 1.4f, h * 0.6f))
            }
            drawOval(Ink, Offset(m.x - rx, m.y - 3f), Size(rx * 2, h), style = Stroke(3.5f))
        }

        f.sleepy > 0.5f -> {
            val breathe = 1f + 0.18f * sin(f.seconds * 1.7f)
            drawOval(MouthInside, Offset(m.x - 3.2f, m.y - 3f), Size(6.4f, 7.5f * breathe))
            drawOval(Ink, Offset(m.x - 3.2f, m.y - 3f), Size(6.4f, 7.5f * breathe), style = Stroke(3f))
        }

        f.surprise > 0.5f -> {
            // "Oh!": boca redonda
            val r = 4f + 4f * f.surprise
            drawOval(MouthInside, Offset(m.x - r * 0.8f, m.y - 2f), Size(r * 1.6f, r * 2f))
            drawOval(Ink, Offset(m.x - r * 0.8f, m.y - 2f), Size(r * 1.6f, r * 2f), style = Stroke(3.5f))
        }

        f.nervous > 0.5f -> {
            // boca tremida (zigue-zague)
            val p = Path()
            val steps = 6
            val tremble = sin(f.seconds * 30f) * 0.6f
            for (i in 0..steps) {
                val x = m.x - w * 0.8f + (w * 1.6f) * i / steps
                val y = m.y + 3f + (if (i % 2 == 0) -2.4f else 2.4f) + tremble
                if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
            }
            drawPath(p, Ink, style = stroke)
        }

        f.think > 0.5f -> {
            // "Hmm": boquinha de lado
            val p = Path().apply {
                moveTo(m.x - w * 0.2f, m.y + 4f)
                quadraticTo(m.x + w * 0.25f, m.y + 6f, m.x + w * 0.65f, m.y + 2f)
            }
            drawPath(p, Ink, style = stroke)
        }

        f.open > 0.2f && f.smile > 0f -> {
            val depth = 7f + 20f * f.open
            val p = Path().apply {
                moveTo(m.x - w, m.y - 2f)
                cubicTo(m.x - w * 0.9f, m.y + depth * 1.35f, m.x + w * 0.9f, m.y + depth * 1.35f, m.x + w, m.y - 2f)
                close()
            }
            drawPath(p, MouthInside)
            clipPath(p) {
                drawOval(Tongue, Offset(m.x - w * 0.5f, m.y + depth * 0.55f), Size(w, depth * 0.7f))
            }
            drawPath(p, Ink, style = Stroke(3.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }

        f.smile >= 0f -> {
            val depth = 3f + 11f * f.smile
            val p = Path().apply {
                moveTo(m.x - w * 0.8f, m.y)
                quadraticTo(m.x, m.y + depth * 1.6f, m.x + w * 0.8f, m.y)
            }
            drawPath(p, Ink, style = stroke)
        }

        else -> {
            val depth = 11f * -f.smile
            val p = Path().apply {
                moveTo(m.x - w * 0.8f, m.y + 6f)
                quadraticTo(m.x, m.y + 6f - depth * 1.6f, m.x + w * 0.8f, m.y + 6f)
            }
            drawPath(p, Ink, style = stroke)
        }
    }
}

// ---------- efeitos ----------

/** Pontos de referência de cada personagem para os efeitos (gota de suor, "!" e balão de pensamento). */
internal class Anchors(val headTop: Offset, val headSide: Offset)

private val ConfettiColors = listOf(
    Color(0xFF8B5CF6), Color(0xFF19D3C5), Color(0xFFFFB000), Color(0xFFFF4D6D), Color(0xFF3DD17F),
)

internal fun starPath(cx: Float, cy: Float, outer: Float, inner: Float): Path = Path().apply {
    for (i in 0 until 8) {
        val r = if (i % 2 == 0) outer else inner
        val a = (-90f + i * 45f) * PI.toFloat() / 180f
        val x = cx + r * cos(a)
        val y = cy + r * sin(a)
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

internal fun DrawScope.drawSparkle(cx: Float, cy: Float, size: Float, color: Color) {
    if (size < 0.5f) return
    val p = starPath(cx, cy, size, size * 0.28f)
    drawPath(p, color)
    drawPath(p, Ink, style = Stroke(2.5f, join = StrokeJoin.Round))
}

/** Comemoração: poucos confetes (o corpo é que pula) e duas estrelinhas piscando. */
internal fun DrawScope.drawCelebrationEffects(f: Frame) {
    val w = f.celebrate
    if (w < 0.05f) return
    for (i in 0 until 9) {
        val phase = (f.seconds * 0.5f + i * 0.111f * 3.1f) % 1f
        val x = 10f + ((i * 61) % 180) + sin(f.seconds * 2f + i) * 6f
        val y = -10f + phase * 200f
        val alpha = (1f - phase * phase) * w
        rotate(f.seconds * 160f + i * 40f, Offset(x, y)) {
            val color = ConfettiColors[i % ConfettiColors.size].copy(alpha = alpha)
            if (i % 3 == 0) drawCircle(color, 3.4f, Offset(x, y)) else drawRect(color, Offset(x - 3f, y - 5f), Size(6f, 10f))
        }
    }
    for ((i, spot) in listOf(Offset(26f, 40f), Offset(172f, 30f)).withIndex()) {
        val pulse = 0.55f + 0.45f * sin(f.seconds * 5f + i * 2.1f)
        drawSparkle(spot.x, spot.y, 10f * pulse * w, Gold)
    }
}

internal fun DrawScope.drawHappyEffects(f: Frame) {
    val w = ((maxOf(f.happyEyes, f.proud) - f.celebrate).coerceIn(0f, 1f)) * (1f - f.celebrate)
    if (w < 0.05f) return
    val pulse = 0.6f + 0.4f * sin(f.seconds * 4f)
    drawSparkle(168f, 40f, 8f * pulse * w, Gold)
    drawSparkle(34f, 58f, 6f * (1.2f - pulse * 0.4f) * w, Color(0xFF7DE8DD))
    if (f.proud > 0.3f) drawSparkle(150f, 16f, 6f * (0.6f + 0.4f * sin(f.seconds * 3f + 1f)) * f.proud, Gold)
}

internal fun DrawScope.drawSleepyEffects(f: Frame) {
    val w = f.sleepy
    if (w < 0.05f) return
    for (i in 0 until 3) {
        val phase = (f.seconds / 2.6f + i / 3f) % 1f
        val size = 7f + phase * 9f
        val x = 150f + phase * 24f
        val y = 66f - phase * 52f
        val alpha = sin(phase * PI.toFloat()).coerceIn(0f, 1f) * w
        val z = Path().apply {
            moveTo(x, y)
            lineTo(x + size, y)
            lineTo(x, y + size)
            lineTo(x + size, y + size)
        }
        drawPath(z, Ink.copy(alpha = alpha), style = Stroke(7f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(z, Color(0xFFB9A4FF).copy(alpha = alpha), style = Stroke(3.6f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/** Pensando: balão de pensamento com reticências que aparecem uma a uma. */
internal fun DrawScope.drawThinkEffects(f: Frame, anchors: Anchors) {
    val w = f.think
    if (w < 0.05f) return
    val base = Offset(anchors.headTop.x + 44f, anchors.headTop.y - 2f)
    val bob = sin(f.seconds * 2f) * 2f
    val bubble = Color.White.copy(alpha = w)
    val line = Ink.copy(alpha = w)
    for ((i, r) in listOf(3.5f, 5.5f).withIndex()) {
        val c = Offset(base.x - 22f + i * 9f, base.y + 22f - i * 9f + bob)
        drawCircle(bubble, r, c)
        drawCircle(line, r, c, style = Stroke(2.5f))
    }
    val body = Rect(base.x - 8f, base.y - 18f + bob, base.x + 34f, base.y + 6f + bob)
    drawOval(bubble, body.topLeft, body.size)
    drawOval(line, body.topLeft, body.size, style = Stroke(3f))
    val shown = ((f.seconds * 2f) % 4f).toInt() // 0..3 pontinhos
    for (i in 0 until 3) {
        if (i < shown) drawCircle(line, 2.6f, Offset(body.left + 11f + i * 10f, body.center.y))
    }
}

/** Nervoso: gota de suor escorrendo na lateral da cabeça. */
internal fun DrawScope.drawSweat(f: Frame, anchors: Anchors) {
    val w = f.nervous
    if (w < 0.05f) return
    val phase = (f.seconds % 1.6f) / 1.6f
    val c = Offset(anchors.headSide.x, anchors.headSide.y + phase * 10f)
    val alpha = (1f - phase * 0.6f) * w
    val p = Path().apply {
        moveTo(c.x, c.y - 9f)
        cubicTo(c.x + 7f, c.y, c.x + 6f, c.y + 7f, c.x, c.y + 7f)
        cubicTo(c.x - 6f, c.y + 7f, c.x - 7f, c.y, c.x, c.y - 9f)
        close()
    }
    drawPath(p, TearColor.copy(alpha = alpha))
    drawPath(p, Ink.copy(alpha = alpha), style = Stroke(2.4f, join = StrokeJoin.Round))
    drawCircle(Color.White.copy(alpha = 0.8f * alpha), 1.8f, Offset(c.x - 2f, c.y + 1f))
}

/** Surpreso: "!" saltando acima da cabeça. */
internal fun DrawScope.drawSurpriseEffects(f: Frame, anchors: Anchors) {
    val w = f.surprise
    if (w < 0.05f) return
    val jump = abs(sin(f.seconds * 6f)) * 4f
    val c = Offset(anchors.headTop.x + 30f, anchors.headTop.y - 6f - jump)
    val h = 18f * w
    drawLine(Ink, Offset(c.x, c.y - h), Offset(c.x, c.y), strokeWidth = 9f, cap = StrokeCap.Round)
    drawLine(Gold, Offset(c.x, c.y - h), Offset(c.x, c.y), strokeWidth = 4.5f, cap = StrokeCap.Round)
    drawCircle(Ink, 5f * w, Offset(c.x, c.y + 10f))
    drawCircle(Gold, 2.6f * w, Offset(c.x, c.y + 10f))
}

/** Lágrima caindo de um dos olhos quando triste. */
internal fun DrawScope.drawTear(from: Offset, f: Frame) {
    val w = f.sadEyes
    if (w < 0.5f) return
    val phase = (f.seconds % 1.8f) / 1.8f
    val y = from.y + phase * 26f
    val alpha = (1f - phase * phase) * w
    val p = Path().apply {
        moveTo(from.x, y - 7f)
        cubicTo(from.x + 6f, y, from.x + 5f, y + 6f, from.x, y + 6f)
        cubicTo(from.x - 5f, y + 6f, from.x - 6f, y, from.x, y - 7f)
        close()
    }
    drawPath(p, TearColor.copy(alpha = alpha))
    drawPath(p, Ink.copy(alpha = alpha), style = Stroke(2.2f, join = StrokeJoin.Round))
}

/** Gorro de dormir (roxo com pompom turquesa) por cima da cabeça. */
internal fun DrawScope.drawNightCap(center: Offset, width: Float, f: Frame) {
    val w = f.sleepy
    if (w < 0.05f) return
    val sway = sin(f.seconds * 1.3f) * 3f
    translate(0f, (1f - w) * -30f) {
        val body = Path().apply {
            moveTo(center.x - width / 2, center.y + 8f)
            cubicTo(center.x - width * 0.35f, center.y - 34f, center.x + width * 0.1f, center.y - 40f, center.x + width * 0.55f + sway, center.y - 18f)
            cubicTo(center.x + width * 0.45f, center.y - 8f, center.x + width * 0.4f, center.y + 2f, center.x + width / 2, center.y + 8f)
            close()
        }
        fillPath(body, Color(0xFF7C55E8))
        val band = Path().apply {
            addRoundRect(androidx.compose.ui.geometry.RoundRect(Rect(center.x - width / 2 - 3f, center.y + 2f, center.x + width / 2 + 3f, center.y + 14f), androidx.compose.ui.geometry.CornerRadius(6f, 6f)))
        }
        fillPath(band, Color(0xFF19D3C5))
        drawCircle(Color.White, 7f, Offset(center.x + width * 0.55f + sway, center.y - 17f))
        drawCircle(Ink, 7f, Offset(center.x + width * 0.55f + sway, center.y - 17f), style = Stroke(OUTLINE))
    }
}

// ---------- corpo ----------

/** Movimento do corpo inteiro neste quadro: deslocamento, escala (squash & stretch) e inclinação. */
internal class BodyMotion(val dx: Float, val dy: Float, val scaleX: Float, val scaleY: Float, val tilt: Float) {
    /** Quanto o corpo está acima do chão (para a sombra encolher). */
    val lift: Float get() = (-dy).coerceAtLeast(0f)
}

internal fun bodyMotion(f: Frame): BodyMotion {
    val s = f.seconds
    val breathe = sin(s * TWO_PI / lerp(2.4f, 3.8f, f.sleepy)) * lerp(2.6f, 1.6f, f.sleepy)
    val hopPhase = abs(sin(s * TWO_PI / lerp(1.4f, 0.9f, f.celebrate)))
    val hop = hopPhase * lerp(8f, 24f, f.celebrate) * f.bounce
    // Na hora de tocar o chão achata, no alto do pulo estica (física de desenho animado).
    val air = (hopPhase * 2f - 1f) * f.bounce
    val fidgetHop = f.fidgetHop * 14f
    val popLift = if (f.pop > 0f) f.pop * 5f else 0f
    val dy = breathe - hop - fidgetHop - popLift + f.droop * 7f
    val jitter = sin(s * 47f) * 0.9f * f.nervous
    val breathSquash = 0.03f * sin(s * TWO_PI / 2.4f) * (1f - f.bounce)
    val scaleY = (1f - 0.07f * f.droop + breathSquash) * (1f + 0.05f * air) * (1f + 0.12f * f.pop) * (1f + 0.03f * f.proud)
    val scaleX = (1f - 0.035f * air) * (1f - 0.08f * f.pop)
    val tilt = sin(s * 1.2f) * 4f * f.sleepy - 3f * f.droop + sin(s * TWO_PI / 0.9f) * 3f * f.celebrate +
        f.lookX * 3f * f.idle + 5f * f.think - 3f * f.proud
    return BodyMotion(jitter, dy, scaleX, scaleY, tilt)
}

/** Sombra no chão: encolhe e clareia quando o personagem sobe. */
internal fun DrawScope.drawGroundShadow(motion: BodyMotion, width: Float = 112f, y: Float = 193f) {
    val k = (1f - motion.lift / 70f).coerceIn(0.45f, 1f)
    val rx = width / 2 * k
    drawOval(Ink.copy(alpha = 0.13f * k), Offset(100f - rx, y - 6f * k), Size(rx * 2, 12f * k))
}

/** Aplica o movimento do corpo todo (respirar, pular, murchar, squash & stretch) ao bloco desenhado. */
internal fun DrawScope.withBodyMotion(motion: BodyMotion, pivotY: Float = 190f, block: DrawScope.() -> Unit) {
    translate(motion.dx, motion.dy) {
        scale(motion.scaleX, motion.scaleY, pivot = Offset(100f, pivotY)) {
            rotate(motion.tilt, pivot = Offset(100f, pivotY)) { block() }
        }
    }
}
