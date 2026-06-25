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
    primary = PhonePePurpleLight,
    onPrimary = Color.White,
    primaryContainer = PhonePePurpleDark,
    onPrimaryContainer = Color.White,
    secondary = PhonePePurple,
    onSecondary = Color.White,
    background = Color(0xFF120a1c),
    surface = Color(0xFF1c1229),
    onBackground = Color.White,
    onSurface = Color.White,
    error = PhonePeRed
)

private val LightColorScheme = lightColorScheme(
    primary = PhonePePurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFf2ebfa),
    onPrimaryContainer = PhonePePurpleDark,
    secondary = PhonePePurpleLight,
    onSecondary = Color.White,
    background = Color(0xFFfcfaff),
    surface = Color.White,
    onBackground = Color(0xFF1a1c1e),
    onSurface = Color(0xFF1a1c1e),
    error = PhonePeRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Set to false by default to strictly enforce PhonePe branding
    content: @Composable () -> Unit,
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
