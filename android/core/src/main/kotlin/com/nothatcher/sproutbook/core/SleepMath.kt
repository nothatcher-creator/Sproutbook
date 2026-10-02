package com.nothatcher.sproutbook.core

import kotlin.math.*

object SleepMath {
    fun total(intervals: List<Pair<Long, Long>>, from: Long, to: Long): Long {
        var end = from
        var total = 0L
        intervals
            .sortedBy { it.first }
            .forEach { (s, e) ->
                val left = maxOf(s, from, end)
                val right = minOf(e, to)
                if (right > left) {
                    total += right - left
                    end = right
                }
            }
        return total
    }

    fun overlaps(start: Long, end: Long, others: List<Pair<Long, Long>>) = others.any {
        start < it.second && end > it.first
    }
}

/** Quiet, deterministic PCM. No continuous synthesis thread; the platform loops static audio. */
object NoisePcm {
    fun generate(kind: String, samples: Int = 48000): ShortArray {
        require(kind in SoundCatalog.kinds && samples >= 1000)
        val random = java.util.Random(73)
        var brown = 0.0
        var smooth = 0.0
        var breeze = 0.0
        val result =
            ShortArray(samples) { i ->
                val white = random.nextDouble() * 2 - 1
                brown = (brown + white * .035).coerceIn(-1.0, 1.0) * .997
                smooth = smooth * .86 + white * .14
                breeze = breeze * .98 + white * .02
                val v =
                    when (kind) {
                        "Brown" -> brown
                        "Fan" -> smooth * .7 + sin(2 * PI * 60 * i / 24000) * .1
                        "Rain" -> white * .22 + smooth * .7
                        "Ocean" -> (smooth * .8 + white * .12) * (.6 - .4 * cos(2 * PI * i / samples))
                        "Hush" -> white * .28 * (.55 - .45 * cos(2 * PI * i / 48000))
                        "Stream" -> smooth * .6 + white * .08 + sin(2 * PI * 190 * i / 24000) * .025
                        "Breeze" -> breeze * 1.8 + smooth * .15
                        else -> white * .3
                    }
                (v.coerceIn(-.8, .8) * 16000).toInt().toShort()
            }
        // Short raised-cosine fade at the seam avoids a click in indefinite loops.
        for (i in 0 until 240) {
            val scale = (1 - cos(PI * i / 240)) / 2
            result[i] = (result[i] * scale).toInt().toShort()
            val j = result.lastIndex - i
            result[j] = (result[j] * scale).toInt().toShort()
        }
        return result
    }
}
