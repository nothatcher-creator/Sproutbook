package com.nothatcher.sproutbook

import androidx.compose.ui.test.*
import org.junit.Assert.*
import org.junit.Test

class CareFlowTest : FlowFixture() {
    @Test
    fun inventorySaveAdjustAndDelete() {
        care("Family cupboard")
        pageClick("Add supply")
        compose.onNodeWithText("Name").performTextInput("Wipes")
        compose.onNodeWithText("Quantity").performTextReplacement("3")
        compose.onNodeWithText("Save supply").performScrollTo().performClick()
        awaitDb { app.db.inventorys().all().size == 1 }
        compose.onNodeWithContentDescription("Use one Wipes").performScrollTo().performClick()
        awaitDb { app.db.inventorys().all().single().quantity == 2.0 }
        pageClick("Wipes")
        compose.onNodeWithText("Delete supply").performScrollTo().performClick()
        compose.onNodeWithText("Delete").performClick()
        awaitDb { app.db.inventorys().all().isEmpty() }
    }

    @Test
    fun foodAndMealSave() {
        care("Solids & meals")
        pageClick("Introduce a food")
        compose.onNodeWithText("Food").performTextInput("Pear")
        compose.onNodeWithText("Liked").performClick()
        compose.onNodeWithText("Save food").performScrollTo().performClick()
        awaitDb { app.db.foods().all().firstOrNull()?.liking == "Liked" }
        pageClick("This week's meals")
        compose.onAllNodesWithText("Breakfast").onFirst().performClick()
        compose.onNodeWithText("Meal").performTextInput("Soft pear and cereal")
        compose.onNodeWithText("Save meal").performScrollTo().performClick()
        awaitDb { app.db.meals().all().size == 1 }
    }

    @Test
    fun memoryAddTimelineEditDelete() {
        care("Memory tree")
        pageClick("Add a memory")
        compose.onNodeWithText("Memory title").performTextInput("First smile")
        compose.onNodeWithText("Save memory").performScrollTo().performClick()
        awaitDb { app.db.memorys().all().size == 1 }
        pageClick("Timeline")
        pageClick("First smile")
        compose.onNodeWithText("Memory title").performTextReplacement("A lovely smile")
        compose.onNodeWithText("Save memory").performScrollTo().performClick()
        awaitDb { app.db.memorys().all().single().title == "A lovely smile" }
        pageClick("A lovely smile")
        compose.onNodeWithText("Delete memory").performScrollTo().performClick()
        compose.onNodeWithText("Delete").performClick()
        awaitDb { app.db.memorys().all().isEmpty() }
    }

    @Test
    fun pregnancySessionsPersist() {
        care("Pregnancy")
        pageClick("Kicks")
        pageClick("+ Kick")
        awaitDb { app.db.pregnancyEvents().all().firstOrNull()?.count == 1 }
        pageClick("Finish & reset session")
        awaitDb { app.db.pregnancyEvents().all().firstOrNull()?.endsAt != null }
        pageClick("Contractions")
        pageClick("Start contraction")
        awaitDb {
            app.db.pregnancyEvents().all().any { it.kind == "Contraction" && it.endsAt == null }
        }
        pageClick("Stop contraction")
        awaitDb { app.db.pregnancyEvents().all().all { it.endsAt != null } }
    }
}
