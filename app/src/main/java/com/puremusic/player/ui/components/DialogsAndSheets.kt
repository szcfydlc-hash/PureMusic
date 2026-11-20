package com.puremusic.player.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.puremusic.player.data.model.EqualizerState
import com.puremusic.player.data.model.LyricLine
import com.puremusic.player.data.model.Playlist
import com.puremusic.player.data.model.Song
import java.util.Locale

@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建歌单") },
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
        title = { Text("添加到歌单") },
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
                    HorizontalDivider()
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("定时关闭") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (remainingMs != null) {
                    Text(
                        text = "剩余倒计时：${formatCountdown(remainingMs)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
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
                        FilterChip(
                            selected = false,
                            onClick = {
                                onSelectMinutes(minutes)
                                onDismiss()
                            },
                            label = { Text("${minutes} 分钟") },
                        )
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
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "音频均衡器",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Switch(
                    checked = state.enabled,
                    enabled = state.available,
                    onCheckedChange = onToggleEnabled,
                )
            }

            if (!state.available) {
                Text(
                    text = "当前设备或音频会话暂未就绪，请先播放一首歌曲后再打开均衡器。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                if (state.presets.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        state.presets.forEachIndexed { index, presetName ->
                            FilterChip(
                                selected = state.currentPreset == index,
                                onClick = { onSelectPreset(index) },
                                enabled = state.enabled,
                                label = { Text(presetName) },
                            )
                        }
                    }
                }

                val minVal = state.minBandLevelMb.toFloat()
                val maxVal = state.maxBandLevelMb.toFloat().coerceAtLeast(minVal + 1f)

                state.bands.forEach { band ->
                    val freqLabel = if (band.centerFreqHz >= 1000) {
                        String.format(Locale.getDefault(), "%.1f kHz", band.centerFreqHz / 1000f)
                    } else {
                        "${band.centerFreqHz} Hz"
                    }
                    val dbLabel = String.format(Locale.getDefault(), "%+.1f dB", band.levelMb / 100f)

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(freqLabel, style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = dbLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Slider(
                            value = band.levelMb.toFloat().coerceIn(minVal, maxVal),
                            onValueChange = { onBandLevelChange(band.index, it.toInt()) },
                            valueRange = minVal..maxVal,
                            enabled = state.enabled,
                        )
                    }
                }
            }
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
    ModalBottomSheet(onDismissRequest = onDismiss) {
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
            HorizontalDivider()
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
    lyricFontFamily: androidx.compose.ui.text.font.FontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
    lyricItalic: Boolean = true,
    onDismiss: () -> Unit,
    onSeekTo: (Long) -> Unit,
) {
    val activeIdx = remember(lyrics, positionMs) {
        lyrics.indexOfLast { it.timeMs <= positionMs }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
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
            HorizontalDivider()
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
                                    fontStyle = if (lyricItalic) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
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
        title = { Text("歌曲信息") },
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
