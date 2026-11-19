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

enum class ThemeMode(val label: String) {
    SYSTEM("跟随系统"),
    LIGHT("浅色模式"),
    DARK("深色模式"),
}

enum class AccentPalette(val label: String, val seedColorHex: Long) {
    ORANGE("活力橙", 0xFFE65100),
    OCEAN("深海蓝", 0xFF0277BD),
    EMERALD("翡翠绿", 0xFF2E7D32),
    ROSE("绯红", 0xFFC2185B),
    VIOLET("星河紫", 0xFF6A1B9A),
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
