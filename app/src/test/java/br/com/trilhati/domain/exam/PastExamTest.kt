package br.com.trilhati.domain.exam

import br.com.trilhati.domain.TestData
import br.com.trilhati.domain.model.AnswerKey
import br.com.trilhati.domain.model.ExamRef
import br.com.trilhati.domain.model.Question
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PastExamTest {
    private fun q(
        banca: String, body: String, year: Int, number: Int, lessonId: String = "u1l1",
        key: AnswerKey = AnswerKey.Final, cargo: String = "Analista",
    ): Question = TestData.question("${banca.lowercase()}-$year-q$number", lessonId)
        .copy(ref = ExamRef(banca, body, year, number, key, cargo))

    private val general: (Question) -> Boolean = { it.lessonId.removePrefix("u").substringBefore("l").toInt() > 10 }

    @Test fun `agrupa por prova, da mais recente para a mais antiga, na ordem das questoes`() {
        val exams = PastExams.from(
            listOf(
                q("FCC", "Prefeitura de Teresina", 2016, 54), q("FGV", "Banestes", 2021, 12),
                q("FCC", "Prefeitura de Teresina", 2016, 22), q("FGV", "Banestes", 2018, 3),
                TestData.question("ia-sem-ref"),
            ),
            general,
        )
        assertEquals(listOf("fgv-banestes-2021", "fgv-banestes-2018", "fcc-prefeitura-de-teresina-2016"), exams.map { it.id })
        val teresina = exams.last()
        assertEquals("FCC · Prefeitura de Teresina 2016", teresina.title)
        assertEquals(listOf(22, 54), teresina.questions(ExamPart.Full).map { it.ref?.number })
        assertEquals(2, teresina.size)
    }

    @Test fun `prova com especificos e gerais oferece as tres partes`() {
        val exam = PastExams.from(
            listOf(q("FGV", "Banestes", 2018, 1, "u11l2"), q("FGV", "Banestes", 2018, 51, "u1l1"), q("FGV", "Banestes", 2018, 2, "u12l1")),
            general,
        ).single()
        assertEquals(ExamPart.entries.toList(), exam.parts)
        assertEquals(listOf(1, 2, 51), exam.questions(ExamPart.Full).map { it.ref?.number })
        assertEquals(listOf(51), exam.questions(ExamPart.Specific).map { it.ref?.number })
        assertEquals(listOf(1, 2), exam.questions(ExamPart.General).map { it.ref?.number })
        assertEquals("FGV · Banestes 2018 · Específicos", exam.title(ExamPart.Specific))
        assertEquals("FGV · Banestes 2018", exam.title(ExamPart.Full))
    }

    @Test fun `prova so de especificos tem uma parte e qualquer parte vale a prova inteira`() {
        val exam = PastExams.from(listOf(q("FCC", "DPE-RR", 2015, 31), q("FCC", "DPE-RR", 2015, 32)), general).single()
        assertEquals(listOf(ExamPart.Full), exam.parts)
        assertEquals(2, exam.questions(ExamPart.General).size)
        assertEquals("FCC · DPE-RR 2015", exam.title(ExamPart.Specific))
    }

    @Test fun `gabarito da prova e o menos firme entre as questoes`() {
        val exam = PastExams.from(
            listOf(q("FCC", "DPE-RR", 2015, 1), q("FCC", "DPE-RR", 2015, 2, key = AnswerKey.Unconfirmed), q("FCC", "DPE-RR", 2015, 3, key = AnswerKey.Preliminary)),
            general,
        ).single()
        assertEquals(AnswerKey.Unconfirmed, exam.answerKey)
    }

    @Test fun `mesma banca, orgao e ano com cargos diferentes viram provas diferentes`() {
        val exams = PastExams.from(listOf(q("FGV", "Banestes", 2018, 1, cargo = "Analista"), q("FGV", "Banestes", 2018, 1, cargo = "Técnico")), general)
        // a segunda leva o cargo no id (0.14.0; antes era "-2")
        assertEquals(listOf("fgv-banestes-2018", "fgv-banestes-2018-tecnico"), exams.map { it.id })
    }

    @Test fun `slug sem acentos nem simbolos`() {
        assertEquals("fcc-prefeitura-de-sao-jose-2016", PastExams.slug("FCC · Prefeitura de São José 2016"))
    }

    @Test fun `abrir gasta um coracao inteiro, a nao ser no modo sem vidas`() {
        assertEquals(2, ExamRules.openCostHalves(noLives = false))
        assertEquals(0, ExamRules.openCostHalves(noLives = true))
        assertTrue(ExamRules.canOpen(halves = 2, noLives = false))
        assertFalse(ExamRules.canOpen(halves = 1, noLives = false)) // meio coração não abre
        assertFalse(ExamRules.canOpen(halves = 0, noLives = false))
        assertTrue(ExamRules.canOpen(halves = 0, noLives = true))
    }

    @Test fun `so conta como concluida quando todas foram respondidas`() {
        assertFalse(ExamRules.completed(answered = 36, total = 37))
        assertTrue(ExamRules.completed(answered = 37, total = 37))
        assertFalse(ExamRules.completed(answered = 0, total = 0))
    }

    @Test fun `tempo estimado em horas e minutos`() {
        assertEquals("4h21", ExamRules.durationLabel(87 * 180_000L))
        assertEquals("1h05", ExamRules.durationLabel(65 * 60_000L))
        assertEquals("52 min", ExamRules.durationLabel(52 * 60_000L))
        assertEquals("1 min", ExamRules.durationLabel(0))
    }
}
