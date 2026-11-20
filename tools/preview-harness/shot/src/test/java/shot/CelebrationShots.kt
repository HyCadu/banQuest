package shot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import br.com.trilhati.domain.achievement.AchievementEngine
import br.com.trilhati.domain.celebration.CelebrationInput
import br.com.trilhati.domain.celebration.CelebrationPlan
import br.com.trilhati.domain.celebration.CelebrationState
import br.com.trilhati.domain.celebration.CelebrationStep
import br.com.trilhati.domain.celebration.StreakWeek
import br.com.trilhati.domain.lesson.LessonRun
import br.com.trilhati.domain.pace.PaceConfig
import br.com.trilhati.domain.progress.DailyLog
import br.com.trilhati.domain.progress.ProgressProcessor
import br.com.trilhati.domain.progress.ProgressState
import br.com.trilhati.domain.progress.SessionKind
import br.com.trilhati.domain.progress.SessionResult
import br.com.trilhati.feature.session.CelebrationActions
import br.com.trilhati.feature.session.SessionActions
import br.com.trilhati.feature.session.SessionContent
import br.com.trilhati.feature.session.SessionSource
import br.com.trilhati.feature.session.SessionUi
import br.com.trilhati.ui.animation.celebration.StreakIgnition
import br.com.trilhati.ui.mascot.MascotCharacter
import br.com.trilhati.ui.theme.Trilha
import br.com.trilhati.ui.theme.TrilhaTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Celebração do fim da atividade (0.15.0): quadros parados de cada etapa, quadros no meio da animação (a coreografia
 * roda de verdade no relógio do teste) e acessibilidade da chama.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w412dp-h892dp-xxhdpi")
class CelebrationShots {
    @get:Rule val rule = createComposeRule()

    /** Lição com ofensiva nova (2 → 3) e uma conquista, como no primeiro estudo do dia. */
    private fun finished(): SessionUi.Finished {
        val qs = course[0].lessons[0].questions.take(8)
        var run = LessonRun.start(qs)
        run = LessonRun.answer(run, false, 150_000, 180_000)
        repeat(8) { run = LessonRun.answer(run, true, 120_000, 180_000) }
        val state = fakeState(true)
        val sr = SessionResult(SessionKind.Lesson, "u1l1", "u1", run.records, run.bestCombo, run.perfect)
        val out = ProgressProcessor.apply(
            ProgressState(state.snapshot.stats, state.snapshot.lessonProgress, emptyMap(), DailyLog(TODAY, 40), state.quests, emptySet(), 1),
            sr, TODAY, NOW_MS, { 1 }, 0,
        )
        val summary = out.summary.copy(
            streakBefore = 2, streakAfter = 3, streakIncreased = true,
            newAchievements = listOf(AchievementEngine.all.first()),
        )
        val logs = mapOf(TODAY.minusDays(2) to DailyLog(TODAY.minusDays(2), lessons = 1), TODAY.minusDays(1) to DailyLog(TODAY.minusDays(1), lessons = 1), TODAY to DailyLog(TODAY, lessons = 1))
        return SessionUi.Finished("Modelo ER e normalização", SessionSource.Lesson, out.copy(summary = summary), run, PaceConfig(), week = StreakWeek.of(TODAY, logs))
    }

    private fun start(ui: SessionUi.Finished): CelebrationState = CelebrationPlan.start(
        CelebrationInput(true, ui.processed.summary.totalXp, ui.processed.summary.newAchievements.size, ui.processed.summary.streakIncreased),
    )

    private fun save(name: String) = rule.onRoot().captureToImage().asAndroidBitmap().save(name)

    @Test fun etapasPrevistas() {
        assertEquals(
            listOf(CelebrationStep.Completion, CelebrationStep.Results, CelebrationStep.XpReward, CelebrationStep.Achievements, CelebrationStep.Streak, CelebrationStep.Finished),
            start(finished()).steps,
        )
    }

    /** Resultado completo e parado: XP, indicadores, fichas, conquista, tempo e revisão. */
    @Test fun resultadoCompleto() = rule.shot("c_result_full.png") {
        val ui = finished()
        SessionContent(ui, mutableStateOf(NOW_MS), MascotCharacter.Guaiamum, SessionActions(), {}, {}, {}, {}, animated = false)
    }

    @Test fun chamaApagada() = rule.shot("c_streak_unlit.png") {
        val ui = finished()
        StreakIgnition(2, 3, ui.week, MascotCharacter.Tartaruga, lit = false, onLight = {}, onContinue = {}, animated = false)
    }

    @Test fun chamaAcesa() = rule.shot("c_streak_lit.png") {
        val ui = finished()
        StreakIgnition(2, 3, ui.week, MascotCharacter.Tartaruga, lit = true, onLight = {}, onContinue = {}, animated = false)
    }

    /** A coreografia de verdade: conclusão em andamento, XP saltando, "Continuar" segurando a chama. */
    @Test fun conclusaoAnimada() {
        val ui = finished()
        var state by mutableStateOf(start(ui))
        rule.mainClock.autoAdvance = false
        rule.setContent {
            TrilhaTheme {
                Box(Modifier.background(Trilha.colors.background)) {
                    SessionContent(
                        ui, mutableStateOf(NOW_MS), MascotCharacter.Guaiamum, SessionActions(), {}, {}, {}, {},
                        celebration = state,
                        celebrationActions = CelebrationActions(onStepDone = { state = state.complete(it) }, onSkip = { state = state.skip() }),
                    )
                }
            }
        }
        rule.mainClock.advanceTimeBy(260)
        save("c_anim_1_title.png")
        rule.mainClock.advanceTimeBy(900)
        save("c_anim_2_results.png")
        rule.mainClock.advanceTimeBy(1_300)
        save("c_anim_3_xp.png")
        rule.mainClock.advanceTimeBy(2_500)
        save("c_anim_4_achievement.png")
        rule.mainClock.advanceTimeBy(1_500)
        // Antes da chama, a cena espera o "Continuar".
        assertEquals(CelebrationStep.Achievements, state.step)
        rule.onNodeWithText("Continuar", ignoreCase = true).performClick()
        rule.mainClock.advanceTimeBy(100)
        assertEquals(CelebrationStep.Streak, state.step)
    }

    /** Toque na chama: antecipação, ignição com anel e faíscas, contador saltando, semana e mascote. */
    @Test fun ignicaoAnimada() {
        val ui = finished()
        var lit by mutableStateOf(false)
        var lights = 0
        rule.mainClock.autoAdvance = false
        rule.setContent {
            TrilhaTheme {
                StreakIgnition(2, 3, ui.week, MascotCharacter.Guaiamum, lit = lit, onLight = { lights++; lit = true }, onContinue = {})
            }
        }
        rule.mainClock.advanceTimeBy(700)
        save("c_ign_0_idle.png")
        rule.onNodeWithText("Acender a chama", ignoreCase = true).performClick()
        rule.mainClock.advanceTimeBy(260)
        save("c_ign_1_burst.png")
        rule.mainClock.advanceTimeBy(500)
        save("c_ign_2_counter.png")
        rule.mainClock.advanceTimeBy(2_600)
        save("c_ign_3_done.png")
        assertEquals("acende uma vez só", 1, lights)
    }

    @Test fun chamaTemAlternativaAcessivel() {
        val ui = finished()
        rule.mainClock.autoAdvance = false
        rule.setContent {
            TrilhaTheme { StreakIgnition(2, 3, ui.week, MascotCharacter.Guaiamum, lit = false, onLight = {}, onContinue = {}, animated = false) }
        }
        rule.mainClock.advanceTimeBy(300)
        val clickables = rule.onAllNodes(hasClickAction()).fetchSemanticsNodes()
        assertTrue("botão e chama tocáveis", clickables.size >= 2)
        val unlabeled = clickables.filter { n ->
            n.config.getOrNull(SemanticsProperties.Text).isNullOrEmpty() && n.config.getOrNull(SemanticsProperties.ContentDescription).isNullOrEmpty()
        }
        assertTrue("sem rótulo: ${unlabeled.map { it.boundsInRoot }}", unlabeled.isEmpty())
    }
}
