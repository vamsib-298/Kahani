package com.vl.kahani.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val KahaniColorScheme = darkColorScheme(
    primary = KahaniColors.Saffron,
    onPrimary = KahaniColors.Maroon950,
    secondary = KahaniColors.Saffron,
    onSecondary = KahaniColors.Maroon950,
    background = KahaniColors.Maroon900,
    onBackground = KahaniColors.TextPrimary,
    surface = KahaniColors.Maroon800,
    onSurface = KahaniColors.TextPrimary,
    surfaceVariant = KahaniColors.Maroon700,
    onSurfaceVariant = KahaniColors.TextMuted,
    outline = KahaniColors.Maroon600,
    outlineVariant = KahaniColors.Maroon600,
    error = KahaniColors.Saffron,
    onError = KahaniColors.Maroon950,
    scrim = KahaniColors.Maroon950,
)

/**
 * Kahani is a single warm dark theme by design — the "storyteller's room at dusk" identity does not
 * survive a light mode or dynamic color. The Reader carries its own Day Mode surface instead.
 */
@Composable
fun KahaniTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KahaniColorScheme,
        typography = Typography,
        content = content,
    )
}