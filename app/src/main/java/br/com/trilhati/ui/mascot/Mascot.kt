package br.com.trilhati.ui.mascot

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

/** Os quatro personagens originais, desenhados 100% em código (nada de imagens). */
enum class MascotCharacter(val displayName: String) {
    Guaiamum("Guaiamum"),
    Tartaruga("Tartaruga-marinha"),
    Tamandua("Tamanduá"),
    Preguica("Preguiça"),
}

enum class MascotPose(val label: String) {
    Idle("parado"),
    Happy("feliz"),
    Sad("triste"),
    Celebrate("comemorando"),
    Sleepy("sonolento"),
}

/** Valores-alvo de cada pose; a transição entre poses é animada por mola. */
private class PoseSpec(
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
)

private val MascotPose.spec: PoseSpec
    get() = when (this) {
        MascotPose.Idle -> PoseSpec(0.45f, 0f, 0f, 0f, 0f, 0f, 0.35f, 0f, 0f, 0f, 0f)
        MascotPose.Happy -> PoseSpec(1f, 0.45f, 1f, 0f, 0f, 0.3f, 1f, 0f, 0f, 0f, 0.4f)
        MascotPose.Sad -> PoseSpec(-1f, 0f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 0f)
        MascotPose.Celebrate -> PoseSpec(1f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f)
        MascotPose.Sleepy -> PoseSpec(0.2f, 0f, 0f, 1f, 0f, 0f, 0.5f, 0.3f, 1f, 0f, 0f)
    }

/**
 * Desenha um mascote. Tudo é desenhado numa grade de 200x200 e escalado para o tamanho disponível.
 *
 * @param animated `false` congela a animação em [frozenSeconds] (útil para prévias e para quem desliga animações).
 * @param decorative `true` esconde o mascote do TalkBack.
 */
@Composable
fun Mascot(
    character: MascotCharacter,
    pose: MascotPose,
    modifier: Modifier = Modifier,
    animated: Boolean = true,
    frozenSeconds: Float = 0.9f,
    decorative: Boolean = false,
    contentDescription: String? = null,
) {
    val spec = pose.spec
    val motion = spring<Float>(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    val smile by animateFloatAsState(spec.smile, motion, label = "smile")
    val open by animateFloatAsState(spec.open, motion, label = "open")
    val happyEyes by animateFloatAsState(spec.happyEyes, motion, label = "happyEyes")
    val closedEyes by animateFloatAsState(spec.closedEyes, motion, label = "closedEyes")
    val sadEyes by animateFloatAsState(spec.sadEyes, motion, label = "sadEyes")
    val arms by animateFloatAsState(spec.arms, motion, label = "arms")
    val blush by animateFloatAsState(spec.blush, motion, label = "blush")
    val droop by animateFloatAsState(spec.droop, motion, label = "droop")
    val sleepy by animateFloatAsState(spec.sleepy, motion, label = "sleepy")
    val celebrate by animateFloatAsState(spec.celebrate, motion, label = "celebrate")
    val bounce by animateFloatAsState(spec.bounce, motion, label = "bounce")

    var seconds by remember { mutableFloatStateOf(frozenSeconds) }
    if (animated && !br.com.trilhati.ui.theme.LocalReducedMotion.current) {
        LaunchedEffect(Unit) {
            val start = androidx.compose.runtime.withFrameNanos { it }
            while (true) {
                androidx.compose.runtime.withFrameNanos { seconds = (it - start) / 1_000_000_000f }
            }
        }
    }

    val semanticsModifier = if (decorative) {
        Modifier.clearAndSetSemantics { }
    } else {
        val description = contentDescription ?: "${character.displayName}, ${pose.label}"
        Modifier.semantics { this.contentDescription = description }
    }

    Canvas(modifier.aspectRatio(1f).then(semanticsModifier)) {
        val frame = Frame(
            seconds = seconds,
            smile = smile, open = open, happyEyes = happyEyes, closedEyes = closedEyes, sadEyes = sadEyes,
            arms = arms, blush = blush, droop = droop, sleepy = sleepy, celebrate = celebrate, bounce = bounce,
        )
        scale(size.minDimension / GRID, pivot = Offset.Zero) {
            drawMascot(character, frame)
        }
    }
}

internal const val GRID = 200f
