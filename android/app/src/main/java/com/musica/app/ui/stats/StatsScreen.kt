package com.musica.app.ui.stats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.musica.app.data.model.Song
import com.musica.app.data.model.TopSong
import com.musica.app.ui.player.PlaybackViewModel


@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    navController: NavController
) {

    val topSongs by viewModel.top50Songs.collectAsState()

    val playbackViewModel: PlaybackViewModel =
        viewModel()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        // ==========================================
        // TITLE
        // ==========================================

        Text(
            text = "Statistics",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )


        // ==========================================
        // SUBTITLE
        // ==========================================

        Text(
            text = "Your Top 50 Songs",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )


        // ==========================================
        // EMPTY STATE
        // ==========================================

        if (topSongs.isEmpty()) {

            Text(
                text = "Listen to some songs to build your Top 50."
            )

        } else {


            // ==========================================
            // CONVERT TOP SONG → SONG
            // ==========================================

            val songs =
                topSongs.map { song ->

                    Song(
                        videoId = song.videoId,

                        title = song.title,

                        artists = song.artists
                            .split(",")
                            .map {
                                it.trim()
                            }
                            .filter {
                                it.isNotBlank()
                            },

                        album = song.album,

                        duration = song.duration,

                        thumbnail = song.thumbnail
                    )
                }


            // ==========================================
            // PLAY ALL / SHUFFLE
            // ==========================================

            Row(
                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {


                // ======================================
                // PLAY ALL
                // ======================================

                Button(
                    modifier =
                        Modifier.weight(1f),

                    onClick = {

                        playbackViewModel.playSongs(
                            songs = songs,
                            shuffle = false
                        )

                        navController.navigate(
                            "now_playing"
                        )
                    }
                ) {

                    Text(
                        text = "Play All"
                    )
                }


                // ======================================
                // SHUFFLE
                // ======================================

                Button(
                    modifier =
                        Modifier.weight(1f),

                    onClick = {

                        playbackViewModel.playSongs(
                            songs = songs,
                            shuffle = true
                        )

                        navController.navigate(
                            "now_playing"
                        )
                    }
                ) {

                    Text(
                        text = "Shuffle"
                    )
                }
            }


            Spacer(
                modifier = Modifier.height(16.dp)
            )


            // ==========================================
            // TOP 50 LIST
            // ==========================================

            LazyColumn(
                modifier = Modifier.fillMaxSize(),

                verticalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                itemsIndexed(
                    topSongs
                ) { index, song ->


                    TopSongRow(
                        position = index + 1,

                        song = song,

                        onClick = {

                            // ------------------------------
                            // Convert TopSong → Song
                            // ------------------------------

                            val playableSong =
                                Song(

                                    videoId =
                                        song.videoId,

                                    title =
                                        song.title,

                                    artists =
                                        song.artists
                                            .split(",")
                                            .map {
                                                it.trim()
                                            }
                                            .filter {
                                                it.isNotBlank()
                                            },

                                    album =
                                        song.album,

                                    duration =
                                        song.duration,

                                    thumbnail =
                                        song.thumbnail
                                )


                            // ------------------------------
                            // Play selected song
                            // ------------------------------

                            playbackViewModel.playSong(
                                playableSong
                            )


                            // ------------------------------
                            // Open Now Playing
                            // ------------------------------

                            navController.navigate(
                                "now_playing"
                            )
                        }
                    )
                }
            }
        }
    }
}


// ==================================================
// TOP SONG ROW
// ==================================================

@Composable
private fun TopSongRow(
    position: Int,
    song: TopSong,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(
                vertical = 8.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {


        // ==========================================
        // RANK
        // ==========================================

        Text(
            text = "$position",

            modifier =
                Modifier.padding(
                    end = 16.dp
                ),

            fontSize = 16.sp,

            fontWeight =
                FontWeight.Bold
        )


        // ==========================================
        // SONG INFORMATION
        // ==========================================

        Column(
            modifier =
                Modifier.weight(1f)
        ) {

            Text(
                text = song.title,

                fontWeight =
                    FontWeight.SemiBold
            )

            Text(
                text = song.artists,

                fontSize = 14.sp
            )
        }


        // ==========================================
        // PLAY COUNT
        // ==========================================

        Text(
            text = "${song.playCount} plays",

            fontSize = 14.sp,

            fontWeight =
                FontWeight.Bold
        )
    }
}