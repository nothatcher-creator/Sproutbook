package com.nothatcher.sproutbook.core

import org.junit.Assert.*
import org.junit.Test

class AdviceCatalogTest {
    @Test
    fun helpStartsWithEmergencyAndAlwaysThreeSteps() {
        assertEquals(9, AdviceCatalog.help.size)
        assertTrue(AdviceCatalog.help.first().emergency)
        assertTrue(
            AdviceCatalog.help.all {
                it.steps.size == 3 &&
                    it.steps.all(String::isNotBlank) &&
                    it.source.startsWith("https://")
            }
        )
    }

    @Test
    fun searchFindsLatchAcrossCaseAndTrimsInput() {
        assertTrue(AdviceCatalog.search("Mom", "All", " LATCH ").isNotEmpty())
        assertTrue(AdviceCatalog.search("Dad", "All", "never shake").isNotEmpty())
        assertTrue(AdviceCatalog.search("Mom", "All", "nonexistent xyz").isEmpty())
    }
}
