package com.nothatcher.sproutbook.core

/** Optional decorative motion is subordinate to lifecycle, accessibility and battery preferences. */
object WoodlandMotion {
    fun shouldAnimate(reduceMotion: Boolean, resumed: Boolean, batterySaver: Boolean,
        systemAnimators: Boolean, mainPage: Boolean, keyboardVisible: Boolean, windowFocused: Boolean = true): Boolean =
        !reduceMotion && resumed && !batterySaver && systemAnimators && mainPage && !keyboardVisible && windowFocused
}
