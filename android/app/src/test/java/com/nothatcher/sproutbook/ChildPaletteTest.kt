package com.nothatcher.sproutbook

import androidx.compose.ui.graphics.luminance
import com.nothatcher.sproutbook.ui.woodlandColorScheme
import org.junit.Assert.*
import org.junit.Test

class ChildPaletteTest {
    @Test
    fun eachChildAccentKeepsReadableTextInBothThemes() {
        for (dark in listOf(false, true)) for (accent in listOf("Forest", "Moss", "Amber", "Sky")) {
            val scheme = woodlandColorScheme(dark, accent)
            listOf(scheme.primary to scheme.onPrimary, scheme.primaryContainer to scheme.onPrimaryContainer,
                scheme.secondaryContainer to scheme.onSecondaryContainer).forEach { (background, text) ->
                val a = background.luminance(); val b = text.luminance()
                val contrast = (maxOf(a, b) + .05f) / (minOf(a, b) + .05f)
                assertTrue("$accent ${if (dark) "dark" else "light"} text contrast $contrast", contrast >= 4.5f)
            }
        }
    }
}
