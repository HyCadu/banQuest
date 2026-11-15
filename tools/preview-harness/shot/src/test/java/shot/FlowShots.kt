package shot

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import br.com.trilhati.data.prefs.Settings
import br.com.trilhati.domain.model.QuestionType
import br.com.trilhati.domain.pace.PaceConfig
import br.com.trilhati.domain.lesson.LessonRun
import br.com.trilhati.domain.progress.*
import br.com.trilhati.feature.app.AppActions
import br.com.trilhati.feature.main.MainContent
import br.com.trilhati.feature.main.MainTab
import br.com.trilhati.feature.session.*
import br.com.trilhati.feature.settings.SettingsContent
import br.com.trilhati.ui.mascot.MascotCharacter
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w412dp-h892dp-xxhdpi")
class FlowShots {
    @get:Rule val rule = createComposeRule()
    private val snack = SnackbarHostState()

    private fun tab(name: String, t: MainTab, progress: Boolean, dark: Boolean = false, character: String = "Guaiamum") =
        rule.shot(name, dark) { MainContent(fakeState(progress, character), NOW_MS, AppActions(), snack, initialTab = t, animated = false) }

    @Test fun trilhaFresh() = tab("t_path_fresh.png", MainTab.Path, false)
    @Test fun trilhaProgress() = tab("t_path_progress.png", MainTab.Path, true, character = "Tartaruga")
    @Test fun trilhaDark() = tab("t_path_dark.png", MainTab.Path, true, dark = true, character = "Preguica")
    @Test fun practice() = tab("t_practice.png", MainTab.Practice, true)
    @Test fun leagues() = tab("t_leagues.png", MainTab.Leagues, true)
    @Test fun shop() = tab("t_shop.png", MainTab.Shop, true)
    @Test fun profile() = tab("t_profile.png", MainTab.Profile, true)
    @Test fun settings() = rule.shot("t_settings.png") { SettingsContent(Settings(), 50, 50, {}, {}, {}, {}, {}, {}, {}, {}) }

    private fun playing(type: QuestionType, answered: Int = 2, phase: Phase = Phase.Answering, exercise: ExerciseState? = null, idx: Int = 0, combo: Int = 4): SessionUi.Playing {
        val q = course[0].lessons.flatMap { it.questions }.filter { it.type == type }[idx]
        val ex = exercise ?: when (type) {
            QuestionType.Match -> ExerciseState.Match((q.pairs.indices.toList().reversed()))
            QuestionType.OrderCode -> ExerciseState.Order(q.lines.indices.toList().reversed())
            else -> ExerciseState.Choice()
        }
        return SessionUi.Playing("Modelo ER e normalização", q, ex, phase, 0.375f, combo, 4, false, PaceConfig(), 180_000, NOW_MS - 95_000, answered, 42_000, true)
    }

    private fun session(name: String, ui: SessionUi, dark: Boolean = false) =
        rule.shot(name, dark) { SessionContent(ui, mutableStateOf(NOW_MS), MascotCharacter.Guaiamum, SessionActions(), {}, {}, {}, {}, animated = false) }

    @Test fun sMcq() = session("s_mcq.png", playing(QuestionType.Mcq5, exercise = ExerciseState.Choice(2)))
    @Test fun sMcqCorrect() { val p = playing(QuestionType.Mcq5); session("s_mcq_ok.png", p.copy(exercise = ExerciseState.Choice(p.question.answer), phase = Phase.Feedback(true, null, 15))) }
    @Test fun sMcqWrong() { val p = playing(QuestionType.Mcq5); val wrong = (p.question.answer + 1) % 5; session("s_mcq_bad.png", p.copy(exercise = ExerciseState.Choice(wrong), phase = Phase.Feedback(false, p.question.options[p.question.answer], 0))) }
    @Test fun sCase() = session("s_case.png", playing(QuestionType.Case, idx = 3, exercise = ExerciseState.Choice(null)))
    @Test fun sCode() = session("s_code.png", playing(QuestionType.CodeOutput, exercise = ExerciseState.Choice(1)))
    @Test fun sMatch() { val p = playing(QuestionType.Match); session("s_match.png", p.copy(exercise = ExerciseState.Match(listOf(2, 0, 3, 1), solved = setOf(0), selectedLeft = 2))) }
    @Test fun sOrder() { val p = playing(QuestionType.OrderCode); session("s_order.png", p.copy(exercise = ExerciseState.Order(listOf(3, 0, 4), listOf(1, 2)))) }
    @Test fun sTf() = session("s_tf.png", playing(QuestionType.TrueFalse, exercise = ExerciseState.Choice(0)))
    @Test fun sDark() = session("s_mcq_dark.png", playing(QuestionType.Mcq5, exercise = ExerciseState.Choice(1)), dark = true)
    @Test fun sOver() { val p = playing(QuestionType.Mcq5); session("s_over.png", p.copy(startedAtMs = NOW_MS - 200_000, combo = 0)) }
    @Test fun sNoHearts() = session("s_nohearts.png", SessionUi.NoHearts(120, 1_240_000, true))

    @Test fun result() {
        val qs = course[0].lessons[0].questions.take(8)
        var run = LessonRun.start(qs)
        run = LessonRun.answer(run, false, 150_000, 180_000)
        repeat(7) { run = LessonRun.answer(run, true, 130_000, 180_000) }
        run = LessonRun.answer(run, true, 90_000, 180_000)
        val state = fakeState(true)
        val sr = SessionResult(SessionKind.Lesson, "u1l1", "u1", run.records, run.bestCombo, run.perfect)
        val out = ProgressProcessor.apply(
            ProgressState(state.snapshot.stats, state.snapshot.lessonProgress, emptyMap(), DailyLog(TODAY, 40), state.quests, emptySet(), state.snapshot.league, 100),
            sr, TODAY, NOW_MS, { 1 }, 0,
        )
        session("s_result.png", SessionUi.Finished("Modelo ER e normalização", SessionSource.Lesson, out, run, PaceConfig()))
    }
}
