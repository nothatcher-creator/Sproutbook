package com.nothatcher.sproutbook.core

import java.io.ByteArrayOutputStream
import java.io.InputStream

object BoundedIo {
    fun read(input: InputStream, max: Int): ByteArray {
        require(max > 0)
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        while (true) {
            val n = input.read(buffer)
            if (n < 0) break
            require(out.size() + n <= max) { "This file is too large." }
            out.write(buffer, 0, n)
        }
        return out.toByteArray()
    }
}
