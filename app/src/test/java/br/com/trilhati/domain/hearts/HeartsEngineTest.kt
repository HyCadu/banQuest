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
        // 45 min = 3 metades: 2 + 1½ = 3½, e o relógio fica no último quarto de hora contado.
        val s = HeartsEngine.regen(HeartsState(2, 0), 45 * min)
        assertEquals(HeartsState(3, 45 * min, half = true), s)
        assertEquals(15 * min, HeartsEngine.msUntilNext(s, 45 * min))
        assertEquals(25 * min, HeartsEngine.msUntilNext(HeartsState(2, 0), 5 * min))
    }

    @Test fun `erro de ensino custa meio coracao e o segundo meio completa um`() {
        val a = HeartsEngine.loseHalf(HeartsState(5, 0), nowMs = 1_000)
        assertEquals(HeartsState(4, 1_000, half = true), a)
        // Na tela: "4,5" com o coração rachado; para o TalkBack, "4 corações e meio".
        assertEquals("4,5", HeartsEngine.display(a))
        assertEquals("5", HeartsEngine.display(HeartsState(5, 0)))
        assertEquals("4 corações e meio", HeartsEngine.description(a))
        val b = HeartsEngine.loseHalf(a, nowMs = 2_000)
        assertEquals(HeartsState(4, 1_000), b)
        // Questão de prova custa um inteiro, mesmo partindo de meio.
        assertEquals(HeartsState(3, 1_000, half = true), HeartsEngine.lose(a, 2_000))
    }

    @Test fun `meio coracao volta em 15 minutos`() {
        val s = HeartsState(4, 0, half = true)
        assertEquals(s, HeartsEngine.regen(s, 14 * min))
        assertEquals(HeartsState(5, 15 * min), HeartsEngine.regen(s, 15 * min))
        assertEquals(HeartsState(1, 0), HeartsEngine.regen(HeartsState(0, 0, half = true), 15 * min).copy(updatedAtMs = 0))
    }

    @Test fun `com meio coracao ainda da para jogar, e perder inteiro zera`() {
        val s = HeartsState(0, 0, half = true)
        assertEquals(1, s.halves)
        assertEquals("0,5", HeartsEngine.display(s))
        assertEquals("meio coração", HeartsEngine.description(s))
        assertEquals("1 coração", HeartsEngine.description(HeartsState(1, 0)))
        assertEquals(0, HeartsEngine.lose(s, 10).halves)
        assertEquals(0, HeartsEngine.loseHalf(s, 10).halves)
    }

    @Test fun `recarregar e ganhar funcionam com meio coracao`() {
        assertEquals(HeartsState(5, 9), HeartsEngine.gain(HeartsState(4, 0, half = true), 1, 9))
        assertEquals(HeartsState(3, 0, half = true), HeartsEngine.gain(HeartsState(2, 0, half = true), 1, 9))
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
