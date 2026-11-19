package com.puremusic.player.ui.screens

import android.net.Uri
import android.os.Build
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
import androidx.compose.ui.draw.blur
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
import com.puremusic.player.data.model.LyricFontStyle
import com.puremusic.player.data.model.LyricLine
import com.puremusic.player.data.model.PlayerTheme
import com.puremusic.player.data.model.PlayerUiState
import com.puremusic.player.data.model.RepeatMode
import com.puremusic.player.data.model.Song
import com.puremusic.player.ui.components.AlbumArtwork
import com.puremusic.player.ui.components.formatCountdown
import com.puremusic.player.ui.components.formatDuration
import com.puremusic.player.ui.components.liquidGlass
import com.puremusic.player.ui.theme.ForestGreen
import com.puremusic.player.ui.theme.VermilionAccent
import kotlinx.coroutines.isActive

@Composable
fun NowPlayingScreen(
    playerState: PlayerUiState,
    allSongs: List<Song>,
    lyrics: List<LyricLine>,
    playerTheme: PlayerTheme,
    lyricFontStyle: LyricFontStyle,
    liquidGlassEnabled: Boolean,
    isTabletLayout: Boolean,
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

    val vinylAngle = remember { Animatable(0f) }
    val wallGlideFraction = remember { Animatable(0f) }
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
    LaunchedEffect(playerState.isPlaying) {
        if (playerState.isPlaying) {
            while (isActive) {
                val start = wallGlideFraction.value % 1f
                wallGlideFraction.snapTo(start)
                wallGlideFraction.animateTo(
                    targetValue = start + 1f,
                    animationSpec = tween(durationMillis = 28_000, easing = LinearEasing),
                )
            }
        }
    }

    val lyricFamily = when (lyricFontStyle) {
        LyricFontStyle.SERIF_ITALIC, LyricFontStyle.SERIF -> FontFamily.Serif
        LyricFontStyle.SANS -> FontFamily.SansSerif
        LyricFontStyle.MONOSPACE -> FontFamily.Monospace
    }
    val lyricStyleItalic = if (lyricFontStyle == LyricFontStyle.SERIF_ITALIC) FontStyle.Italic else FontStyle.Normal

    // Atmospheric deep forest-emerald backdrop for Poster Wall theme matching the v3.0 mockup
    val useAtmosphericEmerald = playerTheme == PlayerTheme.POSTER_WALL
    val primaryTextColor = if (useAtmosphericEmerald) Color(0xFFF6F3EE) else MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = if (useAtmosphericEmerald) Color(0xFFC8D3CC) else MaterialTheme.colorScheme.onSurfaceVariant
    val activeAccent = if (useAtmosphericEmerald) Color(0xFF28895E) else MaterialTheme.colorScheme.primary

    val bgModifier = if (useAtmosphericEmerald) {
        Modifier.background(
            Brush.verticalGradient(
                0.00f to Color(0xFF0E3B2A),
                0.45f to Color(0xFF17382A),
                0.78f to Color(0xFF232C25),
                1.00f to Color(0xFF2B2722),
            ),
        )
    } else {
        Modifier.background(MaterialTheme.colorScheme.background)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(bgModifier),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 1. Clean Top Bar with Back + Subtle Utility Icons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = onClose, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                        contentDescription = "返回",
                        tint = primaryTextColor.copy(alpha = 0.9f),
                        modifier = Modifier.size(26.dp),
                    )
                }

                if (sleepTimerRemainingMs != null) {
                    Text(
                        text = "定时 · ${formatCountdown(sleepTimerRemainingMs)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = activeAccent,
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onToggleFavorite(song) }, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = "喜欢",
                            tint = if (isFavorite) Color(0xFFFF6E6E) else primaryTextColor.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    IconButton(onClick = onOpenLyrics, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Lyrics,
                            contentDescription = "歌词",
                            tint = primaryTextColor.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    IconButton(onClick = onOpenQueue, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.QueueMusic,
                            contentDescription = "播放队列",
                            tint = primaryTextColor.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Box {
                        IconButton(onClick = { moreMenuExpanded = true }, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = Icons.Rounded.MoreHoriz,
                                contentDescription = "更多",
                                tint = primaryTextColor.copy(alpha = 0.8f),
                                modifier = Modifier.size(20.dp),
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

            Spacer(modifier = Modifier.height(6.dp))

            // 2. Hero Area (Strictly Bounded, Zero Overlap)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.1f)
                    .clipToBounds(),
                contentAlignment = Alignment.Center,
            ) {
                when (playerTheme) {
                    PlayerTheme.POSTER_WALL -> PosterWallVinylHero(
                        currentSong = song,
                        allSongs = allSongs,
                        vinylRotation = vinylAngle.value,
                        glideFraction = wallGlideFraction.value,
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

            // 3. Centered Editorial Title & Artist
            Text(
                text = song.title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp,
                ),
                color = primaryTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Serif,
                ),
                color = secondaryTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Independent-Font Synced Bilingual Lyrics
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
                            fontFamily = lyricFamily,
                            fontStyle = lyricStyleItalic,
                            fontWeight = FontWeight.Medium,
                        ),
                        color = primaryTextColor.copy(alpha = 0.94f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                    )
                    if (!activeLyric.secondaryText.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = activeLyric.secondaryText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = lyricFamily,
                                fontStyle = FontStyle.Italic,
                            ),
                            color = secondaryTextColor.copy(alpha = 0.82f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    val subtleAlbum = if (song.album.equals(song.title, ignoreCase = true)) {
                        "PureMusic · ${song.folderName}"
                    } else {
                        song.album
                    }
                    Text(
                        text = subtleAlbum,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = lyricFamily,
                            fontStyle = lyricStyleItalic,
                        ),
                        color = secondaryTextColor.copy(alpha = 0.72f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Sleek Ultra-Thin Progress Bar
            ThinSeekBar(
                positionMs = playerState.positionMs,
                durationMs = durationMs,
                activeLineColor = if (useAtmosphericEmerald) Color(0xFF3E9A6E) else Color(0xFF2A2927),
                accentColor = activeAccent,
                trackColor = secondaryTextColor.copy(alpha = 0.28f),
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
                    color = secondaryTextColor.copy(alpha = 0.85f),
                )
                Text(
                    text = formatDuration(playerState.durationMs),
                    style = MaterialTheme.typography.labelMedium,
                    color = secondaryTextColor.copy(alpha = 0.85f),
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 6. Unified 5-Button Liquid Glass Control Dock (exact match to v3.0 mockup!)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .liquidGlass(
                        enabled = liquidGlassEnabled,
                        shape = RoundedCornerShape(40.dp),
                        elevation = 14.dp,
                        darkSurface = useAtmosphericEmerald,
                    )
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onToggleShuffle) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = "随机播放",
                        tint = if (playerState.shuffleEnabled) activeAccent else primaryTextColor.copy(alpha = 0.85f),
                        modifier = Modifier.size(21.dp),
                    )
                }

                IconButton(onClick = onSkipPrevious) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = "上一首",
                        tint = primaryTextColor,
                        modifier = Modifier.size(28.dp),
                    )
                }

                FilledIconButton(
                    onClick = onTogglePlayPause,
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = ForestGreen,
                        contentColor = Color.White,
                    ),
                    modifier = Modifier
                        .size(62.dp)
                        .shadow(10.dp, CircleShape),
                ) {
                    Icon(
                        imageVector = if (playerState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (playerState.isPlaying) "暂停" else "播放",
                        modifier = Modifier.size(32.dp),
                    )
                }

                IconButton(onClick = onSkipNext) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "下一首",
                        tint = primaryTextColor,
                        modifier = Modifier.size(28.dp),
                    )
                }

                IconButton(onClick = onCycleRepeat) {
                    Icon(
                        imageVector = when (playerState.repeatMode) {
                            RepeatMode.ONE -> Icons.Rounded.RepeatOne
                            else -> Icons.Rounded.Repeat
                        },
                        contentDescription = "循环模式",
                        tint = if (playerState.repeatMode != RepeatMode.OFF) activeAccent else primaryTextColor.copy(alpha = 0.85f),
                        modifier = Modifier.size(21.dp),
                    )
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
    glideFraction: Float,
    modifier: Modifier = Modifier,
) {
    val wallRows = remember(allSongs, currentSong.id) {
        val distinctAlbums = allSongs.distinctBy { it.albumId }.map { it.albumArtUri }
        val pool = if (distinctAlbums.isNotEmpty()) distinctAlbums else listOf(currentSong.albumArtUri)
        List(6) { row ->
            val baseFive = List(5) { col -> pool[(row * 7 + col * 3) % pool.size] }
            baseFive + baseFive
        }
    }

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        val targetRatio = 5f / 6f
        val wallWidth = min(maxWidth, maxHeight * targetRatio)
        val wallHeight = wallWidth / targetRatio
        val discSize = min(wallWidth * 0.72f, 240.dp)
        val gap = 6.dp
        val tileSize = (wallWidth - gap * 4) / 5f
        val cycleWidth = (tileSize + gap) * 5f
        val softBlurMod = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Modifier.blur(4.5.dp)
        } else {
            Modifier
        }

        Box(
            modifier = Modifier.size(width = wallWidth, height = wallHeight),
            contentAlignment = Alignment.Center,
        ) {
            // Softly blurred gliding 5x6 album wall
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds()
                    .then(softBlurMod),
                verticalArrangement = Arrangement.spacedBy(gap),
            ) {
                for (row in 0 until 6) {
                    val shiftDp = if (row % 2 == 0) {
                        -cycleWidth * glideFraction
                    } else {
                        -cycleWidth * (1f - glideFraction)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clipToBounds(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxHeight()
                                .offset(x = shiftDp),
                            horizontalArrangement = Arrangement.spacedBy(gap),
                        ) {
                            wallRows[row].forEach { uri ->
                                AlbumArtwork(
                                    uri = uri,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .width(tileSize)
                                        .fillMaxHeight(),
                                    cornerRadius = 5.dp,
                                    iconSize = 14.dp,
                                )
                            }
                        }
                    }
                }
            }

            // Atmospheric emerald vignette blending edges seamlessly into background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.00f to Color(0xFF0E3B2A).copy(alpha = 0.55f),
                            0.20f to Color.Transparent,
                            0.78f to Color.Transparent,
                            1.00f to Color(0xFF17382A).copy(alpha = 0.92f),
                        ),
                    ),
            )

            // Crisp giant glossy 12-inch LP floating sharply in center
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
        val sleeveSize = min(maxHeight * 0.84f, maxWidth * 0.67f)
        val discSize = sleeveSize * 0.94f
        val totalWidth = sleeveSize + discSize * 0.42f

        Box(
            modifier = Modifier
                .size(width = min(totalWidth, maxWidth), height = sleeveSize + 16.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                SpinningVinylDisc(
                    albumArtUri = currentSong.albumArtUri,
                    rotationDegrees = vinylRotation,
                    discSize = discSize,
                )
            }

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
            .shadow(24.dp, CircleShape)
            .clip(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            drawCircle(
                brush = Brush.radialGradient(
                    0.00f to Color(0xFF121212),
                    0.42f to Color(0xFF090909),
                    0.75f to Color(0xFF161616),
                    0.95f to Color(0xFF0A0A0A),
                    1.00f to Color(0xFF242424),
                    center = center,
                    radius = radius,
                ),
                radius = radius,
                center = center,
            )

            drawCircle(
                brush = Brush.sweepGradient(
                    0.00f to Color.Transparent,
                    0.12f to Color.Transparent,
                    0.20f to Color.White.copy(alpha = 0.16f),
                    0.24f to Color.White.copy(alpha = 0.38f),
                    0.28f to Color.White.copy(alpha = 0.14f),
                    0.36f to Color.Transparent,
                    0.62f to Color.Transparent,
                    0.70f to Color.White.copy(alpha = 0.16f),
                    0.74f to Color.White.copy(alpha = 0.40f),
                    0.78f to Color.White.copy(alpha = 0.14f),
                    0.86f to Color.Transparent,
                    1.00f to Color.Transparent,
                    center = center,
                ),
                radius = radius * 0.96f,
                center = center,
            )

            for (i in 0..22) {
                val factor = 0.43f + i * 0.023f
                val isTrackGap = i == 7 || i == 15
                val alpha = when {
                    isTrackGap -> 0.17f
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

            drawCircle(
                color = Color.White.copy(alpha = 0.14f),
                radius = radius * 0.405f,
                center = center,
                style = Stroke(width = 1.6f),
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.28f),
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

        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(Color(0xFF141414))
                .border(1.2.dp, Color.White.copy(alpha = 0.4f), CircleShape),
        )
    }
}

@Composable
private fun ThinSeekBar(
    positionMs: Long,
    durationMs: Long,
    activeLineColor: Color,
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
                color = activeLineColor,
                start = Offset(0f, y),
                end = Offset(activeX, y),
                strokeWidth = 3.8f,
                cap = StrokeCap.Round,
            )
            if (isDragging) {
                drawCircle(
                    color = accentColor.copy(alpha = 0.26f),
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
