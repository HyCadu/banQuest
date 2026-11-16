package br.com.trilhati.feature.session

import androidx.compose.runtime.Immutable
import br.com.trilhati.domain.lesson.LessonRunState
import br.com.trilhati.domain.model.Question
import br.com.trilhati.domain.pace.PaceConfig
import br.com.trilhati.domain.progress.ProcessedSession

/** De onde vem a sessão: uma lição da trilha, treino de erros (revisão espaçada) ou revisão livre. */
enum class SessionSource(val route: String) {
    Lesson("lesson"),
    Errors("errors"),
    Review("review"),
    ;

    companion object {
        fun fromRoute(route: String?): SessionSource = entries.firstOrNull { it.route == route } ?: Lesson
    }
}

/** Estado de interação do exercício atual. */
sealed interface ExerciseState {
    /** Múltipla escolha, caso prático, saída de código, lacuna e certo/errado. */
    data class Choice(val selected: Int? = null) : ExerciseState

    /**
     * Associação de pares. [rightOrder] é a ordem (embaralhada) em que as definições aparecem: cada item é o
     * índice do par original. [solved] guarda os pares já acertados.
     */
    data class Match(
        val rightOrder: List<Int>,
        val solved: Set<Int> = emptySet(),
        val selectedLeft: Int? = null,
        val wrongPair: Pair<Int, Int>? = null,
        val mistakes: Int = 0,
    ) : ExerciseState

    /** Ordenar linhas: [pool] são os índices (embaralhados) ainda não colocados; [placed] a sequência escolhida. */
    data class Order(val pool: List<Int>, val placed: List<Int> = emptyList()) : ExerciseState
}

sealed interface Phase {
    data object Answering : Phase

    /**
     * Depois de verificar: [correct], o texto da resposta certa (para erros) e o XP ganho.
     * [slips] > 0 com [correct] = false: completou corrigindo (associar pares), sem custo de coração.
     * [skipped]: o aluno usou o "Não sei" (sem custo de coração; o exercício volta no fim).
     */
    data class Feedback(val correct: Boolean, val correctText: String?, val xp: Int, val slips: Int = 0, val skipped: Boolean = false) : Phase {
        val corrected: Boolean get() = !correct && slips > 0
    }
}

@Immutable
sealed interface SessionUi {
    data object Loading : SessionUi

    data class Empty(val title: String, val message: String) : SessionUi

    /** Sem corações: não dá para começar (ou continuar) uma lição. */
    data class NoHearts(val gems: Int, val msUntilNext: Long?, val endedMidLesson: Boolean) : SessionUi

    data class Playing(
        val title: String,
        val question: Question,
        val exercise: ExerciseState,
        val phase: Phase,
        val progress: Float,
        val combo: Int,
        val hearts: Int?,
        val retry: Boolean,
        val pace: PaceConfig,
        val targetMs: Long,
        val startedAtMs: Long,
        val answered: Int,
        val slackMs: Long,
        val haptics: Boolean,
        /** Questões que ainda faltam, contando a atual (para o "tempo restante da lição"). */
        val remaining: Int = 1,
        /** Soma dos tempos-alvo das questões que faltam, contando a atual (cada nível tem o seu alvo). */
        val remainingBudgetMs: Long = remaining * targetMs,
        /** Conceito da questão atual, para a dica nos exercícios de aprender. */
        val concept: br.com.trilhati.domain.model.Concept? = null,
        /** Primeiro exercício de um conceito que o aluno ainda não viu: a dica já vem aberta, como "Novo conceito". */
        val newConcept: Boolean = false,
        /** Meio coração além de [hearts]: o coração do contador aparece rachado. */
        val halfHeart: Boolean = false,
        /** Estoque do "Não sei"; o botão só aparece nos exercícios de ensino. */
        val helps: Int = 0,
    ) : SessionUi {
        /** Texto ao lado do coração: "5" ou "4,5" (ver [br.com.trilhati.domain.hearts.HeartsEngine.display]). */
        val heartsText: String? get() = hearts?.let { if (halfHeart) "$it,5" else "$it" }
        val canAskHelp: Boolean get() = phase == Phase.Answering && question.level != br.com.trilhati.domain.model.QuestionLevel.Exam
    }

    data object Saving : SessionUi

    data class Finished(
        val title: String,
        val source: SessionSource,
        val processed: ProcessedSession,
        val run: LessonRunState,
        val pace: PaceConfig,
    ) : SessionUi
}
