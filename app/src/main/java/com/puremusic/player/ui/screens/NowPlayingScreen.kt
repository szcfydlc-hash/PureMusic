package com.puremusic.player.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.puremusic.player.data.model.LyricFontStyle
import com.puremusic.player.data.model.LyricLine
import com.puremusic.player.data.model.PlayerTheme
import com.puremusic.player.data.model.PlayerUiState
import com.puremusic.player.data.model.RepeatMode
import com.puremusic.player.data.model.Song
import com.puremusic.player.ui.components.AlbumArtwork
import com.puremusic.player.ui.components.formatCountdown
import com.puremusic.player.ui.theme.DynamicColorExtractor
import com.puremusic.player.ui.theme.DynamicSongPalette
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
    onSelectSongFromWall: (Song) -> Unit,
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

    val context = LocalContext.current
    val song = playerState.currentSong ?: return
    val durationMs = playerState.durationMs.coerceAtLeast(1L)

    // Dynamic color extraction from current song's album art
    var extractedPalette by remember { mutableStateOf(DynamicSongPalette()) }
    LaunchedEffect(song.id, song.albumArtUri) {
        extractedPalette = DynamicColorExtractor.extractFromUri(context, song.albumArtUri)
    }

    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val animatedBg by animateColorAsState(
        targetValue = if (isDarkTheme) extractedPalette.backgroundDark else extractedPalette.backgroundLight,
        animationSpec = tween(durationMillis = 550),
        label = "dynamicBg",
    )
    val animatedAccent by animateColorAsState(
        targetValue = extractedPalette.accent,
        animationSpec = tween(durationMillis = 550),
        label = "dynamicAccent",
    )

    val primaryTextColor = if (isDarkTheme) Color(0xFFF3EFEA) else Color(0xFF1D1B1A)
    val secondaryTextColor = if (isDarkTheme) Color(0xFFB3ADA6) else Color(0xFF6A635D)

    val activeLyric = remember(lyrics, playerState.positionMs) {
        lyrics.lastOrNull { it.timeMs <= playerState.positionMs }
    }

    // Smooth vinyl rotation preserving angle on pause (poster wall stays stationary unless user swipes!)
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

    val lyricFamily = when (lyricFontStyle) {
        LyricFontStyle.SERIF_ITALIC, LyricFontStyle.SERIF -> FontFamily.Serif
        LyricFontStyle.SANS -> FontFamily.SansSerif
        LyricFontStyle.MONOSPACE -> FontFamily.Monospace
    }
    val lyricStyleItalic = if (lyricFontStyle == LyricFontStyle.SERIF_ITALIC) FontStyle.Italic else FontStyle.Normal

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(animatedBg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 1. HERO SECTION
            if (playerTheme == PlayerTheme.POSTER_WALL) {
                // Exact reference layout: edge-to-edge 4-row manually-swipeable & tappable wall at very top + giant LP straddling bottom edge + floating circular '<' button
                InteractivePosterWallHeader(
                    currentSong = song,
                    allSongs = allSongs,
                    vinylRotation = vinylAngle.value,
                    backButtonBg = animatedBg.copy(alpha = 0.92f),
                    backIconTint = primaryTextColor,
                    onClose = onClose,
                    onSelectSong = onSelectSongFromWall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.22f),
                )
            } else {
                // Other 3 selectable themes (Vermilion Cream / Warm Wood Studio / Retro Hi-Fi Deck)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    IconButton(onClick = onClose, modifier = Modifier.size(38.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                            contentDescription = "返回",
                            tint = primaryTextColor,
                        )
                    }
                    if (sleepTimerRemainingMs != null) {
                        Text(
                            text = "定时 ${formatCountdown(sleepTimerRemainingMs)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = animatedAccent,
                        )
                    }
                    Spacer(modifier = Modifier.size(38.dp))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.1f)
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    when (playerTheme) {
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
                        PlayerTheme.POSTER_WALL -> Unit
                    }
                }
            }

            // 2. LOWER EDITORIAL CONTENT (Title · Artist · Lyric · Thin Seek Bar · 5-Button Controls · 5-Icon Footer)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = song.title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.2.sp,
                    ),
                    color = primaryTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(3.dp))
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

                // Synced Lyrics Line (or album credit line)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    if (activeLyric != null) {
                        Text(
                            text = activeLyric.primaryText,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = lyricFamily,
                                fontStyle = lyricStyleItalic,
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = primaryTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                        )
                        if (!activeLyric.secondaryText.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = activeLyric.secondaryText,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = lyricFamily),
                                color = secondaryTextColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                            )
                        }
                    } else {
                        Text(
                            text = if (song.album.equals(song.title, ignoreCase = true)) {
                                "歌手 : ${song.artist}"
                            } else {
                                "专辑 : ${song.album}"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = lyricFamily,
                                fontStyle = lyricStyleItalic,
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = primaryTextColor.copy(alpha = 0.86f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Ultra-Thin Minimalist Progress Line with Dynamic Color Dot
                ThinSeekBar(
                    positionMs = playerState.positionMs,
                    durationMs = durationMs,
                    activeLineColor = primaryTextColor.copy(alpha = 0.78f),
                    accentColor = animatedAccent,
                    trackColor = secondaryTextColor.copy(alpha = 0.28f),
                    onSeekTo = onSeekTo,
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 5-Button Transport Row: Shuffle · Prev · Dynamic Circle Play/Pause · Next · Repeat
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onToggleShuffle) {
                        Icon(
                            imageVector = Icons.Rounded.Shuffle,
                            contentDescription = "随机播放",
                            tint = if (playerState.shuffleEnabled) animatedAccent else primaryTextColor.copy(alpha = 0.85f),
                            modifier = Modifier.size(21.dp),
                        )
                    }

                    IconButton(onClick = onSkipPrevious) {
                        Icon(
                            imageVector = Icons.Rounded.SkipPrevious,
                            contentDescription = "上一首",
                            tint = primaryTextColor,
                            modifier = Modifier.size(26.dp),
                        )
                    }

                    FilledIconButton(
                        onClick = onTogglePlayPause,
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = animatedAccent,
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

                    IconButton(onClick = onSkipNext) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = "下一首",
                            tint = primaryTextColor,
                            modifier = Modifier.size(26.dp),
                        )
                    }

                    IconButton(onClick = onCycleRepeat) {
                        Icon(
                            imageVector = when (playerState.repeatMode) {
                                RepeatMode.ONE -> Icons.Rounded.RepeatOne
                                else -> Icons.Rounded.Repeat
                            },
                            contentDescription = "循环模式",
                            tint = if (playerState.repeatMode != RepeatMode.OFF) animatedAccent else primaryTextColor.copy(alpha = 0.85f),
                            modifier = Modifier.size(21.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom 5-Icon Utility Row matching exact photo: Heart · Queue · EQ/Audio · Lyrics · Settings
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { onToggleFavorite(song) }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = "喜欢",
                            tint = if (isFavorite) animatedAccent else primaryTextColor.copy(alpha = 0.82f),
                            modifier = Modifier.size(22.dp),
                        )
                    }

                    IconButton(onClick = onOpenQueue) {
                        Icon(
                            imageVector = Icons.Rounded.QueueMusic,
                            contentDescription = "播放队列",
                            tint = primaryTextColor.copy(alpha = 0.82f),
                            modifier = Modifier.size(22.dp),
                        )
                    }

                    IconButton(onClick = onOpenEqualizer) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = "音效与均衡器",
                            tint = primaryTextColor.copy(alpha = 0.82f),
                            modifier = Modifier.size(22.dp),
                        )
                    }

                    IconButton(onClick = onOpenLyrics) {
                        Icon(
                            imageVector = Icons.Rounded.Lyrics,
                            contentDescription = "歌词",
                            tint = if (lyrics.isNotEmpty()) animatedAccent else primaryTextColor.copy(alpha = 0.82f),
                            modifier = Modifier.size(22.dp),
                        )
                    }

                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "外观与设置",
                            tint = primaryTextColor.copy(alpha = 0.82f),
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InteractivePosterWallHeader(
    currentSong: Song,
    allSongs: List<Song>,
    vinylRotation: Float,
    backButtonBg: Color,
    backIconTint: Color,
    onClose: () -> Unit,
    onSelectSong: (Song) -> Unit,
    modifier: Modifier = Modifier,
) {
    val wallSongs = remember(allSongs, currentSong.id) {
        if (allSongs.size >= 24) {
            allSongs
        } else if (allSongs.isNotEmpty()) {
            List(24) { idx -> allSongs[idx % allSongs.size] }
        } else {
            List(24) { currentSong }
        }
    }

    BoxWithConstraints(modifier = modifier) {
        // 4-row manually scrollable grid occupies top ~72% of header; giant vinyl straddles its bottom edge
        val gridHeight = maxHeight * 0.72f
        val discSize = min(maxWidth * 0.64f, maxHeight * 0.62f)

        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Edge-to-edge 4-row manually swipeable & tappable poster wall (stationary while playing!)
            LazyHorizontalGrid(
                rows = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(gridHeight)
                    .align(Alignment.TopCenter),
                contentPadding = PaddingValues(horizontal = 3.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                items(wallSongs.size) { index ->
                    val tileSong = wallSongs[index]
                    PosterWallTile(
                        song = tileSong,
                        isCurrent = tileSong.id == currentSong.id,
                        onClick = { onSelectSong(tileSong) },
                    )
                }
            }

            // 2. Floating Circular '<' Back Button on top-left
            Box(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(start = 14.dp, top = 8.dp)
                    .size(40.dp)
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(backButtonBg)
                    .clickable(onClick = onClose)
                    .align(Alignment.TopStart),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    contentDescription = "返回曲库",
                    tint = backIconTint,
                    modifier = Modifier.size(24.dp),
                )
            }

            // 3. Giant Glossy Black 12-inch LP Straddling the Bottom Edge of the 4-Row Wall
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = gridHeight - discSize * 0.56f),
            ) {
                AnimatedContent(
                    targetState = currentSong.id to currentSong.albumArtUri,
                    transitionSpec = {
                        (fadeIn(tween(320)) + scaleIn(initialScale = 0.92f, animationSpec = tween(320))) togetherWith
                            (fadeOut(tween(220)) + scaleOut(targetScale = 0.96f, animationSpec = tween(220)))
                    },
                    label = "vinylTrackSwitch",
                ) { (_, artUri) ->
                    SpinningVinylDisc(
                        albumArtUri = artUri,
                        rotationDegrees = vinylRotation,
                        discSize = discSize,
                    )
                }
            }
        }
    }
}

@Composable
private fun PosterWallTile(
    song: Song,
    isCurrent: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(2.dp))
            .clickable(onClick = onClick),
    ) {
        SubcomposeAsyncImage(
            model = song.albumArtUri,
            contentDescription = song.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            loading = { DarkVinylTileFallback() },
            error = { DarkVinylTileFallback() },
        )
        if (isCurrent) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(2.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(2.dp)),
            )
        }
    }
}

@Composable
private fun DarkVinylTileFallback() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(0.74f)
                .clip(CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD93829)),
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
        val deckWidth = min(maxWidth, maxHeight * 1.15f)
        val windowHeight = min(maxHeight * 0.56f, 186.dp)
        val artSize = windowHeight - 24.dp

        Column(
            modifier = Modifier
                .width(deckWidth)
                .shadow(16.dp, RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFE6E2DA), Color(0xFFC8C3B9), Color(0xFFB9B4AA)),
                    ),
                )
                .border(1.5.dp, Color(0xFF8C877E), RoundedCornerShape(18.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Top Braun-style Deck Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "PureMusic",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Color(0xFF282623),
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .width(26.dp)
                                .height(2.dp)
                                .background(Color(0xFF5A5650)),
                        )
                    }
                }
            }

            // Dark Tinted Cassette / Album Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(windowHeight)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF191A1C))
                    .border(1.dp, Color(0xFF3A3C40), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                contentAlignment = Alignment.Center,
            ) {
                AlbumArtwork(
                    uri = currentSong.albumArtUri,
                    contentDescription = currentSong.album,
                    modifier = Modifier.size(artSize),
                    cornerRadius = 6.dp,
                    iconSize = 44.dp,
                )
            }

            // Bottom Power LED + Amber VU Level Meter Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isPlaying) Color(0xFF39B54A) else Color(0xFFC93B2B)),
                    )
                    Text(
                        text = "POWER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                        ),
                        color = Color(0xFF47443F),
                    )
                }

                // Amber VU Meter Strip
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF1F2124))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val litBars = if (isPlaying) 7 else 2
                    for (idx in 0 until 9) {
                        val barColor = when {
                            idx >= litBars -> Color(0xFF3C3E42)
                            idx >= 7 -> Color(0xFFD93829)
                            else -> Color(0xFFE8A42A)
                        }
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(1.5.dp))
                                .background(barColor),
                        )
                    }
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
                strokeWidth = 3.0f,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = activeLineColor,
                start = Offset(0f, y),
                end = Offset(activeX, y),
                strokeWidth = 3.5f,
                cap = StrokeCap.Round,
            )
            if (isDragging) {
                drawCircle(
                    color = accentColor.copy(alpha = 0.25f),
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
