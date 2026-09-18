package com.musica.app.ui.search
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

private data class SearchSong(
    val title: String,
    val artist: String,
    val album: String,
    val duration: String,
    val imageUrl: String
)

private val sampleResults = listOf(
    SearchSong(
        "Apna Bana Le",
        "Arijit Singh",
        "Bhediya",
        "4:21",
        "https://i.ytimg.com/vi/ElZfdU54Cp8/hqdefault.jpg"
    ),
    SearchSong(
        "Arabic Kuthu",
        "Anirudh Ravichander",
        "Beast",
        "4:39",
        "https://i.ytimg.com/vi/OiC1rgCPm1E/hqdefault.jpg"
    ),
    SearchSong(
        "Infinity",
        "Jaymes Young",
        "Feel Something",
        "3:57",
        "https://i.ytimg.com/vi/PWqEPKduGm8/hqdefault.jpg"
    ),
    SearchSong(
        "Zaalima",
        "Arijit Singh",
        "Raees",
        "5:05",
        "https://i.ytimg.com/vi/3P4Z7kK4sJk/hqdefault.jpg"
    )
)

@Composable
fun SearchScreen() {

    var query by remember {
        mutableStateOf("")
    }

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
            verticalArrangement = Arrangement.spacedBy(18.dp)
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

                Spacer(modifier = Modifier.height(14.dp))

                SearchBar(
                    query = query,
                    onQueryChange = {
                        query = it
                    }
                )
            }

            // ==========================================
            // RECENT SEARCHES
            // ==========================================

            if (query.isEmpty()) {

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

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Browse music",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

            } else {

                // ==========================================
                // RESULTS
                // ==========================================

                item {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = "Songs",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "${sampleResults.size} results",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                }

                items(sampleResults) { song ->

                    SearchResultRow(
                        song = song
                    )
                }
            }
        }

        // ==========================================
        // MINI PLAYER
        // ==========================================

        SearchMiniPlayer(
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
// SEARCH BAR
// =====================================================

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {

    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        placeholder = {
            Text(
                text = "What do you want to listen to?",
                color = Color.Gray
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.LightGray
            )
        },
        trailingIcon = {

            if (query.isNotEmpty()) {

                IconButton(
                    onClick = {
                        onQueryChange("")
                    }
                ) {

                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = Color.LightGray
                    )
                }

            } else {

                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice search",
                    tint = Color.LightGray
                )
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(15.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF181818),
            unfocusedContainerColor = Color(0xFF181818),
            disabledContainerColor = Color(0xFF181818),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
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
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF202020)),
            contentAlignment = Alignment.Center
        ) {

            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(19.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = text,
            color = Color.White,
            fontSize = 15.sp
        )
    }
}

// =====================================================
// SEARCH RESULT
// =====================================================

@Composable
private fun SearchResultRow(
    song: SearchSong
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        AsyncImage(
            model = song.imageUrl,
            contentDescription = song.title,
            modifier = Modifier
                .size(62.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(13.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = song.title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = "${song.artist} • ${song.album}",
                color = Color.Gray,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = song.duration,
                color = Color.DarkGray,
                fontSize = 12.sp
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
private fun SearchMiniPlayer(
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
            model = sampleResults.first().imageUrl,
            contentDescription = null,
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(7.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = sampleResults.first().title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = sampleResults.first().artist,
                color = Color.Gray,
                fontSize = 12.sp
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