package com.nothatcher.sproutbook

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.test.platform.app.InstrumentationRegistry
import com.nothatcher.sproutbook.core.TreeTheme
import com.nothatcher.sproutbook.data.Child
import com.nothatcher.sproutbook.data.Memory
import java.io.File
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/** Synthetic-family evidence for saved tree styles and the profile's local preview. */
class TreeThemeFlowTest : FlowFixture() {
    @Test
    fun profilePreviewDiscardsDismissedChangesAndSavesOnlyThisChild() {
        runBlocking {
            app.settings.boolean("dark", false)
            app.settings.boolean("reduceMotion", true)
            app.db.children().save(Child(id = "qa-sibling", name = "Ash", treeStyle = "Night"))
        }
        compose.onNodeWithTag("nav-more").performClick()
        waitText("Your family")
        openWillowProfile()
        compose.onNodeWithText("Blossom").performScrollTo().performClick()
        compose.onNodeWithText("Blossom").assertIsSelected()
        compose.onNodeWithText(TreeTheme.BLOSSOM.description).performScrollTo().assertIsDisplayed()
        assertEquals("Summer", runBlocking { app.db.children().get("qa-child")!!.treeStyle })
        capture("profile-blossom-draft")

        // Dismissing the profile sheet is its cancel path; it must not save the preview.
        back()
        compose.waitUntil(60000) { compose.onAllNodesWithText("Save profile").fetchSemanticsNodes().isEmpty() }
        assertEquals("Summer", runBlocking { app.db.children().get("qa-child")!!.treeStyle })
        openWillowProfile()
        compose.onNodeWithText("Winter").performScrollTo().performClick()
        compose.onNodeWithText("Save profile").performScrollTo().performClick()
        awaitDb { app.db.children().get("qa-child")?.treeStyle == "Winter" }
        assertEquals("Night", runBlocking { app.db.children().get("qa-sibling")!!.treeStyle })

        runBlocking { app.settings.boolean("grandparent", true) }
        waitText("Grandparent mode · Read only")
        openWillowProfile()
        compose.onNodeWithText("Memory tree · Winter").assertExists()
        compose.onNodeWithText("Memory tree style").assertDoesNotExist()
        compose.onNodeWithText("Save profile").assertDoesNotExist()
        assertEquals("Winter", runBlocking { app.db.children().get("qa-child")!!.treeStyle })
        back()
        runBlocking { app.settings.boolean("grandparent", false) }
    }

    @Test
    fun newThemeGalleryKeepsMemoryRecordsAndPlacementInLightAndDarkMode() {
        val anchors = listOf(0, 2, 4, 5, 8, 10, 17, 22, 25, 31)
        val categories = listOf("General", "First", "Family moment", "Milestone")
        val memories = anchors.mapIndexed { i, anchor ->
            Memory(id = "theme-leaf-$i", childId = "qa-child", title = "A small adventure ${i + 1}",
                occurredOn = LocalDate.now().toEpochDay(), category = categories[i % categories.size],
                notes = "A day together in the woodland.", anchor = anchor)
        }
        runBlocking {
            app.settings.boolean("dark", false)
            app.settings.boolean("reduceMotion", true)
            memories.forEach { app.db.memorys().save(it) }
        }
        care("Memory tree")
        val treeLabel = "Memory tree, ${memories.size} leaves. A timeline is also available."
        try {
            for (dark in listOf(false, true)) {
                runBlocking { app.settings.boolean("dark", dark) }
                for (theme in listOf(TreeTheme.SPRING, TreeTheme.BLOSSOM, TreeTheme.WINTER, TreeTheme.RAINBOW)) {
                    runBlocking {
                        val child = app.db.children().get("qa-child")!!
                        app.repository.saveChild(child.copy(treeStyle = theme.style))
                    }
                    waitText("${theme.style} woodland · Willow's growing story")
                    compose.onNodeWithContentDescription(treeLabel).performScrollTo().assertIsDisplayed()
                    capture("${theme.style.lowercase()}-${if (dark) "dark" else "light"}")
                    assertEquals(theme.style, runBlocking { app.db.children().get("qa-child")!!.treeStyle })
                    val restored = runBlocking { app.db.memorys().forChild("qa-child") }.associateBy { it.id }
                    memories.forEach { assertEquals(it, restored[it.id]) }
                }
            }
        } finally {
            runBlocking { app.settings.boolean("dark", false) }
        }
    }

    private fun openWillowProfile() {
        val profileCard = hasText("Willow") and hasClickAction() and !hasContentDescription("Switch child")
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(profileCard)
        compose.onNode(profileCard).performScrollTo().assertIsDisplayed().performClick()
        waitText("Willow's profile")
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.waitForIdleSync()
        instrumentation.runOnMainSync { compose.activity.window.decorView.postInvalidateOnAnimation() }
        // Give the emulator's software renderer one frame after a saved theme changes.
        Thread.sleep(300)
        instrumentation.waitForIdleSync()
        val automation = instrumentation.uiAutomation
        automation.waitForIdle(100, 5000)
        val bitmap = automation.takeScreenshot()
        assertNotNull(bitmap)
        val group = InstrumentationRegistry.getArguments().getString("snapshotSet") ?: "normal"
        File(app.filesDir, "tree-theme-evidence/$group-$name.png").let { target ->
            target.parentFile!!.mkdirs()
            target.outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
        }
        bitmap.recycle()
    }
}
