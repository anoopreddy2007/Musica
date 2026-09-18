package com.musica.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.musica.app.data.model.Song
import com.musica.app.data.repository.SearchRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val repository: SearchRepository
) : ViewModel() {

    private val _query =
        MutableStateFlow("")

    val query: StateFlow<String> =
        _query.asStateFlow()


    private val _songs =
        MutableStateFlow<List<Song>>(emptyList())

    val songs: StateFlow<List<Song>> =
        _songs.asStateFlow()


    private val _isLoading =
        MutableStateFlow(false)

    val isLoading: StateFlow<Boolean> =
        _isLoading.asStateFlow()


    private val _error =
        MutableStateFlow<String?>(null)

    val error: StateFlow<String?> =
        _error.asStateFlow()


    private var searchJob: Job? = null


    fun updateQuery(
        newQuery: String
    ) {

        _query.value = newQuery

        searchJob?.cancel()

        if (newQuery.isBlank()) {

            _songs.value = emptyList()
            _error.value = null
            _isLoading.value = false

            return
        }


        searchJob =
            viewModelScope.launch {

                delay(300)

                search(newQuery.trim())
            }
    }


    fun search(
        query: String = _query.value.trim()
    ) {

        if (query.isBlank()) {
            return
        }

        searchJob?.cancel()

        searchJob =
            viewModelScope.launch {

                _isLoading.value = true
                _error.value = null

                val result =
                    repository.searchSongs(
                        query = query,
                        limit = 20
                    )

                result
                    .onSuccess { songs ->

                        _songs.value = songs
                    }
                    .onFailure { exception ->

                        _songs.value = emptyList()

                        _error.value =
                            exception.message
                                ?: "Search failed"
                    }

                _isLoading.value = false
            }
    }


    fun clearSearch() {

        searchJob?.cancel()

        _query.value = ""
        _songs.value = emptyList()
        _error.value = null
        _isLoading.value = false
    }
}


class SearchViewModelFactory(
    private val repository: SearchRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (
            modelClass.isAssignableFrom(
                SearchViewModel::class.java
            )
        ) {

            return SearchViewModel(
                repository
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class"
        )
    }
}