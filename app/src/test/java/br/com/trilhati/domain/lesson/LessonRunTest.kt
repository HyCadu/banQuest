package br.com.trilhati.domain.lesson

import br.com.trilhati.domain.TestData
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonRunTest {
    private val pool = TestData.questions(10)

    @Test fun `sorteia 8 das 10 sem repetir`() {
        val picked = LessonRun.pick(pool, emptySet(), Random(1))
        assertEquals(8, picked.size)
        assertEquals(8, picked.map { it.id }.toSet().size)
    }

    @Test fun `prioriza as questoes que o aluno errou`() {
        val wrong = setOf("u1l1q03", "u1l1q07", "u1l1q09")
        repeat(20) { seed ->
            val ids = LessonRun.pick(pool, wrong, Random(seed)).map { it.id }.toSet()
            assertTrue("seed $seed deveria conter os erros", ids.containsAll(wrong))
        }
    }

    @Test fun `mesma semente gera a mesma lição`() {
        assertEquals(LessonRun.pick(pool, emptySet(), Random(42)), LessonRun.pick(pool, emptySet(), Random(42)))
    }

    @Test fun `pool menor que 8 devolve o que tem`() {
        assertEquals(3, LessonRun.pick(TestData.questions(3), emptySet(), Random(0)).size)
    }

    @Test fun `licao perfeita acaba apos 8 respostas certas e soma xp`() {
        var s = LessonRun.start(pool.take(8))
        repeat(8) { s = LessonRun.answer(s, true, 60_000, 180_000) }
        assertTrue(s.finished)
        assertTrue(s.perfect)
        assertEquals(8 * 15, s.xp)
        assertEquals(8, s.bestCombo)
        assertEquals(1f, s.progress, 0f)
        assertEquals(100, s.accuracyPercent)
    }

    @Test fun `erro volta uma vez no fim da fila valendo 5`() {
        var s = LessonRun.start(pool.take(3))
        val first = s.current!!.question
        s = LessonRun.answer(s, false, 10_000, 180_000)
        assertEquals(3, s.queue.size) // 2 restantes + a repetição
        assertTrue(s.queue.last().retry)
        assertEquals(first, s.queue.last().question)
        assertEquals(0, s.combo)
        s = LessonRun.answer(s, true, 10_000, 180_000)
        s = LessonRun.answer(s, true, 10_000, 180_000)
        s = LessonRun.answer(s, true, 10_000, 180_000) // acerta a repetição
        assertTrue(s.finished)
        assertFalse(s.perfect)
        assertEquals(1, s.mistakes)
        assertEquals(15 + 15 + 5, s.xp)
    }

    @Test fun `errar de novo na repeticao nao cria outra repeticao`() {
        var s = LessonRun.start(pool.take(1))
        s = LessonRun.answer(s, false, 1, 1)
        s = LessonRun.answer(s, false, 1, 1)
        assertTrue(s.finished)
        assertEquals(2, s.records.size)
    }

    @Test fun `combo e zerado no erro e o melhor combo e guardado`() {
        var s = LessonRun.start(pool.take(6))
        repeat(3) { s = LessonRun.answer(s, true, 1, 1) }
        s = LessonRun.answer(s, false, 1, 1)
        s = LessonRun.answer(s, true, 1, 1)
        assertEquals(1, s.combo)
        assertEquals(3, s.bestCombo)
    }

    @Test fun `progresso considera as repeticoes acrescentadas`() {
        var s = LessonRun.start(pool.take(4))
        s = LessonRun.answer(s, false, 1, 1)
        assertEquals(1f / 5f, s.progress, 0.0001f)
    }

    @Test fun `tempo total e soma dos alvos entram no resumo`() {
        var s = LessonRun.start(pool.take(2))
        s = LessonRun.answer(s, true, 100_000, 180_000)
        s = LessonRun.answer(s, true, 50_000, 240_000)
        assertEquals(150_000, s.totalTimeMs)
        assertEquals(420_000, s.targetsSumMs)
    }

    @Test fun `associar pares corrigido vale 5, zera o combo e nao repete no fim`() {
        var s = LessonRun.start(pool.take(3))
        s = LessonRun.answer(s, true, 10_000, 180_000)
        val matched = s.current!!.question
        s = LessonRun.answer(s, false, 10_000, 180_000, corrected = true)
        assertEquals(1, s.queue.size) // só a terceira questão: nada voltou para o fim
        assertEquals(0, s.combo)
        val record = s.records.last()
        assertEquals(matched, record.question)
        assertTrue(record.corrected); assertFalse(record.correct)
        assertEquals(5, record.xp)
        s = LessonRun.answer(s, true, 10_000, 180_000)
        assertTrue(s.finished)
        assertFalse("corrigir não é lição perfeita", s.perfect)
        assertEquals(1, s.mistakes)
        assertEquals(15 + 5 + 15, s.xp)
    }

    @Test fun `acerto limpo nunca vira corrigido`() {
        var s = LessonRun.start(pool.take(1))
        s = LessonRun.answer(s, true, 10_000, 180_000, corrected = true)
        assertTrue(s.records.last().correct); assertFalse(s.records.last().corrected)
        assertEquals(15, s.xp)
    }

    @Test fun `prova anterior nao repete erros no fim e acaba depois de responder todas`() {
        var s = LessonRun.start(TestData.questions(3), repeatMistakes = false)
        s = LessonRun.answer(s, false, 60_000, 180_000)
        s = LessonRun.answer(s, true, 60_000, 180_000)
        s = LessonRun.answer(s, false, 60_000, 180_000)
        assertTrue(s.finished)
        assertEquals(3, s.records.size)
        assertEquals(3, s.planned)
        assertEquals(15, s.xp)
        assertFalse(s.perfect)
    }

    @Test fun `treino sem recompensa nao da xp por resposta`() {
        var s = LessonRun.start(TestData.questions(2), awardsXp = false)
        s = LessonRun.answer(s, true, 10_000, 180_000)
        s = LessonRun.answer(s, false, 10_000, 180_000)
        s = LessonRun.answer(s, true, 10_000, 180_000) // repetição
        assertTrue(s.finished)
        assertEquals(0, s.xp)
        assertTrue(s.records.all { it.xp == 0 })
    }
}
