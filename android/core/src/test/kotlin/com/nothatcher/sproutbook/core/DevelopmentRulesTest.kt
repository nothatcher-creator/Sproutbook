package com.nothatcher.sproutbook.core

import org.junit.Assert.*
import org.junit.Test

class DevelopmentRulesTest {
    @Test
    fun twentyTeethAndSafeDates() {
        assertTrue(DevelopmentRules.validTooth(19, "Erupted", 20, 20))
        assertFalse(DevelopmentRules.validTooth(20, "Observed", null, 20))
        assertFalse(DevelopmentRules.validTooth(1, "Erupted", 21, 20))
        assertFalse(DevelopmentRules.validTooth(1, "Observed", 20, 20))
    }

    @Test
    fun milestoneMemoryKeepsIdentity() {
        assertEquals(DevelopmentRules.memoryId("a"), DevelopmentRules.memoryId("a"))
        assertNotEquals(DevelopmentRules.memoryId("a"), DevelopmentRules.memoryId("b"))
    }
}
