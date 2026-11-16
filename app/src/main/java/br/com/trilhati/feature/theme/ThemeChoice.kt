package br.com.trilhati.feature.theme

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import br.com.trilhati.ui.components.ChunkyButton
import br.com.trilhati.ui.mascot.Mascot
import br.com.trilhati.ui.mascot.MascotCharacter
import br.com.trilhati.ui.mascot.MascotPose
import br.com.trilhati.ui.theme.Spacing
import br.com.trilhati.ui.theme.ThemePalette
import br.com.trilhati.ui.theme.Trilha
import br.com.trilhati.ui.theme.TrilhaColors
import br.com.trilhati.ui.theme.paletteColors

/** O que cada tema tem em 60/30/10, para a amostra: fundo (60), cor principal (30) e destaque (10). */
private fun TrilhaColors.swatch() = Triple(background, primary, secondary)

private fun ThemePalette.blurb(): String = when (this) {
    ThemePalette.Claro -> "Roxo e turquesa no fundo claro"
    ThemePalette.Escuro -> "Roxo e turquesa no fundo escuro"
    ThemePalette.Eletrico -> "Azul e limão no fundo branco"
    ThemePalette.Chiclete -> "Rosa e azul no fundo creme"
}

/**
 * Escolha do tema na primeira entrada: tocar numa paleta já aplica o tema na tela inteira (prévia ao vivo), e o aviso
 * lembra que dá para trocar depois em Configurações › Tema.
 */
@Composable
fun ThemeChoiceScreen(
    selected: ThemePalette,
    character: MascotCharacter,
    onPick: (ThemePalette) -> Unit,
    onConfirm: () -> Unit,
    animated: Boolean = true,
) {
    val c = Trilha.colors
    Column(Modifier.fillMaxSize().background(c.background).statusBarsPadding().navigationBarsPadding()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = Spacing.screen, vertical = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Mascot(character, MascotPose.Happy, Modifier.size(96.dp), animated = animated, decorative = true)
            Text("Escolha o seu tema", style = MaterialTheme.typography.headlineMedium, color = c.ink)
            ThemePalette.entries.forEach { palette -> PaletteCard(palette, palette == selected, { onPick(palette) }, Modifier.fillMaxWidth()) }
            Text(
                "Dá para trocar quando quiser em Configurações › Tema.",
                style = MaterialTheme.typography.bodyMedium, color = c.inkSoft,
            )
        }
        ChunkyButton("Começar", onConfirm, Modifier.fillMaxWidth().padding(Spacing.screen))
    }
}

/** Cartão de uma paleta: nome, descrição curta, a faixa 60/30/10 e uma miniatura de tela. */
@Composable
fun PaletteCard(palette: ThemePalette, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, compact: Boolean = false) {
    val c = Trilha.colors
    val p = paletteColors(palette)
    val (sixty, thirty, ten) = p.swatch()
    val shape = RoundedCornerShape(20.dp)
    Surface(
        modifier = modifier
            .semantics(mergeDescendants = true) {
                role = Role.RadioButton; this.selected = selected; contentDescription = "Tema ${palette.displayName}: ${palette.blurb()}"
            }
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = c.surface,
        border = BorderStroke(if (selected) 3.dp else 2.dp, if (selected) c.primary else c.outline),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (!compact) MiniScreen(p)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(palette.displayName, style = MaterialTheme.typography.titleMedium, color = c.ink)
                if (!compact) Text(palette.blurb(), style = MaterialTheme.typography.bodySmall, color = c.inkSoft)
                // Faixa 60/30/10 nas proporções da regra.
                Row(Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(50)).border(1.dp, c.outline, RoundedCornerShape(50))) {
                    Box(Modifier.weight(6f).height(14.dp).background(sixty))
                    Box(Modifier.weight(3f).height(14.dp).background(thirty))
                    Box(Modifier.weight(1f).height(14.dp).background(ten))
                }
            }
        }
    }
}

/** Miniatura de uma tela no tema: fundo, um cartão com linhas de texto e um botão. */
@Composable
private fun MiniScreen(p: TrilhaColors) {
    Column(
        Modifier.width(64.dp).height(84.dp).clip(RoundedCornerShape(12.dp)).background(p.background).padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(Modifier.width(30.dp).height(5.dp).background(p.ink, RoundedCornerShape(50)))
        Column(
            Modifier.fillMaxWidth().background(p.surface, RoundedCornerShape(6.dp)).padding(5.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Box(Modifier.fillMaxWidth().height(3.dp).background(p.ink.copy(alpha = 0.7f), RoundedCornerShape(50)))
            Box(Modifier.width(28.dp).height(3.dp).background(p.ink.copy(alpha = 0.5f), RoundedCornerShape(50)))
        }
        Spacer(Modifier.weight(1f))
        Box(Modifier.size(12.dp).background(p.secondary, RoundedCornerShape(50)))
        Box(Modifier.fillMaxWidth().height(12.dp).background(p.primary, RoundedCornerShape(6.dp)))
    }
}
