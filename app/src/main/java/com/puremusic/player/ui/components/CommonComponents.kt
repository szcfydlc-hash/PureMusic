package com.puremusic.player.ui.components

import android.net.Uri
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
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
import com.puremusic.player.ui.theme.LocalLayoutDensity
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
    cornerRadius: Dp = 8.dp,
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
            .border(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f), shape),
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
    extraMenuActionLabel: String? = null,
    onExtraMenuAction: (() -> Unit)? = null,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val density = LocalLayoutDensity.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = density.rowVerticalPaddingDp.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(density.artSizeDp.dp)) {
            AlbumArtwork(
                uri = song.albumArtUri,
                contentDescription = song.album,
                modifier = Modifier.fillMaxSize(),
                cornerRadius = 10.dp,
                iconSize = 20.dp,
            )
            if (isCurrent) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp))
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
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${song.artist} · ${formatDuration(song.durationMs)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "更多操作",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
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
                if (onShowSongInfo != null) {
                    DropdownMenuItem(
                        text = { Text("歌曲信息") },
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
    modifier: Modifier = Modifier,
) {
    val song = playerState.currentSong ?: return
    val pillShape = RoundedCornerShape(22.dp)
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .height(62.dp)
            .clip(pillShape)
            .clickable(onClick = onOpenNowPlaying),
    ) {
        if (liquidGlassEnabled) {
            val blurModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Modifier.blur(26.dp)
            } else {
                Modifier
            }
            AlbumArtwork(
                uri = song.albumArtUri,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .then(blurModifier)
                    .alpha(if (isDark) 0.30f else 0.36f),
                cornerRadius = 22.dp,
                iconSize = 18.dp,
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isDark) {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF1A1D24).copy(alpha = 0.88f),
                                    Color(0xFF222630).copy(alpha = 0.80f),
                                    Color(0xFF1A1D24).copy(alpha = 0.88f),
                                ),
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.86f),
                                    Color(0xFFF6F7FB).copy(alpha = 0.78f),
                                    Color.White.copy(alpha = 0.86f),
                                ),
                            )
                        },
                    )
                    .border(
                        width = 0.8.dp,
                        brush = Brush.linearGradient(
                            colors = if (isDark) {
                                listOf(
                                    Color.White.copy(alpha = 0.22f),
                                    Color.White.copy(alpha = 0.06f),
                                    Color.White.copy(alpha = 0.16f),
                                )
                            } else {
                                listOf(
                                    Color.White.copy(alpha = 0.96f),
                                    Color(0xFFD5DAE5).copy(alpha = 0.55f),
                                    Color.White.copy(alpha = 0.90f),
                                )
                            },
                        ),
                        shape = pillShape,
                    ),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isDark) Color(0xFF181B22) else Color.White)
                    .border(
                        0.8.dp,
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.14f),
                        pillShape,
                    ),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF121316))
                            .border(1.dp, Color.White.copy(alpha = 0.22f), CircleShape)
                            .padding(9.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        AlbumArtwork(
                            uri = current.albumArtUri,
                            contentDescription = current.album,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            cornerRadius = 20.dp,
                            iconSize = 12.dp,
                        )
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

            FilledIconButton(
                onClick = onTogglePlayPause,
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                modifier = Modifier.size(38.dp),
            ) {
                Icon(
                    imageVector = if (playerState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (playerState.isPlaying) "暂停" else "播放",
                    modifier = Modifier.size(21.dp),
                )
            }

            IconButton(
                onClick = onSkipNext,
                modifier = Modifier.size(34.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    contentDescription = "下一首",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}
