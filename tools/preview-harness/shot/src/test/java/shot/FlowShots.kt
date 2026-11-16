package shot

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import br.com.trilhati.data.content.toDomain
import br.com.trilhati.data.prefs.Settings
import br.com.trilhati.domain.model.QuestionType
import br.com.trilhati.domain.pace.PaceConfig
import br.com.trilhati.domain.lesson.LessonRun
import br.com.trilhati.domain.progress.*
import br.com.trilhati.feature.app.AppActions
import br.com.trilhati.feature.main.MainContent
import br.com.trilhati.feature.main.SyncConflictDialog
import br.com.trilhati.feature.app.SocialUi
import br.com.trilhati.feature.auth.*
import br.com.trilhati.feature.ranking.RankingContent
import br.com.trilhati.feature.ranking.RankingScope
import br.com.trilhati.feature.main.MainTab
import br.com.trilhati.feature.session.*
import br.com.trilhati.feature.settings.SettingsContent
import br.com.trilhati.feature.settings.SettingsCallbacks
import br.com.trilhati.feature.settings.SettingsUi
import br.com.trilhati.ui.mascot.MascotCharacter
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
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

    private fun tab(name: String, t: MainTab, progress: Boolean, dark: Boolean = false, character: String = "Guaiamum", social: SocialUi = fakeSocial()) =
        rule.shot(name, dark) { MainContent(fakeState(progress, character), NOW_MS, AppActions(), snack, initialTab = t, animated = false, social = social) }

    @Test fun trilhaPrompt() = rule.shot("t_path_prompt.png") { MainContent(fakeState(true), NOW_MS, AppActions(), snack, animated = false, showReminderPrompt = true) }
    @Test fun trilhaFresh() = tab("t_path_fresh.png", MainTab.Path, false)
    @Test fun trilhaProgress() = tab("t_path_progress.png", MainTab.Path, true, character = "Tartaruga")
    @Test fun trilhaDark() = tab("t_path_dark.png", MainTab.Path, true, dark = true, character = "Preguica")
    @Test fun practice() = tab("t_practice.png", MainTab.Practice, true)
    @Test fun rankingTab() = tab("t_ranking.png", MainTab.Ranking, true)
    @Test fun trilhaPairRisk() = rule.shot("t_path_pair_risk.png") {
        MainContent(fakeState(true).let { it.copy(snapshot = it.snapshot.copy(dailyLogs = emptyMap())) }, NOW_MS, AppActions(), snack, animated = false, social = fakeSocial())
    }
    private val release = br.com.trilhati.domain.update.ReleaseInfo(9, "0.5.1", "https://exemplo.supabase.co/a.apk", "Correções no ranking e personagens mais rápidos.", 1)
    @Test fun trilhaUpdate() = rule.shot("t_path_update.png") {
        MainContent(fakeState(true), NOW_MS, AppActions(), snack, animated = false, social = fakeSocial(), updateBanner = release)
    }
    @Test fun updateRequired() = rule.shot("d_update_required.png") { br.com.trilhati.feature.update.UpdateRequiredDialog(release.copy(minVersionCode = 9), {}) }
    @Test fun conflict() = rule.shot("d_conflict.png") {
        SyncConflictDialog(br.com.trilhati.data.social.SyncConflict(4, "outro", "2026-11-18T10:00:00Z", 900, 6, 640, 2), {}, {})
    }

    private fun login(name: String, ui: AuthUi) = rule.shot(name) { LoginContent(ui, {}, {}, {}, {}, {}, {}, {}, {}, {}, animated = false) }
    @Test fun loginEmail() = login("l_email.png", AuthUi(email = "carlos@exemplo.com"))
    @Test fun loginCode() = login("l_code.png", AuthUi(step = AuthStep.Code, email = "carlos@exemplo.com", code = "4821", resendInSec = 42, error = "Código errado ou vencido. Confira o e-mail ou peça outro."))
    @Test fun loginProfile() = login("l_profile.png", AuthUi(step = AuthStep.Profile, email = "carlos@exemplo.com", name = "Carlos", mascot = "Tamandua"))
    @Test fun shop() = tab("t_shop.png", MainTab.Shop, true)
    @Test fun profile() = tab("t_profile.png", MainTab.Profile, true)
    @Test fun settings() = rule.shot("t_settings.png") { SettingsContent(SettingsUi(Settings(), 50, 50, false, listOf(br.com.trilhati.data.content.ImportedPack("u2.json", "u2-programacao", 1, "Programação e OO", 50)), "0.4.0", fakeSocial()), SettingsCallbacks()) }

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
    @Test fun sMatchCorrected() { val p = playing(QuestionType.Match); session("s_match_corrected.png", p.copy(exercise = ExerciseState.Match(listOf(2, 0, 3, 1), solved = setOf(0, 1, 2, 3), mistakes = 1), phase = Phase.Feedback(false, null, 5, slips = 1))) }
    @Test fun sOrder() { val p = playing(QuestionType.OrderCode); session("s_order.png", p.copy(exercise = ExerciseState.Order(listOf(3, 0, 4), listOf(1, 2)))) }
    @Test fun sTf() = session("s_tf.png", playing(QuestionType.TrueFalse, exercise = ExerciseState.Choice(0)))
    @Test fun sDark() = session("s_mcq_dark.png", playing(QuestionType.Mcq5, exercise = ExerciseState.Choice(1)), dark = true)
    @Test fun sOver() { val p = playing(QuestionType.Mcq5); session("s_over.png", p.copy(startedAtMs = NOW_MS - 200_000, combo = 0)) }
    @Test fun sNoHearts() = session("s_nohearts.png", SessionUi.NoHearts(120, 1_240_000, true))

    // Questões de provas anteriores: etiqueta da prova e selo "explicação não revisada".
    private val exam by lazy {
        kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            .decodeFromString(br.com.trilhati.data.content.ExamPackDto.serializer(), java.io.File("../../../app/src/main/assets/provas/provas-anteriores.json").readText())
            .toDomain()
    }
    private fun examQuestion(id: String) = exam.first { it.id == id }
    private fun examPlaying(id: String, exercise: ExerciseState = ExerciseState.Choice(), phase: Phase = Phase.Answering) =
        SessionUi.Playing("Subconsultas, CTEs e funções analíticas", examQuestion(id), exercise, phase, 0.375f, 0, 4, false, PaceConfig(), 180_000, NOW_MS - 95_000, 2, 42_000, true)
    @Test fun sExam() = session("s_exam.png", examPlaying("fgv-banestes-2021-q48", ExerciseState.Choice(1)))
    @Test fun sExamPreliminary() = session("s_exam_preliminar.png", examPlaying("fcc-teresina-2016-q22", ExerciseState.Choice(2)))
    @Test fun sExamFeedback() {
        val q = examQuestion("fgv-banestes-2018-q64")
        val wrong = (q.answer + 1) % 5
        session("s_exam_feedback.png", examPlaying(q.id, ExerciseState.Choice(wrong), Phase.Feedback(false, q.options[q.answer], 0)))
    }

    @Test fun result() {
        val qs = course[0].lessons[0].questions.take(8)
        var run = LessonRun.start(qs)
        run = LessonRun.answer(run, false, 150_000, 180_000)
        repeat(7) { run = LessonRun.answer(run, true, 130_000, 180_000) }
        run = LessonRun.answer(run, true, 90_000, 180_000)
        val state = fakeState(true)
        val sr = SessionResult(SessionKind.Lesson, "u1l1", "u1", run.records, run.bestCombo, run.perfect)
        val out = ProgressProcessor.apply(
            ProgressState(state.snapshot.stats, state.snapshot.lessonProgress, emptyMap(), DailyLog(TODAY, 40), state.quests, emptySet(), 100),
            sr, TODAY, NOW_MS, { 1 }, 0,
        )
        session("s_result.png", SessionUi.Finished("Modelo ER e normalização", SessionSource.Lesson, out, run, PaceConfig()))
    }
}
