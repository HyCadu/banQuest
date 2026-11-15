package br.com.trilhati.feature.session

import br.com.trilhati.domain.pace.PaceZone
import br.com.trilhati.ui.mascot.MascotPose
import org.junit.Assert.assertEquals
import org.junit.Test

class LessonMascotTest {
    @Test fun `pensa respondendo e fica nervoso quando o tempo aperta`() {
        assertEquals(MascotPose.Thinking, LessonMascot.answering(null))
        assertEquals(MascotPose.Thinking, LessonMascot.answering(PaceZone.OnPace))
        assertEquals(MascotPose.Nervous, LessonMascot.answering(PaceZone.Warning))
        assertEquals(MascotPose.Nervous, LessonMascot.answering(PaceZone.Over))
    }

    @Test fun `reage ao resultado e se surpreende a cada 5 de combo`() {
        assertEquals(MascotPose.Happy, LessonMascot.feedback(AnswerOutcome.Correct, 3))
        assertEquals(MascotPose.Surprised, LessonMascot.feedback(AnswerOutcome.Correct, 5))
        assertEquals(MascotPose.Surprised, LessonMascot.feedback(AnswerOutcome.Correct, 10))
        assertEquals(MascotPose.Happy, LessonMascot.feedback(AnswerOutcome.Correct, 6))
        assertEquals(MascotPose.Happy, LessonMascot.feedback(AnswerOutcome.Corrected, 0))
        assertEquals(MascotPose.Sad, LessonMascot.feedback(AnswerOutcome.Wrong, 0))
    }
}
