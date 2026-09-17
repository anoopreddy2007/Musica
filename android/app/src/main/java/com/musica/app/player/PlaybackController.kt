package com.musica.app.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

class PlaybackController(
    context: Context
) {

    private val player: ExoPlayer =
        ExoPlayer.Builder(context.applicationContext)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()

    fun play(uri: Uri) {
        val mediaItem = MediaItem.fromUri(uri)

        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    fun pause() {
        player.pause()
    }

    fun resume() {
        player.play()
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }

    fun setVolume(volume: Float) {
        player.volume = volume.coerceIn(0f, 1f)
    }

    fun isPlaying(): Boolean {
        return player.isPlaying
    }

    fun getCurrentPosition(): Long {
        return player.currentPosition
    }

    fun getDuration(): Long {
        return player.duration.coerceAtLeast(0L)
    }

    fun release() {
        player.release()
    }
}