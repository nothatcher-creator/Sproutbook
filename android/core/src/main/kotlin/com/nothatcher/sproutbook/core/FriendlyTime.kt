package com.nothatcher.sproutbook.core

import java.time.LocalDate
import java.time.Period

/** Short, glanceable wording for busy parents; exact timestamps stay in the history rows. */
object FriendlyTime {
    fun ago(thenMillis: Long, nowMillis: Long): String {
        val minutes = ((nowMillis - thenMillis) / 60_000).coerceAtLeast(0)
        return when {
            minutes < 1 -> "just now"
            minutes < 60 -> "$minutes min ago"
            minutes < 24 * 60 -> {
                val rest = minutes % 60
                if (rest == 0L) "${minutes / 60} h ago" else "${minutes / 60} h $rest min ago"
            }
            minutes < 48 * 60 -> "yesterday"
            else -> "${minutes / (24 * 60)} days ago"
        }
    }

    /** "3 weeks", "7 months", "2 years" — or null before birth. */
    fun age(birthday: LocalDate, today: LocalDate): String? {
        if (birthday > today) return null
        val period = Period.between(birthday, today)
        val months = period.toTotalMonths()
        fun plural(n: Long, unit: String) = "$n $unit" + if (n == 1L) "" else "s"
        return when {
            months < 1 -> {
                val days = today.toEpochDay() - birthday.toEpochDay()
                if (days < 14) (if (days == 0L) "Born today" else plural(days, "day")) else plural(days / 7, "week")
            }
            months < 24 -> plural(months, "month")
            else -> plural(period.years.toLong(), "year")
        }
    }

    fun greeting(hour: Int) = when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Quiet hours"
    }
}
