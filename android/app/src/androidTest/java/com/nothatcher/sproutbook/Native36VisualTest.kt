package com.nothatcher.sproutbook

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.test.platform.app.InstrumentationRegistry
import com.nothatcher.sproutbook.core.BirthPlanCatalog
import com.nothatcher.sproutbook.core.BirthPlanningCatalog
import com.nothatcher.sproutbook.data.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/** Captures actual native screens with synthetic data; root inspects the rendered images. */
class Native36VisualTest : FlowFixture() {
    @Test
    fun birthWishlistAndTreeControlsRemainReachable() {
        val group = InstrumentationRegistry.getArguments().getString("snapshotSet") ?: "normal"
        runBlocking {
            app.settings.boolean("dark", false)
            app.settings.boolean("reduceMotion", true)
            app.repository.saveChild(app.db.children().all().single().copy(stage = "PREGNANCY", accent = "Moss"))
            app.repository.savePrep(PrepItem(id = BirthPlanCatalog.prefix("qa-child") + "priorities", childId = "qa-child",
                kind = "Note", title = "What matters most", notes = "Explain choices calmly. Our care records and transfer bag are ready."))
            listOf("maternity-phone" to "+1 555 010 0123", "address" to "Sample home; side gate access").forEach { (key, value) ->
                val field = BirthPlanCatalog.fields.first { it.key == key }
                app.repository.savePrep(PrepItem(id = BirthPlanCatalog.prefix("qa-child") + key, childId = "qa-child",
                    kind = "Note", title = field.title, notes = value))
            }
            app.repository.saveWishlist(WishlistItem(childId = "qa-child", title = "A woodland storybook", notes = "A little idea for the next chapter"))
            listOf("First", "Milestone", "Family moment", "Health", "Achievement", "General").forEachIndexed { i, category ->
                app.repository.saveMemory(Memory(id = "visual-memory-$i", childId = "qa-child", title = "Sample memory ${i + 1}",
                    occurredOn = 20600, category = category, anchor = i * 4, chapter = 0))
                app.repository.arrangeMemory("qa-child", "visual-memory-$i", i * 4)
            }
        }
        care("Pregnancy")
        pageClick("Homebirth & freebirth")
        compose.onNode(hasScrollToIndexAction()).performScrollToIndex(0)
        capture("$group-homebirth")
        pageClick("Birth plan")
        pageClick("Your homebirth plan")
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Maternity team phone"))
        compose.onNodeWithText("Maternity team phone").performScrollTo()
        capture("$group-birth-plan")
        pageClick("Open labour mode")
        compose.onNode(hasScrollToIndexAction()).performScrollToIndex(0)
        capture("$group-labour-top")
        pageClick("Start contraction")
        capture("$group-labour-timer")
        pageClick("Stop contraction")
        awaitDb { app.db.pregnancyEvents().forChild("qa-child").any { it.endsAt != null } }
        // The Room write can finish before its snackbar is emitted; observe it first.
        waitText("Contraction saved")
        compose.waitUntil(60000) { compose.onAllNodesWithText("Contraction saved").fetchSemanticsNodes().isEmpty() }
        compose.onNodeWithContentDescription("Back").performClick()
        care("Wishlist")
        pageClick("A woodland storybook")
        compose.onNodeWithText("Save wishlist item").performScrollTo().assertIsEnabled()
        capture("$group-wishlist-editor")
        back()
        compose.onNodeWithContentDescription("Back").performClick()
        care("Memory tree")
        for (style in listOf("Summer", "Autumn", "Night")) {
            runBlocking { app.repository.saveChild(app.db.children().all().single().copy(treeStyle = style)) }
            waitText("$style woodland · Willow's growing story")
            compose.onNodeWithContentDescription("Memory tree, 6 leaves. A timeline is also available.").performScrollTo()
            capture("$group-tree-${style.lowercase()}")
            assertEquals(listOf(0, 4, 8, 12, 16, 20), runBlocking { app.db.memorys().forChild("qa-child").sortedBy { it.id }.map { it.anchor } })
        }
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.waitForIdleSync()
        // Compose's virtual clock can settle before the native window is rendered.
        // Wait for Android accessibility/window idle before capturing pixels.
        val automation = instrumentation.uiAutomation
        automation.waitForIdle(100, 5000)
        assertEquals(app.packageName, automation.rootInActiveWindow?.packageName?.toString())
        val bitmap = automation.takeScreenshot()
        assertNotNull(bitmap)
        val file = File(app.filesDir, "qa36-evidence/$name.png")
        file.parentFile!!.mkdirs()
        file.outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
        bitmap.recycle()
    }
}
