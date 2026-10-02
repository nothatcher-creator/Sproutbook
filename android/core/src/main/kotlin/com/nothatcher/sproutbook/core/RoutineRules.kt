package com.nothatcher.sproutbook.core

import java.time.LocalDate

object RoutineRules {
    fun valid(weekdays: Int, timeMinutes: Int) = weekdays in 1..127 && timeMinutes in -1..1439

    fun isDue(weekdays: Int, day: LocalDate) =
        weekdays in 1..127 && weekdays and (1 shl (day.dayOfWeek.value - 1)) != 0
}
