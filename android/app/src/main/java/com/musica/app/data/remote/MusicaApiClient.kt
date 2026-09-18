package com.musica.app.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object MusicaApiClient {

    private const val BASE_URL =
        "https://musica-zm21.onrender.com/"

    private val httpClient =
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

    private val retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()

    val api: MusicaApi by lazy {
        retrofit.create(MusicaApi::class.java)
    }

    val streamApi: StreamApi by lazy {
        retrofit.create(StreamApi::class.java)
    }
}