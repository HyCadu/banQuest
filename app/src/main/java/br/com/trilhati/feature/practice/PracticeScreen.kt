package br.com.trilhati.feature.practice

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
import br.com.trilhati.domain.srs.SrsEngine
import br.com.trilhati.feature.app.AppActions
import br.com.trilhati.feature.app.AppState
import br.com.trilhati.ui.components.ChunkyButton
import br.com.trilhati.ui.components.ChunkyStyle
import br.com.trilhati.ui.components.InfoButton
import br.com.trilhati.ui.components.GameProgressBar
import br.com.trilhati.ui.components.MascotSays
import br.com.trilhati.ui.icons.GameIcon
import br.com.trilhati.ui.icons.GameIconView
import br.com.trilhati.ui.mascot.MascotCharacter
import br.com.trilhati.ui.mascot.MascotPose
import br.com.trilhati.ui.theme.Spacing
import br.com.trilhati.ui.theme.Trilha

/** Pontos fracos: lições com baixa taxa de acerto de primeira, para o aluno saber o que revisar. */
data class WeakSpot(val lessonTitle: String, val accuracy: Int, val answered: Int)

fun weakSpots(state: AppState, limit: Int = 3): List<WeakSpot> {
    val lessons = state.course.flatMap { it.lessons }.associateBy { it.id }
    return state.snapshot.questionStats.values
        .groupBy { it.lessonId }
        .mapNotNull { (lessonId, stats) ->
            val answered = stats.sumOf { it.times }
            val correct = stats.sumOf { it.correct }
            val lesson = lessons[lessonId] ?: return@mapNotNull null
            if (answered < 4) null else WeakSpot(lesson.title, correct * 100 / answered, answered)
        }
        .filter { it.accuracy < 75 }
        .sortedBy { it.accuracy }
        .take(limit)
}

/** Aba Praticar: revisão espaçada dos erros (intervalos 1, 3, 7 e 14 dias) e revisão livre. */
@Composable
fun PracticeContent(state: AppState, actions: AppActions, modifier: Modifier = Modifier, animated: Boolean = true) {
    val c = Trilha.colors
    val character = MascotCharacter.entries.firstOrNull { it.name == state.snapshot.profile.character } ?: MascotCharacter.Guaiamum
    val due = state.snapshot.dueCount
    val bank = state.snapshot.errorBankSize
    val weak = weakSpots(state)

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = Spacing.screen, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Praticar erros", Modifier.weight(1f), style = MaterialTheme.typography.displaySmall, color = c.primaryText)
            InfoButton(
                "Como funciona",
                "Toda questão que você erra volta para revisão depois de ${SrsEngine.INTERVALS_DAYS[0]}, " +
                    "${SrsEngine.INTERVALS_DAYS[1]}, ${SrsEngine.INTERVALS_DAYS[2]} e ${SrsEngine.INTERVALS_DAYS[3]} dias, se você for acertando. " +
                    "Errou de novo? O ciclo recomeça. Terminar um treino devolve 1 coração.",
            )
        }
        MascotSays(
            character = character,
            pose = if (due > 0) MascotPose.Idle else MascotPose.Happy,
            text = when {
                bank == 0 && !state.hasAnyCompletedLesson -> "Faça uma lição e eu guardo seus erros."
                bank == 0 -> "Nenhum erro guardado. Mandou bem!"
                due > 0 -> "$due ${if (due == 1) "erro esperando" else "erros esperando"} revisão. Bora?"
                else -> "Nada vencido hoje."
            },
            animated = animated,
        )
        Row(
            Modifier.fillMaxWidth().background(c.surface, RoundedCornerShape(20.dp)).border(2.dp, c.outline, RoundedCornerShape(20.dp)).padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Counter("Vencidas", due, GameIcon.Clock)
            Counter("Erros guardados", bank, GameIcon.Target)
        }
        when {
            due > 0 -> ChunkyButton("Treinar erros ($due)", actions.openErrors, Modifier.fillMaxWidth(), ChunkyStyle.Primary)
            bank > 0 -> ChunkyButton("Treinar os mais antigos", actions.openErrors, Modifier.fillMaxWidth(), ChunkyStyle.Neutral)
        }
        ChunkyButton(
            "Revisão livre", actions.openReview, Modifier.fillMaxWidth(), ChunkyStyle.Secondary,
            enabled = state.hasAnyCompletedLesson,
        )
        if (!state.hasAnyCompletedLesson) {
            Text("Libera depois da primeira lição.", style = MaterialTheme.typography.bodySmall, color = c.inkSoft)
        }
        if (weak.isNotEmpty()) {
            Text("Seus pontos fracos", style = MaterialTheme.typography.headlineSmall, color = c.ink)
            weak.forEach { w ->
                Column(
                    Modifier.fillMaxWidth().background(c.surface, RoundedCornerShape(16.dp)).border(2.dp, c.outline, RoundedCornerShape(16.dp)).padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(w.lessonTitle, style = MaterialTheme.typography.titleSmall, color = c.ink)
                    GameProgressBar(w.accuracy / 100f, color = c.error, height = 10.dp)
                    Text("${w.accuracy}% de acerto", style = MaterialTheme.typography.bodySmall, color = c.inkSoft)
                }
            }
        }
    }
}

@Composable
private fun Counter(label: String, value: Int, icon: GameIcon) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            GameIconView(icon, size = 26.dp)
            Text(value.toString(), style = MaterialTheme.typography.headlineMedium, color = Trilha.colors.ink)
        }
        Text(label, style = MaterialTheme.typography.labelMedium, color = Trilha.colors.inkSoft)
    }
}
