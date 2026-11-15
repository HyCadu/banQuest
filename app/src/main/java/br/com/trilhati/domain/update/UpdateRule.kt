package br.com.trilhati.domain.update

/** Versão publicada (o que o servidor diz que é a mais nova). */
data class ReleaseInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val notes: String,
    val minVersionCode: Int,
)

/** Situação deste aparelho em relação à versão mais nova. */
sealed interface UpdateStatus {
    data object UpToDate : UpdateStatus

    /** Há versão nova; dá para continuar usando esta. */
    data class Available(val release: ReleaseInfo) : UpdateStatus

    /** Esta versão ficou abaixo da mínima: precisa atualizar para continuar (ex.: o servidor mudou). */
    data class Required(val release: ReleaseInfo) : UpdateStatus

    val releaseOrNull: ReleaseInfo?
        get() = when (this) {
            is Available -> release
            is Required -> release
            UpToDate -> null
        }
}

/** Regras do aviso de nova versão. */
object UpdateRule {
    /** Ao abrir ou voltar para o app, não pergunta ao servidor mais de uma vez a cada 30 min. */
    const val CHECK_INTERVAL_MS = 30 * 60_000L

    fun status(currentVersionCode: Int, latest: ReleaseInfo?): UpdateStatus = when {
        latest == null || !isSafeUrl(latest.apkUrl) -> UpdateStatus.UpToDate
        latest.versionCode <= currentVersionCode -> UpdateStatus.UpToDate
        currentVersionCode < latest.minVersionCode -> UpdateStatus.Required(latest)
        else -> UpdateStatus.Available(latest)
    }

    /** Só abre link https, sem espaços (o link vem do banco; nada de http ou esquema estranho). */
    fun isSafeUrl(url: String): Boolean = url.startsWith("https://") && url.length > "https://".length && url.none { it.isWhitespace() }

    /** Notificação: uma vez por versão nova. */
    fun shouldNotify(status: UpdateStatus, lastNotifiedCode: Int): Boolean =
        status.releaseOrNull?.let { it.versionCode > lastNotifiedCode } == true

    /** Aviso na Trilha: some quando o aluno toca "Agora não" para aquela versão. Obrigatória vira diálogo, não aviso. */
    fun showBanner(status: UpdateStatus, dismissedCode: Int): Boolean =
        status is UpdateStatus.Available && status.release.versionCode > dismissedCode

    fun shouldCheck(nowMs: Long, lastCheckMs: Long): Boolean = lastCheckMs <= 0 || nowMs - lastCheckMs >= CHECK_INTERVAL_MS
}
