package br.com.trilhati.domain.progress

import br.com.trilhati.domain.TestData
import br.com.trilhati.domain.time.APP_ZONE
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class DailyGoalTest {
    private val today = LocalDate.of(2026, 11, 18)
    private val exam = LocalDate.of(2027, 1, 24)
    private fun msOn(day: LocalDate) = day.atTime(10, 0).atZone(APP_ZONE).toInstant().toEpochMilli()
    private fun done(id: String, day: LocalDate, times: Int = 1) = id to LessonProgress(id, id.substringBefore('l'), times, 100, 0, msOn(day))

    @Test fun `divide as licoes que faltam pelos dias de estudo, arredondando para cima`() {
        assertEquals(67, DailyGoal.studyDays(today, exam))
        assertEquals(2, DailyGoal.lessonsFor(remaining = 100, days = 67))
        assertEquals(1, DailyGoal.lessonsFor(remaining = 67, days = 67))
        assertEquals(2, DailyGoal.lessonsFor(remaining = 68, days = 67))
        assertEquals(12, DailyGoal.lessonsFor(remaining = 120, days = 10))
    }

    @Test fun `trilha terminada, dia da prova ou depois dela - meta minima de uma licao`() {
        assertEquals(1, DailyGoal.lessonsFor(remaining = 0, days = 30))
        assertEquals(0, DailyGoal.studyDays(exam, exam))
        assertEquals(0, DailyGoal.studyDays(exam.plusDays(3), exam))
        assertEquals(1, DailyGoal.lessonsFor(remaining = 40, days = 0))
    }

    @Test fun `conta as duas trilhas e as licoes ainda sem conteudo`() {
        val course = listOf(TestData.unit(1, 5), TestData.unit(2, 5, withContent = false))
        // 10 lições em 4 dias: 3 por dia.
        assertEquals(3, DailyGoal.lessons(course, emptyMap(), today, today.plusDays(4), APP_ZONE))
    }

    @Test fun `licao concluida antes de hoje sai da conta`() {
        val course = listOf(TestData.unit(1, 5))
        val progress = mapOf(done("u1l1", today.minusDays(1)), done("u1l2", today.minusDays(1)))
        // Faltam 3 em 2 dias: 2 por dia.
        assertEquals(2, DailyGoal.lessons(course, progress, today, today.plusDays(2), APP_ZONE))
    }

    @Test fun `a meta nao cai ao longo do dia - licao nova de hoje continua contando`() {
        val course = listOf(TestData.unit(1, 5))
        val morning = DailyGoal.lessons(course, emptyMap(), today, today.plusDays(2), APP_ZONE)
        val afternoon = DailyGoal.lessons(course, mapOf(done("u1l1", today), done("u1l2", today)), today, today.plusDays(2), APP_ZONE)
        assertEquals(3, morning)
        assertEquals(morning, afternoon)
        // Amanhã, as duas de hoje já saem da conta: faltam 3 em 1 dia.
        assertEquals(3, DailyGoal.lessons(course, mapOf(done("u1l1", today), done("u1l2", today)), today.plusDays(1), today.plusDays(2), APP_ZONE))
    }

    @Test fun `repetir hoje uma licao de outro dia nao a devolve para a conta`() {
        val course = listOf(TestData.unit(1, 4))
        val progress = mapOf(done("u1l1", today, times = 2))
        // Faltam 3 em 3 dias.
        assertEquals(1, DailyGoal.lessons(course, progress, today, today.plusDays(3), APP_ZONE))
    }

    @Test fun `meta batida com licoes ou provas inteiras de hoje`() {
        assertEquals(false, DailyGoal.met(1, 2))
        assertEquals(true, DailyGoal.met(2, 2))
        assertEquals(true, DailyGoal.met(3, 2))
    }
}
