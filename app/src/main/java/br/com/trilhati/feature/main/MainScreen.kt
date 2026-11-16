package br.com.trilhati.feature.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import br.com.trilhati.feature.app.SocialUi
import br.com.trilhati.feature.ranking.RankingContent
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
    Ranking("Ranking", GameIcon.Trophy),
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
    val social by viewModel.social.collectAsState()
    val update by viewModel.update.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    androidx.lifecycle.compose.LifecycleResumeEffect(viewModel) {
        viewModel.refreshNotificationPermission()
        viewModel.onAppResumed()
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
            refreshSocial = viewModel::refreshSocial,
            requestFriend = { viewModel.requestFriend(it) },
            respondRequest = { id, accept -> viewModel.respondRequest(id, accept) },
            addFriendByCode = viewModel::addFriendByCode,
            removeFriend = { viewModel.removeFriend(it) },
            openUpdate = { url ->
                if (runCatching { uriHandler.openUri(url) }.isFailure) viewModel.message("Não achei um navegador para baixar. O link: $url")
            },
            dismissUpdate = { viewModel.dismissUpdate(it) },
            shareFriendCode = { code ->
                val text = "Bora estudar para o concurso do Banestes no BanQuest? Me adiciona como amigo com o código $code."
                val send = android.content.Intent(android.content.Intent.ACTION_SEND).setType("text/plain")
                    .putExtra(android.content.Intent.EXTRA_TEXT, text)
                context.startActivity(android.content.Intent.createChooser(send, "Compartilhar código de amigo"))
            },
        )
    }
    val showPrompt = state?.let { s ->
        !notificationsAllowed && !s.settings.reminderPromptDismissed && s.hasAnyCompletedLesson
    } == true
    MainContent(
        state, nowMs, actions, snackbar, showReminderPrompt = showPrompt, social = social,
        updateBanner = update.status.releaseOrNull.takeIf { update.showBanner },
    )
    social.conflict?.let { conflict ->
        SyncConflictDialog(conflict, onKeepCloud = { viewModel.resolveConflict(keepLocal = false) }, onKeepLocal = { viewModel.resolveConflict(keepLocal = true) })
    }
}

/** Dois aparelhos mexeram no progresso: o aluno escolhe qual fica. Sem "fechar" — a escolha é obrigatória. */
@Composable
fun SyncConflictDialog(conflict: br.com.trilhati.data.social.SyncConflict, onKeepCloud: () -> Unit, onKeepLocal: () -> Unit) {
    br.com.trilhati.ui.components.TrilhaDialog(
        onDismiss = { },
        title = "Qual progresso fica?",
        content = {
            Text(
                "Sua conta tem progresso salvo por outro aparelho.\n\n" +
                    "Na nuvem: ${conflict.serverXp} XP, ${conflict.serverLessons} lições.\n" +
                    "Neste aparelho: ${conflict.localXp} XP, ${conflict.localLessons} lições.\n\n" +
                    "O que você não escolher será substituído. O ranking da semana não muda.",
                style = MaterialTheme.typography.bodyMedium, color = Trilha.colors.ink,
            )
        },
        actions = {
            br.com.trilhati.ui.components.ChunkyButton("Usar o da nuvem", onKeepCloud, Modifier.fillMaxWidth())
            br.com.trilhati.ui.components.ChunkyButton(
                "Manter o deste aparelho", onKeepLocal, Modifier.fillMaxWidth(), br.com.trilhati.ui.components.ChunkyStyle.Neutral,
            )
        },
    )
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
    social: SocialUi = SocialUi(),
    updateBanner: br.com.trilhati.domain.update.ReleaseInfo? = null,
) {
    var tabIndex by rememberSaveable { mutableIntStateOf(initialTab.ordinal) }
    val tabActions = remember(actions) {
        actions.copy(openShop = { tabIndex = MainTab.Shop.ordinal }, openRanking = { tabIndex = MainTab.Ranking.ordinal })
    }
    val pairsAtRisk = remember(social.overview, state?.today) {
        val overview = social.overview
        // Cache de outro dia não diz se o amigo estudou hoje, mas a dupla continua valendo até hoje à noite.
        overview?.friends.orEmpty()
            .filter { it.pairStreak > 0 && !(overview?.today == state?.today?.toString() && it.meToday) }
            .map { it.displayName to it.pairStreak }
    }
    val tabs = MainTab.entries.map { tab ->
        val badge = state != null && when (tab) {
            MainTab.Practice -> state.snapshot.dueCount > 0
            MainTab.Ranking -> social.overview?.requests?.isNotEmpty() == true ||
                state.quests.any { q -> QuestEngine.byId(q.questId)?.let { QuestEngine.isComplete(it, q.progress) } == true && !q.claimed }
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
                    MainTab.Path -> PathContent(
                        state, nowMs, tabActions, animated = animated, showReminderPrompt = showReminderPrompt, pairsAtRisk = pairsAtRisk,
                        updateBanner = updateBanner,
                    )
                    MainTab.Practice -> PracticeContent(state, tabActions, animated = animated)
                    MainTab.Ranking -> RankingContent(state, social, nowMs, tabActions, animated = animated)
                    MainTab.Shop -> ShopContent(state, tabActions)
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
