package com.puremusic.player.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import com.puremusic.player.R

class PureMusicWidgetLargeProvider : PureMusicWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        updateWidgetsForLayout(
            context = context,
            manager = appWidgetManager,
            appWidgetIds = appWidgetIds,
            layoutRes = R.layout.widget_pure_music_4x2,
            highRes = true,
            title = cachedTitle,
            artist = cachedArtist,
            isPlaying = cachedIsPlaying,
            artUri = cachedArtUri,
            isLossless = cachedIsLossless,
        )
    }
}
