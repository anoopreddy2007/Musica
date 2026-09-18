package com.musica.app.data.extractor

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request as NewPipeRequest
import org.schabi.newpipe.extractor.downloader.Response
import java.util.concurrent.TimeUnit

class AndroidDownloader : Downloader() {

    companion object {

        private const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/140.0.0.0 Safari/537.36"
    }


    private val client =
        OkHttpClient.Builder()
            .connectTimeout(
                30,
                TimeUnit.SECONDS
            )
            .readTimeout(
                30,
                TimeUnit.SECONDS
            )
            .writeTimeout(
                30,
                TimeUnit.SECONDS
            )
            .followRedirects(true)
            .followSslRedirects(true)
            .build()


    override fun execute(
        request: NewPipeRequest
    ): Response {

        val httpRequestBuilder =
            Request.Builder()
                .url(request.url())
                .addHeader(
                    "User-Agent",
                    USER_AGENT
                )


        request.headers()
            .forEach { (name, values) ->

                httpRequestBuilder
                    .removeHeader(name)

                values.forEach { value ->

                    httpRequestBuilder
                        .addHeader(
                            name,
                            value
                        )
                }
            }


        val data =
            request.dataToSend()


        val requestBody =
            if (data != null) {

                RequestBody.create(
                    null,
                    data
                )

            } else {
                null
            }


        val okhttpRequest =
            httpRequestBuilder
                .method(
                    request.httpMethod(),
                    requestBody
                )
                .build()


        client
            .newCall(
                okhttpRequest
            )
            .execute()
            .use { response ->

                val responseBody =
                    response.body
                        ?.string()
                        ?: ""


                val headers =
                    response.headers
                        .toMultimap()


                val latestUrl =
                    response.request
                        .url
                        .toString()


                return Response(
                    response.code,
                    response.message,
                    headers,
                    responseBody,
                    latestUrl
                )
            }
    }
}