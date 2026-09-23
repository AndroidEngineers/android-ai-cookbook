package com.androidengineers.pocketchat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import com.androidengineers.pocketchat.data.ChatRepository

class ChatViewModel(val repository: ChatRepository, private val savedState: SavedStateHandle): ViewModel() {
    val selected = savedState.getStateFlow<String?>("conversation", null)
    val draft = savedState.getStateFlow("draft", "")
    fun draft(text: String) { savedState["draft"] = text.take(6000); selected.value?.let { savedState["draft:$it"] = text.take(6000) } }
    fun open(id: String?) { if(selected.value != id) { repository.stop(); savedState["draft"] = id?.let { savedState.get<String>("draft:$it") } ?: "" }; savedState["conversation"] = id }
    fun newChat(prompt: String = "") { open(repository.create()); draft(prompt) }
    fun send() { val id = selected.value ?: return; if(repository.send(id, draft.value)) draft("") }
}
