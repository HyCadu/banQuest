package shot

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.com.trilhati.data.db.AppDatabase
import br.com.trilhati.data.remote.ApiResult
import br.com.trilhati.data.remote.AuthRepository
import br.com.trilhati.data.remote.ErrorKind
import br.com.trilhati.data.remote.SupabaseAuthApi
import br.com.trilhati.data.remote.SupabaseConfig
import br.com.trilhati.data.remote.SupabaseSocialApi
import br.com.trilhati.data.social.AuthStore
import br.com.trilhati.data.update.AppVersion
import br.com.trilhati.data.update.UpdateRepository
import br.com.trilhati.data.update.UpdateStore
import br.com.trilhati.domain.update.UpdateStatus
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/** Aviso de versão nova: cliente real (SupabaseSocialApi + UpdateRepository) contra o Supabase falso, sem login. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class UpdateCheckTest {
    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var clock: MutableClock
    private lateinit var server: FakeSupabase
    private lateinit var store: UpdateStore
    private val url = "https://exemplo.supabase.co/storage/v1/object/public/apk/banquest-0.5.1.apk"

    private fun repo(installed: Int): UpdateRepository {
        val config = SupabaseConfig("https://teste.supabase.co", "sb_publishable_teste")
        val auth = AuthRepository(SupabaseAuthApi(config, server, clock), AuthStore(context), clock)
        val notifications = br.com.trilhati.notify.Notifications(context).also { it.createChannels() }
        return UpdateRepository(SupabaseSocialApi(config, server, auth), store, notifications, AppVersion(installed, "0.5.0"), clock)
    }

    @Before fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        shadowOf(context as android.app.Application).grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS)
        clock = MutableClock(Instant.parse("2026-11-18T15:00:00Z"))
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        server = FakeSupabase(clock)
        store = UpdateStore(context)
        runBlocking { AuthStore(context).clear(); store.setLatest(null, 0); store.setNotified(0); store.setDismissed(0) }
    }

    @After fun tearDown() = db.close()

    private fun updateNotifications() = shadowOf(context.getSystemService(NotificationManager::class.java)).allNotifications
        .filter { it.channelId == br.com.trilhati.notify.Notifications.CHANNEL_UPDATES }

    @Test fun avisaUmaVezPorVersaoMesmoSemLogin() = runBlocking {
        val updates = repo(installed = 8)
        assertEquals(ApiResult.Ok(UpdateStatus.UpToDate), updates.check(force = true))
        assertTrue(updateNotifications().isEmpty())

        server.releases += FakeSupabase.Release(9, "0.5.1", url, "Correções no ranking")
        val r = updates.check(force = true) as ApiResult.Ok
        assertTrue(r.value is UpdateStatus.Available)
        val n = updateNotifications().single()
        assertEquals("BanQuest 0.5.1 disponível", n.extras.getString(Notification.EXTRA_TITLE))
        assertEquals("Correções no ranking", n.extras.getString(Notification.EXTRA_TEXT))
        assertTrue(updates.ui.first().showBanner)

        // de novo: não repete a notificação
        context.getSystemService(NotificationManager::class.java).cancelAll()
        updates.check(force = true)
        assertTrue(updateNotifications().isEmpty())
    }

    @Test fun agoraNaoEscondeOAvisoAteAProximaVersao() = runBlocking {
        val updates = repo(installed = 8)
        server.releases += FakeSupabase.Release(9, "0.5.1", url)
        updates.check(force = true)
        updates.dismiss(9)
        assertFalse(updates.ui.first().showBanner)
        server.releases += FakeSupabase.Release(10, "0.5.2", url)
        updates.check(force = true)
        assertTrue(updates.ui.first().showBanner)
    }

    @Test fun versaoAbaixoDaMinimaFicaObrigatoria() = runBlocking {
        val updates = repo(installed = 8)
        server.releases += FakeSupabase.Release(10, "0.6.0", url, "Servidor novo", minCode = 9)
        val r = updates.check(force = true) as ApiResult.Ok
        assertTrue(r.value is UpdateStatus.Required)
        assertFalse("obrigatória vira diálogo, não aviso", updates.ui.first().showBanner)
        assertEquals("Atualize o BanQuest", updateNotifications().single().extras.getString(Notification.EXTRA_TITLE))
    }

    @Test fun quemJaAtualizouNaoVeAviso() = runBlocking {
        server.releases += FakeSupabase.Release(9, "0.5.1", url)
        val updates = repo(installed = 9)
        assertEquals(ApiResult.Ok(UpdateStatus.UpToDate), updates.check(force = true))
        assertFalse(updates.ui.first().showBanner)
    }

    @Test fun semInternetMantemOUltimoEstadoEConfereNoMaximoACada30Min() = runBlocking {
        val updates = repo(installed = 8)
        server.releases += FakeSupabase.Release(9, "0.5.1", url)
        updates.check()
        val calls = server.paths.count { it.endsWith("latest_release") }
        clock.instant = clock.instant.plusSeconds(10 * 60)
        updates.check() // dentro de 30 min: não pergunta de novo
        assertEquals(calls, server.paths.count { it.endsWith("latest_release") })
        clock.instant = clock.instant.plusSeconds(25 * 60)
        server.offline = true
        val r = updates.check()
        assertTrue(r is ApiResult.Err && r.kind == ErrorKind.Network)
        assertTrue("o aviso continua com o último estado", updates.ui.first().status is UpdateStatus.Available)
    }
}
