package br.com.trilhati.domain.progress

import br.com.trilhati.domain.hearts.HeartsEngine
import br.com.trilhati.domain.streak.StreakEngine
import br.com.trilhati.domain.util.SplitMix
import java.time.LocalDate

enum class ShopItem(val priceGems: Int) {
    StreakFreeze(200),
    RefillHearts(350),
}

sealed interface PurchaseResult {
    data class Success(val gemsLeft: Int) : PurchaseResult
    data class NotEnoughGems(val missing: Int) : PurchaseResult
    data object AlreadyFull : PurchaseResult
}

/** Regras da loja e dos baús (PLAN.md §7). */
object ShopEngine {
    const val LESSON_GEMS = 5
    const val PERFECT_GEMS = 5
    const val DAILY_GOAL_GEMS = 20
    const val UNIT_CHEST_GEMS = 60

    /** Pode comprar? Devolve o motivo da recusa, se houver. */
    fun canBuy(item: ShopItem, gems: Int, hearts: Int, freezes: Int): PurchaseResult? = when {
        item == ShopItem.RefillHearts && hearts >= HeartsEngine.MAX -> PurchaseResult.AlreadyFull
        item == ShopItem.StreakFreeze && freezes >= StreakEngine.MAX_FREEZES -> PurchaseResult.AlreadyFull
        gems < item.priceGems -> PurchaseResult.NotEnoughGems(item.priceGems - gems)
        else -> null
    }

    /** Gemas do baú diário: de 10 a 40, sempre o mesmo valor para o mesmo dia. */
    fun dailyChestGems(day: LocalDate): Int = 10 + SplitMix(day.toEpochDay() * 31L + 7).nextInt(4) * 10
}
