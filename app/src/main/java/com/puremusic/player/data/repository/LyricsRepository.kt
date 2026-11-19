package com.puremusic.player.data.repository

import android.os.Environment
import com.puremusic.player.data.model.LyricLine
import com.puremusic.player.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.charset.Charset

class LyricsRepository {

    private val timeTagRegex = Regex("\\[(\\d{1,2}):(\\d{2})(?:[.:](\\d{1,3}))?]")

    suspend fun loadLyricsForSong(song: Song): List<LyricLine> = withContext(Dispatchers.IO) {
        val candidate = findLrcFile(song) ?: return@withContext emptyList()
        val rawText = readTextWithFallback(candidate) ?: return@withContext emptyList()
        parseLrc(rawText)
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
        if (!utf8.contains('�')) {
            utf8
        } else {
            runCatching { bytes.toString(Charset.forName("GBK")) }.getOrDefault(utf8)
        }
    }.getOrNull()

    fun parseLrc(raw: String): List<LyricLine> {
        val rawEntries = mutableListOf<Pair<Long, String>>()
        raw.lineSequence().forEach { line ->
            val matches = timeTagRegex.findAll(line).toList()
            if (matches.isEmpty()) return@forEach
            val text = line.replace(timeTagRegex, "").trim()
            if (text.isEmpty()) return@forEach

            matches.forEach { match ->
                val minutes = match.groupValues[1].toLongOrNull() ?: 0L
                val seconds = match.groupValues[2].toLongOrNull() ?: 0L
                val fracRaw = match.groupValues[3]
                val millis = when (fracRaw.length) {
                    0 -> 0L
                    1 -> (fracRaw.toLongOrNull() ?: 0L) * 100L
                    2 -> (fracRaw.toLongOrNull() ?: 0L) * 10L
                    else -> fracRaw.take(3).toLongOrNull() ?: 0L
                }
                val totalMs = minutes * 60_000L + seconds * 1_000L + millis
                rawEntries.add(totalMs to text)
            }
        }

        if (rawEntries.isEmpty()) return emptyList()
        val grouped = linkedMapOf<Long, MutableList<String>>()
        rawEntries.sortedBy { it.first }.forEach { (timeMs, text) ->
            grouped.getOrPut(timeMs) { mutableListOf() }.add(text)
        }

        return grouped.entries.map { (timeMs, lines) ->
            LyricLine(
                timeMs = timeMs,
                primaryText = lines.first(),
                secondaryText = lines.getOrNull(1),
            )
        }
    }
}
