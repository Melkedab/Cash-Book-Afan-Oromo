package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldDarkPrimary,
    onPrimary = EmeraldDarkOnPrimary,
    primaryContainer = EmeraldDarkPrimaryContainer,
    onPrimaryContainer = EmeraldDarkOnPrimaryContainer,
    secondary = Color(0xFFA1D0C1),
    onSecondary = Color(0xFF0C372D),
    secondaryContainer = Color(0xFF234E43),
    onSecondaryContainer = Color(0xFFBCECE0),
    tertiary = Color(0xFFFFD56B),
    onTertiary = Color(0xFF432C00),
    tertiaryContainer = Color(0xFF624300),
    onTertiaryContainer = Color(0xFFFFE088),
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    onBackground = Color(0xFFE2E7E2),
    onSurface = Color(0xFFE2E7E2)
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = EmeraldOnPrimary,
    primaryContainer = EmeraldPrimaryContainer,
    onPrimaryContainer = EmeraldOnPrimaryContainer,
    secondary = TealSecondary,
    onSecondary = TealOnSecondary,
    secondaryContainer = TealSecondaryContainer,
    onSecondaryContainer = TealOnSecondaryContainer,
    tertiary = GoldTertiary,
    onTertiary = GoldOnTertiary,
    tertiaryContainer = GoldTertiaryContainer,
    onTertiaryContainer = GoldOnTertiaryContainer,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    onBackground = Color(0xFF181D1A),
    onSurface = Color(0xFF181D1A)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent branding identity
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
