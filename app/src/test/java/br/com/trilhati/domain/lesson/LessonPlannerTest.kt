package br.com.trilhati.domain.lesson

import br.com.trilhati.domain.TestData
import br.com.trilhati.domain.model.AdaptedFrom
import br.com.trilhati.domain.model.AnswerKey
import br.com.trilhati.domain.model.ExamRef
import br.com.trilhati.domain.model.Question
import br.com.trilhati.domain.model.QuestionLevel
import br.com.trilhati.domain.model.QuestionType
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonPlannerTest {
    private fun q(
        id: String,
        concept: String,
        level: QuestionLevel,
        step: Int? = null,
        type: QuestionType = QuestionType.Quick,
        adaptedFrom: String? = null,
    ): Question = TestData.question(id, type = type).copy(
        conceptId = concept, level = level, step = step,
        adaptedFrom = adaptedFrom?.let { AdaptedFrom(it, ExamRef("FGV", "Banestes", 2021, 41, AnswerKey.Final)) },
    )

    private val order = (1..6).map { "c$it" }

    /** 6 conceitos; cada um com passos 1, 2 (duas variantes) e 3, e duas questões de prova. */
    private val pool: List<Question> = order.flatMap { c ->
        listOf(
            q("${c}s1", c, QuestionLevel.Learn, 1, QuestionType.TrueFalse),
            q("${c}s2a", c, QuestionLevel.Learn, 2),
            q("${c}s2b", c, QuestionLevel.Learn, 2, QuestionType.TrueFalse),
            q("${c}s3", c, QuestionLevel.Practice, 3, QuestionType.FillBlank),
            q("${c}x1", c, QuestionLevel.Exam, type = QuestionType.Mcq5),
            q("${c}x2", c, QuestionLevel.Exam, type = QuestionType.Case),
        )
    }

    private fun plan(missed: Set<String> = emptySet(), seen: Set<String> = emptySet(), seed: Int = 1, from: List<Question> = pool) =
        LessonPlanner.plan(from, missed, Random(seed), order, seen)

    @Test fun `primeira vez traz os 3 primeiros conceitos, um bloco de cada, em ordem, e a prova no fim`() {
        repeat(20) { seed ->
            val p = plan(seed = seed)
            assertEquals(LessonPlanner.SIZE, p.size)
            assertEquals(p.size, p.map { it.id }.toSet().size)
            val teaching = p.take(9)
            // Blocos contíguos de c1, c2 e c3, cada um no passo 1, 2 e 3.
            assertEquals(listOf("c1", "c1", "c1", "c2", "c2", "c2", "c3", "c3", "c3"), teaching.map { it.conceptId })
            teaching.chunked(3).forEach { block -> assertEquals(listOf(1, 2, 3), block.map { it.step }) }
            // Prova no fim, dos mesmos conceitos.
            val exam = p.drop(9)
            assertTrue(exam.all { it.level == QuestionLevel.Exam && it.conceptId in setOf("c1", "c2", "c3") })
            assertEquals(3, exam.map { it.conceptId }.toSet().size)
        }
    }

    @Test fun `depois de ver os 3 primeiros, a proxima sessao avanca para os seguintes`() {
        val seen = pool.filter { it.conceptId in setOf("c1", "c2", "c3") && it.level != QuestionLevel.Exam }.map { it.id }.toSet()
        val p = plan(seen = seen)
        assertEquals(listOf("c4", "c5", "c6"), p.take(9).map { it.conceptId }.distinct())
        assertEquals(1, p.first().step)
    }

    @Test fun `com tudo visto, os conceitos dos erros voltam primeiro e o erro abre o bloco`() {
        val seen = pool.map { it.id }.toSet()
        repeat(10) { seed ->
            val p = plan(missed = setOf("c5s3"), seen = seen, seed = seed)
            val concepts = p.take(9).map { it.conceptId }.distinct()
            assertTrue("c5" in concepts)
            assertTrue("c5s3" in p.map { it.id })
            // Revisão: não recomeça pelo exemplo resolvido (passo 1) quando há outros passos.
            val c5 = p.filter { it.conceptId == "c5" && it.level != QuestionLevel.Exam }
            assertEquals(3, c5.size)
            assertEquals(c5.map { it.step }, c5.map { it.step }.sortedBy { it })
        }
    }

    @Test fun `variantes do mesmo passo nao aparecem juntas no bloco novo`() {
        repeat(20) { seed ->
            val c1 = plan(seed = seed).filter { it.conceptId == "c1" && it.level != QuestionLevel.Exam }
            assertEquals(1, c1.count { it.step == 2 })
        }
    }

    @Test fun `questao oficial fica fora da prova quando uma adaptada dela esta no bloco`() {
        val official = q("fgv-banestes-2021-q41", "c1", QuestionLevel.Exam, type = QuestionType.Mcq5)
        val small = listOf(
            q("a1", "c1", QuestionLevel.Learn, 1, adaptedFrom = official.id),
            q("a2", "c1", QuestionLevel.Learn, 2),
            q("a3", "c1", QuestionLevel.Practice, 3, QuestionType.Match),
            official,
            q("outra", "c1", QuestionLevel.Exam, type = QuestionType.Case),
        )
        repeat(20) { seed ->
            val p = LessonPlanner.plan(small, emptySet(), Random(seed), listOf("c1"))
            assertTrue("a1" in p.map { it.id })
            assertFalse(official in p)
            assertTrue(p.last().id == "outra")
        }
    }

    @Test fun `banco pequeno usa o que houver`() {
        val tiny = pool.filter { it.conceptId == "c1" }
        val p = plan(from = tiny)
        assertEquals(tiny.size, p.size)
        assertEquals(listOf(1, 2, 3), p.take(3).map { it.step })
    }

    @Test fun `licao sem conceitos continua no sorteio antigo de 8`() {
        val legacy = TestData.questions(16)
        assertFalse(LessonPlanner.usesConcepts(legacy))
        assertEquals(LessonRun.pick(legacy, emptySet(), Random(9)), LessonPlanner.plan(legacy, emptySet(), Random(9)))
        assertEquals(LessonRun.SIZE, LessonPlanner.plan(legacy, emptySet(), Random(9)).size)
    }

    @Test fun `mesma semente, mesma sessao`() {
        assertEquals(plan(setOf("c2s3"), seed = 42), plan(setOf("c2s3"), seed = 42))
    }

    @Test fun `prova evita tipos iguais seguidos`() {
        val a = q("a", "c", QuestionLevel.Exam, type = QuestionType.Mcq5)
        val b = q("b", "c", QuestionLevel.Exam, type = QuestionType.Mcq5)
        val c = q("c", "c", QuestionLevel.Exam, type = QuestionType.Case)
        assertEquals(listOf(a, c, b), LessonPlanner.spreadTypes(listOf(a, b, c), null))
        assertEquals(listOf(c, a, b), LessonPlanner.spreadTypes(listOf(a, b, c), a))
    }
}
