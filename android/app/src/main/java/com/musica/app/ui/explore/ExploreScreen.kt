package com.musica.app.ui.explore

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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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

private data class ExploreSong(
    val title: String,
    val artist: String,
    val imageUrl: String
)

private data class ExploreCategory(
    val name: String,
    val imageUrl: String
)

private val exploreSongs = listOf(
    ExploreSong(
        "Apna Bana Le",
        "Arijit Singh",
        "https://i.ytimg.com/vi/ElZfdU54Cp8/hqdefault.jpg"
    ),
    ExploreSong(
        "Arabic Kuthu",
        "Anirudh Ravichander",
        "https://i.ytimg.com/vi/OiC1rgCPm1E/hqdefault.jpg"
    ),
    ExploreSong(
        "Infinity",
        "Jaymes Young",
        "https://i.ytimg.com/vi/PWqEPKduGm8/hqdefault.jpg"
    ),
    ExploreSong(
        "Zaalima",
        "Arijit Singh",
        "https://i.ytimg.com/vi/3P4Z7kK4sJk/hqdefault.jpg"
    )
)

private val categories = listOf(
    ExploreCategory(
        "Chill",
        "https://i.ytimg.com/vi/PWqEPKduGm8/hqdefault.jpg"
    ),
    ExploreCategory(
        "Workout",
        "https://i.ytimg.com/vi/OiC1rgCPm1E/hqdefault.jpg"
    ),
    ExploreCategory(
        "Romantic",
        "https://i.ytimg.com/vi/ElZfdU54Cp8/hqdefault.jpg"
    ),
    ExploreCategory(
        "Lo-fi",
        "https://i.ytimg.com/vi/PWqEPKduGm8/hqdefault.jpg"
    )
)

@Composable
fun ExploreScreen() {

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
            verticalArrangement = Arrangement.spacedBy(26.dp)
        ) {

            // ==========================================
            // HEADER
            // ==========================================

            item {

                Text(
                    text = "Explore",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                ExploreSearchBar()
            }

            // ==========================================
            // TABS
            // ==========================================

            item {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    ExploreTab(
                        text = "Explore",
                        selected = true
                    )

                    ExploreTab(
                        text = "Suggestions",
                        selected = false
                    )

                    ExploreTab(
                        text = "Albums",
                        selected = false
                    )
                }
            }

            // ==========================================
            // FOR YOU
            // ==========================================

            item {

                SectionTitle("For you")

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    items(exploreSongs) { song ->

                        ExploreSongCard(song)
                    }
                }
            }

            // ==========================================
            // MOODS
            // ==========================================

            item {

                SectionTitle("Moods and moments")

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    items(categories) { category ->

                        CategoryCard(category)
                    }
                }
            }

            // ==========================================
            // POPULAR NOW
            // ==========================================

            item {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    SectionTitle("Popular now")

                    IconButton(
                        onClick = { }
                    ) {

                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = Color.White
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {

                    exploreSongs.forEach { song ->

                        ExploreSongRow(song)
                    }
                }
            }
        }

        // ==========================================
        // MINI PLAYER
        // ==========================================

        ExploreMiniPlayer(
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
private fun ExploreSearchBar() {

    TextField(
        value = "",
        onValueChange = { },
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        placeholder = {
            Text(
                text = "Search music",
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
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
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
// TAB
// =====================================================

@Composable
private fun ExploreTab(
    text: String,
    selected: Boolean
) {

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (selected)
                    Color.White
                else
                    Color(0xFF181818)
            )
            .clickable { }
            .padding(
                horizontal = 18.dp,
                vertical = 9.dp
            )
    ) {

        Text(
            text = text,
            color = if (selected) Color.Black else Color.LightGray,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// =====================================================
// SECTION TITLE
// =====================================================

@Composable
private fun SectionTitle(
    title: String
) {

    Text(
        text = title,
        color = Color.White,
        fontSize = 21.sp,
        fontWeight = FontWeight.Bold
    )
}

// =====================================================
// SONG CARD
// =====================================================

@Composable
private fun ExploreSongCard(
    song: ExploreSong
) {

    Column(
        modifier = Modifier
            .width(150.dp)
            .clickable { }
    ) {

        AsyncImage(
            model = song.imageUrl,
            contentDescription = song.title,
            modifier = Modifier
                .size(150.dp)
                .clip(RoundedCornerShape(14.dp)),
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
// CATEGORY CARD
// =====================================================

@Composable
private fun CategoryCard(
    category: ExploreCategory
) {

    Box(
        modifier = Modifier
            .width(190.dp)
            .height(115.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { }
    ) {

        AsyncImage(
            model = category.imageUrl,
            contentDescription = category.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
        )

        Text(
            text = category.name,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp),
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// =====================================================
// SONG ROW
// =====================================================

@Composable
private fun ExploreSongRow(
    song: ExploreSong
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
private fun ExploreMiniPlayer(
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
            model = exploreSongs.first().imageUrl,
            contentDescription = "Current song",
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
                text = exploreSongs.first().title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = exploreSongs.first().artist,
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
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}