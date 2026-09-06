package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val LocalBitesColorScheme = darkColorScheme(
    primary = AmberPrimary,
    onPrimary = TextPrimary,
    primaryContainer = AmberContainer,
    onPrimaryContainer = OnAmberContainer,
    secondary = AmberSecondary,
    onSecondary = CharcoalBackground,
    secondaryContainer = CharcoalSurfaceElevated,
    onSecondaryContainer = AmberTertiary,
    tertiary = AmberTertiary,
    onTertiary = CharcoalBackground,
    background = CharcoalBackground,
    onBackground = TextPrimary,
    surface = CharcoalSurface,
    onSurface = TextPrimary,
    surfaceVariant = CharcoalCard,
    onSurfaceVariant = TextSecondary,
    outline = CharcoalBorder,
    outlineVariant = CharcoalBorder.copy(alpha = 0.6f)
)

@Composable
fun LocalBitesTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LocalBitesColorScheme,
        typography = Typography,
        content = content
    )
}

// Compatibility wrapper
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    LocalBitesTheme(content = content)
}

