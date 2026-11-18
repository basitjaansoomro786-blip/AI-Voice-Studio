package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AiVoiceDarkColorScheme = darkColorScheme(
    primary = ElectricPurple,
    onPrimary = TextWhite,
    primaryContainer = Color(0xFF581C87),
    onPrimaryContainer = Color(0xFFE9D5FF),
    secondary = NeonCyan,
    onSecondary = BgDark,
    secondaryContainer = Color(0xFF155E75),
    onSecondaryContainer = Color(0xFFCFFAFE),
    background = BgDark,
    onBackground = TextWhite,
    surface = SurfaceDark,
    onSurface = TextWhite,
    surfaceVariant = CardDark,
    onSurfaceVariant = TextGray,
    error = ErrorRed,
    onError = TextWhite,
    outline = BorderGlow
)

@Composable
fun AIVoiceStudioTheme(
    darkTheme: Boolean = true, // Force premium dark theme by default for AI Voice Studio
    content: @Composable () -> Unit
) {
    val colorScheme = AiVoiceDarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
