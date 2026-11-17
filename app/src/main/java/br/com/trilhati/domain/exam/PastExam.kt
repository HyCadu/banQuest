package br.com.trilhati.domain.exam

import br.com.trilhati.domain.model.AnswerKey
import br.com.trilhati.domain.model.Question
import java.text.Normalizer

/** Parte da prova que o aluno escolhe fazer. Só as provas com Específicos e Gerais oferecem as partes. */
enum class ExamPart(val label: String) {
    Full("Prova inteira"),
    Specific("Específicos"),
    General("Gerais"),
}

/**
 * Prova anterior (modo "Provas anteriores" da aba Praticar), montada com as questões do pacote de provas: só as que
 * valem para o edital atual (o `tools/build_provas.py` tira as de versão antiga e as fora do edital), na ordem da prova.
 */
data class PastExam(
    /** Ex.: "fgv-banestes-2018". Vai na rota da tela. */
    val id: String,
    val banca: String,
    val body: String,
    val year: Int,
    val cargo: String,
    val answerKey: AnswerKey,
    val specific: List<Question>,
    val general: List<Question>,
) {
    /** Ex.: "FGV · Banestes 2018". */
    val title: String get() = "$banca · $body $year"

    val size: Int get() = specific.size + general.size

    /** Partes que dá para escolher: a prova inteira e, se ela tiver as duas, só Específicos ou só Gerais. */
    val parts: List<ExamPart>
        get() = if (specific.isNotEmpty() && general.isNotEmpty()) ExamPart.entries.toList() else listOf(ExamPart.Full)

    /** Questões da [part], na ordem da prova. Uma parte que a prova não tem vale a prova inteira. */
    fun questions(part: ExamPart): List<Question> = when {
        part == ExamPart.Specific && specific.isNotEmpty() && general.isNotEmpty() -> specific
        part == ExamPart.General && specific.isNotEmpty() && general.isNotEmpty() -> general
        else -> (specific + general).sortedBy { it.ref?.number ?: Int.MAX_VALUE }
    }

    /** Título da sessão: "FGV · Banestes 2018" ou "FGV · Banestes 2018 · Específicos". */
    fun title(part: ExamPart): String = if (part == ExamPart.Full || parts.size == 1) title else "$title · ${part.label}"
}

object PastExams {
    /**
     * Agrupa as questões de provas anteriores por prova (banca, órgão, ano e cargo), da mais recente para a mais antiga.
     * [isGeneral] diz se a questão é dos Conhecimentos Gerais (unidades G1–G4).
     */
    fun from(questions: List<Question>, isGeneral: (Question) -> Boolean): List<PastExam> {
        val groups = questions.filter { it.ref != null }
            .groupBy { q -> q.ref?.let { Key(it.banca, it.body, it.year, it.cargo) } }
            .mapNotNull { (key, qs) -> key?.let { it to qs.sortedBy { q -> q.ref?.number ?: Int.MAX_VALUE } } }
            .sortedWith(compareByDescending<Pair<Key, List<Question>>> { it.first.year }.thenBy { it.first.banca }.thenBy { it.first.body }.thenBy { it.first.cargo })
        val used = mutableSetOf<String>()
        return groups.map { (key, qs) ->
            val base = slug("${key.banca} ${key.body} ${key.year}")
            val id = generateSequence(1) { it + 1 }.map { n -> if (n == 1) base else "$base-$n" }.first { used.add(it) }
            val (general, specific) = qs.partition(isGeneral)
            PastExam(id, key.banca, key.body, key.year, key.cargo, worstKey(qs), specific, general)
        }
    }

    /** O gabarito menos firme entre as questões (se uma for preliminar, a prova é preliminar). */
    private fun worstKey(questions: List<Question>): AnswerKey =
        questions.mapNotNull { it.ref?.answerKey }.maxByOrNull { it.ordinal } ?: AnswerKey.Final

    /** "FCC Prefeitura de Teresina 2016" → "fcc-prefeitura-de-teresina-2016". */
    fun slug(text: String): String =
        Normalizer.normalize(text, Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "")
            .lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')

    private data class Key(val banca: String, val body: String, val year: Int, val cargo: String)
}

/**
 * Regras da prova anterior (pedido do dono, 0.10.0): abrir gasta um coração inteiro, errar não custa coração, os erros
 * não voltam no fim e o XP só é creditado se o aluno responder todas as questões. No "modo sem vidas", abrir é grátis.
 */
object ExamRules {
    /** Metades de coração que abrir uma prova custa (um coração inteiro). */
    const val OPEN_COST_HALVES = 2

    fun openCostHalves(noLives: Boolean): Int = if (noLives) 0 else OPEN_COST_HALVES

    /** Dá para abrir com [halves] metades de coração? Precisa de um coração inteiro, a não ser no modo sem vidas. */
    fun canOpen(halves: Int, noLives: Boolean): Boolean = halves >= openCostHalves(noLives)

    /** A prova acabou quando todas as questões foram respondidas: só então há XP. */
    fun completed(answered: Int, total: Int): Boolean = total > 0 && answered >= total

    /** Tempo estimado de prova: "4h21", "1h05" ou "52 min" (arredonda para o minuto mais próximo). */
    fun durationLabel(ms: Long): String {
        val minutes = ((ms + 30_000) / 60_000).coerceAtLeast(1)
        return if (minutes >= 60) "${minutes / 60}h${(minutes % 60).toString().padStart(2, '0')}" else "$minutes min"
    }
}
