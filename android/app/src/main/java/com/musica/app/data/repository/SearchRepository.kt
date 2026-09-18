package com.musica.app.data.repository

import com.musica.app.data.model.Song
import com.musica.app.data.remote.MusicaApi

class SearchRepository(
    private val api: MusicaApi
) {

    suspend fun searchSongs(
        query: String,
        limit: Int = 20
    ): Result<List<Song>> {

        return try {

            val response = api.searchSongs(
                query = query,
                limit = limit
            )

            if (!response.error.isNullOrBlank()) {

                Result.failure(
                    Exception(response.error)
                )

            } else {

                val songs = response.songs.map { remoteSong ->

                    Song(
                        videoId = remoteSong.videoId,
                        title = remoteSong.title,
                        artists = remoteSong.artists,
                        album = remoteSong.album,
                        duration = remoteSong.duration,
                        thumbnail = remoteSong.thumbnail
                    )
                }

                Result.success(songs)
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }
}