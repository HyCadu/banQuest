package br.com.trilhati.ui.animation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import br.com.trilhati.domain.celebration.MotionLevel
import br.com.trilhati.ui.theme.LocalReducedMotion

/**
 * Sistema de animação do BanQuest (0.15.0). Tudo aqui é Compose nativo (Animatable, transições infinitas, Canvas e
 * pointerInput), sem biblioteca nova, e serve a qualquer atividade com questões: lição, treino, revisão, prova anterior.
 *
 * Pastas:
 * - `feedback/`: microinterações de cada questão (acerto e erro), nível [MotionLevel.Micro].
 * - `effects/`: partículas, brilho, brasas e contador, peças usadas pelas outras.
 * - `celebration/`: conclusão da atividade, conquistas e a chama da ofensiva.
 */

/**
 * A animação está ligada? Desliga quando a tela pede ([animated] = false, nas prévias e testes) ou quando o Android está
 * com "remover animações" ativado ([LocalReducedMotion]). Desligada, cada componente mostra direto o estado final.
 */
@Composable
@ReadOnlyComposable
fun motionOn(animated: Boolean): Boolean = animated && !LocalReducedMotion.current

/** Durações em milissegundos, para as peças do sistema falarem a mesma língua. */
object MotionMs {
    /** Acerto: o cartão cresce e volta com mola. */
    const val CORRECT_POP = 110

    /** Erro: tremida curta, nunca punitiva. */
    const val WRONG_SHAKE = 320

    /** Faíscas do acerto. */
    const val SPARKLE = 520

    /** Brilho da barra de progresso quando ela avança. */
    const val PROGRESS_FLASH = 380

    /** Contadores da conclusão. */
    const val COUNTER = 850

    /** Entrada escalonada dos cartões da conclusão. */
    const val STAGGER = 130
}

/** Fator de tempo da celebração: a completa respira mais; o fim de um treino é mais curto. */
fun MotionLevel.timeScale(): Float = when (this) {
    MotionLevel.Micro -> 0.5f
    MotionLevel.Medium -> 0.75f
    MotionLevel.Full -> 1f
}
