package com.nothatcher.sproutbook.core

import org.junit.Assert.*
import org.junit.Test

class BirthAdviceTest {
    @Test
    fun homebirthAndUnassistedBirthAreDiscoverableForBothParents() {
        for (audience in listOf("Mom", "Dad")) {
            assertTrue("Homebirth missing for $audience", AdviceCatalog.search(audience, "All", "homebirth").isNotEmpty())
            assertTrue("Free birth missing for $audience", AdviceCatalog.search(audience, "All", "free birth").isNotEmpty())
            assertTrue("Unassisted birth missing for $audience", AdviceCatalog.search(audience, "All", "unassisted birth").isNotEmpty())
        }
    }

    @Test
    fun freebirthAdviceIncludesQualifiedCareAndEmergencyHelp() {
        val articles = AdviceCatalog.search("Mom", "Birth planning", "freebirth")
        assertTrue("Freebirth advice missing", articles.isNotEmpty())
        articles.forEach { article ->
            assertTrue(article.body.contains("qualified", ignoreCase = true))
            assertTrue(article.body.contains("emergency", ignoreCase = true))
            assertTrue(article.source.startsWith("https://www.nhs.uk/"))
        }
    }
}
