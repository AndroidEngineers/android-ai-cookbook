package com.androidengineers.pocketcommunity

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidengineers.pocketcommunity.data.*
import com.androidengineers.pocketcommunity.ui.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConversationTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val event =
        CommunityEvent(
            "e1",
            "Test community",
            "",
            "Bengaluru",
            "Tomorrow",
            "Test venue",
            "",
            "https://example.com",
            "https://example.com/map",
            "Android",
            "Free",
            false,
        )

    private val requests = mutableListOf<AgentRequest>()

    private fun setup(fail: Boolean = false, configured: Boolean = true) {
        val repo =
            object : CommunityRepository {
                override suspend fun discover(): List<CommunityEvent> {
                    if (fail) error("Discovery unavailable")
                    return listOf(event)
                }

                override suspend fun ask(endpoint: String, request: AgentRequest): AgentReply {
                    requests += request
                    val messages =
                        when (request.action?.get("name")?.toString()?.trim('"')) {
                            "venue" -> SurfaceMessages.venue(request.surfaceId, event)
                            "prepare",
                            "refine" -> SurfaceMessages.prepare(request.surfaceId, event)
                            else -> SurfaceMessages.event(request.surfaceId, event)
                        }
                    return AgentReply("Test agent response", messages)
                }
            }
        compose.setContent {
            MaterialTheme {
                CommunityScreen(
                    androidx.lifecycle.viewmodel.compose.viewModel(
                        factory =
                            object : androidx.lifecycle.ViewModelProvider.Factory {
                                @Suppress("UNCHECKED_CAST")
                                override fun <T : androidx.lifecycle.ViewModel> create(
                                    modelClass: Class<T>
                                ): T =
                                    CommunityViewModel(
                                        repo,
                                        communityCatalog(),
                                        if (configured) "http://127.0.0.1:8787/chat" else "",
                                    )
                                        as T
                            }
                    )
                ) {}
            }
        }
    }

    @Test
    fun discoverVenueAndBoundChecklist() {
        setup()
        compose.onNodeWithText("Find Android events").performClick()
        compose.waitUntil(10000) {
            compose.onAllNodesWithText("Explore venue").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Explore venue").performScrollTo().performClick()
        compose.waitUntil(10000) {
            compose.onAllNodesWithText("Test venue").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Test venue").assertExists()
        org.junit.Assert.assertTrue(requests.last().history.isNotEmpty())
        org.junit.Assert.assertNotNull(requests.last().action)
        org.junit.Assert.assertTrue(requests.last().previousComponents.isNotEmpty())
        compose.onAllNodesWithText("Help me prepare").onLast().performScrollTo().performClick()
        compose.waitUntil(10000) {
            compose
                .onAllNodesWithText("Review the event details")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        compose
            .onNodeWithText("Review the event details")
            .performScrollTo()
            .performClick()
            .assertIsOn()
        compose.onNodeWithText("Update my plan").performScrollTo().performClick()
        compose.waitUntil(10000) { requests.size == 4 }
        org.junit.Assert.assertTrue(requests.last().surfaceData.toString().contains("true"))
        compose.onAllNodesWithText("Save event").onLast().performScrollTo().performClick()
        compose.waitUntil(10000) {
            compose
                .onAllNodesWithText(
                    "Saved for this session: Test community. Registration is separate."
                )
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    @Test
    fun missingEndpointRequiresSetup() {
        setup(configured = false)
        compose.onNodeWithText("Connect Gemini agent").assertExists()
        compose.onNodeWithText("Find Android events").assertDoesNotExist()
        org.junit.Assert.assertTrue(requests.isEmpty())
    }

    @Test
    fun discoveryFailureIsRecoverable() {
        setup(true)
        compose.onNodeWithText("Find Android events").performClick()
        compose.waitUntil(10000) {
            compose.onAllNodesWithText("Discovery unavailable").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Dismiss").performClick()
        compose.onNodeWithText("Discovery unavailable").assertDoesNotExist()
    }
}
