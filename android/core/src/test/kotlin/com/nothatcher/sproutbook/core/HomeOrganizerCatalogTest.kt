package com.nothatcher.sproutbook.core

import org.junit.Assert.*
import org.junit.Test

class HomeOrganizerCatalogTest {
    private fun decode(
        sections: String = "",
        hidden: String = "",
        actions: String = "memory,schedule",
        background: String = "woodland",
        photo: String? = null,
    ) = HomeOrganizerCatalog.decode(sections, hidden, actions, background, photo)

    private fun rejected(block: () -> Unit) {
        try {
            block()
            fail("Invalid organizer configuration accepted")
        } catch (_: IllegalArgumentException) {}
    }

    @Test
    fun unchangedChildDefaultsRetainTheWholeTodayAndOriginalShortcuts() {
        val actual = decode()
        assertEquals(HomeCustomization(), actual)
        assertEquals("chapter", actual.sectionOrder.first())
        assertEquals("caregiver_notes", actual.sectionOrder.last())
        assertEquals(21, actual.sectionOrder.size)
        assertTrue(actual.hiddenSections.isEmpty())
        assertEquals(listOf("memory", "schedule"), actual.quickActions)
        assertEquals("woodland", actual.background)
        assertNull(actual.backgroundPhoto)
    }

    @Test
    fun partialStoredOrderKeepsChosenOrderAndAppendsNewSections() {
        val actual = decode("memory_tree,growth,chapter", "feeding,diapers", "wishlist,memory")
        assertEquals(listOf("memory_tree", "growth", "chapter"), actual.sectionOrder.take(3))
        assertEquals(
            HomeOrganizerCatalog.sectionIds.filterNot { it in actual.sectionOrder.take(3) },
            actual.sectionOrder.drop(3),
        )
        assertEquals(setOf("feeding", "diapers"), actual.hiddenSections)
        assertEquals(listOf("wishlist", "memory"), actual.quickActions)
        HomeOrganizerCatalog.validate(actual)
    }

    @Test
    fun everySectionCanBeHiddenAndQuickActionsCanBeEmpty() {
        val actual = decode(hidden = HomeOrganizerCatalog.sectionIds.joinToString(","), actions = "")
        assertEquals(HomeOrganizerCatalog.sectionIds.toSet(), actual.hiddenSections)
        assertTrue(actual.quickActions.isEmpty())
    }

    @Test
    fun rejectsUnknownDuplicateAndMalformedCsvWithoutSilentlyDroppingChoices() {
        for (sections in listOf("missing", "chapter,chapter", "chapter,", ",chapter", " chapter", " ")) {
            rejected { decode(sections = sections) }
        }
        for (hidden in listOf("chapter,chapter", "unknown", "chapter,,growth")) {
            rejected { decode(hidden = hidden) }
        }
        for (actions in listOf("memory,memory", "unknown", "schedule,", " schedule")) {
            rejected { decode(actions = actions) }
        }
        rejected { decode(sections = "a".repeat(2049)) }
        rejected { decode(hidden = "a".repeat(2049)) }
        rejected { decode(actions = "a".repeat(2049)) }
    }

    @Test
    fun configurationMustContainEverySectionOnceAndOnlyKnownChoices() {
        rejected { HomeOrganizerCatalog.validate(HomeCustomization(sectionOrder = listOf("chapter"))) }
        rejected { HomeOrganizerCatalog.validate(HomeCustomization(sectionOrder = HomeOrganizerCatalog.sectionIds + "chapter")) }
        rejected { HomeOrganizerCatalog.validate(HomeCustomization(hiddenSections = setOf("unknown"))) }
        rejected { HomeOrganizerCatalog.validate(HomeCustomization(quickActions = listOf("memory", "memory"))) }
        rejected { HomeOrganizerCatalog.validate(HomeCustomization(quickActions = listOf("unknown"))) }
    }

    @Test
    fun presetBackgroundsAndAppPrivatePhotoNamesAreValidated() {
        for (background in listOf("woodland", "morning", "meadow", "evening", "plain")) {
            assertEquals(background, decode(background = background).background)
        }
        val photo = decode(background = "photo", photo = "f2b9_-test.jpg")
        assertEquals("f2b9_-test.jpg", photo.backgroundPhoto)
        rejected { decode(background = "unknown") }
        rejected { decode(background = "photo") }
        for (name in listOf("", "../x.jpg", "/x.jpg", "x.png", "x.JPG", "x.jpg/other", "a".repeat(157) + ".jpg")) {
            rejected { decode(background = "photo", photo = name) }
        }
        rejected { decode(background = "plain", photo = "x.jpg") }
    }

    @Test
    fun accessibleMovesAreBoundedAndReversibleWithoutChangingMembership() {
        val initial = HomeOrganizerCatalog.sectionIds
        val up = HomeOrganizerCatalog.move(initial, "memory_tree", -1)
        assertEquals(initial.indexOf("memory_tree") - 1, up.indexOf("memory_tree"))
        assertEquals(initial, HomeOrganizerCatalog.move(up, "memory_tree", 1))
        assertEquals(initial, HomeOrganizerCatalog.move(initial, initial.first(), -1))
        assertEquals(initial, HomeOrganizerCatalog.move(initial, initial.last(), 1))
        assertEquals(initial, HomeOrganizerCatalog.move(initial, "unknown", 1))
        assertEquals(listOf("memory", "schedule"), HomeOrganizerCatalog.move(listOf("schedule", "memory"), "memory", -1))
        rejected { HomeOrganizerCatalog.move(initial, "chapter", 2) }
    }

    @Test
    fun catalogIdentitiesAreUniqueAndRoutesAreRealNativeFeatureDestinations() {
        assertEquals(HomeOrganizerCatalog.sectionIds.size, HomeOrganizerCatalog.sectionIds.distinct().size)
        assertEquals(23, HomeOrganizerCatalog.actionIds.size)
        assertEquals(HomeOrganizerCatalog.actionIds.size, HomeOrganizerCatalog.actionIds.distinct().size)
        assertEquals(6, HomeOrganizerCatalog.backgroundIds.size)
        assertEquals("memories?new=true", HomeOrganizerCatalog.quickActions.single { it.id == "memory" }.route)
        assertEquals("schedule", HomeOrganizerCatalog.quickActions.single { it.id == "schedule" }.route)
        assertTrue(HomeOrganizerCatalog.sections.all { it.title.isNotBlank() && it.detail.isNotBlank() })
        assertTrue(HomeOrganizerCatalog.quickActions.all { it.title.isNotBlank() && it.route.isNotBlank() })
    }
}
