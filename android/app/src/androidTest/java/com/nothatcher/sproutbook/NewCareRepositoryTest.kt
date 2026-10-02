package com.nothatcher.sproutbook

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nothatcher.sproutbook.data.*
import com.nothatcher.sproutbook.services.BackupManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class NewCareRepositoryTest {
    @Test
    fun transactionsChildIsolationBackupAndReadOnly(): Unit = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val settings = Settings(context)
        val old = settings.flow.first().grandparent
        val repo = FamilyRepository(db, settings)
        try {
            settings.boolean("grandparent", false)
            db.children().save(Child(id = "a", name = "Willow"))
            db.children().save(Child(id = "b", name = "Ash"))
            repo.saveDiaper(Diaper(id = "d", childId = "a", recordedAt = 1000, kind = "Mixed"))
            val bottle = BottlePrep(id = "p", childId = "a", preparedAt = 1000, amountMl = 90.0)
            repo.saveBottlePrep(bottle)
            repo.finishBottle("p", "a", true, 2000)
            try {
                repo.finishBottle("p", "a", true, 2000)
                fail("Duplicate use accepted")
            } catch (_: IllegalArgumentException) {}
            assertEquals(1, db.feeds().all().size)
            assertEquals("Used", db.bottlePreps().get("p")!!.status)
            repo.saveBottlePrep(
                BottlePrep(id = "partial", childId = "a", preparedAt = 1000, amountMl = 120.0)
            )
            repo.finishBottle("partial", "a", true, 2000, 30.0)
            assertEquals(
                30.0,
                db.feeds().get(db.bottlePreps().get("partial")!!.feedId!!)!!.amountMl,
                0.0,
            )
            assertEquals(120.0, db.bottlePreps().get("partial")!!.amountMl, 0.0)
            val stock =
                Inventory(
                    id = "i",
                    childId = "a",
                    name = "Wipes",
                    quantity = 0.0,
                    threshold = 2.0,
                    unit = "packs",
                )
            repo.saveInventory(stock)
            repo.addLowStock("a")
            repo.addLowStock("a")
            val shopping = db.shoppingItems().all().single()
            assertEquals(3.0, shopping.quantity, 0.0)
            repo.buyShopping(shopping.id, "a")
            repo.buyShopping(shopping.id, "a")
            assertEquals(3.0, db.inventorys().get("i")!!.quantity, 0.0)
            try {
                repo.saveShopping(shopping.copy(childId = "b"))
                fail("Cross-child link accepted")
            } catch (_: IllegalArgumentException) {}
            assertTrue(db.diapers().forChild("b").isEmpty())
            val backup = BackupManager(context, repo)
            val snapshot = backup.snapshot()
            db.children().delete("a")
            assertTrue(backup.restore(snapshot))
            assertEquals("Mixed", db.diapers().get("d")!!.kind)
            assertEquals(2, db.feeds().all().size)
            assertEquals("Used", db.bottlePreps().get("p")!!.status)
            assertTrue(db.shoppingItems().get(shopping.id)!!.checked)
            settings.boolean("grandparent", true)
            try {
                repo.saveDiaper(Diaper(childId = "a", recordedAt = 1000))
                fail("Read-only diaper accepted")
            } catch (_: IllegalStateException) {}
            try {
                repo.addLowStock("a")
                fail("Read-only shopping accepted")
            } catch (_: IllegalStateException) {}
            try {
                repo.saveBottlePrep(bottle)
                fail("Read-only bottle accepted")
            } catch (_: IllegalStateException) {}
            settings.boolean("grandparent", false)
            val legacy = snapshot.deepCopy().apply { addProperty("version", 1) }
            listOf("diapers", "bottlePreps", "shoppingItems", "routines", "routineCompletions", "pottyLogs", "milkContainers").forEach {
                legacy.getAsJsonObject("tables").remove(it)
            }
            assertTrue(backup.restore(legacy))
            assertEquals(2, db.feeds().all().size)
            assertTrue(db.diapers().all().isEmpty())
            assertEquals(3.0, db.inventorys().get("i")!!.quantity, 0.0)
        } finally {
            settings.boolean("grandparent", old)
            db.close()
        }
    }

    @Test
    fun failedRestockRollsBackAndDeletedParentsClearLinks(): Unit = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val settings = Settings(context)
        val old = settings.flow.first().grandparent
        val repo = FamilyRepository(db, settings)
        try {
            settings.boolean("grandparent", false)
            db.children().save(Child(id = "a", name = "Willow"))
            repo.saveInventory(
                Inventory(id = "i", childId = "a", name = "Wipes", quantity = 999999.0)
            )
            repo.saveShopping(
                ShoppingItem(
                    id = "s",
                    childId = "a",
                    name = "Wipes",
                    quantity = 2.0,
                    inventoryId = "i",
                )
            )
            try {
                repo.buyShopping("s", "a")
                fail("Overflow accepted")
            } catch (_: IllegalArgumentException) {}
            assertFalse(db.shoppingItems().get("s")!!.checked)
            assertEquals(999999.0, db.inventorys().get("i")!!.quantity, 0.0)
            db.inventorys().delete("i", "a")
            assertNull(db.shoppingItems().get("s")!!.inventoryId)
            repo.saveBottlePrep(
                BottlePrep(id = "p", childId = "a", preparedAt = 1000, amountMl = 90.0)
            )
            repo.finishBottle("p", "a", true, 2000)
            val feed = db.bottlePreps().get("p")!!.feedId!!
            db.feeds().delete(feed, "a")
            assertNull(db.bottlePreps().get("p")!!.feedId)
            assertEquals("Used", db.bottlePreps().get("p")!!.status)
        } finally {
            settings.boolean("grandparent", old)
            db.close()
        }
    }
}
