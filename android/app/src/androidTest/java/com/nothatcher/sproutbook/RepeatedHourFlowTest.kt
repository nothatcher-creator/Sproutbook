package com.nothatcher.sproutbook

import androidx.compose.ui.test.*
import com.nothatcher.sproutbook.data.*
import java.time.Instant
import java.util.TimeZone
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/** Editing notes must not reinterpret the later occurrence of a repeated clock hour. */
class RepeatedHourFlowTest : FlowFixture() {
    private val laterHour = Instant.parse("2025-11-02T06:30:00.125Z").toEpochMilli()

    private fun inRepeatedHour(block: () -> Unit) {
        val original = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))
        try {
            block()
        } finally {
            TimeZone.setDefault(original)
        }
    }

    private fun seedMilk(status: String = "Frozen") = runBlocking {
        app.repository.saveMilk(
            MilkContainer(
                id = "dst-milk",
                childId = "qa-child",
                label = "Autumn reserve",
                storedAt = laterHour,
                amountMl = 90.125,
            )
        )
        if (status != "Frozen") app.repository.finishMilk("dst-milk", "qa-child", status == "Used")
    }

    @Test
    fun unchangedFrozenContainerCanBeUsedDuringRepeatedHour() = inRepeatedHour {
        seedMilk()
        care("Milk freezer")
        pageClick("Autumn reserve")
        compose.onNodeWithText("Mark used").performScrollTo().assertIsEnabled().performClick()
        awaitDb { app.db.milkContainers().get("dst-milk")!!.status == "Used" }
        assertEquals(laterHour, runBlocking { app.db.milkContainers().get("dst-milk")!!.storedAt })
    }

    @Test
    fun usedContainerNotesPreserveRepeatedHourTimestamp() = inRepeatedHour {
        seedMilk("Used")
        care("Milk freezer")
        pageClick("Used")
        pageClick("Autumn reserve")
        compose.onNodeWithText("Container notes").performTextInput("Original date retained")
        compose.onNodeWithText("Save container").performScrollTo().performClick()
        awaitDb { app.db.milkContainers().get("dst-milk")!!.notes == "Original date retained" }
        assertEquals(laterHour, runBlocking { app.db.milkContainers().get("dst-milk")!!.storedAt })
    }

    @Test
    fun pottyNotesPreserveRepeatedHourTimestamp() = inRepeatedHour {
        runBlocking {
            app.repository.savePotty(
                PottyLog(
                    id = "dst-potty",
                    childId = "qa-child",
                    recordedAt = laterHour,
                    kind = "Tried",
                )
            )
        }
        care("Potty journal")
        pageClick("Tried potty visit")
        compose.onNodeWithText("Potty notes").performTextInput("A calm visit")
        compose.onNodeWithText("Save potty visit").performScrollTo().performClick()
        awaitDb { app.db.pottyLogs().get("dst-potty")!!.notes == "A calm visit" }
        assertEquals(laterHour, runBlocking { app.db.pottyLogs().get("dst-potty")!!.recordedAt })
    }
}
