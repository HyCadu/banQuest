package br.com.trilhati.feature.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.com.trilhati.feature.main.MainScreen
import br.com.trilhati.feature.session.SessionSource
import br.com.trilhati.feature.session.SessionScreen
import br.com.trilhati.feature.settings.SettingsScreen
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
    val nav = rememberNavController()

    TrilhaTheme(settings.themeMode) {
        NavHost(navController = nav, startDestination = Routes.MAIN) {
            composable(Routes.MAIN) {
                MainScreen(
                    viewModel = appViewModel,
                    onOpenLesson = { nav.navigate(Routes.session(SessionSource.Lesson, it)) },
                    onOpenErrors = { nav.navigate(Routes.session(SessionSource.Errors)) },
                    onOpenReview = { nav.navigate(Routes.session(SessionSource.Review)) },
                    onOpenSettings = { nav.navigate(Routes.SETTINGS) },
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
}
