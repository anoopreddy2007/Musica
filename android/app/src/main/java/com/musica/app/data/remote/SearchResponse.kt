package com.musica.app.data.remote

data class SearchResponse(
    val query: String,
    val count: Int,
    val songs: List<RemoteSong>,
    val error: String? = null
)

data class RemoteSong(
    val videoId: String,
    val title: String,
    val artists: List<String> = emptyList(),
    val album: String? = null,
    val duration: String? = null,
    val thumbnail: String? = null
)