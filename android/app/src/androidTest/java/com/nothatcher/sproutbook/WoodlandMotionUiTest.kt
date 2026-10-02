package com.nothatcher.sproutbook

import android.provider.Settings
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import com.nothatcher.sproutbook.ui.WoodlandPlaybackState
import org.junit.Assert.assertNull
import org.junit.Test

class WoodlandMotionUiTest : FlowFixture() {
    private fun shell(command: String) {
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command).use { descriptor ->
            java.io.FileInputStream(descriptor.fileDescriptor).use { it.readBytes() }
        }
    }
    private fun waitAsset(id: String, state: String) {
        compose.waitUntil(60000) {
            if (!compose.mainClock.autoAdvance) compose.mainClock.advanceTimeByFrame()
            compose.onAllNodes(hasTestTag("motion-$id") and
                SemanticsMatcher.expectValue(WoodlandPlaybackState, state), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }
    private fun capture(name: String) {
        val bitmap = requireNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot())
        java.io.File(app.getExternalFilesDir(null), "v35-$name.png").outputStream().use {
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
    }
    @Test fun visibleTabAnimationsStopForAccessibilityAndResume() {
        // Compose's auto-advance policy deliberately cancels infinite animations.
        // Use a controlled clock to test real active loops and policy transitions.
        compose.mainClock.autoAdvance = false
        val oldScale = Settings.Global.getFloat(app.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        try {
            shell("settings put global animator_duration_scale 1")
            runBlocking { app.settings.boolean("reduceMotion", false); app.settings.boolean("dark", true) }
            waitAsset("icon-today", "Animated")
            val decorativeIcon = compose.onNodeWithTag("motion-icon-today", useUnmergedTree = true).fetchSemanticsNode()
            assertNull(decorativeIcon.config.getOrNull(SemanticsProperties.StateDescription))
            capture("today-motion")
            for (route in listOf("schedule", "care", "more")) {
                compose.onNodeWithTag("nav-$route").performClick()
                // Finish native navigation's finite transition on the controlled clock.
                compose.mainClock.advanceTimeBy(1000)
                compose.waitForIdle()
                waitAsset("icon-$route", "Animated")
                compose.onNodeWithTag("nav-$route").assertIsSelected()
                capture("$route-motion")
                if (route == "care") {
                    waitAsset("milestone-bloom", "Animated")
                    compose.onNode(hasScrollToIndexAction()).performScrollToIndex(5)
                    compose.waitUntil(60000) {
                        compose.mainClock.advanceTimeByFrame()
                        compose.onAllNodes(hasTestTag("motion-milestone-bloom") and
                            SemanticsMatcher.expectValue(WoodlandPlaybackState, "Animated"), useUnmergedTree = true)
                            .fetchSemanticsNodes().isEmpty()
                    }
                    compose.onNode(hasScrollToIndexAction()).performScrollToIndex(0)
                    waitAsset("milestone-bloom", "Animated")
                }
            }
            shell("settings put global animator_duration_scale 0")
            waitAsset("icon-more", "Still")
            shell("settings put global animator_duration_scale 1")
            waitAsset("icon-more", "Animated")
            runBlocking { app.settings.boolean("reduceMotion", true) }
            waitAsset("icon-more", "Still")
            runBlocking { app.settings.boolean("dark", false) }
            capture("more-light-still")
            compose.onNodeWithTag("nav-today").performClick()
            compose.mainClock.advanceTimeBy(1000)
            compose.waitForIdle()
            waitAsset("icon-today", "Still")
            capture("today-light-still")
        } finally {
            shell("settings put global animator_duration_scale $oldScale")
            runBlocking { app.settings.boolean("reduceMotion", true); app.settings.boolean("dark", true) }
            compose.mainClock.advanceTimeByFrame()
            compose.mainClock.autoAdvance = true
        }
    }
    @Test fun animationDecorationDoesNotInterceptFeedingControls() {
        val oldScale = Settings.Global.getFloat(app.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        try {
            shell("settings put global animator_duration_scale 1")
            runBlocking { app.settings.boolean("reduceMotion", false) }
            compose.waitForIdle()
            care("Feeding")
            pageClick("Log bottle")
            compose.onNodeWithText("Amount · mL").performTextInput("95")
            compose.onNodeWithText("Save feeding").performScrollTo().performClick()
            awaitDb { app.db.feeds().all().singleOrNull()?.amountMl == 95.0 }
            compose.onNodeWithText("Saved").assertExists()
        } finally {
            shell("settings put global animator_duration_scale $oldScale")
            runBlocking { app.settings.boolean("reduceMotion", true) }
        }
    }
}
