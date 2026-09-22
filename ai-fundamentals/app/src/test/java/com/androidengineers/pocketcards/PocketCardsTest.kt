package com.androidengineers.pocketcards

import com.androidengineers.pocketcards.data.*
import com.androidengineers.pocketcards.ui.PocketCardsViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.Before
import org.junit.After

@OptIn(ExperimentalCoroutinesApi::class)
class PocketCardsTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun teardown() { Dispatchers.resetMain() }
    class Memory : DeckRepository {
        var decks = emptyList<Deck>(); var fail = false
        override suspend fun load() = decks
        override suspend fun save(deck: Deck): List<Deck> { check(!fail); decks = listOf(deck) + decks.filterNot { it.id == deck.id }; return decks }
        override suspend fun delete(id: String): List<Deck> { decks = decks.filterNot { it.id == id }; return decks }
    }
    class Generator(override val available: Boolean = true) : CardGenerator {
        var calls = 0; var fail = false
        override suspend fun generate(notes: String): List<Flashcard> { calls++; delay(1000); check(!fail); return listOf(Flashcard("What is a token?", "A unit processed by a model.")) }
    }
    private val notes = "A token is a unit of text processed by a language model."
    @Test fun parsesStructuredCards() { assertEquals("Q", CardValidation.parse("""{"cards":[{"question":" Q ","answer":" A "}]}""").single().question) }
    @Test fun rejectsEmptyDeck() { assertThrows(IllegalArgumentException::class.java) { CardValidation.parse("""{"cards":[]}""") } }
    @Test fun rejectsDuplicateQuestions() { assertThrows(IllegalArgumentException::class.java) { CardValidation.validate(listOf(Flashcard("Same", "A"), Flashcard("same", "B"))) } }
    @Test fun rejectsOversizedAnswer() { assertThrows(IllegalArgumentException::class.java) { CardValidation.validate(listOf(Flashcard("Q", "a".repeat(1501)))) } }
    @Test fun rejectsMissingOrUnknownFields() { assertThrows(Exception::class.java) { CardValidation.parse("""{"cards":[{"question":"Q"}]}""") }; assertThrows(Exception::class.java) { CardValidation.parse("""{"cards":[],"command":"delete"}""") } }
    @Test fun generatesThenSavesEditedCards() = runTest(dispatcher) {
        val repo = Memory(); val gen = Generator(); val vm = PocketCardsViewModel(repo, gen); runCurrent()
        vm.notes(notes); vm.generate(); advanceUntilIdle()
        assertEquals(1, vm.state.value.draft.size)
        vm.title("My deck"); vm.updateCard(0,"Edited question", "Edited answer"); vm.save(); advanceUntilIdle()
        assertEquals("Edited answer", repo.decks.single().cards.single().answer); assertTrue(vm.state.value.draft.isEmpty())
    }
    @Test fun cancellationPreventsLateOutput() = runTest(dispatcher) {
        val vm = PocketCardsViewModel(Memory(), Generator()); runCurrent(); vm.notes(notes); vm.generate(); runCurrent(); vm.cancel(); advanceUntilIdle()
        assertFalse(vm.state.value.generating); assertTrue(vm.state.value.draft.isEmpty()); assertEquals(notes, vm.state.value.notes)
    }
    @Test fun rejectsShortInputWithoutProviderCall() = runTest(dispatcher) {
        val gen = Generator(); val vm = PocketCardsViewModel(Memory(), gen); runCurrent(); vm.notes("short"); vm.generate(); advanceUntilIdle()
        assertEquals(0, gen.calls); assertNotNull(vm.state.value.error)
    }
    @Test fun missingConfigurationDoesNotGenerate() = runTest(dispatcher) {
        val gen = Generator(false); val vm = PocketCardsViewModel(Memory(), gen); runCurrent(); vm.notes(notes); vm.generate(); advanceUntilIdle()
        assertEquals(0, gen.calls); assertNotNull(vm.state.value.error)
    }
    @Test fun generationFailurePreservesNotesAndCanRetry() = runTest(dispatcher) {
        val gen = Generator().apply { fail = true }; val vm = PocketCardsViewModel(Memory(), gen); runCurrent(); vm.notes(notes); vm.generate(); advanceUntilIdle()
        assertEquals(notes, vm.state.value.notes); assertNotNull(vm.state.value.error)
        gen.fail = false; vm.generate(); advanceUntilIdle(); assertEquals(1, vm.state.value.draft.size); assertNull(vm.state.value.error)
    }
    @Test fun failedSavePreservesDraft() = runTest(dispatcher) {
        val repo = Memory().apply { fail = true }; val vm = PocketCardsViewModel(repo, Generator()); runCurrent(); vm.edit(sampleDeck); vm.save(); advanceUntilIdle()
        assertEquals(5, vm.state.value.draft.size); assertNotNull(vm.state.value.error); assertFalse(vm.state.value.saving)
    }
    @Test fun savingAnEditReplacesRatherThanDuplicates() = runTest(dispatcher) {
        val repo = Memory(); val vm = PocketCardsViewModel(repo, Generator()); runCurrent(); vm.edit(sampleDeck); vm.save(); advanceUntilIdle()
        vm.edit(repo.decks.single()); vm.title("Updated"); vm.save(); advanceUntilIdle(); assertEquals(1, repo.decks.size); assertEquals("Updated", repo.decks.single().title)
        vm.delete(repo.decks.single().id); advanceUntilIdle(); assertTrue(vm.state.value.decks.isEmpty())
    }
    @Test fun doubleGenerateMakesOneRequest() = runTest(dispatcher) {
        val gen = Generator(); val vm = PocketCardsViewModel(Memory(), gen); runCurrent(); vm.notes(notes); vm.generate(); vm.generate(); advanceUntilIdle(); assertEquals(1, gen.calls)
    }
}
