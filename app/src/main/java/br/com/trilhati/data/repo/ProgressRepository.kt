package br.com.trilhati.data.repo

import androidx.room.withTransaction
import br.com.trilhati.data.db.AchievementEntity
import br.com.trilhati.data.db.AppDatabase
import br.com.trilhati.data.db.AttemptEntity
import br.com.trilhati.data.db.OutboxSessionEntity
import br.com.trilhati.data.db.ProgressDao
import br.com.trilhati.data.db.ShopStateEntity
import br.com.trilhati.data.db.UserProfileEntity
import br.com.trilhati.domain.achievement.AchievementEngine
import br.com.trilhati.domain.achievement.AchievementInput
import br.com.trilhati.domain.hearts.HeartsEngine
import br.com.trilhati.domain.progress.HelpEngine
import br.com.trilhati.domain.model.CourseUnit
import br.com.trilhati.domain.progress.DailyLog
import br.com.trilhati.domain.progress.ProcessedSession
import br.com.trilhati.domain.progress.ProgressProcessor
import br.com.trilhati.domain.progress.ProgressState
import br.com.trilhati.domain.progress.PurchaseResult
import br.com.trilhati.domain.progress.QuestProgress
import br.com.trilhati.domain.progress.SessionResult
import br.com.trilhati.domain.progress.ShopEngine
import br.com.trilhati.domain.progress.ShopItem
import br.com.trilhati.domain.progress.Stats
import br.com.trilhati.domain.progress.TrailEngine
import br.com.trilhati.domain.quest.QuestEngine
import br.com.trilhati.domain.streak.StreakEngine
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * Único ponto de acesso ao progresso do aluno. Carrega o estado do Room, delega toda regra aos motores de
 * domínio (puros, testados) e grava o resultado, sempre dentro de uma transação.
 */
@Singleton
class ProgressRepository @Inject constructor(
    private val db: AppDatabase,
    private val dao: ProgressDao,
    private val clock: Clock,
) {
    private fun nowMs() = clock.millis()

    // ---------- leitura reativa

    /** Retrato completo do progresso, reemitido a cada mudança no banco. [today] define dados "do dia". */
    fun snapshot(today: LocalDate): Flow<AppSnapshot> {
        val core = combine(dao.observeProfile(), dao.observeStats(), dao.observeLessonProgress(), dao.observeQuestionStats()) { p, s, l, q ->
            Core(p, s, l, q)
        }
        val extra = combine(
            dao.observeDailyLogs(today.minusDays(90).toEpochDay()), dao.observeAchievements(),
            dao.observeShop(), dao.observeOutboxCount(),
        ) { logs, ach, shop, outbox -> Extra(logs, ach, shop, outbox) }
        val counts = combine(dao.observeErrorBankCount(), dao.observeDueCount(today.toEpochDay())) { bank, due -> bank to due }
        return combine(core, extra, counts) { c, e, n ->
            AppSnapshot(
                profile = c.profile?.let { Profile(it.name, it.character, it.dailyGoalXp) } ?: Profile(Profile.DEFAULT_NAME, Profile.DEFAULT_CHARACTER, Profile.DEFAULT_GOAL_XP),
                stats = c.stats?.toDomain() ?: Stats(),
                lessonProgress = c.lessons.map { it.toDomain() }.associateBy { it.lessonId },
                questionStats = c.questions.map { it.toDomain() }.associateBy { it.questionId },
                dailyLogs = e.logs.map { it.toDomain() }.associateBy { it.day },
                achievements = e.achievements.toMap(),
                shop = e.shop?.toDomain() ?: ShopState(null, emptySet()),
                errorBankSize = n.first,
                dueCount = n.second,
                pendingUploads = e.outbox,
            )
        }
    }

    fun observeQuests(today: LocalDate): Flow<List<QuestProgress>> =
        dao.observeQuests(today.toEpochDay()).map { rows -> rows.map { it.toDomain() } }

    private class Core(
        val profile: UserProfileEntity?,
        val stats: br.com.trilhati.data.db.StatsEntity?,
        val lessons: List<br.com.trilhati.data.db.LessonProgressEntity>,
        val questions: List<br.com.trilhati.data.db.QuestionStatEntity>,
    )

    private class Extra(
        val logs: List<br.com.trilhati.data.db.DailyLogEntity>,
        val achievements: List<AchievementEntity>,
        val shop: ShopStateEntity?,
        val outbox: Int,
    )

    // ---------- preparação diária

    /**
     * Garante as linhas básicas, as 3 missões de [today] e o registro do dia, e reconcilia a ofensiva.
     * É barato e idempotente: chame ao abrir o app e quando o dia mudar.
     */
    suspend fun ensureReady(today: LocalDate): Unit = db.withTransaction { ensureReadyInternal(today) }

    private suspend fun ensureReadyInternal(today: LocalDate) {
        val now = nowMs()
        if (dao.profile() == null) {
            dao.upsertProfile(UserProfileEntity(name = Profile.DEFAULT_NAME, character = Profile.DEFAULT_CHARACTER, dailyGoalXp = Profile.DEFAULT_GOAL_XP, createdAtEpochMillis = now))
        }
        if (dao.stats() == null) dao.upsertStats(Stats(hearts = br.com.trilhati.domain.hearts.HeartsState(HeartsEngine.MAX, now)).toEntity())
        if (dao.shop() == null) dao.upsertShop(ShopState(null, emptySet()).toEntity())
        if (dao.dailyLog(today.toEpochDay()) == null) dao.upsertDailyLog(DailyLog(today).toEntity())
        if (dao.quests(today.toEpochDay()).isEmpty()) {
            dao.upsertQuests(QuestEngine.questsFor(today).map { QuestProgress(today, it.id).toEntity() })
        }
        // A ofensiva é reconciliada ao abrir o app: dias perdidos consomem congelamentos (ou zeram a ofensiva).
        // No mesmo passo entra o "Não sei" do dia.
        val stats = checkNotNull(dao.stats()).toDomain()
        val reconciled = StreakEngine.reconcile(stats.streak, today)
        val updated = stats.copy(streak = reconciled.state, help = HelpEngine.daily(stats.help, today))
        if (updated != stats) dao.upsertStats(updated.toEntity())
        if (reconciled.state != stats.streak) {
            reconciled.frozenDays.forEach { day ->
                val log = dao.dailyLog(day.toEpochDay())?.toDomain() ?: DailyLog(day)
                dao.upsertDailyLog(log.copy(froze = true).toEntity())
            }
        }
    }

    // ---------- conquistas que dependem do servidor

    /**
     * Registra um pódio (top 3 do ranking geral) na semana que começou em [weekStart], uma vez por semana.
     * Devolve as conquistas novas (o chamador mostra o aviso).
     */
    suspend fun awardPodium(weekStart: LocalDate, course: List<CourseUnit>): List<br.com.trilhati.domain.achievement.AchievementDef> = db.withTransaction {
        val shop = (dao.shop() ?: ShopStateEntity(1, -1, "")).toDomain()
        val key = "podium:$weekStart"
        if (key in shop.claimedUnitChests) return@withTransaction emptyList()
        // A marca "já premiado nesta semana" fica junto dos baús abertos: mesma natureza (recompensa única).
        dao.upsertShop(shop.copy(claimedUnitChests = shop.claimedUnitChests + key).toEntity())
        var stats = checkNotNull(dao.stats()).toDomain()
        stats = stats.copy(podiums = stats.podiums + 1)
        val unlocked = dao.achievementIds().toSet()
        val progress = dao.lessonProgress().map { it.toDomain() }.associateBy { it.lessonId }
        val input = AchievementInput(
            longestStreak = stats.streak.longest, totalXp = stats.totalXp, perfectLessons = stats.perfectLessons,
            unitsCompleted = TrailEngine.completedUnitCount(course, progress), pacedLessons = stats.pacedLessons,
            reviewCorrect = stats.reviewCorrect, podiums = stats.podiums,
        )
        val fresh = AchievementEngine.newlyUnlocked(input, unlocked)
        stats = stats.copy(gems = stats.gems + fresh.sumOf { it.rewardGems })
        dao.upsertStats(stats.toEntity())
        dao.upsertAchievements(fresh.map { AchievementEntity(it.id, nowMs()) })
        fresh
    }

    // ---------- sessões de estudo

    /** Grava o resultado de uma lição ou treino e devolve o resumo para a tela de resultado. */
    suspend fun applySession(result: SessionResult, today: LocalDate, course: List<CourseUnit>): ProcessedSession = db.withTransaction {
        ensureReadyInternal(today)
        val now = nowMs()
        val ids = result.records.map { it.question.id }.distinct()
        val profile = checkNotNull(dao.profile())
        val lessonProgress = dao.lessonProgress().map { it.toDomain() }.associateBy { it.lessonId }
        val state = ProgressState(
            stats = checkNotNull(dao.stats()).toDomain(),
            lessonProgress = lessonProgress,
            questionStats = dao.questionStats(ids).map { it.toDomain() }.associateBy { it.questionId },
            todayLog = dao.dailyLog(today.toEpochDay())?.toDomain() ?: DailyLog(today),
            todayQuests = dao.quests(today.toEpochDay()).map { it.toDomain() },
            unlockedAchievements = dao.achievementIds().toSet(),
            dailyGoalXp = profile.dailyGoalXp,
        )
        val before = TrailEngine.completedUnitCount(course, lessonProgress)
        val out = ProgressProcessor.apply(state, result, today, now, { TrailEngine.completedUnitCount(course, it) }, before)
        val s = out.state
        dao.upsertStats(s.stats.toEntity())
        result.lessonId?.let { id -> s.lessonProgress[id]?.let { dao.upsertLessonProgress(it.toEntity()) } }
        dao.upsertQuestionStats(s.questionStats.values.map { it.toEntity() })
        dao.upsertDailyLog(s.todayLog.toEntity())
        dao.upsertQuests(s.todayQuests.map { it.toEntity() })
        dao.upsertAchievements(out.summary.newAchievements.map { AchievementEntity(it.id, now) })
        // Fila para o servidor: alimenta o ranking da semana e a ofensiva em dupla (enviada pelo SyncWorker).
        dao.insertOutbox(
            OutboxSessionEntity(
                id = java.util.UUID.randomUUID().toString(), day = today.toEpochDay(),
                kind = when (result.kind) {
                    br.com.trilhati.domain.progress.SessionKind.Lesson -> "lesson"
                    br.com.trilhati.domain.progress.SessionKind.Practice -> "practice"
                    br.com.trilhati.domain.progress.SessionKind.Exam -> "exam"
                },
                xp = out.summary.totalXp, answers = result.records.size, durationMs = result.records.sumOf { it.timeMs },
                streak = s.stats.streak.current, streakLongest = s.stats.streak.longest,
                lastStudyDay = s.stats.streak.lastDay.toDay(), createdAtMs = now,
            ),
        )
        out.summary.frozenDays.forEach { day ->
            val log = dao.dailyLog(day.toEpochDay())?.toDomain() ?: DailyLog(day)
            dao.upsertDailyLog(log.copy(froze = true).toEntity())
        }
        dao.insertAttempts(
            result.records.map {
                AttemptEntity(questionId = it.question.id, correct = it.correct, retry = it.retry, timeMs = it.timeMs, atMs = now, kind = result.kind.name)
            },
        )
        out
    }

    suspend fun missedIds(lessonId: String): Set<String> = dao.missedIds(lessonId).toSet()
    suspend fun answeredIds(lessonId: String): Set<String> = dao.answeredIds(lessonId).toSet()
    suspend fun dueIds(today: LocalDate, limit: Int): List<String> = dao.dueIds(today.toEpochDay(), limit)
    suspend fun bankIds(limit: Int): List<String> = dao.bankIds(limit)

    // ---------- corações

    /** Perde [halves] metades de coração: 2 num erro de prova, 1 num erro de ensino (aprender e praticar). */
    suspend fun loseHeart(halves: Int = 2) = db.withTransaction {
        val stats = checkNotNull(dao.stats()).toDomain()
        dao.upsertStats(stats.copy(hearts = HeartsEngine.lose(stats.hearts, nowMs(), halves)).toEntity())
    }

    // ---------- botão "Não sei"

    /** Gasta um "Não sei". Devolve `false` se não havia nenhum. */
    suspend fun useHelp(): Boolean = db.withTransaction {
        val stats = checkNotNull(dao.stats()).toDomain()
        val used = HelpEngine.use(stats.help) ?: return@withTransaction false
        dao.upsertStats(stats.copy(help = used).toEntity())
        true
    }

    // ---------- loja, baús e missões

    suspend fun buy(item: ShopItem): PurchaseResult = db.withTransaction {
        val stats = checkNotNull(dao.stats()).toDomain()
        val hearts = HeartsEngine.regen(stats.hearts, nowMs())
        ShopEngine.canBuy(item, stats.gems, hearts.hearts, stats.streak.freezes, stats.help.count)?.let { return@withTransaction it }
        val paid = stats.copy(gems = stats.gems - item.priceGems)
        val updated = when (item) {
            ShopItem.StreakFreeze -> paid.copy(streak = StreakEngine.addFreeze(stats.streak))
            ShopItem.RefillHearts -> paid.copy(hearts = HeartsEngine.refill(nowMs()))
            ShopItem.Help -> paid.copy(help = HelpEngine.buy(stats.help))
        }
        dao.upsertStats(updated.toEntity())
        PurchaseResult.Success(updated.gems)
    }

    /** Resgata a recompensa de uma missão concluída. Devolve as gemas ganhas ou `null` se não pôde. */
    suspend fun claimQuest(today: LocalDate, questId: String): Int? = db.withTransaction {
        val def = QuestEngine.byId(questId) ?: return@withTransaction null
        val row = dao.quests(today.toEpochDay()).firstOrNull { it.questId == questId } ?: return@withTransaction null
        if (row.claimed || !QuestEngine.isComplete(def, row.progress)) return@withTransaction null
        dao.upsertQuests(listOf(row.copy(claimed = true)))
        val stats = checkNotNull(dao.stats())
        dao.upsertStats(stats.copy(gems = stats.gems + def.rewardGems))
        def.rewardGems
    }

    suspend fun claimDailyChest(today: LocalDate): Int? = db.withTransaction {
        val shop = (dao.shop() ?: ShopStateEntity(1, -1, "")).toDomain()
        if (shop.lastDailyChestDay == today) return@withTransaction null
        val gems = ShopEngine.dailyChestGems(today)
        dao.upsertShop(shop.copy(lastDailyChestDay = today).toEntity())
        val stats = checkNotNull(dao.stats())
        dao.upsertStats(stats.copy(gems = stats.gems + gems))
        gems
    }

    /** Abre o baú de uma unidade concluída. O chamador garante que a unidade está completa. */
    suspend fun claimUnitChest(unitId: String): Int? = db.withTransaction {
        val shop = (dao.shop() ?: ShopStateEntity(1, -1, "")).toDomain()
        if (unitId in shop.claimedUnitChests) return@withTransaction null
        dao.upsertShop(shop.copy(claimedUnitChests = shop.claimedUnitChests + unitId).toEntity())
        val stats = checkNotNull(dao.stats())
        dao.upsertStats(stats.copy(gems = stats.gems + ShopEngine.UNIT_CHEST_GEMS))
        ShopEngine.UNIT_CHEST_GEMS
    }

    // ---------- perfil

    suspend fun setCharacter(character: String) = db.withTransaction {
        dao.profile()?.let { dao.upsertProfile(it.copy(character = character)) }
    }

    suspend fun setName(name: String) = db.withTransaction {
        dao.profile()?.let { dao.upsertProfile(it.copy(name = name.trim().ifBlank { Profile.DEFAULT_NAME }.take(24))) }
    }

    suspend fun setDailyGoal(xp: Int) = db.withTransaction {
        dao.profile()?.let { dao.upsertProfile(it.copy(dailyGoalXp = xp)) }
    }

    // ---------- backup

    suspend fun exportBackup(): br.com.trilhati.data.backup.Backup = db.withTransaction {
        br.com.trilhati.data.backup.Backup(
            exportedAtMs = nowMs(),
            profile = dao.profile(), stats = dao.stats(), lessonProgress = dao.lessonProgress(),
            questionStats = dao.allQuestionStats(), attempts = dao.allAttempts(), dailyLogs = dao.allDailyLogs(),
            quests = dao.allQuests(), achievements = dao.allAchievements(),
            shop = dao.shop(), packs = dao.packMetas(),
        )
    }

    /** Substitui todo o progresso pelo do backup, de uma vez (se algo falhar, nada muda). */
    suspend fun importBackup(backup: br.com.trilhati.data.backup.Backup, today: LocalDate) = db.withTransaction {
        dao.deleteProfile(); dao.deleteStats(); dao.deleteLessonProgress(); dao.deleteQuestionStats(); dao.deleteAttempts()
        dao.deleteDailyLogs(); dao.deleteQuests(); dao.deleteAchievements(); dao.deleteShop(); dao.deletePackMetas()
        backup.profile?.let { dao.upsertProfile(it) }
        backup.stats?.let { dao.upsertStats(it) }
        dao.upsertLessonProgressList(backup.lessonProgress)
        dao.upsertQuestionStats(backup.questionStats)
        dao.upsertAttempts(backup.attempts)
        dao.upsertDailyLogs(backup.dailyLogs)
        dao.upsertQuests(backup.quests)
        dao.upsertAchievements(backup.achievements)
        backup.shop?.let { dao.upsertShop(it) }
        dao.upsertPackMetas(backup.packs)
        ensureReadyInternal(today)
    }

    suspend fun recordPack(pack: String, version: Int) = dao.upsertPackMeta(br.com.trilhati.data.db.PackMetaEntity(pack, version))

    /** Apaga todo o progresso (Configurações > Apagar dados). */
    suspend fun eraseAll() = db.clearAllTables()
}
