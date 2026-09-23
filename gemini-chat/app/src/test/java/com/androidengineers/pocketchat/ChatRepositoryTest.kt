package com.androidengineers.pocketchat

import com.androidengineers.pocketchat.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatRepositoryTest {
    class Store(var value: Library = Library()): LibraryStore {
        override suspend fun load() = value
        override suspend fun save(library: Library) { value=library }
    }
    class Service: ChatService {
        val requests=mutableListOf<List<ChatPart>>()
        val streams=mutableListOf<Channel<ChatEvent>>()
        override fun stream(key:String,model:String,parts:List<ChatPart>): Flow<ChatEvent> {
            requests+=parts
            val channel=Channel<ChatEvent>(Channel.UNLIMITED);streams+=channel
            return channel.receiveAsFlow()
        }
    }
    @Test fun completionPersistsAndFollowupIncludesHistory() = runTest {
        val store=Store();val service=Service();val r=ChatRepository(store,service,backgroundScope)
        runCurrent();r.configure("test-key","gemini-2.5-flash");val id=r.create()
        assertTrue(r.send(id,"Remember my cat is Luna"));runCurrent()
        service.streams[0].send(ChatEvent.Text("Hello Luna"));service.streams[0].send(ChatEvent.Finished("STOP"));service.streams[0].close();runCurrent()
        assertEquals(ReplyStatus.COMPLETE,store.value.conversations.single().turns.single().status)
        r.send(id,"What is her name?");runCurrent()
        assertEquals(listOf("user","model","user"),service.requests.last().map{it.role})
        assertEquals("Hello Luna",service.requests.last()[1].text)
    }
    @Test fun stopPreservesPartialAndRejectsDuplicateSend() = runTest {
        val service=Service();val r=ChatRepository(Store(),service,backgroundScope)
        runCurrent();r.configure("test","gemini-2.5-flash");val id=r.create()
        assertTrue(r.send(id,"Question"));assertFalse(r.send(id,"Duplicate"));runCurrent()
        service.streams[0].send(ChatEvent.Text("Partial"));runCurrent();r.stop();runCurrent()
        val turn=r.library.value.conversations.single().turns.single()
        assertEquals("Partial",turn.answer);assertEquals(ReplyStatus.STOPPED,turn.status);assertNull(r.active.value)
    }
    @Test fun retryDoesNotDuplicateUserTurnOrIncludeStoppedAnswer() = runTest {
        val service=Service();val r=ChatRepository(Store(),service,backgroundScope)
        runCurrent();r.configure("test","gemini-2.5-flash");val id=r.create();r.send(id,"Question");runCurrent();r.stop();runCurrent()
        assertTrue(r.send(id,"Question",retry=true));runCurrent()
        assertEquals(1,r.library.value.conversations.single().turns.size)
        assertEquals(listOf(ChatPart("user","Question")),service.requests.last())
    }
    @Test fun startupMarksInterruptedAndBackgroundClearsKey() = runTest {
        val c=Conversation(turns=listOf(Turn(prompt="Q",answer="Partial")))
        val r=ChatRepository(Store(Library(listOf(c))),Service(),backgroundScope);runCurrent()
        assertEquals(ReplyStatus.INTERRUPTED,r.library.value.conversations.single().turns.single().status)
        r.configure("test","gemini-2.5-flash");r.background()
        assertFalse(r.configured.value);assertFalse(r.send(c.id,"Next"))
    }
    @Test fun eofWithoutFinishIsFailureAndPreservesPartial() = runTest {
        val service=Service();val r=ChatRepository(Store(),service,backgroundScope)
        runCurrent();r.configure("test","gemini-2.5-flash");val id=r.create();r.send(id,"Question");runCurrent()
        service.streams[0].send(ChatEvent.Text("Partial"));service.streams[0].close();runCurrent()
        val turn=r.library.value.conversations.single().turns.single()
        assertEquals("Partial",turn.answer);assertEquals(ReplyStatus.FAILED,turn.status)
    }
    @Test fun deletingSourceKeepsSavedCopyAndClearRemovesEverything() = runTest {
        val t=Turn(prompt="Q",answer="A",status=ReplyStatus.COMPLETE);val c=Conversation(turns=listOf(t))
        val store=Store(Library(listOf(c)));val r=ChatRepository(store,Service(),backgroundScope);runCurrent()
        r.saveAnswer(c,t);r.saveAnswer(c,t);assertEquals(1,r.library.value.saved.size)
        r.delete(c.id);runCurrent();assertTrue(store.value.conversations.isEmpty());assertEquals("A",store.value.saved.single().text)
        r.clear();runCurrent();assertEquals(Library(),store.value)
    }
    @Test fun failedLoadNeverOverwritesExistingData() = runTest {
        var writes=0
        val store=object:LibraryStore { override suspend fun load():Library=error("broken");override suspend fun save(library:Library){writes++} }
        val r=ChatRepository(store,Service(),backgroundScope);runCurrent()
        assertFalse(r.ready.value);assertEquals(0,writes);assertNotNull(r.notice.value)
    }
    @Test fun contextKeepsWholeRecentCompletedPairsAndSignalsOmission() {
        val turns=listOf(Turn(prompt="1111",answer="2222",status=ReplyStatus.COMPLETE),Turn(prompt="bad",answer="partial",status=ReplyStatus.STOPPED),Turn(prompt="33",answer="44",status=ReplyStatus.COMPLETE))
        val result=buildContext(turns,"next",10)
        assertTrue(result.omitted);assertEquals(listOf("33","44","next"),result.parts.map{it.text})
    }
    @Test fun oversizedPromptIsRejected() {
        assertThrows(IllegalArgumentException::class.java){buildContext(emptyList(),"x".repeat(6001))}
    }
}
