package com.caisaara.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.caisaara.ui.theme.CaisaaraDarkColorScheme

private val CaisaaraLightColorScheme = lightColorScheme(
    primary = EmeraldNormal,
    onPrimary = White,
    primaryContainer = EmeraldLight,
    onPrimaryContainer = EmeraldDarkActive,

    secondary = SlateNormal,
    onSecondary = White,
    secondaryContainer = SlateLight,
    onSecondaryContainer = SlateDarkActive,

    tertiary = AmberNormal,
    onTertiary = White,
    tertiaryContainer = AmberLight,
    onTertiaryContainer = AmberDarkActive,

    error = RedNormal,
    onError = White,
    errorContainer = RedLight,
    onErrorContainer = RedDarkActive,

    background = SlateLight,
    onBackground = SlateDarker,

    surface = White,
    onSurface = SlateDarker,
    surfaceVariant = SlateLightHover,
    onSurfaceVariant = SlateNormal,

    outline = SlateNormalActive,
    outlineVariant = SlateLightActive,

    inverseSurface = SlateDarker,
    inverseOnSurface = SlateLight,
    inversePrimary = EmeraldLightActive,
)

private val CaisaaraDarkColorScheme = darkColorScheme(
    primary = EmeraldNormalHover,
    onPrimary = SlateDarker,
    primaryContainer = EmeraldDarkActive,
    onPrimaryContainer = EmeraldLight,

    secondary = SlateLightActive,
    onSecondary = SlateDarker,
    secondaryContainer = SlateDarkHover,
    onSecondaryContainer = SlateLight,

    tertiary = AmberNormalHover,
    onTertiary = SlateDarker,
    tertiaryContainer = AmberDarkActive,
    onTertiaryContainer = AmberLight,

    error = RedNormalHover,
    onError = SlateDarker,
    errorContainer = RedDarkActive,
    onErrorContainer = RedLight,

    background = SlateDarker,
    onBackground = SlateLight,

    surface = SlateDark,
    onSurface = TextColor,
    surfaceVariant = Feildbackground,
    onSurfaceVariant = PlaceholderColor,

    outline = SlateNormalHover,
    outlineVariant = SlateDarkActive,

    inverseSurface = SlateLight,
    inverseOnSurface = SlateDarker,
    inversePrimary = EmeraldDark,
)

@Composable
fun CaisaaraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> CaisaaraDarkColorScheme
        else -> CaisaaraLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CaisaaraM3Typography,
        content = content,
    )
}