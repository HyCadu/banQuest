package br.com.trilhati.data.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Formato em disco de um pacote de conteúdo (PLAN.md §5.1). */
@Serializable
data class PackDto(
    val schema: Int,
    val pack: String,
    val version: Int,
    val unit: UnitDto,
    val lessons: List<LessonDto>,
)

@Serializable
data class UnitDto(
    val id: String,
    val order: Int,
    val title: String,
    val editalTopics: List<String> = emptyList(),
)

@Serializable
data class LessonDto(
    val id: String,
    val order: Int,
    val title: String,
    val questions: List<QuestionDto>,
    /** Formato por conceitos (opcional): as ideias que a lição ensina. */
    val concepts: List<ConceptDto> = emptyList(),
)

@Serializable
data class ConceptDto(val id: String, val title: String, val summary: String)

/** Questão oficial de onde um exercício foi adaptado. */
@Serializable
data class AdaptedFromDto(val id: String, val banca: String, val orgao: String, val ano: Int, val numero: Int)

/**
 * Pacote de questões de provas anteriores (`assets/provas`), gerado por `tools/build_provas.py`. Cada questão traz a
 * lição da trilha onde entra ([QuestionDto.lesson]) e a prova de origem ([QuestionDto.ref]).
 */
@Serializable
data class ExamPackDto(
    val schema: Int,
    val pack: String,
    val version: Int,
    val questions: List<QuestionDto>,
)

@Serializable
data class ExamRefDto(
    val banca: String,
    val orgao: String,
    val ano: Int,
    val numero: Int,
    val cargo: String = "",
    val prova: String = "",
    /** "definitivo", "preliminar" ou "nao_confirmado". */
    val gabarito: String = "definitivo",
)

@Serializable
data class PairDto(val left: String, val right: String)

@Serializable
data class QuestionDto(
    val id: String,
    val type: String,
    val topic: String,
    val subtopic: String = "",
    val difficulty: Int = 2,
    val style: String,
    val stem: String,
    val code: String? = null,
    val options: List<String> = emptyList(),
    val answer: Int = 0,
    val pairs: List<PairDto> = emptyList(),
    val lines: List<String> = emptyList(),
    val explanation: String,
    val source: String,
    @SerialName("reviewed") val reviewed: Boolean = false,
    /** Só nas questões de provas anteriores: lição da trilha onde a questão entra. */
    val lesson: String? = null,
    /** Só nas questões de provas anteriores: de onde a questão saiu. */
    val ref: ExamRefDto? = null,
    /** Formato por conceitos: id do conceito da lição (ex.: "u1l1c3"). */
    val concept: String? = null,
    /** "aprender", "praticar" ou "prova" (sem nível vale "prova"). */
    val level: String? = null,
    val adaptedFrom: AdaptedFromDto? = null,
    /** Passo dentro do conceito (1, 2, 3…), só nos exercícios de aprender e praticar. */
    val step: Int? = null,
)
