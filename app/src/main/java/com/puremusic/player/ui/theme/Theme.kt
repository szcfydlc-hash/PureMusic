package com.puremusic.player.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.core.view.WindowCompat
import com.puremusic.player.data.model.AppAccentColor
import com.puremusic.player.data.model.AppFontStyle
import com.puremusic.player.data.model.BackgroundStyle

val ForestGreen = Color(0xFF11553B)

val WarmPaperBg = Color(0xFFF6F2EC)
val WarmPaperSidebar = Color(0xFFEEE8DF)
val WarmPaperSurface = Color(0xFFFCF9F5)
val WarmPaperSurfaceVariant = Color(0xFFEBE5DB)
val InkCharcoal = Color(0xFF1B1A18)
val InkMuted = Color(0xFF6E6860)

val DeepInkBg = Color(0xFF111316)
val DeepInkSidebar = Color(0xFF1A1C20)

fun sidebarColorFor(isDark: Boolean): Color = if (isDark) DeepInkSidebar else WarmPaperSidebar

@Composable
fun PureMusicTheme(
    backgroundStyle: BackgroundStyle,
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
            onBackground = Color(0xFFF0EDE8),
            surface = Color(0xFF1A1C21),
            onSurface = Color(0xFFF0EDE8),
            surfaceVariant = Color(0xFF262930),
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
            surfaceVariant = Color(0xFFF1F0EE),
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !useDark
                controller.isAppearanceLightNavigationBars = !useDark
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = customTypography,
        content = content,
    )
}
