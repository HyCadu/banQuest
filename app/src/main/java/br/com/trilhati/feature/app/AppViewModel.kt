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
import br.com.trilhati.data.remote.ApiResult
import br.com.trilhati.data.repo.ProgressRepository
import br.com.trilhati.data.social.SocialState
import br.com.trilhati.data.social.SyncRepository
import br.com.trilhati.data.social.SyncScheduler
import br.com.trilhati.domain.social.SocialMessages
import br.com.trilhati.domain.social.SocialRules
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
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Qual tela raiz mostrar: carregando, entrada (sem conta ou sem perfil) ou o app. */
enum class Gate { Loading, Login, Ready }

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
    private val sync: SyncRepository,
    private val syncScheduler: SyncScheduler,
) : ViewModel() {

    private val socialState: StateFlow<SocialState?> = sync.state.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** Sem conta (ou sem perfil) o app abre na tela de entrada. */
    val gate: StateFlow<Gate> = socialState.map { s ->
        when {
            s == null -> Gate.Loading
            s.ready -> Gate.Ready
            else -> Gate.Login
        }
    }.distinctUntilChanged().stateIn(viewModelScope, SharingStarted.Eagerly, Gate.Loading)

    private val refreshing = MutableStateFlow(false)

    /** Ranking, amigos e situação da sincronização, para a aba Ranking e as Configurações. */
    val social: StateFlow<SocialUi> = combine(socialState, refreshing) { s, busy ->
        SocialUi(
            overview = s?.overview, email = s?.email, pendingUploads = s?.pendingUploads ?: 0,
            lastSyncAtMs = s?.meta?.lastSyncAtMs ?: 0, lastError = s?.meta?.lastError, refreshing = busy, conflict = s?.meta?.conflict,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SocialUi())

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

    /** Relógio de 1 s, para contagens regressivas (próximo coração, fim da semana do ranking). */
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
        // Com a conta pronta: agenda a sincronização de hora em hora e sincroniza ao abrir o app.
        viewModelScope.launch {
            gate.filter { it == Gate.Ready }.collect {
                syncScheduler.ensurePeriodic()
                syncNow(quiet = true)
            }
        }
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

    // ---------- social

    /** Ao voltar para o app: sincroniza se a última vez foi há mais de 5 minutos. */
    fun onAppResumed() {
        val s = socialState.value ?: return
        if (s.ready && !refreshing.value && clock.millis() - s.meta.lastSyncAtMs > RESUME_SYNC_MS) syncNow(quiet = true)
    }

    fun refreshSocial() = syncNow(quiet = false)

    private fun syncNow(quiet: Boolean) {
        if (refreshing.value) return
        viewModelScope.launch {
            refreshing.value = true
            val result = sync.syncNow()
            refreshing.value = false
            if (result is ApiResult.Err && !quiet) message(result.message)
        }
    }

    fun requestFriend(userId: String) = viewModelScope.launch {
        when (val r = sync.requestFriend(userId)) {
            is ApiResult.Ok -> message(SocialMessages.request(r.value))
            is ApiResult.Err -> message(r.message)
        }
    }

    fun respondRequest(requestId: String, accept: Boolean) = viewModelScope.launch {
        when (val r = sync.respondRequest(requestId, accept)) {
            is ApiResult.Ok -> {
                if (r.value == "accepted") sfx(Sfx.Achievement)
                message(SocialMessages.respond(r.value, accept))
            }
            is ApiResult.Err -> message(r.message)
        }
    }

    fun addFriendByCode(input: String) {
        val code = SocialRules.normalizeFriendCode(input) ?: return message(SocialMessages.INVALID_CODE)
        viewModelScope.launch {
            when (val r = sync.addFriendByCode(code)) {
                is ApiResult.Ok -> {
                    if (r.value.result == "added") sfx(Sfx.Achievement)
                    message(SocialMessages.addByCode(r.value.result, r.value.displayName))
                }
                is ApiResult.Err -> message(r.message)
            }
        }
    }

    fun removeFriend(userId: String) = viewModelScope.launch {
        when (val r = sync.removeFriend(userId)) {
            is ApiResult.Ok -> message("Amizade desfeita.")
            is ApiResult.Err -> message(r.message)
        }
    }

    fun setShowInGlobal(show: Boolean) = viewModelScope.launch {
        when (val r = sync.setShowInGlobal(show)) {
            is ApiResult.Ok -> message(if (show) "Você voltou a aparecer no ranking geral." else "Pronto: só seus amigos te veem agora.")
            is ApiResult.Err -> message(r.message)
        }
    }

    /** Conflito entre aparelhos: [keepLocal] mantém o progresso deste aparelho; senão traz o da nuvem. */
    fun resolveConflict(keepLocal: Boolean) = viewModelScope.launch {
        when (val r = sync.resolveConflict(keepLocal)) {
            is ApiResult.Ok -> message(if (keepLocal) "Progresso deste aparelho salvo na nuvem." else "Progresso da nuvem restaurado.")
            is ApiResult.Err -> message(r.message)
        }
    }

    /** Sai da conta. Devolve se havia algo ainda não enviado (a tela pergunta antes). */
    suspend fun hasUnsyncedChanges(): Boolean = sync.hasUnsyncedChanges()

    fun signOut() = viewModelScope.launch {
        syncScheduler.cancelAll()
        sync.signOut()
    }

    fun deleteAccount() = viewModelScope.launch {
        when (val r = sync.deleteAccount()) {
            is ApiResult.Ok -> syncScheduler.cancelAll()
            is ApiResult.Err -> message(r.message)
        }
    }

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
                syncScheduler.syncSoon()
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
        message("Progresso zerado. Começando do zero!")
        syncScheduler.syncSoon()
    }

    private companion object {
        const val RESUME_SYNC_MS = 5 * 60_000L
    }
}
