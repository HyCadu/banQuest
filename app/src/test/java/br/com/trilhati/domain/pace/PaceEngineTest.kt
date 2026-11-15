package br.com.trilhati.domain.pace

import org.junit.Assert.assertEquals
import org.junit.Test

class PaceEngineTest {
    private val target = 180_000L

    @Test fun `abaixo de 2 15 fica no ritmo`() = assertEquals(PaceZone.OnPace, PaceEngine.zone(134_999, target))
    @Test fun `a partir de 2 15 fica ambar`() = assertEquals(PaceZone.Warning, PaceEngine.zone(135_000, target))
    @Test fun `exatamente 3 00 ainda e ambar`() = assertEquals(PaceZone.Warning, PaceEngine.zone(180_000, target))
    @Test fun `estourou fica vermelho`() = assertEquals(PaceZone.Over, PaceEngine.zone(180_001, target))

    @Test fun `fracao restante vai de 1 a 0 e nunca fica negativa`() {
        assertEquals(1f, PaceEngine.remainingFraction(0, target), 0f)
        assertEquals(0.5f, PaceEngine.remainingFraction(90_000, target), 0.0001f)
        assertEquals(0f, PaceEngine.remainingFraction(400_000, target), 0f)
    }

    @Test fun `folga e atraso`() {
        // 5 questões x 3:00 = 15:00 de alvo; usou 12:30 -> folga de 2:30
        assertEquals(150_000, PaceEngine.slackMs(900_000, 750_000))
        // usou 16:00 -> atraso de 1:00
        assertEquals(-60_000, PaceEngine.slackMs(900_000, 960_000))
    }

    @Test fun `media por questao`() {
        assertEquals(120_000, PaceEngine.averageMs(960_000, 8))
        assertEquals(0, PaceEngine.averageMs(0, 0))
    }

    @Test fun `formata minutos e segundos`() {
        assertEquals("2:05", PaceEngine.format(125_000))
        assertEquals("0:09", PaceEngine.format(9_400))
        assertEquals("0:15", PaceEngine.format(-15_000))
    }

    @Test fun `alvo de caso pratico so muda se a opcao estiver ligada`() {
        assertEquals(180_000, PaceConfig().targetMs(isCase = true))
        assertEquals(240_000, PaceConfig(caseTakesFourMinutes = true).targetMs(isCase = true))
        assertEquals(180_000, PaceConfig(caseTakesFourMinutes = true).targetMs(isCase = false))
        assertEquals(150_000, PaceConfig(perQuestionMs = PaceConfig.TIGHT_MS).targetMs(false))
    }
}
