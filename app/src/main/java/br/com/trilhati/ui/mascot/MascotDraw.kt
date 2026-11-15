package br.com.trilhati.ui.mascot

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
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
import kotlin.math.sin

/** Contorno escuro "de desenho animado" comum a todos os mascotes. */
internal val Ink = Color(0xFF2B1A5E)
private val MouthInside = Color(0xFF5A1740)
private val Tongue = Color(0xFFFF7A9A)
private val Cheek = Color(0xFFFF6F91)
private val TearColor = Color(0xFF6CC8FF)

internal const val OUTLINE = 4f

/** Estado instantâneo do mascote em um quadro de animação. */
internal class Frame(
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
) {
    /** 0 = olho aberto, 1 = piscando. Pisca a cada ~3,6 s. */
    val blink: Float
        get() {
            val phase = seconds % 3.6f
            return if (phase > 3.3f) (1f - abs((phase - 3.45f) / 0.15f)).coerceIn(0f, 1f) else 0f
        }

    /** Pequeno vai-e-vem dos braços/garras, mais forte quando comemora. */
    fun wave(speed: Float = 1f): Float = sin(seconds * 2f * PI.toFloat() * speed)
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
    drawMouth(face.mouth, face.mouthWidth, f)
}

private fun DrawScope.drawEye(face: FaceSpec, c: Offset, f: Frame, left: Boolean) {
    val rx = face.eyeRx
    val ry = face.eyeRy
    val line = Stroke(OUTLINE + 0.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val ballRadius = rx * 1.55f
    if (face.whiteEyeBall) {
        drawCircle(Color.White, ballRadius, c)
        drawCircle(Ink, ballRadius, c, style = Stroke(OUTLINE))
    }
    when {
        f.closedEyes > 0.5f -> {
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
            val look = sin(f.seconds * 0.9f) * 1.2f * (1f - f.sleepy)
            if (face.whiteEyeBall) {
                val py = c.y + 1.5f
                val pupilRy = ry * open
                drawOval(Ink, Offset(c.x - rx * 0.75f + look, py - pupilRy), Size(rx * 1.5f, pupilRy * 2f))
                drawCircle(Color.White, rx * 0.28f, Offset(c.x - rx * 0.2f + look, py - ry * 0.35f * open))
            } else {
                val h = ry * open
                drawOval(Ink, Offset(c.x - rx + look * 0.5f, c.y - h), Size(rx * 2f, h * 2f))
                if (open > 0.4f) {
                    drawCircle(Color.White, rx * 0.34f, Offset(c.x - rx * 0.3f + look * 0.5f, c.y - ry * 0.35f))
                    drawCircle(Color.White, rx * 0.17f, Offset(c.x + rx * 0.35f + look * 0.5f, c.y + ry * 0.38f))
                }
            }
            if (f.sadEyes > 0.5f) {
                val sign = if (left) 1f else -1f
                val top = if (face.whiteEyeBall) ballRadius + 3f else ry * 1.5f
                val outer = Offset(c.x - sign * rx * 1.5f, c.y - top)
                val inner = Offset(c.x + sign * rx * 1.2f, c.y - top - ry * 1.0f)
                drawLine(face.lineColor, outer, inner, strokeWidth = OUTLINE, cap = StrokeCap.Round)
            }
        }
    }
}

private fun DrawScope.drawMouth(m: Offset, w: Float, f: Frame) {
    val stroke = Stroke(OUTLINE, cap = StrokeCap.Round, join = StrokeJoin.Round)
    when {
        f.sleepy > 0.5f -> {
            val breathe = 1f + 0.18f * sin(f.seconds * 1.7f)
            drawOval(MouthInside, Offset(m.x - 3.2f, m.y - 3f), Size(6.4f, 7.5f * breathe))
            drawOval(Ink, Offset(m.x - 3.2f, m.y - 3f), Size(6.4f, 7.5f * breathe), style = Stroke(3f))
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
    val p = starPath(cx, cy, size, size * 0.28f)
    drawPath(p, color)
    drawPath(p, Ink, style = Stroke(2.5f, join = StrokeJoin.Round))
}

internal fun DrawScope.drawCelebrationEffects(f: Frame) {
    val w = f.celebrate
    if (w < 0.05f) return
    for (i in 0 until 16) {
        val phase = (f.seconds * 0.55f + i * 0.0625f * 3.1f) % 1f
        val x = 8f + ((i * 53) % 184) + sin(f.seconds * 2f + i) * 6f
        val y = -10f + phase * 215f
        val alpha = (1f - phase * phase) * w
        rotate(f.seconds * 160f + i * 40f, Offset(x, y)) {
            val color = ConfettiColors[i % ConfettiColors.size].copy(alpha = alpha)
            if (i % 3 == 0) {
                drawCircle(color, 3.4f, Offset(x, y))
            } else {
                drawRect(color, Offset(x - 3f, y - 5f), Size(6f, 10f))
            }
        }
    }
    for (i in 0 until 3) {
        val pulse = 0.55f + 0.45f * sin(f.seconds * 5f + i * 2.1f)
        val spots = listOf(Offset(26f, 40f), Offset(176f, 52f), Offset(150f, 18f))
        drawSparkle(spots[i].x, spots[i].y, 9f * pulse * w, Color(0xFFFFD84D))
    }
}

internal fun DrawScope.drawHappyEffects(f: Frame) {
    val w = ((f.happyEyes - f.celebrate).coerceIn(0f, 1f)) * (1f - f.celebrate)
    if (w < 0.05f) return
    val pulse = 0.6f + 0.4f * sin(f.seconds * 4f)
    drawSparkle(168f, 40f, 8f * pulse * w, Color(0xFFFFD84D))
    drawSparkle(34f, 58f, 6f * (1.2f - pulse * 0.4f) * w, Color(0xFF7DE8DD))
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

/** Aplica o movimento do corpo todo (respirar, pular, murchar) ao bloco desenhado. */
internal fun DrawScope.withBodyMotion(f: Frame, pivotY: Float = 190f, block: DrawScope.() -> Unit) {
    val s = f.seconds
    val twoPi = 2f * PI.toFloat()
    val breathe = sin(s * twoPi / lerp(2.4f, 3.8f, f.sleepy)) * lerp(2.6f, 1.6f, f.sleepy)
    val hop = abs(sin(s * twoPi / lerp(1.4f, 0.9f, f.celebrate))) * lerp(8f, 24f, f.celebrate) * f.bounce
    val dy = breathe - hop + f.droop * 7f
    val squash = 1f - 0.07f * f.droop + 0.03f * sin(s * twoPi / 2.4f) * (1f - f.bounce)
    val tilt = sin(s * 1.2f) * 4f * f.sleepy - 3f * f.droop + sin(s * twoPi / 0.9f) * 3f * f.celebrate
    translate(0f, dy) {
        scale(1f, squash, pivot = Offset(100f, pivotY)) {
            rotate(tilt, pivot = Offset(100f, pivotY)) { block() }
        }
    }
}
