package com.nothatcher.sproutbook
import com.nothatcher.sproutbook.features.teeth.archPoints
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs
class ToothLayoutTest {
    @Test fun fortyEightDpTargetsDoNotOverlapEvenOnNarrowPortrait() {
        for (width in listOf(240f, 284f, 320f, 420f)) {
            val points = archPoints(width, true) + archPoints(width, false)
            assertEquals(20, points.size)
            points.forEach { assertTrue(it.x >= 24 && it.x <= width - 24) }
            for (i in points.indices) for (j in 0 until i) {
                val dx = abs(points[i].x - points[j].x)
                val dy = abs(points[i].y - points[j].y)
                assertTrue("48dp targets $i/$j overlap at width $width", dx >= 48 || dy >= 48)
            }
        }
    }
}
