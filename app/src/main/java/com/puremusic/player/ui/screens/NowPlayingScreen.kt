package com.puremusic.player.ui.screens

import android.app.Activity
import android.net.Uri
import android.view.WindowManager
import androidx.compose.runtime.DisposableEffect
import com.puremusic.player.data.model.AudioTechSpec
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.graphics.Path
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
import androidx.compose.ui.platform.LocalConfiguration
import com.puremusic.player.ui.theme.LocalCustomFontVersion
import com.puremusic.player.ui.theme.resolveLyricFontFamily
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.puremusic.player.ui.theme.LocalHapticEnabled
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
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
import com.puremusic.player.data.model.ImmersionLyricStyle
import com.puremusic.player.data.model.SpectrumStyle
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

@OptIn(ExperimentalFoundationApi::class)
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
    singleRepeatRemaining: Int = 0,
    onOpenRepeatCountDialog: () -> Unit = {},
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
    isTranslatingLyrics: Boolean = false,
    onTranslateLyrics: () -> Unit = {},
    audioSpec: AudioTechSpec = AudioTechSpec(),
    lyricOffsetMs: Long = 0L,
    keepScreenOn: Boolean = false,
    carModeEnabled: Boolean = false,
    onToggleCarMode: (Boolean) -> Unit = {},
    immersionStyle: ImmersionLyricStyle = ImmersionLyricStyle.VINYL_STUDY,
    onSelectImmersionStyle: (ImmersionLyricStyle) -> Unit = {},
    spectrumStyle: SpectrumStyle = SpectrumStyle.SILK_RIBBON,
    onSelectSpectrumStyle: (SpectrumStyle) -> Unit = {},
    onAdjustLyricOffset: (Long) -> Unit = {},
    onResetLyricOffset: () -> Unit = {},
) {
    var showFullLyrics by remember { mutableStateOf(false) }
    BackHandler {
        if (showFullLyrics) showFullLyrics = false else onClose()
    }

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val hapticEnabled = LocalHapticEnabled.current
    DisposableEffect(keepScreenOn) {
        val window = (context as? Activity)?.window
        if (keepScreenOn) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
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

    val adjustedPosMs = (playerState.positionMs + lyricOffsetMs).coerceAtLeast(0L)
    val activeLyric = remember(lyrics, adjustedPosMs) {
        lyrics.lastOrNull { it.timeMs <= adjustedPosMs }
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

    val customFontVer = LocalCustomFontVersion.current
    val lyricFamily = remember(lyricFontStyle, customFontVer) {
        resolveLyricFontFamily(context, lyricFontStyle)
    }
    val lyricStyleItalic = if (lyricFontStyle == LyricFontStyle.SERIF_ITALIC) FontStyle.Italic else FontStyle.Normal

    val activeAccentColor = if (liquidGlassEnabled) animatedAccent else MaterialTheme.colorScheme.primary
    val darkBgTop by animateColorAsState(
        targetValue = lerp(Color(0xFF231F20), extractedPalette.accent, 0.56f),
        animationSpec = tween(durationMillis = 550),
        label = "darkAmbientTop",
    )
    val darkBgMid by animateColorAsState(
        targetValue = lerp(Color(0xFF1A1718), extractedPalette.accent, 0.38f),
        animationSpec = tween(durationMillis = 550),
        label = "darkAmbientMid",
    )
    val darkBgBottom by animateColorAsState(
        targetValue = lerp(Color(0xFF121011), extractedPalette.accent, 0.22f),
        animationSpec = tween(durationMillis = 550),
        label = "darkAmbientBottom",
    )
    val rootInteraction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(interactionSource = rootInteraction, indication = null) {}
            .then(
                if (isDarkTheme) {
                    Modifier.background(
                        Brush.verticalGradient(
                            0.0f to darkBgTop,
                            0.46f to darkBgMid,
                            1.0f to darkBgBottom,
                        ),
                    )
                } else {
                    Modifier.background(baseCanvas)
                },
            ),
    ) {
        if (isDarkTheme) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.radialGradient(
                        0.0f to animatedAccent.copy(alpha = 0.36f),
                        0.45f to animatedAccent.copy(alpha = 0.16f),
                        1.0f to Color.Transparent,
                        center = Offset(size.width * 0.88f, size.height * 0.14f),
                        radius = size.maxDimension * 0.62f,
                    ),
                )
            }
        }
        if (carModeEnabled) {
            MinimalistCarModeView(
                song = song,
                playerState = playerState,
                durationMs = durationMs,
                activeLyricText = activeLyric?.primaryText.orEmpty(),
                hapticEnabled = hapticEnabled,
                onTogglePlayPause = {
                    if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onTogglePlayPause()
                },
                onSkipPrevious = onSkipPrevious,
                onSkipNext = onSkipNext,
                onSeekTo = onSeekTo,
                onExitCarMode = { onToggleCarMode(false) },
            )
            return@Box
        }

        if (showFullLyrics) {
            ImmersiveLyricsHost(
                style = immersionStyle,
                onSelectStyle = onSelectImmersionStyle,
                spectrumStyle = spectrumStyle,
                onSelectSpectrumStyle = onSelectSpectrumStyle,
                song = song,
                allSongs = allSongs,
                lyrics = lyrics,
                positionMs = playerState.positionMs,
                durationMs = durationMs,
                isPlaying = playerState.isPlaying,
                vinylRotationProvider = { vinylAngle.value },
                lyricFamily = lyricFamily,
                lyricStyleItalic = lyricStyleItalic,
                dynamicAccent = extractedPalette.accent,
                isDarkTheme = isDarkTheme,
                isTranslatingLyrics = isTranslatingLyrics,
                lyricOffsetMs = lyricOffsetMs,
                onBackToVinyl = { showFullLyrics = false },
                onTogglePlayPause = onTogglePlayPause,
                onSkipPrevious = onSkipPrevious,
                onSkipNext = onSkipNext,
                onSeekTo = onSeekTo,
                onTranslateLyrics = onTranslateLyrics,
                onOpenLyricsSearch = { onOpenLyricsSearch(song) },
                onOpenCoverSearch = { onOpenCoverSearch(song) },
                onOpenFullLyricsSheet = onOpenLyrics,
                onAdjustLyricOffset = onAdjustLyricOffset,
                onResetLyricOffset = onResetLyricOffset,
                architecturalGridContent = {
                    EditorialFullScreenLyricsView(
                        song = song,
                        lyrics = lyrics,
                        positionMs = playerState.positionMs,
                        durationMs = durationMs,
                        isPlaying = playerState.isPlaying,
                        lyricFamily = lyricFamily,
                        lyricStyleItalic = lyricStyleItalic,
                        accentColor = activeAccentColor,
                        primaryTextColor = primaryTextColor,
                        secondaryTextColor = secondaryTextColor,
                        onBackToVinyl = { showFullLyrics = false },
                        onClosePlayer = onClose,
                        onTogglePlayPause = onTogglePlayPause,
                        onSkipPrevious = onSkipPrevious,
                        onSkipNext = onSkipNext,
                        onSeekTo = onSeekTo,
                        onOpenLyricsSearch = { onOpenLyricsSearch(song) },
                        onOpenCoverSearch = { onOpenCoverSearch(song) },
                        isTranslatingLyrics = isTranslatingLyrics,
                        onTranslateLyrics = onTranslateLyrics,
                        onAddToPlaylist = { onAddToPlaylist(song) },
                        isDarkTheme = isDarkTheme,
                        onOpenFullLyricsSheet = onOpenLyrics,
                        audioSpec = audioSpec,
                        lyricOffsetMs = lyricOffsetMs,
                        onAdjustLyricOffset = onAdjustLyricOffset,
                        onResetLyricOffset = onResetLyricOffset,
                        currentImmersionStyle = immersionStyle,
                        onSelectImmersionStyle = onSelectImmersionStyle,
                    )
                },
            )
            return@Box
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isLandscape = maxWidth > maxHeight && maxWidth >= 520.dp
            val isCompactHeight = maxHeight < 480.dp
            if (isLandscape) {
                LandscapeVinylSleeveNowPlaying(
                    isCompactHeight = isCompactHeight,
                    onEnterCarMode = { onToggleCarMode(true) },
                    song = song,
                    playerState = playerState,
                    durationMs = durationMs,
                    adjustedPosMs = adjustedPosMs,
                    lyrics = lyrics,
                    vinylRotationProvider = { vinylAngle.value },
                    isFavorite = isFavorite,
                    isDarkTheme = isDarkTheme,
                    lyricFamily = lyricFamily,
                    lyricStyleItalic = lyricStyleItalic,
                    accentColor = activeAccentColor,
                    primaryTextColor = primaryTextColor,
                    secondaryTextColor = secondaryTextColor,
                    audioSpec = audioSpec,
                    isTranslatingLyrics = isTranslatingLyrics,
                    onClose = onClose,
                    onTogglePlayPause = onTogglePlayPause,
                    onSkipPrevious = onSkipPrevious,
                    onSkipNext = onSkipNext,
                    onSeekTo = onSeekTo,
                    singleRepeatRemaining = singleRepeatRemaining,
                    onOpenRepeatCountDialog = onOpenRepeatCountDialog,
                    onToggleShuffle = onToggleShuffle,
                    onCycleRepeat = onCycleRepeat,
                    onToggleFavorite = { onToggleFavorite(song) },
                    onOpenQueue = onOpenQueue,
                    onOpenEqualizer = onOpenEqualizer,
                    onOpenSleepTimer = onOpenSleepTimer,
                    onOpenSettings = onOpenSettings,
                    onOpenLyricsSearch = { onOpenLyricsSearch(song) },
                    onOpenCoverSearch = { onOpenCoverSearch(song) },
                    onTranslateLyrics = onTranslateLyrics,
                    onOpenFullLyrics = { showFullLyrics = true },
                )
            } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 1. Edge-to-Edge 4-Row Manually Swipeable & Tappable Poster Wall + Giant 180g Glossy Vinyl
            InteractivePosterWallHeader(
                currentSong = song,
                allSongs = allSongs,
                vinylRotationProvider = { vinylAngle.value },
                backButtonBg = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
                backIconTint = primaryTextColor,
                sleepTimerRemainingMs = sleepTimerRemainingMs,
                accentColor = activeAccentColor,
                onClose = onClose,
                onSelectSong = onSelectSongFromWall,
                onOpenSleepTimer = onOpenSleepTimer,
                onLongPressVinyl = { onOpenCoverSearch(song) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.22f),
            )

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
                                if (absY > absX && dragTotalY > 110f) {
                                    onClose()
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
                        if (audioSpec.displayText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            AudioSpecStamp(
                                spec = audioSpec,
                                accentColor = activeAccentColor,
                                outlineColor = MaterialTheme.colorScheme.outline,
                                secondaryTextColor = secondaryTextColor,
                            )
                        }
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
                        AnimatedContent(
                            targetState = activeLyric,
                            transitionSpec = {
                                (fadeIn(tween(240)) + slideInVertically(tween(240)) { it / 3 }) togetherWith
                                    (fadeOut(tween(180)) + slideOutVertically(tween(180)) { -it / 3 })
                            },
                            label = "vinylLyricPreview",
                        ) { line ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = line.primaryText,
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
                                if (!line.secondaryText.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = line.secondaryText,
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = lyricFamily),
                                        color = secondaryTextColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
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

                Spacer(modifier = Modifier.weight(1f, fill = false))
                Spacer(modifier = Modifier.height(8.dp))

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

                Spacer(modifier = Modifier.weight(1f, fill = false))
                Spacer(modifier = Modifier.height(8.dp))

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
                        onClick = {
                            if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onTogglePlayPause()
                        },
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = when {
                                liquidGlassEnabled -> activeAccentColor
                                isDarkTheme -> Color(0xFFF2ECE1)
                                else -> primaryTextColor
                            },
                            contentColor = when {
                                liquidGlassEnabled -> Color.White
                                isDarkTheme -> Color(0xFF141110)
                                else -> baseCanvas
                            },
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

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .combinedClickable(
                                onClick = {
                                    if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onCycleRepeat()
                                },
                                onLongClick = {
                                    if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onOpenRepeatCountDialog()
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = when {
                                singleRepeatRemaining > 0 -> Icons.Rounded.RepeatOne
                                playerState.repeatMode == RepeatMode.ONE -> Icons.Rounded.RepeatOne
                                else -> Icons.Rounded.Repeat
                            },
                            contentDescription = "循环模式（长按设置复听次数）",
                            tint = if (singleRepeatRemaining > 0 || playerState.repeatMode != RepeatMode.OFF) {
                                activeAccentColor
                            } else {
                                primaryTextColor.copy(alpha = 0.85f)
                            },
                            modifier = Modifier.size(21.dp),
                        )
                        if (singleRepeatRemaining > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 4.dp, end = 2.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(activeAccentColor)
                                    .padding(horizontal = 4.dp, vertical = 0.5.dp),
                            ) {
                                Text(
                                    text = "×$singleRepeatRemaining",
                                    fontSize = 9.sp,
                                    lineHeight = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f, fill = false))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
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
                            imageVector = Icons.Rounded.Tune,
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
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LandscapeVinylSleeveNowPlaying(
    isCompactHeight: Boolean = false,
    onEnterCarMode: () -> Unit = {},
    song: Song,
    playerState: PlayerUiState,
    durationMs: Long,
    adjustedPosMs: Long,
    lyrics: List<LyricLine>,
    vinylRotationProvider: () -> Float,
    isFavorite: Boolean,
    isDarkTheme: Boolean,
    lyricFamily: FontFamily,
    lyricStyleItalic: FontStyle,
    accentColor: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    audioSpec: AudioTechSpec,
    isTranslatingLyrics: Boolean,
    onClose: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onSeekTo: (Long) -> Unit,
    singleRepeatRemaining: Int = 0,
    onOpenRepeatCountDialog: () -> Unit = {},
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenLyricsSearch: () -> Unit,
    onOpenCoverSearch: () -> Unit,
    onTranslateLyrics: () -> Unit,
    onOpenFullLyrics: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val activeIdx = remember(lyrics, adjustedPosMs) {
        lyrics.indexOfLast { it.timeMs <= adjustedPosMs }
    }
    val prevLine = if (activeIdx > 0) lyrics.getOrNull(activeIdx - 1) else null
    val currLine = if (activeIdx >= 0) lyrics.getOrNull(activeIdx) else lyrics.firstOrNull()
    val nextLine = if (activeIdx >= 0) lyrics.getOrNull(activeIdx + 1) else lyrics.getOrNull(1)
    val activeLyricColor = if (isDarkTheme) Color(0xFFF6DEC8) else accentColor

    Row(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = if (isCompactHeight) 16.dp else 24.dp, vertical = if (isCompactHeight) 4.dp else 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // LEFT PANE (~48%): Top '<' and '...' + Square Physical Album Sleeve with Spinning Peeking Vinyl LP
        Box(
            modifier = Modifier
                .weight(0.48f)
                .fillMaxHeight(),
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(40.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    contentDescription = "返回曲库",
                    tint = primaryTextColor,
                    modifier = Modifier.size(26.dp),
                )
            }

            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreHoriz,
                        contentDescription = "更多操作",
                        tint = primaryTextColor,
                        modifier = Modifier.size(24.dp),
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline),
                ) {
                    DropdownMenuItem(
                        text = { Text("播放队列") },
                        onClick = { menuExpanded = false; onOpenQueue() },
                    )
                    DropdownMenuItem(
                        text = { Text("音频均衡器") },
                        onClick = { menuExpanded = false; onOpenEqualizer() },
                    )
                    DropdownMenuItem(
                        text = { Text(if (playerState.shuffleEnabled) "关闭随机播放" else "开启随机播放") },
                        onClick = { menuExpanded = false; onToggleShuffle() },
                    )
                    DropdownMenuItem(
                        text = {
                            val modeLabel = when (playerState.repeatMode) {
                                RepeatMode.OFF -> "循环模式：顺序"
                                RepeatMode.ALL -> "循环模式：列表循环"
                                RepeatMode.ONE -> "循环模式：单曲循环"
                            }
                            Text(modeLabel)
                        },
                        onClick = { menuExpanded = false; onCycleRepeat() },
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (singleRepeatRemaining > 0) "单曲复听（剩余 $singleRepeatRemaining 遍）"
                                else "单曲复听次数",
                            )
                        },
                        onClick = { menuExpanded = false; onOpenRepeatCountDialog() },
                    )
                    DropdownMenuItem(
                        text = { Text("睡眠定时") },
                        onClick = { menuExpanded = false; onOpenSleepTimer() },
                    )
                    DropdownMenuItem(
                        text = { Text("在线匹配歌词") },
                        onClick = { menuExpanded = false; onOpenLyricsSearch() },
                    )
                    DropdownMenuItem(
                        text = { Text("更换封面") },
                        onClick = { menuExpanded = false; onOpenCoverSearch() },
                    )
                    DropdownMenuItem(
                        text = { Text("进入车机模式") },
                        onClick = { menuExpanded = false; onEnterCarMode() },
                    )
                    DropdownMenuItem(
                        text = { Text("外观与设置") },
                        onClick = { menuExpanded = false; onOpenSettings() },
                    )
                }
            }

            // Centered Square Album Sleeve + Continuously Rotating Peeking 96-Groove Vinyl LP
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 34.dp, bottom = 12.dp, start = 6.dp, end = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                val sleeveSize = min(maxWidth * (if (isCompactHeight) 0.62f else 0.68f), maxHeight * (if (isCompactHeight) 0.88f else 0.86f))
                val discSize = sleeveSize * 0.90f
                val totalWidth = sleeveSize + discSize * 0.36f

                Box(
                    modifier = Modifier
                        .width(totalWidth)
                        .height(sleeveSize),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    // Spinning Black Vinyl LP peeking ~36% out from the right edge of the sleeve
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd),
                    ) {
                        SpinningVinylDisc(
                            albumArtUri = song.albumArtUri,
                            rotationProvider = vinylRotationProvider,
                            discSize = discSize,
                        )
                    }

                    // Physical Square Album Sleeve sitting in front with realistic editorial shadow
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(sleeveSize)
                            .shadow(18.dp, RoundedCornerShape(2.dp), ambientColor = Color.Black.copy(alpha = 0.40f))
                            .clip(RoundedCornerShape(2.dp))
                            .combinedClickable(
                                onClick = onOpenFullLyrics,
                                onLongClick = onOpenCoverSearch,
                            ),
                    ) {
                        AlbumArtwork(
                            uri = song.albumArtUri,
                            contentDescription = song.album,
                            modifier = Modifier.fillMaxSize(),
                            cornerRadius = 2.dp,
                            iconSize = 42.dp,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(24.dp))

        // RIGHT PANE (~52%): Compact Top Track Header + Left-Aligned 3-Line Editorial Lyrics + Thin SeekBar + 3-Button Controls
        Column(
            modifier = Modifier
                .weight(0.52f)
                .fillMaxHeight()
                .padding(end = 12.dp, top = 4.dp, bottom = 4.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Top Track Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AlbumArtwork(
                        uri = song.albumArtUri,
                        contentDescription = song.album,
                        modifier = Modifier.size(42.dp),
                        cornerRadius = 4.dp,
                        iconSize = 16.dp,
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = primaryTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.bodySmall,
                            color = secondaryTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (lyrics.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(primaryTextColor.copy(alpha = 0.08f))
                                .clickable(enabled = !isTranslatingLyrics, onClick = onTranslateLyrics)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = if (isTranslatingLyrics) "译…" else "译",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = secondaryTextColor,
                            )
                        }
                    }
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = "喜欢",
                            tint = if (isFavorite) accentColor else secondaryTextColor,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            // Middle: Generous Left-Aligned 3-Line Dynamic Kinetic Lyrics
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clickable(onClick = onOpenFullLyrics),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (lyrics.isNotEmpty()) {
                    AnimatedContent(
                        targetState = Triple(prevLine?.primaryText, currLine, nextLine?.primaryText),
                        transitionSpec = {
                            (fadeIn(tween(260)) + slideInVertically(tween(260)) { it / 5 }) togetherWith
                                (fadeOut(tween(180)) + slideOutVertically(tween(180)) { -it / 5 })
                        },
                        label = "landscape3LineLyrics",
                    ) { (prevText, current, nextText) ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(if (isCompactHeight) 6.dp else 12.dp),
                            horizontalAlignment = Alignment.Start,
                        ) {
                            Text(
                                text = prevText.orEmpty(),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = lyricFamily,
                                    fontStyle = lyricStyleItalic,
                                ),
                                color = secondaryTextColor.copy(alpha = 0.72f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = current?.primaryText.orEmpty(),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontSize = if (isCompactHeight) 22.sp else 30.sp,
                                        lineHeight = if (isCompactHeight) 28.sp else 38.sp,
                                        fontFamily = lyricFamily,
                                        fontStyle = lyricStyleItalic,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = activeLyricColor,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                if (!current?.secondaryText.isNullOrBlank()) {
                                    Text(
                                        text = current?.secondaryText.orEmpty(),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = lyricFamily),
                                        color = secondaryTextColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                            Text(
                                text = nextText.orEmpty(),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = lyricFamily,
                                    fontStyle = lyricStyleItalic,
                                ),
                                color = secondaryTextColor.copy(alpha = 0.72f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.Start,
                    ) {
                        Text(
                            text = if (song.album.isNotBlank() && !song.album.equals(song.title, ignoreCase = true)) {
                                "专辑 · ${song.album}"
                            } else {
                                song.artist
                            },
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = lyricFamily,
                                fontStyle = lyricStyleItalic,
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = primaryTextColor.copy(alpha = 0.85f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (audioSpec.displayText.isNotBlank()) {
                            AudioSpecStamp(
                                spec = audioSpec,
                                accentColor = accentColor,
                                outlineColor = MaterialTheme.colorScheme.outline,
                                secondaryTextColor = secondaryTextColor,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(0.75.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                .clickable(onClick = onOpenLyricsSearch)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Text(
                                text = "在线匹配歌词",
                                style = MaterialTheme.typography.labelMedium,
                                color = accentColor,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }

            // Bottom: Thin Seek Bar + Timestamps + Minimalist 3-Icon Playback Controls
            Column(modifier = Modifier.fillMaxWidth()) {
                ThinSeekBar(
                    positionMs = playerState.positionMs,
                    durationMs = durationMs,
                    activeLineColor = accentColor,
                    accentColor = if (isDarkTheme) Color(0xFFF2ECE1) else primaryTextColor,
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

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onSkipPrevious) {
                        Icon(
                            imageVector = Icons.Rounded.SkipPrevious,
                            contentDescription = "上一首",
                            tint = primaryTextColor,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                    IconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier.size(52.dp),
                    ) {
                        Icon(
                            imageVector = if (playerState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (playerState.isPlaying) "暂停" else "播放",
                            tint = primaryTextColor,
                            modifier = Modifier.size(36.dp),
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
    vinylRotationProvider: () -> Float,
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
    val wallSongs = remember(allSongs) {
        if (allSongs.size >= 24) {
            allSongs
        } else if (allSongs.isNotEmpty()) {
            List(24) { idx -> allSongs[idx % allSongs.size] }
        } else {
            List(24) { currentSong }
        }
    }
    val initialIdx = remember(wallSongs) {
        val idx = wallSongs.indexOfFirst { it.id == currentSong.id }
        if (idx >= 0) (idx - 4).coerceAtLeast(0) else 0
    }
    val wallGridState = rememberLazyGridState(initialFirstVisibleItemIndex = initialIdx)
    LaunchedEffect(currentSong.id, wallSongs) {
        val target = wallSongs.indexOfFirst { it.id == currentSong.id }
        if (target >= 0) {
            val visible = wallGridState.layoutInfo.visibleItemsInfo.any { it.index == target }
            if (!visible) {
                wallGridState.animateScrollToItem((target - 4).coerceAtLeast(0))
            }
        }
    }

    BoxWithConstraints(modifier = modifier) {
        val gridHeight = maxHeight * 0.68f
        val discSize = min(maxWidth * 0.74f, maxHeight * 0.72f)

        Box(modifier = Modifier.fillMaxSize()) {
            LazyHorizontalGrid(
                rows = GridCells.Fixed(4),
                state = wallGridState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(gridHeight)
                    .graphicsLayer()
                    .align(Alignment.TopCenter),
                contentPadding = PaddingValues(0.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items(
                    count = wallSongs.size,
                    key = { idx -> "${wallSongs[idx].id}_$idx" },
                    contentType = { "poster_tile" },
                ) { index ->
                    val tileSong = wallSongs[index]
                    PosterWallTile(
                        song = tileSong,
                        isCurrent = tileSong.id == currentSong.id,
                        onClick = { onSelectSong(tileSong) },
                    )
                }
            }

            // Subtle top status-bar gradient scrim so clock/battery icons remain crisp over bright covers
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(26.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.Black.copy(alpha = 0.44f),
                            1.0f to Color.Transparent,
                        ),
                    ),
            )

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
                SpinningVinylDisc(
                    albumArtUri = currentSong.albumArtUri,
                    rotationProvider = vinylRotationProvider,
                    discSize = discSize,
                )
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
    val request = remember(song.id, song.albumArtUri) {
        val cacheKey = "tile_${song.id}_${song.albumArtUri}"
        ImageRequest.Builder(context)
            .data(song.albumArtUri)
            .size(220)
            .memoryCacheKey(cacheKey)
            .diskCacheKey(cacheKey)
            .crossfade(false)
            .build()
    }
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .aspectRatio(1f)
            .clip(RectangleShape)
            .background(Color.White.copy(alpha = if (isCurrent) 1f else 0.92f))
            .clickable(onClick = onClick)
            .padding(if (isCurrent) 1.5.dp else 1.dp),
    ) {
        SubcomposeAsyncImage(
            model = request,
            contentDescription = song.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            loading = { DarkVinylTileFallback() },
            error = { DarkVinylTileFallback() },
        )
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
    rotationProvider: () -> Float,
    discSize: Dp,
) {
    Box(
        modifier = Modifier
            .size(discSize)
            .shadow(26.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.50f))
            .clip(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        // 180g Audiophile Brushed-Luster LP Shader (96 dense micro-grooves + silky upper-right/lower-left conical sheen)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // 1. Deep virgin jet-black vinyl radial tone zones
            drawCircle(
                brush = Brush.radialGradient(
                    0.00f to Color(0xFF070709),
                    0.38f to Color(0xFF060608),
                    0.44f to Color(0xFF131316),
                    0.50f to Color(0xFF0B0B0D),
                    0.74f to Color(0xFF121215),
                    0.93f to Color(0xFF0C0C0E),
                    0.97f to Color(0xFF19191C),
                    1.00f to Color(0xFF0A0A0C),
                    center = center,
                    radius = radius,
                ),
                radius = radius,
                center = center,
            )

            // 2. Broad silky brushed-silver conical reflections (upper-right ~0.875 & lower-left ~0.375)
            drawCircle(
                brush = Brush.sweepGradient(
                    0.00f to Color.Transparent,
                    0.24f to Color.Transparent,
                    0.31f to Color(0xFFDCE0E6).copy(alpha = 0.14f),
                    0.375f to Color(0xFFEAECEF).copy(alpha = 0.34f),
                    0.44f to Color(0xFFDCE0E6).copy(alpha = 0.14f),
                    0.51f to Color.Transparent,
                    0.74f to Color.Transparent,
                    0.81f to Color(0xFFDCE0E6).copy(alpha = 0.15f),
                    0.875f to Color(0xFFEAECEF).copy(alpha = 0.36f),
                    0.94f to Color(0xFFDCE0E6).copy(alpha = 0.14f),
                    1.00f to Color.Transparent,
                    center = center,
                ),
                radius = radius * 0.965f,
                center = center,
            )

            // 3. Soft inner satin sheen core (refined, never harsh or washed out)
            drawCircle(
                brush = Brush.sweepGradient(
                    0.00f to Color.Transparent,
                    0.335f to Color.Transparent,
                    0.375f to Color.White.copy(alpha = 0.24f),
                    0.415f to Color.Transparent,
                    0.835f to Color.Transparent,
                    0.875f to Color.White.copy(alpha = 0.26f),
                    0.915f to Color.Transparent,
                    1.00f to Color.Transparent,
                    center = center,
                ),
                radius = radius * 0.955f,
                center = center,
            )

            // 4. 96 ultra-dense alternating etched micro-grooves + 3 dark track-separator bands
            for (i in 0..95) {
                val factor = 0.435f + i * 0.00545f
                val isTrackGap = i == 26 || i == 54 || i == 80
                when {
                    isTrackGap -> {
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.46f),
                            radius = radius * factor,
                            center = center,
                            style = Stroke(width = 2.0f),
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.10f),
                            radius = radius * (factor + 0.002f),
                            center = center,
                            style = Stroke(width = 0.7f),
                        )
                    }
                    i % 2 == 0 -> {
                        val lightAlpha = if (i % 6 == 0) 0.11f else 0.055f
                        drawCircle(
                            color = Color.White.copy(alpha = lightAlpha),
                            radius = radius * factor,
                            center = center,
                            style = Stroke(width = 0.55f),
                        )
                    }
                    else -> {
                        val darkAlpha = if (i % 5 == 0) 0.34f else 0.20f
                        drawCircle(
                            color = Color.Black.copy(alpha = darkAlpha),
                            radius = radius * factor,
                            center = center,
                            style = Stroke(width = 0.75f),
                        )
                    }
                }
            }

            // 5. Recessed dark dead-wax run-out band around center label
            drawCircle(
                color = Color.Black.copy(alpha = 0.52f),
                radius = radius * 0.405f,
                center = center,
                style = Stroke(width = radius * 0.06f),
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = radius * 0.432f,
                center = center,
                style = Stroke(width = 0.8f),
            )

            // 6. Clean matte dark outer LP bevel (no bright white halo ring)
            drawCircle(
                color = Color.White.copy(alpha = 0.10f),
                radius = radius - 1.2f,
                center = center,
                style = Stroke(width = 1.1f),
            )
            drawCircle(
                color = Color.Black.copy(alpha = 0.90f),
                radius = radius - 3.2f,
                center = center,
                style = Stroke(width = 2.4f),
            )
        }

        // Clean unobstructed 37% circular album art label (GPU graphicsLayer zero-recomposition spin, zero shadow flash)
        Box(
            modifier = Modifier
                .size(discSize * 0.37f)
                .graphicsLayer { rotationZ = rotationProvider() }
                .clip(CircleShape)
                .border(1.5.dp, Color(0xFF0A0A0B), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(
                targetState = albumArtUri,
                transitionSpec = { fadeIn(tween(360)) togetherWith fadeOut(tween(240)) },
                label = "vinylCenterArtworkCrossfade",
            ) { artUri ->
                AlbumArtwork(
                    uri = artUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    cornerRadius = discSize,
                    iconSize = 24.dp,
                )
            }
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
    val haptic = LocalHapticFeedback.current
    val hapticEnabled = LocalHapticEnabled.current
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
                        if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
private fun EditorialFullScreenLyricsView(
    song: Song,
    lyrics: List<LyricLine>,
    positionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    lyricFamily: FontFamily,
    lyricStyleItalic: FontStyle,
    accentColor: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    onBackToVinyl: () -> Unit,
    onClosePlayer: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onOpenLyricsSearch: () -> Unit,
    onOpenCoverSearch: () -> Unit,
    isTranslatingLyrics: Boolean = false,
    onTranslateLyrics: () -> Unit = {},
    onAddToPlaylist: () -> Unit = {},
    isDarkTheme: Boolean = false,
    onOpenFullLyricsSheet: () -> Unit = {},
    audioSpec: AudioTechSpec = AudioTechSpec(),
    lyricOffsetMs: Long = 0L,
    onAdjustLyricOffset: (Long) -> Unit = {},
    onResetLyricOffset: () -> Unit = {},
    currentImmersionStyle: ImmersionLyricStyle = ImmersionLyricStyle.ARCHITECTURAL_GRID,
    onSelectImmersionStyle: (ImmersionLyricStyle) -> Unit = {},
) {
    val effectivePosMs = (positionMs + lyricOffsetMs).coerceAtLeast(0L)
    val activeIdx = remember(lyrics, effectivePosMs) {
        val found = lyrics.indexOfLast { it.timeMs <= effectivePosMs }
        if (found >= 0) found else if (lyrics.isNotEmpty()) 0 else -1
    }
    var showOffsetBar by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    val outlineColor = MaterialTheme.colorScheme.outline
    val activeLineColor = if (isDarkTheme) primaryTextColor else accentColor
    val textMeasurer = rememberTextMeasurer()
    val isPhoneLandscapeLyrics = LocalConfiguration.current.screenHeightDp < 480

    val gridCells = remember(lyrics, activeIdx) {
        if (lyrics.isEmpty() || activeIdx < 0) {
            listOf(
                floatArrayOf(0.55f, 0.24f, 0.34f, -10f),
                floatArrayOf(0.84f, 0.28f, 0.28f, 0f),
                floatArrayOf(0.70f, 0.48f, 0.36f, 0f),
                floatArrayOf(0.86f, 0.64f, 0.30f, 0f),
            )
        } else {
            val window = ((activeIdx - 2).coerceAtLeast(0)..(activeIdx + 3).coerceAtMost(lyrics.lastIndex))
            window.mapIndexed { i, lineIdx ->
                val txt = lyrics[lineIdx].primaryText.trim()
                val lastToken = txt.split(Regex("[\\s,，。!?！？/]+")).lastOrNull { it.isNotBlank() } ?: txt
                val seed = (lastToken.hashCode() and 0x7FFFFFFF)
                val xRatio = 0.42f + ((seed % 46) / 100f)
                val yRatio = 0.18f + (i * 0.115f)
                val sizeRatio = (0.20f + (lastToken.length.coerceIn(1, 8) * 0.032f)).coerceAtMost(0.42f)
                val angle = if (i == 0 || lineIdx == activeIdx) ((seed % 22) - 11).toFloat() else 0f
                floatArrayOf(xRatio, yRatio, sizeRatio, angle)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Breathing Animated Last-Word Architectural Wireframe Grid Backdrop
        val gridPhase by animateFloatAsState(
            targetValue = (activeIdx % 8).toFloat(),
            animationSpec = tween(durationMillis = 750),
            label = "gridBreathPhase",
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridColor = accentColor.copy(alpha = if (isDarkTheme) 0.12f else 0.13f)
            val w = size.width
            val h = size.height
            gridCells.forEachIndexed { idx, vals ->
                val driftX = kotlin.math.sin((gridPhase + idx) * 0.75f) * 0.025f
                val driftY = kotlin.math.cos((gridPhase + idx) * 0.65f) * 0.015f
                val cx = w * (vals[0] + driftX)
                val cy = h * (vals[1] + driftY)
                val boxW = w * vals[2]
                val boxH = boxW * (if (idx % 2 == 0) 0.72f else 0.92f)
                val rot = vals[3] + kotlin.math.sin((gridPhase + idx) * 0.5f) * 2.5f
                rotate(degrees = rot, pivot = Offset(cx, cy)) {
                    drawRect(
                        color = gridColor,
                        topLeft = Offset(cx - boxW / 2f, cy - boxH / 2f),
                        size = Size(boxW, boxH),
                        style = Stroke(width = 1.1f),
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 2. Clean Top Bar: '<' on left, '...' on right (no "动态字幕" label)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = onBackToVinyl, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                        contentDescription = "返回黑胶",
                        tint = primaryTextColor,
                        modifier = Modifier.size(24.dp),
                    )
                }

                Box {
                    IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.MoreHoriz,
                            contentDescription = "更多操作",
                            tint = primaryTextColor,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                        border = BorderStroke(0.75.dp, outlineColor),
                    ) {
                        ImmersionLyricStyle.entries.forEach { option ->
                            val isSelected = option == currentImmersionStyle
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    )
                                },
                                trailingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                } else {
                                    null
                                },
                                onClick = {
                                    menuExpanded = false
                                    onSelectImmersionStyle(option)
                                },
                            )
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            thickness = 0.5.dp,
                            color = outlineColor.copy(alpha = 0.4f),
                        )
                        DropdownMenuItem(
                            text = { Text("全部歌词列表", style = MaterialTheme.typography.bodyMedium) },
                            onClick = { menuExpanded = false; onOpenFullLyricsSheet() },
                        )
                        DropdownMenuItem(
                            text = { Text(if (showOffsetBar) "隐藏歌词微调" else "歌词时间微调 (±0.5s)") },
                            onClick = { menuExpanded = false; showOffsetBar = !showOffsetBar },
                        )
                        DropdownMenuItem(
                            text = { Text("在线匹配 / 导入歌词") },
                            onClick = { menuExpanded = false; onOpenLyricsSearch() },
                        )
                        DropdownMenuItem(
                            text = { Text("更换封面") },
                            onClick = { menuExpanded = false; onOpenCoverSearch() },
                        )
                        DropdownMenuItem(
                            text = { Text("添加到歌单") },
                            onClick = { menuExpanded = false; onAddToPlaylist() },
                        )
                        DropdownMenuItem(
                            text = { Text("收起播放页") },
                            onClick = { menuExpanded = false; onClosePlayer() },
                        )
                    }
                }
            }

            // 3. Flush Left-Aligned Sleeve+Vinyl & Right-Aligned Title/Artist/Spec + Inline「译」Pill
            if (!isPhoneLandscapeLyrics) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 82.dp, height = 60.dp)
                        .clickable(onClick = onBackToVinyl),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0C0C0E))
                            .border(0.8.dp, Color.White.copy(alpha = 0.22f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(15.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.85f)),
                        )
                    }
                    AlbumArtwork(
                        uri = song.albumArtUri,
                        contentDescription = song.album,
                        modifier = Modifier
                            .size(60.dp)
                            .shadow(5.dp, RoundedCornerShape(3.dp)),
                        cornerRadius = 3.dp,
                        iconSize = 22.dp,
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = primaryTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.labelMedium,
                        color = secondaryTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (audioSpec.displayText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        AudioSpecStamp(
                            spec = audioSpec,
                            accentColor = activeLineColor,
                            outlineColor = outlineColor,
                            secondaryTextColor = secondaryTextColor,
                        )
                    }
                }

                if (lyrics.isNotEmpty()) {
                    val pillShape = RoundedCornerShape(6.dp)
                    Box(
                        modifier = Modifier
                            .clip(pillShape)
                            .border(0.75.dp, outlineColor, pillShape)
                            .clickable(enabled = !isTranslatingLyrics, onClick = onTranslateLyrics)
                            .padding(horizontal = 9.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isTranslatingLyrics) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(13.dp),
                                strokeWidth = 1.6.dp,
                                color = accentColor,
                            )
                        } else {
                            Text(
                                text = "译",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDarkTheme) primaryTextColor else accentColor,
                            )
                        }
                    }
                }
            }

            }
            // 4. Fixed-Stage Auto-Sized Editorial Kinetic Typography Lyrics
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                val stageWidthPx = constraints.maxWidth
                val stageHeightPx = constraints.maxHeight

                if (lyrics.isEmpty() || activeIdx < 0) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "暂无同步歌词",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = lyricFamily,
                                fontStyle = lyricStyleItalic,
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = activeLineColor,
                        )
                        Text(
                            text = "可在线匹配带时间轴的 .lrc 歌词或从本地导入",
                            style = MaterialTheme.typography.bodySmall,
                            color = secondaryTextColor,
                            textAlign = TextAlign.Center,
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(7.dp))
                                .background(accentColor)
                                .clickable(onClick = onOpenLyricsSearch)
                                .padding(horizontal = 18.dp, vertical = 9.dp),
                        ) {
                            Text(
                                text = "在线匹配 / 导入歌词",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                } else {
                    AnimatedContent(
                        targetState = activeIdx,
                        transitionSpec = {
                            val forward = targetState >= initialState
                            val enterSlide = slideInVertically(tween(380)) { if (forward) it / 6 else -it / 6 }
                            val exitSlide = slideOutVertically(tween(240)) { if (forward) -it / 6 else it / 6 }
                            (fadeIn(tween(380)) + enterSlide + scaleIn(tween(380), initialScale = 0.96f)) togetherWith
                                (fadeOut(tween(220)) + exitSlide)
                        },
                        label = "autoFitEditorialLyrics",
                        modifier = Modifier.fillMaxSize(),
                    ) { idx ->
                        val prevLine = lyrics.getOrNull(idx - 1)
                        val currLine = lyrics.getOrNull(idx)
                        val nextLine = lyrics.getOrNull(idx + 1)

                        val currentText = currLine?.primaryText.orEmpty()
                        val targetTextHeightPx = (stageHeightPx * 0.56f).toInt().coerceAtLeast(120)
                        val fittedFontSp = remember(currentText, stageWidthPx, targetTextHeightPx, lyricFamily, lyricStyleItalic) {
                            var candidate = if (isPhoneLandscapeLyrics) 38f else 64f
                            val minCandidate = if (isPhoneLandscapeLyrics) 18f else 24f
                            while (candidate >= minCandidate) {
                                val layout = textMeasurer.measure(
                                    text = currentText,
                                    style = TextStyle(
                                        fontFamily = lyricFamily,
                                        fontStyle = lyricStyleItalic,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = candidate.sp,
                                        lineHeight = (candidate * 1.18f).sp,
                                    ),
                                    constraints = Constraints(maxWidth = stageWidthPx),
                                )
                                if (layout.size.height <= targetTextHeightPx) {
                                    break
                                }
                                candidate -= 3f
                            }
                            candidate.coerceIn(minCandidate, if (isPhoneLandscapeLyrics) 38f else 64f)
                        }

                        val styledCurrent = remember(currentText, primaryTextColor, activeLineColor) {
                            val words = currentText.split(" ").filter { it.isNotBlank() }
                            buildAnnotatedString {
                                if (words.size >= 3) {
                                    val splitStart = 1
                                    val splitEnd = (words.size / 2 + 1).coerceAtMost(words.lastIndex)
                                    val part1 = words.subList(0, splitStart).joinToString(" ") + " "
                                    val part2 = words.subList(splitStart, splitEnd).joinToString(" ")
                                    val part3 = " " + words.subList(splitEnd, words.size).joinToString(" ")
                                    pushStyle(SpanStyle(color = primaryTextColor))
                                    append(part1)
                                    pop()
                                    pushStyle(SpanStyle(color = activeLineColor))
                                    append(part2)
                                    pop()
                                    pushStyle(SpanStyle(color = primaryTextColor))
                                    append(part3)
                                    pop()
                                } else if (currentText.length >= 4) {
                                    val midStart = currentText.length / 3
                                    val midEnd = (currentText.length * 2 / 3).coerceAtLeast(midStart + 1)
                                    pushStyle(SpanStyle(color = primaryTextColor))
                                    append(currentText.substring(0, midStart))
                                    pop()
                                    pushStyle(SpanStyle(color = activeLineColor))
                                    append(currentText.substring(midStart, midEnd))
                                    pop()
                                    pushStyle(SpanStyle(color = primaryTextColor))
                                    append(currentText.substring(midEnd))
                                    pop()
                                } else {
                                    pushStyle(SpanStyle(color = activeLineColor))
                                    append(currentText)
                                    pop()
                                }
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start,
                        ) {
                            if (prevLine != null) {
                                Text(
                                    text = prevLine.primaryText,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontFamily = lyricFamily,
                                        fontStyle = lyricStyleItalic,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 17.sp,
                                        lineHeight = 23.sp,
                                    ),
                                    color = secondaryTextColor.copy(alpha = 0.65f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .clickable(enabled = prevLine.timeMs > 0L) { onSeekTo(prevLine.timeMs) }
                                        .padding(bottom = 12.dp),
                                )
                            }

                            Text(
                                text = styledCurrent,
                                style = TextStyle(
                                    fontFamily = lyricFamily,
                                    fontStyle = lyricStyleItalic,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = fittedFontSp.sp,
                                    lineHeight = (fittedFontSp * 1.18f).sp,
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            )

                            val bottomNote = currLine?.secondaryText?.takeIf { it.isNotBlank() } ?: nextLine?.primaryText
                            if (!bottomNote.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = bottomNote,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontFamily = lyricFamily,
                                        fontStyle = lyricStyleItalic,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 17.sp,
                                        lineHeight = 23.sp,
                                    ),
                                    color = secondaryTextColor.copy(alpha = 0.80f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.clickable(enabled = nextLine != null) {
                                        nextLine?.let { onSeekTo(it.timeMs) }
                                    },
                                )
                            }
                        }
                    }
                }
            }

            if (showOffsetBar) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(0.75.dp, outlineColor, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "-0.5s",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onAdjustLyricOffset(-500L) }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                    val secStr = String.format(java.util.Locale.US, "%+.1fs", lyricOffsetMs / 1000f)
                    Text(
                        text = if (lyricOffsetMs == 0L) "0.0s" else secStr,
                        style = MaterialTheme.typography.labelMedium,
                        color = primaryTextColor,
                        modifier = Modifier.clickable { onResetLyricOffset() },
                    )
                    Text(
                        text = "+0.5s",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onAdjustLyricOffset(500L) }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }

            // 5. Thin Progress Bar & 3-Button Minimal Playback Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 10.dp),
            ) {
                ThinSeekBar(
                    positionMs = positionMs,
                    durationMs = durationMs.coerceAtLeast(1L),
                    activeLineColor = accentColor,
                    accentColor = accentColor,
                    trackColor = secondaryTextColor.copy(alpha = 0.25f),
                    onSeekTo = onSeekTo,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = formatDuration(positionMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = secondaryTextColor,
                    )
                    Text(
                        text = formatDuration(durationMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = secondaryTextColor,
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onSkipPrevious) {
                        Icon(
                            imageVector = Icons.Rounded.SkipPrevious,
                            contentDescription = "上一首",
                            tint = primaryTextColor,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                    IconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier.size(52.dp),
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (isPlaying) "暂停" else "播放",
                            tint = activeLineColor,
                            modifier = Modifier.size(36.dp),
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
                }
            }
        }
    }
}

@Composable
private fun AudioSpecStamp(
    spec: AudioTechSpec,
    accentColor: Color,
    outlineColor: Color,
    secondaryTextColor: Color,
) {
    val stampShape = RoundedCornerShape(4.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        if (spec.isHiRes) {
            Box(
                modifier = Modifier
                    .clip(stampShape)
                    .background(accentColor.copy(alpha = 0.14f))
                    .border(0.6.dp, accentColor.copy(alpha = 0.55f), stampShape)
                    .padding(horizontal = 5.dp, vertical = 1.dp),
            ) {
                Text(
                    text = "Hi-Res",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, lineHeight = 12.sp),
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Box(
            modifier = Modifier
                .clip(stampShape)
                .border(0.6.dp, outlineColor.copy(alpha = 0.85f), stampShape)
                .padding(horizontal = 6.dp, vertical = 1.dp),
        ) {
            Text(
                text = spec.displayText,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, lineHeight = 12.sp),
                color = secondaryTextColor,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun MinimalistCarModeView(
    song: Song,
    playerState: PlayerUiState,
    durationMs: Long,
    activeLyricText: String,
    hapticEnabled: Boolean,
    onTogglePlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onExitCarMode: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val bgColor = Color(0xFF121214)
    val creamInk = Color(0xFFF5F1E8)
    val cinnabar = Color(0xFFC83E2B)
    val mutedInk = Color(0xFFA69F95)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 22.dp, vertical = 12.dp),
    ) {
        val carMaxWidth = maxWidth
        val carMaxHeight = maxHeight
        val isWide = carMaxWidth > carMaxHeight
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar: "车机模式" badge + "退出" pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                ) {
                    Text(
                        text = "车机模式",
                        style = MaterialTheme.typography.labelMedium,
                        color = mutedInk,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, creamInk.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                        .clickable(onClick = onExitCarMode)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = "退出",
                        style = MaterialTheme.typography.labelLarge,
                        color = creamInk,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (isWide) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(0.38f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center,
                    ) {
                        val artSize = min(carMaxWidth * 0.34f, carMaxHeight * 0.72f)
                        AlbumArtwork(
                            uri = song.albumArtUri,
                            contentDescription = song.album,
                            modifier = Modifier
                                .size(artSize)
                                .shadow(18.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black),
                            cornerRadius = 16.dp,
                            iconSize = 48.dp,
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(0.62f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = creamInk,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = song.artist,
                                style = MaterialTheme.typography.titleMedium,
                                color = mutedInk,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                            )
                            if (activeLyricText.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = activeLyricText,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = cinnabar,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }

                        Column(modifier = Modifier.fillMaxWidth(0.92f)) {
                            ThinSeekBar(
                                positionMs = playerState.positionMs,
                                durationMs = durationMs,
                                activeLineColor = creamInk,
                                accentColor = cinnabar,
                                trackColor = Color.White.copy(alpha = 0.22f),
                                onSeekTo = onSeekTo,
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(formatDuration(playerState.positionMs), style = MaterialTheme.typography.labelMedium, color = mutedInk)
                                Text(formatDuration(playerState.durationMs), style = MaterialTheme.typography.labelMedium, color = mutedInk)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            FilledIconButton(
                                onClick = {
                                    if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSkipPrevious()
                                },
                                shape = CircleShape,
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = creamInk, contentColor = bgColor),
                                modifier = Modifier.size(62.dp),
                            ) {
                                Icon(Icons.Rounded.SkipPrevious, contentDescription = "上一首", modifier = Modifier.size(32.dp))
                            }
                            FilledIconButton(
                                onClick = onTogglePlayPause,
                                shape = CircleShape,
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = cinnabar, contentColor = Color.White),
                                modifier = Modifier.size(84.dp),
                            ) {
                                Icon(
                                    imageVector = if (playerState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = if (playerState.isPlaying) "暂停" else "播放",
                                    modifier = Modifier.size(44.dp),
                                )
                            }
                            FilledIconButton(
                                onClick = {
                                    if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSkipNext()
                                },
                                shape = CircleShape,
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = creamInk, contentColor = bgColor),
                                modifier = Modifier.size(62.dp),
                            ) {
                                Icon(Icons.Rounded.SkipNext, contentDescription = "下一首", modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AlbumArtwork(
                        uri = song.albumArtUri,
                        contentDescription = song.album,
                        modifier = Modifier
                            .size(min(carMaxWidth * 0.62f, carMaxHeight * 0.34f))
                            .shadow(18.dp, RoundedCornerShape(16.dp)),
                        cornerRadius = 16.dp,
                        iconSize = 48.dp,
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = creamInk,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.titleMedium,
                            color = mutedInk,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (activeLyricText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = activeLyricText,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = cinnabar,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                    Column(modifier = Modifier.fillMaxWidth()) {
                        ThinSeekBar(
                            positionMs = playerState.positionMs,
                            durationMs = durationMs,
                            activeLineColor = creamInk,
                            accentColor = cinnabar,
                            trackColor = Color.White.copy(alpha = 0.22f),
                            onSeekTo = onSeekTo,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(formatDuration(playerState.positionMs), style = MaterialTheme.typography.labelMedium, color = mutedInk)
                            Text(formatDuration(playerState.durationMs), style = MaterialTheme.typography.labelMedium, color = mutedInk)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FilledIconButton(
                            onClick = {
                                if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSkipPrevious()
                            },
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = creamInk, contentColor = bgColor),
                            modifier = Modifier.size(66.dp),
                        ) {
                            Icon(Icons.Rounded.SkipPrevious, contentDescription = "上一首", modifier = Modifier.size(34.dp))
                        }
                        FilledIconButton(
                            onClick = onTogglePlayPause,
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = cinnabar, contentColor = Color.White),
                            modifier = Modifier.size(88.dp),
                        ) {
                            Icon(
                                imageVector = if (playerState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = if (playerState.isPlaying) "暂停" else "播放",
                                modifier = Modifier.size(46.dp),
                            )
                        }
                        FilledIconButton(
                            onClick = {
                                if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSkipNext()
                            },
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = creamInk, contentColor = bgColor),
                            modifier = Modifier.size(66.dp),
                        ) {
                            Icon(Icons.Rounded.SkipNext, contentDescription = "下一首", modifier = Modifier.size(34.dp))
                        }
                    }
                }
            }
        }
    }
}
