package com.musica.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        LikedSongEntity::class,
        HistoryEntity::class,
        PlaylistEntity::class,
        PlaylistSongEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MusicaDatabase : RoomDatabase() {

    abstract fun likedSongDao(): LikedSongDao

    abstract fun historyDao(): HistoryDao

    abstract fun playlistDao(): PlaylistDao

    abstract fun playlistSongDao(): PlaylistSongDao
}