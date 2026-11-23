package com.puremusic.player.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.puremusic.player.data.model.LyricLine
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import com.puremusic.player.data.model.TranslationSettings
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

data class SongMetaOverride(
    val title: String,
    val artist: String,
    val album: String,
    val genre: String = "",
)

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

    private val metaFile: File
        get() = File(context.filesDir, "custom_metadata.json")

    private val lyricsDir: File
        get() = File(context.filesDir, "custom_lyrics").apply { if (!exists()) mkdirs() }

    private fun normalizeLyricQuery(raw: String): String {
        return raw
            .replace(Regex("""[(\[（【][^)\]）】]*(?:live|伴奏|纯音乐|remaster|remix|ver\.?|版|主题曲|插曲|片尾曲|片头曲|feat\.?|ft\.?)[^)\]）】]*[)\]）】]""", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("""[(\[（【][^)\]）】]{1,28}[)\]）】]"""), " ")
            .replace(Regex("""\b(?:feat\.?|ft\.?)\s+.*$""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s+"""), " ")
            .trim()
    }

    private fun parseLrclibResponse(body: String, fallbackTitle: String): List<OnlineLyricCandidate> {
        val arr = JSONArray(body)
        return buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val synced = obj.optString("syncedLyrics").takeIf { it.isNotBlank() && it != "null" }
                val plain = obj.optString("plainLyrics").takeIf { it.isNotBlank() && it != "null" }
                if (synced.isNullOrBlank() && plain.isNullOrBlank()) continue
                add(
                    OnlineLyricCandidate(
                        id = obj.optLong("id", i.toLong()),
                        trackName = obj.optString("trackName").ifBlank { fallbackTitle },
                        artistName = obj.optString("artistName").ifBlank { "未知歌手" },
                        albumName = obj.optString("albumName").orEmpty(),
                        durationSec = obj.optDouble("duration", 0.0).toInt(),
                        syncedLyrics = synced,
                        plainLyrics = plain,
                    ),
                )
            }
        }
    }

    suspend fun searchOnlineLyrics(query: String): Result<List<OnlineLyricCandidate>> = withContext(Dispatchers.IO) {
        val rawQuery = query.trim()
        if (rawQuery.isEmpty()) return@withContext Result.success(emptyList())
        val cleaned = normalizeLyricQuery(rawQuery).ifBlank { rawQuery }
        runCatching {
            val merged = LinkedHashMap<Long, OnlineLyricCandidate>()
            // Pass 1: Cleaned full query ("Title Artist")
            val primaryUrl = "https://lrclib.net/api/search?q=${URLEncoder.encode(cleaned, "UTF-8")}"
            parseLrclibResponse(httpGetText(primaryUrl), cleaned).forEach { merged[it.id] = it }

            // Pass 2: Fallback title-only fuzzy match for Chinese/mixed tracks when full query misses
            val syncedHits = merged.values.count { it.hasSynced }
            val titleOnly = cleaned.split(Regex("""\s+[-–—]\s+|\s+""")).firstOrNull()?.trim().orEmpty()
            if (syncedHits < 2 && titleOnly.isNotBlank() && titleOnly != cleaned) {
                runCatching {
                    val fallbackUrl = "https://lrclib.net/api/search?track_name=${URLEncoder.encode(titleOnly, "UTF-8")}"
                    parseLrclibResponse(httpGetText(fallbackUrl), titleOnly).forEach { merged.putIfAbsent(it.id, it) }
                }
            }

            merged.values
                .sortedWith(compareByDescending<OnlineLyricCandidate> { it.hasSynced })
                .take(18)
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

    suspend fun savePlaylistCoverFromUri(playlistId: String, sourceUri: Uri): Uri? = withContext(Dispatchers.IO) {
        runCatching {
            val bytes = context.contentResolver.openInputStream(sourceUri)?.use { it.readBytes() }
                ?: return@runCatching null
            val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return@runCatching null
            val maxDim = 800
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
            val safePrefix = "playlist_${playlistId}_"
            coversDir.listFiles()?.forEach { f ->
                if (f.name.startsWith(safePrefix)) runCatching { f.delete() }
            }
            val outFile = File(coversDir, "${safePrefix}${System.currentTimeMillis()}.jpg")
            FileOutputStream(outFile).use { fos ->
                scaled.compress(Bitmap.CompressFormat.JPEG, 90, fos)
            }
            scaled.recycle()
            Uri.fromFile(outFile)
        }.getOrNull()
    }

    suspend fun clearPlaylistCover(playlistId: String) = withContext(Dispatchers.IO) {
        val safePrefix = "playlist_${playlistId}_"
        coversDir.listFiles()?.forEach { f ->
            if (f.name.startsWith(safePrefix)) runCatching { f.delete() }
        }
    }

    suspend fun loadAllPlaylistCovers(): Map<String, Uri> = withContext(Dispatchers.IO) {
        val result = mutableMapOf<String, Uri>()
        val files = coversDir.listFiles()?.sortedBy { it.lastModified() } ?: return@withContext result
        for (file in files) {
            if (!file.isFile || !file.name.startsWith("playlist_")) continue
            val rest = file.name.removePrefix("playlist_")
            val playlistId = rest.substringBeforeLast("_")
            if (playlistId.isNotBlank()) {
                result[playlistId] = Uri.fromFile(file)
            }
        }
        result
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

    suspend fun loadAllMetaOverrides(): Map<Long, SongMetaOverride> = withContext(Dispatchers.IO) {
        if (!metaFile.exists()) return@withContext emptyMap()
        runCatching {
            val root = JSONObject(metaFile.readText(Charsets.UTF_8))
            val map = mutableMapOf<Long, SongMetaOverride>()
            val keys = root.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val songId = key.toLongOrNull() ?: continue
                val obj = root.optJSONObject(key) ?: continue
                map[songId] = SongMetaOverride(
                    title = obj.optString("title"),
                    artist = obj.optString("artist"),
                    album = obj.optString("album"),
                    genre = obj.optString("genre"),
                )
            }
            map
        }.getOrDefault(emptyMap())
    }

    suspend fun saveMetaOverride(songId: Long, override: SongMetaOverride) = withContext(Dispatchers.IO) {
        runCatching {
            val root = if (metaFile.exists()) {
                runCatching { JSONObject(metaFile.readText(Charsets.UTF_8)) }.getOrDefault(JSONObject())
            } else {
                JSONObject()
            }
            val obj = JSONObject().apply {
                put("title", override.title.trim())
                put("artist", override.artist.trim())
                put("album", override.album.trim())
                put("genre", override.genre.trim())
            }
            root.put(songId.toString(), obj)
            metaFile.writeText(root.toString(), Charsets.UTF_8)
        }
    }

    suspend fun clearMetaOverride(songId: Long) = withContext(Dispatchers.IO) {
        runCatching {
            if (!metaFile.exists()) return@runCatching
            val root = JSONObject(metaFile.readText(Charsets.UTF_8))
            root.remove(songId.toString())
            metaFile.writeText(root.toString(), Charsets.UTF_8)
        }
    }

    suspend fun clearCustomLyrics(songId: Long) = withContext(Dispatchers.IO) {
        val target = File(lyricsDir, "song_${songId}.lrc")
        if (target.exists()) runCatching { target.delete() }
    }

    suspend fun translateLyricsToBilingualLrc(
        lines: List<LyricLine>,
        settings: TranslationSettings = TranslationSettings(),
    ): Result<String> = withContext(Dispatchers.IO) {
        if (lines.isEmpty()) return@withContext Result.failure(IllegalArgumentException("暂无可翻译歌词"))
        val srcLang = detectSourceLanguage(lines)
            ?: return@withContext Result.failure(IllegalStateException("当前歌词已是中文，无需翻译"))

        val uniqueTexts = lines.map { it.primaryText.trim() }.filter { it.isNotBlank() }.distinct()
        if (uniqueTexts.isEmpty()) return@withContext Result.failure(IllegalArgumentException("暂无有效歌词文本"))

        val translatedMapResult: Result<Map<String, String>> = if (settings.customApiKey.isNotBlank()) {
            translateWithCustomOpenAiApi(uniqueTexts, settings)
        } else {
            translateWithMyMemoryApi(uniqueTexts, srcLang)
        }

        translatedMapResult.mapCatching { map ->
            buildString {
                lines.forEach { line ->
                    val tag = formatLrcTimestamp(line.timeMs)
                    val primary = line.primaryText.trim()
                    append(tag).append(primary).append('\n')
                    val translated = map[primary]?.trim()?.takeIf { it.isNotBlank() && !it.equals(primary, ignoreCase = true) }
                        ?: line.secondaryText?.trim()?.takeIf { it.isNotBlank() }
                    if (!translated.isNullOrBlank()) {
                        append(tag).append(translated).append('\n')
                    }
                }
            }
        }
    }

    private fun detectSourceLanguage(lines: List<LyricLine>): String? {
        val sample = lines.joinToString(" ") { it.primaryText }
        var cjk = 0
        var jpKana = 0
        var koHangul = 0
        var cyrillic = 0
        var latin = 0
        for (ch in sample) {
            when (ch.code) {
                in 0x3040..0x30FF -> jpKana++
                in 0xAC00..0xD7AF -> koHangul++
                in 0x0400..0x04FF -> cyrillic++
                in 0x4E00..0x9FFF -> cjk++
                in 0x0041..0x007A -> latin++
            }
        }
        return when {
            jpKana >= 4 -> "ja"
            koHangul >= 4 -> "ko"
            cyrillic >= 6 -> "ru"
            cjk > latin && jpKana == 0 && koHangul == 0 -> null
            else -> "en"
        }
    }

    private fun translateWithMyMemoryApi(
        texts: List<String>,
        srcLang: String,
    ): Result<Map<String, String>> = runCatching {
        val langPair = "$srcLang|zh-CN"
        val out = LinkedHashMap<String, String>(texts.size)
        val sep = " ||| "
        var i = 0
        while (i < texts.size) {
            val batch = mutableListOf<String>()
            var charLen = 0
            while (i < texts.size && batch.size < 8) {
                val nextLen = texts[i].length + sep.length
                if (batch.isNotEmpty() && charLen + nextLen > 420) break
                batch.add(texts[i])
                charLen += nextLen
                i++
            }
            val joined = batch.joinToString(sep)
            val url = "https://api.mymemory.translated.net/get?q=${URLEncoder.encode(joined, "UTF-8")}&langpair=${URLEncoder.encode(langPair, "UTF-8")}"
            val body = runCatching { httpGetText(url) }.getOrNull() ?: continue
            val root = JSONObject(body)
            val translatedText = root.optJSONObject("responseData")?.optString("translatedText").orEmpty()
            if (translatedText.isNotBlank()) {
                val parts = translatedText.split(Regex("""\s*\|\|\|\s*"""))
                if (parts.size == batch.size) {
                    batch.indices.forEach { idx ->
                        val cleaned = parts[idx].trim()
                        if (cleaned.isNotBlank()) out[batch[idx]] = cleaned
                    }
                } else {
                    for (line in batch) {
                        val singleUrl = "https://api.mymemory.translated.net/get?q=${URLEncoder.encode(line, "UTF-8")}&langpair=${URLEncoder.encode(langPair, "UTF-8")}"
                        val sBody = runCatching { httpGetText(singleUrl) }.getOrNull() ?: continue
                        val sTrans = JSONObject(sBody).optJSONObject("responseData")?.optString("translatedText").orEmpty().trim()
                        if (sTrans.isNotBlank()) out[line] = sTrans
                    }
                }
            }
        }
        if (out.isEmpty()) error("公开翻译接口暂不可用，请检查网络或配置自定义 API Key")
        out
    }

    private fun translateWithCustomOpenAiApi(
        texts: List<String>,
        settings: TranslationSettings,
    ): Result<Map<String, String>> = runCatching {
        val base = settings.customBaseUrl.trim().ifBlank { "https://api.openai.com/v1" }.trimEnd('/')
        val endpoint = if (base.endsWith("/chat/completions")) base else "$base/chat/completions"
        val model = settings.customModel.trim().ifBlank { "gpt-4o-mini" }
        val out = LinkedHashMap<String, String>(texts.size)

        texts.chunked(30).forEach { chunk ->
            val numbered = chunk.mapIndexed { idx, s -> "${idx + 1}. $s" }.joinToString("\n")
            val messages = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", "You are a poetic song lyric translator. Translate each numbered line into natural Simplified Chinese. Keep the exact numbering (1. 2. ...) and line count. Output only the translated lines.")
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", numbered)
                })
            }
            val payload = JSONObject().apply {
                put("model", model)
                put("temperature", 0.2)
                put("messages", messages)
            }
            val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15_000
                readTimeout = 30_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Authorization", "Bearer ${settings.customApiKey.trim()}")
            }
            try {
                conn.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
                val code = conn.responseCode
                val respText = (if (code in 200..299) conn.inputStream else conn.errorStream)
                    ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                if (code !in 200..299) error("自定义接口返回 HTTP $code")
                val content = JSONObject(respText)
                    .optJSONArray("choices")
                    ?.optJSONObject(0)
                    ?.optJSONObject("message")
                    ?.optString("content")
                    .orEmpty()
                val respLines = content.lines().map { it.trim() }.filter { it.isNotBlank() }
                respLines.forEachIndexed { idx, rawLine ->
                    val stripped = rawLine.replace(Regex("""^\d+[\.\)、:]\s*"""), "").trim()
                    if (idx < chunk.size && stripped.isNotBlank()) {
                        out[chunk[idx]] = stripped
                    }
                }
            } finally {
                conn.disconnect()
            }
        }
        if (out.isEmpty()) error("自定义接口未返回有效译文，请检查配置")
        out
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
