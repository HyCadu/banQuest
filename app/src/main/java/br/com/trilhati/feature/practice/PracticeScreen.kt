package br.com.trilhati.feature.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import br.com.trilhati.domain.exam.ExamPart
import br.com.trilhati.domain.exam.ExamRules
import br.com.trilhati.domain.exam.PastExam
import br.com.trilhati.domain.model.AnswerKey
import br.com.trilhati.domain.pace.PaceConfig
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

/** Aba Praticar: revisão espaçada dos erros (intervalos 1, 3, 7 e 14 dias), revisão livre e provas anteriores. */
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
                    "Ela só sai dos erros guardados depois de acertar as ${SrsEngine.INTERVALS_DAYS.size} revisões, cada uma no seu dia. " +
                    "Errou de novo? O ciclo recomeça. XP e 1 coração vêm só da revisão das vencidas; treinar antes do dia " +
                    "e a revisão livre servem para estudar, sem XP.",
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
        if (bank > 0) {
            Text(
                "Cada erro sai daqui depois de ${SrsEngine.INTERVALS_DAYS.size} revisões certas, em dias diferentes.",
                style = MaterialTheme.typography.bodySmall, color = c.inkSoft,
            )
        }
        when {
            due > 0 -> ChunkyButton("Treinar erros ($due)", actions.openErrors, Modifier.fillMaxWidth(), ChunkyStyle.Primary)
            bank > 0 -> ChunkyButton("Treinar os mais antigos (sem XP)", actions.openErrors, Modifier.fillMaxWidth(), ChunkyStyle.Neutral)
        }
        ChunkyButton(
            "Revisão livre (sem XP)", actions.openReview, Modifier.fillMaxWidth(), ChunkyStyle.Secondary,
            enabled = state.hasAnyCompletedLesson,
        )
        if (!state.hasAnyCompletedLesson) {
            Text("Libera depois da primeira lição.", style = MaterialTheme.typography.bodySmall, color = c.inkSoft)
        }
        if (state.exams.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Provas anteriores", Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall, color = c.ink)
                InfoButton(
                    "Provas anteriores",
                    "Provas de concursos passados, só com as questões que valem para o edital atual. Abrir uma prova gasta " +
                        "1 coração, errar não tira coração e o XP só vem se você responder todas as questões." +
                        if (state.targetExam == br.com.trilhati.domain.exam.TargetExam.TecnicoBancario) {
                            " As provas daqui são de cargos de TI: para o Técnico Bancário aparecem só os Conhecimentos Gerais, " +
                                "que são os mesmos do seu edital."
                        } else {
                            ""
                        },
                )
            }
            state.exams.forEach { exam -> ExamCard(exam, state.settings.paceConfig) { actions.openExam(exam.id) } }
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

/** Cartão de uma prova anterior: nome, número de questões, tempo estimado e o aviso de gabarito. */
@Composable
private fun ExamCard(exam: PastExam, pace: PaceConfig, onClick: () -> Unit) {
    val c = Trilha.colors
    val all = exam.questions(ExamPart.Full)
    val time = ExamRules.durationLabel(all.sumOf { pace.targetMs(it) })
    val parts = when {
        exam.specific.isNotEmpty() && exam.general.isNotEmpty() -> "Específicos e Gerais"
        exam.specific.isEmpty() -> "Só Gerais" // o Técnico Bancário vê só os Gerais das provas de TI
        else -> "Específicos"
    }
    val key = when (exam.answerKey) {
        AnswerKey.Final -> null
        AnswerKey.Preliminary -> "Gabarito preliminar"
        AnswerKey.Unconfirmed -> "Gabarito não confirmado"
    }
    Row(
        Modifier.fillMaxWidth()
            .background(c.surface, RoundedCornerShape(16.dp)).border(2.dp, c.outline, RoundedCornerShape(16.dp))
            .clickable(onClickLabel = "abrir a prova", role = Role.Button, onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        GameIconView(GameIcon.Book, size = 34.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(exam.title, style = MaterialTheme.typography.titleSmall, color = c.ink)
            Text("${all.size} questões · $parts · cerca de $time", style = MaterialTheme.typography.bodySmall, color = c.inkSoft)
            if (key != null) Text(key, style = MaterialTheme.typography.labelMedium, color = c.warningText)
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
