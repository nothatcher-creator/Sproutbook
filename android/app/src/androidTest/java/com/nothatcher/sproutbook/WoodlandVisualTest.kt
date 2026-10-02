package com.nothatcher.sproutbook

import androidx.compose.ui.test.*
import androidx.compose.ui.semantics.SemanticsActions
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Test

/** Actual Android captures use synthetic child data only. */
class WoodlandVisualTest : FlowFixture() {
    private fun capture(name: String) {
        compose.waitForIdle()
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        // A native bottom sheet owns focus instead of the Activity. Reject system overlays,
        // while accepting this application's active dialog window.
        compose.waitUntil(60000) { automation.rootInActiveWindow?.packageName == app.packageName }
        val bitmap = requireNotNull(automation.takeScreenshot())
        java.io.File(app.getExternalFilesDir(null), "v34-$name.png").outputStream().use {
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
    }
    @Test fun paintedTabsKeepNativeNavigationAndLabels() {
        runBlocking {
            app.settings.boolean("dark", true)
            app.settings.boolean("reduceMotion", true)
            app.db.appointments().save(com.nothatcher.sproutbook.data.Appointment(
                childId = "qa-child", title = "Woodland visit", startsAt = System.currentTimeMillis() + 3600000))
        }
        listOf("today" to "Today", "schedule" to "Schedule", "care" to "Care", "more" to "Your family").forEach { (route, title) ->
            compose.onNodeWithTag("nav-$route").performClick()
            compose.onNode(hasScrollToIndexAction()).performScrollToIndex(0)
            waitText(title)
            compose.onNodeWithTag("nav-$route").assertIsSelected()
            capture(route)
        }
    }
    @Test fun lightThemeLargeTextAndLoggingControlsRemainUsable() {
        // Configure the device to 150% before launching this instrumentation test.
        // Changing font scale during the test races Android's Activity/IME recreation.
        org.junit.Assume.assumeTrue("Run with device font_scale=1.5 before Activity launch",
            compose.activity.resources.configuration.fontScale >= 1.49f)
        try {
            runBlocking { app.settings.boolean("dark", false); app.settings.boolean("reduceMotion", true) }
            waitText("YOUR BABY CHAPTER")
            capture("today-light-large")
            care("Feeding")
            pageClick("Log bottle")
            compose.onNodeWithText("Amount · mL").performTextInput("80")
            compose.onNodeWithText("Save feeding").performScrollTo().assertIsDisplayed()
            capture("feeding-editor-light-large")
            // Exercise the native accessibility action at large type; regular touch
            // add/edit/delete flows are verified separately by FeatureFlowTest.
            compose.onNodeWithText("Save feeding").performSemanticsAction(SemanticsActions.OnClick) { it() }
            capture("feeding-after-save-light-large")
            try {
                awaitDb { app.db.feeds().all().singleOrNull()?.amountMl == 80.0 }
            } finally {
                java.io.File(app.getExternalFilesDir(null), "v34-feeding-save-records.txt").writeText(
                    runBlocking { app.db.feeds().all().toString() })
            }
        } finally {
            runBlocking { app.settings.boolean("dark", true) }
        }
    }
}
