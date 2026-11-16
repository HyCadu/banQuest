package br.com.trilhati.data.content

import br.com.trilhati.domain.model.CourseUnit
import br.com.trilhati.domain.model.Curriculum
import br.com.trilhati.domain.model.Lesson
import br.com.trilhati.domain.model.Question

/**
 * Monta a trilha completa (10 unidades, 48 lições) juntando o esqueleto do currículo com as questões dos pacotes.
 * Lições sem pacote ficam sem questões e aparecem como "conteúdo chegando".
 *
 * As questões de provas anteriores ([examQuestions]) entram no banco da lição indicada, mas só nas lições que já têm
 * questões do pacote: assim nenhuma lição vira uma lição de 1 ou 2 questões, e as que estão esperando entram sozinhas
 * quando o pacote da unidade chegar.
 */
object CourseAssembler {
    fun assemble(packUnits: List<CourseUnit>, examQuestions: List<Question> = emptyList()): List<CourseUnit> {
        val byUnit = packUnits.associateBy { it.id }
        val examByLesson = examQuestions.groupBy { it.lessonId }
        return Curriculum.units.map { info ->
            val pack = byUnit[info.id]
            val packLessons = pack?.lessons?.associateBy { it.id }.orEmpty()
            val lessons = info.lessonTitles.mapIndexed { index, title ->
                val id = Curriculum.lessonId(info.order, index + 1)
                val lesson = packLessons[id]?.copy(unitId = info.id, order = index + 1)
                    ?: Lesson(id = id, unitId = info.id, order = index + 1, title = title, questions = emptyList())
                val extra = examByLesson[id].orEmpty()
                if (lesson.hasContent && extra.isNotEmpty()) lesson.copy(questions = (lesson.questions + extra).distinctBy { it.id }) else lesson
            }
            CourseUnit(info.id, info.order, pack?.title ?: info.title, pack?.editalTopics.orEmpty(), lessons)
        }
    }
}
