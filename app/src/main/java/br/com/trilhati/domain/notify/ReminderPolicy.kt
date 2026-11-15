package br.com.trilhati.domain.notify

import br.com.trilhati.domain.streak.StreakStatus
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

enum class ReminderKind { Daily, StreakRisk }

data class ReminderTrigger(val at: LocalDateTime, val kind: ReminderKind)

/** Mensagem pronta para virar notificação. */
data class ReminderMessage(val kind: ReminderKind, val title: String, val text: String)

/**
 * Quando e se avisar (PLAN.md §8): no horário escolhido (★ 19:00) se nenhuma lição foi feita no dia; às 21:30, só se há
 * ofensiva ativa e ainda nada foi feito. Avisos atrasados demais (aparelho desligado, economia de bateria) são descartados.
 */
object ReminderPolicy {
    val DEFAULT_TIME: LocalTime = LocalTime.of(19, 0)
    val RISK_TIME: LocalTime = LocalTime.of(21, 30)
    const val MAX_LATENESS_MINUTES = 90L

    /** Próximo disparo estritamente depois de [now]. Se o horário escolhido for 21:30, vale só o aviso de risco. */
    fun next(now: LocalDateTime, reminderTime: LocalTime): ReminderTrigger {
        val candidates = (0L..1L).flatMap { offset ->
            val day = now.toLocalDate().plusDays(offset)
            buildList {
                if (reminderTime != RISK_TIME) add(ReminderTrigger(day.atTime(reminderTime), ReminderKind.Daily))
                add(ReminderTrigger(day.atTime(RISK_TIME), ReminderKind.StreakRisk))
            }
        }
        return candidates.filter { it.at.isAfter(now) }.minBy { it.at }
    }

    /** Decide se o disparo [kind], previsto para [scheduledAt], deve virar notificação agora. */
    fun decide(
        kind: ReminderKind,
        now: LocalDateTime,
        scheduledAt: LocalDateTime,
        enabled: Boolean,
        lessonsToday: Int,
        streakStatus: StreakStatus,
        streakCount: Int,
        character: String,
        /** Ofensivas em dupla em risco: nome do amigo e dias juntos (só as que ainda dependem de você hoje). */
        pairsAtRisk: List<Pair<String, Int>> = emptyList(),
    ): ReminderMessage? {
        if (!enabled) return null
        if (Duration.between(scheduledAt, now).toMinutes() > MAX_LATENESS_MINUTES) return null
        if (now.toLocalDate() != scheduledAt.toLocalDate()) return null
        if (lessonsToday > 0) return null
        val dayIndex = now.toLocalDate().toEpochDay().toInt()
        return when (kind) {
            ReminderKind.Daily -> ReminderTexts.daily(character, dayIndex, streakCount)
            ReminderKind.StreakRisk -> {
                val pair = pairsAtRisk.filter { it.second > 0 }.maxByOrNull { it.second }
                when {
                    streakStatus == StreakStatus.AtRisk && streakCount > 0 ->
                        ReminderTexts.risk(character, dayIndex, streakCount).let { msg ->
                            if (pair == null) msg else msg.copy(text = msg.text + " A ofensiva com ${pair.first} (${pair.second} ${if (pair.second == 1) "dia" else "dias"}) também.")
                        }
                    pair != null -> ReminderTexts.pairRisk(pair.first, pair.second)
                    else -> null
                }
            }
        }
    }
}

/** Textos na voz do personagem escolhido, variando de um dia para o outro. */
object ReminderTexts {
    /** "o Guaiamum", "a Preguiça"... para montar as frases. */
    fun withArticle(character: String): String = when (character) {
        "Tartaruga" -> "a Tartaruga-marinha"
        "Tamandua" -> "o Tamanduá"
        "Preguica" -> "a Preguiça"
        else -> "o Guaiamum"
    }

    private fun cap(s: String) = s.replaceFirstChar { it.uppercase() }

    fun daily(character: String, dayIndex: Int, streak: Int): ReminderMessage {
        val who = withArticle(character)
        val options = listOf(
            "Hora da trilha!" to "${cap(who)} separou 8 questões fresquinhas para você. Leva uns 20 minutos.",
            "Bora estudar?" to "Uma lição por dia e a prova do Banestes fica menor. ${cap(who)} está esperando.",
            "Seu ritmo de prova te espera" to "Treine 3:00 por questão hoje. ${cap(who)} vai cronometrar.",
            "Só uma lição" to if (streak > 0) "Mantenha a ofensiva de $streak ${if (streak == 1) "dia" else "dias"}. ${cap(who)} acredita em você!" else "Comece uma ofensiva hoje. ${cap(who)} acredita em você!",
        )
        val (title, text) = options[Math.floorMod(dayIndex, options.size)]
        return ReminderMessage(ReminderKind.Daily, title, text)
    }

    fun pairRisk(friend: String, days: Int): ReminderMessage = ReminderMessage(
        ReminderKind.StreakRisk,
        "Ofensiva em dupla em risco!",
        "Você e $friend estão há $days ${if (days == 1) "dia" else "dias"} estudando juntos. Falta você hoje: faça uma lição antes da meia-noite.",
    )

    fun risk(character: String, dayIndex: Int, streak: Int): ReminderMessage {
        val who = withArticle(character)
        val days = "$streak ${if (streak == 1) "dia" else "dias"}"
        val options = listOf(
            "Sua ofensiva está em risco!" to "${cap(who)} está de olho na sua ofensiva de $days. Faça uma lição antes da meia-noite.",
            "Ainda dá tempo!" to "Uma lição rápida salva sua ofensiva de $days. ${cap(who)} já está roendo as unhas.",
        )
        val (title, text) = options[Math.floorMod(dayIndex, options.size)]
        return ReminderMessage(ReminderKind.StreakRisk, title, text)
    }
}
