package br.com.trilhati.domain.hearts

/** Corações: [hearts] disponíveis e o instante (ms) até onde a regeneração já foi contabilizada. */
data class HeartsState(val hearts: Int, val updatedAtMs: Long)

/** Regras de corações (PLAN.md §7): máximo 5, regenera 1 a cada 30 minutos. */
object HeartsEngine {
    const val MAX = 5
    const val REGEN_MS = 30L * 60 * 1000

    /** Contabiliza os corações recuperados com o passar do tempo. */
    fun regen(state: HeartsState, nowMs: Long): HeartsState {
        if (state.hearts >= MAX) return HeartsState(MAX, nowMs)
        val elapsed = (nowMs - state.updatedAtMs).coerceAtLeast(0)
        val gained = (elapsed / REGEN_MS).toInt()
        val hearts = (state.hearts + gained).coerceAtMost(MAX)
        return if (hearts >= MAX) HeartsState(MAX, nowMs) else HeartsState(hearts, state.updatedAtMs + gained * REGEN_MS)
    }

    /** Perde um coração; se estava cheio, o relógio de regeneração começa agora. */
    fun lose(state: HeartsState, nowMs: Long): HeartsState {
        val current = regen(state, nowMs)
        val updatedAt = if (current.hearts >= MAX) nowMs else current.updatedAtMs
        return HeartsState((current.hearts - 1).coerceAtLeast(0), updatedAt)
    }

    fun gain(state: HeartsState, amount: Int, nowMs: Long): HeartsState {
        val current = regen(state, nowMs)
        val hearts = (current.hearts + amount).coerceAtMost(MAX)
        return if (hearts >= MAX) HeartsState(MAX, nowMs) else HeartsState(hearts, current.updatedAtMs)
    }

    fun refill(nowMs: Long): HeartsState = HeartsState(MAX, nowMs)

    /** Quanto falta para o próximo coração; `null` se já está cheio. */
    fun msUntilNext(state: HeartsState, nowMs: Long): Long? {
        val current = regen(state, nowMs)
        if (current.hearts >= MAX) return null
        return (current.updatedAtMs + REGEN_MS - nowMs).coerceIn(0, REGEN_MS)
    }
}
