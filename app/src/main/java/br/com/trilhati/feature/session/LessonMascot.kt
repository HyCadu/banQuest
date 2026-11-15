package br.com.trilhati.feature.session

import br.com.trilhati.domain.pace.PaceZone
import br.com.trilhati.ui.mascot.MascotPose

/** Como terminou a última resposta, para o personagem reagir. */
enum class AnswerOutcome { Correct, Corrected, Wrong }

/**
 * Pose do personagem durante a lição.
 *  - Respondendo: pensa; fica nervoso quando o cronômetro visível passa do âmbar (o ritmo de prova é o diferencial).
 *  - Depois de verificar: feliz no acerto (surpreso a cada 5 de combo), feliz também no "corrigido", triste no erro.
 */
object LessonMascot {
    const val COMBO_MILESTONE = 5

    fun answering(zone: PaceZone?): MascotPose = when (zone) {
        PaceZone.Warning, PaceZone.Over -> MascotPose.Nervous
        else -> MascotPose.Thinking
    }

    fun feedback(outcome: AnswerOutcome, combo: Int): MascotPose = when (outcome) {
        AnswerOutcome.Correct -> if (combo >= COMBO_MILESTONE && combo % COMBO_MILESTONE == 0) MascotPose.Surprised else MascotPose.Happy
        AnswerOutcome.Corrected -> MascotPose.Happy
        AnswerOutcome.Wrong -> MascotPose.Sad
    }
}
