package br.com.trilhati.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Tokens de cor do BanQuest (roxo + turquesa, original). Nada de paleta copiada de outros apps.
 *
 * Convenção: cada cor "cheia" tem uma `*Edge` (a borda inferior mais escura que dá o efeito 3D dos botões)
 * e uma `on*` (cor do texto/ícone sobre ela, com contraste mínimo de 4,5:1 — garantido por teste).
 */
@Immutable
data class TrilhaColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceAlt: Color,
    val outline: Color,
    val ink: Color,
    val inkSoft: Color,
    val primary: Color,
    val primaryEdge: Color,
    val onPrimary: Color,
    val primarySoft: Color,
    val primaryText: Color,
    val secondary: Color,
    val secondaryEdge: Color,
    val onSecondary: Color,
    val secondarySoft: Color,
    val secondaryText: Color,
    val success: Color,
    val successEdge: Color,
    val onSuccess: Color,
    val successSoft: Color,
    val successText: Color,
    val error: Color,
    val errorEdge: Color,
    val onError: Color,
    val errorSoft: Color,
    val errorText: Color,
    val warning: Color,
    val warningEdge: Color,
    val onWarning: Color,
    val warningSoft: Color,
    val warningText: Color,
    val flame: Color,
    val gem: Color,
    val heart: Color,
    val locked: Color,
    val lockedEdge: Color,
    val onLocked: Color,
)

val LightTrilhaColors = TrilhaColors(
    isDark = false,
    background = Color(0xFFF6F2FF),
    surface = Color(0xFFFFFFFF),
    surfaceAlt = Color(0xFFEDE6FF),
    outline = Color(0xFFD9CEF7),
    ink = Color(0xFF1E1240),
    inkSoft = Color(0xFF5A4C80),
    primary = Color(0xFF6B3FD6),
    primaryEdge = Color(0xFF4A24A8),
    onPrimary = Color(0xFFFFFFFF),
    primarySoft = Color(0xFFE9DFFF),
    primaryText = Color(0xFF5B31C4),
    secondary = Color(0xFF19D3C5),
    secondaryEdge = Color(0xFF0E9C92),
    onSecondary = Color(0xFF1E1240),
    secondarySoft = Color(0xFFD4F8F4),
    secondaryText = Color(0xFF08746F),
    success = Color(0xFF1A7F45),
    successEdge = Color(0xFF126332),
    onSuccess = Color(0xFFFFFFFF),
    successSoft = Color(0xFFDFF7E8),
    successText = Color(0xFF17703D),
    error = Color(0xFFD63A50),
    errorEdge = Color(0xFFA81F33),
    onError = Color(0xFFFFFFFF),
    errorSoft = Color(0xFFFFE3E7),
    errorText = Color(0xFFA81F33),
    warning = Color(0xFFFFB000),
    warningEdge = Color(0xFFD18A00),
    onWarning = Color(0xFF1E1240),
    warningSoft = Color(0xFFFFF1CC),
    warningText = Color(0xFF7A4F00),
    flame = Color(0xFFFF7A1A),
    gem = Color(0xFF2FA8FF),
    heart = Color(0xFFFF4D6D),
    locked = Color(0xFFD3CAEA),
    lockedEdge = Color(0xFFB3A8D3),
    onLocked = Color(0xFF5A4C80),
)

val DarkTrilhaColors = TrilhaColors(
    isDark = true,
    background = Color(0xFF150E2B),
    surface = Color(0xFF221842),
    surfaceAlt = Color(0xFF2D2154),
    outline = Color(0xFF3F3170),
    ink = Color(0xFFEFE9FF),
    inkSoft = Color(0xFFB5A8D9),
    primary = Color(0xFF7C55E8),
    primaryEdge = Color(0xFF5A34C7),
    onPrimary = Color(0xFFFFFFFF),
    primarySoft = Color(0xFF33266B),
    primaryText = Color(0xFFB79CFF),
    secondary = Color(0xFF3EE6D8),
    secondaryEdge = Color(0xFF1FB8AB),
    onSecondary = Color(0xFF150E2B),
    secondarySoft = Color(0xFF123E48),
    secondaryText = Color(0xFF3EE6D8),
    success = Color(0xFF3DD17F),
    successEdge = Color(0xFF249A5A),
    onSuccess = Color(0xFF0B2416),
    successSoft = Color(0xFF16382A),
    successText = Color(0xFF5BE398),
    error = Color(0xFFFF6B7F),
    errorEdge = Color(0xFFD63A50),
    onError = Color(0xFF2B0A12),
    errorSoft = Color(0xFF3D1822),
    errorText = Color(0xFFFF8A9A),
    warning = Color(0xFFFFC233),
    warningEdge = Color(0xFFD99A00),
    onWarning = Color(0xFF1E1240),
    warningSoft = Color(0xFF3D3010),
    warningText = Color(0xFFFFC233),
    flame = Color(0xFFFF8A3D),
    gem = Color(0xFF5CBBFF),
    heart = Color(0xFFFF6B88),
    locked = Color(0xFF3A2F63),
    lockedEdge = Color(0xFF2C2350),
    onLocked = Color(0xFFB5A8D9),
)

/**
 * Tema "Elétrico" (60/30/10 da referência do dono, fundo sempre claro): 60% branco #FFFFFF no fundo e nos cartões, 30%
 * azul #0131CD na cor principal (botões, lição atual, faixa da unidade) e 10% verde-limão #B9FC01 no destaque (lições
 * concluídas, combos). O texto é azul-marinho; o texto sobre o limão é o próprio azul.
 */
val EletricoColors = TrilhaColors(
    isDark = false,
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFF4F7FF),
    surfaceAlt = Color(0xFFE6ECFF),
    outline = Color(0xFFCBD7FA),
    ink = Color(0xFF0A1A5C),
    inkSoft = Color(0xFF3D4C85),
    primary = Color(0xFF0131CD),
    primaryEdge = Color(0xFF00249A),
    onPrimary = Color(0xFFFFFFFF),
    primarySoft = Color(0xFFE3EAFF),
    primaryText = Color(0xFF0131CD),
    secondary = Color(0xFFB9FC01),
    secondaryEdge = Color(0xFF86BA00),
    onSecondary = Color(0xFF0131CD),
    secondarySoft = Color(0xFFEEFFC2),
    secondaryText = Color(0xFF3B5700),
    success = Color(0xFF1A7F45),
    successEdge = Color(0xFF126332),
    onSuccess = Color(0xFFFFFFFF),
    successSoft = Color(0xFFDFF7E8),
    successText = Color(0xFF17703D),
    error = Color(0xFFD63A50),
    errorEdge = Color(0xFFA81F33),
    onError = Color(0xFFFFFFFF),
    errorSoft = Color(0xFFFFE3E7),
    errorText = Color(0xFFA81F33),
    warning = Color(0xFFFFB000),
    warningEdge = Color(0xFFD18A00),
    onWarning = Color(0xFF0A1A5C),
    warningSoft = Color(0xFFFFF1CC),
    warningText = Color(0xFF7A4F00),
    flame = Color(0xFFFF7A1A),
    gem = Color(0xFF2FA8FF),
    heart = Color(0xFFFF4D6D),
    locked = Color(0xFFE3E8F7),
    lockedEdge = Color(0xFFC6CEE8),
    onLocked = Color(0xFF4A5578),
)

/**
 * Tema "Chiclete" (60/30/10 da referência do dono, fundo sempre claro): 60% creme #FEF1CF no fundo e nos cartões, 30%
 * rosa #FC3B7F na cor principal (botões, lição atual, faixa da unidade) e 10% azul #0032A8 no destaque. O texto sobre o
 * rosa é quase preto: branco sobre esse rosa fica em 3,5:1, abaixo do mínimo de 4,5:1.
 */
val ChicleteColors = TrilhaColors(
    isDark = false,
    background = Color(0xFFFEF1CF),
    surface = Color(0xFFFFF9E9),
    surfaceAlt = Color(0xFFFBE7BC),
    outline = Color(0xFFF0D9A8),
    ink = Color(0xFF0E1A52),
    inkSoft = Color(0xFF4A4470),
    primary = Color(0xFFFC3B7F),
    primaryEdge = Color(0xFFCC1F5E),
    onPrimary = Color(0xFF1A0B2E),
    primarySoft = Color(0xFFFFE0EC),
    primaryText = Color(0xFFA3104B),
    secondary = Color(0xFF0032A8),
    secondaryEdge = Color(0xFF00227A),
    onSecondary = Color(0xFFFEF1CF),
    secondarySoft = Color(0xFFDDE6FF),
    secondaryText = Color(0xFF0032A8),
    success = Color(0xFF1A7F45),
    successEdge = Color(0xFF126332),
    onSuccess = Color(0xFFFFFFFF),
    successSoft = Color(0xFFDFF7E8),
    successText = Color(0xFF17703D),
    error = Color(0xFFD63A50),
    errorEdge = Color(0xFFA81F33),
    onError = Color(0xFFFFFFFF),
    errorSoft = Color(0xFFFFE3E7),
    errorText = Color(0xFFA81F33),
    warning = Color(0xFFFFB000),
    warningEdge = Color(0xFFD18A00),
    onWarning = Color(0xFF0E1A52),
    warningSoft = Color(0xFFFFF1CC),
    warningText = Color(0xFF7A4F00),
    flame = Color(0xFFFF7A1A),
    gem = Color(0xFF2FA8FF),
    heart = Color(0xFFE8274B),
    locked = Color(0xFFF0E0BD),
    lockedEdge = Color(0xFFD9C595),
    onLocked = Color(0xFF5E4E2E),
)

/**
 * Cores da trilha dos Gerais: a cor secundária do tema vira a principal (e vice-versa). Assim cada trilha tem a sua cara
 * em qualquer tema, e todo par texto/fundo continua com o contraste já testado.
 */
fun TrilhaColors.forGeneralTrack(): TrilhaColors = copy(
    primary = secondary, primaryEdge = secondaryEdge, onPrimary = onSecondary, primarySoft = secondarySoft, primaryText = secondaryText,
    secondary = primary, secondaryEdge = primaryEdge, onSecondary = onPrimary, secondarySoft = primarySoft, secondaryText = primaryText,
)

/** Mistura duas paletas (0 = [a], 1 = [b]) para animar a troca de trilha. */
fun lerp(a: TrilhaColors, b: TrilhaColors, t: Float): TrilhaColors {
    fun m(x: Color, y: Color) = androidx.compose.ui.graphics.lerp(x, y, t)
    return TrilhaColors(
        isDark = if (t < 0.5f) a.isDark else b.isDark,
        background = m(a.background, b.background), surface = m(a.surface, b.surface), surfaceAlt = m(a.surfaceAlt, b.surfaceAlt),
        outline = m(a.outline, b.outline), ink = m(a.ink, b.ink), inkSoft = m(a.inkSoft, b.inkSoft),
        primary = m(a.primary, b.primary), primaryEdge = m(a.primaryEdge, b.primaryEdge), onPrimary = m(a.onPrimary, b.onPrimary),
        primarySoft = m(a.primarySoft, b.primarySoft), primaryText = m(a.primaryText, b.primaryText),
        secondary = m(a.secondary, b.secondary), secondaryEdge = m(a.secondaryEdge, b.secondaryEdge),
        onSecondary = m(a.onSecondary, b.onSecondary), secondarySoft = m(a.secondarySoft, b.secondarySoft),
        secondaryText = m(a.secondaryText, b.secondaryText),
        success = m(a.success, b.success), successEdge = m(a.successEdge, b.successEdge), onSuccess = m(a.onSuccess, b.onSuccess),
        successSoft = m(a.successSoft, b.successSoft), successText = m(a.successText, b.successText),
        error = m(a.error, b.error), errorEdge = m(a.errorEdge, b.errorEdge), onError = m(a.onError, b.onError),
        errorSoft = m(a.errorSoft, b.errorSoft), errorText = m(a.errorText, b.errorText),
        warning = m(a.warning, b.warning), warningEdge = m(a.warningEdge, b.warningEdge), onWarning = m(a.onWarning, b.onWarning),
        warningSoft = m(a.warningSoft, b.warningSoft), warningText = m(a.warningText, b.warningText),
        flame = m(a.flame, b.flame), gem = m(a.gem, b.gem), heart = m(a.heart, b.heart),
        locked = m(a.locked, b.locked), lockedEdge = m(a.lockedEdge, b.lockedEdge), onLocked = m(a.onLocked, b.onLocked),
    )
}

val LocalTrilhaColors = staticCompositionLocalOf { LightTrilhaColors }
