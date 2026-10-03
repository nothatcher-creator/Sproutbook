package com.nothatcher.sproutbook

import android.content.Context
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nothatcher.sproutbook.core.HomeCustomization
import com.nothatcher.sproutbook.data.*
import com.nothatcher.sproutbook.services.BackupManager
import com.nothatcher.sproutbook.services.PhotoStore
import java.io.File
import java.io.FileNotFoundException
import java.util.Base64
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.*
import org.junit.Test

class HomeOrganizerRepositoryTest {
    @Test
    fun changingOneHomeKeepsOtherChildrenSelectionAndRecords(): Unit = runBlocking {
        withFamily { _, repo ->
            val other = repo.db.children().get("b")!!
            val wanted = customizedHome()
            repo.saveHomeCustomization("a", wanted)

            val home = repo.db.children().get("a")!!.homeCustomization()
            assertEquals(listOf("memory_tree", "chapter"), home.sectionOrder.take(2))
            assertEquals(setOf("growth"), home.hiddenSections)
            assertEquals(listOf("schedule", "memory"), home.quickActions)
            assertEquals("meadow", home.background)
            assertEquals(other, repo.db.children().get("b"))
            assertEquals("b", repo.settings.flow.first().selected)
            assertEquals("Caregiver notes", repo.db.children().get("a")!!.notes)
            assertEquals(90.125, repo.db.feeds().get("kept-feed")!!.amountMl, 0.0)
            assertEquals("Kept memory", repo.db.memorys().get("kept-memory")!!.title)

            rejected { repo.saveHomeCustomization("missing", wanted) }
            assertEquals(2, repo.db.children().all().size)
            assertEquals(home, repo.db.children().get("a")!!.homeCustomization())
        }
    }

    @Test
    fun savingAnOlderProfileDoesNotUndoNewHomeSettings(): Unit = runBlocking {
        withFamily { _, repo ->
            val staleProfile = repo.db.children().get("a")!!
            repo.saveHomeCustomization("a", customizedHome())
            val savedHome = repo.db.children().get("a")!!.homeCustomization()
            repo.saveChild(staleProfile.copy(name = "Willow Renamed", notes = "Updated notes"))

            assertEquals("Willow Renamed", repo.db.children().get("a")!!.name)
            assertEquals("Updated notes", repo.db.children().get("a")!!.notes)
            assertEquals(savedHome, repo.db.children().get("a")!!.homeCustomization())
            assertEquals(90.125, repo.db.feeds().get("kept-feed")!!.amountMl, 0.0)
        }
    }

    @Test
    fun emptyButtonsPersistAndInvalidOrCaregiverWritesKeepExistingHome(): Unit = runBlocking {
        withFamily { context, repo ->
            repo.saveHomeCustomization("a", customizedHome().copy(quickActions = emptyList()))
            val before = repo.db.children().get("a")!!
            assertTrue(before.homeCustomization().quickActions.isEmpty())
            rejected {
                repo.saveHomeCustomization(
                    "a",
                    customizedHome().copy(quickActions = listOf("memory", "memory")),
                )
            }
            assertEquals(before, repo.db.children().get("a"))

            val photoNames = File(context.filesDir, "photos").list()?.toSet().orEmpty()
            repo.settings.boolean("grandparent", true)
            readOnly { repo.saveHomeCustomization("a", HomeCustomization()) }
            readOnly {
                repo.importHomeBackground(context, "a", Uri.fromFile(File(context.cacheDir, "absent.jpg")))
            }
            assertEquals(before, repo.db.children().get("a"))
            assertEquals(photoNames, File(context.filesDir, "photos").list()?.toSet().orEmpty())
            assertEquals("b", repo.settings.flow.first().selected)
        }
    }

    @Test
    fun backgroundCopyIsPrivateChildScopedAndOldImagesRemainAfterReplacement(): Unit = runBlocking {
        withFamily { context, repo ->
            val source = createImage(context)
            val created = mutableListOf<File>()
            try {
                repo.saveHomeCustomization("a", customizedHome())
                val beforeImport = repo.db.children().get("a")!!
                val first = repo.importHomeBackground(context, "a", Uri.fromFile(source))
                created += PhotoStore.file(context, first)
                assertNotEquals(source.name, first)
                assertTrue(created.single().isFile)
                assertEquals(beforeImport, repo.db.children().get("a"))
                repo.saveHomeCustomization(
                    "a", beforeImport.homeCustomization().copy(background = "photo", backgroundPhoto = first),
                )
                assertEquals("photo", repo.db.children().get("a")!!.homeBackground)
                assertEquals(first, repo.db.children().get("a")!!.homeBackgroundPhoto)
                assertEquals(listOf("schedule", "memory"), repo.db.children().get("a")!!.homeCustomization().quickActions)
                assertNull(repo.db.children().get("b")!!.homeBackgroundPhoto)
                assertEquals("b", repo.settings.flow.first().selected)

                val second = repo.importHomeBackground(context, "a", Uri.fromFile(source))
                created += PhotoStore.file(context, second)
                assertNotEquals(first, second)
                assertEquals(first, repo.db.children().get("a")!!.homeBackgroundPhoto)
                repo.saveHomeCustomization(
                    "a", repo.db.children().get("a")!!.homeCustomization().copy(backgroundPhoto = second),
                )
                assertTrue(created.all { it.isFile })
                repo.saveHomeCustomization("a", HomeCustomization())
                assertNull(repo.db.children().get("a")!!.homeBackgroundPhoto)
                assertTrue(created.all { it.isFile })
                assertTrue(source.isFile)
            } finally {
                source.delete()
                created.forEach { it.delete() }
            }
        }
    }

    @Test
    fun backupIncludesAndRemapsBackgroundWithoutChangingOtherChildSettings(): Unit = runBlocking {
        withFamily { context, repo ->
            val source = createImage(context)
            val created = mutableListOf<File>()
            try {
                repo.saveHomeCustomization("a", customizedHome().copy(quickActions = emptyList()))
                repo.saveHomeCustomization("b", HomeCustomization(background = "evening"))
                val originalName = repo.importHomeBackground(context, "a", Uri.fromFile(source))
                val original = PhotoStore.file(context, originalName)
                created += original
                repo.saveHomeCustomization(
                    "a", repo.db.children().get("a")!!.homeCustomization().copy(background = "photo", backgroundPhoto = originalName),
                )
                val bytes = original.readBytes()
                val expectedA = repo.db.children().get("a")!!.homeCustomization()
                val expectedB = repo.db.children().get("b")!!.homeCustomization()
                val backup = BackupManager(context, repo)
                val snapshot = backup.snapshot()
                assertArrayEquals(bytes, Base64.getDecoder().decode(snapshot.getAsJsonObject("photos")[originalName].asString))

                val invalid = snapshot.deepCopy().apply { getAsJsonObject("photos").remove(originalName) }
                rejected { backup.restore(invalid) }
                assertEquals(expectedA, repo.db.children().get("a")!!.homeCustomization())
                assertEquals(expectedB, repo.db.children().get("b")!!.homeCustomization())
                assertEquals(90.125, repo.db.feeds().get("kept-feed")!!.amountMl, 0.0)

                assertTrue(backup.restore(snapshot))
                val restored = repo.db.children().get("a")!!.homeCustomization()
                assertNotEquals(originalName, restored.backgroundPhoto)
                val restoredFile = PhotoStore.file(context, restored.backgroundPhoto!!)
                created += restoredFile
                assertArrayEquals(bytes, restoredFile.readBytes())
                assertEquals(expectedA.copy(backgroundPhoto = restored.backgroundPhoto), restored)
                assertEquals(expectedB, repo.db.children().get("b")!!.homeCustomization())
                assertTrue(original.isFile)
                assertEquals("b", repo.settings.flow.first().selected)
                assertEquals("Kept memory", repo.db.memorys().get("kept-memory")!!.title)
            } finally {
                source.delete()
                created.forEach { it.delete() }
            }
        }
    }

    @Test
    fun caregiverEnabledWhilePhotoReadWaitsRejectsImportAndRemovesOnlyNewImage(): Unit = runBlocking {
        withFamily { context, repo ->
            val source = createImage(context)
            var oldPhoto: File? = null
            val photos = File(context.filesDir, "photos")
            var beforeNames = emptySet<String>()
            val token = UUID.randomUUID().toString()
            val uri = Uri.parse("content://${HomeBackgroundTestProvider.AUTHORITY}/$token")
            val resolver = context.contentResolver
            try {
                val oldName = repo.importHomeBackground(context, "a", Uri.fromFile(source))
                val savedPhoto = PhotoStore.file(context, oldName)
                oldPhoto = savedPhoto
                val oldBytes = savedPhoto.readBytes()
                repo.saveHomeCustomization(
                    "a", customizedHome().copy(background = "photo", backgroundPhoto = oldName),
                )
                val beforeChild = repo.db.children().get("a")!!
                val otherChild = repo.db.children().get("b")!!
                val beforeFeed = repo.db.feeds().get("kept-feed")!!
                val beforeMemory = repo.db.memorys().get("kept-memory")!!
                beforeNames = photos.list()?.toSet().orEmpty()
                resolver.call(uri, "arm", token, Bundle().apply { putByteArray("image", source.readBytes()) })

                coroutineScope {
                    val pending = async(Dispatchers.IO) {
                        runCatching { repo.importHomeBackground(context, "a", uri) }
                    }
                    try {
                        val waiting = withContext(Dispatchers.IO) {
                            resolver.call(uri, "await_read", token, null)?.getBoolean("waiting") ?: false
                        }
                        assertTrue("Import must pass the first guard and request the blocked image stream", waiting)
                        assertFalse("Photo import must still be waiting for image bytes", pending.isCompleted)
                        repo.settings.boolean("grandparent", true)
                        resolver.call(uri, "release", token, null)

                        val outcome = withTimeout(10_000) { pending.await() }
                        val error = outcome.exceptionOrNull()
                        assertTrue("A completed copy must fail the second caregiver check", error is IllegalStateException)
                        assertTrue(error!!.message.orEmpty().contains("Grandparent mode is read-only"))
                        assertEquals(beforeNames, photos.list()?.toSet().orEmpty())
                        assertEquals(beforeChild, repo.db.children().get("a"))
                        assertEquals(otherChild, repo.db.children().get("b"))
                        assertEquals(beforeFeed, repo.db.feeds().get("kept-feed"))
                        assertEquals(beforeMemory, repo.db.memorys().get("kept-memory"))
                        assertArrayEquals(oldBytes, savedPhoto.readBytes())
                        assertEquals("b", repo.settings.flow.first().selected)
                    } finally {
                        resolver.call(uri, "release", token, null)
                        withTimeoutOrNull(10_000) { pending.await() }
                        pending.cancel()
                    }
                }
            } finally {
                resolver.call(uri, "clear", token, null)
                // Keep the shared test photo store clean even if a regression leaves a new file.
                if (beforeNames.isNotEmpty()) {
                    photos.list()?.filterNot { it in beforeNames }?.forEach { PhotoStore.file(context, it).delete() }
                }
                oldPhoto?.delete()
                source.delete()
            }
        }
    }

    @Test
    fun organizerSettingsSurviveDatabaseCloseAndReopen(): Unit = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "home-organizer-${UUID.randomUUID()}.db"
        val settings = Settings(context)
        val before = settings.flow.first()
        var db = Room.databaseBuilder(context, AppDatabase::class.java, name)
            .addMigrations(MIGRATION_11_12, MIGRATION_12_13).build()
        try {
            settings.boolean("grandparent", false)
            db.children().save(Child(id = "a", name = "Willow"))
            FamilyRepository(db, settings).saveHomeCustomization("a", customizedHome())
            val wanted = db.children().get("a")!!.homeCustomization()
            db.close()
            db = Room.databaseBuilder(context, AppDatabase::class.java, name)
                .addMigrations(MIGRATION_11_12, MIGRATION_12_13).build()
            assertEquals(wanted, db.children().get("a")!!.homeCustomization())
            assertEquals(AppDatabase.VERSION, db.openHelper.readableDatabase.version)
        } finally {
            settings.boolean("grandparent", before.grandparent)
            settings.select(before.selected)
            db.close()
            context.deleteDatabase(name)
        }
    }

    private fun customizedHome() = HomeCustomization(
        sectionOrder = listOf("memory_tree", "chapter") +
            HomeCustomization().sectionOrder.filterNot { it in setOf("memory_tree", "chapter") },
        hiddenSections = setOf("growth"),
        quickActions = listOf("schedule", "memory"),
        background = "meadow",
    )

    private fun createImage(context: Context): File {
        val file = File(context.cacheDir, "home-source-${UUID.randomUUID()}.png")
        val bitmap = Bitmap.createBitmap(32, 24, Bitmap.Config.ARGB_8888)
        try {
            bitmap.eraseColor(android.graphics.Color.GREEN)
            file.outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
        } finally {
            bitmap.recycle()
        }
        return file
    }

    private suspend fun withFamily(block: suspend (Context, FamilyRepository) -> Unit) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val settings = Settings(context)
        val before = settings.flow.first()
        try {
            settings.boolean("grandparent", false)
            db.children().save(Child(id = "a", name = "Willow", notes = "Caregiver notes"))
            db.children().save(Child(id = "b", name = "Ash"))
            settings.select("b")
            val repo = FamilyRepository(db, settings)
            repo.saveFeed(Feed(id = "kept-feed", childId = "a", kind = "Bottle", startsAt = 1000, amountMl = 90.125))
            repo.saveMemory(Memory(id = "kept-memory", childId = "a", title = "Kept memory", occurredOn = 1))
            block(context, repo)
        } finally {
            settings.boolean("grandparent", before.grandparent)
            settings.boolean("notifications", before.notifications)
            settings.select(before.selected)
            db.close()
        }
    }

    private suspend fun rejected(block: suspend () -> Unit) {
        try {
            block()
            fail("Invalid organizer change accepted")
        } catch (expected: IllegalArgumentException) {}
    }

    private suspend fun readOnly(block: suspend () -> Unit) {
        try {
            block()
            fail("Read-only organizer change accepted")
        } catch (expected: IllegalStateException) {}
    }
}
