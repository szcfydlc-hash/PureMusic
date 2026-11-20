package com.puremusic.player.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
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
import coil.request.ImageRequest
import com.puremusic.player.data.model.LyricFontStyle
import com.puremusic.player.data.model.LyricLine
import com.puremusic.player.data.model.PlayerUiState
import com.puremusic.player.data.model.RepeatMode
import com.puremusic.player.data.model.Song
import com.puremusic.player.ui.components.AlbumArtwork
import com.puremusic.player.ui.components.formatCountdown
import com.puremusic.player.ui.components.formatDuration
import com.puremusic.player.ui.theme.DynamicColorExtractor
import com.puremusic.player.ui.theme.DynamicSongPalette
import kotlinx.coroutines.isActive

@Composable
fun NowPlayingScreen(
    playerState: PlayerUiState,
    allSongs: List<Song>,
    lyrics: List<LyricLine>,
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
    onOpenCoverSearch: (Song) -> Unit,
    onOpenLyricsSearch: (Song) -> Unit,
) {
    var showFullLyrics by remember { mutableStateOf(false) }
    BackHandler {
        if (showFullLyrics) showFullLyrics = false else onClose()
    }

    val context = LocalContext.current
    val song = playerState.currentSong ?: return
    val durationMs = playerState.durationMs.coerceAtLeast(1L)

    // Real-time dynamic color extraction from current song's album art
    var extractedPalette by remember { mutableStateOf(DynamicSongPalette()) }
    LaunchedEffect(song.id, song.albumArtUri) {
        extractedPalette = DynamicColorExtractor.extractFromUri(context, song.albumArtUri)
    }

    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val baseCanvas = MaterialTheme.colorScheme.background
    val animatedGlow by animateColorAsState(
        targetValue = extractedPalette.glowColor,
        animationSpec = tween(durationMillis = 550),
        label = "ambientGlow",
    )
    val animatedAccent by animateColorAsState(
        targetValue = extractedPalette.accent,
        animationSpec = tween(durationMillis = 550),
        label = "dynamicAccent",
    )

    val primaryTextColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    val activeLyric = remember(lyrics, playerState.positionMs) {
        lyrics.lastOrNull { it.timeMs <= playerState.positionMs }
    }

    val vinylAngle = remember { Animatable(0f) }
    LaunchedEffect(playerState.isPlaying) {
        if (playerState.isPlaying) {
            while (isActive) {
                val start = vinylAngle.value % 360f
                vinylAngle.snapTo(start)
                vinylAngle.animateTo(
                    targetValue = start + 360f,
                    animationSpec = tween(durationMillis = 16_000, easing = LinearEasing),
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

    val activeAccentColor = if (liquidGlassEnabled) animatedAccent else MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(baseCanvas)
            .then(
                if (liquidGlassEnabled) {
                    Modifier.background(
                        Brush.radialGradient(
                            0.00f to animatedGlow.copy(alpha = if (isDarkTheme) 0.24f else 0.14f),
                            0.55f to animatedGlow.copy(alpha = if (isDarkTheme) 0.08f else 0.04f),
                            1.00f to Color.Transparent,
                            center = Offset(540f, 520f),
                            radius = 950f,
                        ),
                    )
                } else {
                    Modifier
                },
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 1. Smooth Toggle between 180g Vinyl Poster Wall <-> Full-Screen Synced Scrolling Lyrics
            AnimatedContent(
                targetState = showFullLyrics,
                transitionSpec = { fadeIn(tween(240)) togetherWith fadeOut(tween(180)) },
                label = "vinylOrFullLyrics",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.22f),
            ) { fullLyricsMode ->
                if (fullLyricsMode) {
                    FullScreenLyricsPane(
                        song = song,
                        lyrics = lyrics,
                        positionMs = playerState.positionMs,
                        lyricFamily = lyricFamily,
                        lyricStyleItalic = lyricStyleItalic,
                        accentColor = activeAccentColor,
                        primaryTextColor = primaryTextColor,
                        secondaryTextColor = secondaryTextColor,
                        onClosePlayer = onClose,
                        onBackToVinyl = { showFullLyrics = false },
                        onOpenLyricsSearch = { onOpenLyricsSearch(song) },
                        onOpenCoverSearch = { onOpenCoverSearch(song) },
                        onSeekTo = onSeekTo,
                    )
                } else {
                    InteractivePosterWallHeader(
                        currentSong = song,
                        allSongs = allSongs,
                        vinylRotation = vinylAngle.value,
                        backButtonBg = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
                        backIconTint = primaryTextColor,
                        sleepTimerRemainingMs = sleepTimerRemainingMs,
                        accentColor = activeAccentColor,
                        onClose = onClose,
                        onSelectSong = onSelectSongFromWall,
                        onOpenSleepTimer = onOpenSleepTimer,
                        onLongPressVinyl = { onOpenCoverSearch(song) },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            // 2. Refined Editorial Track Metadata, Lyrics, Progress & Controls (supports swipe left/right to skip, swipe down to close)
            var dragTotalX by remember { mutableFloatStateOf(0f) }
            var dragTotalY by remember { mutableFloatStateOf(0f) }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(song.id) {
                        detectDragGestures(
                            onDragStart = {
                                dragTotalX = 0f
                                dragTotalY = 0f
                            },
                            onDrag = { _, dragAmount ->
                                dragTotalX += dragAmount.x
                                dragTotalY += dragAmount.y
                            },
                            onDragEnd = {
                                val absX = kotlin.math.abs(dragTotalX)
                                val absY = kotlin.math.abs(dragTotalY)
                                when {
                                    absX > absY && dragTotalX < -85f -> onSkipNext()
                                    absX > absY && dragTotalX > 85f -> onSkipPrevious()
                                    absY > absX && dragTotalY > 110f -> onClose()
                                }
                            },
                        )
                    }
                    .padding(horizontal = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                AnimatedContent(
                    targetState = song.title to song.artist,
                    transitionSpec = { fadeIn(tween(260)) togetherWith fadeOut(tween(180)) },
                    label = "titleTransition",
                ) { (title, artist) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp,
                            ),
                            color = primaryTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = artist,
                            style = MaterialTheme.typography.bodyLarge,
                            color = secondaryTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showFullLyrics = !showFullLyrics },
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
                                fontWeight = FontWeight.Medium,
                            ),
                            color = secondaryTextColor.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                ThinSeekBar(
                    positionMs = playerState.positionMs,
                    durationMs = durationMs,
                    activeLineColor = primaryTextColor.copy(alpha = 0.78f),
                    accentColor = activeAccentColor,
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
                        style = MaterialTheme.typography.labelSmall,
                        color = secondaryTextColor,
                    )
                    Text(
                        text = formatDuration(playerState.durationMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = secondaryTextColor,
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onToggleShuffle) {
                        Icon(
                            imageVector = Icons.Rounded.Shuffle,
                            contentDescription = "随机播放",
                            tint = if (playerState.shuffleEnabled) activeAccentColor else primaryTextColor.copy(alpha = 0.85f),
                            modifier = Modifier.size(21.dp),
                        )
                    }

                    IconButton(onClick = onSkipPrevious) {
                        Icon(
                            imageVector = Icons.Rounded.SkipPrevious,
                            contentDescription = "上一首",
                            tint = primaryTextColor,
                            modifier = Modifier.size(27.dp),
                        )
                    }

                    FilledIconButton(
                        onClick = onTogglePlayPause,
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (liquidGlassEnabled) activeAccentColor else primaryTextColor,
                            contentColor = if (liquidGlassEnabled) Color.White else baseCanvas,
                        ),
                        modifier = Modifier
                            .size(66.dp)
                            .then(if (liquidGlassEnabled) Modifier.shadow(10.dp, CircleShape) else Modifier),
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
                            modifier = Modifier.size(27.dp),
                        )
                    }

                    IconButton(onClick = onCycleRepeat) {
                        Icon(
                            imageVector = when (playerState.repeatMode) {
                                RepeatMode.ONE -> Icons.Rounded.RepeatOne
                                else -> Icons.Rounded.Repeat
                            },
                            contentDescription = "循环模式",
                            tint = if (playerState.repeatMode != RepeatMode.OFF) activeAccentColor else primaryTextColor.copy(alpha = 0.85f),
                            modifier = Modifier.size(21.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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

                    IconButton(onClick = { showFullLyrics = !showFullLyrics }) {
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InteractivePosterWallHeader(
    currentSong: Song,
    allSongs: List<Song>,
    vinylRotation: Float,
    backButtonBg: Color,
    backIconTint: Color,
    sleepTimerRemainingMs: Long?,
    accentColor: Color,
    onClose: () -> Unit,
    onSelectSong: (Song) -> Unit,
    onOpenSleepTimer: () -> Unit,
    onLongPressVinyl: () -> Unit = {},
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
        val gridHeight = maxHeight * 0.68f
        val discSize = min(maxWidth * 0.74f, maxHeight * 0.72f)

        Box(modifier = Modifier.fillMaxSize()) {
            LazyHorizontalGrid(
                rows = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(gridHeight)
                    .align(Alignment.TopCenter),
                contentPadding = PaddingValues(horizontal = 3.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
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

            // Floating circular '<' back button
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

            if (sleepTimerRemainingMs != null) {
                Box(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(end = 14.dp, top = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(backButtonBg)
                        .clickable(onClick = onOpenSleepTimer)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .align(Alignment.TopEnd),
                ) {
                    Text(
                        text = formatCountdown(sleepTimerRemainingMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            // Giant glossy LP straddling row 4 with smooth song-switch transition
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = gridHeight - discSize * 0.58f),
            ) {
                AnimatedContent(
                    targetState = currentSong.id to currentSong.albumArtUri,
                    transitionSpec = {
                        (fadeIn(tween(300)) + scaleIn(initialScale = 0.92f, animationSpec = tween(300))) togetherWith
                            (fadeOut(tween(200)) + scaleOut(targetScale = 0.96f, animationSpec = tween(200)))
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
    val context = LocalContext.current
    val request = remember(song.albumArtUri) {
        ImageRequest.Builder(context)
            .data(song.albumArtUri)
            .size(220)
            .crossfade(true)
            .build()
    }
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
    ) {
        SubcomposeAsyncImage(
            model = request,
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
                    .border(2.dp, Color.White.copy(alpha = 0.9f), RoundedCornerShape(6.dp)),
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
fun SpinningVinylDisc(
    albumArtUri: Uri?,
    rotationDegrees: Float,
    discSize: Dp,
) {
    Box(
        modifier = Modifier
            .size(discSize)
            .shadow(26.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.50f))
            .clip(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        // 180g Audiophile Glossy Jet-Black LP Shader (dual diagonal hourglass specular sheen + 56 micro-grooves)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // 1. Deep jet-black virgin vinyl radial tone zones (raised outer lip, music grooves, glossy dead-wax)
            drawCircle(
                brush = Brush.radialGradient(
                    0.00f to Color(0xFF060607),
                    0.37f to Color(0xFF050506),
                    0.44f to Color(0xFF121214),
                    0.48f to Color(0xFF070708),
                    0.72f to Color(0xFF101012),
                    0.91f to Color(0xFF070708),
                    0.96f to Color(0xFF1C1C1F),
                    1.00f to Color(0xFF0A0A0B),
                    center = center,
                    radius = radius,
                ),
                radius = radius,
                center = center,
            )

            // 2. Broad primary diagonal studio hourglass sheen (upper-left to lower-right like studio-lit LP)
            drawCircle(
                brush = Brush.sweepGradient(
                    0.00f to Color.Transparent,
                    0.05f to Color.Transparent,
                    0.10f to Color(0xFFDADCE2).copy(alpha = 0.16f),
                    0.135f to Color.White.copy(alpha = 0.34f),
                    0.17f to Color(0xFFDADCE2).copy(alpha = 0.14f),
                    0.24f to Color.Transparent,
                    0.55f to Color.Transparent,
                    0.60f to Color(0xFFDADCE2).copy(alpha = 0.16f),
                    0.635f to Color.White.copy(alpha = 0.36f),
                    0.67f to Color(0xFFDADCE2).copy(alpha = 0.14f),
                    0.74f to Color.Transparent,
                    1.00f to Color.Transparent,
                    center = center,
                ),
                radius = radius * 0.965f,
                center = center,
            )

            // 3. Razor-sharp secondary specular highlight core cones inside the hourglass sheen
            drawCircle(
                brush = Brush.sweepGradient(
                    0.00f to Color.Transparent,
                    0.118f to Color.Transparent,
                    0.135f to Color.White.copy(alpha = 0.52f),
                    0.152f to Color.Transparent,
                    0.618f to Color.Transparent,
                    0.635f to Color.White.copy(alpha = 0.54f),
                    0.652f to Color.Transparent,
                    1.00f to Color.Transparent,
                    center = center,
                ),
                radius = radius * 0.955f,
                center = center,
            )

            // 4. Subtle perpendicular ambient cross-reflection for realistic 3D vinyl sheen
            drawCircle(
                brush = Brush.sweepGradient(
                    0.00f to Color.Transparent,
                    0.34f to Color.Transparent,
                    0.385f to Color.White.copy(alpha = 0.10f),
                    0.43f to Color.Transparent,
                    0.84f to Color.Transparent,
                    0.885f to Color.White.copy(alpha = 0.10f),
                    0.93f to Color.Transparent,
                    1.00f to Color.Transparent,
                    center = center,
                ),
                radius = radius * 0.95f,
                center = center,
            )

            // 5. 56 ultra-fine concentric micro-grooves + 3 glossy track-separator bands
            for (i in 0..55) {
                val factor = 0.445f + i * 0.0091f
                val isTrackGap = i == 15 || i == 31 || i == 46
                val alpha = when {
                    isTrackGap -> 0.24f
                    i % 4 == 0 -> 0.11f
                    i % 2 == 0 -> 0.055f
                    else -> 0.025f
                }
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = radius * factor,
                    center = center,
                    style = Stroke(width = if (isTrackGap) 1.5f else 0.65f),
                )
            }

            // 6. Smooth dark mirror dead-wax run-out groove ring around center label
            drawCircle(
                color = Color.Black.copy(alpha = 0.45f),
                radius = radius * 0.41f,
                center = center,
                style = Stroke(width = radius * 0.055f),
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.14f),
                radius = radius * 0.438f,
                center = center,
                style = Stroke(width = 1.0f),
            )

            // 7. Raised beveled outer LP rim highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.30f),
                radius = radius - 1.4f,
                center = center,
                style = Stroke(width = 1.6f),
            )
            drawCircle(
                color = Color.Black.copy(alpha = 0.92f),
                radius = radius - 3.6f,
                center = center,
                style = Stroke(width = 2.4f),
            )
        }

        // Clean unobstructed 37% circular album art label (zero clutter covering artwork)
        Box(
            modifier = Modifier
                .size(discSize * 0.37f)
                .rotate(rotationDegrees)
                .clip(CircleShape)
                .border(1.5.dp, Color(0xFF0A0A0B), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            AlbumArtwork(
                uri = albumArtUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                cornerRadius = discSize,
                iconSize = 24.dp,
            )
        }
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

@Composable
private fun FullScreenLyricsPane(
    song: Song,
    lyrics: List<LyricLine>,
    positionMs: Long,
    lyricFamily: FontFamily,
    lyricStyleItalic: FontStyle,
    accentColor: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    onClosePlayer: () -> Unit,
    onBackToVinyl: () -> Unit,
    onOpenLyricsSearch: () -> Unit,
    onOpenCoverSearch: () -> Unit,
    onSeekTo: (Long) -> Unit,
) {
    val activeIdx = remember(lyrics, positionMs) {
        lyrics.indexOfLast { it.timeMs <= positionMs }
    }
    val listState = rememberLazyListState()
    LaunchedEffect(activeIdx) {
        if (activeIdx >= 0 && activeIdx < lyrics.size) {
            val target = (activeIdx - 2).coerceAtLeast(0)
            listState.animateScrollToItem(target)
        }
    }

    val outlineColor = MaterialTheme.colorScheme.outline
    val pillShape = RoundedCornerShape(7.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
    ) {
        // Top Header: Close + Mini Cover/Title + Match Lyrics + Back to Vinyl
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(0.75.dp, outlineColor, CircleShape)
                    .clickable(onClick = onClosePlayer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    contentDescription = "返回",
                    tint = primaryTextColor,
                    modifier = Modifier.size(22.dp),
                )
            }

            AlbumArtwork(
                uri = song.albumArtUri,
                contentDescription = song.album,
                modifier = Modifier
                    .size(38.dp)
                    .clickable(onClick = onOpenCoverSearch),
                cornerRadius = 5.dp,
                iconSize = 16.dp,
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.labelSmall,
                    color = secondaryTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Box(
                modifier = Modifier
                    .clip(pillShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(0.75.dp, outlineColor, pillShape)
                    .clickable(onClick = onOpenLyricsSearch)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "匹配歌词",
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Box(
                modifier = Modifier
                    .clip(pillShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(0.75.dp, outlineColor, pillShape)
                    .clickable(onClick = onBackToVinyl)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "黑胶",
                    style = MaterialTheme.typography.labelSmall,
                    color = primaryTextColor,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        if (lyrics.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "暂无同步歌词",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryTextColor,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "可在线匹配带时间轴的 .lrc 歌词，或从本地导入。",
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryTextColor,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(pillShape)
                            .background(accentColor)
                            .clickable(onClick = onOpenLyricsSearch)
                            .padding(horizontal = 16.dp, vertical = 9.dp),
                    ) {
                        Text(
                            text = "在线匹配 / 导入歌词",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(pillShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(0.75.dp, outlineColor, pillShape)
                            .clickable(onClick = onOpenCoverSearch)
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                    ) {
                        Text(
                            text = "更换封面",
                            style = MaterialTheme.typography.labelMedium,
                            color = primaryTextColor,
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                itemsIndexed(lyrics) { index, line ->
                    val isActive = index == activeIdx
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                if (line.timeMs > 0L) onSeekTo(line.timeMs)
                            }
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = line.primaryText,
                            style = (if (isActive) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge).copy(
                                fontFamily = lyricFamily,
                                fontStyle = lyricStyleItalic,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            ),
                            color = if (isActive) accentColor else secondaryTextColor.copy(alpha = 0.72f),
                            textAlign = TextAlign.Center,
                        )
                        if (!line.secondaryText.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = line.secondaryText,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = lyricFamily),
                                color = if (isActive) primaryTextColor.copy(alpha = 0.88f) else secondaryTextColor.copy(alpha = 0.52f),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}
