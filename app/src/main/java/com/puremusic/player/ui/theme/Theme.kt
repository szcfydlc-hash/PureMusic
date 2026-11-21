package com.puremusic.player.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import android.content.Context
import android.graphics.Typeface
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.puremusic.player.data.model.LyricFontStyle
import java.io.File
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.core.view.WindowCompat
import com.puremusic.player.data.model.AppAccentColor
import com.puremusic.player.data.model.AppFontStyle
import com.puremusic.player.data.model.AppFontWeight
import com.puremusic.player.data.model.BackgroundStyle
import com.puremusic.player.data.model.LayoutDensity
import com.puremusic.player.data.model.LiquidGlassLevel
import com.puremusic.player.data.model.MiniPlayerStyle
import kotlin.math.tan

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

// Distinct, authentic dyed archival paper tones (~92% lightness so 宣纸浅色 is unmistakably distinct from 素白纸 #FFFFFF!)
private fun lightPaperFor(accent: AppAccentColor): PaperPalette = when (accent) {
    AppAccentColor.PRUSSIAN_BLUE -> PaperPalette(
        bg = Color(0xFFE6EDF7),
        card = Color(0xFFF5F8FC),
        variant = Color(0xFFD8E3F2),
        high = Color(0xFFCBD8EB),
        ink = Color(0xFF151C26),
        muted = Color(0xFF536173),
        outline = Color(0xFFC3D2E6),
    )
    AppAccentColor.PINE_GREEN -> PaperPalette(
        bg = Color(0xFFE7F0EC),
        card = Color(0xFFF5FAF8),
        variant = Color(0xFFD8E7E0),
        high = Color(0xFFCADDD4),
        ink = Color(0xFF14201B),
        muted = Color(0xFF50635B),
        outline = Color(0xFFC2D6CD),
    )
    AppAccentColor.CINNABAR_RED -> PaperPalette(
        bg = Color(0xFFF2E7E0),
        card = Color(0xFFFAF5F2),
        variant = Color(0xFFE7D7CE),
        high = Color(0xFFDEC9BD),
        ink = Color(0xFF211816),
        muted = Color(0xFF6B5B55),
        outline = Color(0xFFD9C5BB),
    )
    AppAccentColor.WALNUT_SEPIA -> PaperPalette(
        bg = Color(0xFFEFE7DA),
        card = Color(0xFFFAF6EE),
        variant = Color(0xFFE3D7C4),
        high = Color(0xFFD9C9B2),
        ink = Color(0xFF211B15),
        muted = Color(0xFF695D50),
        outline = Color(0xFFD5C6B2),
    )
    AppAccentColor.ANTIQUE_OCHRE -> PaperPalette(
        bg = Color(0xFFF2ECD9),
        card = Color(0xFFFAF7EE),
        variant = Color(0xFFE6DEC5),
        high = Color(0xFFDCD2B4),
        ink = Color(0xFF211D13),
        muted = Color(0xFF69614E),
        outline = Color(0xFFD8CEB2),
    )
    AppAccentColor.PURE_INK -> PaperPalette(
        bg = Color(0xFFEBEAE6),
        card = Color(0xFFF7F6F3),
        variant = Color(0xFFDFDDD7),
        high = Color(0xFFD4D2CB),
        ink = Color(0xFF181817),
        muted = Color(0xFF5E5D59),
        outline = Color(0xFFCFCDC6),
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
val LocalMiniPlayerStyle = compositionLocalOf { MiniPlayerStyle.CLASSIC_BOOK }
val LocalMiniPlayerHeightDp = compositionLocalOf { 58 }
val LocalHapticEnabled = compositionLocalOf { true }
val LocalCustomFontVersion = compositionLocalOf { 0 }

fun sidebarColorFor(isDark: Boolean): Color = if (isDark) Color(0xFF1A1A1C) else Color(0xFFEEECE7)

@Composable
fun PureMusicTheme(
    backgroundStyle: BackgroundStyle,
    accentColor: AppAccentColor,
    fontStyle: AppFontStyle,
    layoutDensity: LayoutDensity = LayoutDensity.STANDARD,
    fontScale: Float = 1.0f,
    fontWeight: AppFontWeight = AppFontWeight.NORMAL,
    fontWeightValue: Int = fontWeight.weightValue,
    fontSlantDegrees: Float = 0f,
    liquidGlassLevel: LiquidGlassLevel = LiquidGlassLevel.FULL,
    floatingControlsEnabled: Boolean = false,
    miniPlayerStyle: MiniPlayerStyle = MiniPlayerStyle.CLASSIC_BOOK,
    miniPlayerHeightDp: Int = 58,
    hapticEnabled: Boolean = true,
    customFontVersion: Int = 0,
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
        // Pure crisp neutral gallery white — zero warm tint, unmistakably distinct from 宣纸浅色!
        lightColorScheme(
            primary = primaryAccent,
            onPrimary = onPrimaryColor,
            primaryContainer = primaryAccent.copy(alpha = 0.10f),
            onPrimaryContainer = primaryAccent,
            secondary = primaryAccent.copy(alpha = 0.85f),
            onSecondary = onPrimaryColor,
            secondaryContainer = Color(0xFFF2F2F4),
            onSecondaryContainer = Color(0xFF161618),
            tertiary = primaryAccent,
            onTertiary = onPrimaryColor,
            tertiaryContainer = Color(0xFFF2F2F4),
            onTertiaryContainer = Color(0xFF161618),
            background = Color(0xFFFFFFFF),
            onBackground = Color(0xFF161618),
            surface = Color(0xFFF7F7F8),
            onSurface = Color(0xFF161618),
            surfaceVariant = Color(0xFFEEEEF0),
            onSurfaceVariant = Color(0xFF66666B),
            surfaceTint = Color.Transparent,
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFF7F7F8),
            surfaceContainer = Color(0xFFF7F7F8),
            surfaceContainerHigh = Color(0xFFEEEEF0),
            surfaceContainerHighest = Color(0xFFE5E5E8),
            outline = Color(0xFFE2E2E6),
            outlineVariant = Color(0xFFEAEAEF),
        )
    } else {
        val p = lightPaperFor(accentColor)
        lightColorScheme(
            primary = primaryAccent,
            onPrimary = onPrimaryColor,
            primaryContainer = primaryAccent.copy(alpha = 0.14f),
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
            outlineVariant = p.outline.copy(alpha = 0.75f),
        )
    }

    val themeContext = LocalContext.current
    val family = remember(fontStyle, customFontVersion) {
        resolveAppFontFamily(themeContext, fontStyle)
    }
    val clampedSlant = fontSlantDegrees.coerceIn(0f, 16f)
    val textFontStyle = if (fontStyle == AppFontStyle.SERIF_ITALIC && clampedSlant < 0.5f) {
        FontStyle.Italic
    } else {
        FontStyle.Normal
    }
    val geometricTransform = if (clampedSlant >= 0.5f) {
        TextGeometricTransform(skewX = -tan(Math.toRadians(clampedSlant.toDouble())).toFloat())
    } else {
        null
    }
    val baseWeight = FontWeight(fontWeightValue.coerceIn(200, 900))

    val scale = fontScale.coerceIn(0.85f, 1.25f)
    fun androidx.compose.ui.text.TextStyle.scaled() = copy(
        fontFamily = family,
        fontStyle = textFontStyle,
        fontWeight = baseWeight,
        textGeometricTransform = geometricTransform,
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
        LocalMiniPlayerStyle provides miniPlayerStyle,
        LocalMiniPlayerHeightDp provides miniPlayerHeightDp,
        LocalHapticEnabled provides hapticEnabled,
        LocalCustomFontVersion provides customFontVersion,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = customTypography,
            content = content,
        )
    }
}

fun resolveAppFontFamily(context: Context, style: AppFontStyle): FontFamily {
    return when (style) {
        AppFontStyle.SERIF, AppFontStyle.SERIF_ITALIC -> FontFamily.Serif
        AppFontStyle.SANS -> FontFamily.SansSerif
        AppFontStyle.MONOSPACE -> FontFamily.Monospace
        AppFontStyle.WENKAI -> loadAssetFontOrFallback(context, "fonts/lxgw_wenkai_lite.ttf", FontFamily.Serif)
        AppFontStyle.PLAYFAIR -> loadAssetFontOrFallback(context, "fonts/playfair_italic.ttf", FontFamily.Serif)
        AppFontStyle.INTER -> loadAssetFontOrFallback(context, "fonts/inter_regular.ttf", FontFamily.SansSerif)
        AppFontStyle.CUSTOM -> loadCustomUserFontOrFallback(context, FontFamily.Serif)
    }
}

fun resolveLyricFontFamily(context: Context, style: LyricFontStyle): FontFamily {
    return when (style) {
        LyricFontStyle.SERIF, LyricFontStyle.SERIF_ITALIC -> FontFamily.Serif
        LyricFontStyle.SANS -> FontFamily.SansSerif
        LyricFontStyle.MONOSPACE -> FontFamily.Monospace
        LyricFontStyle.WENKAI -> loadAssetFontOrFallback(context, "fonts/lxgw_wenkai_lite.ttf", FontFamily.Serif)
        LyricFontStyle.PLAYFAIR -> loadAssetFontOrFallback(context, "fonts/playfair_italic.ttf", FontFamily.Serif)
        LyricFontStyle.CUSTOM -> loadCustomUserFontOrFallback(context, FontFamily.Serif)
    }
}

private fun loadAssetFontOrFallback(context: Context, assetPath: String, fallback: FontFamily): FontFamily {
    return runCatching {
        val tf = Typeface.createFromAsset(context.assets, assetPath)
        FontFamily(tf)
    }.getOrDefault(fallback)
}

private fun loadCustomUserFontOrFallback(context: Context, fallback: FontFamily): FontFamily {
    return runCatching {
        val fontFile = File(File(context.filesDir, "custom_fonts"), "user_font.ttf")
        if (fontFile.exists() && fontFile.length() > 1024L) {
            FontFamily(Typeface.createFromFile(fontFile))
        } else {
            fallback
        }
    }.getOrDefault(fallback)
}
