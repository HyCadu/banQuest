package br.com.trilhati.ui.animation.celebration

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/** Mola das entradas da celebração: passa um pouco do ponto e assenta (antecipação e recompensa, sem exagero). */
internal fun enterSpring(): AnimationSpec<Float> = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow)

/** Mola mais solta, para o número da ofensiva e o XP "saltarem". */
internal fun popSpring(): AnimationSpec<Float> = spring(dampingRatio = 0.42f, stiffness = Spring.StiffnessLow)

/**
 * Entrada de um bloco guiada por [p] (0 = escondido, 1 = no lugar): sobe um pouco, cresce e aparece. Lido só na fase
 * de desenho; o bloco ocupa o espaço desde o começo, então nada pula no layout.
 */
internal fun Modifier.reveal(p: Animatable<Float, AnimationVector1D>, rise: Float = 22f, fromScale: Float = 0.86f): Modifier = graphicsLayer {
    val v = p.value
    alpha = v.coerceIn(0f, 1f)
    translationY = (1f - v) * rise.dp.toPx()
    val s = fromScale + (1f - fromScale) * v
    scaleX = s
    scaleY = s
}
