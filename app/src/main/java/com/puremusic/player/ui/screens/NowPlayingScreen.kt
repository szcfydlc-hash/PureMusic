package com.puremusic.player.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode as AnimRepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.puremusic.player.data.model.LyricLine
import com.puremusic.player.data.model.PlayerTheme
import com.puremusic.player.data.model.PlayerUiState
import com.puremusic.player.data.model.RepeatMode
import com.puremusic.player.data.model.Song
import com.puremusic.player.ui.components.AlbumArtwork
import com.puremusic.player.ui.components.formatCountdown
import com.puremusic.player.ui.components.formatDuration
import com.puremusic.player.ui.theme.VermilionAccent

@Composable
fun NowPlayingScreen(
    playerState: PlayerUiState,
    allSongs: List<Song>,
    lyrics: List<LyricLine>,
    playerTheme: PlayerTheme,
    isFavorite: Boolean,
    sleepTimerRemainingMs: Long?,
    onClose: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onOpenQueue: () -> Unit,
    onOpenLyrics: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenSettings: () -> Unit,
    onAddToPlaylist: (Song) -> Unit,
) {
    BackHandler(onBack = onClose)

    val song = playerState.currentSong ?: return
    val durationMs = playerState.durationMs.coerceAtLeast(1L)
    var moreMenuExpanded by remember { mutableStateOf(false) }

    val activeLyric = remember(lyrics, playerState.positionMs) {
        lyrics.lastOrNull { it.timeMs <= playerState.positionMs }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "vinylSpin")
    val animatedAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16_000, easing = LinearEasing),
            repeatMode = AnimRepeatMode.Restart,
        ),
        label = "vinylAngle",
    )
    val vinylRotation = if (playerState.isPlaying) animatedAngle else 0f

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Minimalist top bar with subtle back chevron
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                        contentDescription = "返回曲库",
                        modifier = Modifier.size(28.dp),
                    )
                }
                if (sleepTimerRemainingMs != null) {
                    Text(
                        text = "定时关闭 ${formatCountdown(sleepTimerRemainingMs)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
                Spacer(modifier = Modifier.size(48.dp))
            }

            // Hero Artwork Area — adapts to the 4 selectable themes
            when (playerTheme) {
                PlayerTheme.POSTER_WALL -> PosterWallVinylHero(
                    currentSong = song,
                    allSongs = allSongs,
                    vinylRotation = vinylRotation,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.15f),
                )

                PlayerTheme.VERMILION_CREAM -> VermilionCreamHero(
                    currentSong = song,
                    vinylRotation = vinylRotation,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.05f),
                )

                PlayerTheme.WARM_WOOD -> WarmWoodStudioHero(
                    currentSong = song,
                    vinylRotation = vinylRotation,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.05f),
                )

                PlayerTheme.RETRO_DECK -> RetroHiFiDeckHero(
                    currentSong = song,
                    isPlaying = playerState.isPlaying,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.05f),
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Centered Song Title & Artist (classic editorial serif feel)
            Text(
                text = song.title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Medium,
                ),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = song.artist,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Normal,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Synced Bilingual Lyrics Preview (2 lines)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                if (activeLyric != null) {
                    Text(
                        text = activeLyric.primaryText,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                    )
                    if (!activeLyric.secondaryText.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activeLyric.secondaryText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    Text(
                        text = song.album,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sleek Minimalist Thin Progress Bar
            ThinSeekBar(
                positionMs = playerState.positionMs,
                durationMs = durationMs,
                accentColor = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.28f),
                onSeekTo = onSeekTo,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = formatDuration(playerState.positionMs),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = formatDuration(playerState.durationMs),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Transport Row: Previous · Accent Circle Play/Pause · Next
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onSkipPrevious,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = "上一首",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(30.dp),
                    )
                }

                FilledIconButton(
                    onClick = onTogglePlayPause,
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                    ),
                    modifier = Modifier
                        .size(66.dp)
                        .shadow(6.dp, CircleShape),
                ) {
                    Icon(
                        imageVector = if (playerState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (playerState.isPlaying) "暂停" else "播放",
                        modifier = Modifier.size(34.dp),
                    )
                }

                IconButton(
                    onClick = onSkipNext,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "下一首",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(30.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Bottom 6-Icon Minimalist Action Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onToggleShuffle) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = "随机播放",
                        tint = if (playerState.shuffleEnabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.78f)
                        },
                        modifier = Modifier.size(22.dp),
                    )
                }

                IconButton(onClick = onCycleRepeat) {
                    Icon(
                        imageVector = when (playerState.repeatMode) {
                            RepeatMode.ONE -> Icons.Rounded.RepeatOne
                            else -> Icons.Rounded.Repeat
                        },
                        contentDescription = "循环模式",
                        tint = if (playerState.repeatMode != RepeatMode.OFF) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.78f)
                        },
                        modifier = Modifier.size(22.dp),
                    )
                }

                IconButton(onClick = { onToggleFavorite(song) }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "喜欢",
                        tint = if (isFavorite) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.78f)
                        },
                        modifier = Modifier.size(22.dp),
                    )
                }

                IconButton(onClick = onOpenLyrics) {
                    Icon(
                        imageVector = Icons.Rounded.Lyrics,
                        contentDescription = "歌词",
                        tint = if (lyrics.isNotEmpty()) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.78f)
                        },
                        modifier = Modifier.size(22.dp),
                    )
                }

                IconButton(onClick = onOpenQueue) {
                    Icon(
                        imageVector = Icons.Rounded.QueueMusic,
                        contentDescription = "播放队列",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.78f),
                        modifier = Modifier.size(22.dp),
                    )
                }

                Box {
                    IconButton(onClick = { moreMenuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Rounded.MoreHoriz,
                            contentDescription = "更多",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.78f),
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    DropdownMenu(
                        expanded = moreMenuExpanded,
                        onDismissRequest = { moreMenuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("添加到歌单") },
                            leadingIcon = { Icon(Icons.Rounded.PlaylistAdd, contentDescription = null) },
                            onClick = {
                                moreMenuExpanded = false
                                onAddToPlaylist(song)
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("音频均衡器") },
                            leadingIcon = { Icon(Icons.Rounded.Equalizer, contentDescription = null) },
                            onClick = {
                                moreMenuExpanded = false
                                onOpenEqualizer()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("定时关闭") },
                            leadingIcon = { Icon(Icons.Rounded.Timer, contentDescription = null) },
                            onClick = {
                                moreMenuExpanded = false
                                onOpenSleepTimer()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("外观与主题设置") },
                            leadingIcon = { Icon(Icons.Rounded.Palette, contentDescription = null) },
                            onClick = {
                                moreMenuExpanded = false
                                onOpenSettings()
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PosterWallVinylHero(
    currentSong: Song,
    allSongs: List<Song>,
    vinylRotation: Float,
    modifier: Modifier = Modifier,
) {
    val wallUris = remember(allSongs, currentSong.id) {
        val distinctAlbums = allSongs.distinctBy { it.albumId }.map { it.albumArtUri }
        val pool = if (distinctAlbums.isNotEmpty()) distinctAlbums else listOf(currentSong.albumArtUri)
        List(30) { index -> pool[index % pool.size] }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        // 5x6 Album Poster Wall matching editorial vinyl wall proportions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(5f / 6f),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            for (row in 0 until 6) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    for (col in 0 until 5) {
                        val uri = wallUris[row * 5 + col]
                        AlbumArtwork(
                            uri = uri,
                            contentDescription = null,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            cornerRadius = 3.dp,
                            iconSize = 14.dp,
                        )
                    }
                }
            }
        }

        // Floating Spinning Vinyl Record Overlay
        SpinningVinylDisc(
            albumArtUri = currentSong.albumArtUri,
            rotationDegrees = vinylRotation,
            discSize = 188.dp,
        )
    }
}

@Composable
private fun VermilionCreamHero(
    currentSong: Song,
    vinylRotation: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.22f)
                .shadow(14.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(VermilionAccent, Color(0xFFB53314)),
                    ),
                )
                .padding(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            SpinningVinylDisc(
                albumArtUri = currentSong.albumArtUri,
                rotationDegrees = vinylRotation,
                discSize = 210.dp,
            )
        }
    }
}

@Composable
private fun WarmWoodStudioHero(
    currentSong: Song,
    vinylRotation: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            contentAlignment = Alignment.Center,
        ) {
            // Vinyl peeking from the right side of the album sleeve
            Box(modifier = Modifier.offset(x = 58.dp)) {
                SpinningVinylDisc(
                    albumArtUri = currentSong.albumArtUri,
                    rotationDegrees = vinylRotation,
                    discSize = 196.dp,
                )
            }
            // Square Album Sleeve on the left
            Box(
                modifier = Modifier
                    .offset(x = (-32).dp)
                    .size(204.dp)
                    .shadow(14.dp, RoundedCornerShape(14.dp)),
            ) {
                AlbumArtwork(
                    uri = currentSong.albumArtUri,
                    contentDescription = currentSong.album,
                    modifier = Modifier.fillMaxSize(),
                    cornerRadius = 14.dp,
                    iconSize = 56.dp,
                )
            }
        }
    }
}

@Composable
private fun RetroHiFiDeckHero(
    currentSong: Song,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFDCD8D0), Color(0xFFC2BDB3)),
                    ),
                )
                .border(1.5.dp, Color(0xFF8C877E), RoundedCornerShape(18.dp))
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "PURE HI-FI DECK",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Color(0xFF3A3732),
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) Color(0xFF2E7D32) else Color(0xFFB7362A)),
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1D1D1D))
                    .padding(10.dp),
                contentAlignment = Alignment.Center,
            ) {
                AlbumArtwork(
                    uri = currentSong.albumArtUri,
                    contentDescription = currentSong.album,
                    modifier = Modifier
                        .size(152.dp),
                    cornerRadius = 8.dp,
                    iconSize = 48.dp,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .width(44.dp)
                            .height(10.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF9F9A90)),
                    )
                }
            }
        }
    }
}

@Composable
fun SpinningVinylDisc(
    albumArtUri: Uri?,
    rotationDegrees: Float,
    discSize: Dp,
) {
    Box(
        modifier = Modifier
            .size(discSize)
            .shadow(16.dp, CircleShape)
            .clip(CircleShape)
            .rotate(rotationDegrees),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // Base black vinyl body
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF1E1E1E), Color(0xFF0A0A0A), Color(0xFF1A1A1A)),
                    center = center,
                    radius = radius,
                ),
                radius = radius,
                center = center,
            )

            // Subtle vinyl highlight sheen
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.14f),
                        Color.Transparent,
                        Color.White.copy(alpha = 0.10f),
                        Color.Transparent,
                    ),
                    center = center,
                ),
                radius = radius * 0.96f,
                center = center,
            )

            // Concentric vinyl grooves
            val grooveFactors = listOf(0.56f, 0.64f, 0.72f, 0.80f, 0.88f, 0.94f)
            grooveFactors.forEach { factor ->
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = radius * factor,
                    center = center,
                    style = Stroke(width = 1.2f),
                )
            }
        }

        // Circular center album label
        AlbumArtwork(
            uri = albumArtUri,
            contentDescription = null,
            modifier = Modifier
                .size(discSize * 0.44f)
                .clip(CircleShape)
                .border(2.dp, Color(0xFF121212), CircleShape),
            cornerRadius = discSize,
            iconSize = 26.dp,
        )

        // Spindle center hole
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(Color(0xFF151515))
                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
        )
    }
}

@Composable
private fun ThinSeekBar(
    positionMs: Long,
    durationMs: Long,
    accentColor: Color,
    trackColor: Color,
    onSeekTo: (Long) -> Unit,
) {
    var widthPx by remember { mutableFloatStateOf(1f) }
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }

    val progressFraction = if (isDragging) {
        dragFraction
    } else {
        (positionMs.toFloat() / durationMs.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .onSizeChanged { widthPx = it.width.toFloat().coerceAtLeast(1f) }
            .pointerInput(durationMs) {
                detectTapGestures { offset ->
                    val fraction = (offset.x / widthPx).coerceIn(0f, 1f)
                    onSeekTo((fraction * durationMs).toLong())
                }
            }
            .pointerInput(durationMs) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        dragFraction = (offset.x / widthPx).coerceIn(0f, 1f)
                    },
                    onHorizontalDrag = { change, _ ->
                        dragFraction = (change.position.x / widthPx).coerceIn(0f, 1f)
                    },
                    onDragEnd = {
                        isDragging = false
                        onSeekTo((dragFraction * durationMs).toLong())
                    },
                    onDragCancel = {
                        isDragging = false
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp),
        ) {
            val y = size.height / 2f
            val activeX = size.width * progressFraction
            drawLine(
                color = trackColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 3.5f,
            )
            drawLine(
                color = Color(0xFF2B2B2B),
                start = Offset(0f, y),
                end = Offset(activeX, y),
                strokeWidth = 4f,
            )
            drawCircle(
                color = accentColor,
                radius = 10f,
                center = Offset(activeX, y),
            )
        }
    }
}
