package com.splitsmart.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary          = Primary,
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFE8E6FF),
    secondary        = Secondary,
    onSecondary      = Color.White,
    secondaryContainer = Color(0xFFB3F5E8),
    background       = Background,
    onBackground     = Color(0xFF1A1A2E),
    surface          = Surface,
    onSurface        = Color(0xFF1A1A2E),
    surfaceVariant   = SurfaceVariant,
    onSurfaceVariant = Color(0xFF4A4A6A),
    error            = NegativeRed,
    onError          = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary          = Color(0xFF9D95FF),
    onPrimary        = Color(0xFF0D0D1A),
    primaryContainer = Color(0xFF3D35B0),
    secondary        = Color(0xFF00E8BB),
    onSecondary      = Color(0xFF0D0D1A),
    secondaryContainer = Color(0xFF005C4B),
    background       = BackgroundDark,
    onBackground     = Color(0xFFE8E8F5),
    surface          = SurfaceDark,
    onSurface        = Color(0xFFE8E8F5),
    surfaceVariant   = SurfaceVariantDark,
    onSurfaceVariant = Color(0xFFB8B8D0),
    error            = Color(0xFFFF8FA3),
    onError          = Color(0xFF3B000F)
)

@Composable
fun SplitSmartTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        content     = content
    )
}
