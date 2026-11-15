package br.com.trilhati.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.pow
import org.junit.Assert.assertTrue
import org.junit.Test

/** Garante contraste WCAG AA (4,5:1) para todo par texto/fundo usado no app, nos dois temas. */
class ContrastTest {

    private fun channel(v: Float): Double {
        val c = v.toDouble()
        return if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
    }

    private fun luminance(c: Color): Double =
        0.2126 * channel(c.red) + 0.7152 * channel(c.green) + 0.0722 * channel(c.blue)

    private fun ratio(a: Color, b: Color): Double {
        val (hi, lo) = listOf(luminance(a), luminance(b)).sortedDescending()
        return (hi + 0.05) / (lo + 0.05)
    }

    private fun pairs(c: TrilhaColors): Map<String, Pair<Color, Color>> = mapOf(
        "ink/background" to (c.ink to c.background),
        "ink/surface" to (c.ink to c.surface),
        "ink/surfaceAlt" to (c.ink to c.surfaceAlt),
        "inkSoft/background" to (c.inkSoft to c.background),
        "inkSoft/surface" to (c.inkSoft to c.surface),
        "onPrimary/primary" to (c.onPrimary to c.primary),
        "primaryText/background" to (c.primaryText to c.background),
        "primaryText/surface" to (c.primaryText to c.surface),
        "primaryText/primarySoft" to (c.primaryText to c.primarySoft),
        "onSecondary/secondary" to (c.onSecondary to c.secondary),
        "secondaryText/background" to (c.secondaryText to c.background),
        "secondaryText/surface" to (c.secondaryText to c.surface),
        "secondaryText/secondarySoft" to (c.secondaryText to c.secondarySoft),
        "onSuccess/success" to (c.onSuccess to c.success),
        "successText/successSoft" to (c.successText to c.successSoft),
        "onError/error" to (c.onError to c.error),
        "errorText/errorSoft" to (c.errorText to c.errorSoft),
        "onWarning/warning" to (c.onWarning to c.warning),
        "warningText/warningSoft" to (c.warningText to c.warningSoft),
        "onLocked/locked" to (c.onLocked to c.locked),
    )

    private fun check(name: String, c: TrilhaColors) {
        val failures = pairs(c).filter { (_, p) -> ratio(p.first, p.second) < 4.5 }
            .map { (k, p) -> "$k = ${"%.2f".format(ratio(p.first, p.second))}" }
        assertTrue("$name com contraste abaixo de 4,5:1: $failures", failures.isEmpty())
    }

    @Test
    fun `tema claro atende contraste AA`() = check("claro", LightTrilhaColors)

    @Test
    fun `tema escuro atende contraste AA`() = check("escuro", DarkTrilhaColors)
}
