package com.puremusic.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    elevation: Dp = 6.dp,
    darkSurface: Boolean? = null,
): Modifier {
    val isDark = darkSurface ?: (MaterialTheme.colorScheme.background.luminance() < 0.5f)
    if (!enabled) {
        return this
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.14f),
                shape = shape,
            )
    }

    val glassBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF2B2F36).copy(alpha = 0.88f),
                Color(0xFF1D2026).copy(alpha = 0.82f),
            ),
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFCFBF9).copy(alpha = 0.92f),
                Color(0xFFF0EAE0).copy(alpha = 0.86f),
            ),
        )
    }

    val specularRim = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.28f),
                Color.White.copy(alpha = 0.08f),
                Color.White.copy(alpha = 0.18f),
            ),
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.96f),
                Color(0xFFD8D0C5).copy(alpha = 0.50f),
                Color.White.copy(alpha = 0.86f),
            ),
        )
    }

    return this
        .clip(shape)
        .background(glassBrush)
        .border(width = 1.dp, brush = specularRim, shape = shape)
}
