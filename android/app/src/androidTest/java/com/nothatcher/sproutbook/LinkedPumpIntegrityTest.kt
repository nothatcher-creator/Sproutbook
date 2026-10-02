package com.nothatcher.sproutbook

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nothatcher.sproutbook.data.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class LinkedPumpIntegrityTest {
    @Test
    fun storedPumpCannotBecomeBottle(): Unit = checkChange {
        it.copy(kind = "Bottle", amountMl = 90.0)
    }

    @Test fun storedPumpCannotMoveToAnotherChild(): Unit = checkChange { it.copy(childId = "b") }

    private fun checkChange(change: (Feed) -> Feed): Unit = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val settings = Settings(context)
        val original = settings.flow.first().grandparent
        try {
            settings.boolean("grandparent", false)
            db.children().save(Child(id = "a", name = "Willow"))
            db.children().save(Child(id = "b", name = "Ash"))
            val repo = FamilyRepository(db, settings)
            val pump =
                Feed(
                    id = "p",
                    childId = "a",
                    kind = "Pump",
                    startsAt = 1000,
                    leftMl = 30.0,
                    rightMl = 60.0,
                )
            repo.saveFeed(pump)
            repo.storePump("p", "a")
            try {
                repo.saveFeed(change(pump))
                fail("Stored pump identity changed")
            } catch (_: IllegalArgumentException) {}
            assertEquals("a", db.feeds().get("p")!!.childId)
            assertEquals("Pump", db.feeds().get("p")!!.kind)
        } finally {
            settings.boolean("grandparent", original)
            db.close()
        }
    }
}
