package com.nothatcher.sproutbook.core

import org.junit.Assert.*
import org.junit.Test

class GrowthMathTest {
    @Test
    fun poundsAndKilogramsRoundTrip() {
        assertEquals(4.5359237, GrowthMath.canonical("Weight", 10.0, "lb")!!, 0.000001)
        assertEquals(10.0, GrowthMath.display("Weight", 4.5359237, "lb"), 0.000001)
    }

    @Test
    fun inchesAndCentimetersRoundTrip() {
        assertEquals(50.8, GrowthMath.canonical("Height", 20.0, "in")!!, 0.000001)
        assertEquals(20.0, GrowthMath.display("Height", 50.8, "in"), 0.000001)
    }

    @Test
    fun legacyUnitsNormalize() {
        assertEquals(3.5, GrowthMath.canonical("Weight", 3500.0, "g")!!, 0.000001)
        assertEquals(50.0, GrowthMath.canonical("Height", 0.5, "m")!!, 0.000001)
    }

    @Test
    fun unknownOrInvalidMeasurementsStayOffChart() {
        assertNull(GrowthMath.canonical("Height", 20.0, "unknown"))
        assertNull(GrowthMath.canonical("Weight", Double.NaN, "kg"))
        assertNull(GrowthMath.canonical("Weight", -1.0, "kg"))
    }
}
