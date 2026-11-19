package com.puremusic.player.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.liquidGlass(
    enabled: Boolean,
    shape: Shape = RoundedCornerShape(24.dp),
    elevation: Dp = 10.dp,
    darkSurface: Boolean? = null,
): Modifier {
    val isDark = darkSurface ?: (MaterialTheme.colorScheme.background.luminance() < 0.5f)
    if (!enabled) {
        return this
            .shadow(elevation = elevation / 2, shape = shape)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.14f),
                shape = shape,
            )
    }

    val transition = rememberInfiniteTransition(label = "liquidGlassShimmer")
    val shimmerPhase by transition.animateFloat(
        initialValue = -0.6f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4_200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerPhase",
    )

    val glassBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.18f),
                Color.White.copy(alpha = 0.08f),
                Color.White.copy(alpha = 0.13f),
            ),
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.74f),
                Color(0xFFF3EEE6).copy(alpha = 0.48f),
                Color.White.copy(alpha = 0.62f),
            ),
        )
    }

    val specularRim = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.48f),
                Color(0xFF9AD4FF).copy(alpha = 0.16f),
                Color.White.copy(alpha = 0.30f),
            ),
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.96f),
                Color(0xFFE5D8FF).copy(alpha = 0.42f),
                Color.White.copy(alpha = 0.84f),
            ),
        )
    }

    return this
        .shadow(elevation = elevation, shape = shape, ambientColor = Color.Black.copy(alpha = 0.16f))
        .clip(shape)
        .background(glassBrush)
        .drawWithContent {
            drawContent()
            val w = size.width.coerceAtLeast(1f)
            val h = size.height.coerceAtLeast(1f)
            val sweepCenter = w * shimmerPhase
            val bandWidth = w * 0.42f
            val shimmerBrush = Brush.linearGradient(
                colors = if (isDark) {
                    listOf(
                        Color.Transparent,
                        Color(0xFFA8D8FF).copy(alpha = 0.08f),
                        Color.White.copy(alpha = 0.20f),
                        Color(0xFFE3C2FF).copy(alpha = 0.08f),
                        Color.Transparent,
                    )
                } else {
                    listOf(
                        Color.Transparent,
                        Color(0xFFC9E8FF).copy(alpha = 0.16f),
                        Color.White.copy(alpha = 0.44f),
                        Color(0xFFF8D8EE).copy(alpha = 0.16f),
                        Color.Transparent,
                    )
                },
                start = Offset(sweepCenter - bandWidth, 0f),
                end = Offset(sweepCenter + bandWidth, h),
            )
            drawRect(brush = shimmerBrush)
        }
        .border(width = 1.2.dp, brush = specularRim, shape = shape)
}
