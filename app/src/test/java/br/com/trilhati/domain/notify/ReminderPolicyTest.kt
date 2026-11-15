package br.com.trilhati.domain.notify

import br.com.trilhati.domain.streak.StreakStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderPolicyTest {
    private val day = LocalDate.of(2026, 11, 18)
    private fun at(h: Int, m: Int = 0, d: LocalDate = day) = d.atTime(h, m)
    private val seven = LocalTime.of(19, 0)

    @Test fun `de manha o proximo e o lembrete das 19h`() {
        assertEquals(ReminderTrigger(at(19), ReminderKind.Daily), ReminderPolicy.next(at(9), seven))
    }

    @Test fun `depois das 19h o proximo e o aviso de risco das 21h30`() {
        assertEquals(ReminderTrigger(at(21, 30), ReminderKind.StreakRisk), ReminderPolicy.next(at(19, 0), seven))
    }

    @Test fun `depois das 21h30 o proximo e o lembrete de amanha`() {
        assertEquals(ReminderTrigger(at(19, d = day.plusDays(1)), ReminderKind.Daily), ReminderPolicy.next(at(22), seven))
    }

    @Test fun `horario escolhido depois das 21h30 tambem funciona`() {
        assertEquals(ReminderTrigger(at(22), ReminderKind.Daily), ReminderPolicy.next(at(21, 45), LocalTime.of(22, 0)))
    }

    @Test fun `horario igual a 21h30 agenda so o aviso de risco`() {
        assertEquals(ReminderKind.StreakRisk, ReminderPolicy.next(at(10), LocalTime.of(21, 30)).kind)
    }

    private fun decide(
        kind: ReminderKind = ReminderKind.Daily, now: LocalDateTime = at(19, 1), scheduled: LocalDateTime = at(19),
        enabled: Boolean = true, lessons: Int = 0, status: StreakStatus = StreakStatus.AtRisk, streak: Int = 4,
    ) = ReminderPolicy.decide(kind, now, scheduled, enabled, lessons, status, streak, "Guaiamum")

    @Test fun `avisa se nao estudou hoje`() = assertNotNull(decide())
    @Test fun `nao avisa se ja fez licao`() = assertNull(decide(lessons = 1))
    @Test fun `nao avisa se lembretes desligados`() = assertNull(decide(enabled = false))
    @Test fun `descarta aviso muito atrasado`() = assertNull(decide(now = at(21, 0)))
    @Test fun `descarta aviso de outro dia`() = assertNull(decide(now = at(0, 30, day.plusDays(1)), scheduled = at(23, 50)))

    @Test fun `aviso de risco so com ofensiva ativa e em risco`() {
        assertNotNull(decide(ReminderKind.StreakRisk, at(21, 31), at(21, 30)))
        assertNull(decide(ReminderKind.StreakRisk, at(21, 31), at(21, 30), streak = 0, status = StreakStatus.None))
        assertNull(decide(ReminderKind.StreakRisk, at(21, 31), at(21, 30), status = StreakStatus.DoneToday))
    }

    @Test fun `texto usa a voz do personagem e a ofensiva`() {
        val risk = ReminderTexts.risk("Preguica", 0, 12)
        assertTrue(risk.text.contains("Preguiça"))
        assertTrue(risk.text.contains("12 dias"))
        assertTrue(ReminderTexts.risk("Guaiamum", 1, 1).text.contains("1 dia"))
        assertEquals("o Tamanduá", ReminderTexts.withArticle("Tamandua"))
    }

    @Test fun `texto varia ao longo dos dias`() {
        val titles = (0..10).map { ReminderTexts.daily("Guaiamum", it, 3).title }.toSet()
        assertTrue(titles.size >= 3)
    }

    @Test fun `aviso das 21h30 cita a ofensiva em dupla junto da individual`() {
        val msg = ReminderPolicy.decide(ReminderKind.StreakRisk, at(21, 31), at(21, 30), true, 0, StreakStatus.AtRisk, 4, "Guaiamum", listOf("Bia" to 6))
        assertTrue(msg!!.text.contains("Bia (6 dias)"))
    }

    @Test fun `so a ofensiva em dupla em risco tambem avisa`() {
        val msg = ReminderPolicy.decide(ReminderKind.StreakRisk, at(21, 31), at(21, 30), true, 0, StreakStatus.None, 0, "Guaiamum", listOf("Bia" to 1, "Caio" to 3))
        assertEquals("Ofensiva em dupla em risco!", msg!!.title)
        assertTrue(msg.text.contains("Caio") && msg.text.contains("3 dias"))
    }

    @Test fun `ofensiva em dupla nao avisa se voce ja estudou`() {
        assertNull(ReminderPolicy.decide(ReminderKind.StreakRisk, at(21, 31), at(21, 30), true, 1, StreakStatus.DoneToday, 4, "Guaiamum", listOf("Bia" to 6)))
    }
}
