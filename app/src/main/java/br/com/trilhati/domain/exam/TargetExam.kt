package br.com.trilhati.domain.exam

import br.com.trilhati.domain.model.CourseUnit
import br.com.trilhati.domain.model.Curriculum
import br.com.trilhati.domain.model.Track
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Lugar de uma unidade do catálogo ([Curriculum]) na trilha de uma prova. [lessons] limita quantas lições entram
 * (`null` = todas): o Técnico Bancário vê só as 3 primeiras de Matemática financeira, porque VPL e TIR não estão no
 * edital dele.
 */
data class UnitSlot(val unitId: String, val lessons: Int? = null)

/**
 * A prova que o aluno está estudando (0.13.0). Muda a trilha de Específicos, a data da contagem regressiva, o formato
 * da prova e o tempo-alvo padrão por questão. Os Gerais são os mesmos nas duas, e o progresso é um só: uma lição feita
 * vale em qualquer prova que a tenha.
 *
 * Datas e formato: edital nº 01/2027 (Analista, cap. 9) e edital de nível médio de 16/10/2026 (Técnico, itens 8.1 e 9.1).
 */
enum class TargetExam(
    /** Id guardado nas configurações e no retrato do progresso. Nunca mude. */
    val id: String,
    val title: String,
    val date: LocalDate,
    val questionCount: Int,
    val durationMinutes: Int,
    val specific: List<UnitSlot>,
    val general: List<UnitSlot>,
) {
    AnalistaTi(
        id = "analista-ti",
        title = "Analista de TI",
        date = LocalDate.of(2027, 2, 21),
        questionCount = 80,
        durationMinutes = 240,
        specific = (1..10).map { UnitSlot("u$it") },
        general = listOf(UnitSlot("u11"), UnitSlot("u12"), UnitSlot("u13"), UnitSlot("u14"), UnitSlot("u15")),
    ),
    TecnicoBancario(
        id = "tecnico-bancario",
        title = "Técnico Bancário",
        date = LocalDate.of(2027, 1, 24),
        questionCount = 70,
        durationMinutes = 240,
        specific = listOf(UnitSlot("u16"), UnitSlot("u17"), UnitSlot("u15", lessons = 3), UnitSlot("u18"), UnitSlot("u19")),
        general = listOf(UnitSlot("u11"), UnitSlot("u12"), UnitSlot("u13"), UnitSlot("u14")),
    ),
    ;

    /** Tempo-alvo padrão por questão: a duração da prova dividida pelas questões, em passos de 5 s (3:00 e 3:25). */
    val paceTargetMs: Long get() = durationMinutes * 60_000L / questionCount / 5_000L * 5_000L

    /** Texto do formato, para o painel e o diálogo: "80 questões em 4 horas". */
    val formatLabel: String get() = "$questionCount questões em ${durationMinutes / 60} horas"

    /** Todas as unidades da prova (Específicos e Gerais). */
    val unitIds: Set<String> get() = (specific + general).map { it.unitId }.toSet()

    /** Dias que faltam a partir de [today], nunca abaixo de zero. */
    fun daysLeft(today: LocalDate): Int = ChronoUnit.DAYS.between(today, date).coerceAtLeast(0).toInt()

    companion object {
        /** Quem já usava o app antes da 0.13.0 estudava para Analista. */
        val DEFAULT = AnalistaTi

        fun fromId(id: String?): TargetExam? = entries.firstOrNull { it.id == id }
    }
}

/** Monta a trilha de uma prova a partir das unidades do catálogo (já com as questões dos pacotes). */
object ExamCourse {
    /**
     * Específicos primeiro ("Unidade 1"…), depois os Gerais ("G1"…), cada unidade com a trilha e o rótulo da posição
     * dela nessa prova. Unidades que faltam no catálogo são puladas.
     */
    fun courseFor(exam: TargetExam, catalog: List<CourseUnit>): List<CourseUnit> {
        val byId = catalog.associateBy { it.id }
        fun place(slots: List<UnitSlot>, track: Track, label: (Int) -> String) = slots.mapNotNull { slot ->
            byId[slot.unitId]?.let { slot to it }
        }.mapIndexed { index, (slot, unit) ->
            val lessons = slot.lessons?.let { unit.lessons.take(it) } ?: unit.lessons
            unit.copy(lessons = lessons, track = track, code = label(index + 1))
        }
        return place(exam.specific, Track.Specific) { "Unidade $it" } + place(exam.general, Track.General) { "G$it" }
    }
}
