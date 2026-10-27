package com.nothatcher.sproutbook.core

import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class FriendlyTimeTest {
    private val minute = 60_000L

    @Test
    fun agoRoundsToGlanceableUnits() {
        assertEquals("just now", FriendlyTime.ago(0, 30_000))
        assertEquals("just now", FriendlyTime.ago(10 * minute, 0))
        assertEquals("12 min ago", FriendlyTime.ago(0, 12 * minute))
        assertEquals("2 h ago", FriendlyTime.ago(0, 120 * minute))
        assertEquals("2 h 5 min ago", FriendlyTime.ago(0, 125 * minute))
        assertEquals("yesterday", FriendlyTime.ago(0, 30 * 60 * minute))
        assertEquals("3 days ago", FriendlyTime.ago(0, 3 * 24 * 60 * minute))
    }

    @Test
    fun ageUsesDaysWeeksMonthsThenYears() {
        val today = LocalDate.of(2026, 10, 27)
        assertNull(FriendlyTime.age(today.plusDays(1), today))
        assertEquals("Born today", FriendlyTime.age(today, today))
        assertEquals("1 day", FriendlyTime.age(today.minusDays(1), today))
        assertEquals("3 weeks", FriendlyTime.age(today.minusDays(22), today))
        assertEquals("1 month", FriendlyTime.age(today.minusMonths(1), today))
        assertEquals("23 months", FriendlyTime.age(today.minusMonths(23), today))
        assertEquals("2 years", FriendlyTime.age(today.minusMonths(24), today))
        assertEquals("14 years", FriendlyTime.age(LocalDate.of(2012, 1, 5), today))
    }

    @Test
    fun greetingFollowsTheDay() {
        assertEquals("Good morning", FriendlyTime.greeting(7))
        assertEquals("Good afternoon", FriendlyTime.greeting(13))
        assertEquals("Good evening", FriendlyTime.greeting(19))
        assertEquals("Quiet hours", FriendlyTime.greeting(2))
    }
}
