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
    @Test fun rankingFriends() = ranking("r_friends.png", fakeSocial(), RankingScope.Friends)
    @Test fun rankingFriendsDark() = ranking("r_friends_dark.png", fakeSocial(), RankingScope.Friends, dark = true)
    @Test fun rankingNoFriends() = ranking("r_nofriends.png", fakeSocial(friends = false, requests = false, lastPodium = false), RankingScope.Friends)
    @Test fun rankingHiddenOffline() = ranking("r_hidden_offline.png", fakeSocial(hidden = true, pending = 2, error = "Sem conexão com a internet."), RankingScope.Global)
    @Test fun rankingNeverLoaded() = ranking("r_never.png", SocialUi(lastError = "Sem conexão com a internet."), RankingScope.Global)
    @Test fun settingsFull() = rule.shot("t_settings_full.png") {
        SettingsContent(SettingsUi(Settings(), 50, 50, false, emptyList(), "0.4.0", fakeSocial(pending = 1)), SettingsCallbacks())
    }
}
