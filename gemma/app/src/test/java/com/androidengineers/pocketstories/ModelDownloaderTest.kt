package com.androidengineers.pocketstories

import com.androidengineers.pocketstories.data.ModelDownloader
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.*
import org.junit.Assert.*
import org.junit.rules.TemporaryFolder

class ModelDownloaderTest {
    @get:Rule val folder = TemporaryFolder()
    private val server = MockWebServer()
    private val bytes = "verified model fixture"
    private val hash =
        MessageDigest.getInstance("SHA-256").digest(bytes.toByteArray()).joinToString("") {
            "%02x".format(it)
        }
    private lateinit var target: File
    private val partial
        get() = File(target.path + ".download")

    @Before
    fun setup() {
        server.start()
        target = File(folder.root, "model")
    }

    @After
    fun close() {
        server.shutdown()
    }

    private suspend fun transfer() =
        ModelDownloader().download(
            server.url("/model").toString(),
            target,
            bytes.length.toLong(),
            hash,
            0,
        ) {}

    @Test
    fun installsOnlyVerifiedFile() = runBlocking {
        server.enqueue(MockResponse().setBody(bytes))
        transfer()
        assertEquals(bytes, target.readText())
        assertFalse(partial.exists())
    }

    @Test
    fun corruptFileNeverBecomesReady() = runBlocking {
        server.enqueue(MockResponse().setBody("x".repeat(bytes.length)))
        assertTrue(runCatching { transfer() }.isFailure)
        assertFalse(target.exists())
        assertFalse(partial.exists())
    }

    @Test
    fun resumesWithRange() = runBlocking {
        partial.writeText(bytes.take(5))
        server.enqueue(
            MockResponse()
                .setResponseCode(206)
                .setHeader("Content-Range", "bytes 5-${bytes.length-1}/${bytes.length}")
                .setBody(bytes.drop(5))
        )
        transfer()
        assertEquals("bytes=5-", server.takeRequest().getHeader("Range"))
        assertEquals(bytes, target.readText())
    }

    @Test
    fun serverIgnoringRangeRestartsSafely() = runBlocking {
        partial.writeText(bytes.take(5))
        server.enqueue(MockResponse().setBody(bytes))
        transfer()
        assertEquals(bytes, target.readText())
    }

    @Test
    fun interruptedBodyIsRetainedForRetry() = runBlocking {
        server.enqueue(MockResponse().setBody(bytes.take(5)))
        assertTrue(runCatching { transfer() }.isFailure)
        assertFalse(target.exists())
        assertEquals(bytes.take(5), partial.readText())
        server.enqueue(
            MockResponse()
                .setResponseCode(206)
                .setHeader("Content-Range", "bytes 5-${bytes.length-1}/${bytes.length}")
                .setBody(bytes.drop(5))
        )
        transfer()
        assertEquals(bytes, target.readText())
    }

    @Test
    fun invalidRangeDoesNotAppend() = runBlocking {
        partial.writeText(bytes.take(5))
        server.enqueue(
            MockResponse()
                .setResponseCode(206)
                .setHeader("Content-Range", "bytes 0-4/5")
                .setBody("wrong")
        )
        assertTrue(runCatching { transfer() }.isFailure)
        assertEquals(bytes.take(5), partial.readText())
        assertFalse(target.exists())
    }

    @Test
    fun cancellationInterruptsBlockedRead() = runBlocking {
        server.enqueue(MockResponse().setBody(bytes).setBodyDelay(3, TimeUnit.SECONDS))
        val job = launch(Dispatchers.Default) { transfer() }
        withContext(Dispatchers.IO) { assertNotNull(server.takeRequest(2, TimeUnit.SECONDS)) }
        withTimeout(1500) { job.cancelAndJoin() }
        assertFalse(target.exists())
    }
}
