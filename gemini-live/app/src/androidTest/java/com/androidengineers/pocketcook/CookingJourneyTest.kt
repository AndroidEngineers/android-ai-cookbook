package com.androidengineers.pocketcook
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CookingJourneyTest {
 @get:Rule val compose = createAndroidComposeRule<MainActivity>()
 @Before fun resetRecipeProgress() {
  compose.activity.getSharedPreferences("recipe_progress", android.content.Context.MODE_PRIVATE).edit().clear().commit()
 }
 @Test fun recreationRetainsCurrentStep() {
  compose.onNodeWithText("Tomato pasta").performClick()
  compose.onNodeWithText("Start cooking").performClick()
  compose.onNodeWithText("Next").performScrollTo().performClick()
  compose.activityRule.scenario.recreate()
  compose.onNodeWithText("STEP 2 OF 5").assertIsDisplayed()
  compose.onNodeWithText("Soften the onion").assertIsDisplayed()
 }
 @Test fun offlineRecipeCompletesWithoutLastStepCrash() {
  compose.onNodeWithText("Tomato pasta").performClick()
  compose.onNodeWithText("Start cooking").performClick()
  repeat(4) { compose.onNodeWithText("Next").performScrollTo().performClick() }
  compose.onNodeWithText("Finish cooking").performScrollTo().performClick()
  compose.onNodeWithText("Made with a little help.").assertIsDisplayed()
  compose.onNodeWithText("Back to recipes").performClick()
  compose.onNodeWithText("PocketCook").assertIsDisplayed()
 }
 @Test fun voiceRequiresExplicitConfiguration() {
  compose.onNodeWithText("Tomato pasta").performClick()
  compose.onNodeWithText("Start cooking").performClick()
  compose.onNodeWithText("Talk to PocketCook").performScrollTo().performClick()
  compose.onNodeWithText("Connect your Gemini").assertIsDisplayed()
  compose.onNodeWithText("Use for this session").assertIsNotEnabled()
  compose.onNodeWithText("Close").performClick()
  compose.onNodeWithText("Next").performScrollTo().assertIsEnabled()
 }
}
