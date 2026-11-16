package br.com.trilhati.feature.session

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import br.com.trilhati.domain.model.Question
import br.com.trilhati.domain.model.QuestionType
import br.com.trilhati.domain.pace.PaceEngine
import br.com.trilhati.domain.pace.PaceZone
import br.com.trilhati.domain.pace.TimerMode
import br.com.trilhati.ui.components.ChunkyButton
import br.com.trilhati.ui.components.ChunkyStyle
import br.com.trilhati.ui.components.CodeBlock
import br.com.trilhati.ui.components.ComboBadge
import br.com.trilhati.ui.components.FeedbackPanel
import br.com.trilhati.ui.components.GameProgressBar
import br.com.trilhati.ui.components.PaceChip
import br.com.trilhati.ui.components.PaceRing
import br.com.trilhati.ui.components.TrilhaDialog
import br.com.trilhati.ui.components.ExamRefChip
import br.com.trilhati.ui.components.UnreviewedBadge
import br.com.trilhati.ui.icons.GameIcon
import br.com.trilhati.ui.icons.GameIconView
import br.com.trilhati.ui.icons.IconColors
import br.com.trilhati.ui.mascot.Mascot
import br.com.trilhati.ui.mascot.MascotCharacter
import br.com.trilhati.ui.mascot.MascotPose
import br.com.trilhati.ui.theme.Spacing
import br.com.trilhati.ui.theme.Trilha

@Composable
fun SessionScreen(onExit: () -> Unit, onOpenErrors: () -> Unit, viewModel: SessionViewModel = hiltViewModel()) {
    val ui by viewModel.ui.collectAsState()
    val now = viewModel.now.collectAsState(initial = 0L)
    val characterName by viewModel.character.collectAsState()
    val character = MascotCharacter.entries.firstOrNull { it.name == characterName } ?: MascotCharacter.Guaiamum
    val actions = remember(viewModel) {
        SessionActions(
            onSelect = viewModel::select, onCheck = viewModel::check, onContinue = viewModel::continueAfterFeedback,
            onMatchLeft = viewModel::matchLeft, onMatchRight = viewModel::matchRight,
            onOrderPlace = viewModel::orderPlace, onOrderRemove = viewModel::orderRemove,
        )
    }
    SessionContent(
        ui = ui, now = now, character = character, actions = actions, onExit = onExit, onOpenErrors = onOpenErrors,
        onRefill = viewModel::refillHeartsAndRetry, onRetry = viewModel::retryAfterWaiting,
    )
}

/** Tela cheia da sessão. Sem ViewModel, para poder ser pré-visualizada. */
@Composable
fun SessionContent(
    ui: SessionUi,
    now: State<Long>,
    character: MascotCharacter,
    actions: SessionActions,
    onExit: () -> Unit,
    onOpenErrors: () -> Unit,
    onRefill: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    animated: Boolean = true,
) {
    var confirmExit by remember { mutableStateOf(false) }
    val playing = ui as? SessionUi.Playing
    BackHandler(enabled = playing != null) { confirmExit = true }

    Box(modifier.fillMaxSize().background(Trilha.colors.background)) {
        when (ui) {
            SessionUi.Loading, SessionUi.Saving -> CenterMessage(character, "Preparando…", animated)
            is SessionUi.Empty -> EmptyScreen(ui, character, onExit, animated)
            is SessionUi.NoHearts -> NoHeartsScreen(ui, character, onRefill, onRetry, onOpenErrors, onExit, animated)
            is SessionUi.Playing -> PlayingScreen(ui, now, character, actions, onClose = { confirmExit = true }, animated = animated)
            is SessionUi.Finished -> ResultScreen(ui, character, onContinue = onExit, animated = animated)
        }
    }

    if (confirmExit) {
        TrilhaDialog(
            onDismiss = { confirmExit = false },
            title = "Sair da lição?",
            content = { Text("Você perde o progresso desta lição. Os corações já gastos não voltam.", style = MaterialTheme.typography.bodyMedium, color = Trilha.colors.ink) },
            actions = {
                ChunkyButton("Continuar estudando", { confirmExit = false }, Modifier.fillMaxWidth())
                ChunkyButton("Sair", { confirmExit = false; onExit() }, Modifier.fillMaxWidth(), ChunkyStyle.Neutral)
            },
        )
    }
}

@Composable
private fun PlayingScreen(ui: SessionUi.Playing, now: State<Long>, character: MascotCharacter, actions: SessionActions, onClose: () -> Unit, animated: Boolean) {
    val c = Trilha.colors
    val haptic = LocalHapticFeedback.current
    val feedback = ui.phase as? Phase.Feedback
    LaunchedEffect(feedback) {
        if (feedback != null && ui.haptics) {
            haptic.performHapticFeedback(if (feedback.correct || feedback.corrected) HapticFeedbackType.Confirm else HapticFeedbackType.Reject)
        }
    }
    val q = ui.question
    val canCheck = when (val ex = ui.exercise) {
        is ExerciseState.Choice -> ex.selected != null
        is ExerciseState.Order -> ex.pool.isEmpty()
        is ExerciseState.Match -> false
    }
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        // Cabeçalho: X, progresso, cronômetro e corações
        Row(Modifier.fillMaxWidth().padding(horizontal = Spacing.screen, vertical = Spacing.sm), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier.size(40.dp).semantics { contentDescription = "Sair da lição"; role = Role.Button }.clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) { GameIconView(GameIcon.Close, size = 26.dp, colors = IconColors.flat(c.inkSoft, c.inkSoft)) }
            GameProgressBar(ui.progress, Modifier.weight(1f), height = 16.dp)
            if (ui.hearts != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    GameIconView(GameIcon.Heart, size = 26.dp)
                    Text(ui.hearts.toString(), style = MaterialTheme.typography.titleMedium, color = c.ink)
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = Spacing.screen), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LessonBuddy(character, ui, now, animated)
            // Os chips quebram linha sozinhos com fonte grande, sem espremer o anel do cronômetro.
            androidx.compose.foundation.layout.FlowRow(
                Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (ui.combo >= 3) ComboBadge(ui.combo)
                if (ui.pace.mode == TimerMode.Pace && ui.answered > 0) PaceChip((ui.slackMs / 1000).toInt())
                if (ui.pace.mode == TimerMode.Pace && ui.phase == Phase.Answering) LessonTimeChip(ui, now)
            }
            if (ui.pace.mode == TimerMode.Pace && ui.phase == Phase.Answering) TimerRing(ui, now)
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = Spacing.screen, vertical = Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            QuestionTags(q)
            if (ui.retry) Text("Repetição de um erro: acertando agora vale 5 XP.", style = MaterialTheme.typography.labelMedium, color = c.warningText)
            Text(q.stem, style = MaterialTheme.typography.titleMedium, color = c.ink)
            q.code?.let { CodeBlock(it) }
            ExerciseBody(q, ui.exercise, ui.phase, actions)
        }
        // Rodapé: verificar ou painel de feedback
        Column(Modifier.fillMaxWidth().navigationBarsPadding()) {
            if (feedback == null) {
                if (q.type != QuestionType.Match) {
                    ChunkyButton("Verificar", actions.onCheck, Modifier.fillMaxWidth().padding(Spacing.screen), ChunkyStyle.Primary, enabled = canCheck)
                } else {
                    Box(Modifier.size(0.dp))
                }
            }
            AnimatedVisibility(visible = feedback != null, enter = slideInVertically { it }, exit = slideOutVertically { it }) {
                if (feedback != null) {
                    FeedbackPanel(
                        correct = feedback.correct,
                        title = when {
                            feedback.corrected -> "Pares completos!"
                            feedback.correct -> praise(ui.answered)
                            else -> consolation(ui.answered)
                        } + if ((feedback.correct || feedback.corrected) && feedback.xp > 0) "  +${feedback.xp} XP" else "",
                        message = buildFeedbackMessage(feedback, q.explanation),
                        character = character,
                        onContinue = actions.onContinue,
                        animated = animated,
                        partial = feedback.corrected,
                        pose = LessonMascot.feedback(
                            when {
                                feedback.correct -> AnswerOutcome.Correct
                                feedback.corrected -> AnswerOutcome.Corrected
                                else -> AnswerOutcome.Wrong
                            },
                            ui.combo,
                        ),
                    )
                }
            }
        }
    }
}

/** Etiqueta da prova (questões reais) e selo de revisão, lado a lado; quebram linha com fonte grande. */
@Composable
private fun QuestionTags(q: Question) {
    val ref = q.ref
    if (ref == null && !q.showUnreviewedBadge) return
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (ref != null) ExamRefChip(ref.label)
        if (q.showUnreviewedBadge) UnreviewedBadge(explanationOnly = q.isFromExam)
    }
}

private fun buildFeedbackMessage(feedback: Phase.Feedback, explanation: String): String = when {
    feedback.corrected -> {
        val slips = if (feedback.slips == 1) "1 associação" else "${feedback.slips} associações"
        "Você errou $slips no caminho, então ela volta em Praticar erros. Sem perder coração.\n\n$explanation"
    }
    feedback.correct || feedback.correctText == null -> explanation
    else -> "Resposta correta: ${feedback.correctText}\n\n$explanation"
}

private val PRAISE = listOf("Mandou bem!", "Isso aí!", "Perfeito!", "Boa, craque!", "Na mosca!", "Gabaritou essa!")
private val CONSOLATION = listOf("Quase lá!", "Opa, escorregou!", "Acontece!", "Não foi dessa vez.", "Anota aí!")
private fun praise(n: Int) = PRAISE[n % PRAISE.size]
private fun consolation(n: Int) = CONSOLATION[n % CONSOLATION.size]

/**
 * Personagem pequeno ao lado dos chips: pensa enquanto você responde e fica nervoso quando o cronômetro visível
 * passa do âmbar. Só o estado do cronômetro (não cada tique) provoca recomposição.
 */
@Composable
private fun LessonBuddy(character: MascotCharacter, ui: SessionUi.Playing, now: State<Long>, animated: Boolean) {
    val timerVisible = ui.pace.mode == TimerMode.Pace && ui.phase == Phase.Answering
    val zone by remember(ui.startedAtMs, ui.targetMs, timerVisible) {
        androidx.compose.runtime.derivedStateOf {
            if (timerVisible) PaceEngine.zone((now.value - ui.startedAtMs).coerceAtLeast(0), ui.targetMs) else null
        }
    }
    val pose = if (ui.phase == Phase.Answering) LessonMascot.answering(zone) else MascotPose.Idle
    Mascot(character, pose, Modifier.size(52.dp), animated = animated, decorative = true)
}

@Composable
private fun TimerRing(ui: SessionUi.Playing, now: State<Long>) {
    // Só este trecho lê o relógio de 250 ms; o resto da tela não recompõe a cada tique.
    val elapsed = (now.value - ui.startedAtMs).coerceAtLeast(0)
    val zone = PaceEngine.zone(elapsed, ui.targetMs)
    val label = if (zone == PaceZone.Over) "+" + PaceEngine.format(elapsed - ui.targetMs) else PaceEngine.format(ui.targetMs - elapsed)
    PaceRing(PaceEngine.remainingFraction(elapsed, ui.targetMs), label, zone, size = 60.dp)
}

/** "Prova: tempo restante da lição" (PLAN.md §7): quanto sobra do orçamento das questões que faltam. */
@Composable
private fun LessonTimeChip(ui: SessionUi.Playing, now: State<Long>) {
    val c = Trilha.colors
    val elapsed = (now.value - ui.startedAtMs).coerceAtLeast(0)
    val left = ui.remaining * ui.targetMs - elapsed
    val late = left < 0
    androidx.compose.material3.Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
        color = if (late) c.errorSoft else c.surfaceAlt,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, if (late) c.error else c.outline),
    ) {
        Text(
            "Lição " + (if (late) "-" else "") + PaceEngine.format(left),
            Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (late) c.errorText else c.inkSoft,
        )
    }
}
