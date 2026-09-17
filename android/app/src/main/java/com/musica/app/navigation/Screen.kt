package com.musica.app.navigation

sealed class Screen(
    val route: String,
    val title: String
) {
    data object Home : Screen(
        route = "home",
        title = "Home"
    )

    data object Explore : Screen(
        route = "explore",
        title = "Explore"
    )

    data object Search : Screen(
        route = "search",
        title = "Search"
    )

    data object Library : Screen(
        route = "library",
        title = "Library"
    )

    data object LikedSongs : Screen(
        route = "liked_songs",
        title = "Liked Songs"
    )

    data object History : Screen(
        route = "history",
        title = "History"
    )

    data object Stats : Screen(
        route = "stats",
        title = "Stats"
    )

    data object Playlist : Screen(
        route = "playlist/{playlistId}",
        title = "Playlist"
    )
}