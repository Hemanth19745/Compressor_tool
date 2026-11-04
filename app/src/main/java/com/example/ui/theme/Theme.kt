package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val PastelLightColorScheme = lightColorScheme(
    primary = PastelMintAccent,
    onPrimary = Color.White,
    primaryContainer = PastelMintContainer,
    onPrimaryContainer = Color(0xFF0B3829),
    secondary = PastelSkyAccent,
    onSecondary = Color.White,
    secondaryContainer = PastelSkyContainer,
    onSecondaryContainer = Color(0xFF0A2E46),
    tertiary = PastelLavenderAccent,
    onTertiary = Color.White,
    tertiaryContainer = PastelLavenderContainer,
    onTertiaryContainer = Color(0xFF2B1749),
    background = AlabasterBackground,
    onBackground = Color(0xFF191C1B),
    surface = AlabasterSurface,
    onSurface = Color(0xFF191C1B),
    surfaceVariant = AlabasterSurfaceVariant,
    onSurfaceVariant = Color(0xFF424945),
    outline = Color(0xFFD0D7D3)
)

private val PastelDarkColorScheme = darkColorScheme(
    primary = DarkMintAccent,
    onPrimary = Color(0xFF073527),
    primaryContainer = DarkMintContainer,
    onPrimaryContainer = Color(0xFFB8F5DC),
    secondary = DarkSkyAccent,
    onSecondary = Color(0xFF082D44),
    secondaryContainer = DarkSkyContainer,
    onSecondaryContainer = Color(0xFFC9E4FF),
    tertiary = DarkLavenderAccent,
    onTertiary = Color(0xFF331E54),
    tertiaryContainer = DarkLavenderContainer,
    onTertiaryContainer = Color(0xFFECDCFF),
    background = SlateDarkBackground,
    onBackground = Color(0xFFE4E7E5),
    surface = SlateDarkSurface,
    onSurface = Color(0xFFE4E7E5),
    surfaceVariant = SlateDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFBFC8C3),
    outline = Color(0xFF3C4440)
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun SqueezeStudioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> PastelDarkColorScheme
        else -> PastelLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
