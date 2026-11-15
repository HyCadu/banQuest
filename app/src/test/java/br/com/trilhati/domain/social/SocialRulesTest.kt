package br.com.trilhati.domain.social

import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SocialRulesTest {
    @Test fun `email valido e invalido`() {
        assertTrue(SocialRules.isValidEmail("carlos@exemplo.com"))
        assertTrue(SocialRules.isValidEmail("  a.b+c@d.com.br "))
        assertFalse(SocialRules.isValidEmail("carlos"))
        assertFalse(SocialRules.isValidEmail("carlos@exemplo"))
        assertFalse(SocialRules.isValidEmail("ca rlos@exemplo.com"))
    }

    @Test fun `apelido entre 2 e 24 caracteres sem espacos sobrando`() {
        assertEquals("Ana Clara", SocialRules.normalizeName("  Ana   Clara "))
        assertTrue(SocialRules.isValidName("Bi"))
        assertFalse(SocialRules.isValidName(" A "))
        assertFalse(SocialRules.isValidName("x".repeat(25)))
    }

    @Test fun `codigo de amigo aceita varias formas de digitar`() {
        assertEquals("TRI-ABC23", SocialRules.normalizeFriendCode("tri-abc23"))
        assertEquals("TRI-ABC23", SocialRules.normalizeFriendCode("TRI ABC23"))
        assertEquals("TRI-ABC23", SocialRules.normalizeFriendCode("abc23"))
        assertEquals("TRI-ABC23", SocialRules.normalizeFriendCode(" TRIABC23 "))
    }

    @Test fun `codigo invalido`() {
        assertNull(SocialRules.normalizeFriendCode("abc"))
        assertNull(SocialRules.normalizeFriendCode("TRI-ABC0O")) // 0 e O não existem no alfabeto dos códigos
        assertNull(SocialRules.normalizeFriendCode("TRI-ABC231"))
    }

    private val today = LocalDate.of(2026, 11, 18)

    @Test fun `limites iguais aos do servidor`() {
        assertTrue(SessionLimits.accepts("lesson", 150, 8, 16_000, today, today))
        assertFalse(SessionLimits.accepts("lesson", 151, 8, 16_000, today, today))
        assertTrue(SessionLimits.accepts("practice", 120, 8, 16_000, today, today))
        assertFalse(SessionLimits.accepts("practice", 121, 8, 16_000, today, today))
        assertFalse(SessionLimits.accepts("lesson", 50, 8, 15_999, today, today))
        assertFalse(SessionLimits.accepts("lesson", 50, 0, 16_000, today, today))
        assertFalse(SessionLimits.accepts("lesson", 50, 8, 16_000, today.plusDays(1), today))
        assertTrue(SessionLimits.accepts("lesson", 50, 8, 16_000, today.minusDays(7), today))
        assertFalse(SessionLimits.accepts("lesson", 50, 8, 16_000, today.minusDays(8), today))
        assertFalse(SessionLimits.accepts("boss", 50, 8, 16_000, today, today))
    }

    @Test fun `primeira sincronizacao`() {
        assertEquals(InitialSync.UploadLocal, InitialSyncRule.decide(localHasProgress = true, cloudExists = false))
        assertEquals(InitialSync.UploadLocal, InitialSyncRule.decide(localHasProgress = false, cloudExists = false))
        assertEquals(InitialSync.DownloadCloud, InitialSyncRule.decide(localHasProgress = false, cloudExists = true))
        assertEquals(InitialSync.AskUser, InitialSyncRule.decide(localHasProgress = true, cloudExists = true))
        // backup do Android restaurado igual ao da nuvem: não pergunta
        assertEquals(InitialSync.DownloadCloud, InitialSyncRule.decide(localHasProgress = true, cloudExists = true, sameTotals = true))
    }

    @Test fun `podio exige top 3 com pelo menos 3 participantes e xp`() {
        assertTrue(PodiumRule.earned(1, 3, 100))
        assertTrue(PodiumRule.earned(3, 10, 10))
        assertFalse(PodiumRule.earned(4, 10, 100))
        assertFalse(PodiumRule.earned(1, 2, 100))
        assertFalse(PodiumRule.earned(1, 5, 0))
        assertFalse(PodiumRule.earned(null, 5, 100))
    }

    @Test fun `semana do ranking comeca na segunda e termina na proxima segunda a meia-noite local`() {
        val sunday = LocalDate.of(2026, 11, 15)
        val monday = LocalDate.of(2026, 11, 16)
        assertEquals(LocalDate.of(2026, 11, 9), RankingWeek.start(sunday))
        assertEquals(monday, RankingWeek.start(monday))
        val zone = ZoneId.of("America/Sao_Paulo")
        assertEquals(monday.atStartOfDay(zone).toInstant().toEpochMilli(), RankingWeek.endMs(sunday, zone))
    }

    @Test fun `tempo restante da semana em texto curto`() {
        assertEquals("2d 5h", RankingWeek.remainingLabel((2 * 24 + 5) * 3_600_000L + 59_000))
        assertEquals("3h 12min", RankingWeek.remainingLabel(3 * 3_600_000L + 12 * 60_000L))
        assertEquals("45min", RankingWeek.remainingLabel(45 * 60_000L))
        assertEquals("0min", RankingWeek.remainingLabel(-5))
    }

    @Test fun `situacao de hoje na ofensiva em dupla`() {
        assertEquals(PairToday.BothDone, PairStreakRule.today(meToday = true, friendToday = true))
        assertEquals(PairToday.WaitingMe, PairStreakRule.today(meToday = false, friendToday = true))
        assertEquals(PairToday.WaitingFriend, PairStreakRule.today(meToday = true, friendToday = false))
        assertEquals(PairToday.NobodyYet, PairStreakRule.today(meToday = false, friendToday = false))
        assertTrue(PairStreakRule.atRiskForMe(pairStreak = 4, meToday = false))
        assertFalse(PairStreakRule.atRiskForMe(pairStreak = 4, meToday = true))
        assertFalse(PairStreakRule.atRiskForMe(pairStreak = 0, meToday = false))
    }

    @Test fun `mensagens das respostas de amizade`() {
        assertEquals("Pedido de amizade enviado!", SocialMessages.request("requested"))
        assertEquals("Vocês agora são amigos!", SocialMessages.request("accepted"))
        assertEquals("Pedido recusado.", SocialMessages.respond("declined", accepted = false))
        assertEquals("Pronto! Ana agora é seu amigo.", SocialMessages.addByCode("added", "Ana"))
        assertEquals("Esse é o seu próprio código.", SocialMessages.addByCode("self", null))
        assertEquals("Não deu certo agora. Tente de novo.", SocialMessages.request("algo_novo"))
    }
}
