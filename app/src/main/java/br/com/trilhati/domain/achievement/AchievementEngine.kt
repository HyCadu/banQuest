package br.com.trilhati.domain.achievement

/** Dados necessários para avaliar as conquistas. */
data class AchievementInput(
    val longestStreak: Int = 0,
    val totalXp: Int = 0,
    val perfectLessons: Int = 0,
    val unitsCompleted: Int = 0,
    val pacedLessons: Int = 0,
    val reviewCorrect: Int = 0,
    val podiums: Int = 0,
)

data class AchievementDef(
    val id: String,
    val title: String,
    val description: String,
    val rewardGems: Int,
    val unlocked: (AchievementInput) -> Boolean,
)

/** Conquistas (PLAN.md §7, ≈15). */
object AchievementEngine {
    private fun streak(days: Int, gems: Int) = AchievementDef(
        "streak$days", "Ofensiva de $days dias", "Estude $days dias seguidos.", gems,
    ) { it.longestStreak >= days }

    private fun xp(total: Int, label: String, gems: Int) = AchievementDef(
        "xp$total", "$label XP", "Acumule $label XP no total.", gems,
    ) { it.totalXp >= total }

    val all: List<AchievementDef> = listOf(
        streak(3, 20), streak(7, 40), streak(14, 60), streak(30, 100), streak(60, 150), streak(100, 250),
        xp(1_000, "1 mil", 30), xp(5_000, "5 mil", 80), xp(20_000, "20 mil", 200),
        AchievementDef("perfect1", "Sem erros!", "Conclua uma lição perfeita.", 20) { it.perfectLessons >= 1 },
        AchievementDef("perfect10", "Mestre da perfeição", "Conclua 10 lições perfeitas.", 80) { it.perfectLessons >= 10 },
        AchievementDef("unit1", "Unidade concluída", "Conclua todas as lições de uma unidade.", 60) { it.unitsCompleted >= 1 },
        AchievementDef("paced10", "No ritmo da prova", "Termine 10 lições com média de até 3:00 por questão.", 80) { it.pacedLessons >= 10 },
        AchievementDef("review50", "Caçador de erros", "Acerte 50 questões na revisão de erros.", 60) { it.reviewCorrect >= 50 },
        AchievementDef("podium", "Pódio da semana", "Termine uma semana entre os 3 primeiros do ranking geral.", 100) { it.podiums >= 1 },
    )

    fun byId(id: String): AchievementDef? = all.firstOrNull { it.id == id }

    fun newlyUnlocked(input: AchievementInput, alreadyUnlocked: Set<String>): List<AchievementDef> =
        all.filter { it.id !in alreadyUnlocked && it.unlocked(input) }
}
