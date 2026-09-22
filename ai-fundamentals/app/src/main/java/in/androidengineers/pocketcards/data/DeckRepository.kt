package `in`.androidengineers.pocketcards.data

import android.util.AtomicFile
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface DeckRepository {
    suspend fun load(): List<Deck>
    suspend fun save(deck: Deck): List<Deck>
    suspend fun delete(id: String): List<Deck>
}
/** One small local collection; atomic replacement avoids partially written decks. */
class FileDeckRepository(file: File) : DeckRepository {
    private val file = AtomicFile(file)
    private val mutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true }
    private fun read(): List<Deck> {
        if (!file.baseFile.exists() && !File(file.baseFile.path + ".bak").exists()) return emptyList()
        val decks = file.openRead().bufferedReader().use { json.decodeFromString<List<Deck>>(it.readText()) }
        require(decks.map { it.id }.distinct().size == decks.size) { "Duplicate deck IDs" }
        return decks.map { it.copy(title = CardValidation.title(it.title), cards = CardValidation.validate(it.cards)) }
    }
    private fun write(decks: List<Deck>) {
        val out = file.startWrite()
        try { out.write(json.encodeToString(decks).toByteArray()); file.finishWrite(out) }
        catch (e: Exception) { file.failWrite(out); throw e }
    }
    override suspend fun load() = withContext(Dispatchers.IO) { mutex.withLock { read() } }
    override suspend fun save(deck: Deck) = withContext(Dispatchers.IO) { mutex.withLock {
        val valid = deck.copy(title = CardValidation.title(deck.title), cards = CardValidation.validate(deck.cards))
        val all = listOf(valid) + read().filterNot { it.id == valid.id }
        write(all); all
    } }
    override suspend fun delete(id: String) = withContext(Dispatchers.IO) { mutex.withLock { val all = read().filterNot { it.id == id }; write(all); all } }
}
