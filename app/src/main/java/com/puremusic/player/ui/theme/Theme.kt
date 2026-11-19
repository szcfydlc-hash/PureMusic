package com.puremusic.player.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.puremusic.player.data.model.AppAccentColor
import com.puremusic.player.data.model.AppFontStyle
import com.puremusic.player.data.model.BackgroundStyle
import com.puremusic.player.data.model.PlayerTheme

val ForestGreen = Color(0xFF11553B)
val VermilionAccent = Color(0xFFD1431E)
val WarmWoodAccent = Color(0xFF2E5C45)
val RetroRedAccent = Color(0xFFB7362A)

val WarmPaperBg = Color(0xFFF5F1EB)
val WarmPaperSidebar = Color(0xFFEFEAE1)
val WarmPaperSurface = Color(0xFFFBF8F3)
val WarmPaperSurfaceVariant = Color(0xFFEAE4DA)
val InkCharcoal = Color(0xFF1C1B1A)
val InkMuted = Color(0xFF6B665F)

val DeepInkBg = Color(0xFF121417)
val DeepInkSidebar = Color(0xFF1C1E22)

@Composable
fun PureMusicTheme(
    backgroundStyle: BackgroundStyle,
    playerTheme: PlayerTheme,
    accentColor: AppAccentColor,
    fontStyle: AppFontStyle,
    content: @Composable () -> Unit,
) {
    val useDark = when (backgroundStyle) {
        BackgroundStyle.DEEP_INK -> true
        BackgroundStyle.SYSTEM -> isSystemInDarkTheme()
        BackgroundStyle.WARM_PAPER, BackgroundStyle.PURE_WHITE -> false
    }

    val primaryAccent = Color(accentColor.colorHex)

    val colorScheme = if (useDark) {
        darkColorScheme(
            primary = primaryAccent,
            onPrimary = Color.White,
            primaryContainer = primaryAccent.copy(alpha = 0.24f),
            onPrimaryContainer = Color(0xFFEDEAE4),
            secondary = primaryAccent.copy(alpha = 0.8f),
            onSecondary = Color.White,
            background = DeepInkBg,
            onBackground = Color(0xFFEDEAE4),
            surface = Color(0xFF1A1C20),
            onSurface = Color(0xFFEDEAE4),
            surfaceVariant = Color(0xFF272A30),
            onSurfaceVariant = Color(0xFFA6A198),
        )
    } else if (backgroundStyle == BackgroundStyle.PURE_WHITE) {
        lightColorScheme(
            primary = primaryAccent,
            onPrimary = Color.White,
            primaryContainer = primaryAccent.copy(alpha = 0.14f),
            onPrimaryContainer = primaryAccent,
            secondary = primaryAccent.copy(alpha = 0.8f),
            onSecondary = Color.White,
            background = Color(0xFFFFFFFF),
            onBackground = InkCharcoal,
            surface = Color(0xFFFAFAFA),
            onSurface = InkCharcoal,
            surfaceVariant = Color(0xFFF0F0EE),
            onSurfaceVariant = InkMuted,
        )
    } else {
        lightColorScheme(
            primary = primaryAccent,
            onPrimary = Color.White,
            primaryContainer = primaryAccent.copy(alpha = 0.14f),
            onPrimaryContainer = primaryAccent,
            secondary = primaryAccent.copy(alpha = 0.8f),
            onSecondary = Color.White,
            background = WarmPaperBg,
            onBackground = InkCharcoal,
            surface = WarmPaperSurface,
            onSurface = InkCharcoal,
            surfaceVariant = WarmPaperSurfaceVariant,
            onSurfaceVariant = InkMuted,
        )
    }

    val family = when (fontStyle) {
        AppFontStyle.SANS -> FontFamily.SansSerif
        AppFontStyle.SERIF -> FontFamily.Serif
        AppFontStyle.MONOSPACE -> FontFamily.Monospace
    }

    val base = Typography()
    val customTypography = Typography(
        displayLarge = base.displayLarge.copy(fontFamily = family),
        displayMedium = base.displayMedium.copy(fontFamily = family),
        displaySmall = base.displaySmall.copy(fontFamily = family),
        headlineLarge = base.headlineLarge.copy(fontFamily = family),
        headlineMedium = base.headlineMedium.copy(fontFamily = family),
        headlineSmall = base.headlineSmall.copy(fontFamily = family),
        titleLarge = base.titleLarge.copy(fontFamily = family),
        titleMedium = base.titleMedium.copy(fontFamily = family),
        titleSmall = base.titleSmall.copy(fontFamily = family),
        bodyLarge = base.bodyLarge.copy(fontFamily = family),
        bodyMedium = base.bodyMedium.copy(fontFamily = family),
        bodySmall = base.bodySmall.copy(fontFamily = family),
        labelLarge = base.labelLarge.copy(fontFamily = family),
        labelMedium = base.labelMedium.copy(fontFamily = family),
        labelSmall = base.labelSmall.copy(fontFamily = family),
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = customTypography,
        content = content,
    )
}
