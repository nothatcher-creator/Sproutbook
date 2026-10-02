package com.nothatcher.sproutbook

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nothatcher.sproutbook.data.*
import com.nothatcher.sproutbook.services.BackupManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test

class EverydayRepositoryTest {
    @Test
    fun datedCompletionIsUniqueUndoableAndChildScoped(): Unit = fixture { repo ->
        val r = Routine(id = "r", childId = "a", title = "Pack school bag")
        repo.saveRoutine(r)
        coroutineScope { List(8) { async { repo.completeRoutine("r", "a", 1, true) } }.awaitAll() }
        assertEquals(1, repo.db.routineCompletions().all().size)
        repo.completeRoutine("r", "a", 2, true)
        repo.completeRoutine("r", "a", 1, false)
        assertEquals(2L, repo.db.routineCompletions().all().single().day)
        rejected { repo.completeRoutine("r", "b", 2, false) }
        rejected { repo.saveRoutine(r.copy(childId = "b")) }
        rejected { repo.saveRoutine(r.copy(weekdays = 0)) }
        rejected { repo.saveRoutine(r.copy(timeMinutes = 1440)) }
        rejected {
            repo.completeRoutine("r", "a", java.time.LocalDate.now().plusDays(1).toEpochDay(), true)
        }
        assertTrue(repo.db.routineCompletions().forDay("b", 2).first().isEmpty())
        repo.db.routines().delete("r", "a")
        assertTrue(repo.db.routineCompletions().all().isEmpty())
    }

    @Test
    fun pumpTransferAndTerminalStockAreAtomic(): Unit = fixture { repo ->
        repo.saveFeed(
            Feed(
                id = "f",
                childId = "a",
                kind = "Pump",
                startsAt = 1000,
                leftMl = 30.0,
                rightMl = 60.0,
            )
        )
        repo.storePump("f", "a")
        rejected { repo.storePump("f", "a") }
        rejected { repo.storePump("f", "b") }
        val bag = repo.db.milkContainers().all().single()
        assertEquals(90.0, bag.amountMl, 0.0)
        assertEquals("f", bag.pumpId)
        repo.finishMilk(bag.id, "a", true)
        rejected { repo.finishMilk(bag.id, "a", true) }
        rejected { repo.saveMilk(bag.copy(status = "Frozen")) }
        assertEquals("Used", repo.db.milkContainers().get(bag.id)!!.status)
        assertEquals(1, repo.db.feeds().all().size) // Storage use must not fabricate a feed.
        repo.saveMilk(
            MilkContainer(
                id = "m",
                childId = "a",
                label = "Bag two",
                storedAt = 1000,
                amountMl = 45.0,
            )
        )
        rejected {
            repo.saveMilk(
                MilkContainer(childId = "a", label = "Bad", storedAt = 1000, amountMl = Double.NaN)
            )
        }
        rejected { repo.saveMilk(bag.copy(childId = "b")) }
        repo.db.feeds().delete("f", "a")
        assertNull(repo.db.milkContainers().get(bag.id)!!.pumpId)
        assertEquals("Used", repo.db.milkContainers().get(bag.id)!!.status)
    }

    @Test
    fun backupRoundTripAndCaregiverGuardForEveryNewMutation(): Unit = fixture { repo ->
        repo.saveRoutine(Routine(id = "r", childId = "a", title = "Bedtime story"))
        repo.completeRoutine("r", "a", 1, true)
        repo.savePotty(
            PottyLog(
                id = "p",
                childId = "a",
                recordedAt = 1000,
                kind = "Tried",
                notes = "Asked to try",
            )
        )
        repo.saveMilk(
            MilkContainer(id = "m", childId = "a", storedAt = 1000, label = "Bag", amountMl = 90.0)
        )
        val manager = BackupManager(ApplicationProvider.getApplicationContext(), repo)
        val snapshot = manager.snapshot()
        assertEquals(3, snapshot["version"].asInt)
        assertTrue(manager.restore(snapshot))
        assertEquals("Asked to try", repo.db.pottyLogs().get("p")!!.notes)
        assertEquals(1, repo.db.routineCompletions().all().size)
        assertEquals(90.0, repo.db.milkContainers().get("m")!!.amountMl, 0.0)
        repo.settings.boolean("grandparent", true)
        guarded { repo.saveRoutine(Routine(childId = "a", title = "No")) }
        guarded { repo.completeRoutine("r", "a", 1, false) }
        guarded { repo.savePotty(PottyLog(childId = "a", recordedAt = 1000)) }
        guarded {
            repo.saveMilk(
                MilkContainer(childId = "a", storedAt = 1000, label = "No", amountMl = 10.0)
            )
        }
        guarded { repo.finishMilk("m", "a", true) }
        guarded { repo.storePump("missing", "a") }
        assertEquals("Frozen", repo.db.milkContainers().get("m")!!.status)
    }

    private suspend fun rejected(action: suspend () -> Unit) {
        try {
            action()
            fail("Invalid mutation accepted")
        } catch (_: IllegalArgumentException) {}
    }

    private suspend fun guarded(action: suspend () -> Unit) {
        try {
            action()
            fail("Caregiver mutation accepted")
        } catch (_: IllegalStateException) {}
    }

    private fun fixture(test: suspend (FamilyRepository) -> Unit): Unit = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val settings = Settings(context)
        val old = settings.flow.first().grandparent
        try {
            settings.boolean("grandparent", false)
            db.children().save(Child(id = "a", name = "Willow"))
            db.children().save(Child(id = "b", name = "Ash"))
            test(FamilyRepository(db, settings))
        } finally {
            settings.boolean("grandparent", old)
            db.close()
        }
    }
}
