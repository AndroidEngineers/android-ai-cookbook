package com.androidengineers.pocketstories

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StoriesUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun firstAdventureRoutesToHonestModelSetup() {
        compose.onNodeWithText("Start an adventure").performScrollTo().performClick()
        compose.onNodeWithText("What kind of magic?").assertIsDisplayed()
        compose.onNodeWithContentDescription("Model setup").performClick()
        compose.onNodeWithText("Gemma 4 · E2B").assertIsDisplayed()
        compose.onNodeWithText("Download Gemma · 2.59 GB").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun sampleIsExplicitlyLabelled() {
        compose.onNodeWithTag("story-list").performScrollToNode(hasText("The midnight workshop"))
        compose.onNodeWithText("The midnight workshop").performClick()
        compose.onNodeWithText("A HAND-WRITTEN SAMPLE · NOT AI OUTPUT").assertIsDisplayed()
    }

    @Test
    fun capturedSceneSurvivesReopeningWithoutAModel() {
        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
            .uiAutomation
            .executeShellCommand(
                "pm grant com.androidengineers.pocketstories android.permission.CAMERA"
            )
            .close()
        compose.onNodeWithText("Start an adventure").performScrollTo().performClick()
        compose.onNodeWithText("Find your first scene").performScrollTo().performClick()
        compose.onNodeWithText("Capture this scene").assertIsDisplayed()
        // CameraX readiness is asynchronous; the UI explicitly exposes a recoverable error.
        compose.waitUntil(15_000) {
            compose
                .onAllNodes(hasText("Capture this scene") and isEnabled())
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        compose.onNodeWithText("Capture this scene").performClick()
        compose.waitUntil(15_000) {
            compose
                .onAllNodesWithText("Your captured scene · stays on this phone")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        compose.onNodeWithText("Write the opening").performScrollTo().performClick()
        compose.onNodeWithText("Gemma 4 · E2B").assertIsDisplayed()
        compose.onNodeWithContentDescription("Bookshelf").performClick()
        compose
            .onNodeWithTag("story-list")
            .performScrollToNode(hasText("An ordinary little adventure"))
        compose.onNodeWithText("An ordinary little adventure").performClick()
        compose.onNodeWithText("Your captured scene · stays on this phone").assertExists()
    }
}
