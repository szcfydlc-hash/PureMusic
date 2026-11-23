package com.puremusic.player.ui.theme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import android.net.Uri
import androidx.compose.ui.graphics.Color
import com.puremusic.player.ui.components.AudioArtFetcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.cos
import kotlin.math.sin

data class DynamicSongPalette(
    val glowColor: Color = Color(0xFF93A6B2),
    val accent: Color = Color(0xFF7B8E9B),
)

object DynamicColorExtractor {

    suspend fun extractFromUri(context: Context, albumArtUri: Uri?): DynamicSongPalette =
        withContext(Dispatchers.IO) {
            if (albumArtUri == null) return@withContext DynamicSongPalette()
            val bitmap = decodeSmallBitmap(context, albumArtUri) ?: return@withContext DynamicSongPalette()
            try {
                analyzeBitmap(bitmap)
            } finally {
                bitmap.recycle()
            }
        }

    private fun decodeSmallBitmap(context: Context, uri: Uri): Bitmap? = runCatching {
        val opts = BitmapFactory.Options().apply { inSampleSize = 8 }
        if (uri.toString().contains("/audio/media/")) {
            AudioArtFetcher.loadPerSongBitmap(context, uri, 96)
        } else {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val decoded = BitmapFactory.decodeStream(stream, null, opts)
                if (decoded != null) {
                    val scaled = Bitmap.createScaledBitmap(decoded, 28, 28, true)
                    if (scaled != decoded) decoded.recycle()
                    scaled
                } else {
                    null
                }
            }
        }
    }.getOrNull()

    /**
     * Dominant-hue 24-bin histogram averaging:
     * Reflects the album cover's true primary color region instead of picking a single
     * noisy high-saturation corner pixel, and keeps dark-mode tints breathable and refined.
     */
    private fun analyzeBitmap(bitmap: Bitmap): DynamicSongPalette {
        val binCount = 24
        val binWeights = FloatArray(binCount)
        val binSin = FloatArray(binCount)
        val binCos = FloatArray(binCount)
        val binSat = FloatArray(binCount)
        val binVal = FloatArray(binCount)

        val hsv = FloatArray(3)
        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                val pixel = bitmap.getPixel(x, y)
                val r = AndroidColor.red(pixel)
                val g = AndroidColor.green(pixel)
                val b = AndroidColor.blue(pixel)
                AndroidColor.RGBToHSV(r, g, b, hsv)
                val hue = hsv[0]
                val sat = hsv[1]
                val value = hsv[2]

                // Skip near-black shadows and near-white highlights
                if (value < 0.14f || (value > 0.96f && sat < 0.08f)) continue
                if (sat < 0.10f) continue

                val bin = ((hue / 15f).toInt()).coerceIn(0, binCount - 1)
                val rad = Math.toRadians(hue.toDouble())
                val weight = 0.35f + sat * 0.95f
                binWeights[bin] += weight
                binSin[bin] += (sin(rad) * weight).toFloat()
                binCos[bin] += (cos(rad) * weight).toFloat()
                binSat[bin] += sat * weight
                binVal[bin] += value * weight
            }
        }

        var bestBin = -1
        var bestWeight = 0f
        for (i in 0 until binCount) {
            if (binWeights[i] > bestWeight) {
                bestWeight = binWeights[i]
                bestBin = i
            }
        }

        if (bestBin < 0 || bestWeight <= 0f) {
            return DynamicSongPalette()
        }

        // Smoothly merge adjacent hue bins if they share weight so boundary hues stay accurate
        var totalW = 0f
        var sumSin = 0f
        var sumCos = 0f
        var sumSat = 0f
        var sumVal = 0f
        for (offset in -1..1) {
            val idx = (bestBin + offset + binCount) % binCount
            val factor = if (offset == 0) 1f else 0.45f
            totalW += binWeights[idx] * factor
            sumSin += binSin[idx] * factor
            sumCos += binCos[idx] * factor
            sumSat += binSat[idx] * factor
            sumVal += binVal[idx] * factor
        }

        var avgHue = Math.toDegrees(kotlin.math.atan2(sumSin.toDouble(), sumCos.toDouble())).toFloat()
        if (avgHue < 0f) avgHue += 360f
        val avgSat = (sumSat / totalW).coerceIn(0f, 1f)
        val avgVal = (sumVal / totalW).coerceIn(0f, 1f)

        // Soft, luminous mist accent (for halos, dark-mode icons, waveform highlights)
        hsv[0] = avgHue
        hsv[1] = avgSat.coerceIn(0.26f, 0.56f)
        hsv[2] = avgVal.coerceIn(0.68f, 0.88f)
        val glow = Color(AndroidColor.HSVToColor(hsv))

        // Breathable mid-tone muted hue (for airy dark-mode surface blends)
        hsv[0] = avgHue
        hsv[1] = avgSat.coerceIn(0.24f, 0.50f)
        hsv[2] = avgVal.coerceIn(0.46f, 0.68f)
        val accent = Color(AndroidColor.HSVToColor(hsv))

        return DynamicSongPalette(glowColor = glow, accent = accent)
    }
}
