package com.androidengineers.pocketchat

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidengineers.pocketchat.data.*
import com.androidengineers.pocketchat.ui.PocketChatApp
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatJourneyTest {
    @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
    private val scope=CoroutineScope(SupervisorJob()+Dispatchers.Main.immediate)
    private val streams=mutableListOf<Channel<ChatEvent>>()
    private val requests=mutableListOf<List<ChatPart>>()
    private lateinit var repo:ChatRepository
    private lateinit var vm:ChatViewModel
    @Before fun setup() {
        compose.runOnUiThread {
            val store=object:LibraryStore { var data=Library();override suspend fun load()=data;override suspend fun save(library:Library){data=library} }
            val service=object:ChatService { override fun stream(key:String,model:String,parts:List<ChatPart>):Flow<ChatEvent> { requests+=parts;val c=Channel<ChatEvent>(Channel.UNLIMITED);streams+=c;return c.receiveAsFlow() } }
            repo=ChatRepository(store,service,scope);vm=ChatViewModel(repo,SavedStateHandle())
        }
        compose.setContent { PocketChatApp(vm) }
        compose.waitUntil(5000){repo.ready.value}
    }
    @After fun teardown(){scope.cancel()}
    @Test fun sendStopRetryFollowupSaveAndReopen() {
        compose.runOnIdle { repo.configure("test-only","gemini-2.5-flash") }
        compose.onNodeWithText("New chat").performClick()
        compose.onNodeWithText("Ask a follow-up…").performTextInput("My cat is Luna")
        compose.onNodeWithContentDescription("Send message").assertIsEnabled().performClick()
        compose.waitUntil(5000){streams.size==1}
        runBlocking{streams[0].send(ChatEvent.Text("Luna is"))}
        compose.onNodeWithText("Luna is").assertIsDisplayed()
        compose.onNodeWithText(" Stop generating").performClick()
        compose.onNodeWithText("Stopped").assertExists()
        compose.onNodeWithText(" Retry").performClick()
        compose.waitUntil(5000){streams.size==2}
        runBlocking{streams[1].send(ChatEvent.Text("Luna is a lovely name."));streams[1].send(ChatEvent.Finished("STOP"));streams[1].close()}
        compose.waitUntil(5000){repo.active.value==null}
        compose.onNodeWithText(" Save").performClick()
        compose.onNodeWithText("Ask a follow-up…").performTextInput("What is her name?")
        compose.onNodeWithContentDescription("Send message").performClick()
        compose.waitUntil(5000){streams.size==3}
        compose.runOnIdle { Assert.assertEquals("Luna is a lovely name.",requests.last()[1].text) }
        runBlocking{streams[2].send(ChatEvent.Text("Her name is Luna."));streams[2].send(ChatEvent.Finished("STOP"));streams[2].close()}
        compose.waitUntil(5000){repo.active.value==null}
        compose.onNodeWithContentDescription("Back to chats").performClick()
        compose.onNodeWithText("Saved").performClick()
        compose.onNodeWithText("Worth keeping").assertIsDisplayed()
        compose.onNodeWithText("Luna is a lovely name.").assertIsDisplayed()
        compose.onNodeWithText("Open conversation →").performClick()
        compose.onNodeWithText("Her name is Luna.").assertExists()
    }
    @Test fun missingCredentialsPreservesDraftAndShowsSetup() {
        compose.onNodeWithText("New chat").performClick()
        compose.onNodeWithText("Ask a follow-up…").performTextInput("Help me learn")
        compose.onNodeWithContentDescription("Send message").performClick()
        compose.onNodeWithText("Help me learn").assertIsDisplayed()
        compose.onNodeWithText("Add your Gemini key to start chatting →").performClick()
        compose.onNodeWithText("Gemini API key").assertExists()
        Assert.assertTrue(requests.isEmpty())
    }
    @Test fun draftsSurviveSwitching() {
        compose.onNodeWithText("New chat").performClick()
        compose.onNodeWithText("Ask a follow-up…").performTextInput("A question in progress")
        compose.onNodeWithContentDescription("Back to chats").performClick()
        compose.onNodeWithText("New conversation").performClick()
        compose.onNodeWithText("A question in progress").assertIsDisplayed()
    }
}
