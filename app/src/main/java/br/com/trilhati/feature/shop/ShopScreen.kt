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
import br.com.trilhati.domain.progress.HelpEngine
import br.com.trilhati.domain.progress.ShopItem
import br.com.trilhati.domain.streak.StreakEngine
import br.com.trilhati.feature.app.AppActions
import br.com.trilhati.feature.app.AppState
import br.com.trilhati.ui.components.ChunkyButton
import br.com.trilhati.ui.components.ChunkyStyle
import br.com.trilhati.ui.icons.GameIcon
import br.com.trilhati.ui.icons.GameIconView
import br.com.trilhati.ui.theme.Spacing
import br.com.trilhati.ui.theme.Trilha

/** Aba Loja: gemas, baú diário, congelamento de ofensiva, recarga de corações e "Não sei". Um item por linha. */
@Composable
fun ShopContent(state: AppState, actions: AppActions, modifier: Modifier = Modifier) {
    val c = Trilha.colors
    val stats = state.snapshot.stats
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
        ShopRow(GameIcon.Chest, "Baú diário", "Gemas grátis todo dia") {
            ChunkyButton(if (chestAvailable) "Abrir" else "Amanhã", actions.claimDailyChest, style = ChunkyStyle.Warning, enabled = chestAvailable, minHeight = 44.dp)
        }
        ShopRow(GameIcon.Snowflake, "Congelar ofensiva", "Salva um dia sem estudo · $freezes/${StreakEngine.MAX_FREEZES}") {
            PriceButton(ShopItem.StreakFreeze, enabled = freezes < StreakEngine.MAX_FREEZES) { actions.buy(ShopItem.StreakFreeze) }
        }
        ShopRow(
            GameIcon.Heart, "Recarregar corações",
            if (state.settings.noLives) "Modo sem vidas ligado" else "Enche os ${HeartsEngine.MAX} corações",
        ) {
            PriceButton(ShopItem.RefillHearts, enabled = !state.settings.noLives && stats.hearts.halves < HeartsEngine.MAX_HALVES) {
                actions.buy(ShopItem.RefillHearts)
            }
        }
        val helps = stats.help.count
        ShopRow(GameIcon.Info, "Não sei", "Resposta explicada sem perder coração · $helps/${HelpEngine.MAX}") {
            PriceButton(ShopItem.Help, enabled = helps < HelpEngine.MAX) { actions.buy(ShopItem.Help) }
        }
    }
}

/** Item da loja numa linha: ícone, nome, uma linha curta e o botão à direita. */
@Composable
private fun ShopRow(icon: GameIcon, title: String, subtitle: String, action: @Composable () -> Unit) {
    val c = Trilha.colors
    Row(
        Modifier.fillMaxWidth().background(c.surface, RoundedCornerShape(22.dp)).border(2.dp, c.outline, RoundedCornerShape(22.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        GameIconView(icon, size = 40.dp)
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = c.ink)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = c.inkSoft)
        }
        action()
    }
}

@Composable
private fun PriceButton(item: ShopItem, enabled: Boolean, onClick: () -> Unit) {
    ChunkyButton(item.priceGems.toString(), onClick, style = ChunkyStyle.Secondary, enabled = enabled, icon = GameIcon.Gem, minHeight = 44.dp)
}
