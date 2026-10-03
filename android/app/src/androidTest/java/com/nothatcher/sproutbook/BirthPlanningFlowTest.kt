package com.nothatcher.sproutbook

import androidx.compose.ui.test.*
import com.nothatcher.sproutbook.core.BirthPlanCatalog
import com.nothatcher.sproutbook.core.BirthPlanningCatalog
import com.nothatcher.sproutbook.data.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class BirthPlanningFlowTest : FlowFixture() {
    @Test
    fun birthPlanningIsReachableFromPregnancy() {
        care("Pregnancy")
        pageClick("Homebirth & freebirth")
        compose.onNode(hasScrollToIndexAction()).performScrollToIndex(0)
        compose.onNodeWithText(BirthPlanningCatalog.emergencyTitle).assertExists()
        pageClick("Other urgent warning signs")
        compose.onNodeWithText(BirthPlanningCatalog.urgent).assertExists()
        pageClick("Freebirth")
        pageClick("Considering freebirth / unassisted birth")
        compose.onNodeWithText(BirthPlanningCatalog.guides.first { it.id == "free" }.paragraphs[1]).assertExists()
    }

    @Test
    fun checklistEditsCustomItemsAndSeparateGuidesPersist() {
        care("Pregnancy")
        pageClick("Homebirth & freebirth")
        val template = BirthPlanningCatalog.guides.first().items.first()
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasContentDescription("Checklist: ${template.title}"))
        compose.onNodeWithContentDescription("Checklist: ${template.title}").performScrollTo().performClick()
        val id = BirthPlanningCatalog.prefix("qa-child", "home") + template.key
        awaitDb { app.db.prepItems().get(id)?.completed == true }
        pageClick(template.title)
        compose.onNodeWithText("Notes").performTextReplacement("Our midwife discussion is booked")
        compose.onNodeWithText("Save preparation").performScrollTo().performClick()
        awaitDb { app.db.prepItems().get(id)?.notes == "Our midwife discussion is booked" }
        pageClick("Add to homebirth list")
        compose.onNodeWithText("Title").performTextInput("Neighbour for pet care")
        compose.onNodeWithText("Notes").performTextInput("Keep their phone on paper")
        compose.onNodeWithText("Save preparation").performScrollTo().performClick()
        awaitDb { app.db.prepItems().all().any { it.title == "Neighbour for pet care" } }
        pageClick("Freebirth")
        assertEquals(2, runBlocking { app.db.prepItems().forChild("qa-child").size })
        pageClick("Homebirth")
        pageClick("Neighbour for pet care")
        compose.onNodeWithText("Delete item").performScrollTo().performClick()
        compose.onNodeWithText("Delete").performClick()
        awaitDb { app.db.prepItems().all().size == 1 }
        assertTrue(runBlocking { app.db.prepItems().get(id)!!.completed })
    }

    @Test
    fun homebirthPlanFeedsLabourModeAndContractionSurvivesRecreation() {
        care("Pregnancy")
        pageClick("Homebirth plan")
        savePlanField("Maternity team phone", "+1 (555) 123-4567")
        savePlanField("Birth address & access", "Sample address; side gate")
        savePlanField("Hospital & transfer plan", "Discussed destination; support person has the bag")
        pageClick("Open labour mode")
        pageClick("Start contraction")
        awaitDb { app.db.pregnancyEvents().forChild("qa-child").any { it.kind == "Contraction" && it.endsAt == null } }
        val running = runBlocking { app.db.pregnancyEvents().forChild("qa-child").single() }
        compose.activityRule.scenario.recreate()
        compose.onNode(hasScrollToIndexAction()).performScrollToIndex(0)
        waitText("Labour focus")
        pageClick("Stop contraction")
        awaitDb { app.db.pregnancyEvents().get(running.id)?.endsAt != null }
        assertEquals(running.startsAt, runBlocking { app.db.pregnancyEvents().get(running.id)!!.startsAt })
        pageClick("Hospital & transfer plan")
        compose.onNodeWithText("Discussed destination; support person has the bag").assertExists()
        pageClick("View or update homebirth plan")
        pageClick("Maternity team phone")
        compose.onNode(hasSetTextAction() and hasText("Maternity team phone")).assertTextContains("+1 (555) 123-4567")
        assertEquals(3, runBlocking { app.db.prepItems().all().size })
    }

    @Test
    fun childSwitchDiscardsPlanDraftAndCaregiverModeBlocksActions() {
        runBlocking { app.db.children().save(Child(id = "qa-second", name = "Ash", stage = "PREGNANCY")) }
        care("Pregnancy")
        pageClick("Homebirth plan")
        pageClick("Birth address & access")
        compose.onNode(hasSetTextAction() and hasText("Birth address & access")).performTextInput("Unsaved address")
        runBlocking { app.settings.select("qa-second") }
        waitText("Labour mode")
        pageClick("Homebirth plan")
        pageClick("Birth address & access")
        compose.onNode(hasSetTextAction() and hasText("Birth address & access")).assertTextEquals("Birth address & access", "")
        runBlocking { app.settings.boolean("grandparent", true) }
        waitText("Grandparent mode · Read only")
        compose.onNodeWithText("Save birth plan").assertDoesNotExist()
        pageClick("Homebirth")
        val first = BirthPlanningCatalog.guides.first().items.first()
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasContentDescription("Checklist: ${first.title}"))
        compose.onNodeWithContentDescription("Checklist: ${first.title}").performScrollTo().assertIsNotEnabled()
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Add to homebirth list"))
        compose.onNodeWithText("Add to homebirth list").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("Save preparation").assertDoesNotExist()
        assertTrue(runBlocking { app.db.prepItems().all().isEmpty() })
    }

    private fun savePlanField(title: String, value: String) {
        pageClick(title)
        compose.onNode(hasSetTextAction() and hasText(title)).performTextReplacement(value)
        compose.onNodeWithText("Save birth plan").performScrollTo().performClick()
        val key = BirthPlanCatalog.fields.first { it.title == title }.key
        awaitDb { app.db.prepItems().get(BirthPlanCatalog.prefix("qa-child") + key)?.notes == value }
    }
}
