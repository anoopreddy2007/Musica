package com.musica.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
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
}