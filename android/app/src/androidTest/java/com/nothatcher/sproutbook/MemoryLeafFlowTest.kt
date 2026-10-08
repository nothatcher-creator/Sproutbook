package com.nothatcher.sproutbook

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.test.platform.app.InstrumentationRegistry
import com.nothatcher.sproutbook.data.*
import com.nothatcher.sproutbook.services.PhotoStore
import java.io.File
import java.io.FileInputStream
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/** Native keepsake navigation and record protection, using a dedicated synthetic family. */
class MemoryLeafFlowTest : FlowFixture() {
    private val treeLabel = "Memory tree, 1 leaves. A timeline is also available."

    private fun seedMemory(title: String = "First woodland picnic", notes: String = "A little day to remember.", photo: String? = null): Memory {
        val memory = Memory(id = "leaf-keepsake", childId = "qa-child", title = title,
            occurredOn = LocalDate.of(2026, 8, 20).toEpochDay(), category = "Family moment", notes = notes, photo = photo)
        runBlocking {
            app.repository.saveChild(app.db.children().get("qa-child")!!.copy(birthday = LocalDate.of(2025, 8, 20).toEpochDay()))
            app.repository.saveMemory(memory)
        }
        return runBlocking { app.db.memorys().get(memory.id)!! }
    }

    private fun tapLeaf() {
        compose.waitUntil(60000) { compose.onAllNodesWithContentDescription(treeLabel).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription(treeLabel).performScrollTo()
            .performTouchInput { click(Offset(width * .22f, height * .22f)) }
        waitForCard()
    }

    private fun waitForCard() {
        compose.waitUntil(60000) { compose.onAllNodesWithTag("memory-leaf-card").fetchSemanticsNodes().isNotEmpty() }
    }

    @Test
    fun treeOpensKeepsakeBeforeEditingAndTimelineReopensTheSavedStory() {
        val original = seedMemory()
        care("Memory tree")
        if (InstrumentationRegistry.getArguments().getString("openViaTimeline") == "true") {
            pageClick("Timeline")
            pageClick(original.title)
            waitForCard()
        } else tapLeaf()
        capture("keepsake-top")
        val inCard = hasAnyAncestor(hasTestTag("memory-leaf-card"))
        compose.onNode(hasText(original.title) and inCard).assertIsDisplayed()
        compose.onNode(hasText("Willow's story · Chapter 1") and inCard).assertExists()
        compose.onNode(hasText("Family moment") and inCard).assertExists()
        compose.onNode(hasText("Age at this moment · 1 year") and inCard).assertExists()
        compose.onNodeWithText("Memory title").assertDoesNotExist()
        assertEquals(original, runBlocking { app.db.memorys().get(original.id) })
        val restingFrame = visibleWindowFrame()
        compose.onNode(hasText("Edit memory") and inCard).assertIsDisplayed().performClick()
        compose.onNodeWithText("Memory title").performTextReplacement("A picnic under the old oak")
        settleKeyboardBeforeSave(restingFrame)
        val save = compose.onNodeWithText("Save memory").performScrollTo().assertIsDisplayed().assertIsEnabled()
        captureMemorySaveInput("before", save)
        save.performClick()
        captureMemorySaveInput("after", save)
        try {
            awaitDb { app.db.memorys().get(original.id)?.title == "A picnic under the old oak" }
        } catch (failure: Throwable) {
            captureMemorySaveInput("failed-db-wait", save)
            throw failure
        }
        waitForCard()
        waitText("A picnic under the old oak")
        compose.onNodeWithText("Close").performClick()
        pageClick("Timeline")
        pageClick("A picnic under the old oak")
        waitForCard()
        compose.onNodeWithText("Memory title").assertDoesNotExist()
        recordInputState("before-timeline-back")
        back()
        recordInputState("after-timeline-back")
        compose.waitUntil(10000) { compose.onAllNodesWithTag("memory-leaf-card").fetchSemanticsNodes().isEmpty() }
        compose.onNodeWithTag("memory-leaf-card").assertDoesNotExist()
        assertEquals(original.anchor, runBlocking { app.db.memorys().get(original.id)!!.anchor })
        assertEquals(original.chapter, runBlocking { app.db.memorys().get(original.id)!!.chapter })
    }

    @Test
    fun todayLeafDeepLinkSurvivesRecreationAndDismissalStaysDismissed() {
        val original = seedMemory()
        compose.onNodeWithTag("today-list").performScrollToNode(hasTestTag("today-section-memory_tree"))
        tapLeaf()
        compose.onNodeWithText(original.title).assertIsDisplayed()
        compose.activityRule.scenario.recreate()
        waitForCard()
        compose.onNodeWithText(original.title).assertIsDisplayed()
        compose.onNodeWithText("Close").performClick()
        compose.activityRule.scenario.recreate()
        waitText("1 moments in Willow's story")
        compose.onNodeWithTag("memory-leaf-card").assertDoesNotExist()
        assertEquals(original, runBlocking { app.db.memorys().get(original.id) })
    }

    @Test
    fun enablingGrandparentModeRevokesArrangementAndOpenEditorControls() {
        val original = seedMemory()
        care("Memory tree")
        compose.waitUntil(60000) { compose.onAllNodesWithContentDescription(treeLabel).fetchSemanticsNodes().isNotEmpty() }
        pageClick("Arrange")
        waitText("Done arranging")
        val arrangingActions = compose.onNodeWithContentDescription(treeLabel).performScrollTo()
            .fetchSemanticsNode().config[SemanticsActions.CustomActions]
        assertTrue(arrangingActions.any { it.label == "Select ${original.title}" })
        runBlocking { app.settings.boolean("grandparent", true) }
        waitText("Grandparent mode · Read only")
        val actions = compose.onNodeWithContentDescription(treeLabel).performScrollTo()
            .fetchSemanticsNode().config[SemanticsActions.CustomActions]
        assertTrue(actions.any { it.label == "Open ${original.title}" })
        assertFalse(actions.any { it.label.startsWith("Select ") || it.label.startsWith("Move ") })
        tapLeaf()
        compose.onNodeWithText("Edit memory").assertDoesNotExist()
        runBlocking { app.settings.boolean("grandparent", false) }
        waitText("Edit memory")
        compose.onNodeWithText("Edit memory").performClick()
        compose.onNodeWithText("Memory title").assertExists()
        runBlocking { app.settings.boolean("grandparent", true) }
        compose.waitUntil(60000) { compose.onAllNodesWithText("Memory title").fetchSemanticsNodes().isEmpty() }
        compose.onNodeWithText("Memory title").assertDoesNotExist()
        compose.onNodeWithText("Save memory").assertDoesNotExist()
        compose.onNodeWithText("Delete memory").assertDoesNotExist()
        back()
        waitText("Read-only keepsake")
        compose.onNodeWithText("Edit memory").assertDoesNotExist()
        compose.onNodeWithText("Close").performClick()
        assertEquals(original, runBlocking { app.db.memorys().get(original.id) })
    }

    @Test
    fun grandparentCanReadPhotoAndLongStoryAndUseAccessibleOpenWithoutMutation() {
        val filename = "qa-leaf-photo.jpg"
        val photo = Bitmap.createBitmap(120, 90, Bitmap.Config.ARGB_8888).apply { eraseColor(android.graphics.Color.rgb(130, 170, 110)) }
        PhotoStore.file(app, filename).let { target ->
            target.parentFile!!.mkdirs()
            target.outputStream().use { assertTrue(photo.compress(Bitmap.CompressFormat.JPEG, 90, it)) }
        }
        photo.recycle()
        val notes = List(60) { "Little detail ${it + 1}: we laughed together in the warm afternoon." }.joinToString("\n") + "\nThe whole story is still here."
        val original = seedMemory("The afternoon we found a tiny feather beside the old woodland oak", notes, filename)
        runBlocking { app.settings.boolean("grandparent", true) }
        care("Memory tree")
        val action = compose.onNodeWithContentDescription(treeLabel).performScrollTo()
            .fetchSemanticsNode().config[SemanticsActions.CustomActions].single { it.label == "Open ${original.title}" }
        compose.runOnUiThread { assertTrue(action.action()) }
        waitForCard()
        compose.onNodeWithText("Edit memory").assertDoesNotExist()
        compose.onNodeWithText("Memory title").assertDoesNotExist()
        compose.onNodeWithText("Read-only keepsake").assertExists()
        compose.waitUntil(60000) {
            compose.onAllNodesWithContentDescription("Photo for ${original.title}").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithContentDescription("Photo for ${original.title}").performScrollTo().assertIsDisplayed()
        capture("read-only-photo")
        // The last block follows the full story, proving that its end remains reachable.
        compose.onNodeWithText("A little part of Willow's story, kept close.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Close").assertIsDisplayed()
        capture("read-only-long-story")
        back()
        compose.onNodeWithTag("memory-leaf-card").assertDoesNotExist()
        assertEquals(original, runBlocking { app.db.memorys().get(original.id) })
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.waitForIdleSync()
        val automation = instrumentation.uiAutomation
        automation.waitForIdle(100, 5000)
        val bitmap = automation.takeScreenshot()
        assertNotNull(bitmap)
        val group = InstrumentationRegistry.getArguments().getString("snapshotSet") ?: "normal"
        File(app.filesDir, "memory-leaf-evidence/$group-$name.png").let { file ->
            file.parentFile!!.mkdirs()
            file.outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
        }
        bitmap.recycle()
    }

    private fun recordInputState(name: String) {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        val input = automation.executeShellCommand("dumpsys input_method").use { descriptor ->
            java.io.FileInputStream(descriptor.fileDescriptor).use { it.readBytes().toString(Charsets.UTF_8) }
        }
        val relevant = input.lineSequence().filter {
            "mInputShown" in it || "mShowRequested" in it || "mCurFocusedWindow" in it || "mServedView" in it
        }.joinToString("\n")
        File(app.filesDir, "memory-leaf-evidence/$name.txt").let { file ->
            file.parentFile!!.mkdirs()
            file.writeText(relevant)
        }
    }

    private fun visibleWindowFrame() = compose.runOnUiThread {
        android.graphics.Rect().also { compose.activity.window.decorView.getWindowVisibleDisplayFrame(it) }
    }

    private fun nativeShell(command: String): String =
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command).use { descriptor ->
            FileInputStream(descriptor.fileDescriptor).use { it.readBytes().toString(Charsets.UTF_8) }
        }

    /** Compose idleness can precede the native IME surface and resized dialog viewport. */
    private fun settleKeyboardBeforeSave(restingFrame: android.graphics.Rect) {
        compose.waitUntil(30000) {
            nativeShell("dumpsys input").lineSequence().any {
                "u0 InputMethod}" in it && "visible=true" in it
            } && visibleWindowFrame().bottom < restingFrame.bottom
        }
        // One hardware Back dismisses the confirmed visible keyboard, leaving the editor open.
        back()
        compose.waitUntil(30000) {
            val input = nativeShell("dumpsys input_method")
            "mInputShown=false" in input && "mShowRequested=false" in input &&
                visibleWindowFrame() == restingFrame
        }
        compose.onNodeWithText("Memory title").assertExists()
    }

    /** Single-tap native evidence distinguishes editor validation from hit/window geometry. */
    private fun captureMemorySaveInput(stage: String, target: SemanticsNodeInteraction) {
        runCatching {
            val group = InstrumentationRegistry.getArguments().getString("snapshotSet") ?: "normal"
            val folder = File(app.filesDir, "memory-save-input-evidence/$group").apply { mkdirs() }
            val geometry = runCatching {
                val node = target.fetchSemanticsNode()
                "targetPx=${node.boundsInRoot}; targetCenterPx=${node.boundsInRoot.center}; " +
                    "unclippedDp=${target.getUnclippedBoundsInRoot()}"
            }.getOrElse { "Save node absent: ${it.javaClass.simpleName}" }
            val state = "stage=$stage; $geometry; activityFocus=${compose.activity.hasWindowFocus()}\n" +
                "memory=${runBlocking { app.db.memorys().get("leaf-keepsake") }}\n" +
                "grandparent=${runBlocking { app.settings.flow.first().grandparent }}\n"
            android.util.Log.i("SproutMemorySaveQa", state)
            File(folder, "$stage-state.txt").writeText(state)
            val roots = compose.onAllNodes(isRoot(), useUnmergedTree = true)
            File(folder, "$stage-semantics.txt").writeText(
                roots.fetchSemanticsNodes().indices.joinToString("\n") { roots[it].printToString() }
            )
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
            val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
            automation.takeScreenshot()?.let { bitmap ->
                try {
                    File(folder, "$stage-screen.png").outputStream().use {
                        check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
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
        }.onFailure { android.util.Log.e("SproutMemorySaveQa", "Capture $stage failed: ${it.javaClass.simpleName}: ${it.message}") }
    }
}
