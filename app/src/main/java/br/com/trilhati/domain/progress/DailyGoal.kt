package br.com.trilhati.domain.progress

import br.com.trilhati.domain.model.CourseUnit
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Meta do dia em lições (0.14.1, pedido do dono: a meta de XP fixa não dizia nada). É o ritmo para terminar a trilha
 * da prova escolhida até a véspera dela: as lições que faltavam no começo do dia divididas pelos dias de estudo que
 * restam, arredondado para cima. Conta as duas trilhas (Específicos e Gerais) e também as lições ainda em preparação,
 * porque elas fazem parte do que falta estudar.
 *
 * A meta não muda ao longo do dia: as lições concluídas pela primeira vez hoje voltam para a conta de "faltavam".
 * Fazer uma prova anterior inteira ou repetir uma lição conta como uma lição feita, como na ofensiva.
 */
object DailyGoal {
    /** Meta mínima: mesmo com a trilha terminada (ou no dia da prova), uma lição ou revisão por dia. */
    const val MIN_LESSONS = 1

    fun lessons(
        course: List<CourseUnit>,
        progress: Map<String, LessonProgress>,
        today: LocalDate,
        examDate: LocalDate,
        zone: ZoneId,
    ): Int {
        val remaining = course.flatMap { it.lessons }.map { it.id }.distinct().count { id ->
            val p = progress[id]
            p == null || !p.completed || firstCompletedOn(p, today, zone)
        }
        return lessonsFor(remaining, studyDays(today, examDate))
    }

    /** [remaining] lições em [days] dias, arredondado para cima; nunca menos que [MIN_LESSONS]. */
    fun lessonsFor(remaining: Int, days: Int): Int {
        if (remaining <= 0 || days <= 0) return MIN_LESSONS
        return maxOf(MIN_LESSONS, (remaining + days - 1) / days)
    }

    /** Dias de estudo de hoje até a véspera da prova (hoje conta). No dia da prova, ou depois, zero. */
    fun studyDays(today: LocalDate, examDate: LocalDate): Int =
        java.time.temporal.ChronoUnit.DAYS.between(today, examDate).coerceAtLeast(0).toInt()

    /** Concluída hoje pela primeira vez: só uma conclusão, e ela foi hoje. */
    private fun firstCompletedOn(p: LessonProgress, today: LocalDate, zone: ZoneId): Boolean =
        p.timesCompleted == 1 && Instant.ofEpochMilli(p.completedAtMs).atZone(zone).toLocalDate() == today

    /** A meta foi batida com [lessonsToday] lições (ou provas anteriores inteiras) concluídas hoje? */
    fun met(lessonsToday: Int, goal: Int): Boolean = lessonsToday >= goal
}
