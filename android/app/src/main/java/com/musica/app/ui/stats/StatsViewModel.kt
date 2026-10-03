package com.musica.app.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.musica.app.data.model.TopSong
import com.musica.app.data.repository.StatsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class StatsViewModel(
    private val repository: StatsRepository
) : ViewModel() {

    val top50Songs: StateFlow<List<TopSong>> =
        repository.getTop50Songs()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )
}

class StatsViewModelFactory(
    private val repository: StatsRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatsViewModel::class.java)) {
            return StatsViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}