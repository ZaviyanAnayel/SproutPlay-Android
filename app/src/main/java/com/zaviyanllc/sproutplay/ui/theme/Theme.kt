package com.zaviyanllc.sproutplay.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Twin Trap is always dark — the reference look is a dark navy board.
 * No light theme: forcing dark keeps every screen consistent.
 */
private val TrapColorScheme = darkColorScheme(
    primary = TrapGold,
    onPrimary = Color(0xFF1A1400),
    primaryContainer = Color(0xFF3A2E00),
    onPrimaryContainer = TrapGold,
    secondary = TrapPink,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4C0A2E),
    onSecondaryContainer = TrapPink,
    tertiary = TrapLime,
    onTertiary = Color(0xFF1A2E05),
    tertiaryContainer = Color(0xFF2E4A0B),
    onTertiaryContainer = TrapLime,
    background = TrapNavy,
    onBackground = TrapWhite,
    surface = TrapCard,
    onSurface = TrapWhite,
    surfaceVariant = TrapCardBorder,
    onSurfaceVariant = TrapMuted,
    outline = TrapCardBorder,
    error = TrapDanger,
)

val TrapShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(34.dp),
)

@Composable
fun SproutPlayTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TrapColorScheme,
        typography = TrapTypography,
        shapes = TrapShapes,
        content = content,
    )
}
