package com.nothatcher.sproutbook.core

import org.junit.Assert.*
import org.junit.Test

class SleepMathTest {
    @Test
    fun midnightClippingAndUnion() {
        assertEquals(90L, SleepMath.total(listOf(0L to 50L, 40L to 100L), 10, 120))
    }

    @Test
    fun outsideDay() {
        assertEquals(0L, SleepMath.total(listOf(0L to 10L), 20, 30))
    }

    @Test
    fun touchingIsNotOverlap() {
        assertFalse(SleepMath.overlaps(10, 20, listOf(0L to 10L)))
        assertTrue(SleepMath.overlaps(9, 20, listOf(0L to 10L)))
    }

    @Test
    fun eachSoundHasEnergyAndBoundedLevel() {
        listOf("White", "Brown", "Fan", "Rain").forEach {
            val a = NoisePcm.generate(it)
            assertTrue(it, a.any { v -> v != 0.toShort() })
            assertTrue(a.all { kotlin.math.abs(it.toInt()) < 16000 })
            assertEquals(a.first(), a.last())
        }
    }
}
