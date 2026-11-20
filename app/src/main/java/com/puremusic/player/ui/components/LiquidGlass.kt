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
            .background(MaterialTheme.colorScheme.surfaceVariant)
    }

    val glassBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF21252E).copy(alpha = 0.88f),
                Color(0xFF171A21).copy(alpha = 0.84f),
            ),
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF7F8FB).copy(alpha = 0.94f),
                Color(0xFFEDEFF4).copy(alpha = 0.90f),
            ),
        )
    }

    val specularRim = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.18f),
                Color.White.copy(alpha = 0.05f),
                Color.White.copy(alpha = 0.12f),
            ),
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.98f),
                Color(0xFFE0E4EC).copy(alpha = 0.70f),
                Color.White.copy(alpha = 0.90f),
            ),
        )
    }

    return this
        .clip(shape)
        .background(glassBrush)
        .border(width = 0.8.dp, brush = specularRim, shape = shape)
}
