package br.com.trilhati.domain.lesson

import org.junit.Assert.assertEquals
import org.junit.Test

class XpEngineTest {
    @Test fun `acerto de primeira em ate 2 minutos vale 15`() = assertEquals(15, XpEngine.answerXp(true, false, 119_000))
    @Test fun `acerto de primeira exatamente em 2 minutos ainda tem bonus`() = assertEquals(15, XpEngine.answerXp(true, false, 120_000))
    @Test fun `acerto de primeira depois de 2 minutos vale 10`() = assertEquals(10, XpEngine.answerXp(true, false, 120_001))
    @Test fun `acerto na repeticao vale 5 sem bonus de tempo`() = assertEquals(5, XpEngine.answerXp(true, true, 5_000))
    @Test fun `erro nao da XP`() {
        assertEquals(0, XpEngine.answerXp(false, false, 1_000))
        assertEquals(0, XpEngine.answerXp(false, true, 1_000))
    }
    @Test fun `conclusao vale 20 e licao perfeita soma 10`() {
        assertEquals(20, XpEngine.completionXp(false))
        assertEquals(30, XpEngine.completionXp(true))
    }
    @Test fun `nivel comeca em 1 e sobe a cada 500 XP`() {
        assertEquals(1, XpEngine.level(0))
        assertEquals(1, XpEngine.level(499))
        assertEquals(2, XpEngine.level(500))
        assertEquals(3, XpEngine.level(1_000))
        assertEquals(250, XpEngine.xpIntoLevel(750))
        assertEquals(0.5f, XpEngine.levelProgress(750), 0.0001f)
    }

    @Test fun `corrigido vale 5 sem bonus de tempo`() {
        assertEquals(5, XpEngine.answerXp(false, false, 5_000, corrected = true))
        assertEquals(0, XpEngine.answerXp(false, false, 5_000, corrected = false))
    }
}
