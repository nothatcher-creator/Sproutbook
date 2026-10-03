package com.nothatcher.sproutbook

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nothatcher.sproutbook.data.*
import com.nothatcher.sproutbook.services.BackupManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class WishlistRepositoryTest {
    @Test
    fun savesCustomizationAndWishlistAcrossReopenAndBackup(): Unit = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "wishlist-persistence-test.db"
        context.deleteDatabase(name)
        val settings = Settings(context)
        val original = settings.flow.first()
        var db = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
        try {
            settings.boolean("grandparent", false)
            var repo = FamilyRepository(db, settings)
            repo.saveChild(Child(id = "a", name = "Willow", accent = "Amber", treeStyle = "Autumn", notes = "Family notes"))
            repo.saveChild(Child(id = "b", name = "Ash", accent = "Sky", treeStyle = "Night"))
            repo.saveWishlist(WishlistItem(id = "a-idea", childId = "a", title = "  Wooden blocks  ", notes = "For winter", link = "https://example.com/blocks"))
            repo.saveWishlist(WishlistItem(id = "b-idea", childId = "b", title = "Story book"))
            repo.setWishlistObtained("a-idea", "a", true)
            repo.saveInventory(Inventory(id = "stock", childId = "a", name = "Wipes", quantity = 3.0))
            repo.saveShopping(ShoppingItem(id = "shopping", childId = "a", name = "Wipes"))
            db.close()
            db = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
            repo = FamilyRepository(db, settings)
            assertEquals("Amber", db.children().all().single { it.id == "a" }.accent)
            assertEquals("Autumn", db.children().all().single { it.id == "a" }.treeStyle)
            assertEquals("Sky", db.children().all().single { it.id == "b" }.accent)
            assertEquals("Wooden blocks", db.wishlistItems().get("a-idea")!!.title)
            assertTrue(db.wishlistItems().get("a-idea")!!.obtained)
            val backup = BackupManager(context, repo)
            val snapshot = backup.snapshot()
            repo.deleteChild("a")
            assertNull(db.wishlistItems().get("a-idea"))
            assertTrue(backup.restore(snapshot))
            assertEquals("Family notes", db.children().all().single { it.id == "a" }.notes)
            assertEquals("Autumn", db.children().all().single { it.id == "a" }.treeStyle)
            assertEquals("Night", db.children().all().single { it.id == "b" }.treeStyle)
            assertEquals("https://example.com/blocks", db.wishlistItems().get("a-idea")!!.link)
            assertTrue(db.wishlistItems().get("a-idea")!!.obtained)
            assertEquals(listOf("b-idea"), repo.observeWishlist("b").first().map { it.id })
            assertEquals(3.0, db.inventorys().get("stock")!!.quantity, 0.0)
            assertFalse(db.shoppingItems().get("shopping")!!.checked)
        } finally {
            settings.boolean("grandparent", original.grandparent)
            settings.boolean("notifications", original.notifications)
            settings.select(original.selected)
            db.close()
            context.deleteDatabase(name)
        }
    }

    @Test
    fun ownershipAndCaregiverGuardsProtectEveryWishlistMutation(): Unit = runBlocking {
        withFamily { repo ->
            val item = WishlistItem(id = "idea", childId = "a", title = "Balance bike")
            repo.saveWishlist(item)
            rejected { repo.saveWishlist(item.copy(childId = "b", title = "Wrong child")) }
            rejected { repo.setWishlistObtained("idea", "b", true) }
            rejected { repo.deleteWishlist("idea", "b") }
            rejected { repo.saveWishlist(item.copy(id = "orphan", childId = "missing")) }
            assertFalse(repo.db.wishlistItems().get("idea")!!.obtained)
            assertTrue(repo.db.wishlistItems().forChild("b").isEmpty())
            repo.settings.boolean("grandparent", true)
            readOnly { repo.saveWishlist(item.copy(title = "Read only")) }
            readOnly { repo.setWishlistObtained("idea", "a", true) }
            readOnly { repo.deleteWishlist("idea", "a") }
            readOnly { repo.saveChild(Child(id = "a", name = "Willow", accent = "Moss")) }
            assertEquals("Balance bike", repo.db.wishlistItems().get("idea")!!.title)
            assertFalse(repo.db.wishlistItems().get("idea")!!.obtained)
            repo.settings.boolean("grandparent", false)
            repo.deleteWishlist("idea", "a")
            assertNull(repo.db.wishlistItems().get("idea"))
        }
    }

    @Test
    fun validatesProfileChoicesAndWishlistTextAndLinksBeforeWriting(): Unit = runBlocking {
        withFamily { repo ->
            for (accent in listOf("Forest", "Moss", "Amber", "Sky"))
                repo.saveChild(Child(id = "a", name = "Willow", accent = accent))
            for (treeStyle in listOf("Summer", "Autumn", "Night"))
                repo.saveChild(Child(id = "a", name = "Willow", treeStyle = treeStyle))
            val before = repo.db.children().all().single { it.id == "a" }
            rejected { repo.saveChild(before.copy(accent = "Unknown")) }
            rejected { repo.saveChild(before.copy(treeStyle = "Unknown")) }
            assertEquals(before, repo.db.children().all().single { it.id == "a" })
            val item = WishlistItem(id = "idea", childId = "a", title = "A book")
            for (link in listOf("", "http://example.com/book", "https://example.com/book?edition=2#cover")) {
                repo.saveWishlist(item.copy(link = link))
                assertEquals(link, repo.db.wishlistItems().get("idea")!!.link)
            }
            for (link in listOf("javascript:alert(1)", "file:///book", "https:///missing-host", "https://user:secret@example.com/book", "https://example.com/a b", "https://example.com:99999/book", "https://example.com/" + "x".repeat(2048)))
                rejected { repo.saveWishlist(item.copy(link = link)) }
            rejected { repo.saveWishlist(item.copy(title = " ")) }
            rejected { repo.saveWishlist(item.copy(title = "x".repeat(161))) }
            rejected { repo.saveWishlist(item.copy(notes = "x".repeat(10001))) }
            assertEquals("https://example.com/book?edition=2#cover", repo.db.wishlistItems().get("idea")!!.link)
        }
    }

    @Test
    fun filtersBeforePaginationAndCountsAllRecordsForTheSelectedChild(): Unit = runBlocking {
        withFamily { repo ->
            repo.db.wishlistItems().save(WishlistItem(id = "old", childId = "a", title = "Older idea", updatedAt = 1))
            repo.db.wishlistItems().save(WishlistItem(id = "new", childId = "a", title = "Newer idea", obtained = true, updatedAt = 2))
            repo.db.wishlistItems().save(WishlistItem(id = "other", childId = "b", title = "Other child", obtained = true, updatedAt = 3))
            assertEquals(listOf("old"), repo.observeWishlist("a", 1, "Wanted").first().map { it.id })
            assertEquals(listOf("new"), repo.observeWishlist("a", 1, "Obtained").first().map { it.id })
            assertEquals(2, repo.db.wishlistItems().count("a").first())
            assertEquals(2, repo.observeWishlist("a", 50002).first().size)
            assertEquals(1, repo.db.wishlistItems().obtainedCount("a").first())
            repo.setWishlistObtained("old", "a", true)
            assertTrue(repo.observeWishlist("a", 1, "Wanted").first().isEmpty())
            assertEquals(2, repo.db.wishlistItems().obtainedCount("a").first())
        }
    }

    @Test
    fun oldBackupsDefaultCustomizationAndClearWishlistWithoutLosingOtherRecords(): Unit = runBlocking {
        withFamily { repo ->
            repo.saveWishlist(WishlistItem(childId = "a", title = "Current idea"))
            repo.saveFeed(Feed(id = "feed", childId = "a", kind = "Bottle", startsAt = 1000, amountMl = 90.0))
            val backup = BackupManager(ApplicationProvider.getApplicationContext(), repo)
            val old = backup.snapshot().apply {
                addProperty("version", 3)
                getAsJsonObject("tables").remove("wishlistItems")
                getAsJsonObject("tables").getAsJsonArray("children").forEach {
                    it.asJsonObject.remove("accent")
                    it.asJsonObject.remove("treeStyle")
                }
            }
            assertTrue(backup.restore(old))
            assertEquals("Forest", repo.db.children().all().single { it.id == "a" }.accent)
            assertEquals("Summer", repo.db.children().all().single { it.id == "a" }.treeStyle)
            assertTrue(repo.db.wishlistItems().all().isEmpty())
            assertEquals(90.0, repo.db.feeds().get("feed")!!.amountMl, 0.0)
        }
    }

    @Test
    fun invalidWishlistBackupLeavesTheCurrentFamilyIntact(): Unit = runBlocking {
        withFamily { repo ->
            repo.saveChild(Child(id = "a", name = "Willow", accent = "Moss", treeStyle = "Night"))
            repo.saveWishlist(WishlistItem(id = "idea", childId = "a", title = "Original idea"))
            val backup = BackupManager(ApplicationProvider.getApplicationContext(), repo)
            val snapshot = backup.snapshot()
            val invalidOwner = snapshot.deepCopy().apply {
                getAsJsonObject("tables").getAsJsonArray("wishlistItems")[0].asJsonObject
                    .addProperty("childId", "missing")
            }
            val missingWishlist = snapshot.deepCopy().apply {
                getAsJsonObject("tables").remove("wishlistItems")
            }
            val missingCustomization = snapshot.deepCopy().apply {
                getAsJsonObject("tables").getAsJsonArray("children")[0].asJsonObject.remove("accent")
            }
            for (invalid in listOf(invalidOwner, missingWishlist, missingCustomization)) {
                rejected { backup.restore(invalid) }
                assertEquals("Moss", repo.db.children().get("a")!!.accent)
                assertEquals("Night", repo.db.children().get("a")!!.treeStyle)
                assertEquals(2, repo.db.children().all().size)
                assertEquals("Original idea", repo.db.wishlistItems().get("idea")!!.title)
                assertEquals("a", repo.db.wishlistItems().get("idea")!!.childId)
            }
        }
    }

    private suspend fun withFamily(block: suspend (FamilyRepository) -> Unit) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val settings = Settings(context)
        val original = settings.flow.first()
        try {
            settings.boolean("grandparent", false)
            db.children().save(Child(id = "a", name = "Willow"))
            db.children().save(Child(id = "b", name = "Ash"))
            block(FamilyRepository(db, settings))
        } finally {
            settings.boolean("grandparent", original.grandparent)
            settings.boolean("notifications", original.notifications)
            settings.select(original.selected)
            db.close()
        }
    }

    private suspend fun rejected(block: suspend () -> Unit) {
        try { block(); fail("Invalid write accepted") } catch (_: IllegalArgumentException) {}
    }

    private suspend fun readOnly(block: suspend () -> Unit) {
        try { block(); fail("Read-only write accepted") } catch (_: IllegalStateException) {}
    }
}
