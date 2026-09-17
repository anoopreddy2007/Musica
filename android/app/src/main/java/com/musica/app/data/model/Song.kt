package com.musica.app.data.model

data class Song(
    val videoId: String,
    val title: String,
    val artists: List<String> = emptyList(),
    val album: String? = null,
    val duration: String? = null,
    val thumbnail: String? = null
)