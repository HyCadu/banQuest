package br.com.trilhati.data.content

import br.com.trilhati.domain.lesson.LessonPlanner
import br.com.trilhati.domain.model.Curriculum
import br.com.trilhati.domain.model.QuestionLevel
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

    @Test fun `todos os pacotes embutidos sao validos, por conceitos, e cada questao oficial tem conceito da licao`() {
        val packs = File("src/main/assets/content").listFiles { f -> f.extension == "json" }!!.sortedBy { it.name }
            .map { json.decodeFromString(PackDto.serializer(), it.readText(Charsets.UTF_8)) }
        assertEquals(listOf("u1", "u11", "u2"), packs.map { it.unit.id })
        packs.forEach { assertEquals(it.pack, emptyList<String>(), ContentValidator.validate(it)) }
        val exam = parseProvas().toDomain()
        val course = CourseAssembler.assemble(packs.mapNotNull { it.toDomain() }, exam)
        // U2 e G1 (u11) chegaram na 0.10.0; as oficiais que esperavam por elas entram ligadas a um conceito da lição.
        // 0.11.0: + IDECAN Banestes 2012 e Instituto Access Banestes 2024 (U2: 22 + 13; G1: 35 + 20).
        // 0.14.0: + FGV Banestes 2018 e 2023 de Técnico Bancário (G1: + 19 + 11).
        listOf("u2" to 35, "u11" to 85).forEach { (id, official) ->
            val unit = course.first { it.id == id }
            assertTrue(unit.lessons.all { it.hasContent && it.concepts.isNotEmpty() })
            assertEquals(id, official, unit.lessons.sumOf { l -> l.questions.count { it.isFromExam } })
            unit.lessons.forEach { l -> assertTrue(l.id, l.questions.all { l.concept(it.conceptId) != null }) }
        }
        assertTrue(course.filter { it.id !in setOf("u1", "u2", "u11") }.none { it.hasContent })
    }

    @Test fun `unidade 1 tem 5 licoes por conceitos, a primeira com 50 exercicios e as outras com 30`() {
        val pack = parseU1()
        assertEquals(5, pack.lessons.size)
        assertEquals(50, pack.lessons[0].questions.size)
        assertTrue(pack.lessons.all { it.concepts.isNotEmpty() })
        assertTrue(pack.lessons.drop(1).all { it.questions.size == 30 && it.concepts.size == 5 })
    }

    @Test fun `licao piloto tem conceitos e cada um passa por aprender, praticar e prova`() {
        val exam = parseProvas().toDomain()
        val lesson = CourseAssembler.assemble(listOfNotNull(parseU1().toDomain()), exam)[0].lessons[0]
        assertEquals(7, lesson.concepts.size)
        assertTrue(lesson.questions.all { lesson.concept(it.conceptId) != null })
        lesson.concepts.forEach { c ->
            val qs = lesson.questions.filter { it.conceptId == c.id }
            assertTrue("${c.id} aprender", qs.count { it.level == QuestionLevel.Learn } >= 2)
            assertTrue("${c.id} praticar", qs.count { it.level == QuestionLevel.Practice } >= 2)
            assertTrue("${c.id} prova", qs.any { it.level == QuestionLevel.Exam })
            // Sempre há item de aprender e de praticar que não é adaptado: a oficial na sessão não deixa o conceito vazio.
            assertTrue(qs.any { it.level == QuestionLevel.Learn && it.adaptedFrom == null })
            assertTrue(qs.any { it.level == QuestionLevel.Practice && it.adaptedFrom == null })
            // Passos 1, 2 e 3 para montar o bloco "exemplo → aplicar → variar".
            assertTrue(qs.filter { it.level != QuestionLevel.Exam }.mapNotNull { it.step }.toSet().containsAll(setOf(1, 2, 3)))
        }
        assertTrue(LessonPlanner.usesConcepts(lesson.questions))
        assertEquals(LessonPlanner.SIZE, LessonPlanner.plan(lesson.questions, emptySet(), kotlin.random.Random(1)).size)
    }

    @Test fun `adaptadas apontam para questoes oficiais que existem, com a mesma referencia`() {
        val exam = parseProvas().toDomain().associateBy { it.id }
        val adapted = parseU1().toDomain()!!.lessons.flatMap { it.questions }.filter { it.adaptedFrom != null }
        assertEquals(8, adapted.size)
        adapted.forEach { q ->
            val a = q.adaptedFrom!!
            val official = exam[a.questionId]
            assertNotNull("${q.id} → ${a.questionId}", official)
            assertEquals(official!!.ref!!.shortLabel, a.ref.shortLabel)
            assertEquals(official.lessonId, q.lessonId)
            assertTrue(q.source == QuestionSource.Ia && q.level != QuestionLevel.Exam)
        }
        assertEquals("adaptada de FCC · Prefeitura de Teresina 2016 · questão 56", adapted.first { it.adaptedFrom!!.questionId.endsWith("q56") }.adaptedFrom!!.label)
    }

    @Test fun `validador reprova formato por conceitos com problemas`() {
        val pack = parseU1()
        val lesson = pack.lessons[0]
        val quick = lesson.questions.first { it.type == "quick" }
        fun broken(f: (QuestionDto) -> QuestionDto) =
            ContentValidator.validate(pack.copy(lessons = listOf(lesson.copy(questions = listOf(f(quick))))))
        assertEquals(emptyList<String>(), broken { it })
        assertTrue(broken { it.copy(options = it.options + "Outra" + "Mais uma") }.any { "alternativas" in it })
        assertTrue(broken { it.copy(concept = "u1l1c99") }.any { "conceito inexistente" in it })
        assertTrue(broken { it.copy(level = "facil") }.any { "level" in it })
        val adapted = lesson.questions.first { it.adaptedFrom != null }
        val officialWithAdapted = ContentValidator.validate(
            pack.copy(lessons = listOf(lesson.copy(questions = listOf(adapted.copy(source = "prova_real", ref = parseProvas().questions[0].ref))))),
        )
        assertTrue(officialWithAdapted.any { "adaptado" in it })
        val noConcepts = ContentValidator.validate(pack.copy(lessons = listOf(lesson.copy(concepts = emptyList(), questions = listOf(quick)))))
        assertTrue(noConcepts.any { "sem conceitos" in it })
    }

    @Test fun `toda questao de IA vem como nao revisada`() {
        val unit = parseU1().toDomain()
        assertNotNull(unit)
        val all = unit!!.lessons.flatMap { it.questions }
        assertEquals(170, all.size)
        assertTrue(all.all { it.source == QuestionSource.Ia && !it.reviewed && it.showUnreviewedBadge })
    }

    @Test fun `mapeia todos os tipos de exercicio da unidade 1`() {
        val types = parseU1().toDomain()!!.lessons.flatMap { it.questions }.map { it.type }.toSet()
        assertEquals(
            setOf(QuestionType.Mcq5, QuestionType.Case, QuestionType.TrueFalse, QuestionType.Match, QuestionType.OrderCode, QuestionType.FillBlank, QuestionType.CodeOutput, QuestionType.Quick),
            types,
        )
    }

    @Test fun `catalogo tem 19 unidades e 91 licoes, com conteudo so na unidade 1`() {
        val catalog = CourseAssembler.assemble(listOfNotNull(parseU1().toDomain()))
        assertEquals(19, catalog.size)
        assertEquals(91, catalog.sumOf { it.lessons.size })
        assertEquals("u14l4", catalog[13].lessons[3].id)
        assertTrue(catalog[0].lessons.all { it.hasContent })
        assertTrue(catalog.drop(1).none { it.hasContent })
        assertEquals("u1l1", catalog[0].lessons[0].id)
        assertEquals("u10l4", catalog[9].lessons[3].id)
    }

    @Test fun `trilha do analista mantem as 48 licoes de especificos e os gerais ganham a G5`() {
        val catalog = CourseAssembler.assemble(listOfNotNull(parseU1().toDomain()))
        val course = br.com.trilhati.domain.exam.ExamCourse.courseFor(br.com.trilhati.domain.exam.TargetExam.AnalistaTi, catalog)
        val specific = course.filter { it.track == br.com.trilhati.domain.model.Track.Specific }
        val general = course.filter { it.track == br.com.trilhati.domain.model.Track.General }
        assertEquals(48, specific.sumOf { it.lessons.size })
        assertEquals(21, general.sumOf { it.lessons.size })
        assertEquals(listOf("G1", "G2", "G3", "G4", "G5"), general.map { it.code })
        assertEquals(listOf("u11", "u12", "u13", "u14", "u15"), general.map { it.id })
        assertEquals("Unidade 10", specific.last().code)
    }

    @Test fun `curriculo segue a distribuicao do plano`() {
        // u1–u10 (Analista), u11–u15 (Gerais, com a G2 dividida em u12 e u15) e u16–u19 (Técnico Bancário).
        assertEquals(listOf(5, 5, 5, 5, 4, 6, 5, 4, 5, 4, 5, 4, 4, 4, 4, 7, 5, 4, 6), Curriculum.units.map { it.lessonTitles.size })
        assertEquals((1..19).map { "u$it" }, Curriculum.units.map { it.id })
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
        val pack = parseU1()
        val course = CourseAssembler.assemble(listOfNotNull(pack.toDomain()), exam)
        val u1 = course[0]
        u1.lessons.forEachIndexed { i, lesson ->
            val extra = exam.count { it.lessonId == lesson.id }
            assertEquals("lição ${lesson.id}", pack.lessons[i].questions.size + extra, lesson.questions.size)
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

    @Test fun `questao de prova real aceita 4 alternativas, e a de IA continua exigindo 5`() {
        val pack = parseProvas()
        val q = pack.questions.first { it.type == "mcq5" && it.answer < 4 }
        assertEquals(emptyList<String>(), ContentValidator.validate(pack.copy(questions = listOf(q.copy(options = q.options.take(4))))))
        assertTrue(ContentValidator.validate(pack.copy(questions = listOf(q.copy(options = q.options.take(3))))).any { "alternativas" in it })
        val u1 = parseU1()
        val ia = u1.lessons[0].questions.first { it.type == "mcq5" && it.answer < 4 }
        val iaWith4 = u1.copy(lessons = listOf(u1.lessons[0].copy(questions = listOf(ia.copy(options = ia.options.take(4))))))
        assertTrue(ContentValidator.validate(iaWith4).any { "alternativas" in it })
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
