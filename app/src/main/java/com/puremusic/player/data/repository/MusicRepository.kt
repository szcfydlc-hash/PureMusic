package com.puremusic.player.data.repository

import android.content.ContentUris
import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaScannerConnection
import com.puremusic.player.data.model.AudioTechSpec
import java.util.Locale
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.puremusic.player.data.model.Album
import com.puremusic.player.data.model.Artist
import com.puremusic.player.data.model.MusicFolder
import com.puremusic.player.data.model.Song
import com.puremusic.player.data.model.SortOrder
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class MusicRepository(private val context: Context) {

    private val albumArtBaseUri: Uri = Uri.parse("content://media/external/audio/albumart")
    private val audioExtensions = setOf("mp3", "flac", "wav", "m4a", "aac", "ogg", "opus", "wma", "ape")

    suspend fun triggerMediaScanAndLoad(): List<Song> = withContext(Dispatchers.IO) {
        val roots = listOf(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        ).filter { it != null && it.exists() && it.isDirectory }

        val pathsToScan = buildList {
            for (dir in roots) {
                runCatching {
                    dir.walkTopDown()
                        .maxDepth(4)
                        .filter { it.isFile && it.extension.lowercase() in audioExtensions }
                        .take(150)
                        .forEach { add(it.absolutePath) }
                }
            }
        }

        if (pathsToScan.isNotEmpty()) {
            val done = CompletableDeferred<Unit>()
            val remaining = AtomicInteger(pathsToScan.size)
            runCatching {
                MediaScannerConnection.scanFile(
                    context,
                    pathsToScan.toTypedArray(),
                    null,
                ) { _, _ ->
                    if (remaining.decrementAndGet() <= 0 && !done.isCompleted) {
                        done.complete(Unit)
                    }
                }
            }.onFailure {
                if (!done.isCompleted) done.complete(Unit)
            }
            withTimeoutOrNull(2_500L) { done.await() }
        }

        loadLocalSongs()
    }

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                add(MediaStore.Audio.Media.GENRE)
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
            val genreCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                cursor.getColumnIndex(MediaStore.Audio.Media.GENRE)
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
                val albumArtUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentUri
                } else {
                    ContentUris.withAppendedId(albumArtBaseUri, albumId)
                }

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
                        genre = normalizeGenre(if (genreCol >= 0) cursor.getString(genreCol) else null),
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

    suspend fun extractAudioTechSpec(song: Song): AudioTechSpec = withContext(Dispatchers.IO) {
        val ext = song.filePath.substringAfterLast('.', "").uppercase(Locale.US)
        val extractor = MediaExtractor()
        var sampleRate = 0
        var bitrateKbps = 0
        var mimeFormat = ext
        try {
            extractor.setDataSource(context, song.contentUri, null)
            for (i in 0 until extractor.trackCount) {
                val fmt = extractor.getTrackFormat(i)
                val mime = fmt.getString(MediaFormat.KEY_MIME).orEmpty()
                if (mime.startsWith("audio/")) {
                    if (fmt.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
                        sampleRate = fmt.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                    }
                    if (fmt.containsKey(MediaFormat.KEY_BIT_RATE)) {
                        bitrateKbps = fmt.getInteger(MediaFormat.KEY_BIT_RATE) / 1000
                    }
                    if (mimeFormat.isBlank()) {
                        mimeFormat = when {
                            mime.contains("flac") -> "FLAC"
                            mime.contains("mpeg") -> "MP3"
                            mime.contains("mp4a") -> "AAC"
                            mime.contains("raw") -> "WAV"
                            mime.contains("opus") -> "OPUS"
                            mime.contains("vorbis") -> "OGG"
                            else -> "AUDIO"
                        }
                    }
                    break
                }
            }
        } catch (_: Exception) {
        } finally {
            runCatching { extractor.release() }
        }
        if (bitrateKbps <= 0 && song.filePath.isNotBlank() && song.durationMs > 1000L) {
            val bytes = runCatching { File(song.filePath).length() }.getOrDefault(0L)
            if (bytes > 0L) {
                bitrateKbps = ((bytes * 8L) / song.durationMs).toInt()
            }
        }
        val lossless = mimeFormat in setOf("FLAC", "WAV", "ALAC", "APE", "DSD")
        val hiRes = (lossless && sampleRate >= 48_000) || bitrateKbps >= 900
        AudioTechSpec(
            format = mimeFormat.ifBlank { "AUDIO" },
            sampleRateHz = sampleRate,
            bitrateKbps = bitrateKbps,
            isHiRes = hiRes,
        )
    }

    private fun normalizeGenre(raw: String?): String {
        val cleaned = raw
            ?.replace(Regex("""^\(\d+\)\s*"""), "")
            ?.trim()
            .orEmpty()
        if (cleaned.length <= 1) return ""
        val lower = cleaned.lowercase()
        if (lower == "<unknown>" || lower == "unknown" || lower == "other" || lower == "misc" || lower == "none" || lower.all { it.isDigit() }) {
            return ""
        }
        return when {
            lower.contains("singer") || lower.contains("songwriter") || lower.contains("folk") ||
                lower.contains("acoustic") || lower.contains("民谣") -> "民谣"
            lower.contains("electronic") || lower.contains("electronica") || lower.contains("edm") ||
                lower.contains("trip-hop") || lower.contains("trip hop") || lower.contains("downtempo") ||
                lower.contains("house") || lower.contains("techno") || lower.contains("trance") ||
                lower.contains("dubstep") || lower.contains("drum & bass") || lower.contains("chillwave") ||
                lower.contains("lo-fi") || lower.contains("lofi") || lower.contains("synth") ||
                lower.contains("电子") -> "电子"
            lower.contains("new age") || lower.contains("ambient") || lower.contains("easy listening") ||
                lower.contains("instrumental") || lower.contains("lounge") || lower.contains("chillout") ||
                lower.contains("piano") || lower.contains("world") || lower.contains("meditation") ||
                lower.contains("轻音乐") || lower.contains("纯音乐") || lower.contains("器乐") ||
                lower.contains("新世纪") -> "轻音乐"
            lower.contains("indie") || lower.contains("alternative") || lower.contains("shoegaze") ||
                lower.contains("dream pop") || lower.contains("post-rock") || lower.contains("britpop") ||
                lower.contains("独立") || lower.contains("另类") -> "独立"
            lower.contains("soundtrack") || lower.contains("ost") || lower.contains("score") ||
                lower.contains("film") || lower.contains("movie") || lower.contains("game") ||
                lower.contains("anime") || lower.contains("原声") || lower.contains("影视") ||
                lower.contains("动漫") || lower.contains("游戏") -> "原声"
            lower.contains("jazz") || lower.contains("swing") || lower.contains("bossa") ||
                lower.contains("fusion") || lower.contains("爵士") -> "爵士"
            lower.contains("metal") || lower.contains("金属") -> "金属"
            lower.contains("rock") || lower.contains("punk") || lower.contains("grunge") ||
                lower.contains("摇滚") -> "摇滚"
            lower.contains("classical") || lower.contains("orchestra") || lower.contains("symphony") ||
                lower.contains("opera") || lower.contains("baroque") || lower.contains("chamber") ||
                lower.contains("古典") || lower.contains("交响") -> "古典"
            lower.contains("hip-hop") || lower.contains("hip hop") || lower.contains("hiphop") ||
                lower.contains("rap") || lower.contains("trap") || lower.contains("说唱") ||
                lower.contains("嘻哈") -> "说唱"
            lower.contains("r&b") || lower.contains("rnb") || lower.contains("soul") ||
                lower.contains("funk") || lower.contains("节奏布鲁斯") || lower.contains("灵魂") -> "R&B"
            lower.contains("blues") || lower.contains("蓝调") || lower.contains("布鲁斯") -> "蓝调"
            lower.contains("country") || lower.contains("americana") || lower.contains("bluegrass") ||
                lower.contains("乡村") -> "乡村"
            lower.contains("pop") || lower.contains("top 40") || lower.contains("top40") ||
                lower.contains("dance") || lower.contains("vocal") || lower.contains("ballad") ||
                lower.contains("adult contemporary") || lower.contains("流行") || lower.contains("华语") ||
                lower.contains("国语") || lower.contains("粤语") -> "流行"
            else -> ""
        }
    }
}
