package br.com.trilhati.domain.model

/** Tipos de exercício (PLAN.md §5.1). */
enum class QuestionType { Mcq5, TrueFalse, Match, OrderCode, FillBlank, CodeOutput, Case }

/** Estilo da questão, usado para calibrar a mistura estilo FCC. */
enum class QuestionStyle { Conceitual, Literal, CasoPratico, Excecao }

/** De onde veio a questão. Só `FccReal` pode ser chamada de "da FCC" (CLAUDE.md, regra 4). */
enum class QuestionSource { Autoral, Ia, FccReal }

data class MatchPair(val left: String, val right: String)

data class Question(
    val id: String,
    val lessonId: String,
    val type: QuestionType,
    val topic: String,
    val subtopic: String,
    val difficulty: Int,
    val style: QuestionStyle,
    val stem: String,
    val code: String?,
    /** Alternativas (mcq5, case, code_output, fill_blank e truefalse). */
    val options: List<String>,
    /** Índice da alternativa correta em [options]. */
    val answer: Int,
    /** Pares termo↔definição (match). */
    val pairs: List<MatchPair>,
    /** Linhas na ordem correta (order_code). O app embaralha na hora de exibir. */
    val lines: List<String>,
    val explanation: String,
    val source: QuestionSource,
    val reviewed: Boolean,
) {
    /** Casos práticos ganham mais tempo no cronômetro, se o aluno ativar a opção. */
    val isCase: Boolean get() = type == QuestionType.Case || style == QuestionStyle.CasoPratico

    /** Toda questão que não foi conferida pelo dono mostra o selo "não revisada". */
    val showUnreviewedBadge: Boolean get() = !reviewed
}

data class Lesson(
    val id: String,
    val unitId: String,
    val order: Int,
    val title: String,
    val questions: List<Question>,
) {
    /** Lição sem questões ainda ("conteúdo chegando"). */
    val hasContent: Boolean get() = questions.isNotEmpty()
}

/** Unidade da trilha. Chamada `CourseUnit` para não colidir com `kotlin.Unit`. */
data class CourseUnit(
    val id: String,
    val order: Int,
    val title: String,
    val editalTopics: List<String>,
    val lessons: List<Lesson>,
) {
    val hasContent: Boolean get() = lessons.any { it.hasContent }
}
