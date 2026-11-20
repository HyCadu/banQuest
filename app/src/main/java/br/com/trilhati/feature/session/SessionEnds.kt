package br.com.trilhati.feature.session

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import br.com.trilhati.domain.celebration.CelebrationState
import br.com.trilhati.domain.celebration.CelebrationStep
import br.com.trilhati.ui.animation.celebration.AchievementItem
import br.com.trilhati.ui.animation.celebration.CompletionData
import br.com.trilhati.ui.animation.celebration.CompletionScene
import br.com.trilhati.ui.animation.celebration.RewardItem
import br.com.trilhati.ui.animation.celebration.RewardTone
import br.com.trilhati.ui.animation.celebration.StatItem
import br.com.trilhati.ui.animation.celebration.StatKind
import br.com.trilhati.ui.animation.celebration.StreakIgnition
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
            when {
                ui.forExam -> "Abrir uma prova gasta 1 coração inteiro. Recupere um e volte para a prova."
                ui.endedMidLesson -> "A lição termina aqui, mas o que você acertou foi bom. Recupere corações e volte mais forte."
                else -> "Você precisa de pelo menos 1 coração para fazer uma lição."
            },
            style = MaterialTheme.typography.bodyLarge, color = c.inkSoft, textAlign = TextAlign.Center,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            repeat(5) { GameIconView(GameIcon.Heart, size = 30.dp, modifier = Modifier.padding(2.dp)) }
        }
        // Só a revisão de erros vencidos devolve coração (0.10.1): sem nenhum vencido, o botão não aparece.
        if (ui.dueErrors > 0) {
            ChunkyButton("Revisar erros vencidos (ganhe 1 coração)", onOpenErrors, Modifier.fillMaxWidth(), ChunkyStyle.Primary)
        }
        ChunkyButton(
            "Recarregar por 350 gemas", onRefill, Modifier.fillMaxWidth(),
            if (ui.dueErrors > 0) ChunkyStyle.Secondary else ChunkyStyle.Primary, enabled = ui.gems >= 350, icon = GameIcon.Gem,
        )
        if (ui.gems < 350) Text("Você tem ${ui.gems} gemas.", style = MaterialTheme.typography.bodySmall, color = c.inkSoft)
        ui.msUntilNext?.let { untilNext ->
            Text("Ou espere: o próximo coração volta em ${PaceEngine.format(untilNext)}.", style = MaterialTheme.typography.bodyMedium, color = c.inkSoft)
            ChunkyButton("Já passou o tempo? Tentar de novo", onRetry, Modifier.fillMaxWidth(), ChunkyStyle.Neutral)
        }
        ChunkyButton("Sair", onExit, Modifier.fillMaxWidth(), ChunkyStyle.Neutral)
    }
}

/**
 * Fim da sessão (qualquer modalidade): a conclusão coreografada e, quando a ofensiva subiu, a chama para acender. Com
 * [celebration] nulo (prévias) ou "remover animações" ligado no Android, mostra o resultado completo e parado.
 */
@Composable
internal fun FinishedScreen(
    ui: SessionUi.Finished,
    character: MascotCharacter,
    onExit: () -> Unit,
    animated: Boolean,
    celebration: CelebrationState? = null,
    actions: CelebrationActions = CelebrationActions(),
) {
    val reduced = br.com.trilhati.ui.theme.LocalReducedMotion.current
    LaunchedEffect(reduced, celebration?.finished) {
        if (reduced && celebration != null && !celebration.finished) actions.onSkip()
    }
    val streak = celebration?.step == CelebrationStep.Streak
    AnimatedContent(
        targetState = streak,
        transitionSpec = {
            if (animated && !reduced) fadeIn(tween(450, delayMillis = 120)) togetherWith fadeOut(tween(300))
            else EnterTransition.None togetherWith ExitTransition.None
        },
        label = "celebration",
    ) { showStreak ->
        val state = celebration
        if (showStreak && state != null) {
            val summary = ui.processed.summary
            StreakIgnition(
                before = summary.streakBefore,
                after = summary.streakAfter,
                week = ui.week,
                character = character,
                lit = state.flameLit,
                onLight = actions.onLightFlame,
                onContinue = onExit,
                animated = animated,
                haptics = ui.haptics,
            )
        } else {
            ResultScreen(ui, character, onExit, animated, state, actions.onStepDone, actions.onSkip)
        }
    }
}

@Composable
internal fun ResultScreen(
    ui: SessionUi.Finished,
    character: MascotCharacter,
    onContinue: () -> Unit,
    animated: Boolean,
    celebration: CelebrationState? = null,
    onStepDone: (CelebrationStep) -> Unit = {},
    onSkip: () -> Unit = {},
) {
    val c = Trilha.colors
    val run = ui.run
    val isExam = ui.source == SessionSource.Exam
    val avg = PaceEngine.averageMs(run.totalTimeMs, run.records.size)
    val targetAvg = PaceEngine.averageMs(run.targetsSumMs, run.records.size)
    val slackSeconds = (PaceEngine.slackMs(run.targetsSumMs, run.totalTimeMs) / 1000).toInt()
    val data = remember(ui) { ui.toCompletionData() }

    CompletionScene(
        data = data,
        character = character,
        onContinue = onContinue,
        celebration = celebration,
        onStepDone = onStepDone,
        onSkip = onSkip,
        animated = animated,
    ) {
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
        val wrong = run.firstTryRecords.filter { !it.correct }
        if (wrong.isNotEmpty()) {
            Text("Para revisar", style = MaterialTheme.typography.headlineSmall, color = c.ink, modifier = Modifier.fillMaxWidth())
            if (isExam) {
                val n = wrong.size
                Text(
                    "${if (n == 1) "1 questão errada foi" else "$n questões erradas foram"} para Praticar erros.",
                    style = MaterialTheme.typography.bodyMedium, color = c.inkSoft, modifier = Modifier.fillMaxWidth(),
                )
            }
            wrong.take(5).forEach { r ->
                Text(
                    (if (r.corrected) "Corrigida no caminho · " else "") + r.question.stem.take(140) + if (r.question.stem.length > 140) "…" else "",
                    Modifier.fillMaxWidth().background(if (r.corrected) c.warningSoft else c.errorSoft, RoundedCornerShape(14.dp)).padding(12.dp),
                    style = MaterialTheme.typography.bodySmall, color = c.ink,
                )
            }
        }
    }
}

/**
 * Converte o resultado real da sessão no que a conclusão mostra: título, indicadores (acertos de primeira, precisão,
 * melhor combo), XP, fichas de recompensa e conquistas. Nada inventado; o que não aconteceu não aparece.
 */
internal fun SessionUi.Finished.toCompletionData(): CompletionData {
    val summary = processed.summary
    val isLesson = source == SessionSource.Lesson
    val isExam = source == SessionSource.Exam
    val perfect = run.perfect
    val firstTry = run.firstTryRecords
    val stats = buildList {
        add(StatItem("Acertos", firstTry.count { it.correct }, StatKind.Fraction, GameIcon.Check, total = firstTry.size))
        add(StatItem("Precisão", run.accuracyPercent, StatKind.Percent, GameIcon.Target))
        if (!isExam) add(StatItem("Melhor combo", run.bestCombo, StatKind.Count, GameIcon.Flame))
    }
    val levelAfter = br.com.trilhati.domain.lesson.XpEngine.level(processed.state.stats.totalXp)
    val levelBefore = br.com.trilhati.domain.lesson.XpEngine.level(processed.state.stats.totalXp - summary.totalXp)
    val rewards = buildList {
        if (levelAfter > levelBefore) add(RewardItem("Nível $levelAfter!", GameIcon.Crown, RewardTone.Primary))
        if (summary.gemsEarned > 0) add(RewardItem("+${summary.gemsEarned}", GameIcon.Gem, RewardTone.Secondary, "+${summary.gemsEarned} gemas"))
        if (summary.streakIncreased) {
            val days = "${summary.streakAfter} ${if (summary.streakAfter == 1) "dia" else "dias"}"
            add(RewardItem(days, GameIcon.Flame, RewardTone.Warning, "Ofensiva de $days"))
        }
        if (summary.frozenDays.isNotEmpty()) add(RewardItem("Congelamento usado", GameIcon.Snowflake, RewardTone.Secondary))
        if (summary.goalJustMet) add(RewardItem("Meta batida", GameIcon.Star, RewardTone.Success))
        if (summary.unitCompleted) add(RewardItem("Baú liberado", GameIcon.Chest, RewardTone.Warning, "Unidade concluída: baú liberado na trilha"))
        val quests = summary.questsCompleted
        if (quests.isNotEmpty()) {
            add(
                RewardItem(
                    if (quests.size == 1) "1 missão" else "${quests.size} missões", GameIcon.Target, RewardTone.Success,
                    "Missões cumpridas: ${quests.joinToString()}",
                ),
            )
        }
    }
    return CompletionData(
        title = when {
            !rewarded -> "Treino concluído!"
            isExam && perfect -> "Prova gabaritada!"
            isExam -> "Prova concluída!"
            !isLesson -> "Treino concluído!"
            perfect -> "Lição perfeita!"
            else -> "Lição concluída!"
        },
        subtitle = title,
        note = if (!rewarded) "Treino para estudar: sem XP e sem coração. Eles vêm das revisões no dia certo." else null,
        pose = when {
            perfect -> MascotPose.Celebrate
            summary.streakIncreased || summary.newAchievements.isNotEmpty() -> MascotPose.Proud
            else -> MascotPose.Happy
        },
        xp = summary.totalXp.takeIf { rewarded },
        stats = stats,
        rewards = rewards,
        achievements = summary.newAchievements.map { AchievementItem(it.title, it.description) },
    )
}
