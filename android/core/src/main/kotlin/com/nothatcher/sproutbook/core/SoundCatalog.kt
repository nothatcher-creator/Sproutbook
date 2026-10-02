package com.nothatcher.sproutbook.core

object SoundCatalog {
    val kinds = listOf("White", "Brown", "Fan", "Rain", "Ocean", "Hush", "Stream", "Breeze")
    val timers = listOf(15, 30, 60, 90, 0)
    fun description(kind: String) = when (kind) {
        "White" -> "Soft, steady static"
        "Brown" -> "A deeper, gentle rumble"
        "Fan" -> "A familiar, even hum"
        "Rain" -> "Fine rainfall texture"
        "Ocean" -> "Slow waves of sound"
        "Hush" -> "A soft rhythmic shush"
        "Stream" -> "A light, flowing murmur"
        else -> "A low, airy rustle"
    }
}

/** Monotonic milliseconds only. Paused timers spend no time; zero means continuous. */
data class SoundTimer(val remainingMs: Long? = null, val deadlineMs: Long? = null) {
    fun remaining(now: Long): Long? = deadlineMs?.let { (it - now).coerceAtLeast(0) } ?: remainingMs
    fun pause(now: Long) = SoundTimer(remaining(now))
    fun resume(now: Long) = copy(deadlineMs = remainingMs?.let { now + it })
    companion object {
        fun start(minutes: Int, now: Long): SoundTimer {
            require(minutes in SoundCatalog.timers)
            return SoundTimer(if (minutes == 0) null else minutes * 60000L).resume(now)
        }
    }
}
