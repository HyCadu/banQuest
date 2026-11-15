package br.com.trilhati.data.remote

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Mensagens que o aluno vê quando o Supabase Auth recusa ou falha ao mandar o código. */
class SupabaseAuthApiTest {
    private class OneAnswer(val code: Int, val body: String) : HttpTransport {
        var lastUrl = ""
        override suspend fun post(url: String, headers: Map<String, String>, body: String): HttpResponse {
            lastUrl = url
            return HttpResponse(code, this.body)
        }
    }

    private val clock = Clock.fixed(Instant.parse("2026-11-18T15:00:00Z"), ZoneOffset.UTC)
    private val config = SupabaseConfig("https://teste.supabase.co", "sb_publishable_teste")

    private fun sendCode(code: Int, body: String): ApiResult<Unit> = runBlocking {
        SupabaseAuthApi(config, OneAnswer(code, body), clock).sendCode("a@b.com")
    }

    @Test fun `falha do smtp explica o que conferir`() {
        val r = sendCode(500, """{"code":500,"error_code":"unexpected_failure","msg":"Error sending magic link email"}""")
        assertTrue(r is ApiResult.Err)
        r as ApiResult.Err
        assertEquals(ErrorKind.Server, r.kind)
        assertTrue(r.message, r.message.contains("SMTP"))
        // primeiro acesso de um e-mail novo usa o template de confirmação
        val c = sendCode(500, """{"code":500,"error_code":"unexpected_failure","msg":"Error sending confirmation email"}""") as ApiResult.Err
        assertTrue(c.message.contains("SMTP"))
    }

    @Test fun `outro erro 5xx mostra o texto do servidor`() {
        val r = sendCode(500, """{"code":500,"error_code":"unexpected_failure","msg":"Database error saving new user"}""") as ApiResult.Err
        assertTrue(r.message, r.message.contains("Database error saving new user"))
        val empty = sendCode(503, "") as ApiResult.Err
        assertEquals("O servidor deu erro (503). Tente de novo em instantes.", empty.message)
    }

    @Test fun `email fora da equipe sem smtp proprio`() {
        val r = sendCode(400, """{"code":400,"error_code":"email_address_not_authorized","msg":"Email address not authorized"}""") as ApiResult.Err
        assertTrue(r.message.contains("SMTP"))
    }

    @Test fun `limite de envios`() {
        val r = sendCode(429, """{"code":429,"error_code":"over_email_send_rate_limit","msg":"email rate limit exceeded"}""") as ApiResult.Err
        assertEquals(ErrorKind.RateLimited, r.kind)
    }

    @Test fun `sucesso chama o endpoint de otp`() {
        val http = OneAnswer(200, "{}")
        val r = runBlocking { SupabaseAuthApi(config, http, clock).sendCode("a@b.com") }
        assertTrue(r is ApiResult.Ok)
        assertEquals("https://teste.supabase.co/auth/v1/otp", http.lastUrl)
    }
}
