package shot

import androidx.compose.ui.test.junit4.createComposeRule
import br.com.trilhati.data.prefs.Settings
import br.com.trilhati.feature.app.AppActions
import br.com.trilhati.feature.app.SocialUi
import br.com.trilhati.feature.ranking.RankingContent
import br.com.trilhati.feature.ranking.RankingScope
import br.com.trilhati.feature.settings.SettingsCallbacks
import br.com.trilhati.feature.settings.SettingsContent
import br.com.trilhati.feature.settings.SettingsUi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Telas que rolam, renderizadas numa "tela" bem alta para ver o conteúdo inteiro de uma vez. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w412dp-h2600dp-xxhdpi")
class TallShots {
    @get:Rule val rule = createComposeRule()

    private fun ranking(name: String, social: SocialUi, scope: RankingScope, dark: Boolean = false) =
        rule.shot(name, dark) { RankingContent(fakeState(true), social, NOW_MS, AppActions(), initialScope = scope, animated = false) }

    @Test fun rankingGlobal() = ranking("r_global.png", fakeSocial(), RankingScope.Global)
    /** Celular estreito com a fonte do sistema aumentada (0.14.2): a ofensiva "1 dia" não pode quebrar letra por letra. */
    @Test @Config(sdk = [35], qualifiers = "w360dp-h2600dp-xxhdpi")
    fun rankingGlobalFonteGrande() = rule.shot("r_global_fonte_grande.png") {
        val d = androidx.compose.ui.platform.LocalDensity.current
        androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(d.density, fontScale = 1.3f)) {
            RankingContent(fakeState(true), fakeSocial(), NOW_MS, AppActions(), initialScope = RankingScope.Global, animated = false)
        }
    }
    @Test fun rankingFriends() = ranking("r_friends.png", fakeSocial(), RankingScope.Friends)
    @Test fun rankingFriendsDark() = ranking("r_friends_dark.png", fakeSocial(), RankingScope.Friends, dark = true)
    @Test fun rankingNoFriends() = ranking("r_nofriends.png", fakeSocial(friends = false, requests = false, lastPodium = false), RankingScope.Friends)
    @Test fun rankingHiddenOffline() = ranking("r_hidden_offline.png", fakeSocial(hidden = true, pending = 2, error = "Sem conexão com a internet."), RankingScope.Global)
    @Test fun rankingNeverLoaded() = ranking("r_never.png", SocialUi(lastError = "Sem conexão com a internet."), RankingScope.Global)
    @Test fun settingsFull() = rule.shot("t_settings_full.png") {
        SettingsContent(
            SettingsUi(
                Settings(), 50, 50, false, emptyList(), "0.5.0", fakeSocial(pending = 1),
                br.com.trilhati.data.update.UpdateUi(
                    br.com.trilhati.data.update.AppVersion(8, "0.5.0"),
                    br.com.trilhati.domain.update.UpdateStatus.Available(
                        br.com.trilhati.domain.update.ReleaseInfo(9, "0.5.1", "https://exemplo.supabase.co/a.apk", "Correções no ranking.", 1),
                    ),
                    showBanner = true, lastCheckMs = NOW_MS,
                ),
            ),
            SettingsCallbacks(),
        )
    }

    /** Configurações no tema Chiclete: a seção Tema mostra os quatro temas em duas colunas. */
    @Test fun settingsChiclete() = rule.shot("th_settings_chiclete.png", palette = br.com.trilhati.ui.theme.ThemePalette.Chiclete) {
        SettingsContent(
            SettingsUi(Settings(palette = br.com.trilhati.ui.theme.ThemePalette.Chiclete), 50, 50, false, emptyList(), "0.8.0", fakeSocial()),
            SettingsCallbacks(),
        )
    }
}
