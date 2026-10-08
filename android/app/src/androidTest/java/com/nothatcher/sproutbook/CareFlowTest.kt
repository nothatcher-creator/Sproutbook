package com.nothatcher.sproutbook

import androidx.compose.ui.test.*
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.io.FileInputStream
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class CareFlowTest : FlowFixture() {
    @Test
    fun inventorySaveAdjustAndDelete() {
        care("Family cupboard")
        pageClick("Add supply")
        compose.onNodeWithText("Name").performTextInput("Wipes")
        compose.onNodeWithText("Quantity").performTextReplacement("3")
        compose.onNodeWithText("Save supply").performScrollTo().performClick()
        awaitDb { app.db.inventorys().all().singleOrNull()?.quantity == 3.0 }
        // A committed row can arrive before the editor's separate window is removed.
        // Wait for the real touch destination; do not click through a closing sheet.
        compose.waitUntil(60000) { compose.onAllNodesWithText("Save supply").fetchSemanticsNodes().isEmpty() }
        compose.waitUntil(60000) { compose.activity.hasWindowFocus() }
        // The Saved snackbar overlaps this newly added row's decrement button.
        compose.waitUntil(60000) { compose.onAllNodesWithText("Saved").fetchSemanticsNodes().isEmpty() }
        android.util.Log.i("SproutInventoryQa", "Before Use one: stock=${runBlocking { app.db.inventorys().all() }}; " +
            "activityFocus=${compose.activity.hasWindowFocus()}")
        val useOne = compose.onNodeWithContentDescription("Use one Wipes").performScrollTo()
            .assertIsDisplayed().assertIsEnabled()
        captureInventoryInput("before-tap", useOne)
        useOne.performClick()
        captureInventoryInput("after-tap", useOne)
        try {
            awaitDb { app.db.inventorys().all().single().quantity == 2.0 }
        } catch (failure: Throwable) {
            captureInventoryInput("failure", useOne)
            // Keep the original failure, with enough synthetic state to locate a missed tap.
            runCatching {
                android.util.Log.e("SproutInventoryQa", "After Use one: stock=${runBlocking { app.db.inventorys().all() }}; " +
                    "grandparent=${runBlocking { app.settings.flow.first().grandparent }}; " +
                    "activityFocus=${compose.activity.hasWindowFocus()}")
                val roots = compose.onAllNodes(isRoot(), useUnmergedTree = true)
                roots.fetchSemanticsNodes().indices.forEach { roots[it].printToLog("SproutInventoryQa") }
            }
            throw failure
        }
        pageClick("Wipes")
        compose.onNodeWithText("Delete supply").performScrollTo().performClick()
        compose.onNodeWithText("Delete").performClick()
        awaitDb { app.db.inventorys().all().isEmpty() }
    }

    /** Dedicated-fixture evidence survives DB resets and avoids Android's log-message limit. */
    private fun captureInventoryInput(stage: String, target: SemanticsNodeInteraction) {
        runCatching {
            val folder = File(app.filesDir, "inventory-input-evidence").apply { mkdirs() }
            val node = target.fetchSemanticsNode()
            val viewport = compose.onNode(hasScrollToIndexAction()).fetchSemanticsNode().boundsInRoot
            val geometry = "stage=$stage; targetPx=${node.boundsInRoot}; " +
                "targetCenterPx=${node.boundsInRoot.center}; unclippedDp=${target.getUnclippedBoundsInRoot()}; " +
                "viewportPx=$viewport; activityFocus=${compose.activity.hasWindowFocus()}"
            // Short enough to retain the complete target/viewport geometry in logcat too.
            android.util.Log.i("SproutInventoryQa", geometry)
            val state = geometry + "\nstock=${runBlocking { app.db.inventorys().all() }}\n" +
                "grandparent=${runBlocking { app.settings.flow.first().grandparent }}\n"
            File(folder, "$stage-state.txt").writeText(state)
            val roots = compose.onAllNodes(isRoot(), useUnmergedTree = true)
            File(folder, "$stage-semantics.txt").writeText(
                roots.fetchSemanticsNodes().indices.joinToString("\n") { roots[it].printToString() }
            )
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            val visibleFrame = android.graphics.Rect()
            val windowGeometry = compose.runOnUiThread {
                val decor = compose.activity.window.decorView
                val location = IntArray(2)
                decor.getLocationOnScreen(location)
                decor.getWindowVisibleDisplayFrame(visibleFrame)
                "decorSizePx=${decor.width}x${decor.height}; decorLocationPx=${location.toList()}; " +
                    "visibleFramePx=$visibleFrame; bottomSystemInsetPx=${decor.rootWindowInsets?.systemWindowInsetBottom}"
            }
            File(folder, "$stage-window-geometry.txt").writeText(windowGeometry)
            val automation = instrumentation.uiAutomation
            automation.takeScreenshot()?.let { bitmap ->
                try {
                    File(folder, "$stage-screen.png").outputStream().use { output ->
                        check(bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, output))
                    }
                } finally { bitmap.recycle() }
            }
            listOf("window" to "dumpsys window windows", "input" to "dumpsys input",
                "ime" to "dumpsys input_method").forEach { (name, command) ->
                automation.executeShellCommand(command).use { descriptor ->
                    FileInputStream(descriptor.fileDescriptor).use { input ->
                        File(folder, "$stage-$name.txt").outputStream().use { input.copyTo(it) }
                    }
                }
            }
        }.onFailure { android.util.Log.e("SproutInventoryQa", "Capture $stage failed: ${it.javaClass.simpleName}: ${it.message}") }
    }

    @Test
    fun foodAndMealSave() {
        care("Solids & meals")
        pageClick("Introduce a food")
        compose.onNodeWithText("Food").performTextInput("Pear")
        compose.onNodeWithText("Liked").performClick()
        compose.onNodeWithText("Save food").performScrollTo().performClick()
        awaitDb { app.db.foods().all().firstOrNull()?.liking == "Liked" }
        pageClick("This week's meals")
        compose.onAllNodesWithText("Breakfast").onFirst().performClick()
        compose.onNodeWithText("Meal").performTextInput("Soft pear and cereal")
        compose.onNodeWithText("Save meal").performScrollTo().performClick()
        awaitDb { app.db.meals().all().size == 1 }
    }

    @Test
    fun memoryAddTimelineEditDelete() {
        care("Memory tree")
        pageClick("Add a memory")
        compose.onNodeWithText("Memory title").performTextInput("First smile")
        compose.onNodeWithText("Save memory").performScrollTo().performClick()
        awaitDb { app.db.memorys().all().size == 1 }
        pageClick("Timeline")
        pageClick("First smile")
        compose.onNodeWithText("Memory title").assertDoesNotExist()
        compose.onNodeWithText("Edit memory").performClick()
        compose.onNodeWithText("Memory title").performTextReplacement("A lovely smile")
        compose.onNodeWithText("Save memory").performScrollTo().performClick()
        awaitDb { app.db.memorys().all().single().title == "A lovely smile" }
        // The editor has the same "A moment to keep" title; wait for the viewer itself.
        compose.waitUntil(60000) { compose.onAllNodesWithTag("memory-leaf-card").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Close").performClick()
        pageClick("A lovely smile")
        compose.onNodeWithText("Edit memory").performClick()
        compose.onNodeWithText("Delete memory").performScrollTo().performClick()
        compose.onNodeWithText("Delete").performClick()
        awaitDb { app.db.memorys().all().isEmpty() }
    }

    @Test
    fun pregnancySessionsPersist() {
        care("Pregnancy")
        pageClick("Kicks")
        pageClick("+ Kick")
        awaitDb { app.db.pregnancyEvents().all().firstOrNull()?.count == 1 }
        pageClick("Finish & reset session")
        awaitDb { app.db.pregnancyEvents().all().firstOrNull()?.endsAt != null }
        pageClick("Contractions")
        pageClick("Start contraction")
        awaitDb {
            app.db.pregnancyEvents().all().any { it.kind == "Contraction" && it.endsAt == null }
        }
        pageClick("Stop contraction")
        awaitDb { app.db.pregnancyEvents().all().all { it.endsAt != null } }
    }
}
