package br.com.trilhati.domain.progress

import br.com.trilhati.domain.model.CourseUnit

enum class LessonStatus { Locked, Available, Done, Perfect }

/** Desbloqueio da trilha: concluir uma lição libera a seguinte; só a primeira não concluída fica disponível. */
object TrailEngine {
    fun statuses(units: List<CourseUnit>, progress: Map<String, LessonProgress>): Map<String, LessonStatus> {
        val result = LinkedHashMap<String, LessonStatus>()
        var foundCurrent = false
        for (lesson in units.sortedBy { it.order }.flatMap { u -> u.lessons.sortedBy { it.order } }) {
            val p = progress[lesson.id]
            result[lesson.id] = when {
                p != null && p.completed -> if (p.perfect) LessonStatus.Perfect else LessonStatus.Done
                !foundCurrent && lesson.hasContent -> LessonStatus.Available.also { foundCurrent = true }
                else -> {
                    // A primeira lição sem conteúdo bloqueia o restante: não dá para "pular" a trilha.
                    foundCurrent = true
                    LessonStatus.Locked
                }
            }
        }
        return result
    }

    fun isUnitComplete(unit: CourseUnit, progress: Map<String, LessonProgress>): Boolean =
        unit.lessons.isNotEmpty() && unit.lessons.all { l -> l.hasContent && progress[l.id]?.completed == true }

    /** Unidades totalmente concluídas, mapeadas para os ids das lições (para o cálculo de conquistas). */
    fun completedUnitCount(units: List<CourseUnit>, progress: Map<String, LessonProgress>): Int =
        units.count { isUnitComplete(it, progress) }
}
