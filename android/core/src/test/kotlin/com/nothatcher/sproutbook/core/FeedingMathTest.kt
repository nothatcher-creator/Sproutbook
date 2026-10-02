package com.nothatcher.sproutbook.core

import org.junit.Assert.*
import org.junit.Test

class FeedingMathTest {
    @Test
    fun ounceConversionIsExact() {
        assertEquals(29.5735295625, FeedingMath.ml(1.0), 0.00000001)
    }

    @Test
    fun unitRoundTripPreservesAmount() {
        assertEquals(123.0, FeedingMath.ml(FeedingMath.ounces(123.0)), 0.00000001)
    }

    @Test
    fun poundsConvertToKilograms() {
        assertEquals(4.5359237, FeedingMath.kg(10.0), 0.00000001)
    }

    @Test
    fun planningRangeIs150To200MlPerKg() {
        val range = FeedingMath.estimate(5.0, 8)
        assertEquals(750.0, range.first, 0.0)
        assertEquals(1000.0, range.second, 0.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun zeroFeedsRejected() {
        FeedingMath.estimate(5.0, 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun nonFiniteWeightRejected() {
        FeedingMath.estimate(Double.NaN, 8)
    }
}
