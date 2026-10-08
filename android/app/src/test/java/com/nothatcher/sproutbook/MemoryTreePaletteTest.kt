package com.nothatcher.sproutbook

import com.nothatcher.sproutbook.features.memories.memoryTreePalette
import org.junit.Assert.assertEquals
import org.junit.Test

class MemoryTreePaletteTest {
    @Test
    fun everyTreeStyleHasItsOwnAppearanceInLightAndDarkMode() {
        val styles = listOf("Summer", "Autumn", "Night", "Spring", "Blossom", "Winter", "Rainbow")
        for (dark in listOf(false, true)) {
            val appearances = styles.map { style ->
                val palette = memoryTreePalette(style, dark)
                palette.canopy to palette.foliage
            }
            assertEquals("Tree styles must not fall back to the same appearance", styles.size, appearances.toSet().size)
        }
    }
}
