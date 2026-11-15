package br.com.trilhati.domain.srs

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SrsEngineTest {
    private val d = LocalDate.of(2026, 11, 15)

    @Test fun `erro cria cartao com revisao em 1 dia`() {
        val c = SrsEngine.onAnswer(SrsCard(), correct = false, today = d)
        assertEquals(SrsCard(0, d.plusDays(1)), c)
    }

    @Test fun `acerto sem cartao nao cria cartao`() {
        assertFalse(SrsEngine.onAnswer(SrsCard(), true, d).active)
    }

    @Test fun `acertos sucessivos seguem 1 3 7 14 e depois o item se forma`() {
        var c = SrsEngine.onAnswer(SrsCard(), false, d)
        c = SrsEngine.onAnswer(c, true, d.plusDays(1)); assertEquals(SrsCard(1, d.plusDays(4)), c)
        c = SrsEngine.onAnswer(c, true, d.plusDays(4)); assertEquals(SrsCard(2, d.plusDays(11)), c)
        c = SrsEngine.onAnswer(c, true, d.plusDays(11)); assertEquals(SrsCard(3, d.plusDays(25)), c)
        assertTrue(SrsEngine.graduates(c))
        c = SrsEngine.onAnswer(c, true, d.plusDays(25)); assertFalse(c.active)
    }

    @Test fun `erro em qualquer degrau zera para 1 dia`() {
        val c = SrsEngine.onAnswer(SrsCard(2, d), false, d)
        assertEquals(SrsCard(0, d.plusDays(1)), c)
    }

    @Test fun `vencido quando a data de revisao chegou`() {
        val c = SrsCard(1, d)
        assertTrue(SrsEngine.isDue(c, d))
        assertTrue(SrsEngine.isDue(c, d.plusDays(5)))
        assertFalse(SrsEngine.isDue(c, d.minusDays(1)))
        assertFalse(SrsEngine.isDue(SrsCard(), d))
    }
}
