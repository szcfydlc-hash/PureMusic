package com.puremusic.player

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.puremusic.player.data.model.Song
import com.puremusic.player.ui.MusicViewModel
import com.puremusic.player.ui.components.AddToPlaylistDialog
import com.puremusic.player.ui.components.CreatePlaylistDialog
import com.puremusic.player.ui.components.EqualizerBottomSheet
import com.puremusic.player.ui.components.LyricsBottomSheet
import com.puremusic.player.ui.components.MiniPlayerBar
import com.puremusic.player.ui.components.QueueBottomSheet
import com.puremusic.player.ui.components.SleepTimerDialog
import com.puremusic.player.ui.screens.LibraryScreen
import com.puremusic.player.ui.screens.NowPlayingScreen
import com.puremusic.player.ui.screens.SettingsScreen
import com.puremusic.player.ui.theme.PureMusicTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    val accentColor by viewModel.accentColor.collectAsStateWithLifecycle()
    val liquidGlassEnabled by viewModel.liquidGlassEnabled.collectAsStateWithLifecycle()

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
        val favoriteSongIds by viewModel.favoriteSongIds.collectAsStateWithLifecycle()
        val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
        val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
        val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
        val playerState by viewModel.playerState.collectAsStateWithLifecycle()
        val currentLyrics by viewModel.currentLyrics.collectAsStateWithLifecycle()
        val equalizerState by viewModel.equalizerState.collectAsStateWithLifecycle()
        val sleepTimerRemainingMs by viewModel.sleepTimerRemainingMs.collectAsStateWithLifecycle()

        var showNowPlaying by remember { mutableStateOf(false) }
        var showSettings by remember { mutableStateOf(false) }
        var showEqualizer by remember { mutableStateOf(false) }
        var showSleepTimer by remember { mutableStateOf(false) }
        var showQueue by remember { mutableStateOf(false) }
        var showLyrics by remember { mutableStateOf(false) }
        var showCreatePlaylist by remember { mutableStateOf(false) }
        var pendingPlaylistSong by remember { mutableStateOf<Song?>(null) }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isTabletLayout = when (layoutMode) {
                LayoutMode.TABLET -> true
                LayoutMode.PHONE -> false
                LayoutMode.AUTO -> maxWidth >= 600.dp
            }
            val miniPlayerVisible = playerState.currentSong != null && !isTabletLayout

            LibraryScreen(
                songs = songs,
                allSongs = allSongs,
                albums = albums,
                artists = artists,
                folders = folders,
                playlists = playlists,
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
                onRequestCreatePlaylist = { showCreatePlaylist = true },
                onDeletePlaylist = viewModel::deletePlaylist,
                onRemoveSongFromPlaylist = viewModel::removeSongFromPlaylist,
                onOpenEqualizer = { showEqualizer = true },
                onOpenSleepTimer = { showSleepTimer = true },
                onOpenThemeSettings = { showSettings = true },
                resolveSongsByIds = viewModel::resolveSongsByIds,
                bottomContentPadding = PaddingValues(bottom = if (miniPlayerVisible) 88.dp else 16.dp),
            )

            if (miniPlayerVisible) {
                MiniPlayerBar(
                    playerState = playerState,
                    liquidGlassEnabled = liquidGlassEnabled,
                    onOpenNowPlaying = { showNowPlaying = true },
                    onTogglePlayPause = viewModel::togglePlayPause,
                    onSkipNext = viewModel::skipNext,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding(),
                )
            }

            AnimatedVisibility(
                visible = showNowPlaying && playerState.currentSong != null,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
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
                    onToggleShuffle = viewModel::toggleShuffle,
                    onCycleRepeat = viewModel::cycleRepeatMode,
                    onToggleFavorite = { viewModel.toggleFavorite(it.id) },
                    onOpenQueue = { showQueue = true },
                    onOpenLyrics = { showLyrics = true },
                    onOpenEqualizer = { showEqualizer = true },
                    onOpenSleepTimer = { showSleepTimer = true },
                    onOpenSettings = { showSettings = true },
                    onAddToPlaylist = { pendingPlaylistSong = it },
                )
            }

            AnimatedVisibility(
                visible = showSettings,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                SettingsScreen(
                    songCount = allSongs.size,
                    albumCount = albums.size,
                    allFolders = allDiscoveredFolders,
                    excludedFolders = excludedFolders,
                    minDurationFilter = minDurationFilter,
                    scanStatus = scanStatus,
                    layoutMode = layoutMode,
                    liquidGlassEnabled = liquidGlassEnabled,
                    accentColor = accentColor,
                    fontStyle = fontStyle,
                    lyricFontStyle = lyricFontStyle,
                    backgroundStyle = backgroundStyle,
                    onClose = { showSettings = false },
                    onSelectLayoutMode = viewModel::setLayoutMode,
                    onToggleLiquidGlass = viewModel::setLiquidGlassEnabled,
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
                )
            }
        }

        if (showCreatePlaylist) {
            CreatePlaylistDialog(
                onDismiss = { showCreatePlaylist = false },
                onCreate = { viewModel.createPlaylist(it) },
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

        if (showSleepTimer) {
            SleepTimerDialog(
                remainingMs = sleepTimerRemainingMs,
                onDismiss = { showSleepTimer = false },
                onSelectMinutes = viewModel::startSleepTimer,
                onCancelTimer = viewModel::cancelSleepTimer,
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

        if (showLyrics) {
            val lyricFamily = when (lyricFontStyle) {
                LyricFontStyle.SERIF_ITALIC, LyricFontStyle.SERIF -> FontFamily.Serif
                LyricFontStyle.SANS -> FontFamily.SansSerif
                LyricFontStyle.MONOSPACE -> FontFamily.Monospace
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
