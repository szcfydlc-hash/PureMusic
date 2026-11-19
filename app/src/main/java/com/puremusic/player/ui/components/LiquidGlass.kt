package com.puremusic.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.liquidGlass(
    enabled: Boolean,
    shape: Shape = RoundedCornerShape(22.dp),
    elevation: Dp = 8.dp,
): Modifier {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return if (enabled) {
        val glassBrush = if (isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF33373D).copy(alpha = 0.72f),
                    Color(0xFF1F2227).copy(alpha = 0.62f),
                ),
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.78f),
                    Color(0xFFF3EEE6).copy(alpha = 0.60f),
                ),
            )
        }
        val specularRim = if (isDark) {
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.34f),
                    Color.White.copy(alpha = 0.08f),
                    Color.White.copy(alpha = 0.22f),
                ),
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.94f),
                    Color.White.copy(alpha = 0.28f),
                    Color.White.copy(alpha = 0.70f),
                ),
            )
        }
        this
            .shadow(elevation = elevation, shape = shape, ambientColor = Color.Black.copy(alpha = 0.16f))
            .clip(shape)
            .background(glassBrush)
            .border(width = 1.dp, brush = specularRim, shape = shape)
    } else {
        this
            .shadow(elevation = elevation / 2, shape = shape)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.16f),
                shape = shape,
            )
    }
}
