package com.nothatcher.sproutbook

import androidx.compose.ui.test.*
import com.nothatcher.sproutbook.data.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class NewCareFlowTest : FlowFixture() {
    @Before
    fun units() {
        runBlocking {
            app.settings.string("weightUnit", "kg")
            app.settings.string("volumeUnit", "mL")
        }
    }

    @Test
    fun newToolsAreReadOnlyForCaregivers() {
        runBlocking { app.settings.boolean("grandparent", true) }
        waitText("Grandparent mode · Read only")
        listOf(
                "Diapers" to "Log wet diaper",
                "Growth journal" to "Add measurement",
                "Prepared bottles" to "Prepare a bottle",
                "Shopping list" to "Add shopping item",
            )
            .forEach { (route, action) ->
                care(route)
                compose.onNodeWithText(action).performScrollTo().assertIsNotEnabled()
                compose.onNodeWithContentDescription("Back").performClick()
            }
    }

    @Test
    fun diaperQuickLogEditDelete() {
        care("Diapers")
        compose.onNodeWithText("Log wet diaper").performScrollTo().performClick()
        awaitDb { app.db.diapers().all().size == 1 }
        pageClick("Wet diaper")
        compose.onNodeWithText("Mixed").performClick()
        compose.onNodeWithText("Diaper notes").performTextInput("After nap")
        compose.onNodeWithText("Save diaper").performScrollTo().performClick()
        awaitDb { app.db.diapers().all().single().kind == "Mixed" }
        pageClick("Mixed diaper")
        compose.onNodeWithText("Delete diaper").performScrollTo().performClick()
        compose.onNodeWithText("Delete").performClick()
        awaitDb { app.db.diapers().all().isEmpty() }
    }

    @Test
    fun growthAddEditDelete() {
        care("Growth journal")
        compose.onNodeWithText("Add measurement").performScrollTo().performClick()
        waitText("Measurement")
        compose.onNodeWithText("Measurement").performTextInput("4.5")
        compose.onNodeWithText("Save measurement").performScrollTo().performClick()
        awaitDb { app.db.healthRecords().all().size == 1 }
        waitText("Measurement saved")
        compose.waitUntil(60000) {
            compose.onAllNodesWithText("Measurement saved").fetchSemanticsNodes().isEmpty()
        }
        pageClick("Weight · 4.50 kg")
        compose.onNodeWithText("Measurement").performTextReplacement("5")
        compose.onNodeWithText("Save measurement").performScrollTo().performClick()
        awaitDb { app.db.healthRecords().all().single().value.toDouble() == 5.0 }
        waitText("Measurement saved")
        compose.waitUntil(60000) {
            compose.onAllNodesWithText("Measurement saved").fetchSemanticsNodes().isEmpty()
        }
        pageClick("Weight · 5.00 kg")
        compose.onNodeWithText("Delete measurement").performScrollTo().performClick()
        compose.onNodeWithText("Delete").performClick()
        awaitDb { app.db.healthRecords().all().isEmpty() }
    }

    @Test
    fun noteEditsPreservePreciseAndUnknownGrowthUnits() {
        runBlocking {
            app.repository.saveHealth(
                HealthRecord(
                    id = "precision",
                    childId = "qa-child",
                    kind = "Weight",
                    recordedAt = 1000,
                    title = "Weight",
                    value = "3.215",
                    unit = "kg",
                )
            )
        }
        care("Growth journal")
        pageClick("Weight · 3.22 kg")
        compose.onNodeWithText("Measurement notes").performTextInput("Visit note")
        compose.onNodeWithText("Save measurement").performScrollTo().performClick()
        awaitDb { app.db.healthRecords().get("precision")!!.notes == "Visit note" }
        assertEquals("3.215", runBlocking { app.db.healthRecords().get("precision")!!.value })
    }

    @Test
    fun noteEditsPreserveUnknownGrowthUnits() {
        care("Growth journal")
        runBlocking {
            app.repository.saveHealth(
                HealthRecord(
                    id = "unknown",
                    childId = "qa-child",
                    kind = "Weight",
                    recordedAt = 2000,
                    title = "Weight",
                    value = "7",
                    unit = "unknown",
                )
            )
        }
        pageClick("Weight · 7 unknown")
        compose.onNodeWithText("Measurement notes").performTextInput("Original units")
        compose.onNodeWithText("Save measurement").performScrollTo().performClick()
        awaitDb { app.db.healthRecords().get("unknown")!!.notes == "Original units" }
        assertEquals("unknown", runBlocking { app.db.healthRecords().get("unknown")!!.unit })
        assertEquals("7", runBlocking { app.db.healthRecords().get("unknown")!!.value })
    }

    @Test
    fun unsavedUnlinkCannotReplenishSupplies() {
        runBlocking {
            app.repository.saveInventory(
                Inventory(
                    id = "stock",
                    childId = "qa-child",
                    name = "Wipes",
                    quantity = 0.0,
                    threshold = 2.0,
                    unit = "packs",
                )
            )
            app.repository.addLowStock("qa-child")
        }
        care("Shopping list")
        pageClick("Wipes")
        compose.onNodeWithText("Unlink supply").performScrollTo().performClick()
        compose.onNodeWithText("Mark purchased").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("Save shopping item").performScrollTo().performClick()
        awaitDb { app.db.shoppingItems().all().single().inventoryId == null }
        pageClick("Wipes")
        compose.onNodeWithText("Mark purchased").performScrollTo().performClick()
        awaitDb { app.db.shoppingItems().all().single().checked }
        assertEquals(0.0, runBlocking { app.db.inventorys().get("stock")!!.quantity }, 0.0)
    }

    @Test
    fun prepareUseBottleAndShopping() {
        care("Prepared bottles")
        compose.onNodeWithText("Prepare a bottle").performScrollTo().performClick()
        compose.onNodeWithText("Prepared amount · mL").performTextInput("90")
        compose.onNodeWithText("Save preparation").performScrollTo().performClick()
        awaitDb { app.db.bottlePreps().all().size == 1 }
        pageClick("Prepared · 90 mL")
        compose.onNodeWithText("Prepared amount · mL").performTextReplacement("60")
        compose.onNodeWithText("Use & log feeding").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("Save preparation").performScrollTo().performClick()
        awaitDb { app.db.bottlePreps().all().single().amountMl == 60.0 }
        pageClick("Prepared · 60 mL")
        compose.onNodeWithText("Amount actually fed · mL").performTextReplacement("30")
        compose.onNodeWithText("Use & log feeding").performScrollTo().performClick()
        awaitDb {
            app.db.feeds().all().size == 1 && app.db.bottlePreps().all().single().status == "Used"
        }
        awaitDb { app.db.feeds().all().single().amountMl == 30.0 }
        compose.waitUntil(60000) {
            compose.onAllNodesWithText("Use & log feeding").fetchSemanticsNodes().isEmpty()
        }
        compose.onNodeWithContentDescription("Back").performClick()
        care("Shopping list")
        compose.onNodeWithText("Add shopping item").performScrollTo().performClick()
        compose.onNodeWithText("Item name").performTextInput("Nappies")
        compose.onNodeWithText("Save shopping item").performScrollTo().performClick()
        awaitDb { app.db.shoppingItems().all().size == 1 }
        pageClick("Nappies")
        compose.onNodeWithText("Quantity to buy").performTextReplacement("2")
        compose.onNodeWithText("Mark purchased").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("Save shopping item").performScrollTo().performClick()
        awaitDb { app.db.shoppingItems().all().single().quantity == 2.0 }
        pageClick("Nappies")
        compose.onNodeWithText("Mark purchased").performScrollTo().performClick()
        awaitDb { app.db.shoppingItems().all().single().checked }
    }
}
