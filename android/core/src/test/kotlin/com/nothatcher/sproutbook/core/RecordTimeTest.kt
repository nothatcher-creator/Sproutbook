package com.nothatcher.sproutbook.core

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class RecordTimeTest {
    private val zone = ZoneId.of("America/New_York")
    private val date = LocalDate.of(2025, 11, 2)
    private val repeatedTime = LocalTime.of(1, 30, 0, 125_000_000)

    @Test
    fun unchangedLaterRepeatedHourKeepsOriginalInstant() {
        assertEquals(1762065000125L, RecordTime.edited(1762065000125L, date, repeatedTime, zone))
    }

    @Test
    fun unchangedEarlierRepeatedHourKeepsOriginalInstant() {
        assertEquals(1762061400125L, RecordTime.edited(1762061400125L, date, repeatedTime, zone))
    }

    @Test
    fun changedClockUsesEditedLocalTime() {
        assertEquals(
            1762066800000L,
            RecordTime.edited(1762065000125L, date, LocalTime.of(2, 0), zone),
        )
    }

    @Test
    fun changedDateUsesEditedDateAndRetainsFractionalTime() {
        assertEquals(
            1762151400125L,
            RecordTime.edited(1762065000125L, LocalDate.of(2025, 11, 3), repeatedTime, zone),
        )
    }
}
