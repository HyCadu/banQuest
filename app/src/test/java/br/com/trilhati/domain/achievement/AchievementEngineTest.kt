package br.com.trilhati.domain.achievement

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementEngineTest {
    @Test fun `ha 15 conquistas com ids unicos`() {
        assertEquals(15, AchievementEngine.all.size)
        assertEquals(15, AchievementEngine.all.map { it.id }.toSet().size)
    }

    @Test fun `nada desbloqueado no inicio`() {
        assertTrue(AchievementEngine.newlyUnlocked(AchievementInput(), emptySet()).isEmpty())
    }

    @Test fun `ofensivas desbloqueiam nos limites 3 7 14 30 60 100`() {
        val ids = { days: Int -> AchievementEngine.newlyUnlocked(AchievementInput(longestStreak = days), emptySet()).map { it.id }.toSet() }
        assertEquals(setOf("streak3"), ids(3))
        assertEquals(setOf("streak3", "streak7"), ids(7))
        assertEquals(6, ids(100).size)
    }

    @Test fun `xp 1k 5k 20k`() {
        val ids = { xp: Int -> AchievementEngine.newlyUnlocked(AchievementInput(totalXp = xp), emptySet()).map { it.id }.toSet() }
        assertEquals(setOf("xp1000"), ids(1_000))
        assertEquals(setOf("xp1000", "xp5000"), ids(5_000))
        assertEquals(3, ids(20_000).size)
    }

    @Test fun `nao repete conquista ja desbloqueada`() {
        val r = AchievementEngine.newlyUnlocked(AchievementInput(longestStreak = 7), setOf("streak3"))
        assertEquals(listOf("streak7"), r.map { it.id })
    }

    @Test fun `demais conquistas`() {
        val all = AchievementEngine.newlyUnlocked(
            AchievementInput(perfectLessons = 10, unitsCompleted = 1, pacedLessons = 10, reviewCorrect = 50, bestLeagueTier = AchievementEngine.GOLD_TIER),
            emptySet(),
        ).map { it.id }.toSet()
        assertEquals(setOf("perfect1", "perfect10", "unit1", "paced10", "review50", "gold"), all)
    }
}
