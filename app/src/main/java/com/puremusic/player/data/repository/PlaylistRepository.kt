package com.puremusic.player.data.repository

import java.util.Calendar
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.first
import com.puremusic.player.data.model.AppAccentColor
import com.puremusic.player.data.model.AppFontStyle
import com.puremusic.player.data.model.AppFontWeight
import com.puremusic.player.data.model.BackgroundStyle
import com.puremusic.player.data.model.LayoutDensity
import com.puremusic.player.data.model.LayoutMode
import com.puremusic.player.data.model.LiquidGlassLevel
import com.puremusic.player.data.model.ImmersionLyricStyle
import com.puremusic.player.data.model.KaraokeLyricMode
import com.puremusic.player.data.model.LyricTranslationEngine
import com.puremusic.player.data.model.TranslationSettings
import com.puremusic.player.data.model.SpectrumStyle
import com.puremusic.player.data.model.LyricFontStyle
import com.puremusic.player.data.model.MinDurationFilter
import com.puremusic.player.data.model.MiniPlayerStyle
import com.puremusic.player.data.model.PlayerTheme
import com.puremusic.player.data.model.Playlist
import com.puremusic.player.data.model.SortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private val Context.pureMusicDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "puremusic_preferences",
)

data class PlaybackSnapshot(
    val lastSongId: Long? = null,
    val lastPositionMs: Long = 0L,
    val queueIds: List<Long> = emptyList(),
)

class PlaylistRepository(private val context: Context) {

    private object Keys {
        val PLAYLISTS_JSON = stringPreferencesKey("playlists_json")
        val FAVORITE_SONG_IDS = stringSetPreferencesKey("favorite_song_ids")
        val PLAYER_THEME = stringPreferencesKey("player_theme")
        val FONT_STYLE = stringPreferencesKey("font_style")
        val LYRIC_FONT_STYLE = stringPreferencesKey("lyric_font_style")
        val BACKGROUND_STYLE = stringPreferencesKey("background_style")
        val LAYOUT_MODE = stringPreferencesKey("layout_mode")
        val LAYOUT_DENSITY = stringPreferencesKey("layout_density")
        val FONT_SCALE = floatPreferencesKey("font_scale")
        val LIQUID_GLASS_ENABLED = booleanPreferencesKey("liquid_glass_enabled")
        val FLOATING_CONTROLS_ENABLED = booleanPreferencesKey("floating_controls_enabled")
        val LIQUID_GLASS_LEVEL = stringPreferencesKey("liquid_glass_level")
        val MINI_PLAYER_STYLE = stringPreferencesKey("mini_player_style")
        val MINI_PLAYER_HEIGHT_DP = intPreferencesKey("mini_player_height_dp")
        val FONT_WEIGHT = stringPreferencesKey("font_weight")
        val FONT_WEIGHT_VALUE = intPreferencesKey("font_weight_value")
        val FONT_SLANT_DEGREES = floatPreferencesKey("font_slant_degrees")
        val PLAYBACK_SPEED = floatPreferencesKey("playback_speed")
        val FADE_IN_SECONDS = intPreferencesKey("fade_in_seconds")
        val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        val VOLUME_NORMALIZATION = booleanPreferencesKey("volume_normalization")
        val CAR_MODE_ENABLED = booleanPreferencesKey("car_mode_enabled")
        val IMMERSION_STYLE = stringPreferencesKey("immersion_style")
        val SPECTRUM_STYLE = stringPreferencesKey("spectrum_style")
        val KARAOKE_LYRIC_MODE = stringPreferencesKey("karaoke_lyric_mode")
        val TRANSLATION_ENGINE = stringPreferencesKey("translation_engine")
        val TRANSLATE_BASE_URL = stringPreferencesKey("translate_base_url")
        val TRANSLATE_API_KEY = stringPreferencesKey("translate_api_key")
        val TRANSLATE_MODEL = stringPreferencesKey("translate_model")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val RECENT_SONG_IDS = stringPreferencesKey("recent_song_ids")
        val PLAY_COUNTS_JSON = stringPreferencesKey("play_counts_json")
        val HOURLY_PLAYS_JSON = stringPreferencesKey("hourly_plays_json")
        val LYRIC_OFFSETS_JSON = stringPreferencesKey("lyric_offsets_json")
        val EXCLUDED_FOLDERS = stringSetPreferencesKey("excluded_folders")
        val MIN_DURATION_FILTER = stringPreferencesKey("min_duration_filter")
        val ACCENT_COLOR = stringPreferencesKey("accent_color")
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val LAST_SONG_ID = longPreferencesKey("last_song_id")
        val LAST_POSITION_MS = longPreferencesKey("last_position_ms")
        val LAST_QUEUE_IDS = stringPreferencesKey("last_queue_ids")
        val PREF_STYLE_EPOCH = intPreferencesKey("pref_style_epoch")
    }

    val playlistsFlow: Flow<List<Playlist>> = context.pureMusicDataStore.data.map { prefs ->
        decodePlaylists(prefs[Keys.PLAYLISTS_JSON].orEmpty())
    }

    val favoriteSongIdsFlow: Flow<Set<Long>> = context.pureMusicDataStore.data.map { prefs ->
        prefs[Keys.FAVORITE_SONG_IDS].orEmpty().mapNotNull { it.toLongOrNull() }.toSet()
    }

    val playerThemeFlow: Flow<PlayerTheme> = context.pureMusicDataStore.data.map { prefs ->
        prefs[Keys.PLAYER_THEME]?.let { name ->
            PlayerTheme.entries.firstOrNull { it.name == name }
        } ?: PlayerTheme.POSTER_WALL
    }

    val fontStyleFlow: Flow<AppFontStyle> = context.pureMusicDataStore.data.map { prefs ->
        prefs[Keys.FONT_STYLE]?.let { name ->
            AppFontStyle.entries.firstOrNull { it.name == name }
        } ?: AppFontStyle.SANS
    }

    val lyricFontStyleFlow: Flow<LyricFontStyle> = context.pureMusicDataStore.data.map { prefs ->
        prefs[Keys.LYRIC_FONT_STYLE]?.let { name ->
            LyricFontStyle.entries.firstOrNull { it.name == name }
        } ?: LyricFontStyle.SANS
    }

    val backgroundStyleFlow: Flow<BackgroundStyle> = context.pureMusicDataStore.data.map { prefs ->
        prefs[Keys.BACKGROUND_STYLE]?.let { name ->
            BackgroundStyle.entries.firstOrNull { it.name == name }
        } ?: BackgroundStyle.WARM_PAPER
    }

    val layoutDensityFlow: Flow<LayoutDensity> = context.pureMusicDataStore.data.map { prefs ->
        prefs[Keys.LAYOUT_DENSITY]?.let { name ->
            LayoutDensity.entries.firstOrNull { it.name == name }
        } ?: LayoutDensity.STANDARD
    }

    val fontScaleFlow: Flow<Float> = context.pureMusicDataStore.data.map { prefs ->
        (prefs[Keys.FONT_SCALE] ?: 1.0f).coerceIn(0.85f, 1.25f)
    }

    val layoutModeFlow: Flow<LayoutMode> = context.pureMusicDataStore.data.map { prefs ->
        prefs[Keys.LAYOUT_MODE]?.let { name ->
            LayoutMode.entries.firstOrNull { it.name == name }
        } ?: LayoutMode.AUTO
    }

    val liquidGlassEnabledFlow: Flow<Boolean> = context.pureMusicDataStore.data.map { prefs ->
        prefs[Keys.LIQUID_GLASS_ENABLED] ?: false
    }

    val floatingControlsEnabledFlow: Flow<Boolean> = context.pureMusicDataStore.data.map { prefs ->
        prefs[Keys.FLOATING_CONTROLS_ENABLED] ?: false
    }

    val liquidGlassLevelFlow: Flow<LiquidGlassLevel> = context.pureMusicDataStore.data.map { prefs ->
        prefs[Keys.LIQUID_GLASS_LEVEL]?.let { name ->
            LiquidGlassLevel.entries.firstOrNull { it.name == name }
        } ?: LiquidGlassLevel.FULL
    }

    val miniPlayerStyleFlow: Flow<MiniPlayerStyle> = context.pureMusicDataStore.data.map { prefs ->
        prefs[Keys.MINI_PLAYER_STYLE]?.let { name ->
            MiniPlayerStyle.entries.firstOrNull { it.name == name }
        } ?: MiniPlayerStyle.CLASSIC_BOOK
    }

    val miniPlayerHeightDpFlow: Flow<Int> = context.pureMusicDataStore.data.map { prefs ->
        (prefs[Keys.MINI_PLAYER_HEIGHT_DP] ?: 58).coerceIn(48, 68)
    }

    val fontWeightFlow: Flow<AppFontWeight> = context.pureMusicDataStore.data.map { prefs ->
        prefs[Keys.FONT_WEIGHT]?.let { name ->
            AppFontWeight.entries.firstOrNull { it.name == name }
        } ?: AppFontWeight.NORMAL
    }

    val fontWeightValueFlow: Flow<Int> = context.pureMusicDataStore.data.map { prefs ->
        val stored = prefs[Keys.FONT_WEIGHT_VALUE]
        if (stored != null) {
            stored.coerceIn(200, 900)
        } else {
            val fw = prefs[Keys.FONT_WEIGHT]?.let { name ->
                AppFontWeight.entries.firstOrNull { it.name == name }
            } ?: AppFontWeight.NORMAL
            fw.weightValue
        }
    }

    val fontSlantDegreesFlow: Flow<Float> = context.pureMusicDataStore.data.map { prefs ->
        (prefs[Keys.FONT_SLANT_DEGREES] ?: 0f).coerceIn(0f, 16f)
    }

    val playbackSpeedFlow: Flow<Float> = context.pureMusicDataStore.data.map { prefs ->
        (prefs[Keys.PLAYBACK_SPEED] ?: 1.0f).coerceIn(0.5f, 2.0f)
    }

    val fadeInSecondsFlow: Flow<Int> = context.pureMusicDataStore.data.map { prefs ->
        (prefs[Keys.FADE_IN_SECONDS] ?: 0).coerceIn(0, 6)
    }

    val keepScreenOnFlow: Flow<Boolean> = context.pureMusicDataStore.data.map { prefs ->
        prefs[Keys.KEEP_SCREEN_ON] ?: false
    }


    val hapticEnabledFlow: Flow<Boolean> = context.pureMusicDataStore.data.map { prefs ->
        prefs[Keys.HAPTIC_ENABLED] ?: true
    }

    val volumeNormalizationFlow: Flow<Boolean> = context.pureMusicDataStore.data.map { prefs ->
        prefs[Keys.VOLUME_NORMALIZATION] ?: false
    }

    val carModeEnabledFlow: Flow<Boolean> = context.pureMusicDataStore.data.map { prefs ->
        prefs[Keys.CAR_MODE_ENABLED] ?: false
    }

    val immersionStyleFlow: Flow<ImmersionLyricStyle> = context.pureMusicDataStore.data.map { prefs ->
        val raw = prefs[Keys.IMMERSION_STYLE]
        ImmersionLyricStyle.entries.firstOrNull { it.name == raw } ?: ImmersionLyricStyle.VINYL_STUDY
    }

    val spectrumStyleFlow: Flow<SpectrumStyle> = context.pureMusicDataStore.data.map { prefs ->
        val raw = prefs[Keys.SPECTRUM_STYLE]
        SpectrumStyle.entries.firstOrNull { it.name == raw } ?: SpectrumStyle.SILK_RIBBON
    }

    val karaokeLyricModeFlow: Flow<KaraokeLyricMode> = context.pureMusicDataStore.data.map { prefs ->
        val raw = prefs[Keys.KARAOKE_LYRIC_MODE]
        KaraokeLyricMode.entries.firstOrNull { it.name == raw } ?: KaraokeLyricMode.SMART
    }

    val translationSettingsFlow: Flow<TranslationSettings> = context.pureMusicDataStore.data.map { prefs ->
        val engRaw = prefs[Keys.TRANSLATION_ENGINE]
        val engine = LyricTranslationEngine.entries.firstOrNull { it.name == engRaw }
            ?: LyricTranslationEngine.OFFLINE_MLKIT
        TranslationSettings(
            engine = engine,
            customBaseUrl = prefs[Keys.TRANSLATE_BASE_URL].orEmpty(),
            customApiKey = prefs[Keys.TRANSLATE_API_KEY].orEmpty(),
            customModel = prefs[Keys.TRANSLATE_MODEL]?.takeIf { it.isNotBlank() } ?: "gpt-4o-mini",
        )
    }

    val recentSongIdsFlow: Flow<List<Long>> = context.pureMusicDataStore.data.map { prefs ->
        prefs[Keys.RECENT_SONG_IDS]
            .orEmpty()
            .split(",")
            .mapNotNull { it.trim().toLongOrNull() }
    }

    val playCountsFlow: Flow<Map<Long, Int>> = context.pureMusicDataStore.data.map { prefs ->
        decodeLongIntMap(prefs[Keys.PLAY_COUNTS_JSON].orEmpty())
    }

    val hourlyPlaysFlow: Flow<Map<Int, Int>> = context.pureMusicDataStore.data.map { prefs ->
        decodeIntIntMap(prefs[Keys.HOURLY_PLAYS_JSON].orEmpty())
    }

    val lyricOffsetsFlow: Flow<Map<Long, Long>> = context.pureMusicDataStore.data.map { prefs ->
        decodeLongLongMap(prefs[Keys.LYRIC_OFFSETS_JSON].orEmpty())
    }

    val excludedFoldersFlow: Flow<Set<String>> = context.pureMusicDataStore.data.map { prefs ->
        prefs[Keys.EXCLUDED_FOLDERS].orEmpty()
    }

    val minDurationFilterFlow: Flow<MinDurationFilter> = context.pureMusicDataStore.data.map { prefs ->
        prefs[Keys.MIN_DURATION_FILTER]?.let { name ->
            MinDurationFilter.entries.firstOrNull { it.name == name }
        } ?: MinDurationFilter.NONE
    }

    val accentColorFlow: Flow<AppAccentColor> = context.pureMusicDataStore.data.map { prefs ->
        prefs[Keys.ACCENT_COLOR]?.let { name ->
            AppAccentColor.entries.firstOrNull { it.name == name }
        } ?: AppAccentColor.CINNABAR_RED
    }

    val sortOrderFlow: Flow<SortOrder> = context.pureMusicDataStore.data.map { prefs ->
        prefs[Keys.SORT_ORDER]?.let { name ->
            SortOrder.entries.firstOrNull { it.name == name }
        } ?: SortOrder.TITLE
    }

    suspend fun createPlaylist(name: String, initialSongIds: List<Long> = emptyList()) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        context.pureMusicDataStore.edit { prefs ->
            val current = decodePlaylists(prefs[Keys.PLAYLISTS_JSON].orEmpty()).toMutableList()
            current.add(
                Playlist(
                    id = UUID.randomUUID().toString(),
                    name = trimmed,
                    songIds = initialSongIds.distinct(),
                ),
            )
            prefs[Keys.PLAYLISTS_JSON] = encodePlaylists(current)
        }
    }

    suspend fun renamePlaylist(playlistId: String, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        context.pureMusicDataStore.edit { prefs ->
            val updated = decodePlaylists(prefs[Keys.PLAYLISTS_JSON].orEmpty()).map { playlist ->
                if (playlist.id == playlistId) playlist.copy(name = trimmed) else playlist
            }
            prefs[Keys.PLAYLISTS_JSON] = encodePlaylists(updated)
        }
    }

    suspend fun setPlaylistSongs(playlistId: String, songIds: List<Long>) {
        context.pureMusicDataStore.edit { prefs ->
            val updated = decodePlaylists(prefs[Keys.PLAYLISTS_JSON].orEmpty()).map { playlist ->
                if (playlist.id == playlistId) playlist.copy(songIds = songIds.distinct()) else playlist
            }
            prefs[Keys.PLAYLISTS_JSON] = encodePlaylists(updated)
        }
    }

    suspend fun setFavoriteSongIds(songIds: Set<Long>) {
        context.pureMusicDataStore.edit { prefs ->
            prefs[Keys.FAVORITE_SONG_IDS] = songIds.map { it.toString() }.toSet()
        }
    }

    suspend fun deletePlaylist(playlistId: String) {
        context.pureMusicDataStore.edit { prefs ->
            val updated = decodePlaylists(prefs[Keys.PLAYLISTS_JSON].orEmpty())
                .filterNot { it.id == playlistId }
            prefs[Keys.PLAYLISTS_JSON] = encodePlaylists(updated)
        }
    }

    suspend fun addSongToPlaylist(playlistId: String, songId: Long) {
        context.pureMusicDataStore.edit { prefs ->
            val updated = decodePlaylists(prefs[Keys.PLAYLISTS_JSON].orEmpty()).map { playlist ->
                if (playlist.id == playlistId && songId !in playlist.songIds) {
                    playlist.copy(songIds = playlist.songIds + songId)
                } else {
                    playlist
                }
            }
            prefs[Keys.PLAYLISTS_JSON] = encodePlaylists(updated)
        }
    }

    suspend fun removeSongFromPlaylist(playlistId: String, songId: Long) {
        context.pureMusicDataStore.edit { prefs ->
            val updated = decodePlaylists(prefs[Keys.PLAYLISTS_JSON].orEmpty()).map { playlist ->
                if (playlist.id == playlistId) {
                    playlist.copy(songIds = playlist.songIds.filterNot { it == songId })
                } else {
                    playlist
                }
            }
            prefs[Keys.PLAYLISTS_JSON] = encodePlaylists(updated)
        }
    }

    suspend fun toggleFavorite(songId: Long) {
        context.pureMusicDataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITE_SONG_IDS].orEmpty().toMutableSet()
            val key = songId.toString()
            if (key in current) current.remove(key) else current.add(key)
            prefs[Keys.FAVORITE_SONG_IDS] = current
        }
    }

    suspend fun setPlayerTheme(theme: PlayerTheme) {
        context.pureMusicDataStore.edit { it[Keys.PLAYER_THEME] = theme.name }
    }

    suspend fun setFontStyle(style: AppFontStyle) {
        context.pureMusicDataStore.edit { it[Keys.FONT_STYLE] = style.name }
    }

    suspend fun setLyricFontStyle(style: LyricFontStyle) {
        context.pureMusicDataStore.edit { it[Keys.LYRIC_FONT_STYLE] = style.name }
    }

    suspend fun setBackgroundStyle(style: BackgroundStyle) {
        context.pureMusicDataStore.edit { it[Keys.BACKGROUND_STYLE] = style.name }
    }

    suspend fun setLayoutDensity(density: LayoutDensity) {
        context.pureMusicDataStore.edit { it[Keys.LAYOUT_DENSITY] = density.name }
    }

    suspend fun setFontScale(scale: Float) {
        context.pureMusicDataStore.edit { it[Keys.FONT_SCALE] = scale.coerceIn(0.85f, 1.25f) }
    }

    suspend fun setLayoutMode(mode: LayoutMode) {
        context.pureMusicDataStore.edit { it[Keys.LAYOUT_MODE] = mode.name }
    }

    suspend fun setLiquidGlassEnabled(enabled: Boolean) {
        context.pureMusicDataStore.edit { it[Keys.LIQUID_GLASS_ENABLED] = enabled }
    }

    suspend fun setFloatingControlsEnabled(enabled: Boolean) {
        context.pureMusicDataStore.edit { it[Keys.FLOATING_CONTROLS_ENABLED] = enabled }
    }

    suspend fun setLiquidGlassLevel(level: LiquidGlassLevel) {
        context.pureMusicDataStore.edit { it[Keys.LIQUID_GLASS_LEVEL] = level.name }
    }

    suspend fun setMiniPlayerStyle(style: MiniPlayerStyle) {
        context.pureMusicDataStore.edit { it[Keys.MINI_PLAYER_STYLE] = style.name }
    }

    suspend fun setMiniPlayerHeightDp(heightDp: Int) {
        context.pureMusicDataStore.edit { it[Keys.MINI_PLAYER_HEIGHT_DP] = heightDp.coerceIn(48, 68) }
    }

    suspend fun setFontWeight(weight: AppFontWeight) {
        context.pureMusicDataStore.edit {
            it[Keys.FONT_WEIGHT] = weight.name
            it[Keys.FONT_WEIGHT_VALUE] = weight.weightValue
        }
    }

    suspend fun setFontWeightValue(value: Int) {
        val clamped = value.coerceIn(200, 900)
        val closest = AppFontWeight.entries.minByOrNull { kotlin.math.abs(it.weightValue - clamped) } ?: AppFontWeight.NORMAL
        context.pureMusicDataStore.edit {
            it[Keys.FONT_WEIGHT_VALUE] = clamped
            it[Keys.FONT_WEIGHT] = closest.name
        }
    }

    suspend fun setFontSlantDegrees(degrees: Float) {
        context.pureMusicDataStore.edit { it[Keys.FONT_SLANT_DEGREES] = degrees.coerceIn(0f, 16f) }
    }

    suspend fun setPlaybackSpeed(speed: Float) {
        context.pureMusicDataStore.edit { it[Keys.PLAYBACK_SPEED] = speed.coerceIn(0.5f, 2.0f) }
    }

    suspend fun setFadeInSeconds(seconds: Int) {
        context.pureMusicDataStore.edit { it[Keys.FADE_IN_SECONDS] = seconds.coerceIn(0, 6) }
    }

    suspend fun setKeepScreenOn(enabled: Boolean) {
        context.pureMusicDataStore.edit { it[Keys.KEEP_SCREEN_ON] = enabled }
    }


    suspend fun setHapticEnabled(enabled: Boolean) {
        context.pureMusicDataStore.edit { it[Keys.HAPTIC_ENABLED] = enabled }
    }

    suspend fun setCarModeEnabled(enabled: Boolean) {
        context.pureMusicDataStore.edit { it[Keys.CAR_MODE_ENABLED] = enabled }
    }

    suspend fun setImmersionStyle(style: ImmersionLyricStyle) {
        context.pureMusicDataStore.edit { it[Keys.IMMERSION_STYLE] = style.name }
    }

    suspend fun setSpectrumStyle(style: SpectrumStyle) {
        context.pureMusicDataStore.edit { it[Keys.SPECTRUM_STYLE] = style.name }
    }

    suspend fun setKaraokeLyricMode(mode: KaraokeLyricMode) {
        context.pureMusicDataStore.edit { it[Keys.KARAOKE_LYRIC_MODE] = mode.name }
    }

    suspend fun recordSongPlayed(songId: Long) {
        context.pureMusicDataStore.edit { prefs ->
            val currentRecent = prefs[Keys.RECENT_SONG_IDS]
                .orEmpty()
                .split(",")
                .mapNotNull { it.trim().toLongOrNull() }
                .filterNot { it == songId }
            val updatedRecent = (listOf(songId) + currentRecent).take(50)
            prefs[Keys.RECENT_SONG_IDS] = updatedRecent.joinToString(",")

            val counts = decodeLongIntMap(prefs[Keys.PLAY_COUNTS_JSON].orEmpty()).toMutableMap()
            counts[songId] = (counts[songId] ?: 0) + 1
            prefs[Keys.PLAY_COUNTS_JSON] = encodeLongIntMap(counts)

            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY).coerceIn(0, 23)
            val hourly = decodeIntIntMap(prefs[Keys.HOURLY_PLAYS_JSON].orEmpty()).toMutableMap()
            hourly[hour] = (hourly[hour] ?: 0) + 1
            prefs[Keys.HOURLY_PLAYS_JSON] = encodeIntIntMap(hourly)
        }
    }

    suspend fun setLyricOffsetMs(songId: Long, offsetMs: Long) {
        context.pureMusicDataStore.edit { prefs ->
            val map = decodeLongLongMap(prefs[Keys.LYRIC_OFFSETS_JSON].orEmpty()).toMutableMap()
            if (offsetMs == 0L) map.remove(songId) else map[songId] = offsetMs.coerceIn(-15_000L, 15_000L)
            prefs[Keys.LYRIC_OFFSETS_JSON] = encodeLongLongMap(map)
        }
    }

    suspend fun toggleFolderExcluded(folderPath: String) {
        context.pureMusicDataStore.edit { prefs ->
            val current = prefs[Keys.EXCLUDED_FOLDERS].orEmpty().toMutableSet()
            if (folderPath in current) current.remove(folderPath) else current.add(folderPath)
            prefs[Keys.EXCLUDED_FOLDERS] = current
        }
    }

    suspend fun setMinDurationFilter(filter: MinDurationFilter) {
        context.pureMusicDataStore.edit { it[Keys.MIN_DURATION_FILTER] = filter.name }
    }

    suspend fun ensureV8BookStyleEpoch() {
        context.pureMusicDataStore.edit { prefs ->
            val epoch = prefs[Keys.PREF_STYLE_EPOCH] ?: 0
            if (epoch < 8) {
                prefs[Keys.FONT_STYLE] = AppFontStyle.SANS.name
                prefs[Keys.LYRIC_FONT_STYLE] = LyricFontStyle.SANS.name
                prefs[Keys.BACKGROUND_STYLE] = BackgroundStyle.WARM_PAPER.name
                prefs[Keys.LIQUID_GLASS_ENABLED] = false
                prefs[Keys.ACCENT_COLOR] = AppAccentColor.CINNABAR_RED.name
                prefs[Keys.PREF_STYLE_EPOCH] = 8
            }
        }
    }

    suspend fun resetAppearance() {
        context.pureMusicDataStore.edit { prefs ->
            prefs[Keys.PLAYER_THEME] = PlayerTheme.POSTER_WALL.name
            prefs[Keys.FONT_STYLE] = AppFontStyle.SANS.name
            prefs[Keys.LYRIC_FONT_STYLE] = LyricFontStyle.SANS.name
            prefs[Keys.BACKGROUND_STYLE] = BackgroundStyle.WARM_PAPER.name
            prefs[Keys.LAYOUT_MODE] = LayoutMode.AUTO.name
            prefs[Keys.LAYOUT_DENSITY] = LayoutDensity.STANDARD.name
            prefs[Keys.FONT_SCALE] = 1.0f
            prefs[Keys.LIQUID_GLASS_ENABLED] = false
            prefs[Keys.ACCENT_COLOR] = AppAccentColor.CINNABAR_RED.name
        }
    }

    suspend fun setAccentColor(color: AppAccentColor) {
        context.pureMusicDataStore.edit { it[Keys.ACCENT_COLOR] = color.name }
    }

    suspend fun savePlaybackSnapshot(songId: Long, positionMs: Long, queueIds: List<Long>) {
        context.pureMusicDataStore.edit { prefs ->
            prefs[Keys.LAST_SONG_ID] = songId
            prefs[Keys.LAST_POSITION_MS] = positionMs.coerceAtLeast(0L)
            prefs[Keys.LAST_QUEUE_IDS] = queueIds.take(300).joinToString(",")
        }
    }

    suspend fun loadPlaybackSnapshot(): PlaybackSnapshot {
        val prefs = context.pureMusicDataStore.data.first()
        val songId = prefs[Keys.LAST_SONG_ID]
        val posMs = prefs[Keys.LAST_POSITION_MS] ?: 0L
        val qIds = prefs[Keys.LAST_QUEUE_IDS]
            .orEmpty()
            .split(",")
            .mapNotNull { it.trim().toLongOrNull() }
        return PlaybackSnapshot(lastSongId = songId, lastPositionMs = posMs, queueIds = qIds)
    }

    suspend fun setSortOrder(order: SortOrder) {
        context.pureMusicDataStore.edit { it[Keys.SORT_ORDER] = order.name }
    }

    private fun encodePlaylists(playlists: List<Playlist>): String {
        val root = JSONArray()
        playlists.forEach { playlist ->
            val obj = JSONObject()
            obj.put("id", playlist.id)
            obj.put("name", playlist.name)
            val ids = JSONArray()
            playlist.songIds.forEach { ids.put(it) }
            obj.put("songIds", ids)
            root.put(obj)
        }
        return root.toString()
    }

    private fun decodePlaylists(json: String): List<Playlist> {
        if (json.isBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(json)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val id = obj.optString("id")
                    val name = obj.optString("name")
                    val idsJson = obj.optJSONArray("songIds")
                    val songIds = buildList {
                        if (idsJson != null) {
                            for (j in 0 until idsJson.length()) {
                                add(idsJson.getLong(j))
                            }
                        }
                    }
                    if (id.isNotBlank() && name.isNotBlank()) {
                        add(Playlist(id = id, name = name, songIds = songIds))
                    }
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun encodeLongIntMap(map: Map<Long, Int>): String {
        val obj = JSONObject()
        map.forEach { (k, v) -> obj.put(k.toString(), v) }
        return obj.toString()
    }

    private fun decodeLongIntMap(json: String): Map<Long, Int> {
        if (json.isBlank()) return emptyMap()
        return runCatching {
            val obj = JSONObject(json)
            val result = mutableMapOf<Long, Int>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val id = k.toLongOrNull() ?: continue
                result[id] = obj.optInt(k, 0)
            }
            result
        }.getOrDefault(emptyMap())
    }

    private fun encodeLongLongMap(map: Map<Long, Long>): String {
        val obj = JSONObject()
        map.forEach { (k, v) -> obj.put(k.toString(), v) }
        return obj.toString()
    }

    private fun decodeLongLongMap(json: String): Map<Long, Long> {
        if (json.isBlank()) return emptyMap()
        return runCatching {
            val obj = JSONObject(json)
            val result = mutableMapOf<Long, Long>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val id = k.toLongOrNull() ?: continue
                result[id] = obj.optLong(k, 0L)
            }
            result
        }.getOrDefault(emptyMap())
    }

    private fun encodeIntIntMap(map: Map<Int, Int>): String {
        val obj = JSONObject()
        map.forEach { (k, v) -> obj.put(k.toString(), v) }
        return obj.toString()
    }

    private fun decodeIntIntMap(json: String): Map<Int, Int> {
        if (json.isBlank()) return emptyMap()
        return runCatching {
            val obj = JSONObject(json)
            val result = mutableMapOf<Int, Int>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val h = k.toIntOrNull() ?: continue
                if (h in 0..23) result[h] = obj.optInt(k, 0)
            }
            result
        }.getOrDefault(emptyMap())
    }
}
