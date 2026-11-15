package br.com.trilhati.ui.mascot

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
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
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

private const val TWO_PI = 2f * PI.toFloat()

private fun smoothStep(t: Float): Float = t.coerceIn(0f, 1f).let { it * it * (3f - 2f * it) }

internal fun DrawScope.drawMascot(character: MascotCharacter, f: Frame) {
    val motion = bodyMotion(f)
    val anchors = when (character) {
        MascotCharacter.Guaiamum -> Anchors(headTop = Offset(100f, 40f), headSide = Offset(158f, 94f))
        MascotCharacter.Tartaruga -> Anchors(headTop = Offset(100f, 34f), headSide = Offset(138f, 118f))
        MascotCharacter.Tamandua -> Anchors(headTop = Offset(92f, 48f), headSide = Offset(56f, 78f))
        MascotCharacter.Preguica -> Anchors(headTop = Offset(100f, 48f), headSide = Offset(154f, 80f))
    }
    val shadowWidth = when (character) {
        MascotCharacter.Guaiamum -> 150f
        MascotCharacter.Tartaruga -> 128f
        MascotCharacter.Tamandua -> 140f
        MascotCharacter.Preguica -> 112f
    }
    drawGroundShadow(motion, shadowWidth)
    withBodyMotion(motion) {
        when (character) {
            MascotCharacter.Guaiamum -> drawGuaiamum(f)
            MascotCharacter.Tartaruga -> drawTartaruga(f)
            MascotCharacter.Tamandua -> drawTamandua(f)
            MascotCharacter.Preguica -> drawPreguica(f)
        }
        drawSweat(f, anchors)
    }
    drawHappyEffects(f)
    drawCelebrationEffects(f)
    drawSleepyEffects(f)
    drawThinkEffects(f, anchors)
    drawSurpriseEffects(f, anchors)
}

// ===================== Guaiamum (caranguejo azul-arroxeado) =====================
// Jeito próprio: anda de lado (duas idas e voltas) estalando as garras.

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
    drawArc(Color.White.copy(alpha = 0.3f), start + 20f, 70f, false, Offset(center.x - radius * 0.7f, center.y - radius * 0.7f), Size(radius * 1.4f, radius * 1.4f), style = Stroke(3f, cap = StrokeCap.Round))
}

private fun DrawScope.drawGuaiamum(f: Frame) {
    val sig = f.signature
    val sidestep = sin(f.fidgetT * TWO_PI * 2f) * 10f * sig
    translate(sidestep, 0f) {
        val legSpeed = 6f + 16f * sig
        val wiggle = sin(f.seconds * legSpeed) * (2f + 2.5f * sig) * (1f - f.sleepy)
        // pernas
        val legs = listOf(
            Triple(Offset(52f, 128f), Offset(30f, 138f), Offset(22f, 156f)),
            Triple(Offset(58f, 138f), Offset(38f, 152f), Offset(34f, 172f)),
            Triple(Offset(70f, 146f), Offset(56f, 164f), Offset(56f, 184f)),
        )
        for ((i, leg) in legs.withIndex()) {
            val (a, b, c) = leg
            for (side in listOf(1f, -1f)) {
                fun mirror(o: Offset) = if (side > 0) o else Offset(200f - o.x, o.y)
                val step = wiggle * side * (if (i % 2 == 0) 1f else -1f)
                val p = Path().apply {
                    moveTo(mirror(a).x, mirror(a).y)
                    lineTo(mirror(b).x, mirror(b).y + step)
                    lineTo(mirror(c).x, mirror(c).y - maxOf(0f, step) * sig)
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
        // braços e garras (a direita acena)
        val lClaw = mix(Offset(26f, 100f), Offset(30f, 46f + f.wave(1.1f) * 5f), f.arms)
        val rClaw = mix(Offset(172f, 104f), Offset(170f, 52f), f.rightArm) + Offset(f.rightArmSwing(4f), -f.rightArmSwing(5f))
        limb(Offset(50f, 118f), lClaw, 8f, CrabDark)
        limb(Offset(150f, 118f), rClaw, 8f, CrabDark)
        val calm = 52f - 22f * (0.5f + 0.5f * sin(f.seconds * 3.2f)) * (1f - f.droop)
        val snap = 58f - 54f * (0.5f + 0.5f * sin(f.seconds * 24f))
        val pinch = lerp(calm, snap, sig)
        crabClaw(lClaw, 21f, 225f, pinch)
        crabClaw(rClaw, 16f, 315f, pinch)
        // corpo com barriga clara e sombra embaixo
        val bodyRect = Rect(36f, 76f, 164f, 168f)
        val body = Path().apply { addOval(bodyRect) }
        drawPath(body, CrabBody)
        drawOval(CrabBelly, Offset(58f, 122f), Size(84f, 40f))
        shadeInside(body, bodyRect, 0.14f)
        drawPath(body, Ink, style = Stroke(OUTLINE))
        for ((sx, sy) in listOf(80f to 104f, 100f to 98f, 120f to 104f)) {
            drawCircle(CrabDark.copy(alpha = 0.45f), 4.5f, Offset(sx, sy))
        }
        shine(76f, 98f, 18f, 8f, -25f)
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
}

private operator fun Offset.plus(o: Offset) = Offset(x + o.x, y + o.y)

// ===================== Tartaruga-marinha =====================
// Jeito próprio: recolhe a cabeça para dentro do casco, espera e volta com um pulinho.

private val TurtleSkin = Color(0xFF8EE2AE)
private val TurtleSkinDark = Color(0xFF55C487)
private val TurtleShell = Color(0xFF2FA57A)
private val TurtleShellLight = Color(0xFF4CCB9C)

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
    // Recolher: entra (0–0,3), fica escondida (0,3–0,65) e sai (0,65–1).
    val t = f.fidgetT
    val tuck = if (f.fidget == Fidget.Signature) {
        when {
            t < 0.3f -> smoothStep(t / 0.3f)
            t < 0.65f -> 1f
            else -> 1f - smoothStep((t - 0.65f) / 0.35f)
        } * f.idle
    } else 0f
    val peek = if (f.fidget == Fidget.Signature && t > 0.65f) sin((t - 0.65f) / 0.35f * PI.toFloat()) * 4f * f.idle else 0f

    // patas de trás
    fillOval(68f, 172f, 17f, 11f, TurtleSkinDark)
    fillOval(132f, 172f, 17f, 11f, TurtleSkinDark)
    // casco
    val shellRect = Rect(28f, 38f, 172f, 150f)
    val shell = Path().apply { addOval(shellRect) }
    drawPath(shell, TurtleShell)
    clipPath(shell) {
        fillPath(hexagon(100f, 64f, 22f), TurtleShellLight, outline = 3.5f)
        fillPath(hexagon(58f, 92f, 19f), TurtleShellLight, outline = 3.5f)
        fillPath(hexagon(142f, 92f, 19f), TurtleShellLight, outline = 3.5f)
        fillPath(hexagon(100f, 112f, 18f), TurtleShellLight, outline = 3.5f)
    }
    shadeInside(shell, shellRect, 0.16f)
    drawPath(shell, Ink, style = Stroke(OUTLINE))
    shine(70f, 56f, 24f, 8f, -28f)
    // nadadeiras (recolhem junto com a cabeça; a direita acena)
    val inward = 10f * tuck
    val lFlip = mix(Offset(40f + inward, 142f), Offset(32f, 98f + paddle), f.arms)
    val rRest = Offset(160f - inward, 142f)
    val rFlip = mix(rRest, Offset(168f, 98f), f.rightArm) + Offset(0f, -f.rightArmSwing(5f))
    fillOval(lFlip.x, lFlip.y + paddle * (1f - f.arms), 21f, 11f, TurtleSkinDark, degrees = lerp(-28f, -75f, f.arms))
    fillOval(rFlip.x, rFlip.y - paddle * (1f - f.rightArm), 21f, 11f, TurtleSkinDark, degrees = lerp(28f, 75f, f.rightArm))
    // cabeça: entra por trás da borda do casco. A parte escondida é o casco "subido" (borda curva, não um corte
    // reto); sobre a cabeça, essa borda ganha contorno, como se fosse a beirada do casco passando na frente dela.
    val rimLift = lerp(62f, 0f, tuck)
    val rim = Path().apply { addOval(Rect(shellRect.left, shellRect.top - rimLift, shellRect.right, shellRect.bottom - rimLift)) }
    val hidden = Path().apply { op(shell, rim, PathOperation.Intersect) }
    val headDy = -18f * tuck - peek
    val headScale = 1f - 0.15f * tuck
    clipPath(hidden, ClipOp.Difference) {
        translate(0f, headDy) {
            scale(headScale, pivot = Offset(100f, 136f)) { fillOvalShaded(100f, 136f, 40f, 33f, TurtleSkin) }
            shine(86f, 122f, 14f, 6f, -20f)
            drawNightCap(Offset(100f, 104f), 64f, f)
            drawFace(
                FaceSpec(
                    eyeL = Offset(85f, 132f), eyeR = Offset(115f, 132f), eyeRx = 5.4f, eyeRy = 7f,
                    mouth = Offset(100f, 148f), mouthWidth = 11f,
                    blushL = Offset(75f, 146f), blushR = Offset(125f, 146f),
                ),
                if (tuck > 0.4f) f.copy(closedEyes = 1f, happyEyes = 0f) else f,
            )
            drawTear(Offset(80f, 142f), f)
        }
    }
    if (tuck > 0.02f) {
        val head = Path().apply { addOval(Rect(100f - 40f * headScale, 136f + headDy - 33f * headScale, 100f + 40f * headScale, 136f + headDy + 33f * headScale)) }
        clipPath(head) { clipPath(shell) { drawPath(rim, Ink, style = Stroke(OUTLINE)) } }
    }
}

// ===================== Tamanduá-bandeira =====================
// De perfil (focinho comprido para a direita), faixa preta com borda branca no ombro e cauda em bandeira.
// Jeito próprio: fareja (o focinho treme) e mostra a língua comprida três vezes.

private val AntFur = Color(0xFFA9B1C6)
private val AntFurDark = Color(0xFF7A859F)
private val AntSnout = Color(0xFF9DA6BD)
private val AntStripe = Color(0xFF2E2A45)
private val AntClaw = Color(0xFFF2EBDD)
private val AntTongue = Color(0xFFFF7A9A)

private fun DrawScope.antClaws(paw: Offset) {
    for (dx in listOf(-5f, 4f)) {
        val p = Path().apply {
            moveTo(paw.x + dx, paw.y + 4f)
            quadraticTo(paw.x + dx + 7f, paw.y + 10f, paw.x + dx + 3f, paw.y + 16f)
        }
        drawPath(p, Ink, style = Stroke(6.5f, cap = StrokeCap.Round))
        drawPath(p, AntClaw, style = Stroke(3f, cap = StrokeCap.Round))
    }
}

private fun DrawScope.drawTamandua(f: Frame) {
    val sig = f.signature
    val t = f.fidgetT
    // cauda em bandeira, atrás, à esquerda
    val tailSwing = sin(f.seconds * 2.2f) * 5f + sin(f.seconds * 9f) * 3f * sig
    translate(68f, 176f) {
        rotate(-6f + tailSwing, Offset.Zero) {
            val plume = tailPlume()
            drawPath(plume, AntFur)
            shadeInside(plume, Rect(-70f, -122f, 14f, 12f), 0.14f)
            // mechas de pelo ao longo da cauda
            for (i in 0 until 5) {
                val k = 0.18f + i * 0.15f
                val a = bezier(Offset(4f, 0f), Offset(10f, -60f), Offset(-6f, -116f), k)
                val b = bezier(Offset(-14f, 10f), Offset(-76f, -58f), Offset(-6f, -118f), k)
                drawLine(AntFurDark, mix(a, b, 0.25f), mix(a, b, 0.75f), 3.5f, StrokeCap.Round)
            }
            drawPath(plume, Ink, style = Stroke(OUTLINE, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
    // corpo e faixa preta com borda branca
    val bodyRect = Rect(52f, 120f, 156f, 196f)
    val body = Path().apply { addOval(bodyRect) }
    drawPath(body, AntFur)
    clipPath(body) {
        val band = Path().apply { moveTo(40f, 132f); lineTo(80f, 116f); lineTo(176f, 186f); lineTo(152f, 214f); close() }
        drawPath(band, Color.White)
        val inner = Path().apply { moveTo(50f, 138f); lineTo(80f, 126f); lineTo(164f, 188f); lineTo(146f, 206f); close() }
        drawPath(inner, AntStripe)
    }
    shadeInside(body, bodyRect, 0.14f)
    drawPath(body, Ink, style = Stroke(OUTLINE))
    // patas da frente com garras grandes (a direita acena)
    val lPaw = mix(Offset(80f, 186f), Offset(54f, 118f + f.wave(1.2f) * 4f), f.arms)
    // levantada, a pata direita fica acima do focinho (senão some atrás dele)
    val rPaw = mix(Offset(130f, 186f), Offset(150f, 64f), f.rightArm) + Offset(f.rightArmSwing(3f), -f.rightArmSwing(4f))
    for (paw in listOf(lPaw, rPaw)) {
        fillOval(paw.x, paw.y, 13f, 10f, AntFurDark)
        antClaws(paw)
    }
    // orelhas pequenas e redondas
    fillOval(68f, 64f, 10f, 10f, AntFurDark)
    fillOval(108f, 58f, 10f, 10f, AntFurDark)
    drawCircle(AntStripe.copy(alpha = 0.5f), 4.5f, Offset(68f, 65f))
    drawCircle(AntStripe.copy(alpha = 0.5f), 4.5f, Offset(108f, 59f))
    // cabeça
    fillOvalShaded(92f, 92f, 38f, 34f, AntFur)
    shine(74f, 72f, 13f, 6f, -30f)
    // focinho comprido: fareja (treme) e cai um pouco quando triste
    val sniff = sin(f.seconds * 16f) * 2.5f * sig
    val snoutAngle = sniff + 7f * f.droop - 3f * f.think
    rotate(snoutAngle, Offset(104f, 108f)) {
        // cone: largo na base, fino e um pouco caído na ponta
        val snout = Path().apply {
            moveTo(94f, 86f)
            cubicTo(122f, 82f, 150f, 96f, 171f, 110f)
            quadraticTo(183f, 117f, 175f, 124f)
            cubicTo(156f, 125f, 124f, 130f, 96f, 130f)
            close()
        }
        drawPath(snout, AntSnout)
        shadeInside(snout, Rect(94f, 82f, 183f, 130f), 0.15f)
        // contorno só por cima, na ponta e por baixo: a base se funde com a cabeça (sem "caixa")
        val edge = Path().apply {
            moveTo(100f, 85.5f)
            cubicTo(124f, 82f, 150f, 96f, 171f, 110f)
            quadraticTo(183f, 117f, 175f, 124f)
            cubicTo(156f, 125f, 126f, 130f, 102f, 130f)
        }
        drawPath(edge, Ink, style = Stroke(OUTLINE, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawLine(Color.White.copy(alpha = 0.35f), Offset(116f, 92f), Offset(148f, 101f), 3.5f, StrokeCap.Round)
        // língua: sai três vezes durante o gesto
        val flick = max(0f, sin(t * TWO_PI * 3f)) * sig
        if (flick > 0.02f) {
            val len = 26f * flick
            val tongue = Path().apply {
                moveTo(179f, 120f)
                quadraticTo(184f + len * 0.6f, 122f + len * 0.2f, 182f + len * 0.5f, 120f + len)
            }
            drawPath(tongue, Ink, style = Stroke(6f, cap = StrokeCap.Round))
            drawPath(tongue, AntTongue, style = Stroke(3f, cap = StrokeCap.Round))
        }
        // narina na ponta
        drawOval(Ink, Offset(170f, 110f), Size(11f, 9f))
        drawCircle(Color.White.copy(alpha = 0.7f), 1.6f, Offset(173f, 112.5f))
        drawFace(
            FaceSpec(
                eyeL = Offset(78f, 84f), eyeR = Offset(104f, 80f), eyeRx = 4.6f, eyeRy = 6.2f,
                mouth = Offset(122f, 119f), mouthWidth = 9f,
                blushL = Offset(66f, 102f), blushR = Offset(122f, 106f),
            ),
            f,
        )
    }
    drawNightCap(Offset(88f, 62f), 62f, f)
    drawTear(Offset(73f, 92f), f)
}

private fun bezier(p0: Offset, p1: Offset, p2: Offset, t: Float): Offset {
    val u = 1f - t
    return Offset(u * u * p0.x + 2 * u * t * p1.x + t * t * p2.x, u * u * p0.y + 2 * u * t * p1.y + t * t * p2.y)
}

/** Cauda em bandeira: borda de dentro lisa e borda de fora em pontas de pelo (não em "nuvem"). */
private fun tailPlume(): Path = Path().apply {
    val tip = Offset(-6f, -118f)
    moveTo(8f, 6f)
    cubicTo(14f, -36f, 8f, -80f, tip.x, tip.y)
    val n = 7
    var prev = tip
    for (i in 1..n) {
        val t = i / n.toFloat()
        // borda de fora: da ponta até a base, bojuda para a esquerda; cada mecha de pelo cai para baixo
        val p = bezier(tip, Offset(-80f, -58f), Offset(-14f, 10f), t)
        val dir = Offset(p.x - prev.x, p.y - prev.y)
        val len = kotlin.math.sqrt(dir.x * dir.x + dir.y * dir.y).coerceAtLeast(0.01f)
        val normal = Offset(dir.y / len, -dir.x / len) // aponta para fora (esquerda)
        val tangent = Offset(dir.x / len, dir.y / len)
        val out = if (i == n) 3f else 7f
        val lock = mix(prev, p, 0.7f) + Offset(normal.x * out + tangent.x * 4f, normal.y * out + tangent.y * 4f)
        quadraticTo(mix(prev, p, 0.35f).x + normal.x * out * 0.9f, mix(prev, p, 0.35f).y + normal.y * out * 0.9f, lock.x, lock.y)
        lineTo(p.x, p.y)
        prev = p
    }
    close()
}

// ===================== Preguiça =====================
// Mãos grandes com três garras compridas. Jeito próprio: um bocejo lento, espreguiçando os braços.

private val SlothFur = Color(0xFFD1AA82)
private val SlothFurDark = Color(0xFFA67F57)
private val SlothFace = Color(0xFFF6E7CF)
private val SlothPatch = Color(0xFF6B4A32)
private val SlothClaw = Color(0xFF9C8466)

private fun DrawScope.slothHand(hand: Offset) {
    fillOval(hand.x, hand.y, 12f, 10f, SlothFurDark)
    for (k in -1..1) {
        val base = Offset(hand.x + k * 6f, hand.y + 5f)
        val tip = Offset(hand.x + k * 7f + 4f, hand.y + 20f)
        val p = Path().apply {
            moveTo(base.x, base.y)
            quadraticTo(base.x + 8f, base.y + 9f, tip.x, tip.y)
        }
        drawPath(p, Ink, style = Stroke(6.5f, cap = StrokeCap.Round))
        drawPath(p, SlothClaw, style = Stroke(2.8f, cap = StrokeCap.Round))
    }
}

private fun DrawScope.drawPreguica(f: Frame) {
    val yawn = f.signature
    val face = if (yawn > 0.05f) f.copy(yawn = yawn) else f
    val sway = sin(f.seconds * 1.4f) * 3f
    // braços compridos (espreguiça no bocejo; a direita acena)
    val stretch = 0.55f * yawn
    val lHand = mix(Offset(44f, 172f), Offset(44f, 84f + f.wave(1f) * 5f), max(f.arms, stretch))
    val rHand = mix(Offset(156f, 172f), Offset(156f, 84f), max(f.rightArm, stretch)) + Offset(0f, -f.rightArmSwing(5f))
    val lHandSway = Offset(lHand.x + sway * (1f - f.arms), lHand.y)
    val rHandSway = Offset(rHand.x - sway * (1f - f.rightArm), rHand.y)
    limb(Offset(62f, 138f), lHandSway, 16f, SlothFurDark)
    limb(Offset(138f, 138f), rHandSway, 16f, SlothFurDark)
    // corpo
    val bodyRect = Rect(50f, 124f, 150f, 200f)
    val body = Path().apply { addOval(bodyRect) }
    drawPath(body, SlothFur)
    drawOval(SlothFace, Offset(70f, 146f), Size(60f, 48f))
    shadeInside(body, bodyRect, 0.13f)
    drawPath(body, Ink, style = Stroke(OUTLINE))
    slothHand(lHandSway)
    slothHand(rHandSway)
    // orelhinhas e cabeça (inclina para trás no bocejo)
    rotate(-6f * yawn, Offset(100f, 140f)) {
        fillOval(58f, 62f, 11f, 11f, SlothFurDark)
        fillOval(142f, 62f, 11f, 11f, SlothFurDark)
        fillOvalShaded(100f, 98f, 56f, 46f, SlothFur, shade = 0.12f)
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
            face,
        )
        drawTear(Offset(76f, 108f), f)
    }
}
