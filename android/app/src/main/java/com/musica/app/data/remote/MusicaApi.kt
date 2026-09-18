package com.musica.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface MusicaApi {

    @GET("search")
    suspend fun searchSongs(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20
    ): SearchResponse
}