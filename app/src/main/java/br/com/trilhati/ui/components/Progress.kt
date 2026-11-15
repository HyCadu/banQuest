package br.com.trilhati.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.trilhati.R
import br.com.trilhati.domain.pace.PaceZone
import br.com.trilhati.ui.theme.Trilha

/** Barra de progresso grossa e arredondada, com brilho; anima ao mudar. */
@Composable
fun GameProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = Trilha.colors.secondary,
    height: Dp = 18.dp,
) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), tween(450, easing = FastOutSlowInEasing), label = "progress")
    val track = Trilha.colors.surfaceAlt
    val description = stringResource(R.string.progress_description, (progress.coerceIn(0f, 1f) * 100).toInt())
    Canvas(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(50))
            .semantics { contentDescription = description },
    ) {
        val radius = CornerRadius(size.height / 2f)
        drawRoundRect(track, cornerRadius = radius)
        val w = (size.width * animated).coerceAtLeast(if (animated > 0f) size.height else 0f)
        if (w > 0f) {
            drawRoundRect(color, size = Size(w, size.height), cornerRadius = radius)
            drawRoundRect(
                Color.White.copy(alpha = 0.32f),
                topLeft = Offset(size.height * 0.4f, size.height * 0.2f),
                size = Size((w - size.height * 0.8f).coerceAtLeast(0f), size.height * 0.24f),
                cornerRadius = CornerRadius(size.height * 0.12f),
            )
        }
    }
}

/**
 * Anel do cronômetro de ritmo. [remaining] vai de 1 (tempo cheio) a 0; no estouro fica em 0 e a zona vira [PaceZone.Over].
 * O texto central ([label]) vem pronto, para o anel não conhecer regra de tempo.
 */
@Composable
fun PaceRing(
    remaining: Float,
    label: String,
    zone: PaceZone,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
) {
    val c = Trilha.colors
    val color = when (zone) {
        PaceZone.OnPace -> c.secondary
        PaceZone.Warning -> c.warning
        PaceZone.Over -> c.error
    }
    val textColor = when (zone) {
        PaceZone.OnPace -> c.secondaryText
        PaceZone.Warning -> c.warningText
        PaceZone.Over -> c.errorText
    }
    val animated by animateFloatAsState(remaining.coerceIn(0f, 1f), tween(300), label = "pace")
    val pulse = rememberInfiniteTransition(label = "overPulse")
    val overAlpha by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "overAlpha",
    )
    val track = c.surfaceAlt
    val description = stringResource(R.string.pace_ring_description, label)
    Box(
        modifier = modifier.size(size).semantics(mergeDescendants = true) { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(size)) {
            val stroke = size.toPx() * 0.13f
            val inset = stroke / 2f
            val arc = Size(this.size.width - stroke, this.size.height - stroke)
            drawArc(track, 0f, 360f, false, Offset(inset, inset), arc, style = Stroke(stroke))
            val sweep = if (zone == PaceZone.Over) 360f else 360f * animated
            drawArc(
                color = color.copy(alpha = if (zone == PaceZone.Over) overAlpha else 1f),
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arc,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
        // O número acompanha o tamanho do anel (não a fonte do sistema), para nunca vazar da borda.
        val labelSize = with(androidx.compose.ui.platform.LocalDensity.current) { (size * 0.24f).toSp() }
        Text(label, style = MaterialTheme.typography.titleSmall.copy(fontSize = labelSize, lineHeight = labelSize), color = textColor, textAlign = TextAlign.Center)
    }
}

/** Pílula "Folga 0:42" / "Atraso 0:15": quanto o aluno está à frente ou atrás do ritmo de prova. */
@Composable
fun PaceChip(deltaSeconds: Int, modifier: Modifier = Modifier) {
    val c = Trilha.colors
    val ahead = deltaSeconds >= 0
    val abs = kotlin.math.abs(deltaSeconds)
    val clock = "%d:%02d".format(abs / 60, abs % 60)
    val text = stringResource(if (ahead) R.string.pace_ahead else R.string.pace_behind, clock)
    androidx.compose.material3.Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = if (ahead) c.secondarySoft else c.warningSoft,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, if (ahead) c.secondary else c.warning),
    ) {
        Text(
            text,
            Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (ahead) c.secondaryText else c.warningText,
        )
    }
}
