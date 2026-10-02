package com.nothatcher.sproutbook.core

import org.junit.Assert.*
import org.junit.Test

class BoundedIoTest {
    @Test(expected = IllegalArgumentException::class)
    fun oversizedReadRejected() {
        BoundedIo.read(byteArrayOf(1, 2, 3).inputStream(), 2)
    }

    @Test
    fun exactLimitAccepted() {
        assertArrayEquals(byteArrayOf(1, 2), BoundedIo.read(byteArrayOf(1, 2).inputStream(), 2))
    }
}
