package com.musica.app.ui.stats

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musica.app.data.model.TopSong

@Composable
fun StatsScreen(
    viewModel: StatsViewModel
) {
    val topSongs by viewModel.top50Songs.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text(
            text = "Statistics",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "Your Top 50 Songs",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        if (topSongs.isEmpty()) {

            Text(
                text = "Listen to some songs to build your Top 50."
            )

        } else {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                itemsIndexed(topSongs) { index, song ->

                    TopSongRow(
                        position = index + 1,
                        song = song
                    )
                }
            }
        }
    }
}

@Composable
private fun TopSongRow(
    position: Int,
    song: TopSong
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = "$position",
            modifier = Modifier.padding(end = 16.dp),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = song.title,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = song.artists,
                fontSize = 14.sp
            )
        }

        Text(
            text = "${song.playCount} plays",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}