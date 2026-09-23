package com.androidengineers.pocketchat.data

import kotlinx.serialization.Serializable
import kotlinx.coroutines.flow.Flow
import java.util.UUID

fun newId(): String = UUID.randomUUID().toString()
@Serializable enum class ReplyStatus { COMPLETE, STREAMING, STOPPED, FAILED, INTERRUPTED }
@Serializable data class Turn(val id: String = newId(), val prompt: String, val answer: String = "", val status: ReplyStatus = ReplyStatus.STREAMING, val error: String? = null)
@Serializable data class Conversation(val id: String = newId(), val title: String = "New conversation", val turns: List<Turn> = emptyList())
@Serializable data class SavedAnswer(val id: String, val title: String, val text: String, val conversationId: String)
@Serializable data class Library(val conversations: List<Conversation> = emptyList(), val saved: List<SavedAnswer> = emptyList())
data class ChatPart(val role: String, val text: String)
data class ContextWindow(val parts: List<ChatPart>, val omitted: Boolean)

/** A conservative character budget, not an exact tokenizer. Keep whole completed pairs. */
fun buildContext(turns: List<Turn>, prompt: String, maxChars: Int = 24000): ContextWindow {
    require(prompt.length <= 6000 && prompt.length <= maxChars) { "Keep your question under 6,000 characters." }
    var remaining = maxChars - prompt.length
    val selected = mutableListOf<Turn>()
    val eligible = turns.filter { it.status == ReplyStatus.COMPLETE }
    for (turn in eligible.asReversed()) {
        val size = turn.prompt.length + turn.answer.length
        if (size > remaining) break
        selected.add(0, turn); remaining -= size
    }
    return ContextWindow(selected.flatMap { listOf(ChatPart("user", it.prompt), ChatPart("model", it.answer)) } + ChatPart("user", prompt), selected.size < eligible.size)
}

sealed interface ChatEvent {
    data class Text(val value: String): ChatEvent
    data class Finished(val reason: String): ChatEvent
}
interface ChatService { fun stream(key: String, model: String, parts: List<ChatPart>): Flow<ChatEvent> }
class ChatFailure(message: String): Exception(message)
interface LibraryStore { suspend fun load(): Library; suspend fun save(library: Library) }
