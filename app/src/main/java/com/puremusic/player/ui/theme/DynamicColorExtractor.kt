package com.puremusic.player.ui.theme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class DynamicSongPalette(
    val backgroundLight: Color = WarmPaperBg,
    val backgroundDark: Color = Color(0xFF16181A),
    val accent: Color = ForestGreen,
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
        context.contentResolver.openInputStream(uri)?.use { stream ->
            val decoded = BitmapFactory.decodeStream(stream, null, opts)
            if (decoded != null) {
                val scaled = Bitmap.createScaledBitmap(decoded, 24, 24, true)
                if (scaled != decoded) decoded.recycle()
                scaled
            } else {
                null
            }
        }
    }.getOrNull()

    private fun analyzeBitmap(bitmap: Bitmap): DynamicSongPalette {
        var rSum = 0L
        var gSum = 0L
        var bSum = 0L
        var count = 0L

        var bestSatScore = -1f
        var bestAccentPixel = AndroidColor.rgb(17, 85, 59)

        val hsv = FloatArray(3)
        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                val pixel = bitmap.getPixel(x, y)
                val r = AndroidColor.red(pixel)
                val g = AndroidColor.green(pixel)
                val b = AndroidColor.blue(pixel)
                rSum += r
                gSum += g
                bSum += b
                count++

                AndroidColor.RGBToHSV(r, g, b, hsv)
                val sat = hsv[1]
                val value = hsv[2]
                // Prefer rich, non-muddy colors for the Play button & seek dot
                if (value in 0.22f..0.88f && sat > 0.22f) {
                    val score = sat * 1.4f + (1f - kotlin.math.abs(value - 0.50f))
                    if (score > bestSatScore) {
                        bestSatScore = score
                        bestAccentPixel = pixel
                    }
                }
            }
        }

        if (count == 0L) return DynamicSongPalette()

        val avgColor = Color(
            red = (rSum / count).toInt() / 255f,
            green = (gSum / count).toInt() / 255f,
            blue = (bSum / count).toInt() / 255f,
        )

        // Ensure accent has rich, legible contrast with white play/pause icon
        AndroidColor.colorToHSV(bestAccentPixel, hsv)
        hsv[1] = hsv[1].coerceIn(0.38f, 0.78f)
        hsv[2] = hsv[2].coerceIn(0.26f, 0.46f)
        val refinedAccent = if (bestSatScore > 0f) {
            Color(AndroidColor.HSVToColor(hsv))
        } else {
            ForestGreen
        }

        // Gentle warm-paper wash tinted by album art dominant color
        val bgLight = lerp(WarmPaperBg, avgColor, 0.14f)
        val bgDark = lerp(Color(0xFF141517), avgColor, 0.22f)

        return DynamicSongPalette(
            backgroundLight = bgLight,
            backgroundDark = bgDark,
            accent = refinedAccent,
        )
    }
}
