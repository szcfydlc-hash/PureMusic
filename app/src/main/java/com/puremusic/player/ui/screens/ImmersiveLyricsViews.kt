package com.puremusic.player.ui.screens

import androidx.compose.animation.scaleOut
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.puremusic.player.data.model.AudioTechSpec
import com.puremusic.player.data.model.ImmersionLyricStyle
import com.puremusic.player.data.model.SpectrumStyle
import com.puremusic.player.data.model.LyricLine
import com.puremusic.player.data.model.Song
import com.puremusic.player.service.AudioSpectrumBus
import com.puremusic.player.ui.components.AlbumArtwork
import com.puremusic.player.ui.components.formatDuration
import com.puremusic.player.ui.theme.LocalAppFontWeightValue
import com.puremusic.player.ui.theme.LocalHapticEnabled
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun ImmersiveLyricsHost(
    style: ImmersionLyricStyle,
    onSelectStyle: (ImmersionLyricStyle) -> Unit,
    spectrumStyle: SpectrumStyle = SpectrumStyle.SILK_RIBBON,
    onSelectSpectrumStyle: (SpectrumStyle) -> Unit = {},
    song: Song,
    allSongs: List<Song>,
    lyrics: List<LyricLine>,
    audioSpec: AudioTechSpec = AudioTechSpec(),
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
    architecturalGridContent: @Composable () -> Unit,
) {
    val effectivePosMs = (positionMs + lyricOffsetMs).coerceAtLeast(0L)
    val activeIdx = remember(lyrics, effectivePosMs) {
        val found = lyrics.indexOfLast { it.timeMs <= effectivePosMs }
        if (found >= 0) found else if (lyrics.isNotEmpty()) 0 else -1
    }
    var showOffsetBar by remember { mutableStateOf(false) }
    val spectrumBands by AudioSpectrumBus.bands.collectAsState()
    val view = LocalView.current
    val sceneHasDarkBackdrop = when (style) {
        ImmersionLyricStyle.SPATIAL_GALLERY, ImmersionLyricStyle.VINYL_LIGHTFIELD -> true
        ImmersionLyricStyle.VINYL_STUDY, ImmersionLyricStyle.ARCHITECTURAL_GRID -> isDarkTheme
    }
    DisposableEffect(sceneHasDarkBackdrop, isDarkTheme) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        controller?.isAppearanceLightStatusBars = !sceneHasDarkBackdrop
        controller?.isAppearanceLightNavigationBars = !sceneHasDarkBackdrop
        onDispose {
            controller?.isAppearanceLightStatusBars = !isDarkTheme
            controller?.isAppearanceLightNavigationBars = !isDarkTheme
        }
    }

    AnimatedContent(
        targetState = style,
        transitionSpec = {
            (fadeIn(tween(260)) + scaleIn(initialScale = 0.97f, animationSpec = tween(260))) togetherWith
                (fadeOut(tween(180)) + scaleOut(targetScale = 1.02f, animationSpec = tween(180)))
        },
        label = "immersionSceneSwitch",
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {},
    ) { targetStyle ->
        when (targetStyle) {
            ImmersionLyricStyle.ARCHITECTURAL_GRID -> {
                architecturalGridContent()
            }

            ImmersionLyricStyle.VINYL_STUDY -> {
                VinylStudyImmersiveView(
                    song = song,
                    lyrics = lyrics,
                    audioSpec = audioSpec,
                    activeIdx = activeIdx,
                    positionMs = positionMs,
                    durationMs = durationMs,
                    isPlaying = isPlaying,
                    spectrumBands = spectrumBands,
                    vinylRotationProvider = vinylRotationProvider,
                    lyricFamily = lyricFamily,
                    lyricStyleItalic = lyricStyleItalic,
                    isDarkTheme = isDarkTheme,
                    currentStyle = targetStyle,
                    isTranslatingLyrics = isTranslatingLyrics,
                    showOffsetBar = showOffsetBar,
                    lyricOffsetMs = lyricOffsetMs,
                    onToggleOffsetBar = { showOffsetBar = !showOffsetBar },
                    onSelectStyle = onSelectStyle,
                    spectrumStyle = spectrumStyle,
                    onSelectSpectrumStyle = onSelectSpectrumStyle,
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
                    audioSpec = audioSpec,
                    activeIdx = activeIdx,
                    positionMs = positionMs,
                    durationMs = durationMs,
                    isPlaying = isPlaying,
                    spectrumBands = spectrumBands,
                    lyricFamily = lyricFamily,
                    lyricStyleItalic = lyricStyleItalic,
                    dynamicAccent = dynamicAccent,
                    currentStyle = targetStyle,
                    isTranslatingLyrics = isTranslatingLyrics,
                    showOffsetBar = showOffsetBar,
                    lyricOffsetMs = lyricOffsetMs,
                    onToggleOffsetBar = { showOffsetBar = !showOffsetBar },
                    onSelectStyle = onSelectStyle,
                    spectrumStyle = spectrumStyle,
                    onSelectSpectrumStyle = onSelectSpectrumStyle,
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
                    audioSpec = audioSpec,
                    activeIdx = activeIdx,
                    positionMs = positionMs,
                    durationMs = durationMs,
                    isPlaying = isPlaying,
                    spectrumBands = spectrumBands,
                    vinylRotationProvider = vinylRotationProvider,
                    lyricFamily = lyricFamily,
                    lyricStyleItalic = lyricStyleItalic,
                    dynamicAccent = dynamicAccent,
                    currentStyle = targetStyle,
                    isTranslatingLyrics = isTranslatingLyrics,
                    showOffsetBar = showOffsetBar,
                    lyricOffsetMs = lyricOffsetMs,
                    onToggleOffsetBar = { showOffsetBar = !showOffsetBar },
                    onSelectStyle = onSelectStyle,
                    spectrumStyle = spectrumStyle,
                    onSelectSpectrumStyle = onSelectSpectrumStyle,
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
 */
@Composable
private fun VinylStudyImmersiveView(
    song: Song,
    lyrics: List<LyricLine>,
    audioSpec: AudioTechSpec,
    activeIdx: Int,
    positionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    spectrumBands: FloatArray,
    vinylRotationProvider: () -> Float,
    lyricFamily: FontFamily,
    lyricStyleItalic: FontStyle,
    isDarkTheme: Boolean,
    currentStyle: ImmersionLyricStyle,
    isTranslatingLyrics: Boolean,
    showOffsetBar: Boolean,
    lyricOffsetMs: Long,
    onToggleOffsetBar: () -> Unit,
    onSelectStyle: (ImmersionLyricStyle) -> Unit,
    spectrumStyle: SpectrumStyle,
    onSelectSpectrumStyle: (SpectrumStyle) -> Unit,
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
        val sleeveSize = min(stageW * 0.62f, stageH * (if (isCompactH) 0.34f else 0.35f))
        val discSize = sleeveSize * 0.92f

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
        ) {
            ImmersiveTopBar(
                title = "",
                inkColor = inkPrimary,
                currentStyle = currentStyle,
                isDarkSurface = isDarkTheme,
                isTranslatingLyrics = isTranslatingLyrics,
                onSelectStyle = onSelectStyle,
                currentSpectrumStyle = spectrumStyle,
                onSelectSpectrumStyle = onSelectSpectrumStyle,
                onBack = onBackToVinyl,
                onTranslateLyrics = onTranslateLyrics,
                onToggleOffsetBar = onToggleOffsetBar,
                onOpenLyricsSearch = onOpenLyricsSearch,
                onOpenCoverSearch = onOpenCoverSearch,
                onOpenFullLyricsSheet = onOpenFullLyricsSheet,
            )

            Spacer(modifier = Modifier.height(if (isCompactH) 8.dp else 14.dp))

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
                contentAlignment = Alignment.Center,
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

            Spacer(modifier = Modifier.height(if (isCompactH) 12.dp else 20.dp))

            SongInfoSpecHeader(
                title = song.title,
                artist = song.artist,
                audioSpec = audioSpec,
                titleColor = inkPrimary,
                artistColor = inkSecondary,
                accentColor = cinnabar,
                centered = true,
            )

            Spacer(modifier = Modifier.height(if (isCompactH) 10.dp else 16.dp))
            HorizontalDivider(
                thickness = 0.6.dp,
                color = inkMuted.copy(alpha = 0.35f),
            )
            Spacer(modifier = Modifier.height(if (isCompactH) 10.dp else 20.dp))

            ThreeSlotAutoFitLyricsStage(
                lyrics = lyrics,
                activeIdx = activeIdx,
                lyricFamily = lyricFamily,
                lyricStyleItalic = lyricStyleItalic,
                activeColor = inkPrimary,
                secondaryColor = inkSecondary,
                mutedColor = inkMuted,
                horizontalAlignment = Alignment.CenterHorizontally,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )

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
                spectrumStyle = spectrumStyle,
                playedColor = inkPrimary,
                unplayedColor = inkMuted.copy(alpha = 0.45f),
                needleColor = cinnabar,
                timestampColor = inkSecondary,
                onSeekTo = onSeekTo,
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

/**
 * 2. 空间唱片廊 (Spatial Record Gallery)
 * True diagonal upper-right receding 3D album sleeve stack matching reference geometry,
 * seamless alpha-masked floor mirror reflection (zero dark box seam), pure album-color velvet backdrop,
 * swipe-to-skip & tap-to-play/pause gestures, refined typography + 60fps live PCM spectrum scrubber.
 */
@Composable
private fun SpatialGalleryImmersiveView(
    song: Song,
    allSongs: List<Song>,
    lyrics: List<LyricLine>,
    audioSpec: AudioTechSpec,
    activeIdx: Int,
    positionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    spectrumBands: FloatArray,
    lyricFamily: FontFamily,
    lyricStyleItalic: FontStyle,
    dynamicAccent: Color,
    currentStyle: ImmersionLyricStyle,
    isTranslatingLyrics: Boolean,
    showOffsetBar: Boolean,
    lyricOffsetMs: Long,
    onToggleOffsetBar: () -> Unit,
    onSelectStyle: (ImmersionLyricStyle) -> Unit,
    spectrumStyle: SpectrumStyle,
    onSelectSpectrumStyle: (SpectrumStyle) -> Unit,
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
        targetValue = lerp(Color(0xFF161113), dynamicAccent, 0.34f),
        animationSpec = tween(550),
        label = "galleryBgTop",
    )
    val bgMid by animateColorAsState(
        targetValue = lerp(Color(0xFF110D0F), dynamicAccent, 0.20f),
        animationSpec = tween(550),
        label = "galleryBgMid",
    )
    val bgBottom = Color(0xFF0A0809)
    val ivoryInk = Color(0xFFF3EEE4)
    val mutedInk = Color(0xFF9F9890)
    val cinnabar = Color(0xFFC83E2B)

    // 6-sleeve flagship corridor (1 commanding hero + 5 receding stepped sleeves)
    val stackSongs = remember(song.id, allSongs) {
        if (allSongs.isEmpty()) {
            listOf(song)
        } else {
            val idx = allSongs.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
            List(minOf(6, allSongs.size)) { step ->
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
                    0.50f to bgMid,
                    1.0f to bgBottom,
                ),
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        val stageW = maxWidth
        val stageH = maxHeight
        // Commanding foreground hero cover (~72% screen width, max 38% height)
        val frontSize = min(stageW * 0.72f, stageH * 0.38f)

        // Warm studio ambient backdrop light + subtle glossy floor horizon glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    0.0f to dynamicAccent.copy(alpha = 0.26f),
                    0.65f to dynamicAccent.copy(alpha = 0.08f),
                    1.0f to Color.Transparent,
                    center = Offset(size.width * 0.44f, size.height * 0.30f),
                    radius = size.width * 0.78f,
                ),
                radius = size.width * 0.78f,
                center = Offset(size.width * 0.44f, size.height * 0.30f),
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                ImmersiveTopBar(
                    title = "",
                    inkColor = ivoryInk,
                    currentStyle = currentStyle,
                    isTranslatingLyrics = isTranslatingLyrics,
                    onSelectStyle = onSelectStyle,
                    currentSpectrumStyle = spectrumStyle,
                    onSelectSpectrumStyle = onSelectSpectrumStyle,
                    onBack = onBackToVinyl,
                    onTranslateLyrics = onTranslateLyrics,
                    onToggleOffsetBar = onToggleOffsetBar,
                    onOpenLyricsSearch = onOpenLyricsSearch,
                    onOpenCoverSearch = onOpenCoverSearch,
                    onOpenFullLyricsSheet = onOpenFullLyricsSheet,
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Full-bleed 3D Flagship Record Gallery Stage (hero + 5 stepped metallic sleeves + peeking vinyl + mirror floor)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(frontSize * 1.38f)
                    .pointerInput(song.id) {
                        detectTapGestures(
                            onTap = {
                                if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                scope.launch {
                                    tapScale.animateTo(0.96f, tween(80))
                                    tapScale.animateTo(1f, spring(dampingRatio = 0.62f, stiffness = Spring.StiffnessMedium))
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
                contentAlignment = Alignment.TopStart,
            ) {
                val baseX = (stageW - frontSize) * 0.30f
                val baseY = 58.dp
                val dynamicRotY = 16f + (dragOffsetX.value * 0.07f).coerceIn(-7f, 7f)
                val camDist = 6.2f * density

                // Stepped 1/z recession factors tuned so all 6 sleeves frame cleanly inside screen width
                val scaleSteps = floatArrayOf(1.00f, 0.88f, 0.78f, 0.70f, 0.63f, 0.57f)
                val xFractions = floatArrayOf(0.00f, 0.21f, 0.37f, 0.50f, 0.60f, 0.68f)
                val yLiftDp = floatArrayOf(0f, 18f, 33f, 45f, 54f, 61f)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset { IntOffset(dragOffsetX.value.roundToInt(), 0) }
                        .graphicsLayer {
                            scaleX = tapScale.value
                            scaleY = tapScale.value
                        },
                ) {
                    for (depth in (stackSongs.lastIndex downTo 0)) {
                        val itemSong = stackSongs[depth]
                        val isFront = depth == 0
                        val dIdx = depth.coerceAtMost(scaleSteps.lastIndex)
                        val p = scaleSteps[dIdx]
                        val xShift = baseX + (frontSize * xFractions[dIdx])
                        val yShift = (baseY - yLiftDp[dIdx].dp).coerceAtLeast(2.dp)
                        val rotY = if (isFront) dynamicRotY else 15f
                        val sleeveAlpha = (1f - depth * 0.08f).coerceAtLeast(0.46f)
                        val spineW = 8.5.dp
                        val topRimH = 3.5.dp

                        // Unified 3D plane per sleeve: jacket + peeking vinyl + contact AO + mirror reflection
                        Column(
                            modifier = Modifier
                                .offset(x = xShift, y = yShift)
                                .width(frontSize + 18.dp)
                                .graphicsLayer {
                                    transformOrigin = TransformOrigin(0.0f, 0.42f)
                                    scaleX = p
                                    scaleY = p
                                    rotationY = rotY
                                    cameraDistance = camDist
                                    alpha = sleeveAlpha
                                },
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(frontSize + 18.dp)
                                    .height(frontSize),
                            ) {
                                // Subtle black vinyl LP peeking out to the right on receding sleeves (depths 1..4)
                                if (depth in 1..4) {
                                    Canvas(
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .size(frontSize * 0.86f),
                                    ) {
                                        val r = size.minDimension * 0.5f
                                        val c = Offset(size.width * 0.5f, size.height * 0.5f)
                                        drawCircle(
                                            brush = Brush.radialGradient(
                                                0.0f to Color(0xFF0A0A0B),
                                                0.72f to Color(0xFF171719),
                                                0.90f to Color(0xFF28282B),
                                                1.0f to Color(0xFF0D0D0F),
                                                center = c,
                                                radius = r,
                                            ),
                                            radius = r,
                                            center = c,
                                        )
                                        for (g in 1..5) {
                                            drawCircle(
                                                color = Color.White.copy(alpha = 0.06f),
                                                radius = r * (0.64f + g * 0.065f),
                                                center = c,
                                                style = Stroke(width = 0.7f),
                                            )
                                        }
                                    }
                                }

                                // Thick metallic-spined physical vinyl jacket
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .size(frontSize)
                                        .shadow(
                                            elevation = if (isFront) 32.dp else 18.dp,
                                            shape = RoundedCornerShape(3.dp),
                                            ambientColor = Color.Black,
                                            spotColor = Color.Black,
                                        )
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color(0xFF1E1D1C)),
                                ) {
                                    Row(modifier = Modifier.fillMaxSize()) {
                                        // 8.5dp Machined Brushed-Aluminum 3D Left Spine with specular corner bevel
                                        Box(
                                            modifier = Modifier
                                                .width(spineW)
                                                .fillMaxSize()
                                                .background(
                                                    Brush.horizontalGradient(
                                                        0.00f to Color(0xFF6B6A67),
                                                        0.18f to Color(0xFFEBE9E5),
                                                        0.46f to Color(0xFFC5C3BE),
                                                        0.78f to Color(0xFF7D7B77),
                                                        0.94f to Color(0xFF3E3D3B),
                                                        1.00f to Color(0xFF1A1918),
                                                    ),
                                                ),
                                        ) {
                                            // Vertical sheen gradient along the metallic spine
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(
                                                        Brush.verticalGradient(
                                                            0.0f to Color.White.copy(alpha = 0.24f),
                                                            0.25f to Color.Transparent,
                                                            0.78f to Color.Black.copy(alpha = 0.18f),
                                                            1.0f to Color.Black.copy(alpha = 0.36f),
                                                        ),
                                                    ),
                                            )
                                        }

                                        // Jacket front face + beveled metallic top rim + recessed dark bezel
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxSize(),
                                        ) {
                                            // 3.5dp Top Metallic Bevel Rim
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(topRimH)
                                                    .background(
                                                        Brush.verticalGradient(
                                                            0.0f to Color(0xFFF0EEEA),
                                                            0.55f to Color(0xFFB5B3AE),
                                                            1.0f to Color(0xFF4A4946),
                                                        ),
                                                    ),
                                            )

                                            // Recessed dark gunmetal border framing the album cover
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxWidth()
                                                    .background(Color(0xFF161514))
                                                    .padding(1.5.dp)
                                                    .border(0.6.dp, Color.White.copy(alpha = 0.22f), RectangleShape),
                                            ) {
                                                AlbumArtwork(
                                                    uri = itemSong.albumArtUri,
                                                    contentDescription = itemSong.album,
                                                    modifier = Modifier.fillMaxSize(),
                                                    cornerRadius = 0.dp,
                                                    iconSize = if (isFront) 54.dp else 34.dp,
                                                )

                                                if (isFront) {
                                                    // Subtle museum-grade diagonal satin glass sheen + soft bottom-right vignette
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .background(
                                                                Brush.linearGradient(
                                                                    0.00f to Color.White.copy(alpha = 0.14f),
                                                                    0.32f to Color.White.copy(alpha = 0.03f),
                                                                    0.52f to Color.Transparent,
                                                                    0.82f to Color.Black.copy(alpha = 0.06f),
                                                                    1.00f to Color.Black.copy(alpha = 0.20f),
                                                                ),
                                                            ),
                                                    )
                                                } else {
                                                    // Inter-sleeve cast shadow from the sleeve directly in front + depth darkening
                                                    val depthDark = (0.16f + depth * 0.11f).coerceAtMost(0.68f)
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .background(
                                                                Brush.horizontalGradient(
                                                                    0.0f to Color.Black.copy(alpha = (depthDark + 0.34f).coerceAtMost(0.84f)),
                                                                    0.55f to Color.Black.copy(alpha = depthDark + 0.12f),
                                                                    1.0f to Color.Black.copy(alpha = depthDark),
                                                                ),
                                                            ),
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Razor-thin dark Contact Ambient Occlusion (AO) line anchoring sleeve onto glossy floor
                            Box(
                                modifier = Modifier
                                    .width(frontSize)
                                    .height(2.5.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            0.0f to Color.Black.copy(alpha = if (isFront) 0.88f else 0.65f),
                                            1.0f to Color.Black.copy(alpha = 0.0f),
                                        ),
                                    ),
                            )

                            // Seamless parallel mirror floor reflection (shares exact 3D plane -> zero V-gap)
                            if (depth <= 3) {
                                val reflStrength = when (depth) {
                                    0 -> 0.40f
                                    1 -> 0.20f
                                    2 -> 0.11f
                                    else -> 0.06f
                                }
                                Row(
                                    modifier = Modifier
                                        .width(frontSize)
                                        .height(frontSize * 0.34f)
                                        .graphicsLayer {
                                            compositingStrategy = CompositingStrategy.Offscreen
                                        }
                                        .drawWithContent {
                                            drawContent()
                                            drawRect(
                                                brush = Brush.verticalGradient(
                                                    0.00f to Color.Black.copy(alpha = reflStrength),
                                                    0.52f to Color.Black.copy(alpha = reflStrength * 0.26f),
                                                    1.00f to Color.Transparent,
                                                ),
                                                blendMode = BlendMode.DstIn,
                                            )
                                        }
                                        .clip(RectangleShape),
                                ) {
                                    // Reflected metallic spine continuation
                                    Box(
                                        modifier = Modifier
                                            .width(spineW)
                                            .fillMaxSize()
                                            .background(
                                                Brush.horizontalGradient(
                                                    0.0f to Color(0xFF4F4E4B),
                                                    0.35f to Color(0xFFA8A6A1),
                                                    1.0f to Color(0xFF2B2A28),
                                                ),
                                            ),
                                    )
                                    AlbumArtwork(
                                        uri = itemSong.albumArtUri,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(frontSize)
                                            .graphicsLayer { scaleY = -1f },
                                        cornerRadius = 0.dp,
                                        iconSize = 32.dp,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Song title + artist subtle anchor + centered refined lyrics
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SongInfoSpecHeader(
                    title = song.title,
                    artist = song.artist,
                    audioSpec = audioSpec,
                    titleColor = ivoryInk,
                    artistColor = mutedInk,
                    accentColor = Color(0xFFD8B888),
                    centered = true,
                )
                Spacer(modifier = Modifier.height(8.dp))

                ThreeSlotAutoFitLyricsStage(
                    lyrics = lyrics,
                    activeIdx = activeIdx,
                    lyricFamily = lyricFamily,
                    lyricStyleItalic = lyricStyleItalic,
                    activeColor = ivoryInk,
                    secondaryColor = mutedInk,
                    mutedColor = mutedInk.copy(alpha = 0.65f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                )
            }

            if (showOffsetBar) {
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                    ImmersiveOffsetStrip(
                        lyricOffsetMs = lyricOffsetMs,
                        inkColor = ivoryInk,
                        accentColor = cinnabar,
                        onAdjust = onAdjustLyricOffset,
                        onReset = onResetLyricOffset,
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                LiveSpectrumWaveformSeekBar(
                    songId = song.id,
                    positionMs = positionMs,
                    durationMs = durationMs,
                    isPlaying = isPlaying,
                    spectrumBands = spectrumBands,
                    spectrumStyle = spectrumStyle,
                    playedColor = ivoryInk,
                    unplayedColor = Color.White.copy(alpha = 0.24f),
                    needleColor = cinnabar,
                    timestampColor = mutedInk,
                    onSeekTo = onSeekTo,
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

/**
 * 3. 黑胶光场 (Vinyl Lightfield)
 * Seamless dynamic album-color velvet backdrop, full-size left-bled spinning LP + brushed-silver tonearm,
 * complete stacked Song Title & Artist on the left beside compact champagne-gold controls,
 * refined appropriately sized lyrics + 60fps live PCM spectrum waveform scrubber.
 */
@Composable
private fun VinylLightfieldImmersiveView(
    song: Song,
    lyrics: List<LyricLine>,
    audioSpec: AudioTechSpec,
    activeIdx: Int,
    positionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    spectrumBands: FloatArray,
    vinylRotationProvider: () -> Float,
    lyricFamily: FontFamily,
    lyricStyleItalic: FontStyle,
    dynamicAccent: Color,
    currentStyle: ImmersionLyricStyle,
    isTranslatingLyrics: Boolean,
    showOffsetBar: Boolean,
    lyricOffsetMs: Long,
    onToggleOffsetBar: () -> Unit,
    onSelectStyle: (ImmersionLyricStyle) -> Unit,
    spectrumStyle: SpectrumStyle,
    onSelectSpectrumStyle: (SpectrumStyle) -> Unit,
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
        val discSize = min(stageW * 0.94f, stageH * 0.43f)

        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                ImmersiveTopBar(
                    title = "",
                    inkColor = ivoryInk,
                    currentStyle = currentStyle,
                    isTranslatingLyrics = isTranslatingLyrics,
                    onSelectStyle = onSelectStyle,
                    currentSpectrumStyle = spectrumStyle,
                    onSelectSpectrumStyle = onSelectSpectrumStyle,
                    onBack = onBackToVinyl,
                    onTranslateLyrics = onTranslateLyrics,
                    onToggleOffsetBar = onToggleOffsetBar,
                    onOpenLyricsSearch = onOpenLyricsSearch,
                    onOpenCoverSearch = onOpenCoverSearch,
                    onOpenFullLyricsSheet = onOpenFullLyricsSheet,
                )
            }

            // Unclipped Full-Size Left-Bled Vinyl + Brushed-Silver Tonearm Stage
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(discSize + 8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = -discSize * 0.19f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
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

                Canvas(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 14.dp, top = 6.dp)
                        .size(width = discSize * 0.46f, height = discSize * 0.88f),
                ) {
                    val pivot = Offset(size.width * 0.74f, size.height * 0.13f)
                    // Machined anodized base housing with chamfered highlight rings
                    drawCircle(color = Color(0xFF1E1D1C), radius = size.width * 0.25f, center = pivot)
                    drawCircle(
                        color = Color(0xFFD8C6AA).copy(alpha = 0.28f),
                        radius = size.width * 0.25f,
                        center = pivot,
                        style = Stroke(width = 1.6f),
                    )
                    drawCircle(color = Color(0xFF131211), radius = size.width * 0.16f, center = pivot)
                    drawCircle(
                        brush = Brush.linearGradient(listOf(champagneLight, Color(0xFF8F7B5E))),
                        radius = size.width * 0.085f,
                        center = pivot,
                    )

                    rotate(degrees = tonearmAngle, pivot = pivot) {
                        val armEnd = Offset(pivot.x - size.width * 0.05f, pivot.y + size.height * 0.67f)
                        // Soft shadow cast onto vinyl surface
                        drawLine(
                            color = Color.Black.copy(alpha = 0.55f),
                            start = Offset(pivot.x + 6f, pivot.y + 7f),
                            end = Offset(armEnd.x + 6f, armEnd.y + 7f),
                            strokeWidth = 12f,
                            cap = StrokeCap.Round,
                        )
                        // Precision counterweight cylinder
                        drawRoundRect(
                            brush = Brush.horizontalGradient(listOf(Color(0xFF3A3835), Color(0xFF1F1E1C))),
                            topLeft = Offset(pivot.x - 17f, pivot.y - 30f),
                            size = Size(34f, 24f),
                            cornerRadius = CornerRadius(5f, 5f),
                        )
                        drawRoundRect(
                            color = champagneMid.copy(alpha = 0.35f),
                            topLeft = Offset(pivot.x - 17f, pivot.y - 30f),
                            size = Size(34f, 24f),
                            cornerRadius = CornerRadius(5f, 5f),
                            style = Stroke(width = 1f),
                        )
                        // Brushed silver-champagne tubular arm
                        drawLine(
                            brush = Brush.horizontalGradient(
                                0.0f to Color(0xFF9C9890),
                                0.45f to Color(0xFFF0EAE0),
                                1.0f to Color(0xFF77726B),
                            ),
                            start = pivot,
                            end = armEnd,
                            strokeWidth = 9.5f,
                            cap = StrokeCap.Round,
                        )
                        // Headshell cartridge with champagne-gold finger-lift tab
                        drawRoundRect(
                            brush = Brush.verticalGradient(listOf(Color(0xFF2B2825), Color(0xFF171514))),
                            topLeft = Offset(armEnd.x - 13f, armEnd.y - 4f),
                            size = Size(26f, 38f),
                            cornerRadius = CornerRadius(5f, 5f),
                        )
                        drawRoundRect(
                            color = champagneLight.copy(alpha = 0.42f),
                            topLeft = Offset(armEnd.x - 13f, armEnd.y - 4f),
                            size = Size(26f, 38f),
                            cornerRadius = CornerRadius(5f, 5f),
                            style = Stroke(width = 1.2f),
                        )
                        // Subtle gold finger-lift lever on right of headshell
                        drawLine(
                            color = champagneLight.copy(alpha = 0.85f),
                            start = Offset(armEnd.x + 12f, armEnd.y + 10f),
                            end = Offset(armEnd.x + 24f, armEnd.y + 18f),
                            strokeWidth = 2.6f,
                            cap = StrokeCap.Round,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // First Row Directly Below Vinyl: Complete 2-Line Stacked Song Title + Artist on Left, Controls on Right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                SongInfoSpecHeader(
                    title = song.title,
                    artist = song.artist,
                    audioSpec = audioSpec,
                    titleColor = ivoryInk,
                    artistColor = mutedInk,
                    accentColor = brassNeedle,
                    centered = false,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp),
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    IconButton(
                        onClick = {
                            if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSkipPrevious()
                        },
                        modifier = Modifier.size(34.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipPrevious,
                            contentDescription = "上一首",
                            tint = ivoryInk.copy(alpha = 0.86f),
                            modifier = Modifier.size(21.dp),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
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
                            modifier = Modifier.size(22.dp),
                        )
                    }

                    IconButton(
                        onClick = {
                            if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSkipNext()
                        },
                        modifier = Modifier.size(34.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = "下一首",
                            tint = ivoryInk.copy(alpha = 0.86f),
                            modifier = Modifier.size(21.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            ThreeSlotAutoFitLyricsStage(
                lyrics = lyrics,
                activeIdx = activeIdx,
                lyricFamily = lyricFamily,
                lyricStyleItalic = lyricStyleItalic,
                activeColor = ivoryInk,
                secondaryColor = mutedInk,
                mutedColor = mutedInk.copy(alpha = 0.62f),
                horizontalAlignment = Alignment.Start,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            )

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
                    spectrumStyle = spectrumStyle,
                    playedColor = ivoryInk,
                    unplayedColor = ivoryInk.copy(alpha = 0.24f),
                    needleColor = brassNeedle,
                    timestampColor = mutedInk,
                    onSeekTo = onSeekTo,
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

/**
 * Fluid 60fps Live Audio Spectrum + Draggable Waveform Progress Scrubber.
 * Runs a silky frame-synced per-bar spring/exponential animator over real-time PCM spectrum energy
 * with graceful gravity decay so every beat feels lush and alive even on quiet ambient music.
 */
@Composable
fun LiveSpectrumWaveformSeekBar(
    songId: Long,
    positionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    spectrumBands: FloatArray,
    spectrumStyle: SpectrumStyle = SpectrumStyle.SILK_RIBBON,
    playedColor: Color,
    unplayedColor: Color,
    needleColor: Color,
    timestampColor: Color,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val barCount = 54
    val beatEnergy by AudioSpectrumBus.beatEnergy.collectAsState()
    val restingContour = remember(songId) {
        FloatArray(barCount) { idx ->
            val t = idx.toFloat() / (barCount - 1)
            val arch = 0.22f + 0.22f * sin(t * Math.PI).toFloat()
            val detail = 0.08f * sin(idx * 0.52f + (songId % 9)).toFloat()
            (arch + detail).coerceIn(0.14f, 0.48f)
        }
    }

    val animatedLevels = remember { FloatArray(barCount) { 0.16f } }
    val peakHoldLevels = remember { FloatArray(barCount) { 0.16f } }
    var frameTick by remember { mutableIntStateOf(0) }
    var wavePhase by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isPlaying) {
        var lastNs = 0L
        while (isActive) {
            withFrameNanos { nowNs ->
                val dt = if (lastNs == 0L) 0.016f else ((nowNs - lastNs) / 1_000_000_000f).coerceIn(0.004f, 0.05f)
                lastNs = nowNs

                // Always read LIVE StateFlow values directly on every 60fps frame (zero stale closure!)
                val liveBands = AudioSpectrumBus.bands.value
                val liveBeat = AudioSpectrumBus.beatEnergy.value
                if (isPlaying) wavePhase += dt * (5.5f + liveBeat * 6.5f)

                val bandCount = liveBands.size.coerceAtLeast(1)
                var energySum = 0f
                for (b in liveBands) energySum += b
                val hasLiveFft = energySum > 0.02f

                var changed = false
                for (i in 0 until barCount) {
                    val target = if (isPlaying) {
                        if (hasLiveFft) {
                            // Direct real-time FFT frequency mapping (Left: Bass -> Mid: Vocals -> Right: Treble)
                            val fPos = (i.toFloat() / (barCount - 1)) * (bandCount - 1)
                            val lo = fPos.toInt().coerceIn(0, bandCount - 1)
                            val hi = (lo + 1).coerceIn(0, bandCount - 1)
                            val frac = fPos - lo
                            val fftVal = liveBands[lo] * (1f - frac) + liveBands[hi] * frac
                            (0.07f + fftVal * 0.90f).coerceIn(0.07f, 0.97f)
                        } else {
                            val t = i.toFloat() / (barCount - 1)
                            val env = 0.45f + 0.55f * sin(t * Math.PI).toFloat()
                            val wave = 0.5f + 0.5f * sin(wavePhase - i * 0.28f)
                            (0.14f + wave * 0.62f * env).coerceIn(0.14f, 0.85f)
                        }
                    } else {
                        restingContour[i] * 0.45f
                    }

                    val cur = animatedLevels[i]
                    val speed = if (target > cur) 38f else 12.5f
                    val next = cur + (target - cur) * (dt * speed).coerceAtMost(1f)
                    if (abs(next - cur) > 0.001f) changed = true
                    animatedLevels[i] = next

                    val prevPeak = peakHoldLevels[i]
                    peakHoldLevels[i] = if (next >= prevPeak) {
                        next
                    } else {
                        (prevPeak - dt * 0.62f).coerceAtLeast(next)
                    }
                }
                if (changed || isPlaying) frameTick++
            }
            if (!isPlaying) {
                kotlinx.coroutines.delay(16L)
            }
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
                .height(68.dp)
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
                    .height(62.dp),
            ) {
                val _tick = frameTick
                val totalW = size.width
                val totalH = size.height
                val playheadX = (totalW * progress).coerceIn(2f, totalW - 2f)

                when (spectrumStyle) {
                    SpectrumStyle.SILK_RIBBON -> {
                        val step = totalW / barCount
                        val barW = (step * 0.46f).coerceIn(2.2f, 4.2f)
                        val baseY = totalH - 2f
                        val crestPath = Path()
                        var prevX = 0f
                        var prevY = baseY

                        for (i in 0 until barCount) {
                            val cx = i * step + step / 2f
                            val isPlayed = cx <= playheadX
                            val rawLevel = animatedLevels[i]
                            val level = if (isPlayed) rawLevel else (rawLevel * 0.82f).coerceIn(0.06f, 0.86f)
                            val barH = ((totalH - 6f) * level).coerceAtLeast(4f)
                            val topY = baseY - barH

                            // Vertical bottom-to-top mist fade inside each slender bar
                            val barBrush = Brush.verticalGradient(
                                0.0f to (if (isPlayed) playedColor else unplayedColor),
                                0.72f to (if (isPlayed) playedColor.copy(alpha = 0.72f) else unplayedColor.copy(alpha = 0.55f)),
                                1.0f to Color.Transparent,
                                startY = topY,
                                endY = baseY,
                            )
                            drawRoundRect(
                                brush = barBrush,
                                topLeft = Offset(cx - barW / 2f, topY),
                                size = Size(barW, barH),
                                cornerRadius = CornerRadius(barW / 2f, barW / 2f),
                            )

                            val crestY = (topY - 2f).coerceAtLeast(2f)
                            if (i == 0) {
                                crestPath.moveTo(cx, crestY)
                            } else {
                                crestPath.quadraticTo(prevX, prevY, (prevX + cx) / 2f, (prevY + crestY) / 2f)
                            }
                            prevX = cx
                            prevY = crestY
                        }
                        crestPath.lineTo(prevX, prevY)

                        clipRect(left = 0f, top = 0f, right = playheadX, bottom = totalH) {
                            drawPath(crestPath, color = playedColor.copy(alpha = 0.22f), style = Stroke(width = 5.5f, cap = StrokeCap.Round))
                            drawPath(crestPath, color = playedColor.copy(alpha = 0.90f), style = Stroke(width = 2.0f, cap = StrokeCap.Round))
                        }
                        clipRect(left = playheadX, top = 0f, right = totalW, bottom = totalH) {
                            drawPath(crestPath, color = unplayedColor.copy(alpha = 0.80f), style = Stroke(width = 1.6f, cap = StrokeCap.Round))
                        }

                        // Clean brightness split marks progress without vertical stick
                    }

                    SpectrumStyle.HARMONIC_STRINGS -> {
                        val midY = totalH / 2f
                        val pts = 72
                        // Center reference hairline
                        drawLine(
                            color = unplayedColor.copy(alpha = 0.35f),
                            start = Offset(0f, midY),
                            end = Offset(totalW, midY),
                            strokeWidth = 0.8f,
                        )

                        val bassEnergy = animatedLevels[barCount / 2]
                        val midEnergy = animatedLevels[barCount / 3]
                        val highEnergy = animatedLevels[(barCount * 3) / 4]
                        val maxAmp = totalH * 0.47f

                        var primaryAtPlayheadY = midY
                        val stringSpecs = listOf(
                            Triple(0.0f, 2.2f, playedColor),
                            Triple(1.35f, 1.5f, Color(0xFFD8B888)),
                            Triple(2.6f, 1.2f, needleColor.copy(alpha = 0.85f)),
                        )
                        val amps = listOf(
                            (0.30f + bassEnergy * 0.88f).coerceAtMost(1f) * maxAmp,
                            (0.24f + midEnergy * 0.78f).coerceAtMost(1f) * maxAmp,
                            (0.18f + highEnergy * 0.66f).coerceAtMost(1f) * maxAmp,
                        )
                        val freqs = listOf(2.4f, 3.2f, 4.1f)

                        for (sIdx in 0..2) {
                            val path = Path()
                            val phaseShift = stringSpecs[sIdx].first
                            val strokeW = stringSpecs[sIdx].second
                            val col = stringSpecs[sIdx].third
                            val amp = if (isPlaying) amps[sIdx] else maxAmp * (0.16f - sIdx * 0.04f)
                            val freq = freqs[sIdx]

                            for (p in 0..pts) {
                                val t = p.toFloat() / pts
                                val x = t * totalW
                                // Pin both ends cleanly to 0 amplitude like real acoustic strings
                                val pinWindow = sin(t * Math.PI).toFloat()
                                val y = midY + sin(t * freq * Math.PI.toFloat() * 2f + wavePhase * (1f + sIdx * 0.25f) + phaseShift) * amp * pinWindow
                                if (p == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                if (sIdx == 0 && abs(x - playheadX) <= (totalW / pts)) {
                                    primaryAtPlayheadY = y
                                }
                            }
                            clipRect(left = 0f, top = 0f, right = playheadX, bottom = totalH) {
                                if (sIdx == 0) {
                                    drawPath(path, color = col.copy(alpha = 0.22f), style = Stroke(width = 6f, cap = StrokeCap.Round))
                                }
                                drawPath(path, color = col.copy(alpha = if (sIdx == 0) 0.95f else 0.72f), style = Stroke(width = strokeW, cap = StrokeCap.Round))
                            }
                            clipRect(left = playheadX, top = 0f, right = totalW, bottom = totalH) {
                                drawPath(path, color = col.copy(alpha = if (sIdx == 0) 0.30f else 0.20f), style = Stroke(width = strokeW * 0.85f, cap = StrokeCap.Round))
                            }
                        }

                        // Clean glowing intersection bead on primary string (no vertical line)
                        drawCircle(color = playedColor.copy(alpha = 0.28f), radius = 7.5f, center = Offset(playheadX, primaryAtPlayheadY))
                        drawCircle(color = playedColor, radius = 3.4f, center = Offset(playheadX, primaryAtPlayheadY))
                    }

                    SpectrumStyle.DOT_MATRIX -> {
                        val cols = 34
                        val rows = 8
                        val colStep = totalW / cols
                        val matrixBottom = totalH - 7f
                        val rowStep = (matrixBottom - 3f) / rows
                        val dotRadius = (minOf(colStep, rowStep) * 0.31f).coerceIn(1.6f, 3.0f)

                        for (c in 0 until cols) {
                            val cx = c * colStep + colStep / 2f
                            val isPlayed = cx <= playheadX
                            val mappedIdx = (c * barCount) / cols
                            val level = if (isPlayed) animatedLevels[mappedIdx] else (animatedLevels[mappedIdx] * 0.82f).coerceIn(0.06f, 0.86f)
                            val litRows = (level * rows).roundToInt().coerceIn(1, rows)
                            val peakRow = (peakHoldLevels[mappedIdx] * rows).roundToInt().coerceIn(litRows, rows)

                            for (r in 0 until rows) {
                                val cy = matrixBottom - r * rowStep - rowStep / 2f
                                val rowFade = 0.58f + (r.toFloat() / rows) * 0.42f
                                val isLit = r < litRows
                                val color = when {
                                    isLit && isPlayed -> playedColor.copy(alpha = rowFade)
                                    isLit -> unplayedColor.copy(alpha = rowFade * 0.85f)
                                    else -> unplayedColor.copy(alpha = 0.14f)
                                }
                                drawCircle(color = color, radius = dotRadius, center = Offset(cx, cy))
                            }

                            // Floating gravity-falling cinnabar peak-hold jewel dot on played columns
                            if (isPlayed && peakRow in 1..rows) {
                                val py = matrixBottom - (peakRow - 1) * rowStep - rowStep / 2f
                                drawCircle(color = needleColor.copy(alpha = 0.30f), radius = dotRadius * 2.1f, center = Offset(cx, py))
                                drawCircle(color = needleColor, radius = dotRadius * 1.05f, center = Offset(cx, py))
                            }
                        }

                        // Ultra-fine vintage hardware hairline ticks along bottom baseline
                        for (tIdx in 0..40) {
                            val tx = (tIdx / 40f) * totalW
                            val tickH = if (tIdx % 5 == 0) 4.5f else 2.2f
                            drawLine(
                                color = unplayedColor.copy(alpha = 0.45f),
                                start = Offset(tx, totalH - tickH),
                                end = Offset(tx, totalH),
                                strokeWidth = 0.9f,
                            )
                        }

                        // Clean played/unplayed dot contrast marks progress without vertical line
                    }

                    SpectrumStyle.INK_MOUNTAIN -> {
                        val step = totalW / (barCount - 1)
                        val baseY = totalH - 2f
                        val frontRidge = Path()
                        val frontFill = Path()
                        val backRidge = Path()
                        val backFill = Path()

                        var prevFx = 0f
                        var prevFy = baseY
                        var prevBx = 0f
                        var prevBy = baseY
                        var playheadRidgeY = baseY * 0.5f

                        for (i in 0 until barCount) {
                            val cx = i * step
                            val level = animatedLevels[i]
                            val fy = (baseY - (totalH - 8f) * level).coerceIn(4f, baseY - 3f)
                            val shiftedIdx = (i + 7) % barCount
                            val backLevel = (animatedLevels[shiftedIdx] * 0.72f + restingContour[i] * 0.25f).coerceIn(0.12f, 0.86f)
                            val by = (baseY - (totalH - 10f) * backLevel).coerceIn(6f, baseY - 4f)

                            if (abs(cx - playheadX) <= step) {
                                playheadRidgeY = fy
                            }

                            if (i == 0) {
                                frontRidge.moveTo(cx, fy)
                                frontFill.moveTo(cx, baseY)
                                frontFill.lineTo(cx, fy)
                                backRidge.moveTo(cx, by)
                                backFill.moveTo(cx, baseY)
                                backFill.lineTo(cx, by)
                            } else {
                                val mx = (prevFx + cx) / 2f
                                frontRidge.quadraticTo(prevFx, prevFy, mx, (prevFy + fy) / 2f)
                                frontFill.quadraticTo(prevFx, prevFy, mx, (prevFy + fy) / 2f)
                                backRidge.quadraticTo(prevBx, prevBy, mx, (prevBy + by) / 2f)
                                backFill.quadraticTo(prevBx, prevBy, mx, (prevBy + by) / 2f)
                            }
                            prevFx = cx; prevFy = fy
                            prevBx = cx; prevBy = by
                        }
                        frontRidge.lineTo(prevFx, prevFy)
                        frontFill.lineTo(prevFx, prevFy)
                        frontFill.lineTo(totalW, baseY)
                        frontFill.close()

                        backRidge.lineTo(prevBx, prevBy)
                        backFill.lineTo(prevBx, prevBy)
                        backFill.lineTo(totalW, baseY)
                        backFill.close()

                        // Distant translucent parallax mountain ridge behind
                        drawPath(
                            path = backFill,
                            brush = Brush.verticalGradient(
                                0.0f to unplayedColor.copy(alpha = 0.24f),
                                1.0f to Color.Transparent,
                                startY = 4f,
                                endY = baseY,
                            ),
                        )
                        drawPath(backRidge, color = unplayedColor.copy(alpha = 0.35f), style = Stroke(width = 1.1f))

                        // Foreground mountain: misty gradient fill on played left, whisper-thin contour on unplayed right
                        clipRect(left = 0f, top = 0f, right = playheadX, bottom = totalH) {
                            drawPath(
                                path = frontFill,
                                brush = Brush.verticalGradient(
                                    0.0f to playedColor.copy(alpha = 0.56f),
                                    0.78f to playedColor.copy(alpha = 0.14f),
                                    1.0f to Color.Transparent,
                                    startY = 2f,
                                    endY = baseY,
                                ),
                            )
                            drawPath(frontRidge, color = playedColor.copy(alpha = 0.92f), style = Stroke(width = 2.0f, cap = StrokeCap.Round))
                        }
                        clipRect(left = playheadX, top = 0f, right = totalW, bottom = totalH) {
                            drawPath(frontRidge, color = unplayedColor.copy(alpha = 0.72f), style = Stroke(width = 1.3f, cap = StrokeCap.Round))
                        }

                        // Glowing antique-brass diamond marker riding smoothly on the ridge crest
                        val d = if (isDragging) 6.5f else 5.0f
                        val diamond = Path().apply {
                            moveTo(playheadX, playheadRidgeY - d)
                            lineTo(playheadX + d, playheadRidgeY)
                            lineTo(playheadX, playheadRidgeY + d)
                            lineTo(playheadX - d, playheadRidgeY)
                            close()
                        }
                        drawCircle(color = needleColor.copy(alpha = 0.30f), radius = d * 1.9f, center = Offset(playheadX, playheadRidgeY))
                        drawPath(path = diamond, color = needleColor)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

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
    isDarkSurface: Boolean = true,
    isTranslatingLyrics: Boolean,
    onSelectStyle: (ImmersionLyricStyle) -> Unit,
    currentSpectrumStyle: SpectrumStyle = SpectrumStyle.SILK_RIBBON,
    onSelectSpectrumStyle: (SpectrumStyle) -> Unit = {},
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
                style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.sp),
                color = inkColor.copy(alpha = 0.74f),
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
                if (menuExpanded) {
                    ImmersiveControlSheet(
                        isDarkSurface = isDarkSurface,
                        currentStyle = currentStyle,
                        currentSpectrumStyle = currentSpectrumStyle,
                        onSelectStyle = onSelectStyle,
                        onSelectSpectrumStyle = onSelectSpectrumStyle,
                        onToggleOffsetBar = {
                            menuExpanded = false
                            onToggleOffsetBar()
                        },
                        onOpenFullLyricsSheet = {
                            menuExpanded = false
                            onOpenFullLyricsSheet()
                        },
                        onOpenLyricsSearch = {
                            menuExpanded = false
                            onOpenLyricsSearch()
                        },
                        onOpenCoverSearch = {
                            menuExpanded = false
                            onOpenCoverSearch()
                        },
                        onDismiss = { menuExpanded = false },
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

private val inlineBilingualRegex = Regex("""^([A-Za-z0-9\p{Punct}\s'’\-—–]+?)[\s/|]+([\u4e00-\u9fff].+)$""")

/**
 * Splits a LyricLine into (originalText, translationOrNull).
 * Handles both explicit LyricLine.secondaryText AND embedded inline bilingual LRC lines
 * such as "Cause it's the sound of love' 因为这是爱的声音".
 */
private fun splitBilingualLyric(line: LyricLine): Pair<String, String?> {
    val primary = line.primaryText.trim()
    val explicitTrans = line.secondaryText?.trim()?.takeIf { it.isNotBlank() }
    if (explicitTrans != null) return primary to explicitTrans

    val match = inlineBilingualRegex.matchEntire(primary)
    if (match != null) {
        val orig = match.groupValues[1].trim()
        val trans = match.groupValues[2].trim()
        val latinLetters = orig.count { it.isLetter() }
        val hanChars = trans.count { it in '\u4e00'..'\u9fff' }
        if (latinLetters >= 3 && hanChars >= 2) {
            return orig to trans
        }
    }
    return primary to null
}

/**
 * Fixed 3-Slot Auto-Fit Bilingual Lyrics Stage:
 * - No translation: Line 1 (prev lyric, fixed 13sp) | Line 2 (active lyric, auto-scaled in fixed box so 100% complete) | Line 3 (next lyric, fixed 13sp).
 * - With translation: Line 1 (prev lyric, fixed 13sp) | Line 2 (active original lyric, auto-scaled in fixed box so 100% complete) | Line 3 (active translation, fixed 13sp).
 */
@Composable
private fun ThreeSlotAutoFitLyricsStage(
    lyrics: List<LyricLine>,
    activeIdx: Int,
    lyricFamily: FontFamily,
    lyricStyleItalic: FontStyle,
    activeColor: Color,
    secondaryColor: Color,
    mutedColor: Color,
    horizontalAlignment: Alignment.Horizontal,
    textAlign: TextAlign,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val activeWeight = FontWeight((LocalAppFontWeightValue.current + 150).coerceIn(400, 900))

    Box(
        modifier = modifier,
        contentAlignment = if (horizontalAlignment == Alignment.CenterHorizontally) {
            Alignment.Center
        } else {
            Alignment.CenterStart
        },
    ) {
        if (lyrics.isEmpty() || activeIdx < 0) {
            Text(
                text = "暂无歌词 · 点击右上角匹配在线歌词",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = lyricFamily,
                    fontSize = 14.sp,
                ),
                color = secondaryColor,
                textAlign = textAlign,
            )
            return@Box
        }

        val prevPair = lyrics.getOrNull(activeIdx - 1)?.let { splitBilingualLyric(it) }
        val curPair = splitBilingualLyric(lyrics[activeIdx])
        val nextPair = lyrics.getOrNull(activeIdx + 1)?.let { splitBilingualLyric(it) }

        val line1Text = prevPair?.first.orEmpty()
        val line2Text = curPair.first
        val hasTranslation = !curPair.second.isNullOrBlank()
        val line3Text = if (hasTranslation) curPair.second.orEmpty() else nextPair?.first.orEmpty()

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = horizontalAlignment,
            verticalArrangement = Arrangement.Center,
        ) {
            // Slot 1: Fixed 13sp previous lyric
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp),
                contentAlignment = if (horizontalAlignment == Alignment.CenterHorizontally) {
                    Alignment.Center
                } else {
                    Alignment.CenterStart
                },
            ) {
                if (line1Text.isNotBlank()) {
                    Text(
                        text = line1Text,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = lyricFamily,
                            fontStyle = lyricStyleItalic,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                        ),
                        color = mutedColor,
                        textAlign = textAlign,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Slot 2: Fixed-height stage with automatic font sizing (24sp -> 11sp) so 100% of active lyric fits with ZERO ellipsis
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(74.dp),
                contentAlignment = if (horizontalAlignment == Alignment.CenterHorizontally) {
                    Alignment.Center
                } else {
                    Alignment.CenterStart
                },
            ) {
                val maxWpx = constraints.maxWidth.coerceAtLeast(1)
                val maxHpx = constraints.maxHeight.coerceAtLeast(1)
                val fittedFontSp = remember(line2Text, maxWpx, maxHpx, lyricFamily, lyricStyleItalic) {
                    var chosen = 12
                    for (candidate in 24 downTo 11) {
                        val result = textMeasurer.measure(
                            text = line2Text,
                            style = TextStyle(
                                fontFamily = lyricFamily,
                                fontStyle = lyricStyleItalic,
                                fontSize = candidate.sp,
                                fontWeight = activeWeight,
                                lineHeight = (candidate * 1.32f).sp,
                                textAlign = textAlign,
                            ),
                            constraints = Constraints(maxWidth = maxWpx),
                        )
                        if (result.size.height <= maxHpx && !result.didOverflowWidth) {
                            chosen = candidate
                            break
                        }
                    }
                    chosen
                }

                Text(
                    text = line2Text,
                    style = TextStyle(
                        fontFamily = lyricFamily,
                        fontStyle = lyricStyleItalic,
                        fontSize = fittedFontSp.sp,
                        fontWeight = activeWeight,
                        lineHeight = (fittedFontSp * 1.32f).sp,
                        textAlign = textAlign,
                    ),
                    color = activeColor,
                    textAlign = textAlign,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Slot 3: Fixed 13sp translation (when bilingual) or next lyric (when monolingual)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                contentAlignment = if (horizontalAlignment == Alignment.CenterHorizontally) {
                    Alignment.TopCenter
                } else {
                    Alignment.TopStart
                },
            ) {
                if (line3Text.isNotBlank()) {
                    Text(
                        text = line3Text,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = lyricFamily,
                            fontStyle = if (hasTranslation) FontStyle.Normal else lyricStyleItalic,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                        ),
                        color = if (hasTranslation) secondaryColor else mutedColor,
                        textAlign = textAlign,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun SongInfoSpecHeader(
    title: String,
    artist: String,
    audioSpec: AudioTechSpec,
    titleColor: Color,
    artistColor: Color,
    accentColor: Color,
    centered: Boolean,
    modifier: Modifier = Modifier,
) {
    val userWeight = LocalAppFontWeightValue.current
    val titleWeight = FontWeight((userWeight + 250).coerceIn(500, 900))
    val align = if (centered) Alignment.CenterHorizontally else Alignment.Start
    val textAlign = if (centered) TextAlign.Center else TextAlign.Start

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = align,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = if (centered) 22.sp else 19.sp,
                fontWeight = titleWeight,
                letterSpacing = (-0.3).sp,
            ),
            color = titleColor,
            textAlign = textAlign,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = artist,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            color = artistColor,
            textAlign = textAlign,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        val specText = audioSpec.displayText
        if (audioSpec.isHiRes || specText.isNotBlank()) {
            Spacer(modifier = Modifier.height(7.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (audioSpec.isHiRes) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .border(0.8.dp, accentColor.copy(alpha = 0.65f), RoundedCornerShape(6.dp))
                            .background(accentColor.copy(alpha = 0.10f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = "Hi-Res",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = accentColor,
                        )
                    }
                }
                if (specText.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .border(0.7.dp, artistColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = specText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                letterSpacing = 0.2.sp,
                            ),
                            color = artistColor.copy(alpha = 0.90f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ImmersiveControlSheet(
    isDarkSurface: Boolean,
    currentStyle: ImmersionLyricStyle,
    currentSpectrumStyle: SpectrumStyle,
    onSelectStyle: (ImmersionLyricStyle) -> Unit,
    onSelectSpectrumStyle: (SpectrumStyle) -> Unit,
    onToggleOffsetBar: () -> Unit,
    onOpenFullLyricsSheet: () -> Unit,
    onOpenLyricsSearch: () -> Unit,
    onOpenCoverSearch: () -> Unit,
    onDismiss: () -> Unit,
) {
    val menuBg = if (isDarkSurface) Color(0xFF1D1C1A).copy(alpha = 0.96f) else Color(0xFFFAF6EE).copy(alpha = 0.98f)
    val pillBg = if (isDarkSurface) Color.White.copy(alpha = 0.07f) else Color(0xFFEFE9DC)
    val borderColor = if (isDarkSurface) Color.White.copy(alpha = 0.14f) else Color(0xFFDAD2C3)
    val ink = if (isDarkSurface) Color(0xFFF2ECE1) else Color(0xFF1E1B18)
    val muted = if (isDarkSurface) Color(0xFF9C948A) else Color(0xFF6B655D)
    val accent = if (isDarkSurface) Color(0xFFE0C08E) else Color(0xFFC83E2B)

    DropdownMenu(
        expanded = true,
        onDismissRequest = onDismiss,
        containerColor = menuBg,
        tonalElevation = 0.dp,
        shadowElevation = 12.dp,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.75.dp, borderColor),
    ) {
        Column(
            modifier = Modifier
                .width(236.dp)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Section 1: 沉浸场景 (compact 2x2 grid — keeps bottom lyrics & spectrum 100% visible!)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "沉浸场景",
                    style = MaterialTheme.typography.labelSmall,
                    color = muted,
                )
                ImmersionLyricStyle.entries.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        rowItems.forEach { option ->
                            val selected = option == currentStyle
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) accent.copy(alpha = 0.18f) else pillBg)
                                    .border(
                                        width = if (selected) 0.9.dp else 0.5.dp,
                                        color = if (selected) accent else borderColor.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(8.dp),
                                    )
                                    .clickable { onSelectStyle(option) }
                                    .padding(vertical = 7.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = option.label,
                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.5.sp),
                                    color = if (selected) accent else ink,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }

            // Section 2: 实时频谱 (compact 2x2 grid — live preview at bottom completely unobstructed!)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "实时频谱",
                    style = MaterialTheme.typography.labelSmall,
                    color = muted,
                )
                SpectrumStyle.entries.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        rowItems.forEach { spOpt ->
                            val selected = spOpt == currentSpectrumStyle
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) accent.copy(alpha = 0.18f) else pillBg)
                                    .border(
                                        width = if (selected) 0.9.dp else 0.5.dp,
                                        color = if (selected) accent else borderColor.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(8.dp),
                                    )
                                    .clickable { onSelectSpectrumStyle(spOpt) }
                                    .padding(vertical = 7.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = spOpt.label,
                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.5.sp),
                                    color = if (selected) accent else ink,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(thickness = 0.5.dp, color = borderColor.copy(alpha = 0.6f))

            // Section 3: Compact 2x2 quick tools
            val tools = listOf(
                "时间微调" to onToggleOffsetBar,
                "完整歌词" to onOpenFullLyricsSheet,
                "匹配歌词" to onOpenLyricsSearch,
                "替换封面" to onOpenCoverSearch,
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                tools.chunked(2).forEach { rowTools ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        rowTools.forEach { (label, action) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(pillBg)
                                    .clickable(onClick = action)
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.5.sp),
                                    color = ink.copy(alpha = 0.88f),
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
