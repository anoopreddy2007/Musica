package com.musica.app.data.local

import androidx.room.Entity

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "videoId"]
)
data class PlaylistSongEntity(
    val playlistId: Long,
    val videoId: String,
    val title: String,
    val artists: String,
    val album: String?,
    val duration: String?,
    val thumbnail: String?,
    val addedAt: Long = System.currentTimeMillis()
)