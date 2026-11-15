package br.com.trilhati.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import br.com.trilhati.R

private fun nunito(weight: FontWeight) = Font(
    resId = R.font.nunito,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

private fun mono(weight: FontWeight) = Font(
    resId = R.font.jetbrains_mono,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

/** Família arredondada (Nunito, licença OFL, embutida no APK: o app é offline). */
val NunitoFamily = FontFamily(
    nunito(FontWeight.Normal),
    nunito(FontWeight.Medium),
    nunito(FontWeight.SemiBold),
    nunito(FontWeight.Bold),
    nunito(FontWeight.ExtraBold),
    nunito(FontWeight.Black),
)

/** Família monoespaçada para código e SQL (JetBrains Mono, licença OFL). */
val MonoFamily = FontFamily(mono(FontWeight.Normal), mono(FontWeight.Medium), mono(FontWeight.Bold))

/** Estilo para trechos de código nas questões. */
val CodeTextStyle = TextStyle(
    fontFamily = MonoFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 21.sp,
)

private fun style(size: Int, line: Int, weight: FontWeight, tracking: Float = 0f) = TextStyle(
    fontFamily = NunitoFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = tracking.em,
)

val TrilhaTypography = Typography(
    displayLarge = style(48, 56, FontWeight.Black),
    displayMedium = style(40, 48, FontWeight.Black),
    displaySmall = style(32, 40, FontWeight.ExtraBold),
    headlineLarge = style(28, 36, FontWeight.ExtraBold),
    headlineMedium = style(24, 32, FontWeight.ExtraBold),
    headlineSmall = style(20, 28, FontWeight.ExtraBold),
    titleLarge = style(20, 28, FontWeight.Bold),
    titleMedium = style(17, 24, FontWeight.Bold),
    titleSmall = style(15, 22, FontWeight.Bold),
    bodyLarge = style(17, 26, FontWeight.Medium),
    bodyMedium = style(15, 22, FontWeight.Medium),
    bodySmall = style(13, 18, FontWeight.Medium),
    labelLarge = style(16, 22, FontWeight.ExtraBold, 0.02f),
    labelMedium = style(13, 18, FontWeight.Bold, 0.02f),
    labelSmall = style(11, 16, FontWeight.Bold, 0.04f),
)
