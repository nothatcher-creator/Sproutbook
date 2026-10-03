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
        capture("$group-birth-plan")
        pageClick("Open labour mode")
        compose.onNode(hasScrollToIndexAction()).performScrollToIndex(0)
        capture("$group-labour-top")
        pageClick("Start contraction")
        capture("$group-labour-timer")
        pageClick("Stop contraction")
        awaitDb { app.db.pregnancyEvents().forChild("qa-child").any { it.endsAt != null } }
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
            compose.waitForIdle()
            compose.onNode(hasScrollToIndexAction()).performScrollToIndex(0)
            capture("$group-tree-${style.lowercase()}")
            assertEquals(listOf(0, 4, 8, 12, 16, 20), runBlocking { app.db.memorys().forChild("qa-child").sortedBy { it.id }.map { it.anchor } })
        }
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        assertNotNull(bitmap)
        val file = File(app.filesDir, "qa36-evidence/$name.png")
        file.parentFile!!.mkdirs()
        file.outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
    }
}
