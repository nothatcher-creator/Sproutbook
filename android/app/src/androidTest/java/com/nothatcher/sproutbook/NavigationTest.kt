package com.nothatcher.sproutbook

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun emptyFamily() = kotlinx.coroutines.runBlocking {
        val app = androidx.test.core.app.ApplicationProvider.getApplicationContext<BabyForgeApp>()
        app.settings.boolean("grandparent", false)
        app.db.children().all().forEach { app.db.children().delete(it.id) }
    }

    @Test
    fun profileCreationEnablesSafeNavigation() {
        compose.waitUntil(60000) {
            compose.onAllNodesWithText("Create first profile").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("nav-schedule").assertIsNotEnabled()
        compose.onNodeWithText("Create first profile").performClick()
        compose.onNodeWithText("Child's name").performTextInput("Rowan")
        compose.onNodeWithText("Save profile").performScrollTo().performClick()
        compose.waitUntil(60000) {
            compose.onAllNodesWithText("YOUR BABY CHAPTER").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("nav-schedule").performClick()
        compose.onNodeWithTag("nav-care").performClick()
        compose.onNodeWithTag("nav-more").performClick()
        compose.onNodeWithText("Your family").assertIsDisplayed()
        compose.onNodeWithTag("nav-today").performClick()
        compose.onNodeWithText("YOUR BABY CHAPTER").assertIsDisplayed()
    }
}
