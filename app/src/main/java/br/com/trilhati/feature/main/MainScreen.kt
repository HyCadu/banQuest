package br.com.trilhati.feature.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.trilhati.domain.quest.QuestEngine
import br.com.trilhati.feature.app.AppActions
import br.com.trilhati.feature.app.AppState
import br.com.trilhati.feature.app.AppViewModel
import br.com.trilhati.feature.leagues.LeaguesContent
import br.com.trilhati.feature.path.PathContent
import br.com.trilhati.feature.practice.PracticeContent
import br.com.trilhati.feature.profile.ProfileContent
import br.com.trilhati.feature.shop.ShopContent
import br.com.trilhati.ui.components.NavTab
import br.com.trilhati.ui.components.TrilhaBottomBar
import br.com.trilhati.ui.icons.GameIcon
import br.com.trilhati.ui.mascot.Mascot
import br.com.trilhati.ui.mascot.MascotCharacter
import br.com.trilhati.ui.mascot.MascotPose
import br.com.trilhati.ui.theme.Trilha

/** Aba selecionada. A Trilha é a tela inicial. */
enum class MainTab(val label: String, val icon: GameIcon) {
    Path("Trilha", GameIcon.House),
    Practice("Praticar", GameIcon.Target),
    Leagues("Ligas", GameIcon.Trophy),
    Shop("Loja", GameIcon.Bag),
    Profile("Perfil", GameIcon.Person),
}

@Composable
fun MainScreen(
    viewModel: AppViewModel,
    onOpenLesson: (String) -> Unit,
    onOpenErrors: () -> Unit,
    onOpenReview: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val nowMs by viewModel.ticker.collectAsState(initial = 0L)
    val notificationsAllowed by viewModel.notificationsAllowed.collectAsState()
    androidx.lifecycle.compose.LifecycleResumeEffect(viewModel) {
        viewModel.refreshNotificationPermission()
        onPauseOrDispose { }
    }
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
    ) { granted ->
        viewModel.refreshNotificationPermission()
        viewModel.setReminderEnabled(true)
        viewModel.dismissReminderPrompt()
        viewModel.message(if (granted) "Combinado! Eu te lembro no horário escolhido." else "Sem permissão, não consigo avisar. Dá para mudar em Configurações.")
    }
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.messages.collect { snackbar.showSnackbar(it) }
    }
    val actions = remember(viewModel) {
        AppActions(
            openLesson = onOpenLesson, openErrors = onOpenErrors, openReview = onOpenReview, openSettings = onOpenSettings,
            buy = viewModel::buy, claimQuest = viewModel::claimQuest, claimDailyChest = viewModel::claimDailyChest,
            claimUnitChest = viewModel::claimUnitChest, setCharacter = viewModel::setCharacter, setName = viewModel::setName,
            setGoal = viewModel::setGoal, message = viewModel::message,
            enableReminders = {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    viewModel.setReminderEnabled(true)
                    viewModel.dismissReminderPrompt()
                }
            },
            dismissReminderPrompt = viewModel::dismissReminderPrompt,
        )
    }
    val showPrompt = state?.let { s ->
        !notificationsAllowed && !s.settings.reminderPromptDismissed && s.hasAnyCompletedLesson
    } == true
    MainContent(state, nowMs, actions, snackbar, showReminderPrompt = showPrompt)
}

/** Casca das abas, sem ViewModel: dá para renderizar com dados de exemplo. */
@Composable
fun MainContent(
    state: AppState?,
    nowMs: Long,
    actions: AppActions,
    snackbar: SnackbarHostState,
    modifier: Modifier = Modifier,
    initialTab: MainTab = MainTab.Path,
    animated: Boolean = true,
    showReminderPrompt: Boolean = false,
) {
    var tabIndex by rememberSaveable { mutableIntStateOf(initialTab.ordinal) }
    val tabActions = remember(actions) { actions.copy(openShop = { tabIndex = MainTab.Shop.ordinal }) }
    val tabs = MainTab.entries.map { tab ->
        val badge = state != null && when (tab) {
            MainTab.Practice -> state.snapshot.dueCount > 0
            MainTab.Leagues -> state.quests.any { q -> QuestEngine.byId(q.questId)?.let { QuestEngine.isComplete(it, q.progress) } == true && !q.claimed }
            MainTab.Shop -> state.snapshot.shop.lastDailyChestDay != state.today
            else -> false
        }
        NavTab(tab.label, tab.icon, badge)
    }
    Scaffold(
        modifier = modifier,
        containerColor = Trilha.colors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(snackbar) { data ->
                Snackbar(data, shape = RoundedCornerShape(16.dp), containerColor = Trilha.colors.ink, contentColor = Trilha.colors.background)
            }
        },
        bottomBar = { TrilhaBottomBar(tabs, tabIndex, onSelect = { tabIndex = it }) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).statusBarsPadding().background(Trilha.colors.background)) {
            if (state == null) {
                Loading()
            } else {
                when (MainTab.entries[tabIndex]) {
                    MainTab.Path -> PathContent(state, nowMs, tabActions, animated = animated, showReminderPrompt = showReminderPrompt)
                    MainTab.Practice -> PracticeContent(state, tabActions, animated = animated)
                    MainTab.Leagues -> LeaguesContent(state, nowMs, tabActions)
                    MainTab.Shop -> ShopContent(state, tabActions, animated = animated)
                    MainTab.Profile -> ProfileContent(state, tabActions, animated = animated)
                }
            }
        }
    }
}

@Composable
private fun Loading() {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center) {
        Mascot(MascotCharacter.Guaiamum, MascotPose.Idle, Modifier.size(140.dp), decorative = true)
        Text("Carregando sua trilha…", style = MaterialTheme.typography.titleMedium, color = Trilha.colors.inkSoft)
    }
}
