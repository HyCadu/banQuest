package br.com.trilhati.feature.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.com.trilhati.feature.auth.LoginScreen
import br.com.trilhati.feature.main.MainScreen
import br.com.trilhati.feature.session.SessionSource
import br.com.trilhati.feature.session.SessionScreen
import br.com.trilhati.feature.settings.SettingsScreen
import br.com.trilhati.ui.theme.Trilha
import br.com.trilhati.ui.theme.TrilhaTheme

/** Rotas de navegação do app. As abas ficam dentro de [MAIN]; lição e configurações abrem em tela cheia. */
object Routes {
    const val MAIN = "main"
    const val SESSION = "session/{kind}/{id}"
    const val SETTINGS = "settings"
    fun session(kind: SessionSource, id: String = "-") = "session/${kind.route}/$id"
}

@Composable
fun AppRoot() {
    val appViewModel: AppViewModel = hiltViewModel()
    val settings by appViewModel.settings.collectAsState()
    val settingsReady by appViewModel.settingsReady.collectAsState()
    val gate by appViewModel.gate.collectAsState()
    val update by appViewModel.update.collectAsState()
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    TrilhaTheme(settings.palette) {
        when {
            // Lendo a sessão salva: fundo liso por um instante, sem piscar a tela de entrada.
            gate == Gate.Loading || !settingsReady -> Box(Modifier.fillMaxSize().background(Trilha.colors.background))
            gate == Gate.Login -> LoginScreen()
            // Primeira entrada (ou primeira vez depois da atualização com temas): escolher o tema, uma vez.
            !settings.themeChosen -> {
                val state by appViewModel.state.collectAsState()
                val character = br.com.trilhati.ui.mascot.MascotCharacter.entries
                    .firstOrNull { it.name == state?.snapshot?.profile?.character } ?: br.com.trilhati.ui.mascot.MascotCharacter.Guaiamum
                val selected = br.com.trilhati.ui.theme.resolvePalette(settings.palette)
                br.com.trilhati.feature.theme.ThemeChoiceScreen(
                    selected = selected, character = character,
                    onPick = { appViewModel.setPalette(it) }, onConfirm = { appViewModel.confirmThemeChoice(selected) },
                )
            }
            else -> MainNav(appViewModel)
        }
        (update.status as? br.com.trilhati.domain.update.UpdateStatus.Required)?.let { required ->
            br.com.trilhati.feature.update.UpdateRequiredDialog(required.release, onDownload = {
                runCatching { uriHandler.openUri(required.release.apkUrl) }
            })
        }
    }
}

@Composable
private fun MainNav(appViewModel: AppViewModel) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.MAIN) {
        composable(Routes.MAIN) {
            MainScreen(
                viewModel = appViewModel,
                onOpenLesson = { nav.navigate(Routes.session(SessionSource.Lesson, it)) },
                onOpenErrors = { nav.navigate(Routes.session(SessionSource.Errors)) },
                onOpenReview = { nav.navigate(Routes.session(SessionSource.Review)) },
                onOpenSettings = { nav.navigate(Routes.SETTINGS) },
                onOpenExam = { nav.navigate(Routes.session(SessionSource.Exam, it)) },
            )
        }
        composable(
            Routes.SESSION,
            arguments = listOf(navArgument("kind") { type = NavType.StringType }, navArgument("id") { type = NavType.StringType }),
        ) {
            SessionScreen(
                onExit = { nav.popBackStack() },
                onOpenErrors = {
                    nav.popBackStack()
                    nav.navigate(Routes.session(SessionSource.Errors))
                },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(viewModel = appViewModel, onBack = { nav.popBackStack() })
        }
    }
}
