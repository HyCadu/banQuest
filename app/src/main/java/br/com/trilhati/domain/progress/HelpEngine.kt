package br.com.trilhati.domain.progress

import java.time.LocalDate

/** Estoque do botão "Não sei": [count] usos e o dia (epochDay) em que o presente diário foi dado pela última vez. */
data class HelpState(val count: Int = HelpEngine.START, val grantedDay: Long = 0)

/**
 * Regras do "Não sei" (DECISIONS.md, 0.7.1): quem começa ganha [START]; a cada dia novo ganha mais um, até [DAILY_CAP];
 * na loja dá para comprar acima disso, até [MAX]. Só vale nos exercícios de ensino (aprender e praticar).
 */
object HelpEngine {
    const val START = 3
    const val DAILY_CAP = 3
    const val MAX = 9

    /** Presente do dia: +1 se ainda não ganhou hoje e está abaixo de [DAILY_CAP]. Sempre marca o dia. */
    fun daily(state: HelpState, today: LocalDate): HelpState {
        val day = today.toEpochDay()
        if (state.grantedDay >= day) return state
        return HelpState(if (state.count < DAILY_CAP) state.count + 1 else state.count, day)
    }

    /** Gasta um uso; `null` se não tem. */
    fun use(state: HelpState): HelpState? = if (state.count <= 0) null else state.copy(count = state.count - 1)

    fun buy(state: HelpState): HelpState = state.copy(count = (state.count + 1).coerceAtMost(MAX))
}
