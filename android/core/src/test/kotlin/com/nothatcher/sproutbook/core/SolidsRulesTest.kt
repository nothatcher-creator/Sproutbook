package com.nothatcher.sproutbook.core

import org.junit.Assert.*
import org.junit.Test

class SolidsRulesTest {
    @Test
    fun caseAndWhitespaceDoNotRepeatFoods() {
        assertEquals(
            listOf("Tofu"),
            SolidsRules.untried(listOf("Avocado", "Tofu"), listOf(" avocado ")),
        )
    }

    @Test
    fun mealSlotsAndChildrenStaySeparate() {
        assertNotEquals(SolidsRules.mealId("a", 1, "Lunch"), SolidsRules.mealId("b", 1, "Lunch"))
        assertNotEquals(SolidsRules.mealId("a", 1, "Lunch"), SolidsRules.mealId("a", 1, "Dinner"))
    }
}
