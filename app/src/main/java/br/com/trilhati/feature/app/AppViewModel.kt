package br.com.trilhati.feature.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.trilhati.data.content.ContentRepository
import br.com.trilhati.data.prefs.Settings
import br.com.trilhati.data.prefs.SettingsRepository
import br.com.trilhati.data.repo.ProgressRepository
import br.com.trilhati.domain.pace.TimerMode
import br.com.trilhati.domain.progress.PurchaseResult
import br.com.trilhati.domain.progress.ShopItem
import br.com.trilhati.domain.time.millisFlow
import br.com.trilhati.domain.time.today
import br.com.trilhati.domain.time.todayFlow
import br.com.trilhati.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** ViewModel do app inteiro (escopo da Activity): alimenta as 5 abas e executa as ações da loja, perfil e missões. */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AppViewModel @Inject constructor(
    private val progress: ProgressRepository,
    private val settingsRepo: SettingsRepository,
    private val content: ContentRepository,
    private val clock: Clock,
) : ViewModel() {

    val state: StateFlow<AppState?> = clock.todayFlow().flatMapLatest { day ->
        flow {
            progress.ensureReady(day)
            val course = content.course
            emitAll(
                combine(progress.snapshot(day), progress.observeQuests(day), settingsRepo.settings) { snap, quests, settings ->
                    AppState(day, course, snap, quests, settings)
                },
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Relógio de 1 s, para contagens regressivas (próximo coração, fim da semana da liga). */
    val ticker: Flow<Long> = clock.millisFlow(1_000)

    val settings: StateFlow<Settings> = settingsRepo.settings.stateIn(viewModelScope, SharingStarted.Eagerly, Settings())

    private val messageChannel = Channel<String>(Channel.BUFFERED)

    /** Mensagens curtas para o Snackbar ("Congelamento comprado!"). */
    val messages: Flow<String> = messageChannel.receiveAsFlow()

    fun message(text: String) {
        messageChannel.trySend(text)
    }

    fun buy(item: ShopItem) = viewModelScope.launch {
        val label = if (item == ShopItem.StreakFreeze) "Congelamento de ofensiva" else "Corações recarregados"
        val text = when (val r = progress.buy(item)) {
            is PurchaseResult.Success -> if (item == ShopItem.StreakFreeze) "$label comprado! Ele protege a ofensiva sozinho." else "$label! Pode voltar a estudar."
            is PurchaseResult.NotEnoughGems -> "Faltam ${r.missing} gemas para comprar isso."
            PurchaseResult.AlreadyFull -> if (item == ShopItem.StreakFreeze) "Você já tem 2 congelamentos, o máximo." else "Seus corações já estão cheios."
        }
        message(text)
    }

    fun claimQuest(id: String) = viewModelScope.launch {
        val gems = progress.claimQuest(clock.today(), id)
        if (gems != null) message("Missão cumprida! +$gems gemas")
    }

    fun claimDailyChest() = viewModelScope.launch {
        val gems = progress.claimDailyChest(clock.today())
        message(if (gems != null) "Baú diário aberto: +$gems gemas!" else "O baú de hoje já foi aberto. Volte amanhã!")
    }

    fun claimUnitChest(unitId: String) = viewModelScope.launch {
        val gems = progress.claimUnitChest(unitId)
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
    fun setTheme(mode: ThemeMode) = viewModelScope.launch { settingsRepo.setTheme(mode) }

    fun eraseAll() = viewModelScope.launch {
        progress.eraseAll()
        settingsRepo.eraseAll()
        progress.ensureReady(clock.today())
        message("Dados apagados. Começando do zero!")
    }
}
