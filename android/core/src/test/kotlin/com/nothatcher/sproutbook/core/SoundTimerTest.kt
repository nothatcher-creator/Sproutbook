package com.nothatcher.sproutbook.core
import org.junit.Assert.*
import org.junit.Test
class SoundTimerTest {
    @Test fun pausePreservesTimeAndResumeUsesNewDeadline() {
        val timer = SoundTimer.start(15, 1000)
        assertEquals(899000L, timer.remaining(2000))
        val paused = timer.pause(2000)
        assertEquals(899000L, paused.remaining(9999999))
        val resumed = paused.resume(10000000)
        assertEquals(898000L, resumed.remaining(10001000))
        assertEquals(0L, resumed.remaining(20000000))
    }
    @Test fun continuousHasNoDeadlineThroughPauseAndResume() {
        assertNull(SoundTimer.start(0, 1).pause(2).resume(9000000).remaining(999999999))
    }
    @Test(expected = IllegalArgumentException::class) fun invalidTimerIsRejected() { SoundTimer.start(-1, 1) }
}
