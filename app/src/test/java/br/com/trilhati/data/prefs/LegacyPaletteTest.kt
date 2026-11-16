package br.com.trilhati.data.prefs

import br.com.trilhati.ui.theme.ThemePalette
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Quem usava claro ou escuro fixo até a 0.7.3 continua com o mesmo tema; "Sistema" segue o aparelho. */
class LegacyPaletteTest {
    @Test
    fun `modo claro antigo vira o tema Claro`() = assertEquals(ThemePalette.Claro, legacyPalette("Light"))

    @Test
    fun `modo escuro antigo vira o tema Escuro`() = assertEquals(ThemePalette.Escuro, legacyPalette("Dark"))

    @Test
    fun `sistema ou nada continua seguindo o aparelho`() {
        assertNull(legacyPalette("System"))
        assertNull(legacyPalette(null))
    }
}
