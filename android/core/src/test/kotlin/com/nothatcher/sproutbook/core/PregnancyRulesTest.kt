package com.nothatcher.sproutbook.core

import org.junit.Assert.*
import org.junit.Test

class PregnancyRulesTest {
    @Test
    fun dueDateMeansFortyWeeks() {
        assertEquals(280, PregnancyRules.gestationDays(500, 500))
        assertEquals(140, PregnancyRules.gestationDays(500, 360))
        assertEquals(0, PregnancyRules.gestationDays(500, 100))
    }

    @Test
    fun intervalsAreStartToStartInChronologicalOrder() {
        assertEquals(listOf(100L, 150L), PregnancyRules.intervals(listOf(350, 100, 200)))
    }
}
