package com.androidengineers.pocketcook
import com.androidengineers.pocketcook.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

class PocketCookTest {
 @Test fun interruptedMessageDiscardsItsAudio() {
  val events = LiveProtocol.parse("""{"serverContent":{"interrupted":true,"modelTurn":{"parts":[{"inlineData":{"mimeType":"audio/pcm;rate=24000","data":"AAAAAA=="}}]}}}""")
  assertEquals(listOf(LiveEvent.Interrupted),events)
 }
 @Test fun unknownEventsAreIgnored() { assertTrue(LiveProtocol.parse("""{"futureEvent":{"value":1}}""").isEmpty()) }
 @Test fun parsesSetupTranscriptsAndCompletion() {
  val events = LiveProtocol.parse("""{"setupComplete":{},"serverContent":{"inputTranscription":{"text":"Repeat please"},"turnComplete":true}}""")
  assertEquals(listOf(LiveEvent.Ready,LiveEvent.Transcript("You","Repeat please"),LiveEvent.TurnComplete),events)
 }
 @Test(expected=IllegalArgumentException::class) fun wrongSampleRateRejected() { LiveProtocol.parse("""{"serverContent":{"modelTurn":{"parts":[{"inlineData":{"mimeType":"audio/pcm;rate=240000","data":"AAAAAA=="}}]}}}""") }
 @Test(expected=IllegalArgumentException::class) fun oddPcmRejected() { LiveProtocol.parse("""{"serverContent":{"modelTurn":{"parts":[{"inlineData":{"mimeType":"audio/pcm;rate=24000","data":"AA=="}}]}}}""") }
 @Test fun setupHasRecipeAndAudioConfigurationButNoCredential() {
  val setup = Json.parseToJsonElement(LiveProtocol.setup("sample-live", Recipes.all.first(),1)).jsonObject.getValue("setup").jsonObject
  assertEquals("models/sample-live", setup.getValue("model").jsonPrimitive.content)
  assertTrue(setup.getValue("systemInstruction").toString().contains("Current step: 2"))
  assertFalse(setup.containsKey("key"))
 }
 @Test fun progressClampsBothEndsAndLastStepFinishes() {
  val store=MemoryProgress(); val live=FakeLive();val vm=CookViewModel(store,live)
  vm.select("pasta");vm.cook(); vm.step(-100)
  assertEquals(0,vm.state.value.step)
  repeat(20){vm.step(1)}
  assertEquals(Recipes.all.first().steps.lastIndex,vm.state.value.step)
  vm.finish();assertEquals(Screen.Finished,vm.state.value.screen);assertEquals(0,store.read("pasta"));assertTrue(live.ends>0)
 }
 @Test fun progressRestoresAndInvalidStoredStepIsClamped() {
  val store=MemoryProgress();store.write("pasta",500)
  val vm=CookViewModel(store,FakeLive());vm.select("pasta")
  assertEquals(4,vm.state.value.step)
  vm.step(-1);val restored=CookViewModel(store,FakeLive());restored.select("pasta");assertEquals(3,restored.state.value.step)
 }
 @Test fun backgroundClearsCredentialsAndEndsVoice() {
  val live=FakeLive();val vm=CookViewModel(MemoryProgress(),live)
  vm.configure("local-test-value","example-live");assertTrue(vm.configured)
  vm.background();assertFalse(vm.configured);assertEquals(1,live.ends)
 }
 @Test fun recipeSwitchClearsIngredientChecks() {
  val vm=CookViewModel(MemoryProgress(),FakeLive());vm.select("pasta");vm.check(0);assertTrue(0 in vm.state.value.checked)
  vm.select("bowl");assertTrue(vm.state.value.checked.isEmpty())
 }
}
class MemoryProgress : ProgressStore {
 private val values=mutableMapOf<String,Int>()
 override fun read(recipe:String)=values[recipe]?:0
 override fun write(recipe:String,step:Int){values[recipe]=step}
}
class FakeLive : LiveConnection {
 override val state=MutableStateFlow(VoiceState());var ends=0
 override fun start(key:String,model:String,recipe:Recipe,step:Int) {}
 override fun mute(){}
 override fun updateStep(step:Int){}
 override fun end(message:String){ends++;state.value=VoiceState(message=message)}
}
