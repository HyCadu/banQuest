package shot

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import br.com.trilhati.ui.mascot.Mascot
import br.com.trilhati.ui.mascot.MascotCharacter
import br.com.trilhati.ui.mascot.MascotPose
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Grade dos 4 personagens × 5 poses, e quadros em sequência, para revisar o desenho e a animação. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w800dp-h900dp-xhdpi")
class MascotShots {
    @get:Rule val rule = createComposeRule()

    @Test fun grid() = rule.shot("m_grid.png") {
        Column {
            MascotCharacter.entries.forEach { c ->
                Row { MascotPose.entries.forEach { p -> Mascot(c, p, Modifier.size(150.dp), animated = false, decorative = true) } }
            }
            Row { MascotPose.entries.forEach { p -> Text(p.label, Modifier.size(150.dp, 24.dp)) } }
        }
    }

    @Test fun frames() = rule.shot("m_frames.png") {
        Column {
            listOf(MascotPose.Idle, MascotPose.Celebrate).forEach { pose ->
                MascotCharacter.entries.forEach { c ->
                    Row { listOf(0f, 0.4f, 0.8f, 1.2f, 1.6f, 3.45f).forEach { t -> Mascot(c, pose, Modifier.size(110.dp), animated = false, frozenSeconds = t, decorative = true) } }
                }
            }
        }
    }
}
