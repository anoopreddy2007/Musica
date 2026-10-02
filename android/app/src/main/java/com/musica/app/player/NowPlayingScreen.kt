package com.musica.app.ui.player

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.musica.app.data.local.DatabaseProvider
import com.musica.app.data.model.Song
import com.musica.app.data.repository.LibraryRepository
import com.musica.app.ui.library.LibraryViewModel
import com.musica.app.ui.library.LibraryViewModelFactory
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    onBack: () -> Unit
) {

    // ==========================================================
    // VIEWMODELS
    // ==========================================================

    val playbackViewModel: PlaybackViewModel = viewModel()

    val context = androidx.compose.ui.platform.LocalContext.current

    val database = DatabaseProvider.getDatabase(context)

    val libraryRepository = LibraryRepository(
        database.likedSongDao(),
        database.historyDao(),
        database.playlistDao(),
        database.playlistSongDao()
    )

    val libraryViewModel: LibraryViewModel = viewModel(
        factory = LibraryViewModelFactory(libraryRepository)
    )

    // ==========================================================
    // PLAYBACK STATE
    // ==========================================================

    val song by playbackViewModel.currentSong.collectAsState()

    // Stable local value.
    // This prevents Kotlin smart-cast errors caused by
    // the delegated Compose property.
    val currentSong = song

    val isPlaying by playbackViewModel.isPlaying.collectAsState()

    val isLoading by playbackViewModel.isLoading.collectAsState()

    val queue by playbackViewModel.queue.collectAsState()

    val currentQueueIndex by playbackViewModel.currentQueueIndex.collectAsState()

    val shuffleEnabled by playbackViewModel.shuffleEnabled.collectAsState()

    val repeatMode by playbackViewModel.repeatMode.collectAsState()

    // ==========================================================
    // LIKE STATE
    // ==========================================================

    val currentSongId = currentSong?.videoId

    val isLiked by if (currentSongId != null) {

        libraryViewModel
            .isLiked(currentSongId)
            .collectAsState()

    } else {

        remember {
            mutableStateOf(false)
        }
    }

    // ==========================================================
    // QUEUE SHEET
    // ==========================================================

    var showQueue by remember {
        mutableStateOf(false)
    }

    // ==========================================================
    // POSITION
    // ==========================================================

    var position by remember {
        mutableLongStateOf(0L)
    }

    var duration by remember {
        mutableLongStateOf(0L)
    }

    // ==========================================================
    // UPDATE POSITION
    // ==========================================================

    LaunchedEffect(
        currentSong,
        isPlaying,
        isLoading
    ) {

        while (true) {

            position =
                playbackViewModel.currentPosition()

            duration =
                playbackViewModel.duration()

            delay(500)
        }
    }

    // ==========================================================
    // NOTHING PLAYING
    // ==========================================================

    if (currentSong == null) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {

            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            ) {

                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = "Nothing playing",
                color = Color.Gray,
                fontSize = 18.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        return
    }

    // ==========================================================
    // NOW PLAYING
    // ==========================================================

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {

            // ==================================================
            // TOP BAR
            // ==================================================

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = onBack
                ) {

                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        showQueue = true
                    }
                ) {

                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        contentDescription = "Queue",
                        tint = Color.White
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            // ==================================================
            // ARTWORK
            // ==================================================

            AsyncImage(
                model = currentSong.thumbnail,
                contentDescription = currentSong.title,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(330.dp),

                contentScale = ContentScale.Crop
            )

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            // ==================================================
            // SONG INFO
            // ==================================================

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = currentSong.title,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(
                        modifier = Modifier.height(5.dp)
                    )

                    Text(
                        text = currentSong.artists.joinToString(", "),
                        color = Color.Gray,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!currentSong.album.isNullOrBlank()) {

                        Text(
                            text = currentSong.album ?: "",
                            color = Color.DarkGray,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // ==================================================
                // LIKE
                // ==================================================

                IconButton(
                    onClick = {

                        if (isLiked) {

                            libraryViewModel.unlikeSong(
                                currentSong.videoId
                            )

                        } else {

                            libraryViewModel.likeSong(
                                currentSong
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

                        tint = Color.White,

                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            // ==================================================
            // SEEK BAR
            // ==================================================

            val safeDuration =
                duration.coerceAtLeast(1L)

            Slider(
                value =
                    position
                        .toFloat()
                        .coerceIn(
                            0f,
                            safeDuration.toFloat()
                        ),

                onValueChange = {
                    position = it.toLong()
                },

                onValueChangeFinished = {
                    playbackViewModel.seekTo(position)
                },

                valueRange =
                    0f..safeDuration.toFloat(),

                modifier = Modifier.fillMaxWidth()
            )

            // ==================================================
            // TIME
            // ==================================================

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = formatTime(position),
                    color = Color.Gray,
                    fontSize = 12.sp
                )

                Text(
                    text = formatTime(duration),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            // ==================================================
            // PLAYBACK CONTROLS
            // ==================================================

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // ==================================================
                // SHUFFLE
                // ==================================================

                IconButton(
                    onClick = {
                        playbackViewModel.toggleShuffle()
                    }
                ) {

                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",

                        tint =
                            if (shuffleEnabled) {
                                Color.White
                            } else {
                                Color.Gray
                            },

                        modifier = Modifier.size(25.dp)
                    )
                }

                // ==================================================
                // PREVIOUS
                // ==================================================

                IconButton(
                    onClick = {
                        playbackViewModel.previousSong()
                    }
                ) {

                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // ==================================================
                // PLAY / PAUSE
                // ==================================================

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            Color.White,
                            CircleShape
                        )
                        .clickable {
                            playbackViewModel.togglePlayPause()
                        },

                    contentAlignment = Alignment.Center
                ) {

                    if (isLoading) {

                        CircularProgressIndicator(
                            modifier = Modifier.size(26.dp),
                            color = Color.Black
                        )

                    } else {

                        Icon(
                            imageVector =
                                if (isPlaying) {
                                    Icons.Default.Pause
                                } else {
                                    Icons.Default.PlayArrow
                                },

                            contentDescription =
                                if (isPlaying) {
                                    "Pause"
                                } else {
                                    "Play"
                                },

                            tint = Color.Black,

                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                // ==================================================
                // NEXT
                // ==================================================

                IconButton(
                    onClick = {
                        playbackViewModel.nextSong()
                    }
                ) {

                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // ==================================================
                // REPEAT
                // ==================================================

                IconButton(
                    onClick = {
                        playbackViewModel.cycleRepeatMode()
                    }
                ) {

                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = "Repeat",

                        tint =
                            if (
                                repeatMode !=
                                Player.REPEAT_MODE_OFF
                            ) {
                                Color.White
                            } else {
                                Color.Gray
                            },

                        modifier = Modifier.size(25.dp)
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            // ==================================================
            // REPEAT STATUS
            // ==================================================

            Text(
                text = when (repeatMode) {

                    Player.REPEAT_MODE_ONE ->
                        "Repeat one"

                    Player.REPEAT_MODE_ALL ->
                        "Repeat queue"

                    else ->
                        ""
                },

                color = Color.Gray,
                fontSize = 12.sp,

                modifier = Modifier.align(
                    Alignment.CenterHorizontally
                )
            )
        }
    }

    // ==========================================================
    // QUEUE BOTTOM SHEET
    // ==========================================================

    if (showQueue) {

        ModalBottomSheet(

            onDismissRequest = {
                showQueue = false
            },

            sheetState =
                rememberModalBottomSheetState(
                    skipPartiallyExpanded = true
                ),

            containerColor =
                Color(0xFF101010)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
            ) {

                // ==================================================
                // QUEUE HEADER
                // ==================================================

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = "Queue",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "${queue.size} songs",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }

                    IconButton(
                        onClick = {

                            playbackViewModel.clearQueue()

                            showQueue = false
                        }
                    ) {

                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear queue",
                            tint = Color.Gray
                        )
                    }
                }

                // ==================================================
                // QUEUE CONTENT
                // ==================================================

                if (queue.isEmpty()) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(
                            text = "Your queue is empty",
                            color = Color.Gray,
                            fontSize = 15.sp
                        )
                    }

                } else {

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(420.dp),

                        verticalArrangement =
                            Arrangement.spacedBy(6.dp)
                    ) {

                        itemsIndexed(
                            queue
                        ) { index, queueSong ->

                            QueueSongRow(

                                song = queueSong,

                                isCurrent =
                                    index ==
                                    currentQueueIndex,

                                onClick = {

                                    playbackViewModel
                                        .playQueueItem(index)

                                    showQueue = false
                                },

                                onRemove = {

                                    playbackViewModel
                                        .removeFromQueue(index)
                                }
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(24.dp)
                )
            }
        }
    }
}

// =============================================================
// QUEUE ROW
// =============================================================

@Composable
private fun QueueSongRow(
    song: Song,
    isCurrent: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color =
                    if (isCurrent) {
                        Color(0xFF252525)
                    } else {
                        Color(0xFF171717)
                    },

                shape =
                    RoundedCornerShape(10.dp)
            )
            .clickable {
                onClick()
            }
            .padding(8.dp),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        // ==========================================================
        // ARTWORK
        // ==========================================================

        AsyncImage(
            model = song.thumbnail,
            contentDescription = song.title,

            modifier = Modifier
                .size(52.dp),

            contentScale =
                ContentScale.Crop
        )

        Spacer(
            modifier = Modifier.width(12.dp)
        )

        // ==========================================================
        // SONG INFORMATION
        // ==========================================================

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = song.title,
                color = Color.White,
                fontSize = 14.sp,

                fontWeight =
                    if (isCurrent) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Normal
                    },

                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text =
                    song.artists.joinToString(", "),

                color = Color.Gray,
                fontSize = 12.sp,

                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (isCurrent) {

                Text(
                    text = "Now playing",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
            }
        }

        // ==========================================================
        // REMOVE
        // ==========================================================

        IconButton(
            onClick = onRemove
        ) {

            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = Color.Gray
            )
        }
    }
}

// =============================================================
// FORMAT TIME
// =============================================================

private fun formatTime(
    milliseconds: Long
): String {

    if (milliseconds <= 0L) {
        return "0:00"
    }

    val totalSeconds =
        milliseconds / 1000L

    val minutes =
        totalSeconds / 60L

    val seconds =
        totalSeconds % 60L

    return "%d:%02d".format(
        minutes,
        seconds
    )
}