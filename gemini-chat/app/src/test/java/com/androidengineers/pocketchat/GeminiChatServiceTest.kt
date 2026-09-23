package com.androidengineers.pocketchat

import com.androidengineers.pocketchat.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.SocketPolicy
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import java.io.StringReader
import java.util.concurrent.TimeUnit

class GeminiChatServiceTest {
    @Test fun sseHandlesCrLfCommentsMultipleLinesAndUnterminatedLastFrame() {
        val frames=mutableListOf<String>()
        readSse(StringReader(": ping\r\ndata: one\r\ndata: two\r\n\r\ndata: last")){frames+=it}
        assertEquals(listOf("one\ntwo","last"),frames)
    }
    @Test fun textDecoderIgnoresThoughtsAndPreservesFinishReason() {
        val result=decodeGeminiEvent("""{"candidates":[{"content":{"parts":[{"thought":true,"text":"private"},{"text":"Hello"}]},"finishReason":"STOP"}]}""")
        assertEquals(listOf(ChatEvent.Text("Hello"),ChatEvent.Finished("STOP")),result)
    }
    @Test fun blockedPromptIsNotAnEmptySuccess() {
        assertThrows(ChatFailure::class.java){decodeGeminiEvent("""{"promptFeedback":{"blockReason":"SAFETY"}}""")}
    }
    @Test fun oversizedSseFrameIsBounded() {
        assertThrows(ChatFailure::class.java){readSse(StringReader("data: "+"x".repeat(262145))) { }}
    }
    @Test fun transportSendsContextAndStreamsText() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setHeader("Content-Type","text/event-stream").setBody("data: {\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Hello\"}]}}]}\n\ndata: {\"candidates\":[{\"finishReason\":\"STOP\"}]}\n\n"))
            val events=GeminiChatService(endpoint=server.url("/").toString()).stream("test-only","gemini-2.5-flash",listOf(ChatPart("user","Hello"))).toList()
            assertEquals(listOf(ChatEvent.Text("Hello"),ChatEvent.Finished("STOP")),events)
            val request=server.takeRequest(2,TimeUnit.SECONDS)!!
            assertEquals("test-only",request.getHeader("x-goog-api-key"))
            assertFalse(request.path!!.contains("test-only"))
            val payload=kotlinx.serialization.json.Json.parseToJsonElement(request.body.readUtf8()).toString()
            assertTrue(payload.contains("Hello"))
            assertTrue(payload.contains("AI learning assistant from Android Engineers"))
            assertTrue(payload.contains("https://www.androidengineers.in/masterclass/one-to-one-mentoring"))
        }
    }
    @Test fun cancellingCollectionCancelsUnderlyingHttpCall() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
            val client=OkHttpClient()
            val job=launch(start=CoroutineStart.UNDISPATCHED) {
                GeminiChatService(client,server.url("/").toString()).stream("test","gemini-2.5-flash",listOf(ChatPart("user","Hello"))).collect()
            }
            assertNotNull(withContext(Dispatchers.IO) { server.takeRequest(2,TimeUnit.SECONDS) })
            val call=client.dispatcher.runningCalls().single()
            job.cancelAndJoin()
            assertTrue(call.isCanceled())
        }
    }
    @Test fun quotaErrorHasActionableMessageWithoutEchoingProviderBody() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(429).setBody("sensitive provider detail"))
            val failure=runCatching { GeminiChatService(endpoint=server.url("/").toString()).stream("test","gemini-2.5-flash",emptyList()).toList() }.exceptionOrNull()
            assertTrue(failure is ChatFailure);assertTrue(failure!!.message!!.contains("quota"));assertFalse(failure.message!!.contains("sensitive"))
        }
    }
}
