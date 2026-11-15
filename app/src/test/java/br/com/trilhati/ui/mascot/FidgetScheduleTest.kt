package br.com.trilhati.ui.mascot

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FidgetScheduleTest {
    /** Início de cada gesto encontrado varrendo o tempo em passos de 10 ms. */
    private fun starts(seed: Int, untilSec: Float): List<Pair<Float, Fidget>> {
        val found = mutableListOf<Pair<Float, Fidget>>()
        var previous: FidgetMoment? = null
        var s = 0f
        while (s < untilSec) {
            val now = FidgetSchedule.at(s, seed)
            if (now != null && (previous == null || now.t < previous.t)) found += s to now.fidget
            previous = now
            s += 0.01f
        }
        return found
    }

    @Test fun `quieto nos primeiros segundos`() {
        for (seed in 0..3) {
            var s = 0f
            while (s < FidgetSchedule.FIRST_DELAY_SEC) {
                assertNull(FidgetSchedule.at(s, seed))
                s += 0.05f
            }
        }
    }

    @Test fun `um gesto a cada 4 a 8 segundos`() {
        for (seed in 0..3) {
            val list = starts(seed, 300f)
            assertTrue("poucos gestos: ${list.size}", list.size >= 45)
            list.zipWithNext().forEach { (a, b) ->
                val gap = b.first - a.first
                assertTrue("intervalo de $gap s (semente $seed)", gap in 3.95f..8.05f)
            }
        }
    }

    @Test fun `progresso vai de 0 a 1 e todos os gestos aparecem`() {
        val kinds = mutableSetOf<Fidget>()
        var s = 0f
        while (s < 400f) {
            FidgetSchedule.at(s, 2)?.let { assertTrue(it.t in 0f..1f); kinds += it.fidget }
            s += 0.05f
        }
        assertEquals(Fidget.entries.toSet(), kinds)
    }

    @Test fun `deterministico e diferente entre personagens`() {
        assertEquals(FidgetSchedule.at(123.4f, 1), FidgetSchedule.at(123.4f, 1))
        assertNotEquals(starts(0, 120f), starts(1, 120f))
    }
}
