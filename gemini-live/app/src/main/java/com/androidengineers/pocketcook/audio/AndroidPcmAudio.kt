package com.androidengineers.pocketcook.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.*
import android.media.audiofx.AcousticEchoCanceler
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.androidengineers.pocketcook.data.PcmAudio
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs

/** Each instance belongs to exactly one Live session. No background recording. */
@OptIn(ExperimentalCoroutinesApi::class)
class AndroidPcmAudio(context: Context) : PcmAudio {
 private val manager = context.getSystemService(AudioManager::class.java)
 private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
 private val lock = Any()
 private val closed = AtomicBoolean(false)
 private val muted = AtomicBoolean(false)
 private val turn = AtomicInteger(0)
 private val captureEpoch = AtomicInteger(0)
 private val queue = Channel<Pair<Int, ByteArray>>(32)
 private var previousMode: Int? = null
 private var selectedSpeaker = false
 private var previousSpeaker = false
 private var recorder: AudioRecord? = null
 private var track: AudioTrack? = null
 private var echo: AcousticEchoCanceler? = null
 private var failure: () -> Unit = {}
 private var speaking: (Boolean) -> Unit = {}
 private val attributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build()
 private val focus = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
  .setAudioAttributes(attributes).setOnAudioFocusChangeListener({ change -> if (change < 0 && !closed.get()) { close(); failure() } }, Handler(Looper.getMainLooper())).build()

 @SuppressLint("MissingPermission") // Activity requests runtime permission before start; failures are handled.
 override fun start(onInput: (ByteArray, Float) -> Unit, onFailure: () -> Unit, onSpeaking: (Boolean) -> Unit) {
  failure = onFailure; speaking = onSpeaking
  check(manager.requestAudioFocus(focus) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
  try {
   previousMode = manager.mode
   manager.mode = AudioManager.MODE_IN_COMMUNICATION
   if (Build.VERSION.SDK_INT >= 31) {
    if (manager.communicationDevice == null || manager.communicationDevice?.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE) {
     manager.availableCommunicationDevices.firstOrNull { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }?.let { selectedSpeaker = manager.setCommunicationDevice(it) }
    }
   } else {
    @Suppress("DEPRECATION")
    previousSpeaker = manager.isSpeakerphoneOn
    @Suppress("DEPRECATION")
    manager.isSpeakerphoneOn = true
    selectedSpeaker = true
   }
   val inputSize = AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
   check(inputSize > 0)
   recorder = AudioRecord.Builder().setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
    .setAudioFormat(AudioFormat.Builder().setSampleRate(16000).setChannelMask(AudioFormat.CHANNEL_IN_MONO).setEncoding(AudioFormat.ENCODING_PCM_16BIT).build())
    .setBufferSizeInBytes(maxOf(inputSize, 6400)).build()
   check(recorder?.state == AudioRecord.STATE_INITIALIZED)
   if (AcousticEchoCanceler.isAvailable()) echo = AcousticEchoCanceler.create(recorder!!.audioSessionId)?.apply { enabled = true }
   val outputSize = AudioTrack.getMinBufferSize(24000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
   check(outputSize > 0)
   track = AudioTrack.Builder().setAudioAttributes(attributes)
    .setAudioFormat(AudioFormat.Builder().setSampleRate(24000).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).setEncoding(AudioFormat.ENCODING_PCM_16BIT).build())
    .setTransferMode(AudioTrack.MODE_STREAM).setBufferSizeInBytes(maxOf(outputSize, 4800)).build()
   check(track?.state == AudioTrack.STATE_INITIALIZED)
   // Small network chunks must be allowed to start without filling the entire device buffer.
   if (Build.VERSION.SDK_INT >= 31) track!!.setStartThresholdInFrames(1)
   track!!.play(); recorder!!.startRecording()
  } catch (e: Exception) { close(); throw e }
  scope.launch {
   val input = recorder ?: return@launch
   val buffer = ByteArray(3200) // Reads may return less than 100 ms.
   val chunks = PcmChunker()
   var epoch = captureEpoch.get()
   try {
    while (isActive && !closed.get()) {
     if (epoch != captureEpoch.get()) { chunks.clear(); epoch = captureEpoch.get() }
     if (muted.get()) { chunks.clear(); delay(20); continue }
     val count = input.read(buffer, 0, buffer.size, AudioRecord.READ_NON_BLOCKING)
     if (count < 0) { if (!muted.get() && !closed.get()) error("capture"); continue }
     if (count == 0) { delay(10); continue }
     if (!muted.get() && epoch == captureEpoch.get()) {
      chunks.append(buffer, count - count % 2) { chunk ->
       var peak = 0
       for (i in chunk.indices step 2) {
        val sample = ((chunk[i].toInt() and 255) or (chunk[i + 1].toInt() shl 8)).toShort().toInt()
        peak = maxOf(peak, abs(sample))
       }
       onInput(chunk, (peak / 12000f).coerceIn(0f, 1f))
      }
     }
    }
   } catch (_: CancellationException) { throw CancellationException() }
   catch (_: Exception) { if (!closed.get()) { close(); failure() } }
  }
  scope.launch {
   try {
    for ((epoch, bytes) in queue) {
     if (epoch != turn.get()) continue
     var offset = 0
     while (offset < bytes.size && isActive && !closed.get() && epoch == turn.get()) {
      val count = synchronized(lock) {
       if (epoch != turn.get() || closed.get()) 0 else track?.write(bytes, offset, minOf(2400, bytes.size-offset), AudioTrack.WRITE_NON_BLOCKING) ?: 0
      }
      check(count >= 0)
      synchronized(lock) { if (epoch == turn.get()) writtenFrames += count / 2 }
      offset += count
      if (count == 0) delay(5)
     }
     // Keep feeding the next network chunk. Waiting for this chunk to drain can
     // deadlock before the AudioTrack startup threshold has been reached.

    }
   } catch (_: CancellationException) { throw CancellationException() }
   catch (_: Exception) { if (!closed.get()) { close(); failure() } }
  }
  scope.launch {
   var lastSpeaking = false
   while (isActive && !closed.get()) {
    val active = synchronized(lock) {
     val head = track?.playbackHeadPosition?.toLong()?.and(0xffffffffL) ?: 0L
     writtenFrames > head || !queue.isEmpty
    }
    if (active != lastSpeaking) { speaking(active); lastSpeaking = active }
    delay(20)
   }
  }
 }
 // Playback completion is observed independently; it never blocks the PCM writer.
 // This accessor also lets the device regression test verify real playback progress.
 internal fun playedFrames(): Long = synchronized(lock) {
  track?.playbackHeadPosition?.toLong()?.and(0xffffffffL) ?: 0L
 }
 // Accumulated frame count is managed under the track lock.
 private var writtenFrames = 0L
 override fun play(pcm: ByteArray) {
  if (closed.get()) return
  if (queue.trySend(turn.get() to pcm).isFailure) { close(); failure() }
 }
 override fun interrupt() {
  synchronized(lock) {
   turn.incrementAndGet()
   while (queue.tryReceive().isSuccess) Unit
   track?.let { runCatching { it.pause(); it.flush(); writtenFrames = 0; it.play() } }
  }
  speaking(false)
 }
 override fun mute(muted: Boolean) {
  if (closed.get()) return
  this.muted.set(muted)
  captureEpoch.incrementAndGet()
  runCatching { if (muted) recorder?.stop() else recorder?.startRecording() }.onFailure { close(); failure() }
 }
 override fun close() {
  if (!closed.compareAndSet(false, true)) return
  scope.cancel(); queue.close()
  synchronized(lock) {
   runCatching { recorder?.stop() }; recorder?.release(); recorder = null
   runCatching { track?.pause(); track?.flush() }; track?.release(); track = null
   echo?.release(); echo = null
  }
  if (selectedSpeaker) {
   if (Build.VERSION.SDK_INT >= 31) {
    if (manager.communicationDevice?.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) manager.clearCommunicationDevice()
   } else {
    @Suppress("DEPRECATION")
    manager.isSpeakerphoneOn = previousSpeaker
   }
  }
  previousMode?.let { manager.mode = it }
  manager.abandonAudioFocusRequest(focus)
 }
}
