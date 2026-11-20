package com.puremusic.player.ui.screens

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
import androidx.compose.material.icons.rounded.ViewModule
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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
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
    onReplaceCover: (Song) -> Unit,
    onMatchLyrics: (Song) -> Unit,
    onRequestCreatePlaylist: () -> Unit,
    onRequestDeletePlaylist: (Playlist) -> Unit,
    onRequestRenamePlaylist: (Playlist) -> Unit,
    onRequestManagePlaylistSongs: (Playlist) -> Unit,
    onRequestManageFavorites: () -> Unit,
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
    var albumCompactThreeCol by remember { mutableStateOf(true) }

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

        val livePlaylist = if (detail is CollectionDetail.PlaylistDetail) {
            playlists.firstOrNull { it.id == detail.playlistId }
        } else {
            null
        }
        val liveTitle = livePlaylist?.name ?: detail.title
        val liveSubtitle = when (detail) {
            is CollectionDetail.PlaylistDetail -> "自定义歌单 · ${detailSongs.size} 首"
            CollectionDetail.FavoritesDetail -> "收藏列表 · ${detailSongs.size} 首"
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 16.dp, top = 14.dp, bottom = 6.dp),
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
                        Spacer(modifier = Modifier.height(2.dp))
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

                        if (activeMainTab == MainTab.LIBRARY && selectedSubTab == LibrarySubTab.SONGS) {
                            Box {
                                HeaderToolButton(
                                    onClick = { sortMenuExpanded = true },
                                    active = false,
                                    liquidGlassEnabled = liquidGlassEnabled,
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
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 0.dp,
                                    border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline),
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

                            HeaderToolButton(
                                onClick = { usePosterGrid = !usePosterGrid },
                                active = usePosterGrid,
                                liquidGlassEnabled = liquidGlassEnabled,
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

                // 4. Refined Flat Outlined Action Buttons (播放全部 + 随机播放)
                if (activeMainTab == MainTab.LIBRARY && selectedSubTab == LibrarySubTab.SONGS && !isLoading && songs.isNotEmpty()) {
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
                                .background(
                                    if (liquidGlassEnabled) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    },
                                )
                                .border(0.75.dp, outlineColor, RoundedCornerShape(actionCorner))
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
                                .border(0.75.dp, outlineColor, RoundedCornerShape(actionCorner))
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
                                liquidGlassEnabled = liquidGlassEnabled,
                                onSelectAlbum = { activeDetail = CollectionDetail.AlbumDetail(it) },
                                bottomPadding = bottomContentPadding,
                            )

                            MainTab.PLAYLISTS -> PlaylistsTabContent(
                                playlists = playlists,
                                favoriteCount = favoriteSongIds.size,
                                liquidGlassEnabled = liquidGlassEnabled,
                                onOpenFavorites = { activeDetail = CollectionDetail.FavoritesDetail },
                                onPlayFavorites = {
                                    val favSongs = allSongs.filter { it.id in favoriteSongIds }
                                    if (favSongs.isNotEmpty()) onPlaySongs(favSongs, 0, false)
                                },
                                onOpenPlaylist = {
                                    activeDetail = CollectionDetail.PlaylistDetail(it.id, it.name)
                                },
                                onRequestDeletePlaylist = onRequestDeletePlaylist,
                                onRequestRenamePlaylist = onRequestRenamePlaylist,
                                onRequestManagePlaylistSongs = onRequestManagePlaylistSongs,
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
    val shape = if (liquidGlassEnabled) CircleShape else RoundedCornerShape(7.dp)
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
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "简约音乐",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            )
            HorizontalDivider(thickness = 0.75.dp, color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(4.dp))

            navItems.forEach { (tab, icon) ->
                val selected = tab == activeMainTab
                val itemShape = RoundedCornerShape(if (liquidGlassEnabled) 14.dp else 8.dp)
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
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(19.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
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
            val cardShape = RoundedCornerShape(if (liquidGlassEnabled) 20.dp else 10.dp)
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
    onRefresh: () -> Unit,
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
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
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                        border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline),
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
                            text = { Text("歌曲信息") },
                            onClick = { itemMenuExpanded = false; onShowSongInfo(song) },
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
                        .padding(end = 2.dp, bottom = bottomPadding.calculateBottomPadding()),
                )
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
                        onReplaceCover = { onReplaceCover(song) },
                        onMatchLyrics = { onMatchLyrics(song) },
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
            horizontalArrangement = Arrangement.spacedBy(if (gridColumns >= 3) 12.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
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
                    .padding(horizontal = 18.dp, vertical = 12.dp),
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
                    .padding(horizontal = 18.dp, vertical = 12.dp),
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
    favoriteCount: Int,
    liquidGlassEnabled: Boolean,
    onOpenFavorites: () -> Unit,
    onPlayFavorites: () -> Unit,
    onOpenPlaylist: (Playlist) -> Unit,
    onRequestDeletePlaylist: (Playlist) -> Unit,
    onRequestRenamePlaylist: (Playlist) -> Unit,
    onRequestManagePlaylistSongs: (Playlist) -> Unit,
    onRequestManageFavorites: () -> Unit,
    bottomPadding: PaddingValues,
) {
    val cardShape = RoundedCornerShape(if (liquidGlassEnabled) 16.dp else 8.dp)

    LazyColumn(
        contentPadding = PaddingValues(
            start = 18.dp,
            end = 18.dp,
            top = 12.dp,
            bottom = 20.dp + bottomPadding.calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "favorites_hero") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(0.8.dp, MaterialTheme.colorScheme.outline, cardShape)
                    .clickable(onClick = onOpenFavorites)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(if (liquidGlassEnabled) 14.dp else 6.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp),
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
                        text = "$favoriteCount 首珍藏曲目",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (favoriteCount > 0) {
                    IconButton(onClick = onPlayFavorites) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = "播放我喜欢的音乐",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp),
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
                    modifier = Modifier.padding(top = 6.dp, start = 2.dp),
                )
            }
        }

        items(playlists, key = { it.id }) { playlist ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(0.75.dp, MaterialTheme.colorScheme.outline, cardShape)
                    .clickable { onOpenPlaylist(playlist) }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.QueueMusic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
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
                var plMenuExpanded by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { plMenuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "歌单操作",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    DropdownMenu(
                        expanded = plMenuExpanded,
                        onDismissRequest = { plMenuExpanded = false },
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                        border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline),
                    ) {
                        DropdownMenuItem(
                            text = { Text("添加 / 管理歌曲") },
                            onClick = {
                                plMenuExpanded = false
                                onRequestManagePlaylistSongs(playlist)
                            },
                        )
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
