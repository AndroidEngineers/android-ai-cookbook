package com.androidengineers.pocketstories.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

@Entity(tableName = "stories")
data class StoryRecord(@PrimaryKey val id: String, val payload: String, val updated: Long)

@Dao
interface StoryDao {
    @Query("SELECT * FROM stories ORDER BY updated DESC") fun observe(): Flow<List<StoryRecord>>

    @Upsert suspend fun save(record: StoryRecord)

    @Query("DELETE FROM stories WHERE id = :id") suspend fun delete(id: String)
}

@Database(entities = [StoryRecord::class], version = 1, exportSchema = true)
abstract class StoryDatabase : RoomDatabase() {
    abstract fun stories(): StoryDao
}

interface StoryPersistence {
    val stories: Flow<List<Story>>

    suspend fun save(story: Story)

    suspend fun delete(id: String)
}

class StoryStore(private val dao: StoryDao) : StoryPersistence {
    private val json = Json { ignoreUnknownKeys = true }
    override val stories =
        dao.observe().map { records -> records.map { json.decodeFromString<Story>(it.payload) } }

    override suspend fun save(story: Story) =
        dao.save(StoryRecord(story.id, json.encodeToString(story), story.updated))

    override suspend fun delete(id: String) = dao.delete(id)
}
