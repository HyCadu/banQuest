package br.com.trilhati.domain.celebration

import br.com.trilhati.domain.celebration.CelebrationStep.Achievements
import br.com.trilhati.domain.celebration.CelebrationStep.Completion
import br.com.trilhati.domain.celebration.CelebrationStep.Finished
import br.com.trilhati.domain.celebration.CelebrationStep.Results
import br.com.trilhati.domain.celebration.CelebrationStep.Streak
import br.com.trilhati.domain.celebration.CelebrationStep.XpReward
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class CelebrationTest {
    private val full = CelebrationInput(rewarded = true, xp = 150, achievements = 1, streakIncreased = true)

    @Test fun `atividade completa passa por todas as etapas, na ordem`() {
        assertEquals(listOf(Completion, Results, XpReward, Achievements, Streak, Finished), CelebrationPlan.steps(full))
        assertEquals(MotionLevel.Full, CelebrationPlan.level(full))
    }

    @Test fun `etapas sem nada para mostrar ficam de fora`() {
        val plain = CelebrationInput(rewarded = true, xp = 90, achievements = 0, streakIncreased = false)
        assertEquals(listOf(Completion, Results, XpReward, Finished), CelebrationPlan.steps(plain))
        assertEquals(MotionLevel.Medium, CelebrationPlan.level(plain))
    }

    @Test fun `treino livre nao mostra XP`() {
        val free = CelebrationInput(rewarded = false, xp = 0, achievements = 0, streakIncreased = false)
        assertEquals(listOf(Completion, Results, Finished), CelebrationPlan.steps(free))
    }

    @Test fun `so a ofensiva nova ja e celebracao completa`() {
        val streakOnly = CelebrationInput(rewarded = true, xp = 0, achievements = 0, streakIncreased = true)
        assertEquals(listOf(Completion, Results, Streak, Finished), CelebrationPlan.steps(streakOnly))
        assertEquals(MotionLevel.Full, CelebrationPlan.level(streakOnly))
    }

    @Test fun `so anda para a frente e ignora conclusao repetida ou fora de hora`() {
        var s = CelebrationPlan.start(full)
        assertEquals(Completion, s.step)
        s = s.complete(Completion)
        assertEquals(Results, s.step)
        // Toque duplo, recomposição ou etapa errada: nada muda.
        assertSame(s, s.complete(Completion))
        assertSame(s, s.complete(XpReward))
        s = s.complete(Results).complete(XpReward).complete(Achievements)
        assertEquals(Streak, s.step)
        assertTrue(s.past(Achievements))
        assertTrue(s.reached(Streak))
        assertFalse(s.past(Streak))
    }

    @Test fun `a etapa da chama so termina depois de acesa, e acende uma vez`() {
        var s = CelebrationPlan.start(full).complete(Completion).complete(Results).complete(XpReward).complete(Achievements)
        assertSame(s, s.complete(Streak))
        s = s.lightFlame()
        assertTrue(s.flameLit)
        assertSame(s, s.lightFlame())
        s = s.complete(Streak)
        assertTrue(s.finished)
        assertSame(s, s.complete(Finished))
    }

    @Test fun `acender fora da etapa da chama nao faz nada`() {
        val s = CelebrationPlan.start(full)
        assertSame(s, s.lightFlame())
    }

    @Test fun `pular mostra tudo e conta a chama como acesa`() {
        val s = CelebrationPlan.start(full).complete(Completion).skip()
        assertTrue(s.finished)
        assertTrue(s.skipped)
        assertTrue(s.flameLit)
        assertSame(s, s.skip())
        CelebrationStep.entries.forEach { assertTrue(s.reached(it)) }
    }

    @Test fun `etapa que nao faz parte da atividade conta como passada`() {
        val s = CelebrationPlan.start(CelebrationInput(rewarded = false, xp = 0, achievements = 0, streakIncreased = false))
        assertTrue(s.past(Streak))
        assertTrue(s.past(XpReward))
        assertFalse(s.past(Completion))
        assertFalse(s.hasStreak)
    }
}
