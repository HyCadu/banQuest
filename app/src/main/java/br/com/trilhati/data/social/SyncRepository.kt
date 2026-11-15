package br.com.trilhati.data.social

import br.com.trilhati.data.backup.Backup
import br.com.trilhati.data.backup.BackupCodec
import br.com.trilhati.data.backup.BackupRead
import br.com.trilhati.data.content.ContentRepository
import br.com.trilhati.data.db.OutboxSessionEntity
import br.com.trilhati.data.db.ProgressDao
import br.com.trilhati.data.remote.ApiResult
import br.com.trilhati.data.remote.AuthRepository
import br.com.trilhati.data.remote.ErrorKind
import br.com.trilhati.data.remote.SocialApi
import br.com.trilhati.data.repo.ProgressRepository
import br.com.trilhati.domain.social.InitialSync
import br.com.trilhati.domain.social.InitialSyncRule
import br.com.trilhati.domain.social.PodiumRule
import br.com.trilhati.domain.social.SessionLimits
import br.com.trilhati.domain.streak.StreakEngine
import br.com.trilhati.domain.time.today
import br.com.trilhati.notify.Notifications
import java.security.MessageDigest
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

/** O que a interface precisa saber do lado social. */
data class SocialState(
    val signedIn: Boolean,
    val email: String?,
    val meta: SyncMeta,
    val pendingUploads: Int,
) {
    val overview: SocialOverview? get() = meta.overview
    val ready: Boolean get() = signedIn && meta.profileReady
}

/**
 * Sincronização com o Supabase. O progresso de estudo vive no Room (funciona offline); este repositório:
 *  1. envia a fila de sessões (ranking da semana e ofensiva em dupla);
 *  2. atualiza a ofensiva individual mostrada aos outros;
 *  3. sobe o retrato do progresso (o mesmo JSON do backup) com controle de versão, detectando conflito entre aparelhos;
 *  4. baixa ranking, amigos e pedidos para o cache (ver offline), avisa de pedido novo e concede o "Pódio da semana".
 */
@Singleton
class SyncRepository @Inject constructor(
    private val api: SocialApi,
    private val auth: AuthRepository,
    private val store: SyncStore,
    private val progress: ProgressRepository,
    private val dao: ProgressDao,
    private val content: ContentRepository,
    private val notifications: Notifications,
    private val clock: Clock,
) {
    private val lock = Mutex()
    private val json = Json { ignoreUnknownKeys = true }

    val state: Flow<SocialState> = combine(auth.session, store.meta, dao.observeOutboxCount()) { session, meta, pending ->
        SocialState(signedIn = session != null, email = session?.email, meta = meta, pendingUploads = pending)
    }

    // ------------------------------------------------------------------ perfil e primeira entrada

    /** Perfil do servidor (null = ainda não criado). */
    suspend fun remoteProfile(): ApiResult<RemoteProfile?> = api.myProfile()

    suspend fun saveProfile(displayName: String, mascot: String): ApiResult<RemoteProfile> {
        val result = api.upsertProfile(displayName, mascot)
        if (result is ApiResult.Ok) {
            progress.setName(result.value.displayName)
            progress.setCharacter(result.value.mascot)
        }
        return result
    }

    /**
     * Depois do login e do perfil: decide entre subir o progresso deste aparelho, baixar o da nuvem, ou perguntar
     * (os dois têm progresso). Devolve a decisão tomada; em [InitialSync.AskUser] o conflito fica salvo para a tela.
     */
    suspend fun initialSync(): ApiResult<InitialSync> = lock.withLock {
        val today = clock.today()
        progress.ensureReady(today)
        val cloud = when (val pulled = api.pullSnapshot()) {
            is ApiResult.Ok -> pulled.value
            is ApiResult.Err -> return@withLock pulled
        }
        val local = progress.exportBackup()
        val localHasProgress = (local.stats?.totalXp ?: 0) > 0 || (local.stats?.lessonsCompleted ?: 0) > 0
        val cloudStats = cloud?.let { decodeCloud(it) }?.stats
        val sameTotals = cloudStats != null && cloudStats.totalXp == (local.stats?.totalXp ?: 0) &&
            cloudStats.lessonsCompleted == (local.stats?.lessonsCompleted ?: 0)
        val decision = InitialSyncRule.decide(localHasProgress, cloud != null, sameTotals)
        when (decision) {
            InitialSync.UploadLocal -> {
                val push = pushLocked(local, expectedVersion = cloud?.version ?: 0)
                if (push is ApiResult.Err) return@withLock push
            }
            InitialSync.DownloadCloud -> {
                val backup = cloud?.let { decodeCloud(it) } ?: return@withLock ApiResult.Err(ErrorKind.Server, "O progresso salvo na nuvem está corrompido.")
                progress.importBackup(backup, today)
                store.setSnapshot(cloud.version, hashOf(progress.exportBackup()))
            }
            InitialSync.AskUser -> cloud?.let { store.setConflict(conflictOf(it, local)) }
        }
        store.setProfileReady(true)
        ApiResult.Ok(decision)
    }

    // ------------------------------------------------------------------ sincronização completa

    /** Uma rodada completa. Chamada ao abrir o app, depois de cada lição e a cada ~1 h em segundo plano. */
    suspend fun syncNow(): ApiResult<Unit> = lock.withLock {
        auth.current() ?: return@withLock ApiResult.Err(ErrorKind.NotSignedIn, "Entre na sua conta.")
        if (!store.current().profileReady) return@withLock ApiResult.Err(ErrorKind.ProfileMissing, "Crie seu perfil.")
        val today = clock.today()

        flushOutbox(today)?.let { return@withLock fail(it) }
        pushStreak(today)?.let { return@withLock fail(it) }
        if (store.current().conflict == null) {
            val push = pushLocked(progress.exportBackup(), store.current().snapshotVersion)
            if (push is ApiResult.Err) return@withLock fail(push)
        }
        when (val overview = refreshOverviewLocked()) {
            is ApiResult.Err -> fail(overview)
            is ApiResult.Ok -> ApiResult.Ok(Unit)
        }
    }

    private suspend fun fail(err: ApiResult.Err): ApiResult.Err {
        store.setError(err.message)
        if (err.kind == ErrorKind.ProfileMissing) store.setProfileReady(false)
        return err
    }

    /** Envia as sessões guardadas. Recusadas pelo servidor (fora dos limites) saem da fila para não travar. */
    private suspend fun flushOutbox(today: LocalDate): ApiResult.Err? {
        while (true) {
            val batch = dao.outbox(25)
            if (batch.isEmpty()) return null
            for (item in batch) {
                val upload = item.toUpload()
                if (!SessionLimits.accepts(upload.kind, upload.xp, upload.answers, upload.durationMs, LocalDate.parse(upload.day), today)) {
                    dao.deleteOutbox(item.id)
                    continue
                }
                when (val r = api.submitSession(upload)) {
                    is ApiResult.Ok -> dao.deleteOutbox(item.id)
                    is ApiResult.Err -> return r
                }
            }
        }
    }

    private suspend fun pushStreak(today: LocalDate): ApiResult.Err? {
        val stats = progress.exportBackup().stats ?: return null
        val streak = br.com.trilhati.domain.streak.StreakState(
            stats.streakCurrent, stats.streakLongest, LocalDate.ofEpochDay(stats.streakLastDay).takeIf { stats.streakLastDay >= 0 },
            stats.freezes,
        )
        val shown = StreakEngine.reconcile(streak, today).state
        val key = "${shown.current}|${shown.longest}|${shown.lastDay}"
        if (key == store.current().lastStreakSent) return null
        return when (val r = api.updateStreak(shown.current, maxOf(shown.longest, shown.current), shown.lastDay?.toString())) {
            is ApiResult.Ok -> { store.setLastStreakSent(key); null }
            is ApiResult.Err -> r
        }
    }

    private suspend fun pushLocked(backup: Backup, expectedVersion: Long): ApiResult<Unit> {
        val hash = hashOf(backup)
        val meta = store.current()
        if (hash == meta.lastPushedHash && expectedVersion == meta.snapshotVersion && expectedVersion > 0) return ApiResult.Ok(Unit)
        val payload = json.parseToJsonElement(BackupCodec.encode(backup))
        return when (val r = api.pushSnapshot(expectedVersion, store.deviceId(), payload)) {
            is ApiResult.Err -> r
            is ApiResult.Ok -> if (r.value.ok) {
                store.setSnapshot(r.value.version ?: (expectedVersion + 1), hash)
                ApiResult.Ok(Unit)
            } else {
                // Outro aparelho mexeu no progresso: guarda o conflito para o aluno decidir.
                when (val cloud = api.pullSnapshot()) {
                    is ApiResult.Ok -> cloud.value?.let { store.setConflict(conflictOf(it, backup)) }
                    is ApiResult.Err -> return cloud
                }
                ApiResult.Ok(Unit)
            }
        }
    }

    /** O aluno escolheu qual progresso fica: o deste aparelho ([keepLocal]) ou o da nuvem. */
    suspend fun resolveConflict(keepLocal: Boolean): ApiResult<Unit> = lock.withLock {
        val today = clock.today()
        val result = if (keepLocal) {
            pushLocked(progress.exportBackup(), expectedVersion = -1)
        } else {
            when (val pulled = api.pullSnapshot()) {
                is ApiResult.Err -> pulled
                is ApiResult.Ok -> {
                    val cloud = pulled.value
                    val backup = cloud?.let { decodeCloud(it) }
                    if (cloud != null && backup != null) {
                        progress.importBackup(backup, today)
                        store.setSnapshot(cloud.version, hashOf(progress.exportBackup()))
                    }
                    ApiResult.Ok(Unit)
                }
            }
        }
        if (result is ApiResult.Ok) store.setConflict(null)
        result
    }

    // ------------------------------------------------------------------ social

    suspend fun refreshOverview(): ApiResult<SocialOverview> = lock.withLock { refreshOverviewLocked() }

    private suspend fun refreshOverviewLocked(): ApiResult<SocialOverview> {
        val result = api.overview()
        if (result !is ApiResult.Ok) return result
        val overview = result.value
        store.setOverview(overview, clock.millis())
        notifyNewRequests(overview)
        maybeAwardPodium(overview)
        return result
    }

    private suspend fun notifyNewRequests(overview: SocialOverview) {
        val seen = store.current().seenRequestIds
        overview.requests.filter { it.requestId !in seen }.take(3).forEach {
            notifications.postSocial("Pedido de amizade", "${it.displayName} quer ser seu amigo no Trilha TI. Abra a aba Ranking para aceitar.")
        }
        store.markRequestsSeen(overview.requests.map { it.requestId }.toSet())
    }

    private suspend fun maybeAwardPodium(overview: SocialOverview) {
        val last = overview.lastWeek ?: return
        if (!PodiumRule.earned(last.pos, last.participants, last.xp)) return
        val weekStart = runCatching { LocalDate.parse(overview.weekStart).minusWeeks(1) }.getOrNull() ?: return
        val fresh = progress.awardPodium(weekStart, content.course)
        fresh.forEach { notifications.postSocial("Conquista: ${it.title}", "Você terminou a semana em ${last.pos}º lugar. +${it.rewardGems} gemas!") }
    }

    suspend fun requestFriend(userId: String): ApiResult<String> = afterChange(api.requestFriend(userId))
    suspend fun respondRequest(requestId: String, accept: Boolean): ApiResult<String> = afterChange(api.respondRequest(requestId, accept))
    suspend fun addFriendByCode(code: String): ApiResult<AddByCodeResult> = afterChange(api.addFriendByCode(code))
    suspend fun removeFriend(userId: String): ApiResult<String> = afterChange(api.removeFriend(userId))
    suspend fun setShowInGlobal(show: Boolean): ApiResult<RemoteProfile> = afterChange(api.setShowInGlobal(show))

    private suspend fun <T> afterChange(result: ApiResult<T>): ApiResult<T> {
        if (result is ApiResult.Ok) refreshOverview()
        return result
    }

    // ------------------------------------------------------------------ conta

    /** Há algo deste aparelho que ainda não chegou à nuvem? (avisar antes de sair da conta) */
    suspend fun hasUnsyncedChanges(): Boolean =
        dao.outbox(1).isNotEmpty() || hashOf(progress.exportBackup()) != store.current().lastPushedHash

    /** Sai da conta e limpa o progresso deste aparelho (ele está guardado na nuvem). */
    suspend fun signOut() {
        auth.signOut()
        clearLocal()
    }

    suspend fun deleteAccount(): ApiResult<Unit> {
        val result = api.deleteAccount()
        if (result is ApiResult.Ok) {
            auth.forgetSession()
            clearLocal()
        }
        return result
    }

    private suspend fun clearLocal() {
        store.clearAccount()
        dao.clearOutbox()
        progress.eraseAll()
        progress.ensureReady(clock.today())
    }

    // ------------------------------------------------------------------ utilitários

    private fun decodeCloud(snapshot: RemoteSnapshot): Backup? =
        (BackupCodec.decode(snapshot.payload.toString()) as? BackupRead.Ok)?.backup

    private fun conflictOf(cloud: RemoteSnapshot, local: Backup): SyncConflict {
        val remote = decodeCloud(cloud)
        return SyncConflict(
            serverVersion = cloud.version, serverDevice = cloud.deviceId, serverUpdatedAt = cloud.updatedAt,
            serverXp = remote?.stats?.totalXp ?: 0, serverLessons = remote?.stats?.lessonsCompleted ?: 0,
            localXp = local.stats?.totalXp ?: 0, localLessons = local.stats?.lessonsCompleted ?: 0,
        )
    }

    /** Impressão digital do progresso, ignorando a hora da exportação (para só reenviar quando algo mudou). */
    private fun hashOf(backup: Backup): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(BackupCodec.encode(backup.copy(exportedAtMs = 0)).toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun OutboxSessionEntity.toUpload() = SessionUpload(
        id = id, day = LocalDate.ofEpochDay(day).toString(), kind = kind, xp = xp, answers = answers, durationMs = durationMs,
        streak = streak, streakLongest = maxOf(streakLongest, streak),
        lastStudyDay = lastStudyDay.takeIf { it >= 0 }?.let { LocalDate.ofEpochDay(it).toString() },
    )
}
