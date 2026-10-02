package com.nothatcher.sproutbook

import androidx.compose.ui.test.*
import com.nothatcher.sproutbook.data.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class EverydayFlowTest : FlowFixture() {
    private fun awaitSheetClosed(label: String) {
        compose.waitUntil(60000) {
            compose.onAllNodesWithText(label).fetchSemanticsNodes().isEmpty()
        }
    }

    private fun awaitMessageClosed(label: String) {
        waitText(label)
        compose.waitUntil(60000) {
            compose.onAllNodesWithText(label).fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun routineCreateCompleteUndoEditDelete() {
        care("Routines & responsibilities")
        pageClick("Add routine")
        compose.onNodeWithText("Routine title").performTextInput("Pack school bag")
        compose.onNodeWithText("Responsible person (optional)").performTextInput("Together")
        compose.onNodeWithText("Save routine").performScrollTo().performClick()
        awaitDb { app.db.routines().all().size == 1 }
        awaitSheetClosed("Save routine")
        awaitMessageClosed("Routine saved")
        pageClick("Complete Pack school bag")
        awaitDb { app.db.routineCompletions().all().size == 1 }
        pageClick("Undo Pack school bag")
        awaitDb { app.db.routineCompletions().all().isEmpty() }
        pageClick("Pack school bag")
        compose.onNodeWithText("Routine title").performTextReplacement("Pack activity bag")
        compose.onNodeWithText("Save routine").performScrollTo().performClick()
        awaitDb { app.db.routines().all().single().title == "Pack activity bag" }
        awaitSheetClosed("Save routine")
        awaitMessageClosed("Routine saved")
        pageClick("Pack activity bag")
        compose.onNodeWithText("Delete routine").performScrollTo().performClick()
        compose.onNodeWithText("Delete").performClick()
        awaitDb { app.db.routines().all().isEmpty() }
    }

    @Test
    fun pottyQuickLogAndManualEditDelete() {
        care("Potty journal")
        pageClick("Log wet potty")
        awaitDb { app.db.pottyLogs().all().size == 1 }
        awaitMessageClosed("Potty visit logged")
        pageClick("Wet potty visit")
        compose.onNodeWithText("Potty notes").performTextInput("Asked to try")
        compose.onNodeWithText("Save potty visit").performScrollTo().performClick()
        awaitDb { app.db.pottyLogs().all().single().notes == "Asked to try" }
        awaitSheetClosed("Save potty visit")
        awaitMessageClosed("Potty visit saved")
        pageClick("Wet potty visit")
        compose.onNodeWithText("Delete potty visit").performScrollTo().performClick()
        compose.onNodeWithText("Delete").performClick()
        awaitDb { app.db.pottyLogs().all().isEmpty() }
    }

    @Test
    fun milkManualSaveDirtyDraftGuardAndUse() {
        care("Milk freezer")
        pageClick("Add container")
        compose.onNodeWithText("Container label").performTextInput("Bag one")
        compose.onNodeWithText("Stored amount · mL").performTextInput("90")
        compose.onNodeWithText("Save container").performScrollTo().performClick()
        awaitDb { app.db.milkContainers().all().size == 1 }
        awaitSheetClosed("Save container")
        awaitMessageClosed("Container saved")
        pageClick("Bag one")
        compose.onNodeWithText("Stored amount · mL").performTextReplacement("120")
        compose.onNodeWithText("Mark used").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("Save container").performScrollTo().performClick()
        awaitDb { app.db.milkContainers().all().single().amountMl == 120.0 }
        awaitSheetClosed("Save container")
        awaitMessageClosed("Container saved")
        pageClick("Bag one")
        compose.onNodeWithText("Mark used").performScrollTo().performClick()
        awaitDb { app.db.milkContainers().all().single().status == "Used" }
        assertTrue(runBlocking { app.db.feeds().all().isEmpty() })
    }

    @Test
    fun pumpLinkAndCaregiverGuards() {
        runBlocking {
            app.repository.saveFeed(
                Feed(
                    id = "pump",
                    childId = "qa-child",
                    kind = "Pump",
                    startsAt = 1000,
                    leftMl = 30.0,
                    rightMl = 60.0,
                )
            )
        }
        care("Milk freezer")
        pageClick("Store a pump session")
        compose.onNodeWithText("Pump · 90 mL").performScrollTo().performClick()
        awaitDb { app.db.milkContainers().all().size == 1 }
        awaitSheetClosed("Choose a saved pump")
        awaitMessageClosed("Pump stored")
        pageClick("Store a pump session")
        compose.onNodeWithText("No unstored pump sessions").assertExists()
        back()
        runBlocking { app.settings.boolean("grandparent", true) }
        waitText("Grandparent mode · Read only")
        compose.onNodeWithText("Add container").performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun newToolsSwitchChildAndStayReadOnly() {
        runBlocking {
            app.db.children().save(Child(id = "ash", name = "Ash", stage = "TODDLER"))
            app.repository.saveRoutine(Routine(childId = "qa-child", title = "Willow routine"))
            app.repository.savePotty(PottyLog(childId = "qa-child", recordedAt = 1000))
            app.repository.saveMilk(
                MilkContainer(
                    childId = "qa-child",
                    storedAt = 1000,
                    label = "Willow bag",
                    amountMl = 90.0,
                )
            )
        }
        care("Routines & responsibilities")
        compose.onNodeWithContentDescription("Switch child").performClick()
        compose.onNodeWithText("Ash").performClick()
        waitText("Toddler · Your growing family")
        compose.onNodeWithText("Willow routine").assertDoesNotExist()
        runBlocking { app.settings.boolean("grandparent", true) }
        waitText("Grandparent mode · Read only")
        compose.onNodeWithText("Add routine").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithContentDescription("Back").performClick()
        care("Potty journal")
        compose.onNodeWithText("Log wet potty").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithContentDescription("Back").performClick()
        care("Milk freezer")
        compose.onNodeWithText("Add container").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("Store a pump session").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("Willow bag").assertDoesNotExist()
    }
}
