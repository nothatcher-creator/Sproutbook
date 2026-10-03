package com.nothatcher.sproutbook.core

import com.google.gson.*
import org.junit.Assert.*
import org.junit.Test

class BackupFormatTest {
    private fun base(): JsonObject =
        JsonObject().apply {
            addProperty("format", "sproutbook")
            addProperty("version", BackupFormat.VERSION)
            addProperty("exportedAt", 1000)
            add("settings", JsonObject())
            add("photos", JsonObject())
            add(
                "tables",
                JsonObject().apply {
                    BackupFormat.tables.forEach { add(it, JsonArray()) }
                    getAsJsonArray("children")
                        .add(
                            JsonParser.parseString(
                                """{"id":"a","name":"Rowan","stage":"BABY","updatedAt":1000,"accent":"Forest","treeStyle":"Summer","homeSections":"","homeHiddenSections":"","homeQuickActions":"memory,schedule","homeBackground":"woodland","homeBackgroundPhoto":null}"""
                            )
                        )
                },
            )
        }

    @Test
    fun legacyBackupUpgradesWithoutLosingRecords() {
        val old = base().apply { addProperty("version", 1) }
        val child = old.getAsJsonObject("tables").getAsJsonArray("children")[0].asJsonObject
        listOf("accent", "treeStyle", "homeSections", "homeHiddenSections", "homeQuickActions", "homeBackground", "homeBackgroundPhoto").forEach { child.remove(it) }
        listOf("diapers", "bottlePreps", "shoppingItems", "routines", "routineCompletions", "pottyLogs", "milkContainers").forEach {
            old.getAsJsonObject("tables").remove(it)
        }
        val upgraded = BackupFormat.validate(old.toString())
        assertEquals(BackupFormat.VERSION, upgraded["version"].asInt)
        assertEquals(
            "Rowan",
            upgraded
                .getAsJsonObject("tables")
                .getAsJsonArray("children")[0]
                .asJsonObject["name"]
                .asString,
        )
        assertEquals(0, upgraded.getAsJsonObject("tables").getAsJsonArray("diapers").size())
    }

    @Test
    fun newBackupRequiresNewTables() {
        invalid { it.getAsJsonObject("tables").remove("diapers") }
    }

    @Test
    fun rejectInvalidPreparedBottle() {
        invalid {
            it.getAsJsonObject("tables")
                .getAsJsonArray("bottlePreps")
                .add(
                    JsonParser.parseString(
                        """{"id":"b","childId":"a","preparedAt":1000,"amountMl":-1,"status":"Prepared"}"""
                    )
                )
        }
    }

    @Test
    fun rejectShoppingForeignChildLink() {
        invalid {
            val tables = it.getAsJsonObject("tables")
            tables
                .getAsJsonArray("shoppingItems")
                .add(
                    JsonParser.parseString(
                        """{"id":"s","childId":"a","name":"Wipes","quantity":2,"unit":"packs","inventoryId":"missing"}"""
                    )
                )
        }
    }

    private fun invalid(change: (JsonObject) -> Unit) {
        val b = base()
        BackupFormat.validate(b.toString())
        change(b)
        try {
            BackupFormat.validate(b.toString())
            fail("Invalid backup accepted")
        } catch (expected: IllegalArgumentException) {}
    }

    @Test
    fun missingOptionalFieldsRepair() {
        val b = BackupFormat.validate(base().toString())
        assertEquals(
            "",
            b.getAsJsonObject("tables").getAsJsonArray("children")[0].asJsonObject["notes"].asString,
        )
    }

    @Test
    fun rejectFutureVersion() {
        invalid { it.addProperty("version", 99) }
    }

    @Test
    fun rejectMissingTables() {
        invalid { it.getAsJsonObject("tables").remove("feeds") }
    }

    @Test
    fun rejectDuplicateIdentities() {
        invalid {
            val a = it.getAsJsonObject("tables").getAsJsonArray("children")
            a.add(a[0].deepCopy())
        }
    }

    @Test
    fun rejectOrphanRecord() {
        invalid {
            it.getAsJsonObject("tables")
                .getAsJsonArray("inventorys")
                .add(
                    JsonParser.parseString(
                        """{"id":"s","childId":"missing","name":"Wipes","quantity":2,"updatedAt":1000}"""
                    )
                )
        }
    }

    @Test
    fun rejectWrongTypes() {
        invalid {
            it.getAsJsonObject("tables")
                .getAsJsonArray("children")[0]
                .asJsonObject
                .addProperty("name", 12)
        }
    }

    @Test
    fun rejectInvalidEnum() {
        invalid {
            it.getAsJsonObject("tables")
                .getAsJsonArray("children")[0]
                .asJsonObject
                .addProperty("stage", "ALIEN")
        }
    }

    @Test
    fun rejectPhotoPathTraversal() {
        invalid { it.getAsJsonObject("photos").addProperty("../bad.jpg", "AA==") }
    }

    @Test
    fun rejectDuplicateTreeSlots() {
        invalid {
            val rows = it.getAsJsonObject("tables").getAsJsonArray("memorys")
            listOf("x", "y").forEach { id ->
                rows.add(
                    JsonParser.parseString(
                        """{"id":"$id","childId":"a","title":"Smile","occurredOn":1,"anchor":1,"chapter":0,"updatedAt":1000}"""
                    )
                )
            }
        }
    }

    @Test
    fun pastRemindersDoNotSpam() {
        assertNull(ReminderPolicy.delay(100000, 15, 100000))
        assertEquals(100000L, ReminderPolicy.delay(1000000, 15, 0))
        assertNull(ReminderPolicy.delay(1000000, 0, 0))
    }

    @Test
    fun rejectToothIndexBeforeIntegerNarrowing() {
        invalid {
            it.getAsJsonObject("tables")
                .getAsJsonArray("tooths")
                .add(
                    JsonParser.parseString(
                        """{"id":"a-tooth-4294967296","childId":"a","toothIndex":4294967296,"stage":"Not seen"}"""
                    )
                )
        }
    }

    @Test
    fun rejectFutureHistoricalRecords() {
        val tomorrow = java.time.LocalDate.now().plusDays(1).toEpochDay()
        val future = System.currentTimeMillis() + 86400000
        val records =
            mapOf(
                "feeds" to """{"id":"f","childId":"a","startsAt":$future,"amountMl":90}""",
                "sleeps" to """{"id":"s","childId":"a","startsAt":$future}""",
                "foods" to """{"id":"f","childId":"a","name":"Pear","introducedOn":$tomorrow}""",
                "milestones" to
                    """{"id":"m","childId":"a","title":"Smile","stage":"BABY","completedOn":$tomorrow}""",
                "healthRecords" to
                    """{"id":"h","childId":"a","kind":"Doctor note","title":"Visit","recordedAt":$future}""",
            )
        records.forEach { (table, record) ->
            invalid {
                it.getAsJsonObject("tables")
                    .getAsJsonArray(table)
                    .add(JsonParser.parseString(record))
            }
        }
    }

    @Test
    fun futurePlanningRemainsValid() {
        val b = base()
        val day = java.time.LocalDate.now().plusDays(10).toEpochDay()
        val tables = b.getAsJsonObject("tables")
        tables
            .getAsJsonArray("appointments")
            .add(
                JsonParser.parseString(
                    """{"id":"a1","childId":"a","title":"Visit","startsAt":${System.currentTimeMillis()+86400000}}"""
                )
            )
        tables
            .getAsJsonArray("meals")
            .add(
                JsonParser.parseString(
                    """{"id":"${SolidsRules.mealId("a",day,"Lunch")}","childId":"a","title":"Pear","day":$day,"slot":"Lunch"}"""
                )
            )
        BackupFormat.validate(b.toString())
    }
}
