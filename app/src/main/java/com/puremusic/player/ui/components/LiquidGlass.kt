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
    shape: Shape = RoundedCornerShape(24.dp),
    elevation: Dp = 10.dp,
    darkSurface: Boolean? = null,
): Modifier {
    val isDark = darkSurface ?: (MaterialTheme.colorScheme.background.luminance() < 0.5f)
    return if (enabled) {
        val glassBrush = if (isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.22f),
                    Color.White.copy(alpha = 0.11f),
                    Color.White.copy(alpha = 0.16f),
                ),
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.72f),
                    Color(0xFFF3EFE8).copy(alpha = 0.46f),
                    Color.White.copy(alpha = 0.58f),
                ),
            )
        }
        val specularRim = if (isDark) {
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.55f),
                    Color.White.copy(alpha = 0.14f),
                    Color.White.copy(alpha = 0.36f),
                ),
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.96f),
                    Color.White.copy(alpha = 0.35f),
                    Color.White.copy(alpha = 0.80f),
                ),
            )
        }
        this
            .shadow(elevation = elevation, shape = shape, ambientColor = Color.Black.copy(alpha = 0.18f))
            .clip(shape)
            .background(glassBrush)
            .border(width = 1.2.dp, brush = specularRim, shape = shape)
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
