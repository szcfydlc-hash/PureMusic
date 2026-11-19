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
import com.puremusic.player.data.model.AppFontStyle
import com.puremusic.player.data.model.Artist
import com.puremusic.player.data.model.BackgroundStyle
import com.puremusic.player.data.model.EqualizerState
import com.puremusic.player.data.model.LayoutMode
import com.puremusic.player.data.model.LyricLine
import com.puremusic.player.data.model.MusicFolder
import com.puremusic.player.data.model.PlayerTheme
import com.puremusic.player.data.model.PlayerUiState
import com.puremusic.player.data.model.Playlist
import com.puremusic.player.data.model.RepeatMode
import com.puremusic.player.data.model.Song
import com.puremusic.player.data.model.SortOrder
import com.puremusic.player.data.repository.LyricsRepository
import com.puremusic.player.data.repository.MusicRepository
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
    private val lyricsRepository = LyricsRepository()
    private val sleepTimerManager = SleepTimerManager()

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private var positionTickerJob: Job? = null
    private var lyricsJob: Job? = null
    private var lastLoadedLyricsSongId: Long? = null

    private val _rawSongs = MutableStateFlow<List<Song>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _currentLyrics = MutableStateFlow<List<LyricLine>>(emptyList())
    val currentLyrics: StateFlow<List<LyricLine>> = _currentLyrics.asStateFlow()

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
        initialValue = AppFontStyle.SANS,
    )

    val backgroundStyle: StateFlow<BackgroundStyle> = playlistRepository.backgroundStyleFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BackgroundStyle.WARM_PAPER,
    )

    val layoutMode: StateFlow<LayoutMode> = playlistRepository.layoutModeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LayoutMode.AUTO,
    )

    val allSongs: StateFlow<List<Song>> = combine(_rawSongs, sortOrder) { songs, order ->
        musicRepository.sortSongs(songs, order)
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

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            syncPlayerSnapshot()
            if (isPlaying) startPositionTicker() else stopPositionTicker()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            syncPlayerSnapshot()
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
                    if (controller.isPlaying) startPositionTicker()
                }
            },
            ContextCompat.getMainExecutor(app),
        )
    }

    fun loadLibrary() {
        viewModelScope.launch {
            _isLoading.value = true
            val loaded = runCatching { musicRepository.loadLocalSongs() }.getOrDefault(emptyList())
            _rawSongs.value = loaded
            _isLoading.value = false
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

    fun cancelSleepTimer() {
        sleepTimerManager.cancel()
    }

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

    fun setBackgroundStyle(style: BackgroundStyle) {
        viewModelScope.launch { playlistRepository.setBackgroundStyle(style) }
    }

    fun setLayoutMode(mode: LayoutMode) {
        viewModelScope.launch { playlistRepository.setLayoutMode(mode) }
    }

    fun resetAppearance() {
        viewModelScope.launch { playlistRepository.resetAppearance() }
    }

    fun resolveSongsByIds(ids: List<Long>): List<Song> {
        val lookup = _rawSongs.value.associateBy { it.id }
        return ids.mapNotNull { lookup[it] }
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
        val songLookup = _rawSongs.value.associateBy { it.id }

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
            }
        }
    }

    private fun stopPositionTicker() {
        positionTickerJob?.cancel()
        positionTickerJob = null
    }

    override fun onCleared() {
        stopPositionTicker()
        lyricsJob?.cancel()
        sleepTimerManager.cancel()
        mediaController?.removeListener(playerListener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
        super.onCleared()
    }
}
