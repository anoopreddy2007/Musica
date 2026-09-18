package com.musica.app.ui.player

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.musica.app.data.extractor.AudioStreamResolver
import com.musica.app.data.model.Song
import com.musica.app.player.PlaybackController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlaybackViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val playbackController =
        PlaybackController(application)


    private val streamResolver =
        AudioStreamResolver(application)


    private val _currentSong =
        MutableStateFlow<Song?>(null)

    val currentSong: StateFlow<Song?> =
        _currentSong.asStateFlow()


    private val _isLoading =
        MutableStateFlow(false)

    val isLoading: StateFlow<Boolean> =
        _isLoading.asStateFlow()


    private val _isPlaying =
        MutableStateFlow(false)

    val isPlaying: StateFlow<Boolean> =
        _isPlaying.asStateFlow()


    private val _error =
        MutableStateFlow<String?>(null)

    val error: StateFlow<String?> =
        _error.asStateFlow()


    fun playSong(
        song: Song
    ) {

        viewModelScope.launch {

            _isLoading.value =
                true

            _error.value =
                null


            val result =
                streamResolver.resolve(
                    song
                )


            result.fold(

                onSuccess = { resolved ->

                    _currentSong.value =
                        song


                    playbackController.play(
                        Uri.parse(
                            resolved.url
                        )
                    )


                    _isPlaying.value =
                        true
                },

                onFailure = { exception ->

                    _error.value =
                        exception.message
                            ?: "Unable to extract audio"

                    _isPlaying.value =
                        false
                }
            )


            _isLoading.value =
                false
        }
    }


    fun togglePlayPause() {

        playbackController
            .togglePlayPause()

        _isPlaying.value =
            playbackController
                .isPlaying()
    }


    fun pause() {

        playbackController.pause()

        _isPlaying.value =
            false
    }


    fun resume() {

        playbackController.resume()

        _isPlaying.value =
            true
    }


    fun clearError() {

        _error.value =
            null
    }


    override fun onCleared() {

        playbackController.release()

        super.onCleared()
    }
}