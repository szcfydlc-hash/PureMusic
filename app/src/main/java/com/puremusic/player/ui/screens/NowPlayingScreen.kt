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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.puremusic.player.ui.components.formatDuration
import com.puremusic.player.ui.theme.DynamicColorExtractor
import com.puremusic.player.ui.theme.DynamicSongPalette
import com.puremusic.player.ui.theme.RetroRedAccent
import com.puremusic.player.ui.theme.VermilionAccent
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.sin

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

    var extractedPalette by remember { mutableStateOf(DynamicSongPalette()) }
    LaunchedEffect(song.id, song.albumArtUri) {
        extractedPalette = DynamicColorExtractor.extractFromUri(context, song.albumArtUri)
    }

    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // Poster Wall uses dynamic album-art color extraction; the other 3 themes honor their signature tactile palettes
    val targetBg = when (playerTheme) {
        PlayerTheme.POSTER_WALL -> if (isDarkTheme) extractedPalette.backgroundDark else extractedPalette.backgroundLight
        PlayerTheme.VERMILION_CREAM -> if (isDarkTheme) Color(0xFF1A1412) else Color(0xFFFAF3E8)
        PlayerTheme.WARM_WOOD -> if (isDarkTheme) Color(0xFF171513) else Color(0xFFF4EFE6)
        PlayerTheme.RETRO_DECK -> if (isDarkTheme) Color(0xFF1B1C1E) else Color(0xFFE6E2DA)
    }
    val targetAccent = when (playerTheme) {
        PlayerTheme.POSTER_WALL -> extractedPalette.accent
        PlayerTheme.VERMILION_CREAM -> VermilionAccent
        PlayerTheme.WARM_WOOD -> if (isDarkTheme) Color(0xFFCBB08C) else Color(0xFF2B2825)
        PlayerTheme.RETRO_DECK -> RetroRedAccent
    }

    val animatedBg by animateColorAsState(
        targetValue = targetBg,
        animationSpec = tween(durationMillis = 500),
        label = "playerBg",
    )
    val animatedAccent by animateColorAsState(
        targetValue = targetAccent,
        animationSpec = tween(durationMillis = 500),
        label = "playerAccent",
    )

    val primaryTextColor = if (isDarkTheme) Color(0xFFF3EFEA) else Color(0xFF1E1C1A)
    val secondaryTextColor = if (isDarkTheme) Color(0xFFB3ADA6) else Color(0xFF6B655F)
    val playIconTint = if (playerTheme == PlayerTheme.WARM_WOOD && isDarkTheme) Color(0xFF181614) else Color.White

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
                            modifier = Modifier.size(26.dp),
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
                        .weight(1.18f)
                        .padding(horizontal = 22.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    when (playerTheme) {
                        PlayerTheme.VERMILION_CREAM -> VermilionCreamHero(
                            currentSong = song,
                            vinylRotation = vinylAngle.value,
                            modifier = Modifier.fillMaxSize(),
                        )
                        PlayerTheme.WARM_WOOD -> VinylGalleryKraftHero(
                            currentSong = song,
                            vinylRotation = vinylAngle.value,
                            modifier = Modifier.fillMaxSize(),
                        )
                        PlayerTheme.RETRO_DECK -> RetroHiFiCassetteHero(
                            currentSong = song,
                            isPlaying = playerState.isPlaying,
                            reelRotation = vinylAngle.value * 2f,
                            modifier = Modifier.fillMaxSize(),
                        )
                        PlayerTheme.POSTER_WALL -> Unit
                    }
                }
            }

            // 2. LOWER EDITORIAL SECTION (Title · Artist · Lyrics · SeekBar + Timestamps · Transport · Footer)
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
                        fontFamily = if (playerTheme == PlayerTheme.RETRO_DECK) FontFamily.SansSerif else FontFamily.Serif,
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
                        fontFamily = if (playerTheme == PlayerTheme.RETRO_DECK) FontFamily.SansSerif else FontFamily.Serif,
                    ),
                    color = secondaryTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(12.dp))

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
                    activeLineColor = if (playerTheme == PlayerTheme.VERMILION_CREAM) VermilionAccent else primaryTextColor.copy(alpha = 0.78f),
                    accentColor = animatedAccent,
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

                // 5-Button Transport Row (Brushed-silver 3D hardware keys for Retro Deck, clean editorial icons for others!)
                if (playerTheme == PlayerTheme.RETRO_DECK) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        MetallicDeckKey(
                            icon = Icons.Rounded.Shuffle,
                            contentDescription = "随机播放",
                            active = playerState.shuffleEnabled,
                            onClick = onToggleShuffle,
                        )
                        MetallicDeckKey(
                            icon = Icons.Rounded.SkipPrevious,
                            contentDescription = "上一首",
                            active = false,
                            onClick = onSkipPrevious,
                        )
                        FilledIconButton(
                            onClick = onTogglePlayPause,
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = RetroRedAccent,
                                contentColor = Color.White,
                            ),
                            modifier = Modifier
                                .size(64.dp)
                                .shadow(10.dp, CircleShape)
                                .border(1.5.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                        ) {
                            Icon(
                                imageVector = if (playerState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = if (playerState.isPlaying) "暂停" else "播放",
                                modifier = Modifier.size(32.dp),
                            )
                        }
                        MetallicDeckKey(
                            icon = Icons.Rounded.SkipNext,
                            contentDescription = "下一首",
                            active = false,
                            onClick = onSkipNext,
                        )
                        MetallicDeckKey(
                            icon = if (playerState.repeatMode == RepeatMode.ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                            contentDescription = "循环模式",
                            active = playerState.repeatMode != RepeatMode.OFF,
                            onClick = onCycleRepeat,
                        )
                    }
                } else {
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
                                contentColor = playIconTint,
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
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom 5-Icon Utility Row
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
private fun MetallicDeckKey(
    icon: ImageVector,
    contentDescription: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .shadow(6.dp, CircleShape)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFECEAE4), Color(0xFFB5B0A6), Color(0xFFDAD6CE)),
                ),
            )
            .border(1.dp, Color(0xFF7A766E), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (active) RetroRedAccent else Color(0xFF232220),
            modifier = Modifier.size(20.dp),
        )
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
        val gridHeight = maxHeight * 0.72f
        val discSize = min(maxWidth * 0.64f, maxHeight * 0.62f)

        Box(modifier = Modifier.fillMaxSize()) {
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
    // Exact match to image_15ae41.jpg: Big 1:1 square rounded vermilion card + giant 84%-width glossy black LP
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        val cardSize = min(maxWidth * 0.94f, maxHeight * 0.92f)
        val discSize = cardSize * 0.84f

        Box(
            modifier = Modifier
                .size(cardSize)
                .shadow(20.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFE34A22), VermilionAccent, Color(0xFFC43714)),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(
                targetState = currentSong.id to currentSong.albumArtUri,
                transitionSpec = {
                    (fadeIn(tween(300)) + scaleIn(initialScale = 0.92f, animationSpec = tween(300))) togetherWith
                        (fadeOut(tween(200)) + scaleOut(targetScale = 0.96f, animationSpec = tween(200)))
                },
                label = "vermilionVinylSwitch",
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

@Composable
private fun VinylGalleryKraftHero(
    currentSong: Song,
    vinylRotation: Float,
    modifier: Modifier = Modifier,
) {
    // Exact match to image_15ae38.jpg: Tactile kraft-paper cardboard vinyl jacket framing the album art + 42%-peeking giant LP
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        val sleeveSize = min(maxHeight * 0.86f, maxWidth * 0.66f)
        val discSize = sleeveSize * 0.94f
        val totalWidth = min(sleeveSize + discSize * 0.42f, maxWidth)

        Box(
            modifier = Modifier.size(width = totalWidth, height = sleeveSize + 14.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            // Giant glossy LP peeking 42% out to the right
            Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                SpinningVinylDisc(
                    albumArtUri = currentSong.albumArtUri,
                    rotationDegrees = vinylRotation,
                    discSize = discSize,
                )
            }

            // Boutique Kraft Cardboard Sleeve with centered square cover art & right paper-edge highlight
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(sleeveSize)
                    .shadow(20.dp, RoundedCornerShape(3.dp))
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFD1BD9E), Color(0xFFC3AC8B), Color(0xFFBBA381)),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                AlbumArtwork(
                    uri = currentSong.albumArtUri,
                    contentDescription = currentSong.album,
                    modifier = Modifier
                        .size(sleeveSize * 0.56f)
                        .shadow(4.dp, RoundedCornerShape(2.dp)),
                    cornerRadius = 2.dp,
                    iconSize = 36.dp,
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(2.dp)
                        .background(Color.White.copy(alpha = 0.40f)),
                )
            }
        }
    }
}

@Composable
private fun RetroHiFiCassetteHero(
    currentSong: Song,
    isPlaying: Boolean,
    reelRotation: Float,
    modifier: Modifier = Modifier,
) {
    // Exact match to image_15ae5b.jpg: Brushed-aluminum Braun Hi-Fi Deck + Drawn Cassette with Dual Spinning Toothed Reels + Green POWER LED + Dual-Row Amber VU Meter!
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        val deckWidth = min(maxWidth, maxHeight * 1.06f)
        val windowHeight = min(maxHeight * 0.54f, 196.dp)

        Column(
            modifier = Modifier
                .width(deckWidth)
                .shadow(18.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFEAE6DE), Color(0xFFD2CEC5), Color(0xFFBEB9AF)),
                    ),
                )
                .border(1.2.dp, Color(0xFF8C877E), RoundedCornerShape(16.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Top Brushed-Aluminum Header
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
                    color = Color(0xFF262422),
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.5.dp)) {
                    repeat(4) {
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .height(1.8.dp)
                                .background(Color(0xFF524F49)),
                        )
                    }
                }
            }

            // Dark Glass Cassette Viewport with Drawn Cassette & Dual Spinning Toothed Reels
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(windowHeight)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF191B1E))
                    .border(1.5.dp, Color(0xFF3A3D42), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                // Subtle album art glow inside cassette label area
                AlbumArtwork(
                    uri = currentSong.albumArtUri,
                    contentDescription = currentSong.album,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.22f),
                    cornerRadius = 10.dp,
                    iconSize = 32.dp,
                )

                // Cassette Shell + Dual Spinning Tape Reels Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                ) {
                    val w = size.width
                    val h = size.height
                    // Inner cassette shell outline
                    drawRoundRect(
                        color = Color(0xFF2A2D32).copy(alpha = 0.85f),
                        topLeft = Offset(w * 0.06f, h * 0.08f),
                        size = androidx.compose.ui.geometry.Size(w * 0.88f, h * 0.84f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f, 14f),
                    )
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.14f),
                        topLeft = Offset(w * 0.06f, h * 0.08f),
                        size = androidx.compose.ui.geometry.Size(w * 0.88f, h * 0.84f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f, 14f),
                        style = Stroke(width = 1.5f),
                    )

                    // Center tape bridge window
                    drawRoundRect(
                        color = Color(0xFF121316),
                        topLeft = Offset(w * 0.22f, h * 0.28f),
                        size = androidx.compose.ui.geometry.Size(w * 0.56f, h * 0.40f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f),
                    )

                    // Draw left and right toothed spinning tape reels
                    val reelRadius = minOf(w, h) * 0.16f
                    val reelCenters = listOf(Offset(w * 0.34f, h * 0.48f), Offset(w * 0.66f, h * 0.48f))
                    reelCenters.forEach { center ->
                        // Dark tape roll
                        drawCircle(
                            color = Color(0xFF3A342E),
                            radius = reelRadius * 1.32f,
                            center = center,
                        )
                        // Hub ring
                        drawCircle(
                            color = Color(0xFFD2CEC5),
                            radius = reelRadius * 0.72f,
                            center = center,
                            style = Stroke(width = 4f),
                        )
                        // 6 rotating teeth spokes
                        for (t in 0 until 6) {
                            val rad = Math.toRadians((reelRotation + t * 60f).toDouble())
                            val sx = center.x + (reelRadius * 0.34f * cos(rad)).toFloat()
                            val sy = center.y + (reelRadius * 0.34f * sin(rad)).toFloat()
                            val ex = center.x + (reelRadius * 0.68f * cos(rad)).toFloat()
                            val ey = center.y + (reelRadius * 0.68f * sin(rad)).toFloat()
                            drawLine(
                                color = Color(0xFFE6E2DA),
                                start = Offset(sx, sy),
                                end = Offset(ex, ey),
                                strokeWidth = 3f,
                                cap = StrokeCap.Round,
                            )
                        }
                        // Center spindle hole
                        drawCircle(
                            color = Color(0xFF141517),
                            radius = reelRadius * 0.28f,
                            center = center,
                        )
                    }
                }
            }

            // Bottom Hardware Strip: Green POWER LED + VU METER label + Dual-Row Amber LED Strip
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
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(if (isPlaying) Color(0xFF42C453) else RetroRedAccent)
                            .border(1.dp, Color.Black.copy(alpha = 0.25f), CircleShape),
                    )
                    Text(
                        text = "POWER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                        ),
                        color = Color(0xFF3E3B36),
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "VU METER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.sp,
                        ),
                        color = Color(0xFF4F4B45),
                    )
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF1D1F22))
                            .padding(horizontal = 7.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        val rowLit = if (isPlaying) listOf(7, 6) else listOf(2, 1)
                        rowLit.forEach { lit ->
                            Row(horizontalArrangement = Arrangement.spacedBy(2.5.dp)) {
                                for (idx in 0 until 9) {
                                    val barColor = when {
                                        idx >= lit -> Color(0xFF3A3D42)
                                        idx >= 7 -> Color(0xFFD93829)
                                        else -> Color(0xFFE8A62E)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .width(6.dp)
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(1.dp))
                                            .background(barColor),
                                    )
                                }
                            }
                        }
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
