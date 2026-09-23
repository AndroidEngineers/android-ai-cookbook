package com.androidengineers.pocketchat.data

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.io.Reader
import java.util.concurrent.TimeUnit

fun createChatService(): ChatService = GeminiChatService()

/** SSE frames may contain several data lines and can span arbitrary network chunks. */
fun readSse(reader: Reader, consume: (String) -> Unit) {
    val input = reader.buffered()
    val data = StringBuilder(); val line = StringBuilder()
    fun flushLine() {
        val value = line.toString().removeSuffix("\r"); line.setLength(0)
        if(value.isEmpty()) { if(data.isNotEmpty()) { consume(data.toString().trimEnd('\n')); data.setLength(0) } }
        else if(value.startsWith("data:")) { data.append(value.substring(5).removePrefix(" ")).append('\n'); if(data.length > 262144) throw ChatFailure("The response frame was too large.") }
    }
    while(true) {
        val value = input.read(); if(value == -1) break
        if(value == 10) flushLine() else { line.append(value.toChar()); if(line.length > 262144) throw ChatFailure("The response frame was too large.") }
    }
    if(line.isNotEmpty()) flushLine()
    if(data.isNotEmpty()) consume(data.toString().trimEnd('\n'))
}

fun decodeGeminiEvent(data: String): List<ChatEvent> {
    if(data == "[DONE]") return emptyList()
    val obj = Json.parseToJsonElement(data).jsonObject
    if(obj["error"] != null) throw ChatFailure("Gemini returned an error. Check model access and retry.")
    val blocked = obj["promptFeedback"]?.jsonObject?.get("blockReason")?.jsonPrimitive?.content
    if(blocked != null && blocked != "BLOCK_REASON_UNSPECIFIED") throw ChatFailure("Gemini could not answer this request. Try rephrasing it.")
    val candidate = obj["candidates"]?.jsonArray?.firstOrNull()?.jsonObject ?: return emptyList()
    val result = mutableListOf<ChatEvent>()
    candidate["content"]?.jsonObject?.get("parts")?.jsonArray?.forEach { part ->
        val p = part.jsonObject
        if(p["thought"]?.jsonPrimitive?.booleanOrNull != true) p["text"]?.jsonPrimitive?.contentOrNull?.let { result += ChatEvent.Text(it) }
    }
    candidate["finishReason"]?.jsonPrimitive?.content?.let { result += ChatEvent.Finished(it) }
    return result
}

class GeminiChatService(
    private val client: OkHttpClient = OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).readTimeout(45, TimeUnit.SECONDS).callTimeout(120, TimeUnit.SECONDS).retryOnConnectionFailure(false).build(),
    private val endpoint: String = "https://generativelanguage.googleapis.com/v1beta/"
): ChatService {
    override fun stream(key: String, model: String, parts: List<ChatPart>): Flow<ChatEvent> = callbackFlow {
        val body = buildJsonObject {
            putJsonArray("contents") { parts.forEach { part -> add(buildJsonObject { put("role", part.role); putJsonArray("parts") { add(buildJsonObject { put("text", part.text) }) } }) } }
            putJsonObject("systemInstruction") { putJsonArray("parts") { add(buildJsonObject { put("text", AndroidEngineersGuide.systemInstruction) }) } }
            putJsonObject("generationConfig") { put("maxOutputTokens", 4096); put("candidateCount", 1); putJsonObject("thinkingConfig") { put("thinkingBudget", 0) } }
        }
        val request = Request.Builder().url("${endpoint}models/$model:streamGenerateContent?alt=sse").header("x-goog-api-key", key).post(body.toString().toRequestBody("application/json".toMediaType())).build()
        val call = client.newCall(request)
        call.enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) { close(ChatFailure("Connection interrupted. Check your network and retry.")) }
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    try {
                        if(!response.isSuccessful) throw ChatFailure(when(response.code) { 400,401,403 -> "Check your API key and Gemini API access in Settings."; 404 -> "This model is unavailable. Check the model name in Settings."; 429 -> "Gemini quota reached. Check your quota and try later."; else -> "Gemini is unavailable (HTTP ${response.code}). Try later." })
                        val body = response.body ?: throw ChatFailure("Gemini returned an empty response.")
                        readSse(body.charStream()) { data -> decodeGeminiEvent(data).forEach { event -> if(trySendBlocking(event).isFailure) throw IOException("Stream closed") } }
                        close()
                    } catch(e: Exception) { close(if(e is ChatFailure) e else ChatFailure("The response was interrupted or unreadable. Try again.")) }
                }
            }
        })
        awaitClose { call.cancel() }
    }
}
