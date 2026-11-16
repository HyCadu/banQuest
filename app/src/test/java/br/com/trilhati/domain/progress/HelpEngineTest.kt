package br.com.trilhati.domain.progress

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HelpEngineTest {
    private val day = LocalDate.of(2026, 11, 18)

    @Test fun `comeca com 3 e ganha 1 por dia ate 3`() {
        assertEquals(3, HelpState().count)
        val used = HelpState(1, day.toEpochDay() - 1)
        val next = HelpEngine.daily(used, day)
        assertEquals(HelpState(2, day.toEpochDay()), next)
        // No mesmo dia não ganha de novo.
        assertEquals(next, HelpEngine.daily(next, day))
        // Cheio (3) não passa disso, mas marca o dia.
        assertEquals(HelpState(3, day.toEpochDay()), HelpEngine.daily(HelpState(3, 0), day))
    }

    @Test fun `comprado passa do limite diario ate o maximo, e o presente nao tira nada`() {
        var s = HelpState(3, day.toEpochDay())
        repeat(10) { s = HelpEngine.buy(s) }
        assertEquals(HelpEngine.MAX, s.count)
        assertEquals(HelpEngine.MAX, HelpEngine.daily(s, day.plusDays(1)).count)
    }

    @Test fun `usar gasta um e sem estoque nao usa`() {
        assertEquals(2, HelpEngine.use(HelpState(3, 0))?.count)
        assertNull(HelpEngine.use(HelpState(0, 0)))
    }

    @Test fun `loja vende Nao sei por 50 gemas e recusa quando cheio`() {
        assertEquals(50, ShopItem.Help.priceGems)
        assertEquals(PurchaseResult.AlreadyFull, ShopEngine.canBuy(ShopItem.Help, 500, 5, 0, helps = HelpEngine.MAX))
        assertEquals(PurchaseResult.NotEnoughGems(10), ShopEngine.canBuy(ShopItem.Help, 40, 5, 0, helps = 1))
        assertNull(ShopEngine.canBuy(ShopItem.Help, 50, 5, 0, helps = 1))
        // Recarregar corações vale com meio coração faltando.
        assertTrue(ShopEngine.canBuy(ShopItem.RefillHearts, 500, 4, 0) == null)
    }
}
