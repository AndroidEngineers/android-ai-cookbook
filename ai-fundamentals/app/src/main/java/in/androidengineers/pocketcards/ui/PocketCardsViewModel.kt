package `in`.androidengineers.pocketcards.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.androidengineers.pocketcards.data.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PocketState(
    val decks: List<Deck> = emptyList(), val loading: Boolean = true,
    val notes: String = "", val title: String = "", val draft: List<Flashcard> = emptyList(),
    val generating: Boolean = false, val saving: Boolean = false, val error: String? = null,
    val draftOrigin: String = "Created by you", val editingId: String? = null, val revision: Int = 0
)
class PocketCardsViewModel(private val repository: DeckRepository, private val generator: CardGenerator) : ViewModel() {
    private val mutable = MutableStateFlow(PocketState())
    val state = mutable.asStateFlow()
    val aiAvailable get() = generator.available
    private var generation: Job? = null
    private var request = 0
    init { reload() }
    fun reload() { viewModelScope.launch {
        mutable.update { it.copy(loading = true, error = null) }
        try { val decks = repository.load(); mutable.update { it.copy(decks = decks, loading = false) } }
        catch (_: Exception) { mutable.update { it.copy(loading = false, error = "Your decks could not be read. Retry before making changes; existing files have been kept.") } }
    } }
    fun notes(value: String) { if (!state.value.generating) mutable.update { it.copy(notes = value.take(CardValidation.MAX_NOTES), error = null) } }
    fun title(value: String) { if (!state.value.saving) mutable.update { it.copy(title = value.take(80)) } }
    fun dismissError() { mutable.update { it.copy(error = null) } }
    fun startNew() { cancel(); mutable.update { it.copy(notes = "", title = "", draft = emptyList(), editingId = null, draftOrigin = "Created by you", error = null) } }
    fun manual() { cancel(); mutable.update { it.copy(draft = listOf(Flashcard("", "")), editingId = null, draftOrigin = "Created by you", error = null) } }
    fun edit(deck: Deck) { cancel(); mutable.update { it.copy(title = deck.title, draft = deck.cards, editingId = deck.id.takeUnless { id -> id == sampleDeck.id }, draftOrigin = deck.origin, error = null) } }
    fun updateCard(index: Int, question: String, answer: String) {
        if (state.value.saving) return
        mutable.update { it.copy(draft = it.draft.mapIndexed { i, c -> if (i == index) Flashcard(question.take(300), answer.take(1500)) else c }, error = null) }
    }
    fun addCard() { if (!state.value.saving && state.value.draft.size < 10) mutable.update { it.copy(draft = it.draft + Flashcard("", "")) } }
    fun removeCard(index: Int) { if (!state.value.saving) mutable.update { it.copy(draft = it.draft.filterIndexed { i, _ -> i != index }) } }
    fun generate() {
        if (state.value.generating) return
        val notes = state.value.notes.trim()
        if (!aiAvailable) { mutable.update { it.copy(error = "AI generation needs Firebase setup. You can still create cards yourself or study the sample deck.") }; return }
        if (notes.length < 40) { mutable.update { it.copy(error = "Add at least 40 characters of notes so there is something to learn from.") }; return }
        val id = ++request
        mutable.update { it.copy(generating = true, error = null) }
        generation = viewModelScope.launch {
            try {
                val cards = CardValidation.validate(generator.generate(notes))
                if (id == request) mutable.update { it.copy(draft = cards, title = it.title.ifBlank { "My study notes" }, generating = false, draftOrigin = "AI-generated · reviewed by you", editingId = null, revision = it.revision + 1) }
            } catch (_: TimeoutCancellationException) {
                if (id == request) mutable.update { it.copy(generating = false, error = "That took too long. Your notes are still here. Please try again.") }
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { if (id == request) mutable.update { it.copy(generating = false, error = "We couldn't create valid cards. Check your connection and Firebase setup, or try different notes.") } }
        }
    }
    fun cancel() { request++; generation?.cancel(); mutable.update { it.copy(generating = false) } }
    fun save() {
        if (state.value.saving) return
        val s = state.value
        val deck = try { Deck(id = s.editingId ?: java.util.UUID.randomUUID().toString(), title = CardValidation.title(s.title), cards = CardValidation.validate(s.draft), origin = s.draftOrigin) }
        catch (e: IllegalArgumentException) { mutable.update { it.copy(error = e.message) }; return }
        mutable.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            try { val all = repository.save(deck); mutable.update { it.copy(decks = all, saving = false, draft = emptyList(), editingId = null, revision = it.revision + 1) } }
            catch (_: Exception) { mutable.update { it.copy(saving = false, error = "Couldn't save this deck. Your cards are still here; please retry.") } }
        }
    }
    fun delete(id: String) { viewModelScope.launch {
        try { val all = repository.delete(id); mutable.update { it.copy(decks = all, error = null) } }
        catch (_: Exception) { mutable.update { it.copy(error = "Couldn't delete this deck. Please retry.") } }
    } }
}
