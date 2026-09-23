package com.androidengineers.pocketchat.data

import androidx.room.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

// Each conversation is an aggregate. The sample does not need cross-message SQL queries yet.
@Entity data class LibraryRow(@PrimaryKey val id: Int = 1, val json: String)
@Dao interface LibraryDao {
    @Query("SELECT * FROM LibraryRow WHERE id = 1") suspend fun read(): LibraryRow?
    @Upsert suspend fun write(row: LibraryRow)
}
@Database(entities = [LibraryRow::class], version = 1, exportSchema = true)
abstract class ChatDatabase: RoomDatabase() { abstract fun library(): LibraryDao }
class RoomLibraryStore(private val dao: LibraryDao): LibraryStore {
    private val json = Json { ignoreUnknownKeys = true }
    override suspend fun load(): Library = dao.read()?.let { json.decodeFromString<Library>(it.json) } ?: Library()
    override suspend fun save(library: Library) = dao.write(LibraryRow(json = json.encodeToString(library)))
}
