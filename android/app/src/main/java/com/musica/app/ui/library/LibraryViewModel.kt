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

    val likedSongs: StateFlow<List<com.musica.app.data.local.LikedSongEntity>> =
        repository.getLikedSongs()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun isLiked(videoId: String): StateFlow<Boolean> {
        return repository.isLiked(videoId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = false
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

    val history: StateFlow<List<com.musica.app.data.local.HistoryEntity>> =
        repository.getHistory()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
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

    val playlists: StateFlow<List<com.musica.app.data.local.PlaylistEntity>> =
        repository.getPlaylists()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun createPlaylist(name: String) {
        if (name.trim().isEmpty()) return

        viewModelScope.launch {
            repository.createPlaylist(name)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
        }
    }

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
}

/**
 * Factory used to create LibraryViewModel
 * with the required LibraryRepository.
 */
class LibraryViewModelFactory(
    private val repository: LibraryRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(LibraryViewModel::class.java)) {
            return LibraryViewModel(repository) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class"
        )
    }
}