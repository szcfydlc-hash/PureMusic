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
import androidx.compose.material3.Switch
import androidx.compose.material3.Slider
import androidx.compose.material.icons.rounded.Tune
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.Bitmap
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
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
import com.puremusic.player.data.model.KaraokeLyricMode
import com.puremusic.player.data.model.LyricWord
import com.puremusic.player.data.model.SpectrumStyle
import com.puremusic.player.data.model.LyricLine
import com.puremusic.player.data.model.Song
import com.puremusic.player.service.AudioSpectrumBus
import com.puremusic.player.ui.components.AlbumArtwork
import com.puremusic.player.ui.components.formatDuration
import com.puremusic.player.ui.theme.LocalAppFontWeightValue
import com.puremusic.player.ui.theme.LocalKaraokeLyricMode
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
    dynamicGlow: Color = dynamicAccent,
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
                    dynamicAccent = dynamicAccent,
                    dynamicGlow = dynamicGlow,
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
                    dynamicGlow = dynamicGlow,
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
                    dynamicGlow = dynamicGlow,
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
    dynamicAccent: Color = Color(0xFF7B8E9B),
    dynamicGlow: Color = Color(0xFF93A6B2),
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
    val studyTuner = LocalImmersiveTuner.current
    val studyBgTop by animateColorAsState(
        targetValue = lerp(Color(0xFF141618), dynamicAccent, 0.56f * studyTuner.moodStrength),
        animationSpec = tween(durationMillis = 420),
        label = "studyBgTop",
    )
    val studyBgMid by animateColorAsState(
        targetValue = lerp(Color(0xFF0E1012), dynamicAccent, 0.36f * studyTuner.moodStrength),
        animationSpec = tween(durationMillis = 420),
        label = "studyBgMid",
    )
    val studyBgBottom by animateColorAsState(
        targetValue = lerp(Color(0xFF090A0C), dynamicAccent, 0.18f * studyTuner.moodStrength),
        animationSpec = tween(durationMillis = 420),
        label = "studyBgBottom",
    )
    val inkPrimary = if (isDarkTheme) Color(0xFFF4F7F9) else Color(0xFF1E1B18)
    val inkSecondary = if (isDarkTheme) lerp(Color(0xFFA3AEB5), dynamicGlow, 0.22f) else Color(0xFF6B655D)
    val inkMuted = if (isDarkTheme) lerp(Color(0xFF646F77), dynamicGlow, 0.18f) else Color(0xFFB5ADA1)
    val cinnabar = if (isDarkTheme) lerp(Color(0xFFAECAD8), dynamicGlow, 0.55f) else Color(0xFFC83E2B)

    val haptic = LocalHapticFeedback.current
    val hapticEnabled = LocalHapticEnabled.current
    val scope = rememberCoroutineScope()
    val dragOffsetX = remember { Animatable(0f) }
    val tapScale = remember { Animatable(1f) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (isDarkTheme) {
                    Modifier.background(
                        Brush.verticalGradient(
                            0.0f to studyBgTop,
                            0.48f to studyBgMid,
                            1.0f to studyBgBottom,
                        ),
                    )
                } else {
                    Modifier.background(Color(0xFFF5F0E6))
                },
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        if (isDarkTheme) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.radialGradient(
                        0.0f to dynamicGlow.copy(alpha = 0.32f * studyTuner.moodStrength),
                        0.46f to dynamicGlow.copy(alpha = 0.14f * studyTuner.moodStrength),
                        1.0f to Color.Transparent,
                        center = Offset(size.width * 0.54f, size.height * 0.26f),
                        radius = size.minDimension * 0.64f,
                    ),
                )
            }
        }
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
                    .height(sleeveSize + 34.dp)
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
                        .height(sleeveSize + 32.dp),
                ) {
                    val reflectionHeight = 30.dp
                    val reflectionAlpha = if (isDarkTheme) 0.30f else 0.14f

                    // 1. Subtle curved tabletop reflection under the peeking black vinyl LP
                    Canvas(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = sleeveSize * 0.50f, y = sleeveSize - 1.dp)
                            .width(discSize)
                            .height(22.dp),
                    ) {
                        drawOval(
                            brush = Brush.radialGradient(
                                0.0f to Color.Black.copy(alpha = if (isDarkTheme) 0.24f else 0.08f),
                                0.55f to Color.Black.copy(alpha = if (isDarkTheme) 0.09f else 0.03f),
                                1.0f to Color.Transparent,
                                center = Offset(size.width * 0.56f, 0f),
                                radius = size.width * 0.46f,
                            ),
                            topLeft = Offset(size.width * 0.12f, -size.height * 0.35f),
                            size = Size(size.width * 0.86f, size.height * 1.15f),
                        )
                    }

                    // 2. Real vertically-mirrored album sleeve reflection with offscreen DstIn fade
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(y = sleeveSize)
                            .width(sleeveSize)
                            .height(reflectionHeight)
                            .clip(RectangleShape)
                            .graphicsLayer {
                                compositingStrategy = CompositingStrategy.Offscreen
                            }
                            .drawWithContent {
                                drawContent()
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        0.00f to Color.Black.copy(alpha = reflectionAlpha),
                                        0.28f to Color.Black.copy(alpha = reflectionAlpha * 0.48f),
                                        0.68f to Color.Black.copy(alpha = reflectionAlpha * 0.12f),
                                        1.00f to Color.Transparent,
                                    ),
                                    blendMode = BlendMode.DstIn,
                                )
                            },
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .size(sleeveSize)
                                .graphicsLayer {
                                    scaleY = -1f
                                },
                        ) {
                            AlbumArtwork(
                                uri = song.albumArtUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                cornerRadius = 0.dp,
                                iconSize = 48.dp,
                            )
                        }
                    }

                    // 3. Crisp razor-thin tabletop contact shadow directly under the sleeve edge
                    Canvas(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(y = sleeveSize - 1.dp)
                            .width(sleeveSize + discSize * 0.26f)
                            .height(7.dp),
                    ) {
                        val sleeveW = sleeveSize.toPx()
                        drawRect(
                            brush = Brush.verticalGradient(
                                0.0f to Color.Black.copy(alpha = if (isDarkTheme) 0.58f else 0.22f),
                                0.45f to Color.Black.copy(alpha = if (isDarkTheme) 0.20f else 0.07f),
                                1.0f to Color.Transparent,
                            ),
                            topLeft = Offset(0f, 0f),
                            size = Size(sleeveW, size.height),
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

            Spacer(modifier = Modifier.height(if (isCompactH) 4.dp else 8.dp))

            SongInfoSpecHeader(
                title = song.title,
                artist = song.artist,
                audioSpec = audioSpec,
                titleColor = inkPrimary,
                artistColor = inkSecondary,
                accentColor = cinnabar,
                centered = true,
            )

            Spacer(modifier = Modifier.height(if (isCompactH) 12.dp else 20.dp))

            ThreeSlotAutoFitLyricsStage(
                lyrics = lyrics,
                activeIdx = activeIdx,
                positionMs = positionMs + lyricOffsetMs,
                isPlaying = isPlaying,
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
                playedColor = if (isDarkTheme) lerp(inkPrimary, dynamicGlow, 0.30f) else inkPrimary,
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
private fun rememberMattePaperGrain(): androidx.compose.ui.graphics.ImageBitmap {
    return remember {
        val w = 96
        val h = 96
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        var seed = 0x5A17C3B1
        val pixels = IntArray(w * h)
        for (i in pixels.indices) {
            seed = seed * 1664525 + 1013904223
            val v = (seed ushr 24) and 0xFF
            // Subtle warm-neutral fine paper grain speckle
            val lum = if (v > 128) 248 else 24
            val alpha = ((v and 0x1F) + 8).coerceIn(6, 36)
            pixels[i] = (alpha shl 24) or (lum shl 16) or (lum shl 8) or lum
        }
        bmp.setPixels(pixels, 0, w, 0, 0, w, h)
        bmp.asImageBitmap()
    }
}

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
    dynamicGlow: Color = dynamicAccent,
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
    val tuner = LocalImmersiveTuner.current
    val bgTop by animateColorAsState(
        targetValue = lerp(Color(0xFF14181C), dynamicAccent, 0.42f * tuner.moodStrength),
        animationSpec = tween(550),
        label = "galleryBgTop",
    )
    val bgMid by animateColorAsState(
        targetValue = lerp(Color(0xFF0D1114), dynamicAccent, 0.24f * tuner.moodStrength),
        animationSpec = tween(550),
        label = "galleryBgMid",
    )
    val bgBottom = Color(0xFF06080A)
    val ivoryInk = Color(0xFFF4F7F9)
    val mutedInk = lerp(Color(0xFF9DA7AE), dynamicGlow, 0.18f)
    val cinnabar = lerp(Color(0xFFAECAD8), dynamicGlow, 0.52f)

    // 5-sleeve architectural corridor (1 commanding hero + 4 mathematically receding sleeves)
    val stackSongs = remember(song.id, allSongs) {
        if (allSongs.isEmpty()) {
            listOf(song)
        } else {
            val idx = allSongs.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
            List(minOf(5, allSongs.size)) { step ->
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

        // Overhead museum gallery spotlight cone + soft cover-tinted studio air
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.radialGradient(
                    0.0f to dynamicGlow.copy(alpha = 0.32f * tuner.moodStrength),
                    0.42f to dynamicAccent.copy(alpha = 0.14f),
                    0.78f to dynamicAccent.copy(alpha = 0.04f),
                    1.0f to Color.Transparent,
                    center = Offset(size.width * 0.40f, size.height * 0.16f),
                    radius = size.maxDimension * 0.58f,
                ),
            )
            // Subtle floor specular horizon sheen directly under the gallery array
            drawRect(
                brush = Brush.radialGradient(
                    0.0f to dynamicGlow.copy(alpha = 0.10f),
                    0.60f to Color.Transparent,
                    center = Offset(size.width * 0.44f, size.height * 0.52f),
                    radius = size.width * 0.65f,
                ),
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
                // Option C: Architectural Collector Hardcover Array — Razor-strict 1-point vanishing perspective
                val totalFanW = frontSize * 1.40f
                val baseX = ((stageW - totalFanW) * 0.50f).coerceAtLeast(14.dp)
                val baseY = 24.dp
                val dynamicRotY = 15f + (dragOffsetX.value * 0.06f).coerceIn(-6f, 6f)
                val camDist = 11.5f * density

                // Decisive monotonic scale steps + rhythmic decaying spacing (+0.135, +0.110, +0.090, +0.070)
                // High cameraDistance eliminates perspective tilt distortion so top/bottom edges stay strictly monotonic!
                val scaleSteps = floatArrayOf(1.00f, 0.87f, 0.76f, 0.67f, 0.59f)
                val rightProtrude = floatArrayOf(0.00f, 0.135f, 0.245f, 0.335f, 0.405f)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset { IntOffset(dragOffsetX.value.roundToInt(), 0) }
                        .graphicsLayer {
                            scaleX = tapScale.value
                            scaleY = tapScale.value
                        },
                ) {
                    val matteGrain = rememberMattePaperGrain()
                    for (depth in (stackSongs.lastIndex downTo 0)) {
                        val itemSong = stackSongs[depth]
                        val isFront = depth == 0
                        val dIdx = depth.coerceAtMost(scaleSteps.lastIndex)
                        val p = scaleSteps[dIdx]
                        val xShift = baseX + frontSize * (1.0f - p + rightProtrude[dIdx])
                        val yShift = baseY
                        val rotY = if (isFront) dynamicRotY else 15f
                        val sleeveAlpha = (1f - depth * 0.09f).coerceAtLeast(0.56f)
                        // Pure architectural album sleeve: NO left metallic spine, NO peeking half-vinyl,
                        // crisp thin white hairline border + subtle satin-matte frosted paper tactile texture
                        Column(
                            modifier = Modifier
                                .offset(x = xShift, y = yShift)
                                .width(frontSize)
                                .graphicsLayer {
                                    transformOrigin = TransformOrigin(0.0f, 0.50f)
                                    scaleX = p
                                    scaleY = p
                                    rotationY = rotY
                                    cameraDistance = camDist
                                    alpha = sleeveAlpha
                                },
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(frontSize)
                                    .height(frontSize),
                            ) {
                                // Clean frameless-spine album sleeve with crisp thin white hairline border
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .shadow(
                                            elevation = if (isFront) 32.dp else 16.dp,
                                            shape = RoundedCornerShape(3.dp),
                                            ambientColor = Color.Black,
                                            spotColor = Color.Black,
                                        )
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color(0xFF141312))
                                        .border(
                                            width = if (isFront) 0.9.dp else 0.7.dp,
                                            color = Color.White.copy(alpha = if (isFront) 0.76f else 0.44f),
                                            shape = RoundedCornerShape(3.dp),
                                        ),
                                ) {
                                    AlbumArtwork(
                                        uri = itemSong.albumArtUri,
                                        contentDescription = itemSong.album,
                                        modifier = Modifier.fillMaxSize(),
                                        cornerRadius = 0.dp,
                                        iconSize = if (isFront) 54.dp else 34.dp,
                                    )

                                    // Delicate satin-matte frosted paper micro-grain texture overlay
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        drawImage(
                                            image = matteGrain,
                                            dstSize = IntSize(size.width.toInt().coerceAtLeast(1), size.height.toInt().coerceAtLeast(1)),
                                            alpha = if (!tuner.matteCover) 0f else if (isFront) 0.52f else 0.34f,
                                        )
                                    }

                                    if (isFront) {
                                        // Soft top-left satin-matte studio sheen + gentle archival edge depth
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.linearGradient(
                                                        0.00f to Color(0xFFFFFDF8).copy(alpha = 0.14f),
                                                        0.28f to Color.White.copy(alpha = 0.045f),
                                                        0.56f to Color.Transparent,
                                                        0.86f to Color.Black.copy(alpha = 0.10f),
                                                        1.00f to Color.Black.copy(alpha = 0.22f),
                                                    ),
                                                ),
                                        )
                                    } else {
                                        // Progressive gallery depth shadow cast from the sleeve ahead
                                        val depthDark = (0.20f + depth * 0.13f).coerceAtMost(0.72f)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.horizontalGradient(
                                                        0.0f to Color.Black.copy(alpha = (depthDark + 0.36f).coerceAtMost(0.88f)),
                                                        0.46f to Color.Black.copy(alpha = depthDark + 0.10f),
                                                        1.0f to Color.Black.copy(alpha = depthDark),
                                                    ),
                                                ),
                                        )
                                    }
                                }
                            }

                            // Razor-thin dark Contact AO line anchoring each sleeve onto obsidian floor
                            Box(
                                modifier = Modifier
                                    .width(frontSize)
                                    .height(3.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            0.0f to Color.Black.copy(alpha = if (isFront) 0.92f else 0.70f),
                                            1.0f to Color.Transparent,
                                        ),
                                    ),
                            )

                            // Crisp bottom-aligned obsidian floor mirror reflection (exact bottom edge mirrored, zero mismatch)
                            if (depth <= 3) {
                                val reflStrength = when (depth) {
                                    0 -> 0.34f
                                    1 -> 0.16f
                                    2 -> 0.08f
                                    else -> 0.04f
                                }
                                Row(
                                    modifier = Modifier
                                        .width(frontSize)
                                        .height(frontSize * 0.28f)
                                        .clip(RectangleShape)
                                        .graphicsLayer {
                                            compositingStrategy = CompositingStrategy.Offscreen
                                        }
                                        .drawWithContent {
                                            drawContent()
                                            drawRect(
                                                brush = Brush.verticalGradient(
                                                    0.00f to Color.Black.copy(alpha = reflStrength),
                                                    0.32f to Color.Black.copy(alpha = reflStrength * 0.42f),
                                                    0.72f to Color.Black.copy(alpha = reflStrength * 0.10f),
                                                    1.00f to Color.Transparent,
                                                ),
                                                blendMode = BlendMode.DstIn,
                                            )
                                        },
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(0.dp)
                                            .fillMaxSize()
                                            .background(
                                                Brush.horizontalGradient(
                                                    0.0f to Color(0xFF4F4E4B),
                                                    0.28f to Color(0xFF8C8A85),
                                                    1.0f to Color(0xFF1F1E1D),
                                                ),
                                            ),
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxSize()
                                            .clip(RectangleShape),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .fillMaxWidth()
                                                .height(frontSize)
                                                .graphicsLayer { scaleY = -1f },
                                        ) {
                                            AlbumArtwork(
                                                uri = itemSong.albumArtUri,
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                cornerRadius = 0.dp,
                                                iconSize = 32.dp,
                                            )
                                        }
                                    }
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
                    positionMs = positionMs + lyricOffsetMs,
                    isPlaying = isPlaying,
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
                    playedColor = lerp(ivoryInk, dynamicGlow, 0.30f),
                    unplayedColor = Color.White.copy(alpha = 0.22f),
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
    dynamicGlow: Color = dynamicAccent,
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
    val lfTuner = LocalImmersiveTuner.current
    val bgTop by animateColorAsState(
        targetValue = lerp(Color(0xFF121110), dynamicAccent, 0.54f * lfTuner.moodStrength),
        animationSpec = tween(420),
        label = "lightfieldBgTop",
    )
    val bgMid by animateColorAsState(
        targetValue = lerp(Color(0xFF0D0C0B), dynamicAccent, 0.34f * lfTuner.moodStrength),
        animationSpec = tween(420),
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

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.radialGradient(
                    0.0f to dynamicGlow.copy(alpha = 0.34f * lfTuner.moodStrength),
                    0.48f to dynamicAccent.copy(alpha = 0.15f * lfTuner.moodStrength),
                    0.85f to Color.Transparent,
                    center = Offset(size.width * 0.32f, size.height * 0.24f),
                    radius = size.maxDimension * 0.58f,
                ),
            )
        }

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
                positionMs = positionMs + lyricOffsetMs,
                isPlaying = isPlaying,
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

                fun sampleSmooth(arr: FloatArray, u: Float): Float {
                    val fIdx = u.coerceIn(0f, 1f) * (arr.size - 1)
                    val i0 = fIdx.toInt().coerceIn(0, arr.size - 1)
                    val i1 = (i0 + 1).coerceAtMost(arr.size - 1)
                    val t = fIdx - i0
                    val smoothT = t * t * (3f - 2f * t)
                    return arr[i0] + (arr[i1] - arr[i0]) * smoothT
                }

                when (spectrumStyle) {
                    SpectrumStyle.SILK_RIBBON -> {
                        // Ultra-dense micro-capsules (76–96 fine bars on phone screens) with pronounced wave peaks
                        val displayBars = (totalW / (4.2f * density)).toInt().coerceIn(72, 96)
                        val step = totalW / displayBars
                        val barW = (step * 0.48f).coerceIn(1.5f * density, 2.5f * density)
                        val baseY = totalH - 2f
                        val crestPath = Path()
                        var prevX = 0f
                        var prevY = baseY

                        for (i in 0 until displayBars) {
                            val u = i.toFloat() / (displayBars - 1).coerceAtLeast(1)
                            val cx = i * step + step / 2f
                            val isPlayed = cx <= playheadX
                            val rawLevel = sampleSmooth(animatedLevels, u)
                            val dynamicLevel = (rawLevel * rawLevel * 0.45f + rawLevel * 0.55f).coerceIn(0.04f, 0.98f)
                            val level = if (isPlayed) dynamicLevel else (dynamicLevel * 0.78f).coerceIn(0.04f, 0.84f)
                            val barH = ((totalH - 5f) * level).coerceAtLeast(3.5f)
                            val topY = baseY - barH

                            val barBrush = Brush.verticalGradient(
                                0.0f to (if (isPlayed) playedColor else unplayedColor),
                                0.72f to (if (isPlayed) playedColor.copy(alpha = 0.68f) else unplayedColor.copy(alpha = 0.48f)),
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

                            val crestY = (topY - 1.8f).coerceAtLeast(2f)
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
                            drawPath(crestPath, color = playedColor.copy(alpha = 0.22f), style = Stroke(width = 4.8f, cap = StrokeCap.Round))
                            drawPath(crestPath, color = playedColor.copy(alpha = 0.92f), style = Stroke(width = 1.85f, cap = StrokeCap.Round))
                        }
                        clipRect(left = playheadX, top = 0f, right = totalW, bottom = totalH) {
                            drawPath(crestPath, color = unplayedColor.copy(alpha = 0.76f), style = Stroke(width = 1.45f, cap = StrokeCap.Round))
                        }
                    }

                    SpectrumStyle.HARMONIC_STRINGS -> {
                        val midY = totalH / 2f
                        val pts = 84
                        // Center reference hairline
                        drawLine(
                            color = unplayedColor.copy(alpha = 0.32f),
                            start = Offset(0f, midY),
                            end = Offset(totalW, midY),
                            strokeWidth = 0.8f,
                        )

                        fun sampleFreqBand(u: Float): Float {
                            val clamped = u.coerceIn(0f, 1f)
                            val fIdx = clamped * (barCount - 1)
                            val i0 = fIdx.toInt().coerceIn(0, barCount - 1)
                            val i1 = (i0 + 1).coerceAtMost(barCount - 1)
                            val frac = fIdx - i0
                            val st = frac * frac * (3f - 2f * frac)
                            return animatedLevels[i0] + (animatedLevels[i1] - animatedLevels[i0]) * st
                        }

                        val maxAmp = totalH * 0.48f
                        var primaryAtPlayheadY = midY
                        // 3 Acoustic Strings mapped to real Bass/Fundamental, Mid/Vocal, and High/Air FFT bands
                        val stringSpecs = listOf(
                            Triple(0.0f, 2.1f, playedColor),
                            Triple(1.45f, 1.4f, Color(0xFFD8B888)),
                            Triple(2.85f, 1.05f, needleColor.copy(alpha = 0.82f)),
                        )

                        for (sIdx in 0..2) {
                            val path = Path()
                            val phaseShift = stringSpecs[sIdx].first
                            val strokeW = stringSpecs[sIdx].second
                            val col = stringSpecs[sIdx].third
                            var prevX = 0f
                            var prevY = midY

                            for (p in 0..pts) {
                                val t = p.toFloat() / pts
                                val x = t * totalW
                                val pinWindow = sin(t * Math.PI).toFloat()
                                val localAmp = if (isPlaying) {
                                    // Map each string to its true real-time FFT spectrum region along x
                                    val bandEnergy = when (sIdx) {
                                        0 -> sampleFreqBand(t * 0.55f)                 // Low-Mid / Bass body
                                        1 -> sampleFreqBand(0.20f + t * 0.55f)         // Mid / Vocal & Piano
                                        else -> sampleFreqBand(0.45f + t * 0.55f)      // High / Hi-hat & Air
                                    }
                                    val contrast = Math.pow(bandEnergy.toDouble(), 1.15).toFloat()
                                    val weight = when (sIdx) { 0 -> 1.00f; 1 -> 0.84f; else -> 0.68f }
                                    (0.05f + contrast * 0.95f) * maxAmp * weight
                                } else {
                                    maxAmp * (0.14f - sIdx * 0.035f)
                                }
                                // True harmonic standing-wave carrier modulated by local real-time frequency energy
                                val carrier = when (sIdx) {
                                    0 -> sin(t * 13.5f + wavePhase * 1.05f + phaseShift)
                                    1 -> sin(t * 19.5f - wavePhase * 1.30f + phaseShift)
                                    else -> sin(t * 27.0f + wavePhase * 1.75f + phaseShift)
                                }
                                val y = midY + carrier * localAmp * pinWindow
                                if (p == 0) {
                                    path.moveTo(x, y)
                                } else {
                                    path.quadraticTo(prevX, prevY, (prevX + x) * 0.5f, (prevY + y) * 0.5f)
                                }
                                prevX = x
                                prevY = y
                                if (sIdx == 0 && abs(x - playheadX) <= (totalW / pts)) {
                                    primaryAtPlayheadY = y
                                }
                            }
                            path.lineTo(prevX, prevY)
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
                        // Ultra-dense dot matrix (54–66 cols x 12 rows) with vivid tall surges & cinnabar peak dots
                        val cols = (totalW / (5.8f * density)).toInt().coerceIn(52, 68)
                        val rows = 12
                        val colStep = totalW / cols
                        val matrixBottom = totalH - 6f
                        val rowStep = (matrixBottom - 2f) / rows
                        val dotRadius = (minOf(colStep, rowStep) * 0.34f).coerceIn(1.3f * density, 2.2f * density)

                        for (c in 0 until cols) {
                            val u = c.toFloat() / (cols - 1).coerceAtLeast(1)
                            val cx = c * colStep + colStep / 2f
                            val isPlayed = cx <= playheadX
                            val rawLevel = sampleSmooth(animatedLevels, u)
                            val boosted = (rawLevel * 1.18f).coerceIn(0.05f, 1.0f)
                            val level = if (isPlayed) boosted else (boosted * 0.78f).coerceIn(0.04f, 0.85f)
                            val litRows = (level * rows).roundToInt().coerceIn(1, rows)
                            val rawPeak = (sampleSmooth(peakHoldLevels, u) * 1.18f).coerceIn(level, 1.0f)
                            val peakRow = (rawPeak * rows).roundToInt().coerceIn(litRows, rows)

                            for (r in 0 until rows) {
                                val cy = matrixBottom - r * rowStep - rowStep / 2f
                                val rowFade = 0.62f + (r.toFloat() / rows) * 0.38f
                                val isLit = r < litRows
                                val color = when {
                                    isLit && isPlayed -> playedColor.copy(alpha = rowFade)
                                    isLit -> unplayedColor.copy(alpha = rowFade * 0.82f)
                                    else -> unplayedColor.copy(alpha = 0.11f)
                                }
                                drawCircle(color = color, radius = dotRadius, center = Offset(cx, cy))
                            }

                            // Vivid floating gravity-falling cinnabar peak-hold jewel dot
                            if (isPlayed && peakRow in 1..rows) {
                                val py = matrixBottom - (peakRow - 1) * rowStep - rowStep / 2f
                                drawCircle(color = needleColor.copy(alpha = 0.28f), radius = dotRadius * 2.1f, center = Offset(cx, py))
                                drawCircle(color = needleColor, radius = dotRadius * 1.12f, center = Offset(cx, py))
                            }
                        }

                        // Ultra-fine vintage hardware hairline ticks along bottom baseline
                        for (tIdx in 0..50) {
                            val tx = (tIdx / 50f) * totalW
                            val tickH = if (tIdx % 5 == 0) 4.0f else 2.0f
                            drawLine(
                                color = unplayedColor.copy(alpha = 0.42f),
                                start = Offset(tx, totalH - tickH),
                                end = Offset(tx, totalH),
                                strokeWidth = 0.85f,
                            )
                        }
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


data class ImmersiveTunerState(
    val moodStrength: Float = 0.85f,
    val matteCover: Boolean = true,
    val vinylRpm: Int = 33,
    val onUpdateMoodStrength: (Float) -> Unit = {},
    val onToggleMatteCover: (Boolean) -> Unit = {},
    val onUpdateVinylRpm: (Int) -> Unit = {},
)


private val CREDIT_META_REGEX = Regex(
    """^(作词|作曲|编曲|制作人|混音|母带|录音|词|曲|发行|演唱|出品|Lyricist|Composer|Producer|Arranged|Mixed|Mastered)\s*[:：]""",
    RegexOption.IGNORE_CASE,
)

private fun isLyricCreditMetadata(line: String): Boolean = CREDIT_META_REGEX.containsMatchIn(line.trim())

val LocalImmersiveTuner = androidx.compose.runtime.compositionLocalOf { ImmersiveTunerState() }

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
    moodStrength: Float = 0.85f,
    matteCover: Boolean = true,
    vinylRpm: Int = 33,
    onUpdateMoodStrength: (Float) -> Unit = {},
    onToggleMatteCover: (Boolean) -> Unit = {},
    onUpdateVinylRpm: (Int) -> Unit = {},
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var tuneExpanded by remember { mutableStateOf(false) }
    val tuner = LocalImmersiveTuner.current

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

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val pillBg = if (isDarkSurface) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f)
            val pillBorder = inkColor.copy(alpha = 0.24f)
            val goldAccent = Color(0xFFE4C68D)

            // 1) '译' unified 30dp capsule
            Box(
                modifier = Modifier
                    .height(30.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(pillBg)
                    .border(0.75.dp, pillBorder, RoundedCornerShape(15.dp))
                    .clickable(enabled = !isTranslatingLyrics, onClick = onTranslateLyrics)
                    .padding(horizontal = 11.dp),
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
                        color = inkColor.copy(alpha = 0.88f),
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            // 2) '微调' unified 30dp circle + Dark Obsidian Champagne-Gold Dropdown (matches Menu style 1:1!)
            Box {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(if (tuneExpanded) goldAccent.copy(alpha = 0.16f) else pillBg)
                        .border(0.75.dp, if (tuneExpanded) goldAccent.copy(alpha = 0.85f) else pillBorder, CircleShape)
                        .clickable { tuneExpanded = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = "氛围与质感调节",
                        tint = if (tuneExpanded) goldAccent else inkColor.copy(alpha = 0.88f),
                        modifier = Modifier.size(15.dp),
                    )
                }

                MaterialTheme(
                    colorScheme = MaterialTheme.colorScheme.copy(
                        surface = Color(0xF0181715),
                        surfaceContainer = Color(0xF0181715),
                    ),
                ) {
                    DropdownMenu(
                        expanded = tuneExpanded,
                        onDismissRequest = { tuneExpanded = false },
                        shape = RoundedCornerShape(16.dp),
                        containerColor = Color(0xF0181715),
                        tonalElevation = 0.dp,
                        shadowElevation = 12.dp,
                        border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.14f)),
                        modifier = Modifier.width(256.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            // Header: 动态取色氛围强度 + %
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "动态取色氛围强度",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFFD6CEC2),
                                )
                                Text(
                                    text = "${(tuner.moodStrength * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = goldAccent,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }

                            // Custom sleek 3dp champagne-gold hairline slider (no chunky red Material Slider!)
                            var sliderW by remember { mutableFloatStateOf(1f) }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp)
                                    .onSizeChanged { sliderW = it.width.toFloat().coerceAtLeast(1f) }
                                    .pointerInput(Unit) {
                                        detectTapGestures { off ->
                                            tuner.onUpdateMoodStrength((off.x / sliderW).coerceIn(0f, 1f))
                                        }
                                    }
                                    .pointerInput(Unit) {
                                        detectHorizontalDragGestures { change, _ ->
                                            change.consume()
                                            tuner.onUpdateMoodStrength((change.position.x / sliderW).coerceIn(0f, 1f))
                                        }
                                    },
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                Canvas(modifier = Modifier.fillMaxWidth().height(16.dp)) {
                                    val cy = size.height / 2f
                                    val frac = tuner.moodStrength.coerceIn(0f, 1f)
                                    val thumbX = (size.width * frac).coerceIn(6.dp.toPx(), size.width - 6.dp.toPx())
                                    drawLine(
                                        color = Color.White.copy(alpha = 0.16f),
                                        start = Offset(0f, cy),
                                        end = Offset(size.width, cy),
                                        strokeWidth = 3.dp.toPx(),
                                        cap = StrokeCap.Round,
                                    )
                                    drawLine(
                                        color = goldAccent,
                                        start = Offset(0f, cy),
                                        end = Offset(thumbX, cy),
                                        strokeWidth = 3.dp.toPx(),
                                        cap = StrokeCap.Round,
                                    )
                                    drawCircle(
                                        color = goldAccent.copy(alpha = 0.28f),
                                        radius = 9.dp.toPx(),
                                        center = Offset(thumbX, cy),
                                    )
                                    drawCircle(
                                        color = goldAccent,
                                        radius = 5.5.dp.toPx(),
                                        center = Offset(thumbX, cy),
                                    )
                                }
                            }

                            // 3 Quick Presets: 极简 0% / 柔和 60% / 浓郁 100%
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                listOf(0.0f to "极简 0%", 0.60f to "柔和 60%", 1.0f to "浓郁 100%").forEach { (presetVal, label) ->
                                    val isCur = abs(tuner.moodStrength - presetVal) < 0.06f
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isCur) goldAccent.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.05f))
                                            .border(
                                                width = if (isCur) 0.9.dp else 0.6.dp,
                                                color = if (isCur) goldAccent else Color.White.copy(alpha = 0.16f),
                                                shape = RoundedCornerShape(10.dp),
                                            )
                                            .clickable { tuner.onUpdateMoodStrength(presetVal) }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            color = if (isCur) goldAccent else Color(0xFFAFA79B),
                                            fontWeight = if (isCur) FontWeight.SemiBold else FontWeight.Normal,
                                            maxLines = 1,
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(thickness = 0.5.dp, color = Color.White.copy(alpha = 0.10f))

                            // Matte Frosted Cover toggle (slim champagne-gold pill switch)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "封面亚光磨砂质感",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFF2ECE1),
                                )
                                Box(
                                    modifier = Modifier
                                        .width(38.dp)
                                        .height(20.dp)
                                        .clip(CircleShape)
                                        .background(if (tuner.matteCover) goldAccent else Color.White.copy(alpha = 0.18f))
                                        .clickable { tuner.onToggleMatteCover(!tuner.matteCover) }
                                        .padding(2.dp),
                                    contentAlignment = if (tuner.matteCover) Alignment.CenterEnd else Alignment.CenterStart,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF181715)),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3) '···' unified 30dp circle
            Box {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(if (menuExpanded) goldAccent.copy(alpha = 0.16f) else pillBg)
                        .border(0.75.dp, if (menuExpanded) goldAccent.copy(alpha = 0.85f) else pillBorder, CircleShape)
                        .clickable { menuExpanded = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreHoriz,
                        contentDescription = "切换沉浸场景与歌词设置",
                        tint = if (menuExpanded) goldAccent else inkColor.copy(alpha = 0.88f),
                        modifier = Modifier.size(16.dp),
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
private fun rememberSmoothLyricPosMs(
    anchorPosMs: Long,
    isPlaying: Boolean,
): Long {
    var smoothMs by remember { mutableLongStateOf(anchorPosMs) }
    val latestAnchor by rememberUpdatedState(anchorPosMs)
    val latestAnchorRealtime = remember { mutableLongStateOf(android.os.SystemClock.elapsedRealtime()) }

    LaunchedEffect(anchorPosMs) {
        val now = android.os.SystemClock.elapsedRealtime()
        latestAnchorRealtime.longValue = now
        // Snap immediately on seek/track switch (> 550ms jump), otherwise let phase-locked loop slew smoothly
        if (abs(anchorPosMs - smoothMs) > 550L || !isPlaying) {
            smoothMs = anchorPosMs
        }
    }

    LaunchedEffect(isPlaying) {
        if (!isPlaying) {
            smoothMs = latestAnchor
            return@LaunchedEffect
        }
        var lastNanos = 0L
        while (true) {
            withFrameNanos { nanos ->
                val dtMs = if (lastNanos == 0L) 16f else ((nanos - lastNanos) / 1_000_000f).coerceIn(0f, 50f)
                lastNanos = nanos
                val expected = latestAnchor + (android.os.SystemClock.elapsedRealtime() - latestAnchorRealtime.longValue).coerceIn(0L, 1500L)
                val drift = expected - smoothMs
                smoothMs = when {
                    abs(drift) > 550L -> expected
                    drift > 0L -> (smoothMs + dtMs + drift * 0.12f).toLong()
                    else -> (smoothMs + dtMs * 0.85f).toLong().coerceAtMost(expected + 40L)
                }
            }
        }
    }
    return smoothMs
}

private fun synthesizeSmartWords(text: String, startMs: Long, endMs: Long): List<LyricWord> {
    if (text.isBlank()) return emptyList()
    val safeDur = ((endMs - startMs) * 0.90f).toLong().coerceIn(800L, 10_000L)
    val tokens = mutableListOf<String>()
    val sb = StringBuilder()
    for (ch in text) {
        val isCjk = ch.code in 0x4E00..0x9FFF || ch.code in 0x3040..0x30FF || ch.code in 0xAC00..0xD7AF
        if (isCjk) {
            if (sb.isNotEmpty()) { tokens.add(sb.toString()); sb.clear() }
            tokens.add(ch.toString())
        } else if (ch == ' ') {
            sb.append(ch)
            tokens.add(sb.toString())
            sb.clear()
        } else {
            sb.append(ch)
        }
    }
    if (sb.isNotEmpty()) tokens.add(sb.toString())
    if (tokens.isEmpty()) return emptyList()

    val weights = tokens.map { tok ->
        val trimmed = tok.trim()
        when {
            trimmed.isEmpty() -> 0.4f
            trimmed.length == 1 && trimmed[0].code >= 0x4E00 -> 1.15f
            else -> (trimmed.length * 0.42f).coerceIn(0.8f, 2.8f)
        }
    }
    val totalWeight = weights.sum().coerceAtLeast(1f)
    var cursor = startMs
    return tokens.mapIndexed { idx, tok ->
        val slice = (safeDur * (weights[idx] / totalWeight)).toLong().coerceAtLeast(50L)
        val wStart = cursor
        val wEnd = (cursor + slice).coerceAtLeast(wStart + 50L)
        cursor = wEnd
        LyricWord(tok, wStart, wEnd)
    }
}

@Composable
private fun ThreeSlotAutoFitLyricsStage(
    lyrics: List<LyricLine>,
    activeIdx: Int,
    positionMs: Long = 0L,
    isPlaying: Boolean = false,
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
        val isCreditLine = isLyricCreditMetadata(line2Text)
        val effectiveActiveColor = if (isCreditLine) secondaryColor.copy(alpha = 0.82f) else activeColor
        val effectiveActiveWeight = if (isCreditLine) FontWeight.Normal else activeWeight
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
                                fontWeight = effectiveActiveWeight,
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

                val karaokeMode = LocalKaraokeLyricMode.current
                val activeLine = lyrics[activeIdx]
                val nextLineStart = lyrics.getOrNull(activeIdx + 1)?.timeMs ?: (activeLine.timeMs + 4200L)
                val enableKaraoke = when (karaokeMode) {
                    KaraokeLyricMode.CLASSIC -> false
                    KaraokeLyricMode.EXACT_ONLY -> activeLine.words.isNotEmpty()
                    KaraokeLyricMode.SMART -> true
                }

                AnimatedContent(
                    targetState = Triple(activeIdx, line2Text, fittedFontSp),
                    transitionSpec = {
                        (slideInVertically(tween(340, easing = FastOutSlowInEasing)) { it / 3 } + fadeIn(tween(300))) togetherWith
                            (slideOutVertically(tween(220, easing = FastOutSlowInEasing)) { -it / 3 } + fadeOut(tween(180)))
                    },
                    label = "lyricLineGlide",
                ) { (targetIdx, targetText, targetSp) ->
                    val targetLine = lyrics.getOrNull(targetIdx) ?: activeLine
                    val targetNextStart = lyrics.getOrNull(targetIdx + 1)?.timeMs ?: (targetLine.timeMs + 4200L)
                    val targetCredit = isLyricCreditMetadata(targetText)
                    val targetFinalSp = if (targetCredit) minOf(targetSp, 14) else targetSp
                    val targetColor = if (targetCredit) secondaryColor.copy(alpha = 0.82f) else activeColor
                    val targetWeight = if (targetCredit) FontWeight.Normal else activeWeight

                    if (!enableKaraoke || targetCredit) {
                        Text(
                            text = targetText,
                            style = TextStyle(
                                fontFamily = lyricFamily,
                                fontStyle = lyricStyleItalic,
                                fontSize = targetFinalSp.sp,
                                fontWeight = targetWeight,
                                lineHeight = (targetFinalSp * 1.32f).sp,
                                textAlign = textAlign,
                            ),
                            color = targetColor,
                            textAlign = textAlign,
                        )
                    } else {
                        val smoothPosMs = rememberSmoothLyricPosMs(positionMs, isPlaying)
                        val words = remember(targetLine.timeMs, targetText, targetLine.words, targetNextStart) {
                            if (targetLine.words.isNotEmpty()) targetLine.words
                            else synthesizeSmartWords(targetText, targetLine.timeMs, targetNextStart)
                        }
                        // Compute exact continuous character-level fractional progress (0f..targetText.length)
                        val exactCharPos = run {
                            if (words.isEmpty()) targetText.length.toFloat()
                            else {
                                var charCursor = 0
                                var result = 0f
                                var found = false
                                for (w in words) {
                                    val len = w.text.length.coerceAtLeast(1)
                                    if (smoothPosMs < w.startMs) {
                                        result = charCursor.toFloat()
                                        found = true
                                        break
                                    } else if (smoothPosMs <= w.endMs) {
                                        val rawT = ((smoothPosMs - w.startMs).toFloat() / (w.endMs - w.startMs).coerceAtLeast(40L).toFloat()).coerceIn(0f, 1f)
                                        // Smooth S-curve easing inside each syllable for organic human vocal pacing
                                        val easedT = rawT * rawT * (3f - 2f * rawT)
                                        result = charCursor + easedT * len
                                        found = true
                                        break
                                    }
                                    charCursor += len
                                }
                                if (found) result else targetText.length.toFloat()
                            }
                        }

                        var layoutResult by remember(targetText, targetFinalSp) { mutableStateOf<TextLayoutResult?>(null) }
                        val unsungColor = targetColor.copy(alpha = 0.28f)
                        val featherPx = 42f

                        Text(
                            text = targetText,
                            style = TextStyle(
                                fontFamily = lyricFamily,
                                fontStyle = lyricStyleItalic,
                                fontSize = targetFinalSp.sp,
                                fontWeight = targetWeight,
                                lineHeight = (targetFinalSp * 1.32f).sp,
                                textAlign = textAlign,
                            ),
                            color = targetColor,
                            textAlign = textAlign,
                            onTextLayout = { layoutResult = it },
                            modifier = Modifier
                                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                                .drawWithContent {
                                    drawContent()
                                    val lr = layoutResult
                                    if (lr != null && targetText.isNotEmpty()) {
                                        val lineCount = lr.lineCount
                                        val maxChar = (targetText.length - 1).coerceAtLeast(0)
                                        for (lineIdx in 0 until lineCount) {
                                            val lineStartChar = lr.getLineStart(lineIdx)
                                            val lineEndChar = lr.getLineEnd(lineIdx, visibleEnd = true).coerceAtLeast(lineStartChar)
                                            val lineTop = lr.getLineTop(lineIdx)
                                            val lineBottom = lr.getLineBottom(lineIdx)
                                            val lineLeft = lr.getLineLeft(lineIdx)
                                            val lineRight = lr.getLineRight(lineIdx)

                                            val sweepX = when {
                                                exactCharPos <= lineStartChar -> lineLeft - featherPx
                                                exactCharPos >= lineEndChar -> lineRight + featherPx
                                                else -> {
                                                    val cIdx = exactCharPos.toInt().coerceIn(lineStartChar, maxChar)
                                                    val frac = exactCharPos - cIdx
                                                    val box = lr.getBoundingBox(cIdx)
                                                    box.left + (box.right - box.left) * frac
                                                }
                                            }
                                            val gradStart = (sweepX - featherPx * 0.55f).coerceAtLeast(0f)
                                            val gradEnd = (sweepX + featherPx * 0.45f).coerceAtLeast(gradStart + 1f)
                                            drawRect(
                                                brush = Brush.horizontalGradient(
                                                    0.0f to targetColor,
                                                    1.0f to unsungColor,
                                                    startX = gradStart,
                                                    endX = gradEnd,
                                                ),
                                                topLeft = Offset(0f, lineTop),
                                                size = Size(size.width, (lineBottom - lineTop).coerceAtLeast(1f)),
                                                blendMode = BlendMode.SrcIn,
                                            )
                                        }
                                    }
                                },
                        )
                    }
                }
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
