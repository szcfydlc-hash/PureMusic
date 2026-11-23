package com.puremusic.player.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.BitmapLoader
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.session.CacheBitmapLoader
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import com.puremusic.player.MainActivity
import com.puremusic.player.ui.components.AudioArtFetcher
import com.puremusic.player.widget.PureMusicWidgetProvider
import java.io.IOException
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var artworkExecutor: ExecutorService? = null

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        // Gapless-ready continuous pre-buffering LoadControl (50s buffer, seamless track boundary)
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                15_000,
                50_000,
                1_000,
                2_000,
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        val renderersFactory = object : DefaultRenderersFactory(this) {
            override fun buildAudioSink(
                context: Context,
                enableFloatOutput: Boolean,
                enableAudioTrackPlaybackParams: Boolean,
            ): AudioSink {
                return DefaultAudioSink.Builder(context)
                    .setAudioProcessors(arrayOf(SpectrumAudioProcessor()))
                    .build()
            }
        }

        val player = ExoPlayer.Builder(this, renderersFactory)
            .setLoadControl(loadControl)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
        player.pauseAtEndOfMediaItems = false

        if (player.audioSessionId != C.AUDIO_SESSION_ID_UNSET) {
            AudioEqualizerManager.attachToSession(player.audioSessionId)
        }

        player.addListener(
            object : Player.Listener {
                override fun onAudioSessionIdChanged(audioSessionId: Int) {
                    if (audioSessionId != C.AUDIO_SESSION_ID_UNSET) {
                        AudioEqualizerManager.attachToSession(audioSessionId)
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (!isPlaying) AudioSpectrumBus.resetToIdle()
                    pushWidgetUpdate(player)
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    pushWidgetUpdate(player)
                }
            },
        )

        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val executor = Executors.newFixedThreadPool(2)
        artworkExecutor = executor

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivityPendingIntent)
            .setBitmapLoader(CacheBitmapLoader(AudioNotificationBitmapLoader(applicationContext, executor)))
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val player = mediaSession?.player
        when (intent?.action) {
            PureMusicWidgetProvider.ACTION_TOGGLE -> {
                if (player != null && player.mediaItemCount > 0) {
                    if (player.isPlaying) player.pause() else player.play()
                    pushWidgetUpdate(player)
                }
            }
            PureMusicWidgetProvider.ACTION_NEXT -> {
                player?.seekToNextMediaItem()
            }
            PureMusicWidgetProvider.ACTION_PREV -> {
                player?.seekToPreviousMediaItem()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun pushWidgetUpdate(player: Player) {
        val metadata = player.currentMediaItem?.mediaMetadata
        PureMusicWidgetProvider.pushState(
            context = this,
            title = metadata?.title?.toString(),
            artist = metadata?.artist?.toString(),
            isPlaying = player.isPlaying,
        )
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        AudioSpectrumBus.resetToIdle()
        AudioEqualizerManager.release()
        artworkExecutor?.shutdownNow()
        artworkExecutor = null
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}

@UnstableApi
private class AudioNotificationBitmapLoader(
    private val context: Context,
    private val executor: ExecutorService,
) : BitmapLoader {

    override fun supportsMimeType(mimeType: String): Boolean {
        return mimeType.startsWith("image/")
    }

    override fun decodeBitmap(data: ByteArray): ListenableFuture<Bitmap> {
        val future = SettableFuture.create<Bitmap>()
        executor.execute {
            try {
                val bmp = BitmapFactory.decodeByteArray(data, 0, data.size)
                if (bmp != null) {
                    future.set(bmp)
                } else {
                    future.setException(IOException("Unable to decode artwork bytes"))
                }
            } catch (t: Throwable) {
                future.setException(t)
            }
        }
        return future
    }

    override fun loadBitmap(uri: Uri): ListenableFuture<Bitmap> {
        val future = SettableFuture.create<Bitmap>()
        executor.execute {
            try {
                val uriStr = uri.toString()
                val bmp: Bitmap? = when {
                    uriStr.contains("/audio/media/") -> {
                        AudioArtFetcher.loadPerSongBitmap(context, uri, 512)
                    }
                    uri.scheme == "http" || uri.scheme == "https" -> {
                        URL(uriStr).openStream().use { BitmapFactory.decodeStream(it) }
                    }
                    else -> {
                        context.contentResolver.openInputStream(uri)?.use {
                            BitmapFactory.decodeStream(it)
                        }
                    }
                }
                if (bmp != null) {
                    future.set(bmp)
                } else {
                    future.setException(IOException("No artwork found for $uri"))
                }
            } catch (t: Throwable) {
                future.setException(t)
            }
        }
        return future
    }
}
