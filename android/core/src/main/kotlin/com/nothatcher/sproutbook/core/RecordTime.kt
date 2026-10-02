package com.nothatcher.sproutbook.core

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

object RecordTime {
    /** Notes-only edits must keep the exact instant, including its DST overlap offset. */
    fun edited(original: Long, date: LocalDate, time: LocalTime, zone: ZoneId): Long {
        val initial = Instant.ofEpochMilli(original).atZone(zone)
        return if (date == initial.toLocalDate() && time == initial.toLocalTime()) original
        else date.atTime(time).atZone(zone).toInstant().toEpochMilli()
    }
}
