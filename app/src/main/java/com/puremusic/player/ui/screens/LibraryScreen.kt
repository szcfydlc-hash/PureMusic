package com.puremusic.player.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.MusicOff
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.puremusic.player.data.model.Album
import com.puremusic.player.data.model.Artist
import com.puremusic.player.data.model.LibraryTab
import com.puremusic.player.data.model.MusicFolder
import com.puremusic.player.data.model.PlayerUiState
import com.puremusic.player.data.model.Playlist
import com.puremusic.player.data.model.Song
import com.puremusic.player.data.model.SortOrder
import com.puremusic.player.ui.components.AlbumArtwork
import com.puremusic.player.ui.components.SongRowItem
import com.puremusic.player.ui.components.formatCountdown

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
    sleepTimerRemainingMs: Long?,
    onSearchQueryChange: (String) -> Unit,
    onSelectSortOrder: (SortOrder) -> Unit,
    onRefreshLibrary: () -> Unit,
    onPlaySongs: (List<Song>, Int, Boolean) -> Unit,
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
    var searchActive by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    BackHandler(enabled = activeDetail != null || searchActive) {
        if (activeDetail != null) {
            activeDetail = null
        } else {
            searchActive = false
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
        topBar = {
            if (searchActive) {
                TopAppBar(
                    title = {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = { Text("搜索歌曲、歌手或专辑") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                searchActive = false
                                onSearchQueryChange("")
                            },
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "关闭搜索")
                        }
                    },
                    actions = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Rounded.Close, contentDescription = "清空")
                            }
                        }
                    },
                )
            } else {
                TopAppBar(
                    title = {
                        Text(
                            text = "简约音乐",
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    actions = {
                        if (sleepTimerRemainingMs != null) {
                            AssistChip(
                                onClick = onOpenSleepTimer,
                                label = { Text(formatCountdown(sleepTimerRemainingMs)) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Rounded.Timer,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
                                },
                            )
                        }
                        IconButton(onClick = { searchActive = true }) {
                            Icon(Icons.Rounded.Search, contentDescription = "搜索")
                        }
                        Box {
                            IconButton(onClick = { sortMenuExpanded = true }) {
                                Icon(Icons.AutoMirrored.Rounded.Sort, contentDescription = "排序")
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
                        IconButton(onClick = onOpenEqualizer) {
                            Icon(Icons.Rounded.Equalizer, contentDescription = "均衡器")
                        }
                        IconButton(onClick = onOpenSleepTimer) {
                            Icon(Icons.Rounded.Timer, contentDescription = "定时关闭")
                        }
                        IconButton(onClick = onOpenThemeSettings) {
                            Icon(Icons.Rounded.Palette, contentDescription = "主题设置")
                        }
                    },
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == LibraryTab.PLAYLISTS) {
                FloatingActionButton(
                    onClick = onRequestCreatePlaylist,
                    modifier = Modifier.padding(bottomContentPadding),
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "新建歌单")
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            PrimaryScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                edgePadding = 12.dp,
            ) {
                LibraryTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { Text(tab.label) },
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (selectedTab) {
                    LibraryTab.SONGS -> SongsTabContent(
                        songs = songs,
                        favoriteSongIds = favoriteSongIds,
                        playerState = playerState,
                        onPlaySongs = onPlaySongs,
                        onToggleFavorite = onToggleFavorite,
                        onRequestAddToPlaylist = onRequestAddToPlaylist,
                        onRefresh = onRefreshLibrary,
                        bottomPadding = bottomContentPadding,
                    )

                    LibraryTab.ALBUMS -> AlbumsTabContent(
                        albums = albums,
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

@Composable
private fun SongsTabContent(
    songs: List<Song>,
    favoriteSongIds: Set<Long>,
    playerState: PlayerUiState,
    onPlaySongs: (List<Song>, Int, Boolean) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onRequestAddToPlaylist: (Song) -> Unit,
    onRefresh: () -> Unit,
    bottomPadding: PaddingValues,
) {
    if (songs.isEmpty()) {
        EmptyLibraryPlaceholder(onRefresh = onRefresh)
        return
    }

    LazyColumn(contentPadding = bottomPadding) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledTonalButton(
                    onClick = { onPlaySongs(songs, 0, true) },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Rounded.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("随机播放 (${songs.size})")
                }
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Rounded.Refresh, contentDescription = "重新扫描本地音乐")
                }
            }
        }
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
}

@Composable
private fun AlbumsTabContent(
    albums: List<Album>,
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
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(
            start = 12.dp,
            end = 12.dp,
            top = 12.dp,
            bottom = 12.dp + bottomPadding.calculateBottomPadding(),
        ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(albums, key = { it.id }) { album ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectAlbum(album) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    AlbumArtwork(
                        uri = album.albumArtUri,
                        contentDescription = album.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        cornerRadius = 12.dp,
                        iconSize = 40.dp,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = album.name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${album.artist} · ${album.songs.size} 首",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
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
                    modifier = Modifier.size(28.dp),
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
            HorizontalDivider(modifier = Modifier.padding(start = 58.dp))
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
                    modifier = Modifier.size(28.dp),
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
            HorizontalDivider(modifier = Modifier.padding(start = 58.dp))
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
                    modifier = Modifier.size(28.dp),
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
            HorizontalDivider()
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
                    modifier = Modifier.size(28.dp),
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
            HorizontalDivider(modifier = Modifier.padding(start = 58.dp))
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
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = detail.title,
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
        )
        Spacer(modifier = Modifier.height(16.dp))
        FilledTonalButton(onClick = onRefresh) {
            Icon(Icons.Rounded.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("重新扫描")
        }
    }
}
