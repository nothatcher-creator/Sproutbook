package com.nothatcher.sproutbook

import android.content.ClipboardManager
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import com.nothatcher.sproutbook.services.SoundService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class NativeControlsTest : FlowFixture() {
    @Test
    fun nursingAndPumpingSaveTheirOwnFields() {
        care("Feeding")
        compose.onAllNodesWithText("Breast").onFirst().performClick()
        pageClick("Log breast")
        compose.onNodeWithText("Both").performClick()
        compose.onNodeWithText("Duration · minutes").performTextInput("12")
        compose.onNodeWithText("Save feeding").performScrollTo().performClick()
        awaitDb { app.db.feeds().all().singleOrNull()?.durationSeconds == 720L }
        compose.onAllNodesWithText("Pump").onFirst().performScrollTo().performClick()
        pageClick("Log pump")
        compose.onNodeWithText("Left amount · mL").performTextInput("40")
        compose.onNodeWithText("Right amount · mL").performTextInput("50")
        compose.onNodeWithText("Save feeding").performScrollTo().performClick()
        awaitDb { app.db.feeds().all().any { it.kind == "Pump" && it.amountMl == 90.0 } }
        assertEquals("Both", runBlocking { app.db.feeds().all().single { it.kind == "Breast" }.side })
    }

    @Test
    fun sleepAndSoundControlsReachNativeService() {
        care("Sleep & sound")
        pageClick("Start nap")
        awaitDb { app.db.sleeps().all().singleOrNull()?.endsAt == null && app.db.sleeps().all().size == 1 }
        pageClick("Stop sleep")
        awaitDb { app.db.sleeps().all().single().endsAt != null }
        pageClick("Sound machine")
        pageClick("Brown")
        pageClick("15 min")
        compose.onNodeWithContentDescription("Sound volume").performScrollTo()
            .performSemanticsAction(SemanticsActions.SetProgress) { it(.15f) }
        try {
            android.util.Log.i("SproutAudioQa", "Before click: ${SoundService.state.value}; clock=${compose.mainClock.currentTime}")
            pageClick("Play brown sound")
            android.util.Log.i("SproutAudioQa", "After click: ${SoundService.state.value}; clock=${compose.mainClock.currentTime}")
            awaitDb { SoundService.state.value.playing || SoundService.state.value.error != null }
            assertNull(SoundService.state.value.error)
            assertTrue(SoundService.state.value.playing)
            assertEquals("Brown", SoundService.state.value.kind)
            assertEquals(15, SoundService.state.value.minutes)
            assertEquals(.15f, SoundService.state.value.volume, .01f)
            pageClick("Stop brown sound")
            awaitDb { !SoundService.state.value.playing }
        } finally {
            SoundService.stop(app)
        }
    }

    @Test
    fun milestoneCompletionCreatesLeafFromLabelledSwitches() {
        care("Milestones")
        pageClick("First smile")
        compose.onNodeWithContentDescription("Completed").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Create a memory leaf").performScrollTo().performClick()
        compose.onNodeWithText("Save milestone").performScrollTo().performClick()
        awaitDb { app.db.milestones().all().singleOrNull()?.memoryId != null }
        assertEquals("First smile", runBlocking { app.db.memorys().all().single().title })
    }

    @Test
    fun emergencyCardEditsAndCopiesOffline() {
        care("Emergency card")
        pageClick("Edit emergency card")
        compose.onNode(hasText("Allergies") and hasSetTextAction()).performTextInput("Peanut")
        compose.onNodeWithText("Doctor / clinic").performScrollTo().performTextInput("Family clinic")
        compose.onNodeWithText("Save emergency card").performScrollTo().performClick()
        awaitDb { app.db.emergencyCards().all().singleOrNull()?.allergies == "Peanut" }
        waitText("Peanut")
        compose.waitUntil(60000) { compose.activity.hasWindowFocus() }
        pageClick("Copy emergency card")
        compose.waitForIdle()
        compose.runOnUiThread {
            val copied = app.getSystemService(ClipboardManager::class.java).primaryClip
                ?.getItemAt(0)?.text.toString()
            assertTrue(copied.contains("Willow") && copied.contains("Peanut"))
        }
    }

    @Test
    fun healthRecordSavesEditsAndDeletes() {
        care("Health journal")
        pageClick("Add health record")
        compose.onNodeWithText("Title").performScrollTo().performTextInput("Clinic notes")
        compose.onNodeWithText("Save health record").performScrollTo().performClick()
        awaitDb { app.db.healthRecords().all().size == 1 }
        pageClick("Clinic notes")
        compose.onNodeWithText("Title").performScrollTo().performTextReplacement("Follow-up notes")
        compose.onNodeWithText("Save health record").performScrollTo().performClick()
        awaitDb { app.db.healthRecords().all().single().title == "Follow-up notes" }
        pageClick("Follow-up notes")
        compose.onNodeWithText("Delete health record").performScrollTo().performClick()
        compose.onNodeWithText("Delete").performClick()
        awaitDb { app.db.healthRecords().all().isEmpty() }
    }
}
