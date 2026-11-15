package br.com.trilhati.domain.social

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

/** Regras do lado do app para o social. Espelham as do servidor (supabase/migrations) para não enviar o que será recusado. */
object SocialRules {
    const val NAME_MIN = 2
    const val NAME_MAX = 24
    private val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    private val codeBody = Regex("^[A-Z2-9]{5}$")

    fun isValidEmail(email: String): Boolean = emailRegex.matches(email.trim())

    /** Apelido sem espaços sobrando. */
    fun normalizeName(name: String): String = name.trim().replace(Regex("\\s+"), " ")

    fun isValidName(name: String): Boolean = normalizeName(name).length in NAME_MIN..NAME_MAX

    /** Aceita "tri-abc23", "TRI ABC23", "abc23"… e devolve "TRI-ABC23"; `null` se não for um código possível. */
    fun normalizeFriendCode(input: String): String? {
        val compact = input.uppercase().filter { it.isLetterOrDigit() }
        val body = when {
            compact.startsWith("TRI") && compact.length == 8 -> compact.substring(3)
            compact.length == 5 -> compact
            else -> return null
        }
        return if (codeBody.matches(body)) "TRI-$body" else null
    }

    fun isValidCode(input: String): Boolean = normalizeFriendCode(input) != null
}

/** Limites anti-trapaça (iguais aos de `submit_session` no servidor). */
object SessionLimits {
    const val LESSON_MAX_XP = 150
    const val PRACTICE_MAX_XP = 120
    const val MIN_MS_PER_ANSWER = 2_000L
    const val MAX_DAYS_LATE = 7L
    const val MAX_ANSWERS = 30

    fun accepts(kind: String, xp: Int, answers: Int, durationMs: Long, day: LocalDate, today: LocalDate): Boolean {
        val maxXp = when (kind) {
            "lesson" -> LESSON_MAX_XP
            "practice" -> PRACTICE_MAX_XP
            else -> return false
        }
        return xp in 0..maxXp &&
            answers in 1..MAX_ANSWERS &&
            durationMs >= answers * MIN_MS_PER_ANSWER &&
            !day.isAfter(today) && !day.isBefore(today.minusDays(MAX_DAYS_LATE))
    }
}

enum class InitialSync { UploadLocal, DownloadCloud, AskUser }

/**
 * Primeira entrada num aparelho: o que fazer com o progresso local e o da nuvem.
 * [sameTotals]: os dois têm o mesmo XP e as mesmas lições (ex.: backup do Android restaurado) — não há o que perguntar.
 */
object InitialSyncRule {
    fun decide(localHasProgress: Boolean, cloudExists: Boolean, sameTotals: Boolean = false): InitialSync = when {
        !cloudExists -> InitialSync.UploadLocal
        !localHasProgress || sameTotals -> InitialSync.DownloadCloud
        else -> InitialSync.AskUser
    }
}

/** "Pódio da semana": terminar entre os 3 primeiros do ranking geral, com pelo menos 3 pessoas que estudaram. */
object PodiumRule {
    const val MIN_PARTICIPANTS = 3

    fun earned(pos: Int?, participants: Int, xp: Int): Boolean =
        pos != null && pos in 1..3 && participants >= MIN_PARTICIPANTS && xp > 0
}

/** Semana do ranking: de segunda a domingo, virando à meia-noite no fuso do app (igual a `week_start_brt` no servidor). */
object RankingWeek {
    fun start(day: LocalDate): LocalDate = day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    /** Instante (ms) em que a semana de [day] termina: a próxima segunda, 00:00, em [zone]. */
    fun endMs(day: LocalDate, zone: ZoneId): Long = start(day).plusWeeks(1).atStartOfDay(zone).toInstant().toEpochMilli()

    /** "2d 5h", "3h 12min", "45min". */
    fun remainingLabel(ms: Long): String {
        val totalMinutes = ms.coerceAtLeast(0) / 60_000
        val days = totalMinutes / (60 * 24)
        val hours = (totalMinutes / 60) % 24
        val minutes = totalMinutes % 60
        return when {
            days > 0 -> "${days}d ${hours}h"
            hours > 0 -> "${hours}h ${minutes}min"
            else -> "${minutes}min"
        }
    }
}

/** Situação de hoje numa ofensiva em dupla. */
enum class PairToday { BothDone, WaitingMe, WaitingFriend, NobodyYet }

/** Ofensiva em dupla: conta os dias em que os DOIS estudaram (o servidor calcula o número; aqui só a leitura de hoje). */
object PairStreakRule {
    fun today(meToday: Boolean, friendToday: Boolean): PairToday = when {
        meToday && friendToday -> PairToday.BothDone
        friendToday -> PairToday.WaitingMe
        meToday -> PairToday.WaitingFriend
        else -> PairToday.NobodyYet
    }

    /** A dupla perde a ofensiva hoje se você não estudar (vale avisar). */
    fun atRiskForMe(pairStreak: Int, meToday: Boolean): Boolean = pairStreak > 0 && !meToday
}

/** Textos curtos (Snackbar) para as respostas das funções de amizade do servidor. */
object SocialMessages {
    fun request(result: String): String = when (result) {
        "requested" -> "Pedido de amizade enviado!"
        "accepted" -> "Vocês agora são amigos!"
        "already_friends" -> "Vocês já são amigos."
        "not_found" -> "Essa pessoa não está mais no ranking geral."
        "self" -> "Esse é você!"
        else -> "Não deu certo agora. Tente de novo."
    }

    fun respond(result: String, accepted: Boolean): String = when (result) {
        "accepted" -> "Pedido aceito. Agora vocês têm uma ofensiva em dupla!"
        "declined" -> "Pedido recusado."
        "not_found" -> "Esse pedido não existe mais."
        else -> if (accepted) "Não consegui aceitar agora." else "Não consegui recusar agora."
    }

    fun addByCode(result: String, name: String?): String = when (result) {
        "added" -> "Pronto! ${name ?: "Seu amigo"} agora é seu amigo."
        "already_friends" -> "Você e ${name ?: "essa pessoa"} já são amigos."
        "not_found" -> "Nenhum aluno com esse código. Confira as letras."
        "self" -> "Esse é o seu próprio código."
        else -> "Não deu certo agora. Tente de novo."
    }

    const val INVALID_CODE = "Código inválido. Ele tem o formato TRI-XXXXX."
}
