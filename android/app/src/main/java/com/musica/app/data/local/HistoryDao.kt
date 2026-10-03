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
            h.title AS title,
            h.artists AS artists,
            h.album AS album,
            h.duration AS duration,
            h.thumbnail AS thumbnail,
            COUNT(*) AS playCount,
            MAX(h.playedAt) AS lastPlayedAt
        FROM play_history h
        GROUP BY
            h.videoId,
            h.title,
            h.artists,
            h.album,
            h.duration,
            h.thumbnail
        ORDER BY
            playCount DESC,
            lastPlayedAt DESC
        LIMIT 50
    """)
    fun getTop50Songs(): Flow<List<TopSong>>
}