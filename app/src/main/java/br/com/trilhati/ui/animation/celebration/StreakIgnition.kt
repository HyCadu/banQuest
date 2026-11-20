package br.com.trilhati.ui.animation.celebration

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.trilhati.domain.celebration.StreakWeek
import br.com.trilhati.domain.celebration.WeekDayInfo
import br.com.trilhati.ui.animation.effects.AnimatedCounter
import br.com.trilhati.ui.animation.effects.EmberField
import br.com.trilhati.ui.animation.effects.ParticleBurst
import br.com.trilhati.ui.animation.effects.glow
import br.com.trilhati.ui.animation.motionOn
import br.com.trilhati.ui.components.ChunkyButton
import br.com.trilhati.ui.components.ChunkyStyle
import br.com.trilhati.ui.icons.GameIcon
import br.com.trilhati.ui.icons.GameIconView
import br.com.trilhati.ui.icons.IconColors
import br.com.trilhati.ui.mascot.Mascot
import br.com.trilhati.ui.mascot.MascotCharacter
import br.com.trilhati.ui.mascot.MascotPose
import br.com.trilhati.ui.theme.Spacing
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** A cena da chama é sempre escura, em qualquer tema: a luz da chama é a protagonista. */
private object NightColors {
    val background = Color(0xFF15122B)
    val ink = Color(0xFFF6F3FF)
    val inkSoft = Color(0xFFC3BCE4)
    val dayOff = Color(0xFF2C2748)
    val dayFuture = Color(0xFF221E3B)
    val frozen = Color(0xFF5FB8F2)
}

/** Progresso de cada parte da cena, criado já no fim quando a chama estava acesa (rotação ou volta para a tela). */
private class IgnitionState(lit: Boolean, days: Int) {
    private fun a(on: Boolean) = Animatable(if (on) 1f else 0f)
    val heat = a(lit)
    val color = a(lit)
    val scale = Animatable(1f)
    val ring = Animatable(1f)
    val lift = a(lit)
    val counter = a(lit)
    val label = a(lit)
    val days: List<Animatable<Float, AnimationVector1D>> = List(days) { a(lit) }
    val mascot = a(lit)
    val button = a(lit)
    var burst by mutableIntStateOf(0)
    var counting by mutableStateOf(false)

    suspend fun showAll() {
        (listOf(heat, color, lift, counter, label, mascot, button) + days).forEach { it.snapTo(1f) }
        scale.snapTo(1f)
        ring.snapTo(1f)
    }
}

/**
 * Celebração da ofensiva: a tela escurece, a chama aparece apagada (balançando, com o núcleo respirando e brasas
 * subindo) e o aluno a acende arrastando para cima, tocando nela ou no botão. Na ignição o núcleo incha, a chama
 * encolhe e explode para cima, um anel de luz se expande e saem faíscas; depois o número de dias salta, a semana entra
 * dia a dia e o mascote comemora.
 *
 * Só apresenta: a ofensiva já foi gravada pela lógica do app. [onLight] avisa que a chama foi acesa (uma vez; o estado
 * fica no ViewModel), e a cena volta acesa depois de rotação ou ao voltar para a tela.
 *
 * @param before ofensiva antes desta atividade (o contador sobe dela até [after]).
 */
@Composable
fun StreakIgnition(
    before: Int,
    after: Int,
    week: List<WeekDayInfo>,
    character: MascotCharacter,
    lit: Boolean,
    onLight: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    animated: Boolean = true,
    haptics: Boolean = true,
) {
    val on = motionOn(animated)
    val litAtStart = remember { lit }
    val state = remember { IgnitionState(lit, week.size) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    var asked by remember { mutableStateOf(lit) }
    val ignite: () -> Unit = {
        if (!asked) {
            asked = true
            onLight()
        }
    }
    val time = if (on) {
        rememberInfiniteTransition(label = "flame").animateFloat(0f, 1f, infiniteRepeatable(tween(2_200, easing = LinearEasing)), label = "flameTime")
    } else {
        null
    }

    LaunchedEffect(lit, on) {
        if (!lit) return@LaunchedEffect
        if (!on || litAtStart) {
            state.showAll()
            state.counting = true
            return@LaunchedEffect
        }
        // 1. Antecipação: o núcleo incha e a chama encolhe.
        if (haptics) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        coroutineScope {
            launch { state.heat.animateTo(1f, tween(200, easing = FastOutSlowInEasing)) }
            state.scale.animateTo(0.86f, tween(170, easing = FastOutSlowInEasing))
        }
        // 2. Ignição: cor, anel de luz, faíscas e o salto para cima.
        state.burst++
        if (haptics) haptic.performHapticFeedback(HapticFeedbackType.Confirm)
        coroutineScope {
            launch { state.color.animateTo(1f, tween(320)) }
            launch {
                state.ring.snapTo(0f)
                state.ring.animateTo(1f, tween(760, easing = FastOutSlowInEasing))
            }
            launch {
                state.scale.animateTo(1.2f, tween(150, easing = FastOutSlowInEasing))
                state.scale.animateTo(1f, popSpring())
            }
            // 3. A chama sobe para dar lugar ao contador, que salta passando do ponto.
            delay(260)
            launch { state.lift.animateTo(1f, enterSpring()) }
            delay(180)
            state.counting = true
            launch { state.counter.animateTo(1f, popSpring()) }
            delay(220)
            launch { state.label.animateTo(1f, enterSpring()) }
            delay(260)
            // 4. A semana entra dia a dia; por fim o mascote e o botão.
            state.days.forEachIndexed { i, d ->
                launch {
                    delay(i * 70L)
                    d.animateTo(1f, popSpring())
                }
            }
            delay(state.days.size * 70L + 200)
            launch { state.mascot.animateTo(1f, popSpring()) }
            delay(250)
            state.button.animateTo(1f, enterSpring())
        }
    }

    val threshold = 220f
    Box(
        modifier.fillMaxSize().background(NightColors.background)
            .then(
                if (lit) {
                    Modifier
                } else {
                    Modifier
                        .pointerInput(Unit) { detectTapGestures(onTap = { ignite() }) }
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onDragEnd = { if (!asked) scope.launch { state.heat.animateTo(0f, tween(260)) } },
                                onDragCancel = { if (!asked) scope.launch { state.heat.animateTo(0f, tween(260)) } },
                                onVerticalDrag = { change, dy ->
                                    change.consume()
                                    if (!asked) {
                                        val next = (state.heat.value - dy / threshold.dp.toPx()).coerceIn(0f, 1f)
                                        scope.launch { state.heat.snapTo(next) }
                                        if (next >= 0.98f) ignite()
                                    }
                                },
                            )
                        }
                },
            )
            .statusBarsPadding().navigationBarsPadding(),
    ) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = Spacing.screen),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Chama: começa no centro e sobe quando acende.
            Box(
                Modifier.size(260.dp).graphicsLayer {
                    translationY = (1f - state.lift.value) * 90.dp.toPx()
                    val s = 1f - 0.12f * state.lift.value
                    scaleX = s
                    scaleY = s
                },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier.size(260.dp).glow(
                        FlameColors.glow,
                        { 0.25f + 0.35f * state.heat.value + 0.4f * state.color.value * (0.85f + 0.15f * ((time?.value ?: 0.5f) * 2f - 1f)) },
                        radius = 0.55f,
                    ),
                )
                if (on) {
                    EmberField(
                        FlameColors.coreOff, Modifier.size(200.dp), count = 12,
                        intensity = { 0.45f + 0.55f * maxOf(state.heat.value, state.color.value) },
                    )
                }
                // Anel de luz da ignição.
                Canvas(Modifier.size(260.dp)) {
                    val r = state.ring.value
                    if (r < 1f) {
                        drawCircle(
                            FlameColors.coreOn.copy(alpha = (1f - r) * 0.8f),
                            radius = size.minDimension * (0.18f + 0.42f * r),
                            center = Offset(size.width / 2f, size.height * 0.55f),
                            style = Stroke((10f * (1f - r) + 2f).dp.toPx()),
                        )
                    }
                }
                FlameArt(
                    Modifier.size(150.dp)
                        .graphicsLayer {
                            val s = state.scale.value
                            scaleX = s
                            scaleY = s
                            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.9f)
                        }
                        .semantics {
                            contentDescription = if (lit) "Chama da ofensiva acesa" else "Chama apagada. Toque duas vezes para acender"
                            if (!lit) {
                                role = Role.Button
                                onClick("Acender a chama") { ignite(); true }
                            }
                        },
                    lit = { state.color.value },
                    heat = { maxOf(state.heat.value, 0.12f) },
                    time = { time?.value ?: 0.25f },
                )
                if (on) {
                    ParticleBurst(
                        trigger = state.burst,
                        colors = listOf(FlameColors.coreOn, FlameColors.bodyOn, FlameColors.glow, Color.White),
                        modifier = Modifier.size(260.dp),
                        count = 40, power = 150.dp, sizeRange = 3f..8f, durationMs = 1_100, gravity = 0.55f,
                        origin = Offset(0.5f, 0.52f),
                    )
                }
                Mascot(
                    character, MascotPose.Celebrate,
                    Modifier.align(Alignment.BottomEnd).size(96.dp).reveal(state.mascot, rise = 30f, fromScale = 0.3f),
                    animated = animated, decorative = true,
                )
            }

            Box(Modifier.height(176.dp), contentAlignment = Alignment.Center) {
                if (!lit) {
                    Text(
                        "Deslize para cima e acenda a chama de hoje",
                        style = MaterialTheme.typography.titleLarge, color = NightColors.ink, textAlign = TextAlign.Center,
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AnimatedCounter(
                            after, play = state.counting,
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 84.sp, fontWeight = FontWeight.Black),
                            color = FlameColors.bodyOn,
                            modifier = Modifier.reveal(state.counter, rise = 40f, fromScale = 0.2f)
                                .semantics { liveRegion = LiveRegionMode.Polite },
                            from = before.coerceAtMost(after), durationMs = 600, motion = on, startAtEnd = litAtStart,
                            description = "Ofensiva de $after ${if (after == 1) "dia" else "dias"}",
                        )
                        Text(
                            if (after == 1) "dia de ofensiva" else "dias de ofensiva",
                            Modifier.reveal(state.label, rise = 12f, fromScale = 0.9f),
                            style = MaterialTheme.typography.headlineSmall, color = FlameColors.bodyOn,
                        )
                    }
                }
            }

            WeekStrip(week, state.days, Modifier.fillMaxWidth().padding(top = Spacing.md))
            Spacer(Modifier.height(Spacing.lg))

            Box(Modifier.fillMaxWidth().height(64.dp), contentAlignment = Alignment.Center) {
                if (!lit) {
                    ChunkyButton("Acender a chama", ignite, Modifier.fillMaxWidth(), ChunkyStyle.Neutral)
                } else {
                    ChunkyButton("Continuar", onContinue, Modifier.fillMaxWidth().reveal(state.button, rise = 16f, fromScale = 0.9f), ChunkyStyle.Primary)
                }
            }
        }
    }
}

/** Os sete dias da semana; os estudados com a chama, o de hoje em destaque. Cada dia entra com seu próprio [reveals]. */
@Composable
private fun WeekStrip(week: List<WeekDayInfo>, reveals: List<Animatable<Float, AnimationVector1D>>, modifier: Modifier = Modifier) {
    Row(
        modifier.semantics(mergeDescendants = true) { contentDescription = StreakWeek.description(week) },
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        week.forEachIndexed { i, day ->
            val p = reveals.getOrNull(i)
            Column(
                Modifier.weight(1f).then(if (p != null) Modifier.reveal(p, rise = 14f, fromScale = 0.4f) else Modifier),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    day.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (day.isToday) FlameColors.bodyOn else NightColors.inkSoft,
                )
                val bg = when {
                    day.studied -> FlameColors.bodyOn
                    day.frozen -> NightColors.frozen
                    day.future -> NightColors.dayFuture
                    else -> NightColors.dayOff
                }
                Box(
                    Modifier.size(36.dp).background(bg, CircleShape)
                        .then(if (day.isToday) Modifier.border(2.5.dp, FlameColors.coreOn, CircleShape) else Modifier),
                    contentAlignment = Alignment.Center,
                ) {
                    when {
                        day.studied -> GameIconView(GameIcon.Check, size = 20.dp, colors = IconColors.flat(Color.White, Color.White))
                        day.frozen -> GameIconView(GameIcon.Snowflake, size = 20.dp)
                        else -> Unit
                    }
                }
            }
        }
    }
}
