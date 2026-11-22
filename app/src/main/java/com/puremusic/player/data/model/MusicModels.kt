package com.puremusic.player.data.model

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val durationMs: Long,
    val contentUri: Uri,
    val albumArtUri: Uri,
    val folderName: String,
    val folderPath: String,
    val filePath: String = "",
    val dateAddedSec: Long,
)

data class Album(
    val id: Long,
    val name: String,
    val artist: String,
    val albumArtUri: Uri,
    val songs: List<Song>,
)

data class Artist(
    val name: String,
    val albumCount: Int,
    val songs: List<Song>,
)

data class MusicFolder(
    val name: String,
    val path: String,
    val songs: List<Song>,
)

data class Playlist(
    val id: String,
    val name: String,
    val songIds: List<Long>,
)

data class LyricLine(
    val timeMs: Long,
    val primaryText: String,
    val secondaryText: String? = null,
)

data class ScanStatus(
    val isScanning: Boolean = false,
    val message: String? = null,
)

enum class RepeatMode {
    OFF,
    ALL,
    ONE,
}

enum class SortOrder(val label: String) {
    TITLE("按歌名"),
    ARTIST("按歌手"),
    DURATION("按时长"),
    DATE_ADDED("按添加时间"),
}

enum class PlayerTheme(val label: String) {
    POSTER_WALL("唱片海报墙"),
}

enum class AppFontStyle(val label: String) {
    SERIF("优雅衬线"),
    SERIF_ITALIC("宋体微斜"),
    WENKAI("霞鹜文楷"),
    PLAYFAIR("Playfair"),
    INTER("Inter"),
    SANS("清晰黑体"),
    MONOSPACE("等宽体"),
    CUSTOM("自定义字体"),
}

enum class LyricFontStyle(val label: String) {
    SERIF_ITALIC("宋体微斜"),
    WENKAI("霞鹜文楷"),
    PLAYFAIR("Playfair"),
    SERIF("经典衬线"),
    SANS("清晰黑体"),
    MONOSPACE("等宽体"),
    CUSTOM("自定义字体"),
}

enum class BackgroundStyle(val label: String) {
    WARM_PAPER("宣纸浅色"),
    DEEP_INK("夜读深色"),
    SYSTEM("跟随系统"),
    PURE_WHITE("素白纸"),
}

enum class LayoutMode(val label: String) {
    AUTO("自动"),
    PHONE("手机"),
    TABLET("平板"),
}

enum class MinDurationFilter(val label: String, val minMs: Long) {
    NONE("不过滤", 0L),
    SEC_30("过滤 <30秒", 30_000L),
    SEC_60("过滤 <60秒", 60_000L),
}

enum class LibraryTab(val label: String) {
    SONGS("歌曲"),
    ALBUMS("专辑"),
    ARTISTS("歌手"),
    FOLDERS("文件夹"),
    PLAYLISTS("歌单"),
}

data class EqualizerBand(
    val index: Int,
    val centerFreqHz: Int,
    val levelMb: Int,
)

data class EqualizerState(
    val available: Boolean = false,
    val enabled: Boolean = false,
    val minBandLevelMb: Int = -1500,
    val maxBandLevelMb: Int = 1500,
    val bands: List<EqualizerBand> = emptyList(),
    val presets: List<String> = emptyList(),
    val currentPreset: Int = -1,
)

data class PlayerUiState(
    val currentSong: Song? = null,
    val queue: List<Song> = emptyList(),
    val currentQueueIndex: Int = -1,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
)

enum class AppAccentColor(val label: String, val shortLabel: String, val colorHex: Long) {
    CINNABAR_RED("朱砂印泥", "朱砂", 0xFFA63429),
    PINE_GREEN("松烟黛绿", "黛绿", 0xFF2F5D50),
    PRUSSIAN_BLUE("普鲁士蓝", "普蓝", 0xFF2B4C7E),
    WALNUT_SEPIA("胡桃褐", "胡桃", 0xFF7A5438),
    ANTIQUE_OCHRE("秋香赭金", "赭金", 0xFF9E7332),
    PURE_INK("纯墨黑", "纯墨", 0xFF262320),
}

enum class LayoutDensity(
    val label: String,
    val artSizeDp: Int,
    val rowVerticalPaddingDp: Int,
    val gridSpacingDp: Int,
) {
    COMPACT("紧凑", 42, 5, 7),
    STANDARD("标准", 50, 9, 10),
    SPACIOUS("宽松", 58, 14, 14),
}

enum class MainTab(val label: String) {
    LIBRARY("曲库"),
    ALBUMS("专辑"),
    PLAYLISTS("歌单"),
    SETTINGS("设置"),
}

enum class LibrarySubTab(val label: String) {
    SONGS("歌曲"),
    ARTISTS("歌手"),
    FOLDERS("文件夹"),
}

enum class LiquidGlassLevel(val label: String, val desc: String, val blurDp: Int) {
    LOW("低档", "轻盈薄雾，高可读低开销", 10),
    MEDIUM("中档", "均衡磨砂与温和折射高光", 18),
    HIGH("高档", "强通透立体玻璃与双折射描边", 26),
    FULL("完全", "平时无硬边，按住后边缘泛起微光", 34),
}

enum class MiniPlayerStyle(val label: String) {
    CLASSIC_BOOK("极简书页"),
    VINYL_DISC("黑胶唱片"),
    FLOATING_PILL("悬浮胶囊"),
}

enum class AppFontWeight(val label: String, val weightValue: Int) {
    LIGHT("纤细", 300),
    NORMAL("常规", 400),
    MEDIUM("适中", 500),
    SEMIBOLD("加粗", 700),
}

data class AudioTechSpec(
    val format: String = "",
    val sampleRateHz: Int = 0,
    val bitrateKbps: Int = 0,
    val isHiRes: Boolean = false,
) {
    val displayText: String
        get() = buildList {
            if (format.isNotBlank()) add(format)
            if (sampleRateHz > 0) {
                val khz = sampleRateHz / 1000f
                val khzStr = if (sampleRateHz % 1000 == 0) "${sampleRateHz / 1000}kHz" else String.format("%.1fkHz", khz)
                add(khzStr)
            }
            if (bitrateKbps > 0) add("${bitrateKbps}kbps")
        }.joinToString(" · ")
}

enum class ImmersionLyricStyle(val label: String) {
    ARCHITECTURAL_GRID("建筑网格"),
    VINYL_STUDY("黑胶书房"),
    SPATIAL_GALLERY("空间唱片廊"),
    VINYL_LIGHTFIELD("黑胶光场"),
}

enum class SpectrumStyle(val label: String) {
    SILK_RIBBON("丝带微胶囊"),
    HARMONIC_STRINGS("极简弦波"),
    DOT_MATRIX("点阵峰标"),
    INK_MOUNTAIN("水墨声山"),
}
