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

    private val provas = File("src/main/assets/provas/provas-anteriores.json")

    private fun parseU1() = json.decodeFromString(PackDto.serializer(), u1.readText(Charsets.UTF_8))
    private fun parseProvas() = json.decodeFromString(ExamPackDto.serializer(), provas.readText(Charsets.UTF_8))

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

    @Test fun `pacote de provas anteriores do app e valido`() {
        assertEquals(emptyList<String>(), ContentValidator.validate(parseProvas()))
    }

    @Test fun `questoes de prova tem banca, licao e selo so na explicacao`() {
        val questions = parseProvas().toDomain()
        assertEquals(parseProvas().questions.size, questions.size)
        assertTrue(questions.all { it.source == QuestionSource.ProvaReal && it.ref != null && it.isFromExam })
        assertTrue(questions.all { it.lessonId in Curriculum.lessonIds })
        assertTrue(questions.all { !it.reviewed && it.showUnreviewedBadge })
        // CLAUDE.md, regra 4: só questão de prova da FCC pode citar a FCC.
        assertTrue(questions.filter { it.ref?.banca != "FCC" }.none { "FCC" in it.stem || "FCC" in it.explanation })
    }

    @Test fun `provas entram so nas licoes que ja tem pacote`() {
        val exam = parseProvas().toDomain()
        val course = CourseAssembler.assemble(listOfNotNull(parseU1().toDomain()), exam)
        val u1 = course[0]
        u1.lessons.forEach { lesson ->
            val extra = exam.count { it.lessonId == lesson.id }
            assertEquals("lição ${lesson.id}", 10 + extra, lesson.questions.size)
        }
        assertTrue(u1.lessons.sumOf { l -> l.questions.count { it.isFromExam } } > 0)
        // Unidades sem pacote continuam "chegando": nenhuma lição de 1 ou 2 questões de prova.
        assertTrue(course.drop(1).none { it.hasContent })
    }

    @Test fun `provas esperando entram quando o pacote da unidade chega`() {
        val exam = parseProvas().toDomain()
        val u1 = parseU1().toDomain()!!
        val fakeU2 = u1.copy(id = "u2", order = 2, lessons = u1.lessons.mapIndexed { i, l ->
            l.copy(id = "u2l${i + 1}", unitId = "u2", questions = l.questions.map { it.copy(id = "u2-${it.id}", lessonId = "u2l${i + 1}") })
        })
        val course = CourseAssembler.assemble(listOf(u1, fakeU2), exam)
        val u2Exam = exam.count { it.lessonId.startsWith("u2l") }
        assertTrue(u2Exam > 0)
        assertEquals(u2Exam, course[1].lessons.sumOf { l -> l.questions.count { it.isFromExam } })
    }

    @Test fun `validador reprova questao de prova sem referencia ou fora da trilha`() {
        val pack = parseProvas()
        val q = pack.questions[0]
        fun broken(f: (QuestionDto) -> QuestionDto) = ContentValidator.validate(pack.copy(questions = listOf(f(q))))
        assertEquals(emptyList<String>(), broken { it })
        assertTrue(broken { it.copy(ref = null) }.any { "sem ref" in it })
        assertTrue(broken { it.copy(ref = it.ref!!.copy(gabarito = "talvez")) }.any { "gabarito" in it })
        assertTrue(broken { it.copy(lesson = "u3l9") }.any { "lição" in it })
        assertTrue(broken { it.copy(source = "ia") }.any { "prova_real" in it })
        val u1 = parseU1()
        val iaWithRef = u1.lessons[0].questions[0].copy(ref = q.ref)
        assertTrue(ContentValidator.validate(u1.copy(lessons = listOf(u1.lessons[0].copy(questions = listOf(iaWithRef))))).any { "ref" in it })
    }
}
