package br.com.trilhati.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import br.com.trilhati.R
import br.com.trilhati.ui.icons.GameIcon
import br.com.trilhati.ui.icons.GameIconView
import br.com.trilhati.ui.theme.Trilha

/** Pílula com ícone + número (ofensiva e corações no topo da Trilha). */
@Composable
fun StatChip(
    icon: GameIcon,
    value: String,
    description: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    dimmed: Boolean = false,
) {
    Surface(
        modifier = modifier.semantics(mergeDescendants = true) { contentDescription = description },
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = RoundedCornerShape(50),
        color = Trilha.colors.surface,
        border = BorderStroke(2.dp, Trilha.colors.outline),
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 12.dp, top = 5.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (dimmed) {
                GameIconView(icon, size = 24.dp, colors = br.com.trilhati.ui.icons.IconColors.flat(Trilha.colors.locked, Trilha.colors.onLocked))
            } else {
                GameIconView(icon, size = 24.dp)
            }
            Spacer(Modifier.width(6.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, color = Trilha.colors.ink)
        }
    }
}

/**
 * Faixa do topo da trilha (0.13.0): o chip da prova ([examChip], com os dias que faltam) e, à direita, a ofensiva e os
 * corações. As gemas saíram daqui: o saldo fica na Loja.
 */
@Composable
fun TopStatusBar(
    streak: Int,
    hearts: Int,
    examChip: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onStreakClick: (() -> Unit)? = null,
    onHeartsClick: (() -> Unit)? = null,
    showHearts: Boolean = true,
    studiedToday: Boolean = true,
    /** Meio coração além de [hearts]: mostra o coração rachado. */
    halfHeart: Boolean = false,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        examChip()
        Spacer(Modifier.weight(1f))
        StatChip(
            GameIcon.Flame, streak.toString(),
            pluralStringResource(R.plurals.stat_streak_description, streak, streak) + if (studiedToday) "" else ", ainda não estudou hoje",
            onClick = onStreakClick, dimmed = !studiedToday,
        )
        if (showHearts) {
            // Meio coração: "4,5" e o ícone rachado.
            val text = if (halfHeart) "$hearts,5" else hearts.toString()
            val description = pluralStringResource(R.plurals.stat_hearts_description, hearts, hearts) + if (halfHeart) " e meio" else ""
            StatChip(if (halfHeart) GameIcon.HeartCracked else GameIcon.Heart, text, description, onClick = onHeartsClick)
        }
    }
}
