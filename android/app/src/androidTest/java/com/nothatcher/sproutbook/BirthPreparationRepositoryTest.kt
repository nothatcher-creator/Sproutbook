package com.nothatcher.sproutbook

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nothatcher.sproutbook.core.BackupFormat
import com.nothatcher.sproutbook.data.*
import com.nothatcher.sproutbook.services.BackupManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class BirthPreparationRepositoryTest {
    private fun template(guide: String = "home", key: String = "care-team") = PrepItem(
        id = "birth-v1-${"a".repeat(64)}-$guide-$key",
        childId = "a",
        kind = "Bag",
        title = "Discuss the care plan",
        notes = "Questions to bring to the next appointment",
    )

    private suspend fun withRepository(block: suspend (AppDatabase, Settings, FamilyRepository) -> Unit) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val settings = Settings(context)
        val original = settings.flow.first()
        try {
            settings.boolean("grandparent", false)
            db.children().save(Child(id = "a", name = "Willow", stage = "PREGNANCY"))
            db.children().save(Child(id = "b", name = "Ash"))
            block(db, settings, FamilyRepository(db, settings))
        } finally {
            settings.boolean("grandparent", original.grandparent)
            settings.select(original.selected)
            db.close()
        }
    }

    @Test
    fun saveRejectsAnExistingItemOwnedByAnotherChild(): Unit = runBlocking {
        withRepository { db, _, repo ->
            val original = template()
            repo.savePrep(original)
            val saved = db.prepItems().get(original.id)!!

            val failure = runCatching {
                repo.savePrep(original.copy(childId = "b", title = "Wrong child's draft"))
            }.exceptionOrNull()

            assertTrue("A preparation item cannot move to another child by overwriting its ID", failure is IllegalArgumentException)
            assertEquals(saved, db.prepItems().get(original.id))
            assertTrue(db.prepItems().forChild("b").isEmpty())
        }
    }

    @Test
    fun toggleRejectsMalformedUnsavedTemplates(): Unit = runBlocking {
        withRepository { db, _, repo ->
            val valid = template()
            val malformed = listOf(
                valid.copy(title = "   "),
                valid.copy(title = "x".repeat(161)),
                valid.copy(kind = "Unknown"),
                valid.copy(notes = "x".repeat(10001)),
                valid.copy(id = ""),
                valid.copy(id = "x".repeat(161)),
            )
            malformed.forEachIndexed { index, item ->
                assertTrue("Invalid fallback template $index must not be persisted",
                    runCatching { repo.togglePrep(item) }.exceptionOrNull() is IllegalArgumentException)
                assertTrue(db.prepItems().all().isEmpty())
            }
        }
    }

    @Test
    fun saveRejectsIdsThatCannotBeRestoredFromBackup(): Unit = runBlocking {
        withRepository { db, _, repo ->
            for (id in listOf("", "x".repeat(161))) {
                assertTrue("Preparation IDs must fit the backup transport limits",
                    runCatching { repo.savePrep(template().copy(id = id)) }.exceptionOrNull() is IllegalArgumentException)
                assertTrue(db.prepItems().all().isEmpty())
            }
        }
    }

    @Test
    fun togglingAnOldTemplatePreservesSavedEditsAndUsesCurrentCompletion(): Unit = runBlocking {
        withRepository { db, _, repo ->
            val original = template()
            repo.savePrep(original.copy(title = "Our updated care plan", notes = "Keep our chosen contacts"))

            repo.togglePrep(original)
            val completed = db.prepItems().get(original.id)!!
            assertEquals("Our updated care plan", completed.title)
            assertEquals("Keep our chosen contacts", completed.notes)
            assertTrue(completed.completed)

            repo.togglePrep(original)
            val reopened = db.prepItems().get(original.id)!!
            assertEquals(completed.title, reopened.title)
            assertEquals(completed.notes, reopened.notes)
            assertFalse(reopened.completed)
            assertEquals(1, db.prepItems().all().size)
        }
    }

    @Test
    fun caregiverModeBlocksSavingAndTogglingPreparation(): Unit = runBlocking {
        withRepository { db, settings, repo ->
            val original = template()
            repo.savePrep(original)
            val saved = db.prepItems().get(original.id)!!
            val unsaved = template("free", "care-contacts")
            settings.boolean("grandparent", true)

            assertTrue(runCatching { repo.savePrep(saved.copy(notes = "Blocked edit")) }
                .exceptionOrNull() is IllegalStateException)
            assertTrue(runCatching { repo.savePrep(unsaved) }.exceptionOrNull() is IllegalStateException)
            assertTrue(runCatching { repo.togglePrep(saved) }.exceptionOrNull() is IllegalStateException)
            assertTrue(runCatching { repo.togglePrep(unsaved) }.exceptionOrNull() is IllegalStateException)
            assertEquals(saved, db.prepItems().get(original.id))
            assertNull(db.prepItems().get(unsaved.id))
        }
    }

    @Test
    fun editedChecklistsSurviveReopenChildAndStageChangesAndBackup(): Unit = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "qa-birth-preparation-persistence.db"
        context.deleteDatabase(name)
        var db = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
        val settings = Settings(context)
        val original = settings.flow.first()
        try {
            settings.boolean("grandparent", false)
            var repo = FamilyRepository(db, settings)
            repo.saveChild(Child(id = "a", name = "Willow", stage = "PREGNANCY"))
            repo.saveChild(Child(id = "b", name = "Ash"))
            val templates = listOf("home", "free", "transfer", "after").map { template(it) }
            templates.forEach { item ->
                repo.savePrep(item.copy(title = "Our ${item.id.substringAfterLast('-')} plan", notes = "Our saved questions"))
                repo.togglePrep(item)
            }
            val custom = template(key = "custom-00000000-0000-0000-0000-000000000001")
                .copy(title = "Arrange pet care", notes = "A neighbour will help")
            val oldBag = PrepItem(id = "a-bag-0", childId = "a", kind = "Bag", title = "Care records")
            val oldNote = PrepItem(id = "legacy-note", childId = "a", kind = "Note", title = "Birth preferences", notes = "Keep these")
            repo.savePrep(custom)
            repo.savePrep(oldBag)
            repo.savePrep(oldNote)
            val expected = db.prepItems().forChild("a").associateBy { it.id }

            db.close()
            db = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
            repo = FamilyRepository(db, settings)
            assertEquals(expected, db.prepItems().forChild("a").associateBy { it.id })
            assertEquals(AppDatabase.VERSION, db.openHelper.readableDatabase.version)

            repo.saveChild(db.children().all().first { it.id == "a" }.copy(stage = "BABY"))
            settings.select("b")
            assertTrue(db.prepItems().observe("b").first().isEmpty())
            settings.select("a")
            assertEquals(expected, db.prepItems().observe("a").first().associateBy { it.id })

            val backup = BackupManager(context, repo)
            val reviewed = BackupFormat.validate(backup.snapshot().toString())
            db.children().all().forEach { db.children().delete(it.id) }
            assertTrue(db.prepItems().all().isEmpty())
            assertTrue(backup.restore(reviewed))
            assertEquals(expected, db.prepItems().forChild("a").associateBy { it.id })
            assertTrue(db.prepItems().forChild("b").isEmpty())
            assertEquals("BABY", db.children().all().first { it.id == "a" }.stage)
            assertEquals("a", settings.flow.first().selected)
        } finally {
            settings.boolean("grandparent", original.grandparent)
            settings.select(original.selected)
            db.close()
            context.deleteDatabase(name)
        }
    }
}
