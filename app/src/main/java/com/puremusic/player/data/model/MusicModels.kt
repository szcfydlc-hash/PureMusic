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
    VERMILION_CREAM("朱橘奶油"),
    WARM_WOOD("暖木唱片室"),
    POSTER_WALL("唱片海报墙"),
    RETRO_DECK("复古音响"),
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
    WARM_PAPER("暖纸"),
    PURE_WHITE("纯白"),
    DEEP_INK("深墨"),
    SYSTEM("跟随系统"),
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
