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
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
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
        @Volatile internal var cachedPrevArtUri: Uri? = null
        @Volatile internal var cachedNextArtUri: Uri? = null
        @Volatile internal var cachedIsLossless: Boolean = false

        fun pushState(
            context: Context,
            title: String?,
            artist: String?,
            isPlaying: Boolean,
            artUri: Uri? = null,
            isLossless: Boolean = false,
            prevArtUri: Uri? = null,
            nextArtUri: Uri? = null,
        ) {
            if (!title.isNullOrBlank()) cachedTitle = title
            if (!artist.isNullOrBlank()) cachedArtist = artist
            cachedIsPlaying = isPlaying
            if (artUri != null) cachedArtUri = artUri
            if (prevArtUri != null) cachedPrevArtUri = prevArtUri
            if (nextArtUri != null) cachedNextArtUri = nextArtUri
            cachedIsLossless = isLossless

            val manager = AppWidgetManager.getInstance(context)
            val shelfIds = manager.getAppWidgetIds(ComponentName(context, PureMusicWidgetProvider::class.java))
            if (shelfIds.isNotEmpty()) {
                updateWidgetsForLayout(
                    context = context,
                    manager = manager,
                    appWidgetIds = shelfIds,
                    layoutRes = R.layout.widget_pure_music,
                    highRes = false,
                    title = title,
                    artist = artist,
                    isPlaying = isPlaying,
                    artUri = cachedArtUri,
                    isLossless = isLossless,
                )
            }

            val turntableIds = manager.getAppWidgetIds(ComponentName(context, PureMusicWidgetLargeProvider::class.java))
            if (turntableIds.isNotEmpty()) {
                updateWidgetsForLayout(
                    context = context,
                    manager = manager,
                    appWidgetIds = turntableIds,
                    layoutRes = R.layout.widget_pure_music_4x2,
                    highRes = true,
                    title = title,
                    artist = artist,
                    isPlaying = isPlaying,
                    artUri = cachedArtUri,
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
            val artworkBitmap = if (highRes) {
                renderTurntableDeckBitmap(context, artUri)
            } else {
                renderWalnutVinylShelfBitmap(context, artUri)
            }

            appWidgetIds.forEach { widgetId ->
                val views = RemoteViews(context.packageName, layoutRes)
                val safeTitle = title?.takeIf { it.isNotBlank() } ?: "简约音乐"
                val safeArtist = artist?.takeIf { it.isNotBlank() } ?: "PureMusic"
                if (highRes) {
                    views.setTextViewText(R.id.widget_title, safeTitle)
                    views.setTextViewText(R.id.widget_artist, safeArtist)
                    views.setViewVisibility(R.id.widget_badge, if (isLossless) View.VISIBLE else View.GONE)
                } else {
                    views.setTextViewText(R.id.widget_title, "$safeTitle · $safeArtist")
                    views.setViewVisibility(R.id.widget_badge, View.GONE)
                }
                views.setImageViewBitmap(R.id.widget_art, artworkBitmap)
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

        /**
         * Style 1: Floating Walnut Vinyl Shelf Desk Ornament (transparent wallpaper background,
         * dark walnut back-rail, recessed flanking neighbor sleeves, hero cover & peeking 180g black vinyl).
         */
        private fun renderWalnutVinylShelfBitmap(context: Context, uri: Uri?): Bitmap {
            val w = 720
            val h = 290
            val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(out)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            // 1. Dark walnut back-rail behind the sleeves
            val railTop = h * 0.56f
            val railRect = RectF(18f, railTop, w - 18f, h.toFloat())
            paint.shader = LinearGradient(
                0f, railTop, 0f, h.toFloat(),
                intArrayOf(Color.parseColor("#ECE7DE"), Color.parseColor("#DFD9CE"), Color.parseColor("#D1C9BC")),
                null, Shader.TileMode.CLAMP,
            )
            canvas.drawRoundRect(railRect, 14f, 14f, paint)
            paint.shader = null

            // 2. Flanking recessed neighbor sleeves using REAL previous & next album covers (softly dimmed)
            val sideInt = 216
            val sideTop = (h - sideInt - 2).toFloat()
            val leftX = 36f
            val rightX = w - 36f - sideInt
            val leftSleeve = RectF(leftX, sideTop, leftX + sideInt, sideTop + sideInt)
            val rightSleeve = RectF(rightX, sideTop, rightX + sideInt, sideTop + sideInt)

            val prevRaw = (cachedPrevArtUri ?: uri)?.let { loadRawBitmap(context, it, sideInt) }
            val nextRaw = (cachedNextArtUri ?: uri)?.let { loadRawBitmap(context, it, sideInt) }
            val prevBmp = roundedCoverBitmap(prevRaw, sideInt, 12f)
            val nextBmp = roundedCoverBitmap(nextRaw, sideInt, 12f)
            canvas.drawBitmap(prevBmp, leftX, sideTop, null)
            canvas.drawBitmap(nextBmp, rightX, sideTop, null)
            prevBmp.recycle()
            nextBmp.recycle()

            // Soft recessed shadow veil so flanking real covers sit gracefully behind hero sleeve
            paint.color = Color.argb(95, 26, 25, 24)
            canvas.drawRoundRect(leftSleeve, 12f, 12f, paint)
            paint.color = Color.argb(108, 26, 25, 24)
            canvas.drawRoundRect(rightSleeve, 12f, 12f, paint)

            val rimP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 1.6f
                color = Color.argb(65, 255, 248, 236)
            }
            canvas.drawRoundRect(leftSleeve, 12f, 12f, rimP)
            canvas.drawRoundRect(rightSleeve, 12f, 12f, rimP)

            // 3. Peeking glossy 180g matte-black vinyl disc behind the right edge of hero sleeve
            val heroSize = 272
            val heroLeft = (w - heroSize) / 2f - 28f
            val heroTop = (h - heroSize).toFloat()
            val discR = heroSize * 0.45f
            val discCx = heroLeft + heroSize * 0.94f
            val discCy = heroTop + heroSize * 0.50f
            drawGroovedVinylDisc(canvas, discCx, discCy, discR, centerCover = null)

            // 4. Soft cast shadow from hero sleeve onto vinyl & back rail
            paint.color = Color.argb(95, 0, 0, 0)
            canvas.drawRoundRect(RectF(heroLeft + 10f, heroTop + 6f, heroLeft + heroSize + 10f, heroTop + heroSize + 4f), 14f, 14f, paint)

            // 5. Crisp hero album sleeve standing prominently in center of the shelf
            val rawCover = uri?.let { loadRawBitmap(context, it, heroSize) }
            val coverBmp = roundedCoverBitmap(rawCover, heroSize, 14f)
            canvas.drawBitmap(coverBmp, heroLeft, heroTop, null)
            coverBmp.recycle()

            // Crisp thin ivory edge highlight on hero sleeve
            rimP.color = Color.argb(80, 255, 250, 240)
            canvas.drawRoundRect(RectF(heroLeft, heroTop, heroLeft + heroSize, heroTop + heroSize), 14f, 14f, rimP)

            return out
        }

        /**
         * Style 2: Miniature Brushed Champagne-Aluminum Turntable Deck + 180g Grooved Vinyl
         * (with current album cover clipped cleanly onto center label) + Precision Brushed-Brass Tonearm.
         */
        private fun renderTurntableDeckBitmap(context: Context, uri: Uri?): Bitmap {
            val size = 360
            val out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(out)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            // 1. Brushed Champagne-Aluminum recessed square turntable plate
            val plateRect = RectF(6f, 6f, size - 6f, size - 6f)
            paint.shader = LinearGradient(
                0f, 0f, size.toFloat(), size.toFloat(),
                intArrayOf(
                    Color.parseColor("#F5F2EC"),
                    Color.parseColor("#EAE5DC"),
                    Color.parseColor("#DFD9CE"),
                    Color.parseColor("#EFECE5"),
                ),
                null, Shader.TileMode.CLAMP,
            )
            canvas.drawRoundRect(plateRect, 26f, 26f, paint)
            paint.shader = null

            // Subtle machined inner bevel on aluminum plate
            val bevelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 2.4f
                color = Color.argb(120, 255, 252, 245)
            }
            canvas.drawRoundRect(plateRect, 26f, 26f, bevelPaint)

            // 2. Sculpted recessed dark platter well
            val cx = size * 0.47f
            val cy = size * 0.52f
            val wellR = size * 0.405f
            paint.shader = RadialGradient(
                cx, cy, wellR,
                intArrayOf(Color.parseColor("#161618"), Color.parseColor("#29292C"), Color.parseColor("#BAB3A8")),
                floatArrayOf(0.84f, 0.94f, 1.0f),
                Shader.TileMode.CLAMP,
            )
            canvas.drawCircle(cx, cy, wellR, paint)
            paint.shader = null

            // 3. 180g matte-black grooved vinyl disc with current album cover on center label
            val rawCover = uri?.let { loadRawBitmap(context, it, 160) }
            drawGroovedVinylDisc(canvas, cx, cy, wellR * 0.93f, centerCover = rawCover)
            rawCover?.recycle()

            // 4. Precision miniature brushed-brass tonearm resting delicately on the vinyl groove
            val pivotX = size * 0.855f
            val pivotY = size * 0.175f
            val headX = size * 0.695f
            val headY = size * 0.695f

            // Tonearm shadow
            val armShadow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeWidth = 9f
                color = Color.argb(75, 0, 0, 0)
            }
            val armPath = Path().apply {
                moveTo(pivotX, pivotY)
                quadTo(size * 0.84f, size * 0.50f, headX, headY)
            }
            canvas.save()
            canvas.translate(4f, 6f)
            canvas.drawPath(armPath, armShadow)
            canvas.restore()

            // Brushed-brass tonearm tube
            val armPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeWidth = 7.5f
                shader = LinearGradient(
                    pivotX, pivotY, headX, headY,
                    intArrayOf(Color.parseColor("#DFDDD9"), Color.parseColor("#9A9893"), Color.parseColor("#CFCDC8")),
                    null, Shader.TileMode.CLAMP,
                )
            }
            canvas.drawPath(armPath, armPaint)

            // Pivot counterweight & gimbal base
            paint.color = Color.parseColor("#54524E")
            canvas.drawCircle(pivotX, pivotY, 20f, paint)
            paint.shader = RadialGradient(
                pivotX - 4f, pivotY - 4f, 18f,
                intArrayOf(Color.parseColor("#ECEAE6"), Color.parseColor("#9E9C97")),
                null, Shader.TileMode.CLAMP,
            )
            canvas.drawCircle(pivotX, pivotY, 15f, paint)
            paint.shader = null
            paint.color = Color.parseColor("#252422")
            canvas.drawCircle(pivotX, pivotY, 5f, paint)

            // Cartridge headshell angled onto groove
            canvas.save()
            canvas.translate(headX, headY)
            canvas.rotate(42f)
            paint.color = Color.parseColor("#2C2B29")
            canvas.drawRoundRect(RectF(-10f, -15f, 10f, 15f), 4f, 4f, paint)
            paint.color = Color.parseColor("#DAD8D3")
            canvas.drawRoundRect(RectF(-6f, -10f, 6f, 6f), 2f, 2f, paint)
            canvas.restore()

            return out
        }

        private fun drawGroovedVinylDisc(
            canvas: Canvas,
            cx: Float,
            cy: Float,
            radius: Float,
            centerCover: Bitmap?,
        ) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.color = Color.parseColor("#0D0D0F")
            canvas.drawCircle(cx, cy, radius, paint)

            // Anisotropic studio light sheen
            val sweepPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            val sweep = SweepGradient(
                cx, cy,
                intArrayOf(
                    Color.TRANSPARENT,
                    Color.argb(84, 240, 236, 228),
                    Color.TRANSPARENT,
                    Color.argb(74, 240, 236, 228),
                    Color.TRANSPARENT,
                ),
                floatArrayOf(0.0f, 0.14f, 0.30f, 0.64f, 0.85f),
            )
            val mat = Matrix().apply { postRotate(-26f, cx, cy) }
            sweep.setLocalMatrix(mat)
            sweepPaint.shader = sweep
            canvas.drawCircle(cx, cy, radius * 0.97f, sweepPaint)

            // Micro-grooves
            val groovePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 1.2f
            }
            for (i in 0..24) {
                val frac = 0.40f + (i / 24f) * 0.54f
                groovePaint.color = if (i % 2 == 0) Color.argb(30, 255, 250, 240) else Color.argb(85, 0, 0, 0)
                canvas.drawCircle(cx, cy, radius * frac, groovePaint)
            }

            // Outer rim highlight
            val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 1.6f
                color = Color.argb(52, 255, 248, 236)
            }
            canvas.drawCircle(cx, cy, radius - 1f, rimPaint)

            // Center label (album cover if supplied, else warm cinnabar label)
            val labelR = radius * 0.33f
            if (centerCover != null) {
                val labelSize = (labelR * 2f).toInt().coerceAtLeast(16)
                val circleBmp = Bitmap.createBitmap(labelSize, labelSize, Bitmap.Config.ARGB_8888)
                val cCanvas = Canvas(circleBmp)
                val cPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                cCanvas.drawCircle(labelSize / 2f, labelSize / 2f, labelSize / 2f, cPaint)
                cPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
                cCanvas.drawBitmap(centerCover, null, Rect(0, 0, labelSize, labelSize), cPaint)
                canvas.drawBitmap(circleBmp, cx - labelR, cy - labelR, null)
                circleBmp.recycle()
            } else {
                paint.color = Color.parseColor("#353330")
                canvas.drawCircle(cx, cy, labelR, paint)
            }
            // Center spindle hole
            paint.color = Color.parseColor("#181615")
            canvas.drawCircle(cx, cy, radius * 0.055f, paint)
            paint.color = Color.parseColor("#DCDAD5")
            canvas.drawCircle(cx, cy, radius * 0.025f, paint)
        }

        private fun roundedCoverBitmap(rawCover: Bitmap?, size: Int, cornerRadius: Float): Bitmap {
            val out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val cCanvas = Canvas(out)
            val cPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            val rect = Rect(0, 0, size, size)
            val rectF = RectF(rect)
            cCanvas.drawRoundRect(rectF, cornerRadius, cornerRadius, cPaint)
            cPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            if (rawCover != null) {
                cCanvas.drawBitmap(rawCover, null, rect, cPaint)
                rawCover.recycle()
            } else {
                val fb = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#2A2420") }
                cCanvas.drawRect(rectF, fb)
                fb.color = Color.parseColor("#C83E2B")
                cCanvas.drawCircle(size / 2f, size / 2f, size * 0.14f, fb)
            }
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
