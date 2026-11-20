package br.com.trilhati.domain.celebration

import br.com.trilhati.domain.progress.DailyLog
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StreakWeekTest {
    private val wednesday = LocalDate.of(2026, 11, 18)

    @Test fun `semana de segunda a domingo com os dias reais`() {
        val logs = mapOf(
            wednesday.minusDays(2) to DailyLog(wednesday.minusDays(2), lessons = 1), // segunda
            wednesday.minusDays(1) to DailyLog(wednesday.minusDays(1), froze = true), // terça, congelada
            wednesday to DailyLog(wednesday, lessons = 2),
        )
        val week = StreakWeek.of(wednesday, logs)
        assertEquals(7, week.size)
        assertEquals(LocalDate.of(2026, 11, 16), week.first().date)
        assertEquals(listOf("S", "T", "Q", "Q", "S", "S", "D"), week.map { it.label })
        assertTrue(week[0].studied)
        assertTrue(week[1].frozen)
        assertFalse(week[1].studied)
        assertTrue(week[2].studied && week[2].isToday)
        assertTrue(week[3].future)
        assertFalse(week[3].studied)
        assertEquals("Esta semana: estudou segunda, quarta (hoje)", StreakWeek.description(week))
    }

    @Test fun `domingo fecha a semana que comecou na segunda`() {
        val sunday = LocalDate.of(2026, 11, 22)
        val week = StreakWeek.of(sunday, emptyMap())
        assertEquals(LocalDate.of(2026, 11, 16), week.first().date)
        assertTrue(week.last().isToday)
        assertTrue(week.none { it.future })
        assertEquals("Esta semana: nenhum dia de estudo ainda", StreakWeek.description(week))
    }
}
