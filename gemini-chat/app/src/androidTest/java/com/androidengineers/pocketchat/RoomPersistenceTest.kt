package com.androidengineers.pocketchat

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.Context
import com.androidengineers.pocketchat.data.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomPersistenceTest {
    @Test fun savedConversationAndAnswerSurviveDatabaseReopen() = runBlocking {
        val context=ApplicationProvider.getApplicationContext<Context>()
        val name="pocketchat-test-${newId()}.db"
        val expected=Library(listOf(Conversation(id="conversation",title="Garden",turns=listOf(Turn(id="turn",prompt="Basil?",answer="A sunny spot.",status=ReplyStatus.COMPLETE)))),listOf(SavedAnswer("turn","Garden","A sunny spot.","conversation")))
        try {
            val first=Room.databaseBuilder(context,ChatDatabase::class.java,name).build()
            try { RoomLibraryStore(first.library()).save(expected) } finally { first.close() }
            val second=Room.databaseBuilder(context,ChatDatabase::class.java,name).build()
            try { assertEquals(expected,RoomLibraryStore(second.library()).load()) } finally { second.close() }
        } finally { context.deleteDatabase(name) }
    }
}
