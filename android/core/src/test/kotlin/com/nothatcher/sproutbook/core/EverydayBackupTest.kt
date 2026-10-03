package com.nothatcher.sproutbook.core

import com.google.gson.*
import org.junit.Assert.*
import org.junit.Test

class EverydayBackupTest {
    private val added = listOf("routines", "routineCompletions", "pottyLogs", "milkContainers")

    private fun base() =
        JsonObject().apply {
            addProperty("format", "sproutbook")
            addProperty("version", 3)
            add("settings", JsonObject())
            add("photos", JsonObject())
            add(
                "tables",
                JsonObject().apply {
                    (BackupFormat.tables + added).distinct().forEach { add(it, JsonArray()) }
                    getAsJsonArray("children")
                        .add(JsonParser.parseString("""{"id":"a","name":"Willow"}"""))
                },
            )
        }

    @Test
    fun version2PreservesDiaperAndAddsEmptyRoutineTables() {
        val root = base().apply { addProperty("version", 2) }
        val data = root.getAsJsonObject("tables")
        added.forEach { data.remove(it) }
        data
            .getAsJsonArray("diapers")
            .add(
                JsonParser.parseString(
                    """{"id":"d","childId":"a","recordedAt":1000,"kind":"Mixed"}"""
                )
            )
        val restored = BackupFormat.validate(root.toString())
        assertEquals(BackupFormat.VERSION, restored["version"].asInt)
        assertEquals(
            "Mixed",
            restored
                .getAsJsonObject("tables")
                .getAsJsonArray("diapers")[0]
                .asJsonObject["kind"]
                .asString,
        )
        added.forEach {
            assertEquals(0, restored.getAsJsonObject("tables").getAsJsonArray(it).size())
        }
    }

    @Test
    fun version3AcceptsOwnedRoutinesPottyAndMilk() {
        val root = base()
        val data = root.getAsJsonObject("tables")
        data
            .getAsJsonArray("routines")
            .add(
                JsonParser.parseString(
                    """{"id":"r","childId":"a","title":"Pack school bag","weekdays":31,"timeMinutes":480}"""
                )
            )
        data
            .getAsJsonArray("routineCompletions")
            .add(JsonParser.parseString("""{"id":"rc","childId":"a","routineId":"r","day":1}"""))
        data
            .getAsJsonArray("pottyLogs")
            .add(
                JsonParser.parseString(
                    """{"id":"p","childId":"a","recordedAt":1000,"kind":"Tried"}"""
                )
            )
        data
            .getAsJsonArray("milkContainers")
            .add(
                JsonParser.parseString(
                    """{"id":"m","childId":"a","label":"Bag one","storedAt":1000,"amountMl":90}"""
                )
            )
        assertEquals(BackupFormat.VERSION, BackupFormat.validate(root.toString())["version"].asInt)
    }

    @Test
    fun rejectsDuplicateCompletionAndInvalidVolume() {
        val root = base()
        val data = root.getAsJsonObject("tables")
        data
            .getAsJsonArray("routines")
            .add(
                JsonParser.parseString(
                    """{"id":"r","childId":"a","title":"Brush teeth","weekdays":127}"""
                )
            )
        listOf("c1", "c2").forEach {
            data
                .getAsJsonArray("routineCompletions")
                .add(
                    JsonParser.parseString("""{"id":"$it","childId":"a","routineId":"r","day":1}""")
                )
        }
        rejected(root)
        val bad = base()
        bad.getAsJsonObject("tables")
            .getAsJsonArray("milkContainers")
            .add(
                JsonParser.parseString(
                    """{"id":"m","childId":"a","label":"Bag","storedAt":1000,"amountMl":0}"""
                )
            )
        rejected(bad)
    }

    private fun rejected(root: JsonObject) {
        try {
            BackupFormat.validate(root.toString())
            fail("Invalid backup accepted")
        } catch (_: IllegalArgumentException) {}
    }
}
