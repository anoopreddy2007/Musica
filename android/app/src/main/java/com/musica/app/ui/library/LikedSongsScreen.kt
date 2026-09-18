package com.musica.app.ui.library

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.musica.app.data.local.DatabaseProvider
import com.musica.app.data.model.Song
import com.musica.app.data.repository.LibraryRepository

@Composable
fun LikedSongsScreen(
    navController: NavController
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    val database = DatabaseProvider.getDatabase(context)

    val repository = LibraryRepository(
        database.likedSongDao(),
        database.historyDao(),
        database.playlistDao(),
        database.playlistSongDao()
    )

    val viewModel: LibraryViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = LibraryViewModelFactory(repository)
    )

    val likedSongs by viewModel.likedSongs.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        /* HEADER */

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = { navController.popBackStack() }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Liked Songs",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall
                )

                Text(
                    text = "${likedSongs.size} songs",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        /* EMPTY STATE */

        if (likedSongs.isEmpty()) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    Text(
                        text = "No liked songs yet",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    Text(
                        text = "Songs you like will appear here",
                        color = Color.Gray
                    )
                }
            }

        } else {

            /* PLAY CONTROLS */

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clickable {
                            // Playback will be connected later
                        },
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF2A2A2A)
                ) {

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White
                        )

                        Spacer(
                            modifier = Modifier.size(8.dp)
                        )

                        Text(
                            text = "Play All",
                            color = Color.White
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clickable {
                            // Shuffle will be connected later
                        },
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF2A2A2A)
                ) {

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = null,
                            tint = Color.White
                        )

                        Spacer(
                            modifier = Modifier.size(8.dp)
                        )

                        Text(
                            text = "Shuffle",
                            color = Color.White
                        )
                    }
                }
            }

            /* SONG LIST */

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {

                items(
                    items = likedSongs,
                    key = { it.videoId }
                ) { songEntity ->

                    val song = Song(
                        videoId = songEntity.videoId,
                        title = songEntity.title,
                        artists = songEntity.artists
                            .split(", ")
                            .filter { it.isNotBlank() },
                        album = songEntity.album,
                        duration = songEntity.duration,
                        thumbnail = songEntity.thumbnail
                    )

                    LikedSongRow(
                        song = song,
                        onRemove = {
                            viewModel.unlikeSong(song.videoId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LikedSongRow(
    song: Song,
    onRemove: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // Playback will be connected later
            }
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        /* ARTWORK */

        AsyncImage(
            model = song.thumbnail,
            contentDescription = song.title,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(6.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(
            modifier = Modifier.size(12.dp)
        )

        /* SONG INFORMATION */

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = song.title,
                color = Color.White,
                maxLines = 1,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = song.artists.joinToString(", "),
                color = Color.Gray,
                maxLines = 1,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        /* LIKE */

        IconButton(
            onClick = onRemove
        ) {

            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Remove from liked songs",
                tint = Color.White
            )
        }

        /* MORE */

        IconButton(
            onClick = {
                // More menu will be added later
            }
        ) {

            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More",
                tint = Color.Gray
            )
        }
    }
}