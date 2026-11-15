package br.com.trilhati.ui.mascot

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal fun DrawScope.drawMascot(character: MascotCharacter, f: Frame) {
    withBodyMotion(f) {
        when (character) {
            MascotCharacter.Guaiamum -> drawGuaiamum(f)
            MascotCharacter.Tartaruga -> drawTartaruga(f)
            MascotCharacter.Tamandua -> drawTamandua(f)
            MascotCharacter.Preguica -> drawPreguica(f)
        }
    }
    drawHappyEffects(f)
    drawCelebrationEffects(f)
    drawSleepyEffects(f)
}

// ===================== Guaiamum (caranguejo azul-arroxeado) =====================

private val CrabBody = Color(0xFF6F6BF0)
private val CrabDark = Color(0xFF4B45C9)
private val CrabBelly = Color(0xFFAFAAFF)

private fun DrawScope.crabClaw(center: Offset, radius: Float, gapCenterDeg: Float, gapDeg: Float) {
    val start = gapCenterDeg + gapDeg / 2f
    val sweep = 360f - gapDeg
    val topLeft = Offset(center.x - radius, center.y - radius)
    val size = Size(radius * 2, radius * 2)
    drawArc(CrabDark, start, sweep, true, topLeft, size)
    drawArc(Ink, start, sweep, true, topLeft, size, style = Stroke(OUTLINE, join = StrokeJoin.Round))
    drawArc(Color.White.copy(alpha = 0.25f), start + 20f, 70f, false, Offset(center.x - radius * 0.7f, center.y - radius * 0.7f), Size(radius * 1.4f, radius * 1.4f), style = Stroke(3f, cap = StrokeCap.Round))
}

private fun DrawScope.drawGuaiamum(f: Frame) {
    val wiggle = sin(f.seconds * 6f) * 2f * (1f - f.sleepy)
    // pernas
    val legs = listOf(
        Triple(Offset(52f, 128f), Offset(30f, 138f), Offset(22f, 156f)),
        Triple(Offset(58f, 138f), Offset(38f, 152f), Offset(34f, 172f)),
        Triple(Offset(70f, 146f), Offset(56f, 164f), Offset(56f, 184f)),
    )
    for ((a, b, c) in legs) {
        for (side in listOf(1f, -1f)) {
            fun mirror(o: Offset) = if (side > 0) o else Offset(200f - o.x, o.y)
            val p = Path().apply {
                moveTo(mirror(a).x, mirror(a).y)
                lineTo(mirror(b).x, mirror(b).y + wiggle * side)
                lineTo(mirror(c).x, mirror(c).y)
            }
            drawPath(p, Ink, style = Stroke(14f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawPath(p, CrabDark, style = Stroke(6f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
    // hastes dos olhos
    for (x in listOf(80f, 120f)) {
        val top = Offset(if (x < 100f) 76f else 124f, 62f)
        limb(Offset(x, 90f), top, 7f, CrabDark)
    }
    // braços e garras
    val armWave = f.wave(1.1f)
    val lShoulder = Offset(50f, 118f)
    val rShoulder = Offset(150f, 118f)
    val lClaw = mix(Offset(26f, 100f), Offset(30f, 46f + armWave * 5f), f.arms)
    val rClaw = mix(Offset(172f, 104f), Offset(170f, 52f - armWave * 5f), f.arms)
    limb(lShoulder, lClaw, 8f, CrabDark)
    limb(rShoulder, rClaw, 8f, CrabDark)
    val pinch = 52f - 22f * (0.5f + 0.5f * sin(f.seconds * 3.2f)) * (1f - f.droop)
    crabClaw(lClaw, 21f, 225f, pinch)
    crabClaw(rClaw, 16f, 315f, pinch)
    // corpo
    fillOval(100f, 122f, 64f, 46f, CrabBody)
    fillOval(100f, 142f, 42f, 20f, CrabBelly, outline = 0f)
    for ((sx, sy) in listOf(80f to 104f, 100f to 98f, 120f to 104f)) {
        drawCircle(CrabDark.copy(alpha = 0.45f), 4.5f, Offset(sx, sy))
    }
    shine(78f, 100f, 18f, 8f, -25f)
    drawNightCap(Offset(100f, 90f), 64f, f)
    drawFace(
        FaceSpec(
            eyeL = Offset(76f, 52f), eyeR = Offset(124f, 52f), eyeRx = 5.6f, eyeRy = 7.4f,
            mouth = Offset(100f, 126f), mouthWidth = 13f,
            blushL = Offset(66f, 126f), blushR = Offset(134f, 126f), whiteEyeBall = true,
        ),
        f,
    )
    drawTear(Offset(70f, 70f), f)
}

// ===================== Tartaruga-marinha =====================

private val TurtleSkin = Color(0xFF8EE2AE)
private val TurtleSkinDark = Color(0xFF55C487)
private val TurtleShell = Color(0xFF2FA57A)
private val TurtleShellLight = Color(0xFF4CCB9C)
private val TurtleShellDark = Color(0xFF1E7F5C)

private fun hexagon(cx: Float, cy: Float, r: Float): Path = Path().apply {
    for (i in 0 until 6) {
        val a = (60f * i - 30f) * PI.toFloat() / 180f
        val x = cx + r * cos(a)
        val y = cy + r * sin(a)
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

private fun DrawScope.drawTartaruga(f: Frame) {
    val paddle = sin(f.seconds * 3f) * 4f * (1f - f.sleepy)
    // patas de trás
    fillOval(68f, 172f, 17f, 11f, TurtleSkinDark)
    fillOval(132f, 172f, 17f, 11f, TurtleSkinDark)
    // casco
    fillOval(100f, 94f, 72f, 56f, TurtleShell)
    clipPath(Path().apply { addOval(androidx.compose.ui.geometry.Rect(28f, 38f, 172f, 150f)) }) {
        fillPath(hexagon(100f, 64f, 22f), TurtleShellLight, outline = 3.5f)
        fillPath(hexagon(58f, 92f, 19f), TurtleShellLight, outline = 3.5f)
        fillPath(hexagon(142f, 92f, 19f), TurtleShellLight, outline = 3.5f)
        fillPath(hexagon(100f, 112f, 18f), TurtleShellLight, outline = 3.5f)
        drawLine(TurtleShellDark, Offset(100f, 42f), Offset(100f, 44f), 3f)
    }
    drawOval(Ink, Offset(28f, 38f), Size(144f, 112f), style = Stroke(OUTLINE))
    shine(70f, 56f, 24f, 8f, -28f)
    // nadadeiras
    val raise = f.arms
    val lFlip = mix(Offset(40f, 142f), Offset(32f, 98f + paddle), raise)
    val rFlip = mix(Offset(160f, 142f), Offset(168f, 98f - paddle), raise)
    fillOval(lFlip.x, lFlip.y + paddle * (1f - raise), 21f, 11f, TurtleSkinDark, degrees = lerpDeg(-28f, -75f, raise))
    fillOval(rFlip.x, rFlip.y - paddle * (1f - raise), 21f, 11f, TurtleSkinDark, degrees = lerpDeg(28f, 75f, raise))
    // cabeça
    fillOval(100f, 136f, 40f, 33f, TurtleSkin)
    shine(86f, 122f, 14f, 6f, -20f)
    drawNightCap(Offset(100f, 104f), 64f, f)
    drawFace(
        FaceSpec(
            eyeL = Offset(85f, 132f), eyeR = Offset(115f, 132f), eyeRx = 5.4f, eyeRy = 7f,
            mouth = Offset(100f, 148f), mouthWidth = 11f,
            blushL = Offset(75f, 146f), blushR = Offset(125f, 146f),
        ),
        f,
    )
    drawTear(Offset(80f, 142f), f)
}

private fun lerpDeg(a: Float, b: Float, t: Float) = a + (b - a) * t

// ===================== Tamanduá =====================

private val AntFur = Color(0xFFA9B1C6)
private val AntFurDark = Color(0xFF7A859F)
private val AntSnout = Color(0xFF8C97B1)
private val AntStripe = Color(0xFF2E2A45)

private fun DrawScope.drawTamandua(f: Frame) {
    val tailSwing = sin(f.seconds * 2.2f) * 6f
    // cauda felpuda: pluma larga que afina até a ponta
    rotate(14f + tailSwing, Offset(140f, 178f)) {
        val plume = Path().apply {
            moveTo(134f, 176f)
            cubicTo(116f, 150f, 124f, 108f, 150f, 74f)
            cubicTo(158f, 66f, 164f, 70f, 166f, 80f)
            cubicTo(176f, 112f, 184f, 150f, 158f, 184f)
            close()
        }
        fillPath(plume, AntFur)
        for (i in 0 until 4) {
            val y = 100f + i * 22f
            drawLine(AntStripe.copy(alpha = 0.5f), Offset(140f - i * 1.5f, y), Offset(166f - i * 2f, y + 12f), 4.5f, StrokeCap.Round)
        }
    }
    // corpo e faixa preta com borda branca
    fillOval(100f, 160f, 54f, 40f, AntFur)
    clipPath(Path().apply { addOval(androidx.compose.ui.geometry.Rect(46f, 120f, 154f, 200f)) }) {
        val band = Path().apply {
            moveTo(36f, 132f); lineTo(76f, 118f); lineTo(172f, 188f); lineTo(150f, 214f); close()
        }
        drawPath(band, Color.White)
        val inner = Path().apply {
            moveTo(46f, 138f); lineTo(76f, 128f); lineTo(160f, 190f); lineTo(144f, 206f); close()
        }
        drawPath(inner, AntStripe)
    }
    drawOval(Ink, Offset(46f, 120f), Size(108f, 80f), style = Stroke(OUTLINE))
    // patinhas / braços
    val lPaw = mix(Offset(62f, 186f), Offset(52f, 116f + f.wave(1.2f) * 4f), f.arms)
    val rPaw = mix(Offset(138f, 186f), Offset(148f, 116f - f.wave(1.2f) * 4f), f.arms)
    fillOval(lPaw.x, lPaw.y, 13f, 11f, AntFurDark)
    fillOval(rPaw.x, rPaw.y, 13f, 11f, AntFurDark)
    for (dx in listOf(-5f, 0f, 5f)) {
        drawLine(Ink, Offset(lPaw.x + dx, lPaw.y + 5f), Offset(lPaw.x + dx, lPaw.y + 11f), 2.5f, StrokeCap.Round)
        drawLine(Ink, Offset(rPaw.x + dx, rPaw.y + 5f), Offset(rPaw.x + dx, rPaw.y + 11f), 2.5f, StrokeCap.Round)
    }
    // orelhas
    fillOval(66f, 54f, 12f, 12f, AntFurDark)
    fillOval(134f, 54f, 12f, 12f, AntFurDark)
    fillOval(66f, 55f, 5f, 5f, AntStripe.copy(alpha = 0.55f), outline = 0f)
    fillOval(134f, 55f, 5f, 5f, AntStripe.copy(alpha = 0.55f), outline = 0f)
    // cabeça
    fillOval(100f, 98f, 42f, 50f, AntFur)
    val mask = Path().apply {
        moveTo(62f, 70f)
        cubicTo(76f, 50f, 124f, 50f, 138f, 70f)
        cubicTo(124f, 62f, 76f, 62f, 62f, 70f)
        close()
    }
    drawPath(mask, AntFurDark.copy(alpha = 0.7f))
    shine(84f, 76f, 14f, 7f, -30f)
    // focinho comprido
    val snout = Path().apply {
        moveTo(84f, 104f)
        cubicTo(86f, 134f, 92f, 160f, 100f, 174f)
        cubicTo(108f, 160f, 114f, 134f, 116f, 104f)
        close()
    }
    fillPath(snout, AntSnout)
    drawCircle(Ink, 5.5f, Offset(100f, 172f))
    drawNightCap(Offset(100f, 62f), 66f, f)
    drawFace(
        FaceSpec(
            eyeL = Offset(83f, 94f), eyeR = Offset(117f, 94f), eyeRx = 4.8f, eyeRy = 6.4f,
            mouth = Offset(100f, 140f), mouthWidth = 8f,
            blushL = Offset(70f, 116f), blushR = Offset(130f, 116f),
        ),
        f,
    )
    drawTear(Offset(79f, 104f), f)
}

// ===================== Preguiça =====================

private val SlothFur = Color(0xFFD1AA82)
private val SlothFurDark = Color(0xFFA67F57)
private val SlothFace = Color(0xFFF6E7CF)
private val SlothPatch = Color(0xFF6B4A32)

private fun DrawScope.drawPreguica(f: Frame) {
    val sway = sin(f.seconds * 1.4f) * 3f
    // braços compridos
    val lHand = mix(Offset(44f, 172f), Offset(44f, 84f + f.wave(1f) * 5f), f.arms)
    val rHand = mix(Offset(156f, 172f), Offset(156f, 84f - f.wave(1f) * 5f), f.arms)
    val lHandSway = Offset(lHand.x + sway * (1f - f.arms), lHand.y)
    val rHandSway = Offset(rHand.x - sway * (1f - f.arms), rHand.y)
    limb(Offset(62f, 138f), lHandSway, 16f, SlothFurDark)
    limb(Offset(138f, 138f), rHandSway, 16f, SlothFurDark)
    // corpo
    fillOval(100f, 162f, 50f, 38f, SlothFur)
    fillOval(100f, 170f, 30f, 24f, SlothFace, outline = 0f)
    // garras
    for (hand in listOf(lHandSway, rHandSway)) {
        for (dx in listOf(-5f, 0f, 5f)) {
            drawLine(Ink, Offset(hand.x + dx, hand.y + 8f), Offset(hand.x + dx * 1.2f, hand.y + 18f), 3.2f, StrokeCap.Round)
            drawLine(SlothFace, Offset(hand.x + dx, hand.y + 9f), Offset(hand.x + dx * 1.2f, hand.y + 16f), 1.6f, StrokeCap.Round)
        }
    }
    // orelhinhas e cabeça
    fillOval(58f, 62f, 11f, 11f, SlothFurDark)
    fillOval(142f, 62f, 11f, 11f, SlothFurDark)
    fillOval(100f, 98f, 56f, 46f, SlothFur)
    shine(76f, 66f, 18f, 7f, -22f)
    // máscara clara + manchas escuras dos olhos
    fillOval(100f, 106f, 42f, 32f, SlothFace, outline = 0f)
    fillOval(80f, 98f, 15f, 12f, SlothPatch, outline = 0f, degrees = -22f)
    fillOval(120f, 98f, 15f, 12f, SlothPatch, outline = 0f, degrees = 22f)
    fillOval(100f, 113f, 8f, 5.5f, Ink, outline = 0f)
    drawCircle(Color.White.copy(alpha = 0.8f), 1.8f, Offset(97.5f, 111.5f))
    drawNightCap(Offset(100f, 62f), 74f, f)
    drawFace(
        FaceSpec(
            eyeL = Offset(80f, 98f), eyeR = Offset(120f, 98f), eyeRx = 5f, eyeRy = 6.6f,
            mouth = Offset(100f, 124f), mouthWidth = 11f,
            blushL = Offset(66f, 118f), blushR = Offset(134f, 118f), lineColor = SlothFace,
        ),
        f,
    )
    drawTear(Offset(76f, 108f), f)
}
