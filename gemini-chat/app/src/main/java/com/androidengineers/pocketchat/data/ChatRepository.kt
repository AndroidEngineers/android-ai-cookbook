package com.androidengineers.pocketchat.data

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

/** Called on Main; app scope owns streaming across rotation, but not backgrounding. */
class ChatRepository(private val store: LibraryStore, private val service: ChatService, private val scope: CoroutineScope) {
    private val mutableLibrary = MutableStateFlow(Library())
    val library = mutableLibrary.asStateFlow()
    private val mutableReady = MutableStateFlow(false)
    val ready = mutableReady.asStateFlow()
    private val mutableNotice = MutableStateFlow<String?>(null)
    val notice = mutableNotice.asStateFlow()
    private val mutableConfigured = MutableStateFlow(false)
    val configured = mutableConfigured.asStateFlow()
    private val mutableActive = MutableStateFlow<String?>(null)
    val active = mutableActive.asStateFlow()
    private var key = ""
    private var model = "gemini-2.5-flash"
    private var generation: Job? = null
    private var attempt: String? = null
    private val writes = Channel<Library>(Channel.CONFLATED)
    init {
        scope.launch {
            try {
                val loaded = store.load()
                mutableLibrary.value = loaded.copy(conversations = loaded.conversations.map { c -> c.copy(turns = c.turns.map { if(it.status == ReplyStatus.STREAMING) it.copy(status = ReplyStatus.INTERRUPTED) else it }) })
                mutableReady.value = true
                writes.trySend(mutableLibrary.value)
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { mutableNotice.value = "Could not open local history. Restart the app; existing data has not been overwritten." }
        }
        scope.launch {
            for (snapshot in writes) {
                try { store.save(snapshot) }
                catch (e: CancellationException) { throw e }
                catch (_: Exception) { mutableNotice.value = "Local save failed. Keep the app open and free some storage." }
            }
        }
    }
    fun configure(value: String, modelName: String): Boolean {
        if (value.isBlank() || !Regex("[A-Za-z0-9._-]+").matches(modelName)) return false
        stop(); key = value.trim(); model = modelName; mutableConfigured.value = true
        mutableNotice.value = "Key ready for this session. It will be verified on your first request."
        return true
    }
    fun background() { stop(); key = ""; mutableConfigured.value = false }
    fun dismissNotice() { mutableNotice.value = null }
    private fun update(value: Library, persist: Boolean = true) { mutableLibrary.value = value; if(persist) writes.trySend(value) }
    fun create(): String {
        stop()
        val c = Conversation()
        update(library.value.copy(conversations = listOf(c) + library.value.conversations))
        return c.id
    }
    fun rename(id: String, title: String) {
        if(title.isBlank()) return
        update(library.value.copy(conversations = library.value.conversations.map { if(it.id == id) it.copy(title = title.trim().take(80)) else it }))
    }
    fun delete(id: String) {
        if(active.value == id) stop()
        update(library.value.copy(conversations = library.value.conversations.filterNot { it.id == id }))
    }
    fun clear() { stop(); update(Library()) }
    fun saveAnswer(c: Conversation, turn: Turn) {
        if(turn.status != ReplyStatus.COMPLETE || turn.answer.isBlank()) return
        val note = SavedAnswer(turn.id, c.title, turn.answer, c.id)
        update(library.value.copy(saved = listOf(note) + library.value.saved.filterNot { it.id == note.id }))
        mutableNotice.value = "Saved to Worth keeping."
    }
    fun removeSaved(id: String) { update(library.value.copy(saved = library.value.saved.filterNot { it.id == id })) }
    private fun replace(conversation: String, turn: Turn, persist: Boolean = true) {
        update(library.value.copy(conversations = library.value.conversations.map { c -> if(c.id == conversation) c.copy(turns = c.turns.map { if(it.id == turn.id) turn else it }) else c }), persist)
    }
    fun send(id: String, text: String, retry: Boolean = false): Boolean {
        if(!ready.value || active.value != null || text.isBlank()) return false
        if(key.isBlank()) { mutableNotice.value = "Add your Gemini API key in Settings first."; return false }
        val c = library.value.conversations.find { it.id == id } ?: return false
        val old = if(retry) c.turns.lastOrNull()?.takeIf { it.status != ReplyStatus.COMPLETE } ?: return false else null
        val previous = if(old != null) c.turns.dropLast(1) else c.turns
        val context = try { buildContext(previous, text.trim()) } catch(e: IllegalArgumentException) { mutableNotice.value = e.message; return false }
        if(context.omitted) mutableNotice.value = "Earlier messages were left out to keep this request within the context budget."
        val turn = Turn(id = old?.id ?: newId(), prompt = text.trim())
        val token = newId(); attempt = token; mutableActive.value = id
        update(library.value.copy(conversations = library.value.conversations.map { if(it.id == id) it.copy(title = if(it.turns.isEmpty()) text.trim().take(48) else it.title, turns = previous + turn) else it }))
        val requestKey = key; val requestModel = model
        generation = scope.launch {
            var current = turn
            var lastWrite = System.nanoTime()
            var finish: String? = null
            try {
                service.stream(requestKey, requestModel, context.parts).collect { event ->
                    if(attempt != token) return@collect
                    when(event) {
                        is ChatEvent.Text -> {
                            if(current.answer.length + event.value.length > 64000) throw ChatFailure("Answer exceeded the sample's size limit. Try a smaller question.")
                            current = current.copy(answer = current.answer + event.value)
                            val persist = System.nanoTime() - lastWrite > 250_000_000
                            replace(id, current, persist); if(persist) lastWrite = System.nanoTime()
                        }
                        is ChatEvent.Finished -> finish = event.reason
                    }
                }
                if(attempt == token) {
                    if(finish != "STOP" || current.answer.isBlank()) throw ChatFailure(when(finish) { "MAX_TOKENS" -> "The answer reached its output limit. Retry with a narrower question."; "SAFETY" -> "Gemini could not answer this request. Try rephrasing it."; else -> "The response ended without a complete answer. You can retry." })
                    replace(id, current.copy(status = ReplyStatus.COMPLETE))
                }
            } catch(e: CancellationException) { throw e }
            catch(e: Exception) { if(attempt == token) replace(id, current.copy(status = ReplyStatus.FAILED, error = if(e is ChatFailure) e.message else "Connection interrupted. Check your network and retry.")) }
            finally { if(attempt == token) { attempt = null; mutableActive.value = null; writes.trySend(library.value) } }
        }
        return true
    }
    fun stop() {
        val id = active.value
        attempt = null; generation?.cancel(); generation = null; mutableActive.value = null
        val turn = library.value.conversations.find { it.id == id }?.turns?.lastOrNull()
        if(id != null && turn?.status == ReplyStatus.STREAMING) replace(id, turn.copy(status = ReplyStatus.STOPPED))
    }
}
