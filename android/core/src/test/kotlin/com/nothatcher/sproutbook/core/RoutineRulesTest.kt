package com.nothatcher.sproutbook.core

import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class RoutineRulesTest {
    @Test
    fun weekdayMaskIsMondayFirst() {
        val monday = LocalDate.of(2026, 9, 28)
        assertTrue(RoutineRules.isDue(31, monday))
        assertFalse(RoutineRules.isDue(31, monday.plusDays(5)))
        assertTrue(RoutineRules.isDue(127, monday.plusDays(6)))
        assertFalse(RoutineRules.isDue(0, monday))
    }

    @Test
    fun validatesOptionalTimeAndWeekdays() {
        assertTrue(RoutineRules.valid(127, -1))
        assertTrue(RoutineRules.valid(1, 1439))
        assertFalse(RoutineRules.valid(0, 480))
        assertFalse(RoutineRules.valid(128, 0))
        assertFalse(RoutineRules.valid(1, 1440))
        assertFalse(RoutineRules.valid(1, -2))
    }
}
