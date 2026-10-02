package com.nothatcher.sproutbook

import androidx.compose.ui.test.*
import androidx.room.withTransaction
import com.nothatcher.sproutbook.data.*
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class DensityTest : FlowFixture() {
    @Test
    fun denseFamilyKeepsTreeInChapters() {
        runBlocking {
            val now = System.currentTimeMillis()
            app.db.withTransaction {
                listOf("Ash", "Juniper", "Rowan").forEach {
                    app.db.children().save(Child(id = it, name = it))
                }
                repeat(500) { i ->
                    app.db
                        .memorys()
                        .save(
                            Memory(
                                id = "memory-$i",
                                childId = "qa-child",
                                title = "Little moment ${i+1}",
                                occurredOn = LocalDate.now().toEpochDay() - i,
                                anchor = i % 32,
                                chapter = i / 32,
                            )
                        )
                    app.db
                        .feeds()
                        .save(
                            Feed(
                                id = "feed-$i",
                                childId = "qa-child",
                                kind = "Bottle",
                                startsAt = now - i * 10800000L,
                                amountMl = 90.0,
                            )
                        )
                }
                repeat(300) { i ->
                    app.db
                        .appointments()
                        .save(
                            Appointment(
                                id = "visit-$i",
                                childId = "qa-child",
                                title = "Family plan ${i+1}",
                                startsAt = now + i * 86400000L,
                            )
                        )
                }
            }
        }
        care("Memory tree")
        waitText("Growing memories")
        compose
            .onNodeWithContentDescription("Memory tree, 32 leaves. A timeline is also available.")
            .assertExists()
        pageClick("Next chapter")
        compose.onNodeWithText("Chapter 2").assertExists()
        pageClick("Previous")
        compose.onNodeWithText("Chapter 1").assertExists()
        assertEquals(500, runBlocking { app.db.memorys().forChild("qa-child").size })
        compose.onNodeWithTag("nav-today").performClick()
        waitText("YOUR BABY CHAPTER")
    }
}
