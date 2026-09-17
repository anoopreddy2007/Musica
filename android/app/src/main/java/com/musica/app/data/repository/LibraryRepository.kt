package com.musica.app.data.repository

import com.musica.app.data.local.HistoryDao
import com.musica.app.data.local.HistoryEntity
import com.musica.app.data.local.LikedSongDao
import com.musica.app.data.local.LikedSongEntity
import com.musica.app.data.local.PlaylistDao
import com.musica.app.data.local.PlaylistEntity
import com.musica.app.data.local.PlaylistSongDao
import com.musica.app.data.local.PlaylistSongEntity
import com.musica.app.data.model.Song
import kotlinx.coroutines.flow.Flow

class LibraryRepository(
    private val likedSongDao: LikedSongDao,
    private val historyDao: HistoryDao,
    private val playlistDao: PlaylistDao,
    private val playlistSongDao: PlaylistSongDao
) {

    // ==========================================
    // LIKED SONGS
    // ==========================================

    fun getLikedSongs(): Flow<List<LikedSongEntity>> {
        return likedSongDao.getAll()
    }

    fun isLiked(videoId: String): Flow<Boolean> {
        return likedSongDao.isLiked(videoId)
    }

    suspend fun likeSong(song: Song) {
        likedSongDao.insert(
            LikedSongEntity(
                videoId = song.videoId,
                title = song.title,
                artists = song.artists.joinToString(", "),
                album = song.album,
                duration = song.duration,
                thumbnail = song.thumbnail
            )
        )
    }

    suspend fun unlikeSong(videoId: String) {
        likedSongDao.deleteByVideoId(videoId)
    }

    suspend fun clearLikedSongs() {
        likedSongDao.deleteAll()
    }

    // ==========================================
    // HISTORY
    // ==========================================

    fun getHistory(): Flow<List<HistoryEntity>> {
        return historyDao.getAll()
    }

    suspend fun addToHistory(song: Song) {
        historyDao.insert(
            HistoryEntity(
                videoId = song.videoId,
                title = song.title,
                artists = song.artists.joinToString(", "),
                album = song.album,
                duration = song.duration,
                thumbnail = song.thumbnail
            )
        )
    }

    suspend fun deleteHistoryItem(id: Long) {
        historyDao.deleteById(id)
    }

    suspend fun clearHistory() {
        historyDao.deleteAll()
    }

    // ==========================================
    // PLAYLISTS
    // ==========================================

    fun getPlaylists(): Flow<List<PlaylistEntity>> {
        return playlistDao.getAll()
    }

    suspend fun createPlaylist(name: String): Long {
        return playlistDao.insert(
            PlaylistEntity(
                name = name.trim()
            )
        )
    }

    suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.delete(playlistId)
    }

    suspend fun addSongToPlaylist(
        playlistId: Long,
        song: Song
    ) {
        playlistSongDao.insert(
            PlaylistSongEntity(
                playlistId = playlistId,
                videoId = song.videoId,
                title = song.title,
                artists = song.artists.joinToString(", "),
                album = song.album,
                duration = song.duration,
                thumbnail = song.thumbnail
            )
        )
    }

    fun getPlaylistSongs(
        playlistId: Long
    ): Flow<List<PlaylistSongEntity>> {
        return playlistSongDao.getSongs(playlistId)
    }

    suspend fun removeSongFromPlaylist(
        playlistId: Long,
        videoId: String
    ) {
        playlistSongDao.delete(
            playlistId = playlistId,
            videoId = videoId
        )
    }

    suspend fun clearPlaylist(
        playlistId: Long
    ) {
        playlistSongDao.deleteAllFromPlaylist(playlistId)
    }
}