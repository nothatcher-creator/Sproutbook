package com.nothatcher.sproutbook

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import com.nothatcher.sproutbook.data.*
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class GestureTest : FlowFixture() {
    @Test
    fun calendarLongPressKeepsDate() {
        compose.onNodeWithTag("nav-schedule").performClick()
        val day = LocalDate.now()
        compose.onNodeWithTag("day-${day.toEpochDay()}").performTouchInput { longClick() }
        compose.onNodeWithText("Appointment title").performTextInput("Check-up")
        compose.onNodeWithText("Date · $day").assertExists()
        compose.onNodeWithText("Save appointment").performScrollTo().performClick()
        awaitDb { app.db.appointments().all().size == 1 }
        assertEquals(
            day,
            com.nothatcher.sproutbook.ui.dateOf(
                runBlocking { app.db.appointments().all().single().startsAt }
            ),
        )
    }

    @Test
    fun toothTapOnlyViewsLongPressChanges() {
        care("Little teeth")
        compose.onNodeWithTag("tooth-0").performScrollTo().performTouchInput { click() }
        compose.onNodeWithText("Not seen").assertExists()
        assertTrue(runBlocking { app.db.tooths().all().isEmpty() })
        back()
        compose.onNodeWithTag("tooth-0").performScrollTo().performTouchInput { longClick() }
        compose.onNodeWithText("Erupted").performClick()
        compose.onNodeWithText("Save tooth").performScrollTo().performClick()
        awaitDb { app.db.tooths().all().firstOrNull()?.stage == "Erupted" }
    }

    @Test
    fun arrangeLeafSnapsAndScrollDoesNotOpenIt() {
        runBlocking {
            app.repository.saveMemory(
                Memory(
                    id = "gesture-leaf",
                    childId = "qa-child",
                    title = "First smile",
                    occurredOn = 1,
                )
            )
        }
        care("Memory tree")
        compose.onNodeWithText("Arrange").performScrollTo().performClick()
        val tree =
            compose.onNodeWithContentDescription(
                "Memory tree, 1 leaves. A timeline is also available."
            )
        tree.performScrollTo()
        tree.performTouchInput { click(Offset(width * .22f, height * .22f)) }
        tree.performTouchInput { click(Offset(width * .35f, height * .14f)) }
        awaitDb { app.db.memorys().get("gesture-leaf")?.anchor == 1 }
        compose.onNodeWithText("Done arranging").performScrollTo().performClick()
        tree.performTouchInput { swipeUp() }
        compose.onNodeWithText("A moment to keep").assertDoesNotExist()
        assertEquals(1, runBlocking { app.db.memorys().get("gesture-leaf")!!.anchor })
    }

    @Test
    fun longPressDragSnapsToBranch() {
        runBlocking { app.repository.saveMemory(Memory(id="drag-leaf",childId="qa-child",title="A new day",occurredOn=1)) }
        care("Memory tree")
        compose.onNodeWithText("Arrange").performScrollTo().performClick()
        val tree=compose.onNodeWithContentDescription("Memory tree, 1 leaves. A timeline is also available.")
        tree.performScrollTo()
        tree.performTouchInput {
            down(Offset(width*.22f,height*.22f))
            advanceEventTime(700)
            moveTo(Offset(width*.35f,height*.14f),delayMillis=100)
            up()
        }
        awaitDb { app.db.memorys().get("drag-leaf")?.anchor==1 }
    }
    @Test
    fun scrollingAcrossToothNeverChangesOrOpensIt() {
        care("Little teeth")
        compose.onNodeWithTag("tooth-0").performScrollTo().performTouchInput { swipeUp() }
        compose.onNodeWithText("To change this record, close this view and long-press the tooth.").assertDoesNotExist()
        assertTrue(runBlocking { app.db.tooths().all().isEmpty() })
    }
}
