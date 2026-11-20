package com.puremusic.player.ui.theme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import android.net.Uri
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class DynamicSongPalette(
    val glowColor: Color = BerryRed,
    val accent: Color = BerryRed,
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
        var bestScore = -1f
        var bestPixel = AndroidColor.rgb(250, 45, 72)

        val hsv = FloatArray(3)
        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                val pixel = bitmap.getPixel(x, y)
                val r = AndroidColor.red(pixel)
                val g = AndroidColor.green(pixel)
                val b = AndroidColor.blue(pixel)
                AndroidColor.RGBToHSV(r, g, b, hsv)
                val sat = hsv[1]
                val value = hsv[2]
                if (value in 0.25f..0.95f && sat > 0.25f) {
                    val score = sat * 1.6f + (1f - kotlin.math.abs(value - 0.62f))
                    if (score > bestScore) {
                        bestScore = score
                        bestPixel = pixel
                    }
                }
            }
        }

        if (bestScore <= 0f) return DynamicSongPalette()

        // Vibrant contemporary accent (fresh, jewel-like, never muddy)
        AndroidColor.colorToHSV(bestPixel, hsv)
        hsv[1] = hsv[1].coerceIn(0.55f, 0.88f)
        hsv[2] = hsv[2].coerceIn(0.55f, 0.88f)
        val glow = Color(AndroidColor.HSVToColor(hsv))

        hsv[2] = hsv[2].coerceIn(0.45f, 0.72f)
        val accent = Color(AndroidColor.HSVToColor(hsv))

        return DynamicSongPalette(glowColor = glow, accent = accent)
    }
}
