package com.musica.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

import com.musica.app.ui.explore.ExploreScreen
import com.musica.app.ui.home.HomeScreen
import com.musica.app.ui.library.LibraryScreen
import com.musica.app.ui.library.LikedSongsScreen
import com.musica.app.ui.playlist.PlaylistScreen
import com.musica.app.ui.search.SearchScreen

@Composable
fun MusicaNavigation() {

    val navController = rememberNavController()

    val bottomScreens = listOf(
        Screen.Home,
        Screen.Explore,
        Screen.Search,
        Screen.Library
    )

    Scaffold(
        bottomBar = {

            NavigationBar {

                val navBackStackEntry by
                    navController.currentBackStackEntryAsState()

                val currentDestination =
                    navBackStackEntry?.destination

                bottomScreens.forEach { screen ->

                    val icon = when (screen) {

                        Screen.Home ->
                            Icons.Default.Home

                        Screen.Explore ->
                            Icons.Default.Search

                        Screen.Search ->
                            Icons.Default.Search

                        Screen.Library ->
                            Icons.Default.LibraryMusic

                        else ->
                            Icons.Default.Home
                    }

                    NavigationBarItem(

                        selected =
                            currentDestination
                                ?.hierarchy
                                ?.any {
                                    it.route == screen.route
                                } == true,

                        onClick = {

                            navController.navigate(
                                screen.route
                            ) {

                                popUpTo(
                                    navController.graph
                                        .startDestinationId
                                ) {
                                    saveState = true
                                }

                                launchSingleTop = true
                                restoreState = true
                            }
                        },

                        icon = {

                            Icon(
                                imageVector = icon,
                                contentDescription =
                                    screen.title
                            )
                        },

                        label = {

                            Text(
                                text = screen.title
                            )
                        }
                    )
                }
            }
        }

    ) { innerPadding ->

        NavHost(

            navController = navController,

            startDestination =
                Screen.Home.route,

            modifier =
                Modifier.padding(innerPadding)

        ) {

            // ==========================================
            // HOME
            // ==========================================

            composable(
                Screen.Home.route
            ) {

                HomeScreen()
            }

            // ==========================================
            // EXPLORE
            // ==========================================

            composable(
                Screen.Explore.route
            ) {

                ExploreScreen()
            }

            // ==========================================
            // SEARCH
            // ==========================================

            composable(
                Screen.Search.route
            ) {

                SearchScreen()
            }

            // ==========================================
            // LIBRARY
            // ==========================================

            composable(
                Screen.Library.route
            ) {

                LibraryScreen(
                    navController = navController
                )
            }

            // ==========================================
            // LIKED SONGS
            // ==========================================

            composable(
                Screen.LikedSongs.route
            ) {

                LikedSongsScreen(
                    navController = navController
                )
            }

            // ==========================================
            // PLAYLIST DETAIL
            // ==========================================

            composable(
                route = Screen.Playlist.route,

                arguments = listOf(
                    navArgument("playlistId") {
                        type = NavType.LongType
                    }
                )

            ) { backStackEntry ->

                val playlistId =
                    backStackEntry
                        .arguments
                        ?.getLong("playlistId")
                        ?: 0L

                PlaylistScreen(
                    playlistId = playlistId,
                    navController = navController
                )
            }
        }
    }
}