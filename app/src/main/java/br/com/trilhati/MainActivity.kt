package br.com.trilhati

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import br.com.trilhati.data.db.UserProfileDao
import br.com.trilhati.domain.time.daysToExam
import br.com.trilhati.domain.time.today
import br.com.trilhati.feature.gallery.GalleryScreen
import br.com.trilhati.ui.theme.ThemeMode
import br.com.trilhati.ui.theme.TrilhaTheme
import dagger.hilt.android.AndroidEntryPoint
import java.time.Clock
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var clock: Clock
    @Inject lateinit var profileDao: UserProfileDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val today = clock.today().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        val daysToExam = clock.daysToExam()
        setContent {
            var themeMode by rememberSaveable { mutableStateOf(ThemeMode.System) }
            val profile by profileDao.observe().collectAsState(initial = null)
            val dbState = if (profile == null) "banco de dados aberto, sem perfil" else "perfil de ${profile?.name}"
            TrilhaTheme(themeMode) {
                GalleryScreen(
                    themeMode = themeMode,
                    onThemeMode = { themeMode = it },
                    daysToExam = daysToExam,
                    footer = "Fase 1 · hoje (Brasília): $today · $dbState",
                )
            }
        }
    }
}
