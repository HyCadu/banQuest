package br.com.trilhati.domain.streak

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class StreakState(
    val current: Int = 0,
    val longest: Int = 0,
    val lastDay: LocalDate? = null,
    val freezes: Int = 0,
)

/** Resultado de acertar as contas dos dias sem estudo. */
data class StreakReconcile(val state: StreakState, val frozenDays: List<LocalDate>)

enum class StreakStatus { None, DoneToday, AtRisk }

/**
 * Regras da ofensiva (PLAN.md §7): um dia conta ao concluir ao menos uma lição; a virada é à meia-noite local;
 * cada dia perdido consome um congelamento automaticamente (máx. 2 estocados); sem congelamento suficiente, zera.
 */
object StreakEngine {
    const val MAX_FREEZES = 2

    fun reconcile(state: StreakState, today: LocalDate): StreakReconcile {
        val last = state.lastDay
        if (last == null || state.current == 0) return StreakReconcile(state, emptyList())
        val gap = ChronoUnit.DAYS.between(last, today)
        if (gap <= 1) return StreakReconcile(state, emptyList())
        val missed = (gap - 1).toInt()
        if (state.freezes < missed) return StreakReconcile(state.copy(current = 0), emptyList())
        val frozen = (1..missed).map { last.plusDays(it.toLong()) }
        return StreakReconcile(state.copy(freezes = state.freezes - missed, lastDay = today.minusDays(1)), frozen)
    }

    /** Registra uma lição concluída hoje. */
    fun onLessonCompleted(state: StreakState, today: LocalDate): StreakReconcile {
        val reconciled = reconcile(state, today)
        val s = reconciled.state
        if (s.lastDay == today) return reconciled
        val next = s.current + 1
        return StreakReconcile(s.copy(current = next, longest = maxOf(s.longest, next), lastDay = today), reconciled.frozenDays)
    }

    /** Valor a mostrar no topo, já considerando dias perdidos que ainda não foram contabilizados. */
    fun displayCount(state: StreakState, today: LocalDate): Int = reconcile(state, today).state.current

    fun status(state: StreakState, today: LocalDate): StreakStatus {
        val s = reconcile(state, today).state
        return when {
            s.current == 0 -> StreakStatus.None
            s.lastDay == today -> StreakStatus.DoneToday
            else -> StreakStatus.AtRisk
        }
    }

    fun addFreeze(state: StreakState): StreakState = state.copy(freezes = (state.freezes + 1).coerceAtMost(MAX_FREEZES))
}
