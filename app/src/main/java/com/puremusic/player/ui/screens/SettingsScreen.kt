package com.puremusic.player.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.puremusic.player.data.model.AppAccentColor
import com.puremusic.player.data.model.AppFontStyle
import com.puremusic.player.data.model.BackgroundStyle
import com.puremusic.player.data.model.LayoutMode
import com.puremusic.player.data.model.LyricFontStyle
import com.puremusic.player.data.model.MinDurationFilter
import com.puremusic.player.data.model.MusicFolder
import com.puremusic.player.data.model.PlayerTheme
import com.puremusic.player.data.model.ScanStatus
import com.puremusic.player.ui.components.liquidGlass
import com.puremusic.player.ui.theme.DeepInkSidebar
import com.puremusic.player.ui.theme.VermilionAccent
import com.puremusic.player.ui.theme.WarmPaperSidebar

private enum class SettingsSection(val label: String, val icon: ImageVector) {
    ALL_SONGS("全部歌曲", Icons.Rounded.MusicNote),
    LIBRARY("曲库管理", Icons.Rounded.Folder),
    APPEARANCE("外观设置", Icons.Rounded.Palette),
    PLAYBACK("播放设置", Icons.Rounded.PlayCircleOutline),
    ABOUT("关于", Icons.Rounded.Info),
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    songCount: Int,
    albumCount: Int,
    allFolders: List<MusicFolder>,
    excludedFolders: Set<String>,
    minDurationFilter: MinDurationFilter,
    scanStatus: ScanStatus,
    layoutMode: LayoutMode,
    liquidGlassEnabled: Boolean,
    playerTheme: PlayerTheme,
    accentColor: AppAccentColor,
    fontStyle: AppFontStyle,
    lyricFontStyle: LyricFontStyle,
    backgroundStyle: BackgroundStyle,
    onClose: () -> Unit,
    onSelectLayoutMode: (LayoutMode) -> Unit,
    onToggleLiquidGlass: (Boolean) -> Unit,
    onSelectPlayerTheme: (PlayerTheme) -> Unit,
    onSelectAccentColor: (AppAccentColor) -> Unit,
    onSelectFontStyle: (AppFontStyle) -> Unit,
    onSelectLyricFontStyle: (LyricFontStyle) -> Unit,
    onSelectBackgroundStyle: (BackgroundStyle) -> Unit,
    onResetAppearance: () -> Unit,
    onRescanLibrary: () -> Unit,
    onToggleFolderExcluded: (String) -> Unit,
    onSelectMinDurationFilter: (MinDurationFilter) -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
) {
    BackHandler(onBack = onClose)

    var activeSection by remember { mutableStateOf(SettingsSection.APPEARANCE) }
    var showAppFontDialog by remember { mutableStateOf(false) }
    var showLyricFontDialog by remember { mutableStateOf(false) }
    var showBackgroundDialog by remember { mutableStateOf(false) }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val sidebarBg = if (isDark) DeepInkSidebar else WarmPaperSidebar

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        // Seamless full-height two-pane layout with ZERO vertical white dividing line!
        Row(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
        ) {
            // Left Sidebar adapts cleanly to Light / Dark theme
            Column(
                modifier = Modifier
                    .width(124.dp)
                    .fillMaxHeight()
                    .background(sidebarBg)
                    .statusBarsPadding()
                    .padding(horizontal = 10.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "设置",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                )

                SettingsSection.entries.forEach { section ->
                    val selected = section == activeSection
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
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
                            .padding(horizontal = 10.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = section.icon,
                            contentDescription = null,
                            tint = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(17.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = section.label,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selected) {
                                MaterialTheme.colorScheme.onBackground
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            maxLines = 1,
                        )
                    }
                }
            }

            // Right Content Pane (seamless tonal transition, zero divider line)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = activeSection.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    IconButton(onClick = onClose, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "关闭设置",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    when (activeSection) {
                        SettingsSection.APPEARANCE -> AppearanceSettingsPane(
                            layoutMode = layoutMode,
                            liquidGlassEnabled = liquidGlassEnabled,
                            playerTheme = playerTheme,
                            accentColor = accentColor,
                            fontStyle = fontStyle,
                            lyricFontStyle = lyricFontStyle,
                            backgroundStyle = backgroundStyle,
                            onSelectLayoutMode = onSelectLayoutMode,
                            onToggleLiquidGlass = onToggleLiquidGlass,
                            onSelectPlayerTheme = onSelectPlayerTheme,
                            onSelectAccentColor = onSelectAccentColor,
                            onOpenAppFontPicker = { showAppFontDialog = true },
                            onOpenLyricFontPicker = { showLyricFontDialog = true },
                            onSelectBackgroundStyle = onSelectBackgroundStyle,
                            onOpenBackgroundPicker = { showBackgroundDialog = true },
                            onResetAppearance = onResetAppearance,
                        )

                        SettingsSection.LIBRARY -> {
                            Text(
                                text = "当前曲库显示 $songCount 首歌曲 · $albumCount 张专辑 · 共发现 ${allFolders.size} 个音频目录",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            FilledTonalButton(
                                onClick = onRescanLibrary,
                                enabled = !scanStatus.isScanning,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                if (scanStatus.isScanning) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("正在扫描本地目录...")
                                } else {
                                    Icon(Icons.Rounded.Refresh, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("立即重新扫描本地音频")
                                }
                            }

                            if (!scanStatus.message.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                ) {
                                    Text(
                                        text = scanStatus.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.14f))

                            Text("短音频过滤", style = MaterialTheme.typography.titleSmall)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                MinDurationFilter.entries.forEach { filter ->
                                    FilterChip(
                                        selected = minDurationFilter == filter,
                                        onClick = { onSelectMinDurationFilter(filter) },
                                        label = { Text(filter.label) },
                                    )
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.14f))

                            Text("勾选要纳入曲库的文件夹", style = MaterialTheme.typography.titleSmall)
                            if (allFolders.isEmpty()) {
                                Text(
                                    text = "暂未发现音频文件夹，请点击上方“立即重新扫描”。",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    allFolders.forEach { folder ->
                                        val included = folder.path !in excludedFolders
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable { onToggleFolderExcluded(folder.path) }
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Checkbox(
                                                checked = included,
                                                onCheckedChange = { onToggleFolderExcluded(folder.path) },
                                            )
                                            Icon(
                                                imageVector = Icons.Rounded.Folder,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(19.dp),
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${folder.name} (${folder.songs.size}首)",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                )
                                                Text(
                                                    text = folder.path,
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

                        SettingsSection.PLAYBACK -> {
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
                                text = "版本 3.3.0\n\n纯本地离线音乐播放器，采用 Kotlin + Jetpack Compose + Media3 独立编写，支持自由配色、流光液态玻璃、封面动态取色与可滑动唱片海报墙。",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAppFontDialog) {
        OptionPickerDialog(
            title = "选择软件字体",
            options = AppFontStyle.entries.map { it to it.label },
            selected = fontStyle,
            onSelect = {
                onSelectFontStyle(it)
                showAppFontDialog = false
            },
            onDismiss = { showAppFontDialog = false },
        )
    }

    if (showLyricFontDialog) {
        OptionPickerDialog(
            title = "选择歌词字体",
            options = LyricFontStyle.entries.map { it to it.label },
            selected = lyricFontStyle,
            onSelect = {
                onSelectLyricFontStyle(it)
                showLyricFontDialog = false
            },
            onDismiss = { showLyricFontDialog = false },
        )
    }

    if (showBackgroundDialog) {
        OptionPickerDialog(
            title = "选择背景纸色",
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
    liquidGlassEnabled: Boolean,
    playerTheme: PlayerTheme,
    accentColor: AppAccentColor,
    fontStyle: AppFontStyle,
    lyricFontStyle: LyricFontStyle,
    backgroundStyle: BackgroundStyle,
    onSelectLayoutMode: (LayoutMode) -> Unit,
    onToggleLiquidGlass: (Boolean) -> Unit,
    onSelectPlayerTheme: (PlayerTheme) -> Unit,
    onSelectAccentColor: (AppAccentColor) -> Unit,
    onOpenAppFontPicker: () -> Unit,
    onOpenLyricFontPicker: () -> Unit,
    onSelectBackgroundStyle: (BackgroundStyle) -> Unit,
    onOpenBackgroundPicker: () -> Unit,
    onResetAppearance: () -> Unit,
) {
    // 1. Appearance Light / Dark / System Segmented Control
    Text(
        text = "外观模式",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(3.dp),
    ) {
        val quickModes = listOf(
            BackgroundStyle.WARM_PAPER to "浅色",
            BackgroundStyle.DEEP_INK to "深色",
            BackgroundStyle.SYSTEM to "跟随系统",
        )
        quickModes.forEach { (style, label) ->
            val selected = backgroundStyle == style ||
                (style == BackgroundStyle.WARM_PAPER && backgroundStyle == BackgroundStyle.PURE_WHITE)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onSelectBackgroundStyle(style) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    // 2. Layout Segmented Control
    Text(
        text = "布局",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(3.dp),
    ) {
        LayoutMode.entries.forEach { mode ->
            val selected = mode == layoutMode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onSelectLayoutMode(mode) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = mode.label,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    // 3. Free Accent Color Swatch Picker (6 Colors)
    Text(
        text = "自由配色",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppAccentColor.entries.forEach { colorOption ->
            val selected = colorOption == accentColor
            val swatch = Color(colorOption.colorHex)
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .border(
                        width = if (selected) 2.5.dp else 0.dp,
                        color = if (selected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                        shape = CircleShape,
                    )
                    .padding(if (selected) 3.dp else 0.dp)
                    .clip(CircleShape)
                    .background(swatch)
                    .clickable { onSelectAccentColor(colorOption) },
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = colorOption.label,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
    }

    // 4. Animated Liquid Glass Shimmer Preview Card + Switch
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlass(enabled = liquidGlassEnabled, shape = RoundedCornerShape(16.dp), elevation = 6.dp)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "液态玻璃流光动效",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "开启流动虹彩高光玻璃质感",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = liquidGlassEnabled,
            onCheckedChange = onToggleLiquidGlass,
        )
    }

    // 5. 2x2 Player Theme Cards
    Text(
        text = "主题预设",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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

    // 6. Independent Software Font, Lyric Font & Paper Background Pickers
    SettingsValueRow(label = "软件字体", value = fontStyle.label, onClick = onOpenAppFontPicker)
    SettingsValueRow(label = "歌词字体", value = lyricFontStyle.label, onClick = onOpenLyricFontPicker)
    SettingsValueRow(label = "背景纸色", value = backgroundStyle.label, onClick = onOpenBackgroundPicker)

    TextButton(
        onClick = onResetAppearance,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "重置主题",
            style = MaterialTheme.typography.bodySmall,
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
    val activeColor = MaterialTheme.colorScheme.primary
    val borderColor = if (selected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.24f)
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(if (selected) 1.8.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
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
            when (theme) {
                PlayerTheme.VERMILION_CREAM -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1A1A1A)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(VermilionAccent),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                        )
                    }
                }

                PlayerTheme.WARM_WOOD -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF8C3B28)),
                        )
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1A1A1A)),
                        )
                    }
                }

                PlayerTheme.POSTER_WALL -> {
                    Box(contentAlignment = Alignment.Center) {
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF3A5A8C)),
                            )
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF8B5E3C)),
                            )
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF4A6B5D)),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF121212)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFD84315)),
                            )
                        }
                    }
                }

                PlayerTheme.RETRO_DECK -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.76f)
                            .height(32.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(0xFF222222)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
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
                        .background(activeColor),
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
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
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
