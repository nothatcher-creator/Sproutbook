package com.nothatcher.sproutbook

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nothatcher.sproutbook.data.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FoundationTest {
    @Test
    fun profilesSurviveReopenAndStayIndependent(): Unit = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        context.deleteDatabase("test-foundation.db")
        var db =
            Room.databaseBuilder(context, AppDatabase::class.java, "test-foundation.db").build()
        db.children().save(Child(id = "one", name = "Rowan", stage = "BABY"))
        db.children().save(Child(id = "two", name = "Ash", stage = "TEEN"))
        db.children().save(db.children().all().first { it.id == "one" }.copy(stage = "TODDLER"))
        db.close()
        db = Room.databaseBuilder(context, AppDatabase::class.java, "test-foundation.db").build()
        assertEquals(2, db.children().all().size)
        assertEquals("TODDLER", db.children().all().first { it.id == "one" }.stage)
        assertEquals("TEEN", db.children().all().first { it.id == "two" }.stage)
        db.close()
        context.deleteDatabase("test-foundation.db")
    }
}
