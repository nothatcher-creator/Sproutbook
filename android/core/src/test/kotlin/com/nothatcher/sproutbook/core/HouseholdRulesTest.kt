package com.nothatcher.sproutbook.core

import org.junit.Assert.*
import org.junit.Test

class HouseholdRulesTest {
    @Test
    fun stockNeverGoesBelowZero() {
        assertEquals(0.0, HouseholdRules.quantity(.5, -1.0), 0.0)
    }

    @Test
    fun invalidNumbersRejected() {
        assertFalse(HouseholdRules.validStock(Double.NaN, 2.0))
        assertFalse(HouseholdRules.validStock(2.0, -1.0))
        assertTrue(HouseholdRules.validStock(0.0, 2.0))
    }

    @Test
    fun phoneIsDialableOnly() {
        assertEquals("+15551234567", HouseholdRules.phone("+1 (555) 123-4567"))
        assertEquals("", HouseholdRules.phone("hello"))
    }
}
