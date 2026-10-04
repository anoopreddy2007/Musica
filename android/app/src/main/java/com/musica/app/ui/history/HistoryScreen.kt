package com.musica.app.ui.history

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
import androidx.compose.foundation.lazy.items
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

import com.musica.app.data.local.HistoryEntity
import com.musica.app.data.model.Song
import com.musica.app.ui.library.LibraryViewModel
import com.musica.app.ui.player.PlaybackViewModel


@Composable
fun HistoryScreen(
    libraryViewModel: LibraryViewModel,
    navController: NavController
) {

    // ==================================================
    // HISTORY DATA
    // ==================================================

    val history by libraryViewModel.history.collectAsState()

    val playbackViewModel: PlaybackViewModel = viewModel()


    // ==================================================
    // MAIN SCREEN
    // ==================================================

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        // ==================================================
        // HEADER
        // ==================================================

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "History",
                modifier = Modifier.weight(1f),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            if (history.isNotEmpty()) {

                Button(
                    onClick = {
                        libraryViewModel.clearHistory()
                    }
                ) {
                    Text("Clear")
                }
            }
        }


        Spacer(
            modifier = Modifier.height(16.dp)
        )


        // ==================================================
        // EMPTY STATE
        // ==================================================

        if (history.isEmpty()) {

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "No listening history yet.",
                    fontSize = 16.sp
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "Songs you play will appear here."
                )
            }

        } else {

            // ==================================================
            // HISTORY LIST
            // ==================================================

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                items(
                    items = history,
                    key = { it.id }
                ) { historyItem ->

                    HistoryRow(
                        historyItem = historyItem,

                        // ==========================================
                        // PLAY HISTORY SONG
                        // ==========================================

                        onClick = {

                            val song = Song(
                                videoId = historyItem.videoId,
                                title = historyItem.title,

                                artists = historyItem.artists
                                    .split(",")
                                    .map { it.trim() }
                                    .filter { it.isNotBlank() },

                                album = historyItem.album,
                                duration = historyItem.duration,
                                thumbnail = historyItem.thumbnail
                            )

                            playbackViewModel.playSong(song)

                            navController.navigate(
                                "now_playing"
                            )
                        },

                        // ==========================================
                        // DELETE HISTORY ITEM
                        // ==========================================

                        onDelete = {
                            libraryViewModel.deleteHistoryItem(
                                historyItem.id
                            )
                        }
                    )
                }
            }
        }
    }
}


// ==========================================================
// HISTORY ROW
// ==========================================================

@Composable
private fun HistoryRow(
    historyItem: HistoryEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
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

        verticalAlignment = Alignment.CenterVertically
    ) {

        // ==================================================
        // SONG INFORMATION
        // ==================================================

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = historyItem.title,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = historyItem.artists,
                fontSize = 14.sp
            )
        }


        // ==================================================
        // DELETE BUTTON
        // ==================================================

        Button(
            onClick = onDelete
        ) {

            Text(
                text = "Delete"
            )
        }
    }
}