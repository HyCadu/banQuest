package br.com.trilhati.feature.session

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.trilhati.domain.pace.PaceEngine
import br.com.trilhati.ui.components.ChunkyButton
import br.com.trilhati.ui.components.ChunkyStyle
import br.com.trilhati.ui.components.PaceChip
import br.com.trilhati.ui.icons.GameIcon
import br.com.trilhati.ui.icons.GameIconView
import br.com.trilhati.ui.mascot.Mascot
import br.com.trilhati.ui.mascot.MascotCharacter
import br.com.trilhati.ui.mascot.MascotPose
import br.com.trilhati.ui.theme.Spacing
import br.com.trilhati.ui.theme.Trilha

@Composable
internal fun CenterMessage(character: MascotCharacter, text: String, animated: Boolean) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Mascot(character, MascotPose.Idle, Modifier.size(160.dp), animated = animated, decorative = true)
        Text(text, style = MaterialTheme.typography.titleMedium, color = Trilha.colors.inkSoft)
    }
}

@Composable
internal fun EmptyScreen(ui: SessionUi.Empty, character: MascotCharacter, onExit: () -> Unit, animated: Boolean) {
    Column(
        Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(Spacing.screen),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Spacing.md, Alignment.CenterVertically),
    ) {
        Mascot(character, MascotPose.Sleepy, Modifier.size(180.dp), animated = animated)
        Text(ui.title, style = MaterialTheme.typography.headlineLarge, color = Trilha.colors.ink)
        Text(ui.message, style = MaterialTheme.typography.bodyLarge, color = Trilha.colors.inkSoft, textAlign = TextAlign.Center)
        ChunkyButton("Voltar", onExit, Modifier.fillMaxWidth())
    }
}

@Composable
internal fun NoHeartsScreen(
    ui: SessionUi.NoHearts,
    character: MascotCharacter,
    onRefill: () -> Unit,
    onRetry: () -> Unit,
    onOpenErrors: () -> Unit,
    onExit: () -> Unit,
    animated: Boolean,
) {
    val c = Trilha.colors
    Column(
        Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(Spacing.screen),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Spacing.md, Alignment.CenterVertically),
    ) {
        Mascot(character, MascotPose.Sad, Modifier.size(180.dp), animated = animated)
        Text("Acabaram os corações!", style = MaterialTheme.typography.headlineLarge, color = c.ink, textAlign = TextAlign.Center)
        Text(
            if (ui.endedMidLesson) "A lição termina aqui, mas o que você acertou foi bom. Recupere corações e volte mais forte."
            else "Você precisa de pelo menos 1 coração para fazer uma lição.",
            style = MaterialTheme.typography.bodyLarge, color = c.inkSoft, textAlign = TextAlign.Center,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            repeat(5) { GameIconView(GameIcon.Heart, size = 30.dp, modifier = Modifier.padding(2.dp)) }
        }
        ChunkyButton("Praticar erros (ganhe 1 coração)", onOpenErrors, Modifier.fillMaxWidth(), ChunkyStyle.Primary)
        ChunkyButton("Recarregar por 350 gemas", onRefill, Modifier.fillMaxWidth(), ChunkyStyle.Secondary, enabled = ui.gems >= 350, icon = GameIcon.Gem)
        if (ui.gems < 350) Text("Você tem ${ui.gems} gemas.", style = MaterialTheme.typography.bodySmall, color = c.inkSoft)
        ui.msUntilNext?.let { untilNext ->
            Text("Ou espere: o próximo coração volta em ${PaceEngine.format(untilNext)}.", style = MaterialTheme.typography.bodyMedium, color = c.inkSoft)
            ChunkyButton("Já passou o tempo? Tentar de novo", onRetry, Modifier.fillMaxWidth(), ChunkyStyle.Neutral)
        }
        ChunkyButton("Sair", onExit, Modifier.fillMaxWidth(), ChunkyStyle.Neutral)
    }
}

@Composable
internal fun ResultScreen(ui: SessionUi.Finished, character: MascotCharacter, onContinue: () -> Unit, animated: Boolean) {
    val c = Trilha.colors
    val run = ui.run
    val summary = ui.processed.summary
    val isLesson = ui.source == SessionSource.Lesson
    val perfect = run.perfect
    val avg = PaceEngine.averageMs(run.totalTimeMs, run.records.size)
    val targetAvg = PaceEngine.averageMs(run.targetsSumMs, run.records.size)
    val slackSeconds = (PaceEngine.slackMs(run.targetsSumMs, run.totalTimeMs) / 1000).toInt()

    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = Spacing.screen, vertical = Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            val pose = when {
                perfect -> MascotPose.Celebrate
                summary.streakIncreased || summary.newAchievements.isNotEmpty() -> MascotPose.Proud
                else -> MascotPose.Happy
            }
            Mascot(character, pose, Modifier.size(200.dp), animated = animated)
            Text(
                when {
                    !isLesson -> "Treino concluído!"
                    perfect -> "Lição perfeita!"
                    else -> "Lição concluída!"
                },
                style = MaterialTheme.typography.headlineLarge, color = c.primaryText, textAlign = TextAlign.Center,
            )
            Text(ui.title, style = MaterialTheme.typography.bodyMedium, color = c.inkSoft)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ResultStat("XP ganho", "+${summary.totalXp}", GameIcon.Star, Modifier.weight(1f))
                ResultStat("Precisão", "${run.accuracyPercent}%", GameIcon.Target, Modifier.weight(1f))
                ResultStat("Melhor combo", run.bestCombo.toString(), GameIcon.Flame, Modifier.weight(1f))
            }
            // Resumo de tempo: sempre aparece, mesmo com o cronômetro desligado (regra do projeto).
            Column(
                Modifier.fillMaxWidth().background(c.surface, RoundedCornerShape(20.dp)).border(2.dp, c.outline, RoundedCornerShape(20.dp)).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GameIconView(GameIcon.Clock, size = 32.dp)
                    Column(Modifier.weight(1f)) {
                        Text("Tempo médio · alvo ${PaceEngine.format(targetAvg)}", style = MaterialTheme.typography.labelMedium, color = c.inkSoft)
                        Text(PaceEngine.format(avg), style = MaterialTheme.typography.headlineMedium, color = if (avg <= targetAvg) c.ink else c.warningText)
                    }
                    PaceChip(slackSeconds)
                }
            }
            val levelAfter = br.com.trilhati.domain.lesson.XpEngine.level(ui.processed.state.stats.totalXp)
            val levelBefore = br.com.trilhati.domain.lesson.XpEngine.level(ui.processed.state.stats.totalXp - summary.totalXp)
            // Recompensas em fichas curtas, lado a lado (quebram linha sozinhas).
            androidx.compose.foundation.layout.FlowRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (levelAfter > levelBefore) Reward("Nível $levelAfter!", GameIcon.Crown, c.primarySoft, c.primaryText)
                if (summary.gemsEarned > 0) Reward("+${summary.gemsEarned}", GameIcon.Gem, c.secondarySoft, c.secondaryText, "+${summary.gemsEarned} gemas")
                if (summary.streakIncreased) {
                    val days = "${summary.streakAfter} ${if (summary.streakAfter == 1) "dia" else "dias"}"
                    Reward(days, GameIcon.Flame, c.warningSoft, c.warningText, "Ofensiva de $days")
                }
                if (summary.frozenDays.isNotEmpty()) Reward("Congelamento usado", GameIcon.Snowflake, c.secondarySoft, c.secondaryText)
                if (summary.goalJustMet) Reward("Meta batida", GameIcon.Star, c.successSoft, c.successText)
                if (summary.unitCompleted) Reward("Baú liberado", GameIcon.Chest, c.warningSoft, c.warningText, "Unidade concluída: baú liberado na trilha")
                // Missões e conquistas viram uma ficha cada grupo; os nomes ficam para o TalkBack (e para as abas Ranking e Perfil).
                val quests = summary.questsCompleted
                if (quests.isNotEmpty()) {
                    Reward(
                        if (quests.size == 1) "1 missão" else "${quests.size} missões", GameIcon.Target, c.successSoft, c.successText,
                        "Missões cumpridas: ${quests.joinToString()}",
                    )
                }
                val achievements = summary.newAchievements
                if (achievements.isNotEmpty()) {
                    Reward(
                        if (achievements.size == 1) achievements.first().title else "${achievements.size} conquistas", GameIcon.Trophy,
                        c.warningSoft, c.warningText, "Conquistas: ${achievements.joinToString { it.title }}",
                    )
                }
            }

            val wrong = run.firstTryRecords.filter { !it.correct }
            if (wrong.isNotEmpty()) {
                Text("Para revisar", style = MaterialTheme.typography.headlineSmall, color = c.ink, modifier = Modifier.fillMaxWidth())
                wrong.take(5).forEach { r ->
                    Text(
                        (if (r.corrected) "Corrigida no caminho · " else "") + r.question.stem.take(140) + if (r.question.stem.length > 140) "…" else "",
                        Modifier.fillMaxWidth().background(if (r.corrected) c.warningSoft else c.errorSoft, RoundedCornerShape(14.dp)).padding(12.dp),
                        style = MaterialTheme.typography.bodySmall, color = c.ink,
                    )
                }
            }
        }
        ChunkyButton("Continuar", onContinue, Modifier.fillMaxWidth().padding(Spacing.screen), ChunkyStyle.Primary)
    }
}

@Composable
private fun ResultStat(label: String, value: String, icon: GameIcon, modifier: Modifier = Modifier) {
    val c = Trilha.colors
    Column(
        modifier.background(c.surface, RoundedCornerShape(18.dp)).border(2.dp, c.outline, RoundedCornerShape(18.dp)).padding(vertical = 12.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        GameIconView(icon, size = 26.dp)
        Text(value, style = MaterialTheme.typography.headlineSmall, color = c.ink)
        Text(label, style = MaterialTheme.typography.labelSmall, color = c.inkSoft)
    }
}

/** Ficha de recompensa: ícone e poucas palavras; [description] é o que o TalkBack lê. */
@Composable
private fun Reward(text: String, icon: GameIcon, bg: Color, fg: Color, description: String = text) {
    Row(
        Modifier.background(bg, RoundedCornerShape(50)).padding(start = 8.dp, end = 14.dp, top = 6.dp, bottom = 6.dp)
            .semantics(mergeDescendants = true) { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        GameIconView(icon, size = 24.dp)
        Text(text, style = MaterialTheme.typography.titleSmall, color = fg)
    }
}
