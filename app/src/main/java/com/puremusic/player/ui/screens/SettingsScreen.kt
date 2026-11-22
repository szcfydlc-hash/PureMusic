package com.puremusic.player.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.rounded.Dock
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.puremusic.player.ui.theme.resolveLyricFontFamily
import com.puremusic.player.ui.theme.resolveAppFontFamily
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.rounded.FolderCopy
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.puremusic.player.data.model.AppAccentColor
import com.puremusic.player.data.model.AppFontStyle
import com.puremusic.player.data.model.AppFontWeight
import com.puremusic.player.data.model.BackgroundStyle
import com.puremusic.player.data.model.LayoutDensity
import com.puremusic.player.data.model.LayoutMode
import com.puremusic.player.data.model.LiquidGlassLevel
import com.puremusic.player.data.model.LyricFontStyle
import com.puremusic.player.data.model.MinDurationFilter
import com.puremusic.player.data.model.MiniPlayerStyle
import com.puremusic.player.data.model.MusicFolder
import com.puremusic.player.data.model.ScanStatus
import com.puremusic.player.ui.components.liquidGlass
import kotlin.math.roundToInt

private enum class SettingsPage(val title: String) {
    HUB("设置"),
    APPEARANCE("外观与纸色"),
    TYPOGRAPHY("排印与布局"),
    AUDIO("播放与音效"),
    LIBRARY("曲库与备份"),
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
    layoutDensity: LayoutDensity,
    fontScale: Float,
    fontWeight: AppFontWeight,
    fontWeightValue: Int = fontWeight.weightValue,
    fontSlantDegrees: Float,
    playbackSpeed: Float,
    fadeInSeconds: Int,
    gaplessEnabled: Boolean = true,
    hapticEnabled: Boolean = true,
    carModeEnabled: Boolean = false,
    keepScreenOn: Boolean,
    liquidGlassEnabled: Boolean,
    floatingControlsEnabled: Boolean,
    liquidGlassLevel: LiquidGlassLevel,
    miniPlayerStyle: MiniPlayerStyle,
    miniPlayerHeightDp: Int,
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
    onSelectFontWeight: (AppFontWeight) -> Unit,
    onChangeFontWeightValue: (Int) -> Unit = {},
    onChangeFontSlantDegrees: (Float) -> Unit,
    onSelectPlaybackSpeed: (Float) -> Unit,
    onSelectFadeInSeconds: (Int) -> Unit,
    onToggleGapless: (Boolean) -> Unit = {},
    onToggleHaptic: (Boolean) -> Unit = {},
    onToggleCarMode: (Boolean) -> Unit = {},
    onToggleKeepScreenOn: (Boolean) -> Unit,
    onToggleLiquidGlass: (Boolean) -> Unit,
    onToggleFloatingControls: (Boolean) -> Unit,
    onSelectLiquidGlassLevel: (LiquidGlassLevel) -> Unit,
    onSelectMiniPlayerStyle: (MiniPlayerStyle) -> Unit,
    onChangeMiniPlayerHeightDp: (Int) -> Unit,
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
    onExportBackup: () -> Unit = {},
    onImportBackup: () -> Unit = {},
    customFontName: String? = null,
    onImportCustomFont: () -> Unit = {},
    onClearCustomFont: () -> Unit = {},
) {
    BackHandler(onBack = onBackToLibrary)

    var showAppFontDialog by remember { mutableStateOf(false) }
    var showLyricFontDialog by remember { mutableStateOf(false) }
    var showBackgroundDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showFadeInDialog by remember { mutableStateOf(false) }
    var showFolderManagerDialog by remember { mutableStateOf(false) }
    var glassLevelMenuExpanded by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var activeAnchor by remember { mutableStateOf(0) }

    val settingsContext = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val canvasBg = MaterialTheme.colorScheme.background
    val cardBg = MaterialTheme.colorScheme.surface
    val outlineColor = MaterialTheme.colorScheme.outline
    val cardRadius = if (liquidGlassEnabled) 18.dp else 9.dp
    val switchColors = SwitchDefaults.colors(
        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
        checkedTrackColor = MaterialTheme.colorScheme.primary,
        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
        uncheckedBorderColor = outlineColor,
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = canvasBg,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 680.dp)
                .statusBarsPadding(),
        ) {
            // Sticky Header + 4-Pill Quick-Jump Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (isTabletLayout) {
                        IconButton(onClick = onBackToLibrary) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "返回曲库")
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = "设置",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                val quickTabs = listOf("外观" to 0, "排印" to 1, "曲库" to 2, "音效" to 3)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    quickTabs.forEach { (label, idx) ->
                        val selected = activeAnchor == idx
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                )
                                .border(
                                    width = if (selected) 0.dp else 0.75.dp,
                                    color = if (selected) Color.Transparent else outlineColor,
                                    shape = RoundedCornerShape(16.dp),
                                )
                                .clickable {
                                    activeAnchor = idx
                                    coroutineScope.launch { listState.animateScrollToItem(idx) }
                                }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 6.dp,
                    bottom = 24.dp + bottomContentPadding.calculateBottomPadding(),
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item(key = "sec_appearance") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Card 2: 悬浮与液态玻璃 (with live press-testable crystal glass preview!)
            SettingsSectionHeader("悬浮与液态玻璃")
            BookSettingsCard(
                cardBg = cardBg,
                outlineColor = outlineColor,
                radius = cardRadius,
                liquidGlassEnabled = false,
            ) {
                // Interactive Live Preview Stage showing exact translucency, floating shape & FULL press-glow
                val previewInteraction = remember { MutableInteractionSource() }
                val previewPressed by previewInteraction.collectIsPressedAsState()
                val previewShape = RoundedCornerShape(if (floatingControlsEnabled) 26.dp else 8.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(74.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    // Subtle archival text stripes behind the preview capsule so translucency is unmistakably visible!
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "曲库 · 宣纸排印底层透出预览 · 515 首音乐",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                            maxLines = 1,
                        )
                        Text(
                            text = "夜航星 · 光影之间 · 爱在深秋 · 180g 黑胶",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                            maxLines = 1,
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = if (floatingControlsEnabled) 18.dp else 6.dp)
                            .height(46.dp)
                            .liquidGlass(
                                enabled = liquidGlassEnabled,
                                shape = previewShape,
                                isPressed = previewPressed,
                                levelOverride = liquidGlassLevel,
                            )
                            .clickable(
                                interactionSource = previewInteraction,
                                indication = null,
                                onClick = {},
                            )
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = if (liquidGlassEnabled) {
                                "液态玻璃 · ${liquidGlassLevel.label}"
                            } else {
                                "古典书页实底模式"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = if (liquidGlassEnabled && liquidGlassLevel == LiquidGlassLevel.FULL) {
                                "按住测试微光"
                            } else if (floatingControlsEnabled) {
                                "悬浮胶囊"
                            } else {
                                "贴边排印"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(thickness = 0.6.dp, color = outlineColor)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Dock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = "悬浮控件",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = "开启后，底部导航栏与迷你播放条变为悬浮胶囊状态",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = floatingControlsEnabled,
                        onCheckedChange = onToggleFloatingControls,
                        colors = switchColors,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(thickness = 0.6.dp, color = outlineColor)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WaterDrop,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = "液态玻璃",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = "关闭呈现纯粹古典书籍纸页感，开启启用折射玻璃",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = liquidGlassEnabled,
                        onCheckedChange = onToggleLiquidGlass,
                        colors = switchColors,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(thickness = 0.6.dp, color = outlineColor)
                Spacer(modifier = Modifier.height(6.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(if (liquidGlassEnabled) 1f else 0.55f)
                            .clickable(enabled = liquidGlassEnabled) { glassLevelMenuExpanded = true }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "液态玻璃档位",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                text = "${liquidGlassLevel.label} · ${liquidGlassLevel.desc}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = liquidGlassLevel.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp),
                        )
                    }

                    DropdownMenu(
                        expanded = glassLevelMenuExpanded,
                        onDismissRequest = { glassLevelMenuExpanded = false },
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
                        tonalElevation = 0.dp,
                        shadowElevation = 10.dp,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(0.75.dp, outlineColor.copy(alpha = 0.55f)),
                    ) {
                        LiquidGlassLevel.entries.forEach { tier ->
                            val selected = tier == liquidGlassLevel
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = if (selected) "✓ ${tier.label}" else tier.label,
                                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        )
                                        Text(
                                            text = tier.desc,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                },
                                onClick = {
                                    glassLevelMenuExpanded = false
                                    onSelectLiquidGlassLevel(tier)
                                },
                            )
                        }
                    }
                }
            }

                    }
                }
                item(key = "sec_typography") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Card 2.5: 迷你播放条 (3 Styles + Custom Height Ruler)
            SettingsSectionHeader("迷你播放条")
            BookSettingsCard(
                cardBg = cardBg,
                outlineColor = outlineColor,
                radius = cardRadius,
                liquidGlassEnabled = false,
            ) {
                Text(
                    text = "播放条款式",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                SegmentedOptionBar(
                    items = MiniPlayerStyle.entries.map { it.label },
                    selectedIndex = MiniPlayerStyle.entries.indexOf(miniPlayerStyle),
                    liquidGlassEnabled = liquidGlassEnabled,
                    onSelect = { idx -> onSelectMiniPlayerStyle(MiniPlayerStyle.entries[idx]) },
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
                        text = "播放条高度",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${miniPlayerHeightDp}dp",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("纤薄", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    VintageRulerSlider(
                        value = miniPlayerHeightDp.toFloat(),
                        minValue = 48f,
                        maxValue = 68f,
                        steps = 10,
                        onValueChange = { onChangeMiniPlayerHeightDp(it.roundToInt()) },
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp),
                    )
                    Text("舒展", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Card 3: 外观与排版
            SettingsSectionHeader("外观与排版")
            BookSettingsCard(
                cardBg = cardBg,
                outlineColor = outlineColor,
                radius = cardRadius,
                liquidGlassEnabled = liquidGlassEnabled,
            ) {
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

                // Custom Vintage Precision Ruler Font-Scale Slider (zero clunky M3 purple bar!)
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
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "A-",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    VintageRulerSlider(
                        value = fontScale,
                        minValue = 0.85f,
                        maxValue = 1.25f,
                        steps = 8,
                        onValueChange = onChangeFontScale,
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp),
                    )
                    Text(
                        text = "A+",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }


            // Card 4: 字体与纸色
            SettingsSectionHeader("字体与纸色")
            BookSettingsCard(
                cardBg = cardBg,
                outlineColor = outlineColor,
                radius = cardRadius,
                liquidGlassEnabled = liquidGlassEnabled,
            ) {
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onImportCustomFont)
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "导入本地自定义字体",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = customFontName ?: "支持导入任意 .ttf / .otf 字体文件",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (customFontName != null) {
                            Text(
                                text = "清除",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable(onClick = onClearCustomFont)
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                            )
                        }
                        Text(
                            text = if (customFontName != null) "更换 ›" else "导入 ›",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                HorizontalDivider(thickness = 0.6.dp, color = outlineColor)
                SettingsListRow(
                    label = "背景纸色",
                    value = backgroundStyle.label,
                    onClick = { showBackgroundDialog = true },
                )
                HorizontalDivider(thickness = 0.6.dp, color = outlineColor)
                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                ) {
                    Text(
                        text = "山海自有归期，风雨自有相逢",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${fontWeight.label}($fontWeightValue) · ${(fontScale * 100).toInt()}% · 倾斜 ${fontSlantDegrees.roundToInt()}°",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "字体粗细",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                SegmentedOptionBar(
                    items = AppFontWeight.entries.map { it.label },
                    selectedIndex = AppFontWeight.entries.indexOf(fontWeight),
                    liquidGlassEnabled = liquidGlassEnabled,
                    onSelect = { idx -> onSelectFontWeight(AppFontWeight.entries[idx]) },
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "自定义字重微调",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "$fontWeightValue",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("200", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    VintageRulerSlider(
                        value = fontWeightValue.toFloat(),
                        minValue = 200f,
                        maxValue = 900f,
                        steps = 14,
                        onValueChange = { onChangeFontWeightValue(it.roundToInt()) },
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp),
                    )
                    Text("900", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(thickness = 0.6.dp, color = outlineColor)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "字体倾斜度",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = if (fontSlantDegrees < 0.5f) "0° (直立)" else "${fontSlantDegrees.roundToInt()}°",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("0°", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    VintageRulerSlider(
                        value = fontSlantDegrees,
                        minValue = 0f,
                        maxValue = 16f,
                        steps = 8,
                        onValueChange = onChangeFontSlantDegrees,
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp),
                    )
                    Text("16°", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

                    }
                }
                item(key = "sec_library") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Card 5: 曲库管理
            SettingsSectionHeader("曲库管理")
            BookSettingsCard(
                cardBg = cardBg,
                outlineColor = outlineColor,
                radius = cardRadius,
                liquidGlassEnabled = liquidGlassEnabled,
            ) {
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
                            .clip(RoundedCornerShape(6.dp))
                            .background(pine.copy(alpha = if (isDark) 0.24f else 0.10f))
                            .border(0.6.dp, pine.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
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
                Spacer(modifier = Modifier.height(8.dp))
                val chipShape = RoundedCornerShape(6.dp)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MinDurationFilter.entries.forEach { filter ->
                        val selected = minDurationFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(chipShape)
                                .background(
                                    if (selected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.background
                                    },
                                )
                                .border(
                                    width = 0.75.dp,
                                    color = if (selected) MaterialTheme.colorScheme.primary else outlineColor,
                                    shape = chipShape,
                                )
                                .clickable { onSelectMinDurationFilter(filter) }
                                .padding(horizontal = 13.dp, vertical = 7.dp),
                        ) {
                            Text(
                                text = filter.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(thickness = 0.6.dp, color = outlineColor)

                val enabledCount = (allFolders.size - excludedFolders.size).coerceAtLeast(0)
                SettingsListRow(
                    label = "管理扫描文件夹",
                    value = "已启用 $enabledCount / ${allFolders.size}",
                    onClick = { showFolderManagerDialog = true },
                )
                HorizontalDivider(thickness = 0.6.dp, color = outlineColor)
                SettingsListRow(
                    label = "导出歌单与收藏备份",
                    value = "JSON",
                    onClick = onExportBackup,
                )
                HorizontalDivider(thickness = 0.6.dp, color = outlineColor)
                SettingsListRow(
                    label = "从备份恢复歌单与收藏",
                    value = "导入",
                    onClick = onImportBackup,
                )
            }

                    }
                }
                item(key = "sec_audio") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Card 6: 播放与音效
            SettingsSectionHeader("播放与音效")
            BookSettingsCard(
                cardBg = cardBg,
                outlineColor = outlineColor,
                radius = cardRadius,
                liquidGlassEnabled = liquidGlassEnabled,
            ) {
                SettingsActionIconRow(
                    icon = Icons.Rounded.Equalizer,
                    label = "音频均衡器",
                    subtitle = "复古 Hi-Fi 5 段竖推调校与预设",
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
                SettingsListRow(
                    label = "播放倍速",
                    value = "${playbackSpeed}×",
                    onClick = { showSpeedDialog = true },
                )
                HorizontalDivider(thickness = 0.6.dp, color = outlineColor)
                SettingsListRow(
                    label = "切歌交叉淡化 (Crossfade)",
                    value = if (fadeInSeconds <= 0) "关闭" else "${fadeInSeconds} 秒",
                    onClick = { showFadeInDialog = true },
                )
                HorizontalDivider(thickness = 0.6.dp, color = outlineColor)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = "无缝衔接播放 (Gapless)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = "预缓冲音轨消除现场/组曲切歌停顿",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = gaplessEnabled,
                        onCheckedChange = onToggleGapless,
                        colors = switchColors,
                    )
                }
                HorizontalDivider(thickness = 0.6.dp, color = outlineColor)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = "细腻触觉反馈",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = "切歌、进度滑块与长按提供轻微震动质感",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = hapticEnabled,
                        onCheckedChange = onToggleHaptic,
                        colors = switchColors,
                    )
                }
                HorizontalDivider(thickness = 0.6.dp, color = outlineColor)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = "极简车机模式",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = "高对比夜墨大字界面与盲操级切歌大圆钮",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = carModeEnabled,
                        onCheckedChange = onToggleCarMode,
                        colors = switchColors,
                    )
                }
                HorizontalDivider(thickness = 0.6.dp, color = outlineColor)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = "播放页屏幕常亮",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = "欣赏黑胶与 3 行动态字幕时不自动息屏",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = keepScreenOn,
                        onCheckedChange = onToggleKeepScreenOn,
                        colors = switchColors,
                    )
                }
                HorizontalDivider(thickness = 0.6.dp, color = outlineColor)
                SettingsActionIconRow(
                    icon = Icons.Rounded.RestartAlt,
                    label = "恢复默认外观",
                    subtitle = "重置为宣纸书卷风默认排印",
                    outlineColor = outlineColor,
                    onClick = onResetAppearance,
                )
            }


            // Card 7: 关于
            SettingsSectionHeader("关于")
            BookSettingsCard(
                cardBg = cardBg,
                outlineColor = outlineColor,
                radius = cardRadius,
                liquidGlassEnabled = liquidGlassEnabled,
            ) {
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
                            text = "v2.0.1",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "回归音乐本身。纯粹、克制、安静陪伴每一张本地唱片。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
                )
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = outlineColor.copy(alpha = 0.55f),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "PURE MUSIC  ·  本地离线  ·  零广告  ·  原生引擎",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                )
                    }
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
            previewFontStyle = { option ->
                val fam = resolveAppFontFamily(settingsContext, option)
                val st = if (option == AppFontStyle.SERIF_ITALIC || option == AppFontStyle.PLAYFAIR) FontStyle.Italic else FontStyle.Normal
                fam to st
            },
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
            previewFontStyle = { option ->
                val fam = resolveLyricFontFamily(settingsContext, option)
                val st = if (option == LyricFontStyle.SERIF_ITALIC || option == LyricFontStyle.PLAYFAIR) FontStyle.Italic else FontStyle.Normal
                fam to st
            },
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

    if (showSpeedDialog) {
        OptionPickerDialog(
            title = "选择播放倍速",
            options = listOf(0.75f to "0.75× (舒缓)", 1.0f to "1.0× (原速)", 1.25f to "1.25×", 1.5f to "1.5×", 2.0f to "2.0×"),
            selected = playbackSpeed,
            onSelect = {
                onSelectPlaybackSpeed(it)
                showSpeedDialog = false
            },
            onDismiss = { showSpeedDialog = false },
        )
    }

    if (showFadeInDialog) {
        OptionPickerDialog(
            title = "切歌柔和淡入时长",
            options = listOf(0 to "关闭", 1 to "1 秒", 2 to "2 秒", 4 to "4 秒"),
            selected = fadeInSeconds,
            onSelect = {
                onSelectFadeInSeconds(it)
                showFadeInDialog = false
            },
            onDismiss = { showFadeInDialog = false },
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
private fun VintageRulerSlider(
    value: Float,
    minValue: Float,
    maxValue: Float,
    steps: Int,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    var widthPx by remember { mutableFloatStateOf(1f) }
    val trackColor = MaterialTheme.colorScheme.outline
    val activeColor = MaterialTheme.colorScheme.primary
    val knobFill = MaterialTheme.colorScheme.surface
    val knobStroke = MaterialTheme.colorScheme.onSurface
    val range = (maxValue - minValue).coerceAtLeast(0.01f)
    val fraction = ((value - minValue) / range).coerceIn(0f, 1f)

    fun snapFromX(x: Float) {
        val rawFrac = (x / widthPx).coerceIn(0f, 1f)
        val snappedFrac = if (steps > 0) {
            (rawFrac * steps).roundToInt().toFloat() / steps.toFloat()
        } else {
            rawFrac
        }
        onValueChange((minValue + snappedFrac * range).coerceIn(minValue, maxValue))
    }

    Box(
        modifier = modifier
            .onSizeChanged { widthPx = it.width.toFloat().coerceAtLeast(1f) }
            .pointerInput(minValue, maxValue, steps) {
                detectTapGestures { offset -> snapFromX(offset.x) }
            }
            .pointerInput(minValue, maxValue, steps) {
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    snapFromX(change.position.x)
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cy = size.height * 0.42f
            val padX = 12f
            val usableW = (size.width - padX * 2f).coerceAtLeast(1f)
            val thumbX = padX + usableW * fraction

            // Precision ruler ticks below the hairline track
            for (i in 0..steps) {
                val tx = padX + usableW * (i.toFloat() / steps.toFloat())
                val isMajor = i == 0 || i == steps || i == steps / 2
                drawLine(
                    color = trackColor.copy(alpha = if (isMajor) 0.95f else 0.55f),
                    start = Offset(tx, cy + 9f),
                    end = Offset(tx, cy + if (isMajor) 17f else 14f),
                    strokeWidth = if (isMajor) 1.5f else 1.0f,
                )
            }

            // Thin inactive rail
            drawLine(
                color = trackColor,
                start = Offset(padX, cy),
                end = Offset(padX + usableW, cy),
                strokeWidth = 3.6f,
                cap = StrokeCap.Round,
            )

            // Active accent rail
            drawLine(
                color = activeColor,
                start = Offset(padX, cy),
                end = Offset(thumbX, cy),
                strokeWidth = 4.2f,
                cap = StrokeCap.Round,
            )

            // Precision vintage circular knob
            drawCircle(
                color = knobFill,
                radius = 12f,
                center = Offset(thumbX, cy),
            )
            drawCircle(
                color = knobStroke.copy(alpha = 0.85f),
                radius = 12f,
                center = Offset(thumbX, cy),
                style = Stroke(width = 2.0f),
            )
            drawCircle(
                color = activeColor,
                radius = 4f,
                center = Offset(thumbX, cy),
            )
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Row(
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun BookSettingsCard(
    cardBg: Color,
    outlineColor: Color,
    radius: androidx.compose.ui.unit.Dp,
    liquidGlassEnabled: Boolean,
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
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.background)
                .border(0.6.dp, outlineColor, RoundedCornerShape(6.dp)),
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
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(12.dp),
        title = { Text("管理扫描文件夹", fontWeight = FontWeight.SemiBold) },
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
    previewFontStyle: ((T) -> Pair<FontFamily, FontStyle>)? = null,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(12.dp),
        title = { Text(title, fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                options.forEach { (value, label) ->
                    val isSelected = value == selected
                    val customFont = previewFontStyle?.invoke(value)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
                            )
                            .clickable { onSelect(value) }
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onSelect(value) },
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label,
                            style = if (customFont != null) {
                                MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = customFont.first,
                                    fontStyle = customFont.second,
                                )
                            } else {
                                MaterialTheme.typography.bodyMedium
                            },
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        )
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
