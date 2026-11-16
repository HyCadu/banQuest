package br.com.trilhati.data.content

import br.com.trilhati.domain.model.AdaptedFrom
import br.com.trilhati.domain.model.AnswerKey
import br.com.trilhati.domain.model.Concept
import br.com.trilhati.domain.model.CourseUnit
import br.com.trilhati.domain.model.ExamRef
import br.com.trilhati.domain.model.Lesson
import br.com.trilhati.domain.model.MatchPair
import br.com.trilhati.domain.model.Question
import br.com.trilhati.domain.model.QuestionLevel
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
    "quick" -> QuestionType.Quick
    else -> null
}

/** Sem nível no JSON, a questão vale como de prova (formato antigo). */
internal fun parseLevel(value: String?): QuestionLevel? = when (value) {
    null, "prova" -> QuestionLevel.Exam
    "aprender" -> QuestionLevel.Learn
    "praticar" -> QuestionLevel.Practice
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
    "prova_real" -> QuestionSource.ProvaReal
    else -> null
}

internal fun parseAnswerKey(value: String): AnswerKey? = when (value) {
    "definitivo" -> AnswerKey.Final
    "preliminar" -> AnswerKey.Preliminary
    "nao_confirmado" -> AnswerKey.Unconfirmed
    else -> null
}

private fun ExamRefDto.toDomain(): ExamRef? = parseAnswerKey(gabarito)?.let { ExamRef(banca, orgao, ano, numero, it) }

private fun AdaptedFromDto.toDomain() = AdaptedFrom(id, ExamRef(banca, orgao, ano, numero, AnswerKey.Final))

/** Converte uma questão já validada. Devolve `null` se algum valor textual for desconhecido. */
private fun QuestionDto.toDomain(lessonId: String): Question? {
    val parsedType = parseType(type) ?: return null
    val parsedStyle = parseStyle(style) ?: return null
    val parsedSource = parseSource(source) ?: return null
    val parsedLevel = parseLevel(level) ?: return null
    return Question(
        id = id, lessonId = lessonId, type = parsedType, topic = topic, subtopic = subtopic,
        difficulty = difficulty, style = parsedStyle, stem = stem, code = code,
        options = options, answer = answer,
        pairs = pairs.map { MatchPair(it.left, it.right) }, lines = lines,
        explanation = explanation, source = parsedSource, reviewed = reviewed, ref = ref?.toDomain(),
        conceptId = concept, level = parsedLevel, adaptedFrom = adaptedFrom?.toDomain(), step = step,
    )
}

/** Questões de provas anteriores, cada uma com a lição da trilha onde entra. Pressupõe um pacote validado. */
fun ExamPackDto.toDomain(): List<Question> = questions.mapNotNull { q -> q.lesson?.let { q.toDomain(it) } }

/** Mapeia o pacote para o domínio. Pressupõe um pacote já validado por [ContentValidator]. */
fun PackDto.toDomain(): CourseUnit? {
    val lessons = lessons.map { l ->
        Lesson(
            id = l.id,
            unitId = unit.id,
            order = l.order,
            title = l.title,
            questions = l.questions.mapNotNull { q -> q.toDomain(l.id) },
            concepts = l.concepts.map { Concept(it.id, it.title, it.summary) },
        )
    }
    return CourseUnit(unit.id, unit.order, unit.title, unit.editalTopics, lessons)
}
