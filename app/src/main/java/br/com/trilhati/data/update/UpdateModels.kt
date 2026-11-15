package br.com.trilhati.data.update

import br.com.trilhati.domain.update.ReleaseInfo
import br.com.trilhati.domain.update.UpdateStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Versão instalada neste aparelho (vem do BuildConfig). */
data class AppVersion(val code: Int, val name: String)

/** Espelha o JSON de `latest_release()` (supabase/migrations/0002_releases.sql). */
@Serializable
data class AppRelease(
    @SerialName("version_code") val versionCode: Int,
    @SerialName("version_name") val versionName: String,
    @SerialName("apk_url") val apkUrl: String,
    val notes: String = "",
    @SerialName("min_version_code") val minVersionCode: Int = 1,
    @SerialName("published_at") val publishedAt: String? = null,
) {
    fun toInfo() = ReleaseInfo(versionCode, versionName, apkUrl, notes, minVersionCode)
}

/** O que as telas mostram sobre atualização. */
data class UpdateUi(
    val current: AppVersion = AppVersion(0, "?"),
    val status: UpdateStatus = UpdateStatus.UpToDate,
    /** Aviso na Trilha (some com "Agora não"). */
    val showBanner: Boolean = false,
    val lastCheckMs: Long = 0,
    val checking: Boolean = false,
)
