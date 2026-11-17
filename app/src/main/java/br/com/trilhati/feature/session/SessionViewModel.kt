package br.com.trilhati.feature.session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.trilhati.data.content.ContentRepository
import br.com.trilhati.data.prefs.Settings
import br.com.trilhati.data.prefs.SettingsRepository
import br.com.trilhati.data.repo.ProgressRepository
import br.com.trilhati.domain.exam.ExamPart
import br.com.trilhati.domain.exam.ExamRules
import br.com.trilhati.domain.exam.PastExam
import br.com.trilhati.domain.hearts.HeartsEngine
import br.com.trilhati.domain.lesson.LessonPlanner
import br.com.trilhati.domain.lesson.LessonRun
import br.com.trilhati.domain.lesson.LessonRunState
import br.com.trilhati.domain.model.Question
import br.com.trilhati.domain.model.QuestionLevel
import br.com.trilhati.domain.model.QuestionType
import br.com.trilhati.domain.pace.PaceEngine
import br.com.trilhati.domain.progress.PurchaseResult
import br.com.trilhati.domain.progress.SessionKind
import br.com.trilhati.domain.progress.SessionResult
import br.com.trilhati.domain.progress.ShopItem
import br.com.trilhati.domain.time.millisFlow
import br.com.trilhati.domain.time.today
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Conduz uma sessão de estudo: sorteia as questões, controla o cronômetro de ritmo, os corações, o combo e,
 * ao terminar, grava tudo de uma vez no [ProgressRepository].
 */
@HiltViewModel
class SessionViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val content: ContentRepository,
    private val progress: ProgressRepository,
    private val settingsRepo: SettingsRepository,
    private val clock: Clock,
    private val sounds: br.com.trilhati.sound.SoundPlayer,
    private val notifications: br.com.trilhati.notify.Notifications,
    private val syncScheduler: br.com.trilhati.data.social.SyncScheduler,
    private val focus: br.com.trilhati.notify.ExamFocus,
) : ViewModel() {
    private val source = SessionSource.fromRoute(savedState.get<String>("kind"))
    private val lessonId: String? = savedState.get<String>("id")?.takeIf { it != "-" }

    private val _ui = MutableStateFlow<SessionUi>(SessionUi.Loading)
    val ui: StateFlow<SessionUi> = _ui.asStateFlow()

    private val _character = MutableStateFlow("Guaiamum")

    /** Personagem escolhido no perfil, para reagir na tela (feliz, triste, comemorando). */
    val character: StateFlow<String> = _character.asStateFlow()

    /** Relógio de 250 ms que alimenta só o anel do cronômetro. */
    val now: Flow<Long> = clock.millisFlow(250)

    private lateinit var settings: Settings
    private var title = ""
    private var run: LessonRunState? = null

    /** Item na tela agora. Fica guardado à parte porque, na última questão, a fila já esvaziou ao mostrar o feedback. */
    private var shown: br.com.trilhati.domain.lesson.LessonItem? = null
    private var exercise: ExerciseState = ExerciseState.Choice()
    private var phase: Phase = Phase.Answering
    private var startedAtMs = 0L
    /** Corações em metades (null quando a sessão não usa corações). */
    private var heartHalves: Int? = null
    private var helps = 0
    /** Primeiro exercício de cada conceito ainda não visto, nesta sessão: ali a dica abre sozinha. */
    private var introIds: Set<String> = emptySet()
    private var outOfHearts = false
    private var finishing = false

    // Prova anterior (aba Praticar): escolhida na tela de aviso; nada é gasto até "Começar".
    private var exam: PastExam? = null
    private var examPart = ExamPart.Full
    private var examSilence: Boolean? = null
    private var examHearts = 0
    private var examHeartSpent = false
    private var beginning = false

    /** O treino dá XP e coração? Só o de erros vencidos (0.10.1); o treino livre e a revisão livre são só estudo. */
    private var rewarded = true

    init {
        viewModelScope.launch { start() }
    }

    private suspend fun start() {
        _ui.value = SessionUi.Loading
        rewarded = true
        settings = settingsRepo.settings.first()
        val today = clock.today()
        progress.ensureReady(today)
        val snap = progress.snapshot(today).first()
        _character.value = snap.profile.character
        val now = clock.millis()
        val heartsNow = HeartsEngine.regen(snap.stats.hearts, now)
        if (source == SessionSource.Exam) {
            examHearts = heartsNow.halves
            showExamIntro()
            return
        }
        val usesHearts = source == SessionSource.Lesson && !settings.noLives
        if (usesHearts && heartsNow.halves == 0) {
            _ui.value = SessionUi.NoHearts(snap.stats.gems, HeartsEngine.msUntilNext(snap.stats.hearts, now), endedMidLesson = false, dueErrors = snap.dueCount)
            return
        }
        heartHalves = if (usesHearts) heartsNow.halves else null
        helps = snap.stats.help.count
        val random = Random(now)
        val questions: List<Question>
        when (source) {
            SessionSource.Lesson -> {
                val lesson = lessonId?.let(content::lesson)
                if (lesson == null || !lesson.hasContent) {
                    _ui.value = SessionUi.Empty("Em breve", "Esta lição ainda não tem questões. Elas chegam nos próximos pacotes de conteúdo.")
                    return
                }
                title = lesson.title
                val answered = progress.answeredIds(lesson.id)
                questions = LessonPlanner.plan(lesson.questions, progress.missedIds(lesson.id), random, lesson.concepts.map { it.id }, answered)
                val seenConcepts = lesson.questions.filter { it.id in answered }.mapNotNull { it.conceptId }.toSet()
                introIds = questions.filter { it.conceptId != null && it.conceptId !in seenConcepts }
                    .groupBy { it.conceptId }.values.map { it.first().id }.toSet()
            }
            SessionSource.Errors -> {
                // Com erros vencidos, é a revisão espaçada (dá XP e coração). Sem nenhum vencido, os mais antigos viram
                // treino livre: dá para estudar, mas sem recompensa (senão dava para repetir sem fim).
                val due = progress.dueIds(today, LessonRun.SIZE)
                rewarded = due.isNotEmpty()
                title = if (rewarded) "Praticar erros" else "Treino livre"
                questions = content.questions(due.ifEmpty { progress.bankIds(LessonRun.SIZE) }).shuffled(random)
            }
            SessionSource.Review -> {
                rewarded = false
                title = "Revisão livre"
                val done = snap.lessonProgress.values.filter { it.completed }.map { it.lessonId }.toSet()
                val pool = done.mapNotNull(content::lesson).flatMap { it.questions }
                questions = pool.shuffled(random).take(LessonRun.SIZE)
            }
            SessionSource.Exam -> return
        }
        if (questions.isEmpty()) {
            _ui.value = SessionUi.Empty(
                "Nada para praticar",
                if (source == SessionSource.Errors) "Você não tem erros guardados. Faça uma lição e eu guardo o que você errar!" else "Conclua uma lição para liberar a revisão livre.",
            )
            return
        }
        run = LessonRun.start(questions, awardsXp = rewarded)
        enterQuestion()
    }

    private fun enterQuestion() {
        val r = checkNotNull(run)
        val item = r.current ?: return
        shown = item
        exercise = initialExercise(item.question, item.retry)
        phase = Phase.Answering
        startedAtMs = clock.millis()
        publish()
    }

    private fun initialExercise(q: Question, retry: Boolean): ExerciseState = when (q.type) {
        QuestionType.Match -> {
            val seed = q.id.hashCode() + if (retry) 1 else 0
            var order = q.pairs.indices.toList().shuffled(Random(seed))
            if (order == q.pairs.indices.toList() && order.size > 1) order = order.drop(1) + order.first()
            ExerciseState.Match(order)
        }
        QuestionType.OrderCode -> {
            val seed = q.id.hashCode() + if (retry) 1 else 0
            var pool = q.lines.indices.toList().shuffled(Random(seed))
            if (pool == q.lines.indices.toList() && pool.size > 1) pool = pool.drop(1) + pool.first()
            ExerciseState.Order(pool)
        }
        else -> ExerciseState.Choice()
    }

    private fun publish() {
        val r = checkNotNull(run)
        val item = shown ?: return
        val pace = settings.paceConfig
        _ui.value = SessionUi.Playing(
            title = title, question = item.question, exercise = exercise, phase = phase,
            progress = r.progress, combo = r.combo, hearts = heartHalves?.div(2), retry = item.retry, pace = pace,
            targetMs = pace.targetMs(item.question), startedAtMs = startedAtMs, answered = r.records.size,
            slackMs = PaceEngine.slackMs(r.targetsSumMs, r.totalTimeMs), haptics = settings.haptics,
            remaining = if (phase == Phase.Answering) r.queue.size else r.queue.size + 1,
            // Durante a resposta a atual ainda está na fila; no feedback ela já saiu, então soma de novo.
            remainingBudgetMs = r.queue.sumOf { pace.targetMs(it.question) } +
                if (phase == Phase.Answering) 0 else pace.targetMs(item.question),
            concept = item.question.conceptId?.let { content.lesson(item.question.lessonId)?.concept(it) },
            newConcept = !item.retry && item.question.id in introIds,
            halfHeart = heartHalves?.let { it % 2 == 1 } ?: false,
            helps = helps,
            exam = source == SessionSource.Exam,
            examHeartSpent = examHeartSpent,
        )
    }

    // ---------- prova anterior: tela de aviso

    private fun showExamIntro() {
        val found = exam ?: lessonId?.let { content.exam(it, settings.targetExam) }
        if (found == null) {
            _ui.value = SessionUi.Empty("Prova não encontrada", "Ela não está mais no app. Volte e escolha outra prova.")
            return
        }
        exam = found
        if (examPart !in found.parts) examPart = ExamPart.Full
        val silence = examSilence ?: settings.examSilence
        examSilence = silence
        val questions = found.questions(examPart)
        _ui.value = SessionUi.ExamIntro(
            title = found.title, cargo = found.cargo, answerKey = found.answerKey, parts = found.parts, part = examPart,
            partSizes = found.parts.map { found.questions(it).size }, questionCount = questions.size,
            estimatedMs = questions.sumOf { settings.paceConfig.targetMs(it) },
            costHalves = ExamRules.openCostHalves(settings.noLives), canOpen = ExamRules.canOpen(examHearts, settings.noLives),
            silence = silence, silenceAllowed = focus.hasAccess(),
        )
    }

    fun pickExamPart(part: ExamPart) {
        if (_ui.value !is SessionUi.ExamIntro) return
        examPart = part
        showExamIntro()
    }

    /** Interruptor "Silenciar notificações durante a prova" (fica lembrado para as próximas provas). */
    fun setExamSilence(on: Boolean) {
        if (_ui.value !is SessionUi.ExamIntro) return
        examSilence = on
        viewModelScope.launch { settingsRepo.setExamSilence(on) }
        showExamIntro()
    }

    /** Tela do Android onde se libera o acesso ao Não perturbe. */
    fun silenceAccessIntent(): android.content.Intent = focus.accessIntent()

    /** Ao voltar das configurações do Android: confere de novo se o acesso ao Não perturbe foi liberado. */
    fun refreshExamSilenceAccess() {
        if (_ui.value is SessionUi.ExamIntro) showExamIntro()
    }

    /** "Começar": gasta o coração (a não ser no modo sem vidas), liga o Não perturbe se pedido e abre a prova. */
    fun beginExam() {
        val found = exam ?: return
        val intro = _ui.value as? SessionUi.ExamIntro ?: return
        if (beginning) return
        beginning = true
        _ui.value = intro.copy(starting = true)
        viewModelScope.launch {
            val snap = progress.snapshot(clock.today()).first()
            val now = clock.millis()
            val halves = HeartsEngine.regen(snap.stats.hearts, now).halves
            if (!ExamRules.canOpen(halves, settings.noLives)) {
                beginning = false
                _ui.value = SessionUi.NoHearts(snap.stats.gems, HeartsEngine.msUntilNext(snap.stats.hearts, now), endedMidLesson = false, forExam = true, dueErrors = snap.dueCount)
                return@launch
            }
            val cost = ExamRules.openCostHalves(settings.noLives)
            if (cost > 0) progress.loseHeart(cost)
            examHeartSpent = cost > 0
            if (examSilence == true) focus.silence()
            title = found.title(examPart)
            run = LessonRun.start(found.questions(examPart), repeatMistakes = false)
            enterQuestion()
        }
    }

    private fun sfx(sound: br.com.trilhati.sound.Sfx) = sounds.play(sound, settings.sounds)

    // ---------- interação

    fun select(option: Int) {
        val ex = exercise as? ExerciseState.Choice ?: return
        if (phase != Phase.Answering) return
        if (ex.selected != option) sfx(br.com.trilhati.sound.Sfx.Click)
        exercise = ex.copy(selected = option)
        publish()
    }

    /** Botão "Verificar" para múltipla escolha e ordenação. */
    fun check() {
        if (phase != Phase.Answering) return
        val q = checkNotNull(shown).question
        when (val ex = exercise) {
            is ExerciseState.Choice -> ex.selected?.let { submit(it == q.answer) }
            is ExerciseState.Order -> if (ex.pool.isEmpty()) submit(ex.placed == q.lines.indices.toList())
            is ExerciseState.Match -> Unit
        }
    }

    fun matchLeft(index: Int) {
        val ex = exercise as? ExerciseState.Match ?: return
        if (phase != Phase.Answering || index in ex.solved) return
        exercise = ex.copy(selectedLeft = if (ex.selectedLeft == index) null else index, wrongPair = null)
        publish()
    }

    fun matchRight(rightPosition: Int) {
        val ex = exercise as? ExerciseState.Match ?: return
        val left = ex.selectedLeft ?: return
        val pairIndex = ex.rightOrder[rightPosition]
        if (phase != Phase.Answering || pairIndex in ex.solved) return
        if (pairIndex == left) {
            val solved = ex.solved + left
            exercise = ex.copy(solved = solved, selectedLeft = null, wrongPair = null)
            val q = checkNotNull(shown).question
            // Errou algum par no caminho: vale como "corrigida" (sem coração, sem repetir, 5 XP, vai para a revisão).
            if (solved.size == q.pairs.size) submit(ex.mistakes == 0, slips = ex.mistakes) else publish()
        } else {
            exercise = ex.copy(selectedLeft = null, wrongPair = left to pairIndex, mistakes = ex.mistakes + 1)
            publish()
            viewModelScope.launch {
                delay(600)
                (exercise as? ExerciseState.Match)?.let { exercise = it.copy(wrongPair = null); if (phase == Phase.Answering) publish() }
            }
        }
    }

    fun orderPlace(lineIndex: Int) {
        val ex = exercise as? ExerciseState.Order ?: return
        if (phase != Phase.Answering || lineIndex !in ex.pool) return
        exercise = ex.copy(pool = ex.pool - lineIndex, placed = ex.placed + lineIndex)
        publish()
    }

    fun orderRemove(lineIndex: Int) {
        val ex = exercise as? ExerciseState.Order ?: return
        if (phase != Phase.Answering || lineIndex !in ex.placed) return
        exercise = ex.copy(pool = ex.pool + lineIndex, placed = ex.placed - lineIndex)
        publish()
    }

    /**
     * Botão "Não sei" (só nos exercícios de ensino): gasta um uso, mostra a resposta explicada sem custar coração e o
     * exercício volta no fim da lição (como um erro, sem XP).
     */
    fun dontKnow() {
        if (phase != Phase.Answering) return
        val q = shown?.question ?: return
        if (q.level == QuestionLevel.Exam || helps <= 0) return
        helps -= 1
        viewModelScope.launch { progress.useHelp() }
        submit(correct = false, skipped = true)
    }

    private fun submit(correct: Boolean, slips: Int = 0, skipped: Boolean = false) {
        val r = checkNotNull(run)
        val item = checkNotNull(shown)
        val elapsed = (clock.millis() - startedAtMs).coerceAtLeast(0)
        val target = settings.paceConfig.targetMs(item.question)
        val corrected = !correct && slips > 0
        val next = LessonRun.answer(r, correct, elapsed, target, corrected)
        run = next
        val record = next.records.last()
        // Erro em questão de prova custa um coração; em exercício de ensino (aprender e praticar), meio.
        val cost = if (item.question.level == QuestionLevel.Exam) 2 else 1
        val halvesNow = heartHalves
        if (halvesNow != null && !correct && !corrected && !skipped && !item.retry) {
            val left = (halvesNow - cost).coerceAtLeast(0)
            heartHalves = left
            viewModelScope.launch { progress.loseHeart(cost) }
            if (left == 0) outOfHearts = true
        }
        phase = Phase.Feedback(correct, if (correct || corrected) null else correctText(item.question), record.xp, slips, skipped)
        sfx(
            when {
                skipped -> br.com.trilhati.sound.Sfx.Click
                corrected -> br.com.trilhati.sound.Sfx.Correct
                !correct -> br.com.trilhati.sound.Sfx.Wrong
                next.combo == 3 || (next.combo > 3 && next.combo % 5 == 0) -> br.com.trilhati.sound.Sfx.Combo
                else -> br.com.trilhati.sound.Sfx.Correct
            },
        )
        publish()
    }

    private fun correctText(q: Question): String? = when (q.type) {
        QuestionType.Match -> q.pairs.joinToString("\n") { "${it.left} → ${it.right}" }
        QuestionType.OrderCode -> q.lines.joinToString("\n")
        else -> q.options.getOrNull(q.answer)
    }

    fun continueAfterFeedback() {
        val r = run ?: return
        when {
            outOfHearts -> viewModelScope.launch {
                val snap = progress.snapshot(clock.today()).first()
                _ui.value = SessionUi.NoHearts(snap.stats.gems, HeartsEngine.msUntilNext(snap.stats.hearts, clock.millis()), endedMidLesson = true, dueErrors = snap.dueCount)
            }
            r.finished -> finish()
            else -> enterQuestion()
        }
    }

    private fun finish() {
        if (finishing) return
        finishing = true
        val r = checkNotNull(run)
        _ui.value = SessionUi.Saving
        // A prova acabou: desliga o Não perturbe antes de mostrar o resultado.
        if (source == SessionSource.Exam) focus.restore()
        viewModelScope.launch {
            val kind = when (source) {
                SessionSource.Lesson -> SessionKind.Lesson
                SessionSource.Exam -> SessionKind.Exam
                SessionSource.Errors, SessionSource.Review -> if (rewarded) SessionKind.Practice else SessionKind.FreeReview
            }
            val lessonOnly = lessonId.takeIf { source == SessionSource.Lesson }
            val unitId = lessonOnly?.let { content.lesson(it)?.unitId }
            // Só chega aqui com todas as questões respondidas: na prova anterior, é o único caminho que dá XP.
            val result = SessionResult(kind, lessonOnly, unitId, r.records, r.bestCombo, r.perfect)
            val processed = progress.applySession(result, clock.today(), content.allCourses)
            if (kind != SessionKind.Practice) notifications.clear()
            // Manda a sessão para o ranking assim que houver rede (fica na fila se estiver offline).
            syncScheduler.syncSoon()
            sfx(if (processed.summary.newAchievements.isNotEmpty()) br.com.trilhati.sound.Sfx.Achievement else br.com.trilhati.sound.Sfx.Complete)
            _ui.value = SessionUi.Finished(title, source, processed, r, settings.paceConfig, rewarded = rewarded)
        }
    }

    /** Recarrega corações com gemas a partir da tela "sem corações" e reinicia a sessão. */
    fun refillHeartsAndRetry() {
        viewModelScope.launch {
            if (progress.buy(ShopItem.RefillHearts) is PurchaseResult.Success) {
                outOfHearts = false
                finishing = false
                start()
            }
        }
    }

    fun retryAfterWaiting() {
        viewModelScope.launch {
            outOfHearts = false
            start()
        }
    }

    /** Saiu da tela (terminou ou abandonou a prova): o Não perturbe ligado pela prova não pode ficar ligado. */
    override fun onCleared() {
        focus.restore()
    }
}
