package com.nothatcher.sproutbook.core

import java.time.LocalDate

object ProfileRules {
    fun validName(name: String) = name.trim().length in 1..80

    fun validBirthday(date: LocalDate, today: LocalDate) = date <= today && date.year >= 1900

    fun selected(id: String?, ids: List<String>) = id?.takeIf { it in ids } ?: ids.firstOrNull()
}
