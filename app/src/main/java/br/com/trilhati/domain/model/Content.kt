package br.com.trilhati.domain.model

/** Tipos de exercício (PLAN.md §5.1). `Quick` é a escolha rápida: 3 alternativas curtas, para aprender e praticar. */
enum class QuestionType { Mcq5, TrueFalse, Match, OrderCode, FillBlank, CodeOutput, Case, Quick }

/**
 * Nível do exercício dentro da lição (formato por conceitos): aprender (certo/errado, escolha rápida), praticar
 * (associar, completar, interpretar) e prova (questões oficiais e no estilo FCC). Sem nível no JSON vale `Exam`.
 */
enum class QuestionLevel { Learn, Practice, Exam }

/** Ideia pequena que a lição ensina (ex.: "Entidade fraca"). O resumo aparece como dica nos exercícios de aprender. */
data class Concept(val id: String, val title: String, val summary: String)

/** Estilo da questão, usado para calibrar a mistura estilo FCC. */
enum class QuestionStyle { Conceitual, Literal, CasoPratico, Excecao }

/**
 * De onde veio a questão. `ProvaReal` é questão de prova anterior fornecida pelo dono, com a banca em [ExamRef]:
 * só ela (com banca FCC) ou `FccReal` pode ser chamada de "da FCC" (CLAUDE.md, regra 4).
 */
enum class QuestionSource { Autoral, Ia, FccReal, ProvaReal }

/** Situação do gabarito usado na questão de prova. */
enum class AnswerKey { Final, Preliminary, Unconfirmed }

/** Prova de onde a questão saiu (banca, órgão, ano e número), para mostrar a etiqueta na lição. */
data class ExamRef(
    val banca: String,
    val body: String,
    val year: Int,
    val number: Int,
    val answerKey: AnswerKey,
    /** Cargo da prova (ex.: "Analista de Sistemas"); vazio nas questões adaptadas. */
    val cargo: String = "",
) {
    /** Ex.: "FGV · Banestes 2021 · questão 48". */
    val shortLabel: String get() = "$banca · $body $year · questão $number"

    /** Versão de uma linha para a tela da questão: "FCC · Teresina 2016 · Q22". */
    val compactLabel: String get() = "$banca · ${body.removePrefix("Prefeitura de ")} $year · Q$number"

    /** [shortLabel] com o aviso de gabarito quando não é o definitivo. */
    val label: String
        get() = shortLabel + when (answerKey) {
            AnswerKey.Final -> ""
            AnswerKey.Preliminary -> " · gabarito preliminar"
            AnswerKey.Unconfirmed -> " · gabarito não confirmado"
        }
}

/** Exercício de IA feito a partir de uma questão oficial ([questionId]), para ensinar o mesmo assunto de outro jeito. */
data class AdaptedFrom(val questionId: String, val ref: ExamRef) {
    /** Ex.: "adaptada de FCC · Prefeitura de Teresina 2016 · questão 56". */
    val label: String get() = "adaptada de ${ref.shortLabel}"
}

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
    /** Prova de origem, só nas questões de provas anteriores. */
    val ref: ExamRef? = null,
    /** Conceito da lição que o exercício treina (lições no formato por conceitos). */
    val conceptId: String? = null,
    val level: QuestionLevel = QuestionLevel.Exam,
    /** Questão oficial de onde este exercício foi adaptado. Nunca cai na mesma sessão que ela. */
    val adaptedFrom: AdaptedFrom? = null,
    /**
     * Passo dentro do conceito (exercícios de ensino): 1 = exemplo resolvido, 2 = aplicar no mesmo cenário, 3 = variar.
     * Exercícios com o mesmo passo são variantes; a sessão usa um de cada.
     */
    val step: Int? = null,
) {
    /** Casos práticos ganham mais tempo no cronômetro, se o aluno ativar a opção. */
    val isCase: Boolean get() = type == QuestionType.Case || style == QuestionStyle.CasoPratico

    /** Toda questão que não foi conferida pelo dono mostra o selo "não revisada". */
    val showUnreviewedBadge: Boolean get() = !reviewed

    /** Questão de prova anterior: o enunciado é o original; o que pode não estar revisado é só a explicação. */
    val isFromExam: Boolean get() = ref != null
}

data class Lesson(
    val id: String,
    val unitId: String,
    val order: Int,
    val title: String,
    val questions: List<Question>,
    /** Conceitos da lição, na ordem em que são apresentados. Vazio nas lições no formato antigo. */
    val concepts: List<Concept> = emptyList(),
) {
    /** Lição sem questões ainda ("conteúdo chegando"). */
    val hasContent: Boolean get() = questions.isNotEmpty()

    fun concept(id: String?): Concept? = id?.let { wanted -> concepts.firstOrNull { it.id == wanted } }
}

/** As duas trilhas do app: Conhecimentos Específicos (U1–U10) e Conhecimentos Gerais (G1–G4, `u11`–`u14`). */
enum class Track { Specific, General }

/** Unidade da trilha. Chamada `CourseUnit` para não colidir com `kotlin.Unit`. */
data class CourseUnit(
    val id: String,
    val order: Int,
    val title: String,
    val editalTopics: List<String>,
    val lessons: List<Lesson>,
    val track: Track = Track.Specific,
) {
    val hasContent: Boolean get() = lessons.any { it.hasContent }

    /** Rótulo curto: "Unidade 3" nos Específicos, "G2" nos Gerais. */
    val code: String get() = if (track == Track.General) "G${order - Curriculum.SPECIFIC_UNITS}" else "Unidade $order"
}
