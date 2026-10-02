package com.nothatcher.sproutbook

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import com.nothatcher.sproutbook.data.*
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*

open class FlowFixture {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    val app
        get() = ApplicationProvider.getApplicationContext<BabyForgeApp>()

    @Before
    fun seed() {
        runBlocking {
            app.settings.boolean("grandparent", false)
            app.db.children().all().forEach { app.db.children().delete(it.id) }
            app.repository.saveChild(Child(id = "qa-child", name = "Willow"))
        }
        waitText("YOUR BABY CHAPTER")
        // Native system overlays must fail QA rather than allow clicks behind them.
        compose.waitUntil(60000) { compose.activity.hasWindowFocus() }
    }

    fun waitText(text: String) {
        compose.waitUntil(60000) {
            compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    fun pageClick(label: String) {
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText(label))
        // First compose the lazy item, then expose a nested control inside that item.
        compose.onNodeWithText(label).performScrollTo().assertIsDisplayed().performClick()
    }

    fun care(label: String) {
        compose.onNodeWithTag("nav-care").performClick()
        compose.waitForIdle()
        compose.onNodeWithTag("nav-care").assertIsSelected()
        // Care restores its scroll position; its heading may be outside the lazy viewport.
        compose.onNode(hasScrollToIndexAction()).performScrollToIndex(0)
        waitText("A helping hand for every chapter")
        pageClick(label)
    }

    fun back() {
        compose.waitForIdle()
        // Shell-level hardware Back also reaches Compose dialog windows on API 29.
        val instrumentation = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
        instrumentation.uiAutomation.executeShellCommand("input keyevent 4").use { descriptor ->
            java.io.FileInputStream(descriptor.fileDescriptor).use { it.readBytes() }
        }
        compose.waitForIdle()
    }

    fun awaitDb(check: suspend () -> Boolean) {
        compose.waitUntil(60000) { runBlocking { check() } }
    }
}

class FeatureFlowTest : FlowFixture() {
    @Test
    fun feedingAddEditDelete() {
        care("Feeding")
        compose.onNodeWithText("Log bottle").performScrollTo().performClick()
        compose.onNodeWithText("Amount · mL").performTextInput("90")
        compose.onNodeWithText("Save feeding").performScrollTo().performClick()
        awaitDb { app.db.feeds().all().size == 1 }
        pageClick("Bottle · 90 mL")
        compose.onNodeWithText("Amount · mL").performTextReplacement("120")
        compose.onNodeWithText("Save feeding").performScrollTo().performClick()
        awaitDb { app.db.feeds().all().single().amountMl == 120.0 }
        pageClick("Bottle · 120 mL")
        compose.onNodeWithText("Delete feeding").performScrollTo().performClick()
        compose.onNodeWithText("Delete").performClick()
        awaitDb { app.db.feeds().all().isEmpty() }
    }

    @Test
    fun childSwitchAndGrandparentGuard() {
        runBlocking {
            app.db.children().save(Child(id = "qa-teen", name = "Ash", stage = "TEEN"))
            app.repository.saveFeed(
                Feed(childId = "qa-child", kind = "Bottle", startsAt = 1000, amountMl = 90.0)
            )
        }
        compose.onNodeWithContentDescription("Switch child").performClick()
        compose.onNodeWithText("Ash").performClick()
        waitText("YOUR TEEN CHAPTER")
        care("Feeding")
        compose.onNodeWithText("Bottle · 90 mL").assertDoesNotExist()
        runBlocking { app.settings.boolean("grandparent", true) }
        waitText("Grandparent mode · Read only")
        compose.onNodeWithText("Log bottle").performScrollTo().assertIsNotEnabled()
        assertEquals(1, runBlocking { app.db.feeds().all().size })
    }

    @Test
    fun formulaResetsWhenChildChanges() {
        runBlocking { app.db.children().save(Child(id = "qa-second", name = "Ash")) }
        care("Feeding")
        pageClick("Formula planning")
        compose.onNodeWithText("Baby's weight").performTextInput("5")
        compose.onNodeWithContentDescription("Switch child").performClick()
        compose.onNodeWithText("Ash").performClick()
        compose.onNodeWithText("Baby's weight").assertTextEquals("Baby's weight", "")
    }

    @Test
    fun olderFeedingRemainsReachable() {
        runBlocking {
            val now = System.currentTimeMillis()
            app.db.withTransaction {
                repeat(201) { i ->
                    app.db
                        .feeds()
                        .save(
                            Feed(
                                id = "feed-$i",
                                childId = "qa-child",
                                kind = "Bottle",
                                startsAt = now - i * 60000L,
                                amountMl = if (i == 200) 199.0 else 90.0,
                            )
                        )
                }
            }
        }
        care("Feeding")
        pageClick("Load older feeding records")
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Bottle · 199 mL"))
        compose.onNodeWithText("Bottle · 199 mL").assertIsDisplayed()
    }
}
