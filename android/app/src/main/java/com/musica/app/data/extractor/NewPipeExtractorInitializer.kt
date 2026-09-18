package com.musica.app.data.extractor

import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.downloader.Downloader
import java.io.IOException

object NewPipeExtractorInitializer {

    @Volatile
    private var initialized = false

    @Synchronized
    fun initialize(
        downloader: Downloader
    ) {

        if (initialized) {
            return
        }

        NewPipe.init(
            downloader
        )

        initialized = true
    }
}