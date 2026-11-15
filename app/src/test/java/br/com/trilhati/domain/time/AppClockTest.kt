package br.com.trilhati.domain.time

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class AppClockTest {

    private fun fixedAt(utc: String): Clock = Clock.fixed(Instant.parse(utc), ZoneOffset.UTC)

    @Test
    fun `zona do app e America Sao_Paulo`() {
        assertEquals("America/Sao_Paulo", APP_ZONE.id)
    }

    @Test
    fun `23h59 em Brasilia ainda e o mesmo dia`() {
        // 23:59 BRT = 02:59 UTC do dia seguinte
        val clock = fixedAt("2026-11-16T02:59:00Z")
        assertEquals(LocalDate.of(2026, 11, 15), clock.today())
    }

    @Test
    fun `meia-noite em Brasilia vira o dia`() {
        // 00:00 BRT = 03:00 UTC
        val clock = fixedAt("2026-11-16T03:00:00Z")
        assertEquals(LocalDate.of(2026, 11, 16), clock.today())
    }

    @Test
    fun `fuso do relogio injetado e ignorado, vale o fuso do app`() {
        val tokyo = Clock.fixed(Instant.parse("2026-11-15T20:00:00Z"), ZoneOffset.ofHours(9))
        // Em Tóquio já seria dia 16, em Brasília ainda é dia 15.
        assertEquals(LocalDate.of(2026, 11, 15), tokyo.today())
    }

    @Test
    fun `nowInstant devolve o instante do relogio`() {
        val clock = fixedAt("2026-11-15T12:00:00Z")
        assertEquals(Instant.parse("2026-11-15T12:00:00Z"), clock.nowInstant())
    }
}
