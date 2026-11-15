package br.com.trilhati.feature.app

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.trilhati.data.backup.BackupCodec
import br.com.trilhati.data.backup.BackupRead
import br.com.trilhati.data.backup.DocumentIo
import br.com.trilhati.data.content.ContentRepository
import br.com.trilhati.data.content.ImportedPack
import br.com.trilhati.data.content.PackLoad
import br.com.trilhati.data.prefs.Settings
import br.com.trilhati.data.prefs.SettingsRepository
import br.com.trilhati.data.repo.ProgressRepository
import br.com.trilhati.domain.pace.TimerMode
import br.com.trilhati.domain.progress.PurchaseResult
import br.com.trilhati.domain.progress.ShopItem
import br.com.trilhati.domain.time.millisFlow
import br.com.trilhati.domain.time.today
import br.com.trilhati.domain.time.todayFlow
import br.com.trilhati.notify.Notifications
import br.com.trilhati.notify.ReminderScheduler
import br.com.trilhati.sound.Sfx
import br.com.trilhati.sound.SoundPlayer
import br.com.trilhati.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** ViewModel do app inteiro (escopo da Activity): alimenta as 5 abas e executa as ações de loja, perfil, missões e dados. */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AppViewModel @Inject constructor(
    private val progress: ProgressRepository,
    private val settingsRepo: SettingsRepository,
    private val content: ContentRepository,
    private val clock: Clock,
    private val scheduler: ReminderScheduler,
    private val notifications: Notifications,
    private val sounds: SoundPlayer,
    private val documents: DocumentIo,
) : ViewModel() {

    val state: StateFlow<AppState?> = clock.todayFlow().flatMapLatest { day ->
        flow {
            progress.ensureReady(day)
            emitAll(
                combine(progress.snapshot(day), progress.observeQuests(day), settingsRepo.settings, content.courseFlow) { snap, quests, settings, course ->
                    AppState(day, course, snap, quests, settings)
                },
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Relógio de 1 s, para contagens regressivas (próximo coração, fim da semana da liga). */
    val ticker: Flow<Long> = clock.millisFlow(1_000)

    val settings: StateFlow<Settings> = settingsRepo.settings.stateIn(viewModelScope, SharingStarted.Eagerly, Settings())

    private val _notificationsAllowed = MutableStateFlow(notifications.canPost())

    /** Se o Android deixa o app mostrar notificações (permissão e chave do sistema). Atualizado ao voltar para o app. */
    val notificationsAllowed: StateFlow<Boolean> = _notificationsAllowed.asStateFlow()

    private val _importedPacks = MutableStateFlow<List<ImportedPack>>(emptyList())
    val importedPacks: StateFlow<List<ImportedPack>> = _importedPacks.asStateFlow()

    private val messageChannel = Channel<String>(Channel.BUFFERED)

    /** Mensagens curtas para o Snackbar ("Congelamento comprado!"). */
    val messages: Flow<String> = messageChannel.receiveAsFlow()

    init {
        // Reagenda o lembrete sempre que o app abre ou a preferência muda (PLAN.md §8).
        viewModelScope.launch {
            settingsRepo.settings.map { it.reminderEnabled to it.reminderTime }.distinctUntilChanged().collect { (on, time) ->
                scheduler.schedule(on, time)
            }
        }
        refreshImportedPacks()
    }

    fun message(text: String) {
        messageChannel.trySend(text)
    }

    private fun sfx(sound: Sfx) = sounds.play(sound, settings.value.sounds)

    fun refreshNotificationPermission() {
        _notificationsAllowed.value = notifications.canPost()
    }

    fun buy(item: ShopItem) = viewModelScope.launch {
        val label = if (item == ShopItem.StreakFreeze) "Congelamento de ofensiva" else "Corações recarregados"
        val result = progress.buy(item)
        val text = when (result) {
            is PurchaseResult.Success -> if (item == ShopItem.StreakFreeze) "$label comprado! Ele protege a ofensiva sozinho." else "$label! Pode voltar a estudar."
            is PurchaseResult.NotEnoughGems -> "Faltam ${result.missing} gemas para comprar isso."
            PurchaseResult.AlreadyFull -> if (item == ShopItem.StreakFreeze) "Você já tem 2 congelamentos, o máximo." else "Seus corações já estão cheios."
        }
        if (result is PurchaseResult.Success) sfx(Sfx.Complete)
        message(text)
    }

    fun claimQuest(id: String) = viewModelScope.launch {
        val gems = progress.claimQuest(clock.today(), id)
        if (gems != null) {
            sfx(Sfx.Achievement)
            message("Missão cumprida! +$gems gemas")
        }
    }

    fun claimDailyChest() = viewModelScope.launch {
        val gems = progress.claimDailyChest(clock.today())
        if (gems != null) sfx(Sfx.Achievement)
        message(if (gems != null) "Baú diário aberto: +$gems gemas!" else "O baú de hoje já foi aberto. Volte amanhã!")
    }

    fun claimUnitChest(unitId: String) = viewModelScope.launch {
        val gems = progress.claimUnitChest(unitId)
        if (gems != null) sfx(Sfx.Achievement)
        message(if (gems != null) "Baú da unidade aberto: +$gems gemas!" else "Esse baú já foi aberto.")
    }

    fun setCharacter(name: String) = viewModelScope.launch { progress.setCharacter(name) }
    fun setName(name: String) = viewModelScope.launch { progress.setName(name) }
    fun setGoal(xp: Int) = viewModelScope.launch { progress.setDailyGoal(xp) }

    fun setTimerMode(mode: TimerMode) = viewModelScope.launch { settingsRepo.setTimerMode(mode) }
    fun setPaceTarget(ms: Long) = viewModelScope.launch { settingsRepo.setPaceTarget(ms) }
    fun setCaseFourMinutes(on: Boolean) = viewModelScope.launch { settingsRepo.setCaseFourMinutes(on) }
    fun setNoLives(on: Boolean) = viewModelScope.launch { settingsRepo.setNoLives(on) }
    fun setHaptics(on: Boolean) = viewModelScope.launch { settingsRepo.setHaptics(on) }
    fun setSounds(on: Boolean) = viewModelScope.launch {
        settingsRepo.setSounds(on)
        if (on) sounds.play(Sfx.Correct, true)
    }
    fun setTheme(mode: ThemeMode) = viewModelScope.launch { settingsRepo.setTheme(mode) }
    fun setReminderEnabled(on: Boolean) = viewModelScope.launch { settingsRepo.setReminderEnabled(on) }
    fun setReminderTime(time: LocalTime) = viewModelScope.launch {
        settingsRepo.setReminderMinutes(time.hour * 60 + time.minute)
        message("Lembrete marcado para %02d:%02d.".format(time.hour, time.minute))
    }
    fun dismissReminderPrompt() = viewModelScope.launch { settingsRepo.dismissReminderPrompt() }

    // ---------- dados

    fun exportProgress(uri: Uri) = viewModelScope.launch {
        val text = BackupCodec.encode(progress.exportBackup())
        message(if (documents.write(uri, text)) "Progresso exportado. Guarde o arquivo em um lugar seguro." else "Não consegui gravar o arquivo.")
    }

    fun importProgress(uri: Uri) = viewModelScope.launch {
        val text = documents.read(uri) ?: return@launch message("Não consegui ler o arquivo.")
        when (val read = withContext(Dispatchers.Default) { BackupCodec.decode(text) }) {
            is BackupRead.Invalid -> message("Backup recusado: ${read.reason}.")
            is BackupRead.Ok -> {
                progress.importBackup(read.backup, clock.today())
                message("Progresso restaurado!")
            }
        }
    }

    fun importPack(uri: Uri) = viewModelScope.launch {
        val text = documents.read(uri) ?: return@launch message("Não consegui ler o arquivo.")
        val result = withContext(Dispatchers.IO) { content.importPack(text) }
        when (result) {
            is PackLoad.Ok -> {
                progress.recordPack(result.pack.pack, result.pack.version)
                val total = result.unit.lessons.sumOf { it.questions.size }
                message("Pacote importado: ${result.unit.title}, $total questões.")
                refreshImportedPacks()
            }
            is PackLoad.Invalid -> message("Pacote recusado (${result.errors.size} problema(s)): ${result.errors.first()}")
        }
    }

    fun removePack(fileName: String) = viewModelScope.launch {
        withContext(Dispatchers.IO) { content.removeImported(fileName) }
        refreshImportedPacks()
        message("Pacote removido.")
    }

    private fun refreshImportedPacks() = viewModelScope.launch {
        _importedPacks.value = withContext(Dispatchers.IO) { content.importedPacks() }
    }

    fun eraseAll() = viewModelScope.launch {
        progress.eraseAll()
        settingsRepo.eraseAll()
        progress.ensureReady(clock.today())
        message("Dados apagados. Começando do zero!")
    }
}
