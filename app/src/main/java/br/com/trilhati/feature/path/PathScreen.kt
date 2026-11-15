package br.com.trilhati.feature.path

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import br.com.trilhati.domain.hearts.HeartsEngine
import br.com.trilhati.domain.model.CourseUnit
import br.com.trilhati.domain.pace.PaceEngine
import br.com.trilhati.domain.progress.LessonStatus
import br.com.trilhati.feature.app.AppActions
import br.com.trilhati.feature.app.AppState
import br.com.trilhati.ui.components.ChunkyButton
import br.com.trilhati.ui.components.ChunkyStyle
import br.com.trilhati.ui.components.GameProgressBar
import br.com.trilhati.ui.components.NodeState
import br.com.trilhati.ui.components.PathNode
import br.com.trilhati.ui.components.StreakCalendar
import br.com.trilhati.ui.components.TopStatusBar
import br.com.trilhati.ui.components.TrilhaDialog
import br.com.trilhati.ui.components.UnitBanner
import br.com.trilhati.ui.icons.GameIcon
import br.com.trilhati.ui.icons.GameIconView
import br.com.trilhati.ui.mascot.Mascot
import br.com.trilhati.ui.mascot.MascotCharacter
import br.com.trilhati.ui.mascot.MascotPose
import br.com.trilhati.ui.theme.Spacing
import br.com.trilhati.ui.theme.Trilha

/** Posição horizontal (em dp) dos nós, em zigue-zague, repetindo a cada 8 lições. */
private val ZigZag = listOf(0, 52, 80, 52, 0, -52, -80, -52)

private sealed interface PathDialog {
    data object Streak : PathDialog
    data object Hearts : PathDialog
    data object Exam : PathDialog
    data class UnitGuide(val unitId: String) : PathDialog
}

/** Aba Trilha: faixa de status, meta do dia e a trilha de unidades e lições. */
@Composable
fun PathContent(
    state: AppState,
    nowMs: Long,
    actions: AppActions,
    modifier: Modifier = Modifier,
    animated: Boolean = true,
    showReminderPrompt: Boolean = false,
) {
    var dialog by remember { mutableStateOf<PathDialog?>(null) }
    val stats = state.snapshot.stats
    val character = MascotCharacter.entries.firstOrNull { it.name == state.snapshot.profile.character } ?: MascotCharacter.Guaiamum
    val hearts = state.hearts(nowMs)

    Column(modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = Spacing.screen).padding(top = Spacing.sm), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TopStatusBar(
                streak = state.streakCount, gems = stats.gems, hearts = hearts, daysToExam = state.daysToExam,
                showHearts = !state.settings.noLives,
                studiedToday = state.todayLog.lessons > 0,
                onStreakClick = { dialog = PathDialog.Streak },
                onGemsClick = actions.openShop,
                onHeartsClick = { dialog = PathDialog.Hearts },
                onExamClick = { dialog = PathDialog.Exam },
            )
            DailyGoalBar(state.todayLog.xp, state.snapshot.profile.dailyGoalXp)
            if (showReminderPrompt) ReminderPrompt(state, character, actions, animated)
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = Spacing.screen, vertical = Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            state.course.forEach { unit ->
                unitItems(unit, state, character, actions, animated, onOpenGuide = { dialog = PathDialog.UnitGuide(unit.id) })
            }
            item(key = "end") {
                Text(
                    "Mais unidades chegam conforme o conteúdo for liberado.",
                    modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.lg),
                    style = MaterialTheme.typography.bodySmall,
                    color = Trilha.colors.inkSoft,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }

    when (val d = dialog) {
        PathDialog.Streak -> StreakDialog(state, onDismiss = { dialog = null })
        PathDialog.Hearts -> HeartsDialog(state, nowMs, actions, onDismiss = { dialog = null })
        PathDialog.Exam -> ExamDialog(state, onDismiss = { dialog = null })
        is PathDialog.UnitGuide -> state.course.firstOrNull { it.id == d.unitId }?.let { UnitGuideDialog(it, state, onDismiss = { dialog = null }) }
        null -> Unit
    }
}

@Composable
private fun DailyGoalBar(xpToday: Int, goal: Int) {
    val c = Trilha.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        GameIconView(GameIcon.Star, size = 22.dp)
        GameProgressBar((xpToday.toFloat() / goal).coerceIn(0f, 1f), Modifier.weight(1f), color = c.warning, height = 12.dp)
        Text(
            if (xpToday >= goal) "Meta batida!" else "$xpToday/$goal XP",
            style = MaterialTheme.typography.labelMedium,
            color = if (xpToday >= goal) c.successText else c.inkSoft,
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.unitItems(
    unit: CourseUnit,
    state: AppState,
    character: MascotCharacter,
    actions: AppActions,
    animated: Boolean,
    onOpenGuide: () -> Unit,
) {
    val locked = !unit.hasContent
    val done = unit.lessons.count { state.statuses[it.id] == LessonStatus.Done || state.statuses[it.id] == LessonStatus.Perfect }
    val subtitle = when {
        locked -> "Conteúdo chegando em breve"
        else -> "$done de ${unit.lessons.size} lições concluídas"
    }
    stickyHeader(key = "banner_${unit.id}") {
        // Cabeçalho fixo da unidade (PLAN.md §7): fica no topo enquanto os nós rolam por baixo.
        Box(Modifier.fillMaxWidth().background(Trilha.colors.background).padding(top = Spacing.sm, bottom = Spacing.sm)) {
            UnitBanner(unit.order, unit.title, subtitle, locked = locked, onClick = onOpenGuide)
        }
    }
    itemsIndexed(unit.lessons, key = { _, l -> l.id }) { index, lesson ->
        val status = state.statuses[lesson.id] ?: LessonStatus.Locked
        val offsetDp = ZigZag[index % ZigZag.size]
        Box(Modifier.fillMaxWidth().height(if (status == LessonStatus.Available) 144.dp else 104.dp), contentAlignment = Alignment.Center) {
            val nodeState = when (status) {
                LessonStatus.Locked -> NodeState.Locked
                LessonStatus.Available -> NodeState.Current
                LessonStatus.Done -> NodeState.Done
                LessonStatus.Perfect -> NodeState.Perfect
            }
            PathNode(
                state = nodeState,
                label = "Lição ${lesson.order}, ${lesson.title}",
                onClick = {
                    if (status == LessonStatus.Locked) {
                        actions.message(if (!lesson.hasContent) "\"${lesson.title}\" chega em um próximo pacote de conteúdo." else "Conclua a lição anterior para liberar esta.")
                    } else {
                        actions.openLesson(lesson.id)
                    }
                },
                modifier = Modifier.offset(x = offsetDp.dp),
                icon = GameIcon.Star,
            )
            if (status == LessonStatus.Available) {
                val onLeft = offsetDp >= 0
                Mascot(
                    character, MascotPose.Happy,
                    Modifier.size(92.dp).align(if (onLeft) Alignment.CenterStart else Alignment.CenterEnd).padding(horizontal = 4.dp),
                    animated = animated, decorative = true,
                )
            }
        }
    }
    item(key = "chest_${unit.id}") {
        val complete = unit.id in state.completedUnitIds
        val opened = unit.id in state.snapshot.shop.claimedUnitChests
        UnitChest(complete, opened, onClick = {
            when {
                !complete -> actions.message("Conclua todas as lições da unidade para abrir o baú.")
                opened -> actions.message("Você já abriu o baú desta unidade.")
                else -> actions.claimUnitChest(unit.id)
            }
        })
    }
}

@Composable
private fun UnitChest(complete: Boolean, opened: Boolean, onClick: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "chest")
    val bounce by transition.animateFloat(
        0f, -8f, infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "bounce",
    )
    val ready = complete && !opened
    val description = when { opened -> "Baú da unidade, já aberto"; ready -> "Baú da unidade, pronto para abrir"; else -> "Baú da unidade, bloqueado" }
    Box(Modifier.fillMaxWidth().height(110.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .offset { IntOffset(0, if (ready) bounce.dp.roundToPx() else 0) }
                .alpha(if (ready) 1f else 0.45f)
                .semantics { contentDescription = description; role = Role.Button }
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            GameIconView(GameIcon.Chest, size = 72.dp)
        }
        if (ready) {
            Text(
                "ABRIR BAÚ", Modifier.align(Alignment.BottomCenter),
                style = MaterialTheme.typography.labelLarge, color = Trilha.colors.primaryText,
            )
        }
    }
}

@Composable
private fun StreakDialog(state: AppState, onDismiss: () -> Unit) {
    val stats = state.snapshot.stats
    TrilhaDialog(
        onDismiss = onDismiss,
        title = if (state.streakCount > 0) "${state.streakCount} ${if (state.streakCount == 1) "dia" else "dias"} de ofensiva" else "Sem ofensiva ainda",
        content = {
            Text(
                when {
                    state.streakCount == 0 -> "Conclua uma lição hoje para começar sua ofensiva."
                    state.todayLog.lessons > 0 -> "Hoje já está garantido! Volte amanhã para continuar."
                    else -> "Conclua uma lição hoje para não perder a ofensiva."
                },
                style = MaterialTheme.typography.bodyMedium, color = Trilha.colors.ink,
            )
            StreakCalendar(state.snapshot.dailyLogs, state.today)
            Text(
                "Recorde: ${stats.streak.longest} dias · Congelamentos: ${stats.streak.freezes}/2",
                style = MaterialTheme.typography.labelMedium, color = Trilha.colors.inkSoft,
            )
        },
        actions = { ChunkyButton("Fechar", onDismiss, Modifier.fillMaxWidth()) },
    )
}

@Composable
private fun HeartsDialog(state: AppState, nowMs: Long, actions: AppActions, onDismiss: () -> Unit) {
    val hearts = state.hearts(nowMs)
    val wait = state.heartsMsUntilNext(nowMs)
    TrilhaDialog(
        onDismiss = onDismiss,
        title = "$hearts de ${HeartsEngine.MAX} corações",
        content = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(HeartsEngine.MAX) { i -> GameIconView(GameIcon.Heart, size = 30.dp, modifier = Modifier.alpha(if (i < hearts) 1f else 0.25f)) }
            }
            Text(
                if (wait == null) "Corações cheios! Cada erro em uma lição custa 1."
                else "Próximo coração em ${PaceEngine.format(wait)}. Eles voltam 1 a cada 30 minutos.",
                style = MaterialTheme.typography.bodyMedium, color = Trilha.colors.ink,
            )
        },
        actions = {
            if (hearts < HeartsEngine.MAX) {
                ChunkyButton("Recarregar (350 gemas)", { actions.buy(br.com.trilhati.domain.progress.ShopItem.RefillHearts); onDismiss() }, Modifier.fillMaxWidth(), ChunkyStyle.Secondary, icon = GameIcon.Gem)
                ChunkyButton("Praticar erros", { onDismiss(); actions.openErrors() }, Modifier.fillMaxWidth(), ChunkyStyle.Neutral)
            }
            ChunkyButton("Fechar", onDismiss, Modifier.fillMaxWidth())
        },
    )
}

@Composable
private fun ReminderPrompt(state: AppState, character: MascotCharacter, actions: AppActions, animated: Boolean) {
    val c = Trilha.colors
    val time = state.settings.reminderTime
    Column(
        Modifier.fillMaxWidth().background(c.surface, androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
            .border(2.dp, c.primary, androidx.compose.foundation.shape.RoundedCornerShape(20.dp)).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Mascot(character, MascotPose.Sleepy, Modifier.size(56.dp), animated = animated, decorative = true)
            Text(
                "Quer que ${br.com.trilhati.domain.notify.ReminderTexts.withArticle(character.name)} te lembre de estudar às %02d:%02d?".format(time.hour, time.minute),
                style = MaterialTheme.typography.titleSmall, color = c.ink, modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChunkyButton("Ativar lembrete", actions.enableReminders, Modifier.weight(1f), ChunkyStyle.Primary, minHeight = 44.dp)
            ChunkyButton("Agora não", actions.dismissReminderPrompt, Modifier.weight(1f), ChunkyStyle.Neutral, minHeight = 44.dp)
        }
    }
}

@Composable
private fun ExamDialog(state: AppState, onDismiss: () -> Unit) {
    val c = Trilha.colors
    val lessonsWithContent = state.course.sumOf { u -> u.lessons.count { it.hasContent } }
    val allLessons = state.course.sumOf { it.lessons.size }
    val done = state.snapshot.lessonProgress.values.count { it.completed }
    val days = state.daysToExam
    val remainingAll = (allLessons - done).coerceAtLeast(0)
    val perWeek = if (days <= 0) remainingAll else kotlin.math.ceil(remainingAll * 7.0 / days).toInt()
    TrilhaDialog(
        onDismiss = onDismiss,
        title = if (days == 0) "A prova é hoje!" else "Faltam $days ${if (days == 1) "dia" else "dias"}",
        content = {
            Text(
                "Prova do Banestes: 21/02/2027 (domingo), de manhã. São 80 questões em 4 horas, 3:00 por questão em média.",
                style = MaterialTheme.typography.bodyMedium, color = c.ink,
            )
            Text(
                "Você concluiu $done de $allLessons lições da trilha ($lessonsWithContent já têm questões). " +
                    "Para fechar a trilha antes da prova, mantenha cerca de $perWeek ${if (perWeek == 1) "lição" else "lições"} por semana.",
                style = MaterialTheme.typography.bodyMedium, color = c.inkSoft,
            )
        },
        actions = { ChunkyButton("Bora!", onDismiss, Modifier.fillMaxWidth()) },
    )
}

@Composable
private fun UnitGuideDialog(unit: CourseUnit, state: AppState, onDismiss: () -> Unit) {
    val c = Trilha.colors
    TrilhaDialog(
        onDismiss = onDismiss,
        title = "Unidade ${unit.order}: ${unit.title}",
        content = {
            Column(
                Modifier.fillMaxWidth().heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (!unit.hasContent) {
                    Text("As questões desta unidade ainda não chegaram. As lições previstas são:", style = MaterialTheme.typography.bodyMedium, color = c.inkSoft)
                }
                unit.lessons.forEach { lesson ->
                    val status = state.statuses[lesson.id] ?: LessonStatus.Locked
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        when (status) {
                            LessonStatus.Perfect -> GameIconView(GameIcon.Crown, size = 22.dp)
                            LessonStatus.Done -> GameIconView(GameIcon.Check, size = 22.dp, colors = br.com.trilhati.ui.icons.IconColors.flat(c.secondary, c.secondaryEdge))
                            LessonStatus.Available -> GameIconView(GameIcon.Star, size = 22.dp)
                            LessonStatus.Locked -> GameIconView(GameIcon.Lock, size = 22.dp, colors = br.com.trilhati.ui.icons.IconColors(c.onLocked, c.locked, c.lockedEdge))
                        }
                        Text("${lesson.order}. ${lesson.title}", style = MaterialTheme.typography.bodyMedium, color = c.ink, modifier = Modifier.weight(1f))
                        if (lesson.hasContent) Text("${lesson.questions.size} q.", style = MaterialTheme.typography.labelSmall, color = c.inkSoft)
                    }
                }
                if (unit.editalTopics.isNotEmpty()) {
                    Text("Tópicos do edital (Anexo IV, cargo 04)", style = MaterialTheme.typography.titleSmall, color = c.primaryText, modifier = Modifier.padding(top = 6.dp))
                    Text(unit.editalTopics.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = c.inkSoft)
                }
            }
        },
        actions = { ChunkyButton("Fechar", onDismiss, Modifier.fillMaxWidth()) },
    )
}
