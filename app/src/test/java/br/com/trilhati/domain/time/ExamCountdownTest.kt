package br.com.trilhati.domain.time

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class ExamCountdownTest {
    private fun clockAt(utc: String): Clock = Clock.fixed(Instant.parse(utc), ZoneOffset.UTC)

    @Test
    fun `faltam 98 dias em 15 de novembro de 2026`() {
        assertEquals(98, clockAt("2026-11-15T15:00:00Z").daysToExam())
    }

    @Test
    fun `no dia da prova faltam zero dias`() {
        assertEquals(0, clockAt("2027-02-21T12:00:00Z").daysToExam())
    }

    @Test
    fun `depois da prova nunca fica negativo`() {
        assertEquals(0, clockAt("2027-03-10T12:00:00Z").daysToExam())
        assertEquals(-17L, clockAt("2027-03-10T12:00:00Z").daysUntil(EXAM_DATE))
    }

    @Test
    fun `virada do dia usa o fuso de Brasilia`() {
        // 22:00 de 14/11 em Brasília já é 01:00 UTC de 15/11: ainda faltam 99 dias.
        assertEquals(99, clockAt("2026-11-15T01:00:00Z").daysToExam())
    }
}
