# Keep Media3 and audio service classes intact for release builds
-keep class com.puremusic.player.service.PlaybackService { *; }
-keep class androidx.media3.** { *; }
