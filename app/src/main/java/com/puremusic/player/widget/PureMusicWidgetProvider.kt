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

class PureMusicWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        updateAllWidgets(context, appWidgetManager, appWidgetIds)
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

        fun pushState(
            context: Context,
            title: String?,
            artist: String?,
            isPlaying: Boolean,
            artUri: Uri? = null,
            isLossless: Boolean = false,
        ) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, PureMusicWidgetProvider::class.java))
            if (ids.isEmpty()) return
            updateAllWidgets(context, manager, ids, title, artist, isPlaying, artUri, isLossless)
        }

        private fun updateAllWidgets(
            context: Context,
            manager: AppWidgetManager,
            appWidgetIds: IntArray,
            title: String? = null,
            artist: String? = null,
            isPlaying: Boolean = false,
            artUri: Uri? = null,
            isLossless: Boolean = false,
        ) {
            val openAppIntent = PendingIntent.getActivity(
                context,
                10,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val turntableBitmap = renderTurntableWidgetBitmap(context, artUri)

            appWidgetIds.forEach { widgetId ->
                val views = RemoteViews(context.packageName, R.layout.widget_pure_music)
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

        private fun renderTurntableWidgetBitmap(context: Context, uri: Uri?): Bitmap {
            val w = 228
            val h = 162
            val sleeveSize = 154
            val sleeveTop = (h - sleeveSize) / 2f
            val discRadius = 70f
            val discCx = sleeveSize + 14f
            val discCy = h / 2f

            val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(out)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            // 1. Draw peeking 96-groove glossy black vinyl disc on the right
            paint.color = Color.parseColor("#0B0B0D")
            canvas.drawCircle(discCx, discCy, discRadius, paint)

            val sweepPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            val sweep = SweepGradient(
                discCx,
                discCy,
                intArrayOf(
                    Color.TRANSPARENT,
                    Color.argb(72, 234, 236, 239),
                    Color.TRANSPARENT,
                    Color.argb(68, 234, 236, 239),
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
                strokeWidth = 1.1f
            }
            for (i in 0..18) {
                val r = discRadius * (0.44f + i * 0.028f)
                groovePaint.color = if (i % 2 == 0) Color.argb(30, 255, 255, 255) else Color.argb(80, 0, 0, 0)
                canvas.drawCircle(discCx, discCy, r, groovePaint)
            }

            // 2. Decode album cover (per-song or fallback dark vinyl sleeve)
            val rawCover = uri?.let { loadRawBitmap(context, it, sleeveSize) }
            val coverRounded = Bitmap.createBitmap(sleeveSize, sleeveSize, Bitmap.Config.ARGB_8888)
            val cCanvas = Canvas(coverRounded)
            val cPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            val rect = Rect(0, 0, sleeveSize, sleeveSize)
            val rectF = RectF(rect)
            cCanvas.drawRoundRect(rectF, 14f, 14f, cPaint)
            cPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            if (rawCover != null) {
                cCanvas.drawBitmap(rawCover, null, rect, cPaint)
                rawCover.recycle()
            } else {
                val fallbackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#24211F") }
                cCanvas.drawRect(rectF, fallbackPaint)
                fallbackPaint.color = Color.parseColor("#A63429")
                cCanvas.drawCircle(sleeveSize / 2f, sleeveSize / 2f, 22f, fallbackPaint)
            }

            // Soft drop shadow behind sleeve edge over the vinyl disc
            val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(65, 0, 0, 0)
            }
            canvas.drawRoundRect(RectF(6f, sleeveTop + 2f, sleeveSize + 4f, sleeveTop + sleeveSize + 2f), 14f, 14f, shadowPaint)
            canvas.drawBitmap(coverRounded, 0f, sleeveTop, null)
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
