package com.puremusic.player.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.puremusic.player.data.model.LiquidGlassLevel
import com.puremusic.player.ui.theme.LocalLiquidGlassLevel

@Composable
fun Modifier.liquidGlass(
    enabled: Boolean,
    shape: Shape = RoundedCornerShape(28.dp),
    elevation: Dp = 6.dp,
    darkSurface: Boolean? = null,
    isPressed: Boolean = false,
    levelOverride: LiquidGlassLevel? = null,
): Modifier {
    val isDark = darkSurface ?: (MaterialTheme.colorScheme.background.luminance() < 0.5f)
    if (!enabled) {
        return this
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(0.75.dp, MaterialTheme.colorScheme.outline, shape)
    }

    val level = levelOverride ?: LocalLiquidGlassLevel.current
    val pressGlow by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = tween(durationMillis = 160),
        label = "cleanGlassPressGlow",
    )

    // Distinct, crystal-clean translucency & meniscus sheen per tier (zero muddy dark tints!)
    val (baseAlpha, topSheenAlpha, bottomBounceAlpha) = when (level) {
        LiquidGlassLevel.LOW -> Triple(0.90f, 0.10f, 0.04f)
        LiquidGlassLevel.MEDIUM -> Triple(0.76f, 0.24f, 0.08f)
        LiquidGlassLevel.HIGH -> Triple(0.62f, 0.42f, 0.14f)
        LiquidGlassLevel.FULL -> Triple(0.50f, 0.34f, 0.12f)
    }

    val crystalTint = if (isDark) Color(0xFF1E1D1B) else Color(0xFFFFFFFF)
    val crystalLowerTint = if (isDark) Color(0xFF151412) else Color(0xFFFAF8F3)
    val accentColor = MaterialTheme.colorScheme.primary

    val fillBrush = Brush.verticalGradient(
        colors = listOf(
            crystalTint.copy(alpha = (baseAlpha + 0.04f).coerceAtMost(0.96f)),
            crystalLowerTint.copy(alpha = baseAlpha),
        ),
    )

    val borderWidth: Dp = when (level) {
        LiquidGlassLevel.LOW -> 0.75.dp
        LiquidGlassLevel.MEDIUM -> 1.0.dp
        LiquidGlassLevel.HIGH -> 1.35.dp
        LiquidGlassLevel.FULL -> if (pressGlow > 0.04f) 1.8.dp else 0.dp
    }

    val rimBrush: Brush? = when {
        level == LiquidGlassLevel.FULL && pressGlow <= 0.04f -> null // Truly borderless at rest!
        level == LiquidGlassLevel.FULL -> Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = (0.98f * pressGlow).coerceIn(0f, 1f)),
                accentColor.copy(alpha = 0.72f * pressGlow),
                Color.White.copy(alpha = (0.92f * pressGlow).coerceIn(0f, 1f)),
            ),
        )
        level == LiquidGlassLevel.LOW -> Brush.linearGradient(
            colors = listOf(
                if (isDark) Color.White.copy(alpha = 0.14f) else Color(0xFFDCD6C8).copy(alpha = 0.75f),
                if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5DFD2).copy(alpha = 0.55f),
            ),
        )
        level == LiquidGlassLevel.MEDIUM -> Brush.linearGradient(
            colors = listOf(
                if (isDark) Color.White.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.96f),
                if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFD9D2C4).copy(alpha = 0.65f),
                if (isDark) Color.White.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.85f),
            ),
        )
        else -> Brush.linearGradient( // HIGH: vivid crisp crystal prism bevel
            colors = listOf(
                if (isDark) Color.White.copy(alpha = 0.44f) else Color.White,
                if (isDark) accentColor.copy(alpha = 0.22f) else Color(0xFFD6CFC0).copy(alpha = 0.45f),
                if (isDark) Color.White.copy(alpha = 0.30f) else Color.White.copy(alpha = 0.95f),
            ),
        )
    }

    return this
        .clip(shape)
        .background(fillBrush)
        .drawWithContent {
            drawContent()
            // 1. Clean top-edge specular glass meniscus arc
            drawRect(
                brush = Brush.verticalGradient(
                    0.00f to Color.White.copy(alpha = if (isDark) topSheenAlpha * 0.35f else topSheenAlpha),
                    0.28f to Color.White.copy(alpha = if (isDark) topSheenAlpha * 0.08f else topSheenAlpha * 0.18f),
                    0.48f to Color.Transparent,
                ),
            )
            // 2. Clean subtle bottom caustic bounce
            drawRect(
                brush = Brush.verticalGradient(
                    0.80f to Color.Transparent,
                    1.00f to Color.White.copy(alpha = if (isDark) bottomBounceAlpha * 0.4f else bottomBounceAlpha),
                ),
            )
            // 3. Interactive edge-glow highlight on press (especially luminous in FULL mode)
            if (pressGlow > 0.02f) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.16f * pressGlow),
                            Color.White.copy(alpha = 0.12f * pressGlow),
                            Color.Transparent,
                        ),
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = size.maxDimension * 0.70f,
                    ),
                )
            }
        }
        .then(
            if (rimBrush != null && borderWidth > 0.dp) {
                Modifier.border(width = borderWidth, brush = rimBrush, shape = shape)
            } else {
                Modifier
            },
        )
}
