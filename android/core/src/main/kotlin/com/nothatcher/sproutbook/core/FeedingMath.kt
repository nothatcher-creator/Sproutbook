package com.nothatcher.sproutbook.core
object FeedingMath {
    private const val ML_PER_US_OUNCE = 29.5735295625

    fun ml(ounces: Double): Double = ounces * ML_PER_US_OUNCE

    fun ounces(ml: Double): Double = ml / ML_PER_US_OUNCE

    fun kg(pounds: Double): Double = pounds * 0.45359237

    fun estimate(weightKg: Double, feeds: Int): Pair<Double, Double> {
        require(weightKg.isFinite() && weightKg in 2.0..15.0) {
            "Use your care team's plan for this weight."
        }
        require(feeds in 1..20) { "Enter between 1 and 20 feeds." }
        return weightKg * 150 to weightKg * 200
    }
}
