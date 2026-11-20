package br.com.trilhati.ui.animation.effects

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import kotlin.math.roundToInt

/**
 * Número que sobe de [from] até [value] quando [play] liga. Mostra sempre o valor real no fim, e o TalkBack lê o valor
 * final ([description]) desde o começo, sem ouvir a contagem.
 *
 * @param startAtEnd começa já no valor final (etapa já vista: rotação de tela ou volta para a tela).
 * @param motion animação ligada; desligada, o valor aparece direto.
 */
@Composable
fun AnimatedCounter(
    value: Int,
    play: Boolean,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    from: Int = 0,
    durationMs: Int = 850,
    motion: Boolean = true,
    startAtEnd: Boolean = false,
    format: (Int) -> String = { it.toString() },
    description: String = format(value),
) {
    val anim = remember { Animatable(if (startAtEnd || !motion) value.toFloat() else from.toFloat()) }
    LaunchedEffect(play, value, motion) {
        when {
            !motion -> anim.snapTo(value.toFloat())
            play -> anim.animateTo(value.toFloat(), tween(durationMs, easing = FastOutSlowInEasing))
        }
    }
    Text(
        format(anim.value.roundToInt()),
        modifier.semantics { contentDescription = description },
        style = style,
        color = color,
    )
}
