package com.puremusic.player

import androidx.compose.foundation.layout.displayCutoutPadding
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.app.Activity
import android.provider.MediaStore
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.sp
import com.puremusic.player.data.model.BackgroundStyle
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import android.provider.OpenableColumns
import com.puremusic.player.ui.components.SingleTrackRepeatDialog
import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.puremusic.player.data.model.LayoutMode
import com.puremusic.player.data.model.LyricFontStyle
import com.puremusic.player.data.model.MainTab
import com.puremusic.player.data.model.Song
import com.puremusic.player.ui.MusicViewModel
import com.puremusic.player.data.model.Playlist
import com.puremusic.player.ui.components.AddToPlaylistDialog
import com.puremusic.player.ui.components.AudioArtFetcher
import com.puremusic.player.ui.components.BatchSongPickerSheet
import com.puremusic.player.ui.components.ConfirmDeletePlaylistDialog
import com.puremusic.player.ui.components.ConfirmDeleteSongDialog
import com.puremusic.player.ui.components.RenamePlaylistDialog
import com.puremusic.player.ui.components.CreatePlaylistDialog
import com.puremusic.player.ui.components.EqualizerBottomSheet
import com.puremusic.player.ui.components.LyricsBottomSheet
import com.puremusic.player.ui.components.OnlineCoverSearchSheet
import com.puremusic.player.ui.components.OnlineLyricsSearchSheet
import com.puremusic.player.ui.components.MiniPlayerBar
import com.puremusic.player.ui.components.liquidGlass
import com.puremusic.player.ui.components.QueueBottomSheet
import com.puremusic.player.ui.components.SleepTimerDialog
import com.puremusic.player.ui.components.SongInfoDialog
import com.puremusic.player.ui.screens.LibraryScreen
import com.puremusic.player.ui.screens.NowPlayingScreen
import com.puremusic.player.ui.screens.SettingsScreen
import com.puremusic.player.ui.theme.PureMusicTheme
import com.puremusic.player.ui.theme.resolveLyricFontFamily

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Global Coil 25% memory cache + 120MB disk thumbnail cache for 120Hz scrolling
        Coil.setImageLoader {
            ImageLoader.Builder(applicationContext)
                .components {
                    add(AudioArtFetcher.Factory(applicationContext))
                }
                .memoryCache {
                    MemoryCache.Builder(applicationContext)
                        .maxSizePercent(0.25)
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(applicationContext.cacheDir.resolve("coil_art_cache"))
                        .maxSizeBytes(120L * 1024L * 1024L)
                        .build()
                }
                .crossfade(true)
                .respectCacheHeaders(false)
                .build()
        }
        enableEdgeToEdge()
        setContent {
            PureMusicApp()
        }
    }
}

@Composable
private fun PureMusicApp(
    viewModel: MusicViewModel = viewModel(),
) {
    val context = LocalContext.current
    val playerTheme by viewModel.playerTheme.collectAsStateWithLifecycle()
    val fontStyle by viewModel.fontStyle.collectAsStateWithLifecycle()
    val lyricFontStyle by viewModel.lyricFontStyle.collectAsStateWithLifecycle()
    val backgroundStyle by viewModel.backgroundStyle.collectAsStateWithLifecycle()
    val layoutMode by viewModel.layoutMode.collectAsStateWithLifecycle()
    val layoutDensity by viewModel.layoutDensity.collectAsStateWithLifecycle()
    val fontScale by viewModel.fontScale.collectAsStateWithLifecycle()
    val accentColor by viewModel.accentColor.collectAsStateWithLifecycle()
    val liquidGlassEnabled by viewModel.liquidGlassEnabled.collectAsStateWithLifecycle()
    val floatingControlsEnabled by viewModel.floatingControlsEnabled.collectAsStateWithLifecycle()
    val liquidGlassLevel by viewModel.liquidGlassLevel.collectAsStateWithLifecycle()
    val miniPlayerStyle by viewModel.miniPlayerStyle.collectAsStateWithLifecycle()
    val miniPlayerHeightDp by viewModel.miniPlayerHeightDp.collectAsStateWithLifecycle()
    val fontWeight by viewModel.fontWeight.collectAsStateWithLifecycle()
    val fontWeightValue by viewModel.fontWeightValue.collectAsStateWithLifecycle()
    val fontSlantDegrees by viewModel.fontSlantDegrees.collectAsStateWithLifecycle()
    val playbackSpeed by viewModel.playbackSpeed.collectAsStateWithLifecycle()
    val fadeInSeconds by viewModel.fadeInSeconds.collectAsStateWithLifecycle()
    val hapticEnabled by viewModel.hapticEnabled.collectAsStateWithLifecycle()
    val carModeEnabled by viewModel.carModeEnabled.collectAsStateWithLifecycle()

    val immersionStyle by viewModel.immersionStyle.collectAsStateWithLifecycle()


    val spectrumStyle by viewModel.spectrumStyle.collectAsStateWithLifecycle()
    val customFontName by viewModel.customFontName.collectAsStateWithLifecycle()
    val customFontVersion by viewModel.customFontVersion.collectAsStateWithLifecycle()
    val keepScreenOn by viewModel.keepScreenOn.collectAsStateWithLifecycle()
    val singleRepeatRemaining by viewModel.singleRepeatRemaining.collectAsStateWithLifecycle()

    val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val requiredPermissions = remember {
        buildList {
            add(audioPermission)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()
    }

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, audioPermission) == PackageManager.PERMISSION_GRANTED,
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        hasAudioPermission = result[audioPermission] == true ||
            ContextCompat.checkSelfPermission(context, audioPermission) == PackageManager.PERMISSION_GRANTED
    }

    LaunchedEffect(hasAudioPermission) {
        if (hasAudioPermission) {
            viewModel.loadLibrary()
        }
    }

    PureMusicTheme(
        backgroundStyle = backgroundStyle,
        accentColor = accentColor,
        fontStyle = fontStyle,
        layoutDensity = layoutDensity,
        fontScale = fontScale,
        fontWeight = fontWeight,
        fontWeightValue = fontWeightValue,
        fontSlantDegrees = fontSlantDegrees,
        liquidGlassLevel = liquidGlassLevel,
        floatingControlsEnabled = floatingControlsEnabled,
        miniPlayerStyle = miniPlayerStyle,
        miniPlayerHeightDp = miniPlayerHeightDp,
        hapticEnabled = hapticEnabled,
        customFontVersion = customFontVersion,
    ) {
        if (!hasAudioPermission) {
            PermissionPromptScreen(
                onRequestPermission = { permissionLauncher.launch(requiredPermissions) },
            )
            return@PureMusicTheme
        }

        val songs by viewModel.filteredSongs.collectAsStateWithLifecycle()
        val allSongs by viewModel.allSongs.collectAsStateWithLifecycle()
        val albums by viewModel.albums.collectAsStateWithLifecycle()
        val artists by viewModel.artists.collectAsStateWithLifecycle()
        val folders by viewModel.folders.collectAsStateWithLifecycle()
        val allDiscoveredFolders by viewModel.allDiscoveredFolders.collectAsStateWithLifecycle()
        val excludedFolders by viewModel.excludedFolders.collectAsStateWithLifecycle()
        val minDurationFilter by viewModel.minDurationFilter.collectAsStateWithLifecycle()
        val scanStatus by viewModel.scanStatus.collectAsStateWithLifecycle()
        val playlists by viewModel.playlists.collectAsStateWithLifecycle()
        val playlistCovers by viewModel.playlistCovers.collectAsStateWithLifecycle()
        val recentSongs by viewModel.recentSongs.collectAsStateWithLifecycle()
        val mostPlayedSongs by viewModel.mostPlayedSongs.collectAsStateWithLifecycle()
    val playCounts by viewModel.playCounts.collectAsStateWithLifecycle()
    val hourlyPlays by viewModel.hourlyPlays.collectAsStateWithLifecycle()
        val currentAudioSpec by viewModel.currentAudioSpec.collectAsStateWithLifecycle()
        val lyricOffsets by viewModel.lyricOffsets.collectAsStateWithLifecycle()
        val favoriteSongIds by viewModel.favoriteSongIds.collectAsStateWithLifecycle()
        val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
        val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
        val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
        val playerState by viewModel.playerState.collectAsStateWithLifecycle()
        val currentLyrics by viewModel.currentLyrics.collectAsStateWithLifecycle()
        val equalizerState by viewModel.equalizerState.collectAsStateWithLifecycle()
        val sleepTimerRemainingMs by viewModel.sleepTimerRemainingMs.collectAsStateWithLifecycle()
        val lyricSearchResults by viewModel.lyricSearchResults.collectAsStateWithLifecycle()
        val isSearchingLyrics by viewModel.isSearchingLyrics.collectAsStateWithLifecycle()
        val lyricSearchError by viewModel.lyricSearchError.collectAsStateWithLifecycle()
        val isTranslatingLyrics by viewModel.isTranslatingLyrics.collectAsStateWithLifecycle()
        val metaOverrides by viewModel.metaOverrides.collectAsStateWithLifecycle()
        val coverSearchResults by viewModel.coverSearchResults.collectAsStateWithLifecycle()
        val isSearchingCovers by viewModel.isSearchingCovers.collectAsStateWithLifecycle()
        val coverSearchError by viewModel.coverSearchError.collectAsStateWithLifecycle()

        var activeMainTab by remember { mutableStateOf(MainTab.LIBRARY) }
        var showSplash by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(1150L)
        showSplash = false
    }
    var showNowPlaying by remember { mutableStateOf(false) }
        var showEqualizer by remember { mutableStateOf(false) }
    var showRepeatCountDialog by remember { mutableStateOf(false) }
        var showSleepTimer by remember { mutableStateOf(false) }
        var showQueue by remember { mutableStateOf(false) }
        var showLyrics by remember { mutableStateOf(false) }
        var showCreatePlaylist by remember { mutableStateOf(false) }
        var pendingPlaylistSong by remember { mutableStateOf<Song?>(null) }
        var infoDialogSong by remember { mutableStateOf<Song?>(null) }
        var lyricsSearchSong by remember { mutableStateOf<Song?>(null) }
        var coverSearchSong by remember { mutableStateOf<Song?>(null) }
        var pendingGalleryCoverSong by remember { mutableStateOf<Song?>(null) }
        var pendingGalleryCoverPlaylist by remember { mutableStateOf<Playlist?>(null) }

        val playlistCoverLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri ->
            val target = pendingGalleryCoverPlaylist
            pendingGalleryCoverPlaylist = null
            if (uri != null && target != null) {
                viewModel.applyPlaylistCover(target.id, uri)
            }
        }
        var pendingLocalLrcSong by remember { mutableStateOf<Song?>(null) }
        var managingPlaylist by remember { mutableStateOf<Playlist?>(null) }
        var managingFavorites by remember { mutableStateOf(false) }
        var renamingPlaylist by remember { mutableStateOf<Playlist?>(null) }
        var deletingPlaylist by remember { mutableStateOf<Playlist?>(null) }
        var pendingSystemDeleteSong by remember { mutableStateOf<Song?>(null) }
        var legacyDeleteSong by remember { mutableStateOf<Song?>(null) }

        val systemDeleteLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult(),
        ) { result ->
            val target = pendingSystemDeleteSong
            pendingSystemDeleteSong = null
            if (result.resultCode == Activity.RESULT_OK && target != null) {
                viewModel.onSongDeletedFromDevice(target)
            }
        }

        val requestDeleteLocalSong: (Song) -> Unit = { song ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                runCatching {
                    val pi = MediaStore.createDeleteRequest(context.contentResolver, listOf(song.contentUri))
                    pendingSystemDeleteSong = song
                    systemDeleteLauncher.launch(IntentSenderRequest.Builder(pi.intentSender).build())
                }.onFailure {
                    legacyDeleteSong = song
                }
            } else {
                legacyDeleteSong = song
            }
        }

        val galleryCoverLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri ->
            val target = pendingGalleryCoverSong
            pendingGalleryCoverSong = null
            if (uri != null && target != null) {
                viewModel.applyGalleryCover(target, uri)
            }
        }

        val localLrcLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
        ) { uri ->
            val target = pendingLocalLrcSong
            pendingLocalLrcSong = null
            if (uri != null && target != null) {
                viewModel.importLocalLyrics(target, uri)
            }
        }

        var pendingBatchPlaylistSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
        var pendingBatchDeleteSongs by remember { mutableStateOf<List<Song>>(emptyList()) }

        val batchDeleteLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult(),
        ) { result ->
            val targets = pendingBatchDeleteSongs
            pendingBatchDeleteSongs = emptyList()
            if (result.resultCode == Activity.RESULT_OK && targets.isNotEmpty()) {
                targets.forEach { viewModel.onSongDeletedFromDevice(it) }
                Toast.makeText(context, "已删除 ${targets.size} 首本地歌曲", Toast.LENGTH_SHORT).show()
            }
        }

        val requestBatchDeleteSongs: (List<Song>) -> Unit = { targets ->
            if (targets.isNotEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    runCatching {
                        val pi = MediaStore.createDeleteRequest(
                            context.contentResolver,
                            targets.map { it.contentUri },
                        )
                        pendingBatchDeleteSongs = targets
                        batchDeleteLauncher.launch(IntentSenderRequest.Builder(pi.intentSender).build())
                    }.onFailure {
                        Toast.makeText(context, "批量删除请求失败", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    targets.forEach { viewModel.deleteSongLegacy(it) }
                    Toast.makeText(context, "已删除 ${targets.size} 首歌曲", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val exportBackupLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("application/json"),
        ) { uri ->
            if (uri != null) {
                runCatching {
                    val json = viewModel.exportBackupJson()
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(json.toByteArray(Charsets.UTF_8))
                    }
                    Toast.makeText(context, "歌单与收藏备份已导出", Toast.LENGTH_SHORT).show()
                }.onFailure {
                    Toast.makeText(context, "导出失败，请重试", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val importBackupLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
        ) { uri ->
            if (uri != null) {
                runCatching {
                    val text = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                    if (!text.isNullOrBlank()) {
                        viewModel.importBackupJson(text) { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    }
                }.onFailure {
                    Toast.makeText(context, "读取备份文件失败", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val importFontLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
        ) { uri ->
            if (uri != null) {
                var displayName = "自定义字体.ttf"
                runCatching {
                    context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                        if (c.moveToFirst()) {
                            c.getString(0)?.takeIf { it.isNotBlank() }?.let { displayName = it }
                        }
                    }
                }
                viewModel.importCustomFont(uri, displayName) { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isLandscape = maxWidth > maxHeight
            val isTabletLayout = when (layoutMode) {
                LayoutMode.TABLET -> true
                LayoutMode.PHONE -> false
                LayoutMode.AUTO -> minOf(maxWidth, maxHeight) >= 560.dp || (isLandscape && maxWidth >= 560.dp)
            }
            val miniPlayerVisible = playerState.currentSong != null && !isTabletLayout
            val bottomContentPadding = PaddingValues(
                bottom = when {
                    isTabletLayout -> 24.dp
                    miniPlayerVisible -> (96 + miniPlayerHeightDp).dp
                    else -> 86.dp
                },
            )

            val backdropScale by animateFloatAsState(
                targetValue = if (showNowPlaying && playerState.currentSong != null) 0.965f else 1f,
                animationSpec = spring(dampingRatio = 0.88f, stiffness = Spring.StiffnessMediumLow),
                label = "backdropDepthScale",
            )
            AnimatedContent(
                targetState = activeMainTab == MainTab.SETTINGS,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
                label = "mainRootSwitch",
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (isLandscape) {
                            Modifier.displayCutoutPadding().navigationBarsPadding()
                        } else {
                            Modifier
                        },
                    )
                    .graphicsLayer {
                        scaleX = backdropScale
                        scaleY = backdropScale
                    },
            ) { isSettingsTab ->
                if (isSettingsTab) {
                    SettingsScreen(
                        songCount = allSongs.size,
                        albumCount = albums.size,
                        allFolders = allDiscoveredFolders,
                        excludedFolders = excludedFolders,
                        minDurationFilter = minDurationFilter,
                        scanStatus = scanStatus,
                        layoutMode = layoutMode,
                        layoutDensity = layoutDensity,
                        fontScale = fontScale,
                        fontWeight = fontWeight,
                        fontWeightValue = fontWeightValue,
                        fontSlantDegrees = fontSlantDegrees,
                        playbackSpeed = playbackSpeed,
                        fadeInSeconds = fadeInSeconds,
                        hapticEnabled = hapticEnabled,
                        carModeEnabled = carModeEnabled,
                        keepScreenOn = keepScreenOn,
                        liquidGlassEnabled = liquidGlassEnabled,
                        floatingControlsEnabled = floatingControlsEnabled,
                        liquidGlassLevel = liquidGlassLevel,
                        miniPlayerStyle = miniPlayerStyle,
                        miniPlayerHeightDp = miniPlayerHeightDp,
                        accentColor = accentColor,
                        fontStyle = fontStyle,
                        lyricFontStyle = lyricFontStyle,
                        backgroundStyle = backgroundStyle,
                        isTabletLayout = isTabletLayout,
                        bottomContentPadding = bottomContentPadding,
                        onBackToLibrary = { activeMainTab = MainTab.LIBRARY },
                        onSelectLayoutMode = viewModel::setLayoutMode,
                        onSelectLayoutDensity = viewModel::setLayoutDensity,
                        onChangeFontScale = viewModel::setFontScale,
                        onSelectFontWeight = viewModel::setFontWeight,
                        onChangeFontWeightValue = viewModel::setFontWeightValue,
                        onChangeFontSlantDegrees = viewModel::setFontSlantDegrees,
                        onSelectPlaybackSpeed = viewModel::setPlaybackSpeed,
                        onSelectFadeInSeconds = viewModel::setFadeInSeconds,
                        onToggleHaptic = viewModel::setHapticEnabled,
                        onToggleCarMode = {
                            viewModel.setCarModeEnabled(it)
                            if (it && playerState.currentSong != null) showNowPlaying = true
                        },
                        onToggleKeepScreenOn = viewModel::setKeepScreenOn,
                        onToggleLiquidGlass = viewModel::setLiquidGlassEnabled,
                        onToggleFloatingControls = viewModel::setFloatingControlsEnabled,
                        onSelectLiquidGlassLevel = viewModel::setLiquidGlassLevel,
                        onSelectMiniPlayerStyle = viewModel::setMiniPlayerStyle,
                        onChangeMiniPlayerHeightDp = viewModel::setMiniPlayerHeightDp,
                        onSelectAccentColor = viewModel::setAccentColor,
                        onSelectFontStyle = viewModel::setFontStyle,
                        onSelectLyricFontStyle = viewModel::setLyricFontStyle,
                        onSelectBackgroundStyle = viewModel::setBackgroundStyle,
                        onResetAppearance = viewModel::resetAppearance,
                        onRescanLibrary = viewModel::rescanLibraryWithFeedback,
                        onToggleFolderExcluded = viewModel::toggleFolderExcluded,
                        onSelectMinDurationFilter = viewModel::setMinDurationFilter,
                        onOpenEqualizer = { showEqualizer = true },
                        onOpenSleepTimer = { showSleepTimer = true },
                        onExportBackup = { exportBackupLauncher.launch("PureMusic_Backup.json") },
                        onImportBackup = { importBackupLauncher.launch(arrayOf("application/json", "text/*")) },
                        customFontName = customFontName,
                        onImportCustomFont = { importFontLauncher.launch(arrayOf("font/*", "application/octet-stream", "*/*")) },
                        onClearCustomFont = {
                            viewModel.clearCustomFont()
                            Toast.makeText(context, "已清除自定义字体", Toast.LENGTH_SHORT).show()
                        },
                    )
                } else {
                    LibraryScreen(
                        activeMainTab = activeMainTab,
                        onSelectMainTab = { activeMainTab = it },
                        songs = songs,
                        allSongs = allSongs,
                        albums = albums,
                        artists = artists,
                        folders = folders,
                        playlists = playlists,
                        playlistCovers = playlistCovers,
                        recentSongs = recentSongs,
                        mostPlayedSongs = mostPlayedSongs,
                        playCounts = playCounts,
                        hourlyPlays = hourlyPlays,
                        favoriteSongIds = favoriteSongIds,
                        isLoading = isLoading,
                        searchQuery = searchQuery,
                        sortOrder = sortOrder,
                        playerState = playerState,
                        playerTheme = playerTheme,
                        sleepTimerRemainingMs = sleepTimerRemainingMs,
                        liquidGlassEnabled = liquidGlassEnabled,
                        isTabletLayout = isTabletLayout,
                        onSearchQueryChange = viewModel::updateSearchQuery,
                        onSelectSortOrder = viewModel::setSortOrder,
                        onRefreshLibrary = viewModel::rescanLibraryWithFeedback,
                        onPlaySongs = viewModel::playSongs,
                        onTogglePlayPause = viewModel::togglePlayPause,
                        onSkipPrevious = viewModel::skipPrevious,
                        onSkipNext = viewModel::skipNext,
                        onOpenNowPlaying = { showNowPlaying = true },
                        onToggleFavorite = viewModel::toggleFavorite,
                        onRequestAddToPlaylist = { pendingPlaylistSong = it },
                        onShowSongInfo = { infoDialogSong = it },
                        onReplaceCover = { coverSearchSong = it },
                        onMatchLyrics = { lyricsSearchSong = it },
                        onDeleteSong = requestDeleteLocalSong,
                        onBatchAddToPlaylist = { chosen -> pendingBatchPlaylistSongs = chosen },
                        onBatchFavorite = { chosen ->
                            viewModel.addSongsToFavorites(chosen.map { it.id })
                            Toast.makeText(context, "已将 ${chosen.size} 首加入我喜欢", Toast.LENGTH_SHORT).show()
                        },
                        onBatchDeleteSongs = requestBatchDeleteSongs,
                        onRequestCreatePlaylist = { showCreatePlaylist = true },
                        onRequestDeletePlaylist = { deletingPlaylist = it },
                        onRequestRenamePlaylist = { renamingPlaylist = it },
                        onRequestManagePlaylistSongs = { managingPlaylist = it },
                        onRequestManageFavorites = { managingFavorites = true },
                        onRequestReplacePlaylistCover = { pl ->
                            pendingGalleryCoverPlaylist = pl
                            playlistCoverLauncher.launch("image/*")
                        },
                        onClearPlaylistCover = { pl -> viewModel.clearPlaylistCover(pl.id) },
                        onRemoveSongFromPlaylist = viewModel::removeSongFromPlaylist,
                        onOpenEqualizer = { showEqualizer = true },
                        onOpenSleepTimer = { showSleepTimer = true },
                        onOpenThemeSettings = { activeMainTab = MainTab.SETTINGS },
                        resolveSongsByIds = viewModel::resolveSongsByIds,
                        bottomContentPadding = bottomContentPadding,
                    )
                }
            }

            // Bottom Floating Mini-Player + Sleek 4-Tab Navigation Bar (Phone mode)
            if (!isTabletLayout) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .then(
                            if (isLandscape) {
                                Modifier.displayCutoutPadding().navigationBarsPadding()
                            } else {
                                Modifier
                            },
                        ),
                ) {
                    if (miniPlayerVisible) {
                        MiniPlayerBar(
                            playerState = playerState,
                            liquidGlassEnabled = liquidGlassEnabled,
                            onOpenNowPlaying = { showNowPlaying = true },
                            onTogglePlayPause = viewModel::togglePlayPause,
                            onSkipNext = viewModel::skipNext,
                            onSkipPrevious = viewModel::skipPrevious,
                        )
                    }
                    PureMusicBottomNavBar(
                        activeTab = activeMainTab,
                        floating = floatingControlsEnabled,
                        liquidGlassEnabled = liquidGlassEnabled,
                        onSelectTab = { activeMainTab = it },
                    )
                }
            }

            AnimatedVisibility(
                visible = showNowPlaying && playerState.currentSong != null,
                enter = slideInVertically(
                    animationSpec = spring(dampingRatio = 0.88f, stiffness = Spring.StiffnessMediumLow),
                    initialOffsetY = { it },
                ) + fadeIn(tween(200)),
                exit = slideOutVertically(
                    animationSpec = tween(240),
                    targetOffsetY = { it },
                ) + fadeOut(tween(180)),
            ) {
                val currentSong = playerState.currentSong
                NowPlayingScreen(
                    playerState = playerState,
                    allSongs = allSongs,
                    lyrics = currentLyrics,
                    lyricFontStyle = lyricFontStyle,
                    liquidGlassEnabled = liquidGlassEnabled,
                    isTabletLayout = isTabletLayout,
                    isFavorite = currentSong != null && currentSong.id in favoriteSongIds,
                    sleepTimerRemainingMs = sleepTimerRemainingMs,
                    onClose = { showNowPlaying = false },
                    onSelectSongFromWall = { selectedSong ->
                        val idx = allSongs.indexOfFirst { it.id == selectedSong.id }
                        if (idx >= 0) viewModel.playSongs(allSongs, idx, false)
                    },
                    onTogglePlayPause = viewModel::togglePlayPause,
                    onSkipPrevious = viewModel::skipPrevious,
                    onSkipNext = viewModel::skipNext,
                    onSeekTo = viewModel::seekTo,
                    singleRepeatRemaining = singleRepeatRemaining,
                    onOpenRepeatCountDialog = { showRepeatCountDialog = true },
                    onToggleShuffle = viewModel::toggleShuffle,
                    onCycleRepeat = viewModel::cycleRepeatMode,
                    onToggleFavorite = { viewModel.toggleFavorite(it.id) },
                    onOpenQueue = { showQueue = true },
                    onOpenLyrics = { showLyrics = true },
                    onOpenEqualizer = { showEqualizer = true },
                    onOpenSleepTimer = { showSleepTimer = true },
                    onOpenSettings = {
                        showNowPlaying = false
                        activeMainTab = MainTab.SETTINGS
                    },
                    onAddToPlaylist = { pendingPlaylistSong = it },
                    onOpenCoverSearch = { coverSearchSong = it },
                    onOpenLyricsSearch = { lyricsSearchSong = it },
                    isTranslatingLyrics = isTranslatingLyrics,
                    onTranslateLyrics = viewModel::translateCurrentLyricsToChinese,
                    audioSpec = currentAudioSpec,
                    lyricOffsetMs = currentSong?.let { lyricOffsets[it.id] } ?: 0L,
                    keepScreenOn = keepScreenOn,
                    carModeEnabled = carModeEnabled,
                    onToggleCarMode = viewModel::setCarModeEnabled,
                    immersionStyle = immersionStyle,
                    onSelectImmersionStyle = viewModel::setImmersionStyle,
                    spectrumStyle = spectrumStyle,
                    onSelectSpectrumStyle = viewModel::setSpectrumStyle,
                    onAdjustLyricOffset = viewModel::adjustCurrentLyricOffset,
                    onResetLyricOffset = viewModel::resetCurrentLyricOffset,
                )
            }

            AnimatedVisibility(
                visible = showSplash,
                enter = fadeIn(tween(120)),
                exit = fadeOut(tween(420)),
            ) {
                PureMusicSplashOverlay(isDark = backgroundStyle == BackgroundStyle.DEEP_INK)
            }
        }

        if (showCreatePlaylist) {
            CreatePlaylistDialog(
                onDismiss = { showCreatePlaylist = false },
                onCreate = { viewModel.createPlaylist(it) },
            )
        }

        renamingPlaylist?.let { pl ->
            RenamePlaylistDialog(
                currentName = pl.name,
                onDismiss = { renamingPlaylist = null },
                onConfirm = { newName -> viewModel.renamePlaylist(pl.id, newName) },
            )
        }

        deletingPlaylist?.let { pl ->
            ConfirmDeletePlaylistDialog(
                playlistName = pl.name,
                onDismiss = { deletingPlaylist = null },
                onConfirmDelete = { viewModel.deletePlaylist(pl.id) },
            )
        }

        managingPlaylist?.let { pl ->
            val livePl = playlists.firstOrNull { it.id == pl.id } ?: pl
            BatchSongPickerSheet(
                title = "管理「${livePl.name}」",
                allSongs = allSongs,
                initialSelectedIds = livePl.songIds.toSet(),
                onSave = { updatedIds -> viewModel.setPlaylistSongs(livePl.id, updatedIds) },
                onDismiss = { managingPlaylist = null },
            )
        }

        if (managingFavorites) {
            BatchSongPickerSheet(
                title = "管理「我喜欢的音乐」",
                allSongs = allSongs,
                initialSelectedIds = favoriteSongIds,
                onSave = { updatedIds -> viewModel.setFavoriteSongIds(updatedIds.toSet()) },
                onDismiss = { managingFavorites = false },
            )
        }

        pendingPlaylistSong?.let { song ->
            AddToPlaylistDialog(
                song = song,
                playlists = playlists,
                onDismiss = { pendingPlaylistSong = null },
                onSelectPlaylist = { playlistId -> viewModel.addSongToPlaylist(playlistId, song.id) },
                onCreateAndAdd = { name -> viewModel.createPlaylist(name, listOf(song.id)) },
            )
        }

        if (pendingBatchPlaylistSongs.isNotEmpty()) {
            val batchSongs = pendingBatchPlaylistSongs
            val representative = batchSongs.first().copy(title = "已选 ${batchSongs.size} 首歌曲")
            AddToPlaylistDialog(
                song = representative,
                playlists = playlists,
                onDismiss = { pendingBatchPlaylistSongs = emptyList() },
                onSelectPlaylist = { playlistId ->
                    viewModel.addSongsToPlaylist(playlistId, batchSongs.map { it.id })
                    Toast.makeText(context, "已添加 ${batchSongs.size} 首歌曲到歌单", Toast.LENGTH_SHORT).show()
                },
                onCreateAndAdd = { name ->
                    viewModel.createPlaylist(name, batchSongs.map { it.id })
                    Toast.makeText(context, "已创建歌单并加入 ${batchSongs.size} 首歌曲", Toast.LENGTH_SHORT).show()
                },
            )
        }

        if (showSleepTimer) {
            SleepTimerDialog(
                remainingMs = sleepTimerRemainingMs,
                onDismiss = { showSleepTimer = false },
                onSelectMinutes = viewModel::startSleepTimer,
                onCancelTimer = viewModel::cancelSleepTimer,
            )
        }

        if (showRepeatCountDialog) {
            SingleTrackRepeatDialog(
                currentCount = singleRepeatRemaining,
                onDismiss = { showRepeatCountDialog = false },
                onSelectCount = { count ->
                    viewModel.setSingleTrackRepeatCount(count)
                    val tip = if (count <= 1) "已关闭单曲复听计数" else "已设定本曲复听 $count 遍"
                    Toast.makeText(context, tip, Toast.LENGTH_SHORT).show()
                },
            )
        }

        if (showEqualizer) {
            EqualizerBottomSheet(
                state = equalizerState,
                onDismiss = { showEqualizer = false },
                onToggleEnabled = viewModel::setEqualizerEnabled,
                onSelectPreset = viewModel::selectEqualizerPreset,
                onBandLevelChange = viewModel::setEqualizerBand,
            )
        }

        if (showQueue) {
            QueueBottomSheet(
                queue = playerState.queue,
                currentIndex = playerState.currentQueueIndex,
                onDismiss = { showQueue = false },
                onSelectIndex = viewModel::playQueueIndex,
                onRemoveIndex = viewModel::removeFromQueue,
            )
        }

        infoDialogSong?.let { song ->
            SongInfoDialog(
                song = song,
                hasCustomMetadata = song.id in metaOverrides,
                onSaveMetadata = { t, ar, al -> viewModel.saveSongMetadata(song.id, t, ar, al) },
                onRestoreMetadata = { viewModel.clearSongMetadata(song.id) },
                onReplaceCover = { coverSearchSong = song },
                onDismiss = { infoDialogSong = null },
            )
        }

        legacyDeleteSong?.let { song ->
            ConfirmDeleteSongDialog(
                song = song,
                onDismiss = { legacyDeleteSong = null },
                onConfirmDelete = { viewModel.deleteSongLegacy(song) },
            )
        }

        lyricsSearchSong?.let { song ->
            OnlineLyricsSearchSheet(
                song = song,
                results = lyricSearchResults,
                isSearching = isSearchingLyrics,
                errorMessage = lyricSearchError,
                onSearch = viewModel::searchOnlineLyrics,
                onSelectCandidate = { candidate -> viewModel.applyOnlineLyrics(song, candidate) },
                onImportLocalLrc = {
                    pendingLocalLrcSong = song
                    localLrcLauncher.launch(arrayOf("text/*", "application/octet-stream", "*/*"))
                },
                onClearCustomLyrics = { viewModel.clearCustomLyrics(song) },
                onDismiss = { lyricsSearchSong = null },
            )
        }

        coverSearchSong?.let { song ->
            OnlineCoverSearchSheet(
                song = song,
                results = coverSearchResults,
                isSearching = isSearchingCovers,
                errorMessage = coverSearchError,
                onSearch = viewModel::searchOnlineCovers,
                onSelectCandidate = { candidate ->
                    viewModel.applyOnlineCover(song, candidate) {
                        coverSearchSong = null
                    }
                },
                onPickFromGallery = {
                    pendingGalleryCoverSong = song
                    galleryCoverLauncher.launch("image/*")
                },
                onRestoreOriginalCover = { viewModel.clearCustomCover(song) },
                onDismiss = { coverSearchSong = null },
            )
        }

        if (showLyrics) {
            val lyricFamily = remember(lyricFontStyle, customFontVersion) {
                resolveLyricFontFamily(context, lyricFontStyle)
            }
            LyricsBottomSheet(
                songTitle = playerState.currentSong?.title.orEmpty(),
                lyrics = currentLyrics,
                positionMs = playerState.positionMs,
                lyricFontFamily = lyricFamily,
                lyricItalic = lyricFontStyle == LyricFontStyle.SERIF_ITALIC,
                onDismiss = { showLyrics = false },
                onSeekTo = viewModel::seekTo,
            )
        }
    }
}

@Composable
private fun PureMusicBottomNavBar(
    activeTab: MainTab,
    floating: Boolean,
    liquidGlassEnabled: Boolean,
    onSelectTab: (MainTab) -> Unit,
) {
    val items: List<Pair<MainTab, ImageVector>> = listOf(
        MainTab.LIBRARY to Icons.Rounded.LibraryMusic,
        MainTab.ALBUMS to Icons.Rounded.Album,
        MainTab.PLAYLISTS to Icons.Rounded.QueueMusic,
        MainTab.SETTINGS to Icons.Rounded.Settings,
    )
    // Track press state across all 4 tabs so pressing any tab illuminates the capsule edge in FULL tier!
    val tabInteractions = remember { List(4) { MutableInteractionSource() } }
    val pressedStates = tabInteractions.map { it.collectIsPressedAsState().value }
    val anyTabPressed = pressedStates.any { it }

    if (floating) {
        // Detached Centered Floating Pill Dock (strictly controlled by floatingControlsEnabled!)
        val capsuleShape = RoundedCornerShape(32.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 10.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .liquidGlass(
                        enabled = liquidGlassEnabled,
                        shape = capsuleShape,
                        isPressed = anyTabPressed,
                    )
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                items.forEachIndexed { idx, (tab, icon) ->
                    val selected = tab == activeTab
                    val pressed = pressedStates[idx]
                    val tint = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                    }
                    val pillShape = RoundedCornerShape(22.dp)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(pillShape)
                            .then(
                                if (selected || pressed) {
                                    Modifier.background(
                                        MaterialTheme.colorScheme.primary.copy(
                                            alpha = if (pressed) 0.16f else 0.10f,
                                        ),
                                    )
                                } else {
                                    Modifier
                                },
                            )
                            .clickable(
                                interactionSource = tabInteractions[idx],
                                indication = null,
                                onClick = { onSelectTab(tab) },
                            )
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = tab.label,
                            tint = tint,
                            modifier = Modifier.size(21.dp),
                        )
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = tint,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    } else {
        // Docked Edge-to-Edge Footer (supports both flat archival paper & docked translucent glass!)
        val dockedShape = RoundedCornerShape(0.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (liquidGlassEnabled) {
                        Modifier.liquidGlass(
                            enabled = true,
                            shape = dockedShape,
                            isPressed = anyTabPressed,
                        )
                    } else {
                        Modifier.background(MaterialTheme.colorScheme.surface)
                    },
                )
                .navigationBarsPadding(),
        ) {
            if (!liquidGlassEnabled) {
                HorizontalDivider(
                    thickness = 0.75.dp,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                items.forEachIndexed { idx, (tab, icon) ->
                    val selected = tab == activeTab
                    val tint = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(
                                interactionSource = tabInteractions[idx],
                                indication = null,
                                onClick = { onSelectTab(tab) },
                            )
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = tab.label,
                            tint = tint,
                            modifier = Modifier.size(22.dp),
                        )
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = tint,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionPromptScreen(
    onRequestPermission: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.LibraryMusic,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp),
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "欢迎使用简约音乐",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "应用完全离线运行，不收集任何个人数据。请授予本地音频读取权限以扫描并播放手机中的音乐文件。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRequestPermission) {
                Text("授予音乐读取权限")
            }
        }
    }
}

@Composable
private fun PureMusicSplashOverlay(isDark: Boolean) {
    val bgColor = if (isDark) Color(0xFF141312) else Color(0xFFF5F0E6)
    val inkPrimary = if (isDark) Color(0xFFF2ECE1) else Color(0xFF1E1B18)
    val inkMuted = if (isDark) Color(0xFF9C9388) else Color(0xFF8C7E6D)
    val cinnabar = Color(0xFFA9321E)

    val riseProgress = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(Unit) {
        riseProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 720),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {},
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(width = 180.dp, height = 228.dp),
                contentAlignment = Alignment.BottomCenter,
            ) {
                // Rising 96-groove glossy black vinyl LP emerging out of top of cinnabar sleeve
                val discOffsetY = (-24f - riseProgress.value * 44f).dp
                Canvas(
                    modifier = Modifier
                        .size(144.dp)
                        .offset(y = discOffsetY),
                ) {
                    val r = size.minDimension / 2f
                    val c = Offset(size.width / 2f, size.height / 2f)
                    drawCircle(color = Color(0xFF0D0D0F), radius = r, center = c)
                    for (i in 0..16) {
                        val f = 0.46f + i * 0.03f
                        drawCircle(
                            color = if (i % 2 == 0) Color.White.copy(alpha = 0.11f) else Color.Black.copy(alpha = 0.35f),
                            radius = r * f,
                            center = c,
                            style = Stroke(width = 1.0f),
                        )
                    }
                    drawCircle(color = cinnabar, radius = r * 0.34f, center = c)
                }

                // Matte cinnabar-red square archival sleeve embossed with geometric turntable mark
                Box(
                    modifier = Modifier
                        .size(164.dp)
                        .shadow(18.dp, androidx.compose.ui.graphics.RectangleShape, ambientColor = Color.Black.copy(alpha = 0.32f))
                        .background(cinnabar),
                    contentAlignment = Alignment.Center,
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val embossInk = Color(0xFFF4E8D4).copy(alpha = 0.90f)
                        val sw = 3.2f
                        val emCx = size.width * 0.47f
                        val emCy = size.height * 0.52f
                        val emR = size.width * 0.255f
                        drawCircle(color = embossInk, radius = emR, center = Offset(emCx, emCy), style = Stroke(width = sw))
                        drawCircle(color = embossInk, radius = emR * 0.32f, center = Offset(emCx, emCy), style = Stroke(width = sw))
                        drawCircle(color = embossInk, radius = 4f, center = Offset(emCx, emCy))

                        val pivX = emCx + emR * 1.15f
                        val pivY = emCy - emR * 0.82f
                        val pivR = emR * 0.28f
                        drawCircle(color = embossInk, radius = pivR, center = Offset(pivX, pivY), style = Stroke(width = sw))
                        drawLine(
                            color = embossInk,
                            start = Offset(pivX, pivY + pivR * 0.3f),
                            end = Offset(pivX, emCy + emR * 0.16f),
                            strokeWidth = sw,
                        )
                        drawLine(
                            color = embossInk,
                            start = Offset(pivX, emCy + emR * 0.16f),
                            end = Offset(emCx + emR * 0.62f, emCy + emR * 0.66f),
                            strokeWidth = sw,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))
            Text(
                text = "简约音乐",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 4.sp,
                ),
                color = inkPrimary,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "PURE MUSIC · HI-RES AUDIO",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                color = inkMuted,
            )
        }
    }
}
