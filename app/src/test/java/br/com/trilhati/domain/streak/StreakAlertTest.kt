package br.com.trilhati.domain.streak

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StreakAlertTest {
    @Test fun `sem aviso se ja estudou hoje ou nao tem ofensiva`() {
        assertNull(StreakAlert.text(5, studiedToday = true, pairs = listOf("Bia" to 3)))
        assertNull(StreakAlert.text(0, studiedToday = false, pairs = emptyList()))
    }

    @Test fun `so a ofensiva do aluno`() {
        assertEquals("Sua ofensiva de 3 dias acaba hoje se você não fizer uma lição.", StreakAlert.text(3, false, emptyList()))
        assertEquals("Sua ofensiva de 1 dia acaba hoje se você não fizer uma lição.", StreakAlert.text(1, false, emptyList()))
    }

    @Test fun `ofensiva do aluno e duplas - mostra a dupla mais longa`() {
        assertEquals(
            "Sua ofensiva de 3 dias e a dupla com Bia (5 dias) e mais 1 acabam hoje se você não fizer uma lição.",
            StreakAlert.text(3, false, listOf("Leo" to 2, "Bia" to 5)),
        )
    }

    @Test fun `so a dupla`() {
        assertEquals("A ofensiva em dupla com Bia (1 dia) acaba hoje se você não fizer uma lição.", StreakAlert.text(0, false, listOf("Bia" to 1)))
    }
}
