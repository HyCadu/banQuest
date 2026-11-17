package br.com.trilhati.domain.progress

import br.com.trilhati.domain.model.CourseUnit
import br.com.trilhati.domain.model.Track

enum class LessonStatus { Locked, Available, Done, Perfect }

/**
 * Desbloqueio da trilha: concluir uma lição libera a seguinte; só a primeira não concluída fica disponível. Cada trilha
 * (Específicos e Gerais) anda sozinha: uma lição travada nos Específicos não trava os Gerais.
 */
object TrailEngine {
    fun statuses(units: List<CourseUnit>, progress: Map<String, LessonProgress>): Map<String, LessonStatus> {
        val result = LinkedHashMap<String, LessonStatus>()
        Track.entries.forEach { track -> result += trackStatuses(units.filter { it.track == track }, progress) }
        return result
    }

    private fun trackStatuses(units: List<CourseUnit>, progress: Map<String, LessonProgress>): Map<String, LessonStatus> {
        val result = LinkedHashMap<String, LessonStatus>()
        var foundCurrent = false
        // Segue a ordem da trilha da prova, não o número da unidade: no Técnico, a Matemática financeira (u15) é a
        // Unidade 3, depois de u16 e u17. Ordenar por número a punha antes de u16 e travava a Unidade 1 (0.14.0).
        for (lesson in units.flatMap { u -> u.lessons.sortedBy { it.order } }) {
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

    /**
     * Unidades totalmente concluídas (para o cálculo de conquistas). Conta ids distintos: as trilhas das duas provas
     * podem vir juntas, com os Gerais repetidos, e uma unidade conta se estiver completa em qualquer uma delas.
     */
    fun completedUnitCount(units: List<CourseUnit>, progress: Map<String, LessonProgress>): Int =
        units.filter { isUnitComplete(it, progress) }.map { it.id }.toSet().size
}
