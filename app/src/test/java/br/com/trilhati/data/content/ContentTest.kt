package br.com.trilhati.data.content

import br.com.trilhati.domain.model.Curriculum
import br.com.trilhati.domain.model.QuestionSource
import br.com.trilhati.domain.model.QuestionType
import java.io.File
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val u1 = File("src/main/assets/content/u1-banco-de-dados.json")

    private fun parseU1() = json.decodeFromString(PackDto.serializer(), u1.readText(Charsets.UTF_8))

    @Test fun `unidade 1 do app e valida`() {
        assertEquals(emptyList<String>(), ContentValidator.validate(parseU1()))
    }

    @Test fun `unidade 1 tem 5 licoes com 10 questoes cada`() {
        val pack = parseU1()
        assertEquals(5, pack.lessons.size)
        assertTrue(pack.lessons.all { it.questions.size == 10 })
    }

    @Test fun `toda questao de IA vem como nao revisada`() {
        val unit = parseU1().toDomain()
        assertNotNull(unit)
        val all = unit!!.lessons.flatMap { it.questions }
        assertEquals(50, all.size)
        assertTrue(all.all { it.source == QuestionSource.Ia && !it.reviewed && it.showUnreviewedBadge })
    }

    @Test fun `mapeia todos os tipos de exercicio da unidade 1`() {
        val types = parseU1().toDomain()!!.lessons.flatMap { it.questions }.map { it.type }.toSet()
        assertEquals(
            setOf(QuestionType.Mcq5, QuestionType.Case, QuestionType.TrueFalse, QuestionType.Match, QuestionType.OrderCode, QuestionType.FillBlank, QuestionType.CodeOutput),
            types,
        )
    }

    @Test fun `trilha completa tem 10 unidades e 48 licoes, com conteudo so na unidade 1`() {
        val course = CourseAssembler.assemble(listOfNotNull(parseU1().toDomain()))
        assertEquals(10, course.size)
        assertEquals(48, course.sumOf { it.lessons.size })
        assertTrue(course[0].lessons.all { it.hasContent })
        assertTrue(course.drop(1).none { it.hasContent })
        assertEquals("u1l1", course[0].lessons[0].id)
        assertEquals("u10l4", course[9].lessons[3].id)
    }

    @Test fun `curriculo segue a distribuicao do plano`() {
        assertEquals(listOf(5, 5, 5, 5, 4, 6, 5, 4, 5, 4), Curriculum.units.map { it.lessonTitles.size })
    }

    @Test fun `validador reprova json com problemas`() {
        val pack = parseU1()
        val q = pack.lessons[0].questions[0]
        fun broken(f: (QuestionDto) -> QuestionDto) =
            ContentValidator.validate(pack.copy(lessons = listOf(pack.lessons[0].copy(questions = listOf(f(q))))))
        assertTrue(broken { it.copy(answer = 9) }.any { "answer" in it })
        assertTrue(broken { it.copy(options = it.options.take(4)) }.any { "alternativas" in it })
        assertTrue(broken { it.copy(explanation = " ") }.any { "explicação" in it })
        assertTrue(broken { it.copy(type = "xyz") }.any { "type" in it })
        assertTrue(broken { it.copy(reviewed = true) }.any { "IA" in it })
        assertTrue(broken { it.copy(options = it.options.dropLast(1) + "Todas as anteriores") }.any { "proibida" in it })
    }

    @Test fun `validador reprova ids duplicados`() {
        val pack = parseU1()
        val q = pack.lessons[0].questions[0]
        val errors = ContentValidator.validate(pack.copy(lessons = listOf(pack.lessons[0].copy(questions = listOf(q, q)))))
        assertTrue(errors.any { "duplicado" in it })
    }
}
