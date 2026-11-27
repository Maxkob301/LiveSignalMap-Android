package com.livesignalmap.mobile.data.speedtest.ndt7

import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.livesignalmap.mobile.data.speedtest.ndt7.model.Ndt7LocateResponse
import com.livesignalmap.mobile.data.speedtest.ndt7.model.Ndt7LocateTarget
import com.livesignalmap.mobile.data.speedtest.ndt7.model.Ndt7Server
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import okhttp3.ResponseBody
import java.util.concurrent.TimeUnit

internal class Ndt7LocateClient(
    private val httpClient: OkHttpClient,
    private val locateUrl: HttpUrl = DEFAULT_LOCATE_URL,
    private val gson: Gson = Gson(),
    private val timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS
) {
    init {
        require(timeoutMillis > 0) {
            "Timeout must be positive"
        }
    }

    suspend fun locateServers(): List<Ndt7Server> {
        val responseBody = requestServerList()

        val response = try {
            gson.fromJson(
                responseBody,
                Ndt7LocateResponse::class.java
            )
        } catch (e: JsonParseException) {
            throw IOException("Invalid Locate API response")
        }

        val servers = response?.results
            .orEmpty()
            .mapNotNull { target ->
                target?.let { toServer(it) }
            }

        if (servers.isEmpty()) {
            throw IOException("No valid NDT7 servers available")
        }

        return servers
    }

    private fun toServer(
        target: Ndt7LocateTarget
    ): Ndt7Server? {

        val machine = target.machine
            ?.takeIf { it.isNotBlank() }
            ?: return null

        val downloadUrl = target.urls?.get(DOWNLOAD_KEY)
            ?: return null

        val uploadUrl = target.urls[UPLOAD_KEY]
            ?: return null

        if (!isValidWebSocketUrl(downloadUrl, "/ndt/v7/download")) {
            return null
        }

        if (!isValidWebSocketUrl(uploadUrl, "/ndt/v7/upload")) {
            return null
        }

        return Ndt7Server(
            machine = machine,
            downloadUrl = downloadUrl,
            uploadUrl = uploadUrl
        )
    }

    private fun isValidWebSocketUrl(
        url: String,
        expectedPath: String
    ): Boolean {

        if (!url.startsWith("wss://")) {
            return false
        }

        val parsedUrl = (
                "https://" + url.removePrefix("wss://")
                ).toHttpUrlOrNull() ?: return false

        return parsedUrl.encodedPath == expectedPath &&
                parsedUrl.username.isEmpty() &&
                parsedUrl.password.isEmpty() &&
                parsedUrl.fragment == null &&
                !parsedUrl.queryParameter("access_token").isNullOrBlank()
    }

    private fun readResponseBody(body: ResponseBody): String {
        val contentLength = body.contentLength()

        if (contentLength > MAX_RESPONSE_SIZE_BYTES) {
            throw IOException("Locate API response is too large")
        }

        val source = body.source()

        if (source.request(MAX_RESPONSE_SIZE_BYTES + 1)) {
            throw IOException("Locate API response is too large")
        }

        return source.readUtf8()
    }

    private suspend fun requestServerList(): String =
        suspendCancellableCoroutine { continuation ->

            val request = Request.Builder()
                .url(locateUrl)
                .get()
                .build()

            val call = httpClient.newCall(request)

            call.timeout().timeout(
                timeoutMillis,
                TimeUnit.MILLISECONDS
            )

            continuation.invokeOnCancellation {
                call.cancel()
            }

            call.enqueue(object : Callback {

                override fun onFailure(
                    call: Call,
                    e: IOException
                ) {
                    continuation.resumeWithException(e)
                }

                override fun onResponse(
                    call: Call,
                    response: Response
                ) {
                    val body = try {
                        response.use {
                            if (!it.isSuccessful) {
                                throw IOException(
                                    "Locate API returned HTTP ${it.code}"
                                )
                            }

                            val responseBody = it.body
                                ?: throw IOException(
                                    "Locate API returned an empty body"
                                )
                            readResponseBody(responseBody)
                        }
                    } catch (e: Exception) {
                        continuation.resumeWithException(e)
                        return
                    }

                    continuation.resume(body)
                }
            })
        }

    private companion object {
        val DEFAULT_LOCATE_URL =
            "https://locate.measurementlab.net/v2/nearest/ndt/ndt7"
                .toHttpUrl()

        const val DOWNLOAD_KEY = "wss:///ndt/v7/download"
        const val UPLOAD_KEY = "wss:///ndt/v7/upload"

        const val MAX_RESPONSE_SIZE_BYTES = 256L * 1024L
        const val DEFAULT_TIMEOUT_MILLIS = 15_000L
    }
}