package br.com.trilhati.domain.lesson

import br.com.trilhati.domain.model.Question
import kotlin.random.Random

/** Um item da fila: a questão e se é uma repetição de erro. */
data class LessonItem(val question: Question, val retry: Boolean)

/** Registro de uma resposta dada durante a sessão. */
data class AnswerRecord(
    val question: Question,
    val correct: Boolean,
    val retry: Boolean,
    val timeMs: Long,
    val targetMs: Long,
    val xp: Int,
)

/** Estado imutável de uma sessão (lição, treino de erros ou revisão livre). */
data class LessonRunState(
    val queue: List<LessonItem>,
    val planned: Int,
    val completed: Int = 0,
    val combo: Int = 0,
    val bestCombo: Int = 0,
    val xp: Int = 0,
    val records: List<AnswerRecord> = emptyList(),
) {
    val current: LessonItem? get() = queue.firstOrNull()
    val finished: Boolean get() = queue.isEmpty()
    val progress: Float get() = if (planned == 0) 1f else completed.toFloat() / planned
    val firstTryRecords: List<AnswerRecord> get() = records.filter { !it.retry }
    val mistakes: Int get() = firstTryRecords.count { !it.correct }
    val perfect: Boolean get() = finished && mistakes == 0
    val totalTimeMs: Long get() = records.sumOf { it.timeMs }
    val targetsSumMs: Long get() = records.sumOf { it.targetMs }
    val correctCount: Int get() = records.count { it.correct }
    val accuracyPercent: Int get() = firstTryRecords.let { r -> if (r.isEmpty()) 0 else r.count { it.correct } * 100 / r.size }
}

/**
 * Sessão de lição (PLAN.md §5.1 e §7): sorteia 8 das 10 questões do banco priorizando as que o aluno errou;
 * cada erro volta uma vez no fim da fila (valendo 5 XP em vez de 10); combo conta acertos seguidos.
 */
object LessonRun {
    const val SIZE = 8

    /**
     * Escolhe [size] questões do [pool]. As de [priorityIds] (erros anteriores) entram primeiro; o resto é sorteado.
     * A ordem final é embaralhada, mas determinística para um mesmo [random].
     */
    fun pick(pool: List<Question>, priorityIds: Set<String>, random: Random, size: Int = SIZE): List<Question> {
        val (priority, others) = pool.partition { it.id in priorityIds }
        val chosen = priority.shuffled(random).take(size).toMutableList()
        if (chosen.size < size) chosen += others.shuffled(random).take(size - chosen.size)
        return chosen.shuffled(random)
    }

    fun start(questions: List<Question>): LessonRunState =
        LessonRunState(queue = questions.map { LessonItem(it, retry = false) }, planned = questions.size)

    /**
     * Registra a resposta da questão atual. Erro na primeira vez recoloca a questão no fim da fila (uma vez só).
     * [targetMs] é o tempo-alvo daquela questão, usado para calcular folga/atraso no resumo.
     */
    fun answer(state: LessonRunState, correct: Boolean, timeMs: Long, targetMs: Long): LessonRunState {
        val item = state.current ?: return state
        val xp = XpEngine.answerXp(correct, item.retry, timeMs)
        val record = AnswerRecord(item.question, correct, item.retry, timeMs, targetMs, xp)
        val rest = state.queue.drop(1)
        val requeue = !correct && !item.retry
        val combo = if (correct) state.combo + 1 else 0
        return state.copy(
            queue = if (requeue) rest + LessonItem(item.question, retry = true) else rest,
            planned = if (requeue) state.planned + 1 else state.planned,
            completed = state.completed + 1,
            combo = combo,
            bestCombo = maxOf(state.bestCombo, combo),
            xp = state.xp + xp,
            records = state.records + record,
        )
    }
}
