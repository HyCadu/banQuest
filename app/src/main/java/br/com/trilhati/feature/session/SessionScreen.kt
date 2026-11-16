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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import br.com.trilhati.domain.model.Question
import br.com.trilhati.domain.model.QuestionLevel
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
import br.com.trilhati.ui.components.ConceptHint
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
            onOrderPlace = viewModel::orderPlace, onOrderRemove = viewModel::orderRemove, onDontKnow = viewModel::dontKnow,
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
            ui.heartsText?.let { text -> HeartCounter(text, ui.hearts ?: 0, ui.halfHeart, animated) }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = Spacing.screen), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LessonBuddy(character, ui, now, animated)
            // Só o que pede atenção: combo a partir de 3 e o atraso (folga não precisa de aviso; aparece no resumo).
            androidx.compose.foundation.layout.FlowRow(
                Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (ui.combo >= 3) ComboBadge(ui.combo)
                if (ui.pace.mode == TimerMode.Pace && ui.answered > 0 && ui.slackMs < 0) PaceChip((ui.slackMs / 1000).toInt())
            }
            if (ui.pace.mode == TimerMode.Pace && ui.phase == Phase.Answering) TimerRing(ui, now)
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = Spacing.screen, vertical = Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            // Uma faixa curta antes do enunciado: origem, selo de revisão, repetição e a dica do conceito (fechada).
            QuestionMeta(q, ui)
            Text(q.stem, style = MaterialTheme.typography.titleMedium, color = c.ink)
            q.code?.let { CodeBlock(it) }
            ExerciseBody(q, ui.exercise, ui.phase, actions)
        }
        // Rodapé: verificar ou painel de feedback
        Column(Modifier.fillMaxWidth().navigationBarsPadding()) {
            if (feedback == null) {
                Row(
                    Modifier.fillMaxWidth().padding(Spacing.screen),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // "Não sei" (estoque limitado): só nos exercícios de ensino; não custa coração e o exercício volta no fim.
                    if (ui.canAskHelp && ui.helps > 0) {
                        ChunkyButton(
                            "Não sei (${ui.helps})", actions.onDontKnow,
                            if (q.type == QuestionType.Match) Modifier.fillMaxWidth() else Modifier,
                            ChunkyStyle.Neutral, enabled = ui.helps > 0,
                        )
                    }
                    if (q.type != QuestionType.Match) {
                        ChunkyButton("Verificar", actions.onCheck, Modifier.weight(1f), ChunkyStyle.Primary, enabled = canCheck)
                    }
                }
            }
            AnimatedVisibility(visible = feedback != null, enter = slideInVertically { it }, exit = slideOutVertically { it }) {
                if (feedback != null) {
                    FeedbackPanel(
                        correct = feedback.correct,
                        title = when {
                            feedback.skipped -> "Agora você sabe!"
                            feedback.corrected -> "Pares completos!"
                            feedback.correct -> praise(ui.answered)
                            else -> consolation(ui.answered)
                        } + if ((feedback.correct || feedback.corrected) && feedback.xp > 0) "  +${feedback.xp} XP" else "",
                        message = buildFeedbackMessage(feedback, q.explanation, ui.retry),
                        character = character,
                        onContinue = actions.onContinue,
                        animated = animated,
                        partial = feedback.corrected || feedback.skipped,
                        pose = LessonMascot.feedback(
                            when {
                                feedback.correct -> AnswerOutcome.Correct
                                feedback.corrected || feedback.skipped -> AnswerOutcome.Corrected
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

/**
 * Faixa curta antes do enunciado (normalmente uma linha): origem da questão ("FCC · Teresina 2016 · Q22"), o selo
 * "não revisada", a ficha de repetição e a dica do conceito, fechada. Tocar na origem ou no selo abre os detalhes.
 */
@Composable
private fun QuestionMeta(q: Question, ui: SessionUi.Playing) {
    val c = Trilha.colors
    val ref = q.ref
    val adapted = q.adaptedFrom
    val concept = ui.concept?.takeIf { q.level == QuestionLevel.Learn || ui.newConcept }
    var details by remember(q.id) { mutableStateOf(false) }
    var hintOpen by remember(q.id, ui.retry) { mutableStateOf(false) }
    val source = ref?.compactLabel ?: adapted?.let { "Adaptada · ${it.ref.compactLabel}" }
    if (source == null && !q.showUnreviewedBadge && !ui.retry && concept == null) return
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        if (source != null || q.showUnreviewedBadge) {
            Row(
                Modifier.semantics(mergeDescendants = true) { role = Role.Button; contentDescription = "Sobre esta questão: ${source ?: "não revisada"}" }
                    .clickable { details = true },
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (source != null) {
                    GameIconView(GameIcon.Book, size = 16.dp)
                    Text(source, style = MaterialTheme.typography.labelMedium, color = c.inkSoft, maxLines = 1)
                }
                if (q.showUnreviewedBadge) UnreviewedBadge()
            }
        }
        if (ui.retry) RetryChip()
        concept?.let { ConceptHint(it.title, it.summary, hintOpen, onToggle = { hintOpen = !hintOpen }, isNew = ui.newConcept) }
    }
    if (details) {
        TrilhaDialog(
            onDismiss = { details = false },
            title = "Sobre esta questão",
            content = {
                val lines = buildList {
                    ref?.let { add("Questão de prova real: ${it.label}.") }
                    adapted?.let { add("Exercício feito pela IA a partir da ${it.ref.shortLabel}.") }
                    if (q.showUnreviewedBadge) {
                        add(
                            if (q.isFromExam) "O enunciado e a resposta são os da prova; a explicação foi escrita por IA e ainda não foi revisada."
                            else "Gerada por IA e ainda não revisada. Se achar algo errado, anote e avise.",
                        )
                    }
                }
                Text(lines.joinToString("\n\n"), style = MaterialTheme.typography.bodyMedium, color = c.ink)
            },
            actions = { ChunkyButton("Entendi", { details = false }, Modifier.fillMaxWidth()) },
        )
    }
}

@Composable
private fun RetryChip() {
    val c = Trilha.colors
    androidx.compose.material3.Surface(
        modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = "Repetição de um erro: acertando agora vale 5 XP" },
        shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
        color = c.warningSoft,
    ) {
        Text("Repetição · 5 XP", Modifier.padding(horizontal = 10.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, color = c.warningText)
    }
}

/**
 * Corações da lição: "4,5" com o coração rachado quando há meio coração perdido (no lugar de "4½"). Treme quando um
 * coração racha ou quebra.
 */
@Composable
private fun HeartCounter(text: String, full: Int, cracked: Boolean, animated: Boolean) {
    val c = Trilha.colors
    val shake = remember { androidx.compose.animation.core.Animatable(0f) }
    var last by remember { mutableStateOf(text) }
    LaunchedEffect(text) {
        val changed = last != text
        last = text
        if (changed && animated) {
            for (x in listOf(-7f, 7f, -5f, 5f, -2f, 0f)) shake.animateTo(x, androidx.compose.animation.core.tween(45))
        }
    }
    val description = br.com.trilhati.domain.hearts.HeartsEngine.description(
        br.com.trilhati.domain.hearts.HeartsState(full, 0L, cracked),
    )
    Row(
        Modifier.graphicsLayer { translationX = shake.value * density }.semantics(mergeDescendants = true) { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        GameIconView(if (cracked) GameIcon.HeartCracked else GameIcon.Heart, size = 26.dp)
        Text(text, style = MaterialTheme.typography.titleMedium, color = c.ink)
    }
}

private fun buildFeedbackMessage(feedback: Phase.Feedback, explanation: String, retry: Boolean): String = when {
    feedback.skipped -> "Resposta: ${feedback.correctText}\n\n$explanation" +
        if (retry) "" else "\n\nEle volta no fim da lição para você tentar."
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

