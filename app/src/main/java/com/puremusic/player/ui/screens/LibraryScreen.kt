package com.puremusic.player.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.rememberCoroutineScope
import com.puremusic.player.ui.components.AlphabetFastScroller
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.KeyboardArrowDown
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.puremusic.player.data.model.Album
import com.puremusic.player.data.model.Artist
import com.puremusic.player.data.model.LibraryTab
import com.puremusic.player.data.model.MusicFolder
import com.puremusic.player.data.model.PlayerTheme
import com.puremusic.player.data.model.PlayerUiState
import com.puremusic.player.data.model.Playlist
import com.puremusic.player.data.model.Song
import com.puremusic.player.data.model.SortOrder
import com.puremusic.player.ui.components.AlbumArtwork
import com.puremusic.player.ui.components.SongRowItem
import com.puremusic.player.ui.components.formatCountdown
import com.puremusic.player.ui.components.liquidGlass

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
    onRequestCreatePlaylist: () -> Unit,
    onDeletePlaylist: (String) -> Unit,
    onRemoveSongFromPlaylist: (String, Long) -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenThemeSettings: () -> Unit,
    resolveSongsByIds: (List<Long>) -> List<Song>,
    bottomContentPadding: PaddingValues = PaddingValues(0.dp),
) {
    var selectedTab by remember { mutableStateOf(LibraryTab.SONGS) }
    var activeDetail by remember { mutableStateOf<CollectionDetail?>(null) }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var usePosterGrid by remember { mutableStateOf(false) }

    BackHandler(enabled = activeDetail != null || searchQuery.isNotEmpty()) {
        if (activeDetail != null) {
            activeDetail = null
        } else {
            onSearchQueryChange("")
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
        floatingActionButton = {
            if (selectedTab == LibraryTab.PLAYLISTS) {
                FloatingActionButton(
                    onClick = onRequestCreatePlaylist,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.padding(bottomContentPadding),
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "新建歌单")
                }
            }
        },
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = innerPadding.calculateBottomPadding()),
        ) {
            // REAL TABLET LEFT SIDEBAR (Active when Layout = TABLET or AUTO on wide screens)
            if (isTabletLayout) {
                TabletLeftSidebar(
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
                VerticalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.16f))
            }

            // MAIN EDITORIAL CONTENT PANE
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                // 1. Editorial Serif Header + Liquid Glass Settings Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text = "简约音乐",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = "${allSongs.size} 首本地音乐 · ${albums.size} 张专辑",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (sleepTimerRemainingMs != null) {
                            Box(
                                modifier = Modifier
                                    .liquidGlass(enabled = liquidGlassEnabled, shape = RoundedCornerShape(16.dp), elevation = 4.dp)
                                    .clickable(onClick = onOpenSleepTimer)
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Timer,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(15.dp),
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = formatCountdown(sleepTimerRemainingMs),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .liquidGlass(enabled = liquidGlassEnabled, shape = CircleShape, elevation = 6.dp)
                                .clickable(onClick = onOpenThemeSettings),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = "设置与外观",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }

                // 2. Liquid Glass Search Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(44.dp)
                        .liquidGlass(enabled = liquidGlassEnabled, shape = RoundedCornerShape(22.dp), elevation = 5.dp)
                        .padding(horizontal = 14.dp),
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
                                text = "搜索歌曲、专辑、艺人",
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

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Spring-Animated Sliding Capsule Category Bar (animates smoothly ONLY on tab switch!)
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(44.dp)
                        .liquidGlass(enabled = liquidGlassEnabled, shape = RoundedCornerShape(22.dp), elevation = 4.dp)
                        .padding(4.dp),
                ) {
                    val tabCount = LibraryTab.entries.size
                    val tabWidth = maxWidth / tabCount
                    val indicatorOffset by animateDpAsState(
                        targetValue = tabWidth * selectedTab.ordinal,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                        label = "tabIndicatorSlide",
                    )

                    // Sliding active glass pill
                    Box(
                        modifier = Modifier
                            .offset(x = indicatorOffset)
                            .width(tabWidth)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.primary),
                    )

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        LibraryTab.entries.forEach { tab ->
                            val selected = selectedTab == tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable { selectedTab = tab },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = tab.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }

                // 4. Focused Quick-Action Bar (Play All + Shuffle + Sort + View Toggle)
                if (selectedTab == LibraryTab.SONGS && !isLoading && songs.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable { onPlaySongs(songs, 0, false) }
                                    .padding(horizontal = 14.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "播放全部 (${songs.size})",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { onPlaySongs(songs, 0, true) }
                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Shuffle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "随机",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { sortMenuExpanded = true }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = sortOrder.label,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Icon(
                                        imageVector = Icons.Rounded.KeyboardArrowDown,
                                        contentDescription = "排序",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp),
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

                            IconButton(
                                onClick = { usePosterGrid = !usePosterGrid },
                                modifier = Modifier.size(34.dp),
                            ) {
                                Icon(
                                    imageVector = if (usePosterGrid) {
                                        Icons.AutoMirrored.Rounded.ViewList
                                    } else {
                                        Icons.Rounded.GridView
                                    },
                                    contentDescription = "切换视图",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(19.dp),
                                )
                            }
                        }
                    }
                }

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    val gridColumns = if (isTabletLayout) 4 else 3
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            val forward = targetState.ordinal >= initialState.ordinal
                            val slideIn = slideInHorizontally { if (forward) it / 8 else -it / 8 }
                            val slideOut = slideOutHorizontally { if (forward) -it / 8 else it / 8 }
                            (fadeIn() + slideIn) togetherWith (fadeOut() + slideOut)
                        },
                        label = "libraryTabTransition",
                        modifier = Modifier.weight(1f),
                    ) { currentTab ->
                    when (currentTab) {
                        LibraryTab.SONGS -> SongsTabContent(
                            songs = songs,
                            playerTheme = playerTheme,
                            featuredAlbums = if (searchQuery.isBlank()) albums.take(8) else emptyList(),
                            usePosterGrid = usePosterGrid,
                            gridColumns = gridColumns,
                            liquidGlassEnabled = liquidGlassEnabled,
                            favoriteSongIds = favoriteSongIds,
                            playerState = playerState,
                            onPlaySongs = onPlaySongs,
                            onSelectFeaturedAlbum = { activeDetail = CollectionDetail.AlbumDetail(it) },
                            onToggleFavorite = onToggleFavorite,
                            onRequestAddToPlaylist = onRequestAddToPlaylist,
                            onRefresh = onRefreshLibrary,
                            bottomPadding = bottomContentPadding,
                        )

                        LibraryTab.ALBUMS -> AlbumsTabContent(
                            albums = albums,
                            gridColumns = gridColumns,
                            onSelectAlbum = { activeDetail = CollectionDetail.AlbumDetail(it) },
                            bottomPadding = bottomContentPadding,
                        )

                        LibraryTab.ARTISTS -> ArtistsTabContent(
                            artists = artists,
                            onSelectArtist = { activeDetail = CollectionDetail.ArtistDetail(it) },
                            bottomPadding = bottomContentPadding,
                        )

                        LibraryTab.FOLDERS -> FoldersTabContent(
                            folders = folders,
                            onSelectFolder = { activeDetail = CollectionDetail.FolderDetail(it) },
                            bottomPadding = bottomContentPadding,
                        )

                        LibraryTab.PLAYLISTS -> PlaylistsTabContent(
                            playlists = playlists,
                            favoriteCount = favoriteSongIds.size,
                            onOpenFavorites = { activeDetail = CollectionDetail.FavoritesDetail },
                            onOpenPlaylist = {
                                activeDetail = CollectionDetail.PlaylistDetail(it.id, it.name)
                            },
                            onDeletePlaylist = onDeletePlaylist,
                            bottomPadding = bottomContentPadding,
                        )
                    }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabletLeftSidebar(
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
    Column(
        modifier = Modifier
            .width(260.dp)
            .fillMaxHeight()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "平板唱片台",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                ),
            )

            val song = playerState.currentSong
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass(enabled = liquidGlassEnabled, shape = RoundedCornerShape(20.dp), elevation = 8.dp)
                    .clickable(enabled = song != null, onClick = onOpenNowPlaying)
                    .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SpinningVinylDisc(
                        albumArtUri = song?.albumArtUri,
                        rotationDegrees = 0f,
                        discSize = 144.dp,
                    )
                    Text(
                        text = song?.title ?: "暂未播放",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.SemiBold,
                        ),
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                                contentColor = Color.White,
                            ),
                            modifier = Modifier.size(46.dp),
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
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilledTonalButton(onClick = onOpenEqualizer, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Rounded.Equalizer, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("均衡器")
            }
            FilledTonalButton(onClick = onOpenSleepTimer, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Rounded.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("定时关闭")
            }
            FilledTonalButton(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Rounded.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("外观与曲库设置")
            }
        }
    }
}

@Composable
private fun SongsTabContent(
    songs: List<Song>,
    playerTheme: PlayerTheme,
    featuredAlbums: List<Album>,
    usePosterGrid: Boolean,
    gridColumns: Int,
    liquidGlassEnabled: Boolean,
    favoriteSongIds: Set<Long>,
    playerState: PlayerUiState,
    onPlaySongs: (List<Song>, Int, Boolean) -> Unit,
    onSelectFeaturedAlbum: (Album) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onRequestAddToPlaylist: (Song) -> Unit,
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
                start = 16.dp,
                end = 16.dp,
                top = 4.dp,
                bottom = 20.dp + bottomPadding.calculateBottomPadding(),
            ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
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
                            .shadow(3.dp, RoundedCornerShape(8.dp)),
                    ) {
                        AlbumArtwork(
                            uri = song.albumArtUri,
                            contentDescription = song.album,
                            modifier = Modifier.fillMaxSize(),
                            cornerRadius = 8.dp,
                            iconSize = 28.dp,
                        )
                        if (isCurrent) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(6.dp)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = if (playerState.isPlaying) Icons.Rounded.GraphicEq else Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Medium,
                        ),
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
                    end = 26.dp,
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
            start = 16.dp,
            end = 16.dp,
            top = 10.dp,
            bottom = 16.dp + bottomPadding.calculateBottomPadding(),
        ),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(albums, key = { it.id }) { album ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectAlbum(album) },
            ) {
                AlbumArtwork(
                    uri = album.albumArtUri,
                    contentDescription = album.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .shadow(3.dp, RoundedCornerShape(8.dp)),
                    cornerRadius = 8.dp,
                    iconSize = 30.dp,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = album.name,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Medium,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${album.artist} · ${album.songs.size}首",
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
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp),
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = artist.name,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${artist.albumCount} 张专辑 · ${artist.songs.size} 首歌曲",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(start = 56.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.14f),
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
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp),
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = folder.name,
                        style = MaterialTheme.typography.bodyLarge,
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
            }
            HorizontalDivider(
                modifier = Modifier.padding(start = 56.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.14f),
            )
        }
    }
}

@Composable
private fun PlaylistsTabContent(
    playlists: List<Playlist>,
    favoriteCount: Int,
    onOpenFavorites: () -> Unit,
    onOpenPlaylist: (Playlist) -> Unit,
    onDeletePlaylist: (String) -> Unit,
    bottomPadding: PaddingValues,
) {
    LazyColumn(contentPadding = bottomPadding) {
        item(key = "favorites") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenFavorites)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp),
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "我喜欢的音乐",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "$favoriteCount 首歌曲",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.14f))
        }

        items(playlists, key = { it.id }) { playlist ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenPlaylist(playlist) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.QueueMusic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp),
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = playlist.name,
                        style = MaterialTheme.typography.bodyLarge,
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
            HorizontalDivider(
                modifier = Modifier.padding(start = 56.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.14f),
            )
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
                            fontFamily = FontFamily.Serif,
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
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Button(
                            onClick = { onPlaySongs(songs, 0, false) },
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("播放全部")
                        }
                        FilledTonalButton(
                            onClick = { onPlaySongs(songs, 0, true) },
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Rounded.Shuffle, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("随机播放")
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
