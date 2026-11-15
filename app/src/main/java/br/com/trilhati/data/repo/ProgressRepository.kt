package br.com.trilhati.data.repo

import androidx.room.withTransaction
import br.com.trilhati.data.db.AchievementEntity
import br.com.trilhati.data.db.AppDatabase
import br.com.trilhati.data.db.AttemptEntity
import br.com.trilhati.data.db.LeagueWeekEntity
import br.com.trilhati.data.db.ProgressDao
import br.com.trilhati.data.db.ShopStateEntity
import br.com.trilhati.data.db.UserProfileEntity
import br.com.trilhati.domain.achievement.AchievementEngine
import br.com.trilhati.domain.achievement.AchievementInput
import br.com.trilhati.domain.hearts.HeartsEngine
import br.com.trilhati.domain.league.LeagueEngine
import br.com.trilhati.domain.league.LeagueOutcome
import br.com.trilhati.domain.league.LeagueTier
import br.com.trilhati.domain.model.CourseUnit
import br.com.trilhati.domain.progress.DailyLog
import br.com.trilhati.domain.progress.LeagueWeekState
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
            dao.observeLatestLeague(), dao.observeLastClosedLeague(), dao.observeShop(),
        ) { logs, ach, league, closed, shop -> Extra(logs, ach, league, closed, shop) }
        val counts = combine(dao.observeErrorBankCount(), dao.observeDueCount(today.toEpochDay())) { bank, due -> bank to due }
        return combine(core, extra, counts) { c, e, n ->
            AppSnapshot(
                profile = c.profile?.let { Profile(it.name, it.character, it.dailyGoalXp) } ?: Profile(Profile.DEFAULT_NAME, Profile.DEFAULT_CHARACTER, Profile.DEFAULT_GOAL_XP),
                stats = c.stats?.toDomain() ?: Stats(),
                lessonProgress = c.lessons.map { it.toDomain() }.associateBy { it.lessonId },
                questionStats = c.questions.map { it.toDomain() }.associateBy { it.questionId },
                dailyLogs = e.logs.map { it.toDomain() }.associateBy { it.day },
                achievements = e.achievements.toMap(),
                league = e.league?.toDomain() ?: LeagueWeekState(LeagueEngine.weekStart(today)),
                lastLeagueResult = e.closed?.let { LeagueResult(LocalDate.ofEpochDay(it.weekStart), it.tier, it.finalRank, it.outcome) },
                shop = e.shop?.toDomain() ?: ShopState(null, emptySet()),
                errorBankSize = n.first,
                dueCount = n.second,
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
        val league: LeagueWeekEntity?,
        val closed: LeagueWeekEntity?,
        val shop: ShopStateEntity?,
    )

    // ---------- preparação diária

    /**
     * Garante as linhas básicas, as 3 missões de [today], o registro do dia e a liga da semana (fechando semanas
     * passadas). É barato e idempotente: chame ao abrir o app e quando o dia mudar.
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
        val stats = checkNotNull(dao.stats()).toDomain()
        val reconciled = StreakEngine.reconcile(stats.streak, today)
        if (reconciled.state != stats.streak) {
            dao.upsertStats(stats.copy(streak = reconciled.state).toEntity())
            reconciled.frozenDays.forEach { day ->
                val log = dao.dailyLog(day.toEpochDay())?.toDomain() ?: DailyLog(day)
                dao.upsertDailyLog(log.copy(froze = true).toEntity())
            }
        }
        reconcileLeague(today)
    }

    private suspend fun reconcileLeague(today: LocalDate) {
        val currentWeek = LeagueEngine.weekStart(today)
        var week = dao.latestLeague()?.toDomain()
        if (week == null) {
            dao.upsertLeague(LeagueWeekEntity(currentWeek.toEpochDay(), 0, 0, closed = false, finalRank = -1, outcome = 0))
            return
        }
        val name = dao.profile()?.name ?: Profile.DEFAULT_NAME
        var bestTier = checkNotNull(dao.stats()).bestLeagueTier
        var closedAny = false
        while (week!!.weekStart.isBefore(currentWeek)) {
            val tier = LeagueTier.fromIndex(week.tier)
            val standings = LeagueEngine.standings(week.weekStart, tier, 1.0, name, week.userXp)
            val rank = LeagueEngine.rankOfUser(standings)
            val close = LeagueEngine.close(tier, rank, week.userXp)
            val outcome = when (close.outcome) { LeagueOutcome.Promoted -> 1; LeagueOutcome.Relegated -> -1; LeagueOutcome.Stayed -> 0 }
            dao.upsertLeague(LeagueWeekEntity(week.weekStart.toEpochDay(), week.tier, week.userXp, closed = true, finalRank = rank, outcome = outcome))
            bestTier = maxOf(bestTier, close.newTier.ordinal)
            week = LeagueWeekState(week.weekStart.plusWeeks(1), close.newTier.ordinal, 0)
            dao.upsertLeague(LeagueWeekEntity(week.weekStart.toEpochDay(), week.tier, 0, closed = false, finalRank = -1, outcome = 0))
            closedAny = true
        }
        if (closedAny) {
            var stats = checkNotNull(dao.stats()).toDomain().copy(bestLeagueTier = bestTier)
            val unlocked = dao.achievementIds().toSet()
            val input = AchievementInput(
                longestStreak = stats.streak.longest, totalXp = stats.totalXp, perfectLessons = stats.perfectLessons,
                unitsCompleted = if ("unit1" in unlocked) 1 else 0, pacedLessons = stats.pacedLessons,
                reviewCorrect = stats.reviewCorrect, bestLeagueTier = bestTier,
            )
            val fresh = AchievementEngine.newlyUnlocked(input, unlocked)
            stats = stats.copy(gems = stats.gems + fresh.sumOf { it.rewardGems })
            dao.upsertStats(stats.toEntity())
            dao.upsertAchievements(fresh.map { AchievementEntity(it.id, nowMs()) })
        }
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
            league = checkNotNull(dao.latestLeague()).toDomain(),
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
        dao.upsertLeague(LeagueWeekEntity(s.league.weekStart.toEpochDay(), s.league.tier, s.league.userXp, s.league.closed, s.league.finalRank ?: -1, 0))
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
    suspend fun dueIds(today: LocalDate, limit: Int): List<String> = dao.dueIds(today.toEpochDay(), limit)
    suspend fun bankIds(limit: Int): List<String> = dao.bankIds(limit)

    // ---------- corações

    suspend fun loseHeart() = db.withTransaction {
        val stats = checkNotNull(dao.stats()).toDomain()
        dao.upsertStats(stats.copy(hearts = HeartsEngine.lose(stats.hearts, nowMs())).toEntity())
    }

    // ---------- loja, baús e missões

    suspend fun buy(item: ShopItem): PurchaseResult = db.withTransaction {
        val stats = checkNotNull(dao.stats()).toDomain()
        val hearts = HeartsEngine.regen(stats.hearts, nowMs())
        ShopEngine.canBuy(item, stats.gems, hearts.hearts, stats.streak.freezes)?.let { return@withTransaction it }
        val paid = stats.copy(gems = stats.gems - item.priceGems)
        val updated = when (item) {
            ShopItem.StreakFreeze -> paid.copy(streak = StreakEngine.addFreeze(stats.streak))
            ShopItem.RefillHearts -> paid.copy(hearts = HeartsEngine.refill(nowMs()))
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

    /** Apaga todo o progresso (Configurações > Apagar dados). */
    suspend fun eraseAll() = db.clearAllTables()
}
