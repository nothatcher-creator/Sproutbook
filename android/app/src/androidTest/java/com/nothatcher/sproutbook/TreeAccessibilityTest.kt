package com.nothatcher.sproutbook

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.test.platform.app.InstrumentationRegistry
import com.nothatcher.sproutbook.data.Memory
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class TreeAccessibilityTest : FlowFixture() {
    private val treeLabel = "Memory tree, 32 leaves. A timeline is also available."

    private fun actions() = compose.onNodeWithContentDescription(treeLabel)
        .fetchSemanticsNode().config[SemanticsActions.CustomActions]

    private fun inspectNativeTree() {
        assertTrue("Custom actions must stay below Android's 32-action limit", actions().size < 32)
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        compose.waitUntil(10000) { automation.rootInActiveWindow != null }
        fun visit(node: android.view.accessibility.AccessibilityNodeInfo) {
            // Query the framework provider, which Compose-only assertions do not exercise.
            node.actionList
            for (i in 0 until node.childCount) node.getChild(i)?.let(::visit)
        }
        visit(automation.rootInActiveWindow!!)
        compose.onNodeWithContentDescription(treeLabel).assertExists()
    }

    private fun invoke(label: String) {
        val action = actions().single { it.label == label }
        compose.runOnUiThread { assertTrue(action.action()) }
        compose.waitForIdle()
    }

    @Test
    fun nativeAccessibilityCanInspectFullCanopyAndArrange() {
        runBlocking {
            repeat(32) { i ->
                app.db.memorys().save(Memory(id = "a11y-$i", childId = "qa-child",
                    title = "Moment ${i + 1}", occurredOn = java.time.LocalDate.now().toEpochDay(), anchor = i))
            }
        }
        care("Memory tree")
        // Room insertion and navigation can finish before the screen receives all 32 rows.
        compose.waitUntil(60000) {
            compose.onAllNodesWithContentDescription(treeLabel).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithContentDescription(treeLabel).performScrollTo()
        inspectNativeTree()
        invoke("More leaves")
        assertTrue(actions().any { it.label == "Open Moment 32" })
        inspectNativeTree()
        pageClick("Arrange")
        inspectNativeTree()
        invoke("Select Moment 1")
        inspectNativeTree()
        invoke("More branch positions")
        assertTrue(actions().any { it.label == "Move selected leaf to branch 32" })
        inspectNativeTree()
        invoke("Move selected leaf to branch 32")
        awaitDb { app.db.memorys().all().single { it.id == "a11y-0" }.anchor == 31 }
        inspectNativeTree()
    }
}
