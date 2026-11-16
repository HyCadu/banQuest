package br.com.trilhati.domain.lesson

import br.com.trilhati.domain.model.Question
import br.com.trilhati.domain.model.QuestionLevel
import kotlin.random.Random

/**
 * Monta a sessão de uma lição no formato por conceitos (DECISIONS.md, 0.7.1): aprender fazendo, um conceito por vez.
 *
 * - Escolhe [CONCEPTS_PER_SESSION] conceitos **na ordem da lição**: primeiro os que o aluno ainda não viu; depois os dos
 *   erros anteriores; depois os demais, sorteados (revisão).
 * - Cada conceito vira um **bloco** de [STEPS_PER_CONCEPT] exercícios de ensino em sequência (passo 1 = exemplo
 *   resolvido, 2 = aplicar no mesmo cenário, 3 = variar). Num conceito já visto, os erros dele entram primeiro.
 * - Fecha com [EXAM] questões de prova dos conceitos da sessão, evitando dois tipos iguais seguidos.
 * - Exercício adaptado de uma questão oficial nunca cai na mesma sessão que ela: a oficial fica de fora da prova.
 * - Se faltar exercício de ensino, completa com outros dos mesmos conceitos; prova só de conceito já ensinado.
 *
 * Lições no formato antigo (sem conceitos) continuam com o sorteio de [LessonRun.pick] (8 questões).
 */
object LessonPlanner {
    const val CONCEPTS_PER_SESSION = 3
    const val STEPS_PER_CONCEPT = 3
    const val EXAM = 3
    const val SIZE = CONCEPTS_PER_SESSION * STEPS_PER_CONCEPT + EXAM

    /** A lição usa o formato por conceitos quando tem exercícios de ensino ligados a conceitos. */
    fun usesConcepts(pool: List<Question>): Boolean = pool.any { it.conceptId != null && it.level != QuestionLevel.Exam }

    /**
     * [conceptOrder]: conceitos na ordem da lição (vazio = ordem em que aparecem no banco).
     * [seenIds]: exercícios desta lição que o aluno já respondeu alguma vez.
     */
    fun plan(
        pool: List<Question>,
        priorityIds: Set<String>,
        random: Random,
        conceptOrder: List<String> = emptyList(),
        seenIds: Set<String> = emptySet(),
    ): List<Question> {
        if (!usesConcepts(pool)) return LessonRun.pick(pool, priorityIds, random)
        val inPool = pool.mapNotNull { it.conceptId }.distinct()
        val order = (conceptOrder.filter { it in inPool } + inPool).distinct()
        val concepts = chooseConcepts(pool, order, priorityIds, seenIds, random)
        val seenConcepts = conceptsOf(pool, seenIds)

        val blocks = concepts.map { c -> block(pool, c, isNew = c !in seenConcepts, priorityIds, random) }
        val teaching = blocks.flatten().toMutableList()
        val exam = exam(pool, concepts, seenConcepts, priorityIds, teaching, random).toMutableList()

        // Faltou exercício: completa com mais ensino dos mesmos conceitos (nunca com prova de conceito não ensinado).
        if (teaching.size + exam.size < SIZE) {
            val extra = pool.filter { q ->
                q.level != QuestionLevel.Exam && q.conceptId in concepts && q !in teaching && exam.none { it.id == q.adaptedFrom?.questionId }
            }.shuffled(random).sortedBy { it.step ?: Int.MAX_VALUE }
            for (q in extra) if (teaching.size + exam.size < SIZE) teaching.add(insertionIndex(teaching, q, concepts), q)
        }
        return teaching + spreadTypes(exam, teaching.lastOrNull())
    }

    private fun conceptsOf(pool: List<Question>, ids: Set<String>): Set<String> =
        pool.filter { it.id in ids }.mapNotNull { it.conceptId }.toSet()

    /** Conceitos da sessão, devolvidos na ordem da lição. */
    internal fun chooseConcepts(
        pool: List<Question>,
        order: List<String>,
        priorityIds: Set<String>,
        seenIds: Set<String>,
        random: Random,
    ): List<String> {
        val seen = conceptsOf(pool, seenIds)
        val missed = conceptsOf(pool, priorityIds)
        val unseen = order.filter { it !in seen }
        val review = order.filter { it in missed && it !in unseen }
        val rest = order.filter { it !in unseen && it !in review }.shuffled(random)
        return (unseen + review + rest).take(CONCEPTS_PER_SESSION).sortedBy { order.indexOf(it) }
    }

    /**
     * Bloco de um conceito, em ordem de passo. Conceito novo: um exercício de cada passo, começando pelo 1. Conceito já
     * visto: os erros primeiro, depois os passos mais avançados (o exemplo resolvido fica por último).
     */
    private fun block(pool: List<Question>, concept: String, isNew: Boolean, priorityIds: Set<String>, random: Random): List<Question> {
        val items = pool.filter { it.conceptId == concept && it.level != QuestionLevel.Exam }.shuffled(random)
        val byStep = items.groupBy { it.step ?: Int.MAX_VALUE }.toSortedMap()
        val picked = mutableListOf<Question>()
        if (isNew) {
            for ((_, variants) in byStep) {
                if (picked.size >= STEPS_PER_CONCEPT) break
                picked += variants.sortedBy { if (it.id in priorityIds) 0 else 1 }.first()
            }
        } else {
            val missed = items.filter { it.id in priorityIds }
            val others = items.filter { it !in missed }.sortedByDescending { it.step ?: 0 }
            // Uma variante por passo, quando der.
            for (q in missed + others) {
                if (picked.size >= STEPS_PER_CONCEPT) break
                if (picked.none { it.step == q.step }) picked += q
            }
            for (q in missed + others) if (picked.size < STEPS_PER_CONCEPT && q !in picked) picked += q
        }
        // Completa se o conceito tiver poucos passos.
        for (q in items) if (picked.size < STEPS_PER_CONCEPT && q !in picked) picked += q
        return picked.sortedWith(compareBy<Question>({ it.step ?: Int.MAX_VALUE }, { it.level.ordinal }))
    }

    /**
     * Uma questão de prova por conceito da sessão (erros primeiro), sem cair junto de uma adaptada dela já escolhida.
     * Se faltar, completa com conceitos que o aluno já viu antes; nunca com um conceito que ainda não foi ensinado.
     */
    private fun exam(
        pool: List<Question>,
        concepts: List<String>,
        seenConcepts: Set<String>,
        priorityIds: Set<String>,
        teaching: List<Question>,
        random: Random,
    ): List<Question> {
        val blocked = teaching.mapNotNull { it.adaptedFrom?.questionId }.toSet()
        val candidates = pool.filter { it.level == QuestionLevel.Exam && it.id !in blocked }
            .shuffled(random).sortedBy { if (it.id in priorityIds) 0 else 1 }
        val picked = mutableListOf<Question>()
        for (c in concepts) candidates.firstOrNull { it.conceptId == c && it !in picked }?.let { if (picked.size < EXAM) picked += it }
        for (q in candidates) if (picked.size < EXAM && q !in picked && q.conceptId in concepts) picked += q
        for (q in candidates) if (picked.size < EXAM && q !in picked && q.conceptId in seenConcepts) picked += q
        return picked
    }

    /** Onde encaixar um exercício extra: no fim do bloco do conceito dele. */
    private fun insertionIndex(teaching: List<Question>, q: Question, concepts: List<String>): Int {
        val last = teaching.indexOfLast { it.conceptId == q.conceptId }
        if (last >= 0) return last + 1
        val after = concepts.indexOf(q.conceptId)
        val next = teaching.indexOfFirst { concepts.indexOf(it.conceptId) > after }
        return if (next < 0) teaching.size else next
    }

    /**
     * Reordena para não ter dois exercícios seguidos do mesmo tipo, quando der: a cada passo, entre os de tipo diferente
     * do anterior, vai o do tipo que mais sobra (assim o tipo repetido não fica todo para o fim). Empate: o que veio antes.
     */
    internal fun spreadTypes(items: List<Question>, before: Question?): List<Question> {
        val rest = items.toMutableList()
        val out = mutableListOf<Question>()
        var last = before?.type
        while (rest.isNotEmpty()) {
            val counts = rest.groupingBy { it.type }.eachCount()
            val next = rest.filter { it.type != last }.ifEmpty { rest }.maxBy { counts.getValue(it.type) }
            rest.remove(next)
            out += next
            last = next.type
        }
        return out
    }
}
