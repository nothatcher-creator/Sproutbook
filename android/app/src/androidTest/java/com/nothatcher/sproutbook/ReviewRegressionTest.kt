package com.nothatcher.sproutbook

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nothatcher.sproutbook.data.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ReviewRegressionTest {
    @Test
    fun excessivePumpTotalCannotPoisonBackup(): Unit = runBlocking {
        withRepo { repo ->
            try {
                repo.saveFeed(
                    Feed(
                        childId = "a",
                        kind = "Pump",
                        startsAt = 1000,
                        leftMl = 1500.0,
                        rightMl = 1500.0,
                    )
                )
                fail("Excessive total accepted")
            } catch (expected: IllegalArgumentException) {}
            assertTrue(repo.db.feeds().all().isEmpty())
        }
    }

    @Test
    fun excessiveNotesAreRejectedBeforeSaving(): Unit = runBlocking {
        withRepo { repo ->
            try {
                repo.saveChild(Child(id = "a", name = "Rowan", notes = "x".repeat(10001)))
                fail("Oversized notes accepted")
            } catch (expected: IllegalArgumentException) {
                assertTrue(expected.message.orEmpty().contains("Notes"))
            }
            assertEquals("", repo.db.children().all().single().notes)
        }
    }

    @Test
    fun collidingMilestoneLeafNeverCrossesChildren(): Unit = runBlocking {
        withRepo { repo ->
            repo.db.children().save(Child(id = "b", name = "Ash"))
            repo.saveMemory(
                Memory(
                    id = "milestone-collision",
                    childId = "b",
                    title = "Ash's memory",
                    occurredOn = 1,
                )
            )
            repo.saveMilestone(
                Milestone(
                    id = "collision",
                    childId = "a",
                    stage = "BABY",
                    title = "Rowan's smile",
                    completedOn = 1,
                ),
                true,
            )
            val id = repo.db.milestones().get("collision")!!.memoryId!!
            assertEquals("a", repo.db.memorys().get(id)!!.childId)
            assertEquals("Ash's memory", repo.db.memorys().get("milestone-collision")!!.title)
        }
    }

    private suspend fun withRepo(block: suspend (FamilyRepository) -> Unit) {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val settings = Settings(context)
        val old = settings.flow.first().grandparent
        try {
            settings.boolean("grandparent", false)
            db.children().save(Child(id = "a", name = "Rowan"))
            block(FamilyRepository(db, settings))
        } finally {
            settings.boolean("grandparent", old)
            db.close()
        }
    }
}
