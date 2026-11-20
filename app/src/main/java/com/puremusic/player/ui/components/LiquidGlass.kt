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
    shape: Shape = RoundedCornerShape(24.dp),
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
        animationSpec = tween(durationMillis = 180),
        label = "liquidGlassPressGlow",
    )

    val (topAlpha, bottomAlpha, sheenAlpha) = when (level) {
        LiquidGlassLevel.LOW -> Triple(0.84f, 0.78f, 0.14f)
        LiquidGlassLevel.MEDIUM -> Triple(0.74f, 0.66f, 0.24f)
        LiquidGlassLevel.HIGH -> Triple(0.60f, 0.50f, 0.36f)
        LiquidGlassLevel.FULL -> Triple(0.50f, 0.38f, 0.44f)
    }

    val glassBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF282521).copy(alpha = topAlpha),
                Color(0xFF181613).copy(alpha = bottomAlpha),
            ),
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFCFBF7).copy(alpha = topAlpha),
                Color(0xFFF3EEE2).copy(alpha = bottomAlpha),
            ),
        )
    }

    val accentGlowColor = MaterialTheme.colorScheme.primary
    val borderWidth = when (level) {
        LiquidGlassLevel.LOW -> 0.8.dp
        LiquidGlassLevel.MEDIUM -> 1.0.dp
        LiquidGlassLevel.HIGH -> 1.25.dp
        LiquidGlassLevel.FULL -> if (pressGlow > 0.05f) 1.5.dp else 0.45.dp
    }

    val rimBrush = if (level == LiquidGlassLevel.FULL) {
        if (pressGlow > 0.05f) {
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = (0.95f * pressGlow).coerceIn(0.25f, 1f)),
                    accentGlowColor.copy(alpha = 0.55f * pressGlow),
                    Color.White.copy(alpha = (0.85f * pressGlow).coerceIn(0.2f, 1f)),
                ),
            )
        } else {
            // Borderless minimalist look at rest in FULL mode
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = if (isDark) 0.12f else 0.45f),
                    Color.Transparent,
                    Color.White.copy(alpha = if (isDark) 0.06f else 0.25f),
                ),
            )
        }
    } else if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.28f + 0.22f * pressGlow),
                Color(0xFFDCD3C2).copy(alpha = 0.08f + 0.14f * pressGlow),
                Color.White.copy(alpha = 0.18f + 0.20f * pressGlow),
            ),
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.98f),
                Color(0xFFD8D0C0).copy(alpha = 0.55f),
                Color.White.copy(alpha = 0.88f),
            ),
        )
    }

    return this
        .clip(shape)
        .background(glassBrush)
        .drawWithContent {
            drawContent()
            // 1. Curved top-edge glass meniscus highlight
            drawRect(
                brush = Brush.verticalGradient(
                    0.00f to Color.White.copy(alpha = if (isDark) sheenAlpha * 0.42f else sheenAlpha),
                    0.36f to Color.White.copy(alpha = if (isDark) sheenAlpha * 0.10f else sheenAlpha * 0.22f),
                    0.55f to Color.Transparent,
                ),
            )
            // 2. Subtle bottom caustic bounce highlight
            drawRect(
                brush = Brush.verticalGradient(
                    0.75f to Color.Transparent,
                    1.00f to (if (isDark) Color(0xFFE6DCC8) else Color.White).copy(alpha = sheenAlpha * 0.30f),
                ),
            )
            // 3. Interactive press luminescence halo (especially vivid in FULL tier!)
            if (pressGlow > 0.02f) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentGlowColor.copy(alpha = 0.18f * pressGlow),
                            Color.White.copy(alpha = 0.14f * pressGlow),
                            Color.Transparent,
                        ),
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = size.maxDimension * 0.72f,
                    ),
                )
            }
        }
        .border(width = borderWidth, brush = rimBrush, shape = shape)
}
