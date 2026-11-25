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
import com.puremusic.player.data.model.AppFontWeight
import com.puremusic.player.data.model.AudioTechSpec
import com.puremusic.player.data.model.Artist
import com.puremusic.player.data.model.BackgroundStyle
import com.puremusic.player.data.model.EqualizerState
import com.puremusic.player.data.model.LayoutDensity
import com.puremusic.player.data.model.LayoutMode
import com.puremusic.player.data.model.LiquidGlassLevel
import com.puremusic.player.data.model.ImmersionLyricStyle
import android.widget.Toast
import com.puremusic.player.data.model.KaraokeLyricMode
import com.puremusic.player.data.model.TranslationSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.puremusic.player.data.model.SpectrumStyle
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
import com.puremusic.player.widget.PureMusicWidgetProvider
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.io.File
import com.puremusic.player.data.model.SortOrder
import com.puremusic.player.data.repository.LyricsRepository
import com.puremusic.player.data.repository.MusicRepository
import android.net.Uri
import com.puremusic.player.data.repository.OnlineCoverCandidate
import com.puremusic.player.data.repository.OnlineLyricCandidate
import com.puremusic.player.data.repository.OnlineMetadataRepository
import com.puremusic.player.data.repository.SongMetaOverride
import com.puremusic.player.data.repository.PlaylistRepository
import com.puremusic.player.service.AudioEqualizerManager
import com.puremusic.player.service.AudioSpectrumBus
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
    private var lastRecordedPlaySongId: Long? = null
    private var fadeInJob: Job? = null
    private var audioSpecJob: Job? = null
    private var lastSpecSongId: Long? = null

    private val _rawSongs = MutableStateFlow<List<Song>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _scanStatus = MutableStateFlow(ScanStatus())
    val scanStatus: StateFlow<ScanStatus> = _scanStatus.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _currentLyrics = MutableStateFlow<List<LyricLine>>(emptyList())
    val currentLyrics: StateFlow<List<LyricLine>> = _currentLyrics.asStateFlow()

    private val _currentAudioSpec = MutableStateFlow(AudioTechSpec())
    val currentAudioSpec: StateFlow<AudioTechSpec> = _currentAudioSpec.asStateFlow()

    private val _customCovers = MutableStateFlow<Map<Long, Uri>>(emptyMap())
    private val _playlistCovers = MutableStateFlow<Map<String, Uri>>(emptyMap())
    val playlistCovers: StateFlow<Map<String, Uri>> = _playlistCovers.asStateFlow()
    private val _metaOverrides = MutableStateFlow<Map<Long, SongMetaOverride>>(emptyMap())
    val metaOverrides: StateFlow<Map<Long, SongMetaOverride>> = _metaOverrides.asStateFlow()

    private val _lyricSearchResults = MutableStateFlow<List<OnlineLyricCandidate>>(emptyList())
    val lyricSearchResults: StateFlow<List<OnlineLyricCandidate>> = _lyricSearchResults.asStateFlow()
    private val _isSearchingLyrics = MutableStateFlow(false)
    val isSearchingLyrics: StateFlow<Boolean> = _isSearchingLyrics.asStateFlow()
    private val _lyricSearchError = MutableStateFlow<String?>(null)
    val lyricSearchError: StateFlow<String?> = _lyricSearchError.asStateFlow()

    private val _isTranslatingLyrics = MutableStateFlow(false)
    val isTranslatingLyrics: StateFlow<Boolean> = _isTranslatingLyrics.asStateFlow()

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
        initialValue = AppFontStyle.SANS,
    )

    val lyricFontStyle: StateFlow<LyricFontStyle> = playlistRepository.lyricFontStyleFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LyricFontStyle.SANS,
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

    val fontWeight: StateFlow<AppFontWeight> = playlistRepository.fontWeightFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppFontWeight.NORMAL,
    )

    val fontWeightValue: StateFlow<Int> = playlistRepository.fontWeightValueFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 400,
    )

    val fontSlantDegrees: StateFlow<Float> = playlistRepository.fontSlantDegreesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 0f,
    )

    val playbackSpeed: StateFlow<Float> = playlistRepository.playbackSpeedFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 1.0f,
    )

    val fadeInSeconds: StateFlow<Int> = playlistRepository.fadeInSecondsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 0,
    )

    val keepScreenOn: StateFlow<Boolean> = playlistRepository.keepScreenOnFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false,
    )


    val hapticEnabled: StateFlow<Boolean> = playlistRepository.hapticEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = true,
    )

    val volumeNormalizationEnabled: StateFlow<Boolean> = playlistRepository.volumeNormalizationFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false,
    )

    val immersiveMoodStrength: StateFlow<Float> = playlistRepository.immersiveMoodStrengthFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 0.85f,
    )

    val immersiveMatteCover: StateFlow<Boolean> = playlistRepository.immersiveMatteCoverFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = true,
    )

    val immersiveVinylRpm: StateFlow<Int> = playlistRepository.immersiveVinylRpmFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 33,
    )

    val gaplessEnabled: StateFlow<Boolean> = playlistRepository.gaplessEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = true,
    )

    val replayGainEnabled: StateFlow<Boolean> = playlistRepository.replayGainEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = true,
    )

    val perDeviceEqEnabled: StateFlow<Boolean> = playlistRepository.perDeviceEqEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = true,
    )

    val cueSupportEnabled: StateFlow<Boolean> = playlistRepository.cueSupportEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = true,
    )

    val carModeEnabled: StateFlow<Boolean> = playlistRepository.carModeEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false,
    )

    val immersionStyle: StateFlow<ImmersionLyricStyle> = playlistRepository.immersionStyleFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ImmersionLyricStyle.VINYL_STUDY,
    )

    val spectrumStyle: StateFlow<SpectrumStyle> = playlistRepository.spectrumStyleFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SpectrumStyle.SILK_RIBBON,
    )

    val karaokeLyricMode: StateFlow<KaraokeLyricMode> = playlistRepository.karaokeLyricModeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = KaraokeLyricMode.SMART,
    )

    val translationSettings: StateFlow<TranslationSettings> = playlistRepository.translationSettingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TranslationSettings(),
    )



    val lyricOffsets: StateFlow<Map<Long, Long>> = playlistRepository.lyricOffsetsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyMap(),
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

    private fun applyOverrides(song: Song, coverMap: Map<Long, Uri>, metaMap: Map<Long, SongMetaOverride>): Song {
        val customUri = coverMap[song.id]
        val meta = metaMap[song.id]
        var s = if (customUri != null) song.copy(albumArtUri = customUri) else song
        if (meta != null) {
            s = s.copy(
                title = meta.title.ifBlank { s.title },
                artist = meta.artist.ifBlank { s.artist },
                album = meta.album.ifBlank { s.album },
                genre = meta.genre.ifBlank { s.genre },
            )
        }
        return s
    }

    // Active songs after applying custom covers + metadata overrides + folder exclusions + short-audio filter + sort
    val allSongs: StateFlow<List<Song>> = combine(
        combine(_rawSongs, _customCovers, _metaOverrides) { songs, covers, metas ->
            songs.map { applyOverrides(it, covers, metas) }
        },
        excludedFolders,
        minDurationFilter,
        sortOrder,
    ) { songs, excluded, minDur, order ->
        val filtered = songs.filter { song ->
            song.folderPath !in excluded && song.durationMs >= minDur.minMs
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

    val recentSongs: StateFlow<List<Song>> = combine(allSongs, playlistRepository.recentSongIdsFlow) { songs, ids ->
        val map = songs.associateBy { it.id }
        ids.mapNotNull { map[it] }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val mostPlayedSongs: StateFlow<List<Song>> = combine(allSongs, playlistRepository.playCountsFlow) { songs, counts ->
        songs.filter { (counts[it.id] ?: 0) > 0 }
            .sortedByDescending { counts[it.id] ?: 0 }
            .take(50)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val playCounts: StateFlow<Map<Long, Int>> = playlistRepository.playCountsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyMap(),
    )

    val hourlyPlays: StateFlow<Map<Int, Int>> = playlistRepository.hourlyPlaysFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyMap(),
    )

    private val _playerState = MutableStateFlow(PlayerUiState())
    val playerState: StateFlow<PlayerUiState> = _playerState.asStateFlow()

    val equalizerState: StateFlow<EqualizerState> = AudioEqualizerManager.state
    val sleepTimerRemainingMs: StateFlow<Long?> = sleepTimerManager.remainingMs

    private val _customFontName = MutableStateFlow<String?>(null)
    val customFontName: StateFlow<String?> = _customFontName.asStateFlow()
    private val _customFontVersion = MutableStateFlow(0)
    val customFontVersion: StateFlow<Int> = _customFontVersion.asStateFlow()

    private val _singleRepeatRemaining = MutableStateFlow(0)
    val singleRepeatRemaining: StateFlow<Int> = _singleRepeatRemaining.asStateFlow()
    private var repeatRestoreMode: Int = Player.REPEAT_MODE_OFF
    private var singleRepeatSongId: Long? = null
    private var lastRepeatDecrementAtMs: Long = 0L
    private var lastTickerSongId: Long? = null
    private var lastTickerPosMs: Long = 0L
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

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int,
        ) {
            if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION &&
                oldPosition.mediaItemIndex == newPosition.mediaItemIndex &&
                _singleRepeatRemaining.value > 0
            ) {
                consumeSingleRepeatPass()
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val newId = mediaItem?.mediaId?.toLongOrNull()
            if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT && _singleRepeatRemaining.value > 0) {
                consumeSingleRepeatPass()
            } else if (reason != Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT && _singleRepeatRemaining.value > 0) {
                if (newId != singleRepeatSongId) {
                    cancelSingleTrackRepeat(restoreControllerMode = true)
                }
            }
            syncPlayerSnapshot()
            persistCurrentPlayback()
            triggerFadeInIfEnabled()
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
            _playlistCovers.value = onlineMetadataRepository.loadAllPlaylistCovers()
            val fontNameFile = File(File(getApplication<Application>().filesDir, "custom_fonts"), "font_name.txt")
            if (fontNameFile.exists()) {
                _customFontName.value = runCatching { fontNameFile.readText().trim().takeIf { it.isNotBlank() } }.getOrNull()
            }
            _metaOverrides.value = onlineMetadataRepository.loadAllMetaOverrides()
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
                    controller.setPlaybackSpeed(playbackSpeed.value)
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
        if (_singleRepeatRemaining.value > 0) {
            _singleRepeatRemaining.value = 0
        }
        controller.repeatMode = when (controller.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    private fun consumeSingleRepeatPass() {
        val now = System.currentTimeMillis()
        if (now - lastRepeatDecrementAtMs < 1_500L) return
        val currentRem = _singleRepeatRemaining.value
        if (currentRem <= 0) return
        lastRepeatDecrementAtMs = now
        val nextRem = (currentRem - 1).coerceAtLeast(0)
        _singleRepeatRemaining.value = nextRem
        val controller = mediaController
        if (nextRem <= 1 && controller != null) {
            // Final pass has begun -> restore normal repeat mode so finishing advances to next track
            controller.repeatMode = if (repeatRestoreMode == Player.REPEAT_MODE_ONE) {
                Player.REPEAT_MODE_OFF
            } else {
                repeatRestoreMode
            }
        }
        if (nextRem == 0) {
            singleRepeatSongId = null
        }
    }

    private fun cancelSingleTrackRepeat(restoreControllerMode: Boolean) {
        _singleRepeatRemaining.value = 0
        singleRepeatSongId = null
        if (restoreControllerMode) {
            mediaController?.let { controller ->
                controller.repeatMode = if (repeatRestoreMode == Player.REPEAT_MODE_ONE) {
                    Player.REPEAT_MODE_OFF
                } else {
                    repeatRestoreMode
                }
            }
        }
    }

    fun setSingleTrackRepeatCount(count: Int) {
        val controller = mediaController ?: return
        if (count <= 1) {
            cancelSingleTrackRepeat(restoreControllerMode = true)
        } else {
            if (_singleRepeatRemaining.value == 0) {
                repeatRestoreMode = controller.repeatMode
            }
            singleRepeatSongId = _playerState.value.currentSong?.id
            lastRepeatDecrementAtMs = 0L
            _singleRepeatRemaining.value = count
            controller.repeatMode = Player.REPEAT_MODE_ONE
        }
        syncPlayerSnapshot()
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

    fun renamePlaylist(playlistId: String, newName: String) {
        viewModelScope.launch { playlistRepository.renamePlaylist(playlistId, newName) }
    }

    fun setPlaylistSongs(playlistId: String, songIds: List<Long>) {
        viewModelScope.launch { playlistRepository.setPlaylistSongs(playlistId, songIds) }
    }

    fun setFavoriteSongIds(songIds: Set<Long>) {
        viewModelScope.launch { playlistRepository.setFavoriteSongIds(songIds) }
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

    fun setFontWeight(weight: AppFontWeight) {
        viewModelScope.launch { playlistRepository.setFontWeight(weight) }
    }

    fun setFontWeightValue(value: Int) {
        viewModelScope.launch { playlistRepository.setFontWeightValue(value) }
    }

    fun setFontSlantDegrees(degrees: Float) {
        viewModelScope.launch { playlistRepository.setFontSlantDegrees(degrees) }
    }

    fun setPlaybackSpeed(speed: Float) {
        val safe = speed.coerceIn(0.5f, 2.0f)
        mediaController?.setPlaybackSpeed(safe)
        viewModelScope.launch { playlistRepository.setPlaybackSpeed(safe) }
    }

    fun setFadeInSeconds(seconds: Int) {
        viewModelScope.launch { playlistRepository.setFadeInSeconds(seconds) }
    }

    fun setKeepScreenOn(enabled: Boolean) {
        viewModelScope.launch { playlistRepository.setKeepScreenOn(enabled) }
    }


    fun setHapticEnabled(enabled: Boolean) {
        viewModelScope.launch { playlistRepository.setHapticEnabled(enabled) }
    }

    fun setVolumeNormalization(enabled: Boolean) {
        AudioEqualizerManager.setVolumeNormalizationEnabled(enabled)
        viewModelScope.launch { playlistRepository.setVolumeNormalization(enabled) }
    }

    fun setImmersiveMoodStrength(strength: Float) {
        viewModelScope.launch { playlistRepository.setImmersiveMoodStrength(strength) }
    }

    fun setImmersiveMatteCover(enabled: Boolean) {
        viewModelScope.launch { playlistRepository.setImmersiveMatteCover(enabled) }
    }

    fun setImmersiveVinylRpm(rpm: Int) {
        viewModelScope.launch { playlistRepository.setImmersiveVinylRpm(rpm) }
    }

    fun setGaplessEnabled(enabled: Boolean) {
        viewModelScope.launch { playlistRepository.setGaplessEnabled(enabled) }
    }

    fun setReplayGainEnabled(enabled: Boolean) {
        AudioEqualizerManager.setVolumeNormalizationEnabled(enabled || volumeNormalizationEnabled.value)
        viewModelScope.launch { playlistRepository.setReplayGainEnabled(enabled) }
    }

    fun setPerDeviceEqEnabled(enabled: Boolean) {
        viewModelScope.launch { playlistRepository.setPerDeviceEqEnabled(enabled) }
    }

    fun setCueSupportEnabled(enabled: Boolean) {
        viewModelScope.launch { playlistRepository.setCueSupportEnabled(enabled) }
    }

    suspend fun exportBackupJson(): String = playlistRepository.exportAllToJson(allSongs.value)

    suspend fun importBackupJson(jsonText: String): Int = playlistRepository.importAllFromJson(jsonText, allSongs.value)

    suspend fun exportM3u8(): String = playlistRepository.exportFavoritesAndPlaylistsToM3u8(allSongs.value)

    suspend fun importM3u8(name: String, m3uText: String): Int = playlistRepository.importPlaylistFromM3u8(name, m3uText, allSongs.value)

    fun setCarModeEnabled(enabled: Boolean) {
        viewModelScope.launch { playlistRepository.setCarModeEnabled(enabled) }
    }

    fun setImmersionStyle(style: ImmersionLyricStyle) {
        viewModelScope.launch { playlistRepository.setImmersionStyle(style) }
    }

    fun setSpectrumStyle(style: SpectrumStyle) {
        viewModelScope.launch { playlistRepository.setSpectrumStyle(style) }
    }

    fun setKaraokeLyricMode(mode: KaraokeLyricMode) {
        viewModelScope.launch { playlistRepository.setKaraokeLyricMode(mode) }
    }

    fun updateTranslationSettings(settings: TranslationSettings) {
        viewModelScope.launch { playlistRepository.updateTranslationSettings(settings) }
    }



    private suspend fun showToast(msg: String) = withContext(Dispatchers.Main) {
        Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
    }

    fun adjustCurrentLyricOffset(deltaMs: Long) {
        val song = _playerState.value.currentSong ?: return
        val current = lyricOffsets.value[song.id] ?: 0L
        viewModelScope.launch {
            playlistRepository.setLyricOffsetMs(song.id, current + deltaMs)
        }
    }

    fun resetCurrentLyricOffset() {
        val song = _playerState.value.currentSong ?: return
        viewModelScope.launch {
            playlistRepository.setLyricOffsetMs(song.id, 0L)
        }
    }

    private fun triggerFadeInIfEnabled() {
        val secs = fadeInSeconds.value
        val controller = mediaController ?: return
        fadeInJob?.cancel()
        if (secs <= 0 || !controller.isPlaying) {
            controller.volume = 1f
            return
        }
        fadeInJob = viewModelScope.launch {
            val steps = secs * 12
            for (i in 1..steps) {
                controller.volume = (i.toFloat() / steps.toFloat()).coerceIn(0.08f, 1f)
                delay(83L)
            }
            controller.volume = 1f
        }
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
        val metaMap = _metaOverrides.value
        val lookup = _rawSongs.value.associate { s -> s.id to applyOverrides(s, coverMap, metaMap) }
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

    fun translateCurrentLyricsToChinese() {
        if (_isTranslatingLyrics.value) return
        val song = _playerState.value.currentSong ?: return
        val lines = _currentLyrics.value
        if (lines.isEmpty()) return
        val currentSettings = translationSettings.value
        viewModelScope.launch {
            _isTranslatingLyrics.value = true
            val engineTip = if (currentSettings.customApiKey.isNotBlank()) {
                "正在使用自定义接口翻译歌词..."
            } else {
                "正在使用公开免费接口翻译歌词..."
            }
            showToast(engineTip)
            val res = onlineMetadataRepository.translateLyricsToBilingualLrc(lines, currentSettings)
            res.onSuccess { bilingualLrc ->
                val saved = onlineMetadataRepository.saveCustomLyricsText(song.id, bilingualLrc)
                if (saved) {
                    lastLoadedLyricsSongId = null
                    refreshLyricsIfNeeded(song)
                    showToast("歌词翻译完成，已缓存双语字幕")
                }
            }.onFailure { err ->
                showToast(err.message ?: "歌词翻译失败，请检查网络或在设置中切换引擎")
            }
            _isTranslatingLyrics.value = false
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

    fun saveSongMetadata(songId: Long, title: String, artist: String, album: String, genre: String = "") {
        val cleanTitle = title.trim()
        val cleanArtist = artist.trim()
        val cleanAlbum = album.trim()
        if (cleanTitle.isEmpty()) return
        val override = SongMetaOverride(
            title = cleanTitle,
            artist = cleanArtist.ifBlank { "未知歌手" },
            album = cleanAlbum.ifBlank { cleanTitle },
            genre = genre.trim(),
        )
        viewModelScope.launch {
            onlineMetadataRepository.saveMetaOverride(songId, override)
            _metaOverrides.update { it + (songId to override) }
            syncPlayerSnapshot()
        }
    }

    fun clearSongMetadata(songId: Long) {
        viewModelScope.launch {
            onlineMetadataRepository.clearMetaOverride(songId)
            _metaOverrides.update { it - songId }
            syncPlayerSnapshot()
        }
    }

    fun onSongDeletedFromDevice(song: Song) {
        viewModelScope.launch {
            onlineMetadataRepository.clearCustomCover(song.id)
            onlineMetadataRepository.clearCustomLyrics(song.id)
            onlineMetadataRepository.clearMetaOverride(song.id)
            _customCovers.update { it - song.id }
            _metaOverrides.update { it - song.id }
            _rawSongs.update { list -> list.filterNot { it.id == song.id } }

            // Remove from active MediaController queue safely
            val controller = mediaController
            if (controller != null) {
                for (i in controller.mediaItemCount - 1 downTo 0) {
                    if (controller.getMediaItemAt(i).mediaId.toLongOrNull() == song.id) {
                        controller.removeMediaItem(i)
                    }
                }
            }
            activeQueueSongs = activeQueueSongs.filterNot { it.id == song.id }
            syncPlayerSnapshot()
        }
    }

    fun deleteSongLegacy(song: Song) {
        viewModelScope.launch {
            val deleted = runCatching {
                getApplication<Application>().contentResolver.delete(song.contentUri, null, null) > 0
            }.getOrDefault(false)
            if (deleted) {
                onSongDeletedFromDevice(song)
            }
        }
    }

    fun applyPlaylistCover(playlistId: String, sourceUri: Uri) {
        viewModelScope.launch {
            val saved = onlineMetadataRepository.savePlaylistCoverFromUri(playlistId, sourceUri)
            if (saved != null) {
                _playlistCovers.update { it + (playlistId to saved) }
            }
        }
    }

    fun clearPlaylistCover(playlistId: String) {
        viewModelScope.launch {
            onlineMetadataRepository.clearPlaylistCover(playlistId)
            _playlistCovers.update { it - playlistId }
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
        val metaMap = _metaOverrides.value
        val songLookup = _rawSongs.value.associate { s -> s.id to applyOverrides(s, coverMap, metaMap) }

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
        val isLosslessTrack = currentSong?.filePath?.substringAfterLast('.', "")
            ?.lowercase(Locale.US) in setOf("flac", "wav", "alac", "ape", "dsd", "aiff")
        PureMusicWidgetProvider.pushState(
            context = getApplication(),
            title = currentSong?.title,
            artist = currentSong?.artist,
            isPlaying = controller.isPlaying,
            artUri = currentSong?.albumArtUri,
            isLossless = isLosslessTrack,
        )
        refreshLyricsIfNeeded(currentSong)
        if (currentSong != null) {
            if (currentSong.id != lastSpecSongId) {
                lastSpecSongId = currentSong.id
                audioSpecJob?.cancel()
                audioSpecJob = viewModelScope.launch {
                    _currentAudioSpec.value = musicRepository.extractAudioTechSpec(currentSong)
                }
            }
            if (controller.isPlaying && currentSong.id != lastRecordedPlaySongId) {
                lastRecordedPlaySongId = currentSong.id
                viewModelScope.launch { playlistRepository.recordSongPlayed(currentSong.id) }
            }
        } else {
            lastSpecSongId = null
            _currentAudioSpec.value = AudioTechSpec()
        }
    }

    private fun startPositionTicker() {
        if (positionTickerJob?.isActive == true) return
        positionTickerJob = viewModelScope.launch {
            while (isActive) {
                val controller = mediaController
                if (controller != null && controller.isPlaying) {
                    val pos = controller.currentPosition.coerceAtLeast(0L)
                    val dur = controller.duration.takeIf { it > 0L } ?: _playerState.value.durationMs
                    val curId = _playerState.value.currentSong?.id
                    if (_singleRepeatRemaining.value > 0 &&
                        curId != null &&
                        curId == lastTickerSongId &&
                        dur > 4_000L &&
                        lastTickerPosMs >= dur - 2_500L &&
                        pos < 2_000L
                    ) {
                        consumeSingleRepeatPass()
                    }
                    lastTickerSongId = curId
                    lastTickerPosMs = pos
                    if (AudioSpectrumBus.isVisualizerActive) {
                        _playerState.update { it.copy(positionMs = pos, durationMs = dur) }
                    }
                    val fadeMs = fadeInSeconds.value * 1000L
                    if (fadeMs > 0L && dur > fadeMs * 2L && fadeInJob?.isActive != true) {
                        val remaining = (dur - pos).coerceAtLeast(0L)
                        if (remaining in 1L..fadeMs) {
                            controller.volume = (remaining.toFloat() / fadeMs.toFloat()).coerceIn(0.08f, 1f)
                        } else if (controller.volume < 0.99f) {
                            controller.volume = 1f
                        }
                    }
                }
                // Low-power 1500ms background cadence vs crisp 400ms foreground cadence
                delay(if (AudioSpectrumBus.isVisualizerActive) 400L else 1500L)
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

    fun addSongsToPlaylist(playlistId: String, songIds: Collection<Long>) {
        if (songIds.isEmpty()) return
        val existing = playlists.value.firstOrNull { it.id == playlistId } ?: return
        val merged = (existing.songIds + songIds).distinct()
        viewModelScope.launch {
            playlistRepository.setPlaylistSongs(playlistId, merged)
        }
    }

    fun addSongsToFavorites(songIds: Collection<Long>) {
        if (songIds.isEmpty()) return
        val merged = favoriteSongIds.value + songIds
        viewModelScope.launch {
            playlistRepository.setFavoriteSongIds(merged)
        }
    }

    fun exportBackupJson(): String {
        val songMap = allSongs.value.associateBy { it.id }
        val root = JSONObject()
        root.put("app", "PureMusic")
        root.put("version", "1.2")
        root.put("exportedAt", System.currentTimeMillis())

        val favArr = JSONArray()
        favoriteSongIds.value.forEach { id ->
            songMap[id]?.let { s ->
                favArr.put(JSONObject().apply {
                    put("filePath", s.filePath)
                    put("title", s.title)
                    put("artist", s.artist)
                })
            }
        }
        root.put("favorites", favArr)

        val plArr = JSONArray()
        playlists.value.forEach { pl ->
            val items = JSONArray()
            pl.songIds.forEach { id ->
                songMap[id]?.let { s ->
                    items.put(JSONObject().apply {
                        put("filePath", s.filePath)
                        put("title", s.title)
                        put("artist", s.artist)
                    })
                }
            }
            plArr.put(JSONObject().apply {
                put("name", pl.name)
                put("songs", items)
            })
        }
        root.put("playlists", plArr)
        return root.toString(2)
    }

    fun importBackupJson(jsonText: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val msg = runCatching {
                val root = JSONObject(jsonText)
                val library = allSongs.value
                val byPath = library.associateBy { it.filePath }
                val byKey = library.associateBy { "${it.title.lowercase(Locale.US)}|${it.artist.lowercase(Locale.US)}" }

                fun resolveObj(obj: JSONObject): Long? {
                    val path = obj.optString("filePath")
                    if (path.isNotBlank() && byPath.containsKey(path)) return byPath[path]?.id
                    val key = "${obj.optString("title").lowercase(Locale.US)}|${obj.optString("artist").lowercase(Locale.US)}"
                    return byKey[key]?.id
                }

                var restoredFavs = 0
                val favArr = root.optJSONArray("favorites")
                if (favArr != null) {
                    val favIds = mutableSetOf<Long>()
                    favIds.addAll(favoriteSongIds.value)
                    for (i in 0 until favArr.length()) {
                        val id = resolveObj(favArr.getJSONObject(i))
                        if (id != null && favIds.add(id)) restoredFavs++
                    }
                    playlistRepository.setFavoriteSongIds(favIds)
                }

                var restoredPlaylists = 0
                val plArr = root.optJSONArray("playlists")
                if (plArr != null) {
                    val existingByName = playlists.value.associateBy { it.name }
                    for (i in 0 until plArr.length()) {
                        val plObj = plArr.getJSONObject(i)
                        val name = plObj.optString("name").trim()
                        if (name.isBlank()) continue
                        val songsArr = plObj.optJSONArray("songs") ?: JSONArray()
                        val ids = mutableListOf<Long>()
                        for (j in 0 until songsArr.length()) {
                            resolveObj(songsArr.getJSONObject(j))?.let { ids.add(it) }
                        }
                        val existing = existingByName[name]
                        if (existing != null) {
                            playlistRepository.setPlaylistSongs(existing.id, (existing.songIds + ids).distinct())
                        } else {
                            playlistRepository.createPlaylist(name, ids.distinct())
                        }
                        restoredPlaylists++
                    }
                }
                "已恢复 $restoredPlaylists 个歌单与 $restoredFavs 首新增喜欢"
            }.getOrElse { "备份文件格式无效，无法恢复" }
            onResult(msg)
        }
    }

    fun importCustomFont(uri: Uri, displayName: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val app = getApplication<Application>()
            val ok = runCatching {
                val dir = File(app.filesDir, "custom_fonts").apply { if (!exists()) mkdirs() }
                val target = File(dir, "user_font.ttf")
                app.contentResolver.openInputStream(uri)?.use { input ->
                    target.outputStream().use { out -> input.copyTo(out) }
                } ?: return@runCatching false
                // Validate Typeface before activating
                android.graphics.Typeface.createFromFile(target)
                File(dir, "font_name.txt").writeText(displayName)
                true
            }.getOrDefault(false)
            if (ok) {
                _customFontName.value = displayName
                _customFontVersion.value += 1
                playlistRepository.setFontStyle(AppFontStyle.CUSTOM)
                onResult("已导入并应用字体：$displayName")
            } else {
                onResult("字体文件无效，请选择 .ttf 或 .otf 字体")
            }
        }
    }

    fun clearCustomFont() {
        viewModelScope.launch {
            val dir = File(getApplication<Application>().filesDir, "custom_fonts")
            runCatching { File(dir, "user_font.ttf").delete() }
            runCatching { File(dir, "font_name.txt").delete() }
            _customFontName.value = null
            _customFontVersion.value += 1
            if (fontStyle.value == AppFontStyle.CUSTOM) {
                playlistRepository.setFontStyle(AppFontStyle.SANS)
            }
            if (lyricFontStyle.value == LyricFontStyle.CUSTOM) {
                playlistRepository.setLyricFontStyle(LyricFontStyle.SANS)
            }
        }
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
