package br.com.trilhati.domain.streak

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class StreakEngineTest {
    private val d = LocalDate.of(2026, 11, 15)

    @Test fun `primeira licao inicia a ofensiva em 1`() {
        val r = StreakEngine.onLessonCompleted(StreakState(), d)
        assertEquals(StreakState(current = 1, longest = 1, lastDay = d), r.state)
    }

    @Test fun `segunda licao no mesmo dia nao soma`() {
        val s = StreakEngine.onLessonCompleted(StreakState(), d).state
        assertEquals(s, StreakEngine.onLessonCompleted(s, d).state)
    }

    @Test fun `dia seguinte soma 1`() {
        val s = StreakEngine.onLessonCompleted(StreakState(), d).state
        val next = StreakEngine.onLessonCompleted(s, d.plusDays(1)).state
        assertEquals(2, next.current)
        assertEquals(2, next.longest)
    }

    @Test fun `perder 1 dia sem congelamento zera e recomeca em 1`() {
        val s = StreakState(current = 5, longest = 5, lastDay = d)
        val after = StreakEngine.onLessonCompleted(s, d.plusDays(2)).state
        assertEquals(1, after.current)
        assertEquals(5, after.longest)
    }

    @Test fun `perder 1 dia com congelamento consome 1 e mantem a ofensiva`() {
        val s = StreakState(current = 5, longest = 5, lastDay = d, freezes = 2)
        val r = StreakEngine.onLessonCompleted(s, d.plusDays(2))
        assertEquals(6, r.state.current)
        assertEquals(1, r.state.freezes)
        assertEquals(listOf(d.plusDays(1)), r.frozenDays)
    }

    @Test fun `perder 2 dias consome 2 congelamentos`() {
        val s = StreakState(current = 3, longest = 3, lastDay = d, freezes = 2)
        val r = StreakEngine.onLessonCompleted(s, d.plusDays(3))
        assertEquals(4, r.state.current)
        assertEquals(0, r.state.freezes)
        assertEquals(2, r.frozenDays.size)
    }

    @Test fun `congelamentos insuficientes nao salvam e nao sao gastos`() {
        val s = StreakState(current = 3, longest = 3, lastDay = d, freezes = 1)
        val r = StreakEngine.reconcile(s, d.plusDays(3))
        assertEquals(0, r.state.current)
        assertEquals(1, r.state.freezes)
    }

    @Test fun `virada de dia a meia-noite - ontem ainda esta em risco e anteontem nao`() {
        val s = StreakState(current = 4, longest = 4, lastDay = d)
        assertEquals(StreakStatus.DoneToday, StreakEngine.status(s, d))
        assertEquals(StreakStatus.AtRisk, StreakEngine.status(s, d.plusDays(1)))
        assertEquals(StreakStatus.None, StreakEngine.status(s, d.plusDays(2)))
        assertEquals(4, StreakEngine.displayCount(s, d.plusDays(1)))
        assertEquals(0, StreakEngine.displayCount(s, d.plusDays(2)))
    }

    @Test fun `congelamento cobre o dia mas o dia seguinte sem estudo consome outro`() {
        var s = StreakState(current = 7, longest = 7, lastDay = d, freezes = 2)
        s = StreakEngine.reconcile(s, d.plusDays(2)).state // dia d+1 congelado
        assertEquals(1, s.freezes)
        s = StreakEngine.reconcile(s, d.plusDays(3)).state // dia d+2 também congelado
        assertEquals(0, s.freezes)
        assertEquals(7, s.current)
        assertEquals(0, StreakEngine.reconcile(s, d.plusDays(5)).state.current)
    }

    @Test fun `estoque de congelamentos limitado a 2`() {
        var s = StreakState()
        repeat(5) { s = StreakEngine.addFreeze(s) }
        assertEquals(2, s.freezes)
    }

    @Test fun `recorde e preservado`() {
        val s = StreakState(current = 9, longest = 30, lastDay = d)
        assertEquals(30, StreakEngine.onLessonCompleted(s, d.plusDays(1)).state.longest)
    }
}
