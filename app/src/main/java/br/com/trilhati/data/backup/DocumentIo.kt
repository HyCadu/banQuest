package br.com.trilhati.data.backup

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Lê e grava arquivos escolhidos pelo aluno no seletor do Android (sem permissão de armazenamento). */
@Singleton
class DocumentIo @Inject constructor(@ApplicationContext private val context: Context) {
    /** Limite de leitura: protege contra arquivos enormes escolhidos por engano. */
    private val maxBytes = 5 * 1024 * 1024

    suspend fun write(uri: Uri, text: String): Boolean = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openOutputStream(uri, "wt")?.use { it.write(text.toByteArray(Charsets.UTF_8)) } != null
        } catch (_: IOException) {
            false
        } catch (_: SecurityException) {
            false
        }
    }

    suspend fun read(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val bytes = input.readNBytesCompat(maxBytes + 1)
                if (bytes.size > maxBytes) null else bytes.toString(Charsets.UTF_8)
            }
        } catch (_: IOException) {
            null
        } catch (_: SecurityException) {
            null
        }
    }

    private fun java.io.InputStream.readNBytesCompat(limit: Int): ByteArray {
        val out = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(8 * 1024)
        while (out.size() < limit) {
            val n = read(buffer, 0, minOf(buffer.size, limit - out.size()))
            if (n < 0) break
            out.write(buffer, 0, n)
        }
        return out.toByteArray()
    }
}
