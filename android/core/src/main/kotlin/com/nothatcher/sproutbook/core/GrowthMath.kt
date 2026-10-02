package com.nothatcher.sproutbook.core

/** Recorded measurement conversions, with kg/cm as canonical units; no clinical interpretation. */
object GrowthMath {
    fun canonical(kind: String, value: Double, unit: String): Double? {
        if (!value.isFinite() || value <= 0) return null
        return when (kind to unit.lowercase().trim()) {
            "Weight" to "kg" -> value
            "Weight" to "g" -> value / 1000
            "Weight" to "lb",
            "Weight" to "lbs" -> value * 0.45359237
            "Height" to "cm" -> value
            "Height" to "m" -> value * 100
            "Height" to "mm" -> value / 10
            "Height" to "in",
            "Height" to "inches" -> value * 2.54
            else -> null
        }
    }

    fun display(kind: String, canonical: Double, unit: String): Double =
        when (kind to unit) {
            "Weight" to "lb" -> canonical / 0.45359237
            "Height" to "in" -> canonical / 2.54
            else -> canonical
        }
}
