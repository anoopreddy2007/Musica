package com.musica.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface StreamApi {

    @GET("stream/{videoId}")
    suspend fun getStream(
        @Path("videoId") videoId: String
    ): StreamResponse
}