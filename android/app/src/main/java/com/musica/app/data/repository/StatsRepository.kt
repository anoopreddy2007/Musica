package com.musica.app.data.repository

import com.musica.app.data.local.HistoryDao
import com.musica.app.data.model.TopSong
import kotlinx.coroutines.flow.Flow

class StatsRepository(
    private val historyDao: HistoryDao
) {

    fun getTop50Songs(): Flow<List<TopSong>> {
        return historyDao.getTop50Songs()
    }
}