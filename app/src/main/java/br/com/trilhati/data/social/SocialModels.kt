package br.com.trilhati.data.social

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// Espelham o JSON devolvido pelas funções de supabase/migrations/0001_social.sql.

@Serializable
data class RemoteProfile(
    val id: String,
    @SerialName("display_name") val displayName: String,
    val mascot: String,
    @SerialName("friend_code") val friendCode: String,
    @SerialName("show_in_global") val showInGlobal: Boolean = true,
    val streak: Int = 0,
    @SerialName("streak_longest") val streakLongest: Int = 0,
    @SerialName("last_study_day") val lastStudyDay: String? = null,
)

/** Linha do ranking da semana. [friendState]: me, friend, outgoing, incoming ou none. */
@Serializable
data class RankingRow(
    @SerialName("user_id") val userId: String,
    @SerialName("display_name") val displayName: String,
    val mascot: String,
    val xp: Int,
    val streak: Int,
    val pos: Int,
    @SerialName("friend_state") val friendState: String,
) {
    val isMe: Boolean get() = friendState == "me"
}

@Serializable
data class FriendRow(
    @SerialName("user_id") val userId: String,
    @SerialName("display_name") val displayName: String,
    val mascot: String,
    val streak: Int,
    @SerialName("week_xp") val weekXp: Int,
    @SerialName("pair_streak") val pairStreak: Int,
    @SerialName("me_today") val meToday: Boolean,
    @SerialName("friend_today") val friendToday: Boolean,
)

@Serializable
data class FriendRequestRow(
    @SerialName("request_id") val requestId: String,
    @SerialName("from_id") val fromId: String,
    @SerialName("display_name") val displayName: String,
    val mascot: String,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class LastWeekResult(val pos: Int? = null, val xp: Int = 0, val participants: Int = 0)

/** Tudo que a tela social mostra, vindo de uma chamada só (`social_overview`). Fica em cache para ver offline. */
@Serializable
data class SocialOverview(
    val profile: RemoteProfile? = null,
    @SerialName("week_start") val weekStart: String,
    val today: String,
    val global: List<RankingRow> = emptyList(),
    @SerialName("friends_ranking") val friendsRanking: List<RankingRow> = emptyList(),
    val friends: List<FriendRow> = emptyList(),
    val requests: List<FriendRequestRow> = emptyList(),
    @SerialName("last_week") val lastWeek: LastWeekResult? = null,
)

@Serializable
data class SubmitResult(val ok: Boolean, val duplicate: Boolean = false, val reason: String? = null)

@Serializable
data class AddByCodeResult(val result: String, @SerialName("display_name") val displayName: String? = null)

@Serializable
data class PushResult(
    val ok: Boolean,
    val version: Long? = null,
    val reason: String? = null,
    @SerialName("server_version") val serverVersion: Long? = null,
    @SerialName("server_device") val serverDevice: String? = null,
    @SerialName("server_updated_at") val serverUpdatedAt: String? = null,
)

@Serializable
data class RemoteSnapshot(
    val version: Long,
    @SerialName("device_id") val deviceId: String,
    @SerialName("updated_at") val updatedAt: String,
    val payload: JsonElement,
)

/** Sessão de estudo a enviar (vem da fila local). */
data class SessionUpload(
    val id: String,
    val day: String,
    val kind: String,
    val xp: Int,
    val answers: Int,
    val durationMs: Long,
    val streak: Int,
    val streakLongest: Int,
    val lastStudyDay: String?,
)
