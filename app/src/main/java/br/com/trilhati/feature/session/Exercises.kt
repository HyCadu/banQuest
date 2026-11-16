package br.com.trilhati.feature.session

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.trilhati.domain.model.Question
import br.com.trilhati.domain.model.QuestionType
import br.com.trilhati.ui.components.OptionCard
import br.com.trilhati.ui.components.OptionState
import br.com.trilhati.ui.theme.CodeTextStyle
import br.com.trilhati.ui.theme.Trilha

/** Desenha o exercício conforme o tipo da questão. Cada tipo tem sua interação própria. */
@Composable
fun ExerciseBody(
    question: Question,
    exercise: ExerciseState,
    phase: Phase,
    actions: SessionActions,
    modifier: Modifier = Modifier,
) {
    when (exercise) {
        is ExerciseState.Choice -> ChoiceExercise(question, exercise, phase, actions.onSelect, modifier)
        is ExerciseState.Match -> MatchExercise(question, exercise, phase, actions, modifier)
        is ExerciseState.Order -> OrderExercise(question, exercise, phase, actions, modifier)
    }
}

/** Funções que a tela de sessão dispara. */
class SessionActions(
    val onSelect: (Int) -> Unit = {},
    val onCheck: () -> Unit = {},
    val onContinue: () -> Unit = {},
    val onMatchLeft: (Int) -> Unit = {},
    val onMatchRight: (Int) -> Unit = {},
    val onOrderPlace: (Int) -> Unit = {},
    val onOrderRemove: (Int) -> Unit = {},
    val onDontKnow: () -> Unit = {},
)

@Composable
private fun ChoiceExercise(question: Question, ex: ExerciseState.Choice, phase: Phase, onSelect: (Int) -> Unit, modifier: Modifier) {
    val letters = if (question.type == QuestionType.TrueFalse) listOf("C", "E") else listOf("A", "B", "C", "D", "E")
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        question.options.forEachIndexed { index, text ->
            val state = when {
                phase is Phase.Feedback && index == question.answer -> OptionState.Correct
                phase is Phase.Feedback && index == ex.selected -> OptionState.Wrong
                phase is Phase.Feedback -> OptionState.Dimmed
                index == ex.selected -> OptionState.Selected
                else -> OptionState.Idle
            }
            OptionCard(letters.getOrElse(index) { "?" }, text, state, onClick = { onSelect(index) }, enabled = phase == Phase.Answering)
        }
    }
}

@Composable
private fun MatchExercise(question: Question, ex: ExerciseState.Match, phase: Phase, actions: SessionActions, modifier: Modifier) {
    val enabled = phase == Phase.Answering
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Toque num termo e na definição.", style = MaterialTheme.typography.bodySmall, color = Trilha.colors.inkSoft)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                question.pairs.forEachIndexed { index, pair ->
                    val state = when {
                        index in ex.solved -> TileState.Solved
                        ex.selectedLeft == index -> TileState.Selected
                        ex.wrongPair?.first == index -> TileState.Wrong
                        else -> TileState.Idle
                    }
                    MatchTile(pair.left, state, enabled && index !in ex.solved, onClick = { actions.onMatchLeft(index) })
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ex.rightOrder.forEachIndexed { position, pairIndex ->
                    val state = when {
                        pairIndex in ex.solved -> TileState.Solved
                        ex.wrongPair?.second == pairIndex -> TileState.Wrong
                        else -> TileState.Idle
                    }
                    MatchTile(question.pairs[pairIndex].right, state, enabled && pairIndex !in ex.solved && ex.selectedLeft != null, onClick = { actions.onMatchRight(position) })
                }
            }
        }
    }
}

private enum class TileState { Idle, Selected, Solved, Wrong }

@Composable
private fun MatchTile(text: String, state: TileState, enabled: Boolean, onClick: () -> Unit) {
    val c = Trilha.colors
    val (bg, border) = when (state) {
        TileState.Idle -> c.surface to c.outline
        TileState.Selected -> c.primarySoft to c.primary
        TileState.Solved -> c.successSoft to c.success
        TileState.Wrong -> c.errorSoft to c.error
    }
    val shape = RoundedCornerShape(16.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .alpha(if (state == TileState.Solved) 0.55f else 1f)
            .semantics {
                role = Role.Button
                if (state == TileState.Solved) stateDescription = "já associado"
            }
            .background(bg, shape)
            .border(2.dp, border, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = c.ink, textAlign = TextAlign.Center)
    }
}

@Composable
private fun OrderExercise(question: Question, ex: ExerciseState.Order, phase: Phase, actions: SessionActions, modifier: Modifier) {
    val c = Trilha.colors
    val enabled = phase == Phase.Answering
    val feedback = phase as? Phase.Feedback
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Toque nas linhas na ordem certa. Toque de novo em uma linha colocada para tirá-la.", style = MaterialTheme.typography.bodySmall, color = c.inkSoft)
        Column(
            Modifier.fillMaxWidth().heightIn(min = 120.dp).background(c.surfaceAlt, RoundedCornerShape(16.dp)).border(2.dp, if (feedback == null) c.outline else if (feedback.correct) c.success else c.error, RoundedCornerShape(16.dp)).padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (ex.placed.isEmpty()) Text("Sua resposta aparece aqui", style = MaterialTheme.typography.bodySmall, color = c.inkSoft, modifier = Modifier.padding(6.dp))
            ex.placed.forEach { index -> CodeChip(question.lines[index], enabled) { actions.onOrderRemove(index) } }
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ex.pool.forEach { index -> CodeChip(question.lines[index], enabled) { actions.onOrderPlace(index) } }
        }
    }
}

@Composable
private fun CodeChip(line: String, enabled: Boolean, onClick: () -> Unit) {
    val c = Trilha.colors
    val shape = RoundedCornerShape(12.dp)
    Box(
        Modifier.fillMaxWidth().semantics { role = Role.Button }.background(c.surface, shape).border(2.dp, c.outline, shape).clickable(enabled = enabled, onClick = onClick).padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(line.ifBlank { " " }, style = CodeTextStyle, color = c.ink)
    }
}
