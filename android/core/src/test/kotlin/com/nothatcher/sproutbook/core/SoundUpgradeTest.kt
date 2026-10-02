package com.nothatcher.sproutbook.core

import org.junit.Assert.*
import org.junit.Test

class SoundUpgradeTest {
    @Test fun newSoundsProduceDistinctQuietLoops() {
        val sounds = listOf("Ocean", "Hush", "Stream", "Breeze")
        val loops = sounds.map { NoisePcm.generate(it, 192000) }
        loops.forEach { pcm ->
            assertTrue(pcm.count { it != 0.toShort() } > pcm.size / 2)
            assertTrue(pcm.all { kotlin.math.abs(it.toInt()) <= 12800 })
            assertEquals(0.toShort(), pcm.first())
            assertEquals(0.toShort(), pcm.last())
        }
        for (i in loops.indices) for (j in 0 until i)
            assertFalse("Every sound needs its own waveform", loops[i].contentEquals(loops[j]))
    }
}
