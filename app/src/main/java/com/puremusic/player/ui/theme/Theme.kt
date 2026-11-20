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

private data class PaperPalette(
    val bg: Color,
    val card: Color,
    val variant: Color,
    val high: Color,
    val ink: Color,
    val muted: Color,
    val outline: Color,
)

private fun lightPaperFor(accent: AppAccentColor): PaperPalette = when (accent) {
    AppAccentColor.PRUSSIAN_BLUE -> PaperPalette(
        bg = Color(0xFFF2F5FA),
        card = Color(0xFFFAFBFD),
        variant = Color(0xFFE5ECF5),
        high = Color(0xFFD9E3F0),
        ink = Color(0xFF171D26),
        muted = Color(0xFF5F6B7A),
        outline = Color(0xFFD5DFEC),
    )
    AppAccentColor.PINE_GREEN -> PaperPalette(
        bg = Color(0xFFF2F6F4),
        card = Color(0xFFFAFDFB),
        variant = Color(0xFFE4EEE9),
        high = Color(0xFFD8E6DF),
        ink = Color(0xFF16201C),
        muted = Color(0xFF5D6E66),
        outline = Color(0xFFD4E0DA),
    )
    AppAccentColor.CINNABAR_RED -> PaperPalette(
        bg = Color(0xFFF8F4F1),
        card = Color(0xFFFDFBF9),
        variant = Color(0xFFEFE6E1),
        high = Color(0xFFE7DBD5),
        ink = Color(0xFF211A18),
        muted = Color(0xFF72645E),
        outline = Color(0xFFE6DBD6),
    )
    AppAccentColor.WALNUT_SEPIA -> PaperPalette(
        bg = Color(0xFFF6F2EC),
        card = Color(0xFFFCF9F5),
        variant = Color(0xFFECE4D9),
        high = Color(0xFFE3D8CA),
        ink = Color(0xFF211C17),
        muted = Color(0xFF70655B),
        outline = Color(0xFFE2D8CA),
    )
    AppAccentColor.ANTIQUE_OCHRE -> PaperPalette(
        bg = Color(0xFFF7F4EA),
        card = Color(0xFFFDFBF5),
        variant = Color(0xFFEEE8D6),
        high = Color(0xFFE5DEC8),
        ink = Color(0xFF201D15),
        muted = Color(0xFF706958),
        outline = Color(0xFFE4DCC6),
    )
    AppAccentColor.PURE_INK -> PaperPalette(
        bg = Color(0xFFF5F5F4),
        card = Color(0xFFFFFFFF),
        variant = Color(0xFFEAEAE8),
        high = Color(0xFFE0E0DD),
        ink = Color(0xFF181818),
        muted = Color(0xFF666664),
        outline = Color(0xFFDFDFDC),
    )
}

private fun darkPaperFor(accent: AppAccentColor): PaperPalette = when (accent) {
    AppAccentColor.PRUSSIAN_BLUE -> PaperPalette(
        bg = Color(0xFF101319),
        card = Color(0xFF171C24),
        variant = Color(0xFF202733),
        high = Color(0xFF2A3342),
        ink = Color(0xFFE6ECF5),
        muted = Color(0xFF909CAE),
        outline = Color(0xFF2C3545),
    )
    AppAccentColor.PINE_GREEN -> PaperPalette(
        bg = Color(0xFF101513),
        card = Color(0xFF161E1B),
        variant = Color(0xFF1F2A26),
        high = Color(0xFF283631),
        ink = Color(0xFFE5EFEA),
        muted = Color(0xFF8FA39A),
        outline = Color(0xFF2B3A34),
    )
    AppAccentColor.CINNABAR_RED -> PaperPalette(
        bg = Color(0xFF151212),
        card = Color(0xFF1E1A19),
        variant = Color(0xFF292321),
        high = Color(0xFF342C2A),
        ink = Color(0xFFF0E7E4),
        muted = Color(0xFFA69691),
        outline = Color(0xFF39302D),
    )
    AppAccentColor.WALNUT_SEPIA -> PaperPalette(
        bg = Color(0xFF151310),
        card = Color(0xFF1E1B17),
        variant = Color(0xFF29241F),
        high = Color(0xFF342E27),
        ink = Color(0xFFEDE6DC),
        muted = Color(0xFFA09588),
        outline = Color(0xFF383129),
    )
    AppAccentColor.ANTIQUE_OCHRE -> PaperPalette(
        bg = Color(0xFF16140F),
        card = Color(0xFF1F1C15),
        variant = Color(0xFF2A261D),
        high = Color(0xFF353024),
        ink = Color(0xFFEEE9DC),
        muted = Color(0xFFA29A86),
        outline = Color(0xFF3A3427),
    )
    AppAccentColor.PURE_INK -> PaperPalette(
        bg = Color(0xFF111112),
        card = Color(0xFF1A1A1C),
        variant = Color(0xFF242427),
        high = Color(0xFF2E2E32),
        ink = Color(0xFFECECEC),
        muted = Color(0xFF9A9A9E),
        outline = Color(0xFF323236),
    )
}

val LocalLayoutDensity = compositionLocalOf { LayoutDensity.STANDARD }
val LocalLiquidGlassLevel = compositionLocalOf { LiquidGlassLevel.FULL }
val LocalFloatingControls = compositionLocalOf { false }

fun sidebarColorFor(isDark: Boolean): Color = if (isDark) Color(0xFF1A1A1C) else Color(0xFFEEECE7)

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
        Color(0xFFDCDCDC)
    } else {
        Color(accentColor.colorHex)
    }
    val onPrimaryColor = if (accentColor == AppAccentColor.PURE_INK && useDark) {
        Color(0xFF111112)
    } else {
        Color(0xFFFCFBF7)
    }

    val colorScheme = if (useDark) {
        val p = darkPaperFor(accentColor)
        darkColorScheme(
            primary = primaryAccent,
            onPrimary = onPrimaryColor,
            primaryContainer = primaryAccent.copy(alpha = 0.22f),
            onPrimaryContainer = p.ink,
            secondary = primaryAccent.copy(alpha = 0.85f),
            onSecondary = onPrimaryColor,
            secondaryContainer = p.variant,
            onSecondaryContainer = p.ink,
            tertiary = primaryAccent,
            onTertiary = onPrimaryColor,
            tertiaryContainer = p.variant,
            onTertiaryContainer = p.ink,
            background = p.bg,
            onBackground = p.ink,
            surface = p.card,
            onSurface = p.ink,
            surfaceVariant = p.variant,
            onSurfaceVariant = p.muted,
            surfaceTint = Color.Transparent,
            surfaceContainerLowest = p.bg,
            surfaceContainerLow = p.card,
            surfaceContainer = p.card,
            surfaceContainerHigh = p.variant,
            surfaceContainerHighest = p.high,
            outline = p.outline,
            outlineVariant = p.outline.copy(alpha = 0.7f),
        )
    } else if (backgroundStyle == BackgroundStyle.PURE_WHITE) {
        val p = lightPaperFor(accentColor)
        lightColorScheme(
            primary = primaryAccent,
            onPrimary = onPrimaryColor,
            primaryContainer = primaryAccent.copy(alpha = 0.12f),
            onPrimaryContainer = primaryAccent,
            secondary = primaryAccent.copy(alpha = 0.85f),
            onSecondary = onPrimaryColor,
            secondaryContainer = p.variant,
            onSecondaryContainer = p.ink,
            tertiary = primaryAccent,
            onTertiary = onPrimaryColor,
            tertiaryContainer = p.variant,
            onTertiaryContainer = p.ink,
            background = Color(0xFFFFFFFF),
            onBackground = p.ink,
            surface = Color(0xFFFAFAFA),
            onSurface = p.ink,
            surfaceVariant = p.variant,
            onSurfaceVariant = p.muted,
            surfaceTint = Color.Transparent,
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFFAFAFA),
            surfaceContainer = Color(0xFFFAFAFA),
            surfaceContainerHigh = p.variant,
            surfaceContainerHighest = p.high,
            outline = p.outline,
            outlineVariant = p.outline.copy(alpha = 0.7f),
        )
    } else {
        val p = lightPaperFor(accentColor)
        lightColorScheme(
            primary = primaryAccent,
            onPrimary = onPrimaryColor,
            primaryContainer = primaryAccent.copy(alpha = 0.12f),
            onPrimaryContainer = primaryAccent,
            secondary = primaryAccent.copy(alpha = 0.85f),
            onSecondary = onPrimaryColor,
            secondaryContainer = p.variant,
            onSecondaryContainer = p.ink,
            tertiary = primaryAccent,
            onTertiary = onPrimaryColor,
            tertiaryContainer = p.variant,
            onTertiaryContainer = p.ink,
            background = p.bg,
            onBackground = p.ink,
            surface = p.card,
            onSurface = p.ink,
            surfaceVariant = p.variant,
            onSurfaceVariant = p.muted,
            surfaceTint = Color.Transparent,
            surfaceContainerLowest = p.card,
            surfaceContainerLow = p.card,
            surfaceContainer = p.bg,
            surfaceContainerHigh = p.card,
            surfaceContainerHighest = p.high,
            outline = p.outline,
            outlineVariant = p.outline.copy(alpha = 0.7f),
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
