package com.puremusic.player.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.core.view.WindowCompat
import com.puremusic.player.data.model.AppAccentColor
import com.puremusic.player.data.model.AppFontStyle
import com.puremusic.player.data.model.BackgroundStyle
import com.puremusic.player.data.model.LayoutDensity

val ForestGreen = Color(0xFF11553B)

val WarmPaperBg = Color(0xFFF6F2EC)
val WarmPaperSidebar = Color(0xFFEEE8DF)
val WarmPaperSurface = Color(0xFFFCF9F5)
val WarmPaperSurfaceVariant = Color(0xFFEBE5DB)
val InkCharcoal = Color(0xFF1B1A18)
val InkMuted = Color(0xFF6E6860)

val DeepInkBg = Color(0xFF111316)
val DeepInkSidebar = Color(0xFF1A1C20)

val LocalLayoutDensity = compositionLocalOf { LayoutDensity.STANDARD }

fun sidebarColorFor(isDark: Boolean): Color = if (isDark) DeepInkSidebar else WarmPaperSidebar

@Composable
fun PureMusicTheme(
    backgroundStyle: BackgroundStyle,
    accentColor: AppAccentColor,
    fontStyle: AppFontStyle,
    layoutDensity: LayoutDensity = LayoutDensity.STANDARD,
    fontScale: Float = 1.0f,
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

    val scale = fontScale.coerceIn(0.85f, 1.25f)
    fun androidx.compose.ui.text.TextStyle.scaled() = copy(
        fontFamily = family,
        fontSize = fontSize * scale,
        lineHeight = lineHeight * scale,
    )
    val base = Typography()
    val customTypography = Typography(
        displayLarge = base.displayLarge.scaled(),
        displayMedium = base.displayMedium.scaled(),
        displaySmall = base.displaySmall.scaled(),
        headlineLarge = base.headlineLarge.scaled(),
        headlineMedium = base.headlineMedium.scaled(),
        headlineSmall = base.headlineSmall.scaled(),
        titleLarge = base.titleLarge.scaled(),
        titleMedium = base.titleMedium.scaled(),
        titleSmall = base.titleSmall.scaled(),
        bodyLarge = base.bodyLarge.scaled(),
        bodyMedium = base.bodyMedium.scaled(),
        bodySmall = base.bodySmall.scaled(),
        labelLarge = base.labelLarge.scaled(),
        labelMedium = base.labelMedium.scaled(),
        labelSmall = base.labelSmall.scaled(),
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

    CompositionLocalProvider(LocalLayoutDensity provides layoutDensity) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = customTypography,
            content = content,
        )
    }
}
