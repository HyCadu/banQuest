package br.com.trilhati.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.trilhati.data.remote.ApiResult
import br.com.trilhati.data.remote.AuthRepository
import br.com.trilhati.data.repo.Profile
import br.com.trilhati.data.repo.ProgressRepository
import br.com.trilhati.data.social.SyncRepository
import br.com.trilhati.data.social.SyncScheduler
import br.com.trilhati.domain.social.SocialRules
import br.com.trilhati.domain.time.today
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Passos da entrada: e-mail → código → perfil (apelido e personagem) → primeira sincronização. */
enum class AuthStep { Email, Code, Profile, Syncing }

data class AuthUi(
    val step: AuthStep = AuthStep.Email,
    val email: String = "",
    val code: String = "",
    val name: String = "",
    val mascot: String = Profile.DEFAULT_CHARACTER,
    val busy: Boolean = false,
    val error: String? = null,
    val resendInSec: Int = 0,
    val returning: Boolean = false,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: AuthRepository,
    private val sync: SyncRepository,
    private val progress: ProgressRepository,
    private val scheduler: SyncScheduler,
    private val clock: Clock,
) : ViewModel() {
    private val _ui = MutableStateFlow(AuthUi())
    val ui: StateFlow<AuthUi> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            // Já tem sessão (o app fechou no meio da criação do perfil): pula direto para o perfil.
            if (auth.current() != null) goToProfile()
        }
    }

    fun onEmail(value: String) = _ui.update { it.copy(email = value.take(120), error = null) }
    fun onCode(value: String) = _ui.update { it.copy(code = value.filter(Char::isDigit).take(8), error = null) }
    fun onName(value: String) = _ui.update { it.copy(name = value.take(SocialRules.NAME_MAX), error = null) }
    fun onMascot(value: String) = _ui.update { it.copy(mascot = value) }

    fun sendCode() {
        val email = _ui.value.email.trim()
        if (!SocialRules.isValidEmail(email)) {
            _ui.update { it.copy(error = "Digite um e-mail válido.") }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(busy = true, error = null) }
            when (val r = auth.sendCode(email)) {
                is ApiResult.Ok -> {
                    _ui.update { it.copy(step = AuthStep.Code, busy = false, code = "") }
                    startResendCountdown()
                }
                is ApiResult.Err -> _ui.update { it.copy(busy = false, error = r.message) }
            }
        }
    }

    private fun startResendCountdown() = viewModelScope.launch {
        for (s in 60 downTo 0) {
            _ui.update { it.copy(resendInSec = s) }
            delay(1_000)
        }
    }

    fun changeEmail() = _ui.update { it.copy(step = AuthStep.Email, code = "", error = null) }

    fun verify() {
        val state = _ui.value
        if (state.code.length < 6) {
            _ui.update { it.copy(error = "O código tem 6 dígitos.") }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(busy = true, error = null) }
            when (val r = auth.verifyCode(state.email, state.code)) {
                is ApiResult.Ok -> goToProfile()
                is ApiResult.Err -> _ui.update { it.copy(busy = false, error = r.message) }
            }
        }
    }

    /** Prepara o passo do perfil: se a conta já tem perfil no servidor (reinstalação), traz nome e personagem dele. */
    private suspend fun goToProfile() {
        _ui.update { it.copy(busy = true, step = AuthStep.Profile, error = null) }
        progress.ensureReady(clock.today())
        val local = progress.snapshot(clock.today()).first().profile
        when (val r = sync.remoteProfile()) {
            is ApiResult.Ok -> {
                val remote = r.value
                _ui.update {
                    it.copy(
                        busy = false, returning = remote != null,
                        name = remote?.displayName ?: local.name.takeIf { n -> n != Profile.DEFAULT_NAME }.orEmpty(),
                        mascot = remote?.mascot ?: local.character,
                    )
                }
            }
            is ApiResult.Err -> _ui.update {
                it.copy(busy = false, error = r.message, name = local.name.takeIf { n -> n != Profile.DEFAULT_NAME }.orEmpty(), mascot = local.character)
            }
        }
    }

    fun saveProfile() {
        val name = SocialRules.normalizeName(_ui.value.name)
        if (!SocialRules.isValidName(name)) {
            _ui.update { it.copy(error = "O apelido precisa ter de ${SocialRules.NAME_MIN} a ${SocialRules.NAME_MAX} letras.") }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(busy = true, error = null) }
            when (val r = sync.saveProfile(name, _ui.value.mascot)) {
                is ApiResult.Err -> _ui.update { it.copy(busy = false, error = r.message) }
                is ApiResult.Ok -> {
                    _ui.update { it.copy(step = AuthStep.Syncing) }
                    when (val s = sync.initialSync()) {
                        is ApiResult.Ok -> {
                            // O AppRoot vê o perfil pronto e abre a Trilha.
                            scheduler.ensurePeriodic()
                            scheduler.syncSoon()
                        }
                        is ApiResult.Err -> _ui.update { it.copy(step = AuthStep.Profile, busy = false, error = s.message) }
                    }
                }
            }
        }
    }

    fun signOut() = viewModelScope.launch {
        auth.signOut()
        _ui.value = AuthUi()
    }
}
