package br.com.trilhati.ui.mascot

import br.com.trilhati.domain.util.SplitMix
import kotlin.math.floor

/** Gestos que o personagem faz sozinho quando está parado. */
enum class Fidget(val durationSec: Float) {
    /** Olha para um lado, depois para o outro, e volta. */
    LookAround(1.8f),

    /** Pulinho rápido. */
    Hop(0.8f),

    /** O jeito próprio de cada personagem (estalar as garras, recolher a cabeça, mostrar a língua, bocejar). */
    Signature(2.2f),
}

/** Gesto em andamento: qual é e o progresso dele, de 0 a 1. */
data class FidgetMoment(val fidget: Fidget, val t: Float)

/**
 * Agenda determinística dos gestos de quem está parado: um gesto a cada 4 a 8 segundos, em ordem pseudoaleatória.
 * O tempo é dividido em janelas de [SLOT_SEC]; cada janela sorteia (pela semente) um gesto e o instante em que ele
 * começa. Funciona em O(1) para qualquer tempo e é igual sempre que a semente e o tempo são os mesmos (testável e
 * sem guardar estado). Personagens diferentes usam sementes diferentes, para não gesticularem juntos.
 */
object FidgetSchedule {
    const val SLOT_SEC = 6f

    /** Na primeira janela na tela, o personagem fica quieto (nada de gesto pela metade logo ao aparecer). */
    const val FIRST_DELAY_SEC = SLOT_SEC

    /** Folga mínima entre o fim de um gesto e o fim da janela. */
    private const val MARGIN_SEC = 0.6f

    fun at(seconds: Float, seed: Int): FidgetMoment? {
        if (seconds < FIRST_DELAY_SEC) return null
        val slot = floor(seconds / SLOT_SEC).toLong()
        val random = SplitMix(slot * 1_000_003L + seed * 7_919L)
        val roll = random.nextDouble()
        val fidget = when {
            roll < 0.5 -> Fidget.Signature
            roll < 0.8 -> Fidget.LookAround
            else -> Fidget.Hop
        }
        // Começo sorteado só na primeira metade da folga: assim o intervalo entre gestos fica entre ~4 e ~8 s.
        val room = (SLOT_SEC - fidget.durationSec - 2 * MARGIN_SEC) / 2
        val start = slot * SLOT_SEC + MARGIN_SEC + random.nextDouble().toFloat() * room
        if (seconds < start || seconds >= start + fidget.durationSec) return null
        return FidgetMoment(fidget, (seconds - start) / fidget.durationSec)
    }
}
