package com.androidengineers.pocketcommunity.data

import kotlinx.serialization.json.*

const val CATALOG = "https://a2ui.org/specification/v0_9/catalogs/basic/catalog.json"

/** Deterministic protocol examples. These demonstrate the renderer, not model inference. */
class SurfaceMessages(private val id: String) {
    private val components = mutableListOf<JsonObject>()
    private val children = mutableListOf<String>()
    private val values = buildJsonObject { repeat(4) { put("ready$it", false) } }

    private fun add(type: String, fields: JsonObject): String {
        val key = "c${components.size}"
        components += buildJsonObject {
            put("id", key)
            put("component", type)
            fields.forEach { (k, v) -> put(k, v) }
        }
        children += key
        return key
    }

    fun illustration() {
        add(
            "Image",
            buildJsonObject {
                put("url", "asset://community")
                put("description", "Illustration of a developer gathering, not the actual event")
                put("variant", "header")
            },
        )
    }

    fun text(value: String, variant: String = "body") {
        add(
            "Text",
            buildJsonObject {
                put("text", value)
                put("variant", variant)
            },
        )
    }

    fun button(label: String, action: String, eventId: String) {
        val labelId = add("Text", buildJsonObject { put("text", label) })
        children.remove(labelId)
        add(
            "Button",
            buildJsonObject {
                put("child", labelId)
                put("variant", "primary")
                putJsonObject("action") {
                    putJsonObject("event") {
                        put("name", action)
                        putJsonObject("context") { put("eventId", eventId) }
                    }
                }
            },
        )
    }

    fun checkbox(label: String, index: Int) {
        add(
            "CheckBox",
            buildJsonObject {
                put("label", label)
                putJsonObject("value") { put("path", "/ready$index") }
            },
        )
    }

    fun build(): List<String> {
        components += buildJsonObject {
            put("id", "content")
            put("component", "Column")
            put("align", "stretch")
            put("children", JsonArray(children.map(::JsonPrimitive)))
        }
        components += buildJsonObject {
            put("id", "root")
            put("component", "Card")
            put("child", "content")
        }
        fun message(key: String, body: JsonObject) = buildJsonObject {
            put("version", "v0.9")
            put(key, body)
        }
            .toString()
        return listOf(
            message(
                "createSurface",
                buildJsonObject {
                    put("surfaceId", id)
                    put("catalogId", CATALOG)
                },
            ),
            message(
                "updateDataModel",
                buildJsonObject {
                    put("surfaceId", id)
                    put("path", "/")
                    put("value", values)
                },
            ),
            message(
                "updateComponents",
                buildJsonObject {
                    put("surfaceId", id)
                    put("components", JsonArray(components))
                },
            ),
        )
    }

    companion object {
        fun event(id: String, e: CommunityEvent) =
            SurfaceMessages(id)
                .apply {
                    text(
                        if (e.sponsored) "DevEarth · Sponsored listing"
                        else "DevEarth · Published listing",
                        "caption",
                    )
                    illustration()
                    text("Community illustration", "caption")
                    text(e.title, "h4")
                    text("${e.date}\n${e.city} · ${e.entry}")
                    text(e.topics, "caption")
                    button("Explore venue", "venue", e.id)
                    button("Help me prepare", "prepare", e.id)
                }
                .build()

        fun venue(id: String, e: CommunityEvent) =
            SurfaceMessages(id)
                .apply {
                    text("THE VENUE", "caption")
                    text(e.venue, "h4")
                    text(e.address.ifBlank { e.city })
                    text(
                        "Location supplied by DevEarth. Open directions for the live map.",
                        "caption",
                    )
                    if (e.maps.isNotBlank()) button("Open directions", "directions", e.id)
                    button("Help me prepare", "prepare", e.id)
                }
                .build()

        fun prepare(id: String, e: CommunityEvent) =
            SurfaceMessages(id)
                .apply {
                    text("Your meetup checklist", "h4")
                    text(e.title, "caption")
                    listOf(
                            "Review the event details",
                            "Check registration",
                            "Prepare two questions",
                            "Pack essentials for the event",
                        )
                        .forEachIndexed { i, t -> checkbox(t, i) }
                    text(
                        "Suggestions, not organizer requirements. Saving does not register you.",
                        "caption",
                    )
                    button("Update my plan", "refine", e.id)
                    button("Save event", "save", e.id)
                    button("Official event page", "website", e.id)
                }
                .build()
    }
}
