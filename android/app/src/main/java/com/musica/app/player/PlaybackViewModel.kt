package com.musica.app.ui.player

import android.app.Application
import android.content.ComponentName
import android.net.Uri

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken

import com.google.common.util.concurrent.ListenableFuture
import com.musica.app.data.extractor.AudioStreamResolver
import com.musica.app.data.local.DatabaseProvider
import com.musica.app.data.model.Song
import com.musica.app.data.repository.LibraryRepository
import com.musica.app.player.MusicService

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class PlaybackViewModel(
    application: Application
) : AndroidViewModel(application) {

    // ==========================================================
    // EXTRACTOR
    // ==========================================================

    private val streamResolver =
        AudioStreamResolver(application)


    // ==========================================================
    // LIBRARY / HISTORY
    // ==========================================================

    private val libraryRepository =
        DatabaseProvider
            .getDatabase(application)
            .let { database ->

                LibraryRepository(
                    database.likedSongDao(),
                    database.historyDao(),
                    database.playlistDao(),
                    database.playlistSongDao()
                )
            }


    // ==========================================================
    // MEDIA CONTROLLER
    // ==========================================================

    private var mediaController: MediaController? = null

    private var controllerFuture:
        ListenableFuture<MediaController>? = null


    // ==========================================================
    // CURRENT SONG
    // ==========================================================

    private val _currentSong =
        MutableStateFlow<Song?>(null)

    val currentSong:
        StateFlow<Song?> =
        _currentSong.asStateFlow()


    // ==========================================================
    // PLAYING
    // ==========================================================

    private val _isPlaying =
        MutableStateFlow(false)

    val isPlaying:
        StateFlow<Boolean> =
        _isPlaying.asStateFlow()


    // ==========================================================
    // LOADING
    // ==========================================================

    private val _isLoading =
        MutableStateFlow(false)

    val isLoading:
        StateFlow<Boolean> =
        _isLoading.asStateFlow()


    // ==========================================================
    // ERROR
    // ==========================================================

    private val _error =
        MutableStateFlow<String?>(null)

    val error:
        StateFlow<String?> =
        _error.asStateFlow()


    // ==========================================================
    // QUEUE
    // ==========================================================

    private val _queue =
        MutableStateFlow<List<Song>>(emptyList())

    val queue:
        StateFlow<List<Song>> =
        _queue.asStateFlow()


    // ==========================================================
    // CURRENT QUEUE INDEX
    // ==========================================================

    private val _currentQueueIndex =
        MutableStateFlow(-1)

    val currentQueueIndex:
        StateFlow<Int> =
        _currentQueueIndex.asStateFlow()


    // ==========================================================
    // SHUFFLE
    // ==========================================================

    private val _shuffleEnabled =
        MutableStateFlow(false)

    val shuffleEnabled:
        StateFlow<Boolean> =
        _shuffleEnabled.asStateFlow()


    // ==========================================================
    // REPEAT
    // ==========================================================

    private val _repeatMode =
        MutableStateFlow(Player.REPEAT_MODE_OFF)

    val repeatMode:
        StateFlow<Int> =
        _repeatMode.asStateFlow()


    // ==========================================================
    // INITIALIZE
    // ==========================================================

    init {
        connectToMusicService()
    }


    // ==========================================================
    // CONNECT TO MUSIC SERVICE
    // ==========================================================

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

                    // Restore current playback state.

                    restoreCurrentSong(
                        controller
                    )

                    updateQueue(
                        controller
                    )

                    _isPlaying.value =
                        controller.isPlaying

                    _shuffleEnabled.value =
                        controller.shuffleModeEnabled

                    _repeatMode.value =
                        controller.repeatMode

                    _currentQueueIndex.value =
                        controller.currentMediaItemIndex


                    // Player listener.

                    controller.addListener(

                        object :
                            Player.Listener {

                            override fun
                                onIsPlayingChanged(
                                    isPlaying: Boolean
                                ) {

                                _isPlaying.value =
                                    isPlaying
                            }


                            override fun
                                onMediaItemTransition(
                                    mediaItem: MediaItem?,
                                    reason: Int
                                ) {

                                restoreCurrentSong(
                                    controller
                                )

                                updateQueue(
                                    controller
                                )

                                _currentQueueIndex.value =
                                    controller.currentMediaItemIndex
                            }


                            override fun
                                onMediaMetadataChanged(
                                    mediaMetadata: MediaMetadata
                                ) {

                                restoreCurrentSong(
                                    controller
                                )

                                updateQueue(
                                    controller
                                )
                            }


                            override fun
                                onTimelineChanged(
                                    timeline:
                                        androidx.media3.common.Timeline,
                                    reason: Int
                                ) {

                                updateQueue(
                                    controller
                                )

                                _currentQueueIndex.value =
                                    controller.currentMediaItemIndex
                            }


                            override fun
                                onShuffleModeEnabledChanged(
                                    shuffleModeEnabled: Boolean
                                ) {

                                _shuffleEnabled.value =
                                    shuffleModeEnabled
                            }


                            override fun
                                onRepeatModeChanged(
                                    repeatMode: Int
                                ) {

                                _repeatMode.value =
                                    repeatMode
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
                .getMainExecutor(
                    context
                )
        )
    }


    // ==========================================================
    // RESTORE CURRENT SONG
    // ==========================================================

    private fun restoreCurrentSong(
        controller: MediaController
    ) {

        val mediaItem =
            controller.currentMediaItem
                ?: return

        val videoId =
            mediaItem.mediaId
                .takeIf {
                    it.isNotBlank()
                }
                ?: return

        val metadata =
            mediaItem.mediaMetadata

        val title =
            metadata.title
                ?.toString()
                ?: "Unknown title"

        val artistText =
            metadata.artist
                ?.toString()
                ?: ""

        val artists =
            if (artistText.isBlank()) {

                emptyList()

            } else {

                artistText
                    .split(",")
                    .map {
                        it.trim()
                    }
                    .filter {
                        it.isNotBlank()
                    }
            }

        val album =
            metadata.albumTitle
                ?.toString()
                ?.takeIf {
                    it.isNotBlank()
                }

        val thumbnail =
            metadata.artworkUri
                ?.toString()

        _currentSong.value =
            Song(
                videoId = videoId,
                title = title,
                artists = artists,
                album = album,
                duration = null,
                thumbnail = thumbnail
            )
    }


    // ==========================================================
    // CONVERT MEDIA ITEM → SONG
    // ==========================================================

    private fun mediaItemToSong(
        mediaItem: MediaItem
    ): Song? {

        val videoId =
            mediaItem.mediaId
                .takeIf {
                    it.isNotBlank()
                }
                ?: return null

        val metadata =
            mediaItem.mediaMetadata

        val title =
            metadata.title
                ?.toString()
                ?: "Unknown title"

        val artistText =
            metadata.artist
                ?.toString()
                ?: ""

        val artists =
            if (artistText.isBlank()) {

                emptyList()

            } else {

                artistText
                    .split(",")
                    .map {
                        it.trim()
                    }
                    .filter {
                        it.isNotBlank()
                    }
            }

        return Song(
            videoId = videoId,
            title = title,
            artists = artists,
            album =
                metadata.albumTitle
                    ?.toString()
                    ?.takeIf {
                        it.isNotBlank()
                    },
            duration = null,
            thumbnail =
                metadata.artworkUri
                    ?.toString()
        )
    }


    // ==========================================================
    // UPDATE QUEUE
    // ==========================================================

    private fun updateQueue(
        controller: MediaController
    ) {

        val songs =
            mutableListOf<Song>()

        for (
            index in 0 until controller.mediaItemCount
        ) {

            val mediaItem =
                controller.getMediaItemAt(index)

            val song =
                mediaItemToSong(
                    mediaItem
                )

            if (song != null) {
                songs.add(song)
            }
        }

        _queue.value =
            songs

        _currentQueueIndex.value =
            controller.currentMediaItemIndex
    }


    // ==========================================================
    // CREATE MEDIA ITEM
    // ==========================================================

    private fun createMediaItem(
        song: Song,
        url: String
    ): MediaItem {

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

        return MediaItem.Builder()

            .setMediaId(
                song.videoId
            )

            .setUri(
                url
            )

            .setMediaMetadata(
                metadata
            )

            .build()
    }


    // ==========================================================
    // PLAY SONG
    // ==========================================================

    fun playSong(
        song: Song
    ) {

        viewModelScope.launch {

            _isLoading.value =
                true

            _error.value =
                null

            try {

                val controller =
                    mediaController
                        ?: throw Exception(
                            "Music player is still starting"
                        )

                val result =
                    streamResolver.resolve(
                        song
                    )

                result.fold(

                    onSuccess = { resolved ->

                        val mediaItem =
                            createMediaItem(
                                song,
                                resolved.url
                            )

                        controller.setMediaItem(
                            mediaItem
                        )

                        controller.prepare()

                        controller.play()

                        _currentSong.value =
                            song

                        _isPlaying.value =
                            true

                        _currentQueueIndex.value =
                            0

                        libraryRepository
                            .addToHistory(
                                song
                            )

                        updateQueue(
                            controller
                        )
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


    // ==========================================================
    // PLAY MULTIPLE SONGS
    // ==========================================================

    fun playSongs(
        songs: List<Song>,
        shuffle: Boolean = false
    ) {

        viewModelScope.launch {

            if (songs.isEmpty()) {
                return@launch
            }

            _isLoading.value =
                true

            _error.value =
                null

            try {

                val controller =
                    mediaController
                        ?: throw Exception(
                            "Music player is still starting"
                        )

                val orderedSongs =
                    if (shuffle) {
                        songs.shuffled()
                    } else {
                        songs
                    }

                val mediaItems =
                    mutableListOf<MediaItem>()

                val successfullyResolvedSongs =
                    mutableListOf<Song>()

                for (song in orderedSongs) {

                    try {

                        val result =
                            streamResolver.resolve(
                                song
                            )

                        result.fold(

                            onSuccess = { resolved ->

                                mediaItems.add(
                                    createMediaItem(
                                        song,
                                        resolved.url
                                    )
                                )

                                successfullyResolvedSongs.add(
                                    song
                                )
                            },

                            onFailure = {
                                // Skip songs that fail.
                            }
                        )

                    } catch (_: Exception) {
                        // Continue with remaining songs.
                    }
                }

                if (mediaItems.isEmpty()) {

                    throw Exception(
                        "Unable to load songs"
                    )
                }

                controller.shuffleModeEnabled =
                    false

                _shuffleEnabled.value =
                    false

                controller.setMediaItems(
                    mediaItems,
                    0,
                    0L
                )

                controller.prepare()

                controller.play()

                _currentSong.value =
                    successfullyResolvedSongs.first()

                _currentQueueIndex.value =
                    0

                _isPlaying.value =
                    true

                libraryRepository
                    .addToHistory(
                        successfullyResolvedSongs.first()
                    )

                updateQueue(
                    controller
                )

            } catch (
                exception: Exception
            ) {

                _error.value =
                    exception.message
                        ?: "Unable to play songs"

                _isPlaying.value =
                    false

            } finally {

                _isLoading.value =
                    false
            }
        }
    }


    // ==========================================================
    // ADD TO QUEUE
    // ==========================================================

    fun addToQueue(
        song: Song
    ) {

        viewModelScope.launch {

            _error.value =
                null

            try {

                val controller =
                    mediaController
                        ?: throw Exception(
                            "Music player is still starting"
                        )

                _isLoading.value =
                    true

                val result =
                    streamResolver.resolve(
                        song
                    )

                result.fold(

                    onSuccess = { resolved ->

                        val mediaItem =
                            createMediaItem(
                                song,
                                resolved.url
                            )

                        controller.addMediaItem(
                            mediaItem
                        )

                        updateQueue(
                            controller
                        )
                    },

                    onFailure = { exception ->

                        _error.value =
                            exception.message
                                ?: "Unable to add song to queue"
                    }
                )

            } catch (
                exception: Exception
            ) {

                _error.value =
                    exception.message
                        ?: "Unable to add song to queue"

            } finally {

                _isLoading.value =
                    false
            }
        }
    }


    // ==========================================================
    // PLAY NEXT
    // ==========================================================

    fun playNext(
        song: Song
    ) {

        viewModelScope.launch {

            try {

                val controller =
                    mediaController
                        ?: return@launch

                val result =
                    streamResolver.resolve(
                        song
                    )

                result.fold(

                    onSuccess = { resolved ->

                        val mediaItem =
                            createMediaItem(
                                song,
                                resolved.url
                            )

                        val nextIndex =
                            if (
                                controller.currentMediaItemIndex >= 0
                            ) {
                                controller.currentMediaItemIndex + 1
                            } else {
                                0
                            }

                        controller.addMediaItem(
                            nextIndex,
                            mediaItem
                        )

                        updateQueue(
                            controller
                        )
                    },

                    onFailure = { exception ->

                        _error.value =
                            exception.message
                                ?: "Unable to add song"
                    }
                )

            } catch (
                exception: Exception
            ) {

                _error.value =
                    exception.message
                        ?: "Unable to add song"
            }
        }
    }


    // ==========================================================
    // PLAY QUEUE ITEM
    // ==========================================================

    fun playQueueItem(
        index: Int
    ) {

        val controller =
            mediaController
                ?: return

        if (
            index < 0 ||
            index >= controller.mediaItemCount
        ) {
            return
        }

        controller.seekToDefaultPosition(
            index
        )

        controller.play()

        _currentQueueIndex.value =
            index

        restoreCurrentSong(
            controller
        )
    }


    // ==========================================================
    // REORDER QUEUE
    // ==========================================================

    fun reorderQueue(
        fromIndex: Int,
        toIndex: Int
    ) {

        val controller =
            mediaController
                ?: return

        if (
            fromIndex < 0 ||
            fromIndex >= controller.mediaItemCount ||
            toIndex < 0 ||
            toIndex >= controller.mediaItemCount ||
            fromIndex == toIndex
        ) {
            return
        }

        val currentIndex =
            controller.currentMediaItemIndex

        // Never move the currently playing song.

        if (
            fromIndex == currentIndex ||
            toIndex == currentIndex
        ) {
            return
        }

        // Keep current song fixed.
        // Items before current can only move within
        // the "before current" section.
        // Items after current can only move within
        // the "after current" section.

        val fromIsBeforeCurrent =
            currentIndex >= 0 &&
                fromIndex < currentIndex

        val toIsBeforeCurrent =
            currentIndex >= 0 &&
                toIndex < currentIndex

        if (
            currentIndex >= 0 &&
            fromIsBeforeCurrent != toIsBeforeCurrent
        ) {
            return
        }

        controller.moveMediaItem(
            fromIndex,
            toIndex
        )

        updateQueue(
            controller
        )
    }


    // ==========================================================
    // SHUFFLE UPCOMING QUEUE
    // ==========================================================

    fun shuffleUpcomingQueue() {

        val controller =
            mediaController
                ?: return

        val currentIndex =
            controller.currentMediaItemIndex

        if (
            currentIndex < 0 ||
            currentIndex >= controller.mediaItemCount - 1
        ) {
            return
        }

        val upcomingCount =
            controller.mediaItemCount -
                currentIndex -
                1

        if (upcomingCount < 2) {
            return
        }

        // Queue shuffle is separate from the normal
        // Media3 shuffle mode.
        //
        // We physically randomize the upcoming items,
        // so disable Media3 shuffle to make Next follow
        // this new physical order.

        controller.shuffleModeEnabled =
            false

        _shuffleEnabled.value =
            false

        val originalIndices =
            (currentIndex + 1 until controller.mediaItemCount)
                .toList()

        // FIX:
        // Make this list mutable because we use
        // removeAt() and add() below.
        val shuffledIndices =
            originalIndices
                .shuffled()
                .toMutableList()

        // GUARANTEE a different order when possible.

        if (
            shuffledIndices == originalIndices &&
            shuffledIndices.size > 1
        ) {

            val first =
                shuffledIndices.removeAt(0)

            shuffledIndices.add(first)
        }

        // Track which original queue item currently
        // occupies each position.

        val currentOriginalOrder =
            originalIndices.toMutableList()

        for (
            targetPosition
            in originalIndices.indices
        ) {

            val targetIndex =
                currentIndex +
                    1 +
                    targetPosition

            val desiredOriginalIndex =
                shuffledIndices[targetPosition]

            val currentPosition =
                currentOriginalOrder.indexOf(
                    desiredOriginalIndex
                )

            if (
                currentPosition >= 0 &&
                currentPosition != targetPosition
            ) {

                val fromMediaIndex =
                    currentIndex +
                        1 +
                        currentPosition

                controller.moveMediaItem(
                    fromMediaIndex,
                    targetIndex
                )

                val movedOriginalIndex =
                    currentOriginalOrder
                        .removeAt(
                            currentPosition
                        )

                currentOriginalOrder.add(
                    targetPosition,
                    movedOriginalIndex
                )
            }
        }

        updateQueue(
            controller
        )
    }


    // ==========================================================
    // REMOVE FROM QUEUE
    // ==========================================================

    fun removeFromQueue(
        index: Int
    ) {

        val controller =
            mediaController
                ?: return

        if (
            index < 0 ||
            index >= controller.mediaItemCount
        ) {
            return
        }

        // Protect current song.

        if (
            index ==
            controller.currentMediaItemIndex
        ) {
            return
        }

        controller.removeMediaItem(
            index
        )

        updateQueue(
            controller
        )

        if (
            controller.mediaItemCount == 0
        ) {

            _currentSong.value =
                null

            _currentQueueIndex.value =
                -1

            _isPlaying.value =
                false
        }
    }


    // ==========================================================
    // CLEAR QUEUE
    // ==========================================================

    fun clearQueue() {

        val controller =
            mediaController
                ?: return

        val currentIndex =
            controller.currentMediaItemIndex

        if (
            currentIndex < 0 ||
            currentIndex >= controller.mediaItemCount
        ) {
            return
        }

        val currentMediaItem =
            controller.getMediaItemAt(
                currentIndex
            )

        val wasPlaying =
            controller.isPlaying

        val currentPosition =
            controller.currentPosition

        controller.clearMediaItems()

        controller.setMediaItem(
            currentMediaItem
        )

        controller.seekTo(
            currentPosition
        )

        controller.prepare()

        if (wasPlaying) {
            controller.play()
        }

        _currentSong.value =
            mediaItemToSong(
                currentMediaItem
            )

        _queue.value =
            listOfNotNull(
                mediaItemToSong(
                    currentMediaItem
                )
            )

        _currentQueueIndex.value =
            0

        _isPlaying.value =
            wasPlaying
    }


    // ==========================================================
    // PREVIOUS
    // ==========================================================

    fun previousSong() {

        val controller =
            mediaController
                ?: return

        if (
            controller.currentPosition >
            3_000L
        ) {

            controller.seekTo(
                0L
            )

            return
        }

        if (
            controller.hasPreviousMediaItem()
        ) {

            controller.seekToPreviousMediaItem()

        } else {

            controller.seekTo(
                0L
            )
        }
    }


    // ==========================================================
    // NEXT
    // ==========================================================

    fun nextSong() {

        val controller =
            mediaController
                ?: return

        if (
            controller.hasNextMediaItem()
        ) {

            controller.seekToNextMediaItem()

        } else {

            controller.pause()
        }
    }


    // ==========================================================
    // GLOBAL SHUFFLE
    // ==========================================================

    fun toggleShuffle() {

        val controller =
            mediaController
                ?: return

        controller.shuffleModeEnabled =
            !controller.shuffleModeEnabled

        _shuffleEnabled.value =
            controller.shuffleModeEnabled
    }


    // ==========================================================
    // REPEAT
    // ==========================================================

    fun cycleRepeatMode() {

        val controller =
            mediaController
                ?: return

        val nextMode =
            when (
                controller.repeatMode
            ) {

                Player.REPEAT_MODE_OFF ->
                    Player.REPEAT_MODE_ALL

                Player.REPEAT_MODE_ALL ->
                    Player.REPEAT_MODE_ONE

                else ->
                    Player.REPEAT_MODE_OFF
            }

        controller.repeatMode =
            nextMode

        _repeatMode.value =
            nextMode
    }


    // ==========================================================
    // PLAY / PAUSE
    // ==========================================================

    fun togglePlayPause() {

        val controller =
            mediaController
                ?: return

        if (
            controller.isPlaying
        ) {

            controller.pause()

        } else {

            controller.play()
        }
    }


    // ==========================================================
    // PAUSE
    // ==========================================================

    fun pause() {

        mediaController?.pause()
    }


    // ==========================================================
    // RESUME
    // ==========================================================

    fun resume() {

        mediaController?.play()
    }


    // ==========================================================
    // SEEK
    // ==========================================================

    fun seekTo(
        positionMs: Long
    ) {

        mediaController
            ?.seekTo(
                positionMs
            )
    }


    // ==========================================================
    // POSITION
    // ==========================================================

    fun currentPosition(): Long {

        return mediaController
            ?.currentPosition
            ?: 0L
    }


    // ==========================================================
    // DURATION
    // ==========================================================

    fun duration(): Long {

        return mediaController
            ?.duration
            ?.coerceAtLeast(0L)
            ?: 0L
    }


    // ==========================================================
    // CLEAR ERROR
    // ==========================================================

    fun clearError() {

        _error.value =
            null
    }


    // ==========================================================
    // CLEANUP
    // ==========================================================

    override fun onCleared() {

        mediaController?.release()

        controllerFuture = null

        mediaController = null

        super.onCleared()
    }
}