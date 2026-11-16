package shot

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import br.com.trilhati.domain.model.QuestionType
import br.com.trilhati.domain.pace.PaceConfig
import br.com.trilhati.feature.app.AppActions
import br.com.trilhati.feature.main.MainContent
import br.com.trilhati.feature.main.MainTab
import br.com.trilhati.feature.session.*
import br.com.trilhati.ui.mascot.MascotCharacter
import br.com.trilhati.ui.theme.TrilhaTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Acessibilidade automática: todo elemento tocável precisa de rótulo (texto ou descrição) para o TalkBack. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w412dp-h892dp-xxhdpi")
class A11yTest {
    @get:Rule val rule = createComposeRule()

    private fun assertClickablesLabeled(where: String) {
        val nodes = rule.onAllNodes(hasClickAction()).fetchSemanticsNodes()
        assertTrue("$where: nenhum elemento tocável encontrado", nodes.isNotEmpty())
        val unlabeled = nodes.filter { n ->
            val text = n.config.getOrNull(SemanticsProperties.Text)?.joinToString { it.text }.orEmpty()
            val desc = n.config.getOrNull(SemanticsProperties.ContentDescription)?.joinToString().orEmpty()
            text.isBlank() && desc.isBlank()
        }
        assertTrue("$where: ${unlabeled.size} elemento(s) tocável(is) sem rótulo: ${unlabeled.map { it.boundsInRoot }}", unlabeled.isEmpty())
    }

    private fun tab(t: MainTab) {
        rule.mainClock.autoAdvance = false
        rule.setContent { TrilhaTheme { MainContent(fakeState(true), NOW_MS, AppActions(), SnackbarHostState(), initialTab = t, animated = false, showReminderPrompt = true, social = fakeSocial()) } }
        rule.mainClock.advanceTimeBy(300)
        assertClickablesLabeled(t.name)
    }

    @Test fun trilha() = tab(MainTab.Path)
    @Test fun praticar() = tab(MainTab.Practice)
    @Test fun ranking() = tab(MainTab.Ranking)

    @Test fun rankingAmigos() {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            TrilhaTheme {
                br.com.trilhati.feature.ranking.RankingContent(
                    fakeState(true), fakeSocial(), NOW_MS, AppActions(), initialScope = br.com.trilhati.feature.ranking.RankingScope.Friends, animated = false,
                )
            }
        }
        rule.mainClock.advanceTimeBy(300)
        assertClickablesLabeled("ranking amigos")
    }

    @Test fun entrada() {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            TrilhaTheme {
                br.com.trilhati.feature.auth.LoginContent(
                    br.com.trilhati.feature.auth.AuthUi(step = br.com.trilhati.feature.auth.AuthStep.Profile, name = "Carlos"),
                    {}, {}, {}, {}, {}, {}, {}, {}, {}, animated = false,
                )
            }
        }
        rule.mainClock.advanceTimeBy(300)
        assertClickablesLabeled("entrada")
    }
    @Test fun loja() = tab(MainTab.Shop)
    @Test fun perfil() = tab(MainTab.Profile)

    private fun session(type: QuestionType, exercise: ExerciseState) {
        // A ordenação usa a de 5 linhas (u1l4); a da u1l1 tem 3.
        val q = course[0].lessons.flatMap { it.questions }.first { it.type == type && (exercise !is ExerciseState.Order || it.lines.size == 5) }
        val concept = course[0].lessons.first { it.id == q.lessonId }.concept(q.conceptId)
        val ui = SessionUi.Playing("Lição", q, exercise, Phase.Answering, 0.3f, 3, 4, false, PaceConfig(), 180_000, NOW_MS, 2, 0, false, 6, concept = concept)
        rule.mainClock.autoAdvance = false
        rule.setContent { TrilhaTheme { SessionContent(ui, mutableStateOf(NOW_MS), MascotCharacter.Guaiamum, SessionActions(), {}, {}, {}, {}, animated = false) } }
        rule.mainClock.advanceTimeBy(300)
        assertClickablesLabeled("lição $type")
    }

    @Test fun licaoMultiplaEscolha() = session(QuestionType.Mcq5, ExerciseState.Choice(1))
    @Test fun licaoPares() = session(QuestionType.Match, ExerciseState.Match(listOf(1, 0, 3, 2)))
    @Test fun licaoOrdenar() = session(QuestionType.OrderCode, ExerciseState.Order(listOf(4, 3, 2, 1, 0)))
    // Escolha rápida de aprender, com o botão "Dica" do conceito (u1l1).
    @Test fun licaoEscolhaRapidaComDica() = session(QuestionType.Quick, ExerciseState.Choice(1))

    @Test fun fonteGrandeTrilhaELicao() {
        RuntimeEnvironment.setFontScale(1.5f)
        rule.shot("a11y_font_path.png") { MainContent(fakeState(true), NOW_MS, AppActions(), SnackbarHostState(), animated = false) }
    }

    @Test fun fonteGrandeLicao() {
        RuntimeEnvironment.setFontScale(1.5f)
        val q = course[0].lessons.flatMap { it.questions }.first { it.type == QuestionType.Case }
        val ui = SessionUi.Playing("Lição", q, ExerciseState.Choice(2), Phase.Answering, 0.3f, 4, 4, false, PaceConfig(), 180_000, NOW_MS - 30_000, 2, 42_000, false, 6)
        rule.shot("a11y_font_lesson.png") { SessionContent(ui, mutableStateOf(NOW_MS), MascotCharacter.Guaiamum, SessionActions(), {}, {}, {}, {}, animated = false) }
    }
}
