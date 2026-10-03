package com.nothatcher.sproutbook.core

import com.google.gson.*
import org.junit.Assert.*
import org.junit.Test

class HomeOrganizerBackupTest {
    private val homeFields = listOf("homeSections", "homeHiddenSections", "homeQuickActions", "homeBackground", "homeBackgroundPhoto")

    private fun base(version: Int = 5): JsonObject = JsonObject().apply {
        addProperty("format", "sproutbook")
        addProperty("version", version)
        add("settings", JsonObject())
        add("photos", JsonObject())
        add("tables", JsonObject().apply {
            BackupFormat.tables.forEach { add(it, JsonArray()) }
            if (version == 1) listOf("diapers", "bottlePreps", "shoppingItems").forEach { remove(it) }
            if (version < 3) listOf("routines", "routineCompletions", "pottyLogs", "milkContainers").forEach { remove(it) }
            if (version < 4) remove("wishlistItems")
            val child = JsonParser.parseString("""{"id":"a","name":"Willow"}""").asJsonObject
            if (version >= 4) {
                child.addProperty("accent", "Sky")
                child.addProperty("treeStyle", "Night")
            }
            if (version >= 5) {
                child.addProperty("homeSections", "")
                child.addProperty("homeHiddenSections", "")
                child.addProperty("homeQuickActions", "memory,schedule")
                child.addProperty("homeBackground", "woodland")
                child.add("homeBackgroundPhoto", JsonNull.INSTANCE)
            }
            getAsJsonArray("children").add(child)
        })
    }

    private fun JsonObject.child() = getAsJsonObject("tables").getAsJsonArray("children")[0].asJsonObject

    private fun rejected(root: JsonObject) {
        try {
            BackupFormat.validate(root.toString())
            fail("Invalid organizer backup accepted")
        } catch (_: IllegalArgumentException) {}
    }

    private fun invalid(change: (JsonObject) -> Unit) {
        val root = base()
        BackupFormat.validate(root.toString())
        change(root)
        rejected(root)
    }

    @Test
    fun versions1Through4ReceiveNonDestructiveOrganizerDefaults() {
        for (version in 1..4) {
            val old = base(version)
            old.getAsJsonObject("tables").getAsJsonArray("memorys").add(
                JsonParser.parseString("""{"id":"m","childId":"a","title":"First smile","occurredOn":1,"anchor":0}""")
            )
            val restored = BackupFormat.validate(old.toString())
            assertEquals(5, restored["version"].asInt)
            assertEquals("Willow", restored.child()["name"].asString)
            assertEquals("", restored.child()["homeSections"].asString)
            assertEquals("", restored.child()["homeHiddenSections"].asString)
            assertEquals("memory,schedule", restored.child()["homeQuickActions"].asString)
            assertEquals("woodland", restored.child()["homeBackground"].asString)
            assertTrue(restored.child()["homeBackgroundPhoto"].isJsonNull)
            assertEquals("First smile", restored.getAsJsonObject("tables").getAsJsonArray("memorys")[0].asJsonObject["title"].asString)
            if (version == 4) {
                assertEquals("Sky", restored.child()["accent"].asString)
                assertEquals("Night", restored.child()["treeStyle"].asString)
            }
            assertEquals(restored, BackupFormat.validate(restored.toString()))
        }
    }

    @Test
    fun version5RequiresEveryOrganizerFieldIncludingExplicitNullPhoto() {
        for (field in homeFields) invalid { it.child().remove(field) }
    }

    @Test
    fun oldVersion4StillRequiresItsAppearanceFields() {
        for (field in listOf("accent", "treeStyle")) {
            val old = base(4)
            BackupFormat.validate(old.toString())
            old.child().remove(field)
            rejected(old)
        }
    }

    @Test
    fun customizedSectionsShortcutsAndBackgroundPhotoRoundTripTogether() {
        val root = base().apply {
            child().apply {
                addProperty("homeSections", "memory_tree,quick_actions,chapter")
                addProperty("homeHiddenSections", "feeding,diapers")
                addProperty("homeQuickActions", "wishlist,schedule,memory")
                addProperty("homeBackground", "photo")
                addProperty("homeBackgroundPhoto", "woodland_picture.jpg")
            }
            getAsJsonObject("photos").addProperty("woodland_picture.jpg", "AQID")
        }
        val first = BackupFormat.validate(root.toString())
        val restored = BackupFormat.validate(first.toString())
        assertEquals(first, restored)
        for (field in homeFields) assertEquals(root.child()[field], restored.child()[field])
        assertEquals("AQID", restored.getAsJsonObject("photos")["woodland_picture.jpg"].asString)
    }

    @Test
    fun hiddenAllSectionsAndNoShortcutsAreValidSavedChoices() {
        val root = base().apply {
            child().addProperty("homeHiddenSections", HomeOrganizerCatalog.sectionIds.joinToString(","))
            child().addProperty("homeQuickActions", "")
        }
        val restored = BackupFormat.validate(root.toString())
        assertEquals(root.child()["homeHiddenSections"], restored.child()["homeHiddenSections"])
        assertEquals("", restored.child()["homeQuickActions"].asString)
    }

    @Test
    fun backgroundPhotoMustBeIncludedAndCannotEscapePrivatePhotos() {
        invalid { it.child().addProperty("homeBackground", "photo"); it.child().addProperty("homeBackgroundPhoto", "missing.jpg") }
        for (name in listOf("../private.jpg", "/private.jpg", "bad.png")) {
            invalid { it.child().addProperty("homeBackground", "photo"); it.child().addProperty("homeBackgroundPhoto", name) }
        }
        invalid { it.child().addProperty("homeBackgroundPhoto", "unexpected.jpg"); it.getAsJsonObject("photos").addProperty("unexpected.jpg", "AQID") }
        invalid { it.child().addProperty("homeBackground", "photo") }
    }

    @Test
    fun invalidOrganizerChoicesAndWrongTypesAreRejectedBeforeRestore() {
        for ((field, values) in mapOf(
            "homeSections" to listOf("chapter,chapter", "unknown", "chapter,", "a".repeat(2049)),
            "homeHiddenSections" to listOf("growth,growth", "unknown"),
            "homeQuickActions" to listOf("memory,memory", "unknown"),
            "homeBackground" to listOf("unknown", "Woodland", ""),
        )) {
            for (value in values) invalid { it.child().addProperty(field, value) }
            invalid { it.child().addProperty(field, 1) }
            invalid { it.child().add(field, JsonNull.INSTANCE) }
        }
        invalid { it.child().addProperty("homeBackgroundPhoto", 1) }
    }
}
