package br.com.trilhati

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import br.com.trilhati.data.db.UserProfileDao
import br.com.trilhati.domain.time.today
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
        setContent {
            TrilhaTheme {
                val profile by profileDao.observe().collectAsState(initial = null)
                DiagnosticScreen(todayLabel = today, profileName = profile?.name)
            }
        }
    }
}
