package com.androidengineers.pocketcook
import com.androidengineers.pocketcook.data.*
import com.androidengineers.pocketcook.data.Connection
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import okhttp3.*
import okio.ByteString
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LiveSessionTest {
 @Test fun duplicateStartCreatesOneSocketAndNoMicrophoneBeforeSetup() = runTest {
  val sockets=FakeSockets(); val audio=FakeAudio();val live=GeminiLiveConnection(backgroundScope,{audio},sockets)
  live.start("test-only","example-live",Recipes.all.first(),0);live.start("test-only","example-live",Recipes.all.first(),0)
  assertEquals(1,sockets.listeners.size);assertEquals(0,audio.starts)
  sockets.ready(0);runCurrent();assertEquals(1,audio.starts);assertEquals(Connection.Connected,live.state.value.connection)
  live.end();assertEquals(1,audio.closes);assertEquals(Connection.Idle,live.state.value.connection)
 }
 @Test fun oldSocketCannotReopenNewSession() = runTest {
  val sockets=FakeSockets();val audio=FakeAudio();val live=GeminiLiveConnection(backgroundScope,{audio},sockets)
  live.start("test-only","example-live",Recipes.all.first(),0);live.end()
  live.start("test-only","example-live",Recipes.all.first(),0)
  sockets.ready(0);runCurrent();assertEquals(0,audio.starts);assertEquals(Connection.Connecting,live.state.value.connection)
  sockets.ready(1);runCurrent();assertEquals(1,audio.starts)
  live.end()
 }
 @Test fun setupTimeoutIsRecoverableAndNeverStartsCapture() = runTest {
  val sockets=FakeSockets();val audio=FakeAudio();val live=GeminiLiveConnection(backgroundScope,{audio},sockets)
  live.start("test-only","example-live",Recipes.all.first(),0);runCurrent();advanceTimeBy(20_001);runCurrent()
  assertEquals(Connection.Error,live.state.value.connection);assertEquals(0,audio.starts)
 }
 @Test fun interruptionFlushesPlaybackAndMuteReachesAudioAdapter() = runTest {
  val sockets=FakeSockets();val audio=FakeAudio();val live=GeminiLiveConnection(backgroundScope,{audio},sockets)
  live.start("test-only","example-live",Recipes.all.first(),0);sockets.ready(0);runCurrent()
  sockets.listeners[0].onMessage(sockets.socket,"""{"serverContent":{"interrupted":true}}""");runCurrent()
  assertEquals(1,audio.interrupts)
  live.mute();assertTrue(audio.muted);assertTrue(live.state.value.muted)
  live.end()
 }
 @Test fun malformedMessageClosesSessionWithSafeError() = runTest {
  val sockets=FakeSockets();val audio=FakeAudio();val live=GeminiLiveConnection(backgroundScope,{audio},sockets)
  live.start("test-only","example-live",Recipes.all.first(),0);sockets.ready(0);runCurrent()
  sockets.listeners[0].onMessage(sockets.socket,"not json secret-value");runCurrent()
  assertEquals(Connection.Error,live.state.value.connection);assertEquals(1,audio.closes)
  assertFalse(live.state.value.message.contains("secret-value"))
 }
 @Test fun captureOverloadIsBoundedAndRestartDoesNotSendOldAudio() = runTest {
  val sockets=FakeSockets(); val audio=FakeAudio(); val live=GeminiLiveConnection(backgroundScope,{audio},sockets)
  live.start("test-only","example-live",Recipes.all.first(),0); sockets.ready(0); runCurrent()
  repeat(10) { audio.input(ByteArray(3200), 0f) }
  runCurrent()
  assertEquals(Connection.Error,live.state.value.connection)
  val previous = sockets.sent.size
  live.start("test-only","example-live",Recipes.all.first(),0); sockets.ready(1); runCurrent()
  assertEquals(Connection.Connected,live.state.value.connection)
  assertEquals(previous,sockets.sent.size)
  audio.input(ByteArray(3200),0f); runCurrent()
  assertEquals(previous+1,sockets.sent.size)
  live.end()
 }
 @Test fun mutedQueuedSpeechIsDiscardedAndNetworkFailureAllowsRestart() = runTest {
  val sockets=FakeSockets(); val audio=FakeAudio(); val live=GeminiLiveConnection(backgroundScope,{audio},sockets)
  live.start("test-only","example-live",Recipes.all.first(),0); sockets.ready(0); runCurrent()
  audio.input(ByteArray(3200),0f); live.mute(); live.mute(); runCurrent()
  assertFalse(sockets.sent.any { it.contains("audio/pcm") })
  sockets.listeners[0].onFailure(sockets.socket,java.io.IOException("private failure"),null); runCurrent()
  assertEquals(Connection.Error,live.state.value.connection)
  assertFalse(live.state.value.message.contains("private failure"))
  live.start("test-only","example-live",Recipes.all.first(),0); sockets.ready(1); runCurrent()
  assertEquals(Connection.Connected,live.state.value.connection)
  live.end()
 }

}
private class FakeSockets : WebSocket.Factory {
 val sent=mutableListOf<String>()
 val listeners=mutableListOf<WebSocketListener>()
 val socket=object:WebSocket {
  override fun request()=Request.Builder().url("https://example.test").build()
  override fun queueSize()=0L
  override fun send(text:String):Boolean { sent+=text;return true }
  override fun send(bytes:ByteString)=true
  override fun close(code:Int,reason:String?)=true
  override fun cancel(){}
 }
 override fun newWebSocket(request:Request,listener:WebSocketListener):WebSocket {listeners+=listener;return socket}
 fun ready(index:Int){listeners[index].onMessage(socket,"""{"setupComplete":{}}""")}
}
private class FakeAudio:PcmAudio {
 var input:(ByteArray,Float)->Unit = { _, _ -> }
 var starts=0;var closes=0;var interrupts=0;var muted=false
 override fun start(onInput:(ByteArray,Float)->Unit,onFailure:()->Unit,onSpeaking:(Boolean)->Unit){starts++;input=onInput}
 override fun play(pcm:ByteArray){}
 override fun interrupt(){interrupts++}
 override fun mute(muted:Boolean){this.muted=muted}
 override fun close(){closes++}
}
