package com.musica.app.data.model

data class PlaybackState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val position: Long = 0L,
    val duration: Long = 0L,
    val volume: Float = 1.0f
)