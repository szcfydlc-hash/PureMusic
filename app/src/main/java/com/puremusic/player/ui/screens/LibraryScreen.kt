package com.puremusic.player.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MusicOff
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
    onRequestCreatePlaylist: () -> Unit,
    onDeletePlaylist: (String) -> Unit,
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
    var searchExpanded by remember { mutableStateOf(false) }
    var usePosterGrid by remember { mutableStateOf(false) }

    BackHandler(enabled = activeDetail != null || searchExpanded || searchQuery.isNotEmpty()) {
        when {
            activeDetail != null -> activeDetail = null
            searchQuery.isNotEmpty() -> onSearchQueryChange("")
            searchExpanded -> searchExpanded = false
        }
    }

    val detail = activeDetail
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
        }

        CollectionDetailView(
            detail = detail,
            songs = detailSongs,
            favoriteSongIds = favoriteSongIds,
            playerState = playerState,
            onBack = { activeDetail = null },
            onPlaySongs = onPlaySongs,
            onToggleFavorite = onToggleFavorite,
            onRequestAddToPlaylist = onRequestAddToPlaylist,
            onShowSongInfo = onShowSongInfo,
            onRemoveFromPlaylist = if (detail is CollectionDetail.PlaylistDetail) {
                { songId -> onRemoveSongFromPlaylist(detail.playlistId, songId) }
            } else {
                null
            },
            bottomContentPadding = bottomContentPadding,
        )
        return
    }

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
                VerticalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f))
            }

            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                // 1. Sleek Modern Editorial Header (Zero top bloat!)
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 16.dp, top = 12.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = headerTitle,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = headerSubtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (sleepTimerRemainingMs != null) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .clickable(onClick = onOpenSleepTimer)
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
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

                        // Expandable Search Icon Button
                        HeaderCircleIconButton(
                            onClick = {
                                if (searchExpanded && searchQuery.isNotEmpty()) {
                                    onSearchQueryChange("")
                                }
                                searchExpanded = !searchExpanded
                            },
                            active = searchExpanded || searchQuery.isNotEmpty(),
                        ) {
                            Icon(
                                imageVector = if (searchExpanded) Icons.Rounded.Close else Icons.Rounded.Search,
                                contentDescription = "搜索",
                                modifier = Modifier.size(19.dp),
                            )
                        }

                        if (activeMainTab == MainTab.LIBRARY && selectedSubTab == LibrarySubTab.SONGS) {
                            Box {
                                HeaderCircleIconButton(
                                    onClick = { sortMenuExpanded = true },
                                    active = false,
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.Sort,
                                        contentDescription = "排序",
                                        modifier = Modifier.size(19.dp),
                                    )
                                }
                                DropdownMenu(
                                    expanded = sortMenuExpanded,
                                    onDismissRequest = { sortMenuExpanded = false },
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

                            HeaderCircleIconButton(
                                onClick = { usePosterGrid = !usePosterGrid },
                                active = usePosterGrid,
                            ) {
                                Icon(
                                    imageVector = if (usePosterGrid) {
                                        Icons.AutoMirrored.Rounded.ViewList
                                    } else {
                                        Icons.Rounded.GridView
                                    },
                                    contentDescription = "切换视图",
                                    modifier = Modifier.size(19.dp),
                                )
                            }
                        }

                        if (activeMainTab == MainTab.PLAYLISTS) {
                            FilledIconButton(
                                onClick = onRequestCreatePlaylist,
                                shape = CircleShape,
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                                modifier = Modifier.size(36.dp),
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

                // 2. Collapsible Search Input Pill (expands smoothly ONLY on tap!)
                AnimatedVisibility(
                    visible = searchExpanded || searchQuery.isNotEmpty(),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 6.dp)
                            .height(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "搜索歌曲、专辑、歌手",
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

                // 3. Inline Sub-Pills on Library tab (歌曲 / 歌手 / 文件夹)
                if (activeMainTab == MainTab.LIBRARY) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        LibrarySubTab.entries.forEach { subTab ->
                            val selected = subTab == selectedSubTab
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (selected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant
                                        },
                                    )
                                    .clickable { selectedSubTab = subTab }
                                    .padding(horizontal = 16.dp, vertical = 7.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = subTab.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                    color = if (selected) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                )
                            }
                        }
                    }
                }

                // 4. Twin Quick-Action Pills on Songs sub-tab (播放全部 + 随机播放)
                if (activeMainTab == MainTab.LIBRARY && selectedSubTab == LibrarySubTab.SONGS && !isLoading && songs.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable { onPlaySongs(songs, 0, false) },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(19.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "播放全部",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onPlaySongs(songs, 0, true) },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Shuffle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(17.dp),
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

                // 5. Main Tab Content Area
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    val songGridColumns = if (isTabletLayout) 4 else 3
                    val albumGridColumns = if (isTabletLayout) 4 else 2

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
                                    onRefresh = onRefreshLibrary,
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
                                onSelectAlbum = { activeDetail = CollectionDetail.AlbumDetail(it) },
                                bottomPadding = bottomContentPadding,
                            )

                            MainTab.PLAYLISTS -> PlaylistsTabContent(
                                playlists = playlists,
                                favoriteCount = favoriteSongIds.size,
                                onOpenFavorites = { activeDetail = CollectionDetail.FavoritesDetail },
                                onPlayFavorites = {
                                    val favSongs = allSongs.filter { it.id in favoriteSongIds }
                                    if (favSongs.isNotEmpty()) onPlaySongs(favSongs, 0, false)
                                },
                                onOpenPlaylist = {
                                    activeDetail = CollectionDetail.PlaylistDetail(it.id, it.name)
                                },
                                onDeletePlaylist = onDeletePlaylist,
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
private fun HeaderCircleIconButton(
    onClick: () -> Unit,
    active: Boolean,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
                if (active) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides if (active) {
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
    val navItems = listOf(
        MainTab.LIBRARY to Icons.Rounded.LibraryMusic,
        MainTab.ALBUMS to Icons.Rounded.Album,
        MainTab.PLAYLISTS to Icons.Rounded.QueueMusic,
        MainTab.SETTINGS to Icons.Rounded.Settings,
    )

    Column(
        modifier = Modifier
            .width(260.dp)
            .fillMaxHeight()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "简约音乐",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            )

            navItems.forEach { (tab, icon) ->
                val selected = tab == activeMainTab
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        )
                        .clickable {
                            if (tab == MainTab.SETTINGS) onOpenSettings() else onSelectMainTab(tab)
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val song = playerState.currentSong
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass(enabled = liquidGlassEnabled, shape = RoundedCornerShape(20.dp), elevation = 6.dp)
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
                        rotationDegrees = 0f,
                        discSize = 126.dp,
                    )
                    Text(
                        text = song?.title ?: "暂未播放",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = song?.artist ?: "$songCount 首歌曲 · $albumCount 张专辑",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = onSkipPrevious) {
                            Icon(Icons.Rounded.SkipPrevious, contentDescription = "上一首")
                        }
                        FilledIconButton(
                            onClick = onTogglePlayPause,
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                            modifier = Modifier.size(42.dp),
                        ) {
                            Icon(
                                imageVector = if (playerState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = "播放/暂停",
                            )
                        }
                        IconButton(onClick = onSkipNext) {
                            Icon(Icons.Rounded.SkipNext, contentDescription = "下一首")
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalButton(onClick = onOpenEqualizer, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Rounded.Equalizer, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("均衡器", maxLines = 1)
                }
                FilledTonalButton(onClick = onOpenSleepTimer, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Rounded.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("定时", maxLines = 1)
                }
            }
        }
    }
}

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
    onRefresh: () -> Unit,
    bottomPadding: PaddingValues,
) {
    if (songs.isEmpty()) {
        EmptyLibraryPlaceholder(onRefresh = onRefresh)
        return
    }

    if (usePosterGrid) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(gridColumns),
            contentPadding = PaddingValues(
                start = 18.dp,
                end = 18.dp,
                top = 6.dp,
                bottom = 20.dp + bottomPadding.calculateBottomPadding(),
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
                val isCurrent = playerState.currentSong?.id == song.id
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPlaySongs(songs, index, false) },
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .shadow(4.dp, RoundedCornerShape(12.dp), clip = false),
                    ) {
                        AlbumArtwork(
                            uri = song.albumArtUri,
                            contentDescription = song.album,
                            modifier = Modifier.fillMaxSize(),
                            cornerRadius = 12.dp,
                            iconSize = 28.dp,
                        )
                        if (isCurrent) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(6.dp)
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = if (playerState.isPlaying) Icons.Rounded.GraphicEq else Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp),
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(7.dp))
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    } else {
        val listState = rememberLazyListState()
        val scope = rememberCoroutineScope()
        Box(modifier = Modifier.fillMaxSize()) {
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
                        onClick = { onPlaySongs(songs, index, false) },
                        onAddToPlaylist = { onRequestAddToPlaylist(song) },
                        onToggleFavorite = { onToggleFavorite(song.id) },
                        onShowSongInfo = { onShowSongInfo(song) },
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
                        .padding(end = 2.dp, bottom = bottomPadding.calculateBottomPadding()),
                )
            }
        }
    }
}

@Composable
private fun AlbumsTabContent(
    albums: List<Album>,
    gridColumns: Int,
    onSelectAlbum: (Album) -> Unit,
    bottomPadding: PaddingValues,
) {
    if (albums.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("暂无专辑", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(gridColumns),
        contentPadding = PaddingValues(
            start = 18.dp,
            end = 18.dp,
            top = 10.dp,
            bottom = 20.dp + bottomPadding.calculateBottomPadding(),
        ),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        items(albums, key = { it.id }) { album ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectAlbum(album) },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(14.dp),
                            ambientColor = Color.Black.copy(alpha = 0.14f),
                            spotColor = Color.Black.copy(alpha = 0.18f),
                        ),
                ) {
                    AlbumArtwork(
                        uri = album.albumArtUri,
                        contentDescription = album.name,
                        modifier = Modifier.fillMaxSize(),
                        cornerRadius = 14.dp,
                        iconSize = 36.dp,
                    )
                }
                Spacer(modifier = Modifier.height(9.dp))
                Text(
                    text = album.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${album.artist} · ${album.songs.size} 首",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
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
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp),
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
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp),
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(start = 76.dp, end = 18.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
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
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp),
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
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp),
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(start = 76.dp, end = 18.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
            )
        }
    }
}

@Composable
private fun PlaylistsTabContent(
    playlists: List<Playlist>,
    favoriteCount: Int,
    onOpenFavorites: () -> Unit,
    onPlayFavorites: () -> Unit,
    onOpenPlaylist: (Playlist) -> Unit,
    onDeletePlaylist: (String) -> Unit,
    bottomPadding: PaddingValues,
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val cardBg = if (isDark) Color(0xFF151820) else Color(0xFFF8F9FC)

    LazyColumn(
        contentPadding = PaddingValues(
            start = 18.dp,
            end = 18.dp,
            top = 10.dp,
            bottom = 20.dp + bottomPadding.calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "favorites_hero") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.28f else 0.14f),
                                MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.12f else 0.05f),
                            ),
                        ),
                    )
                    .border(
                        0.8.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
                        RoundedCornerShape(20.dp),
                    )
                    .clickable(onClick = onOpenFavorites)
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp),
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "我喜欢的音乐",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$favoriteCount 首收藏歌曲",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (favoriteCount > 0) {
                    FilledIconButton(
                        onClick = onPlayFavorites,
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White,
                        ),
                        modifier = Modifier.size(40.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = "播放我喜欢的音乐",
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
        }

        if (playlists.isNotEmpty()) {
            item(key = "custom_header") {
                Text(
                    text = "自建歌单",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp, start = 4.dp),
                )
            }
        }

        items(playlists, key = { it.id }) { playlist ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardBg)
                    .border(
                        0.5.dp,
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                        RoundedCornerShape(16.dp),
                    )
                    .clickable { onOpenPlaylist(playlist) }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.QueueMusic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = playlist.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${playlist.songIds.size} 首歌曲",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = { onDeletePlaylist(playlist.id) }) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "删除歌单",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollectionDetailView(
    detail: CollectionDetail,
    songs: List<Song>,
    favoriteSongIds: Set<Long>,
    playerState: PlayerUiState,
    onBack: () -> Unit,
    onPlaySongs: (List<Song>, Int, Boolean) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onRequestAddToPlaylist: (Song) -> Unit,
    onShowSongInfo: (Song) -> Unit,
    onRemoveFromPlaylist: ((Long) -> Unit)?,
    bottomContentPadding: PaddingValues,
) {
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
                            text = detail.title,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = detail.subtitle,
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
            )
        },
    ) { innerPadding ->
        if (songs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text("这里还没有歌曲", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = bottomContentPadding,
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable { onPlaySongs(songs, 0, false) },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(19.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "播放全部",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onPlaySongs(songs, 0, true) },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Shuffle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(17.dp),
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
