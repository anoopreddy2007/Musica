package com.musica.app.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.ArtTrack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

import com.musica.app.data.local.DatabaseProvider
import com.musica.app.data.repository.LibraryRepository

private data class LibraryAction(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String
)

private val actions = listOf(
    LibraryAction(
        title = "Liked Songs",
        subtitle = "Your favorite music",
        icon = Icons.Default.Favorite,
        route = "liked_songs"
    ),
    LibraryAction(
        title = "History",
        subtitle = "Recently played",
        icon = Icons.Default.History,
        route = "history"
    ),
    LibraryAction(
        title = "Stats",
        subtitle = "Your listening statistics",
        icon = Icons.Default.QueryStats,
        route = "stats"
    )
)

@Composable
fun LibraryScreen(
    navController: NavController
) {

    val context = androidx.compose.ui.platform.LocalContext.current

    val database = DatabaseProvider.getDatabase(context)

    val repository = LibraryRepository(
        likedSongDao = database.likedSongDao(),
        historyDao = database.historyDao(),
        playlistDao = database.playlistDao(),
        playlistSongDao = database.playlistSongDao()
    )

    val viewModel = androidx.lifecycle.viewmodel.compose.viewModel<LibraryViewModel>(
        factory = LibraryViewModelFactory(repository)
    )

    val playlists by viewModel.playlists.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),

            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 145.dp
            ),

            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // ==========================================
            // HEADER
            // ==========================================

            item {

                Row(
                    modifier = Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.SpaceBetween,

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Text(
                        text = "Library",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = { }
                    ) {

                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Library options",
                            tint = Color.White
                        )
                    }
                }
            }

            // ==========================================
            // QUICK ACTIONS
            // ==========================================

            item {

                LazyRow(
                    horizontalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    items(actions) { action ->

                        LibraryActionCard(
                            action = action,
                            onClick = {

                                navController.navigate(
                                    action.route
                                )
                            }
                        )
                    }
                }
            }

            // ==========================================
            // FILTER TABS
            // ==========================================

            item {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(
                            rememberScrollState()
                        ),

                    horizontalArrangement =
                        Arrangement.spacedBy(9.dp)
                ) {

                    LibraryTab(
                        text = "Playlists",
                        selected = true
                    )

                    LibraryTab(
                        text = "Songs",
                        selected = false
                    )

                    LibraryTab(
                        text = "Albums",
                        selected = false
                    )

                    LibraryTab(
                        text = "Artists",
                        selected = false
                    )

                    LibraryTab(
                        text = "Local",
                        selected = false
                    )
                }
            }

            // ==========================================
            // PLAYLIST HEADER
            // ==========================================

            item {

                Row(
                    modifier = Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.SpaceBetween,

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Text(
                        text = "Playlists",
                        color = Color.White,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = {
                            viewModel.createPlaylist(
                                "New Playlist"
                            )
                        }
                    ) {

                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription =
                                "Create playlist",
                            tint = Color.White
                        )
                    }
                }
            }

            // ==========================================
            // REAL ROOM PLAYLISTS
            // ==========================================

            if (playlists.isEmpty()) {

                item {

                    EmptyPlaylistCard(
                        onClick = {
                            viewModel.createPlaylist(
                                "New Playlist"
                            )
                        }
                    )
                }

            } else {

                items(
                    items = playlists,
                    key = { playlist ->
                        playlist.id
                    }
                ) { playlist ->

                    PlaylistRow(
                        playlistId = playlist.id,
                        playlistName = playlist.name,
                        onClick = {

                            navController.navigate(
                                "playlist/${playlist.id}"
                            )
                        }
                    )
                }
            }

            // ==========================================
            // YOUR MUSIC
            // ==========================================

            item {

                Text(
                    text = "Your music",
                    color = Color.White,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {

                MusicCategoryGrid(
                    onSongsClick = { },
                    onAlbumsClick = { },
                    onArtistsClick = { },
                    onLocalClick = { }
                )
            }
        }

        // ==========================================
        // MINI PLAYER
        // ==========================================

        LibraryMiniPlayer(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    start = 10.dp,
                    end = 10.dp,
                    bottom = 8.dp
                )
        )
    }
}

// =====================================================
// ACTION CARD
// =====================================================

@Composable
private fun LibraryActionCard(
    action: LibraryAction,
    onClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .width(155.dp)
            .height(100.dp)
            .clip(
                RoundedCornerShape(14.dp)
            )
            .background(
                Color(0xFF181818)
            )
            .clickable {
                onClick()
            }
            .padding(14.dp),

        verticalArrangement =
            Arrangement.SpaceBetween
    ) {

        Icon(
            imageVector = action.icon,
            contentDescription = action.title,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )

        Column {

            Text(
                text = action.title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            Text(
                text = action.subtitle,
                color = Color.Gray,
                fontSize = 11.sp
            )
        }
    }
}

// =====================================================
// TAB
// =====================================================

@Composable
private fun LibraryTab(
    text: String,
    selected: Boolean
) {

    Box(
        modifier = Modifier
            .clip(
                RoundedCornerShape(20.dp)
            )
            .background(
                if (selected) {
                    Color.White
                } else {
                    Color(0xFF181818)
                }
            )
            .clickable { }
            .padding(
                horizontal = 17.dp,
                vertical = 9.dp
            )
    ) {

        Text(
            text = text,

            color =
                if (selected) {
                    Color.Black
                } else {
                    Color.LightGray
                },

            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// =====================================================
// PLAYLIST ROW
// =====================================================

@Composable
private fun PlaylistRow(
    playlistId: Long,
    playlistName: String,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(10.dp)
            )
            .clickable {
                onClick()
            }
            .padding(
                vertical = 6.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(
                    RoundedCornerShape(9.dp)
                )
                .background(
                    Color(0xFF202020)
                ),

            contentAlignment =
                Alignment.Center
        ) {

            Icon(
                imageVector =
                    Icons.Default.LibraryMusic,

                contentDescription =
                    "Playlist",

                tint =
                    Color.LightGray,

                modifier =
                    Modifier.size(28.dp)
            )
        }

        Spacer(
            modifier = Modifier.width(13.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = playlistName,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(
                modifier = Modifier.height(3.dp)
            )

            Text(
                text = "Playlist",
                color = Color.Gray,
                fontSize = 13.sp
            )
        }

        IconButton(
            onClick = { }
        ) {

            Icon(
                imageVector =
                    Icons.Default.MoreVert,

                contentDescription =
                    "Playlist options",

                tint =
                    Color.LightGray
            )
        }
    }
}

// =====================================================
// EMPTY PLAYLIST
// =====================================================

@Composable
private fun EmptyPlaylistCard(
    onClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(14.dp)
            )
            .background(
                Color(0xFF181818)
            )
            .clickable {
                onClick()
            }
            .padding(24.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Icon(
            imageVector =
                Icons.Default.LibraryMusic,

            contentDescription = null,

            tint =
                Color.LightGray,

            modifier =
                Modifier.size(34.dp)
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        Text(
            text = "No playlists yet",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Text(
            text = "Tap here to create your first playlist",
            color = Color.Gray,
            fontSize = 13.sp
        )
    }
}

// =====================================================
// MUSIC CATEGORY GRID
// =====================================================

@Composable
private fun MusicCategoryGrid(
    onSongsClick: () -> Unit,
    onAlbumsClick: () -> Unit,
    onArtistsClick: () -> Unit,
    onLocalClick: () -> Unit
) {

    Column(
        verticalArrangement =
            Arrangement.spacedBy(10.dp)
    ) {

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            MusicCategory(
                title = "Songs",
                icon = Icons.Default.MusicNote,
                modifier = Modifier.weight(1f),
                onClick = onSongsClick
            )

            MusicCategory(
                title = "Albums",
                icon = Icons.Default.Album,
                modifier = Modifier.weight(1f),
                onClick = onAlbumsClick
            )
        }

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            MusicCategory(
                title = "Artists",
                icon = Icons.Default.Person,
                modifier = Modifier.weight(1f),
                onClick = onArtistsClick
            )

            MusicCategory(
                title = "Local",
                icon = Icons.Default.ArtTrack,
                modifier = Modifier.weight(1f),
                onClick = onLocalClick
            )
        }
    }
}

// =====================================================
// MUSIC CATEGORY
// =====================================================

@Composable
private fun MusicCategory(
    title: String,
    icon: ImageVector,
    modifier: Modifier,
    onClick: () -> Unit
) {

    Row(
        modifier = modifier
            .height(72.dp)
            .clip(
                RoundedCornerShape(13.dp)
            )
            .background(
                Color(0xFF181818)
            )
            .clickable {
                onClick()
            }
            .padding(
                horizontal = 15.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Color.LightGray,
            modifier = Modifier.size(25.dp)
        )

        Spacer(
            modifier = Modifier.width(12.dp)
        )

        Text(
            text = title,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// =====================================================
// MINI PLAYER
// =====================================================

@Composable
private fun LibraryMiniPlayer(
    modifier: Modifier = Modifier
) {

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(
                RoundedCornerShape(12.dp)
            )
            .background(
                Color(0xFF181818)
            )
            .padding(
                horizontal = 10.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(
                    RoundedCornerShape(7.dp)
                )
                .background(
                    Color(0xFF292929)
                ),

            contentAlignment =
                Alignment.Center
        ) {

            Icon(
                imageVector =
                    Icons.Default.MusicNote,

                contentDescription =
                    null,

                tint =
                    Color.LightGray
            )
        }

        Spacer(
            modifier = Modifier.width(10.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = "Nothing playing",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Choose a song to start listening",
                color = Color.Gray,
                fontSize = 11.sp
            )
        }

        IconButton(
            onClick = { }
        ) {

            Icon(
                imageVector =
                    Icons.Default.PlayArrow,

                contentDescription =
                    "Play",

                tint =
                    Color.White,

                modifier =
                    Modifier.size(30.dp)
            )
        }
    }
}