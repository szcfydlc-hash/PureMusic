package com.puremusic.player.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.puremusic.player.data.model.AppAccentColor
import com.puremusic.player.data.model.AppFontStyle
import com.puremusic.player.data.model.BackgroundStyle
import com.puremusic.player.data.model.LayoutDensity
import com.puremusic.player.data.model.LayoutMode
import com.puremusic.player.data.model.LyricFontStyle
import com.puremusic.player.data.model.MinDurationFilter
import com.puremusic.player.data.model.MusicFolder
import com.puremusic.player.data.model.ScanStatus

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
    layoutDensity: LayoutDensity,
    fontScale: Float,
    liquidGlassEnabled: Boolean,
    accentColor: AppAccentColor,
    fontStyle: AppFontStyle,
    lyricFontStyle: LyricFontStyle,
    backgroundStyle: BackgroundStyle,
    isTabletLayout: Boolean,
    bottomContentPadding: PaddingValues,
    onBackToLibrary: () -> Unit,
    onSelectLayoutMode: (LayoutMode) -> Unit,
    onSelectLayoutDensity: (LayoutDensity) -> Unit,
    onChangeFontScale: (Float) -> Unit,
    onToggleLiquidGlass: (Boolean) -> Unit,
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
    BackHandler(onBack = onBackToLibrary)

    var showAppFontDialog by remember { mutableStateOf(false) }
    var showLyricFontDialog by remember { mutableStateOf(false) }
    var showBackgroundDialog by remember { mutableStateOf(false) }
    var showFolderManagerDialog by remember { mutableStateOf(false) }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val canvasBg = MaterialTheme.colorScheme.background
    val cardBg = MaterialTheme.colorScheme.surface
    val outlineColor = MaterialTheme.colorScheme.outline
    val cardRadius = if (liquidGlassEnabled) 18.dp else 9.dp

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = canvasBg,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 14.dp,
                    bottom = 24.dp + bottomContentPadding.calculateBottomPadding(),
                ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Classic Editorial Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isTabletLayout) {
                    IconButton(onClick = onBackToLibrary) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "返回曲库")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Column {
                    Text(
                        text = "设置",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "纸色、排印与曲库偏好",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Card 1: Live Typography & Density Preview Card
            BookSettingsCard(cardBg = cardBg, outlineColor = outlineColor, radius = cardRadius) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = layoutDensity.rowVerticalPaddingDp.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(layoutDensity.artSizeDp.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(0.6.dp, outlineColor, RoundedCornerShape(5.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(21.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "光影之间",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "实时排印预览 · ${layoutDensity.label} · ${(fontScale * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            // Card 2: 外观与排版
            SettingsSectionHeader("外观与排版")
            BookSettingsCard(cardBg = cardBg, outlineColor = outlineColor, radius = cardRadius) {
                Text(
                    text = "纸色模式",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                val quickModes = listOf(
                    BackgroundStyle.WARM_PAPER to "宣纸浅色",
                    BackgroundStyle.DEEP_INK to "夜读深色",
                    BackgroundStyle.SYSTEM to "跟随系统",
                )
                SegmentedOptionBar(
                    items = quickModes.map { it.second },
                    selectedIndex = quickModes.indexOfFirst { (style, _) ->
                        backgroundStyle == style ||
                            (style == BackgroundStyle.WARM_PAPER && backgroundStyle == BackgroundStyle.PURE_WHITE)
                    }.coerceAtLeast(0),
                    liquidGlassEnabled = liquidGlassEnabled,
                    onSelect = { idx -> onSelectBackgroundStyle(quickModes[idx].first) },
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(thickness = 0.6.dp, color = outlineColor)
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "屏幕布局",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                SegmentedOptionBar(
                    items = LayoutMode.entries.map { it.label },
                    selectedIndex = LayoutMode.entries.indexOf(layoutMode),
                    liquidGlassEnabled = liquidGlassEnabled,
                    onSelect = { idx -> onSelectLayoutMode(LayoutMode.entries[idx]) },
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(thickness = 0.6.dp, color = outlineColor)
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "书脊装帧色",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = accentColor.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppAccentColor.entries.forEach { colorOption ->
                        val selected = colorOption == accentColor
                        val swatch = Color(colorOption.colorHex)
                        val swatchShape = RoundedCornerShape(8.dp)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(swatchShape)
                                    .border(
                                        width = if (selected) 2.dp else 0.75.dp,
                                        color = if (selected) MaterialTheme.colorScheme.onBackground else outlineColor,
                                        shape = swatchShape,
                                    )
                                    .padding(if (selected) 2.5.dp else 0.dp)
                                    .clip(RoundedCornerShape(6.dp))
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
                            Text(
                                text = colorOption.shortLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(thickness = 0.6.dp, color = outlineColor)
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "排版密度",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                SegmentedOptionBar(
                    items = LayoutDensity.entries.map { it.label },
                    selectedIndex = LayoutDensity.entries.indexOf(layoutDensity),
                    liquidGlassEnabled = liquidGlassEnabled,
                    onSelect = { idx -> onSelectLayoutDensity(LayoutDensity.entries[idx]) },
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(thickness = 0.6.dp, color = outlineColor)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "全局字体大小",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${(fontScale * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("A-", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Slider(
                        value = fontScale,
                        onValueChange = onChangeFontScale,
                        valueRange = 0.85f..1.25f,
                        steps = 7,
                        modifier = Modifier.weight(1f),
                    )
                    Text("A+", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                HorizontalDivider(thickness = 0.6.dp, color = outlineColor)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "液态玻璃效果",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = "关闭呈现纯粹古典书籍纸页感，开启叠加毛玻璃",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = liquidGlassEnabled,
                        onCheckedChange = onToggleLiquidGlass,
                    )
                }
            }

            // Card 3: 字体与纸色
            SettingsSectionHeader("字体与纸色")
            BookSettingsCard(cardBg = cardBg, outlineColor = outlineColor, radius = cardRadius) {
                SettingsListRow(
                    label = "软件字体",
                    value = fontStyle.label,
                    onClick = { showAppFontDialog = true },
                )
                HorizontalDivider(thickness = 0.6.dp, color = outlineColor)
                SettingsListRow(
                    label = "歌词字体",
                    value = lyricFontStyle.label,
                    onClick = { showLyricFontDialog = true },
                )
                HorizontalDivider(thickness = 0.6.dp, color = outlineColor)
                SettingsListRow(
                    label = "背景纸色",
                    value = backgroundStyle.label,
                    onClick = { showBackgroundDialog = true },
                )
            }

            // Card 4: 曲库管理
            SettingsSectionHeader("曲库管理")
            BookSettingsCard(cardBg = cardBg, outlineColor = outlineColor, radius = cardRadius) {
                Text(
                    text = "当前显示 $songCount 首歌曲 · $albumCount 张专辑 · 已发现 ${allFolders.size} 个目录",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(12.dp))

                val btnShape = RoundedCornerShape(if (liquidGlassEnabled) 14.dp else 7.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .clip(btnShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(enabled = !scanStatus.isScanning, onClick = onRescanLibrary),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    if (scanStatus.isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "正在扫描本地目录...",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(17.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "立即重新扫描本地音频",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                if (!scanStatus.message.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    val pine = Color(0xFF2F5D50)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(7.dp))
                            .background(pine.copy(alpha = if (isDark) 0.24f else 0.10f))
                            .border(0.6.dp, pine.copy(alpha = 0.35f), RoundedCornerShape(7.dp))
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                    ) {
                        Text(
                            text = scanStatus.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) Color(0xFF8CC4B4) else pine,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(thickness = 0.6.dp, color = outlineColor)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "短音频过滤",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    MinDurationFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = minDurationFilter == filter,
                            onClick = { onSelectMinDurationFilter(filter) },
                            label = { Text(filter.label) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(thickness = 0.6.dp, color = outlineColor)

                val enabledCount = (allFolders.size - excludedFolders.size).coerceAtLeast(0)
                SettingsListRow(
                    label = "管理扫描文件夹",
                    value = "已启用 $enabledCount / ${allFolders.size}",
                    onClick = { showFolderManagerDialog = true },
                )
            }

            // Card 5: 播放与音效
            SettingsSectionHeader("播放与音效")
            BookSettingsCard(cardBg = cardBg, outlineColor = outlineColor, radius = cardRadius) {
                SettingsActionIconRow(
                    icon = Icons.Rounded.Equalizer,
                    label = "音频均衡器",
                    subtitle = "5 段频响调节与预设音效",
                    outlineColor = outlineColor,
                    onClick = onOpenEqualizer,
                )
                HorizontalDivider(thickness = 0.6.dp, color = outlineColor)
                SettingsActionIconRow(
                    icon = Icons.Rounded.Timer,
                    label = "睡眠定时关闭",
                    subtitle = "倒计时结束后自动暂停播放",
                    outlineColor = outlineColor,
                    onClick = onOpenSleepTimer,
                )
                HorizontalDivider(thickness = 0.6.dp, color = outlineColor)
                SettingsActionIconRow(
                    icon = Icons.Rounded.RestartAlt,
                    label = "恢复默认外观",
                    subtitle = "重置为宣纸书卷风默认排印",
                    outlineColor = outlineColor,
                    onClick = onResetAppearance,
                )
            }

            // Card 6: 关于
            SettingsSectionHeader("关于")
            BookSettingsCard(cardBg = cardBg, outlineColor = outlineColor, radius = cardRadius) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "简约音乐 (PureMusic)",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(0.6.dp, outlineColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 9.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = "v8.0.0",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "古典书籍复古版 · 专注纯粹离线音乐聆听，无广告无追踪。支持断点续播、拔出耳机自动暂停与系统音频焦点管理。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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

    if (showFolderManagerDialog) {
        FolderManagerDialog(
            allFolders = allFolders,
            excludedFolders = excludedFolders,
            onToggleFolderExcluded = onToggleFolderExcluded,
            onDismiss = { showFolderManagerDialog = false },
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, top = 2.dp),
    )
}

@Composable
private fun BookSettingsCard(
    cardBg: Color,
    outlineColor: Color,
    radius: androidx.compose.ui.unit.Dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(radius)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(cardBg)
            .border(0.75.dp, outlineColor, shape)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        content = content,
    )
}

@Composable
private fun SegmentedOptionBar(
    items: List<String>,
    selectedIndex: Int,
    liquidGlassEnabled: Boolean,
    onSelect: (Int) -> Unit,
) {
    val outerShape = RoundedCornerShape(if (liquidGlassEnabled) 12.dp else 7.dp)
    val innerShape = RoundedCornerShape(if (liquidGlassEnabled) 9.dp else 5.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(outerShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(0.6.dp, MaterialTheme.colorScheme.outline, outerShape)
            .padding(3.dp),
    ) {
        items.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(innerShape)
                    .background(
                        if (selected) {
                            if (liquidGlassEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                        } else {
                            Color.Transparent
                        },
                    )
                    .clickable { onSelect(index) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (selected) {
                        if (liquidGlassEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.background
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun SettingsListRow(
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun SettingsActionIconRow(
    icon: ImageVector,
    label: String,
    subtitle: String,
    outlineColor: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(MaterialTheme.colorScheme.background)
                .border(0.6.dp, outlineColor, RoundedCornerShape(7.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun FolderManagerDialog(
    allFolders: List<MusicFolder>,
    excludedFolders: Set<String>,
    onToggleFolderExcluded: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("管理扫描文件夹") },
        text = {
            if (allFolders.isEmpty()) {
                Text(
                    text = "暂未发现音频文件夹，请先点击“立即重新扫描本地音频”。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    allFolders.forEach { folder ->
                        val included = folder.path !in excludedFolders
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
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
                                modifier = Modifier.size(18.dp),
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
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("完成")
            }
        },
    )
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
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                options.forEach { (value, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelect(value) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = value == selected,
                            onClick = { onSelect(value) },
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}
