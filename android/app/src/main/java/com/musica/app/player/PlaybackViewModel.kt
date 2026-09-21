package com.musica.app.ui.player

import android.app.Application
import android.content.ComponentName
import android.net.Uri

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken

import com.google.common.util.concurrent.ListenableFuture
import com.musica.app.data.extractor.AudioStreamResolver
import com.musica.app.data.model.Song
import com.musica.app.player.MusicService

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlaybackViewModel(
    application: Application
) : AndroidViewModel(application) {


    // ==========================================
    // EXTRACTOR
    // ==========================================

    private val streamResolver =
        AudioStreamResolver(application)


    // ==========================================
    // MEDIA CONTROLLER
    // ==========================================

    private var mediaController:
        MediaController? = null

    private var controllerFuture:
        ListenableFuture<MediaController>? = null


    // ==========================================
    // CURRENT SONG
    // ==========================================

    private val _currentSong =
        MutableStateFlow<Song?>(null)

    val currentSong:
        StateFlow<Song?> =
        _currentSong.asStateFlow()


    // ==========================================
    // PLAYING
    // ==========================================

    private val _isPlaying =
        MutableStateFlow(false)

    val isPlaying:
        StateFlow<Boolean> =
        _isPlaying.asStateFlow()


    // ==========================================
    // LOADING
    // ==========================================

    private val _isLoading =
        MutableStateFlow(false)

    val isLoading:
        StateFlow<Boolean> =
        _isLoading.asStateFlow()


    // ==========================================
    // ERROR
    // ==========================================

    private val _error =
        MutableStateFlow<String?>(null)

    val error:
        StateFlow<String?> =
        _error.asStateFlow()


    init {

        connectToMusicService()
    }


    // ==========================================
    // CONNECT TO MUSIC SERVICE
    // ==========================================

    private fun connectToMusicService() {

        val context =
            getApplication<Application>()

        val sessionToken =
            SessionToken(
                context,
                ComponentName(
                    context,
                    MusicService::class.java
                )
            )


        controllerFuture =
            MediaController.Builder(
                context,
                sessionToken
            )
                .buildAsync()


        controllerFuture?.addListener(

            {

                try {

                    val controller =
                        controllerFuture?.get()
                            ?: return@addListener


                    mediaController =
                        controller


                    controller.addListener(

                        object :
                            androidx.media3.common.Player.Listener {

                            override fun
                                onIsPlayingChanged(
                                    isPlaying:
                                        Boolean
                                ) {

                                _isPlaying.value =
                                    isPlaying
                            }
                        }
                    )

                } catch (
                    exception: Exception
                ) {

                    _error.value =
                        exception.message
                            ?: "Unable to connect to music player"
                }

            },

            androidx.core.content.ContextCompat
                .getMainExecutor(context)
        )
    }


    // ==========================================
    // PLAY SONG
    // ==========================================

    fun playSong(
        song: Song
    ) {

        viewModelScope.launch {

            _isLoading.value =
                true

            _error.value =
                null


            try {

                // ----------------------------------
                // MAKE SURE CONTROLLER IS READY
                // ----------------------------------

                val controller =
                    mediaController

                        ?: throw Exception(
                            "Music player is still starting"
                        )


                // ----------------------------------
                // RESOLVE AUDIO STREAM
                // ----------------------------------

                val result =
                    streamResolver.resolve(
                        song
                    )


                result.fold(

                    onSuccess = { resolved ->

                        // --------------------------
                        // MEDIA METADATA
                        // --------------------------

                        val metadata =
                            MediaMetadata.Builder()

                                .setTitle(
                                    song.title
                                )

                                .setArtist(
                                    song.artists
                                        .joinToString(", ")
                                )

                                .setAlbumTitle(
                                    song.album
                                )

                                .setArtworkUri(
                                    song.thumbnail
                                        ?.let {
                                            Uri.parse(it)
                                        }
                                )

                                .build()


                        // --------------------------
                        // MEDIA ITEM
                        // --------------------------

                        val mediaItem =
                            MediaItem.Builder()

                                .setUri(
                                    resolved.url
                                )

                                .setMediaMetadata(
                                    metadata
                                )

                                .build()


                        // --------------------------
                        // SEND TO MUSIC SERVICE
                        // --------------------------

                        controller.setMediaItem(
                            mediaItem
                        )

                        controller.prepare()

                        controller.play()


                        // --------------------------
                        // UPDATE UI STATE
                        // --------------------------

                        _currentSong.value =
                            song

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

            } catch (
                exception: Exception
            ) {

                _error.value =
                    exception.message
                        ?: "Playback failed"

                _isPlaying.value =
                    false

            } finally {

                _isLoading.value =
                    false
            }
        }
    }


    // ==========================================
    // PLAY / PAUSE
    // ==========================================

    fun togglePlayPause() {

        val controller =
            mediaController
                ?: return


        if (controller.isPlaying) {

            controller.pause()

        } else {

            controller.play()
        }
    }


    // ==========================================
    // PAUSE
    // ==========================================

    fun pause() {

        mediaController?.pause()
    }


    // ==========================================
    // RESUME
    // ==========================================

    fun resume() {

        mediaController?.play()
    }


    // ==========================================
    // SEEK
    // ==========================================

    fun seekTo(
        positionMs: Long
    ) {

        mediaController
            ?.seekTo(
                positionMs
            )
    }


    // ==========================================
    // POSITION
    // ==========================================

    fun currentPosition(): Long {

        return mediaController
            ?.currentPosition
            ?: 0L
    }


    // ==========================================
    // DURATION
    // ==========================================

    fun duration(): Long {

        return mediaController
            ?.duration
            ?.coerceAtLeast(0L)
            ?: 0L
    }


    // ==========================================
    // CLEAR ERROR
    // ==========================================

    fun clearError() {

        _error.value =
            null
    }


    // ==========================================
    // CLEANUP
    // ==========================================

    override fun onCleared() {

        mediaController?.release()

        controllerFuture = null

        mediaController = null

        super.onCleared()
    }
}