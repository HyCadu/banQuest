package br.com.trilhati.domain.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateRuleTest {
    private fun release(code: Int, min: Int = 1, url: String = "https://exemplo.supabase.co/storage/v1/object/public/apk/banquest.apk") =
        ReleaseInfo(code, "0.$code", url, "Novidades", min)

    @Test fun `sem versao publicada ou versao igual ou menor esta em dia`() {
        assertEquals(UpdateStatus.UpToDate, UpdateRule.status(8, null))
        assertEquals(UpdateStatus.UpToDate, UpdateRule.status(8, release(8)))
        assertEquals(UpdateStatus.UpToDate, UpdateRule.status(9, release(8)))
    }

    @Test fun `versao maior fica disponivel e abaixo da minima fica obrigatoria`() {
        assertEquals(UpdateStatus.Available(release(9)), UpdateRule.status(8, release(9)))
        assertEquals(UpdateStatus.Available(release(10, min = 8)), UpdateRule.status(8, release(10, min = 8)))
        assertEquals(UpdateStatus.Required(release(10, min = 9)), UpdateRule.status(8, release(10, min = 9)))
    }

    @Test fun `link inseguro e ignorado`() {
        assertEquals(UpdateStatus.UpToDate, UpdateRule.status(8, release(9, url = "http://exemplo.com/a.apk")))
        assertEquals(UpdateStatus.UpToDate, UpdateRule.status(8, release(9, url = "javascript:alert(1)")))
        assertEquals(UpdateStatus.UpToDate, UpdateRule.status(8, release(9, url = "https://exemplo.com/a b.apk")))
        assertFalse(UpdateRule.isSafeUrl("https://"))
        assertTrue(UpdateRule.isSafeUrl("https://drive.google.com/uc?id=1"))
    }

    @Test fun `notifica uma vez por versao`() {
        val s = UpdateRule.status(8, release(9))
        assertTrue(UpdateRule.shouldNotify(s, lastNotifiedCode = 0))
        assertFalse(UpdateRule.shouldNotify(s, lastNotifiedCode = 9))
        assertTrue(UpdateRule.shouldNotify(UpdateRule.status(8, release(10)), lastNotifiedCode = 9))
        assertFalse(UpdateRule.shouldNotify(UpdateStatus.UpToDate, 0))
    }

    @Test fun `aviso na trilha some ao dispensar e obrigatoria nao vira aviso`() {
        val s = UpdateRule.status(8, release(9))
        assertTrue(UpdateRule.showBanner(s, dismissedCode = 0))
        assertFalse(UpdateRule.showBanner(s, dismissedCode = 9))
        assertTrue(UpdateRule.showBanner(UpdateRule.status(8, release(10)), dismissedCode = 9))
        assertFalse(UpdateRule.showBanner(UpdateRule.status(8, release(10, min = 9)), dismissedCode = 0))
    }

    @Test fun `confere no maximo a cada 30 minutos`() {
        assertTrue(UpdateRule.shouldCheck(nowMs = 1_000, lastCheckMs = 0))
        assertFalse(UpdateRule.shouldCheck(nowMs = 10 * 60_000L, lastCheckMs = 1))
        assertTrue(UpdateRule.shouldCheck(nowMs = 31 * 60_000L, lastCheckMs = 1))
    }
}
