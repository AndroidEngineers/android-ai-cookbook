package com.androidengineers.pocketcook.data
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.channels.Channel
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.ByteString
import java.util.concurrent.TimeUnit

enum class Connection { Idle, Connecting, Connected, Error }
data class VoiceState(val connection: Connection = Connection.Idle, val muted: Boolean = false, val speaking: Boolean = false, val level: Float = 0f, val transcript: String = "", val message: String = "Ready when you are")
interface LiveConnection {
 val state: StateFlow<VoiceState>
 fun start(key: String, model: String, recipe: Recipe, step: Int)
 fun mute()
 fun updateStep(step: Int)
 fun end(message: String = "Voice session ended")
}
interface PcmAudio {
 fun start(onInput: (ByteArray, Float) -> Unit, onFailure: () -> Unit, onSpeaking: (Boolean) -> Unit)
 fun play(pcm: ByteArray)
 fun interrupt()
 fun mute(muted: Boolean)
 fun close()
}
class GeminiLiveConnection(private val scope: CoroutineScope, private val audioFactory: () -> PcmAudio, private val socketFactory: WebSocket.Factory? = null) : LiveConnection {
 private val mutable = MutableStateFlow(VoiceState())
 override val state = mutable.asStateFlow()
 private val client = OkHttpClient.Builder().readTimeout(0, TimeUnit.MILLISECONDS).pingInterval(20, TimeUnit.SECONDS).build()
 private val incoming = Channel<Pair<Long, String>>(32)
 private data class MicChunk(val generation: Long, val captureEpoch: Long, val bytes: ByteArray, val level: Float)
 // Eight complete 100 ms chunks bound queued input to 800 ms.
 private val microphone = Channel<MicChunk>(8)
 init {
  scope.launch { for ((id, text) in incoming) if (id == generation) handle(text, id) }
  scope.launch { for (chunk in microphone) {
   if (chunk.generation == generation && chunk.captureEpoch == captureEpoch && state.value.connection == Connection.Connected && !state.value.muted) {
    send(LiveProtocol.audio(chunk.bytes)); mutable.update { if (it.connection == Connection.Connected) it.copy(level = chunk.level) else it }
   }
  } }
 }
 private var socket: WebSocket? = null
 private var audio: PcmAudio? = null
 @Volatile private var generation = 0L
 @Volatile private var captureEpoch = 0L
 private var timeout: Job? = null
 private var limit: Job? = null
 override fun start(key: String, model: String, recipe: Recipe, step: Int) {
  if (state.value.connection in listOf(Connection.Connecting, Connection.Connected)) return
  if (key.isBlank() || !model.matches(Regex("[A-Za-z0-9._-]+"))) {
   mutable.value = VoiceState(connection = Connection.Error, message = "Enter your API key and a valid Live model in connection settings."); return
  }
  end()
  val id = generation
  mutable.value = VoiceState(connection = Connection.Connecting, message = "Connecting…")
  val url = "https://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent".toHttpUrl().newBuilder().addQueryParameter("key", key).build()
  socket = (socketFactory ?: client).newWebSocket(Request.Builder().url(url).build(), object : WebSocketListener() {
   override fun onOpen(webSocket: WebSocket, response: Response) { scope.launch { if (id == generation) send(LiveProtocol.setup(model, recipe, step)) else webSocket.cancel() } }
   override fun onMessage(webSocket: WebSocket, text: String) {
    if (text.length > 2_000_000 || incoming.trySend(id to text).isFailure) scope.launch { if (id == generation) fail("Response stream exceeded its limit. Start a new session.") }
   }
   override fun onMessage(webSocket: WebSocket, bytes: ByteString) = onMessage(webSocket, bytes.utf8())
   override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) { scope.launch { if (id == generation) fail("Could not connect. Check your network, API key and model access.") } }
   override fun onClosed(webSocket: WebSocket, code: Int, reason: String) { scope.launch { if (id == generation) fail("Connection ended. Your recipe progress is saved. Try a new session.") } }
   override fun onClosing(webSocket: WebSocket, code: Int, reason: String) { webSocket.close(code, null) }
  })
  timeout = scope.launch { delay(20_000); if (id == generation && state.value.connection == Connection.Connecting) fail("Connection timed out. Please retry.") }
  limit = scope.launch { delay(10 * 60_000L); if (id == generation) end("Ten-minute session ended. Start again when ready.") }
 }
 private fun handle(text: String, id: Long) {
  val events = try { LiveProtocol.parse(text) } catch (_: Exception) { fail("Received an unsupported response. Please start a new session."); return }
  for (event in events) {
   if (id != generation) return
   when(event) {
    LiveEvent.Ready -> {
     if (state.value.connection != Connection.Connecting) continue
     timeout?.cancel()
     try {
      audio = audioFactory().also { engine -> engine.start(
       { bytes, level -> if (microphone.trySend(MicChunk(id, captureEpoch, bytes, level)).isFailure) scope.launch { if (id == generation) fail("Audio capture exceeded its buffer. Please retry.") } },
       { scope.launch { if (id == generation) fail("Audio is unavailable. Check microphone permission and audio focus, then retry.") } },
       { speaking -> scope.launch { if (id == generation) mutable.update { it.copy(speaking = speaking) } } }
      ) }
      mutable.update { it.copy(connection = Connection.Connected, message = "Voice connected") }
     } catch (_: Exception) { fail("Microphone or speaker unavailable. Check permission and retry.") }
    }
    is LiveEvent.Audio -> if (state.value.connection == Connection.Connected) audio?.play(event.pcm)
    LiveEvent.Interrupted -> { audio?.interrupt(); mutable.update { it.copy(speaking = false) } }
    is LiveEvent.Transcript -> mutable.update { it.copy(transcript = (it.transcript + "\n${event.speaker}: ${event.text}").takeLast(12_000)) }
    LiveEvent.TurnComplete -> Unit // Playback status comes from the audio device, not network completion.
    LiveEvent.Expiring -> fail("This session is expiring. Start a new session to continue.")
    LiveEvent.Rejected -> fail("Gemini rejected this session. Check your key, quota and model access.")
   }
  }
 }
 private fun send(message: String) {
  val current = socket ?: return
  if (current.queueSize() > 256_000 || !current.send(message)) fail("Connection is too slow. Please start a new session.")
 }
 override fun mute() {
  if (state.value.connection != Connection.Connected) return
  val muted = !state.value.muted
  captureEpoch++
  audio?.mute(muted)
  while (microphone.tryReceive().isSuccess) Unit
  mutable.update { it.copy(muted = muted, level = 0f) }
  if (muted) send(LiveProtocol.audioEnd())
 }
 override fun updateStep(step: Int) { if (state.value.connection == Connection.Connected) send(LiveProtocol.context(step)) }
 override fun end(message: String) {
  generation++
  timeout?.cancel(); limit?.cancel()
  socket?.cancel(); socket = null
  audio?.close(); audio = null
  while (microphone.tryReceive().isSuccess) Unit
  while (incoming.tryReceive().isSuccess) Unit
  mutable.value = VoiceState(message = message)
 }
 private fun fail(message: String) { val transcript = state.value.transcript; end(); mutable.value = VoiceState(connection = Connection.Error, message = message, transcript = transcript) }
 fun dispose() { end(); client.dispatcher.executorService.shutdown(); client.connectionPool.evictAll(); incoming.close(); microphone.close(); scope.cancel() }
}
