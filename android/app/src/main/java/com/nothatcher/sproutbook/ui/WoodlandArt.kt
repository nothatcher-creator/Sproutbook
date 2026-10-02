package com.nothatcher.sproutbook.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import kotlin.math.*

/** Original woodland painting with native scalable labels. */
@Composable
fun WoodlandBanner(title: String, detail: String, night: Boolean = false) {
    PaintedBanner(title, detail, if (night) com.nothatcher.sproutbook.R.drawable.woodland_scene_more
        else com.nothatcher.sproutbook.R.drawable.woodland_scene_care)
}

@Composable
fun SoundGlyph(kind: String, modifier: Modifier = Modifier, color: Color = Moss) {
    Box(modifier.clearAndSetSemantics {}.drawWithCache {
        val w = size.width; val h = size.height
        val waves = (0..2).map { n -> Path().apply {
            moveTo(w * .1f, h * (.3f + n * .22f))
            cubicTo(w * .35f, h * (.05f + n * .22f), w * .55f,
                h * (.58f + n * .15f), w * .9f, h * (.3f + n * .22f))
        } }
        onDrawBehind {
            val stroke = w * .065f
            when (kind) {
                "Fan" -> {
                    repeat(4) { rotate(it * 90f) {
                        drawOval(color.copy(alpha = .7f), Offset(w * .48f, h * .05f), Size(w * .23f, h * .42f))
                    } }
                    drawCircle(color, w * .08f)
                }
                "Rain" -> repeat(6) {
                    val x = w * (.2f + (it % 3) * .27f); val y = h * (.15f + (it / 3) * .42f)
                    drawLine(color, Offset(x, y), Offset(x - w * .09f, y + h * .24f), stroke, StrokeCap.Round)
                }
                "White", "Brown", "Hush" -> repeat(5) {
                    val level = if (kind == "Brown") .15f + .05f * it else .15f + .12f * (2 - abs(2 - it))
                    val x = w * (.15f + .175f * it)
                    drawLine(color, Offset(x, h * (.5f - level)), Offset(x, h * (.5f + level)), stroke, StrokeCap.Round)
                }
                else -> waves.forEach { drawPath(it, color, style = Stroke(stroke, cap = StrokeCap.Round)) }
            }
        }
    })
}
