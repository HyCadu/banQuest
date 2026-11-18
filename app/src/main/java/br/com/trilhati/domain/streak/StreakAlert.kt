package br.com.trilhati.domain.streak

/**
 * Aviso "ofensiva em risco" do sino da Trilha (0.14.1): a ofensiva do aluno e as ofensivas em dupla que acabam hoje se
 * ele não concluir uma lição. Nulo quando não há nada em risco (já estudou hoje, ou não tem ofensiva nenhuma).
 */
object StreakAlert {
    /**
     * @param streak ofensiva atual do aluno (0 = nenhuma).
     * @param studiedToday já concluiu uma lição (ou prova anterior inteira) hoje.
     * @param pairs ofensivas em dupla em risco: apelido do amigo e dias.
     */
    fun text(streak: Int, studiedToday: Boolean, pairs: List<Pair<String, Int>>): String? {
        if (studiedToday) return null
        val pair = pairs.maxByOrNull { it.second }
        val others = pairs.size - 1
        val pairText = pair?.let { (name, days) ->
            "a dupla com $name (${days(days)})" + if (others > 0) " e mais $others" else ""
        }
        return when {
            streak > 0 && pairText != null -> "Sua ofensiva de ${days(streak)} e $pairText acabam hoje se você não fizer uma lição."
            streak > 0 -> "Sua ofensiva de ${days(streak)} acaba hoje se você não fizer uma lição."
            pairText != null -> "A ofensiva em ${pairText.removePrefix("a ")} acaba hoje se você não fizer uma lição."
            else -> null
        }
    }

    private fun days(n: Int) = if (n == 1) "1 dia" else "$n dias"
}
