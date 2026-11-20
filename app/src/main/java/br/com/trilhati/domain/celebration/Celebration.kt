package br.com.trilhati.domain.celebration

/**
 * Celebração do fim de qualquer atividade com questões (lição, treino, revisão, prova anterior e as que vierem):
 * as etapas que a tela de conclusão apresenta, na ordem, e a intensidade. Só apresenta o que a lógica de negócio já
 * gravou: XP, gemas e ofensiva entram no banco antes da celebração começar, então ela nunca concede nada.
 */
enum class CelebrationStep {
    /** A atividade some, o mascote e o título entram. */
    Completion,

    /** Acertos, precisão e tempo médio, com contadores. */
    Results,

    /** XP ganho, com brilho e partículas, e as fichas de recompensa. */
    XpReward,

    /** Conquistas desbloqueadas agora. */
    Achievements,

    /** Chama da ofensiva: acender, contador de dias e a semana. Só quando a ofensiva subiu nesta atividade. */
    Streak,

    /** Tudo à mostra, parado. */
    Finished,
}

/**
 * Intensidade da animação. [Micro] é o feedback de cada questão (150 a 400 ms); [Medium], o fim de um treino ou
 * revisão (1 a 2 s); [Full], o fim de uma atividade com conquista ou ofensiva nova (3 a 6 s).
 */
enum class MotionLevel(val minMs: Int, val maxMs: Int) {
    Micro(150, 400),
    Medium(1_000, 2_000),
    Full(3_000, 6_000),
}

/** O que a atividade terminada tem para mostrar. Sem valores inventados: vem do resumo já gravado. */
data class CelebrationInput(
    /** Atividade com recompensa (o treino livre e a revisão livre não dão XP). */
    val rewarded: Boolean,
    val xp: Int,
    val achievements: Int,
    /** A ofensiva subiu com esta atividade (primeira lição, ou prova inteira, do dia). */
    val streakIncreased: Boolean,
)

object CelebrationPlan {
    /** As etapas desta atividade, na ordem; as que não têm o que mostrar ficam de fora. */
    fun steps(input: CelebrationInput): List<CelebrationStep> = buildList {
        add(CelebrationStep.Completion)
        add(CelebrationStep.Results)
        if (input.rewarded && input.xp > 0) add(CelebrationStep.XpReward)
        if (input.achievements > 0) add(CelebrationStep.Achievements)
        if (input.streakIncreased) add(CelebrationStep.Streak)
        add(CelebrationStep.Finished)
    }

    fun level(input: CelebrationInput): MotionLevel =
        if (input.streakIncreased || input.achievements > 0) MotionLevel.Full else MotionLevel.Medium

    fun start(input: CelebrationInput): CelebrationState = CelebrationState(steps(input), level = level(input))
}

/**
 * Estado da celebração, imutável: só anda para a frente. Concluir uma etapa que não é a atual não faz nada (evita
 * eventos repetidos depois de recomposição, rotação ou toque duplo), e a etapa da chama só termina depois de acesa.
 * Fica no ViewModel, então sobrevive a rotação e à volta para a tela.
 */
data class CelebrationState(
    val steps: List<CelebrationStep>,
    val index: Int = 0,
    val flameLit: Boolean = false,
    val skipped: Boolean = false,
    val level: MotionLevel = MotionLevel.Medium,
) {
    init {
        require(steps.isNotEmpty() && steps.last() == CelebrationStep.Finished) { "a celebração termina em Finished" }
        require(index in steps.indices)
    }

    val step: CelebrationStep get() = steps[index]
    val finished: Boolean get() = step == CelebrationStep.Finished
    val hasStreak: Boolean get() = CelebrationStep.Streak in steps

    /** A próxima etapa depois da atual (Finished não tem próxima). */
    val next: CelebrationStep? get() = steps.getOrNull(index + 1)

    /** [s] já começou ou passou? Etapas que não fazem parte desta atividade contam como passadas. */
    fun reached(s: CelebrationStep): Boolean {
        val i = steps.indexOf(s)
        return i < 0 || i <= index
    }

    /** [s] já terminou (a atual ainda não)? */
    fun past(s: CelebrationStep): Boolean {
        val i = steps.indexOf(s)
        return i < 0 || i < index || finished
    }

    /** Terminou a etapa [s]: vai para a próxima. Ignorado se [s] não é a atual ou se a chama ainda está apagada. */
    fun complete(s: CelebrationStep): CelebrationState = when {
        s != step || finished -> this
        s == CelebrationStep.Streak && !flameLit -> this
        else -> copy(index = index + 1)
    }

    /** Acendeu a chama (uma vez só, e só na etapa da chama). */
    fun lightFlame(): CelebrationState = if (step == CelebrationStep.Streak && !flameLit) copy(flameLit = true) else this

    /** Pular: tudo à mostra de uma vez. A chama conta como acesa (a ofensiva já foi gravada). */
    fun skip(): CelebrationState =
        if (finished) this else copy(index = steps.lastIndex, flameLit = flameLit || hasStreak, skipped = true)
}
