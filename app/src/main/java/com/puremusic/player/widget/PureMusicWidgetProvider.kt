package com.puremusic.player.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.SweepGradient
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import com.puremusic.player.MainActivity
import com.puremusic.player.R
import com.puremusic.player.service.PlaybackService
import com.puremusic.player.ui.components.AudioArtFetcher

open class PureMusicWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        updateWidgetsForLayout(
            context = context,
            manager = appWidgetManager,
            appWidgetIds = appWidgetIds,
            layoutRes = R.layout.widget_pure_music,
            highRes = false,
            title = cachedTitle,
            artist = cachedArtist,
            isPlaying = cachedIsPlaying,
            artUri = cachedArtUri,
            isLossless = cachedIsLossless,
        )
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_TOGGLE, ACTION_NEXT, ACTION_PREV -> {
                val serviceIntent = Intent(context, PlaybackService::class.java).apply {
                    action = intent.action
                }
                runCatching { context.startForegroundService(serviceIntent) }
                    .onFailure { runCatching { context.startService(serviceIntent) } }
            }
        }
    }

    companion object {
        const val ACTION_TOGGLE = "com.puremusic.player.WIDGET_TOGGLE"
        const val ACTION_NEXT = "com.puremusic.player.WIDGET_NEXT"
        const val ACTION_PREV = "com.puremusic.player.WIDGET_PREV"

        @Volatile internal var cachedTitle: String? = null
        @Volatile internal var cachedArtist: String? = null
        @Volatile internal var cachedIsPlaying: Boolean = false
        @Volatile internal var cachedArtUri: Uri? = null
        @Volatile internal var cachedIsLossless: Boolean = false

        fun pushState(
            context: Context,
            title: String?,
            artist: String?,
            isPlaying: Boolean,
            artUri: Uri? = null,
            isLossless: Boolean = false,
        ) {
            cachedTitle = title
            cachedArtist = artist
            cachedIsPlaying = isPlaying
            cachedArtUri = artUri
            cachedIsLossless = isLossless

            val manager = AppWidgetManager.getInstance(context)
            val compactIds = manager.getAppWidgetIds(ComponentName(context, PureMusicWidgetProvider::class.java))
            if (compactIds.isNotEmpty()) {
                updateWidgetsForLayout(
                    context = context,
                    manager = manager,
                    appWidgetIds = compactIds,
                    layoutRes = R.layout.widget_pure_music,
                    highRes = false,
                    title = title,
                    artist = artist,
                    isPlaying = isPlaying,
                    artUri = artUri,
                    isLossless = isLossless,
                )
            }

            val largeIds = manager.getAppWidgetIds(ComponentName(context, PureMusicWidgetLargeProvider::class.java))
            if (largeIds.isNotEmpty()) {
                updateWidgetsForLayout(
                    context = context,
                    manager = manager,
                    appWidgetIds = largeIds,
                    layoutRes = R.layout.widget_pure_music_4x2,
                    highRes = true,
                    title = title,
                    artist = artist,
                    isPlaying = isPlaying,
                    artUri = artUri,
                    isLossless = isLossless,
                )
            }
        }

        internal fun updateWidgetsForLayout(
            context: Context,
            manager: AppWidgetManager,
            appWidgetIds: IntArray,
            layoutRes: Int,
            highRes: Boolean,
            title: String? = null,
            artist: String? = null,
            isPlaying: Boolean = false,
            artUri: Uri? = null,
            isLossless: Boolean = false,
        ) {
            if (appWidgetIds.isEmpty()) return
            val openAppIntent = PendingIntent.getActivity(
                context,
                10,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val turntableBitmap = renderTurntableWidgetBitmap(context, artUri, highRes)

            appWidgetIds.forEach { widgetId ->
                val views = RemoteViews(context.packageName, layoutRes)
                views.setTextViewText(R.id.widget_title, title?.takeIf { it.isNotBlank() } ?: "简约音乐")
                views.setTextViewText(R.id.widget_artist, artist?.takeIf { it.isNotBlank() } ?: "点击打开本地曲库")
                views.setViewVisibility(R.id.widget_badge, if (isLossless) View.VISIBLE else View.GONE)
                views.setImageViewBitmap(R.id.widget_art, turntableBitmap)
                views.setImageViewResource(
                    R.id.widget_play_pause,
                    if (isPlaying) R.drawable.ic_widget_pause else R.drawable.ic_widget_play,
                )
                views.setOnClickPendingIntent(R.id.widget_root, openAppIntent)
                views.setOnClickPendingIntent(R.id.widget_play_pause, actionPendingIntent(context, ACTION_TOGGLE, 11))
                views.setOnClickPendingIntent(R.id.widget_next, actionPendingIntent(context, ACTION_NEXT, 12))
                views.setOnClickPendingIntent(R.id.widget_prev, actionPendingIntent(context, ACTION_PREV, 13))
                manager.updateAppWidget(widgetId, views)
            }
        }

        private fun renderTurntableWidgetBitmap(context: Context, uri: Uri?, highRes: Boolean): Bitmap {
            // Safe unclipped canvas geometry: discCx + discRadius + margin <= w
            val w = if (highRes) 420 else 236
            val h = if (highRes) 308 else 176
            val sleeveSize = if (highRes) 280 else 160
            val sleeveLeft = if (highRes) 10f else 6f
            val sleeveTop = (h - sleeveSize) / 2f
            val discRadius = if (highRes) 128f else 72f
            val discCx = sleeveLeft + sleeveSize * (if (highRes) 0.95f else 0.92f)
            val discCy = h / 2f
            val cornerRadius = if (highRes) 24f else 14f

            val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(out)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            // 1. Draw complete unclipped glossy black vinyl disc peeking out from the right
            paint.color = Color.parseColor("#0B0B0D")
            canvas.drawCircle(discCx, discCy, discRadius, paint)

            val sweepPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            val sweep = SweepGradient(
                discCx,
                discCy,
                intArrayOf(
                    Color.TRANSPARENT,
                    Color.argb(80, 238, 240, 244),
                    Color.TRANSPARENT,
                    Color.argb(72, 238, 240, 244),
                    Color.TRANSPARENT,
                ),
                floatArrayOf(0.0f, 0.14f, 0.30f, 0.64f, 0.85f),
            )
            val mat = Matrix().apply { postRotate(-25f, discCx, discCy) }
            sweep.setLocalMatrix(mat)
            sweepPaint.shader = sweep
            canvas.drawCircle(discCx, discCy, discRadius * 0.96f, sweepPaint)

            val groovePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = if (highRes) 1.4f else 1.0f
            }
            val grooveSteps = if (highRes) 24 else 16
            for (i in 0..grooveSteps) {
                val frac = 0.42f + (i.toFloat() / grooveSteps) * 0.52f
                val r = discRadius * frac
                groovePaint.color = if (i % 2 == 0) Color.argb(32, 255, 255, 255) else Color.argb(82, 0, 0, 0)
                canvas.drawCircle(discCx, discCy, r, groovePaint)
            }

            // Subtle vinyl center label ring & outer rim highlight
            val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = if (highRes) 1.5f else 1.0f
                color = Color.argb(45, 255, 255, 255)
            }
            canvas.drawCircle(discCx, discCy, discRadius - 1f, rimPaint)

            val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#C83E2B") }
            canvas.drawCircle(discCx, discCy, discRadius * 0.28f, labelPaint)
            labelPaint.color = Color.parseColor("#18181B")
            canvas.drawCircle(discCx, discCy, discRadius * 0.06f, labelPaint)

            // 2. Decode album cover (per-song or fallback dark vinyl sleeve)
            val rawCover = uri?.let { loadRawBitmap(context, it, sleeveSize) }
            val coverRounded = Bitmap.createBitmap(sleeveSize, sleeveSize, Bitmap.Config.ARGB_8888)
            val cCanvas = Canvas(coverRounded)
            val cPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            val rect = Rect(0, 0, sleeveSize, sleeveSize)
            val rectF = RectF(rect)
            cCanvas.drawRoundRect(rectF, cornerRadius, cornerRadius, cPaint)
            cPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            if (rawCover != null) {
                cCanvas.drawBitmap(rawCover, null, rect, cPaint)
                rawCover.recycle()
            } else {
                val fallbackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#24211F") }
                cCanvas.drawRect(rectF, fallbackPaint)
                fallbackPaint.color = Color.parseColor("#C83E2B")
                cCanvas.drawCircle(sleeveSize / 2f, sleeveSize / 2f, sleeveSize * 0.14f, fallbackPaint)
            }

            // Soft drop shadow behind sleeve edge over the vinyl disc
            val shadowOffset = if (highRes) 8f else 5f
            val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(72, 0, 0, 0)
            }
            canvas.drawRoundRect(
                RectF(
                    sleeveLeft + shadowOffset,
                    sleeveTop + 3f,
                    sleeveLeft + sleeveSize + shadowOffset * 0.7f,
                    sleeveTop + sleeveSize + 3f,
                ),
                cornerRadius,
                cornerRadius,
                shadowPaint,
            )
            canvas.drawBitmap(coverRounded, sleeveLeft, sleeveTop, null)
            coverRounded.recycle()
            return out
        }

        private fun loadRawBitmap(context: Context, uri: Uri, targetPx: Int): Bitmap? {
            return runCatching {
                if (uri.toString().contains("/audio/media/")) {
                    AudioArtFetcher.loadPerSongBitmap(context, uri, targetPx)
                } else {
                    val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
                    val sample = maxOf(1, minOf(opts.outWidth, opts.outHeight) / targetPx)
                    val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sample }
                    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, decodeOpts) }
                }
            }.getOrNull()
        }

        private fun actionPendingIntent(context: Context, action: String, reqCode: Int): PendingIntent {
            val intent = Intent(context, PureMusicWidgetProvider::class.java).apply { this.action = action }
            return PendingIntent.getBroadcast(
                context,
                reqCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
    }
}
