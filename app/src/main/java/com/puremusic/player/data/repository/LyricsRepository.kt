package com.puremusic.player.data.repository

import android.content.Context
import android.media.MediaMetadataRetriever
import android.os.Environment
import com.puremusic.player.data.model.LyricLine
import com.puremusic.player.data.model.LyricWord
import com.puremusic.player.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.charset.Charset

class LyricsRepository(private val context: Context) {

    private val timeTagRegex = Regex("\\[(\\d{1,2}):(\\d{2})(?:[.:](\\d{1,3}))?]")

    suspend fun loadLyricsForSong(song: Song): List<LyricLine> = withContext(Dispatchers.IO) {
        val customFile = File(File(context.filesDir, "custom_lyrics"), "song_${song.id}.lrc")
        if (customFile.exists() && customFile.isFile) {
            val customRaw = readTextWithFallback(customFile)
            if (!customRaw.isNullOrBlank()) {
                val parsed = parseLrc(customRaw)
                if (parsed.isNotEmpty()) return@withContext parsed
                return@withContext customRaw.lineSequence()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .map { LyricLine(timeMs = 0L, primaryText = it) }
                    .toList()
            }
        }

        val externalFile = findLrcFile(song)
        if (externalFile != null) {
            val rawText = readTextWithFallback(externalFile)
            if (!rawText.isNullOrBlank()) {
                val parsed = parseLrc(rawText)
                if (parsed.isNotEmpty()) return@withContext parsed
            }
        }

        // Fallback: read embedded lyrics tag from the audio file itself
        val embedded = readEmbeddedLyrics(song)
        if (!embedded.isNullOrBlank()) {
            val parsed = parseLrc(embedded)
            if (parsed.isNotEmpty()) return@withContext parsed
            return@withContext embedded.lineSequence()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .map { LyricLine(timeMs = 0L, primaryText = it) }
                .toList()
        }

        emptyList()
    }

    private fun readEmbeddedLyrics(song: Song): String? {
        // 1. Direct binary tag parsing for FLAC Vorbis Comments, MP3 ID3v2 USLT, and M4A ©lyr
        val parsedTag = runCatching {
            val headerBytes = readBoundedHeaderBytes(song, maxBytes = 512 * 1024) ?: return@runCatching null
            parseFlacEmbeddedLyrics(headerBytes)
                ?: parseId3v2UsltLyrics(headerBytes)
                ?: parseMp4EmbeddedLyrics(headerBytes)
        }.getOrNull()
        if (!parsedTag.isNullOrBlank()) return parsedTag

        // 2. OEM MediaMetadataRetriever fallback
        val retriever = MediaMetadataRetriever()
        return runCatching {
            retriever.setDataSource(context, song.contentUri)
            val raw = retriever.extractMetadata(1001)
            raw?.takeIf { it.isNotBlank() }
        }.getOrNull().also {
            runCatching { retriever.release() }
        }
    }

    private fun readBoundedHeaderBytes(song: Song, maxBytes: Int): ByteArray? {
        val stream = runCatching { context.contentResolver.openInputStream(song.contentUri) }.getOrNull()
            ?: song.filePath.takeIf { it.isNotBlank() }?.let { path ->
                runCatching { File(path).takeIf { it.exists() && it.isFile }?.inputStream() }.getOrNull()
            }
            ?: return null
        return stream.use { input ->
            val buf = ByteArray(maxBytes)
            var total = 0
            while (total < maxBytes) {
                val n = input.read(buf, total, maxBytes - total)
                if (n <= 0) break
                total += n
            }
            if (total == maxBytes) buf else buf.copyOf(total)
        }
    }

    private fun parseFlacEmbeddedLyrics(data: ByteArray): String? {
        if (data.size < 8 || data[0] != 'f'.code.toByte() || data[1] != 'L'.code.toByte() ||
            data[2] != 'a'.code.toByte() || data[3] != 'C'.code.toByte()
        ) return null
        var offset = 4
        while (offset + 4 <= data.size) {
            val header = data[offset].toInt() and 0xFF
            val isLast = (header and 0x80) != 0
            val blockType = header and 0x7F
            val length = ((data[offset + 1].toInt() and 0xFF) shl 16) or
                ((data[offset + 2].toInt() and 0xFF) shl 8) or
                (data[offset + 3].toInt() and 0xFF)
            offset += 4
            if (offset + length > data.size) break
            if (blockType == 4 && length > 8) {
                var pos = offset
                fun readLe32(): Int {
                    if (pos + 4 > offset + length) return -1
                    val v = (data[pos].toInt() and 0xFF) or
                        ((data[pos + 1].toInt() and 0xFF) shl 8) or
                        ((data[pos + 2].toInt() and 0xFF) shl 16) or
                        ((data[pos + 3].toInt() and 0xFF) shl 24)
                    pos += 4
                    return v
                }
                val vendorLen = readLe32()
                if (vendorLen < 0 || pos + vendorLen > offset + length) return null
                pos += vendorLen
                val commentCount = readLe32()
                if (commentCount <= 0 || commentCount > 2048) return null
                for (i in 0 until commentCount) {
                    val cLen = readLe32()
                    if (cLen <= 0 || pos + cLen > offset + length) break
                    val comment = String(data, pos, cLen, Charsets.UTF_8)
                    pos += cLen
                    val eq = comment.indexOf('=')
                    if (eq > 0) {
                        val key = comment.substring(0, eq).uppercase()
                        if (key == "LYRICS" || key == "UNSYNCEDLYRICS" || key == "SYLT") {
                            val value = comment.substring(eq + 1).trim()
                            if (value.isNotBlank()) return value
                        }
                    }
                }
            }
            offset += length
            if (isLast) break
        }
        return null
    }

    private fun parseId3v2UsltLyrics(data: ByteArray): String? {
        if (data.size < 10 || data[0] != 'I'.code.toByte() || data[1] != 'D'.code.toByte() ||
            data[2] != '3'.code.toByte()
        ) return null
        val versionMajor = data[3].toInt() and 0xFF
        val flags = data[5].toInt() and 0xFF
        val tagSize = ((data[6].toInt() and 0x7F) shl 21) or
            ((data[7].toInt() and 0x7F) shl 14) or
            ((data[8].toInt() and 0x7F) shl 7) or
            (data[9].toInt() and 0x7F)
        val tagEnd = (10 + tagSize).coerceAtMost(data.size)
        var pos = 10
        if ((flags and 0x40) != 0 && pos + 4 <= tagEnd) {
            val extSize = ((data[pos].toInt() and 0xFF) shl 24) or
                ((data[pos + 1].toInt() and 0xFF) shl 16) or
                ((data[pos + 2].toInt() and 0xFF) shl 8) or
                (data[pos + 3].toInt() and 0xFF)
            pos += extSize.coerceAtLeast(4)
        }
        while (pos + 10 <= tagEnd) {
            val frameId = String(data, pos, 4, Charsets.ISO_8859_1)
            if (frameId[0] == '\u0000') break
            val frameSize = if (versionMajor >= 4) {
                ((data[pos + 4].toInt() and 0x7F) shl 21) or
                    ((data[pos + 5].toInt() and 0x7F) shl 14) or
                    ((data[pos + 6].toInt() and 0x7F) shl 7) or
                    (data[pos + 7].toInt() and 0x7F)
            } else {
                ((data[pos + 4].toInt() and 0xFF) shl 24) or
                    ((data[pos + 5].toInt() and 0xFF) shl 16) or
                    ((data[pos + 6].toInt() and 0xFF) shl 8) or
                    (data[pos + 7].toInt() and 0xFF)
            }
            pos += 10
            if (frameSize <= 0 || pos + frameSize > tagEnd) break
            if (frameId == "USLT" && frameSize > 5) {
                val enc = data[pos].toInt() and 0xFF
                val start = pos + 4 // skip encoding (1) + language (3)
                val end = pos + frameSize
                val charset = when (enc) {
                    1 -> Charsets.UTF_16
                    2 -> Charsets.UTF_16BE
                    3 -> Charsets.UTF_8
                    else -> Charsets.ISO_8859_1
                }
                val isWide = enc == 1 || enc == 2
                var textStart = start
                if (isWide) {
                    while (textStart + 1 < end) {
                        if (data[textStart] == 0.toByte() && data[textStart + 1] == 0.toByte()) {
                            textStart += 2
                            break
                        }
                        textStart += 2
                    }
                } else {
                    while (textStart < end) {
                        if (data[textStart] == 0.toByte()) {
                            textStart += 1
                            break
                        }
                        textStart += 1
                    }
                }
                if (textStart < end) {
                    val decoded = String(data, textStart, end - textStart, charset).trim('\u0000', ' ', '\r', '\n')
                    if (decoded.isNotBlank()) return decoded
                }
            }
            pos += frameSize
        }
        return null
    }

    private fun parseMp4EmbeddedLyrics(data: ByteArray): String? {
        // Locate "©lyr" atom (0xA9, 'l', 'y', 'r') followed by nested "data" box
        val target = byteArrayOf(0xA9.toByte(), 'l'.code.toByte(), 'y'.code.toByte(), 'r'.code.toByte())
        for (i in 0 until (data.size - 24)) {
            if (data[i] == target[0] && data[i + 1] == target[1] &&
                data[i + 2] == target[2] && data[i + 3] == target[3]
            ) {
                // Inside ©lyr: next 4 bytes = data box size, next 4 bytes = "data", then 8 bytes flags/reserved
                val dataBoxSize = ((data[i + 4].toInt() and 0xFF) shl 24) or
                    ((data[i + 5].toInt() and 0xFF) shl 16) or
                    ((data[i + 6].toInt() and 0xFF) shl 8) or
                    (data[i + 7].toInt() and 0xFF)
                val isData = data[i + 8] == 'd'.code.toByte() && data[i + 9] == 'a'.code.toByte() &&
                    data[i + 10] == 't'.code.toByte() && data[i + 11] == 'a'.code.toByte()
                val textStart = i + 20
                val textLen = dataBoxSize - 16
                if (isData && textLen > 4 && textStart + textLen <= data.size) {
                    val text = String(data, textStart, textLen, Charsets.UTF_8).trim()
                    if (text.isNotBlank()) return text
                }
            }
        }
        return null
    }

    private fun findLrcFile(song: Song): File? {
        if (song.filePath.isNotBlank()) {
            val audioFile = File(song.filePath)
            val sibling = File(audioFile.parentFile, "${audioFile.nameWithoutExtension}.lrc")
            if (sibling.exists() && sibling.isFile) return sibling
        }
        val externalRoot = Environment.getExternalStorageDirectory()
        val fallbackDirs = listOf(
            File(externalRoot, "Music"),
            File(externalRoot, "Lyrics"),
            File(externalRoot, "Download"),
        )
        for (dir in fallbackDirs) {
            val byTitle = File(dir, "${song.title}.lrc")
            if (byTitle.exists() && byTitle.isFile) return byTitle
            val byArtistTitle = File(dir, "${song.artist} - ${song.title}.lrc")
            if (byArtistTitle.exists() && byArtistTitle.isFile) return byArtistTitle
        }
        return null
    }

    private fun readTextWithFallback(file: File): String? = runCatching {
        val bytes = file.readBytes()
        val utf8 = bytes.toString(Charsets.UTF_8)
        if (!utf8.contains('\uFFFD')) {
            utf8
        } else {
            runCatching { bytes.toString(Charset.forName("GBK")) }.getOrDefault(utf8)
        }
    }.getOrNull()

    private fun decodeHtmlEntities(input: String): String {
        if (!input.contains('&')) return input
        return input
            .replace("&apos;", "'")
            .replace("&#39;", "'")
            .replace("&#x27;", "'")
            .replace("&quot;", "\"")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&nbsp;", " ")
    }

    private val enhancedWordRegex = Regex("""<(\d{1,2}):(\d{2})(?:[.:](\d{1,3}))?>([^<]*)""")
    private val yrcLineRegex = Regex("""^\[(\d+),(\d+)](.+)$""")
    private val yrcWordRegex = Regex("""\((\d+),(\d+),\d+\)([^\(]*)""")
    private val ttmlLineRegex = Regex("""<p[^>]*begin="([^"]+)"[^>]*>([\s\S]*?)</p>""", RegexOption.IGNORE_CASE)
    private val ttmlSpanRegex = Regex("""<span[^>]*begin="([^"]+)"[^>]*end="([^"]+)"[^>]*>([^<]*)</span>""", RegexOption.IGNORE_CASE)

    private fun parseClockToMs(clock: String): Long {
        val parts = clock.trim().split(":")
        return when (parts.size) {
            3 -> {
                val h = parts[0].toLongOrNull() ?: 0L
                val m = parts[1].toLongOrNull() ?: 0L
                val s = ((parts[2].toDoubleOrNull() ?: 0.0) * 1000.0).toLong()
                h * 3_600_000L + m * 60_000L + s
            }
            2 -> {
                val m = parts[0].toLongOrNull() ?: 0L
                val s = ((parts[1].toDoubleOrNull() ?: 0.0) * 1000.0).toLong()
                m * 60_000L + s
            }
            else -> ((clock.removeSuffix("s").toDoubleOrNull() ?: 0.0) * 1000.0).toLong()
        }
    }

    private fun parseTimeTagMs(minStr: String, secStr: String, fracRaw: String): Long {
        val minutes = minStr.toLongOrNull() ?: 0L
        val seconds = secStr.toLongOrNull() ?: 0L
        val millis = when (fracRaw.length) {
            0 -> 0L
            1 -> (fracRaw.toLongOrNull() ?: 0L) * 100L
            2 -> (fracRaw.toLongOrNull() ?: 0L) * 10L
            else -> fracRaw.take(3).toLongOrNull() ?: 0L
        }
        return minutes * 60_000L + seconds * 1_000L + millis
    }

    private fun parseTtml(raw: String): List<LyricLine> {
        val out = mutableListOf<LyricLine>()
        ttmlLineRegex.findAll(raw).forEach { m ->
            val startMs = parseClockToMs(m.groupValues[1])
            val inner = m.groupValues[2]
            val words = mutableListOf<LyricWord>()
            ttmlSpanRegex.findAll(inner).forEach { sm ->
                val ws = parseClockToMs(sm.groupValues[1])
                val we = parseClockToMs(sm.groupValues[2]).coerceAtLeast(ws + 60L)
                val wt = decodeHtmlEntities(sm.groupValues[3])
                if (wt.isNotEmpty()) words.add(LyricWord(wt, ws, we))
            }
            val fullText = if (words.isNotEmpty()) {
                words.joinToString("") { it.text }.trim()
            } else {
                decodeHtmlEntities(inner.replace(Regex("<[^>]+>"), "").trim())
            }
            if (fullText.isNotEmpty()) {
                out.add(LyricLine(timeMs = startMs, primaryText = fullText, words = words))
            }
        }
        return out.sortedBy { it.timeMs }
    }

    private fun parseYrc(raw: String): List<LyricLine> {
        val out = mutableListOf<LyricLine>()
        raw.lineSequence().forEach { line ->
            val lm = yrcLineRegex.matchEntire(line.trim()) ?: return@forEach
            val lineStart = lm.groupValues[1].toLongOrNull() ?: return@forEach
            val body = lm.groupValues[3]
            val words = mutableListOf<LyricWord>()
            yrcWordRegex.findAll(body).forEach { wm ->
                val ws = wm.groupValues[1].toLongOrNull() ?: lineStart
                val wd = (wm.groupValues[2].toLongOrNull() ?: 120L).coerceAtLeast(40L)
                val wt = decodeHtmlEntities(wm.groupValues[3])
                if (wt.isNotEmpty()) words.add(LyricWord(wt, ws, ws + wd))
            }
            if (words.isNotEmpty()) {
                val fullText = words.joinToString("") { it.text }.trim()
                if (fullText.isNotEmpty()) {
                    out.add(LyricLine(timeMs = lineStart, primaryText = fullText, words = words))
                }
            }
        }
        return out.sortedBy { it.timeMs }
    }

    fun parseLrc(raw: String): List<LyricLine> {
        // 1. Detect TTML word-by-word XML lyrics
        if (raw.contains("<tt", ignoreCase = true) && raw.contains("<p", ignoreCase = true)) {
            val ttml = parseTtml(raw)
            if (ttml.isNotEmpty()) return ttml
        }
        // 2. Detect YRC word-by-word lyrics
        if (raw.lineSequence().any { yrcLineRegex.matches(it.trim()) }) {
            val yrc = parseYrc(raw)
            if (yrc.isNotEmpty()) return yrc
        }

        // 3. Standard & Enhanced LRC (<mm:ss.xx> inline word tags)
        val rawEntries = mutableListOf<Triple<Long, String, List<LyricWord>>>()
        raw.lineSequence().forEach { line ->
            val matches = timeTagRegex.findAll(line).toList()
            if (matches.isEmpty()) return@forEach
            val bodyRaw = line.replace(timeTagRegex, "").trim()
            if (bodyRaw.isEmpty()) return@forEach

            val wordMatches = enhancedWordRegex.findAll(bodyRaw).toList()
            val parsedWords = mutableListOf<LyricWord>()
            val cleanText = if (wordMatches.isNotEmpty()) {
                val tmp = wordMatches.map { wm ->
                    val ws = parseTimeTagMs(wm.groupValues[1], wm.groupValues[2], wm.groupValues[3])
                    ws to decodeHtmlEntities(wm.groupValues[4])
                }.filter { it.second.isNotEmpty() }
                tmp.forEachIndexed { idx, (ws, wt) ->
                    val nextWs = tmp.getOrNull(idx + 1)?.first ?: (ws + 450L)
                    parsedWords.add(LyricWord(wt, ws, nextWs.coerceAtLeast(ws + 60L)))
                }
                parsedWords.joinToString("") { it.text }.trim()
            } else {
                decodeHtmlEntities(bodyRaw)
            }
            if (cleanText.isEmpty()) return@forEach

            matches.forEach { match ->
                val totalMs = parseTimeTagMs(match.groupValues[1], match.groupValues[2], match.groupValues[3])
                rawEntries.add(Triple(totalMs, cleanText, parsedWords))
            }
        }

        if (rawEntries.isEmpty()) return emptyList()
        val grouped = linkedMapOf<Long, MutableList<Pair<String, List<LyricWord>>>>()
        rawEntries.sortedBy { it.first }.forEach { (timeMs, text, words) ->
            grouped.getOrPut(timeMs) { mutableListOf() }.add(text to words)
        }

        return grouped.entries.map { (timeMs, items) ->
            LyricLine(
                timeMs = timeMs,
                primaryText = items.first().first,
                secondaryText = items.getOrNull(1)?.first,
                words = items.first().second,
            )
        }
    }
}
