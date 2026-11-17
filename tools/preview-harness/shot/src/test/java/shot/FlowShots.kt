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
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot
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

    // ---- Temas (0.8.0): Elétrico e Chiclete, trilha dos Gerais e escolha de tema
    private val eletrico = br.com.trilhati.ui.theme.ThemePalette.Eletrico
    private val chiclete = br.com.trilhati.ui.theme.ThemePalette.Chiclete
    private fun stateOn(track: br.com.trilhati.domain.model.Track) = fakeState(true).let { it.copy(settings = it.settings.copy(trailTrack = track)) }
    private fun pathShot(name: String, palette: br.com.trilhati.ui.theme.ThemePalette, track: br.com.trilhati.domain.model.Track) =
        rule.shot(name, palette = palette) { MainContent(stateOn(track), NOW_MS, AppActions(), snack, animated = false, social = fakeSocial()) }
    @Test fun trilhaEletrico() = pathShot("th_path_eletrico.png", eletrico, br.com.trilhati.domain.model.Track.Specific)
    @Test fun trilhaChiclete() = pathShot("th_path_chiclete.png", chiclete, br.com.trilhati.domain.model.Track.Specific)
    @Test fun trilhaGeraisClaro() = pathShot("th_path_gerais.png", br.com.trilhati.ui.theme.ThemePalette.Claro, br.com.trilhati.domain.model.Track.General)
    @Test fun trilhaGeraisEscuro() = pathShot("th_path_gerais_escuro.png", br.com.trilhati.ui.theme.ThemePalette.Escuro, br.com.trilhati.domain.model.Track.General)
    @Test fun trilhaGeraisEletrico() = pathShot("th_path_gerais_eletrico.png", eletrico, br.com.trilhati.domain.model.Track.General)
    @Test fun trilhaGeraisChiclete() = pathShot("th_path_gerais_chiclete.png", chiclete, br.com.trilhati.domain.model.Track.General)
    @Test fun rankingEletrico() = rule.shot("th_ranking_eletrico.png", palette = eletrico) {
        MainContent(fakeState(true), NOW_MS, AppActions(), snack, initialTab = MainTab.Ranking, animated = false, social = fakeSocial())
    }
    @Test fun lojaChiclete() = rule.shot("th_shop_chiclete.png", palette = chiclete) {
        MainContent(fakeState(true), NOW_MS, AppActions(), snack, initialTab = MainTab.Shop, animated = false, social = fakeSocial())
    }
    @Test fun licaoEletrico() = rule.shot("th_lesson_eletrico.png", palette = eletrico) {
        SessionContent(playing(QuestionType.Mcq5, exercise = ExerciseState.Choice(2)), mutableStateOf(NOW_MS), MascotCharacter.Guaiamum, SessionActions(), {}, {}, {}, {}, animated = false)
    }
    @Test fun licaoChiclete() = rule.shot("th_lesson_chiclete.png", palette = chiclete) {
        val p = playing(QuestionType.Mcq5)
        SessionContent(p.copy(exercise = ExerciseState.Choice(p.question.answer), phase = Phase.Feedback(true, null, 15)), mutableStateOf(NOW_MS), MascotCharacter.Preguica, SessionActions(), {}, {}, {}, {}, animated = false)
    }
    @Test fun escolhaDeTema() = rule.shot("th_choice_claro.png") {
        br.com.trilhati.feature.theme.ThemeChoiceScreen(br.com.trilhati.ui.theme.ThemePalette.Claro, MascotCharacter.Tartaruga, {}, {}, animated = false)
    }
    @Test fun escolhaDeTemaEletrico() = rule.shot("th_choice_eletrico.png", palette = eletrico) {
        br.com.trilhati.feature.theme.ThemeChoiceScreen(eletrico, MascotCharacter.Tartaruga, {}, {}, animated = false)
    }
    /** Um quadro no meio da troca Específicos → Gerais (a animação roda; o resto da tela continua parado). */
    @Test fun trocaDeTrilhaNoMeio() {
        val track = mutableStateOf(br.com.trilhati.domain.model.Track.Specific)
        rule.mainClock.autoAdvance = false
        rule.setContent {
            br.com.trilhati.ui.theme.TrilhaTheme(br.com.trilhati.ui.theme.ThemePalette.Claro) {
                androidx.compose.foundation.layout.Box(androidx.compose.ui.Modifier.background(br.com.trilhati.ui.theme.Trilha.colors.background)) {
                    br.com.trilhati.feature.path.PathContent(stateOn(track.value), NOW_MS, AppActions(), animated = true)
                }
            }
        }
        rule.mainClock.advanceTimeBy(300)
        track.value = br.com.trilhati.domain.model.Track.General
        rule.mainClock.advanceTimeBy(200)
        rule.onRoot().captureToImage().asAndroidBitmap().save("th_switch_mid.png")
        rule.mainClock.advanceTimeBy(800)
        rule.onRoot().captureToImage().asAndroidBitmap().save("th_switch_end.png")
    }
    // Meio coração perdido: o contador mostra "3,5" com o coração rachado (no lugar de "3½").
    @Test fun trilhaHalfHeart() = rule.shot("t_path_half_heart.png") {
        val st = fakeState(true)
        val stats = st.snapshot.stats.copy(hearts = br.com.trilhati.domain.hearts.HeartsState(3, NOW_MS - 5 * 60_000, half = true))
        MainContent(st.copy(snapshot = st.snapshot.copy(stats = stats)), NOW_MS, AppActions(), snack, animated = false, social = fakeSocial())
    }
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
    @Test fun sOrder() { val p = playing(QuestionType.OrderCode, idx = 1); session("s_order.png", p.copy(exercise = ExerciseState.Order(listOf(3, 0, 4), listOf(1, 2)))) }
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
    // Formato por conceitos (lição u1l1): conceito novo com a dica aberta, adaptada da prova, praticar e "Não sei".
    private fun conceptPlaying(pick: (br.com.trilhati.domain.model.Question) -> Boolean, exercise: ExerciseState = ExerciseState.Choice(),
                               phase: Phase = Phase.Answering, newConcept: Boolean = false, halfHeart: Boolean = false): SessionUi.Playing {
        val lesson = course[0].lessons[0]
        val q = lesson.questions.first(pick)
        val pace = PaceConfig()
        return SessionUi.Playing(lesson.title, q, exercise, phase, 0.2f, 1, 4, false, pace, pace.targetMs(q), NOW_MS - 12_000, 1, 25_000, true,
            remaining = 11, concept = lesson.concept(q.conceptId), newConcept = newConcept, halfHeart = halfHeart, helps = 2)
    }
    @Test fun sLearnNewConcept() = session("s_learn_new.png", conceptPlaying({ it.conceptId == "u1l1c1" && it.step == 1 }, ExerciseState.Choice(1), newConcept = true))
    @Test fun sLearnAdapted() = session("s_learn_quick.png", conceptPlaying({ it.conceptId == "u1l1c3" && it.step == 1 }, ExerciseState.Choice(1), halfHeart = true))
    @Test fun sLearnSkipped() {
        val p = conceptPlaying({ it.conceptId == "u1l1c5" && it.step == 2 && it.type == br.com.trilhati.domain.model.QuestionType.Quick })
        session("s_learn_skipped.png", p.copy(phase = Phase.Feedback(false, p.question.options[p.question.answer], 0, skipped = true)))
    }
    @Test fun sPracticeFillOk() {
        val p = conceptPlaying({ it.conceptId == "u1l1c6" && it.type == br.com.trilhati.domain.model.QuestionType.FillBlank }, ExerciseState.Choice(2))
        session("s_practice_fill.png", p.copy(phase = Phase.Feedback(true, null, 12)))
    }
    @Test fun sPracticeMatch() = session("s_practice_match.png", conceptPlaying({ it.conceptId == "u1l1c2" && it.type == br.com.trilhati.domain.model.QuestionType.Match },
        ExerciseState.Match(listOf(2, 0, 1), solved = setOf(0), selectedLeft = 1)))

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
