package com.zaviyanllc.sproutplay.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val SproutLightColorScheme = lightColorScheme(
    primary = CandyPink,
    onPrimary = Color.White,
    primaryContainer = CandyPinkContainer,
    onPrimaryContainer = OnCandyPinkContainer,
    secondary = CandyOrange,
    onSecondary = Color.White,
    secondaryContainer = CandyOrangeContainer,
    onSecondaryContainer = OnCandyOrangeContainer,
    tertiary = CandyGrape,
    onTertiary = Color.White,
    tertiaryContainer = CandyGrapeContainer,
    onTertiaryContainer = OnCandyGrapeContainer,
    background = CreamBackground,
    onBackground = DeepInk,
    surface = CreamSurface,
    onSurface = DeepInk,
    surfaceVariant = CreamSurfaceVariant,
    onSurfaceVariant = MutedInk,
    outline = SoftOutline,
)

private val SproutDarkColorScheme = darkColorScheme(
    primary = NightTurquoise,
    onPrimary = Color(0xFF042F2E),
    primaryContainer = Color(0xFF134E4A),
    onPrimaryContainer = TurquoiseContainer,
    secondary = NightSunshine,
    onSecondary = Color(0xFF422006),
    secondaryContainer = Color(0xFF78350F),
    onSecondaryContainer = SunshineContainer,
    tertiary = NightCoral,
    onTertiary = Color(0xFF4C0519),
    tertiaryContainer = Color(0xFF881337),
    onTertiaryContainer = CoralContainer,
    background = NightBackground,
    onBackground = NightText,
    surface = NightSurface,
    onSurface = NightText,
    surfaceVariant = NightSurfaceVariant,
    onSurfaceVariant = NightMutedText,
    outline = Color(0xFF3B4B6E),
)

val SproutShapes = Shapes(
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(32.dp),
    extraLarge = RoundedCornerShape(40.dp),
)

@Composable
fun SproutPlayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) SproutDarkColorScheme else SproutLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SproutTypography,
        shapes = SproutShapes,
        content = content,
    )
}
