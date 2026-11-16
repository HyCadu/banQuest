package br.com.trilhati.feature.settings

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings as AndroidSettings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import br.com.trilhati.data.content.ImportedPack
import br.com.trilhati.data.prefs.Settings
import br.com.trilhati.domain.pace.PaceConfig
import br.com.trilhati.domain.pace.TimerMode
import br.com.trilhati.feature.app.AppViewModel
import br.com.trilhati.ui.components.ChunkyButton
import br.com.trilhati.ui.components.ChunkyStyle
import br.com.trilhati.ui.components.PillChoice
import br.com.trilhati.ui.components.TrilhaDialog
import br.com.trilhati.ui.theme.Spacing
import br.com.trilhati.ui.theme.ThemeMode
import br.com.trilhati.ui.theme.Trilha
import java.time.LocalTime
import kotlinx.coroutines.launch

/** O que a tela de configurações mostra. */
data class SettingsUi(
    val settings: Settings,
    val questionCount: Int,
    val unreviewedCount: Int,
    val notificationsAllowed: Boolean,
    val importedPacks: List<ImportedPack>,
    val versionName: String,
    val social: br.com.trilhati.feature.app.SocialUi = br.com.trilhati.feature.app.SocialUi(),
    val update: br.com.trilhati.data.update.UpdateUi = br.com.trilhati.data.update.UpdateUi(),
    /** Quantas das [questionCount] são de provas anteriores. */
    val examCount: Int = 0,
)

/** O que a tela de configurações dispara. */
data class SettingsCallbacks(
    val onBack: () -> Unit = {},
    val onTimerMode: (TimerMode) -> Unit = {},
    val onPaceTarget: (Long) -> Unit = {},
    val onCaseFour: (Boolean) -> Unit = {},
    val onNoLives: (Boolean) -> Unit = {},
    val onHaptics: (Boolean) -> Unit = {},
    val onSounds: (Boolean) -> Unit = {},
    val onTheme: (ThemeMode) -> Unit = {},
    val onReminderEnabled: (Boolean) -> Unit = {},
    val onReminderTime: (LocalTime) -> Unit = {},
    val onAllowNotifications: () -> Unit = {},
    val onExport: () -> Unit = {},
    val onImport: () -> Unit = {},
    val onImportPack: () -> Unit = {},
    val onRemovePack: (String) -> Unit = {},
    val onErase: () -> Unit = {},
    val onShowInGlobal: (Boolean) -> Unit = {},
    val onSyncNow: () -> Unit = {},
    /** Há progresso ainda não enviado? (pergunta antes de sair da conta) */
    val checkUnsynced: suspend () -> Boolean = { false },
    val onSignOut: () -> Unit = {},
    val onDeleteAccount: () -> Unit = {},
    val onCheckUpdate: () -> Unit = {},
    val onDownloadUpdate: (url: String) -> Unit = {},
)

@Composable
fun SettingsScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val settings by viewModel.settings.collectAsState()
    val state by viewModel.state.collectAsState()
    val allowed by viewModel.notificationsAllowed.collectAsState()
    val packs by viewModel.importedPacks.collectAsState()
    val social by viewModel.social.collectAsState()
    val update by viewModel.update.collectAsState()
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    val context = LocalContext.current
    val questionCount = state?.course?.sumOf { u -> u.lessons.sumOf { it.questions.size } } ?: 0
    val unreviewed = state?.course?.sumOf { u -> u.lessons.sumOf { l -> l.questions.count { it.showUnreviewedBadge } } } ?: 0
    val fromExams = state?.course?.sumOf { u -> u.lessons.sumOf { l -> l.questions.count { it.isFromExam } } } ?: 0
    val version = remember(context) {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: "?"
    }
    androidx.lifecycle.compose.LifecycleResumeEffect(viewModel) {
        viewModel.refreshNotificationPermission()
        onPauseOrDispose { }
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let(viewModel::exportProgress)
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(viewModel::importProgress)
    }
    val packLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(viewModel::importPack)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        viewModel.refreshNotificationPermission()
        if (!granted) {
            // Recusado (ou recusado de vez): abre a tela do sistema, onde o aluno pode liberar manualmente.
            context.startActivity(
                Intent(AndroidSettings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(AndroidSettings.EXTRA_APP_PACKAGE, context.packageName)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }

    SettingsContent(
        ui = SettingsUi(settings, questionCount, unreviewed, allowed, packs, version, social, update, examCount = fromExams),
        callbacks = SettingsCallbacks(
            onBack = onBack, onTimerMode = viewModel::setTimerMode, onPaceTarget = viewModel::setPaceTarget,
            onCaseFour = viewModel::setCaseFourMinutes, onNoLives = viewModel::setNoLives, onHaptics = viewModel::setHaptics,
            onSounds = viewModel::setSounds, onTheme = viewModel::setTheme, onReminderEnabled = viewModel::setReminderEnabled,
            onReminderTime = viewModel::setReminderTime,
            onAllowNotifications = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    context.startActivity(
                        Intent(AndroidSettings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(AndroidSettings.EXTRA_APP_PACKAGE, context.packageName)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                }
            },
            onExport = { exportLauncher.launch("banquest-progresso-${state?.today ?: "backup"}.json") },
            onImport = { importLauncher.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) },
            onImportPack = { packLauncher.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) },
            onRemovePack = viewModel::removePack,
            onErase = viewModel::eraseAll,
            onShowInGlobal = { viewModel.setShowInGlobal(it) },
            onSyncNow = viewModel::refreshSocial,
            checkUnsynced = viewModel::hasUnsyncedChanges,
            onSignOut = { viewModel.signOut() },
            onDeleteAccount = { viewModel.deleteAccount() },
            onCheckUpdate = viewModel::checkUpdateNow,
            onDownloadUpdate = { url ->
                if (runCatching { uriHandler.openUri(url) }.isFailure) viewModel.message("Não achei um navegador para baixar o APK.")
            },
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(ui: SettingsUi, callbacks: SettingsCallbacks, modifier: Modifier = Modifier) {
    val c = Trilha.colors
    val settings = ui.settings
    var confirmErase by remember { mutableStateOf(false) }
    var confirmImport by remember { mutableStateOf(false) }
    var pickTime by remember { mutableStateOf(false) }
    var confirmSignOut by remember { mutableStateOf<Boolean?>(null) } // valor = havia algo não sincronizado
    var confirmDelete by remember { mutableStateOf(false) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    Column(
        modifier.fillMaxSize().background(c.background).statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.screen, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ChunkyButton("Voltar", callbacks.onBack, style = ChunkyStyle.Neutral, minHeight = 44.dp)
            Text("Configurações", style = MaterialTheme.typography.headlineMedium, color = c.ink)
        }

        Section("Conta", "Seu progresso é copiado para a nuvem a cada lição: trocar de celular restaura tudo.") {
            val social = ui.social
            Text(social.email ?: "Conta conectada", style = MaterialTheme.typography.titleSmall, color = c.ink)
            social.friendCode?.let { Text("Código de amigo: $it", style = MaterialTheme.typography.bodySmall, color = c.inkSoft) }
            SwitchRow(
                "Aparecer no ranking geral",
                "Desligado, só seus amigos veem você. Ainda dá para adicionar amigos pelo código.",
                social.showInGlobal, callbacks.onShowInGlobal,
            )
            val status = when {
                social.refreshing -> "Sincronizando…"
                social.pendingUploads > 0 -> if (social.pendingUploads == 1) "1 sessão esperando internet." else "${social.pendingUploads} sessões esperando internet."
                social.lastError != null -> "Última tentativa falhou: ${social.lastError}"
                social.lastSyncAtMs > 0 -> "Tudo sincronizado."
                else -> "Ainda não sincronizado."
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(status, style = MaterialTheme.typography.bodySmall, color = if (social.lastError != null) c.warningText else c.inkSoft, modifier = Modifier.weight(1f))
                ChunkyButton("Sincronizar", callbacks.onSyncNow, style = ChunkyStyle.Neutral, enabled = !social.refreshing, minHeight = 40.dp)
            }
            ChunkyButton(
                "Sair da conta", { scope.launch { confirmSignOut = callbacks.checkUnsynced() } }, Modifier.fillMaxWidth(), ChunkyStyle.Neutral, minHeight = 46.dp,
            )
            ChunkyButton("Excluir minha conta", { confirmDelete = true }, Modifier.fillMaxWidth(), ChunkyStyle.Error, minHeight = 46.dp)
        }

        Section("Lembretes", "Aviso no horário escolhido se você ainda não estudou, e às 21:30 se a ofensiva estiver em risco.") {
            SwitchRow("Lembrete diário", "Na voz do seu personagem. Só neste aparelho.", settings.reminderEnabled, callbacks.onReminderEnabled)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Horário", style = MaterialTheme.typography.titleSmall, color = c.ink, modifier = Modifier.weight(1f))
                ChunkyButton(
                    "%02d:%02d".format(settings.reminderTime.hour, settings.reminderTime.minute), { pickTime = true },
                    style = ChunkyStyle.Neutral, enabled = settings.reminderEnabled, minHeight = 44.dp,
                )
            }
            if (settings.reminderEnabled && !ui.notificationsAllowed) {
                Text(
                    "As notificações do app estão bloqueadas no Android, então os lembretes não aparecem.",
                    style = MaterialTheme.typography.bodySmall, color = c.errorText,
                )
                ChunkyButton("Permitir notificações", callbacks.onAllowNotifications, Modifier.fillMaxWidth(), ChunkyStyle.Primary, minHeight = 46.dp)
            }
            Text(
                "Alguns fabricantes atrasam avisos para economizar bateria. Se o lembrete chegar tarde, libere o BanQuest " +
                    "na otimização de bateria do aparelho.",
                style = MaterialTheme.typography.bodySmall, color = c.inkSoft,
            )
        }

        Section("Cronômetro de ritmo", "O diferencial do app: treine no ritmo da prova. Ele nunca reprova a resposta.") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PillChoice("Desligado", settings.timerMode == TimerMode.Off, { callbacks.onTimerMode(TimerMode.Off) })
                PillChoice("Discreto", settings.timerMode == TimerMode.Discreet, { callbacks.onTimerMode(TimerMode.Discreet) })
                PillChoice("Ritmo", settings.timerMode == TimerMode.Pace, { callbacks.onTimerMode(TimerMode.Pace) })
            }
            Text(
                when (settings.timerMode) {
                    TimerMode.Off -> "Sem relógio na tela. O tempo ainda é medido para o resumo."
                    TimerMode.Discreet -> "Sem relógio durante a questão; o tempo aparece só no resumo final."
                    TimerMode.Pace -> "Anel de tempo em cada questão, âmbar aos 75% e vermelho ao estourar, mais o tempo restante da lição."
                },
                style = MaterialTheme.typography.bodySmall, color = c.inkSoft,
            )
            Text("Tempo por questão", style = MaterialTheme.typography.titleSmall, color = c.ink)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PillChoice("3:00 (média da prova)", settings.paceTargetMs == PaceConfig.STANDARD_MS, { callbacks.onPaceTarget(PaceConfig.STANDARD_MS) }, Modifier.weight(1f))
                PillChoice("2:30 (sobra tempo)", settings.paceTargetMs == PaceConfig.TIGHT_MS, { callbacks.onPaceTarget(PaceConfig.TIGHT_MS) }, Modifier.weight(1f))
            }
            SwitchRow("Caso prático com 4:00", "Questões de caso prático ganham mais tempo.", settings.caseFourMinutes, callbacks.onCaseFour)
        }

        Section("Estudo") {
            SwitchRow("Modo sem vidas", "Sem corações: estude sem punição por erro.", settings.noLives, callbacks.onNoLives)
            SwitchRow("Sons", "Efeitos curtos de acerto, erro, combo e conquista.", settings.sounds, callbacks.onSounds)
            SwitchRow("Vibração", "Vibra de leve ao acertar e ao errar.", settings.haptics, callbacks.onHaptics)
        }

        Section("Tema") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PillChoice("Sistema", settings.themeMode == ThemeMode.System, { callbacks.onTheme(ThemeMode.System) })
                PillChoice("Claro", settings.themeMode == ThemeMode.Light, { callbacks.onTheme(ThemeMode.Light) })
                PillChoice("Escuro", settings.themeMode == ThemeMode.Dark, { callbacks.onTheme(ThemeMode.Dark) })
            }
        }

        Section("Conteúdo") {
            Text(
                "${ui.questionCount} questões no app, ${ui.examCount} delas de provas anteriores (com a banca e o ano na etiqueta). " +
                    "${ui.unreviewedCount} ainda não foram revisadas: as geradas por IA mostram o selo \"não revisada\", e nas de prova " +
                    "o selo avisa que só a explicação não foi revisada.",
                style = MaterialTheme.typography.bodyMedium, color = c.ink,
            )
            ChunkyButton("Importar pacote de conteúdo (.json)", callbacks.onImportPack, Modifier.fillMaxWidth(), ChunkyStyle.Secondary, minHeight = 46.dp)
            Text("Pacotes novos de unidades chegam como arquivo .json. Eles são validados antes de entrar na trilha.", style = MaterialTheme.typography.bodySmall, color = c.inkSoft)
            ui.importedPacks.forEach { pack ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(pack.unitTitle, style = MaterialTheme.typography.titleSmall, color = c.ink)
                        Text("${pack.pack} · versão ${pack.version} · ${pack.questions} questões", style = MaterialTheme.typography.bodySmall, color = c.inkSoft)
                    }
                    ChunkyButton("Remover", { callbacks.onRemovePack(pack.fileName) }, style = ChunkyStyle.Neutral, minHeight = 40.dp)
                }
            }
        }

        Section("Dados", "O progresso também fica salvo na sua conta. O arquivo de backup é uma cópia extra, só sua.") {
            ChunkyButton("Exportar progresso", callbacks.onExport, Modifier.fillMaxWidth(), ChunkyStyle.Neutral, minHeight = 46.dp)
            ChunkyButton("Importar progresso", { confirmImport = true }, Modifier.fillMaxWidth(), ChunkyStyle.Neutral, minHeight = 46.dp)
            Text("Exporte de vez em quando e guarde o arquivo: ele traz XP, ofensiva, lições, erros e conquistas.", style = MaterialTheme.typography.bodySmall, color = c.inkSoft)
            ChunkyButton("Zerar meu progresso", { confirmErase = true }, Modifier.fillMaxWidth(), ChunkyStyle.Error, minHeight = 46.dp)
        }

        Section("Sobre o app") {
            Text("BanQuest · versão ${ui.versionName}", style = MaterialTheme.typography.titleSmall, color = c.ink)
            val release = ui.update.status.releaseOrNull
            Text(
                when {
                    ui.update.checking -> "Procurando versão nova…"
                    release != null -> "Versão ${release.versionName} disponível." + if (release.notes.isNotBlank()) " ${release.notes}" else ""
                    ui.update.lastCheckMs > 0 -> "Você está na versão mais nova."
                    else -> "Ainda não conferido."
                },
                style = MaterialTheme.typography.bodySmall, color = if (release != null) c.secondaryText else c.inkSoft,
            )
            if (release != null) {
                ChunkyButton("Baixar a versão ${release.versionName}", { callbacks.onDownloadUpdate(release.apkUrl) }, Modifier.fillMaxWidth(), ChunkyStyle.Secondary, minHeight = 46.dp)
            }
            ChunkyButton(
                "Verificar atualização", callbacks.onCheckUpdate, Modifier.fillMaxWidth(), ChunkyStyle.Neutral,
                enabled = !ui.update.checking, minHeight = 46.dp,
            )
            Text(
                "Para instalar, o Android pede permissão para o navegador instalar apps. A versão nova entra por cima, sem perder nada.",
                style = MaterialTheme.typography.bodySmall, color = c.inkSoft,
            )
        }
    }

    if (pickTime) {
        val timeState = rememberTimePickerState(settings.reminderTime.hour, settings.reminderTime.minute, is24Hour = true)
        TrilhaDialog(
            onDismiss = { pickTime = false },
            title = "Horário do lembrete",
            content = {
                TimePicker(
                    state = timeState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = c.surfaceAlt, selectorColor = c.primary, timeSelectorSelectedContainerColor = c.primarySoft,
                        timeSelectorSelectedContentColor = c.primaryText, timeSelectorUnselectedContainerColor = c.surfaceAlt,
                    ),
                )
            },
            actions = {
                ChunkyButton("Salvar", { callbacks.onReminderTime(LocalTime.of(timeState.hour, timeState.minute)); pickTime = false }, Modifier.fillMaxWidth())
                ChunkyButton("Cancelar", { pickTime = false }, Modifier.fillMaxWidth(), ChunkyStyle.Neutral)
            },
        )
    }
    if (confirmImport) {
        TrilhaDialog(
            onDismiss = { confirmImport = false },
            title = "Importar progresso?",
            content = { Text("O progresso do arquivo substitui o atual. Se quiser guardar o atual, exporte antes.", style = MaterialTheme.typography.bodyMedium, color = c.ink) },
            actions = {
                ChunkyButton("Escolher arquivo", { confirmImport = false; callbacks.onImport() }, Modifier.fillMaxWidth())
                ChunkyButton("Cancelar", { confirmImport = false }, Modifier.fillMaxWidth(), ChunkyStyle.Neutral)
            },
        )
    }
    if (confirmErase) {
        TrilhaDialog(
            onDismiss = { confirmErase = false },
            title = "Zerar o progresso?",
            content = {
                Text(
                    "Isso apaga XP, ofensiva, gemas, progresso das lições e conquistas, neste aparelho e na nuvem. O XP já " +
                        "contado no ranking da semana e seus amigos continuam. Não dá para desfazer.",
                    style = MaterialTheme.typography.bodyMedium, color = c.ink,
                )
            },
            actions = {
                ChunkyButton("Sim, zerar", { callbacks.onErase(); confirmErase = false; callbacks.onBack() }, Modifier.fillMaxWidth(), ChunkyStyle.Error)
                ChunkyButton("Cancelar", { confirmErase = false }, Modifier.fillMaxWidth(), ChunkyStyle.Neutral)
            },
        )
    }
    AccountDialogs(confirmSignOut, confirmDelete, callbacks, onDismissSignOut = { confirmSignOut = null }, onDismissDelete = { confirmDelete = false })
}

@Composable
private fun AccountDialogs(
    confirmSignOut: Boolean?,
    confirmDelete: Boolean,
    callbacks: SettingsCallbacks,
    onDismissSignOut: () -> Unit,
    onDismissDelete: () -> Unit,
) {
    val c = Trilha.colors
    if (confirmSignOut != null) {
        TrilhaDialog(
            onDismiss = onDismissSignOut,
            title = "Sair da conta?",
            content = {
                Text(
                    if (confirmSignOut) {
                        "Parte do seu progresso ainda não chegou à nuvem (falta internet). Se sair agora, ele se perde. " +
                            "Conecte-se e toque em Sincronizar antes de sair."
                    } else {
                        "Seu progresso está salvo na nuvem. Este aparelho fica limpo e volta tudo quando você entrar de novo."
                    },
                    style = MaterialTheme.typography.bodyMedium, color = c.ink,
                )
            },
            actions = {
                ChunkyButton(
                    if (confirmSignOut) "Sair mesmo assim" else "Sair", { onDismissSignOut(); callbacks.onSignOut() }, Modifier.fillMaxWidth(),
                    if (confirmSignOut) ChunkyStyle.Error else ChunkyStyle.Primary,
                )
                ChunkyButton("Cancelar", onDismissSignOut, Modifier.fillMaxWidth(), ChunkyStyle.Neutral)
            },
        )
    }
    if (confirmDelete) {
        TrilhaDialog(
            onDismiss = onDismissDelete,
            title = "Excluir a conta?",
            content = {
                Text(
                    "Apaga para sempre sua conta, seu perfil, o progresso na nuvem, o ranking e as amizades. Seus amigos deixam de " +
                        "ver você. Não dá para desfazer.",
                    style = MaterialTheme.typography.bodyMedium, color = c.ink,
                )
            },
            actions = {
                ChunkyButton("Excluir para sempre", { onDismissDelete(); callbacks.onDeleteAccount() }, Modifier.fillMaxWidth(), ChunkyStyle.Error)
                ChunkyButton("Cancelar", onDismissDelete, Modifier.fillMaxWidth(), ChunkyStyle.Neutral)
            },
        )
    }
}

@Composable
private fun Section(title: String, subtitle: String? = null, content: @Composable () -> Unit) {
    val c = Trilha.colors
    Column(
        Modifier.fillMaxWidth().background(c.surface, RoundedCornerShape(22.dp)).border(2.dp, c.outline, RoundedCornerShape(22.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = c.primaryText)
        if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = c.inkSoft)
        content()
    }
}

@Composable
private fun SwitchRow(title: String, description: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    val c = Trilha.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = c.ink)
            Text(description, style = MaterialTheme.typography.bodySmall, color = c.inkSoft)
        }
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}
