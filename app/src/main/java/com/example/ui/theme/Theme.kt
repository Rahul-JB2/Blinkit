package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BrandGreen,
    onPrimary = Color.White,
    secondary = BrandYellow,
    onSecondary = BrandDark,
    background = BrandBgLight,
    onBackground = BrandTextDark,
    surface = Color.White,
    onSurface = BrandTextDark,
    surfaceVariant = Color(0xFFF4F4F5),
    onSurfaceVariant = Color(0xFF71717A),
    outline = BorderColor
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandGreen,
    onPrimary = Color.White,
    secondary = BrandYellow,
    onSecondary = BrandDark,
    background = BrandDark,
    onBackground = Color.White,
    surface = Color(0xFF27272A),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF3F3F46),
    onSurfaceVariant = Color(0xFFD4D4D8),
    outline = Color(0xFF52525B)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // We disable dynamicColor to enforce our bright, distinctive brand identity!
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
