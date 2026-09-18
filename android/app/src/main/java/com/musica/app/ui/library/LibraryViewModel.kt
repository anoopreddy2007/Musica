package com.musica.app.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.musica.app.data.model.Song
import com.musica.app.data.repository.LibraryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val repository: LibraryRepository
) : ViewModel() {

    // ==========================================
    // LIKED SONGS
    // ==========================================

    val likedSongs =
        repository.getLikedSongs()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    fun isLiked(videoId: String): StateFlow<Boolean> {
        return repository.isLiked(videoId)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                false
            )
    }

    fun likeSong(song: Song) {
        viewModelScope.launch {
            repository.likeSong(song)
        }
    }

    fun unlikeSong(videoId: String) {
        viewModelScope.launch {
            repository.unlikeSong(videoId)
        }
    }

    // ==========================================
    // HISTORY
    // ==========================================

    val history =
        repository.getHistory()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    fun addToHistory(song: Song) {
        viewModelScope.launch {
            repository.addToHistory(song)
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteHistoryItem(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // ==========================================
    // PLAYLISTS
    // ==========================================

    val playlists =
        repository.getPlaylists()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    fun createPlaylist(name: String) {

        val trimmedName = name.trim()

        if (trimmedName.isEmpty()) return

        viewModelScope.launch {
            repository.createPlaylist(trimmedName)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
        }
    }

    // ==========================================
    // PLAYLIST SONGS
    // ==========================================

    fun getPlaylistSongs(playlistId: Long) =
        repository.getPlaylistSongs(playlistId)

    fun addSongToPlaylist(
        playlistId: Long,
        song: Song
    ) {

        viewModelScope.launch {

            repository.addSongToPlaylist(
                playlistId = playlistId,
                song = song
            )
        }
    }

    fun removeSongFromPlaylist(
        playlistId: Long,
        videoId: String
    ) {

        viewModelScope.launch {

            repository.removeSongFromPlaylist(
                playlistId = playlistId,
                videoId = videoId
            )
        }
    }

    fun clearPlaylist(playlistId: Long) {

        viewModelScope.launch {

            repository.clearPlaylist(
                playlistId
            )
        }
    }
}

// =====================================================
// VIEWMODEL FACTORY
// =====================================================

class LibraryViewModelFactory(
    private val repository: LibraryRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (
            modelClass.isAssignableFrom(
                LibraryViewModel::class.java
            )
        ) {

            return LibraryViewModel(
                repository
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class"
        )
    }
}