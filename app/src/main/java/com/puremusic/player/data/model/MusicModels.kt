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
    SANS("清晰黑体"),
    SERIF("优雅衬线"),
    MONOSPACE("等宽体"),
}

enum class LyricFontStyle(val label: String) {
    SERIF_ITALIC("优雅斜体衬线"),
    SERIF("经典衬线"),
    SANS("清晰黑体"),
    MONOSPACE("等宽体"),
}

enum class BackgroundStyle(val label: String) {
    PURE_WHITE("极光白"),
    DEEP_INK("曜石黑"),
    SYSTEM("跟随系统"),
    WARM_PAPER("柔和暖纸"),
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

enum class AppAccentColor(val label: String, val colorHex: Long) {
    BERRY_RED("极光莓红", 0xFFFA2D48),
    COBALT_BLUE("电光钴蓝", 0xFF2563EB),
    MINT_TEAL("薄荷青", 0xFF0D9488),
    ELECTRIC_VIOLET("霓虹紫", 0xFF7C3AED),
    VIBRANT_ORANGE("流光橘", 0xFFF97316),
    MONOCHROME("极简曜石", 0xFF18181B),
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
