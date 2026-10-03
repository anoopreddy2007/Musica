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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

import com.musica.app.data.local.DatabaseProvider
import com.musica.app.data.repository.StatsRepository

import com.musica.app.ui.explore.ExploreScreen
import com.musica.app.ui.home.HomeScreen
import com.musica.app.ui.history.HistoryScreen
import com.musica.app.ui.library.LibraryScreen
import com.musica.app.ui.library.LibraryViewModel
import com.musica.app.ui.library.LibraryViewModelFactory
import com.musica.app.ui.library.LikedSongsScreen
import com.musica.app.ui.playlist.PlaylistScreen
import com.musica.app.ui.player.NowPlayingScreen
import com.musica.app.ui.search.SearchScreen
import com.musica.app.ui.stats.StatsScreen
import com.musica.app.ui.stats.StatsViewModel
import com.musica.app.ui.stats.StatsViewModelFactory


@Composable
fun MusicaNavigation() {

    val navController =
        rememberNavController()

    val context =
        LocalContext.current


    // ==========================================
    // DATABASE
    // ==========================================

    val database =
        remember {
            DatabaseProvider.getDatabase(
                context
            )
        }


    // ==========================================
    // LIBRARY REPOSITORY
    // ==========================================

    val libraryRepository =
        remember {

            com.musica.app.data.repository.LibraryRepository(

                database.likedSongDao(),

                database.historyDao(),

                database.playlistDao(),

                database.playlistSongDao()
            )
        }


    // ==========================================
    // LIBRARY VIEWMODEL
    // ==========================================

    val libraryViewModel: LibraryViewModel =
        viewModel(
            factory =
                LibraryViewModelFactory(
                    libraryRepository
                )
        )


    // ==========================================
    // STATS / TOP 50
    // ==========================================

    val statsRepository =
        remember {

            StatsRepository(
                database.historyDao()
            )
        }


    val statsViewModel: StatsViewModel =
        viewModel(
            factory =
                StatsViewModelFactory(
                    statsRepository
                )
        )


    // ==========================================
    // BOTTOM NAVIGATION
    // ==========================================

    val bottomScreens =
        listOf(
            Screen.Home,
            Screen.Explore,
            Screen.Search,
            Screen.Library
        )


    // ==========================================
    // SCAFFOLD
    // ==========================================

    Scaffold(

        bottomBar = {

            NavigationBar {

                val navBackStackEntry by
                    navController
                        .currentBackStackEntryAsState()

                val currentDestination =
                    navBackStackEntry?.destination


                bottomScreens.forEach { screen ->

                    val icon =
                        when (screen) {

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
                                    it.route ==
                                        screen.route
                                } == true,


                        onClick = {

                            navController.navigate(
                                screen.route
                            ) {

                                popUpTo(
                                    navController.graph
                                        .startDestinationId
                                ) {

                                    saveState =
                                        true
                                }

                                launchSingleTop =
                                    true

                                restoreState =
                                    true
                            }
                        },


                        icon = {

                            Icon(

                                imageVector =
                                    icon,

                                contentDescription =
                                    screen.title
                            )
                        },


                        label = {

                            Text(
                                text =
                                    screen.title
                            )
                        }
                    )
                }
            }
        }

    ) { innerPadding ->


        // ==========================================
        // NAVIGATION HOST
        // ==========================================

        NavHost(

            navController =
                navController,

            startDestination =
                Screen.Home.route,

            modifier =
                Modifier.padding(
                    innerPadding
                )

        ) {


            // ======================================
            // HOME
            // ======================================

            composable(
                Screen.Home.route
            ) {

                HomeScreen()
            }


            // ======================================
            // EXPLORE
            // ======================================

            composable(
                Screen.Explore.route
            ) {

                ExploreScreen()
            }


            // ======================================
            // SEARCH
            // ======================================

            composable(
                Screen.Search.route
            ) {

                SearchScreen(
                    navController =
                        navController
                )
            }


            // ======================================
            // LIBRARY
            // ======================================

            composable(
                Screen.Library.route
            ) {

                LibraryScreen(
                    navController =
                        navController
                )
            }


            // ======================================
            // LIKED SONGS
            // ======================================

            composable(
                Screen.LikedSongs.route
            ) {

                LikedSongsScreen(
                    navController =
                        navController
                )
            }


            // ======================================
            // HISTORY
            // ======================================

            composable(
                Screen.History.route
            ) {

                HistoryScreen(

                    libraryViewModel =
                        libraryViewModel,

                    navController =
                        navController
                )
            }


            // ======================================
            // STATS / TOP 50
            // ======================================

            composable(
                Screen.Stats.route
            ) {

                StatsScreen(

                    viewModel =
                        statsViewModel,

                    navController =
                        navController
                )
            }


            // ======================================
            // PLAYLIST DETAIL
            // ======================================

            composable(

                route =
                    Screen.Playlist.route,

                arguments =
                    listOf(

                        navArgument(
                            "playlistId"
                        ) {

                            type =
                                NavType.LongType
                        }
                    )

            ) { backStackEntry ->


                val playlistId =
                    backStackEntry
                        .arguments
                        ?.getLong(
                            "playlistId"
                        )
                        ?: 0L


                PlaylistScreen(

                    playlistId =
                        playlistId,

                    navController =
                        navController
                )
            }


            // ======================================
            // NOW PLAYING
            // ======================================

            composable(
                Screen.NowPlaying.route
            ) {

                NowPlayingScreen(

                    onBack = {

                        navController
                            .popBackStack()
                    }
                )
            }
        }
    }
}