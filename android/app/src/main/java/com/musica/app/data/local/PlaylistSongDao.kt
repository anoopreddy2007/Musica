package com.musica.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistSongDao {

    @Query("""
        SELECT * FROM playlist_songs
        WHERE playlistId = :playlistId
        ORDER BY addedAt ASC
    """)
    fun getSongs(playlistId: Long): Flow<List<PlaylistSongEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(song: PlaylistSongEntity)

    @Query("""
        DELETE FROM playlist_songs
        WHERE playlistId = :playlistId
        AND videoId = :videoId
    """)
    suspend fun delete(playlistId: Long, videoId: String)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun deleteAllFromPlaylist(playlistId: Long)

    @Query("DELETE FROM playlist_songs")
    suspend fun deleteAll()
}