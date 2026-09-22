package com.androidengineers.pocketcards.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID

@Serializable data class Flashcard(val question: String, val answer: String)
@Serializable data class Deck(val id: String = UUID.randomUUID().toString(), val title: String, val cards: List<Flashcard>, val origin: String = "Created by you", val createdAt: Long = System.currentTimeMillis())
@Serializable private data class GeneratedCards(val cards: List<Flashcard>)
object CardValidation {
    const val MAX_NOTES = 8000
    private val json = Json { ignoreUnknownKeys = false }
    fun parse(text: String): List<Flashcard> {
        require(text.length <= 30000) { "The response was too large. Try shorter notes." }
        return validate(json.decodeFromString<GeneratedCards>(text).cards)
    }
    fun validate(cards: List<Flashcard>): List<Flashcard> {
        require(cards.size in 1..10) { "A deck needs between 1 and 10 cards." }
        val cleaned = cards.map { Flashcard(it.question.trim(), it.answer.trim()) }
        require(cleaned.all { it.question.length in 1..300 && it.answer.length in 1..1500 }) { "Each card needs a question (up to 300 characters) and an answer (up to 1,500)." }
        require(cleaned.map { it.question.lowercase() }.distinct().size == cleaned.size) { "Each question should be different." }
        return cleaned
    }
    fun title(value: String): String = value.trim().also { require(it.length in 1..80) { "Give your deck a title (up to 80 characters)." } }
}
val sampleDeck = Deck(id = "pocketcards-starter", title = "A little AI vocabulary", origin = "Sample deck · written by us", createdAt = 0,
    cards = listOf(
        Flashcard("What is a prompt?", "The instructions and input you give a model to guide its response."),
        Flashcard("What is inference?", "Running a trained model on new input to produce a prediction or response."),
        Flashcard("What is a token?", "A unit of text a model processes. It can be a word, part of a word, or punctuation."),
        Flashcard("Does valid JSON guarantee a correct answer?", "No. Structured output can have the right format while containing incorrect information."),
        Flashcard("What does on-device AI mean?", "The model runs on your device rather than sending inference work to a cloud service.")
    ))
