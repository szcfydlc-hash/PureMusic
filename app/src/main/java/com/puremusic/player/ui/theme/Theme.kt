package com.puremusic.player.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.puremusic.player.data.model.AccentPalette
import com.puremusic.player.data.model.ThemeMode

@Composable
fun PureMusicTheme(
    themeMode: ThemeMode,
    accentPalette: AccentPalette,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val accent = Color(accentPalette.seedColorHex)

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = accent.copy(alpha = 0.92f),
            onPrimary = Color.White,
            primaryContainer = accent.copy(alpha = 0.24f),
            onPrimaryContainer = Color(0xFFFFF3E0),
            secondary = accent.copy(alpha = 0.72f),
            onSecondary = Color.White,
            background = Color(0xFF101214),
            onBackground = Color(0xFFECEFF1),
            surface = Color(0xFF16191D),
            onSurface = Color(0xFFECEFF1),
            surfaceVariant = Color(0xFF22272E),
            onSurfaceVariant = Color(0xFFB0BEC5),
        )
    } else {
        lightColorScheme(
            primary = accent,
            onPrimary = Color.White,
            primaryContainer = accent.copy(alpha = 0.14f),
            onPrimaryContainer = accent,
            secondary = accent.copy(alpha = 0.8f),
            onSecondary = Color.White,
            background = Color(0xFFFAFAFA),
            onBackground = Color(0xFF181A1D),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF181A1D),
            surfaceVariant = Color(0xFFF1F3F5),
            onSurfaceVariant = Color(0xFF5F6368),
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
