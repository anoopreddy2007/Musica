package com.musica.app.data.model

data class TopSong(
    val videoId: String,
    val title: String,
    val artists: String,
    val album: String?,
    val duration: String?,
    val thumbnail: String?,
    val playCount: Int,
    val lastPlayedAt: Long
)