package br.com.trilhati.feature.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.trilhati.domain.hearts.HeartsEngine
import br.com.trilhati.domain.progress.ShopItem
import br.com.trilhati.domain.streak.StreakEngine
import br.com.trilhati.feature.app.AppActions
import br.com.trilhati.feature.app.AppState
import br.com.trilhati.ui.components.ChunkyButton
import br.com.trilhati.ui.components.ChunkyStyle
import br.com.trilhati.ui.components.MascotSays
import br.com.trilhati.ui.icons.GameIcon
import br.com.trilhati.ui.icons.GameIconView
import br.com.trilhati.ui.mascot.MascotCharacter
import br.com.trilhati.ui.mascot.MascotPose
import br.com.trilhati.ui.theme.Spacing
import br.com.trilhati.ui.theme.Trilha

/** Aba Loja: gemas, baú diário, congelamento de ofensiva e recarga de corações. */
@Composable
fun ShopContent(state: AppState, actions: AppActions, modifier: Modifier = Modifier, animated: Boolean = true) {
    val c = Trilha.colors
    val stats = state.snapshot.stats
    val character = MascotCharacter.entries.firstOrNull { it.name == state.snapshot.profile.character } ?: MascotCharacter.Guaiamum
    val chestAvailable = state.snapshot.shop.lastDailyChestDay != state.today
    val freezes = stats.streak.freezes

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = Spacing.screen, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Loja", style = MaterialTheme.typography.displaySmall, color = c.primaryText, modifier = Modifier.weight(1f))
            GameIconView(GameIcon.Gem, size = 34.dp)
            Text(stats.gems.toString(), style = MaterialTheme.typography.headlineMedium, color = c.ink)
        }
        MascotSays(
            character = character, pose = if (stats.gems >= ShopItem.StreakFreeze.priceGems) MascotPose.Happy else MascotPose.Idle,
            text = "Gemas vêm de lições, missões, conquistas e baús. Gaste com sabedoria!", animated = animated,
        )
        ShopCard(
            icon = GameIcon.Chest, title = "Baú diário", description = "Uma surpresa de gemas por dia. Abra todo dia!",
            action = {
                ChunkyButton(
                    if (chestAvailable) "Abrir baú" else "Volte amanhã", actions.claimDailyChest, Modifier.fillMaxWidth(),
                    ChunkyStyle.Warning, enabled = chestAvailable, minHeight = 46.dp,
                )
            },
        )
        ShopCard(
            icon = GameIcon.Snowflake, title = "Congelar ofensiva",
            description = "Protege a ofensiva em um dia sem estudar. Usa sozinho. Você tem $freezes de ${StreakEngine.MAX_FREEZES}.",
            action = {
                ChunkyButton(
                    "${ShopItem.StreakFreeze.priceGems} gemas", { actions.buy(ShopItem.StreakFreeze) }, Modifier.fillMaxWidth(),
                    ChunkyStyle.Secondary, enabled = freezes < StreakEngine.MAX_FREEZES, icon = GameIcon.Gem, minHeight = 46.dp,
                )
            },
        )
        ShopCard(
            icon = GameIcon.Heart, title = "Recarregar corações",
            description = if (state.settings.noLives) "O modo sem vidas está ligado, então você não perde corações."
            else "Enche os ${HeartsEngine.MAX} corações de uma vez e deixa você errar à vontade.",
            action = {
                ChunkyButton(
                    "${ShopItem.RefillHearts.priceGems} gemas", { actions.buy(ShopItem.RefillHearts) }, Modifier.fillMaxWidth(),
                    ChunkyStyle.Secondary, enabled = !state.settings.noLives && stats.hearts.hearts < HeartsEngine.MAX, icon = GameIcon.Gem, minHeight = 46.dp,
                )
            },
        )
    }
}

@Composable
private fun ShopCard(icon: GameIcon, title: String, description: String, action: @Composable () -> Unit) {
    val c = Trilha.colors
    Column(
        Modifier.fillMaxWidth().background(c.surface, RoundedCornerShape(22.dp)).border(2.dp, c.outline, RoundedCornerShape(22.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            GameIconView(icon, size = 48.dp)
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = c.ink)
                Text(description, style = MaterialTheme.typography.bodySmall, color = c.inkSoft)
            }
        }
        action()
    }
}
