package com.nothatcher.sproutbook.features.teeth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.unit.dp
import kotlin.math.*

internal data class ToothPoint(val x: Float, val y: Float, val rotation: Float)

/** Disjoint 48dp squares, with a curved arch through their centres. */
internal fun archPoints(width: Float, upper: Boolean): List<ToothPoint> {
    val xs = listOf(24f, 24f, 24f, width / 2 - 72, width / 2 - 24,
        width / 2 + 24, width / 2 + 72, width - 24, width - 24, width - 24)
    val depths = listOf(0f, 48f, 96f, 144f, 192f, 192f, 144f, 96f, 48f, 0f)
    val angles = listOf(90f, 100f, 120f, 150f, 172f, 188f, 210f, 240f, 260f, 270f)
    return xs.indices.map { ToothPoint(xs[it], if (upper) 224 - depths[it] else 282 + depths[it],
        if (upper) angles[it] else 180 - angles[it]) }
}

@Composable
internal fun GumArtwork(modifier: Modifier, width: Float, color: Color) {
    val arches = remember(width) { listOf(true, false).map { upper ->
        val points = archPoints(width, upper)
        Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 0 until points.lastIndex) {
                val a = points[maxOf(0, i - 1)]; val b = points[i]
                val c = points[i + 1]; val d = points[minOf(points.lastIndex, i + 2)]
                cubicTo(b.x + (c.x - a.x) / 6, b.y + (c.y - a.y) / 6,
                    c.x - (d.x - b.x) / 6, c.y - (d.y - b.y) / 6, c.x, c.y)
            }
        }
    } }
    Canvas(modifier) {
        scale(size.width / width, size.height / 506f, pivot = Offset.Zero) {
            arches.forEach { drawPath(it, color.copy(alpha = .18f), style = Stroke(35f, cap = StrokeCap.Round)) }
        }
    }
}

@Composable
internal fun ToothArtwork(molar: Boolean, canine: Boolean, color: Color, rotation: Float) {
    val shape = remember(molar, canine) { Path().apply {
        moveTo(.2f, .22f)
        if (molar) {
            cubicTo(.14f, -.02f, .34f, .02f, .4f, .11f)
            cubicTo(.45f, -.03f, .58f, -.03f, .62f, .11f)
            cubicTo(.78f, -.02f, .87f, .06f, .81f, .25f)
        } else if (canine) {
            quadraticTo(.25f, .08f, .5f, .01f)
            quadraticTo(.75f, .08f, .8f, .22f)
        } else {
            quadraticTo(.25f, .05f, .5f, .06f)
            quadraticTo(.75f, .05f, .8f, .22f)
        }
        cubicTo(.94f, .53f, .82f, .95f, .66f, .94f)
        quadraticTo(.5f, .7f, .34f, .94f)
        cubicTo(.18f, .95f, .06f, .53f, .2f, .22f)
        close()
    } }
    Canvas(Modifier.size(34.dp, 40.dp)) {
        rotate(rotation) {
            scale(size.width, size.height, pivot = Offset.Zero) {
                drawPath(shape, color)
                drawPath(shape, Color(0xFFF5F2E7).copy(alpha = .25f), style = Stroke(.028f))
            }
        }
    }
}
