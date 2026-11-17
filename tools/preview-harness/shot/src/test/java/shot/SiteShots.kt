package shot

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import br.com.trilhati.R
import br.com.trilhati.domain.pace.PaceConfig
import br.com.trilhati.feature.app.AppActions
import br.com.trilhati.feature.main.MainContent
import br.com.trilhati.feature.main.MainTab
import br.com.trilhati.feature.session.ExerciseState
import br.com.trilhati.feature.session.Phase
import br.com.trilhati.feature.session.SessionActions
import br.com.trilhati.feature.session.SessionContent
import br.com.trilhati.feature.session.SessionUi
import br.com.trilhati.ui.mascot.MascotCharacter
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Telas do app para o site de divulgação (`site-banquest/dist/assets/`): renders `site_*.png`, que
 * `tools/build_site_assets.py` converte nos arquivos que o site usa. A tela tem a proporção do celular desenhado no
 * site (`.phone`, largura/altura ≈ 0,49), para não sobrar faixa nas laterais.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w412dp-h840dp-xxhdpi")
class SiteShots {
    @get:Rule val rule = createComposeRule()
    private val snack = SnackbarHostState()

    private fun tab(name: String, tab: MainTab) = rule.shot(name) {
        MainContent(fakeState(true), NOW_MS, AppActions(), snack, initialTab = tab, animated = false, social = fakeSocial())
    }

    /** Trilha: Unidade 1 com duas lições feitas e a próxima pronta para começar. */
    @Test fun trilha() = tab("site_trilha.png", MainTab.Path)

    /** Lição: exercício de aprender de um conceito novo, já respondido, com a explicação aberta. */
    @Test fun licao() {
        val lesson = course[0].lessons[0]
        val q = lesson.questions.first { it.conceptId == "u1l1c1" && it.step == 1 }
        val pace = PaceConfig()
        val ui = SessionUi.Playing(
            lesson.title, q, ExerciseState.Choice(q.answer), Phase.Feedback(true, null, 10), 0.2f, 1, 4, false, pace, pace.targetMs(q),
            NOW_MS - 12_000, 1, 25_000, true, remaining = 11, concept = lesson.concept(q.conceptId), newConcept = true, helps = 2,
        )
        rule.shot("site_licao.png") { SessionContent(ui, mutableStateOf(NOW_MS), MascotCharacter.Guaiamum, SessionActions(), {}, {}, {}, {}, animated = false) }
    }

    /** Revisão: aba Praticar, com os erros das lições esperando revisão. */
    @Test fun revisao() = tab("site_revisao.png", MainTab.Practice)
}

/** Ícone do app inteiro em quadrado arredondado (como a `marca.svg` do site), para a imagem de compartilhamento. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w240dp-h240dp-xxhdpi")
class SiteIconShot {
    @get:Rule val rule = createComposeRule()

    @Test fun marca() = rule.shot("site_marca.png") {
        Box(Modifier.size(240.dp).clip(RoundedCornerShape(60.dp)), contentAlignment = Alignment.Center) {
            Image(painterResource(R.drawable.ic_launcher_background), null, Modifier.requiredSize(240.dp))
            Image(painterResource(R.drawable.ic_launcher_foreground), null, Modifier.requiredSize(240.dp))
        }
    }
}
