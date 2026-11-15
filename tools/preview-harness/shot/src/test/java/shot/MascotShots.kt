package shot

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import br.com.trilhati.ui.mascot.Fidget
import br.com.trilhati.ui.mascot.FidgetSchedule
import br.com.trilhati.ui.mascot.Mascot
import br.com.trilhati.ui.mascot.MascotCharacter
import br.com.trilhati.ui.mascot.MascotPose
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Grade dos 4 personagens × 10 poses, e quadros dos gestos de quem está parado, para revisar desenho e animação. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w1200dp-h560dp-xhdpi")
class MascotShots {
    @get:Rule val rule = createComposeRule()

    @Test fun grid() = rule.shot("m_grid.png") {
        Column {
            MascotCharacter.entries.forEach { c ->
                Row { MascotPose.entries.forEach { p -> Mascot(c, p, Modifier.size(120.dp), animated = false, frozenSeconds = 1.3f, decorative = true) } }
            }
            Row { MascotPose.entries.forEach { p -> Text(p.label, Modifier.size(120.dp, 24.dp)) } }
        }
    }

    /** Momento (em segundos) em que o personagem está no meio de um gesto do tipo [fidget], na fração [at]. */
    private fun momentOf(c: MascotCharacter, fidget: Fidget, at: Float): Float {
        var s = 0f
        while (s < 600f) {
            val m = FidgetSchedule.at(s, c.ordinal + 1)
            if (m != null && m.fidget == fidget && m.t >= at) return s
            s += 0.01f
        }
        error("gesto $fidget não encontrado para $c")
    }

    @Test fun fidgets() = rule.shot("m_fidgets.png") {
        Column {
            MascotCharacter.entries.forEach { c ->
                Row {
                    listOf(0.15f, 0.3f, 0.5f, 0.7f, 0.85f).forEach { at ->
                        Mascot(c, MascotPose.Idle, Modifier.size(120.dp), animated = false, frozenSeconds = momentOf(c, Fidget.Signature, at), decorative = true)
                    }
                    listOf(0.3f, 0.7f).forEach { at ->
                        Mascot(c, MascotPose.Idle, Modifier.size(120.dp), animated = false, frozenSeconds = momentOf(c, Fidget.LookAround, at), decorative = true)
                    }
                    Mascot(c, MascotPose.Idle, Modifier.size(120.dp), animated = false, frozenSeconds = momentOf(c, Fidget.Hop, 0.5f), decorative = true)
                }
            }
        }
    }
}
