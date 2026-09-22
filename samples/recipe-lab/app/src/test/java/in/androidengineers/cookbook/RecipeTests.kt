package `in`.androidengineers.cookbook

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeTests {
    @Test fun streamingCompletesWithExpectedText() = runTest {
        val c = StreamController(this)
        c.start(FixtureSource(), "A")
        advanceUntilIdle()
        assertEquals(StreamState("Fixture: A. No model or network was used.", "Complete"), c.state.value)
    }
    @Test fun streamingFailurePreservesPartialTextAndRecovers() = runTest {
        val c = StreamController(this)
        c.start(FixtureSource(true), "A")
        advanceUntilIdle()
        assertTrue(c.state.value.phase.startsWith("Error"))
        assertEquals("Fixture: ", c.state.value.text)
        c.start(FixtureSource(), "B")
        advanceUntilIdle()
        assertTrue(c.state.value.text.contains("B."))
        assertEquals("Complete", c.state.value.phase)
    }
    @Test fun replacementRejectsLateChunksFromPreviousRequest() = runTest {
        val c = StreamController(this)
        c.start(FixtureSource(), "A")
        val oldRequest = c.requestId()
        advanceTimeBy(300)
        c.start(FixtureSource(), "B")
        c.acceptChunk(oldRequest, "STALE")
        advanceUntilIdle()
        assertEquals("Fixture: B. No model or network was used.", c.state.value.text)
    }
    @Test fun cancelStopsCollectionAndRejectsLateCallbacks() = runTest {
        var cleanedUp = false
        val c = StreamController(this)
        c.start(TextSource { flow { try { emit("partial"); awaitCancellation() } finally { cleanedUp = true } } }, "A")
        runCurrent()
        val old = c.requestId()
        c.cancel()
        c.acceptChunk(old, "stale")
        runCurrent()
        assertTrue(cleanedUp)
        assertEquals(StreamState("partial", "Cancelled"), c.state.value)
    }
    @Test fun emptyOutputCompletesWithoutInventingText() = runTest {
        val c = StreamController(this)
        c.start(TextSource { emptyFlow() }, "A")
        advanceUntilIdle()
        assertEquals(StreamState("", "Complete"), c.state.value)
    }
    @Test fun validExtraction() {
        assertEquals(ExtractedTask("Compose",25), validateTask("""{"title":"Compose","minutes":25}"""))
    }
    @Test fun extractionRejectsMalformedAndUnsupportedValues() {
        listOf("not json", "[]", "{}", """{"title":"","minutes":25}""",
            """{"title":"a","minutes":"25"}""", """{"title":"a","minutes":0}""",
            """{"title":"a","minutes":121}""", """{"title":"a","minutes":2.5}""",
            """{"title":"a","minutes":25,"execute":true}""", """{"title":null,"minutes":25}""",
            "x".repeat(4097)).forEach { raw ->
            assertThrows("Accepted invalid input: $raw", Exception::class.java) { validateTask(raw) }
        }
    }
    @Test fun retryRecoversOnThirdAttempt() = runTest {
        var count = 0
        val result = boundedRead { if (++count < 3) throw IOException("transient"); "ok" }
        assertEquals("ok",result); assertEquals(3,count)
    }
    @Test fun permanentOfflineStopsAtBudget() = runTest {
        var count = 0
        try { boundedRead { count++; throw IOException("offline") }; fail() } catch (e: IOException) {
            assertEquals(3,count); assertTrue(e.message!!.contains("3 attempts"))
        }
    }
    @Test fun timeoutsAreBounded() = runTest {
        var count = 0
        try { boundedRead(timeoutMs=100) { count++; delay(1000); "never" }; fail() } catch (e: IOException) { assertEquals(3,count) }
    }
    @Test fun cancellationDoesNotRetry() = runTest {
        var count = 0
        val job = launch { boundedRead { count++; delay(1000); "never" } }
        runCurrent(); job.cancelAndJoin(); advanceUntilIdle()
        assertEquals(1,count)
    }
    @Test fun outerTimeoutDoesNotBecomeRetry() = runTest {
        var count = 0
        try { withTimeout(50) { boundedRead(timeoutMs=1000) { count++; delay(200); "never" } }; fail() }
        catch (e: TimeoutCancellationException) { assertEquals(1,count) }
    }
    @Test fun validationFailureDoesNotRetry() = runTest {
        var count = 0
        try { boundedRead { count++; throw IllegalArgumentException("bad request") }; fail() }
        catch (e: IllegalArgumentException) { assertEquals(1,count) }
    }
    @Test fun approvalIsExactAndSingleUse() {
        val gate = ApprovalGate(25)
        assertEquals("approval_required",gate.execute(mapOf("minutes" to 60))["status"])
        assertEquals("recorded_in_memory",gate.execute(mapOf("minutes" to 25))["status"])
        assertEquals("already_used",gate.execute(mapOf("minutes" to 25))["status"])
        assertEquals(1,gate.writes)
    }
    @Test fun modelCannotSupplyApprovalOrInvalidMinutes() {
        val gate = ApprovalGate(null)
        assertEquals("approval_required",gate.execute(mapOf("minutes" to 25))["status"])
        for (args in listOf(mapOf("minutes" to 25, "approved" to true), mapOf("minutes" to "25"), mapOf("minutes" to -1), mapOf("minutes" to 2.5)))
            assertEquals("invalid_arguments",gate.execute(args)["status"])
        assertEquals(0,gate.writes)
    }
    @Test fun actualAdkRunnerDeniesUnapprovedTool() = runTest {
        val result = runSafeAgent(null)
        assertEquals(0,result.writes)
        assertTrue(result.transcript.contains("approval_required"))
    }
    @Test fun actualAdkRunnerExecutesApprovedToolOnce() = runTest {
        val result = runSafeAgent(25)
        assertEquals(1,result.writes)
        assertTrue(result.transcript.contains("recorded_in_memory"))
    }
    @Test fun actualAdkRunnerRejectsMismatchedApproval() = runTest {
        val result = runSafeAgent(25,60)
        assertEquals(0,result.writes)
        assertTrue(result.transcript.contains("approval_required"))
    }
    @Test fun actualAdkRunnerStopsRepeatingModel() = runTest {
        var stopped = false
        try { runSafeAgent(25, repeatForever=true) } catch (e: Exception) {
            assertTrue(e.toString(), e.message.orEmpty().contains("Model call budget exceeded"))
            stopped = true
        }
        assertTrue("Expected ADK call budget to stop the run", stopped)
    }
}
