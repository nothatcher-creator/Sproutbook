package com.nothatcher.sproutbook

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.test.*
import androidx.test.platform.app.InstrumentationRegistry
import com.nothatcher.sproutbook.core.HomeCustomization
import com.nothatcher.sproutbook.data.*
import com.nothatcher.sproutbook.services.PhotoStore
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/** Rendered native evidence with a synthetic privately stored image and synthetic child records. */
class Native37VisualTest : FlowFixture() {
    @Test
    fun organizerAndSavedCustomBackgroundRemainReadable() {
        val group = InstrumentationRegistry.getArguments().getString("snapshotSet") ?: "normal"
        val image = BitmapFactory.decodeResource(app.resources, R.drawable.woodland_scene_schedule)
        assertNotNull(image)
        val file = PhotoStore.file(app, "qa-organizer-background.jpg")
        file.parentFile!!.mkdirs()
        file.outputStream().use { assertTrue(image.compress(Bitmap.CompressFormat.JPEG, 88, it)) }
        image.recycle()
        runBlocking {
            app.settings.boolean("dark", false)
            app.settings.boolean("reduceMotion", true)
            app.repository.saveHomeCustomization("qa-child", HomeCustomization().copy(
                quickActions = listOf("memory", "wishlist", "feeding", "sleep"),
                background = "photo", backgroundPhoto = file.name))
            app.repository.saveAppointment(Appointment(id = "visual37-appointment", childId = "qa-child",
                title = "Family picnic", startsAt = System.currentTimeMillis() + 86400000L))
            app.repository.saveMemory(Memory(id = "visual37-memory", childId = "qa-child",
                title = "A little woodland adventure", occurredOn = 20600, anchor = 4))
            app.repository.arrangeMemory("qa-child", "visual37-memory", 4)
        }
        compose.onNodeWithTag("nav-more").performClick()
        waitText("Your family")
        pageClick("Home screen organizer")
        compose.onNodeWithTag("organizer-list").performScrollToIndex(0)
        capture("$group-organizer-sections")
        compose.onNodeWithTag("organizer-list").performScrollToIndex(3)
        compose.onNodeWithTag("section-chapter-down").assertIsDisplayed().assertIsEnabled()
        capture("$group-organizer-section-controls")
        compose.onNodeWithTag("organizer-list").performScrollToNode(hasTestTag("organizer-quick-actions"))
        compose.onNodeWithTag("organizer-quick-actions").performScrollTo().performClick()
        compose.onNodeWithTag("organizer-list").performScrollToIndex(0)
        capture("$group-organizer-quick-actions")
        compose.onNodeWithTag("organizer-list").performScrollToIndex(3)
        compose.onNodeWithTag("quick-memory-down").assertIsDisplayed().assertIsEnabled()
        capture("$group-organizer-quick-controls")
        compose.onNodeWithTag("organizer-list").performScrollToNode(hasTestTag("organizer-background"))
        compose.onNodeWithTag("organizer-background").performScrollTo().performClick()
        compose.onNodeWithTag("organizer-list").performScrollToIndex(0)
        capture("$group-organizer-background")
        compose.onNodeWithTag("organizer-list").performScrollToNode(hasTestTag("organizer-photo-import"))
        compose.onNodeWithTag("organizer-photo-import").performScrollTo().assertIsDisplayed().assertIsEnabled()
        capture("$group-organizer-photo-controls")
        compose.onNodeWithTag("organizer-list").performScrollToNode(hasTestTag("organizer-cancel"))
        compose.onNodeWithTag("organizer-cancel").performScrollTo().performClick()
        compose.onNodeWithTag("nav-today").performClick()
        waitBackground()
        compose.onNodeWithTag("today-list").performScrollToIndex(0)
        capture("$group-custom-background-light")
        runBlocking { app.settings.boolean("dark", true) }
        compose.waitForIdle()
        waitBackground()
        capture("$group-custom-background-dark")
        assertEquals(file.name, runBlocking { app.db.children().get("qa-child")!!.homeBackgroundPhoto })
        assertEquals(4, runBlocking { app.db.memorys().get("visual37-memory")!!.anchor })
        runBlocking { app.settings.boolean("dark", false) }
    }

    private fun waitBackground() {
        compose.waitUntil(60000) {
            compose.onAllNodesWithTag("today-background-photo").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.waitForIdleSync()
        // Android's software renderer can trail Compose's virtual clock during theme changes.
        instrumentation.runOnMainSync { compose.activity.window.decorView.postInvalidateOnAnimation() }
        Thread.sleep(300)
        instrumentation.waitForIdleSync()
        val automation = instrumentation.uiAutomation
        automation.waitForIdle(100, 5000)
        assertEquals(app.packageName, automation.rootInActiveWindow?.packageName?.toString())
        val bitmap = automation.takeScreenshot()
        assertNotNull(bitmap)
        val target = File(app.filesDir, "qa37-evidence/$name.png")
        target.parentFile!!.mkdirs()
        target.outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
        bitmap.recycle()
    }
}
