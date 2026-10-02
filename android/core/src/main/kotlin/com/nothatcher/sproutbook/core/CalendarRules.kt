package com.nothatcher.sproutbook.core

import java.time.*
import java.time.temporal.TemporalAdjusters

object CalendarRules {
    fun monthDays(month: YearMonth): List<LocalDate> {
        val start = monday(month.atDay(1))
        return (0L..41L).map { start.plusDays(it) }
    }

    fun birthday(birth: LocalDate, year: Int): LocalDate? {
        if (year < birth.year) return null
        val month = YearMonth.of(year, birth.month)
        return month.atDay(minOf(birth.dayOfMonth, month.lengthOfMonth()))
    }

    fun monday(date: LocalDate): LocalDate =
        date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
}
