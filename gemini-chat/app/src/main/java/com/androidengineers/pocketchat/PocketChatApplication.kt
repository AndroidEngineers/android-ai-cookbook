package com.androidengineers.pocketchat

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.room.Room
import com.androidengineers.pocketchat.data.*
import kotlinx.coroutines.*

class PocketChatApplication: Application(), DefaultLifecycleObserver {
    lateinit var repository: ChatRepository
        private set
    override fun onCreate() {
        super<Application>.onCreate()
        val database = Room.databaseBuilder(this, ChatDatabase::class.java, "pocketchat.db").build()
        repository = ChatRepository(RoomLibraryStore(database.library()), createChatService(), CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate))
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }
    override fun onStop(owner: LifecycleOwner) { repository.background() }
}
