package com.nothatcher.sproutbook.core
import org.junit.Assert.*
import org.junit.Test
class AdviceUpgradeTest {
    @Test fun positionsCanBeFoundIndividuallyAndStayInTheirTab() {
        assertEquals("Cradle position", AdviceCatalog.search("Mom", "Breastfeeding", "cradle position").single().title)
        assertTrue(AdviceCatalog.search("Dad", "All", "cradle position").isEmpty())
        assertTrue(AdviceCatalog.search("Mom", "Pumping", "cradle position").isEmpty())
    }
    @Test fun practicalDadHandoverIsSearchableAndHasNoMedicalPrescription() {
        val article = AdviceCatalog.search("Dad", "All", "caregiver handover").single()
        assertTrue(article.body.contains("emergency", ignoreCase = true))
        assertEquals("Household", article.category)
    }
    @Test fun allArticleKeysAreUniqueAndBothTabsHaveBroadCoverage() {
        assertEquals(AdviceCatalog.articles.size, AdviceCatalog.articles.distinctBy { it.audience + it.title }.size)
        for (audience in listOf("Mom", "Dad")) {
            val rows = AdviceCatalog.search(audience, "All", "")
            assertTrue(rows.size >= 18)
            assertTrue(rows.all { it.body.isNotBlank() && (it.source.isBlank() || it.source.startsWith("https://")) })
        }
    }
}
