package com.androidengineers.pocketchat

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RotationTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    @Test fun actualActivityRecreationRetainsDraft() {
        compose.waitUntil(5000){compose.onAllNodesWithText("New chat").fetchSemanticsNodes().isNotEmpty()}
        compose.onNodeWithText("New chat").performClick()
        compose.onNodeWithText("Ask a follow-up…").performTextInput("Keep this draft after recreation")
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Keep this draft after recreation").assertIsDisplayed()
    }
}
