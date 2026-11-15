package br.com.trilhati.feature.ranking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.trilhati.data.social.FriendRow
import br.com.trilhati.data.social.FriendRequestRow
import br.com.trilhati.data.social.LastWeekResult
import br.com.trilhati.data.social.RankingRow
import br.com.trilhati.data.social.SocialOverview
import br.com.trilhati.domain.progress.QuestProgress
import br.com.trilhati.domain.quest.QuestEngine
import br.com.trilhati.domain.social.PairStreakRule
import br.com.trilhati.domain.social.PairToday
import br.com.trilhati.domain.social.PodiumRule
import br.com.trilhati.domain.social.RankingWeek
import br.com.trilhati.domain.time.APP_ZONE
import br.com.trilhati.feature.app.AppActions
import br.com.trilhati.feature.app.AppState
import br.com.trilhati.feature.app.SocialUi
import br.com.trilhati.ui.components.ChunkyButton
import br.com.trilhati.ui.components.ChunkyStyle
import br.com.trilhati.ui.components.GameProgressBar
import br.com.trilhati.ui.components.PillChoice
import br.com.trilhati.ui.components.TrilhaDialog
import br.com.trilhati.ui.icons.GameIcon
import br.com.trilhati.ui.icons.GameIconView
import br.com.trilhati.ui.mascot.Mascot
import br.com.trilhati.ui.mascot.MascotCharacter
import br.com.trilhati.ui.mascot.MascotPose
import br.com.trilhati.ui.theme.Spacing
import br.com.trilhati.ui.theme.Trilha

/** Qual lista do ranking está aberta. */
enum class RankingScope { Global, Friends }

/** Aba Ranking: missões do dia, ranking da semana (geral e amigos), pedidos, ofensivas em dupla e código de amigo. */
@Composable
fun RankingContent(
    state: AppState,
    social: SocialUi,
    nowMs: Long,
    actions: AppActions,
    modifier: Modifier = Modifier,
    initialScope: RankingScope = RankingScope.Global,
    animated: Boolean = true,
) {
    val c = Trilha.colors
    var scope by rememberSaveable { mutableStateOf(initialScope) }
    var confirmRemove by remember { mutableStateOf<FriendRow?>(null) }
    val overview = social.overview

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = Spacing.screen, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GameIconView(GameIcon.Trophy, size = 52.dp)
            Column(Modifier.weight(1f)) {
                Text("Ranking da semana", Modifier.semantics { heading() }, style = MaterialTheme.typography.headlineMedium, color = c.primaryText)
                val subtitle = if (nowMs > 0) {
                    "Termina em ${RankingWeek.remainingLabel(RankingWeek.endMs(state.today, APP_ZONE) - nowMs)} · conta o XP de segunda a domingo"
                } else {
                    "Conta o XP de segunda a domingo"
                }
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = c.inkSoft)
            }
        }
        SyncStatus(social, nowMs, actions.refreshSocial)
        QuestsCard(state.quests, actions)
        overview?.lastWeek?.let { LastWeekBanner(it) }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PillChoice("Geral", scope == RankingScope.Global, { scope = RankingScope.Global }, Modifier.weight(1f))
            val friendsLabel = overview?.friends?.size?.takeIf { it > 0 }?.let { "Amigos ($it)" } ?: "Amigos"
            val pending = overview?.requests?.size ?: 0
            PillChoice(if (pending > 0) "$friendsLabel · $pending novo" else friendsLabel, scope == RankingScope.Friends, { scope = RankingScope.Friends }, Modifier.weight(1f))
        }

        if (overview == null) {
            OfflineCard(social, state, actions, animated)
        } else {
            when (scope) {
                RankingScope.Global -> GlobalRanking(overview, social, actions, onOpenFriends = { scope = RankingScope.Friends })
                RankingScope.Friends -> FriendsArea(state, overview, social, actions, onRemove = { confirmRemove = it }, animated = animated)
            }
        }
    }

    confirmRemove?.let { friend ->
        TrilhaDialog(
            onDismiss = { confirmRemove = null },
            title = "Desfazer amizade?",
            content = {
                Text(
                    "Você e ${friend.displayName} deixam de ver um ao outro no ranking de amigos, e a ofensiva em dupla de " +
                        "${plural(friend.pairStreak, "dia", "dias")} acaba.",
                    style = MaterialTheme.typography.bodyMedium, color = c.ink,
                )
            },
            actions = {
                ChunkyButton("Desfazer", { actions.removeFriend(friend.userId); confirmRemove = null }, Modifier.fillMaxWidth(), ChunkyStyle.Error)
                ChunkyButton("Cancelar", { confirmRemove = null }, Modifier.fillMaxWidth(), ChunkyStyle.Neutral)
            },
        )
    }
}

internal fun plural(n: Int, one: String, many: String): String = "$n ${if (n == 1) one else many}"

// ---------------------------------------------------------------------------------------------------------- estado

@Composable
private fun SyncStatus(social: SocialUi, nowMs: Long, onRefresh: () -> Unit) {
    val c = Trilha.colors
    val (text, color) = when {
        social.refreshing -> "Atualizando…" to c.inkSoft
        social.lastError != null && social.overview != null -> "Sem conexão agora. Mostrando o ranking de ${ago(social.lastSyncAtMs, nowMs)}." to c.warningText
        social.lastError != null -> social.lastError to c.errorText
        social.lastSyncAtMs > 0 -> "Atualizado ${ago(social.lastSyncAtMs, nowMs)}" to c.inkSoft
        else -> "Ainda não atualizado" to c.inkSoft
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(Modifier.weight(1f)) {
            Text(text, style = MaterialTheme.typography.bodySmall, color = color)
            if (social.pendingUploads > 0) {
                Text(
                    "${plural(social.pendingUploads, "sessão esperando", "sessões esperando")} internet para contar no ranking.",
                    style = MaterialTheme.typography.bodySmall, color = c.inkSoft,
                )
            }
        }
        ChunkyButton("Atualizar", onRefresh, style = ChunkyStyle.Neutral, enabled = !social.refreshing, minHeight = 40.dp)
    }
}

private fun ago(thenMs: Long, nowMs: Long): String {
    if (thenMs <= 0 || nowMs <= 0) return "antes"
    val minutes = ((nowMs - thenMs) / 60_000).coerceAtLeast(0)
    return when {
        minutes < 1 -> "agora há pouco"
        minutes < 60 -> "há $minutes min"
        minutes < 60 * 24 -> "há ${minutes / 60} h"
        else -> "há ${plural((minutes / (60 * 24)).toInt(), "dia", "dias")}"
    }
}

@Composable
private fun LastWeekBanner(last: LastWeekResult) {
    val c = Trilha.colors
    val pos = last.pos ?: return
    if (last.xp <= 0) return
    val podium = PodiumRule.earned(last.pos, last.participants, last.xp)
    val text = if (podium) {
        "Semana passada: ${pos}º lugar entre ${last.participants}, com ${last.xp} XP. Pódio da semana!"
    } else {
        "Semana passada: ${pos}º lugar entre ${last.participants}, com ${last.xp} XP."
    }
    Row(
        Modifier.fillMaxWidth().background(if (podium) c.warningSoft else c.surfaceAlt, RoundedCornerShape(16.dp)).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (podium) GameIconView(GameIcon.Crown, size = 28.dp)
        Text(text, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = if (podium) c.warningText else c.ink)
    }
}

@Composable
private fun OfflineCard(social: SocialUi, state: AppState, actions: AppActions, animated: Boolean) {
    val c = Trilha.colors
    Card {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Mascot(MascotCharacter.of(state.snapshot.profile.character), MascotPose.Sleepy, Modifier.size(72.dp), animated = animated, decorative = true)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(if (social.refreshing) "Buscando o ranking…" else "Ranking ainda não carregado", style = MaterialTheme.typography.titleSmall, color = c.ink)
                Text(
                    "Conecte-se à internet para ver seus amigos. Pode estudar normalmente: suas lições entram no ranking quando a conexão voltar.",
                    style = MaterialTheme.typography.bodySmall, color = c.inkSoft,
                )
            }
        }
        ChunkyButton("Tentar de novo", actions.refreshSocial, Modifier.fillMaxWidth(), ChunkyStyle.Secondary, enabled = !social.refreshing, minHeight = 46.dp)
    }
}

// ---------------------------------------------------------------------------------------------------------- geral

@Composable
private fun GlobalRanking(overview: SocialOverview, social: SocialUi, actions: AppActions, onOpenFriends: () -> Unit) {
    val c = Trilha.colors
    if (!social.showInGlobal) {
        Notice("Você está oculto do ranking geral: só seus amigos te veem. Dá para mudar em Configurações › Conta.")
    }
    Text(
        "Toque em Adicionar para mandar um pedido de amizade. Quem não aparece aqui pode ser adicionado pelo código, na lista Amigos.",
        style = MaterialTheme.typography.bodySmall, color = c.inkSoft,
    )
    RankingList(overview.global) { row ->
        when (row.friendState) {
            "none" -> SmallAction("+ Adicionar", { actions.requestFriend(row.userId) }, c.secondaryText)
            "outgoing" -> Tag("Pedido enviado", c.inkSoft)
            "incoming" -> {
                val request = overview.requests.firstOrNull { it.fromId == row.userId }
                if (request != null) {
                    SmallAction("Aceitar", { actions.respondRequest(request.requestId, true) }, c.success, filled = true)
                } else {
                    Tag("Quer ser amigo", c.successText)
                }
            }
            "friend" -> Tag("Amigo", c.successText)
            else -> Unit
        }
    }
    if (overview.global.count { !it.isMe } == 0) {
        Card {
            Text("Por enquanto só tem você aqui.", style = MaterialTheme.typography.titleSmall, color = c.ink)
            Text("Mande seu código de amigo para a turma instalar o app e entrar na disputa.", style = MaterialTheme.typography.bodySmall, color = c.inkSoft)
            ChunkyButton("Ver meu código", onOpenFriends, Modifier.fillMaxWidth(), ChunkyStyle.Secondary, minHeight = 44.dp)
        }
    }
}

@Composable
private fun RankingList(rows: List<RankingRow>, trailing: @Composable (RankingRow) -> Unit) {
    val c = Trilha.colors
    Column(
        Modifier.fillMaxWidth().background(c.surface, RoundedCornerShape(20.dp)).border(2.dp, c.outline, RoundedCornerShape(20.dp)).padding(vertical = 6.dp),
    ) {
        rows.forEach { row -> RankRow(row) { trailing(row) } }
    }
}

@Composable
private fun RankRow(row: RankingRow, trailing: @Composable () -> Unit) {
    val c = Trilha.colors
    val podium = row.pos in 1..3 && row.xp > 0
    Row(
        Modifier.fillMaxWidth().background(if (row.isMe) c.primarySoft else Color.Transparent).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(30.dp).background(if (podium) c.warningSoft else c.surfaceAlt, CircleShape), contentAlignment = Alignment.Center) {
            Text(row.pos.toString(), style = MaterialTheme.typography.labelMedium, color = if (podium) c.warningText else c.inkSoft)
        }
        Spacer(Modifier.width(8.dp))
        Mascot(MascotCharacter.of(row.mascot), MascotPose.Idle, Modifier.size(40.dp), animated = false, decorative = true)
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(
                if (row.isMe) "${row.displayName} (você)" else row.displayName,
                style = if (row.isMe) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyLarge, color = c.ink,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${row.xp} XP", style = MaterialTheme.typography.labelLarge, color = c.inkSoft)
                if (row.streak > 0) {
                    GameIconView(GameIcon.Flame, size = 16.dp)
                    Text(plural(row.streak, "dia", "dias"), style = MaterialTheme.typography.labelMedium, color = c.inkSoft)
                }
            }
        }
        trailing()
    }
}

// ---------------------------------------------------------------------------------------------------------- amigos

@Composable
private fun FriendsArea(
    state: AppState,
    overview: SocialOverview,
    social: SocialUi,
    actions: AppActions,
    onRemove: (FriendRow) -> Unit,
    animated: Boolean,
) {
    val c = Trilha.colors
    if (overview.requests.isNotEmpty()) {
        SectionTitle("Pedidos de amizade")
        overview.requests.forEach { RequestCard(it, actions) }
    }
    if (overview.friends.isEmpty()) {
        Card {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Mascot(MascotCharacter.of(state.snapshot.profile.character), MascotPose.Happy, Modifier.size(72.dp), animated = animated, decorative = true)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Estudar junto rende mais!", style = MaterialTheme.typography.titleSmall, color = c.ink)
                    Text(
                        "Mande seu código ou adicione alguém do ranking geral. Com amigos você ganha o ranking da turma e uma ofensiva em dupla com cada um.",
                        style = MaterialTheme.typography.bodySmall, color = c.inkSoft,
                    )
                }
            }
        }
    } else {
        SectionTitle("Ranking dos amigos")
        RankingList(overview.friendsRanking) { }
        SectionTitle("Ofensivas em dupla")
        Text("Cada dia em que vocês dois concluem pelo menos uma lição soma 1 na ofensiva da dupla.", style = MaterialTheme.typography.bodySmall, color = c.inkSoft)
        val fresh = overview.today == state.today.toString()
        overview.friends.forEach { friend ->
            PairCard(
                friend,
                meToday = (fresh && friend.meToday) || state.studiedToday,
                friendToday = fresh && friend.friendToday,
                onRemove = { onRemove(friend) },
            )
        }
    }
    SectionTitle("Adicionar amigos")
    social.friendCode?.let { code -> MyCodeCard(code, actions) }
    AddByCodeCard(actions)
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, Modifier.padding(top = 4.dp).semantics { heading() }, style = MaterialTheme.typography.titleLarge, color = Trilha.colors.ink)
}

@Composable
private fun RequestCard(request: FriendRequestRow, actions: AppActions) {
    val c = Trilha.colors
    Card(highlight = c.success) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Mascot(MascotCharacter.of(request.mascot), MascotPose.Happy, Modifier.size(44.dp), animated = false, decorative = true)
            Text("${request.displayName} quer ser seu amigo", Modifier.weight(1f), style = MaterialTheme.typography.titleSmall, color = c.ink)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChunkyButton("Aceitar", { actions.respondRequest(request.requestId, true) }, Modifier.weight(1f), ChunkyStyle.Success, minHeight = 44.dp)
            ChunkyButton("Recusar", { actions.respondRequest(request.requestId, false) }, Modifier.weight(1f), ChunkyStyle.Neutral, minHeight = 44.dp)
        }
    }
}

@Composable
private fun PairCard(friend: FriendRow, meToday: Boolean, friendToday: Boolean, onRemove: () -> Unit) {
    val c = Trilha.colors
    val today = PairStreakRule.today(meToday, friendToday)
    val atRisk = PairStreakRule.atRiskForMe(friend.pairStreak, meToday)
    val (status, statusColor) = when (today) {
        PairToday.BothDone -> "Vocês dois já estudaram hoje ✓" to c.successText
        PairToday.WaitingMe -> "${friend.displayName} já estudou hoje. Falta você!" to c.warningText
        PairToday.WaitingFriend -> "Você já estudou. Falta ${friend.displayName}." to c.inkSoft
        PairToday.NobodyYet -> (if (atRisk) "Estudem hoje para não perder a ofensiva da dupla." else "Nenhum dos dois estudou hoje ainda.") to
            (if (atRisk) c.warningText else c.inkSoft)
    }
    Card(highlight = if (atRisk) c.warning else null) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Mascot(MascotCharacter.of(friend.mascot), MascotPose.Idle, Modifier.size(48.dp), animated = false, decorative = true)
            Column(Modifier.weight(1f)) {
                Text(friend.displayName, style = MaterialTheme.typography.titleSmall, color = c.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${friend.weekXp} XP na semana · ofensiva própria de ${plural(friend.streak, "dia", "dias")}",
                    style = MaterialTheme.typography.bodySmall, color = c.inkSoft,
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (friend.pairStreak > 0) {
                    GameIconView(GameIcon.Flame, size = 30.dp)
                } else {
                    GameIconView(GameIcon.Flame, size = 30.dp, colors = br.com.trilhati.ui.icons.IconColors.flat(c.locked, c.lockedEdge))
                }
                Text(friend.pairStreak.toString(), style = MaterialTheme.typography.titleMedium, color = c.ink)
            }
        }
        Text(
            if (friend.pairStreak > 0) "Ofensiva da dupla: ${plural(friend.pairStreak, "dia", "dias")} seguidos" else "Ofensiva da dupla: comece hoje!",
            style = MaterialTheme.typography.labelLarge, color = c.ink,
        )
        Text(status, style = MaterialTheme.typography.bodySmall, color = statusColor)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { SmallAction("Desfazer amizade", onRemove, c.inkSoft) }
    }
}

@Composable
private fun MyCodeCard(code: String, actions: AppActions) {
    val c = Trilha.colors
    Card {
        Text("Seu código de amigo", style = MaterialTheme.typography.titleSmall, color = c.ink)
        Text(code, style = MaterialTheme.typography.headlineMedium, color = c.primaryText)
        Text(
            "Quem digitar esse código vira seu amigo na hora, mesmo que um dos dois esteja oculto do ranking geral.",
            style = MaterialTheme.typography.bodySmall, color = c.inkSoft,
        )
        ChunkyButton("Compartilhar código", { actions.shareFriendCode(code) }, Modifier.fillMaxWidth(), ChunkyStyle.Secondary, minHeight = 46.dp)
    }
}

@Composable
private fun AddByCodeCard(actions: AppActions) {
    val c = Trilha.colors
    var code by rememberSaveable { mutableStateOf("") }
    Card {
        Text("Tem o código de alguém?", style = MaterialTheme.typography.titleSmall, color = c.ink)
        OutlinedTextField(
            code, { code = it.take(12) }, Modifier.fillMaxWidth(), label = { Text("Código (ex.: TRI-AB2CD)") }, singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Done),
        )
        ChunkyButton(
            "Adicionar amigo", { actions.addFriendByCode(code); code = "" }, Modifier.fillMaxWidth(), ChunkyStyle.Primary,
            enabled = code.isNotBlank(), minHeight = 46.dp,
        )
    }
}

// ---------------------------------------------------------------------------------------------------------- peças

@Composable
private fun Card(highlight: Color? = null, content: @Composable () -> Unit) {
    val c = Trilha.colors
    Column(
        Modifier.fillMaxWidth().background(c.surface, RoundedCornerShape(20.dp)).border(2.dp, highlight ?: c.outline, RoundedCornerShape(20.dp)).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) { content() }
}

@Composable
private fun Notice(text: String) {
    val c = Trilha.colors
    Text(text, Modifier.fillMaxWidth().background(c.surfaceAlt, RoundedCornerShape(16.dp)).padding(14.dp), style = MaterialTheme.typography.bodyMedium, color = c.ink)
}

/** Ação pequena dentro de uma linha (adicionar, aceitar, desfazer), com área de toque de 48 dp. */
@Composable
private fun SmallAction(text: String, onClick: () -> Unit, color: Color, filled: Boolean = false) {
    val shape = RoundedCornerShape(50)
    Text(
        text,
        Modifier.padding(start = 6.dp).minimumInteractiveComponentSize().clip(shape)
            .then(if (filled) Modifier.background(color, shape) else Modifier.border(2.dp, color, shape))
            .clickable(role = Role.Button, onClick = onClick).padding(horizontal = 14.dp, vertical = 7.dp),
        style = MaterialTheme.typography.labelLarge, color = if (filled) Trilha.colors.onSuccess else color, maxLines = 1,
    )
}

@Composable
private fun Tag(text: String, color: Color) {
    Text(text, Modifier.padding(start = 6.dp), style = MaterialTheme.typography.labelMedium, color = color)
}

/** Missões do dia num cartão compacto (o ranking é o assunto principal da aba). */
@Composable
private fun QuestsCard(quests: List<QuestProgress>, actions: AppActions) {
    val c = Trilha.colors
    val defs = quests.mapNotNull { q -> QuestEngine.byId(q.questId)?.let { q to it } }
    if (defs.isEmpty()) return
    val done = defs.count { (q, d) -> QuestEngine.isComplete(d, q.progress) }
    val claimable = defs.any { (q, d) -> QuestEngine.isComplete(d, q.progress) && !q.claimed }
    Column(
        Modifier.fillMaxWidth().background(c.surface, RoundedCornerShape(20.dp))
            .border(2.dp, if (claimable) c.success else c.outline, RoundedCornerShape(20.dp)).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Missões do dia", Modifier.weight(1f).semantics { heading() }, style = MaterialTheme.typography.titleMedium, color = c.ink)
            Text("$done de ${defs.size} feitas", style = MaterialTheme.typography.labelMedium, color = c.inkSoft)
        }
        defs.forEach { (quest, def) ->
            val complete = QuestEngine.isComplete(def, quest.progress)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row {
                        Text(def.title, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = c.ink)
                        Text("${quest.progress.coerceAtMost(def.target)}/${def.target}", style = MaterialTheme.typography.labelMedium, color = c.inkSoft)
                    }
                    GameProgressBar(quest.progress.toFloat() / def.target, color = if (complete) c.success else c.secondary, height = 8.dp)
                }
                when {
                    quest.claimed -> Text("✓ +${def.rewardGems}", Modifier.width(112.dp), style = MaterialTheme.typography.labelLarge, color = c.successText)
                    complete -> ChunkyButton(
                        "+${def.rewardGems}", { actions.claimQuest(quest.questId) }, Modifier.width(112.dp), ChunkyStyle.Success,
                        icon = GameIcon.Gem, minHeight = 38.dp,
                    )
                    else -> Row(Modifier.width(112.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        GameIconView(GameIcon.Gem, size = 18.dp)
                        Text("+${def.rewardGems}", style = MaterialTheme.typography.labelLarge, color = c.inkSoft)
                    }
                }
            }
        }
    }
}
