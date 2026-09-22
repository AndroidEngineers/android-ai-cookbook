package `in`.androidengineers.pocketcards

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import android.content.Context
import `in`.androidengineers.pocketcards.data.*
import `in`.androidengineers.pocketcards.ui.*
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import java.io.File

class PocketCardsUiTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private fun launch(available: Boolean = false) {
        val repo = object : DeckRepository {
            var all = emptyList<Deck>()
            override suspend fun load() = all
            override suspend fun save(deck: Deck): List<Deck> { all = listOf(deck); return all }
            override suspend fun delete(id: String): List<Deck> { all = emptyList(); return all }
        }
        val gen = object : CardGenerator {
            override val available = available
            override suspend fun generate(notes: String) = listOf(Flashcard("What is inference?", "Running a trained model."))
        }
        val factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST") override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = PocketCardsViewModel(repo, gen) as T
        }
        compose.setContent { PocketTheme { PocketCardsApp(androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)) } }
    }
    @Test fun sampleDeckCanRevealAnswerAndRate() {
        launch(); compose.onNodeWithText("Try a sample deck").performScrollTo().performClick()
        compose.onNodeWithText("What is a prompt?").assertIsDisplayed()
        compose.onNodeWithText("Reveal answer").performScrollTo().performClick()
        compose.onNodeWithText("The instructions and input you give a model to guide its response.").assertIsDisplayed()
        compose.onNodeWithText("Got it").performScrollTo().performClick()
        compose.onNodeWithText("What is inference?").assertIsDisplayed()
    }
    @Test fun missingCloudSetupStillAllowsManualDeck() {
        launch(); compose.onNodeWithContentDescription("Create deck").performClick()
        compose.onNodeWithText("Generate flashcards").assertIsNotEnabled()
        compose.onNodeWithText("Write my own cards").performScrollTo().performClick()
        compose.onNodeWithText("Deck title").performTextInput("Biology")
        compose.onNodeWithText("Question 1").performScrollTo().performTextInput("What do plants need?")
        compose.onNodeWithText("Answer 1").performScrollTo().performTextInput("Light and water.")
        compose.onNodeWithText("Save deck").performScrollTo().performClick()
        compose.onNodeWithText("Biology").performScrollTo().assertIsDisplayed()
    }
    @Test fun injectedGenerationCanBeReviewedAndSaved() {
        launch(true); compose.onNodeWithContentDescription("Create deck").performClick()
        compose.onNodeWithText("Your study notes").performTextInput("Inference means running a trained model on new input.")
        compose.onNodeWithText("Generate flashcards").performScrollTo().performClick()
        compose.onNodeWithText("Deck title").assertExists()
        compose.onNodeWithText("Save deck").performScrollTo().performClick()
        compose.onNodeWithText("My study notes").performScrollTo().assertIsDisplayed()
    }
    @Test fun diskStorageSurvivesRepositoryRecreationAndDelete() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val f = File(context.cacheDir, "decks-${System.nanoTime()}.json")
        try {
            FileDeckRepository(f).save(sampleDeck)
            assertEquals(sampleDeck, FileDeckRepository(f).load().single())
            FileDeckRepository(f).delete(sampleDeck.id)
            assertTrue(FileDeckRepository(f).load().isEmpty())
        } finally { f.delete() }
    }
    @Test fun corruptStorageIsNotOverwritten() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val f = File(context.cacheDir, "bad-decks-${System.nanoTime()}.json")
        try {
            f.writeText("broken")
            try { FileDeckRepository(f).save(sampleDeck); fail("Should reject corrupt storage") } catch (_: kotlinx.serialization.SerializationException) { }
            assertEquals("broken", f.readText())
        } finally { f.delete() }
    }
}
