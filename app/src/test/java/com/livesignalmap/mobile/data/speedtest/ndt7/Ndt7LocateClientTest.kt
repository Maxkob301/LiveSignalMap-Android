package com.livesignalmap.mobile.data.speedtest.ndt7

import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertThrows
import java.io.IOException
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.withTimeout
import okhttp3.EventListener
import okhttp3.Call
import okhttp3.mockwebserver.SocketPolicy
import org.junit.Assert.assertTrue
import java.io.InterruptedIOException
import java.util.concurrent.CountDownLatch

class Ndt7LocateClientTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var httpClient: OkHttpClient
    private lateinit var locateClient: Ndt7LocateClient

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        httpClient = OkHttpClient.Builder()
            .build()

        locateClient = Ndt7LocateClient(
            httpClient = httpClient,
            locateUrl = mockWebServer.url(
                "/v2/nearest/ndt/ndt7"
            )
        )
    }

    @After
    fun tearDown() {
        httpClient.dispatcher.cancelAll()
        httpClient.connectionPool.evictAll()
        httpClient.dispatcher.executorService.shutdown()
        mockWebServer.shutdown()
    }

    @Test
    fun validResponseReturnsServer() = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader(
                    "Content-Type",
                    "application/json"
                )
                .setBody(
                    """
                    {
                        "results": [
                            {
                                "machine": "test.example.org",
                                "urls": {
                                    "wss:///ndt/v7/download":
                                        "wss://test.example.org/ndt/v7/download?access_token=download-token",
                                    "wss:///ndt/v7/upload":
                                        "wss://test.example.org/ndt/v7/upload?access_token=upload-token"
                                }
                            }
                        ]
                    }
                    """.trimIndent()
                )
        )

        val servers = locateClient.locateServers()

        Assert.assertEquals(1, servers.size)

        val server = servers.first()

        Assert.assertEquals(
            "test.example.org",
            server.machine
        )

        Assert.assertEquals(
            "wss://test.example.org/ndt/v7/download?access_token=download-token",
            server.downloadUrl
        )

        Assert.assertEquals(
            "wss://test.example.org/ndt/v7/upload?access_token=upload-token",
            server.uploadUrl
        )
    }

    @Test
    fun locateRequestUsesCorrectMethodAndPath() = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                        "results": [
                            {
                                "machine": "test.example.org",
                                "urls": {
                                    "wss:///ndt/v7/download":
                                        "wss://test.example.org/ndt/v7/download?access_token=download-token",
                                    "wss:///ndt/v7/upload":
                                        "wss://test.example.org/ndt/v7/upload?access_token=upload-token"
                                }
                            }
                        ]
                    }
                    """.trimIndent()
                )
        )

        locateClient.locateServers()

        val request = requireNotNull(
            mockWebServer.takeRequest(
                5,
                TimeUnit.SECONDS
            )
        )

        Assert.assertEquals("GET", request.method)

        Assert.assertEquals(
            "/v2/nearest/ndt/ndt7",
            request.path
        )

        Assert.assertEquals(1, mockWebServer.requestCount)
    }


    private fun createServerJson(
        machine: String = "test.example.org",
        downloadUrl: String =
            "wss://test.example.org/ndt/v7/download?access_token=download-token",
        uploadUrl: String =
            "wss://test.example.org/ndt/v7/upload?access_token=upload-token"
    ): String {
        return """
        {
            "machine": "$machine",
            "urls": {
                "wss:///ndt/v7/download": "$downloadUrl",
                "wss:///ndt/v7/upload": "$uploadUrl"
            }
        }
    """.trimIndent()
    }

    private fun enqueueServers(vararg servers: String) {
        val responseBody = """
        {
            "results": [
                ${servers.joinToString(",")}
            ]
        }
    """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(responseBody)
        )
    }

    @Test
    fun validResponseReturnsMultipleServers() = runBlocking {
        enqueueServers(
            createServerJson(machine = "server1.example.org"),
            createServerJson(machine = "server2.example.org")
        )

        val servers = locateClient.locateServers()

        assertEquals(2, servers.size)
        assertEquals("server1.example.org", servers[0].machine)
        assertEquals("server2.example.org", servers[1].machine)
    }

    @Test
    fun invalidServerIsSkipped() = runBlocking {
        enqueueServers(
            createServerJson(
                machine = "invalid.example.org",
                downloadUrl =
                    "ws://invalid.example.org/ndt/v7/download?access_token=token"
            ),
            createServerJson(
                machine = "valid.example.org"
            )
        )

        val servers = locateClient.locateServers()

        assertEquals(1, servers.size)
        assertEquals("valid.example.org", servers.first().machine)
    }

    @Test
    fun emptyServerListThrowsException() {
        enqueueServers()

        val exception = assertThrows(IOException::class.java) {
            runBlocking {
                locateClient.locateServers()
            }
        }

        assertEquals(
            "No valid NDT7 servers available",
            exception.message
        )
    }
    @Test
    fun serverWithoutAccessTokenIsRejected() {
        enqueueServers(
            createServerJson(
                downloadUrl =
                    "wss://test.example.org/ndt/v7/download"
            )
        )

        val exception = assertThrows(IOException::class.java) {
            runBlocking {
                locateClient.locateServers()
            }
        }

        assertEquals(
            "No valid NDT7 servers available",
            exception.message
        )
    }
    @Test
    fun serverWithIncorrectWebSocketPathIsRejected() {
        enqueueServers(
            createServerJson(
                uploadUrl =
                    "wss://test.example.org/ndt/v7/download?access_token=token"
            )
        )

        val exception = assertThrows(IOException::class.java) {
            runBlocking {
                locateClient.locateServers()
            }
        }

        assertEquals(
            "No valid NDT7 servers available",
            exception.message
        )
    }

    @Test
    fun missingResultsFieldThrowsException() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("{}")
        )

        val exception = assertThrows(IOException::class.java) {
            runBlocking {
                locateClient.locateServers()
            }
        }

        assertEquals(
            "No valid NDT7 servers available",
            exception.message
        )
    }

    @Test
    fun serverWithoutUploadUrlIsRejected() {
        enqueueServers(
            """
        {
            "machine": "test.example.org",
            "urls": {
                "wss:///ndt/v7/download":
                    "wss://test.example.org/ndt/v7/download?access_token=token"
            }
        }
        """.trimIndent()
        )

        val exception = assertThrows(IOException::class.java) {
            runBlocking {
                locateClient.locateServers()
            }
        }

        assertEquals(
            "No valid NDT7 servers available",
            exception.message
        )
    }

    @Test
    fun nullServerIsSkipped() = runBlocking {
        enqueueServers(
            "null",
            createServerJson()
        )

        val servers = locateClient.locateServers()

        assertEquals(1, servers.size)
        assertEquals(
            "test.example.org",
            servers.first().machine
        )
    }

    @Test
    fun malformedJsonThrowsException() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"results": [""")
        )

        val exception = assertThrows(IOException::class.java) {
            runBlocking {
                locateClient.locateServers()
            }
        }

        assertEquals(
            "Invalid Locate API response",
            exception.message
        )
    }

    @Test
    fun unsuccessfulHttpResponsesThrowException() {
        val errorCodes = listOf(400, 403, 429, 500, 503)

        for (code in errorCodes) {
            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(code)
                    .setBody("Server error")
            )

            val exception = assertThrows(IOException::class.java) {
                runBlocking {
                    locateClient.locateServers()
                }
            }

            assertEquals(
                "Locate API returned HTTP $code",
                exception.message
            )
        }

        assertEquals(
            errorCodes.size,
            mockWebServer.requestCount
        )
    }

    @Test
    fun oversizedResponseWithContentLengthIsRejected() {
        val oversizedBody = "x".repeat(256 * 1024 + 1)

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(oversizedBody)
        )

        val exception = assertThrows(IOException::class.java) {
            runBlocking {
                locateClient.locateServers()
            }
        }

        assertEquals(
            "Locate API response is too large",
            exception.message
        )
    }

    @Test
    fun oversizedChunkedResponseIsRejected() {
        val oversizedBody = "x".repeat(256 * 1024 + 1)

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setChunkedBody(oversizedBody, 4096)
        )

        val exception = assertThrows(IOException::class.java) {
            runBlocking {
                locateClient.locateServers()
            }
        }

        assertEquals(
            "Locate API response is too large",
            exception.message
        )
    }
    @Test
    fun requestTimeoutThrowsException() {
        val shortTimeoutClient = Ndt7LocateClient(
            httpClient = httpClient,
            locateUrl = mockWebServer.url(
                "/v2/nearest/ndt/ndt7"
            ),
            timeoutMillis = 500L
        )

        mockWebServer.enqueue(
            MockResponse()
                .setSocketPolicy(SocketPolicy.NO_RESPONSE)
        )

        assertThrows(InterruptedIOException::class.java) {
            runBlocking {
                shortTimeoutClient.locateServers()
            }
        }

        assertEquals(1, mockWebServer.requestCount)
    }
    @Test
    fun cancellationCancelsHttpCall() = runBlocking {
        val cancellationLatch = CountDownLatch(1)

        val cancellableHttpClient = httpClient.newBuilder()
            .eventListener(object : EventListener() {
                override fun canceled(call: Call) {
                    cancellationLatch.countDown()
                }
            })
            .build()

        val cancellableLocateClient = Ndt7LocateClient(
            httpClient = cancellableHttpClient,
            locateUrl = mockWebServer.url(
                "/v2/nearest/ndt/ndt7"
            )
        )

        mockWebServer.enqueue(
            MockResponse()
                .setSocketPolicy(SocketPolicy.NO_RESPONSE)
        )

        val job = launch(
            start = CoroutineStart.UNDISPATCHED
        ) {
            cancellableLocateClient.locateServers()
        }

        try {
            requireNotNull(
                mockWebServer.takeRequest(
                    5,
                    TimeUnit.SECONDS
                )
            )

            withTimeout(5_000L) {
                job.cancelAndJoin()
            }

            assertTrue(job.isCancelled)

            assertTrue(
                cancellationLatch.await(
                    5,
                    TimeUnit.SECONDS
                )
            )
        } finally {
            job.cancelAndJoin()
        }
    }

    @Test
    fun responseAtMaximumSizeIsAccepted() = runBlocking {
        val json = """
        {
            "results": [
                ${createServerJson()}
            ]
        }
    """.trimIndent()

        val maxSize = 256 * 1024

        val paddingSize =
            maxSize - json.toByteArray(Charsets.UTF_8).size

        val responseBody =
            json + " ".repeat(paddingSize)

        assertEquals(
            maxSize,
            responseBody.toByteArray(Charsets.UTF_8).size
        )

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(responseBody)
        )

        val servers = locateClient.locateServers()

        assertEquals(1, servers.size)
        assertEquals(
            "test.example.org",
            servers.first().machine
        )
    }
}