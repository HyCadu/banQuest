package br.com.trilhati.domain.util

import kotlin.random.Random

/**
 * Gerador pseudoaleatório determinístico (SplitMix64). Usado onde o resultado precisa ser sempre o mesmo para a mesma
 * semente (missões do dia, bots da liga), independente da versão do Kotlin.
 */
class SplitMix(seed: Long) {
    private var state = seed

    fun nextLong(): Long {
        state += -0x61c8864680b583ebL
        var z = state
        z = (z xor (z ushr 30)) * -0x40a7b892e31b1a47L
        z = (z xor (z ushr 27)) * -0x6b2fb644ecceee15L
        return z xor (z ushr 31)
    }

    /** Número em [0, 1). */
    fun nextDouble(): Double = (nextLong() ushr 11) * (1.0 / (1L shl 53))

    /** Inteiro em [0, bound). */
    fun nextInt(bound: Int): Int = (nextDouble() * bound).toInt().coerceAtMost(bound - 1)

    fun asRandom(): Random = object : Random() {
        override fun nextBits(bitCount: Int): Int = (this@SplitMix.nextLong() ushr (64 - bitCount)).toInt()
    }
}
