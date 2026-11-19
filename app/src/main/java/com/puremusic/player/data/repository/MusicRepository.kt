package com.puremusic.player.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.puremusic.player.data.model.Album
import com.puremusic.player.data.model.Artist
import com.puremusic.player.data.model.MusicFolder
import com.puremusic.player.data.model.Song
import com.puremusic.player.data.model.SortOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MusicRepository(private val context: Context) {

    private val albumArtBaseUri: Uri = Uri.parse("content://media/external/audio/albumart")

    suspend fun loadLocalSongs(): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = buildList {
            add(MediaStore.Audio.Media._ID)
            add(MediaStore.Audio.Media.TITLE)
            add(MediaStore.Audio.Media.ARTIST)
            add(MediaStore.Audio.Media.ALBUM)
            add(MediaStore.Audio.Media.ALBUM_ID)
            add(MediaStore.Audio.Media.DURATION)
            add(MediaStore.Audio.Media.DATE_ADDED)
            @Suppress("DEPRECATION")
            add(MediaStore.Audio.Media.DATA)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(MediaStore.Audio.Media.RELATIVE_PATH)
            }
        }.toTypedArray()

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} > 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            sortOrder,
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            @Suppress("DEPRECATION")
            val dataCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val relPathCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                cursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH)
            } else {
                -1
            }

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val title = cursor.getString(titleCol)?.takeIf { it.isNotBlank() } ?: "未知歌曲"
                val rawArtist = cursor.getString(artistCol)
                val artist = if (rawArtist.isNullOrBlank() || rawArtist == "<unknown>") "未知歌手" else rawArtist
                val rawAlbum = cursor.getString(albumCol)
                val album = if (rawAlbum.isNullOrBlank() || rawAlbum == "<unknown>") "未知专辑" else rawAlbum
                val albumId = cursor.getLong(albumIdCol)
                val durationMs = cursor.getLong(durationCol)
                val dateAddedSec = cursor.getLong(dateAddedCol)

                val filePath = if (dataCol >= 0) cursor.getString(dataCol).orEmpty() else ""
                val relPath = if (relPathCol >= 0) cursor.getString(relPathCol).orEmpty() else ""
                val (folderName, folderPath) = parseFolderInfo(relPath.ifBlank { filePath }, useRelPath = relPath.isNotBlank())

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id,
                )
                val albumArtUri = ContentUris.withAppendedId(albumArtBaseUri, albumId)

                songs.add(
                    Song(
                        id = id,
                        title = title,
                        artist = artist,
                        album = album,
                        albumId = albumId,
                        durationMs = durationMs,
                        contentUri = contentUri,
                        albumArtUri = albumArtUri,
                        folderName = folderName,
                        folderPath = folderPath,
                        filePath = filePath,
                        dateAddedSec = dateAddedSec,
                    ),
                )
            }
        }
        songs
    }

    fun sortSongs(songs: List<Song>, order: SortOrder): List<Song> = when (order) {
        SortOrder.TITLE -> songs.sortedBy { it.title.lowercase() }
        SortOrder.ARTIST -> songs.sortedWith(compareBy({ it.artist.lowercase() }, { it.title.lowercase() }))
        SortOrder.DURATION -> songs.sortedByDescending { it.durationMs }
        SortOrder.DATE_ADDED -> songs.sortedByDescending { it.dateAddedSec }
    }

    fun groupByAlbums(songs: List<Song>): List<Album> = songs
        .groupBy { it.albumId }
        .values
        .map { albumSongs ->
            val first = albumSongs.first()
            Album(
                id = first.albumId,
                name = first.album,
                artist = first.artist,
                albumArtUri = first.albumArtUri,
                songs = albumSongs.sortedBy { it.title.lowercase() },
            )
        }
        .sortedBy { it.name.lowercase() }

    fun groupByArtists(songs: List<Song>): List<Artist> = songs
        .groupBy { it.artist }
        .entries
        .map { (artistName, artistSongs) ->
            Artist(
                name = artistName,
                albumCount = artistSongs.map { it.albumId }.distinct().size,
                songs = artistSongs.sortedBy { it.title.lowercase() },
            )
        }
        .sortedBy { it.name.lowercase() }

    fun groupByFolders(songs: List<Song>): List<MusicFolder> = songs
        .groupBy { it.folderPath }
        .entries
        .map { (path, folderSongs) ->
            MusicFolder(
                name = folderSongs.first().folderName,
                path = path,
                songs = folderSongs.sortedBy { it.title.lowercase() },
            )
        }
        .sortedBy { it.name.lowercase() }

    private fun parseFolderInfo(rawPath: String, useRelPath: Boolean): Pair<String, String> {
        if (rawPath.isBlank()) return "本地音乐" to "/"
        return if (useRelPath) {
            val trimmed = rawPath.trimEnd('/')
            val name = trimmed.substringAfterLast('/', "本地音乐").ifBlank { "本地音乐" }
            name to trimmed.ifBlank { "/" }
        } else {
            val parent = File(rawPath).parentFile
            val name = parent?.name?.ifBlank { "本地音乐" } ?: "本地音乐"
            val path = parent?.absolutePath ?: "/"
            name to path
        }
    }
}
