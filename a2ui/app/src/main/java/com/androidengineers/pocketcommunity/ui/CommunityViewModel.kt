package com.androidengineers.pocketcommunity.ui

import androidx.a2ui.compose.runtime.A2uiMessageParser
import androidx.a2ui.compose.ui.A2uiCatalog
import androidx.a2ui.compose.ui.A2uiMessageProcessor
import androidx.a2ui.engine.model.A2uiCoreSurfaceModel
import androidx.a2ui.model.processor.processInput
import androidx.a2ui.model.protocol.A2uiClientErrorMessage
import androidx.a2ui.model.protocol.A2uiClientEventMessage
import androidx.a2ui.model.protocol.A2uiDataPath
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidengineers.pocketcommunity.data.*
import java.util.UUID
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.*

data class ConversationItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val user: Boolean = false,
    val surfaceId: String? = null,
)

data class CommunityState(
    val items: List<ConversationItem> = emptyList(),
    val busy: Boolean = false,
    val error: String? = null,
    val trace: List<String> = emptyList(),
    val endpoint: String = "",
    val saved: Set<String> = emptySet(),
    val pendingUrl: String? = null,
    val canRetry: Boolean = false,
)

class CommunityViewModel(
    private val repository: CommunityRepository,
    catalog: A2uiCatalog,
    initialEndpoint: String = "",
    private val saveEndpoint: (String) -> Unit = {},
) : ViewModel() {
    private val mutable = MutableStateFlow(CommunityState(endpoint = initialEndpoint))
    val state = mutable.asStateFlow()
    private val processor = A2uiMessageProcessor(listOf(catalog))
    private val parser = A2uiMessageParser()
    val surfaces = processor.activeSurfaces
    private var events: List<CommunityEvent> = emptyList()
    private var selected: CommunityEvent? = null
    private var lastRequest: AgentRequest? = null
    private var job: Job? = null
    private var generation = 0
    private val componentHistory = linkedMapOf<String, JsonArray>()

    init {
        viewModelScope.launch { processor.collectMessages() }
        viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            processor.outboundEvents.collect { event ->
                trace("OUT $event")
                when (event) {
                    is A2uiClientEventMessage -> action(event)
                    is A2uiClientErrorMessage ->
                        mutable.update {
                            it.copy(
                                error = "Unable to render the agent response: ${event.message}",
                                canRetry = lastRequest != null,
                                items =
                                    it.items.filterNot { item ->
                                        item.surfaceId == event.surfaceId
                                    },
                            )
                        }
                }
            }
        }
    }

    fun configure(endpoint: String) {
        val clean = endpoint.trim()
        if (clean.isNotEmpty() && !validEndpoint(clean)) {
            mutable.update { it.copy(error = "Enter a valid agent endpoint.") }
            return
        }
        if (clean != state.value.endpoint && state.value.items.isNotEmpty()) {
            mutable.update {
                it.copy(error = "Start a new conversation before changing the agent endpoint.")
            }
            return
        }
        saveEndpoint(clean)
        mutable.update { it.copy(endpoint = clean, error = null) }
    }

    fun reset() {
        generation++
        job?.cancel()
        componentHistory.clear()
        selected = null
        events = emptyList()
        lastRequest = null
        surfaces.value.forEach { surface ->
            processor.processInput(
                parser,
                """{"version":"v0.9","deleteSurface":{"surfaceId":"${surface.id}"}}""",
            )
        }
        mutable.value = CommunityState(endpoint = state.value.endpoint, saved = state.value.saved)
    }

    fun dismissError() {
        mutable.update { it.copy(error = null) }
    }

    fun consumeUrl() {
        mutable.update { it.copy(pendingUrl = null) }
    }

    private fun trace(value: String) {
        mutable.update { it.copy(trace = (it.trace + value).takeLast(80)) }
    }

    private fun append(text: String, user: Boolean = false) {
        mutable.update { it.copy(items = it.items + ConversationItem(text = text, user = user)) }
    }

    private fun history() =
        JsonArray(
            state.value.items
                .filter { it.surfaceId == null }
                .takeLast(12)
                .map {
                    buildJsonObject {
                        put("role", if (it.user) "user" else "assistant")
                        put("text", it.text.take(2000))
                    }
                }
        )

    private fun surfaceData(): JsonObject = buildJsonObject {
        surfaces.value.takeLast(6).forEach { surface ->
            val core = surface as? A2uiCoreSurfaceModel
            if (core != null) put(surface.id, jsonValue(core.dataModel[A2uiDataPath("/")]))
        }
    }

    fun send(prompt: String) {
        if (prompt.isBlank() || state.value.busy) return
        if (state.value.endpoint.isBlank()) {
            mutable.update {
                it.copy(
                    error = "Connect your Gemini agent in Setup before starting a conversation."
                )
            }
            return
        }
        val request =
            AgentRequest(
                prompt.take(2000),
                selected?.id,
                UUID.randomUUID().toString(),
                history(),
                surfaceData = surfaceData(),
                previousComponents = previousComponents(),
            )
        append(request.prompt, true)
        submit(request)
    }

    private fun previousComponents() =
        JsonArray(
            componentHistory.entries.toList().takeLast(3).map { (id, components) ->
                buildJsonObject {
                    put("surfaceId", id)
                    put("components", components)
                }
            }
        )

    fun retry() {
        if (!state.value.busy)
            lastRequest?.let { submit(it.copy(surfaceId = UUID.randomUUID().toString())) }
    }

    private fun submit(request: AgentRequest) {
        lastRequest = request
        val epoch = generation
        val endpoint = state.value.endpoint
        mutable.update { it.copy(busy = true, error = null, canRetry = false) }
        job = viewModelScope.launch {
            try {
                events = repository.discover()
                val reply = repository.ask(endpoint, request)
                ensureActive()
                if (epoch != generation) return@launch
                require(reply.messages.size in 1..24) { "Agent returned an invalid message count." }
                reply.messages.forEach { raw ->
                    val message = Json.parseToJsonElement(raw).jsonObject
                    val body =
                        (message["createSurface"]
                                ?: message["updateDataModel"]
                                ?: message["updateComponents"])
                            ?.jsonObject ?: error("Unsupported agent message.")
                    require(body.string("surfaceId") == request.surfaceId) {
                        "Unexpected surface in agent response."
                    }
                }
                append(reply.text)
                mutable.update {
                    it.copy(items = it.items + ConversationItem(surfaceId = request.surfaceId))
                }
                reply.messages.forEach { raw ->
                    val update =
                        Json.parseToJsonElement(raw).jsonObject["updateComponents"]?.jsonObject
                    (update?.get("components") as? JsonArray)?.let {
                        componentHistory[request.surfaceId] = it
                    }
                    trace("IN $raw")
                    processor.processInput(parser, raw)
                    yield()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (epoch == generation)
                    mutable.update {
                        it.copy(
                            error = e.message ?: "Agent unavailable. Please retry.",
                            canRetry = true,
                        )
                    }
            } finally {
                if (epoch == generation) mutable.update { it.copy(busy = false) }
            }
        }
    }

    private fun action(event: A2uiClientEventMessage) {
        if (state.value.busy) return
        val id = event.context["eventId"] as? String
        val e = events.find { it.id == id }
        if (e == null) {
            mutable.update {
                it.copy(error = "This event is no longer available. Ask for current events.")
            }
            return
        }
        selected = e
        when (event.type) {
            "website",
            "directions" -> {
                val url = if (event.type == "website") e.website else e.maps
                if (url.startsWith("https://")) mutable.update { it.copy(pendingUrl = url) }
                else
                    mutable.update { it.copy(error = "No HTTPS link is available for this event.") }
            }
            "save" -> {
                mutable.update { it.copy(saved = it.saved + e.id) }
                append("Saved for this session: ${e.title}. Registration is separate.")
            }
            "venue",
            "prepare",
            "refine" -> {
                val prompt =
                    when (event.type) {
                        "venue" -> "Show the venue for ${e.title}"
                        "prepare" -> "Help me prepare for ${e.title}"
                        else ->
                            "Update the preparation advice using my current checklist selections."
                    }
                val action = buildJsonObject {
                    put("name", event.type)
                    put("surfaceId", event.surfaceId)
                    put("componentId", event.componentId)
                    put("context", jsonValue(event.context))
                }
                val request =
                    AgentRequest(
                        prompt,
                        e.id,
                        UUID.randomUUID().toString(),
                        history(),
                        action,
                        surfaceData(),
                        previousComponents(),
                    )
                append(prompt, true)
                submit(request)
            }
        }
    }
}

private fun jsonValue(value: Any?): JsonElement =
    when (value) {
        null -> JsonNull
        is Boolean -> JsonPrimitive(value)
        is Number -> JsonPrimitive(value)
        is String -> JsonPrimitive(value)
        is Map<*, *> ->
            JsonObject(value.entries.associate { it.key.toString() to jsonValue(it.value) })
        is List<*> -> JsonArray(value.map(::jsonValue))
        else -> JsonNull
    }
