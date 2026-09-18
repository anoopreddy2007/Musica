package com.musica.app.ui.search

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

import com.musica.app.data.local.DatabaseProvider
import com.musica.app.data.model.Song
import com.musica.app.data.remote.MusicaApiClient
import com.musica.app.data.repository.LibraryRepository
import com.musica.app.data.repository.SearchRepository
import com.musica.app.ui.library.LibraryViewModel
import com.musica.app.ui.library.LibraryViewModelFactory
import com.musica.app.ui.player.PlaybackViewModel


// =====================================================
// SEARCH SCREEN
// =====================================================

@Composable
fun SearchScreen() {

    val context = LocalContext.current


    // =================================================
    // SEARCH VIEWMODEL
    // =================================================

    val searchRepository =
        SearchRepository(
            MusicaApiClient.api
        )

    val searchViewModel: SearchViewModel =
        viewModel(
            factory =
                SearchViewModelFactory(
                    searchRepository
                )
        )


    // =================================================
    // LIBRARY VIEWMODEL
    // =================================================

    val database =
        DatabaseProvider.getDatabase(context)

    val libraryRepository =
        LibraryRepository(
            database.likedSongDao(),
            database.historyDao(),
            database.playlistDao(),
            database.playlistSongDao()
        )

    val libraryViewModel: LibraryViewModel =
        viewModel(
            factory =
                LibraryViewModelFactory(
                    libraryRepository
                )
        )


    // =================================================
    // PLAYBACK VIEWMODEL
    // =================================================

    val playbackViewModel: PlaybackViewModel =
        viewModel()


    // =================================================
    // STATE
    // =================================================

    val query by
        searchViewModel.query.collectAsState()

    val songs by
        searchViewModel.songs.collectAsState()

    val isLoading by
        searchViewModel.isLoading.collectAsState()

    val error by
        searchViewModel.error.collectAsState()

    val currentSong by
        playbackViewModel.currentSong.collectAsState()

    val isPlaying by
        playbackViewModel.isPlaying.collectAsState()

    val isPlaybackLoading by
        playbackViewModel.isLoading.collectAsState()

    val playbackError by
        playbackViewModel.error.collectAsState()


    // =================================================
    // SCREEN
    // =================================================

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black)
    ) {

        LazyColumn(

            modifier =
                Modifier.fillMaxSize(),

            contentPadding =
                PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 145.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(18.dp)
        ) {


            // ==========================================
            // HEADER
            // ==========================================

            item {

                Text(
                    text = "Search",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier =
                        Modifier.height(14.dp)
                )

                SearchBar(

                    query = query,

                    onQueryChange = {
                        searchViewModel
                            .updateQuery(it)
                    },

                    onClear = {
                        searchViewModel
                            .clearSearch()
                    }
                )
            }


            // ==========================================
            // EMPTY QUERY
            // ==========================================

            if (query.isBlank()) {

                item {

                    Text(
                        text = "Recent searches",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {

                    RecentSearch(
                        text = "Arijit Singh"
                    )

                    RecentSearch(
                        text = "English hits"
                    )

                    RecentSearch(
                        text = "Workout songs"
                    )
                }

                item {

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    Text(
                        text = "Browse music",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }


            } else {


                // ======================================
                // SEARCH LOADING
                // ======================================

                if (isLoading) {

                    item {

                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        vertical = 30.dp
                                    ),

                            contentAlignment =
                                Alignment.Center
                        ) {

                            CircularProgressIndicator(
                                modifier =
                                    Modifier.size(32.dp),

                                color =
                                    Color.White
                            )
                        }
                    }
                }


                // ======================================
                // SEARCH ERROR
                // ======================================

                if (!error.isNullOrBlank()) {

                    item {

                        Column {

                            Text(
                                text =
                                    "Search failed",

                                color =
                                    Color.White,

                                fontSize =
                                    18.sp,

                                fontWeight =
                                    FontWeight.Bold
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(6.dp)
                            )

                            Text(
                                text =
                                    error
                                        ?: "Unknown error",

                                color =
                                    Color.Gray,

                                fontSize =
                                    14.sp
                            )
                        }
                    }
                }


                // ======================================
                // PLAYBACK ERROR
                // ======================================

                if (!playbackError.isNullOrBlank()) {

                    item {

                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clip(
                                        RoundedCornerShape(12.dp)
                                    )
                                    .background(
                                        Color(0xFF181818)
                                    )
                                    .padding(14.dp)
                        ) {

                            Text(
                                text =
                                    "Playback failed",

                                color =
                                    Color.White,

                                fontSize =
                                    16.sp,

                                fontWeight =
                                    FontWeight.Bold
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(5.dp)
                            )

                            Text(
                                text =
                                    playbackError
                                        ?: "Unable to play this song",

                                color =
                                    Color.Gray,

                                fontSize =
                                    13.sp
                            )
                        }
                    }
                }


                // ======================================
                // RESULTS HEADER
                // ======================================

                if (!isLoading && error == null) {

                    item {

                        Row(

                            modifier =
                                Modifier.fillMaxWidth(),

                            horizontalArrangement =
                                Arrangement.SpaceBetween,

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Text(
                                text = "Songs",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text =
                                    "${songs.size} results",

                                color =
                                    Color.Gray,

                                fontSize =
                                    13.sp
                            )
                        }
                    }
                }


                // ======================================
                // SONG RESULTS
                // ======================================

                items(

                    items = songs,

                    key = {
                        it.videoId
                    }

                ) { song ->

                    RealSearchResultRow(

                        song = song,

                        libraryViewModel =
                            libraryViewModel,

                        playbackViewModel =
                            playbackViewModel,

                        isCurrentSong =
                            currentSong?.videoId ==
                                song.videoId,

                        isPlaying =
                            isPlaying,

                        isPlaybackLoading =
                            isPlaybackLoading
                    )
                }


                // ======================================
                // NO RESULTS
                // ======================================

                if (
                    !isLoading &&
                    error == null &&
                    songs.isEmpty()
                ) {

                    item {

                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        vertical = 30.dp
                                    ),

                            contentAlignment =
                                Alignment.Center
                        ) {

                            Text(
                                text =
                                    "No songs found",

                                color =
                                    Color.Gray,

                                fontSize =
                                    15.sp
                            )
                        }
                    }
                }
            }
        }


        // =================================================
        // MINI PLAYER
        // =================================================

        SearchMiniPlayer(

            song =
                currentSong,

            isPlaying =
                isPlaying,

            isLoading =
                isPlaybackLoading,

            onPlayPause = {
                playbackViewModel
                    .togglePlayPause()
            },

            modifier =
                Modifier
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
}


// =====================================================
// SEARCH BAR
// =====================================================

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {

    TextField(

        value = query,

        onValueChange =
            onQueryChange,

        modifier =
            Modifier
                .fillMaxWidth()
                .height(54.dp),

        placeholder = {

            Text(
                text =
                    "What do you want to listen to?",

                color =
                    Color.Gray
            )
        },

        leadingIcon = {

            Icon(
                imageVector =
                    Icons.Default.Search,

                contentDescription =
                    "Search",

                tint =
                    Color.LightGray
            )
        },

        trailingIcon = {

            if (query.isNotEmpty()) {

                IconButton(
                    onClick = onClear
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Close,

                        contentDescription =
                            "Clear",

                        tint =
                            Color.LightGray
                    )
                }

            } else {

                Icon(
                    imageVector =
                        Icons.Default.Mic,

                    contentDescription =
                        "Voice search",

                    tint =
                        Color.LightGray
                )
            }
        },

        singleLine = true,

        shape =
            RoundedCornerShape(15.dp),

        colors =
            TextFieldDefaults.colors(

                focusedContainerColor =
                    Color(0xFF181818),

                unfocusedContainerColor =
                    Color(0xFF181818),

                disabledContainerColor =
                    Color(0xFF181818),

                focusedTextColor =
                    Color.White,

                unfocusedTextColor =
                    Color.White,

                cursorColor =
                    Color.White,

                focusedIndicatorColor =
                    Color.Transparent,

                unfocusedIndicatorColor =
                    Color.Transparent
            )
    )
}


// =====================================================
// RECENT SEARCH
// =====================================================

@Composable
private fun RecentSearch(
    text: String
) {

    Row(

        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { }
                .padding(
                    vertical = 12.dp
                ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Box(

            modifier =
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        Color(0xFF202020)
                    ),

            contentAlignment =
                Alignment.Center
        ) {

            Icon(
                imageVector =
                    Icons.Default.Search,

                contentDescription =
                    null,

                tint =
                    Color.LightGray,

                modifier =
                    Modifier.size(19.dp)
            )
        }

        Spacer(
            modifier =
                Modifier.width(14.dp)
        )

        Text(
            text = text,
            color = Color.White,
            fontSize = 15.sp
        )
    }
}


// =====================================================
// REAL SEARCH RESULT
// =====================================================

@Composable
private fun RealSearchResultRow(

    song: Song,

    libraryViewModel:
        LibraryViewModel,

    playbackViewModel:
        PlaybackViewModel,

    isCurrentSong:
        Boolean,

    isPlaying:
        Boolean,

    isPlaybackLoading:
        Boolean
) {

    val isLiked by
        libraryViewModel
            .isLiked(song.videoId)
            .collectAsState()


    Row(

        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(10.dp)
                )
                .background(
                    if (isCurrentSong)
                        Color(0xFF181818)
                    else
                        Color.Transparent
                )
                .clickable {

                    playbackViewModel
                        .playSong(song)
                }
                .padding(
                    vertical = 7.dp,
                    horizontal = 4.dp
                ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {


        // ==============================================
        // ARTWORK
        // ==============================================

        AsyncImage(

            model =
                song.thumbnail,

            contentDescription =
                song.title,

            modifier =
                Modifier
                    .size(62.dp)
                    .clip(
                        RoundedCornerShape(8.dp)
                    ),

            contentScale =
                ContentScale.Crop
        )


        Spacer(
            modifier =
                Modifier.width(13.dp)
        )


        // ==============================================
        // SONG INFO
        // ==============================================

        Column(

            modifier =
                Modifier.weight(1f)
        ) {

            Text(

                text =
                    song.title,

                color =
                    Color.White,

                fontSize =
                    15.sp,

                fontWeight =
                    FontWeight.SemiBold,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )

            Spacer(
                modifier =
                    Modifier.height(3.dp)
            )

            Text(

                text =
                    buildString {

                        append(
                            song.artists
                                .joinToString(", ")
                        )

                        if (
                            !song.album
                                .isNullOrBlank()
                        ) {

                            append(
                                " • "
                            )

                            append(
                                song.album
                            )
                        }
                    },

                color =
                    Color.Gray,

                fontSize =
                    13.sp,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )

            Spacer(
                modifier =
                    Modifier.height(2.dp)
            )

            if (
                !song.duration
                    .isNullOrBlank()
            ) {

                Text(

                    text =
                        song.duration!!,

                    color =
                        Color.DarkGray,

                    fontSize =
                        12.sp
                )
            }
        }


        // ==============================================
        // PLAYING / LOADING INDICATOR
        // ==============================================

        if (isCurrentSong) {

            if (isPlaybackLoading) {

                CircularProgressIndicator(

                    modifier =
                        Modifier
                            .size(22.dp)
                            .padding(2.dp),

                    color =
                        Color.White,

                    strokeWidth =
                        2.dp
                )

            } else {

                IconButton(

                    onClick = {

                        playbackViewModel
                            .togglePlayPause()
                    }

                ) {

                    Icon(

                        imageVector =
                            if (isPlaying)
                                Icons.Default.Pause
                            else
                                Icons.Default.PlayArrow,

                        contentDescription =
                            if (isPlaying)
                                "Pause"
                            else
                                "Play",

                        tint =
                            Color.White
                    )
                }
            }
        }


        // ==============================================
        // LIKE BUTTON
        // ==============================================

        IconButton(

            onClick = {

                if (isLiked) {

                    libraryViewModel
                        .unlikeSong(
                            song.videoId
                        )

                } else {

                    libraryViewModel
                        .likeSong(
                            song
                        )
                }
            }

        ) {

            Icon(

                imageVector =
                    if (isLiked) {
                        Icons.Default.Favorite
                    } else {
                        Icons.Default.FavoriteBorder
                    },

                contentDescription =
                    if (isLiked) {
                        "Unlike"
                    } else {
                        "Like"
                    },

                tint =
                    Color.White
            )
        }


        // ==============================================
        // MORE
        // ==============================================

        IconButton(

            onClick = {
                // Playlist / queue menu later
            }

        ) {

            Icon(

                imageVector =
                    Icons.Default.MoreVert,

                contentDescription =
                    "More",

                tint =
                    Color.LightGray
            )
        }
    }
}


// =====================================================
// MINI PLAYER
// =====================================================

@Composable
private fun SearchMiniPlayer(

    song: Song?,

    isPlaying: Boolean,

    isLoading: Boolean,

    onPlayPause: () -> Unit,

    modifier: Modifier = Modifier
) {

    if (song == null) {

        Row(

            modifier =
                modifier
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

            Text(

                text =
                    "Nothing playing",

                color =
                    Color.Gray,

                fontSize =
                    13.sp,

                modifier =
                    Modifier.weight(1f)
            )
        }

        return
    }


    Row(

        modifier =
            modifier
                .fillMaxWidth()
                .height(62.dp)
                .clip(
                    RoundedCornerShape(12.dp)
                )
                .background(
                    Color(0xFF181818)
                )
                .padding(
                    horizontal = 8.dp
                ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {


        // ==========================================
        // ARTWORK
        // ==========================================

        AsyncImage(

            model =
                song.thumbnail,

            contentDescription =
                song.title,

            modifier =
                Modifier
                    .size(46.dp)
                    .clip(
                        RoundedCornerShape(6.dp)
                    ),

            contentScale =
                ContentScale.Crop
        )


        Spacer(
            modifier =
                Modifier.width(10.dp)
        )


        // ==========================================
        // SONG INFO
        // ==========================================

        Column(

            modifier =
                Modifier.weight(1f)
        ) {

            Text(

                text =
                    song.title,

                color =
                    Color.White,

                fontSize =
                    13.sp,

                fontWeight =
                    FontWeight.SemiBold,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )

            Text(

                text =
                    song.artists
                        .joinToString(", "),

                color =
                    Color.Gray,

                fontSize =
                    11.sp,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )
        }


        // ==========================================
        // PLAY / PAUSE
        // ==========================================

        if (isLoading) {

            CircularProgressIndicator(

                modifier =
                    Modifier
                        .size(24.dp),

                color =
                    Color.White,

                strokeWidth =
                    2.dp
            )

        } else {

            IconButton(

                onClick =
                    onPlayPause

            ) {

                Icon(

                    imageVector =
                        if (isPlaying)
                            Icons.Default.Pause
                        else
                            Icons.Default.PlayArrow,

                    contentDescription =
                        if (isPlaying)
                            "Pause"
                        else
                            "Play",

                    tint =
                        Color.White,

                    modifier =
                        Modifier.size(30.dp)
                )
            }
        }
    }
}