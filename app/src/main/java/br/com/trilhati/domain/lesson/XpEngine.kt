package br.com.trilhati.domain.lesson

/** Regras de XP (PLAN.md §7). */
object XpEngine {
    const val CORRECT_FIRST_TRY = 10
    const val CORRECT_RETRY = 5
    const val FAST_BONUS = 5
    const val FAST_LIMIT_MS = 120_000L
    const val COMPLETION = 20
    const val PERFECT_LESSON = 10
    const val LEVEL_XP = 500

    /**
     * XP de uma resposta. Erro não dá XP; acerto na repetição vale metade e não tem bônus de velocidade.
     * [corrected] (completou depois de corrigir um par errado) vale o mesmo que a repetição.
     */
    fun answerXp(correct: Boolean, retry: Boolean, elapsedMs: Long, corrected: Boolean = false): Int = when {
        !correct -> if (corrected) CORRECT_RETRY else 0
        retry -> CORRECT_RETRY
        elapsedMs <= FAST_LIMIT_MS -> CORRECT_FIRST_TRY + FAST_BONUS
        else -> CORRECT_FIRST_TRY
    }

    fun completionXp(perfect: Boolean): Int = COMPLETION + if (perfect) PERFECT_LESSON else 0

    /** Nível exibido: começa em 1 e sobe a cada 500 XP. */
    fun level(totalXp: Int): Int = totalXp.coerceAtLeast(0) / LEVEL_XP + 1

    fun xpIntoLevel(totalXp: Int): Int = totalXp.coerceAtLeast(0) % LEVEL_XP

    fun levelProgress(totalXp: Int): Float = xpIntoLevel(totalXp).toFloat() / LEVEL_XP
}
