package com.musica.app.player

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.musica.app.MainActivity

class MusicService : MediaSessionService() {

    private lateinit var player: ExoPlayer

    private lateinit var mediaSession: MediaSession


    override fun onCreate() {

        super.onCreate()


        // ==========================================
        // EXOPLAYER
        // ==========================================

        player =
            ExoPlayer.Builder(this)

                .setAudioAttributes(

                    AudioAttributes.Builder()

                        .setUsage(
                            C.USAGE_MEDIA
                        )

                        .setContentType(
                            C.AUDIO_CONTENT_TYPE_MUSIC
                        )

                        .build(),

                    true
                )

                .setHandleAudioBecomingNoisy(
                    true
                )

                .build()


        // ==========================================
        // OPEN APP FROM NOTIFICATION
        // ==========================================

        val sessionActivity =
            PendingIntent.getActivity(

                this,

                0,

                Intent(
                    this,
                    MainActivity::class.java
                ),

                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
            )


        // ==========================================
        // MEDIA SESSION
        // ==========================================

        mediaSession =
            MediaSession.Builder(
                this,
                player
            )
                .setSessionActivity(
                    sessionActivity
                )
                .build()
    }


    // ==============================================
    // MEDIA CONTROLLER CONNECTION
    // ==============================================

    override fun onGetSession(
        controllerInfo:
            MediaSession.ControllerInfo
    ): MediaSession {

        return mediaSession
    }


    // ==============================================
    // CLEANUP
    // ==============================================

    override fun onDestroy() {

        mediaSession.release()

        player.release()

        super.onDestroy()
    }
}