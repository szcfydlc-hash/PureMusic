package com.puremusic.player.ui.components

import android.net.Uri
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.puremusic.player.data.model.MiniPlayerStyle
import com.puremusic.player.ui.theme.LocalMiniPlayerHeightDp
import com.puremusic.player.ui.theme.LocalMiniPlayerStyle
import kotlinx.coroutines.isActive
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.puremusic.player.data.model.PlayerUiState
import com.puremusic.player.data.model.Song
import com.puremusic.player.ui.theme.LocalFloatingControls
import com.puremusic.player.ui.theme.LocalLayoutDensity
import com.puremusic.player.ui.theme.LocalLiquidGlassLevel
import java.util.Locale

fun formatDuration(durationMs: Long): String {
    val totalSeconds = (durationMs.coerceAtLeast(0L) / 1000L)
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0L) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}

fun formatCountdown(remainingMs: Long): String {
    val totalSeconds = ((remainingMs + 999L) / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}

@Composable
fun AlbumArtwork(
    uri: Uri?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 5.dp,
    iconSize: Dp = 24.dp,
) {
    val shape = RoundedCornerShape(cornerRadius)
    val context = LocalContext.current
    val request = remember(uri) {
        ImageRequest.Builder(context)
            .data(uri)
            .size(360)
            .crossfade(true)
            .build()
    }
    SubcomposeAsyncImage(
        model = request,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .clip(shape)
            .border(0.6.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.85f), shape),
        loading = { ArtworkPlaceholder(shape = shape, iconSize = iconSize) },
        error = { ArtworkPlaceholder(shape = shape, iconSize = iconSize) },
    )
}

@Composable
private fun ArtworkPlaceholder(
    shape: RoundedCornerShape,
    iconSize: Dp,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.MusicNote,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
            modifier = Modifier.size(iconSize),
        )
    }
}

@Composable
fun SongRowItem(
    song: Song,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShowSongInfo: (() -> Unit)? = null,
    onReplaceCover: (() -> Unit)? = null,
    onMatchLyrics: (() -> Unit)? = null,
    onDeleteSong: (() -> Unit)? = null,
    extraMenuActionLabel: String? = null,
    onExtraMenuAction: (() -> Unit)? = null,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val density = LocalLayoutDensity.current
    val ext = song.filePath.substringAfterLast('.', "").lowercase(Locale.US)
    val isLossless = ext in setOf("flac", "wav", "alac", "ape", "dsd", "aiff")
    val badgeText = if (isLossless) "SQ" else "HQ"
    val badgeColor = if (isLossless) Color(0xFF0D9488) else Color(0xFFD97706)
    val subLine = if (song.album.isNotBlank() && !song.album.equals(song.title, ignoreCase = true) && song.album != "未知专辑") {
        "${song.artist} - ${song.album}"
    } else {
        "${song.artist} · ${formatDuration(song.durationMs)}"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 18.dp, end = 6.dp, top = density.rowVerticalPaddingDp.dp, bottom = density.rowVerticalPaddingDp.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(density.artSizeDp.dp)) {
            AlbumArtwork(
                uri = song.albumArtUri,
                contentDescription = song.album,
                modifier = Modifier.fillMaxSize(),
                cornerRadius = 6.dp,
                iconSize = 19.dp,
            )
            if (isCurrent) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.78f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.GraphicEq else Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(badgeColor.copy(alpha = 0.14f))
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.5.sp,
                            lineHeight = 10.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = badgeColor,
                    )
                }
                Text(
                    text = subLine,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        // Light quick-add '+' icon button (Salt Player style!)
        IconButton(
            onClick = onAddToPlaylist,
            modifier = Modifier.size(34.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "添加到歌单",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                modifier = Modifier.size(18.dp),
            )
        }

        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "更多操作",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                    modifier = Modifier.size(19.dp),
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
                    text = { Text(if (isFavorite) "取消喜欢" else "添加到我喜欢") },
                    leadingIcon = {
                        Icon(
                            imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = null,
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onToggleFavorite()
                    },
                )
                DropdownMenuItem(
                    text = { Text("添加到歌单") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.PlaylistAdd,
                            contentDescription = null,
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onAddToPlaylist()
                    },
                )
                if (onReplaceCover != null) {
                    DropdownMenuItem(
                        text = { Text("更换封面") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Image,
                                contentDescription = null,
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onReplaceCover()
                        },
                    )
                }
                if (onMatchLyrics != null) {
                    DropdownMenuItem(
                        text = { Text("匹配 / 导入歌词") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Lyrics,
                                contentDescription = null,
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onMatchLyrics()
                        },
                    )
                }
                if (onShowSongInfo != null) {
                    DropdownMenuItem(
                        text = { Text("歌曲信息与编辑") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = null,
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onShowSongInfo()
                        },
                    )
                }
                if (extraMenuActionLabel != null && onExtraMenuAction != null) {
                    DropdownMenuItem(
                        text = { Text(extraMenuActionLabel) },
                        onClick = {
                            menuExpanded = false
                            onExtraMenuAction()
                        },
                    )
                }
                if (onDeleteSong != null) {
                    DropdownMenuItem(
                        text = { Text("删除本地文件", color = MaterialTheme.colorScheme.primary) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onDeleteSong()
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun MiniPlayerBar(
    playerState: PlayerUiState,
    liquidGlassEnabled: Boolean,
    onOpenNowPlaying: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val song = playerState.currentSong ?: return
    val isFloating = LocalFloatingControls.current
    val style = LocalMiniPlayerStyle.current
    val barHeightDp = LocalMiniPlayerHeightDp.current.coerceIn(48, 68)
    val artSizeDp = (barHeightDp - 16).coerceIn(32, 50)
    val progress = (playerState.positionMs.toFloat() / playerState.durationMs.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)

    val barShape = when {
        style == MiniPlayerStyle.FLOATING_PILL -> RoundedCornerShape((barHeightDp / 2).dp)
        isFloating -> RoundedCornerShape(28.dp)
        else -> RoundedCornerShape(8.dp)
    }
    val horizontalPad = when {
        style == MiniPlayerStyle.FLOATING_PILL -> 18.dp
        isFloating -> 20.dp
        else -> 12.dp
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val spinAngle = remember { Animatable(0f) }
    LaunchedEffect(playerState.isPlaying, style) {
        if (playerState.isPlaying && style == MiniPlayerStyle.VINYL_DISC) {
            while (isActive) {
                val start = spinAngle.value % 360f
                spinAngle.snapTo(start)
                spinAngle.animateTo(
                    targetValue = start + 360f,
                    animationSpec = tween(durationMillis = 14_000, easing = LinearEasing),
                )
            }
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackBgColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPad, vertical = 5.dp)
            .height(barHeightDp.dp)
            .liquidGlass(
                enabled = liquidGlassEnabled,
                shape = barShape,
                isPressed = isPressed,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onOpenNowPlaying,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AnimatedContent(
                targetState = song,
                transitionSpec = {
                    (fadeIn() + slideInVertically { it / 4 }) togetherWith
                        (fadeOut() + slideOutVertically { -it / 4 })
                },
                label = "miniPlayerTrack",
                modifier = Modifier.weight(1f),
            ) { current ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(11.dp),
                ) {
                    when (style) {
                        MiniPlayerStyle.CLASSIC_BOOK -> {
                            AlbumArtwork(
                                uri = current.albumArtUri,
                                contentDescription = current.album,
                                modifier = Modifier.size(artSizeDp.dp),
                                cornerRadius = 6.dp,
                                iconSize = 18.dp,
                            )
                        }

                        MiniPlayerStyle.VINYL_DISC -> {
                            Box(
                                modifier = Modifier
                                    .size(artSizeDp.dp)
                                    .rotate(spinAngle.value)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0E0E10))
                                    .border(1.dp, Color.White.copy(alpha = 0.22f), CircleShape)
                                    .padding((artSizeDp * 0.23f).dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                AlbumArtwork(
                                    uri = current.albumArtUri,
                                    contentDescription = current.album,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    cornerRadius = 24.dp,
                                    iconSize = 11.dp,
                                )
                            }
                        }

                        MiniPlayerStyle.FLOATING_PILL -> {
                            AlbumArtwork(
                                uri = current.albumArtUri,
                                contentDescription = current.album,
                                modifier = Modifier
                                    .size(artSizeDp.dp)
                                    .clip(CircleShape),
                                cornerRadius = (artSizeDp / 2).dp,
                                iconSize = 16.dp,
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = current.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (barHeightDp >= 52) {
                            Text(
                                text = current.artist,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            when (style) {
                MiniPlayerStyle.CLASSIC_BOOK -> {
                    IconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier.size(34.dp),
                    ) {
                        Icon(
                            imageVector = if (playerState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (playerState.isPlaying) "暂停" else "播放",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(23.dp),
                        )
                    }
                    IconButton(
                        onClick = onSkipNext,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = "下一首",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(21.dp),
                        )
                    }
                }

                MiniPlayerStyle.VINYL_DISC -> {
                    IconButton(
                        onClick = onSkipPrevious,
                        modifier = Modifier.size(30.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipPrevious,
                            contentDescription = "上一首",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    IconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier.size(34.dp),
                    ) {
                        Icon(
                            imageVector = if (playerState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (playerState.isPlaying) "暂停" else "播放",
                            tint = primaryColor,
                            modifier = Modifier.size(23.dp),
                        )
                    }
                    IconButton(
                        onClick = onSkipNext,
                        modifier = Modifier.size(30.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = "下一首",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                MiniPlayerStyle.FLOATING_PILL -> {
                    val ringSize = (barHeightDp - 18).coerceIn(32, 42)
                    Box(
                        modifier = Modifier
                            .size(ringSize.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onTogglePlayPause),
                        contentAlignment = Alignment.Center,
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeW = 2.2.dp.toPx()
                            val inset = strokeW / 2f
                            drawArc(
                                color = trackBgColor,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = Offset(inset, inset),
                                size = Size(size.width - strokeW, size.height - strokeW),
                                style = Stroke(width = strokeW),
                            )
                            drawArc(
                                color = primaryColor,
                                startAngle = -90f,
                                sweepAngle = 360f * progress,
                                useCenter = false,
                                topLeft = Offset(inset, inset),
                                size = Size(size.width - strokeW, size.height - strokeW),
                                style = Stroke(width = strokeW, cap = StrokeCap.Round),
                            )
                        }
                        Icon(
                            imageVector = if (playerState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (playerState.isPlaying) "暂停" else "播放",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(19.dp),
                        )
                    }
                }
            }
        }

        // Style 1 (极简书页): razor-thin 2dp progress hairline along very bottom edge
        if (style == MiniPlayerStyle.CLASSIC_BOOK) {
            Canvas(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(2.dp),
            ) {
                drawLine(
                    color = trackBgColor.copy(alpha = 0.45f),
                    start = Offset(0f, size.height / 2f),
                    end = Offset(size.width, size.height / 2f),
                    strokeWidth = size.height,
                )
                drawLine(
                    color = primaryColor,
                    start = Offset(0f, size.height / 2f),
                    end = Offset(size.width * progress, size.height / 2f),
                    strokeWidth = size.height,
                )
            }
        }
    }
}
