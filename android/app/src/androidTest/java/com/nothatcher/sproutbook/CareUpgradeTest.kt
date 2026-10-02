package com.nothatcher.sproutbook
import androidx.compose.ui.test.*
import com.nothatcher.sproutbook.data.*
import com.nothatcher.sproutbook.services.SoundService
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class CareUpgradeTest : FlowFixture() {
    private fun screenshot(name: String) {
        compose.waitForIdle()
        compose.waitUntil(60000) { compose.activity.hasWindowFocus() }
        val bitmap = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        requireNotNull(bitmap)
        val file = java.io.File(app.getExternalFilesDir(null), "v33-$name.png")
        file.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }
    @Test fun adviceTabsSearchAndExpandNewArticles() {
        care("Mom & Dad advice")
        compose.onNodeWithText("Search advice").performTextInput("cradle position")
        pageClick("Cradle position")
        compose.onNodeWithText("Read the source").assertExists()
        screenshot("advice-mom")
        pageClick("Dad")
        compose.onNodeWithText("Search advice").performTextInput("caregiver handover")
        pageClick("Caregiver handover")
        compose.onNodeWithText("Cradle position").assertDoesNotExist()
        compose.onNodeWithText("Emergency card", substring = true).assertExists()
        screenshot("advice-dad")
    }
    @Test fun soundButtonsPauseResumeSwitchAndStopActualService() {
        care("Sleep & sound")
        pageClick("Sound machine")
        pageClick("Ocean")
        pageClick("Play ocean sound")
        try {
            awaitDb { SoundService.state.value.playing }
            screenshot("sound")
            pageClick("Pause ocean sound")
            awaitDb { SoundService.state.value.paused }
            pageClick("Stream")
            awaitDb { SoundService.state.value.paused && SoundService.state.value.kind == "Stream" }
            pageClick("Resume stream sound")
            awaitDb { SoundService.state.value.playing }
            pageClick("Stop stream sound")
            awaitDb { !SoundService.state.value.active }
        } finally { SoundService.stop(app) }
    }
    @Test fun toothDateSurvivesRestartAndCaregiverCannotEdit() {
        val day = LocalDate.now().minusDays(1).toEpochDay()
        runBlocking { app.repository.saveTooth(Tooth(childId = "qa-child", toothIndex = 4, stage = "Erupted", eruptedOn = day)) }
        compose.activityRule.scenario.recreate()
        care("Little teeth")
        compose.onNodeWithTag("tooth-14").performScrollTo()
        screenshot("teeth")
        compose.onNodeWithTag("tooth-4").performScrollTo().performClick()
        compose.onNode(hasText("Erupted · ${LocalDate.ofEpochDay(day)}") and hasAnyAncestor(isDialog())).assertExists()
        back()
        runBlocking { app.settings.boolean("grandparent", true) }
        waitText("Grandparent mode · Read only")
        compose.onNodeWithTag("tooth-4").performScrollTo().performTouchInput { longClick() }
        compose.onNodeWithText("Save tooth").assertDoesNotExist()
        compose.onNodeWithTag("tooth-4").assert(
            SemanticsMatcher.keyNotDefined(androidx.compose.ui.semantics.SemanticsActions.OnLongClick))
        assertEquals(day, runBlocking { app.db.tooths().all().single().eruptedOn })
    }
    @Test fun pendingSoundChoicesSurviveChangingTabs() {
        care("Sleep & sound")
        pageClick("Sound machine")
        pageClick("Ocean")
        pageClick("15 min")
        pageClick("Sleep journal")
        pageClick("Sound machine")
        compose.onNodeWithText("Play ocean sound").assertExists()
        compose.onNodeWithText("15 min").assertIsSelected()
    }

}
