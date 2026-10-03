package com.nothatcher.sproutbook.core

import com.google.gson.*
import org.junit.Assert.*
import org.junit.Test

class WishlistBackupTest {
    private fun base(version: Int = BackupFormat.VERSION): JsonObject =
        JsonObject().apply {
            addProperty("format", "sproutbook")
            addProperty("version", version)
            add("settings", JsonObject())
            add("photos", JsonObject())
            add(
                "tables",
                JsonObject().apply {
                    (BackupFormat.tables + "wishlistItems").distinct().forEach {
                        add(it, JsonArray())
                    }
                    if (version == 1) {
                        listOf("diapers", "bottlePreps", "shoppingItems").forEach { remove(it) }
                    }
                    if (version < 3) {
                        listOf("routines", "routineCompletions", "pottyLogs", "milkContainers")
                            .forEach { remove(it) }
                    }
                    val child =
                        JsonParser.parseString("""{"id":"a","name":"Willow"}""")
                            .asJsonObject
                    if (version >= 4) {
                        child.addProperty("accent", "Forest")
                        child.addProperty("treeStyle", "Summer")
                    }
                    if (version >= 5) {
                        child.addProperty("homeSections", "")
                        child.addProperty("homeHiddenSections", "")
                        child.addProperty("homeQuickActions", "memory,schedule")
                        child.addProperty("homeBackground", "woodland")
                        child.add("homeBackgroundPhoto", JsonNull.INSTANCE)
                    }
                    getAsJsonArray("children").add(child)
                },
            )
        }

    private fun JsonObject.tables() = getAsJsonObject("tables")

    private fun JsonObject.child() = tables().getAsJsonArray("children")[0].asJsonObject

    private fun JsonObject.wishlist() = tables().getAsJsonArray("wishlistItems")

    private fun item(): JsonObject =
        JsonParser.parseString("""{"id":"w","childId":"a","title":"Balance bike"}""")
            .asJsonObject

    private fun withItem(): JsonObject = base().apply { wishlist().add(item()) }

    private fun rejected(root: JsonObject) {
        try {
            BackupFormat.validate(root.toString())
            fail("Invalid backup accepted")
        } catch (_: IllegalArgumentException) {}
    }

    private fun invalidItem(change: (JsonObject) -> Unit) {
        val root = withItem()
        // A valid control prevents an unknown-table error from satisfying rejection tests.
        BackupFormat.validate(root.toString())
        change(root.wishlist()[0].asJsonObject)
        rejected(root)
    }

    private fun invalidChild(change: (JsonObject) -> Unit) {
        val root = base()
        BackupFormat.validate(root.toString())
        change(root.child())
        rejected(root)
    }

    @Test
    fun versions1Through3RepairMissingWishlistAndChildCustomization() {
        for (version in 1..3) {
            val legacy = base(version).apply { tables().remove("wishlistItems") }
            val restored = BackupFormat.validate(legacy.toString())

            assertEquals(BackupFormat.VERSION, restored["version"].asInt)
            assertEquals("a", restored.child()["id"].asString)
            assertEquals("Willow", restored.child()["name"].asString)
            assertEquals("Forest", restored.child()["accent"].asString)
            assertEquals("Summer", restored.child()["treeStyle"].asString)
            assertEquals(0, restored.wishlist().size())
            assertEquals(restored, BackupFormat.validate(restored.toString()))
        }
    }

    @Test
    fun version4PreservesWishlistAndCustomizationThroughRoundTrip() {
        val root = base(4)
        root.child().apply {
            addProperty("accent", "Sky")
            addProperty("treeStyle", "Night")
        }
        root.tables()
            .getAsJsonArray("children")
            .add(
                JsonParser.parseString(
                    """{"id":"b","name":"Rowan","accent":"Amber","treeStyle":"Autumn"}"""
                )
            )
        val wanted = item().apply {
            addProperty("notes", "A small frame with adjustable handlebars")
            addProperty("link", "https://shop.example.com/bike?color=green#details")
            addProperty("obtained", false)
            addProperty("updatedAt", 1234L)
        }
        val obtained = item().apply {
            addProperty("id", "w2")
            addProperty("childId", "b")
            addProperty("title", "Picture book")
            addProperty("notes", "Birthday gift")
            addProperty("link", "http://books.example.org:8080/book")
            addProperty("obtained", true)
            addProperty("updatedAt", 5678L)
        }
        root.wishlist().apply {
            add(wanted)
            add(obtained)
        }

        val first = BackupFormat.validate(root.toString())
        val restored = BackupFormat.validate(first.toString())

        assertEquals(BackupFormat.VERSION, restored["version"].asInt)
        assertEquals(first, restored)
        assertEquals("Sky", restored.child()["accent"].asString)
        assertEquals("Night", restored.child()["treeStyle"].asString)
        val secondChild = restored.tables().getAsJsonArray("children")[1].asJsonObject
        assertEquals("Amber", secondChild["accent"].asString)
        assertEquals("Autumn", secondChild["treeStyle"].asString)
        assertEquals(wanted, restored.wishlist()[0])
        assertEquals(obtained, restored.wishlist()[1])
    }

    @Test
    fun version4RequiresWishlistTable() {
        val root = base(4)
        BackupFormat.validate(root.toString())
        root.tables().remove("wishlistItems")
        rejected(root)
    }

    @Test
    fun version4RequiresChildAccent() {
        val root = base(4)
        BackupFormat.validate(root.toString())
        root.child().remove("accent")
        rejected(root)
    }

    @Test
    fun version4RequiresChildTreeStyle() {
        val root = base(4)
        BackupFormat.validate(root.toString())
        root.child().remove("treeStyle")
        rejected(root)
    }

    @Test
    fun allSupportedChildCustomizationValuesArePreserved() {
        for (accent in listOf("Forest", "Moss", "Amber", "Sky")) {
            for (treeStyle in listOf("Summer", "Autumn", "Night")) {
                val root = base()
                root.child().apply {
                    addProperty("accent", accent)
                    addProperty("treeStyle", treeStyle)
                }
                val child = BackupFormat.validate(root.toString()).child()
                assertEquals(accent, child["accent"].asString)
                assertEquals(treeStyle, child["treeStyle"].asString)
            }
        }
    }

    @Test
    fun legacyMissingOneCustomizationFieldPreservesTheOther() {
        for (version in 1..3) {
            val accentOnly = base(version).apply { child().addProperty("accent", "Amber") }
            val restoredAccentOnly = BackupFormat.validate(accentOnly.toString()).child()
            assertEquals("Amber", restoredAccentOnly["accent"].asString)
            assertEquals("Summer", restoredAccentOnly["treeStyle"].asString)

            val treeOnly = base(version).apply { child().addProperty("treeStyle", "Night") }
            val restoredTreeOnly = BackupFormat.validate(treeOnly.toString()).child()
            assertEquals("Forest", restoredTreeOnly["accent"].asString)
            assertEquals("Night", restoredTreeOnly["treeStyle"].asString)
        }
    }

    @Test
    fun rejectsInvalidChildAccent() {
        for (accent in listOf("", "Purple", "forest")) {
            invalidChild { it.addProperty("accent", accent) }
        }
        invalidChild { it.add("accent", JsonNull.INSTANCE) }
        invalidChild { it.addProperty("accent", 1) }
    }

    @Test
    fun rejectsInvalidChildTreeStyle() {
        for (treeStyle in listOf("", "Winter", "summer")) {
            invalidChild { it.addProperty("treeStyle", treeStyle) }
        }
        invalidChild { it.add("treeStyle", JsonNull.INSTANCE) }
        invalidChild { it.addProperty("treeStyle", true) }
    }

    @Test
    fun missingWishlistOptionalFieldsUseDefaults() {
        val restored = BackupFormat.validate(withItem().toString()).wishlist()[0].asJsonObject

        assertEquals("", restored["notes"].asString)
        assertEquals("", restored["link"].asString)
        assertFalse(restored["obtained"].asBoolean)
        assertEquals(0L, restored["updatedAt"].asLong)
        assertFalse(restored.has("got"))
    }

    @Test
    fun rejectsWishlistOwnedByMissingChild() {
        invalidItem { it.addProperty("childId", "missing") }
    }

    @Test
    fun rejectsWishlistWithoutChildIdentity() {
        invalidItem { it.remove("childId") }
    }

    @Test
    fun rejectsDuplicateWishlistIdentities() {
        val root = withItem()
        BackupFormat.validate(root.toString())
        root.wishlist().add(root.wishlist()[0].deepCopy())
        rejected(root)
    }

    @Test
    fun wishlistTitleLimitUsesTrimmedLength() {
        for (title in listOf("B".repeat(160), " ${"B".repeat(160)} ")) {
            val root = withItem()
            root.wishlist()[0].asJsonObject.addProperty("title", title)
            val restored = BackupFormat.validate(root.toString())
            assertEquals(title, restored.wishlist()[0].asJsonObject["title"].asString)
        }
    }

    @Test
    fun rejectsMissingBlankOrOversizedWishlistTitle() {
        invalidItem { it.remove("title") }
        for (title in listOf("", " \n\t ", "B".repeat(161))) {
            invalidItem { it.addProperty("title", title) }
        }
    }

    @Test
    fun wishlistNotesAccept10000Characters() {
        val root = withItem()
        root.wishlist()[0].asJsonObject.addProperty("notes", "N".repeat(10000))
        val restored = BackupFormat.validate(root.toString())
        assertEquals("N".repeat(10000), restored.wishlist()[0].asJsonObject["notes"].asString)
    }

    @Test
    fun rejectsWishlistNotesOver10000Characters() {
        invalidItem { it.addProperty("notes", "N".repeat(10001)) }
    }

    @Test
    fun wishlistNotesAndLinkMustBeStrings() {
        for (field in listOf("notes", "link")) {
            invalidItem { it.addProperty(field, 1) }
            invalidItem { it.addProperty(field, true) }
            invalidItem { it.add(field, JsonNull.INSTANCE) }
        }
    }

    @Test
    fun wishlistAcceptsEmptyHttpAndHttpsLinks() {
        for (link in
            listOf(
                "",
                "http://example.com/item",
                "https://shop.example.com/item?q=toy#details",
                "https://example.com:1/item",
                "https://example.com:65535/item",
            )) {
            val root = withItem()
            root.wishlist()[0].asJsonObject.addProperty("link", link)
            val restored = BackupFormat.validate(root.toString())
            assertEquals(link, restored.wishlist()[0].asJsonObject["link"].asString)
        }
    }

    @Test
    fun rejectsWishlistLinksWithoutHttpOrHttpsAndValidHost() {
        for (link in
            listOf(
                "/products/toy",
                "//example.com/toy",
                "ftp://example.com/toy",
                "javascript:alert(1)",
                "https:///toy",
                "https://",
                "https://shop .example.com/toy",
                "https://example.com:not-a-port/toy",
            )) {
            invalidItem { it.addProperty("link", link) }
        }
    }

    @Test
    fun rejectsWishlistLinksWithCredentials() {
        for (link in
            listOf(
                "https://user@example.com/toy",
                "http://user:password@example.com/toy",
                "https://@example.com/toy",
            )) {
            invalidItem { it.addProperty("link", link) }
        }
    }

    @Test
    fun rejectsWishlistLinksWithInvalidPorts() {
        for (port in listOf(0, 65536)) {
            invalidItem { it.addProperty("link", "https://example.com:$port/toy") }
        }
    }

    @Test
    fun wishlistLinkAccepts2048Characters() {
        val prefix = "https://example.com/"
        val link = prefix + "a".repeat(2048 - prefix.length)
        val root = withItem()
        root.wishlist()[0].asJsonObject.addProperty("link", link)
        val restored = BackupFormat.validate(root.toString())
        assertEquals(link, restored.wishlist()[0].asJsonObject["link"].asString)
    }

    @Test
    fun rejectsWishlistLinkOver2048Characters() {
        val prefix = "https://example.com/"
        invalidItem { it.addProperty("link", prefix + "a".repeat(2049 - prefix.length)) }
    }

    @Test
    fun wishlistObtainedMustBeBoolean() {
        invalidItem { it.addProperty("obtained", "true") }
        invalidItem { it.addProperty("obtained", 1) }
        invalidItem { it.add("obtained", JsonNull.INSTANCE) }
    }

    @Test
    fun rejectsGotAliasForObtained() {
        invalidItem { it.addProperty("got", true) }
    }
}
