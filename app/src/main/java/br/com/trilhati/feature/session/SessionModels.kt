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

    /** Depois de verificar: [correct], o texto da resposta certa (para erros) e o XP ganho. */
    data class Feedback(val correct: Boolean, val correctText: String?, val xp: Int) : Phase
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
    ) : SessionUi

    data object Saving : SessionUi

    data class Finished(
        val title: String,
        val source: SessionSource,
        val processed: ProcessedSession,
        val run: LessonRunState,
        val pace: PaceConfig,
    ) : SessionUi
}
