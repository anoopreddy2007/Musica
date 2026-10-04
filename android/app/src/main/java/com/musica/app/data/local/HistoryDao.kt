package com.musica.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.musica.app.data.model.TopSong
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Query("SELECT * FROM play_history ORDER BY playedAt DESC")
    fun getAll(): Flow<List<HistoryEntity>>

    @Insert
    suspend fun insert(history: HistoryEntity)

    @Query("DELETE FROM play_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM play_history")
    suspend fun deleteAll()

    @Query("""
        SELECT
            h.videoId AS videoId,

            (
                SELECT h2.title
                FROM play_history h2
                WHERE h2.videoId = h.videoId
                ORDER BY h2.playedAt DESC, h2.id DESC
                LIMIT 1
            ) AS title,

            (
                SELECT h2.artists
                FROM play_history h2
                WHERE h2.videoId = h.videoId
                ORDER BY h2.playedAt DESC, h2.id DESC
                LIMIT 1
            ) AS artists,

            (
                SELECT h2.album
                FROM play_history h2
                WHERE h2.videoId = h.videoId
                ORDER BY h2.playedAt DESC, h2.id DESC
                LIMIT 1
            ) AS album,

            (
                SELECT h2.duration
                FROM play_history h2
                WHERE h2.videoId = h.videoId
                ORDER BY h2.playedAt DESC, h2.id DESC
                LIMIT 1
            ) AS duration,

            (
                SELECT h2.thumbnail
                FROM play_history h2
                WHERE h2.videoId = h.videoId
                ORDER BY h2.playedAt DESC, h2.id DESC
                LIMIT 1
            ) AS thumbnail,

            COUNT(*) AS playCount,

            MAX(h.playedAt) AS lastPlayedAt

        FROM play_history h

        GROUP BY h.videoId

        ORDER BY
            playCount DESC,
            lastPlayedAt DESC

        LIMIT 50
    """)
    fun getTop50Songs(): Flow<List<TopSong>>
}