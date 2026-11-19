package com.puremusic.player.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.puremusic.player.data.model.AppFontStyle
import com.puremusic.player.data.model.BackgroundStyle
import com.puremusic.player.data.model.LayoutMode
import com.puremusic.player.data.model.PlayerTheme
import com.puremusic.player.ui.theme.ForestGreen
import com.puremusic.player.ui.theme.VermilionAccent

private enum class SettingsSection(val label: String) {
    ALL_SONGS("全部歌曲"),
    LIBRARY("曲库管理"),
    APPEARANCE("外观设置"),
    PLAYBACK("播放设置"),
    ABOUT("关于"),
}

@Composable
fun SettingsScreen(
    songCount: Int,
    albumCount: Int,
    folderCount: Int,
    layoutMode: LayoutMode,
    playerTheme: PlayerTheme,
    fontStyle: AppFontStyle,
    backgroundStyle: BackgroundStyle,
    onClose: () -> Unit,
    onSelectLayoutMode: (LayoutMode) -> Unit,
    onSelectPlayerTheme: (PlayerTheme) -> Unit,
    onSelectFontStyle: (AppFontStyle) -> Unit,
    onSelectBackgroundStyle: (BackgroundStyle) -> Unit,
    onResetAppearance: () -> Unit,
    onRescanLibrary: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
) {
    BackHandler(onBack = onClose)

    var activeSection by remember { mutableStateOf(SettingsSection.APPEARANCE) }
    var showFontDialog by remember { mutableStateOf(false) }
    var showBackgroundDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            // Header: "设置" + Close icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 10.dp, top = 10.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "设置",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "关闭设置",
                    )
                }
            }

            // Two-Pane Split Content
            Row(modifier = Modifier.fillMaxSize()) {
                // Left Navigation Rail
                Column(
                    modifier = Modifier
                        .width(126.dp)
                        .fillMaxHeight()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    SettingsSection.entries.forEach { section ->
                        val selected = section == activeSection
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (selected) {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    } else {
                                        Color.Transparent
                                    },
                                )
                                .clickable {
                                    if (section == SettingsSection.ALL_SONGS) {
                                        onClose()
                                    } else {
                                        activeSection = section
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                        ) {
                            Text(
                                text = section.label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    }
                }

                VerticalDivider(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                )

                // Right Content Pane
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    when (activeSection) {
                        SettingsSection.APPEARANCE -> AppearanceSettingsPane(
                            layoutMode = layoutMode,
                            playerTheme = playerTheme,
                            fontStyle = fontStyle,
                            backgroundStyle = backgroundStyle,
                            onSelectLayoutMode = onSelectLayoutMode,
                            onSelectPlayerTheme = onSelectPlayerTheme,
                            onOpenFontPicker = { showFontDialog = true },
                            onOpenBackgroundPicker = { showBackgroundDialog = true },
                            onResetAppearance = onResetAppearance,
                        )

                        SettingsSection.LIBRARY -> {
                            Text(
                                text = "本地曲库概览",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "已索引 $songCount 首歌曲 · $albumCount 张专辑 · $folderCount 个文件夹",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            FilledTonalButton(onClick = onRescanLibrary) {
                                Icon(Icons.Rounded.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("重新扫描本地音频")
                            }
                        }

                        SettingsSection.PLAYBACK -> {
                            Text(
                                text = "播放与音效",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            FilledTonalButton(
                                onClick = onOpenEqualizer,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Icon(Icons.Rounded.Equalizer, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("打开音频均衡器")
                            }
                            FilledTonalButton(
                                onClick = onOpenSleepTimer,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Icon(Icons.Rounded.Timer, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("设置睡眠定时关闭")
                            }
                            Text(
                                text = "已默认开启耳机拔出自动暂停与系统音频焦点管理。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        SettingsSection.ABOUT, SettingsSection.ALL_SONGS -> {
                            Text(
                                text = "简约音乐 (PureMusic)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "版本 2.2.0\n\n纯本地离线音乐播放器，采用 Kotlin + Jetpack Compose + Media3 独立编写，无广告、无联网追踪。",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showFontDialog) {
        OptionPickerDialog(
            title = "选择字体",
            options = AppFontStyle.entries.map { it to it.label },
            selected = fontStyle,
            onSelect = {
                onSelectFontStyle(it)
                showFontDialog = false
            },
            onDismiss = { showFontDialog = false },
        )
    }

    if (showBackgroundDialog) {
        OptionPickerDialog(
            title = "选择背景",
            options = BackgroundStyle.entries.map { it to it.label },
            selected = backgroundStyle,
            onSelect = {
                onSelectBackgroundStyle(it)
                showBackgroundDialog = false
            },
            onDismiss = { showBackgroundDialog = false },
        )
    }
}

@Composable
private fun AppearanceSettingsPane(
    layoutMode: LayoutMode,
    playerTheme: PlayerTheme,
    fontStyle: AppFontStyle,
    backgroundStyle: BackgroundStyle,
    onSelectLayoutMode: (LayoutMode) -> Unit,
    onSelectPlayerTheme: (PlayerTheme) -> Unit,
    onOpenFontPicker: () -> Unit,
    onOpenBackgroundPicker: () -> Unit,
    onResetAppearance: () -> Unit,
) {
    // 1. Layout Segmented Control
    Text(
        text = "布局",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        LayoutMode.entries.forEach { mode ->
            val selected = mode == layoutMode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) ForestGreen else Color.Transparent)
                    .clickable { onSelectLayoutMode(mode) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = mode.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    // 2. Theme 2x2 Visual Preview Cards
    Text(
        text = "主题",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ThemePreviewCard(
                theme = PlayerTheme.VERMILION_CREAM,
                selected = playerTheme == PlayerTheme.VERMILION_CREAM,
                onClick = { onSelectPlayerTheme(PlayerTheme.VERMILION_CREAM) },
                modifier = Modifier.weight(1f),
            )
            ThemePreviewCard(
                theme = PlayerTheme.WARM_WOOD,
                selected = playerTheme == PlayerTheme.WARM_WOOD,
                onClick = { onSelectPlayerTheme(PlayerTheme.WARM_WOOD) },
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ThemePreviewCard(
                theme = PlayerTheme.POSTER_WALL,
                selected = playerTheme == PlayerTheme.POSTER_WALL,
                onClick = { onSelectPlayerTheme(PlayerTheme.POSTER_WALL) },
                modifier = Modifier.weight(1f),
            )
            ThemePreviewCard(
                theme = PlayerTheme.RETRO_DECK,
                selected = playerTheme == PlayerTheme.RETRO_DECK,
                onClick = { onSelectPlayerTheme(PlayerTheme.RETRO_DECK) },
                modifier = Modifier.weight(1f),
            )
        }
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.16f))

    // 3. Font & Background Selector Rows
    SettingsValueRow(
        label = "字体",
        value = fontStyle.label,
        onClick = onOpenFontPicker,
    )
    SettingsValueRow(
        label = "背景",
        value = backgroundStyle.label,
        onClick = onOpenBackgroundPicker,
    )

    Spacer(modifier = Modifier.height(8.dp))

    // 4. Reset Theme Button
    TextButton(
        onClick = onResetAppearance,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "重置主题",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ThemePreviewCard(
    theme: PlayerTheme,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (selected) ForestGreen else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.28f)
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(if (selected) 1.8.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    when (theme) {
                        PlayerTheme.VERMILION_CREAM -> VermilionAccent
                        PlayerTheme.WARM_WOOD -> Color(0xFFE9E2D7)
                        PlayerTheme.POSTER_WALL -> Color(0xFFDFD9CE)
                        PlayerTheme.RETRO_DECK -> Color(0xFFC8C4BC)
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            // Minimalist miniature preview illustration
            when (theme) {
                PlayerTheme.VERMILION_CREAM -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1A1A1A)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(VermilionAccent),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                        )
                    }
                }

                PlayerTheme.WARM_WOOD -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color(0xFF8C3B28)),
                        )
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1F1F1F)),
                        )
                    }
                }

                PlayerTheme.POSTER_WALL -> {
                    Box(contentAlignment = Alignment.Center) {
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF3A5A8C)),
                            )
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF8B5E3C)),
                            )
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF4A6B5D)),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF121212)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFD84315)),
                            )
                        }
                    }
                }

                PlayerTheme.RETRO_DECK -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.78f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(0xFF222222)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFFD84315)),
                        )
                    }
                }
            }

            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(ForestGreen),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
        }

        Text(
            text = theme.label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
        )
    }
}

@Composable
private fun SettingsValueRow(
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun <T> OptionPickerDialog(
    title: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { (item, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(item) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = item == selected,
                            onClick = { onSelect(item) },
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}
