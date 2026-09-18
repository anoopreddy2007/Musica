package com.musica.app.ui.home

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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

private data class HomeSong(
    val title: String,
    val artist: String,
    val imageUrl: String
)

private data class HomePlaylist(
    val name: String,
    val imageUrl: String
)

private val sampleSongs = listOf(
    HomeSong(
        "Apna Bana Le",
        "Arijit Singh",
        "https://i.ytimg.com/vi/ElZfdU54Cp8/hqdefault.jpg"
    ),
    HomeSong(
        "Arabic Kuthu",
        "Anirudh Ravichander",
        "https://i.ytimg.com/vi/OiC1rgCPm1E/hqdefault.jpg"
    ),
    HomeSong(
        "Zaalima",
        "Arijit Singh",
        "https://i.ytimg.com/vi/3P4Z7kK4sJk/hqdefault.jpg"
    ),
    HomeSong(
        "Infinity",
        "Jaymes Young",
        "https://i.ytimg.com/vi/PWqEPKduGm8/hqdefault.jpg"
    )
)

private val samplePlaylists = listOf(
    HomePlaylist(
        "Liked Songs",
        "https://i.ytimg.com/vi/ElZfdU54Cp8/hqdefault.jpg"
    ),
    HomePlaylist(
        "My Playlist",
        "https://i.ytimg.com/vi/OiC1rgCPm1E/hqdefault.jpg"
    ),
    HomePlaylist(
        "Workout",
        "https://i.ytimg.com/vi/PWqEPKduGm8/hqdefault.jpg"
    )
)

@Composable
fun HomeScreen() {

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
                bottom = 110.dp
            ),
            verticalArrangement = Arrangement.spacedBy(26.dp)
        ) {

            // ==========================================
            // HEADER
            // ==========================================

            item {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Column {

                        Text(
                            text = "Musica",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = "Good evening",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }

                    IconButton(
                        onClick = { }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White
                        )
                    }
                }
            }

            // ==========================================
            // KEEP LISTENING
            // ==========================================

            item {

                SectionHeader(
                    title = "Keep listening",
                    action = "See all"
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    items(sampleSongs.take(3)) { song ->

                        ListeningCard(song = song)
                    }
                }
            }

            // ==========================================
            // YOUR PLAYLISTS
            // ==========================================

            item {

                SectionHeader(
                    title = "Your playlists",
                    action = "See all"
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    items(samplePlaylists) { playlist ->

                        PlaylistCard(
                            playlist = playlist
                        )
                    }

                    item {

                        CreatePlaylistCard()
                    }
                }
            }

            // ==========================================
            // FRESH FINDS
            // ==========================================

            item {

                SectionHeader(
                    title = "Fresh finds",
                    action = "See all"
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {

                    sampleSongs.forEach { song ->

                        SongRow(song = song)
                    }
                }
            }

            // ==========================================
            // LISTEN AGAIN
            // ==========================================

            item {

                SectionHeader(
                    title = "Listen again",
                    action = "See all"
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    items(sampleSongs) { song ->

                        ListeningCard(song = song)
                    }
                }
            }
        }

        // ==========================================
        // MINI PLAYER
        // ==========================================

        MiniPlayer(
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
// SECTION HEADER
// =====================================================

@Composable
private fun SectionHeader(
    title: String,
    action: String
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = title,
            color = Color.White,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = action,
            color = Color.LightGray,
            fontSize = 13.sp
        )
    }
}

// =====================================================
// LISTENING CARD
// =====================================================

@Composable
private fun ListeningCard(
    song: HomeSong
) {

    Column(
        modifier = Modifier
            .width(145.dp)
            .clickable { }
    ) {

        AsyncImage(
            model = song.imageUrl,
            contentDescription = song.title,
            modifier = Modifier
                .size(145.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = song.title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = song.artist,
            color = Color.Gray,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// =====================================================
// PLAYLIST CARD
// =====================================================

@Composable
private fun PlaylistCard(
    playlist: HomePlaylist
) {

    Column(
        modifier = Modifier
            .width(150.dp)
            .clickable { }
    ) {

        AsyncImage(
            model = playlist.imageUrl,
            contentDescription = playlist.name,
            modifier = Modifier
                .size(150.dp)
                .clip(RoundedCornerShape(14.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = playlist.name,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// =====================================================
// CREATE PLAYLIST
// =====================================================

@Composable
private fun CreatePlaylistCard() {

    Column(
        modifier = Modifier
            .width(150.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF181818))
            .clickable { },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Create playlist",
            tint = Color.White,
            modifier = Modifier.size(38.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Create playlist",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// =====================================================
// SONG ROW
// =====================================================

@Composable
private fun SongRow(
    song: HomeSong
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        AsyncImage(
            model = song.imageUrl,
            contentDescription = song.title,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(7.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = song.title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = song.artist,
                color = Color.Gray,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(
            onClick = { }
        ) {

            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More",
                tint = Color.LightGray
            )
        }
    }
}

// =====================================================
// MINI PLAYER
// =====================================================

@Composable
private fun MiniPlayer(
    modifier: Modifier = Modifier
) {

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF181818))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        AsyncImage(
            model = sampleSongs.first().imageUrl,
            contentDescription = "Current song",
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(7.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = sampleSongs.first().title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = sampleSongs.first().artist,
                color = Color.Gray,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(
            onClick = { }
        ) {

            Icon(
                imageVector = Icons.Default.Shuffle,
                contentDescription = "Shuffle",
                tint = Color.LightGray
            )
        }

        IconButton(
            onClick = { }
        ) {

            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}