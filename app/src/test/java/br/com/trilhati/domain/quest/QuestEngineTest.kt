package br.com.trilhati.domain.quest

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestEngineTest {
    private val d = LocalDate.of(2026, 11, 15)

    @Test fun `sorteia 3 missoes distintas por dia`() {
        val q = QuestEngine.questsFor(d)
        assertEquals(3, q.size)
        assertEquals(3, q.map { it.id }.toSet().size)
    }

    @Test fun `o sorteio do dia e deterministico`() {
        assertEquals(QuestEngine.questsFor(d), QuestEngine.questsFor(d))
    }

    @Test fun `dias diferentes variam a combinacao`() {
        val combos = (0 until 30).map { QuestEngine.questsFor(d.plusDays(it.toLong())).map { q -> q.id } }.toSet()
        assertTrue("esperava variedade, veio ${combos.size}", combos.size > 5)
    }

    @Test fun `ids sao unicos e todos achaveis`() {
        assertEquals(QuestEngine.pool.size, QuestEngine.pool.map { it.id }.toSet().size)
        QuestEngine.pool.forEach { assertEquals(it, QuestEngine.byId(it.id)) }
    }

    @Test fun `xp soma e para no alvo`() {
        val def = QuestEngine.byId("xp100")!!
        assertEquals(60, QuestEngine.apply(def, 0, QuestEvent(xp = 60)))
        assertEquals(100, QuestEngine.apply(def, 60, QuestEvent(xp = 80)))
        assertTrue(QuestEngine.isComplete(def, 100))
        assertFalse(QuestEngine.isComplete(def, 99))
    }

    @Test fun `combo guarda o melhor do dia em vez de somar`() {
        val def = QuestEngine.byId("combo6")!!
        assertEquals(4, QuestEngine.apply(def, 0, QuestEvent(bestCombo = 4)))
        assertEquals(4, QuestEngine.apply(def, 4, QuestEvent(bestCombo = 2)))
        assertEquals(6, QuestEngine.apply(def, 4, QuestEvent(bestCombo = 8)))
    }

    @Test fun `licoes e licoes perfeitas contam por evento`() {
        assertEquals(1, QuestEngine.apply(QuestEngine.byId("lessons2")!!, 0, QuestEvent(lessons = 1)))
        assertEquals(1, QuestEngine.apply(QuestEngine.byId("perfect1")!!, 0, QuestEvent(perfectLessons = 1)))
        assertEquals(1, QuestEngine.apply(QuestEngine.byId("practice1")!!, 0, QuestEvent(practiceSessions = 1)))
        assertEquals(1, QuestEngine.apply(QuestEngine.byId("paced1")!!, 0, QuestEvent(pacedLessons = 1)))
        assertEquals(8, QuestEngine.apply(QuestEngine.byId("correct20")!!, 0, QuestEvent(correctAnswers = 8)))
    }
}
