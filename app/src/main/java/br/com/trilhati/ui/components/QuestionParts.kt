package br.com.trilhati.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import br.com.trilhati.R
import br.com.trilhati.ui.icons.GameIcon
import br.com.trilhati.ui.icons.GameIconView
import br.com.trilhati.ui.icons.IconColors
import br.com.trilhati.ui.mascot.Mascot
import br.com.trilhati.ui.mascot.MascotCharacter
import br.com.trilhati.ui.mascot.MascotPose
import br.com.trilhati.ui.theme.CodeTextStyle
import br.com.trilhati.ui.theme.Spacing
import br.com.trilhati.ui.theme.Trilha

enum class OptionState { Idle, Selected, Correct, Wrong, Dimmed }

/** Alternativa de múltipla escolha (A a E), com lombada 3D e cores de acerto/erro. */
@Composable
fun OptionCard(
    letter: String,
    text: String,
    state: OptionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val c = Trilha.colors
    val (face, border, edge, badge, badgeText) = when (state) {
        OptionState.Idle -> OptionColors(c.surface, c.outline, c.outline, c.surfaceAlt, c.primaryText)
        OptionState.Selected -> OptionColors(c.primarySoft, c.primary, c.primary, c.primary, c.onPrimary)
        OptionState.Correct -> OptionColors(c.successSoft, c.success, c.success, c.success, c.onSuccess)
        OptionState.Wrong -> OptionColors(c.errorSoft, c.error, c.error, c.error, c.onError)
        OptionState.Dimmed -> OptionColors(c.surface, c.outline, c.outline, c.surfaceAlt, c.inkSoft)
    }
    val stateText = when (state) {
        OptionState.Correct -> stringResource(R.string.option_correct)
        OptionState.Wrong -> stringResource(R.string.option_wrong)
        OptionState.Selected -> stringResource(R.string.option_selected)
        else -> null
    }
    val description = stringResource(R.string.option_description, letter)
    val shape = RoundedCornerShape(18.dp)
    val scale by animateFloatAsState(
        if (state == OptionState.Correct || state == OptionState.Wrong) 1.02f else 1f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "optionScale",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .semantics(mergeDescendants = true) {
                contentDescription = description
                role = Role.RadioButton
                selected = state == OptionState.Selected
                if (stateText != null) stateDescription = stateText
            }
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
    ) {
        Box(Modifier.matchParentSize().padding(top = 4.dp).background(edge, shape))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp)
                .background(face, shape)
                .border(2.dp, border, shape)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(34.dp).background(badge, CircleShape), contentAlignment = Alignment.Center) {
                when (state) {
                    OptionState.Correct -> GameIconView(GameIcon.Check, size = 20.dp, colors = IconColors.flat(badgeText, badge))
                    OptionState.Wrong -> GameIconView(GameIcon.Close, size = 18.dp, colors = IconColors.flat(badgeText, badge))
                    else -> Text(letter, style = MaterialTheme.typography.titleMedium, color = badgeText)
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = if (state == OptionState.Dimmed) c.inkSoft else c.ink,
            )
        }
    }
}

private data class OptionColors(
    val face: Color,
    val border: Color,
    val edge: Color,
    val badge: Color,
    val badgeText: Color,
)

/** Painel que sobe da base após Verificar: verde (acertou) ou vermelho (errou), com explicação. */
@Composable
fun FeedbackPanel(
    correct: Boolean,
    title: String,
    message: String,
    character: MascotCharacter,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    animated: Boolean = true,
    partial: Boolean = false,
    pose: MascotPose? = null,
) {
    val c = Trilha.colors
    // partial: completou corrigindo um erro (âmbar: nem acerto limpo, nem erro).
    val background = when { partial -> c.warningSoft; correct -> c.successSoft; else -> c.errorSoft }
    val titleColor = when { partial -> c.warningText; correct -> c.successText; else -> c.errorText }
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(Modifier.padding(horizontal = Spacing.md, vertical = Spacing.md).animateContentSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Mascot(
                    character = character,
                    pose = pose ?: if (correct || partial) MascotPose.Happy else MascotPose.Sad,
                    modifier = Modifier.size(72.dp),
                    animated = animated,
                    decorative = true,
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.headlineSmall, color = titleColor)
                    Text(message, style = MaterialTheme.typography.bodyMedium, color = c.ink)
                }
            }
            Spacer(Modifier.size(Spacing.sm))
            ChunkyButton(
                text = stringResource(R.string.feedback_continue),
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                style = when { partial -> ChunkyStyle.Warning; correct -> ChunkyStyle.Success; else -> ChunkyStyle.Error },
            )
        }
    }
}

/**
 * Selo discreto exigido pelo CLAUDE.md: toda questão gerada por IA aparece como "não revisada". Nas questões de
 * provas anteriores ([explanationOnly]) o enunciado é o original, e o selo avisa que só a explicação não foi revisada.
 */
@Composable
fun UnreviewedBadge(modifier: Modifier = Modifier, explanationOnly: Boolean = false) {
    val c = Trilha.colors
    val description = stringResource(if (explanationOnly) R.string.unreviewed_explanation_badge_description else R.string.unreviewed_badge_description)
    Surface(
        modifier = modifier.semantics(mergeDescendants = true) { contentDescription = description },
        shape = RoundedCornerShape(50),
        color = c.warningSoft,
        border = BorderStroke(1.5.dp, c.warning),
    ) {
        Row(
            modifier = Modifier.padding(start = 6.dp, end = 10.dp, top = 3.dp, bottom = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GameIconView(GameIcon.Info, size = 16.dp)
            Spacer(Modifier.width(4.dp))
            Text(
                stringResource(if (explanationOnly) R.string.unreviewed_explanation_badge else R.string.unreviewed_badge),
                style = MaterialTheme.typography.labelSmall,
                color = c.warningText,
            )
        }
    }
}

/** Etiqueta das questões de provas anteriores: banca, órgão, ano e número (ex.: "FGV · Banestes 2021 · questão 48"). */
@Composable
fun ExamRefChip(label: String, modifier: Modifier = Modifier) {
    val c = Trilha.colors
    val description = stringResource(R.string.exam_ref_description, label)
    Surface(
        modifier = modifier.semantics(mergeDescendants = true) { contentDescription = description },
        shape = RoundedCornerShape(50),
        color = c.secondarySoft,
        border = BorderStroke(1.5.dp, c.secondary),
    ) {
        Row(
            modifier = Modifier.padding(start = 6.dp, end = 10.dp, top = 3.dp, bottom = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GameIconView(GameIcon.Book, size = 16.dp)
            Spacer(Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = c.secondaryText)
        }
    }
}

/** Combo de acertos seguidos (aparece a partir de 3). */
@Composable
fun ComboBadge(count: Int, modifier: Modifier = Modifier) {
    val c = Trilha.colors
    val pop by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(Spring.DampingRatioHighBouncy, Spring.StiffnessMedium),
        label = "comboPop$count",
    )
    Surface(
        modifier = modifier.scale(0.9f + 0.1f * pop),
        shape = RoundedCornerShape(50),
        color = c.warning,
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 14.dp, top = 5.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GameIconView(GameIcon.Flame, size = 24.dp)
            Spacer(Modifier.width(6.dp))
            Text(
                stringResource(R.string.combo_label, count).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = c.onWarning,
            )
        }
    }
}

/** Pílula de XP ganho ("+10 XP"). */
@Composable
fun XpPill(text: String, modifier: Modifier = Modifier) {
    val c = Trilha.colors
    Surface(modifier = modifier, shape = RoundedCornerShape(50), color = c.secondarySoft, border = BorderStroke(1.5.dp, c.secondary)) {
        Text(text, Modifier.padding(horizontal = 12.dp, vertical = 4.dp), style = MaterialTheme.typography.labelLarge, color = c.secondaryText)
    }
}

/** Bloco de código/SQL com fonte monoespaçada e rolagem horizontal. */
@Composable
fun CodeBlock(code: String, modifier: Modifier = Modifier) {
    val c = Trilha.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(c.surfaceAlt, RoundedCornerShape(16.dp))
            .border(2.dp, c.outline, RoundedCornerShape(16.dp))
            .horizontalScroll(rememberScrollState())
            .padding(14.dp),
    ) {
        Text(code, style = CodeTextStyle, color = c.ink)
    }
}

/** Mascote "falando": balão de fala com o personagem ao lado. */
@Composable
fun MascotSays(
    character: MascotCharacter,
    pose: MascotPose,
    text: String,
    modifier: Modifier = Modifier,
    animated: Boolean = true,
) {
    val c = Trilha.colors
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Mascot(character, pose, Modifier.size(96.dp), animated = animated)
        Box(Modifier.weight(1f).padding(start = 6.dp)) {
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (-3).dp)
                    .size(14.dp)
                    .rotate(45f)
                    .background(c.surface)
                    .border(2.dp, c.outline),
            )
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = c.surface,
                border = BorderStroke(2.dp, c.outline),
                modifier = Modifier.padding(start = 4.dp),
            ) {
                Text(text, Modifier.padding(14.dp), style = MaterialTheme.typography.bodyLarge, color = c.ink)
            }
        }
    }
}
