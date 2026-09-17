package com.musica.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "liked_songs")
data class LikedSongEntity(
    @PrimaryKey
    val videoId: String,
    val title: String,
    val artists: String,
    val album: String?,
    val duration: String?,
    val thumbnail: String?,
    val likedAt: Long = System.currentTimeMillis()
)