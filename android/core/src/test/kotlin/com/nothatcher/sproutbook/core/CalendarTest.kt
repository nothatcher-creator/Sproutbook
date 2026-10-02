package com.nothatcher.sproutbook.core

import java.time.*
import org.junit.Assert.*
import org.junit.Test

class CalendarTest {
    @Test
    fun monthGridStartsMonday() {
        val days = CalendarRules.monthDays(YearMonth.of(2026, 9))
        assertEquals(42, days.size)
        assertEquals(LocalDate.of(2026, 8, 31), days.first())
    }

    @Test
    fun leapBirthdayUsesFebruary28InOrdinaryYear() {
        assertEquals(
            LocalDate.of(2027, 2, 28),
            CalendarRules.birthday(LocalDate.of(2024, 2, 29), 2027),
        )
    }

    @Test
    fun noBirthdayBeforeBirth() {
        assertNull(CalendarRules.birthday(LocalDate.of(2025, 3, 2), 2024))
    }

    @Test
    fun weekCrossesYearBoundary() {
        assertEquals(LocalDate.of(2025, 12, 29), CalendarRules.monday(LocalDate.of(2026, 1, 2)))
    }
}
