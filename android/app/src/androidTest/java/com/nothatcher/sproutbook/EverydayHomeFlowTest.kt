package com.nothatcher.sproutbook

import androidx.compose.ui.test.*
import com.nothatcher.sproutbook.data.*
import kotlinx.coroutines.runBlocking
import org.junit.Test

class EverydayHomeFlowTest : FlowFixture() {
    @Test
    fun todayOpensLiveRoutineMilkAndToddlerJournal() {
        runBlocking {
            app.repository.saveRoutine(
                Routine(id = "r", childId = "qa-child", title = "Bedtime story")
            )
            app.repository.saveMilk(
                MilkContainer(
                    childId = "qa-child",
                    storedAt = 1000,
                    label = "Reserve",
                    amountMl = 90.0,
                )
            )
        }
        pageClick("Today’s routines · 1 remaining")
        pageClick("Complete Bedtime story")
        awaitDb { app.db.routineCompletions().all().size == 1 }
        compose.onNodeWithTag("nav-today").performClick()
        pageClick("Today’s routines · 0 remaining")
        compose.onNodeWithText("Undo Bedtime story").performScrollTo().assertExists()
        compose.onNodeWithTag("nav-today").performClick()
        pageClick("Milk freezer · 90 mL")
        compose.onNodeWithText("Reserve").performScrollTo().assertExists()
        runBlocking {
            app.repository.saveChild(
                app.db.children().all().first { it.id == "qa-child" }.copy(stage = "TODDLER")
            )
        }
        compose.onNodeWithTag("nav-today").performClick()
        waitText("YOUR TODDLER CHAPTER")
        pageClick("Potty journal")
        compose.onNodeWithText("Log wet potty").performScrollTo().assertExists()
    }
}
