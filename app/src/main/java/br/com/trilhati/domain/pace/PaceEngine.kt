package br.com.trilhati.domain.pace

/** Situação do ritmo de prova numa questão. Nunca reprova a resposta (CLAUDE.md, regra 6). */
enum class PaceZone { OnPace, Warning, Over }

/** Como o cronômetro aparece (PLAN.md §7): desligado, só no resumo final, ou visível durante a questão. */
enum class TimerMode { Off, Discreet, Pace }

/** Ritmo-alvo configurável: 3:00 (média da prova) ou 2:30, com opção de 4:00 para caso prático. */
data class PaceConfig(
    val mode: TimerMode = TimerMode.Pace,
    val perQuestionMs: Long = 180_000L,
    val caseTakesFourMinutes: Boolean = false,
) {
    fun targetMs(isCase: Boolean): Long = if (isCase && caseTakesFourMinutes) CASE_MS else perQuestionMs

    companion object {
        const val STANDARD_MS = 180_000L
        const val TIGHT_MS = 150_000L
        const val CASE_MS = 240_000L
    }
}

object PaceEngine {
    /** Âmbar aos 75% do tempo (2:15 de 3:00); vermelho ao estourar. */
    private const val WARNING_FRACTION = 0.75

    fun zone(elapsedMs: Long, targetMs: Long): PaceZone = when {
        elapsedMs > targetMs -> PaceZone.Over
        elapsedMs >= (targetMs * WARNING_FRACTION).toLong() -> PaceZone.Warning
        else -> PaceZone.OnPace
    }

    /** Fração de tempo que ainda resta (1 = cheio, 0 = acabou). */
    fun remainingFraction(elapsedMs: Long, targetMs: Long): Float =
        if (targetMs <= 0) 0f else (1f - elapsedMs.toFloat() / targetMs).coerceIn(0f, 1f)

    /** Folga (positiva) ou atraso (negativa): soma dos tempos-alvo das questões feitas menos o tempo usado. */
    fun slackMs(targetsSumMs: Long, usedMs: Long): Long = targetsSumMs - usedMs

    fun averageMs(totalMs: Long, count: Int): Long = if (count <= 0) 0 else totalMs / count

    /** Texto `m:ss` para o anel e o resumo. */
    fun format(ms: Long): String {
        val seconds = (kotlin.math.abs(ms) / 1000).toInt()
        return "%d:%02d".format(seconds / 60, seconds % 60)
    }
}
