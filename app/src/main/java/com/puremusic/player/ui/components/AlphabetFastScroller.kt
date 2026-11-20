package com.puremusic.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ALPHABET_INDEX = listOf(
    "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
    "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#",
)

@Composable
fun AlphabetFastScroller(
    onSelectLetter: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var railHeightPx by remember { mutableIntStateOf(1) }
    var activeLetter by remember { mutableStateOf<String?>(null) }

    fun letterForY(y: Float): String {
        val fraction = (y / railHeightPx.coerceAtLeast(1)).coerceIn(0f, 0.999f)
        val idx = (fraction * ALPHABET_INDEX.size).toInt().coerceIn(0, ALPHABET_INDEX.lastIndex)
        return ALPHABET_INDEX[idx]
    }

    Box(modifier = modifier, contentAlignment = Alignment.CenterEnd) {
        activeLetter?.let { letter ->
            Box(
                modifier = Modifier
                    .padding(end = 36.dp)
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = letter,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }

        Column(
            modifier = Modifier
                .width(24.dp)
                .fillMaxHeight()
                .padding(vertical = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                .onSizeChanged { railHeightPx = it.height }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val letter = letterForY(offset.y)
                        activeLetter = letter
                        onSelectLetter(letter)
                        activeLetter = null
                    }
                }
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = { offset ->
                            val letter = letterForY(offset.y)
                            activeLetter = letter
                            onSelectLetter(letter)
                        },
                        onVerticalDrag = { change, _ ->
                            val letter = letterForY(change.position.y)
                            if (letter != activeLetter) {
                                activeLetter = letter
                                onSelectLetter(letter)
                            }
                        },
                        onDragEnd = { activeLetter = null },
                        onDragCancel = { activeLetter = null },
                    )
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            ALPHABET_INDEX.forEach { letter ->
                Text(
                    text = letter,
                    fontSize = 9.sp,
                    lineHeight = 10.sp,
                    fontWeight = if (activeLetter == letter) FontWeight.Bold else FontWeight.Medium,
                    color = if (activeLetter == letter) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}
