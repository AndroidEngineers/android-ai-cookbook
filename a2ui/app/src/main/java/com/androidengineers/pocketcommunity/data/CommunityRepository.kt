package com.androidengineers.pocketcommunity.data

import com.androidengineers.pocketcommunity.BuildConfig
import java.net.URI
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

fun JsonObject.string(key: String) = (get(key) as? JsonPrimitive)?.contentOrNull.orEmpty()

data class CommunityEvent(
    val id: String,
    val title: String,
    val description: String,
    val city: String,
    val date: String,
    val venue: String,
    val address: String,
    val website: String,
    val maps: String,
    val topics: String,
    val entry: String,
    val sponsored: Boolean,
)

data class AgentReply(val text: String, val messages: List<String>)

data class AgentRequest(
    val prompt: String,
    val eventId: String?,
    val surfaceId: String,
    val history: JsonArray = JsonArray(emptyList()),
    val action: JsonObject? = null,
    val surfaceData: JsonObject = JsonObject(emptyMap()),
    val previousComponents: JsonArray = JsonArray(emptyList()),
)

interface CommunityRepository {
    suspend fun discover(): List<CommunityEvent>

    suspend fun ask(endpoint: String, request: AgentRequest): AgentReply
}

class HttpCommunityRepository : CommunityRepository {
    private val client = OkHttpClient.Builder().callTimeout(45, TimeUnit.SECONDS).build()

    override suspend fun discover() =
        withContext(Dispatchers.IO) {
            client
                .newCall(Request.Builder().url("https://devearth.vercel.app/api/discovery").build())
                .execute()
                .use { response ->
                    check(response.isSuccessful) { "DevEarth is unavailable. Please retry." }
                    val body = response.body ?: error("Empty discovery response")
                    val source = body.source()
                    source.request(2_000_001)
                    val raw = source.readUtf8(minOf(source.buffer.size, 2_000_001))
                    require(raw.length <= 2_000_000) { "Discovery response too large" }
                    parseDiscovery(raw)
                }
        }

    override suspend fun ask(endpoint: String, request: AgentRequest) =
        withContext(Dispatchers.IO) {
            require(validEndpoint(endpoint)) { "Use an HTTPS agent endpoint." }
            val payload = buildJsonObject {
                put("prompt", request.prompt.take(2000))
                request.eventId?.let { put("eventId", it) }
                put("surfaceId", request.surfaceId)
                put("history", request.history)
                request.action?.let { put("action", it) }
                put("surfaceData", request.surfaceData)
                put("previousComponents", request.previousComponents)
            }
            client
                .newCall(
                    Request.Builder()
                        .url(endpoint)
                        .post(payload.toString().toRequestBody("application/json".toMediaType()))
                        .build()
                )
                .execute()
                .use { response ->
                    val source = response.body?.source() ?: error("Empty agent response")
                    source.request(150_001)
                    require(source.buffer.size <= 150_000) { "Agent response exceeded the limit" }
                    val raw = source.readUtf8()
                    if (!response.isSuccessful) {
                        val reason = runCatching {
                            Json.parseToJsonElement(raw).jsonObject.string("error")
                        }
                            .getOrDefault("")
                        error(
                            reason.ifBlank {
                                "Agent request failed (${response.code}). Please retry."
                            }
                        )
                    }
                    require(raw.length < 150_000) { "Agent response exceeded the limit" }
                    val data = Json.parseToJsonElement(raw).jsonObject
                    AgentReply(
                        data.string("text"),
                        data["messages"]!!.jsonArray.map { it.toString() },
                    )
                }
        }
}

fun parseDiscovery(raw: String): List<CommunityEvent> {
    val root = Json.parseToJsonElement(raw).jsonObject
    val cities =
        root["cities"]!!.jsonArray.associate {
            val c = it.jsonObject
            c.string("id") to c.string("name")
        }
    val sponsored =
        root["sponsors"]
            ?.jsonArray
            .orEmpty()
            .mapNotNull { (it.jsonObject["sponsor_campaigns"] as? JsonObject)?.string("event_id") }
            .toSet()
    return root["events"]!!.jsonArray.map { element ->
        val e = element.jsonObject
        val date = runCatching {
            DateTimeFormatter.ofPattern("EEE, d MMM · h:mm a")
                .withZone(ZoneId.of(e.string("timezone")))
                .format(Instant.parse(e.string("starts_at")))
        }
            .getOrDefault("Date not listed")
        CommunityEvent(
            e.string("id"),
            e.string("title"),
            e.string("description"),
            cities[e.string("city_id")] ?: "Online",
            date,
            e.string("venue").ifBlank { "Venue not listed" },
            e.string("address"),
            e.string("registration_url"),
            e.string("maps_url"),
            e["event_topics"]
                ?.jsonArray
                .orEmpty()
                .mapNotNull { (it.jsonObject["topics"] as? JsonObject)?.string("name") }
                .joinToString(" · "),
            when ((e["is_free"] as? JsonPrimitive)?.booleanOrNull) {
                true -> "Free"
                false -> "Paid · check official pricing"
                else -> "Price not listed"
            },
            e.string("id") in sponsored,
        )
    }
}

fun validEndpoint(value: String): Boolean = runCatching {
    val uri = URI(value)
    uri.userInfo == null &&
        uri.host != null &&
        (uri.scheme == "https" ||
            (BuildConfig.DEBUG &&
                uri.scheme == "http" &&
                uri.host in setOf("10.0.2.2", "localhost", "127.0.0.1")))
}
    .getOrDefault(false)
