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
import androidx.compose.ui.text.font.FontStyle
import androidx.core.view.WindowCompat
import com.puremusic.player.data.model.AppAccentColor
import com.puremusic.player.data.model.AppFontStyle
import com.puremusic.player.data.model.BackgroundStyle
import com.puremusic.player.data.model.LayoutDensity
import com.puremusic.player.data.model.LiquidGlassLevel

val CinnabarRed = Color(0xFFA63429)
val ForestGreen = Color(0xFF2F5D50)

// Classic Ivory Archival Book Paper
val BookPaperBg = Color(0xFFF7F4EC)
val BookPaperCard = Color(0xFFFCFBF7)
val BookPaperVariant = Color(0xFFEDE8DC)
val BookPaperHigh = Color(0xFFE5DFD2)
val BookInkPrimary = Color(0xFF1E1B18)
val BookInkMuted = Color(0xFF6E675F)
val BookHairlineLight = Color(0xFFDDD6C7)

// Crisp Bright Editorial Paper
val PurePaperBg = Color(0xFFFFFFFF)
val PurePaperCard = Color(0xFFF9F8F5)
val PurePaperVariant = Color(0xFFF0EEE8)
val PurePaperHigh = Color(0xFFE7E4DC)
val PureHairlineLight = Color(0xFFE3DFD6)

// Night Reading Charcoal Cloth & Warm Ivory Ink
val NightBookBg = Color(0xFF141311)
val NightBookCard = Color(0xFF1D1B18)
val NightBookVariant = Color(0xFF282520)
val NightBookHigh = Color(0xFF312D27)
val NightBookInk = Color(0xFFEAE4D8)
val NightBookMuted = Color(0xFF9C9488)
val BookHairlineDark = Color(0xFF332F29)

val LocalLayoutDensity = compositionLocalOf { LayoutDensity.STANDARD }
val LocalLiquidGlassLevel = compositionLocalOf { LiquidGlassLevel.FULL }
val LocalFloatingControls = compositionLocalOf { false }

fun sidebarColorFor(isDark: Boolean): Color = if (isDark) NightBookCard else BookPaperVariant

@Composable
fun PureMusicTheme(
    backgroundStyle: BackgroundStyle,
    accentColor: AppAccentColor,
    fontStyle: AppFontStyle,
    layoutDensity: LayoutDensity = LayoutDensity.STANDARD,
    fontScale: Float = 1.0f,
    liquidGlassLevel: LiquidGlassLevel = LiquidGlassLevel.FULL,
    floatingControlsEnabled: Boolean = false,
    content: @Composable () -> Unit,
) {
    val useDark = when (backgroundStyle) {
        BackgroundStyle.DEEP_INK -> true
        BackgroundStyle.SYSTEM -> isSystemInDarkTheme()
        BackgroundStyle.WARM_PAPER, BackgroundStyle.PURE_WHITE -> false
    }

    val primaryAccent = if (accentColor == AppAccentColor.PURE_INK && useDark) {
        Color(0xFFDCD5C7)
    } else {
        Color(accentColor.colorHex)
    }
    val onPrimaryColor = if (accentColor == AppAccentColor.PURE_INK && useDark) {
        Color(0xFF141311)
    } else {
        Color(0xFFFCFBF7)
    }

    // Explicitly override ALL Material 3 container slots so zero default lavender/purple ever leaks!
    val colorScheme = if (useDark) {
        darkColorScheme(
            primary = primaryAccent,
            onPrimary = onPrimaryColor,
            primaryContainer = primaryAccent.copy(alpha = 0.22f),
            onPrimaryContainer = NightBookInk,
            secondary = primaryAccent.copy(alpha = 0.85f),
            onSecondary = onPrimaryColor,
            secondaryContainer = NightBookVariant,
            onSecondaryContainer = NightBookInk,
            tertiary = primaryAccent,
            onTertiary = onPrimaryColor,
            tertiaryContainer = NightBookVariant,
            onTertiaryContainer = NightBookInk,
            background = NightBookBg,
            onBackground = NightBookInk,
            surface = NightBookCard,
            onSurface = NightBookInk,
            surfaceVariant = NightBookVariant,
            onSurfaceVariant = NightBookMuted,
            surfaceTint = Color.Transparent,
            surfaceContainerLowest = NightBookBg,
            surfaceContainerLow = NightBookCard,
            surfaceContainer = NightBookCard,
            surfaceContainerHigh = NightBookVariant,
            surfaceContainerHighest = NightBookHigh,
            outline = BookHairlineDark,
            outlineVariant = BookHairlineDark.copy(alpha = 0.7f),
        )
    } else if (backgroundStyle == BackgroundStyle.PURE_WHITE) {
        lightColorScheme(
            primary = primaryAccent,
            onPrimary = onPrimaryColor,
            primaryContainer = primaryAccent.copy(alpha = 0.12f),
            onPrimaryContainer = primaryAccent,
            secondary = primaryAccent.copy(alpha = 0.85f),
            onSecondary = onPrimaryColor,
            secondaryContainer = PurePaperVariant,
            onSecondaryContainer = BookInkPrimary,
            tertiary = primaryAccent,
            onTertiary = onPrimaryColor,
            tertiaryContainer = PurePaperVariant,
            onTertiaryContainer = BookInkPrimary,
            background = PurePaperBg,
            onBackground = BookInkPrimary,
            surface = PurePaperCard,
            onSurface = BookInkPrimary,
            surfaceVariant = PurePaperVariant,
            onSurfaceVariant = BookInkMuted,
            surfaceTint = Color.Transparent,
            surfaceContainerLowest = PurePaperBg,
            surfaceContainerLow = PurePaperCard,
            surfaceContainer = PurePaperCard,
            surfaceContainerHigh = PurePaperVariant,
            surfaceContainerHighest = PurePaperHigh,
            outline = PureHairlineLight,
            outlineVariant = PureHairlineLight.copy(alpha = 0.7f),
        )
    } else {
        lightColorScheme(
            primary = primaryAccent,
            onPrimary = onPrimaryColor,
            primaryContainer = primaryAccent.copy(alpha = 0.12f),
            onPrimaryContainer = primaryAccent,
            secondary = primaryAccent.copy(alpha = 0.85f),
            onSecondary = onPrimaryColor,
            secondaryContainer = BookPaperVariant,
            onSecondaryContainer = BookInkPrimary,
            tertiary = primaryAccent,
            onTertiary = onPrimaryColor,
            tertiaryContainer = BookPaperVariant,
            onTertiaryContainer = BookInkPrimary,
            background = BookPaperBg,
            onBackground = BookInkPrimary,
            surface = BookPaperCard,
            onSurface = BookInkPrimary,
            surfaceVariant = BookPaperVariant,
            onSurfaceVariant = BookInkMuted,
            surfaceTint = Color.Transparent,
            surfaceContainerLowest = BookPaperCard,
            surfaceContainerLow = BookPaperCard,
            surfaceContainer = BookPaperBg,
            surfaceContainerHigh = BookPaperCard,
            surfaceContainerHighest = BookPaperHigh,
            outline = BookHairlineLight,
            outlineVariant = BookHairlineLight.copy(alpha = 0.7f),
        )
    }

    val family = when (fontStyle) {
        AppFontStyle.SERIF, AppFontStyle.SERIF_ITALIC -> FontFamily.Serif
        AppFontStyle.SANS -> FontFamily.SansSerif
        AppFontStyle.MONOSPACE -> FontFamily.Monospace
    }
    val textFontStyle = if (fontStyle == AppFontStyle.SERIF_ITALIC) FontStyle.Italic else FontStyle.Normal

    val scale = fontScale.coerceIn(0.85f, 1.25f)
    fun androidx.compose.ui.text.TextStyle.scaled() = copy(
        fontFamily = family,
        fontStyle = textFontStyle,
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

    CompositionLocalProvider(
        LocalLayoutDensity provides layoutDensity,
        LocalLiquidGlassLevel provides liquidGlassLevel,
        LocalFloatingControls provides floatingControlsEnabled,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = customTypography,
            content = content,
        )
    }
}
