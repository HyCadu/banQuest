package br.com.trilhati.ui.animation.feedback

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import br.com.trilhati.ui.animation.MotionMs
import br.com.trilhati.ui.animation.effects.ParticleBurst
import br.com.trilhati.ui.animation.motionOn
import br.com.trilhati.ui.theme.Trilha

/** Resultado de um elemento respondido (alternativa, par, linha), para a microinteração. */
enum class FeedbackKind { None, Correct, Wrong }

/**
 * Movimento de feedback de uma resposta, igual em todos os formatos de questão: no acerto, o elemento cresce um pouco
 * e volta com mola; no erro, treme de leve para os lados e para. Só mexe na camada de desenho ([feedbackMotion]):
 * não muda o layout nem interfere em toques.
 */
@Stable
class FeedbackMotion internal constructor() {
    internal val scale = Animatable(1f)
    internal val shift = Animatable(0f)

    /** Muda a cada novo acerto: dispara as faíscas. */
    internal var sparkles by mutableIntStateOf(0)
}

/** Lembra o movimento e o dispara quando [kind] muda para acerto ou erro. */
@Composable
fun rememberFeedbackMotion(kind: FeedbackKind, animated: Boolean = true): FeedbackMotion {
    val motion = remember { FeedbackMotion() }
    val on = motionOn(animated)
    val px = with(LocalDensity.current) { 1.dp.toPx() }
    // Só anima quando o resultado muda: ao voltar para a tela com a resposta já dada, nada se repete.
    var seen by remember { mutableStateOf(kind) }
    LaunchedEffect(kind, on) {
        val changed = kind != seen
        seen = kind
        if (!on || !changed) {
            motion.scale.snapTo(1f)
            motion.shift.snapTo(0f)
            return@LaunchedEffect
        }
        when (kind) {
            FeedbackKind.Correct -> {
                motion.sparkles++
                motion.scale.animateTo(1.05f, tween(MotionMs.CORRECT_POP, easing = FastOutSlowInEasing))
                motion.scale.animateTo(1f, spring(dampingRatio = 0.42f, stiffness = Spring.StiffnessMediumLow))
            }
            FeedbackKind.Wrong -> motion.shift.animateTo(
                0f,
                keyframes {
                    durationMillis = MotionMs.WRONG_SHAKE
                    -9f * px at 50
                    8f * px at 110
                    -5f * px at 170
                    3f * px at 240
                    0f at MotionMs.WRONG_SHAKE
                },
            )
            FeedbackKind.None -> {
                motion.scale.snapTo(1f)
                motion.shift.snapTo(0f)
            }
        }
    }
    return motion
}

/** Aplica o movimento na camada de desenho (lido só ao desenhar: não recompõe). */
fun Modifier.feedbackMotion(motion: FeedbackMotion): Modifier = graphicsLayer {
    val s = motion.scale.value
    scaleX = s
    scaleY = s
    translationX = motion.shift.value
}

/**
 * Faíscas discretas sobre um elemento acertado. Vai dentro do Box do elemento, por cima, sem receber toques.
 */
@Composable
fun BoxScope.CorrectSparkles(motion: FeedbackMotion, animated: Boolean = true) {
    if (!motionOn(animated)) return
    val c = Trilha.colors
    Box(Modifier.matchParentSize()) {
        ParticleBurst(
            trigger = motion.sparkles,
            colors = listOf(c.success, c.warning, c.secondary),
            modifier = Modifier.matchParentSize(),
            count = 12,
            power = 56.dp,
            sizeRange = 2.5f..4.5f,
            durationMs = MotionMs.SPARKLE,
            gravity = 0.2f,
            origin = Offset(0.12f, 0.5f),
        )
    }
}
