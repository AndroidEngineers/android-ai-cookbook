package com.androidengineers.pocketchat.data
import kotlinx.coroutines.flow.flow
fun createChatService(): ChatService = object: ChatService {
    override fun stream(key: String, model: String, parts: List<ChatPart>) = flow<ChatEvent> { throw ChatFailure("Cloud chat needs a production backend. Local history is available.") }
}
