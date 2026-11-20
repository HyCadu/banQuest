package br.com.trilhati.domain.celebration

import br.com.trilhati.domain.progress.DailyLog
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/** Um dia da faixa semanal da celebração da ofensiva. */
data class WeekDayInfo(
    val date: LocalDate,
    /** "S", "T", "Q"... (segunda a domingo). */
    val label: String,
    /** Concluiu ao menos uma lição (ou prova inteira) no dia: conta para a ofensiva. */
    val studied: Boolean,
    /** Dia salvo por um congelamento. */
    val frozen: Boolean,
    val isToday: Boolean,
    val future: Boolean,
)

object StreakWeek {
    private val LABELS = listOf("S", "T", "Q", "Q", "S", "S", "D")
    private val NAMES = listOf("segunda", "terça", "quarta", "quinta", "sexta", "sábado", "domingo")

    /** A semana de [today], de segunda a domingo, com os dias de estudo reais de [logs]. */
    fun of(today: LocalDate, logs: Map<LocalDate, DailyLog>): List<WeekDayInfo> {
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return (0 until 7).map { i ->
            val day = monday.plusDays(i.toLong())
            val log = logs[day]
            WeekDayInfo(
                date = day,
                label = LABELS[i],
                studied = log != null && log.lessons > 0,
                frozen = log != null && log.froze && log.lessons == 0,
                isToday = day == today,
                future = day.isAfter(today),
            )
        }
    }

    /** Texto para o TalkBack: "Esta semana: estudou segunda e quarta (hoje)". */
    fun description(week: List<WeekDayInfo>): String {
        val studied = week.filter { it.studied }.map { NAMES[it.date.dayOfWeek.value - 1] + if (it.isToday) " (hoje)" else "" }
        return if (studied.isEmpty()) "Esta semana: nenhum dia de estudo ainda" else "Esta semana: estudou ${studied.joinToString(", ")}"
    }
}
