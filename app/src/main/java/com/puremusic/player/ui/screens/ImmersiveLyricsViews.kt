package com.puremusic.player.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.puremusic.player.data.model.ImmersionLyricStyle
import com.puremusic.player.data.model.LyricLine
import com.puremusic.player.data.model.Song
import com.puremusic.player.service.AudioSpectrumBus
import com.puremusic.player.ui.components.AlbumArtwork
import com.puremusic.player.ui.components.formatDuration
import com.puremusic.player.ui.theme.LocalHapticEnabled
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun ImmersiveLyricsHost(
    style: ImmersionLyricStyle,
    onSelectStyle: (ImmersionLyricStyle) -> Unit,
    song: Song,
    allSongs: List<Song>,
    lyrics: List<LyricLine>,
    positionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    vinylRotationProvider: () -> Float,
    lyricFamily: FontFamily,
    lyricStyleItalic: FontStyle,
    dynamicAccent: Color,
    isDarkTheme: Boolean,
    isTranslatingLyrics: Boolean,
    lyricOffsetMs: Long,
    onBackToVinyl: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onTranslateLyrics: () -> Unit,
    onOpenLyricsSearch: () -> Unit,
    onOpenCoverSearch: () -> Unit,
    onOpenFullLyricsSheet: () -> Unit,
    onAdjustLyricOffset: (Long) -> Unit,
    onResetLyricOffset: () -> Unit,
) {
    val effectivePosMs = (positionMs + lyricOffsetMs).coerceAtLeast(0L)
    val activeIdx = remember(lyrics, effectivePosMs) {
        val found = lyrics.indexOfLast { it.timeMs <= effectivePosMs }
        if (found >= 0) found else if (lyrics.isNotEmpty()) 0 else -1
    }
    var showOffsetBar by remember { mutableStateOf(false) }
    val spectrumBands by AudioSpectrumBus.bands.collectAsState()

    AnimatedContent(
        targetState = style,
        transitionSpec = { fadeIn(tween(260)) togetherWith fadeOut(tween(220)) },
        label = "immersionSceneSwitch",
        modifier = Modifier.fillMaxSize(),
    ) { targetStyle ->
        when (targetStyle) {
            ImmersionLyricStyle.VINYL_STUDY -> {
                VinylStudyImmersiveView(
                    song = song,
                    lyrics = lyrics,
                    activeIdx = activeIdx,
                    positionMs = positionMs,
                    durationMs = durationMs,
                    isPlaying = isPlaying,
                    spectrumBands = spectrumBands,
                    vinylRotationProvider = vinylRotationProvider,
                    lyricFamily = lyricFamily,
                    isDarkTheme = isDarkTheme,
                    currentStyle = targetStyle,
                    isTranslatingLyrics = isTranslatingLyrics,
                    showOffsetBar = showOffsetBar,
                    lyricOffsetMs = lyricOffsetMs,
                    onToggleOffsetBar = { showOffsetBar = !showOffsetBar },
                    onSelectStyle = onSelectStyle,
                    onBackToVinyl = onBackToVinyl,
                    onTogglePlayPause = onTogglePlayPause,
                    onSkipPrevious = onSkipPrevious,
                    onSkipNext = onSkipNext,
                    onSeekTo = onSeekTo,
                    onTranslateLyrics = onTranslateLyrics,
                    onOpenLyricsSearch = onOpenLyricsSearch,
                    onOpenCoverSearch = onOpenCoverSearch,
                    onOpenFullLyricsSheet = onOpenFullLyricsSheet,
                    onAdjustLyricOffset = onAdjustLyricOffset,
                    onResetLyricOffset = onResetLyricOffset,
                )
            }

            ImmersionLyricStyle.SPATIAL_GALLERY -> {
                SpatialGalleryImmersiveView(
                    song = song,
                    allSongs = allSongs,
                    lyrics = lyrics,
                    activeIdx = activeIdx,
                    positionMs = positionMs,
                    durationMs = durationMs,
                    isPlaying = isPlaying,
                    spectrumBands = spectrumBands,
                    vinylRotationProvider = vinylRotationProvider,
                    lyricFamily = lyricFamily,
                    dynamicAccent = dynamicAccent,
                    currentStyle = targetStyle,
                    isTranslatingLyrics = isTranslatingLyrics,
                    showOffsetBar = showOffsetBar,
                    lyricOffsetMs = lyricOffsetMs,
                    onToggleOffsetBar = { showOffsetBar = !showOffsetBar },
                    onSelectStyle = onSelectStyle,
                    onBackToVinyl = onBackToVinyl,
                    onTogglePlayPause = onTogglePlayPause,
                    onSkipPrevious = onSkipPrevious,
                    onSkipNext = onSkipNext,
                    onSeekTo = onSeekTo,
                    onTranslateLyrics = onTranslateLyrics,
                    onOpenLyricsSearch = onOpenLyricsSearch,
                    onOpenCoverSearch = onOpenCoverSearch,
                    onOpenFullLyricsSheet = onOpenFullLyricsSheet,
                    onAdjustLyricOffset = onAdjustLyricOffset,
                    onResetLyricOffset = onResetLyricOffset,
                )
            }

            ImmersionLyricStyle.VINYL_LIGHTFIELD -> {
                VinylLightfieldImmersiveView(
                    song = song,
                    lyrics = lyrics,
                    activeIdx = activeIdx,
                    positionMs = positionMs,
                    durationMs = durationMs,
                    isPlaying = isPlaying,
                    spectrumBands = spectrumBands,
                    vinylRotationProvider = vinylRotationProvider,
                    lyricFamily = lyricFamily,
                    dynamicAccent = dynamicAccent,
                    currentStyle = targetStyle,
                    isTranslatingLyrics = isTranslatingLyrics,
                    showOffsetBar = showOffsetBar,
                    lyricOffsetMs = lyricOffsetMs,
                    onToggleOffsetBar = { showOffsetBar = !showOffsetBar },
                    onSelectStyle = onSelectStyle,
                    onBackToVinyl = onBackToVinyl,
                    onTogglePlayPause = onTogglePlayPause,
                    onSkipPrevious = onSkipPrevious,
                    onSkipNext = onSkipNext,
                    onSeekTo = onSeekTo,
                    onTranslateLyrics = onTranslateLyrics,
                    onOpenLyricsSearch = onOpenLyricsSearch,
                    onOpenCoverSearch = onOpenCoverSearch,
                    onOpenFullLyricsSheet = onOpenFullLyricsSheet,
                    onAdjustLyricOffset = onAdjustLyricOffset,
                    onResetLyricOffset = onResetLyricOffset,
                )
            }
        }
    }
}

/**
 * 1. 黑胶书房 (Vinyl Study)
 * Archival warm cream paper (#F5F0E6) in light mode / deep matte charcoal (#151413) in dark mode.
 * 90° crisp physical sleeve + peeking 96-groove spinning black LP + soft floor shadow,
 * left-aligned editorial Song Title, Artist, and flowing lyrics, pure gesture controls + live PCM spectrum bar.
 */
@Composable
private fun VinylStudyImmersiveView(
    song: Song,
    lyrics: List<LyricLine>,
    activeIdx: Int,
    positionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    spectrumBands: FloatArray,
    vinylRotationProvider: () -> Float,
    lyricFamily: FontFamily,
    isDarkTheme: Boolean,
    currentStyle: ImmersionLyricStyle,
    isTranslatingLyrics: Boolean,
    showOffsetBar: Boolean,
    lyricOffsetMs: Long,
    onToggleOffsetBar: () -> Unit,
    onSelectStyle: (ImmersionLyricStyle) -> Unit,
    onBackToVinyl: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onTranslateLyrics: () -> Unit,
    onOpenLyricsSearch: () -> Unit,
    onOpenCoverSearch: () -> Unit,
    onOpenFullLyricsSheet: () -> Unit,
    onAdjustLyricOffset: (Long) -> Unit,
    onResetLyricOffset: () -> Unit,
) {
    val bgColor = if (isDarkTheme) Color(0xFF151413) else Color(0xFFF5F0E6)
    val inkPrimary = if (isDarkTheme) Color(0xFFF2ECE1) else Color(0xFF1E1B18)
    val inkSecondary = if (isDarkTheme) Color(0xFFA69E93) else Color(0xFF6B655D)
    val inkMuted = if (isDarkTheme) Color(0xFF666059) else Color(0xFFB5ADA1)
    val cinnabar = Color(0xFFC83E2B)

    val haptic = LocalHapticFeedback.current
    val hapticEnabled = LocalHapticEnabled.current
    val scope = rememberCoroutineScope()
    val dragOffsetX = remember { Animatable(0f) }
    val tapScale = remember { Animatable(1f) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        val stageW = maxWidth
        val stageH = maxHeight
        val isCompactH = stageH < 520.dp
        val sleeveSize = min(stageW * 0.63f, stageH * (if (isCompactH) 0.34f else 0.36f))
        val discSize = sleeveSize * 0.92f

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
        ) {
            ImmersiveTopBar(
                title = "黑胶书房",
                inkColor = inkPrimary,
                currentStyle = currentStyle,
                isTranslatingLyrics = isTranslatingLyrics,
                onSelectStyle = onSelectStyle,
                onBack = onBackToVinyl,
                onTranslateLyrics = onTranslateLyrics,
                onToggleOffsetBar = onToggleOffsetBar,
                onOpenLyricsSearch = onOpenLyricsSearch,
                onOpenCoverSearch = onOpenCoverSearch,
                onOpenFullLyricsSheet = onOpenFullLyricsSheet,
            )

            Spacer(modifier = Modifier.height(if (isCompactH) 8.dp else 16.dp))

            // 90° Square Physical Sleeve + Peeking Spinning LP + Soft Floor Contact Shadow (Pure Gesture Stage)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(sleeveSize + 18.dp)
                    .pointerInput(song.id) {
                        detectTapGestures(
                            onTap = {
                                if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                scope.launch {
                                    tapScale.animateTo(0.96f, tween(80))
                                    tapScale.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium))
                                }
                                onTogglePlayPause()
                            },
                        )
                    }
                    .pointerInput(song.id) {
                        var totalDrag = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { totalDrag = 0f },
                            onHorizontalDrag = { _, dragAmount ->
                                totalDrag += dragAmount
                                scope.launch { dragOffsetX.snapTo((totalDrag * 0.42f).coerceIn(-90f, 90f)) }
                            },
                            onDragEnd = {
                                if (abs(totalDrag) > 72f) {
                                    if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    if (totalDrag < 0f) onSkipNext() else onSkipPrevious()
                                }
                                scope.launch { dragOffsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow)) }
                            },
                            onDragCancel = {
                                scope.launch { dragOffsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow)) }
                            },
                        )
                    },
                contentAlignment = Alignment.CenterStart,
            ) {
                Box(
                    modifier = Modifier
                        .offset { IntOffset(dragOffsetX.value.roundToInt(), 0) }
                        .graphicsLayer {
                            scaleX = tapScale.value
                            scaleY = tapScale.value
                        }
                        .width(sleeveSize + discSize * 0.46f)
                        .height(sleeveSize + 14.dp),
                ) {
                    // Subtle soft horizontal floor contact shadow
                    Canvas(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .height(14.dp),
                    ) {
                        drawOval(
                            brush = Brush.radialGradient(
                                0.0f to Color.Black.copy(alpha = if (isDarkTheme) 0.48f else 0.16f),
                                1.0f to Color.Transparent,
                                center = Offset(size.width * 0.46f, size.height * 0.45f),
                                radius = size.width * 0.52f,
                            ),
                            topLeft = Offset(0f, 0f),
                            size = Size(size.width * 0.94f, size.height),
                        )
                    }

                    // Peeking 96-groove spinning vinyl LP behind right edge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = sleeveSize * 0.50f, y = (sleeveSize - discSize) / 2f),
                    ) {
                        SpinningVinylDisc(
                            albumArtUri = song.albumArtUri,
                            rotationProvider = vinylRotationProvider,
                            discSize = discSize,
                        )
                    }

                    // Crisp 90° physical album sleeve
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .size(sleeveSize)
                            .shadow(14.dp, RectangleShape, ambientColor = Color.Black.copy(alpha = 0.25f))
                            .border(0.5.dp, Color.Black.copy(alpha = if (isDarkTheme) 0.35f else 0.10f), RectangleShape),
                    ) {
                        AlbumArtwork(
                            uri = song.albumArtUri,
                            contentDescription = song.album,
                            modifier = Modifier.fillMaxSize(),
                            cornerRadius = 0.dp,
                            iconSize = 48.dp,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (isCompactH) 12.dp else 22.dp))

            // Left-aligned Song Title & Artist
            Text(
                text = song.title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.4).sp,
                ),
                color = inkPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = song.artist,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal),
                color = inkSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(if (isCompactH) 12.dp else 24.dp))

            // Left-aligned Editorial Lyrics Stage
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
            ) {
                if (lyrics.isEmpty() || activeIdx < 0) {
                    Text(
                        text = "暂无歌词 · 点击右上角匹配在线歌词",
                        style = MaterialTheme.typography.bodyLarge.copy(fontFamily = lyricFamily),
                        color = inkSecondary,
                    )
                } else {
                    val prevLine = lyrics.getOrNull(activeIdx - 1)
                    val curLine = lyrics[activeIdx]
                    val nextLine = lyrics.getOrNull(activeIdx + 1)

                    if (prevLine != null) {
                        Text(
                            text = prevLine.primaryText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = lyricFamily,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 25.sp,
                            ),
                            color = inkSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Text(
                        text = curLine.primaryText,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = lyricFamily,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 32.sp,
                        ),
                        color = inkPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (!curLine.translatedText.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = curLine.translatedText,
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = lyricFamily),
                            color = inkSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    if (nextLine != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = nextLine.primaryText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = lyricFamily,
                                fontWeight = FontWeight.Normal,
                                lineHeight = 24.sp,
                            ),
                            color = inkMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            if (showOffsetBar) {
                ImmersiveOffsetStrip(
                    lyricOffsetMs = lyricOffsetMs,
                    inkColor = inkPrimary,
                    accentColor = cinnabar,
                    onAdjust = onAdjustLyricOffset,
                    onReset = onResetLyricOffset,
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            LiveSpectrumWaveformSeekBar(
                songId = song.id,
                positionMs = positionMs,
                durationMs = durationMs,
                isPlaying = isPlaying,
                spectrumBands = spectrumBands,
                playedColor = inkPrimary,
                unplayedColor = inkMuted.copy(alpha = 0.42f),
                needleColor = cinnabar,
                timestampColor = inkSecondary,
                onSeekTo = onSeekTo,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/**
 * 2. 空间唱片廊 (Spatial Record Gallery)
 * Pure silky dynamic album-color ambient gradient (zero raindrops),
 * 3D tilted receding physical vinyl sleeve stack from real local queue + peeking LP + silky mirror floor reflection,
 * pure swipe/tap gesture control, centered serif bilingual lyrics, live PCM spectrum waveform scrubber.
 */
@Composable
private fun SpatialGalleryImmersiveView(
    song: Song,
    allSongs: List<Song>,
    lyrics: List<LyricLine>,
    activeIdx: Int,
    positionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    spectrumBands: FloatArray,
    vinylRotationProvider: () -> Float,
    lyricFamily: FontFamily,
    dynamicAccent: Color,
    currentStyle: ImmersionLyricStyle,
    isTranslatingLyrics: Boolean,
    showOffsetBar: Boolean,
    lyricOffsetMs: Long,
    onToggleOffsetBar: () -> Unit,
    onSelectStyle: (ImmersionLyricStyle) -> Unit,
    onBackToVinyl: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onTranslateLyrics: () -> Unit,
    onOpenLyricsSearch: () -> Unit,
    onOpenCoverSearch: () -> Unit,
    onOpenFullLyricsSheet: () -> Unit,
    onAdjustLyricOffset: (Long) -> Unit,
    onResetLyricOffset: () -> Unit,
) {
    val bgTop by animateColorAsState(
        targetValue = lerp(Color(0xFF141118), dynamicAccent, 0.34f),
        animationSpec = tween(550),
        label = "galleryBgTop",
    )
    val bgMid by animateColorAsState(
        targetValue = lerp(Color(0xFF0E0C12), dynamicAccent, 0.20f),
        animationSpec = tween(550),
        label = "galleryBgMid",
    )
    val bgBottom = Color(0xFF0A090C)
    val ivoryInk = Color(0xFFF3EEE4)
    val mutedInk = Color(0xFF9F9890)
    val cinnabar = Color(0xFFC83E2B)

    val stackSongs = remember(song.id, allSongs) {
        if (allSongs.isEmpty()) {
            listOf(song)
        } else {
            val idx = allSongs.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
            List(minOf(4, allSongs.size)) { step ->
                allSongs[(idx + step) % allSongs.size]
            }
        }
    }

    val haptic = LocalHapticFeedback.current
    val hapticEnabled = LocalHapticEnabled.current
    val scope = rememberCoroutineScope()
    val dragOffsetX = remember { Animatable(0f) }
    val tapScale = remember { Animatable(1f) }
    val density = LocalDensity.current.density

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0.0f to bgTop,
                    0.48f to bgMid,
                    1.0f to bgBottom,
                ),
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        val stageW = maxWidth
        val stageH = maxHeight
        val frontSize = min(stageW * 0.56f, stageH * 0.31f)
        val discSize = frontSize * 0.88f

        // Soft ambient glow behind the 3D sleeve stack
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    0.0f to dynamicAccent.copy(alpha = 0.24f),
                    1.0f to Color.Transparent,
                    center = Offset(size.width * 0.48f, size.height * 0.30f),
                    radius = size.width * 0.65f,
                ),
                radius = size.width * 0.65f,
                center = Offset(size.width * 0.48f, size.height * 0.30f),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ImmersiveTopBar(
                title = "空间唱片廊",
                inkColor = ivoryInk,
                currentStyle = currentStyle,
                isTranslatingLyrics = isTranslatingLyrics,
                onSelectStyle = onSelectStyle,
                onBack = onBackToVinyl,
                onTranslateLyrics = onTranslateLyrics,
                onToggleOffsetBar = onToggleOffsetBar,
                onOpenLyricsSearch = onOpenLyricsSearch,
                onOpenCoverSearch = onOpenCoverSearch,
                onOpenFullLyricsSheet = onOpenFullLyricsSheet,
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3D Perspective Receding Physical Sleeve Stack + Peeking LP + Glossy Floor Reflection
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(frontSize * 1.32f)
                    .pointerInput(song.id) {
                        detectTapGestures(
                            onTap = {
                                if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                scope.launch {
                                    tapScale.animateTo(0.95f, tween(80))
                                    tapScale.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium))
                                }
                                onTogglePlayPause()
                            },
                        )
                    }
                    .pointerInput(song.id) {
                        var totalDrag = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { totalDrag = 0f },
                            onHorizontalDrag = { _, dragAmount ->
                                totalDrag += dragAmount
                                scope.launch { dragOffsetX.snapTo((totalDrag * 0.45f).coerceIn(-110f, 110f)) }
                            },
                            onDragEnd = {
                                if (abs(totalDrag) > 72f) {
                                    if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    if (totalDrag < 0f) onSkipNext() else onSkipPrevious()
                                }
                                scope.launch { dragOffsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow)) }
                            },
                            onDragCancel = {
                                scope.launch { dragOffsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow)) }
                            },
                        )
                    },
                contentAlignment = Alignment.TopCenter,
            ) {
                Box(
                    modifier = Modifier
                        .offset { IntOffset(dragOffsetX.value.roundToInt(), 0) }
                        .graphicsLayer {
                            scaleX = tapScale.value
                            scaleY = tapScale.value
                        }
                        .width(frontSize * 1.44f)
                        .height(frontSize * 1.28f),
                    contentAlignment = Alignment.TopStart,
                ) {
                    // Draw receding background sleeves from farthest to nearest (depth 3 -> 1)
                    for (depth in (stackSongs.lastIndex downTo 1)) {
                        val depthSong = stackSongs[depth]
                        val scaleFactor = 1f - depth * 0.085f
                        val xShift = (14 - depth * 12).dp
                        val yShift = (depth * -6 + 18).dp
                        val sleeveAlpha = (1f - depth * 0.22f).coerceAtLeast(0.28f)

                        Box(
                            modifier = Modifier
                                .offset(x = xShift, y = yShift)
                                .size(frontSize)
                                .graphicsLayer {
                                    scaleX = scaleFactor
                                    scaleY = scaleFactor
                                    rotationY = -15f
                                    cameraDistance = 14f * density
                                    alpha = sleeveAlpha
                                }
                                .shadow(12.dp, RoundedCornerShape(3.dp))
                                .border(0.8.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(3.dp)),
                        ) {
                            AlbumArtwork(
                                uri = depthSong.albumArtUri,
                                contentDescription = depthSong.album,
                                modifier = Modifier.fillMaxSize(),
                                cornerRadius = 3.dp,
                                iconSize = 36.dp,
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = depth * 0.16f)),
                            )
                        }
                    }

                    // Peeking glossy spinning black vinyl LP emerging from right opening of front sleeve
                    Box(
                        modifier = Modifier
                            .offset(x = frontSize * 0.56f, y = 18.dp + (frontSize - discSize) / 2f),
                    ) {
                        SpinningVinylDisc(
                            albumArtUri = song.albumArtUri,
                            rotationProvider = vinylRotationProvider,
                            discSize = discSize,
                        )
                    }

                    // Front-most active physical vinyl sleeve (clean razor-thin specular edge, zero clunky plastic frame)
                    Box(
                        modifier = Modifier
                            .offset(x = 24.dp, y = 18.dp)
                            .size(frontSize)
                            .graphicsLayer {
                                rotationY = -13f
                                cameraDistance = 14f * density
                            }
                            .shadow(22.dp, RoundedCornerShape(3.dp), ambientColor = Color.Black)
                            .border(0.9.dp, Color.White.copy(alpha = 0.26f), RoundedCornerShape(3.dp)),
                    ) {
                        AlbumArtwork(
                            uri = song.albumArtUri,
                            contentDescription = song.album,
                            modifier = Modifier.fillMaxSize(),
                            cornerRadius = 3.dp,
                            iconSize = 48.dp,
                        )
                    }

                    // Silky diffused mirror reflection directly beneath the front sleeve
                    Box(
                        modifier = Modifier
                            .offset(x = 24.dp, y = 18.dp + frontSize + 2.dp)
                            .width(frontSize)
                            .height(frontSize * 0.28f)
                            .clip(RectangleShape)
                            .graphicsLayer {
                                rotationY = -13f
                                cameraDistance = 14f * density
                                scaleY = -1f
                                alpha = 0.24f
                            },
                    ) {
                        AlbumArtwork(
                            uri = song.albumArtUri,
                            contentDescription = null,
                            modifier = Modifier.size(frontSize),
                            cornerRadius = 3.dp,
                            iconSize = 32.dp,
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        0.0f to bgBottom,
                                        0.75f to bgBottom.copy(alpha = 0.65f),
                                        1.0f to Color.Transparent,
                                    ),
                                ),
                        )
                    }
                }
            }

            // Centered Editorial Bilingual Serif Lyrics
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (lyrics.isEmpty() || activeIdx < 0) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = lyricFamily,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = ivoryInk,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = mutedInk,
                        textAlign = TextAlign.Center,
                    )
                } else {
                    val prevLine = lyrics.getOrNull(activeIdx - 1)
                    val curLine = lyrics[activeIdx]

                    if (prevLine != null) {
                        Text(
                            text = prevLine.primaryText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = lyricFamily,
                            ),
                            color = mutedInk.copy(alpha = 0.72f),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Text(
                        text = curLine.primaryText,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = lyricFamily,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 34.sp,
                        ),
                        color = ivoryInk,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    if (!curLine.translatedText.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = curLine.translatedText,
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = lyricFamily),
                            color = mutedInk,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            if (showOffsetBar) {
                ImmersiveOffsetStrip(
                    lyricOffsetMs = lyricOffsetMs,
                    inkColor = ivoryInk,
                    accentColor = cinnabar,
                    onAdjust = onAdjustLyricOffset,
                    onReset = onResetLyricOffset,
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            LiveSpectrumWaveformSeekBar(
                songId = song.id,
                positionMs = positionMs,
                durationMs = durationMs,
                isPlaying = isPlaying,
                spectrumBands = spectrumBands,
                playedColor = ivoryInk,
                unplayedColor = Color.White.copy(alpha = 0.24f),
                needleColor = cinnabar,
                timestampColor = mutedInk,
                onSeekTo = onSeekTo,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/**
 * 3. 黑胶光场 (Vinyl Lightfield)
 * Seamless dynamic album-color velvet backdrop (zero horizontal clipping seams!),
 * full-size left-bled 96-groove spinning vinyl LP with complete circular bottom arc + articulated brushed-silver tonearm,
 * first row below vinyl: Song Title & Artist left + vintage brushed champagne-ivory Play/Pause & Prev/Next right,
 * calm vintage italic serif lyrics + Chinese translation, bottom matte-brass live PCM spectrum waveform scrubber.
 */
@Composable
private fun VinylLightfieldImmersiveView(
    song: Song,
    lyrics: List<LyricLine>,
    activeIdx: Int,
    positionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    spectrumBands: FloatArray,
    vinylRotationProvider: () -> Float,
    lyricFamily: FontFamily,
    dynamicAccent: Color,
    currentStyle: ImmersionLyricStyle,
    isTranslatingLyrics: Boolean,
    showOffsetBar: Boolean,
    lyricOffsetMs: Long,
    onToggleOffsetBar: () -> Unit,
    onSelectStyle: (ImmersionLyricStyle) -> Unit,
    onBackToVinyl: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onTranslateLyrics: () -> Unit,
    onOpenLyricsSearch: () -> Unit,
    onOpenCoverSearch: () -> Unit,
    onOpenFullLyricsSheet: () -> Unit,
    onAdjustLyricOffset: (Long) -> Unit,
    onResetLyricOffset: () -> Unit,
) {
    val bgTop by animateColorAsState(
        targetValue = lerp(Color(0xFF1A1612), dynamicAccent, 0.30f),
        animationSpec = tween(550),
        label = "lightfieldBgTop",
    )
    val bgMid by animateColorAsState(
        targetValue = lerp(Color(0xFF14110E), dynamicAccent, 0.18f),
        animationSpec = tween(550),
        label = "lightfieldBgMid",
    )
    val bgBottom = Color(0xFF0C0A09)
    val ivoryInk = Color(0xFFF2ECE1)
    val mutedInk = Color(0xFF9C9388)
    val champagneLight = Color(0xFFE8D8BE)
    val champagneMid = Color(0xFFCBB696)
    val brassNeedle = Color(0xFFC8A97E)

    val tonearmAngle by animateFloatAsState(
        targetValue = if (isPlaying) 24f else 4f,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessLow),
        label = "tonearmSwing",
    )
    val haptic = LocalHapticFeedback.current
    val hapticEnabled = LocalHapticEnabled.current

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0.0f to bgTop,
                    0.52f to bgMid,
                    1.0f to bgBottom,
                ),
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        val stageW = maxWidth
        val stageH = maxHeight
        // Full-size left-bleeding vinyl LP diameter (complete bottom arc cleanly preserved!)
        val discSize = min(stageW * 0.94f, stageH * 0.44f)

        Column(modifier = Modifier.fillMaxSize()) {
            // Minimal top bar (zero song title clutter here)
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                ImmersiveTopBar(
                    title = "",
                    inkColor = ivoryInk,
                    currentStyle = currentStyle,
                    isTranslatingLyrics = isTranslatingLyrics,
                    onSelectStyle = onSelectStyle,
                    onBack = onBackToVinyl,
                    onTranslateLyrics = onTranslateLyrics,
                    onToggleOffsetBar = onToggleOffsetBar,
                    onOpenLyricsSearch = onOpenLyricsSearch,
                    onOpenCoverSearch = onOpenCoverSearch,
                    onOpenFullLyricsSheet = onOpenFullLyricsSheet,
                )
            }

            // Unclipped Upper Vinyl + Brushed-Silver Mechanical Tonearm Stage
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(discSize + 8.dp),
            ) {
                // Full-size left-bled spinning black vinyl LP (offset left ~20%, full bottom rim intact)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = -discSize * 0.19f)
                        .clickable {
                            if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onTogglePlayPause()
                        },
                ) {
                    SpinningVinylDisc(
                        albumArtUri = song.albumArtUri,
                        rotationProvider = vinylRotationProvider,
                        discSize = discSize,
                    )
                }

                // Brushed-silver mechanical tonearm resting cleanly from upper right
                Canvas(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 14.dp, top = 6.dp)
                        .size(width = discSize * 0.46f, height = discSize * 0.88f),
                ) {
                    val pivot = Offset(size.width * 0.76f, size.height * 0.12f)
                    // Base mount
                    drawCircle(
                        color = Color(0xFF202022),
                        radius = size.width * 0.24f,
                        center = pivot,
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.14f),
                        radius = size.width * 0.24f,
                        center = pivot,
                        style = Stroke(width = 1.4f),
                    )
                    drawCircle(
                        color = Color(0xFF141416),
                        radius = size.width * 0.13f,
                        center = pivot,
                    )

                    rotate(degrees = tonearmAngle, pivot = pivot) {
                        val armEnd = Offset(pivot.x - size.width * 0.04f, pivot.y + size.height * 0.68f)
                        // Subtle drop shadow under arm
                        drawLine(
                            color = Color.Black.copy(alpha = 0.48f),
                            start = Offset(pivot.x + 5f, pivot.y + 6f),
                            end = Offset(armEnd.x + 5f, armEnd.y + 6f),
                            strokeWidth = 10f,
                            cap = StrokeCap.Round,
                        )
                        // Counterweight block
                        drawRoundRect(
                            color = Color(0xFF27272A),
                            topLeft = Offset(pivot.x - 16f, pivot.y - 26f),
                            size = Size(32f, 22f),
                            cornerRadius = CornerRadius(4f, 4f),
                        )
                        // Brushed silver tubular tonearm
                        drawLine(
                            brush = Brush.horizontalGradient(
                                0.0f to Color(0xFF9EA2A8),
                                0.5f to Color(0xFFE7E9EC),
                                1.0f to Color(0xFF7D8187),
                            ),
                            start = pivot,
                            end = armEnd,
                            strokeWidth = 7.5f,
                            cap = StrokeCap.Round,
                        )
                        // Matte obsidian headshell cartridge
                        drawRoundRect(
                            color = Color(0xFF19191C),
                            topLeft = Offset(armEnd.x - 12f, armEnd.y - 6f),
                            size = Size(24f, 36f),
                            cornerRadius = CornerRadius(5f, 5f),
                        )
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.16f),
                            topLeft = Offset(armEnd.x - 12f, armEnd.y - 6f),
                            size = Size(24f, 36f),
                            cornerRadius = CornerRadius(5f, 5f),
                            style = Stroke(width = 1f),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // First Row Directly Below Vinyl: Left Song/Artist · Right Vintage Champagne-Gold Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp,
                        ),
                        color = ivoryInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = mutedInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    IconButton(
                        onClick = {
                            if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSkipPrevious()
                        },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipPrevious,
                            contentDescription = "上一首",
                            tint = ivoryInk.copy(alpha = 0.86f),
                            modifier = Modifier.size(22.dp),
                        )
                    }

                    // Vintage brushed champagne-ivory tactile circular Play/Pause button
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .shadow(10.dp, CircleShape, ambientColor = Color.Black)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(champagneLight, champagneMid, Color(0xFFA89375)),
                                ),
                            )
                            .border(1.dp, ivoryInk.copy(alpha = 0.55f), CircleShape)
                            .clickable {
                                if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onTogglePlayPause()
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (isPlaying) "暂停" else "播放",
                            tint = Color(0xFF1A1612),
                            modifier = Modifier.size(24.dp),
                        )
                    }

                    IconButton(
                        onClick = {
                            if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSkipNext()
                        },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = "下一首",
                            tint = ivoryInk.copy(alpha = 0.86f),
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Open Calm Vintage Liner-Note Italic Serif Lyrics Stage
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                if (lyrics.isEmpty() || activeIdx < 0) {
                    Text(
                        text = "暂无歌词 · 点击右上角匹配在线歌词",
                        style = MaterialTheme.typography.bodyLarge.copy(fontFamily = lyricFamily),
                        color = mutedInk,
                    )
                } else {
                    val curLine = lyrics[activeIdx]
                    Text(
                        text = curLine.primaryText,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = lyricFamily,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 38.sp,
                        ),
                        color = ivoryInk,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (!curLine.translatedText.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = curLine.translatedText,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = lyricFamily,
                                fontWeight = FontWeight.Normal,
                            ),
                            color = mutedInk,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            if (showOffsetBar) {
                Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                    ImmersiveOffsetStrip(
                        lyricOffsetMs = lyricOffsetMs,
                        inkColor = ivoryInk,
                        accentColor = brassNeedle,
                        onAdjust = onAdjustLyricOffset,
                        onReset = onResetLyricOffset,
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                LiveSpectrumWaveformSeekBar(
                    songId = song.id,
                    positionMs = positionMs,
                    durationMs = durationMs,
                    isPlaying = isPlaying,
                    spectrumBands = spectrumBands,
                    playedColor = ivoryInk,
                    unplayedColor = ivoryInk.copy(alpha = 0.24f),
                    needleColor = brassNeedle,
                    timestampColor = mutedInk,
                    onSeekTo = onSeekTo,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/**
 * High-density rounded micro-capsule Live Audio Spectrum + Draggable Waveform Progress Scrubber.
 * Played bars on the left of the razor-thin vertical playhead needle dance with real-time PCM audio spectrum energy;
 * unplayed bars on the right show a refined resting contour; dragging anywhere seeks smoothly.
 */
@Composable
fun LiveSpectrumWaveformSeekBar(
    songId: Long,
    positionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    spectrumBands: FloatArray,
    playedColor: Color,
    unplayedColor: Color,
    needleColor: Color,
    timestampColor: Color,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val barCount = 56
    val baseProfile = remember(songId) {
        FloatArray(barCount) { idx ->
            val seed = ((songId * 31L + idx * 17L) xor (idx.toLong() * 131L)) and 0xFFFF
            val wave = 0.32f + 0.24f * sin(idx * 0.38f + (songId % 7)).toFloat()
            val jitter = ((seed % 100) / 100f) * 0.28f
            (wave + jitter).coerceIn(0.18f, 0.82f)
        }
    }

    val haptic = LocalHapticFeedback.current
    val hapticEnabled = LocalHapticEnabled.current
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }
    var widthPx by remember { mutableFloatStateOf(1f) }

    val progress = if (isDragging) {
        dragFraction
    } else {
        if (durationMs > 0L) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    }

    val displayPosMs = if (isDragging) (dragFraction * durationMs).toLong() else positionMs

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .onSizeChanged { widthPx = it.width.toFloat().coerceAtLeast(1f) }
                .pointerInput(durationMs) {
                    detectTapGestures { offset ->
                        if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        val frac = (offset.x / widthPx).coerceIn(0f, 1f)
                        onSeekTo((frac * durationMs).toLong())
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
                    .height(28.dp),
            ) {
                val totalW = size.width
                val totalH = size.height
                val midY = totalH / 2f
                val step = totalW / barCount
                val barW = (step * 0.48f).coerceIn(2.0f, 4.2f)
                val playheadX = totalW * progress
                val bandCount = spectrumBands.size.coerceAtLeast(1)

                for (i in 0 until barCount) {
                    val cx = i * step + step / 2f
                    val isPlayed = cx <= playheadX
                    val baseAmp = baseProfile[i]
                    val liveBand = spectrumBands[(i * bandCount) / barCount]
                    val heightFrac = if (isPlaying && isPlayed) {
                        // Played zone dances smoothly with real PCM audio spectrum + base contour
                        (baseAmp * 0.42f + liveBand * 0.85f).coerceIn(0.16f, 0.96f)
                    } else if (isPlaying) {
                        // Unplayed zone breathes very subtly
                        (baseAmp * 0.62f + liveBand * 0.14f).coerceIn(0.14f, 0.72f)
                    } else {
                        // Paused resting silhouette
                        (baseAmp * 0.56f).coerceIn(0.14f, 0.60f)
                    }

                    val barH = (totalH * heightFrac).coerceAtLeast(4f)
                    drawRoundRect(
                        color = if (isPlayed) playedColor else unplayedColor,
                        topLeft = Offset(cx - barW / 2f, midY - barH / 2f),
                        size = Size(barW, barH),
                        cornerRadius = CornerRadius(barW / 2f, barW / 2f),
                    )
                }

                // Razor-thin vertical playhead needle right at the exact played/unplayed boundary
                val needleX = playheadX.coerceIn(1.5f, totalW - 1.5f)
                drawLine(
                    color = needleColor,
                    start = Offset(needleX, 0f),
                    end = Offset(needleX, totalH),
                    strokeWidth = if (isDragging) 3.4f else 2.4f,
                    cap = StrokeCap.Round,
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = formatDuration(displayPosMs),
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = timestampColor,
            )
            Text(
                text = formatDuration(durationMs),
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = timestampColor,
            )
        }
    }
}

@Composable
private fun ImmersiveTopBar(
    title: String,
    inkColor: Color,
    currentStyle: ImmersionLyricStyle,
    isTranslatingLyrics: Boolean,
    onSelectStyle: (ImmersionLyricStyle) -> Unit,
    onBack: () -> Unit,
    onTranslateLyrics: () -> Unit,
    onToggleOffsetBar: () -> Unit,
    onOpenLyricsSearch: () -> Unit,
    onOpenCoverSearch: () -> Unit,
    onOpenFullLyricsSheet: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                contentDescription = "返回播放页",
                tint = inkColor.copy(alpha = 0.88f),
                modifier = Modifier.size(26.dp),
            )
        }

        if (title.isNotBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp,
                ),
                color = inkColor.copy(alpha = 0.72f),
            )
        } else {
            Spacer(modifier = Modifier.width(1.dp))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .border(0.8.dp, inkColor.copy(alpha = 0.28f), RoundedCornerShape(10.dp))
                    .clickable(enabled = !isTranslatingLyrics, onClick = onTranslateLyrics)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (isTranslatingLyrics) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 1.5.dp,
                        color = inkColor,
                    )
                } else {
                    Text(
                        text = "译",
                        style = MaterialTheme.typography.labelSmall,
                        color = inkColor.copy(alpha = 0.85f),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            Box {
                IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.MoreHoriz,
                        contentDescription = "切换沉浸场景与歌词设置",
                        tint = inkColor.copy(alpha = 0.88f),
                        modifier = Modifier.size(22.dp),
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    ImmersionLyricStyle.entries.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option.label,
                                    fontWeight = if (option == currentStyle) FontWeight.Bold else FontWeight.Normal,
                                )
                            },
                            trailingIcon = if (option == currentStyle) {
                                {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            } else {
                                null
                            },
                            onClick = {
                                menuExpanded = false
                                onSelectStyle(option)
                            },
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("歌词时间轴微调") },
                        onClick = {
                            menuExpanded = false
                            onToggleOffsetBar()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("查看完整歌词列表") },
                        onClick = {
                            menuExpanded = false
                            onOpenFullLyricsSheet()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("在线匹配歌词") },
                        onClick = {
                            menuExpanded = false
                            onOpenLyricsSearch()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("搜索替换封面") },
                        onClick = {
                            menuExpanded = false
                            onOpenCoverSearch()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ImmersiveOffsetStrip(
    lyricOffsetMs: Long,
    inkColor: Color,
    accentColor: Color,
    onAdjust: (Long) -> Unit,
    onReset: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(inkColor.copy(alpha = 0.08f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "提前 0.5s",
            style = MaterialTheme.typography.labelMedium,
            color = inkColor,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onAdjust(-500L) }
                .padding(horizontal = 8.dp, vertical = 4.dp),
        )
        Text(
            text = "偏移 ${String.format("%+.1fs", lyricOffsetMs / 1000f)} (点此重置)",
            style = MaterialTheme.typography.labelSmall,
            color = accentColor,
            modifier = Modifier.clickable(onClick = onReset),
        )
        Text(
            text = "延后 0.5s",
            style = MaterialTheme.typography.labelMedium,
            color = inkColor,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onAdjust(500L) }
                .padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}
