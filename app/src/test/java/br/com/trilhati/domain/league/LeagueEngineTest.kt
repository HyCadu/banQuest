package br.com.trilhati.domain.league

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LeagueEngineTest {
    private val monday = LocalDate.of(2026, 11, 9)

    @Test fun `semana comeca na segunda-feira`() {
        assertEquals(monday, LeagueEngine.weekStart(LocalDate.of(2026, 11, 15))) // domingo
        assertEquals(monday, LeagueEngine.weekStart(monday))
        assertEquals(LocalDate.of(2026, 11, 16), LeagueEngine.weekStart(LocalDate.of(2026, 11, 16)))
    }

    @Test fun `fracao da semana`() {
        assertEquals(0.0, LeagueEngine.weekFraction(monday, 0), 1e-9)
        assertEquals(0.5 / 7, LeagueEngine.weekFraction(monday, 43_200), 1e-9)
        assertEquals(6.0 / 7, LeagueEngine.weekFraction(LocalDate.of(2026, 11, 15), 0), 1e-9)
    }

    @Test fun `bots sao deterministicos`() {
        val a = LeagueEngine.standings(monday, LeagueTier.Bronze, 0.5, "Eu", 100)
        val b = LeagueEngine.standings(monday, LeagueTier.Bronze, 0.5, "Eu", 100)
        assertEquals(a, b)
    }

    @Test fun `sao 30 participantes com nomes unicos e o usuario aparece uma vez`() {
        val s = LeagueEngine.standings(monday, LeagueTier.Prata, 1.0, "Eu", 300)
        assertEquals(30, s.size)
        assertEquals(30, s.map { it.name }.toSet().size)
        assertEquals(1, s.count { it.isUser })
    }

    @Test fun `xp dos bots so cresce ao longo da semana`() {
        for (i in 0 until 29) {
            var last = -1
            for (step in 0..28) {
                val xp = LeagueEngine.botXp(monday, i, LeagueTier.Ouro, step / 28.0)
                assertTrue("bot $i recuou no passo $step", xp >= last)
                last = xp
            }
        }
    }

    @Test fun `no inicio da semana todos os bots tem zero`() {
        (0 until 29).forEach { assertEquals(0, LeagueEngine.botXp(monday, it, LeagueTier.Bronze, 0.0)) }
    }

    @Test fun `semanas diferentes geram resultados diferentes`() {
        val a = (0 until 29).map { LeagueEngine.botXp(monday, it, LeagueTier.Bronze, 1.0) }
        val b = (0 until 29).map { LeagueEngine.botXp(monday.plusWeeks(1), it, LeagueTier.Bronze, 1.0) }
        assertTrue(a != b)
    }

    @Test fun `classificacao ordena do maior para o menor xp`() {
        val s = LeagueEngine.standings(monday, LeagueTier.Bronze, 1.0, "Eu", 5_000)
        assertEquals(1, LeagueEngine.rankOfUser(s))
        assertEquals(s.map { it.xp }.sortedDescending(), s.map { it.xp })
    }

    @Test fun `top 10 sobem`() {
        assertEquals(LeagueOutcome.Promoted, LeagueEngine.close(LeagueTier.Bronze, 10, 200).outcome)
        assertEquals(LeagueTier.Prata, LeagueEngine.close(LeagueTier.Bronze, 1, 200).newTier)
    }

    @Test fun `ultimos 5 descem exceto na bronze`() {
        assertEquals(LeagueOutcome.Relegated, LeagueEngine.close(LeagueTier.Ouro, 26, 10).outcome)
        assertEquals(LeagueTier.Prata, LeagueEngine.close(LeagueTier.Ouro, 30, 10).newTier)
        assertEquals(LeagueOutcome.Stayed, LeagueEngine.close(LeagueTier.Bronze, 30, 0).outcome)
    }

    @Test fun `meio da tabela fica onde esta`() {
        assertEquals(LeagueOutcome.Stayed, LeagueEngine.close(LeagueTier.Safira, 11, 300).outcome)
        assertEquals(LeagueOutcome.Stayed, LeagueEngine.close(LeagueTier.Safira, 25, 300).outcome)
    }

    @Test fun `zero xp nao sobe mesmo no top 10`() {
        assertEquals(LeagueOutcome.Stayed, LeagueEngine.close(LeagueTier.Bronze, 3, 0).outcome)
    }

    @Test fun `diamante nao sobe`() {
        assertEquals(LeagueTier.Diamante, LeagueEngine.close(LeagueTier.Diamante, 1, 999).newTier)
    }

    @Test fun `oito camadas na ordem do plano`() {
        assertEquals(
            listOf("Bronze", "Prata", "Ouro", "Safira", "Rubi", "Esmeralda", "Ametista", "Diamante"),
            LeagueTier.entries.map { it.displayName },
        )
    }

    @Test fun `curva realista - usuario com meta de 100 por dia fica competitivo na bronze`() {
        val s = LeagueEngine.standings(monday, LeagueTier.Bronze, 1.0, "Eu", 700)
        val rank = LeagueEngine.rankOfUser(s)
        assertTrue("rank $rank", rank in 3..20)
    }
}
