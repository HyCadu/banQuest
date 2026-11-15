package br.com.trilhati.feature.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.trilhati.R
import br.com.trilhati.ui.components.ChunkyButton
import br.com.trilhati.ui.components.ChunkyStyle
import br.com.trilhati.ui.components.CodeBlock
import br.com.trilhati.ui.components.ComboBadge
import br.com.trilhati.ui.components.FeedbackPanel
import br.com.trilhati.ui.components.GameProgressBar
import br.com.trilhati.ui.components.MascotSays
import br.com.trilhati.ui.components.NavTab
import br.com.trilhati.ui.components.NodeState
import br.com.trilhati.ui.components.OptionCard
import br.com.trilhati.ui.components.OptionState
import br.com.trilhati.ui.components.PaceChip
import br.com.trilhati.ui.components.PaceRing
import br.com.trilhati.ui.components.PaceZone
import br.com.trilhati.ui.components.PathNode
import br.com.trilhati.ui.components.TopStatusBar
import br.com.trilhati.ui.components.TrilhaBottomBar
import br.com.trilhati.ui.components.UnitBanner
import br.com.trilhati.ui.components.UnreviewedBadge
import br.com.trilhati.ui.components.XpPill
import br.com.trilhati.ui.icons.GameIcon
import br.com.trilhati.ui.icons.GameIconView
import br.com.trilhati.ui.mascot.Mascot
import br.com.trilhati.ui.mascot.MascotCharacter
import br.com.trilhati.ui.mascot.MascotPose
import br.com.trilhati.ui.theme.Spacing
import br.com.trilhati.ui.theme.ThemeMode
import br.com.trilhati.ui.theme.Trilha

/**
 * Catálogo do design system (Fase 1): mostra mascotes, botões, ícones e componentes como ficarão no app.
 * Os textos de exemplo ficam inline de propósito: é uma tela de vitrine, não de produto.
 */
@Composable
fun GalleryScreen(
    themeMode: ThemeMode,
    onThemeMode: (ThemeMode) -> Unit,
    daysToExam: Int,
    footer: String,
    modifier: Modifier = Modifier,
    animated: Boolean = true,
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf(
        NavTab(stringResource(R.string.tab_path), GameIcon.House),
        NavTab(stringResource(R.string.tab_practice), GameIcon.Target, badge = true),
        NavTab(stringResource(R.string.tab_leagues), GameIcon.Trophy),
        NavTab(stringResource(R.string.tab_shop), GameIcon.Bag),
        NavTab(stringResource(R.string.tab_profile), GameIcon.Person),
    )
    Column(modifier.fillMaxSize().background(Trilha.colors.background)) {
        LazyColumn(
            modifier = Modifier.weight(1f).statusBarsPadding(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = Spacing.screen, vertical = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            item { Header(themeMode, onThemeMode, daysToExam) }
            item { MascotPlayground(animated) }
            item { MascotCatalog() }
            item { ButtonsSection() }
            item { StatusSection() }
            item { ProgressSection() }
            item { PathSection(animated) }
            item { QuestionSection(animated) }
            item { SpeechSection(animated) }
            item { IconsSection() }
            item {
                Text(
                    footer,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Trilha.colors.inkSoft,
                    textAlign = TextAlign.Center,
                )
            }
        }
        TrilhaBottomBar(tabs, selectedIndex = tab, onSelect = { tab = it })
    }
}

@Composable
private fun Section(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(title, style = MaterialTheme.typography.headlineSmall, color = Trilha.colors.ink)
        content()
    }
}

@Composable
private fun PillChoice(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = Trilha.colors
    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .semantics { role = Role.RadioButton; this.selected = selected }
            .background(if (selected) c.primary else c.surface, shape)
            .border(2.dp, if (selected) c.primaryEdge else c.outline, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = if (selected) c.onPrimary else c.primaryText)
    }
}

@Composable
private fun Header(themeMode: ThemeMode, onThemeMode: (ThemeMode) -> Unit, daysToExam: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text("Trilha TI", style = MaterialTheme.typography.displaySmall, color = Trilha.colors.primaryText)
        Text(
            "Catálogo de design · Fase 1. É assim que o app vai parecer; a trilha de verdade chega na Fase 4.",
            style = MaterialTheme.typography.bodyMedium,
            color = Trilha.colors.inkSoft,
        )
        TopStatusBar(streak = 12, gems = 340, hearts = 5, daysToExam = daysToExam)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PillChoice("Tema do sistema", themeMode == ThemeMode.System, { onThemeMode(ThemeMode.System) })
            PillChoice("Claro", themeMode == ThemeMode.Light, { onThemeMode(ThemeMode.Light) })
            PillChoice("Escuro", themeMode == ThemeMode.Dark, { onThemeMode(ThemeMode.Dark) })
        }
    }
}

@Composable
private fun MascotPlayground(animated: Boolean) {
    var character by rememberSaveable { mutableStateOf(MascotCharacter.Guaiamum) }
    var pose by rememberSaveable { mutableStateOf(MascotPose.Idle) }
    val c = Trilha.colors
    Section("Mascotes") {
        Box(
            Modifier
                .fillMaxWidth()
                .background(c.surface, RoundedCornerShape(28.dp))
                .border(2.dp, c.outline, RoundedCornerShape(28.dp))
                .padding(Spacing.md),
            contentAlignment = Alignment.Center,
        ) {
            Mascot(character, pose, Modifier.size(230.dp), animated = animated)
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MascotCharacter.entries.forEach { PillChoice(it.displayName, it == character, { character = it }) }
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MascotPose.entries.forEach { PillChoice(it.label.replaceFirstChar(Char::uppercase), it == pose, { pose = it }) }
        }
    }
}

@Composable
private fun MascotCatalog() {
    Section("Todas as poses") {
        MascotCharacter.entries.forEach { character ->
            Text(character.displayName, style = MaterialTheme.typography.titleMedium, color = Trilha.colors.ink)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MascotPose.entries.forEach { pose ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Mascot(character, pose, Modifier.size(112.dp), animated = false, frozenSeconds = 0.9f)
                        Text(pose.label, Modifier.padding(top = 6.dp), style = MaterialTheme.typography.labelSmall, color = Trilha.colors.inkSoft)
                    }
                }
            }
        }
    }
}

@Composable
private fun ButtonsSection() {
    Section("Botões") {
        ChunkyButton("Começar lição", {}, Modifier.fillMaxWidth(), ChunkyStyle.Primary)
        ChunkyButton("Verificar", {}, Modifier.fillMaxWidth(), ChunkyStyle.Secondary)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ChunkyButton("Acertei", {}, Modifier.weight(1f), ChunkyStyle.Success)
            ChunkyButton("Errei", {}, Modifier.weight(1f), ChunkyStyle.Error)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ChunkyButton("Baú", {}, Modifier.weight(1f), ChunkyStyle.Warning, icon = GameIcon.Chest)
            ChunkyButton("Pular", {}, Modifier.weight(1f), ChunkyStyle.Neutral)
        }
        ChunkyButton("Bloqueado", {}, Modifier.fillMaxWidth(), enabled = false)
    }
}

@Composable
private fun StatusSection() {
    Section("Selos e status") {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            ComboBadge(5)
            XpPill("+10 XP")
            UnreviewedBadge()
        }
    }
}

@Composable
private fun ProgressSection() {
    Section("Progresso e cronômetro de ritmo") {
        GameProgressBar(progress = 0.35f)
        GameProgressBar(progress = 0.75f, color = Trilha.colors.primary)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            PaceRing(remaining = 0.72f, label = "2:10", zone = PaceZone.OnPace)
            PaceRing(remaining = 0.22f, label = "0:40", zone = PaceZone.Warning)
            PaceRing(remaining = 0f, label = "+0:12", zone = PaceZone.Over)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PaceChip(deltaSeconds = 42)
            PaceChip(deltaSeconds = -15)
        }
    }
}

@Composable
private fun PathSection(animated: Boolean) {
    Section("Trilha") {
        UnitBanner(1, "Banco de dados", "Modelo ER e normalização")
        Spacer(Modifier.height(4.dp))
        val offsets = listOf(0.dp, 46.dp, 70.dp, 36.dp, -24.dp)
        val nodes = listOf(NodeState.Perfect, NodeState.Done, NodeState.Current, NodeState.Locked, NodeState.Locked)
        nodes.forEachIndexed { i, state ->
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                PathNode(state, "Lição ${i + 1}", {}, Modifier.offset(x = offsets[i]))
                if (state == NodeState.Current) {
                    Mascot(
                        MascotCharacter.Tartaruga, MascotPose.Happy,
                        Modifier.size(84.dp).offset(x = offsets[i] - 112.dp),
                        animated = animated,
                        decorative = true,
                    )
                }
            }
        }
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            GameIconView(GameIcon.Chest, size = 64.dp)
        }
    }
}

@Composable
private fun QuestionSection(animated: Boolean) {
    var chosen by rememberSaveable { mutableIntStateOf(-1) }
    var checked by rememberSaveable { mutableStateOf(false) }
    val correctIndex = 1
    val options = listOf("WHERE", "HAVING", "GROUP BY", "ORDER BY", "DISTINCT")
    Section("Tela de questão") {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GameProgressBar(0.5f, Modifier.weight(1f))
            PaceRing(remaining = 0.6f, label = "1:48", zone = PaceZone.OnPace, size = 56.dp)
        }
        UnreviewedBadge()
        Text(
            "Em SQL, qual cláusula filtra os grupos formados por GROUP BY, depois da agregação?",
            style = MaterialTheme.typography.titleMedium,
            color = Trilha.colors.ink,
        )
        CodeBlock("SELECT depto, COUNT(*)\nFROM funcionario\nGROUP BY depto\n___ COUNT(*) > 5;")
        options.forEachIndexed { i, text ->
            val state = when {
                checked && i == correctIndex -> OptionState.Correct
                checked && i == chosen -> OptionState.Wrong
                checked -> OptionState.Dimmed
                i == chosen -> OptionState.Selected
                else -> OptionState.Idle
            }
            OptionCard("ABCDE"[i].toString(), text, state, onClick = { if (!checked) chosen = i }, enabled = !checked)
        }
        if (!checked) {
            ChunkyButton("Verificar", { checked = true }, Modifier.fillMaxWidth(), ChunkyStyle.Primary, enabled = chosen >= 0)
        } else {
            val right = chosen == correctIndex
            FeedbackPanel(
                correct = right,
                title = if (right) "Mandou bem!" else "Quase lá!",
                message = "HAVING filtra grupos depois da agregação; WHERE filtra linhas antes do agrupamento.",
                character = MascotCharacter.Guaiamum,
                onContinue = { checked = false; chosen = -1 },
                animated = animated,
            )
        }
    }
}

@Composable
private fun SpeechSection(animated: Boolean) {
    Section("Mascote falando") {
        MascotSays(MascotCharacter.Tamandua, MascotPose.Happy, "Faltam poucos dias! Bora garantir a ofensiva de hoje?", animated = animated)
        MascotSays(MascotCharacter.Preguica, MascotPose.Sleepy, "Zzz… só mais uma lição antes de dormir?", animated = animated)
    }
}

@Composable
private fun IconsSection() {
    Section("Ícones") {
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            GameIcon.entries.forEach { icon ->
                Box(
                    Modifier.size(52.dp).background(Trilha.colors.surface, RoundedCornerShape(14.dp)).border(2.dp, Trilha.colors.outline, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center,
                ) { GameIconView(icon, size = 32.dp) }
            }
        }
    }
}
