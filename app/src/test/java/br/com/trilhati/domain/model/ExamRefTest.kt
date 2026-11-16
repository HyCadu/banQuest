package br.com.trilhati.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ExamRefTest {
    @Test fun `etiqueta com gabarito definitivo traz banca, orgao, ano e numero`() {
        assertEquals("FGV · Banestes 2021 · questão 48", ExamRef("FGV", "Banestes", 2021, 48, AnswerKey.Final).label)
    }

    @Test fun `etiqueta avisa quando o gabarito nao e o definitivo`() {
        assertEquals("FCC · DPE-RR 2015 · questão 31 · gabarito não confirmado", ExamRef("FCC", "DPE-RR", 2015, 31, AnswerKey.Unconfirmed).label)
        assertEquals(
            "FCC · Prefeitura de Teresina 2016 · questão 22 · gabarito preliminar",
            ExamRef("FCC", "Prefeitura de Teresina", 2016, 22, AnswerKey.Preliminary).label,
        )
    }

    @Test fun `etiqueta compacta da tela da questao cabe numa linha`() {
        assertEquals("FCC · Teresina 2016 · Q22", ExamRef("FCC", "Prefeitura de Teresina", 2016, 22, AnswerKey.Preliminary).compactLabel)
        assertEquals("FGV · Banestes 2021 · Q48", ExamRef("FGV", "Banestes", 2021, 48, AnswerKey.Final).compactLabel)
    }
}
