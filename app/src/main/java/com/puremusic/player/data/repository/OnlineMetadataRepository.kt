package com.puremusic.player.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.puremusic.player.data.model.LyricLine
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.Charset

data class OnlineLyricCandidate(
    val id: Long,
    val trackName: String,
    val artistName: String,
    val albumName: String,
    val durationSec: Int,
    val syncedLyrics: String?,
    val plainLyrics: String?,
) {
    val hasSynced: Boolean get() = !syncedLyrics.isNullOrBlank()
    val bestLyricsText: String get() = syncedLyrics?.takeIf { it.isNotBlank() } ?: plainLyrics.orEmpty()
    val previewLines: String
        get() = bestLyricsText
            .lineSequence()
            .map { it.replace(Regex("\\[\\d{1,2}:\\d{2}(?:[.:]\\d{1,3})?]"), "").trim() }
            .filter { it.isNotEmpty() }
            .take(2)
            .joinToString("\n")
}

data class OnlineCoverCandidate(
    val trackName: String,
    val artistName: String,
    val albumName: String,
    val previewUrl: String,
    val highResUrl: String,
)

class OnlineMetadataRepository(private val context: Context) {

    private val coversDir: File
        get() = File(context.filesDir, "custom_covers").apply { if (!exists()) mkdirs() }

    private val lyricsDir: File
        get() = File(context.filesDir, "custom_lyrics").apply { if (!exists()) mkdirs() }

    suspend fun searchOnlineLyrics(query: String): Result<List<OnlineLyricCandidate>> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty()) return@withContext Result.success(emptyList())
        runCatching {
            val encoded = URLEncoder.encode(cleanQuery, "UTF-8")
            val endpoint = "https://lrclib.net/api/search?q=$encoded"
            val body = httpGetText(endpoint)
            val arr = JSONArray(body)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val synced = obj.optString("syncedLyrics").takeIf { it.isNotBlank() && it != "null" }
                    val plain = obj.optString("plainLyrics").takeIf { it.isNotBlank() && it != "null" }
                    if (synced.isNullOrBlank() && plain.isNullOrBlank()) continue
                    add(
                        OnlineLyricCandidate(
                            id = obj.optLong("id", i.toLong()),
                            trackName = obj.optString("trackName").ifBlank { cleanQuery },
                            artistName = obj.optString("artistName").ifBlank { "未知歌手" },
                            albumName = obj.optString("albumName").orEmpty(),
                            durationSec = obj.optDouble("duration", 0.0).toInt(),
                            syncedLyrics = synced,
                            plainLyrics = plain,
                        ),
                    )
                }
            }.sortedByDescending { it.hasSynced }.take(15)
        }
    }

    suspend fun searchOnlineCovers(query: String): Result<List<OnlineCoverCandidate>> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty()) return@withContext Result.success(emptyList())
        runCatching {
            val encoded = URLEncoder.encode(cleanQuery, "UTF-8")
            val endpoint = "https://itunes.apple.com/search?term=$encoded&entity=song&limit=18"
            val body = httpGetText(endpoint)
            val root = JSONObject(body)
            val results = root.optJSONArray("results") ?: JSONArray()
            val seen = HashSet<String>()
            buildList {
                for (i in 0 until results.length()) {
                    val obj = results.optJSONObject(i) ?: continue
                    val art100 = obj.optString("artworkUrl100")
                    if (art100.isBlank()) continue
                    val highRes = art100.replace("100x100bb", "600x600bb")
                    if (!seen.add(highRes)) continue
                    add(
                        OnlineCoverCandidate(
                            trackName = obj.optString("trackName").ifBlank { cleanQuery },
                            artistName = obj.optString("artistName").ifBlank { "未知歌手" },
                            albumName = obj.optString("collectionName").ifBlank { obj.optString("trackName") },
                            previewUrl = art100.replace("100x100bb", "300x300bb"),
                            highResUrl = highRes,
                        ),
                    )
                }
            }.take(12)
        }
    }

    suspend fun saveCustomCoverFromUrl(songId: Long, imageUrl: String): Uri? = withContext(Dispatchers.IO) {
        runCatching {
            val bytes = httpGetBytes(imageUrl)
            saveCompressedCoverBytes(songId, bytes)
        }.getOrNull()
    }

    suspend fun saveCustomCoverFromUri(songId: Long, sourceUri: Uri): Uri? = withContext(Dispatchers.IO) {
        runCatching {
            val bytes = context.contentResolver.openInputStream(sourceUri)?.use { it.readBytes() }
                ?: return@runCatching null
            saveCompressedCoverBytes(songId, bytes)
        }.getOrNull()
    }

    suspend fun clearCustomCover(songId: Long) = withContext(Dispatchers.IO) {
        coversDir.listFiles()?.forEach { f ->
            if (f.name.startsWith("song_${songId}_") || f.name == "song_${songId}.jpg") {
                runCatching { f.delete() }
            }
        }
    }

    suspend fun loadAllCustomCovers(): Map<Long, Uri> = withContext(Dispatchers.IO) {
        val result = mutableMapOf<Long, Uri>()
        val files = coversDir.listFiles()?.sortedBy { it.lastModified() } ?: return@withContext result
        for (file in files) {
            if (!file.isFile || !file.name.startsWith("song_")) continue
            val idStr = file.name.removePrefix("song_").substringBefore("_").substringBefore(".")
            val songId = idStr.toLongOrNull() ?: continue
            result[songId] = Uri.fromFile(file)
        }
        result
    }

    suspend fun saveCustomLyricsText(songId: Long, rawText: String): Boolean = withContext(Dispatchers.IO) {
        if (rawText.isBlank()) return@withContext false
        runCatching {
            val target = File(lyricsDir, "song_${songId}.lrc")
            target.writeText(rawText.trim(), Charsets.UTF_8)
            true
        }.getOrDefault(false)
    }

    suspend fun saveCustomLyricsFromUri(songId: Long, sourceUri: Uri): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val bytes = context.contentResolver.openInputStream(sourceUri)?.use { it.readBytes() }
                ?: return@runCatching false
            val utf8 = bytes.toString(Charsets.UTF_8)
            val decoded = if (!utf8.contains('\uFFFD')) {
                utf8
            } else {
                runCatching { bytes.toString(Charset.forName("GBK")) }.getOrDefault(utf8)
            }
            if (decoded.isBlank()) return@runCatching false
            File(lyricsDir, "song_${songId}.lrc").writeText(decoded.trim(), Charsets.UTF_8)
            true
        }.getOrDefault(false)
    }

    suspend fun clearCustomLyrics(songId: Long) = withContext(Dispatchers.IO) {
        val target = File(lyricsDir, "song_${songId}.lrc")
        if (target.exists()) runCatching { target.delete() }
    }

    suspend fun translateLyricsToBilingualLrc(lines: List<LyricLine>): Result<String> = withContext(Dispatchers.IO) {
        if (lines.isEmpty()) return@withContext Result.failure(IllegalArgumentException("Empty lyrics"))
        runCatching {
            val cjkRegex = Regex("[\\u4E00-\\u9FFF]")
            val translatedMap = HashMap<Int, String>()

            // Collect indices of lines that are non-Chinese and don't already have a translation
            val pendingIndices = lines.indices.filter { idx ->
                val line = lines[idx]
                line.secondaryText.isNullOrBlank() &&
                    line.primaryText.isNotBlank() &&
                    !cjkRegex.containsMatchIn(line.primaryText)
            }

            // Translate in safe batches of 18 lines to avoid URL length limits
            val chunkSize = 18
            for (start in pendingIndices.indices step chunkSize) {
                val batchIdx = pendingIndices.subList(start, minOf(start + chunkSize, pendingIndices.size))
                val joined = batchIdx.joinToString("\n") { lines[it].primaryText.replace("\n", " ") }
                val encoded = URLEncoder.encode(joined, "UTF-8")
                val url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=zh-CN&dt=t&q=$encoded"
                val rawJson = httpGetText(url)
                val root = JSONArray(rawJson)
                val segments = root.optJSONArray(0)
                val fullTranslated = buildString {
                    if (segments != null) {
                        for (i in 0 until segments.length()) {
                            val seg = segments.optJSONArray(i)
                            val part = seg?.optString(0).orEmpty()
                            append(part)
                        }
                    }
                }
                val translatedLines = fullTranslated.split("\n")
                batchIdx.forEachIndexed { offset, lineIdx ->
                    val zh = translatedLines.getOrNull(offset)?.trim().orEmpty()
                    if (zh.isNotBlank() && !zh.equals(lines[lineIdx].primaryText.trim(), ignoreCase = true)) {
                        translatedMap[lineIdx] = zh
                    }
                }
            }

            // Re-assemble into standard bilingual .lrc so LyricsRepository groups primary + secondary lines automatically
            buildString {
                lines.forEachIndexed { idx, line ->
                    val tag = formatLrcTimestamp(line.timeMs)
                    append(tag).append(line.primaryText.trim()).append('\n')
                    val secondary = line.secondaryText?.takeIf { it.isNotBlank() } ?: translatedMap[idx]
                    if (!secondary.isNullOrBlank()) {
                        append(tag).append(secondary.trim()).append('\n')
                    }
                }
            }
        }
    }

    private fun formatLrcTimestamp(timeMs: Long): String {
        val safe = timeMs.coerceAtLeast(0L)
        val minutes = safe / 60_000L
        val seconds = (safe % 60_000L) / 1_000L
        val hundredths = (safe % 1_000L) / 10L
        return String.format(Locale.US, "[%02d:%02d.%02d]", minutes, seconds, hundredths)
    }

    private fun saveCompressedCoverBytes(songId: Long, rawBytes: ByteArray): Uri? {
        val decoded = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size) ?: return null
        val maxDim = 900
        val scaled = if (decoded.width > maxDim || decoded.height > maxDim) {
            val ratio = minOf(maxDim.toFloat() / decoded.width, maxDim.toFloat() / decoded.height)
            val w = (decoded.width * ratio).toInt().coerceAtLeast(1)
            val h = (decoded.height * ratio).toInt().coerceAtLeast(1)
            Bitmap.createScaledBitmap(decoded, w, h, true).also {
                if (it != decoded) decoded.recycle()
            }
        } else {
            decoded
        }
        // Remove previous cover files for this song so Coil never serves a stale URI
        coversDir.listFiles()?.forEach { f ->
            if (f.name.startsWith("song_${songId}_") || f.name == "song_${songId}.jpg") {
                runCatching { f.delete() }
            }
        }
        val outFile = File(coversDir, "song_${songId}_${System.currentTimeMillis()}.jpg")
        FileOutputStream(outFile).use { fos ->
            scaled.compress(Bitmap.CompressFormat.JPEG, 92, fos)
        }
        scaled.recycle()
        return Uri.fromFile(outFile)
    }

    private fun httpGetText(urlString: String): String {
        val conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8_000
            readTimeout = 8_000
            setRequestProperty("User-Agent", "PureMusic/8.2.0 (Android)")
            setRequestProperty("Accept", "application/json")
        }
        return try {
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                throw IllegalStateException("HTTP $code")
            }
            text
        } finally {
            conn.disconnect()
        }
    }

    private fun httpGetBytes(urlString: String): ByteArray {
        val conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 10_000
            setRequestProperty("User-Agent", "PureMusic/8.2.0 (Android)")
        }
        return try {
            if (conn.responseCode !in 200..299) throw IllegalStateException("HTTP ${conn.responseCode}")
            conn.inputStream.use { it.readBytes() }
        } finally {
            conn.disconnect()
        }
    }
}
