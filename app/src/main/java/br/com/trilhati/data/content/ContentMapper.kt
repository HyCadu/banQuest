package br.com.trilhati.data.content

import br.com.trilhati.domain.model.CourseUnit
import br.com.trilhati.domain.model.Lesson
import br.com.trilhati.domain.model.MatchPair
import br.com.trilhati.domain.model.Question
import br.com.trilhati.domain.model.QuestionSource
import br.com.trilhati.domain.model.QuestionStyle
import br.com.trilhati.domain.model.QuestionType

/** Converte os valores textuais do JSON nos tipos do domínio. Devolve `null` se o valor for desconhecido. */
internal fun parseType(value: String): QuestionType? = when (value) {
    "mcq5" -> QuestionType.Mcq5
    "truefalse" -> QuestionType.TrueFalse
    "match" -> QuestionType.Match
    "order_code" -> QuestionType.OrderCode
    "fill_blank" -> QuestionType.FillBlank
    "code_output" -> QuestionType.CodeOutput
    "case" -> QuestionType.Case
    else -> null
}

internal fun parseStyle(value: String): QuestionStyle? = when (value) {
    "conceitual" -> QuestionStyle.Conceitual
    "literal" -> QuestionStyle.Literal
    "caso_pratico" -> QuestionStyle.CasoPratico
    "excecao" -> QuestionStyle.Excecao
    else -> null
}

internal fun parseSource(value: String): QuestionSource? = when (value) {
    "autoral" -> QuestionSource.Autoral
    "ia" -> QuestionSource.Ia
    "fcc_real" -> QuestionSource.FccReal
    else -> null
}

/** Mapeia o pacote para o domínio. Pressupõe um pacote já validado por [ContentValidator]. */
fun PackDto.toDomain(): CourseUnit? {
    val lessons = lessons.map { l ->
        Lesson(
            id = l.id,
            unitId = unit.id,
            order = l.order,
            title = l.title,
            questions = l.questions.mapNotNull { q ->
                val type = parseType(q.type) ?: return@mapNotNull null
                val style = parseStyle(q.style) ?: return@mapNotNull null
                val source = parseSource(q.source) ?: return@mapNotNull null
                Question(
                    id = q.id, lessonId = l.id, type = type, topic = q.topic, subtopic = q.subtopic,
                    difficulty = q.difficulty, style = style, stem = q.stem, code = q.code,
                    options = q.options, answer = q.answer,
                    pairs = q.pairs.map { MatchPair(it.left, it.right) }, lines = q.lines,
                    explanation = q.explanation, source = source, reviewed = q.reviewed,
                )
            },
        )
    }
    return CourseUnit(unit.id, unit.order, unit.title, unit.editalTopics, lessons)
}
