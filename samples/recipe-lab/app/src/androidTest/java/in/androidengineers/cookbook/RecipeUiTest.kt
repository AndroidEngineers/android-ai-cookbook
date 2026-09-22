package `in`.androidengineers.cookbook

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.compose.ui.graphics.asAndroidBitmap
import android.graphics.Bitmap
import java.io.File
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test

class RecipeUiTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    private fun open(index: Int) { ui.onNodeWithTag("recipe-$index").performClick() }
    private fun waitFor(tag: String, text: String) {
        ui.waitUntil(10000) { ui.onAllNodes(hasTestTag(tag) and hasText(text, substring=true)).fetchSemanticsNodes().isNotEmpty() }
    }
    private fun screenshot(name: String) {
        val bitmap = ui.onRoot().captureToImage().asAndroidBitmap()
        val directory = InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")
            ?.let { File(it) } ?: ui.activity.getExternalFilesDir(null)!!
        directory.mkdirs()
        File(directory, "$name.png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
    @Test fun streamCompletesAndCanRecover() {
        ui.onNodeWithTag("stream-error").performClick(); waitFor("stream-phase","Error")
        ui.onNodeWithTag("stream-start").performClick(); waitFor("stream-phase","Complete")
        ui.onNodeWithTag("stream-output").assertTextContains("No model or network was used.",substring=true)
        screenshot("streaming")
    }
    @Test fun firebaseFixtureWorksWithoutConfiguration() {
        open(1); ui.onNodeWithTag("stream-start").performClick(); waitFor("stream-phase","Complete")
        ui.onNodeWithTag("firebase-live").performScrollTo().performClick()
        waitFor("firebase-error","Firebase is not configured")
        screenshot("firebase")
    }
    @Test fun leavingScreenCancelsStream() {
        open(2); ui.onNodeWithTag("stream-start").performClick(); open(3)
        open(2); ui.onNodeWithTag("stream-phase").assertTextEquals("Cancelled")
        screenshot("cancellation")
    }
    @Test fun backgroundStopsStream() {
        ui.onNodeWithTag("stream-start").performClick()
        ui.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        ui.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        ui.onNodeWithTag("stream-phase").assertTextEquals("Cancelled")
    }
    @Test fun extractionAcceptsAndRejects() {
        open(3); ui.onNodeWithTag("validate").performClick(); waitFor("extraction-result","Accepted")
        ui.onNodeWithTag("invalid-fixture").performClick(); ui.onNodeWithTag("validate").performClick(); waitFor("extraction-result","Rejected")
        screenshot("extraction")
    }
    @Test fun recoveryRespectsBudget() {
        open(4); ui.onNodeWithTag("retry-recover").performClick(); waitFor("retry-result","Recovered on attempt 3")
        ui.onNodeWithTag("retry-offline").performClick(); waitFor("retry-result","Stopped after 3 attempts")
        ui.onNodeWithTag("retry-timeout").performClick(); waitFor("retry-result","Stopped after 3 attempts")
        screenshot("recovery")
    }
    @Test fun adkApprovalBoundaryWorksOnAndroid() {
        open(5); ui.onNodeWithTag("agent-deny").performClick(); waitFor("agent-result","approval_required")
        ui.onNodeWithTag("agent-approve").performClick(); waitFor("agent-result","Writes: 1")
        ui.onNodeWithTag("agent-mismatch").performClick(); waitFor("agent-result","approval_required")
        ui.onNodeWithTag("agent-result").assertTextContains("Writes: 0",substring=true)
        screenshot("adk")
    }
}
