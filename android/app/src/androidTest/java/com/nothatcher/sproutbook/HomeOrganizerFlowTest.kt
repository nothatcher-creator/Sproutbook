package com.nothatcher.sproutbook

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.test.platform.app.InstrumentationRegistry
import com.nothatcher.sproutbook.core.HomeCustomization
import com.nothatcher.sproutbook.data.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList

/** Actual native navigation and controls, using synthetic family data on a dedicated emulator. */
class HomeOrganizerFlowTest : FlowFixture() {
    private fun customization(childId: String = "qa-child") =
        runBlocking { app.db.children().get(childId)!!.homeCustomization() }

    private fun waitTag(tag: String) {
        compose.waitUntil(60000) {
            compose.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun organizer() {
        compose.onNodeWithTag("nav-more").performClick()
        waitText("Your family")
        pageClick("Home screen organizer")
        waitTag("organizer-list")
    }

    private fun editorTab(tab: String) {
        compose.onNodeWithTag("organizer-list").performScrollToNode(hasTestTag("organizer-$tab"))
        compose.onNodeWithTag("organizer-$tab").performScrollTo().performClick()
        compose.waitForIdle()
    }

    private fun editorClick(tag: String) {
        compose.onNodeWithTag("organizer-list").performScrollToNode(hasTestTag(tag))
        compose.onNodeWithTag(tag).performScrollTo().assertIsDisplayed().performClick()
    }

    private fun todayScroll(tag: String) {
        compose.onNodeWithTag("today-list").performScrollToNode(hasTestTag(tag))
        compose.onNodeWithTag(tag).performScrollTo().assertIsDisplayed()
    }

    private fun savedFeedback() {
        // Observe the feedback before waiting for its dismissal: the database may finish first.
        waitText("Layout saved")
        compose.waitUntil(60000) {
            compose.onAllNodesWithText("Layout saved").fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun savesSectionOrderHiddenCardsQuickActionsAndBackgroundAndOpensRealFeatures() {
        runBlocking {
            app.repository.saveAppointment(
                Appointment(id = "organizer-appointment", childId = "qa-child", title = "Family visit",
                    startsAt = System.currentTimeMillis() + 86400000L)
            )
        }
        val original = customization()
        organizer()
        editorClick("section-chapter-down")
        editorClick("section-sleep-toggle")
        editorTab("quick-actions")
        editorClick("quick-wishlist-toggle")
        editorClick("quick-wishlist-up")
        editorClick("quick-wishlist-up")
        editorClick("quick-schedule-toggle")
        editorTab("background")
        editorClick("background-meadow")
        editorClick("organizer-save")
        val expectedOrder = original.sectionOrder.toMutableList().apply {
            val first = this[0]
            this[0] = this[1]
            this[1] = first
        }
        awaitDb {
            app.db.children().get("qa-child")!!.homeCustomization().let {
                it.sectionOrder == expectedOrder && "sleep" in it.hiddenSections &&
                    it.quickActions == listOf("wishlist", "memory") && it.background == "meadow"
            }
        }
        savedFeedback()
        compose.onNodeWithTag("nav-today").performClick()
        waitTag("today-section-up_next")
        compose.onNodeWithTag("today-list").performScrollToIndex(0)
        val upNext = compose.onNodeWithTag("today-section-up_next").fetchSemanticsNode().boundsInRoot
        val chapter = compose.onNodeWithTag("today-section-chapter").fetchSemanticsNode().boundsInRoot
        assertTrue("The appointment must be physically above the greeting", upNext.top < chapter.top)
        compose.onNodeWithTag("today-section-sleep").assertDoesNotExist()
        todayScroll("today-section-quick_actions")
        compose.onNodeWithTag("today-quick-schedule").assertDoesNotExist()
        val wishlist = compose.onNodeWithTag("today-quick-wishlist").fetchSemanticsNode().boundsInRoot
        val memory = compose.onNodeWithTag("today-quick-memory").fetchSemanticsNode().boundsInRoot
        assertTrue("Shortcut order must match the saved selection",
            wishlist.top < memory.top || (wishlist.top == memory.top && wishlist.left < memory.left))
        compose.onNodeWithTag("today-quick-wishlist").performClick()
        waitText("Willow's wishlist")
        compose.onNodeWithTag("nav-today").performClick()
        todayScroll("today-section-quick_actions")
        compose.onNodeWithTag("today-quick-memory").performClick()
        waitText("A new leaf")
        compose.onNodeWithText("Memory title").performTextInput("A first woodland picnic")
        compose.onNodeWithText("Save memory").performScrollTo().performClick()
        awaitDb { app.db.memorys().forChild("qa-child").singleOrNull()?.title == "A first woodland picnic" }
        compose.waitUntil(60000) {
            compose.onAllNodesWithText("A new leaf").fetchSemanticsNodes().isEmpty()
        }
        // A fresh shortcut visit opens one editor; dismissing it consumes that request.
        compose.onNodeWithTag("nav-today").performClick()
        todayScroll("today-section-quick_actions")
        compose.onNodeWithTag("today-quick-memory").performClick()
        waitText("A new leaf")
        back()
        compose.waitUntil(60000) {
            compose.onAllNodesWithText("A new leaf").fetchSemanticsNodes().isEmpty()
        }
        runBlocking { app.db.children().save(Child(id = "memory-ash", name = "Ash", stage = "TEEN")) }
        compose.onNodeWithContentDescription("Switch child").performClick()
        compose.onNodeWithText("Ash").performClick()
        waitText("0 moments in Ash's story")
        compose.onNodeWithText("A new leaf").assertDoesNotExist()
        compose.onNodeWithContentDescription("Switch child").performClick()
        compose.onNodeWithText("Willow").performClick()
        waitText("1 moments in Willow's story")
        compose.onNodeWithText("A new leaf").assertDoesNotExist()
        assertEquals(1, runBlocking { app.db.appointments().all().size })
        assertEquals(1, runBlocking { app.db.memorys().all().size })
        assertEquals(expectedOrder, customization().sectionOrder)
    }

    @Test
    fun previewAndCancelKeepTheSavedLayoutAndRecordsUntouched() {
        runBlocking {
            app.repository.saveMemory(Memory(id = "preview-memory", childId = "qa-child",
                title = "A saved family moment", occurredOn = 20600, anchor = 7))
            app.repository.arrangeMemory("qa-child", "preview-memory", 7)
        }
        val before = customization()
        organizer()
        editorClick("section-sleep-toggle")
        editorTab("background")
        editorClick("background-evening")
        compose.onNodeWithTag("organizer-list").performScrollToNode(hasTestTag("organizer-preview"))
        compose.onNodeWithTag("organizer-preview").performScrollTo()
        val formerSavePosition = compose.onNodeWithTag("organizer-save").fetchSemanticsNode().boundsInRoot.center
        compose.onNodeWithTag("organizer-preview").performClick()
        waitTag("organizer-preview-back")
        // Real pointer events must not pass through the preview into the retained editor.
        compose.onRoot().performTouchInput {
            click(Offset(width.toFloat() - 12f, 110f))
            click(formerSavePosition)
        }
        compose.onNodeWithTag("organizer-preview-back").assertExists()
        assertEquals("Preview gaps must not trigger the underlying Save button", before, customization())
        compose.onNodeWithTag("today-section-sleep").assertDoesNotExist()
        todayScroll("today-section-help")
        compose.onNode(hasClickAction() and hasAnyAncestor(hasTestTag("today-section-help")))
            .assertIsNotEnabled()
        todayScroll("today-section-quick_actions")
        compose.onNodeWithTag("today-quick-memory").assertIsNotEnabled()
        compose.onNodeWithTag("today-quick-schedule").assertIsNotEnabled()
        todayScroll("today-section-memory_tree")
        compose.onNodeWithContentDescription("Memory tree, 1 leaves. A timeline is also available.")
            .assertIsNotEnabled()
        assertEquals(before, customization())
        assertEquals(7, runBlocking { app.db.memorys().get("preview-memory")!!.anchor })
        compose.onNodeWithTag("organizer-preview-back").performClick()
        waitTag("organizer-list")
        editorClick("organizer-cancel")
        compose.onNodeWithTag("nav-today").performClick()
        todayScroll("today-section-sleep")
        assertEquals(before, customization())
        assertEquals(1, runBlocking { app.db.memorys().all().size })
        assertEquals(7, runBlocking { app.db.memorys().get("preview-memory")!!.anchor })
    }

    @Test
    fun restoreDefaultsNeedsConfirmationAndSaveAndKeepsFamilyRecords() {
        val custom = HomeCustomization().copy(hiddenSections = setOf("sleep", "diapers"),
            quickActions = listOf("wishlist"), background = "evening")
        runBlocking {
            app.repository.saveHomeCustomization("qa-child", custom)
            app.repository.saveFeed(Feed(id = "organizer-feed", childId = "qa-child", kind = "Bottle",
                startsAt = System.currentTimeMillis(), amountMl = 90.0))
        }
        organizer()
        editorClick("organizer-restore")
        waitTag("organizer-restore-keep")
        compose.onNodeWithTag("organizer-restore-keep").performClick()
        assertEquals(custom, customization())
        editorClick("organizer-restore")
        compose.onNodeWithTag("organizer-restore-confirm").performClick()
        assertEquals("Restoration remains a draft until Save", custom, customization())
        editorClick("organizer-save")
        awaitDb { app.db.children().get("qa-child")!!.homeCustomization() == HomeCustomization() }
        savedFeedback()
        assertEquals(90.0, runBlocking { app.db.feeds().get("organizer-feed")!!.amountMl }, 0.0)
        compose.onNodeWithTag("nav-today").performClick()
        todayScroll("today-section-sleep")
        compose.onNodeWithTag("today-section-sleep").assertExists()
    }

    @Test
    fun childSwitchDiscardsDraftAndSavedLayoutsSurviveActivityRecreation() {
        val ashLayout = HomeCustomization().copy(hiddenSections = setOf("chapter"),
            quickActions = listOf("wishlist"), background = "morning")
        runBlocking {
            app.db.children().save(Child(id = "organizer-ash", name = "Ash", stage = "TEEN")
                .withHomeCustomization(ashLayout))
            app.repository.saveMemory(Memory(id = "willow-leaf", childId = "qa-child",
                title = "Willow's own memory", occurredOn = 20600, anchor = 4))
            app.repository.arrangeMemory("qa-child", "willow-leaf", 4)
        }
        val willowLayout = customization()
        organizer()
        editorClick("section-chapter-toggle")
        editorTab("background")
        editorClick("background-meadow")
        compose.onNodeWithContentDescription("Switch child").performClick()
        compose.onNodeWithText("Ash").performClick()
        compose.onNodeWithTag("organizer-list").performScrollToIndex(0)
        waitText("Ash's Today page")
        editorTab("sections")
        editorClick("section-chapter-toggle")
        editorTab("background")
        editorClick("background-evening")
        editorClick("organizer-save")
        awaitDb {
            app.db.children().get("organizer-ash")!!.homeCustomization().let {
                it.background == "evening" && it.hiddenSections.isEmpty()
            }
        }
        savedFeedback()
        assertEquals("Willow's discarded draft must stay unsaved", willowLayout, customization())
        val savedAsh = customization("organizer-ash")
        assertEquals("Ash must start with Ash's saved shortcuts, not Willow's discarded draft",
            ashLayout.copy(hiddenSections = emptySet(), background = "evening"), savedAsh)
        compose.activityRule.scenario.recreate()
        waitTag("nav-today")
        compose.onNodeWithTag("nav-today").performClick()
        waitText("YOUR TEEN CHAPTER")
        todayScroll("today-section-quick_actions")
        compose.onNodeWithTag("today-quick-wishlist").assertIsEnabled()
        assertEquals(savedAsh, customization("organizer-ash"))
        compose.onNodeWithContentDescription("Switch child").performClick()
        compose.onNodeWithText("Willow").performClick()
        waitText("YOUR BABY CHAPTER")
        assertEquals(willowLayout, customization())
        assertEquals(4, runBlocking { app.db.memorys().get("willow-leaf")!!.anchor })
    }

    @Test
    fun caregiverCanPreviewButCannotChangeLayoutOrQuickActions() {
        val before = customization()
        runBlocking { app.settings.boolean("grandparent", true) }
        waitText("Grandparent mode · Read only")
        organizer()
        editorClick("organizer-preview")
        waitTag("organizer-preview-back")
        todayScroll("today-section-quick_actions")
        compose.onNodeWithTag("today-quick-memory").assertIsNotEnabled()
        compose.onNodeWithTag("organizer-preview-back").performClick()
        compose.onNodeWithTag("organizer-list").performScrollToNode(hasTestTag("section-chapter-toggle"))
        compose.onNodeWithTag("section-chapter-toggle").assertIsNotEnabled()
        compose.onNodeWithTag("section-chapter-down").assertIsNotEnabled()
        editorTab("quick-actions")
        compose.onNodeWithTag("organizer-list").performScrollToNode(hasTestTag("quick-memory-toggle"))
        compose.onNodeWithTag("quick-memory-toggle").assertIsNotEnabled()
        compose.onNodeWithTag("quick-memory-down").assertIsNotEnabled()
        editorTab("background")
        compose.onNodeWithTag("organizer-list").performScrollToNode(hasTestTag("background-meadow"))
        compose.onNodeWithTag("background-meadow").assertIsNotEnabled()
        compose.onNodeWithTag("organizer-list").performScrollToNode(hasTestTag("organizer-save"))
        compose.onNodeWithTag("organizer-save").assertIsNotEnabled()
        compose.onNodeWithTag("organizer-restore").assertIsNotEnabled()
        editorClick("organizer-cancel")
        compose.onNodeWithTag("nav-today").performClick()
        todayScroll("today-section-quick_actions")
        compose.onNodeWithTag("today-quick-memory").assertIsNotEnabled()
        compose.onNodeWithTag("today-quick-schedule").assertIsEnabled()
        assertEquals(before, customization())
        assertTrue(runBlocking { app.db.memorys().all().isEmpty() })
    }

    @Test
    fun emptyHomeKeepsOrganizerReachableAndOrdinaryTreeScrollingDoesNotEdit() {
        val noCards = HomeCustomization().let {
            it.copy(hiddenSections = it.sectionOrder.toSet(), quickActions = emptyList(), background = "plain")
        }
        runBlocking {
            app.repository.saveHomeCustomization("qa-child", noCards)
            app.repository.saveMemory(Memory(id = "scroll-memory", childId = "qa-child",
                title = "An anchored memory", occurredOn = 20600, anchor = 11))
            app.repository.arrangeMemory("qa-child", "scroll-memory", 11)
        }
        waitTag("today-organizer-empty")
        compose.onNodeWithTag("today-organizer-empty").performClick()
        waitTag("organizer-list")
        editorClick("section-memory_tree-toggle")
        editorClick("organizer-save")
        awaitDb { "memory_tree" !in app.db.children().get("qa-child")!!.homeCustomization().hiddenSections }
        savedFeedback()
        compose.onNodeWithTag("nav-today").performClick()
        todayScroll("today-section-memory_tree")
        val saved = customization()
        compose.onNodeWithContentDescription("Memory tree, 1 leaves. A timeline is also available.")
            .performTouchInput { swipeUp(durationMillis = 450) }
        compose.onNodeWithTag("today-list").performTouchInput { swipeDown(durationMillis = 450) }
        compose.waitForIdle()
        compose.onNodeWithText("A moment to keep").assertDoesNotExist()
        compose.onNodeWithText("A new leaf").assertDoesNotExist()
        assertEquals(saved, customization())
        assertEquals(11, runBlocking { app.db.memorys().get("scroll-memory")!!.anchor })
        assertEquals(1, runBlocking { app.db.memorys().all().size })
    }

    @Test
    fun stageAwareCardsChangeWithoutResettingLayoutOrPastRecords() {
        val config = HomeCustomization().let {
            it.copy(sectionOrder = listOf("feeding", "pregnancy") +
                it.sectionOrder.filter { id -> id != "feeding" && id != "pregnancy" },
                quickActions = listOf("feeding", "memory"), background = "morning")
        }
        runBlocking {
            app.repository.saveHomeCustomization("qa-child", config)
            app.repository.saveFeed(Feed(id = "stage-feed", childId = "qa-child", kind = "Bottle",
                startsAt = System.currentTimeMillis(), amountMl = 75.0))
            app.repository.saveMemory(Memory(id = "stage-memory", childId = "qa-child",
                title = "Every chapter stays", occurredOn = 20600, anchor = 13))
            app.repository.arrangeMemory("qa-child", "stage-memory", 13)
        }
        todayScroll("today-section-feeding")
        compose.onNodeWithTag("today-section-pregnancy").assertDoesNotExist()
        runBlocking {
            app.repository.saveChild(app.db.children().get("qa-child")!!.copy(stage = "PREGNANCY"))
        }
        waitText("YOUR PREGNANCY CHAPTER")
        todayScroll("today-section-pregnancy")
        compose.onNodeWithTag("today-section-feeding").assertDoesNotExist()
        assertEquals(config, customization())
        runBlocking {
            app.repository.saveChild(app.db.children().get("qa-child")!!.copy(stage = "TODDLER"))
        }
        waitText("YOUR TODDLER CHAPTER")
        todayScroll("today-section-feeding")
        compose.onNodeWithTag("today-section-pregnancy").assertDoesNotExist()
        todayScroll("today-section-potty")
        assertEquals(config, customization())
        assertEquals(75.0, runBlocking { app.db.feeds().get("stage-feed")!!.amountMl }, 0.0)
        assertEquals(13, runBlocking { app.db.memorys().get("stage-memory")!!.anchor })
    }

    @Test
    fun photoPickerResultStaysADraftUntilSaveAndItsPrivateCopySurvivesRecreationAndChildSwitch() {
        val source = File(app.cacheDir, "organizer-picker-source.jpg")
        val bitmap = BitmapFactory.decodeResource(app.resources, R.drawable.woodland_scene_care)
        assertNotNull(bitmap)
        source.outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.JPEG, 88, it)) }
        bitmap.recycle()
        val before = customization()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val interceptedActions = CopyOnWriteArrayList<String>()
        val pickerActions = setOf(
            Intent.ACTION_OPEN_DOCUMENT,
            "android.provider.action.PICK_IMAGES",
            "androidx.activity.result.contract.action.PICK_IMAGES",
            "com.google.android.gms.provider.action.PICK_IMAGES",
        )
        // Simulate only the native picker ActivityResult. This does not test external picker UI.
        val monitor = object : Instrumentation.ActivityMonitor() {
            override fun onStartActivity(intent: Intent): Instrumentation.ActivityResult? {
                if (intent.action !in pickerActions) return null
                interceptedActions += intent.action!!
                return Instrumentation.ActivityResult(Activity.RESULT_OK, Intent().apply {
                    data = Uri.fromFile(source)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                })
            }
        }
        fun privatePhotos(): Set<String> = File(app.filesDir, "photos").listFiles()
            ?.filter { it.isFile }?.map { it.name }?.toSet() ?: emptySet()
        instrumentation.addMonitor(monitor)
        try {
            organizer()
            editorTab("background")
            val originalPhotos = privatePhotos()
            editorClick("organizer-photo-import")
            waitText("Photo ready to preview. Save layout to keep it.")
            val firstCopied = privatePhotos() - originalPhotos
            assertEquals(1, firstCopied.size)
            assertFalse("A picker URI must be copied to a fresh private filename", source.name in firstCopied)
            assertEquals("Importing a photo must not save the background", before, customization())
            editorClick("organizer-preview")
            waitTag("today-background-photo")
            assertEquals(before, customization())
            compose.onNodeWithTag("organizer-preview-back").performClick()
            editorClick("organizer-cancel")
            assertEquals("Cancel must keep the original background", before, customization())

            organizer()
            editorTab("background")
            val photosBeforeSave = privatePhotos()
            editorClick("organizer-photo-import")
            // The first response may still have an identical snackbar. Await the copy as well.
            compose.waitUntil(60000) { (privatePhotos() - photosBeforeSave).size == 1 }
            waitText("Photo ready to preview. Save layout to keep it.")
            val secondCopied = (privatePhotos() - photosBeforeSave).single()
            compose.onNodeWithTag("organizer-list").performScrollToNode(hasTestTag("organizer-save"))
            compose.waitUntil(60000) {
                compose.onAllNodesWithTag("organizer-save").fetchSemanticsNodes().isNotEmpty() &&
                    runCatching { compose.onNodeWithTag("organizer-save").assertIsEnabled() }.isSuccess
            }
            editorClick("organizer-save")
            awaitDb {
                app.db.children().get("qa-child")!!.homeCustomization().let {
                    it.background == "photo" && it.backgroundPhoto == secondCopied
                }
            }
            savedFeedback()
            assertEquals(2, interceptedActions.size)
            assertTrue(interceptedActions.all { it in pickerActions })
            assertTrue(File(File(app.filesDir, "photos"), secondCopied).isFile)
            assertTrue("The original picker image can be removed after the private copy is saved", source.delete())
            compose.onNodeWithTag("nav-today").performClick()
            waitTag("today-background-photo")
            compose.activityRule.scenario.recreate()
            waitTag("today-background-photo")
            assertEquals(secondCopied, customization().backgroundPhoto)
            runBlocking { app.db.children().save(Child(id = "photo-ash", name = "Ash", stage = "TEEN")) }
            compose.onNodeWithContentDescription("Switch child").performClick()
            compose.onNodeWithText("Ash").performClick()
            waitText("YOUR TEEN CHAPTER")
            compose.onNodeWithTag("today-background-photo").assertDoesNotExist()
            assertEquals(HomeCustomization(), customization("photo-ash"))
            assertEquals(secondCopied, customization().backgroundPhoto)
            assertTrue(runBlocking { app.db.memorys().all().isEmpty() })
        } finally {
            instrumentation.removeMonitor(monitor)
            source.delete()
        }
    }
}
