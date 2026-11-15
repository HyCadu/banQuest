package br.com.trilhati.ui.mascot

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
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
    ;

    companion object {
        /** Personagem pelo nome salvo; desconhecido vira o Guaiamum. */
        fun of(name: String?): MascotCharacter = entries.firstOrNull { it.name == name } ?: Guaiamum
    }
}

enum class MascotPose(val label: String) {
    Idle("parado"),
    Happy("feliz"),
    Sad("triste"),
    Celebrate("comemorando"),
    Sleepy("sonolento"),
    Thinking("pensando"),
    Nervous("nervoso"),
    Surprised("surpreso"),
    Wave("acenando"),
    Proud("orgulhoso"),
}

/** Valores-alvo de cada pose; a transição entre poses é animada por mola. */
private class PoseSpec(
    val smile: Float = 0.45f,
    val open: Float = 0f,
    val happyEyes: Float = 0f,
    val closedEyes: Float = 0f,
    val sadEyes: Float = 0f,
    val arms: Float = 0f,
    val blush: Float = 0.35f,
    val droop: Float = 0f,
    val sleepy: Float = 0f,
    val celebrate: Float = 0f,
    val bounce: Float = 0f,
    val think: Float = 0f,
    val nervous: Float = 0f,
    val surprise: Float = 0f,
    val waveArm: Float = 0f,
    val proud: Float = 0f,
    /** Quanto o personagem faz gestos sozinho (olhar em volta, pulinho, o jeito próprio). */
    val idle: Float = 0f,
)

private val MascotPose.spec: PoseSpec
    get() = when (this) {
        MascotPose.Idle -> PoseSpec(idle = 1f)
        MascotPose.Happy -> PoseSpec(smile = 1f, open = 0.45f, happyEyes = 1f, arms = 0.3f, blush = 1f, bounce = 0.4f, idle = 0.6f)
        MascotPose.Sad -> PoseSpec(smile = -1f, sadEyes = 1f, blush = 0f, droop = 1f)
        MascotPose.Celebrate -> PoseSpec(smile = 1f, open = 1f, happyEyes = 1f, arms = 1f, blush = 1f, celebrate = 1f, bounce = 1f)
        MascotPose.Sleepy -> PoseSpec(smile = 0.2f, closedEyes = 1f, blush = 0.5f, droop = 0.3f, sleepy = 1f)
        MascotPose.Thinking -> PoseSpec(smile = 0.1f, blush = 0.3f, think = 1f)
        MascotPose.Nervous -> PoseSpec(smile = -0.3f, blush = 0.15f, nervous = 1f)
        MascotPose.Surprised -> PoseSpec(smile = 0.6f, arms = 0.55f, blush = 0.7f, bounce = 0.3f, surprise = 1f)
        MascotPose.Wave -> PoseSpec(smile = 0.9f, open = 0.35f, blush = 0.8f, waveArm = 1f)
        MascotPose.Proud -> PoseSpec(smile = 0.75f, happyEyes = 1f, blush = 0.9f, proud = 1f)
    }

/**
 * Desenha um mascote. Tudo é desenhado numa grade de 200x200 e escalado para o tamanho disponível.
 *
 * Animação: a troca de pose anima por mola e dá um "squash & stretch" (encolhe, estica e assenta); parado, o personagem
 * faz um gesto a cada 4 a 8 s ([FidgetSchedule]). Com [animated] `false` ou "Remover animações" ligado no Android, fica
 * parado em [frozenSeconds].
 *
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
    // Os valores são lidos só no desenho (Canvas), então a transição não recompõe a tela inteira a cada quadro.
    @Composable fun anim(target: Float, label: String): State<Float> = animateFloatAsState(target, motion, label = label)
    val smile = anim(spec.smile, "smile")
    val open = anim(spec.open, "open")
    val happyEyes = anim(spec.happyEyes, "happyEyes")
    val closedEyes = anim(spec.closedEyes, "closedEyes")
    val sadEyes = anim(spec.sadEyes, "sadEyes")
    val arms = anim(spec.arms, "arms")
    val blush = anim(spec.blush, "blush")
    val droop = anim(spec.droop, "droop")
    val sleepy = anim(spec.sleepy, "sleepy")
    val celebrate = anim(spec.celebrate, "celebrate")
    val bounce = anim(spec.bounce, "bounce")
    val think = anim(spec.think, "think")
    val nervous = anim(spec.nervous, "nervous")
    val surprise = anim(spec.surprise, "surprise")
    val waveArm = anim(spec.waveArm, "waveArm")
    val proud = anim(spec.proud, "proud")
    val idle = anim(spec.idle, "idle")

    val moving = animated && !br.com.trilhati.ui.theme.LocalReducedMotion.current
    var seconds by remember { mutableFloatStateOf(frozenSeconds) }
    if (moving) {
        LaunchedEffect(Unit) {
            val start = androidx.compose.runtime.withFrameNanos { it }
            while (true) {
                androidx.compose.runtime.withFrameNanos { seconds = (it - start) / 1_000_000_000f }
            }
        }
    }

    // Squash & stretch na troca de pose: encolhe (antecipação), estica e assenta com mola.
    val pop = remember { Animatable(0f) }
    var shownPose by remember { mutableStateOf(pose) }
    LaunchedEffect(pose) {
        if (pose == shownPose) return@LaunchedEffect
        shownPose = pose
        if (!moving) return@LaunchedEffect
        pop.animateTo(-1f, tween(90, easing = FastOutSlowInEasing))
        pop.animateTo(1f, tween(120, easing = FastOutSlowInEasing))
        pop.animateTo(0f, spring(dampingRatio = 0.4f, stiffness = 380f))
    }

    val semanticsModifier = if (decorative) {
        Modifier.clearAndSetSemantics { }
    } else {
        val description = contentDescription ?: "${character.displayName}, ${pose.label}"
        Modifier.semantics { this.contentDescription = description }
    }

    Canvas(modifier.aspectRatio(1f).then(semanticsModifier)) {
        val fidget = if (idle.value > 0.05f) FidgetSchedule.at(seconds, character.ordinal + 1) else null
        val frame = Frame(
            seconds = seconds,
            smile = smile.value, open = open.value, happyEyes = happyEyes.value, closedEyes = closedEyes.value,
            sadEyes = sadEyes.value, arms = arms.value, blush = blush.value, droop = droop.value, sleepy = sleepy.value,
            celebrate = celebrate.value, bounce = bounce.value, think = think.value, nervous = nervous.value,
            surprise = surprise.value, waveArm = waveArm.value, proud = proud.value, idle = idle.value,
            pop = pop.value, fidget = fidget?.fidget, fidgetT = fidget?.t ?: 0f,
        )
        scale(size.minDimension / GRID, pivot = Offset.Zero) {
            drawMascot(character, frame)
        }
    }
}

internal const val GRID = 200f
