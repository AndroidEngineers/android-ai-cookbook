package com.androidengineers.pocketstories

import android.app.Application
import androidx.room.Room
import com.androidengineers.pocketstories.data.*

class StoriesApplication : Application() {
    val database by lazy {
        Room.databaseBuilder(this, StoryDatabase::class.java, "stories.db").build()
    }
    val stories by lazy { StoryStore(database.stories()) }
    val model by lazy { ModelStore(this) }
    val images by lazy { ImageStore(this) }
    val generator by lazy { LocalStoryGenerator(model, cacheDir) }
}
