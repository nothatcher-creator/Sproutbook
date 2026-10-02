package com.nothatcher.sproutbook.core
import org.junit.Assert.*
import org.junit.Test

class WoodlandMotionTest {
    @Test fun optedInForegroundPageCanAnimate() {
        assertTrue(WoodlandMotion.shouldAnimate(false,true,false,true,true,false))
    }
    @Test fun eachPowerAccessibilityAndInteractionGateStopsMotion() {
        assertFalse(WoodlandMotion.shouldAnimate(true,true,false,true,true,false))
        assertFalse(WoodlandMotion.shouldAnimate(false,false,false,true,true,false))
        assertFalse(WoodlandMotion.shouldAnimate(false,true,true,true,true,false))
        assertFalse(WoodlandMotion.shouldAnimate(false,true,false,false,true,false))
        assertFalse(WoodlandMotion.shouldAnimate(false,true,false,true,false,false))
        assertFalse(WoodlandMotion.shouldAnimate(false,true,false,true,true,true))
        assertFalse(WoodlandMotion.shouldAnimate(false,true,false,true,true,false,false))
    }
    @Test fun returningToAQuietForegroundPageCanResume() {
        assertTrue(WoodlandMotion.shouldAnimate(false,true,false,true,true,false))
        assertFalse(WoodlandMotion.shouldAnimate(false,false,false,true,true,false))
        assertTrue(WoodlandMotion.shouldAnimate(false,true,false,true,true,false))
    }
}
