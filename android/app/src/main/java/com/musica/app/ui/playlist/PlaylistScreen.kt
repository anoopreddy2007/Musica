package com.musica.app.ui.playlist

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage

import androidx.compose.ui.platform.LocalContext
import com.musica.app.data.local.DatabaseProvider
import com.musica.app.data.repository.LibraryRepository
import com.musica.app.ui.library.LibraryViewModel
import com.musica.app.ui.library.LibraryViewModelFactory

@Composable
fun PlaylistScreen(
    playlistId: Long,
    navController: NavController
) {

    val context = LocalContext.current

    val database =
        DatabaseProvider.getDatabase(context)

    val repository =
        LibraryRepository(
            likedSongDao = database.likedSongDao(),
            historyDao = database.historyDao(),
            playlistDao = database.playlistDao(),
            playlistSongDao = database.playlistSongDao()
        )

    val viewModel: LibraryViewModel = viewModel(
        factory = LibraryViewModelFactory(
            repository
        )
    )

    val playlists by viewModel.playlists.collectAsState()

    val playlist =
        playlists.firstOrNull {
            it.id == playlistId
        }

    val songs by viewModel
        .getPlaylistSongs(playlistId)
        .collectAsState(initial = emptyList())

    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    // ==========================================
    // PLAYLIST NO LONGER EXISTS
    // ==========================================

    if (playlist == null) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),

            contentAlignment = Alignment.Center
        ) {

            Column(
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Icon(
                    imageVector =
                        Icons.Default.LibraryMusic,

                    contentDescription = null,

                    tint = Color.Gray,

                    modifier =
                        Modifier.size(48.dp)
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text(
                    text = "Playlist not found",
                    color = Color.White,
                    fontSize = 18.sp
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                TextButton(
                    onClick = {
                        navController.popBackStack()
                    }
                ) {

                    Text(
                        text = "Go back"
                    )
                }
            }
        }

        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),

            contentPadding =
                PaddingValues(
                    bottom = 145.dp
                )
        ) {

            // ==========================================
            // HEADER
            // ==========================================

            item {

                PlaylistHeader(
                    playlistName =
                        playlist.name,

                    songCount =
                        songs.size,

                    onBack = {
                        navController.popBackStack()
                    },

                    onDelete = {
                        showDeleteDialog = true
                    }
                )
            }

            // ==========================================
            // ACTIONS
            // ==========================================

            item {

                PlaylistActions(
                    hasSongs = songs.isNotEmpty()
                )
            }

            // ==========================================
            // SONG HEADER
            // ==========================================

            item {

                Text(
                    text = "Songs",

                    modifier = Modifier.padding(
                        start = 16.dp,
                        top = 10.dp,
                        bottom = 8.dp
                    ),

                    color = Color.White,

                    fontSize = 20.sp,

                    fontWeight =
                        FontWeight.Bold
                )
            }

            // ==========================================
            // EMPTY PLAYLIST
            // ==========================================

            if (songs.isEmpty()) {

                item {

                    EmptyPlaylist(
                        onAddSongs = {
                            // Search/add-song flow
                            // will be connected later.
                        }
                    )
                }

            } else {

                // ==========================================
                // SONGS
                // ==========================================

                itemsIndexed(
                    items = songs,
                    key = { _, song ->
                        song.videoId
                    }
                ) { index, song ->

                    PlaylistSongRow(
                        index = index + 1,
                        title = song.title,
                        artists = song.artists,
                        album = song.album,
                        duration = song.duration,
                        thumbnail = song.thumbnail,
                        onRemove = {

                            viewModel.removeSongFromPlaylist(
                                playlistId =
                                    playlistId,

                                videoId =
                                    song.videoId
                            )
                        }
                    )
                }

                // ==========================================
                // ADD SONG
                // ==========================================

                item {

                    AddSongsRow(
                        onClick = {
                            // Search/add-song flow
                            // will be connected later.
                        }
                    )
                }
            }
        }

        // ==========================================
        // MINI PLAYER
        // ==========================================

        PlaylistMiniPlayer(
            modifier = Modifier
                .align(
                    Alignment.BottomCenter
                )
                .padding(
                    start = 10.dp,
                    end = 10.dp,
                    bottom = 8.dp
                )
        )
    }

    // ==========================================
    // DELETE CONFIRMATION
    // ==========================================

    if (showDeleteDialog) {

        AlertDialog(

            onDismissRequest = {
                showDeleteDialog = false
            },

            title = {

                Text(
                    text = "Delete playlist?"
                )
            },

            text = {

                Text(
                    text =
                        "\"${playlist.name}\" will be permanently deleted."
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        viewModel.deletePlaylist(
                            playlistId
                        )

                        showDeleteDialog = false

                        navController.popBackStack()
                    }
                ) {

                    Text(
                        text = "Delete",
                        color = Color(0xFFFF6B6B)
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {

                    Text(
                        text = "Cancel"
                    )
                }
            }
        )
    }
}

// =====================================================
// HEADER
// =====================================================

@Composable
private fun PlaylistHeader(
    playlistName: String,
    songCount: Int,
    onBack: () -> Unit,
    onDelete: () -> Unit
) {

    Column {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color(0xFF181818)
                    ),

                contentAlignment =
                    Alignment.Center
            ) {

                Icon(
                    imageVector =
                        Icons.Default.LibraryMusic,

                    contentDescription = null,

                    tint = Color.DarkGray,

                    modifier =
                        Modifier.size(90.dp)
                )
            }

            IconButton(
                onClick = onBack,

                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
            ) {

                Icon(
                    imageVector =
                        Icons.Default.ArrowBack,

                    contentDescription =
                        "Back",

                    tint = Color.White,

                    modifier =
                        Modifier.size(28.dp)
                )
            }

            IconButton(
                onClick = onDelete,

                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
            ) {

                Icon(
                    imageVector =
                        Icons.Default.Delete,

                    contentDescription =
                        "Delete playlist",

                    tint = Color.White,

                    modifier =
                        Modifier.size(25.dp)
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {

                Text(
                    text = playlistName,

                    color = Color.White,

                    fontSize = 30.sp,

                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text = "Your playlist",

                    color = Color.LightGray,

                    fontSize = 14.sp
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text =
                        "$songCount ${if (songCount == 1) "song" else "songs"}",

                    color = Color.Gray,

                    fontSize = 13.sp
                )
            }
        }
    }
}

// =====================================================
// ACTIONS
// =====================================================

@Composable
private fun PlaylistActions(
    hasSongs: Boolean
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 16.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Row(
            modifier = Modifier
                .clip(
                    androidx.compose.foundation.shape
                        .RoundedCornerShape(25.dp)
                )
                .background(
                    if (hasSongs)
                        Color.White
                    else
                        Color(0xFF303030)
                )
                .clickable(enabled = hasSongs) { }
                .padding(
                    horizontal = 20.dp,
                    vertical = 11.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Icon(
                imageVector =
                    Icons.Default.PlayArrow,

                contentDescription =
                    "Play all",

                tint =
                    if (hasSongs)
                        Color.Black
                    else
                        Color.Gray
            )

            Spacer(
                modifier = Modifier.width(7.dp)
            )

            Text(
                text = "Play all",

                color =
                    if (hasSongs)
                        Color.Black
                    else
                        Color.Gray,

                fontSize = 14.sp,

                fontWeight =
                    FontWeight.Bold
            )
        }

        Spacer(
            modifier = Modifier.width(10.dp)
        )

        IconButton(
            onClick = { },
            enabled = hasSongs
        ) {

            Icon(
                imageVector =
                    Icons.Default.Shuffle,

                contentDescription =
                    "Shuffle",

                tint =
                    if (hasSongs)
                        Color.White
                    else
                        Color.DarkGray,

                modifier =
                    Modifier.size(27.dp)
            )
        }
    }
}

// =====================================================
// EMPTY PLAYLIST
// =====================================================

@Composable
private fun EmptyPlaylist(
    onAddSongs: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 30.dp,
                vertical = 45.dp
            ),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Icon(
            imageVector =
                Icons.Default.MusicNote,

            contentDescription = null,

            tint = Color.DarkGray,

            modifier =
                Modifier.size(55.dp)
        )

        Spacer(
            modifier = Modifier.height(14.dp)
        )

        Text(
            text = "No songs yet",

            color = Color.White,

            fontSize = 18.sp,

            fontWeight =
                FontWeight.SemiBold
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Text(
            text =
                "Add songs to start building this playlist.",

            color = Color.Gray,

            fontSize = 13.sp
        )

        Spacer(
            modifier = Modifier.height(18.dp)
        )

        Row(
            modifier = Modifier
                .clip(
                    androidx.compose.foundation.shape
                        .RoundedCornerShape(22.dp)
                )
                .background(Color.White)
                .clickable {
                    onAddSongs()
                }
                .padding(
                    horizontal = 18.dp,
                    vertical = 10.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Icon(
                imageVector =
                    Icons.Default.Add,

                contentDescription =
                    "Add songs",

                tint = Color.Black
            )

            Spacer(
                modifier = Modifier.width(7.dp)
            )

            Text(
                text = "Add songs",

                color = Color.Black,

                fontSize = 14.sp,

                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}

// =====================================================
// SONG ROW
// =====================================================

@Composable
private fun PlaylistSongRow(
    index: Int,
    title: String,
    artists: String,
    album: String?,
    duration: String?,
    thumbnail: String?,
    onRemove: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 7.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Text(
            text = index.toString(),

            color = Color.Gray,

            fontSize = 13.sp,

            modifier =
                Modifier.width(24.dp)
        )

        AsyncImage(
            model = thumbnail,

            contentDescription = title,

            modifier = Modifier
                .size(58.dp)
                .clip(
                    androidx.compose.foundation.shape
                        .RoundedCornerShape(7.dp)
                ),

            contentScale =
                ContentScale.Crop
        )

        Spacer(
            modifier = Modifier.width(12.dp)
        )

        Column(
            modifier =
                Modifier.weight(1f)
        ) {

            Text(
                text = title,

                color = Color.White,

                fontSize = 15.sp,

                fontWeight =
                    FontWeight.SemiBold,

                maxLines = 1,

                overflow =
                    TextOverflow.Ellipsis
            )

            Spacer(
                modifier = Modifier.height(3.dp)
            )

            Text(
                text =
                    if (album.isNullOrBlank())
                        artists
                    else
                        "$artists • $album",

                color = Color.Gray,

                fontSize = 12.sp,

                maxLines = 1,

                overflow =
                    TextOverflow.Ellipsis
            )
        }

        if (!duration.isNullOrBlank()) {

            Text(
                text = duration,

                color = Color.Gray,

                fontSize = 12.sp
            )
        }

        IconButton(
            onClick = onRemove
        ) {

            Icon(
                imageVector =
                    Icons.Default.MoreVert,

                contentDescription =
                    "Song options",

                tint =
                    Color.LightGray
            )
        }
    }
}

// =====================================================
// ADD SONGS
// =====================================================

@Composable
private fun AddSongsRow(
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(
                horizontal = 18.dp,
                vertical = 18.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(
                    RoundedCornerShape(10.dp)
                )
                .background(
                    Color(0xFF181818)
                ),

            contentAlignment =
                Alignment.Center
        ) {

            Icon(
                imageVector =
                    Icons.Default.Add,

                contentDescription =
                    "Add songs",

                tint = Color.White
            )
        }

        Spacer(
            modifier = Modifier.width(14.dp)
        )

        Text(
            text = "Add songs",

            color = Color.White,

            fontSize = 15.sp,

            fontWeight =
                FontWeight.Medium
        )
    }
}

// =====================================================
// MINI PLAYER
// =====================================================

@Composable
private fun PlaylistMiniPlayer(
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

                contentDescription = null,

                tint = Color.LightGray
            )
        }

        Spacer(
            modifier = Modifier.width(10.dp)
        )

        Column(
            modifier =
                Modifier.weight(1f)
        ) {

            Text(
                text = "Nothing playing",

                color = Color.White,

                fontSize = 14.sp,

                fontWeight =
                    FontWeight.SemiBold
            )

            Text(
                text =
                    "Choose a song to start listening",

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

                tint = Color.White,

                modifier =
                    Modifier.size(30.dp)
            )
        }
    }
}