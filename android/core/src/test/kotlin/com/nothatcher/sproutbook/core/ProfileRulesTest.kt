package com.nothatcher.sproutbook.core

import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class ProfileRulesTest {
    @Test
    fun emptyNameRejected() {
        assertFalse(ProfileRules.validName(" "))
    }

    @Test
    fun reasonableNameAccepted() {
        assertTrue(ProfileRules.validName("Rowan"))
    }

    @Test
    fun futureBirthdayRejected() {
        assertFalse(
            ProfileRules.validBirthday(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 9, 29))
        )
    }

    @Test
    fun staleSelectionRepairs() {
        assertEquals("one", ProfileRules.selected("missing", listOf("one", "two")))
    }

    @Test
    fun emptyFamilyHasNoSelection() {
        assertNull(ProfileRules.selected("missing", emptyList()))
    }
}
