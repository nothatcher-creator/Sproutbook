package com.nothatcher.sproutbook.core
object HouseholdRules {
    fun quantity(value: Double, delta: Double): Double {
        require(value.isFinite() && delta.isFinite())
        return (value + delta).coerceIn(0.0, 1000000.0)
    }

    fun validStock(quantity: Double, threshold: Double) =
        quantity.isFinite() &&
            threshold.isFinite() &&
            quantity in 0.0..1000000.0 &&
            threshold in 0.0..1000000.0

    fun phone(raw: String): String {
        val digits = raw.filter { it in '0'..'9' }
        return if (digits.isEmpty()) "" else (if (raw.trim().startsWith("+")) "+" else "") + digits
    }
}
