package com.musica.app.data.local

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    @Volatile
    private var INSTANCE: MusicaDatabase? = null

    fun getDatabase(context: Context): MusicaDatabase {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                MusicaDatabase::class.java,
                "musica.db"
            ).build().also {
                INSTANCE = it
            }
        }
    }
}