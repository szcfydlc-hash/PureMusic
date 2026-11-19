package com.puremusic.player.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.puremusic.player.data.model.AccentPalette
import com.puremusic.player.data.model.Playlist
import com.puremusic.player.data.model.SortOrder
import com.puremusic.player.data.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private val Context.pureMusicDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "puremusic_preferences",
)

class PlaylistRepository(private val context: Context) {

    private object Keys {
        val PLAYLISTS_JSON = stringPreferencesKey("playlists_json")
        val FAVORITE_SONG_IDS = stringSetPreferencesKey("favorite_song_ids")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_PALETTE = stringPreferencesKey("accent_palette")
        val SORT_ORDER = stringPreferencesKey("sort_order")
    }

    val playlistsFlow: Flow<List<Playlist>> = context.pureMusicDataStore.data.map { prefs ->
        decodePlaylists(prefs[Keys.PLAYLISTS_JSON].orEmpty())
    }

    val favoriteSongIdsFlow: Flow<Set<Long>> = context.pureMusicDataStore.data.map { prefs ->
        prefs[Keys.FAVORITE_SONG_IDS].orEmpty().mapNotNull { it.toLongOrNull() }.toSet()
    }

    val themeModeFlow: Flow<ThemeMode> = context.pureMusicDataStore.data.map { prefs ->
        prefs[Keys.THEME_MODE]?.let { name ->
            ThemeMode.entries.firstOrNull { it.name == name }
        } ?: ThemeMode.SYSTEM
    }

    val accentPaletteFlow: Flow<AccentPalette> = context.pureMusicDataStore.data.map { prefs ->
        prefs[Keys.ACCENT_PALETTE]?.let { name ->
            AccentPalette.entries.firstOrNull { it.name == name }
        } ?: AccentPalette.ORANGE
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

    suspend fun setThemeMode(mode: ThemeMode) {
        context.pureMusicDataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setAccentPalette(palette: AccentPalette) {
        context.pureMusicDataStore.edit { it[Keys.ACCENT_PALETTE] = palette.name }
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
