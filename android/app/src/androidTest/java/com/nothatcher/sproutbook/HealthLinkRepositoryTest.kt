package com.nothatcher.sproutbook

import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nothatcher.sproutbook.data.*
import com.nothatcher.sproutbook.services.BackupManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class HealthLinkRepositoryTest {
    @Test
    fun visitSearchFiltersBeforeLimitKeepsChildOwnershipAndTreatsWildcardsLiterally(): Unit = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            db.children().save(Child(id = "a", name = "Willow"))
            db.children().save(Child(id = "b", name = "Ash"))
            repeat(60) { index ->
                db.appointments().save(Appointment(id = "visit-$index", childId = "a",
                    title = "Recent check-up", startsAt = 1000L + index))
            }
            db.appointments().save(Appointment(id = "old", childId = "a", title = "50% follow_up",
                startsAt = 1, location = "North clinic"))
            db.appointments().save(Appointment(id = "foreign", childId = "b",
                title = "50% follow_up", startsAt = 2000, location = "North clinic"))
            assertEquals(51, db.appointments().search("a", "", 51).first().size)
            for (query in listOf("north", "%", "_", "FOLLOW_UP")) {
                assertEquals("old", db.appointments().search("a", query, 1).first().single().id)
            }
            assertNull(db.appointments().observeOne("a", "foreign").first())
            assertEquals("old", db.appointments().observeOne("a", "old").first()!!.id)
        } finally { db.close() }
    }

    @Test
    fun exactLinkAndInstantSurviveReopenStageChangeAndBackup(): Unit = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "qa-health-link-persistence.db"
        context.deleteDatabase(name)
        var db = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
        val settings = Settings(context)
        val originalMode = settings.flow.first().grandparent
        val file = java.io.File(context.cacheDir, "qa-health-link-backup.json")
        try {
            settings.boolean("grandparent", false)
            var repo = FamilyRepository(db, settings)
            repo.saveChild(Child(id = "a", name = "Willow", notes = "Keep earlier chapters"))
            repo.saveChild(Child(id = "b", name = "Ash"))
            repo.saveAppointment(Appointment(id = "morning", childId = "a", title = "Check-up", startsAt = 1000))
            repo.saveAppointment(Appointment(id = "afternoon", childId = "a", title = "Check-up", startsAt = 2000))
            repo.saveAppointment(Appointment(id = "other-visit", childId = "b", title = "Check-up", startsAt = 1000))
            val record = HealthRecord(id = "note", childId = "a", kind = "Doctor note", recordedAt = 1234,
                title = "Visit notes", notes = "Recorded instructions", appointmentId = "morning")
            repo.saveHealth(record)
            db.close()
            db = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
            repo = FamilyRepository(db, settings)
            assertEquals("morning", db.healthRecords().get("note")!!.appointmentId)
            repo.saveChild(db.children().all().first { it.id == "a" }.copy(stage = "TEEN"))
            assertEquals(1234, db.healthRecords().get("note")!!.recordedAt)
            assertEquals("Keep earlier chapters", db.children().all().first { it.id == "a" }.notes)
            assertTrue(runCatching { repo.saveHealth(record.copy(appointmentId = "other-visit")) }
                .exceptionOrNull() is IllegalArgumentException)
            assertEquals("morning", db.healthRecords().get("note")!!.appointmentId)
            val backup = BackupManager(context, repo)
            backup.export(Uri.fromFile(file))
            val reviewed = backup.read(Uri.fromFile(file))
            assertTrue(backup.restore(reviewed))
            assertEquals("morning", db.healthRecords().get("note")!!.appointmentId)
            assertEquals(1234, db.healthRecords().get("note")!!.recordedAt)
            assertEquals("TEEN", db.children().all().first { it.id == "a" }.stage)
            assertEquals(2, db.children().all().size)
            settings.boolean("grandparent", true)
            assertTrue(runCatching { repo.saveHealth(record.copy(appointmentId = null)) }
                .exceptionOrNull() is IllegalStateException)
            assertEquals("morning", db.healthRecords().get("note")!!.appointmentId)
        } finally {
            settings.boolean("grandparent", originalMode)
            db.close(); context.deleteDatabase(name); file.delete()
        }
    }
}
