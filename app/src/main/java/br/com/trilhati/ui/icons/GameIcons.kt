package br.com.trilhati.ui.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Ícones de jogo desenhados em código (grade 48x48), originais e consistentes com os mascotes. */
enum class GameIcon {
    Flame, Gem, Heart, Star, Clock, Lock, Check, Crown, Chest, Trophy, House, Target, Bag, Person, Info, Book, Close, Snowflake,
}

/** Cores de um ícone: preenchimento principal, detalhe e contorno. */
class IconColors(val main: Color, val accent: Color, val outline: Color) {
    companion object {
        /** Silhueta de uma cor só (ex.: abas inativas). */
        fun flat(fill: Color, outline: Color) = IconColors(fill, fill, outline)
    }
}

private val InkIcon = Color(0xFF2B1A5E)

private fun GameIcon.defaults(): IconColors = when (this) {
    GameIcon.Flame -> IconColors(Color(0xFFFF7A1A), Color(0xFFFFD84D), InkIcon)
    GameIcon.Gem -> IconColors(Color(0xFF2FA8FF), Color(0xFF8FD6FF), InkIcon)
    GameIcon.Heart -> IconColors(Color(0xFFFF4D6D), Color(0xFFFFA3B5), InkIcon)
    GameIcon.Star -> IconColors(Color(0xFFFFB000), Color(0xFFFFE08A), InkIcon)
    GameIcon.Clock -> IconColors(Color(0xFFFFFFFF), Color(0xFF6B3FD6), InkIcon)
    GameIcon.Lock -> IconColors(Color(0xFF9A8FC0), Color(0xFF2B1A5E), InkIcon)
    GameIcon.Check -> IconColors(Color(0xFFFFFFFF), Color(0xFFFFFFFF), InkIcon)
    GameIcon.Crown -> IconColors(Color(0xFFFFB000), Color(0xFFFF4D6D), InkIcon)
    GameIcon.Chest -> IconColors(Color(0xFFB8702F), Color(0xFFFFC53D), InkIcon)
    GameIcon.Trophy -> IconColors(Color(0xFFFFB000), Color(0xFFFFE08A), InkIcon)
    GameIcon.House -> IconColors(Color(0xFF8B5CF6), Color(0xFF19D3C5), InkIcon)
    GameIcon.Target -> IconColors(Color(0xFF19D3C5), Color(0xFFFF4D6D), InkIcon)
    GameIcon.Bag -> IconColors(Color(0xFF19D3C5), Color(0xFFFFB000), InkIcon)
    GameIcon.Person -> IconColors(Color(0xFFFF8FB0), Color(0xFF8B5CF6), InkIcon)
    GameIcon.Info -> IconColors(Color(0xFFFFB000), Color(0xFF2B1A5E), InkIcon)
    GameIcon.Book -> IconColors(Color(0xFF6B3FD6), Color(0xFFFFFFFF), InkIcon)
    GameIcon.Close -> IconColors(Color(0xFFFFFFFF), Color(0xFFFFFFFF), InkIcon)
    GameIcon.Snowflake -> IconColors(Color(0xFF8FD6FF), Color(0xFFFFFFFF), InkIcon)
}

/**
 * Desenha um [GameIcon]. Passe [colors] para recolorir (ex.: [IconColors.flat] em abas inativas).
 * Sem [contentDescription], o ícone é tratado como decorativo.
 */
@Composable
fun GameIconView(
    icon: GameIcon,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    colors: IconColors = icon.defaults(),
    contentDescription: String? = null,
) {
    val semanticsModifier = if (contentDescription == null) {
        Modifier.clearAndSetSemantics { }
    } else {
        Modifier.semantics { this.contentDescription = contentDescription }
    }
    Canvas(modifier.size(size).then(semanticsModifier)) {
        scale(this.size.minDimension / 48f, pivot = Offset.Zero) { drawGameIcon(icon, colors) }
    }
}

private const val LINE = 3f

private fun DrawScope.shape(path: Path, fill: Color, outline: Color) {
    drawPath(path, fill)
    drawPath(path, outline, style = Stroke(LINE, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun DrawScope.stroked(path: Path, color: Color, outline: Color, width: Float) {
    drawPath(path, outline, style = Stroke(width + LINE * 2, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(path, color, style = Stroke(width, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun DrawScope.roundRectShape(r: Rect, radius: Float, fill: Color, outline: Color) {
    val p = Path().apply { addRoundRect(RoundRect(r, CornerRadius(radius, radius))) }
    shape(p, fill, outline)
}

private fun DrawScope.circleShape(c: Offset, r: Float, fill: Color, outline: Color) {
    drawCircle(fill, r, c)
    drawCircle(outline, r, c, style = Stroke(LINE))
}

private fun polygon(vararg pts: Pair<Float, Float>): Path = Path().apply {
    pts.forEachIndexed { i, (x, y) -> if (i == 0) moveTo(x, y) else lineTo(x, y) }
    close()
}

private fun DrawScope.drawGameIcon(icon: GameIcon, c: IconColors) {
    val o = c.outline
    when (icon) {
        GameIcon.Flame -> {
            val outer = Path().apply {
                moveTo(24f, 3f)
                cubicTo(27f, 12f, 40f, 18f, 40f, 31f)
                cubicTo(40f, 40f, 33f, 45f, 24f, 45f)
                cubicTo(15f, 45f, 8f, 40f, 8f, 31f)
                cubicTo(8f, 25f, 12f, 21f, 15f, 18f)
                cubicTo(16f, 23f, 19f, 24f, 20f, 23f)
                cubicTo(19f, 15f, 21f, 8f, 24f, 3f)
                close()
            }
            shape(outer, c.main, o)
            val inner = Path().apply {
                moveTo(24f, 24f)
                cubicTo(27f, 28f, 32f, 30f, 32f, 35f)
                cubicTo(32f, 40f, 28f, 42f, 24f, 42f)
                cubicTo(20f, 42f, 16f, 40f, 16f, 35f)
                cubicTo(16f, 31f, 22f, 29f, 24f, 24f)
                close()
            }
            drawPath(inner, c.accent)
        }

        GameIcon.Gem -> {
            val body = polygon(14f to 8f, 34f to 8f, 44f to 19f, 24f to 43f, 4f to 19f)
            shape(body, c.main, o)
            drawPath(polygon(14f to 8f, 34f to 8f, 44f to 19f, 4f to 19f), c.accent)
            val facets = Path().apply {
                moveTo(4f, 19f); lineTo(44f, 19f)
                moveTo(14f, 8f); lineTo(18f, 19f); lineTo(24f, 43f)
                moveTo(34f, 8f); lineTo(30f, 19f); lineTo(24f, 43f)
            }
            drawPath(facets, o, style = Stroke(2.2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawPath(body, o, style = Stroke(LINE, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }

        GameIcon.Heart -> {
            val heart = Path().apply {
                moveTo(24f, 43f)
                cubicTo(6f, 31f, 3f, 21f, 3f, 14f)
                cubicTo(3f, 8f, 8f, 4f, 14f, 4f)
                cubicTo(19f, 4f, 22f, 6f, 24f, 10f)
                cubicTo(26f, 6f, 29f, 4f, 34f, 4f)
                cubicTo(40f, 4f, 45f, 8f, 45f, 14f)
                cubicTo(45f, 21f, 42f, 31f, 24f, 43f)
                close()
            }
            shape(heart, c.main, o)
            drawOval(c.accent, Offset(9f, 9f), Size(8f, 5f))
        }

        GameIcon.Star -> {
            val star = Path().apply {
                for (i in 0 until 10) {
                    val r = if (i % 2 == 0) 21f else 9.5f
                    val a = (-90f + i * 36f) * PI.toFloat() / 180f
                    val x = 24f + r * cos(a)
                    val y = 25f + r * sin(a)
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            shape(star, c.main, o)
            drawCircle(c.accent, 2.6f, Offset(19f, 20f))
        }

        GameIcon.Clock -> {
            circleShape(Offset(24f, 24f), 19f, c.main, o)
            drawLine(c.accent, Offset(24f, 24f), Offset(24f, 12f), 4f, StrokeCap.Round)
            drawLine(c.accent, Offset(24f, 24f), Offset(33f, 28f), 4f, StrokeCap.Round)
            drawCircle(c.accent, 2.5f, Offset(24f, 24f))
        }

        GameIcon.Lock -> {
            val shackle = Path().apply {
                moveTo(16f, 22f); lineTo(16f, 15f)
                cubicTo(16f, 5f, 32f, 5f, 32f, 15f)
                lineTo(32f, 22f)
            }
            stroked(shackle, c.main, o, 4f)
            roundRectShape(Rect(9f, 21f, 39f, 43f), 6f, c.main, o)
            circleShape(Offset(24f, 30f), 3.2f, c.accent, o)
            drawLine(c.accent, Offset(24f, 31f), Offset(24f, 37f), 3.4f, StrokeCap.Round)
        }

        GameIcon.Check -> {
            val p = Path().apply { moveTo(11f, 25f); lineTo(20f, 34f); lineTo(37f, 14f) }
            stroked(p, c.main, o, 7f)
        }

        GameIcon.Snowflake -> {
            val arms = Path().apply {
                for (deg in listOf(0f, 60f, 120f)) {
                    val a = deg * PI.toFloat() / 180f
                    moveTo(24f - 19f * cos(a), 24f - 19f * sin(a))
                    lineTo(24f + 19f * cos(a), 24f + 19f * sin(a))
                }
            }
            stroked(arms, c.main, o, 4.5f)
            circleShape(Offset(24f, 24f), 5f, c.accent, o)
        }

        GameIcon.Close -> {
            val p = Path().apply { moveTo(13f, 13f); lineTo(35f, 35f); moveTo(35f, 13f); lineTo(13f, 35f) }
            stroked(p, c.main, o, 7f)
        }

        GameIcon.Crown -> {
            val crown = polygon(6f to 36f, 3f to 13f, 15f to 24f, 24f to 7f, 33f to 24f, 45f to 13f, 42f to 36f)
            shape(crown, c.main, o)
            roundRectShape(Rect(6f, 35f, 42f, 43f), 3f, c.main, o)
            circleShape(Offset(24f, 29f), 3.6f, c.accent, o)
        }

        GameIcon.Chest -> {
            roundRectShape(Rect(5f, 22f, 43f, 43f), 4f, c.main, o)
            val lid = Path().apply {
                moveTo(5f, 24f); lineTo(5f, 17f)
                cubicTo(5f, 6f, 43f, 6f, 43f, 17f)
                lineTo(43f, 24f)
                close()
            }
            shape(lid, c.main, o)
            drawRect(c.accent, Offset(5f, 22f), Size(38f, 5f))
            drawLine(o, Offset(5f, 22f), Offset(43f, 22f), 2.2f)
            drawRect(c.accent, Offset(11f, 8f), Size(5f, 34f))
            drawRect(c.accent, Offset(32f, 8f), Size(5f, 34f))
            roundRectShape(Rect(20f, 22f, 28f, 33f), 3f, c.accent, o)
        }

        GameIcon.Trophy -> {
            val hl = Path().apply { moveTo(14f, 10f); cubicTo(2f, 8f, 4f, 24f, 16f, 24f) }
            val hr = Path().apply { moveTo(34f, 10f); cubicTo(46f, 8f, 44f, 24f, 32f, 24f) }
            stroked(hl, c.main, o, 3.5f)
            stroked(hr, c.main, o, 3.5f)
            roundRectShape(Rect(21f, 28f, 27f, 36f), 2f, c.main, o)
            roundRectShape(Rect(13f, 35f, 35f, 43f), 3f, c.main, o)
            val cup = Path().apply {
                moveTo(12f, 5f); lineTo(36f, 5f); lineTo(36f, 19f)
                cubicTo(36f, 30f, 12f, 30f, 12f, 19f)
                close()
            }
            shape(cup, c.main, o)
            drawOval(c.accent, Offset(16f, 9f), Size(5f, 12f))
        }

        GameIcon.House -> {
            val body = polygon(10f to 23f, 38f to 23f, 38f to 43f, 10f to 43f)
            shape(body, c.main, o)
            roundRectShape(Rect(19f, 29f, 29f, 43f), 3f, c.accent, o)
            val roof = polygon(2f to 25f, 24f to 5f, 46f to 25f, 40f to 25f, 24f to 12f, 8f to 25f)
            shape(roof, c.accent, o)
        }

        GameIcon.Target -> {
            circleShape(Offset(24f, 24f), 20f, c.main, o)
            circleShape(Offset(24f, 24f), 13f, Color.White, o)
            circleShape(Offset(24f, 24f), 6.5f, c.accent, o)
        }

        GameIcon.Bag -> {
            val handle = Path().apply { moveTo(16f, 17f); cubicTo(16f, 3f, 32f, 3f, 32f, 17f) }
            stroked(handle, c.accent, o, 3f)
            val body = polygon(8f to 15f, 40f to 15f, 43f to 43f, 5f to 43f)
            shape(body, c.main, o)
            circleShape(Offset(24f, 29f), 5.5f, c.accent, o)
        }

        GameIcon.Person -> {
            val body = Path().apply {
                moveTo(5f, 44f)
                cubicTo(5f, 28f, 43f, 28f, 43f, 44f)
                close()
            }
            shape(body, c.accent, o)
            circleShape(Offset(24f, 17f), 11f, c.main, o)
        }

        GameIcon.Info -> {
            circleShape(Offset(24f, 24f), 20f, c.main, o)
            drawLine(c.accent, Offset(24f, 22f), Offset(24f, 34f), 5f, StrokeCap.Round)
            drawCircle(c.accent, 3f, Offset(24f, 14f))
        }

        GameIcon.Book -> {
            val left = Path().apply {
                moveTo(24f, 12f); cubicTo(18f, 7f, 10f, 7f, 4f, 9f); lineTo(4f, 38f)
                cubicTo(10f, 36f, 18f, 36f, 24f, 41f); close()
            }
            val right = Path().apply {
                moveTo(24f, 12f); cubicTo(30f, 7f, 38f, 7f, 44f, 9f); lineTo(44f, 38f)
                cubicTo(38f, 36f, 30f, 36f, 24f, 41f); close()
            }
            shape(left, c.main, o)
            shape(right, c.main, o)
            for (y in listOf(16f, 22f, 28f)) {
                drawLine(c.accent, Offset(9f, y), Offset(19f, y + 1f), 2.4f, StrokeCap.Round)
                drawLine(c.accent, Offset(29f, y + 1f), Offset(39f, y), 2.4f, StrokeCap.Round)
            }
        }
    }
}
