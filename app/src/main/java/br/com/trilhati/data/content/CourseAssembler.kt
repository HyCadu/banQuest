package br.com.trilhati.data.content

import br.com.trilhati.domain.model.CourseUnit
import br.com.trilhati.domain.model.Curriculum
import br.com.trilhati.domain.model.Lesson

/**
 * Monta a trilha completa (10 unidades, 48 lições) juntando o esqueleto do currículo com as questões dos pacotes.
 * Lições sem pacote ficam sem questões e aparecem como "conteúdo chegando".
 */
object CourseAssembler {
    fun assemble(packUnits: List<CourseUnit>): List<CourseUnit> {
        val byUnit = packUnits.associateBy { it.id }
        return Curriculum.units.map { info ->
            val pack = byUnit[info.id]
            val packLessons = pack?.lessons?.associateBy { it.id }.orEmpty()
            val lessons = info.lessonTitles.mapIndexed { index, title ->
                val id = Curriculum.lessonId(info.order, index + 1)
                packLessons[id]?.copy(unitId = info.id, order = index + 1)
                    ?: Lesson(id = id, unitId = info.id, order = index + 1, title = title, questions = emptyList())
            }
            CourseUnit(info.id, info.order, pack?.title ?: info.title, pack?.editalTopics.orEmpty(), lessons)
        }
    }
}
