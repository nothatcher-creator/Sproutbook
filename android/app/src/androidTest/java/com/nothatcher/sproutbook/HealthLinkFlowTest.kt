package com.nothatcher.sproutbook

import androidx.compose.ui.test.*
import com.nothatcher.sproutbook.data.*
import java.time.Instant
import java.util.TimeZone
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Synthetic records on a dedicated emulator only; FlowFixture resets family data. */
class HealthLinkFlowTest : FlowFixture() {
    private fun capture(name: String) {
        compose.waitForIdle()
        val automation = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().uiAutomation
        compose.waitUntil(60000) { automation.rootInActiveWindow?.packageName == app.packageName }
        val bitmap = requireNotNull(automation.takeScreenshot())
        java.io.File(app.getExternalFilesDir(null), "v351-$name-${compose.activity.resources.configuration.fontScale}.png")
            .outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }

    @Test
    fun searchReachesOlderVisitCancelKeepsDraftAndClearRemovesOnlyLink() {
        val now = System.currentTimeMillis()
        runBlocking {
            repeat(60) { index ->
                app.db.appointments().save(Appointment(id = "recent-$index", childId = "qa-child",
                    title = "Recent visit $index", startsAt = now - index * 3600000L))
            }
            app.db.appointments().save(Appointment(id = "older", childId = "qa-child",
                title = "Earlier check-up", startsAt = now - 100 * 3600000L, location = "North clinic"))
        }
        care("Health journal")
        pageClick("Add health record")
        compose.onNodeWithText("Title").performScrollTo().performTextInput("Earlier visit notes")
        compose.onNodeWithText("Choose appointment").performScrollTo().performClick()
        compose.waitUntil(60000) {
            compose.onAllNodesWithTag("health-appointment-recent-0").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("health-appointment-options")
            .performScrollToNode(hasText("Load more appointments"))
        compose.onNodeWithText("Load more appointments").performClick()
        compose.waitUntil(60000) {
            compose.onAllNodesWithText("Load more appointments").fetchSemanticsNodes().isEmpty()
        }
        compose.onNodeWithTag("health-appointment-options")
            .performScrollToNode(hasTestTag("health-appointment-older"))
        compose.onNodeWithTag("health-appointment-older").assertIsDisplayed()
        back()
        compose.onNodeWithText("Link an appointment").assertDoesNotExist()
        compose.onNodeWithText("No linked appointment").assertExists()
        compose.onNodeWithText("Choose appointment").performScrollTo().performClick()
        compose.onNodeWithText("Search title or place").performTextInput("North")
        compose.waitUntil(60000) {
            compose.onAllNodesWithTag("health-appointment-older").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("health-appointment-older").performScrollTo().performClick()
        compose.onNodeWithText("Choose appointment").performScrollTo().performClick()
        compose.onNodeWithText("Search title or place").performTextInput("No such sample visit")
        waitText("No matching appointments. Try another title or place.")
        compose.onNodeWithText("Cancel").performClick()
        compose.onNodeWithText("Save health record").performScrollTo().performClick()
        awaitDb { app.db.healthRecords().all().firstOrNull()?.appointmentId == "older" }
        pageClick("Earlier visit notes")
        compose.onNodeWithText("Choose appointment").performScrollTo().performClick()
        compose.onNodeWithTag("health-appointment-none").performClick()
        compose.onNodeWithText("Save health record").performScrollTo().performClick()
        awaitDb { app.db.healthRecords().all().single().appointmentId == null }
        assertEquals(61, runBlocking { app.db.appointments().all().size })
    }

    @Test
    fun caregiverClosesPickerAndChildSwitchDiscardsUnsavedDraft() {
        runBlocking {
            app.db.children().save(Child(id = "other-child", name = "Ash", stage = "TEEN"))
        }
        care("Health journal")
        pageClick("Add health record")
        compose.onNodeWithText("Title").performScrollTo().performTextInput("Unsaved sample draft")
        compose.onNodeWithText("Choose appointment").performScrollTo().performClick()
        runBlocking { app.settings.boolean("grandparent", true) }
        waitText("Grandparent mode · Read only")
        compose.onNodeWithText("Link an appointment").assertDoesNotExist()
        compose.onNodeWithText("Save health record").assertDoesNotExist()
        runBlocking {
            assertTrue(runCatching {
                app.repository.saveHealth(HealthRecord(childId = "qa-child", kind = "Doctor note",
                    recordedAt = 1, title = "Blocked write"))
            }.exceptionOrNull() is IllegalStateException)
            app.settings.boolean("grandparent", false)
            app.settings.select("other-child")
        }
        compose.waitUntil(60000) {
            compose.onAllNodesWithText("Health record").fetchSemanticsNodes().isEmpty()
        }
        compose.onNodeWithText("Add health record").assertIsEnabled()
        assertTrue(runBlocking { app.db.healthRecords().all().isEmpty() })
    }

    @Test
    fun duplicateVisitLabelsLinkTheChosenVisit() {
        val morning = Instant.parse("2025-05-12T10:00:00Z").toEpochMilli()
        runBlocking {
            app.repository.saveAppointment(Appointment(id = "morning", childId = "qa-child",
                title = "Check-up", startsAt = morning, location = "North clinic"))
            app.repository.saveAppointment(Appointment(id = "afternoon", childId = "qa-child",
                title = "Check-up", startsAt = morning + 5 * 3600000, location = "South clinic"))
        }
        care("Health journal")
        pageClick("Add health record")
        compose.onNodeWithText("Title").performScrollTo().performTextInput("Visit notes")
        compose.onNodeWithText("Choose appointment").performScrollTo().performClick()
        compose.waitUntil(60000) {
            compose.onAllNodesWithTag("health-appointment-afternoon").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("health-appointment-options")
            .performScrollToNode(hasTestTag("health-appointment-morning"))
        capture("health-picker")
        compose.onNodeWithTag("health-appointment-morning").assertIsDisplayed().performClick()
        compose.waitUntil(60000) {
            compose.onAllNodesWithText("North clinic", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        capture("health-linked")
        compose.onNodeWithText("Save health record").performScrollTo().performClick()
        awaitDb { app.db.healthRecords().all().size == 1 }
        assertEquals("morning", runBlocking { app.db.healthRecords().all().single().appointmentId })
    }

    @Test
    fun notesOnlyEditKeepsTheLaterRepeatedHour() {
        val previousZone = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))
            val original = Instant.parse("2025-11-02T06:30:15.123Z").toEpochMilli()
            runBlocking {
                app.repository.saveHealth(HealthRecord(id = "overlap", childId = "qa-child",
                    kind = "Doctor note", recordedAt = original, title = "After clock change"))
            }
            care("Health journal")
            pageClick("After clock change")
            compose.onNodeWithText("Notes / clinician instructions").performScrollTo()
                .performTextInput("Reviewed at follow-up")
            compose.onNodeWithText("Save health record").performScrollTo().performClick()
            awaitDb { app.db.healthRecords().get("overlap")?.notes == "Reviewed at follow-up" }
            assertEquals(original, runBlocking { app.db.healthRecords().get("overlap")!!.recordedAt })
        } finally {
            TimeZone.setDefault(previousZone)
        }
    }
}
