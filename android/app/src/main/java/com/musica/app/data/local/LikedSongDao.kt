package com.musica.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LikedSongDao {

    @Query("SELECT * FROM liked_songs ORDER BY likedAt DESC")
    fun getAll(): Flow<List<LikedSongEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM liked_songs WHERE videoId = :videoId)")
    fun isLiked(videoId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(song: LikedSongEntity)

    @Delete
    suspend fun delete(song: LikedSongEntity)

    @Query("DELETE FROM liked_songs WHERE videoId = :videoId")
    suspend fun deleteByVideoId(videoId: String)

    @Query("DELETE FROM liked_songs")
    suspend fun deleteAll()
}