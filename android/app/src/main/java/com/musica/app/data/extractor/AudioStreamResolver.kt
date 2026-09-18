package com.musica.app.data.extractor

import android.content.Context
import com.musica.app.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.AudioStream

data class ResolvedAudio(
    val song: Song,
    val url: String,
    val mimeType: String?,
    val bitrate: Int
)

class AudioStreamResolver(
    context: Context
) {

    init {

        NewPipeExtractorInitializer.initialize(
            AndroidDownloader()
        )
    }


    suspend fun resolve(
        song: Song
    ): Result<ResolvedAudio> {

        return withContext(
            Dispatchers.IO
        ) {

            try {

                val service =
                    ServiceList.YouTube


                val streamInfo =
                    service
                        .getStreamExtractor(
                            "https://www.youtube.com/watch?v=${song.videoId}"
                        )


                streamInfo.fetchPage()


                val audioStreams =
                    streamInfo
                        .audioStreams


                if (
                    audioStreams.isEmpty()
                ) {

                    return@withContext Result.failure(
                        Exception(
                            "No audio stream found"
                        )
                    )
                }


                val selectedStream =
                    selectBestAudioStream(
                        audioStreams
                    )


                Result.success(

                    ResolvedAudio(

                        song =
                            song,

                        url =
                            selectedStream.content,

                        mimeType =
                            selectedStream.format?.mimeType,

                        bitrate =
                            selectedStream.averageBitrate
                    )
                )

            } catch (e: Exception) {

                Result.failure(e)
            }
        }
    }


    private fun selectBestAudioStream(
        streams: List<AudioStream>
    ): AudioStream {

        return streams
            .filter {
                it.content.isNotBlank()
            }
            .maxByOrNull {
                it.averageBitrate
            }
            ?: streams.first()
    }
}