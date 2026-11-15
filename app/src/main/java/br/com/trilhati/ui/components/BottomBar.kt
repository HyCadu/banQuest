package br.com.trilhati.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import br.com.trilhati.ui.icons.GameIcon
import br.com.trilhati.ui.icons.GameIconView
import br.com.trilhati.ui.icons.IconColors
import br.com.trilhati.ui.theme.Trilha

class NavTab(val label: String, val icon: GameIcon, val badge: Boolean = false)

/** Barra inferior com 5 abas: a aba ativa ganha uma "pílula" colorida e o ícone dá um pulinho. */
@Composable
fun TrilhaBottomBar(
    tabs: List<NavTab>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = Trilha.colors
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = c.surface,
        border = BorderStroke(2.dp, c.outline),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Row(
            modifier = Modifier.navigationBarsPadding().padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            tabs.forEachIndexed { index, tab ->
                NavItem(tab, selected = index == selectedIndex, onClick = { onSelect(index) }, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun NavItem(tab: NavTab, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = Trilha.colors
    val pop by animateFloatAsState(
        if (selected) 1.15f else 1f,
        spring(Spring.DampingRatioHighBouncy, Spring.StiffnessMedium),
        label = "tabPop",
    )
    val iconColors = if (selected) null else IconColors.flat(c.locked, c.onLocked)
    Column(
        modifier = modifier
            .heightIn(min = 56.dp)
            .semantics(mergeDescendants = true) {
                role = Role.Tab
                this.selected = selected
            }
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .background(if (selected) c.primarySoft else Color.Transparent, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.scale(pop)) {
                if (iconColors == null) GameIconView(tab.icon, size = 30.dp) else GameIconView(tab.icon, size = 30.dp, colors = iconColors)
            }
            if (tab.badge) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-2).dp)
                        .size(10.dp)
                        .background(c.error, CircleShape),
                )
            }
        }
        Text(
            tab.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) c.primaryText else c.inkSoft,
        )
    }
}
