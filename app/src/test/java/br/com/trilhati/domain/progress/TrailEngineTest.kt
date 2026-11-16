package br.com.trilhati.domain.progress

import br.com.trilhati.domain.TestData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrailEngineTest {
    private val units = listOf(TestData.unit(1, 3), TestData.unit(2, 2))
    private fun done(id: String, perfect: Boolean = false) =
        LessonProgress(id, id.substringBefore('l'), timesCompleted = 1, perfectCount = if (perfect) 1 else 0)

    @Test fun `no inicio so a primeira licao esta disponivel`() {
        val s = TrailEngine.statuses(units, emptyMap())
        assertEquals(LessonStatus.Available, s["u1l1"])
        assertEquals(LessonStatus.Locked, s["u1l2"])
        assertEquals(LessonStatus.Locked, s["u2l1"])
    }

    @Test fun `concluir libera a seguinte`() {
        val s = TrailEngine.statuses(units, mapOf("u1l1" to done("u1l1")))
        assertEquals(LessonStatus.Done, s["u1l1"])
        assertEquals(LessonStatus.Available, s["u1l2"])
        assertEquals(LessonStatus.Locked, s["u1l3"])
    }

    @Test fun `licao perfeita ganha coroa`() {
        val s = TrailEngine.statuses(units, mapOf("u1l1" to done("u1l1", perfect = true)))
        assertEquals(LessonStatus.Perfect, s["u1l1"])
    }

    @Test fun `concluir a ultima da unidade libera a primeira da proxima`() {
        val p = listOf("u1l1", "u1l2", "u1l3").associateWith { done(it) }
        val s = TrailEngine.statuses(units, p)
        assertEquals(LessonStatus.Available, s["u2l1"])
        assertTrue(TrailEngine.isUnitComplete(units[0], p))
        assertEquals(1, TrailEngine.completedUnitCount(units, p))
    }

    @Test fun `unidade sem conteudo nunca libera e bloqueia o resto`() {
        val withEmpty = listOf(TestData.unit(1, 1), TestData.unit(2, 2, withContent = false), TestData.unit(3, 1))
        val s = TrailEngine.statuses(withEmpty, mapOf("u1l1" to done("u1l1")))
        assertEquals(LessonStatus.Locked, s["u2l1"])
        assertEquals(LessonStatus.Locked, s["u3l1"])
        assertFalse(TrailEngine.isUnitComplete(withEmpty[1], emptyMap()))
    }

    @Test fun `cada trilha anda sozinha`() {
        val general = TestData.unit(11, 2).copy(track = br.com.trilhati.domain.model.Track.General)
        val all = listOf(TestData.unit(1, 2), TestData.unit(2, 2, withContent = false), general)
        val s = TrailEngine.statuses(all, emptyMap())
        // A primeira lição de cada trilha fica disponível, mesmo com lição vazia travando os Específicos.
        assertEquals(LessonStatus.Available, s["u1l1"])
        assertEquals(LessonStatus.Available, s["u11l1"])
        val p = listOf("u1l1", "u1l2").associateWith { done(it) }
        val s2 = TrailEngine.statuses(all, p)
        assertEquals(LessonStatus.Locked, s2["u2l1"])
        assertEquals(LessonStatus.Available, s2["u11l1"])
        assertEquals(LessonStatus.Locked, s2["u11l2"])
    }
}
