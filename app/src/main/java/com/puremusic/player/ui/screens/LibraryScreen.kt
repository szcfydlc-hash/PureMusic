package com.puremusic.player.ui.screens

import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.foundation.layout.offset
import com.puremusic.player.ui.theme.LocalLayoutDensity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.material.icons.rounded.ViewModule
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MusicOff
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.puremusic.player.data.model.Album
import com.puremusic.player.data.model.Artist
import com.puremusic.player.data.model.LibrarySubTab
import com.puremusic.player.data.model.MainTab
import com.puremusic.player.data.model.MusicFolder
import com.puremusic.player.data.model.PlayerTheme
import com.puremusic.player.data.model.PlayerUiState
import com.puremusic.player.data.model.Playlist
import com.puremusic.player.data.model.Song
import com.puremusic.player.data.model.SortOrder
import com.puremusic.player.ui.components.AlbumArtwork
import com.puremusic.player.ui.components.AlphabetFastScroller
import com.puremusic.player.ui.components.SongRowItem
import com.puremusic.player.ui.components.formatCountdown
import com.puremusic.player.ui.components.liquidGlass
import kotlinx.coroutines.launch

sealed interface CollectionDetail {
    val title: String
    val subtitle: String

    data class AlbumDetail(val album: Album) : CollectionDetail {
        override val title: String = album.name
        override val subtitle: String = "${album.artist} · ${album.songs.size} 首"
    }

    data class ArtistDetail(val artist: Artist) : CollectionDetail {
        override val title: String = artist.name
        override val subtitle: String = "${artist.albumCount} 张专辑 · ${artist.songs.size} 首"
    }

    data class FolderDetail(val folder: MusicFolder) : CollectionDetail {
        override val title: String = folder.name
        override val subtitle: String = folder.path
    }

    data class PlaylistDetail(val playlistId: String, override val title: String) : CollectionDetail {
        override val subtitle: String = "自定义歌单"
    }

    data object FavoritesDetail : CollectionDetail {
        override val title: String = "我喜欢的音乐"
        override val subtitle: String = "收藏列表"
    }

    data object RecentDetail : CollectionDetail {
        override val title: String = "最近播放"
        override val subtitle: String = "播放历史"
    }

    data object MostPlayedDetail : CollectionDetail {
        override val title: String = "听歌报告"
        override val subtitle: String = "本地聆听档案"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    activeMainTab: MainTab,
    onSelectMainTab: (MainTab) -> Unit,
    songs: List<Song>,
    allSongs: List<Song>,
    albums: List<Album>,
    artists: List<Artist>,
    folders: List<MusicFolder>,
    playlists: List<Playlist>,
    playlistCovers: Map<String, android.net.Uri>,
    recentSongs: List<Song>,
    mostPlayedSongs: List<Song>,
    playCounts: Map<Long, Int> = emptyMap(),
    hourlyPlays: Map<Int, Int> = emptyMap(),
    favoriteSongIds: Set<Long>,
    isLoading: Boolean,
    searchQuery: String,
    sortOrder: SortOrder,
    playerState: PlayerUiState,
    playerTheme: PlayerTheme,
    sleepTimerRemainingMs: Long?,
    liquidGlassEnabled: Boolean,
    isTabletLayout: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSelectSortOrder: (SortOrder) -> Unit,
    onRefreshLibrary: () -> Unit,
    onPlaySongs: (List<Song>, Int, Boolean) -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onOpenNowPlaying: () -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onRequestAddToPlaylist: (Song) -> Unit,
    onShowSongInfo: (Song) -> Unit,
    onReplaceCover: (Song) -> Unit,
    onMatchLyrics: (Song) -> Unit,
    onDeleteSong: (Song) -> Unit,
    onBatchAddToPlaylist: (List<Song>) -> Unit = {},
    onBatchFavorite: (List<Song>) -> Unit = {},
    onBatchDeleteSongs: (List<Song>) -> Unit = {},
    onRequestCreatePlaylist: () -> Unit,
    onRequestDeletePlaylist: (Playlist) -> Unit,
    onRequestRenamePlaylist: (Playlist) -> Unit,
    onRequestManagePlaylistSongs: (Playlist) -> Unit,
    onRequestManageFavorites: () -> Unit,
    onRequestReplacePlaylistCover: (Playlist) -> Unit,
    onClearPlaylistCover: (Playlist) -> Unit,
    onRemoveSongFromPlaylist: (String, Long) -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenThemeSettings: () -> Unit,
    resolveSongsByIds: (List<Long>) -> List<Song>,
    bottomContentPadding: PaddingValues = PaddingValues(0.dp),
) {
    var selectedSubTab by remember { mutableStateOf(LibrarySubTab.SONGS) }
    var activeDetail by remember { mutableStateOf<CollectionDetail?>(null) }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedSongIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var searchExpanded by remember { mutableStateOf(false) }
    var usePosterGrid by remember { mutableStateOf(false) }
    var albumCompactThreeCol by remember { mutableStateOf(true) }

    BackHandler(enabled = activeDetail != null || searchExpanded || searchQuery.isNotEmpty()) {
        when {
            activeDetail != null -> activeDetail = null
            searchQuery.isNotEmpty() -> onSearchQueryChange("")
            searchExpanded -> searchExpanded = false
        }
    }

    val detail = activeDetail
    if (detail is CollectionDetail.MostPlayedDetail) {
        ListeningReportView(
            allSongs = allSongs,
            mostPlayedSongs = mostPlayedSongs,
            artists = artists,
            playCounts = playCounts,
            hourlyPlays = hourlyPlays,
            playerState = playerState,
            liquidGlassEnabled = liquidGlassEnabled,
            onBack = { activeDetail = null },
            onPlaySongs = onPlaySongs,
            onSelectArtist = { artist -> activeDetail = CollectionDetail.ArtistDetail(artist) },
            bottomPadding = bottomContentPadding,
        )
        return
    }
    if (detail != null) {
        val detailSongs = when (detail) {
            is CollectionDetail.AlbumDetail -> detail.album.songs
            is CollectionDetail.ArtistDetail -> detail.artist.songs
            is CollectionDetail.FolderDetail -> detail.folder.songs
            is CollectionDetail.PlaylistDetail -> {
                val pl = playlists.firstOrNull { it.id == detail.playlistId }
                if (pl != null) resolveSongsByIds(pl.songIds) else emptyList()
            }
            CollectionDetail.FavoritesDetail -> allSongs.filter { it.id in favoriteSongIds }
            CollectionDetail.RecentDetail -> recentSongs
            CollectionDetail.MostPlayedDetail -> mostPlayedSongs
        }

        val livePlaylist = if (detail is CollectionDetail.PlaylistDetail) {
            playlists.firstOrNull { it.id == detail.playlistId }
        } else {
            null
        }
        val liveTitle = livePlaylist?.name ?: detail.title
        val liveSubtitle = when (detail) {
            is CollectionDetail.PlaylistDetail -> "自定义歌单 · ${detailSongs.size} 首"
            CollectionDetail.FavoritesDetail -> "收藏列表 · ${detailSongs.size} 首"
            CollectionDetail.RecentDetail -> "最近播放 · ${detailSongs.size} 首"
            CollectionDetail.MostPlayedDetail -> "常听排行 · ${detailSongs.size} 首"
            else -> detail.subtitle
        }

        CollectionDetailView(
            title = liveTitle,
            subtitle = liveSubtitle,
            songs = detailSongs,
            favoriteSongIds = favoriteSongIds,
            playerState = playerState,
            liquidGlassEnabled = liquidGlassEnabled,
            onBack = { activeDetail = null },
            onPlaySongs = onPlaySongs,
            onToggleFavorite = onToggleFavorite,
            onRequestAddToPlaylist = onRequestAddToPlaylist,
            onShowSongInfo = onShowSongInfo,
            onReplaceCover = onReplaceCover,
            onMatchLyrics = onMatchLyrics,
            onDeleteSong = onDeleteSong,
            onManageSongs = when (detail) {
                is CollectionDetail.PlaylistDetail -> livePlaylist?.let { pl -> { onRequestManagePlaylistSongs(pl) } }
                CollectionDetail.FavoritesDetail -> onRequestManageFavorites
                else -> null
            },
            onRenamePlaylist = livePlaylist?.let { pl -> { onRequestRenamePlaylist(pl) } },
            onRemoveFromPlaylist = if (detail is CollectionDetail.PlaylistDetail) {
                { songId -> onRemoveSongFromPlaylist(detail.playlistId, songId) }
            } else {
                null
            },
            bottomContentPadding = bottomContentPadding,
        )
        return
    }

    val outlineColor = MaterialTheme.colorScheme.outline
    val actionCorner = if (liquidGlassEnabled) 14.dp else 7.dp

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = innerPadding.calculateBottomPadding()),
        ) {
            if (isTabletLayout) {
                TabletLeftSidebar(
                    activeMainTab = activeMainTab,
                    onSelectMainTab = onSelectMainTab,
                    playerState = playerState,
                    songCount = allSongs.size,
                    albumCount = albums.size,
                    liquidGlassEnabled = liquidGlassEnabled,
                    onOpenNowPlaying = onOpenNowPlaying,
                    onTogglePlayPause = onTogglePlayPause,
                    onSkipPrevious = onSkipPrevious,
                    onSkipNext = onSkipNext,
                    onOpenEqualizer = onOpenEqualizer,
                    onOpenSleepTimer = onOpenSleepTimer,
                    onOpenSettings = onOpenThemeSettings,
                )
                VerticalDivider(thickness = 0.75.dp, color = outlineColor)
            }

            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                // 1. Classic Editorial Book Header
                val headerTitle = when (activeMainTab) {
                    MainTab.LIBRARY -> "曲库"
                    MainTab.ALBUMS -> "专辑"
                    MainTab.PLAYLISTS -> "歌单"
                    MainTab.SETTINGS -> "设置"
                }
                val headerSubtitle = when (activeMainTab) {
                    MainTab.LIBRARY -> "${allSongs.size} 首本地音乐 · ${albums.size} 张专辑"
                    MainTab.ALBUMS -> "${albums.size} 张精选专辑"
                    MainTab.PLAYLISTS -> "${playlists.size + 1} 个播放列表"
                    MainTab.SETTINGS -> ""
                }

                val screenCfg = LocalConfiguration.current
                val isCompactLandscape = screenCfg.screenWidthDp > screenCfg.screenHeightDp && screenCfg.screenHeightDp < 500
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 20.dp,
                            end = 16.dp,
                            top = if (isCompactLandscape) 6.dp else 14.dp,
                            bottom = if (isCompactLandscape) 2.dp else 6.dp,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = headerTitle,
                            style = (if (isCompactLandscape) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium).copy(
                                fontWeight = FontWeight.Bold,
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        if (!isCompactLandscape && headerSubtitle.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = headerSubtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (sleepTimerRemainingMs != null) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(actionCorner))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(0.75.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(actionCorner))
                                    .clickable(onClick = onOpenSleepTimer)
                                    .padding(horizontal = 9.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Timer,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = formatCountdown(sleepTimerRemainingMs),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }

                        HeaderToolButton(
                            onClick = {
                                if (searchExpanded && searchQuery.isNotEmpty()) {
                                    onSearchQueryChange("")
                                }
                                searchExpanded = !searchExpanded
                            },
                            active = searchExpanded || searchQuery.isNotEmpty(),
                            liquidGlassEnabled = liquidGlassEnabled,
                        ) {
                            Icon(
                                imageVector = if (searchExpanded) Icons.Rounded.Close else Icons.Rounded.Search,
                                contentDescription = "搜索",
                                modifier = Modifier.size(19.dp),
                            )
                        }



                        if (activeMainTab == MainTab.ALBUMS && !isTabletLayout) {
                            HeaderToolButton(
                                onClick = { albumCompactThreeCol = !albumCompactThreeCol },
                                active = albumCompactThreeCol,
                                liquidGlassEnabled = liquidGlassEnabled,
                            ) {
                                Icon(
                                    imageVector = if (albumCompactThreeCol) Icons.Rounded.GridView else Icons.Rounded.ViewModule,
                                    contentDescription = "切换 2/3 列",
                                    modifier = Modifier.size(19.dp),
                                )
                            }
                        }

                        if (activeMainTab == MainTab.PLAYLISTS) {
                            HeaderToolButton(
                                onClick = onRequestCreatePlaylist,
                                active = true,
                                liquidGlassEnabled = liquidGlassEnabled,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = "新建歌单",
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                }

                // 2. Expandable Search Input Box
                AnimatedVisibility(
                    visible = searchExpanded || searchQuery.isNotEmpty(),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 6.dp)
                            .height(40.dp)
                            .clip(RoundedCornerShape(actionCorner))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(0.75.dp, outlineColor, RoundedCornerShape(actionCorner))
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "检索歌曲、专辑、歌手",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = onSearchQueryChange,
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { onSearchQueryChange("") },
                                modifier = Modifier.size(28.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "清空搜索",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }
                }

                // 3. Book-Index Underline Sub-Tabs on Library tab (歌曲 / 歌手 / 文件夹)
                if (activeMainTab == MainTab.LIBRARY) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 4.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            LibrarySubTab.entries.forEach { subTab ->
                                val selected = subTab == selectedSubTab
                                Column(
                                    modifier = Modifier
                                        .clickable { selectedSubTab = subTab }
                                        .padding(top = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(
                                        text = subTab.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (selected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(34.dp)
                                            .height(2.dp)
                                            .background(
                                                if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            ),
                                    )
                                }
                            }
                        }
                        HorizontalDivider(thickness = 0.75.dp, color = outlineColor)
                    }
                } else {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp),
                        thickness = 0.75.dp,
                        color = outlineColor,
                    )
                }

                // 4. Ultra-Clean Compact Single-Line Toolbar (or Batch Multi-Select Header)
                if (activeMainTab == MainTab.LIBRARY && selectedSubTab == LibrarySubTab.SONGS && !isLoading && songs.isNotEmpty()) {
                    if (isSelectionMode) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, end = 16.dp, top = 6.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                IconButton(
                                    onClick = {
                                        isSelectionMode = false
                                        selectedSongIds = emptySet()
                                    },
                                    modifier = Modifier.size(36.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "退出多选",
                                        tint = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                                Text(
                                    text = "已选 ${selectedSongIds.size} 首",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                            }
                            Text(
                                text = if (selectedSongIds.size == songs.size) "取消全选" else "全选",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        selectedSongIds = if (selectedSongIds.size == songs.size) {
                                            emptySet()
                                        } else {
                                            songs.map { it.id }.toSet()
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                            )
                        }
                    } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 18.dp, end = 12.dp, top = 6.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onPlaySongs(songs, 0, true) }
                                .padding(horizontal = 4.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(LocalLayoutDensity.current.gridSpacingDp.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Shuffle,
                                contentDescription = "随机播放全部",
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(19.dp),
                            )
                            Text(
                                text = "${songs.size}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            IconButton(
                                onClick = {
                                    isSelectionMode = true
                                    selectedSongIds = emptySet()
                                },
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Checklist,
                                    contentDescription = "批量多选",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            Box {
                                IconButton(
                                    onClick = { sortMenuExpanded = true },
                                    modifier = Modifier.size(36.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.Sort,
                                        contentDescription = "排序",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                                DropdownMenu(
                                    expanded = sortMenuExpanded,
                                    onDismissRequest = { sortMenuExpanded = false },
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
                                    tonalElevation = 0.dp,
                                    shadowElevation = 10.dp,
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
                                ) {
                                    SortOrder.entries.forEach { order ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = if (order == sortOrder) "✓ ${order.label}" else order.label,
                                                    fontWeight = if (order == sortOrder) FontWeight.SemiBold else FontWeight.Normal,
                                                )
                                            },
                                            onClick = {
                                                sortMenuExpanded = false
                                                onSelectSortOrder(order)
                                            },
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = { usePosterGrid = !usePosterGrid },
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(
                                    imageVector = if (usePosterGrid) {
                                        Icons.AutoMirrored.Rounded.ViewList
                                    } else {
                                        Icons.Rounded.GridView
                                    },
                                    contentDescription = "切换视图",
                                    tint = if (usePosterGrid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                    }
                }

                // 5. Main Tab Content Area
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    val songGridColumns = if (isTabletLayout) 4 else 3
                    val albumGridColumns = if (isTabletLayout) 4 else if (albumCompactThreeCol) 3 else 2

                    AnimatedContent(
                        targetState = Pair(activeMainTab, selectedSubTab),
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "libraryContentSwitch",
                        modifier = Modifier.weight(1f),
                    ) { (mainTab, subTab) ->
                        when (mainTab) {
                            MainTab.LIBRARY -> when (subTab) {
                                LibrarySubTab.SONGS -> SongsTabContent(
                                    songs = songs,
                                    playerTheme = playerTheme,
                                    usePosterGrid = usePosterGrid,
                                    gridColumns = songGridColumns,
                                    favoriteSongIds = favoriteSongIds,
                                    playerState = playerState,
                                    onPlaySongs = onPlaySongs,
                                    onToggleFavorite = onToggleFavorite,
                                    onRequestAddToPlaylist = onRequestAddToPlaylist,
                                    onShowSongInfo = onShowSongInfo,
            onReplaceCover = onReplaceCover,
            onMatchLyrics = onMatchLyrics,
            onDeleteSong = onDeleteSong,
                                    onRefresh = onRefreshLibrary,
                                    isSelectionMode = isSelectionMode,
                                    selectedSongIds = selectedSongIds,
                                    onToggleSelectSong = { songId ->
                                        selectedSongIds = if (songId in selectedSongIds) {
                                            selectedSongIds - songId
                                        } else {
                                            selectedSongIds + songId
                                        }
                                    },
                                    onEnterSelectionWithSong = { songId ->
                                        isSelectionMode = true
                                        selectedSongIds = selectedSongIds + songId
                                    },
                                    onBatchAddToPlaylist = {
                                        val chosen = songs.filter { it.id in selectedSongIds }
                                        if (chosen.isNotEmpty()) {
                                            onBatchAddToPlaylist(chosen)
                                            isSelectionMode = false
                                            selectedSongIds = emptySet()
                                        }
                                    },
                                    onBatchFavorite = {
                                        val chosen = songs.filter { it.id in selectedSongIds }
                                        if (chosen.isNotEmpty()) {
                                            onBatchFavorite(chosen)
                                            isSelectionMode = false
                                            selectedSongIds = emptySet()
                                        }
                                    },
                                    onBatchDelete = {
                                        val chosen = songs.filter { it.id in selectedSongIds }
                                        if (chosen.isNotEmpty()) {
                                            onBatchDeleteSongs(chosen)
                                            isSelectionMode = false
                                            selectedSongIds = emptySet()
                                        }
                                    },
                                    bottomPadding = bottomContentPadding,
                                )

                                LibrarySubTab.ARTISTS -> ArtistsTabContent(
                                    artists = artists,
                                    onSelectArtist = { activeDetail = CollectionDetail.ArtistDetail(it) },
                                    bottomPadding = bottomContentPadding,
                                )

                                LibrarySubTab.FOLDERS -> FoldersTabContent(
                                    folders = folders,
                                    onSelectFolder = { activeDetail = CollectionDetail.FolderDetail(it) },
                                    bottomPadding = bottomContentPadding,
                                )
                            }

                            MainTab.ALBUMS -> AlbumsTabContent(
                                albums = albums,
                                gridColumns = albumGridColumns,
                                liquidGlassEnabled = liquidGlassEnabled,
                                onSelectAlbum = { activeDetail = CollectionDetail.AlbumDetail(it) },
                                bottomPadding = bottomContentPadding,
                            )

                            MainTab.PLAYLISTS -> PlaylistsTabContent(
                                playlists = playlists,
                                playlistCovers = playlistCovers,
                                resolveSongsByIds = resolveSongsByIds,
                                favoriteSongs = remember(allSongs, favoriteSongIds) { allSongs.filter { it.id in favoriteSongIds } },
                                recentSongs = recentSongs,
                                allSongs = allSongs,
                                playCounts = playCounts,
                                hourlyPlays = hourlyPlays,
                                liquidGlassEnabled = liquidGlassEnabled,
                                onOpenFavorites = { activeDetail = CollectionDetail.FavoritesDetail },
                                onOpenRecent = { activeDetail = CollectionDetail.RecentDetail },
                                onOpenListeningReport = { activeDetail = CollectionDetail.MostPlayedDetail },
                                onPlayFavorites = {
                                    val favSongs = allSongs.filter { it.id in favoriteSongIds }
                                    if (favSongs.isNotEmpty()) onPlaySongs(favSongs, 0, false)
                                },
                                onRequestCreatePlaylist = onRequestCreatePlaylist,
                                onOpenPlaylist = {
                                    activeDetail = CollectionDetail.PlaylistDetail(it.id, it.name)
                                },
                                onRequestDeletePlaylist = onRequestDeletePlaylist,
                                onRequestRenamePlaylist = onRequestRenamePlaylist,
                                onRequestManagePlaylistSongs = onRequestManagePlaylistSongs,
                                onRequestReplacePlaylistCover = onRequestReplacePlaylistCover,
                                onClearPlaylistCover = onClearPlaylistCover,
                                bottomPadding = bottomContentPadding,
                            )

                            MainTab.SETTINGS -> Unit
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderToolButton(
    onClick: () -> Unit,
    active: Boolean,
    liquidGlassEnabled: Boolean,
    content: @Composable () -> Unit,
) {
    val shape = if (liquidGlassEnabled) CircleShape else RoundedCornerShape(6.dp)
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(shape)
            .background(
                when {
                    active -> MaterialTheme.colorScheme.primaryContainer
                    liquidGlassEnabled -> MaterialTheme.colorScheme.surfaceVariant
                    else -> Color.Transparent
                },
            )
            .then(
                if (!liquidGlassEnabled && active) {
                    Modifier.border(0.75.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f), shape)
                } else {
                    Modifier
                },
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides if (active) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        ) {
            content()
        }
    }
}

@Composable
private fun TabletLeftSidebar(
    activeMainTab: MainTab,
    onSelectMainTab: (MainTab) -> Unit,
    playerState: PlayerUiState,
    songCount: Int,
    albumCount: Int,
    liquidGlassEnabled: Boolean,
    onOpenNowPlaying: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val cfg = LocalConfiguration.current
    val isCompactHeight = cfg.screenHeightDp < 520
    val navItems = listOf(
        MainTab.LIBRARY to Icons.Rounded.LibraryMusic,
        MainTab.ALBUMS to Icons.Rounded.Album,
        MainTab.PLAYLISTS to Icons.Rounded.QueueMusic,
        MainTab.SETTINGS to Icons.Rounded.Settings,
    )

    Column(
        modifier = Modifier
            .width(if (isCompactHeight) 184.dp else 260.dp)
            .fillMaxHeight()
            .padding(
                horizontal = if (isCompactHeight) 10.dp else 16.dp,
                vertical = if (isCompactHeight) 8.dp else 14.dp,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(if (isCompactHeight) 3.dp else 6.dp),
        ) {
            if (!isCompactHeight) {
                Text(
                    text = "简约音乐",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                )
                HorizontalDivider(thickness = 0.75.dp, color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(4.dp))
            }

            navItems.forEach { (tab, icon) ->
                val selected = tab == activeMainTab
                val itemShape = RoundedCornerShape(if (liquidGlassEnabled) 12.dp else 8.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(itemShape)
                        .background(
                            if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
                        )
                        .then(
                            if (selected) Modifier.border(0.75.dp, MaterialTheme.colorScheme.outline, itemShape) else Modifier,
                        )
                        .clickable {
                            if (tab == MainTab.SETTINGS) onOpenSettings() else onSelectMainTab(tab)
                        }
                        .padding(
                            horizontal = if (isCompactHeight) 10.dp else 12.dp,
                            vertical = if (isCompactHeight) 7.dp else 10.dp,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(if (isCompactHeight) 17.dp else 19.dp),
                    )
                    Spacer(modifier = Modifier.width(if (isCompactHeight) 8.dp else 10.dp))
                    Text(
                        text = tab.label,
                        style = if (isCompactHeight) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                    )
                }
            }
        }

        val song = playerState.currentSong
        val cardShape = RoundedCornerShape(if (liquidGlassEnabled) 16.dp else 10.dp)
        if (isCompactHeight) {
            // Compact horizontal Now-Playing pill for phone landscape sidebar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(0.75.dp, MaterialTheme.colorScheme.outline, cardShape)
                    .clickable(enabled = song != null, onClick = onOpenNowPlaying)
                    .padding(horizontal = 8.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AlbumArtwork(
                    uri = song?.albumArtUri,
                    contentDescription = song?.title,
                    modifier = Modifier.size(36.dp),
                    cornerRadius = 18.dp,
                    iconSize = 16.dp,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song?.title ?: "暂未播放",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = song?.artist ?: "点击曲目播放",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (song != null) {
                    IconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier.size(30.dp),
                    ) {
                        Icon(
                            imageVector = if (playerState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = "播放/暂停",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (liquidGlassEnabled) {
                                Modifier.liquidGlass(enabled = true, shape = cardShape, elevation = 6.dp)
                            } else {
                                Modifier
                                    .clip(cardShape)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(0.75.dp, MaterialTheme.colorScheme.outline, cardShape)
                            },
                        )
                        .clickable(enabled = song != null, onClick = onOpenNowPlaying)
                        .padding(14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SpinningVinylDisc(
                            albumArtUri = song?.albumArtUri,
                            rotationProvider = { 0f },
                            discSize = 126.dp,
                        )
                        Text(
                            text = song?.title ?: "暂未播放",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = song?.artist ?: "$songCount 首 · $albumCount 张专辑",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(onClick = onSkipPrevious, modifier = Modifier.size(34.dp)) {
                                Icon(Icons.Rounded.SkipPrevious, contentDescription = "上一首", modifier = Modifier.size(20.dp))
                            }
                            FilledIconButton(
                                onClick = onTogglePlayPause,
                                modifier = Modifier.size(40.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.onBackground,
                                    contentColor = MaterialTheme.colorScheme.background,
                                ),
                            ) {
                                Icon(
                                    imageVector = if (playerState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = "播放/暂停",
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                            IconButton(onClick = onSkipNext, modifier = Modifier.size(34.dp)) {
                                Icon(Icons.Rounded.SkipNext, contentDescription = "下一首", modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SongsTabContent(
    songs: List<Song>,
    playerTheme: PlayerTheme,
    usePosterGrid: Boolean,
    gridColumns: Int,
    favoriteSongIds: Set<Long>,
    playerState: PlayerUiState,
    onPlaySongs: (List<Song>, Int, Boolean) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onRequestAddToPlaylist: (Song) -> Unit,
    onShowSongInfo: (Song) -> Unit,
    onReplaceCover: (Song) -> Unit,
    onMatchLyrics: (Song) -> Unit,
    onDeleteSong: (Song) -> Unit,
    onRefresh: () -> Unit,
    isSelectionMode: Boolean = false,
    selectedSongIds: Set<Long> = emptySet(),
    onToggleSelectSong: (Long) -> Unit = {},
    onEnterSelectionWithSong: (Long) -> Unit = {},
    onBatchAddToPlaylist: () -> Unit = {},
    onBatchFavorite: () -> Unit = {},
    onBatchDelete: () -> Unit = {},
    bottomPadding: PaddingValues,
) {
    if (songs.isEmpty()) {
        EmptyLibraryPlaceholder(onRefresh = onRefresh)
        return
    }

    if (usePosterGrid) {
        val gridState = rememberLazyGridState()
        val scope = rememberCoroutineScope()
        Box(modifier = Modifier.fillMaxSize()) {
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(gridColumns),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 26.dp,
                    top = 8.dp,
                    bottom = 24.dp + bottomPadding.calculateBottomPadding(),
                ),
                horizontalArrangement = Arrangement.spacedBy(LocalLayoutDensity.current.gridSpacingDp.dp),
                verticalArrangement = Arrangement.spacedBy((LocalLayoutDensity.current.gridSpacingDp + 4).dp),
            ) {
                itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
                    val isCurrent = playerState.currentSong?.id == song.id
                    var itemMenuExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { onPlaySongs(songs, index, false) },
                                onLongClick = { itemMenuExpanded = true },
                            ),
                    ) {
                        VinylSleeveCover(
                            albumArtUri = song.albumArtUri,
                            contentDescription = song.album,
                            isCurrent = isCurrent,
                            accentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(7.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isCurrent) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                            }
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(
                            thickness = 0.6.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f),
                        )
                    }
                    DropdownMenu(
                        expanded = itemMenuExpanded,
                        onDismissRequest = { itemMenuExpanded = false },
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
                        tonalElevation = 0.dp,
                        shadowElevation = 10.dp,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (song.id in favoriteSongIds) "取消喜欢" else "添加到我喜欢") },
                            onClick = { itemMenuExpanded = false; onToggleFavorite(song.id) },
                        )
                        DropdownMenuItem(
                            text = { Text("添加到歌单") },
                            onClick = { itemMenuExpanded = false; onRequestAddToPlaylist(song) },
                        )
                        DropdownMenuItem(
                            text = { Text("更换封面") },
                            onClick = { itemMenuExpanded = false; onReplaceCover(song) },
                        )
                        DropdownMenuItem(
                            text = { Text("匹配 / 导入歌词") },
                            onClick = { itemMenuExpanded = false; onMatchLyrics(song) },
                        )
                        DropdownMenuItem(
                            text = { Text("歌曲信息与编辑") },
                            onClick = { itemMenuExpanded = false; onShowSongInfo(song) },
                        )
                        DropdownMenuItem(
                            text = { Text("删除本地文件", color = MaterialTheme.colorScheme.primary) },
                            onClick = { itemMenuExpanded = false; onDeleteSong(song) },
                        )
                    }
                    }
                }
            }
            if (songs.size >= 20) {
                AlphabetFastScroller(
                    onSelectLetter = { letter ->
                        val targetIdx = if (letter == "#") {
                            songs.indexOfFirst { s -> s.title.firstOrNull()?.uppercaseChar()?.isLetter() != true }
                        } else {
                            songs.indexOfFirst { s -> s.title.trimStart().uppercase().startsWith(letter) }
                        }
                        if (targetIdx >= 0) {
                            scope.launch { gridState.scrollToItem(targetIdx) }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 2.dp, bottom = bottomPadding.calculateBottomPadding() + 56.dp),
                )
            }
        }
    } else {
        val listState = rememberLazyListState()
        val wideGridState = rememberLazyGridState()
        val scope = rememberCoroutineScope()
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useTwoColumnList = maxWidth >= 600.dp
            if (useTwoColumnList) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = wideGridState,
                    contentPadding = PaddingValues(
                        end = 22.dp,
                        bottom = bottomPadding.calculateBottomPadding(),
                    ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
                        SongRowItem(
                            song = song,
                            isCurrent = playerState.currentSong?.id == song.id,
                            isPlaying = playerState.isPlaying,
                            isFavorite = song.id in favoriteSongIds,
                            onClick = {
                                if (isSelectionMode) onToggleSelectSong(song.id) else onPlaySongs(songs, index, false)
                            },
                            onAddToPlaylist = { onRequestAddToPlaylist(song) },
                            onToggleFavorite = { onToggleFavorite(song.id) },
                            onShowSongInfo = { onShowSongInfo(song) },
                            onReplaceCover = { onReplaceCover(song) },
                            onMatchLyrics = { onMatchLyrics(song) },
                            onDeleteSong = { onDeleteSong(song) },
                            isSelectionMode = isSelectionMode,
                            isSelected = song.id in selectedSongIds,
                            onLongClick = { onEnterSelectionWithSong(song.id) },
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(
                        end = 22.dp,
                        bottom = bottomPadding.calculateBottomPadding(),
                    ),
                ) {
                    itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
                        SongRowItem(
                            song = song,
                            isCurrent = playerState.currentSong?.id == song.id,
                            isPlaying = playerState.isPlaying,
                            isFavorite = song.id in favoriteSongIds,
                            onClick = {
                                if (isSelectionMode) onToggleSelectSong(song.id) else onPlaySongs(songs, index, false)
                            },
                            onAddToPlaylist = { onRequestAddToPlaylist(song) },
                            onToggleFavorite = { onToggleFavorite(song.id) },
                            onShowSongInfo = { onShowSongInfo(song) },
                            onReplaceCover = { onReplaceCover(song) },
                            onMatchLyrics = { onMatchLyrics(song) },
                            onDeleteSong = { onDeleteSong(song) },
                            isSelectionMode = isSelectionMode,
                            isSelected = song.id in selectedSongIds,
                            onLongClick = { onEnterSelectionWithSong(song.id) },
                        )
                    }
                }
            }
            val currentSongIdx = remember(songs, playerState.currentSong?.id) {
                val cid = playerState.currentSong?.id
                if (cid == null) -1 else songs.indexOfFirst { it.id == cid }
            }
            if (isSelectionMode && selectedSongIds.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = bottomPadding.calculateBottomPadding() + 14.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(0.85.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(26.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onBatchAddToPlaylist)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlaylistAdd,
                            contentDescription = "批量加歌单",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(21.dp),
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "加歌单",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onBatchFavorite)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FavoriteBorder,
                            contentDescription = "批量加入喜欢",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(21.dp),
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "我喜欢",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onBatchDelete)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "批量删除",
                            tint = Color(0xFFB83B2A),
                            modifier = Modifier.size(21.dp),
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "删除",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFB83B2A),
                        )
                    }
                }
            }
            if (currentSongIdx >= 0 && !isSelectionMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 32.dp, bottom = bottomPadding.calculateBottomPadding() + 14.dp)
                        .size(40.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(0.75.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .clickable {
                            scope.launch {
                                if (useTwoColumnList) {
                                    wideGridState.animateScrollToItem(currentSongIdx)
                                } else {
                                    listState.animateScrollToItem(currentSongIdx)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MyLocation,
                        contentDescription = "定位当前播放歌曲",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(19.dp),
                    )
                }
            }

            if (songs.size >= 20) {
                AlphabetFastScroller(
                    onSelectLetter = { letter ->
                        val targetIdx = if (letter == "#") {
                            songs.indexOfFirst { s -> s.title.firstOrNull()?.uppercaseChar()?.isLetter() != true }
                        } else {
                            songs.indexOfFirst { s -> s.title.trimStart().uppercase().startsWith(letter) }
                        }
                        if (targetIdx >= 0) {
                            scope.launch { listState.scrollToItem(targetIdx) }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 2.dp, bottom = bottomPadding.calculateBottomPadding() + 56.dp),
                )
            }
        }
    }
}

@Composable
private fun AlbumsTabContent(
    albums: List<Album>,
    gridColumns: Int,
    liquidGlassEnabled: Boolean,
    onSelectAlbum: (Album) -> Unit,
    bottomPadding: PaddingValues,
) {
    if (albums.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("暂无专辑", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(gridColumns),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 26.dp,
                top = 12.dp,
                bottom = 24.dp + bottomPadding.calculateBottomPadding(),
            ),
            horizontalArrangement = Arrangement.spacedBy(LocalLayoutDensity.current.gridSpacingDp.dp),
            verticalArrangement = Arrangement.spacedBy((LocalLayoutDensity.current.gridSpacingDp + 6).dp),
        ) {
            items(albums, key = { it.id }) { album ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectAlbum(album) },
                ) {
                    VinylSleeveCover(
                        albumArtUri = album.albumArtUri,
                        contentDescription = album.name,
                        isCurrent = false,
                        accentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(7.dp))
                    Text(
                        text = "${album.songs.size} 首",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                        maxLines = 1,
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = album.name,
                        style = if (gridColumns >= 3) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = album.artist,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(
                        thickness = 0.6.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f),
                    )
                }
            }
        }

        if (albums.size >= 18) {
            AlphabetFastScroller(
                onSelectLetter = { letter ->
                    val targetIdx = if (letter == "#") {
                        albums.indexOfFirst { a -> a.name.firstOrNull()?.uppercaseChar()?.isLetter() != true }
                    } else {
                        albums.indexOfFirst { a -> a.name.trimStart().uppercase().startsWith(letter) }
                    }
                    if (targetIdx >= 0) {
                        scope.launch { gridState.scrollToItem(targetIdx) }
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 2.dp, bottom = bottomPadding.calculateBottomPadding()),
            )
        }
    }
}

@Composable
private fun VinylSleeveCover(
    albumArtUri: android.net.Uri?,
    contentDescription: String?,
    isCurrent: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    // Lightweight physical vinyl sleeve (left 84% square cover + right peeking glossy black LP edge)
    Box(
        modifier = modifier.aspectRatio(1.16f),
        contentAlignment = Alignment.CenterStart,
    ) {
        // Peeking glossy jet-black vinyl LP disc behind right edge (slides slightly further out when playing!)
        Canvas(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight(0.90f)
                .aspectRatio(1f)
                .padding(end = if (isCurrent) 0.dp else 3.dp),
        ) {
            val r = size.minDimension / 2f
            val c = Offset(size.width / 2f, size.height / 2f)
            drawCircle(
                brush = Brush.radialGradient(
                    0.0f to Color(0xFF0A0A0C),
                    0.75f to Color(0xFF141417),
                    1.0f to Color(0xFF08080A),
                    center = c,
                    radius = r,
                ),
                radius = r,
                center = c,
            )
            // Subtle glossy groove rings
            drawCircle(
                color = Color.White.copy(alpha = 0.14f),
                radius = r * 0.84f,
                center = c,
                style = Stroke(width = 0.9f),
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.10f),
                radius = r * 0.66f,
                center = c,
                style = Stroke(width = 0.8f),
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.22f),
                radius = r - 0.8f,
                center = c,
                style = Stroke(width = 1.0f),
            )
            // Center label peek
            drawCircle(
                color = if (isCurrent) accentColor else Color(0xFF2A2825),
                radius = r * 0.30f,
                center = c,
            )
        }

        // Sharp Square Archival Vinyl Sleeve with delicate printed spine crease on left edge
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(3.dp),
                    ambientColor = Color.Black.copy(alpha = 0.18f),
                    spotColor = Color.Black.copy(alpha = 0.22f),
                ),
        ) {
            AlbumArtwork(
                uri = albumArtUri,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                cornerRadius = 3.dp,
                iconSize = 26.dp,
            )
            // Subtle physical cardboard sleeve spine crease line on left edge
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 4.dp)
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(Color.White.copy(alpha = 0.28f)),
            )
        }
    }
}

@Composable
private fun ArtistsTabContent(
    artists: List<Artist>,
    onSelectArtist: (Artist) -> Unit,
    bottomPadding: PaddingValues,
) {
    if (artists.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("暂无歌手", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    LazyColumn(contentPadding = bottomPadding) {
        items(artists, key = { it.name }) { artist ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectArtist(artist) }
                    .padding(horizontal = 18.dp, vertical = (LocalLayoutDensity.current.rowVerticalPaddingDp + 3).dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(0.75.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = artist.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${artist.albumCount} 张专辑 · ${artist.songs.size} 首歌曲",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                    modifier = Modifier.size(19.dp),
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(start = 72.dp, end = 18.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f),
            )
        }
    }
}

@Composable
private fun FoldersTabContent(
    folders: List<MusicFolder>,
    onSelectFolder: (MusicFolder) -> Unit,
    bottomPadding: PaddingValues,
) {
    if (folders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("暂无音频文件夹", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    LazyColumn(contentPadding = bottomPadding) {
        items(folders, key = { it.path }) { folder ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectFolder(folder) }
                    .padding(horizontal = 18.dp, vertical = (LocalLayoutDensity.current.rowVerticalPaddingDp + 3).dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(0.75.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = folder.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${folder.songs.size} 首 · ${folder.path}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                    modifier = Modifier.size(19.dp),
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(start = 72.dp, end = 18.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f),
            )
        }
    }
}

@Composable
private fun PlaylistsTabContent(
    playlists: List<Playlist>,
    playlistCovers: Map<String, android.net.Uri>,
    resolveSongsByIds: (List<Long>) -> List<Song>,
    favoriteSongs: List<Song>,
    recentSongs: List<Song>,
    allSongs: List<Song>,
    playCounts: Map<Long, Int>,
    hourlyPlays: Map<Int, Int>,
    liquidGlassEnabled: Boolean,
    onOpenFavorites: () -> Unit,
    onOpenRecent: () -> Unit,
    onOpenListeningReport: () -> Unit,
    onPlayFavorites: () -> Unit,
    onRequestCreatePlaylist: () -> Unit,
    onOpenPlaylist: (Playlist) -> Unit,
    onRequestDeletePlaylist: (Playlist) -> Unit,
    onRequestRenamePlaylist: (Playlist) -> Unit,
    onRequestManagePlaylistSongs: (Playlist) -> Unit,
    onRequestReplacePlaylistCover: (Playlist) -> Unit,
    onClearPlaylistCover: (Playlist) -> Unit,
    bottomPadding: PaddingValues,
) {
    val density = LocalLayoutDensity.current
    val cardShape = RoundedCornerShape(if (liquidGlassEnabled) 16.dp else 10.dp)
    val surfaceColor = MaterialTheme.colorScheme.surface
    val outlineColor = MaterialTheme.colorScheme.outline
    val accentColor = MaterialTheme.colorScheme.primary

    val favDurationMs = remember(favoriteSongs) { favoriteSongs.sumOf { it.durationMs.coerceAtLeast(0L) } }
    val favSubtitle = remember(favoriteSongs.size, favDurationMs) {
        if (favoriteSongs.isEmpty()) {
            "暂无珍藏 · 点击心形收藏喜爱曲目"
        } else {
            val totalMins = (favDurationMs / 60_000L).toInt()
            val h = totalMins / 60
            val m = totalMins % 60
            val durText = if (h > 0) "${h}小时${m}分" else "${totalMins.coerceAtLeast(1)}分钟"
            "${favoriteSongs.size} 首珍藏 · $durText"
        }
    }

    val totalPlays = remember(playCounts) { playCounts.values.sum() }
    val estimatedHoursText = remember(allSongs, playCounts) {
        val byId = allSongs.associateBy { it.id }
        val totalMs = playCounts.entries.sumOf { (id, cnt) ->
            (byId[id]?.durationMs ?: 210_000L).coerceAtLeast(0L) * cnt.toLong()
        }
        val hours = totalMs / 3_600_000.0
        when {
            totalMs <= 0L -> "0 分钟"
            hours < 1.0 -> "${(totalMs / 60_000L).coerceAtLeast(1L)} 分钟"
            else -> String.format(java.util.Locale.US, "%.1f 小时", hours)
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(
            start = 18.dp,
            end = 18.dp,
            top = 12.dp,
            bottom = 24.dp + bottomPadding.calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(density.gridSpacingDp.dp),
    ) {
        // 1. Hero Card: 我喜欢的音乐 (Stacked 3-cover vinyl trio + duration + conditional play button)
        item(key = "favorites_hero") {
            val favThumbs = remember(favoriteSongs) { favoriteSongs.mapNotNull { it.albumArtUri }.distinct().take(3) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .background(surfaceColor)
                    .border(0.8.dp, outlineColor, cardShape)
                    .clickable(onClick = onOpenFavorites)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (favThumbs.isNotEmpty()) {
                    // Layered 3-sleeve archival thumbnail stack
                    Box(
                        modifier = Modifier
                            .width(66.dp)
                            .height(56.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        favThumbs.reversed().forEachIndexed { revIdx, uri ->
                            val layerIdx = favThumbs.lastIndex - revIdx
                            val sizeDp = (52 - layerIdx * 5).dp
                            val xOff = (layerIdx * 9).dp
                            Box(
                                modifier = Modifier
                                    .offset(x = xOff)
                                    .size(sizeDp)
                                    .shadow(if (layerIdx == 0) 6.dp else 2.dp, RoundedCornerShape(6.dp))
                                    .clip(RoundedCornerShape(6.dp))
                                    .border(0.7.dp, surfaceColor, RoundedCornerShape(6.dp)),
                            ) {
                                AlbumArtwork(
                                    uri = uri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    cornerRadius = 6.dp,
                                    iconSize = 20.dp,
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.onBackground),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Favorite,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "我喜欢的音乐",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = favSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (favoriteSongs.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onBackground)
                            .clickable(onClick = onPlayFavorites),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = "播放我喜欢的音乐",
                            tint = MaterialTheme.colorScheme.background,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
        }

        // 2. Editorial Bento Row: 最近播放 (left) + 听歌报告 (right)
        item(key = "smart_bento_row") {
            val recentThumbs = remember(recentSongs) { recentSongs.take(3) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Left Card: 最近播放 with overlapping 3-cover preview
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(132.dp)
                        .clip(cardShape)
                        .background(surfaceColor)
                        .border(0.75.dp, outlineColor, cardShape)
                        .clickable(onClick = onOpenRecent)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "最近播放",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                    )

                    if (recentThumbs.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            recentThumbs.forEachIndexed { idx, s ->
                                val isCenter = idx == 0
                                val sz = if (isCenter) 46.dp else 38.dp
                                val xOff = when (idx) {
                                    0 -> 0.dp
                                    1 -> 34.dp
                                    else -> 64.dp
                                }
                                Box(
                                    modifier = Modifier
                                        .offset(x = xOff)
                                        .size(sz)
                                        .shadow(if (isCenter) 5.dp else 2.dp, RoundedCornerShape(6.dp))
                                        .clip(RoundedCornerShape(6.dp))
                                        .border(0.8.dp, surfaceColor, RoundedCornerShape(6.dp)),
                                ) {
                                    AlbumArtwork(
                                        uri = s.albumArtUri,
                                        contentDescription = s.title,
                                        modifier = Modifier.fillMaxSize(),
                                        cornerRadius = 6.dp,
                                        iconSize = 18.dp,
                                    )
                                }
                            }
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                            modifier = Modifier.size(28.dp),
                        )
                    }

                    Text(
                        text = if (recentSongs.isNotEmpty()) {
                            "${recentSongs.size} 首 · ${recentSongs.first().title}"
                        } else {
                            "暂无播放记录"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Right Card: 听歌报告 (live hours, play count, and 24h mini rhythm sparkline)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(132.dp)
                        .clip(cardShape)
                        .background(surfaceColor)
                        .border(0.75.dp, outlineColor, cardShape)
                        .clickable(onClick = onOpenListeningReport)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "听歌报告",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = estimatedHoursText,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$totalPlays 次播放",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                        }

                        // Minimalist 12-bar sparkline (2-hour buckets) highlighting peak listening period
                        val inkBar = MaterialTheme.colorScheme.onBackground
                        Canvas(
                            modifier = Modifier
                                .width(52.dp)
                                .height(42.dp),
                        ) {
                            val buckets = FloatArray(10) { b ->
                                val h1 = b * 2 + 2
                                val h2 = h1 + 1
                                ((hourlyPlays[h1 % 24] ?: 0) + (hourlyPlays[h2 % 24] ?: 0)).toFloat()
                            }
                            val maxV = buckets.maxOrNull() ?: 0f
                            val peakIdx = if (maxV > 0f) buckets.indices.maxByOrNull { buckets[it] } ?: -1 else -1
                            val step = size.width / buckets.size
                            val barW = (step * 0.46f).coerceAtLeast(2f)
                            buckets.forEachIndexed { idx, v ->
                                val ratio = if (maxV > 0f) (v / maxV).coerceIn(0.14f, 1f) else (0.18f + (idx % 3) * 0.08f)
                                val h = size.height * ratio
                                val x = idx * step + (step - barW) * 0.5f
                                val col = when {
                                    maxV <= 0f -> inkBar.copy(alpha = 0.16f)
                                    idx == peakIdx -> accentColor
                                    else -> inkBar.copy(alpha = 0.52f)
                                }
                                drawRoundRect(
                                    color = col,
                                    topLeft = Offset(x, size.height - h),
                                    size = Size(barW, h),
                                    cornerRadius = CornerRadius(barW * 0.5f, barW * 0.5f),
                                )
                            }
                        }
                    }

                    Text(
                        text = if (totalPlays > 0) "查看聆听档案" else "听歌后自动生成",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        maxLines = 1,
                    )
                }
            }
        }

        // 3. Section Header: 自建歌单
        item(key = "user_playlists_header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, start = 2.dp, end = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "自建歌单",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                if (playlists.isNotEmpty()) {
                    Text(
                        text = "${playlists.size} 个",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (playlists.isEmpty()) {
            item(key = "empty_user_playlists") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(cardShape)
                        .background(surfaceColor.copy(alpha = 0.72f))
                        .border(0.75.dp, outlineColor, cardShape)
                        .clickable(onClick = onRequestCreatePlaylist)
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "新建你的第一个自建歌单",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            // Clean 2-column cover card grid for user playlists
            val rows = playlists.chunked(2)
            items(rows, key = { pair -> pair.joinToString("_") { it.id } }) { rowPlaylists ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    rowPlaylists.forEach { playlist ->
                        val customCoverUri = playlistCovers[playlist.id]
                        val autoCoverUri = remember(playlist.songIds) {
                            resolveSongsByIds(playlist.songIds).firstOrNull()?.albumArtUri
                        }
                        val finalCoverUri = customCoverUri ?: autoCoverUri
                        var plMenuExpanded by remember { mutableStateOf(false) }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(cardShape)
                                .background(surfaceColor)
                                .border(0.75.dp, outlineColor, cardShape)
                                .clickable { onOpenPlaylist(playlist) }
                                .padding(10.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (finalCoverUri != null) {
                                    AlbumArtwork(
                                        uri = finalCoverUri,
                                        contentDescription = playlist.name,
                                        modifier = Modifier.fillMaxSize(),
                                        cornerRadius = 8.dp,
                                        iconSize = 36.dp,
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.QueueMusic,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                                        modifier = Modifier.size(34.dp),
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = playlist.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = "${playlist.songIds.size} 首",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Box {
                                    IconButton(
                                        onClick = { plMenuExpanded = true },
                                        modifier = Modifier.size(30.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.MoreVert,
                                            contentDescription = "歌单操作",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = plMenuExpanded,
                                        onDismissRequest = { plMenuExpanded = false },
                                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
                                        tonalElevation = 0.dp,
                                        shadowElevation = 10.dp,
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("添加 / 管理歌曲") },
                                            onClick = {
                                                plMenuExpanded = false
                                                onRequestManagePlaylistSongs(playlist)
                                            },
                                        )
                                        DropdownMenuItem(
                                            text = { Text("更换歌单封面") },
                                            onClick = {
                                                plMenuExpanded = false
                                                onRequestReplacePlaylistCover(playlist)
                                            },
                                        )
                                        if (customCoverUri != null) {
                                            DropdownMenuItem(
                                                text = { Text("恢复自动封面") },
                                                onClick = {
                                                    plMenuExpanded = false
                                                    onClearPlaylistCover(playlist)
                                                },
                                            )
                                        }
                                        DropdownMenuItem(
                                            text = { Text("重命名歌单") },
                                            onClick = {
                                                plMenuExpanded = false
                                                onRequestRenamePlaylist(playlist)
                                            },
                                        )
                                        DropdownMenuItem(
                                            text = { Text("删除歌单", color = MaterialTheme.colorScheme.primary) },
                                            onClick = {
                                                plMenuExpanded = false
                                                onRequestDeletePlaylist(playlist)
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (rowPlaylists.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ListeningReportView(
    allSongs: List<Song>,
    mostPlayedSongs: List<Song>,
    artists: List<Artist>,
    playCounts: Map<Long, Int>,
    hourlyPlays: Map<Int, Int>,
    playerState: PlayerUiState,
    liquidGlassEnabled: Boolean,
    onBack: () -> Unit,
    onPlaySongs: (List<Song>, Int, Boolean) -> Unit,
    onSelectArtist: (Artist) -> Unit,
    bottomPadding: PaddingValues,
) {
    val cardShape = RoundedCornerShape(if (liquidGlassEnabled) 16.dp else 10.dp)
    val surfaceColor = MaterialTheme.colorScheme.surface
    val outlineColor = MaterialTheme.colorScheme.outline
    val accentColor = MaterialTheme.colorScheme.primary
    val inkColor = MaterialTheme.colorScheme.onBackground
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant

    val totalPlays = remember(playCounts) { playCounts.values.sum() }
    val playedSongCount = remember(playCounts) { playCounts.count { it.value > 0 } }
    val totalListeningDisplay = remember(allSongs, playCounts) {
        val byId = allSongs.associateBy { it.id }
        val totalMs = playCounts.entries.sumOf { (id, cnt) ->
            (byId[id]?.durationMs ?: 210_000L).coerceAtLeast(0L) * cnt.toLong()
        }
        val hours = totalMs / 3_600_000.0
        when {
            totalMs <= 0L -> "0m"
            hours < 1.0 -> "${(totalMs / 60_000L).coerceAtLeast(1L)}m"
            else -> String.format(java.util.Locale.US, "%.1fh", hours)
        }
    }

    val topSongs = remember(mostPlayedSongs) { mostPlayedSongs.take(10) }
    val topArtists = remember(allSongs, artists, playCounts) {
        val artistPlayMap = mutableMapOf<String, Int>()
        allSongs.forEach { s ->
            val cnt = playCounts[s.id] ?: 0
            if (cnt > 0) {
                artistPlayMap[s.artist] = (artistPlayMap[s.artist] ?: 0) + cnt
            }
        }
        val artistByName = artists.associateBy { it.name }
        artistPlayMap.entries
            .sortedByDescending { it.value }
            .take(5)
            .mapNotNull { (name, count) ->
                artistByName[name]?.let { it to count }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        // Minimal archival header (zero decorative or unused right buttons)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "返回歌单",
                    tint = inkColor,
                )
            }
            Text(
                text = "听歌报告",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = inkColor,
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 18.dp,
                end = 18.dp,
                top = 4.dp,
                bottom = 28.dp + bottomPadding.calculateBottomPadding(),
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 1. Top 3 Archival Metric Cards
            item(key = "report_metrics") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    listOf(
                        totalListeningDisplay to "累计聆听",
                        "${totalPlays}次" to "累计播放",
                        "${playedSongCount}首" to "听过曲目",
                    ).forEach { (valText, labelText) ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(cardShape)
                                .background(surfaceColor)
                                .border(0.75.dp, outlineColor, cardShape)
                                .padding(vertical = 14.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = valText,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = inkColor,
                                maxLines = 1,
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = labelText,
                                style = MaterialTheme.typography.labelSmall,
                                color = mutedColor,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }

            // 3. Top Played Tracks Card (seamlessly retains all functionality of 常听曲目)
            item(key = "report_top_songs") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(cardShape)
                        .background(surfaceColor)
                        .border(0.75.dp, outlineColor, cardShape)
                        .padding(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "最常循环曲目",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = inkColor,
                        )
                        if (topSongs.isNotEmpty()) {
                            Text(
                                text = "播放全部",
                                style = MaterialTheme.typography.labelMedium,
                                color = accentColor,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onPlaySongs(mostPlayedSongs, 0, false) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (topSongs.isEmpty()) {
                        Text(
                            text = "暂无常听曲目记录，播放任意本地歌曲即可生成排行",
                            style = MaterialTheme.typography.bodySmall,
                            color = mutedColor,
                            modifier = Modifier.padding(vertical = 12.dp),
                        )
                    } else {
                        topSongs.forEachIndexed { idx, s ->
                            val cnt = playCounts[s.id] ?: 1
                            val isCurrent = playerState.currentSong?.id == s.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onPlaySongs(mostPlayedSongs, idx, false) }
                                    .padding(vertical = 7.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = String.format(java.util.Locale.US, "%02d", idx + 1),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (idx < 3) FontWeight.Bold else FontWeight.Medium,
                                    color = if (idx == 0) accentColor else mutedColor,
                                    modifier = Modifier.width(26.dp),
                                )
                                AlbumArtwork(
                                    uri = s.albumArtUri,
                                    contentDescription = s.title,
                                    modifier = Modifier.size(40.dp),
                                    cornerRadius = 6.dp,
                                    iconSize = 18.dp,
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = s.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isCurrent) accentColor else inkColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = s.artist,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = mutedColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                ) {
                                    Text(
                                        text = "×$cnt",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (idx == 0) accentColor else mutedColor,
                                    )
                                }
                            }
                            if (idx < topSongs.lastIndex) {
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = outlineColor.copy(alpha = 0.55f),
                                )
                            }
                        }
                    }
                }
            }

            // 4. Editorial Favorite Artists Card (Serif Rank + Circular Cover Avatar + 2.5dp Ribbon)
            if (topArtists.isNotEmpty()) {
                item(key = "report_top_artists") {
                    val maxArtistPlays = topArtists.first().second.coerceAtLeast(1)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(cardShape)
                            .background(surfaceColor)
                            .border(0.75.dp, outlineColor, cardShape)
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "偏爱歌手",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = inkColor,
                            )
                            Text(
                                text = "TOP ${topArtists.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = mutedColor,
                                letterSpacing = 1.sp,
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        topArtists.forEachIndexed { idx, (artist, cnt) ->
                            val frac = (cnt.toFloat() / maxArtistPlays.toFloat()).coerceIn(0.08f, 1f)
                            val coverUri = artist.songs.firstOrNull { it.albumArtUri != null }?.albumArtUri
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onSelectArtist(artist) }
                                    .padding(vertical = 9.dp, horizontal = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = String.format(java.util.Locale.US, "%02d", idx + 1),
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 15.sp,
                                    fontWeight = if (idx == 0) FontWeight.Bold else FontWeight.Medium,
                                    color = if (idx == 0) accentColor else mutedColor,
                                    modifier = Modifier.width(28.dp),
                                )
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .border(0.8.dp, outlineColor, CircleShape)
                                        .padding(1.5.dp)
                                        .clip(CircleShape),
                                ) {
                                    AlbumArtwork(
                                        uri = coverUri,
                                        contentDescription = artist.name,
                                        modifier = Modifier.fillMaxSize(),
                                        cornerRadius = 19.dp,
                                        iconSize = 16.dp,
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Bottom,
                                    ) {
                                        Text(
                                            text = artist.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = inkColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f),
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Row(verticalAlignment = Alignment.Bottom) {
                                            Text(
                                                text = "$cnt",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (idx == 0) accentColor else inkColor,
                                            )
                                            Text(
                                                text = " 次",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = mutedColor,
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(2.5.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(frac)
                                                .fillMaxHeight()
                                                .clip(CircleShape)
                                                .background(if (idx == 0) accentColor else inkColor.copy(alpha = 0.62f)),
                                        )
                                    }
                                }
                            }
                            if (idx < topArtists.lastIndex) {
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = outlineColor.copy(alpha = 0.55f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollectionDetailView(
    title: String,
    subtitle: String,
    songs: List<Song>,
    favoriteSongIds: Set<Long>,
    playerState: PlayerUiState,
    liquidGlassEnabled: Boolean,
    onBack: () -> Unit,
    onPlaySongs: (List<Song>, Int, Boolean) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onRequestAddToPlaylist: (Song) -> Unit,
    onShowSongInfo: (Song) -> Unit,
    onReplaceCover: (Song) -> Unit,
    onMatchLyrics: (Song) -> Unit,
    onDeleteSong: (Song) -> Unit,
    onManageSongs: (() -> Unit)?,
    onRenamePlaylist: (() -> Unit)?,
    onRemoveFromPlaylist: ((Long) -> Unit)?,
    bottomContentPadding: PaddingValues,
) {
    val actionCorner = if (liquidGlassEnabled) 14.dp else 7.dp
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                title = {
                    Column {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (onManageSongs != null) {
                        IconButton(onClick = onManageSongs) {
                            Icon(Icons.Rounded.Add, contentDescription = "添加或管理歌曲")
                        }
                    }
                    if (onRenamePlaylist != null) {
                        IconButton(onClick = onRenamePlaylist) {
                            Icon(Icons.Rounded.Edit, contentDescription = "重命名歌单", modifier = Modifier.size(20.dp))
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        if (songs.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("这里还没有歌曲", color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (onManageSongs != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(actionCorner))
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable(onClick = onManageSongs)
                            .padding(horizontal = 18.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "添加歌曲到歌单",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = bottomContentPadding,
            ) {
                item {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 18.dp),
                        thickness = 0.75.dp,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(actionCorner))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(0.75.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(actionCorner))
                                .clickable { onPlaySongs(songs, 0, false) },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "播放全部",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(actionCorner))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(0.75.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(actionCorner))
                                .clickable { onPlaySongs(songs, 0, true) },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Shuffle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "随机播放",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
                itemsIndexed(songs, key = { idx, song -> "${song.id}_$idx" }) { index, song ->
                    SongRowItem(
                        song = song,
                        isCurrent = playerState.currentSong?.id == song.id,
                        isPlaying = playerState.isPlaying,
                        isFavorite = song.id in favoriteSongIds,
                        onClick = { onPlaySongs(songs, index, false) },
                        onAddToPlaylist = { onRequestAddToPlaylist(song) },
                        onToggleFavorite = { onToggleFavorite(song.id) },
                        onShowSongInfo = { onShowSongInfo(song) },
                        onReplaceCover = { onReplaceCover(song) },
                        onMatchLyrics = { onMatchLyrics(song) },
                        onDeleteSong = { onDeleteSong(song) },
                        extraMenuActionLabel = if (onRemoveFromPlaylist != null) "从歌单移除" else null,
                        onExtraMenuAction = onRemoveFromPlaylist?.let { callback -> { callback(song.id) } },
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyLibraryPlaceholder(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.MusicOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(64.dp),
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "未发现本地音频文件",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "将 MP3 / FLAC / WAV 等音乐文件放入手机 Music 目录后点击重新扫描。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        FilledTonalButton(onClick = onRefresh) {
            Icon(Icons.Rounded.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("重新扫描")
        }
    }
}
