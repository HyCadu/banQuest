package br.com.trilhati.domain.hearts

/**
 * Corações: [hearts] inteiros disponíveis, mais meio coração se [half] (ex.: 4 + meio = "4½"), e o instante (ms) até
 * onde a regeneração já foi contabilizada. Erro em exercício de ensino custa meio coração; em questão de prova, um.
 */
data class HeartsState(val hearts: Int, val updatedAtMs: Long, val half: Boolean = false) {
    /** Total em metades (0 a 10). */
    val halves: Int get() = hearts * 2 + if (half) 1 else 0
}

/** Regras de corações (PLAN.md §7): máximo 5, regenera meio coração a cada 15 minutos (um inteiro a cada 30). */
object HeartsEngine {
    const val MAX = 5
    const val MAX_HALVES = MAX * 2
    const val REGEN_MS = 30L * 60 * 1000
    const val HALF_REGEN_MS = REGEN_MS / 2

    private fun ofHalves(halves: Int, updatedAtMs: Long) = HeartsState(halves / 2, updatedAtMs, halves % 2 == 1)

    /** Contabiliza as metades recuperadas com o passar do tempo. */
    fun regen(state: HeartsState, nowMs: Long): HeartsState {
        if (state.halves >= MAX_HALVES) return HeartsState(MAX, nowMs)
        val elapsed = (nowMs - state.updatedAtMs).coerceAtLeast(0)
        val gained = (elapsed / HALF_REGEN_MS).toInt()
        val halves = (state.halves + gained).coerceAtMost(MAX_HALVES)
        return if (halves >= MAX_HALVES) HeartsState(MAX, nowMs) else ofHalves(halves, state.updatedAtMs + gained * HALF_REGEN_MS)
    }

    /** Perde [halves] metades (2 = um coração); se estava cheio, o relógio de regeneração começa agora. */
    fun lose(state: HeartsState, nowMs: Long, halves: Int = 2): HeartsState {
        val current = regen(state, nowMs)
        val updatedAt = if (current.halves >= MAX_HALVES) nowMs else current.updatedAtMs
        return ofHalves((current.halves - halves).coerceAtLeast(0), updatedAt)
    }

    /** Erro em exercício de ensino (aprender e praticar). */
    fun loseHalf(state: HeartsState, nowMs: Long): HeartsState = lose(state, nowMs, halves = 1)

    fun gain(state: HeartsState, amount: Int, nowMs: Long): HeartsState {
        val current = regen(state, nowMs)
        val halves = (current.halves + amount * 2).coerceAtMost(MAX_HALVES)
        return if (halves >= MAX_HALVES) HeartsState(MAX, nowMs) else ofHalves(halves, current.updatedAtMs)
    }

    fun refill(nowMs: Long): HeartsState = HeartsState(MAX, nowMs)

    /** Quanto falta para completar o próximo coração inteiro; `null` se já está cheio. */
    fun msUntilNext(state: HeartsState, nowMs: Long): Long? {
        val current = regen(state, nowMs)
        if (current.halves >= MAX_HALVES) return null
        val nextHalf = (current.updatedAtMs + HALF_REGEN_MS - nowMs).coerceIn(0, HALF_REGEN_MS)
        return if (current.half) nextHalf else nextHalf + HALF_REGEN_MS
    }

    /**
     * Texto do contador, ao lado do coração: "5", "4,5" ou "0,5" (vírgula, padrão do português). Com meio coração, o
     * ícone também aparece rachado ([HeartsState.half]); o próximo erro de ensino quebra esse coração.
     */
    fun display(state: HeartsState): String = if (state.half) "${state.hearts},5" else state.hearts.toString()

    /** Texto para o TalkBack: "5 corações", "4 corações e meio", "meio coração". */
    fun description(state: HeartsState): String = when {
        state.half && state.hearts == 0 -> "meio coração"
        state.half -> "${state.hearts} ${if (state.hearts == 1) "coração" else "corações"} e meio"
        else -> "${state.hearts} ${if (state.hearts == 1) "coração" else "corações"}"
    }
}
