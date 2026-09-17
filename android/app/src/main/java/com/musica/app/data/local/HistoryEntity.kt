package com.musica.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "play_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val videoId: String,
    val title: String,
    val artists: String,
    val album: String?,
    val duration: String?,
    val thumbnail: String?,
    val playedAt: Long = System.currentTimeMillis()
)