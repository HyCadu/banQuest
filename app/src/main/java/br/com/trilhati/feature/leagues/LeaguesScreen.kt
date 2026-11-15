package br.com.trilhati.feature.leagues

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.com.trilhati.domain.league.LeagueEngine
import br.com.trilhati.domain.league.LeagueTier
import br.com.trilhati.domain.progress.QuestProgress
import br.com.trilhati.domain.quest.QuestEngine
import br.com.trilhati.domain.time.APP_ZONE
import br.com.trilhati.feature.app.AppActions
import br.com.trilhati.feature.app.AppState
import br.com.trilhati.ui.components.ChunkyButton
import br.com.trilhati.ui.components.ChunkyStyle
import br.com.trilhati.ui.components.GameProgressBar
import br.com.trilhati.ui.icons.GameIcon
import br.com.trilhati.ui.icons.GameIconView
import br.com.trilhati.ui.icons.IconColors
import br.com.trilhati.ui.theme.Spacing
import br.com.trilhati.ui.theme.Trilha
import java.time.Instant

/** Cor de cada camada da liga (original; só identifica o degrau). */
fun LeagueTier.color(): Color = when (this) {
    LeagueTier.Bronze -> Color(0xFFD08A4E)
    LeagueTier.Prata -> Color(0xFFB4BDCB)
    LeagueTier.Ouro -> Color(0xFFFFC21A)
    LeagueTier.Safira -> Color(0xFF3B82F6)
    LeagueTier.Rubi -> Color(0xFFE5395F)
    LeagueTier.Esmeralda -> Color(0xFF22B573)
    LeagueTier.Ametista -> Color(0xFF9B6BFF)
    LeagueTier.Diamante -> Color(0xFF58DDF2)
}

@Composable
fun TierBadge(tier: LeagueTier, size: androidx.compose.ui.unit.Dp, modifier: Modifier = Modifier) {
    GameIconView(
        GameIcon.Gem, modifier, size,
        colors = IconColors(tier.color(), Color.White.copy(alpha = 0.55f), Color(0xFF2B1A5E)),
        contentDescription = "Liga ${tier.displayName}",
    )
}

/** Aba Ligas: missões do dia e a classificação semanal contra rivais fictícios. */
@Composable
fun LeaguesContent(state: AppState, nowMs: Long, actions: AppActions, modifier: Modifier = Modifier) {
    val c = Trilha.colors
    val league = state.snapshot.league
    val tier = LeagueTier.fromIndex(league.tier)
    val profile = state.snapshot.profile
    val minuteKey = nowMs / 60_000
    val weekEndMs = league.weekStart.plusWeeks(1).atStartOfDay(APP_ZONE).toInstant().toEpochMilli()
    val remainingMs = (weekEndMs - nowMs).coerceAtLeast(0)
    val standings = remember(league, minuteKey, profile.name) {
        val secondOfDay = if (nowMs == 0L) 0 else Instant.ofEpochMilli(nowMs).atZone(APP_ZONE).toLocalTime().toSecondOfDay()
        val fraction = LeagueEngine.weekFraction(state.today, secondOfDay)
        LeagueEngine.standings(league.weekStart, tier, fraction, profile.name, league.userXp)
    }
    val rank = LeagueEngine.rankOfUser(standings)

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = Spacing.screen, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Text("Missões do dia", style = MaterialTheme.typography.displaySmall, color = c.primaryText)
        state.quests.forEach { q -> QuestCard(q, onClaim = { actions.claimQuest(q.questId) }) }

        Spacer(Modifier.size(Spacing.xs))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TierBadge(tier, 56.dp)
            Column(Modifier.weight(1f)) {
                Text("Liga ${tier.displayName}", style = MaterialTheme.typography.headlineMedium, color = c.ink)
                Text(
                    "Você é o ${rank}º · ${league.userXp} XP · termina em ${formatRemaining(remainingMs)}",
                    style = MaterialTheme.typography.bodySmall, color = c.inkSoft,
                )
            }
        }
        state.snapshot.lastLeagueResult?.let { last ->
            if (last.weekStart == league.weekStart.minusWeeks(1)) LastWeekBanner(last.rank, last.outcome, tier)
        }
        Text(
            "Os 10 primeiros sobem de liga; os 5 últimos descem. Os rivais são simulados, sem conexão com ninguém.",
            style = MaterialTheme.typography.bodySmall, color = c.inkSoft,
        )
        Column(
            Modifier.fillMaxWidth().background(c.surface, RoundedCornerShape(20.dp)).border(2.dp, c.outline, RoundedCornerShape(20.dp)).padding(vertical = 6.dp),
        ) {
            standings.forEachIndexed { index, entry ->
                val position = index + 1
                if (position == LeagueEngine.PROMOTE_TOP + 1) ZoneLabel("Abaixo da zona de promoção", c.inkSoft)
                if (position == LeagueEngine.SIZE - LeagueEngine.RELEGATE_BOTTOM + 1) ZoneLabel("Zona de rebaixamento", c.errorText)
                StandingRow(position, entry.name, entry.xp, entry.isUser, tier)
            }
        }
    }
}

private fun formatRemaining(ms: Long): String {
    val totalMinutes = ms / 60_000
    val days = totalMinutes / (60 * 24)
    val hours = (totalMinutes / 60) % 24
    val minutes = totalMinutes % 60
    return if (days > 0) "${days}d ${hours}h" else if (hours > 0) "${hours}h ${minutes}min" else "${minutes}min"
}

@Composable
private fun ZoneLabel(text: String, color: Color) {
    Text(text.uppercase(), Modifier.padding(horizontal = 16.dp, vertical = 6.dp), style = MaterialTheme.typography.labelSmall, color = color)
}

@Composable
private fun StandingRow(position: Int, name: String, xp: Int, isUser: Boolean, tier: LeagueTier) {
    val c = Trilha.colors
    val promoted = position <= LeagueEngine.PROMOTE_TOP
    val relegated = position > LeagueEngine.SIZE - LeagueEngine.RELEGATE_BOTTOM && tier != LeagueTier.Bronze
    Row(
        Modifier.fillMaxWidth().background(if (isUser) c.primarySoft else Color.Transparent).padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(28.dp).background(if (promoted) c.successSoft else if (relegated) c.errorSoft else c.surfaceAlt, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(position.toString(), style = MaterialTheme.typography.labelMedium, color = if (promoted) c.successText else if (relegated) c.errorText else c.inkSoft)
        }
        Spacer(Modifier.width(12.dp))
        Text(
            if (isUser) "$name (você)" else name, Modifier.weight(1f),
            style = if (isUser) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium, color = c.ink,
        )
        Text("$xp XP", style = MaterialTheme.typography.labelLarge, color = c.inkSoft)
    }
}

@Composable
private fun LastWeekBanner(rank: Int, outcome: Int, currentTier: LeagueTier) {
    val c = Trilha.colors
    val (text, bg, fg) = when (outcome) {
        1 -> Triple("Semana passada: ${rank}º lugar. Você subiu para a liga ${currentTier.displayName}!", c.successSoft, c.successText)
        -1 -> Triple("Semana passada: ${rank}º lugar. Você desceu para a liga ${currentTier.displayName}. Dá para voltar!", c.errorSoft, c.errorText)
        else -> Triple("Semana passada: ${rank}º lugar. Você continua na liga ${currentTier.displayName}.", c.surfaceAlt, c.ink)
    }
    Text(text, Modifier.fillMaxWidth().background(bg, RoundedCornerShape(16.dp)).padding(14.dp), style = MaterialTheme.typography.bodyMedium, color = fg)
}

@Composable
private fun QuestCard(quest: QuestProgress, onClaim: () -> Unit) {
    val c = Trilha.colors
    val def = QuestEngine.byId(quest.questId) ?: return
    val complete = QuestEngine.isComplete(def, quest.progress)
    Column(
        Modifier.fillMaxWidth().background(c.surface, RoundedCornerShape(20.dp)).border(2.dp, if (complete && !quest.claimed) c.success else c.outline, RoundedCornerShape(20.dp)).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(def.title, style = MaterialTheme.typography.titleSmall, color = c.ink)
                Text("${quest.progress}/${def.target}", style = MaterialTheme.typography.labelMedium, color = c.inkSoft)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                GameIconView(GameIcon.Gem, size = 22.dp)
                Text("+${def.rewardGems}", style = MaterialTheme.typography.labelLarge, color = c.ink)
            }
        }
        GameProgressBar(quest.progress.toFloat() / def.target, color = if (complete) c.success else c.secondary, height = 12.dp)
        when {
            quest.claimed -> Text("Resgatada ✓", style = MaterialTheme.typography.labelMedium, color = c.successText)
            complete -> ChunkyButton("Resgatar ${def.rewardGems} gemas", onClaim, Modifier.fillMaxWidth(), ChunkyStyle.Success, minHeight = 44.dp)
        }
    }
}
