package br.com.trilhati.domain.hearts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HeartsEngineTest {
    private val min = 60_000L
    private val regen = HeartsEngine.REGEN_MS

    @Test fun `perder coracao com tudo cheio inicia o relogio agora`() {
        val s = HeartsEngine.lose(HeartsState(5, 0), nowMs = 1_000)
        assertEquals(HeartsState(4, 1_000), s)
    }

    @Test fun `perder coracao nao reinicia o relogio se ja estava regenerando`() {
        val s = HeartsEngine.lose(HeartsState(3, 5_000), nowMs = 10_000)
        assertEquals(HeartsState(2, 5_000), s)
    }

    @Test fun `nunca fica abaixo de zero`() {
        assertEquals(0, HeartsEngine.lose(HeartsState(0, 0), 10).hearts)
    }

    @Test fun `regenera um coracao a cada 30 minutos`() {
        val start = HeartsState(2, 0)
        assertEquals(2, HeartsEngine.regen(start, 29 * min).hearts)
        assertEquals(3, HeartsEngine.regen(start, 30 * min).hearts)
        assertEquals(4, HeartsEngine.regen(start, 61 * min).hearts)
    }

    @Test fun `regeneracao preserva o resto do tempo`() {
        val s = HeartsEngine.regen(HeartsState(2, 0), 45 * min)
        assertEquals(HeartsState(3, regen), s)
        assertEquals(15 * min, HeartsEngine.msUntilNext(s, 45 * min))
    }

    @Test fun `regeneracao para no maximo de 5`() {
        val s = HeartsEngine.regen(HeartsState(1, 0), 100 * 60 * min)
        assertEquals(5, s.hearts)
        assertNull(HeartsEngine.msUntilNext(s, 100 * 60 * min))
    }

    @Test fun `ganhar coracao respeita o maximo`() {
        assertEquals(5, HeartsEngine.gain(HeartsState(4, 0), 3, 10).hearts)
        assertEquals(3, HeartsEngine.gain(HeartsState(2, 0), 1, 10).hearts)
    }

    @Test fun `recarregar enche tudo`() = assertEquals(HeartsState(5, 777), HeartsEngine.refill(777))
}
