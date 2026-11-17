package br.com.trilhati.domain.srs

import java.time.LocalDate

/** Cartão de revisão de uma questão que o aluno já errou. `index = -1` significa "sem cartão". */
data class SrsCard(val index: Int = NO_CARD, val nextReview: LocalDate? = null) {
    val active: Boolean get() = index >= 0 && nextReview != null

    companion object {
        const val NO_CARD = -1
    }
}

/** Revisão espaçada simples (PLAN.md §7): intervalos de 1, 3, 7 e 14 dias; erro zera. */
object SrsEngine {
    val INTERVALS_DAYS = listOf(1, 3, 7, 14)

    /**
     * Atualiza o cartão depois de uma resposta.
     * - Errou: o cartão (re)começa no primeiro intervalo.
     * - Acertou com cartão vencido: sobe um degrau; depois do último intervalo o item "se forma" e sai do banco de erros.
     * - Acertou antes do dia da revisão: nada muda (0.10.1). Senão, treinar a mesma questão várias vezes no mesmo dia
     *   a "formava" em minutos, e a revisão espaçada perdia o sentido.
     * - Acertou sem cartão: nada acontece (a questão nunca foi errada).
     */
    fun onAnswer(card: SrsCard, correct: Boolean, today: LocalDate): SrsCard {
        if (!correct) return SrsCard(0, today.plusDays(INTERVALS_DAYS[0].toLong()))
        if (!card.active || !isDue(card, today)) return card
        val next = card.index + 1
        return if (next >= INTERVALS_DAYS.size) SrsCard() else SrsCard(next, today.plusDays(INTERVALS_DAYS[next].toLong()))
    }

    /** `true` se um acerto agora faz o item se formar (sair do banco). */
    fun graduates(card: SrsCard): Boolean = card.active && card.index + 1 >= INTERVALS_DAYS.size

    fun isDue(card: SrsCard, today: LocalDate): Boolean = card.nextReview?.let { !it.isAfter(today) } == true && card.active
}
