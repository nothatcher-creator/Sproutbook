package com.nothatcher.sproutbook

import androidx.compose.ui.test.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class QuickLogFlowTest : FlowFixture() {
    private fun has(text: String) = compose.onAllNodesWithText(text, substring = true).fetchSemanticsNodes().isNotEmpty()

    private fun tap(tag: String) {
        compose.onNodeWithTag("today-list").performScrollToNode(hasTestTag(tag))
        compose.onNodeWithTag(tag).performClick()
    }

    @Test
    fun todayLogsDiaperAndSleepInOneTapWithUndo() {
        tap("today-log-diaper-wet")
        compose.waitUntil(10000) { has("Wet diaper logged") && has("Last change just now · wet") }
        assertEquals("Wet", runBlocking { app.db.diapers().all().single().kind })
        compose.onNodeWithTag("snackbar-undo").performClick()
        compose.waitUntil(10000) { runBlocking { app.db.diapers().all().isEmpty() } }

        tap("today-log-sleep-start-nap")
        compose.waitUntil(10000) { has("Sleeping now") }
        tap("today-log-sleep-stop-nap")
        compose.waitUntil(10000) { runBlocking { app.db.sleeps().forChild("qa-child").single().endsAt != null } }
        // The newest Undo replaces "Nap started", so it resumes the nap instead of deleting it.
        compose.waitUntil(10000) { has("Nap ended") }
        compose.onNodeWithTag("snackbar-undo").performClick()
        compose.waitUntil(10000) { runBlocking { app.db.sleeps().forChild("qa-child").single().endsAt == null } }
    }

    @Test
    fun caregiverModeHidesQuickLogButtons() {
        runBlocking { app.settings.boolean("grandparent", true) }
        waitText("Grandparent mode · Read only")
        compose.onNodeWithTag("today-log-diaper-wet").assertDoesNotExist()
        compose.onNodeWithTag("today-log-sleep-start-nap").assertDoesNotExist()
    }
}
