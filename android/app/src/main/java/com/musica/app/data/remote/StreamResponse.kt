package com.musica.app.data.remote

data class StreamResponse(
    val url: String? = null,
    val title: String? = null,
    val duration: Int? = null,
    val ext: String? = null,
    val error: String? = null
)