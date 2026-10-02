package com.nothatcher.sproutbook

import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.nothatcher.sproutbook.services.SoundService
import org.junit.Assert.*
import org.junit.Test

class SoundTest {
    @Test
    fun nativeAudioStartsAndStopsEverySound() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            com.nothatcher.sproutbook.core.SoundCatalog.kinds.forEach { kind ->
                scenario.onActivity { SoundService.play(it, kind, .1f, 15) }
                val deadline = System.currentTimeMillis() + 15000
                while (
                    !SoundService.state.value.playing &&
                        SoundService.state.value.error == null &&
                        System.currentTimeMillis() < deadline
                ) Thread.sleep(100)
                assertNull(SoundService.state.value.error)
                assertTrue("AudioTrack must play $kind", SoundService.state.value.playing)
                assertEquals(kind, SoundService.state.value.kind)
                scenario.onActivity { SoundService.stop(it) }
                val stopped = System.currentTimeMillis() + 5000
                while (
                    SoundService.state.value.playing && System.currentTimeMillis() < stopped
                ) Thread.sleep(50)
                assertFalse(SoundService.state.value.playing)
            }
        }
    }
    @Test fun pauseResumeFreezesCountdownAndSwitchKeepsPausedState() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            fun waitFor(check: () -> Boolean) {
                val deadline = System.currentTimeMillis() + 15000
                while (!check() && System.currentTimeMillis() < deadline) Thread.sleep(50)
                assertTrue(check())
            }
            try {
                scenario.onActivity { SoundService.play(it, "Ocean", .1f, 15) }
                waitFor { SoundService.state.value.playing }
                scenario.onActivity { SoundService.pause(it) }
                waitFor { SoundService.state.value.paused }
                val frozen = SoundService.state.value.clock.remaining(android.os.SystemClock.elapsedRealtime())
                Thread.sleep(150)
                assertEquals(frozen, SoundService.state.value.clock.remaining(android.os.SystemClock.elapsedRealtime()))
                scenario.onActivity { SoundService.play(it, "Stream", .15f, 15, preserve = true) }
                waitFor { SoundService.state.value.paused && SoundService.state.value.kind == "Stream" && !SoundService.state.value.preparing }
                assertEquals(frozen, SoundService.state.value.clock.remaining(android.os.SystemClock.elapsedRealtime()))
                scenario.onActivity { SoundService.resume(it) }
                waitFor { SoundService.state.value.playing }
                assertFalse(SoundService.state.value.paused)
                scenario.onActivity { SoundService.timer(it, 0) }
                waitFor { SoundService.state.value.minutes == 0 }
                assertNull(SoundService.state.value.clock.deadlineMs)
            } finally {
                scenario.onActivity { SoundService.stop(it) }
                waitFor { !SoundService.state.value.active }
            }
        }
    }

    @Test fun pausedSwitchDoesNotInterruptAnotherAudioFocusOwner() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            val lost = java.util.concurrent.atomic.AtomicBoolean(false)
            var manager: android.media.AudioManager? = null
            var other: android.media.AudioFocusRequest? = null
            fun waitFor(check: () -> Boolean) {
                val deadline = System.currentTimeMillis() + 15000
                while (!check() && System.currentTimeMillis() < deadline) Thread.sleep(50)
                assertTrue(check())
            }
            try {
                scenario.onActivity { SoundService.play(it, "Brown", .1f, 15) }
                waitFor { SoundService.state.value.playing }
                scenario.onActivity { SoundService.pause(it) }
                waitFor { SoundService.state.value.paused }
                scenario.onActivity {
                    manager = it.getSystemService(android.media.AudioManager::class.java)
                    other = android.media.AudioFocusRequest.Builder(android.media.AudioManager.AUDIOFOCUS_GAIN)
                        .setAudioAttributes(android.media.AudioAttributes.Builder().setUsage(android.media.AudioAttributes.USAGE_MEDIA).build())
                        .setOnAudioFocusChangeListener { value -> if (value < 0) lost.set(true) }.build()
                    assertEquals(android.media.AudioManager.AUDIOFOCUS_REQUEST_GRANTED, manager!!.requestAudioFocus(other!!))
                    SoundService.play(it, "Ocean", .1f, 15, preserve = true)
                }
                waitFor { SoundService.state.value.paused && SoundService.state.value.kind == "Ocean" && !SoundService.state.value.preparing }
                Thread.sleep(150)
                assertFalse("Changing a paused backdrop must not steal audio focus", lost.get())
            } finally {
                scenario.onActivity { SoundService.stop(it); other?.let { request -> manager?.abandonAudioFocusRequest(request) } }
                waitFor { !SoundService.state.value.active }
            }
        }
    }

}
