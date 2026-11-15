package shot

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import br.com.trilhati.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w720dp-h420dp-xxhdpi")
class IconShots {
    @get:Rule val rule = createComposeRule()

    @androidx.compose.runtime.Composable
    private fun Adaptive(shape: Shape, mono: Boolean = false, size: androidx.compose.ui.unit.Dp = 120.dp) {
        // Ícone adaptativo: só os 72 dp centrais dos 108 dp aparecem através da máscara.
        Box(Modifier.size(size).clip(shape).background(if (mono) Color(0xFFDDE3EA) else Color.Transparent), contentAlignment = Alignment.Center) {
            Box(Modifier.requiredSize(size * 1.5f)) {
                if (!mono) Image(painterResource(R.drawable.ic_launcher_background), null, Modifier.fillMaxSize())
                Image(
                    painterResource(if (mono) R.drawable.ic_launcher_monochrome else R.drawable.ic_launcher_foreground), null, Modifier.fillMaxSize(),
                    colorFilter = if (mono) ColorFilter.tint(Color(0xFF2D4A6B)) else null,
                )
            }
        }
    }

    @Test fun launcher() = rule.shot("icon.png") {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Adaptive(CircleShape)
                Adaptive(RoundedCornerShape(30))
                Adaptive(RoundedCornerShape(12))
                Adaptive(CircleShape, mono = true)
                Adaptive(CircleShape, size = 48.dp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.ic_stat_trilha), null, Modifier.size(24.dp).background(Color(0xFF6B3FD6)))
                Text("ícone de notificação (24 dp), o sistema pinta de branco")
            }
        }
    }
}
