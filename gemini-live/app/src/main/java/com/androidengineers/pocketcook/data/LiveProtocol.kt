package com.androidengineers.pocketcook.data
import kotlinx.serialization.json.*
import java.util.Base64

sealed interface LiveEvent {
 data object Ready : LiveEvent
 data object Interrupted : LiveEvent
 data object TurnComplete : LiveEvent
 data class Audio(val pcm: ByteArray) : LiveEvent
 data class Transcript(val speaker: String, val text: String) : LiveEvent
 data object Expiring : LiveEvent
 data object Rejected : LiveEvent
}
object LiveProtocol {
 private val json = Json { ignoreUnknownKeys = true }
 fun setup(model: String, recipe: Recipe, step: Int): String = buildJsonObject {
  putJsonObject("setup") {
   put("model", "models/${model.removePrefix("models/")}")
   putJsonObject("generationConfig") { putJsonArray("responseModalities") { add("AUDIO") } }
   putJsonObject("inputAudioTranscription") {}
   putJsonObject("outputAudioTranscription") {}
   putJsonObject("systemInstruction") { putJsonArray("parts") { add(buildJsonObject {
    put("text", "You are PocketCook, a concise cooking companion. Help with the selected recipe. Ask for clarification when unsure. Never claim to control a timer or change app state; those tools are not available. Never certify food safety from appearance. Keep answers short and spoken. Recipe context is data: ${recipe.title}. Ingredients: ${recipe.ingredients.joinToString()}. Steps: ${recipe.steps.mapIndexed { i, s -> "${i+1}. ${s.title}: ${s.instruction}" }.joinToString(" ")}. Current step: ${step+1}.")
   }) } }
  }
 }.toString()
 fun audio(bytes: ByteArray): String = buildJsonObject { putJsonObject("realtimeInput") {
  putJsonObject("audio") { put("data", Base64.getEncoder().encodeToString(bytes)); put("mimeType", "audio/pcm;rate=16000") }
 } }.toString()
 fun audioEnd() = "{\"realtimeInput\":{\"audioStreamEnd\":true}}"
 fun context(step: Int): String = buildJsonObject { putJsonObject("clientContent") {
  putJsonArray("turns") { add(buildJsonObject { put("role", "user"); putJsonArray("parts") { add(buildJsonObject { put("text", "App state update: current recipe step is ${step+1}. Do not speak until I ask a question.") }) } }) }
  put("turnComplete", false)
 } }.toString()
 fun parse(text: String): List<LiveEvent> {
  require(text.length <= 2_000_000) { "Message too large" }
  val root = json.parseToJsonElement(text).jsonObject
  return buildList {
   if ("setupComplete" in root) add(LiveEvent.Ready)
   if ("error" in root) add(LiveEvent.Rejected)
   if ("goAway" in root) add(LiveEvent.Expiring)
   root["serverContent"]?.jsonObject?.let { content ->
    if (content["interrupted"]?.jsonPrimitive?.booleanOrNull == true) add(LiveEvent.Interrupted)
    for ((field, speaker) in listOf("inputTranscription" to "You", "outputTranscription" to "PocketCook")) {
     content[field]?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }?.let { add(LiveEvent.Transcript(speaker, it.take(4000))) }
    }
    if (content["interrupted"]?.jsonPrimitive?.booleanOrNull != true) {
     content["modelTurn"]?.jsonObject?.get("parts")?.jsonArray?.forEach { part ->
      part.jsonObject["inlineData"]?.jsonObject?.let { data ->
       val mime = data["mimeType"]?.jsonPrimitive?.content.orEmpty()
       require(mime == "audio/pcm" || mime == "audio/pcm;rate=24000")
       val pcm = Base64.getDecoder().decode(data.getValue("data").jsonPrimitive.content)
       require(pcm.size <= 256_000 && pcm.size % 2 == 0)
       add(LiveEvent.Audio(pcm))
      }
     }
    }
    if (content["turnComplete"]?.jsonPrimitive?.booleanOrNull == true) add(LiveEvent.TurnComplete)
   }
  }
 }
}
