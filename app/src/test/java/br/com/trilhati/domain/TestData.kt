package br.com.trilhati.domain

import br.com.trilhati.domain.lesson.AnswerRecord
import br.com.trilhati.domain.model.CourseUnit
import br.com.trilhati.domain.model.Lesson
import br.com.trilhati.domain.model.MatchPair
import br.com.trilhati.domain.model.Question
import br.com.trilhati.domain.model.QuestionSource
import br.com.trilhati.domain.model.QuestionStyle
import br.com.trilhati.domain.model.QuestionType

object TestData {
    fun question(
        id: String,
        lessonId: String = "u1l1",
        style: QuestionStyle = QuestionStyle.Conceitual,
        type: QuestionType = QuestionType.Mcq5,
    ) = Question(
        id = id, lessonId = lessonId, type = type, topic = "BD", subtopic = "x", difficulty = 2, style = style,
        stem = "Enunciado $id", code = null, options = listOf("A", "B", "C", "D", "E"), answer = 2,
        pairs = emptyList<MatchPair>(), lines = emptyList(), explanation = "Porque sim.",
        source = QuestionSource.Ia, reviewed = false,
    )

    fun questions(count: Int, lessonId: String = "u1l1") = (1..count).map { question("${lessonId}q${it.toString().padStart(2, '0')}", lessonId) }

    fun lesson(unitOrder: Int, order: Int, withContent: Boolean = true) = Lesson(
        id = "u${unitOrder}l$order", unitId = "u$unitOrder", order = order, title = "Lição $order",
        questions = if (withContent) questions(10, "u${unitOrder}l$order") else emptyList(),
    )

    fun unit(order: Int, lessons: Int, withContent: Boolean = true) = CourseUnit(
        id = "u$order", order = order, title = "Unidade $order", editalTopics = emptyList(),
        lessons = (1..lessons).map { lesson(order, it, withContent) },
    )

    fun record(q: Question, correct: Boolean, retry: Boolean = false, timeMs: Long = 60_000, targetMs: Long = 180_000, xp: Int = 0) =
        AnswerRecord(q, correct, retry, timeMs, targetMs, xp)
}
