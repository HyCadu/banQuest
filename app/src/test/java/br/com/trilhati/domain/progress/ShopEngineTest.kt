package br.com.trilhati.domain.progress

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShopEngineTest {
    @Test fun `precos do plano`() {
        assertEquals(200, ShopItem.StreakFreeze.priceGems)
        assertEquals(350, ShopItem.RefillHearts.priceGems)
    }

    @Test fun `compra liberada com gemas suficientes`() {
        assertNull(ShopEngine.canBuy(ShopItem.StreakFreeze, gems = 200, hearts = 5, freezes = 0))
        assertNull(ShopEngine.canBuy(ShopItem.RefillHearts, gems = 350, hearts = 2, freezes = 0))
    }

    @Test fun `falta de gemas informa quantas faltam`() {
        assertEquals(PurchaseResult.NotEnoughGems(50), ShopEngine.canBuy(ShopItem.StreakFreeze, 150, 5, 0))
    }

    @Test fun `nao vende o que ja esta cheio`() {
        assertEquals(PurchaseResult.AlreadyFull, ShopEngine.canBuy(ShopItem.RefillHearts, 999, 5, 0))
        assertEquals(PurchaseResult.AlreadyFull, ShopEngine.canBuy(ShopItem.StreakFreeze, 999, 5, 2))
    }

    @Test fun `bau diario esta entre 10 e 40 e e igual no mesmo dia`() {
        val d = LocalDate.of(2026, 11, 15)
        assertEquals(ShopEngine.dailyChestGems(d), ShopEngine.dailyChestGems(d))
        (0 until 60).forEach {
            val g = ShopEngine.dailyChestGems(d.plusDays(it.toLong()))
            assertTrue(g in 10..40 && g % 10 == 0)
        }
    }
}
