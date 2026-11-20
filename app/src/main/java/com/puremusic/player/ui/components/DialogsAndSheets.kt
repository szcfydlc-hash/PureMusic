package com.puremusic.player.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.puremusic.player.data.model.EqualizerState
import com.puremusic.player.data.model.LyricLine
import com.puremusic.player.data.model.Playlist
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import coil.compose.SubcomposeAsyncImage
import com.puremusic.player.data.model.Song
import com.puremusic.player.data.repository.OnlineCoverCandidate
import com.puremusic.player.data.repository.OnlineLyricCandidate
import java.util.Locale

@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(12.dp),
        title = { Text("新建歌单", fontWeight = FontWeight.SemiBold) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("歌单名称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onCreate(name.trim())
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank(),
            ) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}

@Composable
fun AddToPlaylistDialog(
    song: Song,
    playlists: List<Playlist>,
    onDismiss: () -> Unit,
    onSelectPlaylist: (String) -> Unit,
    onCreateAndAdd: (String) -> Unit,
) {
    var showCreateField by remember { mutableStateOf(playlists.isEmpty()) }
    var newName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(12.dp),
        title = { Text("添加到歌单", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (playlists.isNotEmpty()) {
                    Column(modifier = Modifier.heightIn(max = 220.dp)) {
                        playlists.forEach { playlist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectPlaylist(playlist.id)
                                        onDismiss()
                                    }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.QueueMusic,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(playlist.name, style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        text = "${playlist.songIds.size} 首歌曲",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider(thickness = 0.6.dp, color = MaterialTheme.colorScheme.outline)
                }

                if (showCreateField) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("新建歌单并加入") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    TextButton(onClick = { showCreateField = true }) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("新建歌单")
                    }
                }
            }
        },
        confirmButton = {
            if (showCreateField) {
                TextButton(
                    onClick = {
                        if (newName.isNotBlank()) {
                            onCreateAndAdd(newName.trim())
                            onDismiss()
                        }
                    },
                    enabled = newName.isNotBlank(),
                ) {
                    Text("创建并添加")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SleepTimerDialog(
    remainingMs: Long?,
    onDismiss: () -> Unit,
    onSelectMinutes: (Int) -> Unit,
    onCancelTimer: () -> Unit,
) {
    val options = listOf(10, 15, 30, 45, 60, 90)
    val chipShape = RoundedCornerShape(7.dp)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(12.dp),
        title = { Text("睡眠定时关闭", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (remainingMs != null) {
                    Text(
                        text = "剩余倒计时：${formatCountdown(remainingMs)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                } else {
                    Text(
                        text = "选择在多少分钟后自动暂停播放：",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    options.forEach { minutes ->
                        Box(
                            modifier = Modifier
                                .clip(chipShape)
                                .background(MaterialTheme.colorScheme.background)
                                .border(0.75.dp, MaterialTheme.colorScheme.outline, chipShape)
                                .clickable {
                                    onSelectMinutes(minutes)
                                    onDismiss()
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = "${minutes} 分钟",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (remainingMs != null) {
                TextButton(
                    onClick = {
                        onCancelTimer()
                        onDismiss()
                    },
                ) {
                    Text("取消定时")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerBottomSheet(
    state: EqualizerState,
    onDismiss: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onSelectPreset: (Int) -> Unit,
    onBandLevelChange: (Int, Int) -> Unit,
) {
    val outlineColor = MaterialTheme.colorScheme.outline
    val cardShape = RoundedCornerShape(10.dp)
    val pillShape = RoundedCornerShape(7.dp)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.outline,
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = "音频均衡器",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = if (state.enabled) "复古 Hi-Fi 5 段声场调校已开启" else "点击右侧开关启用频响调校",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = state.enabled,
                    enabled = state.available,
                    onCheckedChange = onToggleEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                        uncheckedBorderColor = outlineColor,
                    ),
                )
            }

            if (!state.available) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(cardShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(0.75.dp, outlineColor, cardShape)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "当前音频会话暂未就绪，请先播放一首歌曲后再打开均衡器。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                // Flat Archival Book Preset Strip
                if (state.presets.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        state.presets.forEachIndexed { index, presetName ->
                            val selected = state.currentPreset == index
                            Box(
                                modifier = Modifier
                                    .clip(pillShape)
                                    .background(
                                        if (selected && state.enabled) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        },
                                    )
                                    .border(
                                        width = 0.75.dp,
                                        color = if (selected && state.enabled) MaterialTheme.colorScheme.primary else outlineColor,
                                        shape = pillShape,
                                    )
                                    .alpha(if (state.enabled) 1f else 0.5f)
                                    .clickable(enabled = state.enabled) { onSelectPreset(index) }
                                    .padding(horizontal = 14.dp, vertical = 7.dp),
                            ) {
                                Text(
                                    text = presetName,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selected && state.enabled) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                )
                            }
                        }
                    }
                }

                // Vintage Hi-Fi Console Card (Frequency Curve + 5 Vertical Faders)
                val minVal = state.minBandLevelMb.toFloat()
                val maxVal = state.maxBandLevelMb.toFloat().coerceAtLeast(minVal + 1f)
                val range = (maxVal - minVal).coerceAtLeast(1f)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(cardShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(0.75.dp, outlineColor, cardShape)
                        .alpha(if (state.enabled) 1f else 0.52f)
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    // 1. Precision Frequency-Response Curve Graph
                    val curveColor = MaterialTheme.colorScheme.primary
                    val gridColor = outlineColor.copy(alpha = 0.85f)
                    val zeroLineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(74.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f))
                            .border(0.6.dp, outlineColor, RoundedCornerShape(6.dp)),
                    ) {
                        val padX = 24f
                        val padY = 14f
                        val w = (size.width - padX * 2f).coerceAtLeast(1f)
                        val h = (size.height - padY * 2f).coerceAtLeast(1f)

                        // Hairline horizontal dB grid (+max, 0, -min)
                        for (row in 0..4) {
                            val gy = padY + h * (row / 4f)
                            drawLine(
                                color = if (row == 2) zeroLineColor else gridColor.copy(alpha = 0.45f),
                                start = Offset(0f, gy),
                                end = Offset(size.width, gy),
                                strokeWidth = if (row == 2) 1.2f else 0.7f,
                            )
                        }

                        if (state.bands.isNotEmpty()) {
                            val pts = state.bands.mapIndexed { idx, band ->
                                val xFrac = if (state.bands.size == 1) 0.5f else idx.toFloat() / (state.bands.size - 1).toFloat()
                                val yNorm = ((band.levelMb.toFloat() - minVal) / range).coerceIn(0f, 1f)
                                Offset(padX + w * xFrac, padY + h * (1f - yNorm))
                            }
                            val path = Path().apply {
                                moveTo(pts.first().x, pts.first().y)
                                for (i in 0 until pts.lastIndex) {
                                    val p0 = pts[i]
                                    val p1 = pts[i + 1]
                                    val midX = (p0.x + p1.x) / 2f
                                    cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                                }
                            }
                            drawPath(
                                path = path,
                                color = curveColor,
                                style = Stroke(width = 2.6f, cap = StrokeCap.Round),
                            )
                            pts.forEach { p ->
                                drawCircle(color = curveColor, radius = 4.5f, center = p)
                            }
                        }
                    }

                    // 2. 5-Band Vertical Precision Fader Rack
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(198.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        state.bands.forEach { band ->
                            val freqLabel = if (band.centerFreqHz >= 1000) {
                                String.format(Locale.getDefault(), "%.1fk", band.centerFreqHz / 1000f)
                            } else {
                                "${band.centerFreqHz}"
                            }
                            val dbLabel = String.format(Locale.getDefault(), "%+.1f", band.levelMb / 100f)
                            val normalized = ((band.levelMb.toFloat() - minVal) / range).coerceIn(0f, 1f)

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = dbLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (band.levelMb != 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                )

                                VerticalVintageFader(
                                    fraction = normalized,
                                    enabled = state.enabled,
                                    onFractionChange = { f ->
                                        val newMb = (minVal + f * range).toInt()
                                        onBandLevelChange(band.index, newMb)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .width(38.dp)
                                        .padding(vertical = 6.dp),
                                )

                                Text(
                                    text = freqLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                )
                            }
                        }
                    }

                    HorizontalDivider(thickness = 0.6.dp, color = outlineColor)

                    // 3. One-Tap Reset to Flat (0 dB)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(pillShape)
                            .clickable(enabled = state.enabled) {
                                state.bands.forEach { band -> onBandLevelChange(band.index, 0) }
                            }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RestartAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "恢复平直响应 (0.0 dB)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VerticalVintageFader(
    fraction: Float,
    enabled: Boolean,
    onFractionChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    var heightPx by remember { mutableFloatStateOf(1f) }
    val trackColor = MaterialTheme.colorScheme.outline
    val activeColor = MaterialTheme.colorScheme.primary
    val knobFill = MaterialTheme.colorScheme.surface
    val knobStroke = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .onSizeChanged { heightPx = it.height.toFloat().coerceAtLeast(1f) }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures { offset ->
                    val f = (1f - offset.y / heightPx).coerceIn(0f, 1f)
                    onFractionChange(f)
                }
            }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectVerticalDragGestures { change, _ ->
                    change.consume()
                    val f = (1f - change.position.y / heightPx).coerceIn(0f, 1f)
                    onFractionChange(f)
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val padY = 14f
            val usableH = (size.height - padY * 2f).coerceAtLeast(1f)
            val topY = padY
            val bottomY = padY + usableH
            val centerY = padY + usableH * 0.5f
            val thumbY = padY + usableH * (1f - fraction.coerceIn(0f, 1f))

            // Subtle side tick marks
            for (t in 0..4) {
                val ty = padY + usableH * (t / 4f)
                drawLine(
                    color = trackColor.copy(alpha = 0.65f),
                    start = Offset(cx - 10f, ty),
                    end = Offset(cx - 5f, ty),
                    strokeWidth = 1f,
                )
                drawLine(
                    color = trackColor.copy(alpha = 0.65f),
                    start = Offset(cx + 5f, ty),
                    end = Offset(cx + 10f, ty),
                    strokeWidth = 1f,
                )
            }

            // Thin hairline vertical rail
            drawLine(
                color = trackColor,
                start = Offset(cx, topY),
                end = Offset(cx, bottomY),
                strokeWidth = 4.2f,
                cap = StrokeCap.Round,
            )

            // Active accent fill from 0 dB center line to thumb
            drawLine(
                color = activeColor,
                start = Offset(cx, centerY),
                end = Offset(cx, thumbY),
                strokeWidth = 4.6f,
                cap = StrokeCap.Round,
            )

            // Precision circular vintage knob
            drawCircle(
                color = knobFill,
                radius = 12f,
                center = Offset(cx, thumbY),
            )
            drawCircle(
                color = knobStroke.copy(alpha = 0.85f),
                radius = 12f,
                center = Offset(cx, thumbY),
                style = Stroke(width = 2.2f),
            )
            drawCircle(
                color = activeColor,
                radius = 4f,
                center = Offset(cx, thumbY),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueBottomSheet(
    queue: List<Song>,
    currentIndex: Int,
    onDismiss: () -> Unit,
    onSelectIndex: (Int) -> Unit,
    onRemoveIndex: (Int) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outline)
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
        ) {
            Text(
                text = "当前播放队列 (${queue.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )
            HorizontalDivider(thickness = 0.75.dp, color = MaterialTheme.colorScheme.outline)
            if (queue.isEmpty()) {
                Text(
                    text = "播放队列为空",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(20.dp),
                )
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                    itemsIndexed(queue) { index, song ->
                        val isCurrent = index == currentIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectIndex(index)
                                    onDismiss()
                                }
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (isCurrent) {
                                Icon(
                                    imageVector = Icons.Rounded.GraphicEq,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp),
                                )
                            } else {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.width(20.dp),
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = song.artist,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            IconButton(onClick = { onRemoveIndex(index) }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "从队列移除",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsBottomSheet(
    songTitle: String,
    lyrics: List<LyricLine>,
    positionMs: Long,
    lyricFontFamily: FontFamily = FontFamily.Serif,
    lyricItalic: Boolean = true,
    onDismiss: () -> Unit,
    onSeekTo: (Long) -> Unit,
) {
    val activeIdx = remember(lyrics, positionMs) {
        lyrics.indexOfLast { it.timeMs <= positionMs }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outline)
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp),
        ) {
            Text(
                text = "歌词 · $songTitle",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            )
            HorizontalDivider(thickness = 0.75.dp, color = MaterialTheme.colorScheme.outline)
            if (lyrics.isEmpty()) {
                Text(
                    text = "未找到本地 .lrc 歌词文件。\n将与音频同名的 .lrc 歌词放入同一目录或 Music/Lyrics 文件夹即可自动同步显示。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 36.dp),
                )
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 440.dp)) {
                    itemsIndexed(lyrics) { index, line ->
                        val isActive = index == activeIdx
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSeekTo(line.timeMs) }
                                .padding(horizontal = 24.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = line.primaryText,
                                style = (if (isActive) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium).copy(
                                    fontFamily = lyricFontFamily,
                                    fontStyle = if (lyricItalic) FontStyle.Italic else FontStyle.Normal,
                                ),
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                            if (!line.secondaryText.isNullOrBlank()) {
                                Text(
                                    text = line.secondaryText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SongInfoDialog(
    song: Song,
    onDismiss: () -> Unit,
) {
    val formatExt = song.filePath
        .substringAfterLast('.', "")
        .uppercase(Locale.getDefault())
        .ifBlank { "LOCAL AUDIO" }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(12.dp),
        title = { Text("歌曲信息", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoMetaRow("歌名", song.title)
                InfoMetaRow("歌手", song.artist)
                InfoMetaRow("专辑", song.album)
                InfoMetaRow("时长", formatDuration(song.durationMs))
                InfoMetaRow("格式", formatExt)
                InfoMetaRow("文件夹", song.folderName)
                if (song.filePath.isNotBlank()) {
                    InfoMetaRow("路径", song.filePath)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("知道了") }
        },
    )
}

@Composable
private fun InfoMetaRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineLyricsSearchSheet(
    song: Song,
    results: List<OnlineLyricCandidate>,
    isSearching: Boolean,
    errorMessage: String?,
    onSearch: (String) -> Unit,
    onSelectCandidate: (OnlineLyricCandidate) -> Unit,
    onImportLocalLrc: () -> Unit,
    onClearCustomLyrics: () -> Unit,
    onDismiss: () -> Unit,
) {
    val defaultQuery = remember(song.id) {
        if (song.artist == "未知歌手") song.title else "${song.title} ${song.artist}"
    }
    var query by remember(song.id) { mutableStateOf(defaultQuery) }
    val outlineColor = MaterialTheme.colorScheme.outline
    val boxShape = RoundedCornerShape(8.dp)

    LaunchedEffect(song.id) {
        onSearch(defaultQuery)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
        dragHandle = { BottomSheetDefaults.DragHandle(color = outlineColor) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "在线匹配歌词",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            // Search Input + Search Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(boxShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(0.75.dp, outlineColor, boxShape)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Box(
                    modifier = Modifier
                        .height(42.dp)
                        .clip(boxShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(enabled = !isSearching && query.isNotBlank()) { onSearch(query) }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "搜索",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            // Results area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp, max = 340.dp),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    isSearching -> CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    !errorMessage.isNullOrBlank() && results.isEmpty() -> {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            itemsIndexed(results) { _, item ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(boxShape)
                                        .background(MaterialTheme.colorScheme.surface)
                                        .border(0.75.dp, outlineColor, boxShape)
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.trackName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            Text(
                                                text = "${item.artistName} · ${formatDuration(item.durationSec * 1000L)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        val pine = Color(0xFF2F5D50)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(5.dp))
                                                .background(if (item.hasSynced) pine.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant)
                                                .padding(horizontal = 7.dp, vertical = 3.dp),
                                        ) {
                                            Text(
                                                text = if (item.hasSynced) "同步 .lrc" else "纯文本",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (item.hasSynced) pine else MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                        }
                                    }

                                    if (item.previewLines.isNotBlank()) {
                                        Text(
                                            text = item.previewLines,
                                            style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .border(0.75.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp))
                                                .clickable {
                                                    onSelectCandidate(item)
                                                    onDismiss()
                                                }
                                                .padding(horizontal = 14.dp, vertical = 5.dp),
                                        ) {
                                            Text(
                                                text = "选用此歌词",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(thickness = 0.6.dp, color = outlineColor)

            // Bottom Fallback Actions: Local .lrc Import + Clear Custom
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .clip(boxShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(0.75.dp, outlineColor, boxShape)
                        .clickable {
                            onDismiss()
                            onImportLocalLrc()
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FolderOpen,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("导入本地 .lrc", style = MaterialTheme.typography.labelMedium)
                }

                TextButton(
                    onClick = {
                        onClearCustomLyrics()
                        onDismiss()
                    },
                ) {
                    Text("清除自定义歌词", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineCoverSearchSheet(
    song: Song,
    results: List<OnlineCoverCandidate>,
    isSearching: Boolean,
    errorMessage: String?,
    onSearch: (String) -> Unit,
    onSelectCandidate: (OnlineCoverCandidate) -> Unit,
    onPickFromGallery: () -> Unit,
    onRestoreOriginalCover: () -> Unit,
    onDismiss: () -> Unit,
) {
    val defaultQuery = remember(song.id) {
        if (song.artist == "未知歌手") song.title else "${song.title} ${song.artist}"
    }
    var query by remember(song.id) { mutableStateOf(defaultQuery) }
    var applyingUrl by remember { mutableStateOf<String?>(null) }
    val outlineColor = MaterialTheme.colorScheme.outline
    val boxShape = RoundedCornerShape(8.dp)

    LaunchedEffect(song.id) {
        onSearch(defaultQuery)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
        dragHandle = { BottomSheetDefaults.DragHandle(color = outlineColor) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "在线匹配封面",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(boxShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(0.75.dp, outlineColor, boxShape)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Box(
                    modifier = Modifier
                        .height(42.dp)
                        .clip(boxShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(enabled = !isSearching && query.isNotBlank()) { onSearch(query) }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "搜索",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp, max = 350.dp),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    isSearching -> CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    !errorMessage.isNullOrBlank() && results.isEmpty() -> {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(results, key = { it.highResUrl }) { item ->
                                val isApplying = applyingUrl == item.highResUrl
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(boxShape)
                                        .background(MaterialTheme.colorScheme.surface)
                                        .border(0.75.dp, outlineColor, boxShape)
                                        .clickable(enabled = applyingUrl == null) {
                                            applyingUrl = item.highResUrl
                                            onSelectCandidate(item)
                                        }
                                        .padding(8.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(5.dp)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        SubcomposeAsyncImage(
                                            model = item.previewUrl,
                                            contentDescription = item.albumName,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                        if (isApplying) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color.Black.copy(alpha = 0.45f)),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                CircularProgressIndicator(
                                                    color = Color.White,
                                                    modifier = Modifier.size(26.dp),
                                                    strokeWidth = 2.5.dp,
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = item.albumName,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = item.artistName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(thickness = 0.6.dp, color = outlineColor)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .clip(boxShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(0.75.dp, outlineColor, boxShape)
                        .clickable {
                            onDismiss()
                            onPickFromGallery()
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("从手机相册选择", style = MaterialTheme.typography.labelMedium)
                }

                TextButton(
                    onClick = {
                        onRestoreOriginalCover()
                        onDismiss()
                    },
                ) {
                    Text("恢复原封面", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
