package br.com.trilhati.domain.exam

import br.com.trilhati.domain.TestData
import br.com.trilhati.domain.model.AnswerKey
import br.com.trilhati.domain.model.Curriculum
import br.com.trilhati.domain.model.ExamRef
import br.com.trilhati.domain.model.Track
import br.com.trilhati.domain.progress.LessonProgress
import br.com.trilhati.domain.progress.LessonStatus
import br.com.trilhati.domain.progress.TrailEngine
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TargetExamTest {
    /** Catálogo de teste: todas as unidades do currículo, só a u1 e a u11 com conteúdo. */
    private val catalog = Curriculum.units.map { info ->
        TestData.unit(info.order, info.lessonTitles.size, withContent = info.id == "u1" || info.id == "u11")
    }

    @Test fun `datas e formato vem dos editais`() {
        assertEquals(LocalDate.of(2027, 2, 21), TargetExam.AnalistaTi.date)
        assertEquals(LocalDate.of(2027, 1, 24), TargetExam.TecnicoBancario.date)
        assertEquals("80 questões em 4 horas", TargetExam.AnalistaTi.formatLabel)
        assertEquals("70 questões em 4 horas", TargetExam.TecnicoBancario.formatLabel)
    }

    @Test fun `tempo-alvo padrao e a duracao dividida pelas questoes, em passos de 5 s`() {
        assertEquals(180_000L, TargetExam.AnalistaTi.paceTargetMs) // 3:00
        assertEquals(205_000L, TargetExam.TecnicoBancario.paceTargetMs) // 4 h / 70 = 3:25,7 → 3:25
    }

    @Test fun `dias que faltam nunca ficam negativos`() {
        val today = LocalDate.of(2026, 11, 17)
        assertEquals(96, TargetExam.AnalistaTi.daysLeft(today))
        assertEquals(68, TargetExam.TecnicoBancario.daysLeft(today))
        assertEquals(0, TargetExam.TecnicoBancario.daysLeft(LocalDate.of(2027, 3, 1)))
    }

    @Test fun `id gravado volta para a mesma prova e o desconhecido vira nulo`() {
        TargetExam.entries.forEach { assertEquals(it, TargetExam.fromId(it.id)) }
        assertNull(TargetExam.fromId("outra"))
        assertNull(TargetExam.fromId(null))
        assertEquals(TargetExam.AnalistaTi, TargetExam.DEFAULT)
    }

    @Test fun `toda unidade das provas existe no catalogo`() {
        val ids = Curriculum.units.map { it.id }.toSet()
        TargetExam.entries.forEach { exam -> assertTrue(exam.name, exam.unitIds.all { it in ids }) }
        // Cada unidade do catálogo está em alguma prova.
        assertEquals(ids, TargetExam.entries.flatMap { it.unitIds }.toSet())
    }

    @Test fun `analista tem u1 a u10 nos especificos e a G5 de matematica financeira nos gerais`() {
        val course = ExamCourse.courseFor(TargetExam.AnalistaTi, catalog)
        val specific = course.filter { it.track == Track.Specific }
        val general = course.filter { it.track == Track.General }
        assertEquals((1..10).map { "u$it" }, specific.map { it.id })
        assertEquals((1..10).map { "Unidade $it" }, specific.map { it.code })
        assertEquals(listOf("u11", "u12", "u13", "u14", "u15"), general.map { it.id })
        assertEquals(listOf("G1", "G2", "G3", "G4", "G5"), general.map { it.code })
        assertEquals(4, general.last().lessons.size)
    }

    @Test fun `tecnico tem os especificos dele, a matematica financeira sem VPL e os mesmos gerais`() {
        val course = ExamCourse.courseFor(TargetExam.TecnicoBancario, catalog)
        val specific = course.filter { it.track == Track.Specific }
        val general = course.filter { it.track == Track.General }
        assertEquals(listOf("u16", "u17", "u15", "u18", "u19"), specific.map { it.id })
        assertEquals((1..5).map { "Unidade $it" }, specific.map { it.code })
        assertEquals(listOf("u15l1", "u15l2", "u15l3"), specific[2].lessons.map { it.id })
        assertEquals(listOf("u11", "u12", "u13", "u14"), general.map { it.id })
        assertEquals(listOf("G1", "G2", "G3", "G4"), general.map { it.code })
    }

    @Test fun `progresso dos gerais vale nas duas provas`() {
        val progress = mapOf("u11l1" to LessonProgress("u11l1", "u11", timesCompleted = 1))
        TargetExam.entries.forEach { exam ->
            val statuses = TrailEngine.statuses(ExamCourse.courseFor(exam, catalog), progress)
            assertEquals(LessonStatus.Done, statuses["u11l1"])
            assertEquals(LessonStatus.Available, statuses["u11l2"])
        }
        // Sem conteúdo, os Específicos do Técnico ficam travados desde a primeira lição.
        val tb = TrailEngine.statuses(ExamCourse.courseFor(TargetExam.TecnicoBancario, catalog), progress)
        assertEquals(LessonStatus.Locked, tb["u16l1"])
    }

    @Test fun `unidade concluida conta uma vez mesmo com as trilhas das duas provas juntas`() {
        val full = catalog.map { it.copy(lessons = it.lessons.map { l -> l.copy(questions = TestData.questions(1, l.id)) }) }
        val done = Curriculum.units.first { it.id == "u11" }.lessonTitles.indices
            .associate { "u11l${it + 1}" to LessonProgress("u11l${it + 1}", "u11", timesCompleted = 1) }
        val both = TargetExam.entries.flatMap { ExamCourse.courseFor(it, full) }
        assertEquals(1, TrailEngine.completedUnitCount(both, done))
    }

    @Test fun `tecnico ve so os gerais das provas que tem gerais, com o mesmo id`() {
        fun q(body: String, number: Int, lessonId: String) = TestData.question("$body-$number", lessonId)
            .copy(ref = ExamRef("FGV", body, 2018, number, AnswerKey.Final, "Analista de TI"))
        val isGeneral = { it: br.com.trilhati.domain.model.Question -> it.lessonId.startsWith("u11") }
        val exams = PastExams.from(listOf(q("Banestes", 1, "u11l1"), q("Banestes", 51, "u1l1"), q("DPE", 30, "u1l1")), isGeneral)
        assertEquals(exams, PastExams.forTarget(exams, TargetExam.AnalistaTi))
        val tb = PastExams.forTarget(exams, TargetExam.TecnicoBancario)
        assertEquals(listOf("fgv-banestes-2018"), tb.map { it.id })
        assertEquals(listOf(1), tb.single().questions(ExamPart.Full).map { it.ref?.number })
        assertEquals(listOf(ExamPart.Full), tb.single().parts)
        assertTrue(tb.single().cargo.startsWith("Só os Conhecimentos Gerais"))
    }

    @Test fun `cargo da prova anterior diz de qual prova ela e`() {
        assertEquals(TargetExam.TecnicoBancario, TargetExam.forCargo("Técnico Bancário"))
        assertEquals(TargetExam.TecnicoBancario, TargetExam.forCargo("TECNICO BANCARIO"))
        assertEquals(TargetExam.AnalistaTi, TargetExam.forCargo("Analista em Tecnologia da Informação"))
        assertEquals(TargetExam.AnalistaTi, TargetExam.forCargo(""))
    }

    @Test fun `prova do proprio cargo vem inteira e a do outro so com os gerais de quem estuda`() {
        fun q(cargo: String, number: Int, lessonId: String) = TestData.question("${cargo.take(3)}-$number", lessonId)
            .copy(ref = ExamRef("FGV", "Banestes", 2018, number, AnswerKey.Final, cargo))
        val ti = "Analista de TI"
        val tb = "Técnico Bancário"
        // Gerais e Específicos seguem o cargo de cada prova, como no ContentRepository.
        val isGeneral = { it: br.com.trilhati.domain.model.Question ->
            TargetExam.forCargo(it.ref?.cargo.orEmpty()).general.any { s -> s.unitId == it.lessonId.substringBefore('l') }
        }
        val exams = PastExams.from(
            listOf(q(ti, 1, "u11l1"), q(ti, 60, "u1l1"), q(tb, 1, "u11l2"), q(tb, 21, "u15l1"), q(tb, 51, "u16l1")),
            isGeneral,
        )
        // mesmo órgão e ano: a segunda leva o cargo no id
        assertEquals(listOf("fgv-banestes-2018", "fgv-banestes-2018-tecnico-bancario"), exams.map { it.id })
        val tbExam = exams.last()
        assertEquals(listOf(21, 51), tbExam.specific.map { it.ref?.number }) // matemática financeira é Específicos no Técnico

        val forTb = PastExams.forTarget(exams, TargetExam.TecnicoBancario)
        assertEquals(tbExam, forTb.single { it.id == tbExam.id }) // a do próprio cargo, inteira
        assertEquals(listOf(1), forTb.single { it.id == "fgv-banestes-2018" }.questions(ExamPart.Full).map { it.ref?.number })

        val forTi = PastExams.forTarget(exams, TargetExam.AnalistaTi)
        assertEquals(exams.first(), forTi.first())
        // o Analista vê do Técnico os Gerais dele, inclusive a matemática financeira (G5)
        assertEquals(listOf(1, 21), forTi.single { it.id == tbExam.id }.questions(ExamPart.Full).map { it.ref?.number })
    }

    @Test fun `trilha do tecnico destrava pela ordem da prova e nao pelo numero da unidade`() {
        // u16 (Unidade 1 do Técnico) com conteúdo; u15 (Unidade 3) sem. A u15 tem número menor, mas vem depois.
        val withU16 = Curriculum.units.map { info ->
            TestData.unit(info.order, info.lessonTitles.size, withContent = info.id == "u16")
        }
        val statuses = TrailEngine.statuses(ExamCourse.courseFor(TargetExam.TecnicoBancario, withU16), emptyMap())
        assertEquals(LessonStatus.Available, statuses["u16l1"])
        assertEquals(LessonStatus.Locked, statuses["u16l2"])
        assertEquals(LessonStatus.Locked, statuses["u15l1"])
        // concluída a u16, a próxima da trilha é a u17 (Unidade 2), ainda sem conteúdo: trava ali
        val done = (1..7).associate { "u16l$it" to LessonProgress("u16l$it", "u16", timesCompleted = 1) }
        val after = TrailEngine.statuses(ExamCourse.courseFor(TargetExam.TecnicoBancario, withU16), done)
        assertEquals(LessonStatus.Done, after["u16l7"])
        assertEquals(LessonStatus.Locked, after["u17l1"])
    }
}
