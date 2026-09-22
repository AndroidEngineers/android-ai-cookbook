package `in`.androidengineers.cookbook

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.IOException

fun interface TextSource { fun stream(prompt: String): Flow<String> }

class FixtureSource(private val fail: Boolean = false) : TextSource {
    override fun stream(prompt: String) = flow {
        for (chunk in listOf("Fixture: ", "$prompt. ", "No model or network was used.")) {
            delay(250)
            emit(chunk)
            if (fail) throw IOException("Injected stream failure")
        }
    }
}

data class StreamState(val text: String = "", val phase: String = "Idle")

/** The owner must call cancel when leaving the screen or stopping its lifecycle. */
class StreamController(private val scope: CoroutineScope) {
    private var generation = 0L
    private var job: Job? = null
    private val mutable = MutableStateFlow(StreamState())
    val state: StateFlow<StreamState> = mutable.asStateFlow()

    fun start(source: TextSource, prompt: String) {
        cancel()
        val request = generation
        mutable.value = StreamState(phase = "Loading")
        job = scope.launch {
            try {
                source.stream(prompt).collect { chunk -> acceptChunk(request, chunk) }
                if (request == generation) mutable.value = mutable.value.copy(phase = "Complete")
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                if (request == generation) mutable.value = mutable.value.copy(phase = "Error: ${error.message}")
            }
        }
    }

    // Request identity also protects against stale external callbacks, not just cooperative Flows.
    internal fun acceptChunk(request: Long, chunk: String) {
        if (request == generation) mutable.value = StreamState(mutable.value.text + chunk, "Streaming")
    }
    internal fun requestId() = generation

    fun cancel() {
        generation++
        job?.cancel()
        job = null
        mutable.value = mutable.value.copy(phase = "Cancelled")
    }
}

/** Retry only idempotent reads. Never wrap tool writes or partially consumed streams. */
suspend fun <T> boundedRead(
    maxAttempts: Int = 3,
    timeoutMs: Long = 1000,
    backoffMs: Long = 100,
    onAttempt: (Int) -> Unit = {},
    read: suspend () -> T,
): T {
    require(maxAttempts in 1..5 && timeoutMs > 0 && backoffMs in 0..5000)
    var last: Exception? = null
    repeat(maxAttempts) { index ->
        currentCoroutineContext().ensureActive()
        onAttempt(index + 1)
        try {
            return withTimeout(timeoutMs) { read() }
        } catch (timeout: TimeoutCancellationException) {
            currentCoroutineContext().ensureActive() // Do not retry cancellation by an outer owner.
            last = timeout
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (transient: IOException) {
            last = transient
        }
        if (index + 1 < maxAttempts) delay(backoffMs * (index + 1))
    }
    throw IOException("Stopped after $maxAttempts attempts", last)
}
