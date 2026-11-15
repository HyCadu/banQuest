package br.com.trilhati.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import br.com.trilhati.domain.achievement.AchievementEngine
import br.com.trilhati.domain.lesson.XpEngine
import br.com.trilhati.domain.pace.PaceEngine
import br.com.trilhati.feature.app.AppActions
import br.com.trilhati.feature.app.AppState
import br.com.trilhati.ui.components.ChunkyButton
import br.com.trilhati.ui.components.ChunkyStyle
import br.com.trilhati.ui.components.GameProgressBar
import br.com.trilhati.ui.components.PillChoice
import br.com.trilhati.ui.components.StreakCalendar
import br.com.trilhati.ui.components.TrilhaDialog
import br.com.trilhati.ui.icons.GameIcon
import br.com.trilhati.ui.icons.GameIconView
import br.com.trilhati.ui.icons.IconColors
import br.com.trilhati.ui.mascot.Mascot
import br.com.trilhati.ui.mascot.MascotCharacter
import br.com.trilhati.ui.mascot.MascotPose
import br.com.trilhati.ui.theme.Spacing
import br.com.trilhati.ui.theme.Trilha

private val GOALS = listOf("Leve" to 50, "Regular" to 100, "Séria" to 200, "Intensa" to 300)

/** Aba Perfil: personagem (só aqui!), nome, meta diária, estatísticas, calendário e conquistas. */
@Composable
fun ProfileContent(state: AppState, actions: AppActions, modifier: Modifier = Modifier, animated: Boolean = true) {
    val c = Trilha.colors
    val profile = state.snapshot.profile
    val stats = state.snapshot.stats
    val character = MascotCharacter.entries.firstOrNull { it.name == profile.character } ?: MascotCharacter.Guaiamum
    var editName by remember { mutableStateOf(false) }

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = Spacing.screen, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        // Cabeçalho
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Mascot(character, if (state.todayLog.goalMet) MascotPose.Proud else MascotPose.Happy, Modifier.size(180.dp), animated = animated)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(profile.name, style = MaterialTheme.typography.headlineLarge, color = c.ink)
                Box(Modifier.clickable { editName = true }.semantics { role = Role.Button }.padding(6.dp)) {
                    Text("Editar", style = MaterialTheme.typography.labelMedium, color = c.primaryText)
                }
            }
            val level = XpEngine.level(stats.totalXp)
            Text("Nível $level · ${stats.totalXp} XP", style = MaterialTheme.typography.titleSmall, color = c.inkSoft)
            GameProgressBar(XpEngine.levelProgress(stats.totalXp), color = c.secondary)
            Text("${XpEngine.LEVEL_XP - XpEngine.xpIntoLevel(stats.totalXp)} XP para o nível ${level + 1}", style = MaterialTheme.typography.bodySmall, color = c.inkSoft)
        }

        // Personagem
        SectionTitle("Meu personagem")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MascotCharacter.entries.forEach { option ->
                val selected = option == character
                Column(
                    Modifier
                        .weight(1f)
                        .semantics { role = Role.RadioButton; this.selected = selected }
                        .background(if (selected) c.primarySoft else c.surface, RoundedCornerShape(18.dp))
                        .border(2.dp, if (selected) c.primary else c.outline, RoundedCornerShape(18.dp))
                        .clickable { actions.setCharacter(option.name) }
                        .padding(vertical = 8.dp, horizontal = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Mascot(option, MascotPose.Idle, Modifier.size(64.dp), animated = false, decorative = true)
                    Text(option.displayName.substringBefore('-'), style = MaterialTheme.typography.labelSmall, color = if (selected) c.primaryText else c.inkSoft, maxLines = 1)
                }
            }
        }

        // Meta diária
        SectionTitle("Meta diária")
        GOALS.chunked(2).forEach { rowGoals ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowGoals.forEach { (label, xp) ->
                    PillChoice("$label · $xp XP", profile.dailyGoalXp == xp, { actions.setGoal(xp) }, Modifier.weight(1f))
                }
            }
        }

        // Estatísticas
        SectionTitle("Estatísticas")
        val target = state.settings.paceTargetMs
        val avg = stats.averageTimeMs
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("Ofensiva", "${state.streakCount} d", "recorde ${stats.streak.longest} d", GameIcon.Flame, Modifier.weight(1f))
            StatCard("Lições", stats.lessonsCompleted.toString(), "${stats.perfectLessons} perfeitas", GameIcon.Star, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("Precisão", if (stats.firstTryAnswered == 0) "—" else "${stats.accuracyPercent}%", "${stats.firstTryAnswered} respostas", GameIcon.Target, Modifier.weight(1f))
            StatCard(
                "Tempo por questão", if (stats.answeredTotal == 0) "—" else PaceEngine.format(avg),
                if (stats.answeredTotal == 0) "alvo ${PaceEngine.format(target)}" else if (avg <= target) "no ritmo (alvo ${PaceEngine.format(target)})" else "acima do alvo ${PaceEngine.format(target)}",
                GameIcon.Clock, Modifier.weight(1f),
            )
        }

        SectionTitle("Últimos dias")
        StreakCalendar(state.snapshot.dailyLogs, state.today)

        // Precisão por unidade
        val unitsWithData = state.course.filter { it.hasContent }.mapNotNull { unit ->
            val ids = unit.lessons.map { it.id }.toSet()
            val qs = state.snapshot.questionStats.values.filter { it.lessonId in ids }
            val times = qs.sumOf { it.times }
            if (times == 0) null else Triple(unit.title, qs.sumOf { it.correct } * 100 / times, times)
        }
        if (unitsWithData.isNotEmpty()) {
            SectionTitle("Acerto por unidade")
            unitsWithData.forEach { (title, accuracy, times) ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row { Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleSmall, color = c.ink); Text("$accuracy% · $times resp.", style = MaterialTheme.typography.labelMedium, color = c.inkSoft) }
                    GameProgressBar(accuracy / 100f, color = if (accuracy >= 70) c.success else c.warning, height = 12.dp)
                }
            }
        }

        // Conquistas
        val unlocked = state.snapshot.achievements
        SectionTitle("Conquistas · ${unlocked.size}/${AchievementEngine.all.size}")
        AchievementEngine.all.forEach { def ->
            val done = def.id in unlocked
            Row(
                Modifier.fillMaxWidth().background(c.surface, RoundedCornerShape(16.dp)).border(2.dp, if (done) c.warning else c.outline, RoundedCornerShape(16.dp)).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (done) GameIconView(GameIcon.Trophy, size = 36.dp)
                else GameIconView(GameIcon.Lock, size = 36.dp, colors = IconColors(c.onLocked, c.locked, c.lockedEdge))
                Column(Modifier.weight(1f)) {
                    Text(def.title, style = MaterialTheme.typography.titleSmall, color = if (done) c.ink else c.inkSoft)
                    Text(def.description, style = MaterialTheme.typography.bodySmall, color = c.inkSoft)
                }
                if (done) Text("+${def.rewardGems}", style = MaterialTheme.typography.labelLarge, color = c.warningText)
            }
        }

        ChunkyButton("Configurações", actions.openSettings, Modifier.fillMaxWidth(), ChunkyStyle.Neutral)
    }

    if (editName) {
        var text by remember { mutableStateOf(profile.name) }
        TrilhaDialog(
            onDismiss = { editName = false },
            title = "Como devo te chamar?",
            content = {
                OutlinedTextField(text, { text = it.take(24) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            },
            actions = {
                ChunkyButton("Salvar", { actions.setName(text); editName = false }, Modifier.fillMaxWidth())
                ChunkyButton("Cancelar", { editName = false }, Modifier.fillMaxWidth(), ChunkyStyle.Neutral)
            },
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.headlineSmall, color = Trilha.colors.ink)
}

@Composable
private fun StatCard(title: String, value: String, caption: String, icon: GameIcon, modifier: Modifier = Modifier) {
    val c = Trilha.colors
    Column(
        modifier.background(c.surface, RoundedCornerShape(18.dp)).border(2.dp, c.outline, RoundedCornerShape(18.dp)).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            GameIconView(icon, size = 20.dp)
            Text(title, style = MaterialTheme.typography.labelMedium, color = c.inkSoft)
        }
        Text(value, style = MaterialTheme.typography.headlineMedium, color = c.ink)
        Text(caption, style = MaterialTheme.typography.bodySmall, color = c.inkSoft)
    }
}
