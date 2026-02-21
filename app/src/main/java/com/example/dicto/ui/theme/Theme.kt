package com.example.dicto.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ============================================================================
// Light Color Scheme
// ============================================================================
private val LightColorScheme = lightColorScheme(
    // Primary — blue actions, icons, selected indicators
    primary            = Brand500,
    onPrimary          = Color.White,
    primaryContainer   = Brand100,
    onPrimaryContainer = Brand600,

    // Secondary — reuse primary family for a monochromatic feel
    secondary            = Brand500,
    onSecondary          = Color.White,
    secondaryContainer   = Brand100,
    onSecondaryContainer = Brand600,

    // Tertiary — same, keeps palette clean
    tertiary            = Brand500,
    onTertiary          = Color.White,
    tertiaryContainer   = Brand100,
    onTertiaryContainer = Brand600,

    // Backgrounds & surfaces
    background         = LightBackground,
    onBackground       = LightOnBackground,
    surface            = LightSurface,
    onSurface          = LightOnSurface,
    surfaceVariant     = LightSurface2,      // Input fields, chips
    onSurfaceVariant   = LightOnSurface2,
    surfaceContainerHighest = LightSurface2,
    surfaceContainerHigh    = LightSurface2,
    surfaceContainer        = LightSurface,
    surfaceContainerLow     = LightBackground,

    // Outlines
    outline        = LightOutline,
    outlineVariant = LightOutline,

    // Error
    error            = ErrorRed,
    onError          = Color.White,
    errorContainer   = ErrorRedContainer,
    onErrorContainer = ErrorRed,

    // Inverse (snackbars, tooltips)
    inverseSurface   = LightOnBackground,
    inverseOnSurface = LightSurface,
    inversePrimary   = Brand400,
)

// ============================================================================
// Dark Color Scheme (AMOLED — True Black base)
// ============================================================================
private val DarkColorScheme = darkColorScheme(
    // Primary
    primary            = Brand400,
    onPrimary          = Color.White,
    primaryContainer   = Brand900,
    onPrimaryContainer = Brand100,

    // Secondary
    secondary            = Brand400,
    onSecondary          = Color.White,
    secondaryContainer   = Brand900,
    onSecondaryContainer = Brand100,

    // Tertiary
    tertiary            = Brand400,
    onTertiary          = Color.White,
    tertiaryContainer   = Brand900,
    onTertiaryContainer = Brand100,

    // Backgrounds & surfaces
    background         = DarkBackground,
    onBackground       = DarkOnBackground,
    surface            = DarkSurface,
    onSurface          = DarkOnSurface,
    surfaceVariant     = DarkSurface2,
    onSurfaceVariant   = DarkOnSurface2,
    surfaceContainerHighest = DarkSurface2,
    surfaceContainerHigh    = DarkSurface2,
    surfaceContainer        = DarkSurface,
    surfaceContainerLow     = DarkBackground,

    // Outlines
    outline        = DarkOutline,
    outlineVariant = DarkOutline,

    // Error
    error            = ErrorRedDark,
    onError          = Color.Black,
    errorContainer   = ErrorRedContainerDark,
    onErrorContainer = ErrorRedDark,

    // Inverse
    inverseSurface   = DarkOnSurface,
    inverseOnSurface = DarkSurface,
    inversePrimary   = Brand600,
)

// ============================================================================
// Theme Composable
// ============================================================================
@Composable
fun DictoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography  = Typography,
        shapes      = Shapes,
        content     = content
    )
}
