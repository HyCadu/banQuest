package br.com.trilhati.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import br.com.trilhati.domain.progress.DailyLog
import br.com.trilhati.ui.icons.GameIcon
import br.com.trilhati.ui.icons.GameIconView
import br.com.trilhati.ui.theme.Trilha
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/**
 * Calendário de dias estudados: [weeks] semanas terminando na semana de [today] (segunda a domingo).
 * Dia estudado = pelo menos uma lição concluída; dia congelado aparece em azul.
 */
@Composable
fun StreakCalendar(
    logs: Map<LocalDate, DailyLog>,
    today: LocalDate,
    modifier: Modifier = Modifier,
    weeks: Int = 5,
) {
    val c = Trilha.colors
    val firstMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks((weeks - 1).toLong())
    val studied = logs.values.count { it.lessons > 0 && !it.day.isBefore(firstMonday) }
    val description = "Calendário dos últimos ${weeks * 7} dias: $studied dias estudados"
    Column(modifier.semantics { contentDescription = description }, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("S", "T", "Q", "Q", "S", "S", "D").forEach {
                Text(it, Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = c.inkSoft, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
        for (w in 0 until weeks) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (d in 0 until 7) {
                    val day = firstMonday.plusWeeks(w.toLong()).plusDays(d.toLong())
                    val log = logs[day]
                    val future = day.isAfter(today)
                    val isToday = day == today
                    val bg = when {
                        log != null && log.lessons > 0 -> c.flame
                        log != null && log.froze -> c.gem
                        else -> c.surfaceAlt
                    }
                    Box(
                        Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(if (future) c.surfaceAlt.copy(alpha = 0.4f) else bg, CircleShape)
                            .then(if (isToday) Modifier.border(2.5.dp, c.primary, CircleShape) else Modifier),
                        contentAlignment = Alignment.Center,
                    ) {
                        when {
                            log != null && log.lessons > 0 -> GameIconView(GameIcon.Flame, size = 16.dp)
                            log != null && log.froze -> GameIconView(GameIcon.Gem, size = 14.dp)
                            else -> Text(day.dayOfMonth.toString(), style = MaterialTheme.typography.labelSmall, color = if (future) c.locked else c.inkSoft)
                        }
                    }
                }
            }
        }
    }
}
