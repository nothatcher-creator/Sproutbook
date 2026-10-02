package com.nothatcher.sproutbook.core
object PregnancyRules {
    fun gestationDays(dueDay: Long, today: Long) = (280 - (dueDay - today)).coerceIn(0, 350).toInt()

    fun intervals(starts: List<Long>) = starts.sorted().zipWithNext { a, b -> b - a }
}
