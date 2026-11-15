package br.com.trilhati.domain.quest

import br.com.trilhati.domain.util.SplitMix
import java.time.LocalDate

/** O que cada missão mede. */
enum class QuestMetric { XpEarned, LessonsCompleted, BestCombo, PacedLessons, PerfectLessons, CorrectAnswers, PracticeSessions }

data class QuestDef(
    val id: String,
    val title: String,
    val metric: QuestMetric,
    val target: Int,
    val rewardGems: Int,
)

/** O que aconteceu numa sessão, para atualizar as missões. */
data class QuestEvent(
    val xp: Int = 0,
    val lessons: Int = 0,
    val bestCombo: Int = 0,
    val pacedLessons: Int = 0,
    val perfectLessons: Int = 0,
    val correctAnswers: Int = 0,
    val practiceSessions: Int = 0,
)

/** Missões diárias (PLAN.md §7): 3 por dia, sorteadas de forma determinística pela data. */
object QuestEngine {
    const val DAILY_COUNT = 3

    val pool: List<QuestDef> = listOf(
        QuestDef("xp100", "Ganhe 100 XP", QuestMetric.XpEarned, 100, 20),
        QuestDef("lessons2", "Conclua 2 lições", QuestMetric.LessonsCompleted, 2, 25),
        QuestDef("combo6", "Acerte 6 questões seguidas", QuestMetric.BestCombo, 6, 20),
        QuestDef("paced1", "Termine uma lição no ritmo de prova", QuestMetric.PacedLessons, 1, 25),
        QuestDef("perfect1", "Conclua uma lição sem errar", QuestMetric.PerfectLessons, 1, 30),
        QuestDef("correct20", "Acerte 20 questões", QuestMetric.CorrectAnswers, 20, 20),
        QuestDef("practice1", "Treine seus erros ou revise", QuestMetric.PracticeSessions, 1, 20),
    )

    fun questsFor(day: LocalDate): List<QuestDef> {
        val rng = SplitMix(day.toEpochDay() * 7919L + 13)
        return pool.shuffled(rng.asRandom()).take(DAILY_COUNT)
    }

    fun byId(id: String): QuestDef? = pool.firstOrNull { it.id == id }

    /** Novo progresso da missão depois do [event]. O combo é "melhor do dia"; as demais métricas somam. */
    fun apply(def: QuestDef, current: Int, event: QuestEvent): Int {
        val next = when (def.metric) {
            QuestMetric.XpEarned -> current + event.xp
            QuestMetric.LessonsCompleted -> current + event.lessons
            QuestMetric.BestCombo -> maxOf(current, event.bestCombo)
            QuestMetric.PacedLessons -> current + event.pacedLessons
            QuestMetric.PerfectLessons -> current + event.perfectLessons
            QuestMetric.CorrectAnswers -> current + event.correctAnswers
            QuestMetric.PracticeSessions -> current + event.practiceSessions
        }
        return next.coerceAtMost(def.target)
    }

    fun isComplete(def: QuestDef, progress: Int): Boolean = progress >= def.target
}
