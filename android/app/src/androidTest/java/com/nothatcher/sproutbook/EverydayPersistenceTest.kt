package com.nothatcher.sproutbook

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nothatcher.sproutbook.data.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class EverydayPersistenceTest {
    @Test
    fun newRecordsSurviveDatabaseCloseAndReopen(): Unit = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "v32-persistence.db"
        context.deleteDatabase(name)
        val settings = Settings(context)
        val old = settings.flow.first().grandparent
        var db = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
        try {
            settings.boolean("grandparent", false)
            db.children().save(Child(id = "a", name = "Willow"))
            val repo = FamilyRepository(db, settings)
            repo.saveRoutine(
                Routine(
                    id = "r",
                    childId = "a",
                    title = "A story",
                    weekdays = 127,
                    timeMinutes = 1140,
                )
            )
            repo.completeRoutine("r", "a", 1, true)
            repo.savePotty(
                PottyLog(id = "p", childId = "a", recordedAt = 1000, kind = "Tried", notes = "Kept")
            )
            repo.saveMilk(
                MilkContainer(
                    id = "m",
                    childId = "a",
                    label = "Precise",
                    storedAt = 1000,
                    amountMl = 90.125,
                )
            )
            db.close()
            db = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
            assertEquals(1140, db.routines().get("r")!!.timeMinutes)
            assertEquals(1L, db.routineCompletions().all().single().day)
            assertEquals("Kept", db.pottyLogs().get("p")!!.notes)
            assertEquals(90.125, db.milkContainers().get("m")!!.amountMl, 0.0)
            assertEquals(11, db.openHelper.readableDatabase.version)
        } finally {
            settings.boolean("grandparent", old)
            db.close()
            context.deleteDatabase(name)
        }
    }
}
