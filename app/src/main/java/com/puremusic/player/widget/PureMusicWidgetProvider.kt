package com.puremusic.player.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.puremusic.player.MainActivity
import com.puremusic.player.R
import com.puremusic.player.service.PlaybackService

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
        ) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, PureMusicWidgetProvider::class.java))
            if (ids.isEmpty()) return
            updateAllWidgets(context, manager, ids, title, artist, isPlaying)
        }

        private fun updateAllWidgets(
            context: Context,
            manager: AppWidgetManager,
            appWidgetIds: IntArray,
            title: String? = null,
            artist: String? = null,
            isPlaying: Boolean = false,
        ) {
            val openAppIntent = PendingIntent.getActivity(
                context,
                10,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

            appWidgetIds.forEach { widgetId ->
                val views = RemoteViews(context.packageName, R.layout.widget_pure_music)
                views.setTextViewText(R.id.widget_title, title?.takeIf { it.isNotBlank() } ?: "简约音乐")
                views.setTextViewText(R.id.widget_artist, artist?.takeIf { it.isNotBlank() } ?: "点击打开本地曲库")
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
