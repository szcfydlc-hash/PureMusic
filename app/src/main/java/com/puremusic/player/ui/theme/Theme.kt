package com.puremusic.player.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.core.view.WindowCompat
import com.puremusic.player.data.model.AppAccentColor
import com.puremusic.player.data.model.AppFontStyle
import com.puremusic.player.data.model.BackgroundStyle
import com.puremusic.player.data.model.LayoutDensity

val BerryRed = Color(0xFFFA2D48)
val ForestGreen = Color(0xFFFA2D48)

val GlacialWhiteBg = Color(0xFFFFFFFF)
val GlacialSidebar = Color(0xFFF4F5F8)
val GlacialSurface = Color(0xFFF8F9FB)
val GlacialSurfaceVariant = Color(0xFFF0F2F6)
val ObsidianInk = Color(0xFF111318)
val CoolMuted = Color(0xFF7A808D)

val WarmPaperBg = Color(0xFFF7F4EE)
val DeepInkBg = Color(0xFF0A0B0E)
val DeepInkSidebar = Color(0xFF14161B)

val LocalLayoutDensity = compositionLocalOf { LayoutDensity.STANDARD }

fun sidebarColorFor(isDark: Boolean): Color = if (isDark) DeepInkSidebar else GlacialSidebar

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
        BackgroundStyle.PURE_WHITE, BackgroundStyle.WARM_PAPER -> false
    }

    val primaryAccent = if (accentColor == AppAccentColor.MONOCHROME && useDark) {
        Color(0xFFE4E4E7)
    } else {
        Color(accentColor.colorHex)
    }
    val onPrimaryColor = if (accentColor == AppAccentColor.MONOCHROME && useDark) {
        Color(0xFF111318)
    } else {
        Color.White
    }

    val colorScheme = if (useDark) {
        darkColorScheme(
            primary = primaryAccent,
            onPrimary = onPrimaryColor,
            primaryContainer = primaryAccent.copy(alpha = 0.20f),
            onPrimaryContainer = Color(0xFFF5F6FA),
            secondary = primaryAccent.copy(alpha = 0.85f),
            onSecondary = onPrimaryColor,
            background = DeepInkBg,
            onBackground = Color(0xFFF5F6FA),
            surface = Color(0xFF14161B),
            onSurface = Color(0xFFF5F6FA),
            surfaceVariant = Color(0xFF1E2129),
            onSurfaceVariant = Color(0xFF9197A4),
        )
    } else if (backgroundStyle == BackgroundStyle.WARM_PAPER) {
        lightColorScheme(
            primary = primaryAccent,
            onPrimary = onPrimaryColor,
            primaryContainer = primaryAccent.copy(alpha = 0.12f),
            onPrimaryContainer = primaryAccent,
            secondary = primaryAccent.copy(alpha = 0.85f),
            onSecondary = onPrimaryColor,
            background = WarmPaperBg,
            onBackground = ObsidianInk,
            surface = Color(0xFFFCFBF8),
            onSurface = ObsidianInk,
            surfaceVariant = Color(0xFFEEEAE2),
            onSurfaceVariant = CoolMuted,
        )
    } else {
        lightColorScheme(
            primary = primaryAccent,
            onPrimary = onPrimaryColor,
            primaryContainer = primaryAccent.copy(alpha = 0.12f),
            onPrimaryContainer = primaryAccent,
            secondary = primaryAccent.copy(alpha = 0.85f),
            onSecondary = onPrimaryColor,
            background = GlacialWhiteBg,
            onBackground = ObsidianInk,
            surface = GlacialSurface,
            onSurface = ObsidianInk,
            surfaceVariant = GlacialSurfaceVariant,
            onSurfaceVariant = CoolMuted,
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
