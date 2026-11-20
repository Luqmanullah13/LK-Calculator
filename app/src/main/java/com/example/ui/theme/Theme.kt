package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class LkExtendedColors(
    val keyBackground: Color,
    val keyOperatorBackground: Color,
    val keySciBackground: Color,
    val keyBorder: Color,
    val keyOperatorBorder: Color,
    val historyText: Color,
    val displayCard: Color,
    val accentRed: Color
)

val LocalLkColors = staticCompositionLocalOf {
    LkExtendedColors(
        keyBackground = DarkKeyNeutral,
        keyOperatorBackground = DarkRedContainer,
        keySciBackground = DarkElevated,
        keyBorder = BorderDark,
        keyOperatorBorder = NeonRed,
        historyText = SoftGrey,
        displayCard = DarkSurface,
        accentRed = NeonRed
    )
}

private val LkDarkColorScheme = darkColorScheme(
    primary = NeonRed,
    onPrimary = PureWhite,
    primaryContainer = DarkRedContainer,
    onPrimaryContainer = PureWhite,
    secondary = CrimsonRed,
    onSecondary = PureWhite,
    secondaryContainer = DarkElevated,
    onSecondaryContainer = PureWhite,
    tertiary = PureWhite,
    onTertiary = PureBlack,
    background = PureBlack,
    onBackground = PureWhite,
    surface = DarkSurface,
    onSurface = PureWhite,
    surfaceVariant = DarkElevated,
    onSurfaceVariant = SoftGrey,
    outline = NeonRed,
    outlineVariant = BorderDark,
    error = NeonRed,
    onError = PureWhite
)

private val LkLightColorScheme = lightColorScheme(
    primary = NeonRed,
    onPrimary = PureWhite,
    primaryContainer = LightRedContainer,
    onPrimaryContainer = CrimsonRed,
    secondary = CrimsonRed,
    onSecondary = PureWhite,
    secondaryContainer = LightElevated,
    onSecondaryContainer = LightTextPrimary,
    tertiary = LightTextPrimary,
    onTertiary = PureWhite,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightElevated,
    onSurfaceVariant = LightTextSecondary,
    outline = NeonRed,
    outlineVariant = BorderLight,
    error = CrimsonRed,
    onError = PureWhite
)

@Composable
fun LkCalculatorTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) LkDarkColorScheme else LkLightColorScheme
    val extendedColors = if (darkTheme) {
        LkExtendedColors(
            keyBackground = DarkKeyNeutral,
            keyOperatorBackground = DarkRedContainer,
            keySciBackground = DarkElevated,
            keyBorder = BorderDark,
            keyOperatorBorder = NeonRed,
            historyText = SoftGrey,
            displayCard = DarkSurface,
            accentRed = NeonRed
        )
    } else {
        LkExtendedColors(
            keyBackground = LightKeyNeutral,
            keyOperatorBackground = LightRedContainer,
            keySciBackground = LightElevated,
            keyBorder = BorderLight,
            keyOperatorBorder = NeonRed,
            historyText = LightTextSecondary,
            displayCard = LightSurface,
            accentRed = NeonRed
        )
    }

    CompositionLocalProvider(LocalLkColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LkTypography,
            content = content
        )
    }
}
