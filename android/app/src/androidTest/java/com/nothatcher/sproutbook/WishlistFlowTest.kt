package com.nothatcher.sproutbook

import androidx.compose.ui.test.*
import com.nothatcher.sproutbook.data.*
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/** Synthetic records on a dedicated emulator only; FlowFixture resets family data. */
class WishlistFlowTest : FlowFixture() {
    private fun sheetClosed() {
        compose.waitUntil(60000) {
            compose.onAllNodesWithText("Wishlist item").fetchSemanticsNodes().isEmpty()
        }
    }

    private fun feedbackFinished(text: String) {
        waitText(text)
        compose.waitUntil(60000) {
            compose.onAllNodesWithText(text).fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun createEditObtainAndDeleteAnIdea() {
        care("Wishlist")
        pageClick("Add wishlist item")
        compose.onNodeWithText("Wishlist title").performTextInput("Woodland storybook")
        compose.onNodeWithText("Wishlist notes (optional)").performTextInput("For bedtime together")
        compose.onNodeWithText("Web link (optional)").performScrollTo()
            .performTextInput("example.com/storybook")
        compose.onNodeWithText("Open wishlist link").performScrollTo().performClick()
        compose.onNodeWithText("Enter a complete http:// or https:// web address.").assertExists()
        compose.onNodeWithText("Web link (optional)").performScrollTo()
            .performTextReplacement("https://example.com/storybook")
        compose.onNodeWithText("Save wishlist item").performScrollTo().performClick()
        awaitDb { app.db.wishlistItems().all().size == 1 }
        sheetClosed()
        feedbackFinished("Wishlist item saved")
        pageClick("Woodland storybook")
        compose.onNodeWithText("Wishlist title").performTextReplacement("Forest storybook")
        compose.onNodeWithText("Wishlist notes (optional)")
            .performTextReplacement("Birthday idea")
        compose.onNodeWithText("Save wishlist item").performScrollTo().performClick()
        awaitDb { app.db.wishlistItems().all().single().title == "Forest storybook" }
        sheetClosed()
        feedbackFinished("Wishlist item saved")
        compose.onNode(hasScrollToIndexAction())
            .performScrollToNode(hasContentDescription("Mark Forest storybook obtained"))
        compose.onNodeWithContentDescription("Mark Forest storybook obtained")
            .performScrollTo().performClick()
        awaitDb { app.db.wishlistItems().all().single().obtained }
        feedbackFinished("Wishlist item marked obtained")
        pageClick("Obtained")
        pageClick("Forest storybook")
        compose.onNodeWithText("Delete wishlist item").performScrollTo().performClick()
        compose.onNodeWithText("Delete").performClick()
        awaitDb { app.db.wishlistItems().all().isEmpty() }
        val removed = runBlocking { app.db.shoppingItems().all() }
        assertTrue(removed.isEmpty())
    }

    @Test
    fun childSwitchDiscardsDraftAndCaregiverCanOnlyRead() {
        runBlocking {
            app.db.children().save(Child(id = "other-child", name = "Ash", stage = "TEEN"))
            app.repository.saveWishlist(
                WishlistItem(id = "willow-wish", childId = "qa-child", title = "Willow's kite",
                    notes = "Choose a gentle windy day", link = "https://example.com/kite")
            )
        }
        care("Wishlist")
        pageClick("Add wishlist item")
        compose.onNodeWithText("Wishlist title").performTextInput("Unsaved idea")
        runBlocking { app.settings.select("other-child") }
        sheetClosed()
        compose.onNodeWithText("Willow's kite").assertDoesNotExist()
        waitText("0 wanted · 0 obtained")
        assertEquals(1, runBlocking { app.db.wishlistItems().all().size })
        runBlocking {
            app.settings.boolean("grandparent", true)
            app.settings.select("qa-child")
        }
        waitText("Grandparent mode · Read only")
        compose.onNodeWithText("Add wishlist item").performScrollTo().assertIsNotEnabled()
        compose.onNode(hasScrollToIndexAction())
            .performScrollToNode(hasContentDescription("Mark Willow's kite obtained"))
        compose.onNodeWithContentDescription("Mark Willow's kite obtained").assertIsNotEnabled()
        pageClick("Willow's kite")
        compose.onNode(hasText("Choose a gentle windy day") and hasAnyAncestor(isDialog()))
            .assertExists()
        compose.onNode(hasText("Open wishlist link") and hasAnyAncestor(isDialog()))
            .assertIsEnabled()
        compose.onNodeWithText("Save wishlist item").assertDoesNotExist()
        compose.onNodeWithText("Delete wishlist item").assertDoesNotExist()
        assertFalse(runBlocking { app.db.wishlistItems().all().single().obtained })
    }

    @Test
    fun filtersCountAllIdeasAndOlderMatchesStayReachable() {
        runBlocking {
            val now = System.currentTimeMillis()
            repeat(201) { index ->
                app.db.wishlistItems().save(
                    WishlistItem(id = "obtained-$index", childId = "qa-child",
                        title = if (index == 200) "Oldest obtained idea" else "Obtained idea $index",
                        obtained = true, updatedAt = now - index * 1000L)
                )
            }
            app.db.wishlistItems().save(
                WishlistItem(id = "wanted", childId = "qa-child", title = "A wanted experience",
                    updatedAt = now - 1000000L)
            )
        }
        care("Wishlist")
        waitText("1 wanted · 201 obtained")
        compose.onNode(hasScrollToIndexAction())
            .performScrollToNode(hasText("A wanted experience"))
        compose.onNodeWithText("A wanted experience").assertIsDisplayed()
        pageClick("Obtained")
        pageClick("Load more wishlist items")
        compose.onNode(hasScrollToIndexAction())
            .performScrollToNode(hasText("Oldest obtained idea"))
        compose.onNodeWithText("Oldest obtained idea").assertIsDisplayed()
    }

    @Test
    fun profileAppearancePersistsWithoutChangingTheirDetails() {
        val birthday = LocalDate.now().minusDays(100).toEpochDay()
        val dueDate = LocalDate.now().minusDays(105).toEpochDay()
        runBlocking {
            val child = app.db.children().all().single()
            app.repository.saveChild(child.copy(avatar = "qa-avatar", birthday = birthday,
                dueDate = dueDate, notes = "Keep these caregiver notes"))
        }
        compose.onNodeWithTag("nav-more").performClick()
        waitText("Your family")
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Willow"))
        compose.onNode(hasText("Willow") and hasAnyAncestor(hasScrollToIndexAction()))
            .performClick()
        compose.onNodeWithText("Sky").performScrollTo().performClick()
        compose.onNodeWithText("Autumn").performScrollTo().performClick()
        compose.onNodeWithText("Save profile").performScrollTo().performClick()
        awaitDb {
            app.db.children().all().single().let { it.accent == "Sky" && it.treeStyle == "Autumn" }
        }
        val child = runBlocking { app.db.children().all().single() }
        assertEquals("qa-avatar", child.avatar)
        assertEquals(birthday, child.birthday)
        assertEquals(dueDate, child.dueDate)
        assertEquals("Keep these caregiver notes", child.notes)
        assertEquals("BABY", child.stage)
        compose.waitUntil(60000) {
            compose.onAllNodesWithText("Save profile").fetchSemanticsNodes().isEmpty()
        }
        compose.onNode(hasScrollToIndexAction())
            .performScrollToNode(hasText("Baby · Selected\nSky accent · Autumn tree"))
        compose.onNodeWithText("Baby · Selected\nSky accent · Autumn tree").assertIsDisplayed()
    }
}
