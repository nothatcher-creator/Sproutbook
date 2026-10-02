package com.nothatcher.sproutbook

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nothatcher.sproutbook.data.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class RepositoryTest {
    @Test
    fun leafCreationIsAtomicAndCaregiverModeGuardsWrites(): Unit = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val settings = Settings(context)
        val old = settings.flow.first().grandparent
        val repo = FamilyRepository(db, settings)
        try {
            settings.boolean("grandparent", false)
            db.children().save(Child(id = "a", name = "Rowan"))
            db.children().save(Child(id = "b", name = "Ash"))
            val m =
                Milestone(childId = "a", stage = "BABY", title = "First smile", completedOn = 10)
            repo.saveMilestone(m, true)
            repo.saveMilestone(m.copy(notes = "A new note"), true)
            assertEquals(1, db.memorys().forChild("a").size)
            assertEquals(0, db.memorys().forChild("b").size)
            repo.saveSleep(Sleep(childId = "a", startsAt = 1000, endsAt = 3000))
            try {
                repo.saveSleep(Sleep(childId = "a", startsAt = 2000, endsAt = 4000))
                fail("Overlap accepted")
            } catch (expected: IllegalArgumentException) {}
            settings.boolean("grandparent", true)
            try {
                repo.saveFeed(
                    Feed(childId = "a", kind = "Bottle", startsAt = 1000, amountMl = 90.0)
                )
                fail("Read-only write accepted")
            } catch (expected: IllegalStateException) {}
            assertEquals(0, db.feeds().all().size)
        } finally {
            settings.boolean("grandparent", old)
            db.close()
        }
    }
}
