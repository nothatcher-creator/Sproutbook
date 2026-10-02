package com.nothatcher.sproutbook

import androidx.compose.ui.test.*
import com.nothatcher.sproutbook.data.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class EverydaySecondaryFlowTest : FlowFixture() {
    private fun feedbackFinished(text: String) {
        waitText(text)
        compose.waitUntil(60000) {
            compose.onAllNodesWithText(text).fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun milkDiscardHistoryNotesAndDelete() {
        runBlocking {
            app.repository.saveMilk(
                MilkContainer(
                    id = "m",
                    childId = "qa-child",
                    label = "Reserve",
                    storedAt = 1000,
                    amountMl = 90.125,
                )
            )
        }
        care("Milk freezer")
        pageClick("Reserve")
        compose.onNodeWithText("Mark discarded").performScrollTo().performClick()
        awaitDb { app.db.milkContainers().get("m")!!.status == "Discarded" }
        feedbackFinished("Container marked discarded")
        pageClick("Discarded")
        pageClick("Reserve")
        compose.onNodeWithText("Container notes").performTextInput("Recorded discard")
        compose.onNodeWithText("Stored amount · mL").assertDoesNotExist()
        compose.onNodeWithText("Save container").performScrollTo().performClick()
        awaitDb { app.db.milkContainers().get("m")!!.notes == "Recorded discard" }
        feedbackFinished("Container saved")
        assertEquals(90.125, runBlocking { app.db.milkContainers().get("m")!!.amountMl }, 0.0)
        pageClick("Reserve")
        compose.onNodeWithText("Delete container").performScrollTo().performClick()
        compose.onNodeWithText("Delete").performClick()
        awaitDb { app.db.milkContainers().all().isEmpty() }
    }

    @Test
    fun pausedRoutineRetainsUndoAndHistory() {
        runBlocking {
            app.repository.saveRoutine(Routine(id = "r", childId = "qa-child", title = "Story"))
            app.repository.completeRoutine(
                "r",
                "qa-child",
                java.time.LocalDate.now().toEpochDay(),
                true,
            )
            app.repository.saveRoutine(app.db.routines().get("r")!!.copy(active = false))
        }
        care("Routines & responsibilities")
        pageClick("Undo Story")
        awaitDb { app.db.routineCompletions().all().isEmpty() }
        pageClick("Manage · Story")
        compose.onNodeWithText("Routine notes").performTextInput("Paused for now")
        compose.onNodeWithText("Save routine").performScrollTo().performClick()
        awaitDb { app.db.routines().get("r")!!.notes == "Paused for now" }
    }
}
