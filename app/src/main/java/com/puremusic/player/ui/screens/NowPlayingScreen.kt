package com.puremusic.player.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.puremusic.player.data.model.LyricLine
import com.puremusic.player.data.model.PlayerTheme
import com.puremusic.player.data.model.PlayerUiState
import com.puremusic.player.data.model.RepeatMode
import com.puremusic.player.data.model.Song
import com.puremusic.player.ui.components.AlbumArtwork
import com.puremusic.player.ui.components.formatCountdown
import com.puremusic.player.ui.components.formatDuration
import com.puremusic.player.ui.theme.VermilionAccent
import kotlinx.coroutines.isActive

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

    // Smooth continuous rotation that preserves exact angle on pause
    val vinylAngle = remember { Animatable(0f) }
    LaunchedEffect(playerState.isPlaying) {
        if (playerState.isPlaying) {
            while (isActive) {
                val start = vinylAngle.value % 360f
                vinylAngle.snapTo(start)
                vinylAngle.animateTo(
                    targetValue = start + 360f,
                    animationSpec = tween(durationMillis = 18_000, easing = LinearEasing),
                )
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 1. Clean Dedicated Header Row (never overlapped)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                        contentDescription = "返回曲库",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                        modifier = Modifier.size(26.dp),
                    )
                }
                if (sleepTimerRemainingMs != null) {
                    Text(
                        text = "定时关闭 · ${formatCountdown(sleepTimerRemainingMs)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
                Spacer(modifier = Modifier.size(36.dp))
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 2. Strictly Bounded Hero Area (zero overflow into header or title)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clipToBounds(),
                contentAlignment = Alignment.Center,
            ) {
                when (playerTheme) {
                    PlayerTheme.POSTER_WALL -> PosterWallVinylHero(
                        currentSong = song,
                        allSongs = allSongs,
                        vinylRotation = vinylAngle.value,
                        modifier = Modifier.fillMaxSize(),
                    )

                    PlayerTheme.VERMILION_CREAM -> VermilionCreamHero(
                        currentSong = song,
                        vinylRotation = vinylAngle.value,
                        modifier = Modifier.fillMaxSize(),
                    )

                    PlayerTheme.WARM_WOOD -> WarmWoodStudioHero(
                        currentSong = song,
                        vinylRotation = vinylAngle.value,
                        modifier = Modifier.fillMaxSize(),
                    )

                    PlayerTheme.RETRO_DECK -> RetroHiFiDeckHero(
                        currentSong = song,
                        isPlaying = playerState.isPlaying,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Editorial Centered Track Title & Artist
            Text(
                text = song.title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.2.sp,
                ),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Normal,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp),
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Synced Bilingual Lyrics Preview (graceful spacing if no .lrc)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
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
                        Spacer(modifier = Modifier.height(3.dp))
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
                    val subtleSubtitle = if (song.album.equals(song.title, ignoreCase = true)) {
                        "本地离线曲库 · ${song.folderName}"
                    } else {
                        song.album
                    }
                    Text(
                        text = subtleSubtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5. Sleek Ultra-Thin Progress Bar
            ThinSeekBar(
                positionMs = playerState.positionMs,
                durationMs = durationMs,
                accentColor = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                onSeekTo = onSeekTo,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
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

            Spacer(modifier = Modifier.height(14.dp))

            // 6. Primary Transport Controls
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
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.88f),
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
                        .shadow(10.dp, CircleShape),
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
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.88f),
                        modifier = Modifier.size(30.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 7. Bottom 6-Icon Minimalist Action Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
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
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
                        },
                        modifier = Modifier.size(21.dp),
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
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
                        },
                        modifier = Modifier.size(21.dp),
                    )
                }

                IconButton(onClick = { onToggleFavorite(song) }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "喜欢",
                        tint = if (isFavorite) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
                        },
                        modifier = Modifier.size(21.dp),
                    )
                }

                IconButton(onClick = onOpenLyrics) {
                    Icon(
                        imageVector = Icons.Rounded.Lyrics,
                        contentDescription = "歌词",
                        tint = if (lyrics.isNotEmpty()) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
                        },
                        modifier = Modifier.size(21.dp),
                    )
                }

                IconButton(onClick = onOpenQueue) {
                    Icon(
                        imageVector = Icons.Rounded.QueueMusic,
                        contentDescription = "播放队列",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
                        modifier = Modifier.size(21.dp),
                    )
                }

                Box {
                    IconButton(onClick = { moreMenuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Rounded.MoreHoriz,
                            contentDescription = "更多",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
                            modifier = Modifier.size(21.dp),
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
        // Stagger indices so identical covers avoid sitting adjacent to each other
        List(30) { index ->
            val row = index / 5
            val col = index % 5
            pool[(row * 7 + col * 3) % pool.size]
        }
    }

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        // Fit 5x6 poster wall strictly inside available height & width
        val targetRatio = 5f / 6f
        val wallWidth = min(maxWidth, maxHeight * targetRatio)
        val wallHeight = wallWidth / targetRatio
        val discSize = min(wallWidth * 0.64f, 224.dp)

        Box(
            modifier = Modifier.size(width = wallWidth, height = wallHeight),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
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
                                    .fillMaxHeight()
                                    .shadow(1.5.dp, RoundedCornerShape(3.dp)),
                                cornerRadius = 3.dp,
                                iconSize = 14.dp,
                            )
                        }
                    }
                }
            }

            SpinningVinylDisc(
                albumArtUri = currentSong.albumArtUri,
                rotationDegrees = vinylRotation,
                discSize = discSize,
            )
        }
    }
}

@Composable
private fun VermilionCreamHero(
    currentSong: Song,
    vinylRotation: Float,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        val cardWidth = min(maxWidth, maxHeight * 1.18f)
        val cardHeight = min(maxHeight * 0.88f, cardWidth / 1.18f)
        val discSize = min(cardHeight * 0.78f, 212.dp)

        Box(
            modifier = Modifier
                .size(width = cardWidth, height = cardHeight)
                .shadow(16.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(VermilionAccent, Color(0xFFB33213)),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            SpinningVinylDisc(
                albumArtUri = currentSong.albumArtUri,
                rotationDegrees = vinylRotation,
                discSize = discSize,
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
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        // Large square sleeve (~67% width) + 94% height glossy LP peeking out to the right
        val sleeveSize = min(maxHeight * 0.84f, maxWidth * 0.67f)
        val discSize = sleeveSize * 0.94f
        val totalWidth = sleeveSize + discSize * 0.42f

        Box(
            modifier = Modifier
                .size(width = min(totalWidth, maxWidth), height = sleeveSize + 16.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            // Glossy 12-inch LP peeking ~40% past the right edge of the sleeve
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd),
            ) {
                SpinningVinylDisc(
                    albumArtUri = currentSong.albumArtUri,
                    rotationDegrees = vinylRotation,
                    discSize = discSize,
                )
            }

            // Crisp square cardboard album sleeve with subtle right-edge highlight & drop shadow
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(sleeveSize)
                    .shadow(18.dp, RoundedCornerShape(4.dp)),
            ) {
                AlbumArtwork(
                    uri = currentSong.albumArtUri,
                    contentDescription = currentSong.album,
                    modifier = Modifier.fillMaxSize(),
                    cornerRadius = 4.dp,
                    iconSize = 56.dp,
                )
                // Paper sleeve opening edge highlight on right side
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(2.dp)
                        .background(Color.White.copy(alpha = 0.42f)),
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
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        val deckWidth = min(maxWidth, maxHeight * 1.25f)
        val windowHeight = min(maxHeight * 0.56f, 180.dp)
        val artSize = windowHeight - 20.dp

        Column(
            modifier = Modifier
                .width(deckWidth)
                .shadow(12.dp, RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFE0DCD4), Color(0xFFC0BBB0)),
                    ),
                )
                .border(1.5.dp, Color(0xFF8C877E), RoundedCornerShape(18.dp))
                .padding(16.dp),
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
                        .background(if (isPlaying) Color(0xFF1B7A43) else Color(0xFFB7362A)),
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(windowHeight)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF191919))
                    .padding(10.dp),
                contentAlignment = Alignment.Center,
            ) {
                AlbumArtwork(
                    uri = currentSong.albumArtUri,
                    contentDescription = currentSong.album,
                    modifier = Modifier.size(artSize),
                    cornerRadius = 8.dp,
                    iconSize = 44.dp,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(9.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF9C968C)),
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
            .shadow(22.dp, CircleShape)
            .clip(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        // Photorealistic 12-inch glossy acetate LP shader with fixed studio hourglass sheen
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // 1. Rich obsidian vinyl base with subtle radial depth
            drawCircle(
                brush = Brush.radialGradient(
                    0.00f to Color(0xFF121212),
                    0.42f to Color(0xFF0A0A0A),
                    0.75f to Color(0xFF161616),
                    0.95f to Color(0xFF0B0B0B),
                    1.00f to Color(0xFF222222),
                    center = center,
                    radius = radius,
                ),
                radius = radius,
                center = center,
            )

            // 2. Dual bright hourglass studio reflections (upper-right / lower-left satin sheen)
            drawCircle(
                brush = Brush.sweepGradient(
                    0.00f to Color.Transparent,
                    0.12f to Color.Transparent,
                    0.20f to Color.White.copy(alpha = 0.14f),
                    0.24f to Color.White.copy(alpha = 0.34f),
                    0.28f to Color.White.copy(alpha = 0.12f),
                    0.36f to Color.Transparent,
                    0.62f to Color.Transparent,
                    0.70f to Color.White.copy(alpha = 0.14f),
                    0.74f to Color.White.copy(alpha = 0.36f),
                    0.78f to Color.White.copy(alpha = 0.12f),
                    0.86f to Color.Transparent,
                    1.00f to Color.Transparent,
                    center = center,
                ),
                radius = radius * 0.96f,
                center = center,
            )

            // 3. Dense fine satin micro-grooves across playable vinyl surface
            for (i in 0..22) {
                val factor = 0.43f + i * 0.023f
                val isTrackGap = i == 7 || i == 15
                val alpha = when {
                    isTrackGap -> 0.16f
                    i % 2 == 0 -> 0.09f
                    else -> 0.04f
                }
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = radius * factor,
                    center = center,
                    style = Stroke(width = if (isTrackGap) 1.5f else 0.95f),
                )
            }

            // 4. Glossy inner run-out border around center label
            drawCircle(
                color = Color.White.copy(alpha = 0.14f),
                radius = radius * 0.405f,
                center = center,
                style = Stroke(width = 1.6f),
            )

            // 5. Crisp metallic outer rim bevel
            drawCircle(
                color = Color.White.copy(alpha = 0.24f),
                radius = radius - 1.5f,
                center = center,
                style = Stroke(width = 2.4f),
            )
            drawCircle(
                color = Color.Black.copy(alpha = 0.85f),
                radius = radius - 3.6f,
                center = center,
                style = Stroke(width = 1.8f),
            )
        }

        // Authentic 38% LP circular center album label
        AlbumArtwork(
            uri = albumArtUri,
            contentDescription = null,
            modifier = Modifier
                .size(discSize * 0.38f)
                .rotate(rotationDegrees)
                .clip(CircleShape)
                .border(2.dp, Color(0xFF0E0E0E), CircleShape),
            cornerRadius = discSize,
            iconSize = 24.dp,
        )

        // Realistic spindle center hole
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(Color(0xFF141414))
                .border(1.2.dp, Color.White.copy(alpha = 0.38f), CircleShape),
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
            .height(26.dp)
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
                .height(18.dp),
        ) {
            val y = size.height / 2f
            val activeX = size.width * progressFraction
            drawLine(
                color = trackColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 3.2f,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = Color(0xFF2A2927),
                start = Offset(0f, y),
                end = Offset(activeX, y),
                strokeWidth = 3.8f,
                cap = StrokeCap.Round,
            )
            if (isDragging) {
                drawCircle(
                    color = accentColor.copy(alpha = 0.22f),
                    radius = 18f,
                    center = Offset(activeX, y),
                )
            }
            drawCircle(
                color = accentColor,
                radius = if (isDragging) 11f else 9f,
                center = Offset(activeX, y),
            )
        }
    }
}
