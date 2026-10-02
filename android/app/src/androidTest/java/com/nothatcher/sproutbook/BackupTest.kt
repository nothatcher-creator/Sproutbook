package com.nothatcher.sproutbook

import android.graphics.Bitmap
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nothatcher.sproutbook.data.*
import com.nothatcher.sproutbook.services.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class BackupTest {
    @Test
    fun exportReviewRestorePhotosAndInvalidRollback(): Unit = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val settings = Settings(context)
        val old = settings.flow.first().grandparent
        val repo = FamilyRepository(db, settings)
        val backup = BackupManager(context, repo)
        val file = java.io.File(context.cacheDir, "qa-backup.json")
        val photo = PhotoStore.file(context, "qa-source.jpg")
        try {
            settings.boolean("grandparent", false)
            db.children().save(Child(id = "a", name = "Rowan"))
            photo.parentFile!!.mkdirs()
            val bitmap = Bitmap.createBitmap(20, 20, Bitmap.Config.ARGB_8888)
            photo.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
            bitmap.recycle()
            repo.saveMemory(
                Memory(childId = "a", title = "First smile", occurredOn = 1, photo = photo.name)
            )
            repo.saveFeed(Feed(childId = "a", kind = "Bottle", startsAt = 1000, amountMl = 90.0))
            backup.export(Uri.fromFile(file))
            val reviewed = backup.read(Uri.fromFile(file))
            val invalid =
                reviewed.deepCopy().apply {
                    getAsJsonObject("tables")
                        .getAsJsonArray("children")[0]
                        .asJsonObject
                        .addProperty("stage", "INVALID")
                }
            try {
                backup.restore(invalid)
                fail("Invalid file accepted")
            } catch (expected: IllegalArgumentException) {}
            assertEquals(1, db.feeds().all().size)
            db.children().delete("a")
            assertTrue(backup.restore(reviewed))
            assertEquals(90.0, db.feeds().all().single().amountMl, 0.0)
            val restored = db.memorys().all().single()
            assertNotEquals(photo.name, restored.photo)
            assertTrue(PhotoStore.file(context, restored.photo!!).isFile)
            assertTrue(photo.isFile)
            assertFalse(settings.flow.first().notifications)
            settings.boolean("grandparent", true)
            try {
                backup.restore(reviewed)
                fail("Read-only import accepted")
            } catch (expected: IllegalStateException) {}
            assertEquals(1, db.children().all().size)
            PhotoStore.file(context, restored.photo!!).delete()
        } finally {
            settings.boolean("grandparent", old)
            file.delete()
            photo.delete()
            db.close()
        }
    }
}
