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
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
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
            val coverBitmap = artUri?.let { loadRoundedCover(context, it, 160, 14f) }

            appWidgetIds.forEach { widgetId ->
                val views = RemoteViews(context.packageName, R.layout.widget_pure_music)
                views.setTextViewText(R.id.widget_title, title?.takeIf { it.isNotBlank() } ?: "简约音乐")
                views.setTextViewText(R.id.widget_artist, artist?.takeIf { it.isNotBlank() } ?: "点击打开本地曲库")
                views.setViewVisibility(R.id.widget_badge, if (isLossless) View.VISIBLE else View.GONE)
                if (coverBitmap != null) {
                    views.setImageViewBitmap(R.id.widget_art, coverBitmap)
                } else {
                    views.setImageViewResource(R.id.widget_art, R.mipmap.ic_launcher_round)
                }
                views.setImageViewResource(
                    R.id.widget_play_pause,
                    if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                )
                views.setOnClickPendingIntent(R.id.widget_root, openAppIntent)
                views.setOnClickPendingIntent(R.id.widget_play_pause, actionPendingIntent(context, ACTION_TOGGLE, 11))
                views.setOnClickPendingIntent(R.id.widget_next, actionPendingIntent(context, ACTION_NEXT, 12))
                views.setOnClickPendingIntent(R.id.widget_prev, actionPendingIntent(context, ACTION_PREV, 13))
                manager.updateAppWidget(widgetId, views)
            }
        }

        private fun loadRoundedCover(context: Context, uri: Uri, targetPx: Int, radiusPx: Float): Bitmap? {
            return runCatching {
                val raw = if (uri.toString().contains("/audio/media/")) {
                    AudioArtFetcher.loadPerSongBitmap(context, uri, targetPx)
                } else {
                    val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
                    val sample = maxOf(1, minOf(opts.outWidth, opts.outHeight) / targetPx)
                    val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sample }
                    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, decodeOpts) }
                } ?: return null
                val scaled = Bitmap.createScaledBitmap(raw, targetPx, targetPx, true)
                if (scaled !== raw) raw.recycle()
                val out = Bitmap.createBitmap(targetPx, targetPx, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(out)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                val rect = Rect(0, 0, targetPx, targetPx)
                val rectF = RectF(rect)
                canvas.drawRoundRect(rectF, radiusPx, radiusPx, paint)
                paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
                canvas.drawBitmap(scaled, rect, rect, paint)
                scaled.recycle()
                out
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
