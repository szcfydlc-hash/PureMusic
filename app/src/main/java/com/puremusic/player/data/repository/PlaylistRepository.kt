package com.puremusic.player.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.first
import com.puremusic.player.data.model.AppAccentColor
import com.puremusic.player.data.model.AppFontStyle
import com.puremusic.player.data.model.BackgroundStyle
import com.puremusic.player.data.model.LayoutDensity
import com.puremusic.player.data.model.LayoutMode
import com.puremusic.player.data.model.LyricFontStyle
import com.puremusic.player.data.model.MinDurationFilter
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
        val EXCLUDED_FOLDERS = stringSetPreferencesKey("excluded_folders")
        val MIN_DURATION_FILTER = stringPreferencesKey("min_duration_filter")
        val ACCENT_COLOR = stringPreferencesKey("accent_color")
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val LAST_SONG_ID = longPreferencesKey("last_song_id")
        val LAST_POSITION_MS = longPreferencesKey("last_position_ms")
        val LAST_QUEUE_IDS = stringPreferencesKey("last_queue_ids")
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
        } ?: LyricFontStyle.SERIF_ITALIC
    }

    val backgroundStyleFlow: Flow<BackgroundStyle> = context.pureMusicDataStore.data.map { prefs ->
        prefs[Keys.BACKGROUND_STYLE]?.let { name ->
            BackgroundStyle.entries.firstOrNull { it.name == name }
        } ?: BackgroundStyle.PURE_WHITE
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
        prefs[Keys.LIQUID_GLASS_ENABLED] ?: true
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
        } ?: AppAccentColor.BERRY_RED
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

    suspend fun resetAppearance() {
        context.pureMusicDataStore.edit { prefs ->
            prefs[Keys.PLAYER_THEME] = PlayerTheme.POSTER_WALL.name
            prefs[Keys.FONT_STYLE] = AppFontStyle.SANS.name
            prefs[Keys.LYRIC_FONT_STYLE] = LyricFontStyle.SERIF_ITALIC.name
            prefs[Keys.BACKGROUND_STYLE] = BackgroundStyle.PURE_WHITE.name
            prefs[Keys.LAYOUT_MODE] = LayoutMode.AUTO.name
            prefs[Keys.LAYOUT_DENSITY] = LayoutDensity.STANDARD.name
            prefs[Keys.FONT_SCALE] = 1.0f
            prefs[Keys.LIQUID_GLASS_ENABLED] = true
            prefs[Keys.ACCENT_COLOR] = AppAccentColor.BERRY_RED.name
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
}
