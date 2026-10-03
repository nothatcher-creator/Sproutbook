package com.nothatcher.sproutbook.core

import org.junit.Assert.*
import org.junit.Test

class BirthPlanningCatalogTest {
    @Test
    fun listAndPlanIdsRemainStableBoundedAndChildScoped() {
        val child = "c".repeat(160)
        val ids = BirthPlanningCatalog.guides.flatMap { guide ->
            guide.items.map { BirthPlanningCatalog.prefix(child, guide.id) + it.key }
        } + BirthPlanCatalog.fields.map { BirthPlanCatalog.prefix(child) + it.key }
        assertEquals(ids.size, ids.distinct().size)
        assertTrue(ids.all { it.length in 1..160 })
        assertEquals(BirthPlanningCatalog.prefix(child, "home"), BirthPlanningCatalog.prefix(child, "home"))
        assertNotEquals(BirthPlanningCatalog.prefix("a", "home"), BirthPlanningCatalog.prefix("b", "home"))
        assertTrue((BirthPlanningCatalog.prefix(child, "after") + "custom-00000000-0000-0000-0000-000000000001").length <= 160)
    }

    @Test
    fun customRowsStayDeletableAndUnknownBagItemsStayVisible() {
        val guide = BirthPlanningCatalog.guides.first()
        val standard = BirthPlanningCatalog.prefix("a", guide.id) + guide.items.first().key
        val custom = BirthPlanningCatalog.prefix("a", guide.id) + "custom-one"
        assertTrue(BirthPlanningCatalog.isBuiltIn("a", standard))
        assertFalse(BirthPlanningCatalog.isBuiltIn("a", custom))
        assertEquals(guide, BirthPlanningCatalog.guideForItem("a", custom))
        assertNull(BirthPlanningCatalog.guideForItem("b", standard))
        assertNull(BirthPlanningCatalog.guideForItem("a", "a-bag-0"))
        assertNull(BirthPlanningCatalog.guideForItem("a", "future-unknown-guide"))
        assertNull(BirthPlanningCatalog.guideForItem("a", BirthPlanCatalog.prefix("a") + "maternity-phone"))
    }

    @Test
    fun phoneEntriesAllowLocalFormatsAndRejectNotesOrDialCommands() {
        assertTrue(BirthPlanCatalog.validPhone(""))
        assertTrue(BirthPlanCatalog.validPhone("999"))
        assertTrue(BirthPlanCatalog.validPhone("15"))
        assertTrue(BirthPlanCatalog.validPhone("+1 (555) 123-4567"))
        assertFalse(BirthPlanCatalog.validPhone("Call the office"))
        assertFalse(BirthPlanCatalog.validPhone("*123#"))
        assertFalse(BirthPlanCatalog.validPhone("1"))
        assertFalse(BirthPlanCatalog.validPhone("1".repeat(41)))
    }
}
