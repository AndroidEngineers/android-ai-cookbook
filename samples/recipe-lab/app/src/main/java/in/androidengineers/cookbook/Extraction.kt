package `in`.androidengineers.cookbook

import kotlinx.serialization.json.*

data class ExtractedTask(val title: String, val minutes: Int)

/** Parse untrusted output and reject unknown fields and coercions; validity is not truth. */
fun validateTask(raw: String): ExtractedTask {
    require(raw.length <= 4096) { "Output too large" }
    val obj = Json.parseToJsonElement(raw) as? JsonObject ?: error("Expected a JSON object")
    require(obj.keys == setOf("title", "minutes")) { "Expected only title and minutes" }
    val title = obj["title"] as? JsonPrimitive ?: error("Title must be text")
    require(title.isString && title.content.isNotBlank() && title.content.length <= 80) { "Title must contain 1–80 characters" }
    val number = obj["minutes"] as? JsonPrimitive ?: error("Minutes must be an integer")
    require(!number.isString) { "Minutes must be a number, not text" }
    val minutes = number.intOrNull ?: error("Minutes must be an integer")
    require(minutes in 1..120) { "Minutes must be in 1..120" }
    return ExtractedTask(title.content, minutes)
}
