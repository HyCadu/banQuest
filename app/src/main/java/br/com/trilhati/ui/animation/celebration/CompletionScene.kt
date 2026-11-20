package br.com.trilhati.ui.animation.celebration

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.trilhati.domain.celebration.CelebrationState
import br.com.trilhati.domain.celebration.CelebrationStep
import br.com.trilhati.domain.pace.PaceEngine
import br.com.trilhati.ui.animation.MotionMs
import br.com.trilhati.ui.animation.effects.AnimatedCounter
import br.com.trilhati.ui.animation.effects.ParticleBurst
import br.com.trilhati.ui.animation.effects.glow
import br.com.trilhati.ui.animation.motionOn
import br.com.trilhati.ui.animation.timeScale
import br.com.trilhati.ui.components.ChunkyButton
import br.com.trilhati.ui.components.ChunkyStyle
import br.com.trilhati.ui.icons.GameIcon
import br.com.trilhati.ui.icons.GameIconView
import br.com.trilhati.ui.mascot.Mascot
import br.com.trilhati.ui.mascot.MascotCharacter
import br.com.trilhati.ui.mascot.MascotPose
import br.com.trilhati.ui.theme.Spacing
import br.com.trilhati.ui.theme.Trilha
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Como um número da conclusão é escrito. */
enum class StatKind { Count, Percent, Fraction, Duration }

/** Um indicador da atividade terminada (valor real, nunca inventado). */
@Immutable
data class StatItem(
    val label: String,
    val value: Int,
    val kind: StatKind,
    val icon: GameIcon,
    /** Denominador de [StatKind.Fraction] ("8/10"). */
    val total: Int = 0,
) {
    fun format(v: Int): String = when (kind) {
        StatKind.Count -> v.toString()
        StatKind.Percent -> "$v%"
        StatKind.Fraction -> "$v/$total"
        StatKind.Duration -> PaceEngine.format(v.toLong())
    }
}

enum class RewardTone { Primary, Secondary, Warning, Success }

/** Ficha curta de recompensa ("+25", "Meta batida"); [description] é o que o TalkBack lê. */
@Immutable
data class RewardItem(val text: String, val icon: GameIcon, val tone: RewardTone, val description: String = text)

@Immutable
data class AchievementItem(val title: String, val description: String)

/**
 * Tudo que a conclusão de uma atividade mostra. Qualquer modalidade (lição, treino, revisão, prova anterior, simulado)
 * monta este objeto a partir do resultado já gravado e ganha a mesma celebração.
 */
@Immutable
data class CompletionData(
    val title: String,
    val subtitle: String,
    val note: String? = null,
    val pose: MascotPose = MascotPose.Happy,
    /** Nulo ou zero: sem etapa de XP. */
    val xp: Int? = null,
    val stats: List<StatItem> = emptyList(),
    val rewards: List<RewardItem> = emptyList(),
    val achievements: List<AchievementItem> = emptyList(),
)

/** Progresso de entrada de cada bloco da conclusão. */
@Stable
private class CompletionReveal(stats: Int, rewards: Int, achievements: Int, shown: (CelebrationStep) -> Boolean) {
    private fun a(on: Boolean) = Animatable(if (on) 1f else 0f)
    val mascot = a(shown(CelebrationStep.Completion))
    val title = a(shown(CelebrationStep.Completion))
    val stats = List(stats) { a(shown(CelebrationStep.Results)) }
    val extra = a(shown(CelebrationStep.Results))
    val xp = a(shown(CelebrationStep.XpReward))
    val rewards = List(rewards) { a(shown(CelebrationStep.XpReward)) }
    val achievements = List(achievements) { a(shown(CelebrationStep.Achievements)) }
    var xpBurst by mutableIntStateOf(0)
    var achievementBurst by mutableIntStateOf(0)

    suspend fun showAll() {
        (listOf(mascot, title, extra, xp) + stats + rewards + achievements).forEach { it.snapTo(1f) }
    }
}

/**
 * Conclusão de uma atividade, coreografada pelas etapas de [celebration]: o mascote e o título entram com mola, os
 * indicadores aparecem um a um com os números subindo até o valor real, o XP é revelado com brilho e partículas e as
 * conquistas entram por último. Cada etapa roda numa única corrotina presa à etapa (cancelável, sem atrasos soltos) e
 * avisa [onStepDone] ao terminar. Com [celebration] nulo, animação desligada ou etapa final, mostra tudo parado.
 *
 * Antes da chama da ofensiva a cena espera o "Continuar" (o aluno lê o resultado antes de acender a chama).
 *
 * @param extra conteúdo da modalidade que entra junto com os indicadores (resumo de tempo, questões para revisar).
 */
@Composable
fun CompletionScene(
    data: CompletionData,
    character: MascotCharacter,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    celebration: CelebrationState? = null,
    onStepDone: (CelebrationStep) -> Unit = {},
    onSkip: () -> Unit = {},
    animated: Boolean = true,
    extra: @Composable ColumnScope.() -> Unit = {},
) {
    val c = Trilha.colors
    val on = motionOn(animated) && celebration != null
    val step = celebration?.step ?: CelebrationStep.Finished
    val reveal = remember(data) {
        CompletionReveal(data.stats.size, data.rewards.size, data.achievements.size) { s ->
            !on || celebration.past(s)
        }
    }
    // Esperando o "Continuar" antes da chama.
    var holding by remember { mutableStateOf(false) }
    LaunchedEffect(step, on) {
        if (!on || celebration.finished || step == CelebrationStep.Streak) {
            reveal.showAll()
            return@LaunchedEffect
        }
        val k = celebration.level.timeScale()
        fun ms(base: Int) = (base * k).toLong()
        when (step) {
            CelebrationStep.Completion -> coroutineScope {
                launch { reveal.mascot.animateTo(1f, popSpring()) }
                delay(ms(160))
                reveal.title.animateTo(1f, enterSpring())
            }
            CelebrationStep.Results -> coroutineScope {
                reveal.stats.forEachIndexed { i, a ->
                    launch {
                        delay(ms(i * MotionMs.STAGGER))
                        a.animateTo(1f, enterSpring())
                    }
                }
                launch {
                    delay(ms(reveal.stats.size * MotionMs.STAGGER))
                    reveal.extra.animateTo(1f, enterSpring())
                }
                // Os contadores sobem enquanto os cartões entram; a etapa espera eles chegarem.
                delay(ms(reveal.stats.size * MotionMs.STAGGER + MotionMs.COUNTER))
            }
            CelebrationStep.XpReward -> coroutineScope {
                reveal.xpBurst++
                reveal.xp.animateTo(1f, popSpring())
                reveal.rewards.forEachIndexed { i, a ->
                    launch {
                        delay(ms(i * 90))
                        a.animateTo(1f, popSpring())
                    }
                }
                delay(ms(MotionMs.COUNTER))
            }
            CelebrationStep.Achievements -> coroutineScope {
                reveal.achievementBurst++
                reveal.achievements.forEachIndexed { i, a ->
                    launch {
                        delay(ms(i * 260))
                        a.animateTo(1f, popSpring())
                    }
                }
                delay(ms(700 + reveal.achievements.size * 260))
            }
            CelebrationStep.Streak, CelebrationStep.Finished -> Unit
        }
        if (celebration.next == CelebrationStep.Streak) holding = true else onStepDone(step)
    }
    val showButton = !on || celebration.finished || holding

    Box(modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Column(Modifier.fillMaxSize()) {
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = Spacing.screen, vertical = Spacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                Mascot(character, data.pose, Modifier.size(200.dp).reveal(reveal.mascot, rise = 40f, fromScale = 0.4f), animated = animated)
                Text(
                    data.title, Modifier.reveal(reveal.title, rise = 16f, fromScale = 0.6f),
                    style = MaterialTheme.typography.headlineLarge, color = c.primaryText, textAlign = TextAlign.Center,
                )
                Text(data.subtitle, Modifier.reveal(reveal.title, rise = 8f, fromScale = 1f), style = MaterialTheme.typography.bodyMedium, color = c.inkSoft)
                data.note?.let {
                    Text(it, Modifier.reveal(reveal.title, rise = 8f, fromScale = 1f), style = MaterialTheme.typography.bodyMedium, color = c.ink, textAlign = TextAlign.Center)
                }
                data.xp?.takeIf { it > 0 }?.let { xp ->
                    XpHero(xp, reveal, step, on, celebration)
                }
                if (data.stats.isNotEmpty()) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        data.stats.forEachIndexed { i, s ->
                            StatTile(
                                s, Modifier.weight(1f).reveal(reveal.stats[i]),
                                play = step == CelebrationStep.Results, motion = on,
                                startAtEnd = celebration == null || celebration.past(CelebrationStep.Results),
                            )
                        }
                    }
                }
                Column(Modifier.fillMaxWidth().reveal(reveal.extra, rise = 16f, fromScale = 0.96f), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    if (data.rewards.isNotEmpty()) {
                        FlowRow(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            data.rewards.forEachIndexed { i, r -> RewardChip(r, Modifier.reveal(reveal.rewards[i], rise = 10f, fromScale = 0.5f)) }
                        }
                    }
                    if (data.achievements.isNotEmpty()) {
                        Box {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                data.achievements.forEachIndexed { i, a ->
                                    AchievementReveal(a, reveal.achievements[i], on)
                                }
                            }
                            if (on) {
                                ParticleBurst(
                                    trigger = reveal.achievementBurst,
                                    colors = listOf(c.warning, c.primary, c.secondary, c.flame),
                                    modifier = Modifier.matchParentSize(),
                                    count = 28, power = 150.dp, durationMs = 1_100, gravity = 0.45f,
                                    origin = Offset(0.12f, 0.3f),
                                )
                            }
                        }
                    }
                    extra()
                }
            }
            if (showButton) {
                ChunkyButton(
                    "Continuar",
                    {
                        if (holding && celebration != null) {
                            holding = false
                            onStepDone(celebration.step)
                        } else {
                            onContinue()
                        }
                    },
                    Modifier.fillMaxWidth().padding(Spacing.screen),
                    ChunkyStyle.Primary,
                )
            }
        }
        if (on && !showButton) {
            Text(
                "Pular",
                Modifier.align(Alignment.TopEnd).padding(8.dp)
                    .semantics { role = Role.Button; contentDescription = "Pular a animação" }
                    .clickable(onClick = onSkip)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                style = MaterialTheme.typography.labelLarge, color = c.inkSoft,
            )
        }
    }
}

@Composable
private fun XpHero(xp: Int, reveal: CompletionReveal, step: CelebrationStep, on: Boolean, celebration: CelebrationState?) {
    val c = Trilha.colors
    val pulse = if (on) {
        rememberInfiniteTransition(label = "xpGlow").animateFloat(
            0.6f, 1f, infiniteRepeatable(tween(1_100, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "xpPulse",
        )
    } else {
        null
    }
    val shape = RoundedCornerShape(22.dp)
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(
            Modifier.size(width = 280.dp, height = 120.dp)
                .glow(c.warning.copy(alpha = 0.55f), { reveal.xp.value.coerceIn(0f, 1f) * (pulse?.value ?: 0.8f) }, radius = 0.6f),
        )
        Row(
            Modifier.reveal(reveal.xp, rise = 30f, fromScale = 0.5f)
                .background(c.warningSoft, shape).border(2.dp, c.warning, shape)
                .padding(horizontal = 22.dp, vertical = 12.dp)
                .semantics(mergeDescendants = true) { contentDescription = "$xp XP ganhos" },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GameIconView(GameIcon.Star, size = 40.dp)
            Column {
                AnimatedCounter(
                    xp, play = step == CelebrationStep.XpReward, style = MaterialTheme.typography.headlineLarge, color = c.warningText,
                    motion = on, startAtEnd = celebration == null || celebration.past(CelebrationStep.XpReward),
                    format = { "+$it XP" },
                )
                Text("XP ganho", style = MaterialTheme.typography.labelMedium, color = c.warningText)
            }
        }
        if (on) {
            ParticleBurst(
                trigger = reveal.xpBurst,
                colors = listOf(c.warning, c.flame, c.secondary, Color.White),
                modifier = Modifier.size(width = 320.dp, height = 220.dp),
                count = 36, power = 140.dp, durationMs = 1_000, gravity = 0.4f,
            )
        }
    }
}

@Composable
private fun StatTile(item: StatItem, modifier: Modifier, play: Boolean, motion: Boolean, startAtEnd: Boolean) {
    val c = Trilha.colors
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier.background(c.surface, shape).border(2.dp, c.outline, shape).padding(vertical = 12.dp, horizontal = 6.dp)
            .semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        GameIconView(item.icon, size = 26.dp)
        AnimatedCounter(
            item.value, play = play, style = MaterialTheme.typography.headlineSmall, color = c.ink,
            motion = motion, startAtEnd = startAtEnd, format = item::format,
        )
        Text(item.label, style = MaterialTheme.typography.labelSmall, color = c.inkSoft, textAlign = TextAlign.Center)
    }
}

@Composable
private fun RewardChip(r: RewardItem, modifier: Modifier) {
    val c = Trilha.colors
    val (bg, fg) = when (r.tone) {
        RewardTone.Primary -> c.primarySoft to c.primaryText
        RewardTone.Secondary -> c.secondarySoft to c.secondaryText
        RewardTone.Warning -> c.warningSoft to c.warningText
        RewardTone.Success -> c.successSoft to c.successText
    }
    Row(
        modifier.background(bg, RoundedCornerShape(50)).padding(start = 8.dp, end = 14.dp, top = 6.dp, bottom = 6.dp)
            .semantics(mergeDescendants = true) { contentDescription = r.description },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        GameIconView(r.icon, size = 24.dp)
        Text(r.text, style = MaterialTheme.typography.titleSmall, color = fg)
    }
}
