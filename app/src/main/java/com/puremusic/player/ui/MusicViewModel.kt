package com.puremusic.player.ui

import android.app.Application
import android.content.ComponentName
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.puremusic.player.data.model.Album
import com.puremusic.player.data.model.AppAccentColor
import com.puremusic.player.data.model.AppFontStyle
import com.puremusic.player.data.model.Artist
import com.puremusic.player.data.model.BackgroundStyle
import com.puremusic.player.data.model.EqualizerState
import com.puremusic.player.data.model.LayoutDensity
import com.puremusic.player.data.model.LayoutMode
import com.puremusic.player.data.model.LiquidGlassLevel
import com.puremusic.player.data.model.LyricFontStyle
import com.puremusic.player.data.model.LyricLine
import com.puremusic.player.data.model.MinDurationFilter
import com.puremusic.player.data.model.MiniPlayerStyle
import com.puremusic.player.data.model.MusicFolder
import com.puremusic.player.data.model.PlayerTheme
import com.puremusic.player.data.model.PlayerUiState
import com.puremusic.player.data.model.Playlist
import com.puremusic.player.data.model.RepeatMode
import com.puremusic.player.data.model.ScanStatus
import com.puremusic.player.data.model.Song
import com.puremusic.player.data.model.SortOrder
import com.puremusic.player.data.repository.LyricsRepository
import com.puremusic.player.data.repository.MusicRepository
import android.net.Uri
import com.puremusic.player.data.repository.OnlineCoverCandidate
import com.puremusic.player.data.repository.OnlineLyricCandidate
import com.puremusic.player.data.repository.OnlineMetadataRepository
import com.puremusic.player.data.repository.PlaylistRepository
import com.puremusic.player.service.AudioEqualizerManager
import com.puremusic.player.service.PlaybackService
import com.puremusic.player.service.SleepTimerManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val musicRepository = MusicRepository(application)
    private val playlistRepository = PlaylistRepository(application)
    private val lyricsRepository = LyricsRepository(application)
    private val onlineMetadataRepository = OnlineMetadataRepository(application)
    private val sleepTimerManager = SleepTimerManager()

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private var positionTickerJob: Job? = null
    private var lyricsJob: Job? = null
    private var lastLoadedLyricsSongId: Long? = null

    private val _rawSongs = MutableStateFlow<List<Song>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _scanStatus = MutableStateFlow(ScanStatus())
    val scanStatus: StateFlow<ScanStatus> = _scanStatus.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _currentLyrics = MutableStateFlow<List<LyricLine>>(emptyList())
    val currentLyrics: StateFlow<List<LyricLine>> = _currentLyrics.asStateFlow()

    private val _customCovers = MutableStateFlow<Map<Long, Uri>>(emptyMap())

    private val _lyricSearchResults = MutableStateFlow<List<OnlineLyricCandidate>>(emptyList())
    val lyricSearchResults: StateFlow<List<OnlineLyricCandidate>> = _lyricSearchResults.asStateFlow()
    private val _isSearchingLyrics = MutableStateFlow(false)
    val isSearchingLyrics: StateFlow<Boolean> = _isSearchingLyrics.asStateFlow()
    private val _lyricSearchError = MutableStateFlow<String?>(null)
    val lyricSearchError: StateFlow<String?> = _lyricSearchError.asStateFlow()

    private val _coverSearchResults = MutableStateFlow<List<OnlineCoverCandidate>>(emptyList())
    val coverSearchResults: StateFlow<List<OnlineCoverCandidate>> = _coverSearchResults.asStateFlow()
    private val _isSearchingCovers = MutableStateFlow(false)
    val isSearchingCovers: StateFlow<Boolean> = _isSearchingCovers.asStateFlow()
    private val _coverSearchError = MutableStateFlow<String?>(null)
    val coverSearchError: StateFlow<String?> = _coverSearchError.asStateFlow()

    val sortOrder: StateFlow<SortOrder> = playlistRepository.sortOrderFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SortOrder.TITLE,
    )

    val playlists: StateFlow<List<Playlist>> = playlistRepository.playlistsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val favoriteSongIds: StateFlow<Set<Long>> = playlistRepository.favoriteSongIdsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptySet(),
    )

    val playerTheme: StateFlow<PlayerTheme> = playlistRepository.playerThemeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PlayerTheme.POSTER_WALL,
    )

    val fontStyle: StateFlow<AppFontStyle> = playlistRepository.fontStyleFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppFontStyle.SERIF,
    )

    val lyricFontStyle: StateFlow<LyricFontStyle> = playlistRepository.lyricFontStyleFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LyricFontStyle.SERIF_ITALIC,
    )

    val backgroundStyle: StateFlow<BackgroundStyle> = playlistRepository.backgroundStyleFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BackgroundStyle.WARM_PAPER,
    )

    val layoutDensity: StateFlow<LayoutDensity> = playlistRepository.layoutDensityFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LayoutDensity.STANDARD,
    )

    val fontScale: StateFlow<Float> = playlistRepository.fontScaleFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 1.0f,
    )

    val layoutMode: StateFlow<LayoutMode> = playlistRepository.layoutModeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LayoutMode.AUTO,
    )

    val accentColor: StateFlow<AppAccentColor> = playlistRepository.accentColorFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppAccentColor.CINNABAR_RED,
    )

    val liquidGlassEnabled: StateFlow<Boolean> = playlistRepository.liquidGlassEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false,
    )

    val floatingControlsEnabled: StateFlow<Boolean> = playlistRepository.floatingControlsEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false,
    )

    val liquidGlassLevel: StateFlow<LiquidGlassLevel> = playlistRepository.liquidGlassLevelFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LiquidGlassLevel.FULL,
    )

    val miniPlayerStyle: StateFlow<MiniPlayerStyle> = playlistRepository.miniPlayerStyleFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MiniPlayerStyle.CLASSIC_BOOK,
    )

    val miniPlayerHeightDp: StateFlow<Int> = playlistRepository.miniPlayerHeightDpFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 58,
    )

    val excludedFolders: StateFlow<Set<String>> = playlistRepository.excludedFoldersFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptySet(),
    )

    val minDurationFilter: StateFlow<MinDurationFilter> = playlistRepository.minDurationFilterFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MinDurationFilter.NONE,
    )

    // All discovered folders (unfiltered) for Library Management checklist
    val allDiscoveredFolders: StateFlow<List<MusicFolder>> = _rawSongs.combine(sortOrder) { songs, _ ->
        musicRepository.groupByFolders(songs)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    // Active songs after applying custom covers + folder exclusions + short-audio duration filter + sort
    val allSongs: StateFlow<List<Song>> = combine(
        _rawSongs,
        _customCovers,
        excludedFolders,
        minDurationFilter,
        sortOrder,
    ) { songs, coverMap, excluded, minDur, order ->
        val filtered = songs.filter { song ->
            song.folderPath !in excluded && song.durationMs >= minDur.minMs
        }.map { song ->
            val customUri = coverMap[song.id]
            if (customUri != null) song.copy(albumArtUri = customUri) else song
        }
        musicRepository.sortSongs(filtered, order)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val filteredSongs: StateFlow<List<Song>> = combine(allSongs, _searchQuery) { songs, query ->
        val q = query.trim().lowercase()
        if (q.isEmpty()) {
            songs
        } else {
            songs.filter {
                it.title.lowercase().contains(q) ||
                    it.artist.lowercase().contains(q) ||
                    it.album.lowercase().contains(q)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val albums: StateFlow<List<Album>> = combine(allSongs, _searchQuery) { songs, query ->
        val grouped = musicRepository.groupByAlbums(songs)
        val q = query.trim().lowercase()
        if (q.isEmpty()) grouped else grouped.filter {
            it.name.lowercase().contains(q) || it.artist.lowercase().contains(q)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val artists: StateFlow<List<Artist>> = combine(allSongs, _searchQuery) { songs, query ->
        val grouped = musicRepository.groupByArtists(songs)
        val q = query.trim().lowercase()
        if (q.isEmpty()) grouped else grouped.filter { it.name.lowercase().contains(q) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val folders: StateFlow<List<MusicFolder>> = combine(allSongs, _searchQuery) { songs, query ->
        val grouped = musicRepository.groupByFolders(songs)
        val q = query.trim().lowercase()
        if (q.isEmpty()) grouped else grouped.filter {
            it.name.lowercase().contains(q) || it.path.lowercase().contains(q)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    private val _playerState = MutableStateFlow(PlayerUiState())
    val playerState: StateFlow<PlayerUiState> = _playerState.asStateFlow()

    val equalizerState: StateFlow<EqualizerState> = AudioEqualizerManager.state
    val sleepTimerRemainingMs: StateFlow<Long?> = sleepTimerManager.remainingMs

    private var activeQueueSongs: List<Song> = emptyList()
    private var hasRestoredPlayback = false

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            syncPlayerSnapshot()
            if (isPlaying) {
                startPositionTicker()
            } else {
                stopPositionTicker()
                persistCurrentPlayback()
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            syncPlayerSnapshot()
            persistCurrentPlayback()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            syncPlayerSnapshot()
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            syncPlayerSnapshot()
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            syncPlayerSnapshot()
        }
    }

    init {
        viewModelScope.launch {
            playlistRepository.ensureV8BookStyleEpoch()
            _customCovers.value = onlineMetadataRepository.loadAllCustomCovers()
            syncPlayerSnapshot()
        }
        connectMediaController()
    }

    private fun connectMediaController() {
        val app = getApplication<Application>()
        val token = SessionToken(app, ComponentName(app, PlaybackService::class.java))
        val future = MediaController.Builder(app, token).buildAsync()
        controllerFuture = future
        future.addListener(
            {
                runCatching {
                    val controller = future.get()
                    mediaController = controller
                    controller.addListener(playerListener)
                    syncPlayerSnapshot()
                    tryRestorePlaybackSnapshot()
                    if (controller.isPlaying) startPositionTicker()
                }
            },
            ContextCompat.getMainExecutor(app),
        )
    }

    private fun tryRestorePlaybackSnapshot() {
        if (hasRestoredPlayback) return
        val controller = mediaController ?: return
        val songs = _rawSongs.value
        if (songs.isEmpty()) return
        if (controller.mediaItemCount > 0) {
            hasRestoredPlayback = true
            return
        }
        hasRestoredPlayback = true
        viewModelScope.launch {
            val snap = runCatching { playlistRepository.loadPlaybackSnapshot() }.getOrNull() ?: return@launch
            val lookup = songs.associateBy { it.id }
            val restoredQueue = snap.queueIds.mapNotNull { lookup[it] }
            val targetSong = snap.lastSongId?.let { lookup[it] } ?: restoredQueue.firstOrNull() ?: return@launch
            val finalQueue = if (restoredQueue.isNotEmpty()) restoredQueue else listOf(targetSong)
            val startIdx = finalQueue.indexOfFirst { it.id == targetSong.id }.coerceAtLeast(0)

            activeQueueSongs = finalQueue
            val mediaItems = finalQueue.map { song ->
                MediaItem.Builder()
                    .setMediaId(song.id.toString())
                    .setUri(song.contentUri)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(song.title)
                            .setArtist(song.artist)
                            .setAlbumTitle(song.album)
                            .setArtworkUri(song.albumArtUri)
                            .build(),
                    )
                    .build()
            }
            controller.setMediaItems(mediaItems, startIdx, snap.lastPositionMs)
            controller.prepare()
            syncPlayerSnapshot()
        }
    }

    private fun persistCurrentPlayback() {
        val current = _playerState.value.currentSong ?: return
        val pos = _playerState.value.positionMs
        val qIds = _playerState.value.queue.map { it.id }
        viewModelScope.launch {
            playlistRepository.savePlaybackSnapshot(current.id, pos, qIds)
        }
    }

    fun loadLibrary() {
        viewModelScope.launch {
            _isLoading.value = true
            val loaded = runCatching { musicRepository.loadLocalSongs() }.getOrDefault(emptyList())
            _rawSongs.value = loaded
            _isLoading.value = false
            syncPlayerSnapshot()
            tryRestorePlaybackSnapshot()
        }
    }

    fun rescanLibraryWithFeedback() {
        if (_scanStatus.value.isScanning) return
        viewModelScope.launch {
            _scanStatus.value = ScanStatus(isScanning = true, message = "正在扫描本地音频目录...")
            val loaded = runCatching { musicRepository.triggerMediaScanAndLoad() }.getOrDefault(emptyList())
            _rawSongs.value = loaded
            val visibleCount = loaded.count {
                it.folderPath !in excludedFolders.value && it.durationMs >= minDurationFilter.value.minMs
            }
            _scanStatus.value = ScanStatus(
                isScanning = false,
                message = "扫描完成：共索引 ${loaded.size} 首音频，当前曲库显示 $visibleCount 首",
            )
            syncPlayerSnapshot()
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun playSongs(songs: List<Song>, startIndex: Int = 0, shuffle: Boolean = false) {
        if (songs.isEmpty()) return
        val controller = mediaController ?: return
        val safeIndex = startIndex.coerceIn(0, songs.lastIndex)

        activeQueueSongs = songs
        val mediaItems = songs.map { song ->
            MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setUri(song.contentUri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setAlbumTitle(song.album)
                        .setArtworkUri(song.albumArtUri)
                        .build(),
                )
                .build()
        }

        controller.shuffleModeEnabled = shuffle
        controller.setMediaItems(mediaItems, safeIndex, 0L)
        controller.prepare()
        controller.play()
        syncPlayerSnapshot()
    }

    fun togglePlayPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            if (controller.mediaItemCount == 0 && allSongs.value.isNotEmpty()) {
                playSongs(allSongs.value, 0)
            } else {
                controller.play()
            }
        }
    }

    fun skipNext() {
        mediaController?.seekToNextMediaItem()
    }

    fun skipPrevious() {
        val controller = mediaController ?: return
        if (controller.currentPosition > 3_000L) {
            controller.seekTo(0L)
        } else {
            controller.seekToPreviousMediaItem()
        }
    }

    fun seekTo(positionMs: Long) {
        val controller = mediaController ?: return
        val clamped = positionMs.coerceAtLeast(0L)
        controller.seekTo(clamped)
        _playerState.update { it.copy(positionMs = clamped) }
    }

    fun toggleShuffle() {
        val controller = mediaController ?: return
        controller.shuffleModeEnabled = !controller.shuffleModeEnabled
    }

    fun cycleRepeatMode() {
        val controller = mediaController ?: return
        controller.repeatMode = when (controller.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    fun playQueueIndex(index: Int) {
        val controller = mediaController ?: return
        if (index in 0 until controller.mediaItemCount) {
            controller.seekTo(index, 0L)
            controller.play()
        }
    }

    fun removeFromQueue(index: Int) {
        val controller = mediaController ?: return
        if (index in 0 until controller.mediaItemCount) {
            controller.removeMediaItem(index)
            if (index in activeQueueSongs.indices) {
                activeQueueSongs = activeQueueSongs.toMutableList().apply { removeAt(index) }
            }
            syncPlayerSnapshot()
        }
    }

    fun startSleepTimer(minutes: Int) {
        sleepTimerManager.start(minutes) {
            mediaController?.pause()
        }
    }

    fun cancelSleepTimer() = sleepTimerManager.cancel()

    fun setEqualizerEnabled(enabled: Boolean) = AudioEqualizerManager.setEnabled(enabled)

    fun setEqualizerBand(bandIndex: Int, levelMb: Int) = AudioEqualizerManager.setBandLevel(bandIndex, levelMb)

    fun selectEqualizerPreset(presetIndex: Int) = AudioEqualizerManager.selectPreset(presetIndex)

    fun createPlaylist(name: String, initialSongIds: List<Long> = emptyList()) {
        viewModelScope.launch { playlistRepository.createPlaylist(name, initialSongIds) }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch { playlistRepository.deletePlaylist(playlistId) }
    }

    fun addSongToPlaylist(playlistId: String, songId: Long) {
        viewModelScope.launch { playlistRepository.addSongToPlaylist(playlistId, songId) }
    }

    fun removeSongFromPlaylist(playlistId: String, songId: Long) {
        viewModelScope.launch { playlistRepository.removeSongFromPlaylist(playlistId, songId) }
    }

    fun toggleFavorite(songId: Long) {
        viewModelScope.launch { playlistRepository.toggleFavorite(songId) }
    }

    fun setSortOrder(order: SortOrder) {
        viewModelScope.launch { playlistRepository.setSortOrder(order) }
    }

    fun setPlayerTheme(theme: PlayerTheme) {
        viewModelScope.launch { playlistRepository.setPlayerTheme(theme) }
    }

    fun setFontStyle(style: AppFontStyle) {
        viewModelScope.launch { playlistRepository.setFontStyle(style) }
    }

    fun setLyricFontStyle(style: LyricFontStyle) {
        viewModelScope.launch { playlistRepository.setLyricFontStyle(style) }
    }

    fun setBackgroundStyle(style: BackgroundStyle) {
        viewModelScope.launch { playlistRepository.setBackgroundStyle(style) }
    }

    fun setLayoutDensity(density: LayoutDensity) {
        viewModelScope.launch { playlistRepository.setLayoutDensity(density) }
    }

    fun setFontScale(scale: Float) {
        viewModelScope.launch { playlistRepository.setFontScale(scale) }
    }

    fun setLayoutMode(mode: LayoutMode) {
        viewModelScope.launch { playlistRepository.setLayoutMode(mode) }
    }

    fun setAccentColor(color: AppAccentColor) {
        viewModelScope.launch { playlistRepository.setAccentColor(color) }
    }

    fun setLiquidGlassEnabled(enabled: Boolean) {
        viewModelScope.launch { playlistRepository.setLiquidGlassEnabled(enabled) }
    }

    fun setFloatingControlsEnabled(enabled: Boolean) {
        viewModelScope.launch { playlistRepository.setFloatingControlsEnabled(enabled) }
    }

    fun setLiquidGlassLevel(level: LiquidGlassLevel) {
        viewModelScope.launch { playlistRepository.setLiquidGlassLevel(level) }
    }

    fun setMiniPlayerStyle(style: MiniPlayerStyle) {
        viewModelScope.launch { playlistRepository.setMiniPlayerStyle(style) }
    }

    fun setMiniPlayerHeightDp(heightDp: Int) {
        viewModelScope.launch { playlistRepository.setMiniPlayerHeightDp(heightDp) }
    }

    fun toggleFolderExcluded(folderPath: String) {
        viewModelScope.launch { playlistRepository.toggleFolderExcluded(folderPath) }
    }

    fun setMinDurationFilter(filter: MinDurationFilter) {
        viewModelScope.launch { playlistRepository.setMinDurationFilter(filter) }
    }

    fun resetAppearance() {
        viewModelScope.launch { playlistRepository.resetAppearance() }
    }

    fun resolveSongsByIds(ids: List<Long>): List<Song> {
        val coverMap = _customCovers.value
        val lookup = _rawSongs.value.associate { s -> s.id to (coverMap[s.id]?.let { s.copy(albumArtUri = it) } ?: s) }
        return ids.mapNotNull { lookup[it] }
    }

    fun searchOnlineLyrics(query: String) {
        viewModelScope.launch {
            _isSearchingLyrics.value = true
            _lyricSearchError.value = null
            val res = onlineMetadataRepository.searchOnlineLyrics(query)
            res.onSuccess { list ->
                _lyricSearchResults.value = list
                if (list.isEmpty()) _lyricSearchError.value = "未找到匹配歌词，可尝试精简关键词重新搜索。"
            }.onFailure {
                _lyricSearchResults.value = emptyList()
                _lyricSearchError.value = "网络连接失败，请检查网络或改用本地导入 .lrc。"
            }
            _isSearchingLyrics.value = false
        }
    }

    fun searchOnlineCovers(query: String) {
        viewModelScope.launch {
            _isSearchingCovers.value = true
            _coverSearchError.value = null
            val res = onlineMetadataRepository.searchOnlineCovers(query)
            res.onSuccess { list ->
                _coverSearchResults.value = list
                if (list.isEmpty()) _coverSearchError.value = "未找到匹配封面，可尝试仅输入歌名或专辑名。"
            }.onFailure {
                _coverSearchResults.value = emptyList()
                _coverSearchError.value = "网络连接失败，请检查网络或从手机相册选择。"
            }
            _isSearchingCovers.value = false
        }
    }

    fun applyOnlineLyrics(song: Song, candidate: OnlineLyricCandidate) {
        viewModelScope.launch {
            val saved = onlineMetadataRepository.saveCustomLyricsText(song.id, candidate.bestLyricsText)
            if (saved && _playerState.value.currentSong?.id == song.id) {
                lastLoadedLyricsSongId = null
                refreshLyricsIfNeeded(song)
            }
        }
    }

    fun importLocalLyrics(song: Song, uri: Uri) {
        viewModelScope.launch {
            val saved = onlineMetadataRepository.saveCustomLyricsFromUri(song.id, uri)
            if (saved && _playerState.value.currentSong?.id == song.id) {
                lastLoadedLyricsSongId = null
                refreshLyricsIfNeeded(song)
            }
        }
    }

    fun clearCustomLyrics(song: Song) {
        viewModelScope.launch {
            onlineMetadataRepository.clearCustomLyrics(song.id)
            if (_playerState.value.currentSong?.id == song.id) {
                lastLoadedLyricsSongId = null
                refreshLyricsIfNeeded(song)
            }
        }
    }

    fun applyOnlineCover(song: Song, candidate: OnlineCoverCandidate, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            val savedUri = onlineMetadataRepository.saveCustomCoverFromUrl(song.id, candidate.highResUrl)
            if (savedUri != null) {
                _customCovers.update { it + (song.id to savedUri) }
                syncPlayerSnapshot()
            }
            onDone()
        }
    }

    fun applyGalleryCover(song: Song, sourceUri: Uri) {
        viewModelScope.launch {
            val savedUri = onlineMetadataRepository.saveCustomCoverFromUri(song.id, sourceUri)
            if (savedUri != null) {
                _customCovers.update { it + (song.id to savedUri) }
                syncPlayerSnapshot()
            }
        }
    }

    fun clearCustomCover(song: Song) {
        viewModelScope.launch {
            onlineMetadataRepository.clearCustomCover(song.id)
            _customCovers.update { it - song.id }
            syncPlayerSnapshot()
        }
    }

    private fun refreshLyricsIfNeeded(song: Song?) {
        if (song == null) {
            lastLoadedLyricsSongId = null
            _currentLyrics.value = emptyList()
            return
        }
        if (song.id == lastLoadedLyricsSongId) return
        lastLoadedLyricsSongId = song.id
        lyricsJob?.cancel()
        lyricsJob = viewModelScope.launch {
            _currentLyrics.value = lyricsRepository.loadLyricsForSong(song)
        }
    }

    private fun syncPlayerSnapshot() {
        val controller = mediaController ?: return
        val coverMap = _customCovers.value
        val songLookup = _rawSongs.value.associate { s -> s.id to (coverMap[s.id]?.let { s.copy(albumArtUri = it) } ?: s) }

        val queue = buildList {
            for (i in 0 until controller.mediaItemCount) {
                val id = controller.getMediaItemAt(i).mediaId.toLongOrNull()
                val matched = (id?.let { songLookup[it] }) ?: activeQueueSongs.getOrNull(i)
                if (matched != null) add(matched)
            }
        }

        val currentIndex = controller.currentMediaItemIndex
        val currentMediaId = controller.currentMediaItem?.mediaId?.toLongOrNull()
        val currentSong = currentMediaId?.let { songLookup[it] } ?: queue.getOrNull(currentIndex)
        val duration = controller.duration.takeIf { it > 0L } ?: currentSong?.durationMs ?: 0L

        val repeat = when (controller.repeatMode) {
            Player.REPEAT_MODE_ALL -> RepeatMode.ALL
            Player.REPEAT_MODE_ONE -> RepeatMode.ONE
            else -> RepeatMode.OFF
        }

        _playerState.value = PlayerUiState(
            currentSong = currentSong,
            queue = queue,
            currentQueueIndex = currentIndex,
            isPlaying = controller.isPlaying,
            positionMs = controller.currentPosition.coerceAtLeast(0L),
            durationMs = duration,
            shuffleEnabled = controller.shuffleModeEnabled,
            repeatMode = repeat,
        )
        refreshLyricsIfNeeded(currentSong)
    }

    private fun startPositionTicker() {
        if (positionTickerJob?.isActive == true) return
        positionTickerJob = viewModelScope.launch {
            while (isActive) {
                val controller = mediaController
                if (controller != null && controller.isPlaying) {
                    val pos = controller.currentPosition.coerceAtLeast(0L)
                    val dur = controller.duration.takeIf { it > 0L } ?: _playerState.value.durationMs
                    _playerState.update { it.copy(positionMs = pos, durationMs = dur) }
                }
                delay(400L)
                if (System.currentTimeMillis() % 5000L < 450L) {
                    persistCurrentPlayback()
                }
            }
        }
    }

    private fun stopPositionTicker() {
        positionTickerJob?.cancel()
        positionTickerJob = null
    }

    override fun onCleared() {
        persistCurrentPlayback()
        stopPositionTicker()
        lyricsJob?.cancel()
        sleepTimerManager.cancel()
        mediaController?.removeListener(playerListener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
        super.onCleared()
    }
}
