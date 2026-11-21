package com.puremusic.player.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.util.Size
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.Options

/**
 * Loads exact per-song embedded artwork directly from each song's MediaStore audio contentUri,
 * eliminating the Android MediaStore bug where unrelated songs in the same folder share one albumId cover.
 */
class AudioArtFetcher(
    private val context: Context,
    private val uri: Uri,
) : Fetcher {

    override suspend fun fetch(): FetchResult? {
        val bitmap: Bitmap? = loadPerSongBitmap(context, uri, 420)
        if (bitmap == null) return null
        return DrawableResult(
            drawable = BitmapDrawable(context.resources, bitmap),
            isSampled = true,
            dataSource = DataSource.DISK,
        )
    }

    class Factory(private val context: Context) : Fetcher.Factory<Uri> {
        override fun create(data: Uri, options: Options, imageLoader: ImageLoader): Fetcher? {
            if (data.scheme == "content" && data.toString().contains("/audio/media/")) {
                return AudioArtFetcher(context, data)
            }
            return null
        }
    }

    companion object {
        fun loadPerSongBitmap(context: Context, uri: Uri, targetPx: Int = 420): Bitmap? {
            // 1. Android 10+ native per-item MediaProvider thumbnail (reads embedded cover of this exact audio file)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val thumb = runCatching {
                    context.contentResolver.loadThumbnail(uri, Size(targetPx, targetPx), null)
                }.getOrNull()
                if (thumb != null) return thumb
            }

            // 2. Direct MediaMetadataRetriever embeddedPicture fallback
            val retriever = MediaMetadataRetriever()
            return runCatching {
                retriever.setDataSource(context, uri)
                val bytes = retriever.embeddedPicture ?: return@runCatching null
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
                val sample = maxOf(1, minOf(bounds.outWidth, bounds.outHeight) / targetPx)
                val opts = BitmapFactory.Options().apply { inSampleSize = sample }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
            }.getOrNull().also {
                runCatching { retriever.release() }
            }
        }
    }
}
