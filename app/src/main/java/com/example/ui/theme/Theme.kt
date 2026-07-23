package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ForestGreen,
    onPrimary = White,
    primaryContainer = ForestGreenContainer,
    onPrimaryContainer = ForestGreenOnContainer,
    secondary = SageGreen,
    onSecondary = White,
    secondaryContainer = SageGreenContainer,
    onSecondaryContainer = SageGreenOnContainer,
    background = DarkCharcoal,
    onBackground = SoftCream,
    surface = DarkCharcoal,
    onSurface = SoftCream,
    surfaceVariant = MutedSlate,
    onSurfaceVariant = WarmGray,
    outline = OliveBorder
)

private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = White,
    primaryContainer = ForestGreenContainer,
    onPrimaryContainer = ForestGreenOnContainer,
    secondary = SageGreen,
    onSecondary = White,
    secondaryContainer = SageGreenContainer,
    onSecondaryContainer = SageGreenOnContainer,
    background = SoftCream,
    onBackground = DarkCharcoal,
    surface = SoftCream,
    onSurface = DarkCharcoal,
    surfaceVariant = WarmGray,
    onSurfaceVariant = MutedSlate,
    outline = OliveBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Force LightColorScheme to maintain high visibility and color consistency with the hardcoded white/cream backgrounds.
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
