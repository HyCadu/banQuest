package br.com.trilhati.data.remote

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Endereço do projeto Supabase e a chave pública (publishable). A chave service_role nunca entra no app. */
data class SupabaseConfig(val url: String, val publishableKey: String) {
    val isConfigured: Boolean get() = url.startsWith("https://") && publishableKey.isNotBlank()
}

data class HttpResponse(val code: Int, val body: String)

/** Sem conexão (ou o servidor não respondeu). O app trata como "tente de novo mais tarde". */
class NetworkException(cause: Throwable) : IOException(cause.message, cause)

/** Transporte HTTP mínimo. A interface existe para os testes trocarem a rede por um servidor falso. */
interface HttpTransport {
    suspend fun post(url: String, headers: Map<String, String>, body: String): HttpResponse
}

/** Implementação com o `HttpURLConnection` do próprio Android: nenhuma biblioteca nova no app. */
class UrlConnectionTransport @Inject constructor() : HttpTransport {
    override suspend fun post(url: String, headers: Map<String, String>, body: String): HttpResponse = withContext(Dispatchers.IO) {
        val connection = try {
            URL(url).openConnection() as HttpURLConnection
        } catch (e: IOException) {
            throw NetworkException(e)
        }
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 15_000
            connection.readTimeout = 25_000
            connection.doOutput = true
            headers.forEach { (k, v) -> connection.setRequestProperty(k, v) }
            connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            val stream = if (code >= 400) connection.errorStream else connection.inputStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            HttpResponse(code, text)
        } catch (e: IOException) {
            throw NetworkException(e)
        } finally {
            connection.disconnect()
        }
    }
}
