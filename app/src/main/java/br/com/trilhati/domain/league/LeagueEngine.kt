package br.com.trilhati.domain.league

import br.com.trilhati.domain.util.SplitMix
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import kotlin.math.pow

enum class LeagueTier(val displayName: String) {
    Bronze("Bronze"), Prata("Prata"), Ouro("Ouro"), Safira("Safira"),
    Rubi("Rubi"), Esmeralda("Esmeralda"), Ametista("Ametista"), Diamante("Diamante");

    companion object {
        fun fromIndex(index: Int): LeagueTier = entries[index.coerceIn(0, entries.lastIndex)]
    }
}

data class LeagueEntry(val name: String, val xp: Int, val isUser: Boolean)

enum class LeagueOutcome { Promoted, Stayed, Relegated }

data class LeagueClose(val finalRank: Int, val outcome: LeagueOutcome, val newTier: LeagueTier)

/**
 * Ligas semanais (PLAN.md §7): 30 participantes (você + 29 bots fictícios). O XP dos bots é simulado de forma
 * determinística por semente (semana + índice), com curva realista ao longo da semana. A semana fecha na segunda 00:00.
 */
object LeagueEngine {
    const val SIZE = 30
    const val PROMOTE_TOP = 10
    const val RELEGATE_BOTTOM = 5

    private val NAMES = listOf(
        "Moqueca Master", "Pão de Queijo Dev", "Ana Compila", "Bia Query", "Caio Cache", "Duda Debug", "Edu Deploy",
        "Fê Framework", "Gabi Git", "Hugo Hash", "Iara Index", "João Join", "Kaká Kernel", "Lia Loop", "Malu Merge",
        "Nando Null", "Olívia Objeto", "Pedro Pipeline", "Quel Query", "Rafa Regex", "Sofi Stack", "Tiago Token",
        "Uli Unix", "Vivi View", "Wagner Web", "Xuxa XML", "Yuri YAML", "Zeca Zip", "Bruna Bit", "Davi Docker",
        "Elis Endpoint", "Fabi Fork", "Gui Garbage", "Heitor Heap", "Isa Interface", "Jade JSON", "Léo Lambda",
        "Mari Mutex", "Nina Node", "Otto Ótimo", "Paty Patch", "Rui Rollback", "Sara Schema", "Téo Thread",
        "Ully Upsert", "Vitor Vetor", "Wesley Widget", "Yasmin Yield", "Zélia Zero", "Cadu Commit",
    )

    fun weekStart(day: LocalDate): LocalDate = day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    /** Fração da semana já passada, de 0 (segunda 00:00) a 1 (domingo 24:00). */
    fun weekFraction(day: LocalDate, secondOfDay: Int): Double {
        val dayIndex = day.dayOfWeek.value - 1
        return ((dayIndex * 86_400.0) + secondOfDay) / (7 * 86_400.0)
    }

    /** XP de um bot numa [fraction] da semana: soma de dias completos + parte linear do dia atual. */
    fun botXp(weekStart: LocalDate, index: Int, tier: LeagueTier, fraction: Double): Int {
        val rng = SplitMix(weekStart.toEpochDay() * 131L + index)
        val ceiling = 900 + tier.ordinal * 150
        val weekly = 120 + rng.nextDouble().pow(1.6) * ceiling
        val weights = DoubleArray(7) { 0.25 + rng.nextDouble() }
        val total = weights.sum()
        val position = fraction.coerceIn(0.0, 1.0) * 7
        val fullDays = position.toInt().coerceAtMost(6)
        val partial = position - fullDays
        var acc = 0.0
        for (d in 0 until fullDays) acc += weights[d]
        acc += weights[fullDays] * partial
        return (weekly * acc / total).toInt()
    }

    fun botName(weekStart: LocalDate, index: Int): String {
        val order = NAMES.indices.toList().shuffled(SplitMix(weekStart.toEpochDay() * 17L + 5).asRandom())
        return NAMES[order[index % order.size]]
    }

    /** Classificação completa (30 entradas), da maior para a menor pontuação; empate desempata por nome. */
    fun standings(weekStart: LocalDate, tier: LeagueTier, fraction: Double, userName: String, userXp: Int): List<LeagueEntry> {
        val bots = (0 until SIZE - 1).map { LeagueEntry(botName(weekStart, it), botXp(weekStart, it, tier, fraction), false) }
        return (bots + LeagueEntry(userName, userXp, true))
            .sortedWith(compareByDescending<LeagueEntry> { it.xp }.thenBy { it.isUser }.thenBy { it.name })
    }

    fun rankOfUser(standings: List<LeagueEntry>): Int = standings.indexOfFirst { it.isUser } + 1

    /**
     * Fecha uma semana: top 10 sobem, últimos 5 descem. Quem fez 0 XP não sobe (liga é para quem estuda)
     * e ninguém desce da Bronze nem sobe da Diamante.
     */
    fun close(tier: LeagueTier, rank: Int, userXp: Int): LeagueClose {
        val promoted = rank <= PROMOTE_TOP && userXp > 0 && tier != LeagueTier.Diamante
        val relegated = rank > SIZE - RELEGATE_BOTTOM && tier != LeagueTier.Bronze
        return when {
            promoted -> LeagueClose(rank, LeagueOutcome.Promoted, LeagueTier.fromIndex(tier.ordinal + 1))
            relegated -> LeagueClose(rank, LeagueOutcome.Relegated, LeagueTier.fromIndex(tier.ordinal - 1))
            else -> LeagueClose(rank, LeagueOutcome.Stayed, tier)
        }
    }
}
