package com.calcnova.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = CalcPrimary,
    onPrimary = CalcOnPrimary,
    primaryContainer = CalcPrimaryContainer,
    onPrimaryContainer = CalcOnPrimaryContainer,
    secondary = CalcSecondary,
    onSecondary = CalcOnSecondary,
    secondaryContainer = CalcSecondaryContainer,
    onSecondaryContainer = CalcOnSecondaryContainer,
    tertiary = CalcTertiary,
    onTertiary = CalcOnTertiary,
    tertiaryContainer = CalcTertiaryContainer,
    onTertiaryContainer = CalcOnTertiaryContainer,
    error = CalcError,
    onError = CalcOnError,
    errorContainer = CalcErrorContainer,
    onErrorContainer = CalcOnErrorContainer,
    background = CalcBackgroundLight,
    onBackground = CalcOnBackgroundLight,
    surface = CalcSurfaceLight,
    onSurface = CalcOnBackgroundLight,
    surfaceVariant = CalcSurfaceVariantLight,
    onSurfaceVariant = CalcOnSurfaceVariantLight
)

private val DarkColors = darkColorScheme(
    primary = CalcPrimary,
    onPrimary = CalcOnPrimary,
    primaryContainer = CalcOnPrimaryContainer,
    onPrimaryContainer = CalcPrimaryContainer,
    secondary = CalcSecondary,
    onSecondary = CalcOnSecondary,
    secondaryContainer = CalcOnSecondaryContainer,
    onSecondaryContainer = CalcSecondaryContainer,
    tertiary = CalcTertiary,
    onTertiary = CalcOnTertiary,
    tertiaryContainer = CalcOnTertiaryContainer,
    onTertiaryContainer = CalcTertiaryContainer,
    error = CalcError,
    onError = CalcOnError,
    errorContainer = CalcErrorContainer,
    onErrorContainer = CalcOnErrorContainer,
    background = CalcBackgroundDark,
    onBackground = CalcOnBackgroundDark,
    surface = CalcSurfaceDark,
    onSurface = CalcOnBackgroundDark,
    surfaceVariant = CalcSurfaceVariantDark,
    onSurfaceVariant = CalcOnSurfaceVariantDark
)

/**
 * On Android 12+ this defaults to Material You dynamic color - the app's
 * palette is generated from the user's wallpaper, so CalcNova visually
 * blends in with the rest of the system UI. Devices below Android 12 (or
 * with dynamic color unavailable) fall back to CalcNova's own designed
 * brand palette above.
 */
@Composable
fun CalcNovaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CalcNovaTypography,
        content = content
    )
}
