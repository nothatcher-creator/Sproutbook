package com.nothatcher.sproutbook.features.memories

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.nothatcher.sproutbook.core.TreeLayout
import com.nothatcher.sproutbook.data.Memory
import com.nothatcher.sproutbook.ui.*
import kotlin.math.*

private fun leafColor(category: String) =
    when (category) {
        "First",
        "Achievement" -> Gold
        "Milestone" -> Sky
        "Funny moment",
        "Family moment" -> Rose
        "Health" -> Color(0xFFB6A6CF)
        else -> Moss
    }

@Composable
fun MemoryTree(
    memories: List<Memory>,
    arrange: Boolean = false,
    onOpen: (Memory) -> Unit = {},
    onMove: (String, Int) -> Unit = { _, _ -> },
) {
    var selected by remember { mutableStateOf<String?>(null) }
    var drag by remember { mutableStateOf<Offset?>(null) }
    var accessibilityPage by remember { mutableIntStateOf(0) }
    val points = remember { TreeLayout.anchors }
    val safe = memories.filter { it.anchor in points.indices }
    LaunchedEffect(arrange, memories.map { it.id }) {
        selected = null
        drag = null
        accessibilityPage = 0
    }
    Canvas(
        Modifier.fillMaxWidth()
            .height(315.dp)
            .semantics {
                contentDescription =
                    "Memory tree, ${memories.size} leaves. A timeline is also available."
                // Android has a small per-node custom-action limit. Keep every leaf and
                // branch reachable in groups, including a full 32-leaf canopy.
                val placing = arrange && selected != null
                val count = if (placing) points.size else safe.size
                val first = (accessibilityPage * 16).coerceAtMost(
                    ((count - 1).coerceAtLeast(0) / 16) * 16
                )
                val pageActions = if (placing) {
                    points.indices.drop(first).take(16).map { i ->
                        CustomAccessibilityAction("Move selected leaf to branch ${i+1}") {
                            selected?.let { onMove(it, i) }
                            selected = null
                            accessibilityPage = 0
                            true
                        }
                    }
                } else {
                    safe.drop(first).take(16).map { memory ->
                        CustomAccessibilityAction(
                            if (arrange) "Select ${memory.title}" else "Open ${memory.title}"
                        ) {
                            if (arrange) selected = memory.id else onOpen(memory)
                            accessibilityPage = 0
                            true
                        }
                    }
                }
                customActions = pageActions + buildList {
                    if (first > 0) add(CustomAccessibilityAction(
                        if (placing) "Previous branch positions" else "Previous leaves"
                    ) {
                        accessibilityPage--
                        true
                    })
                    if (first + 16 < count) add(CustomAccessibilityAction(
                        if (placing) "More branch positions" else "More leaves"
                    ) {
                        accessibilityPage++
                        true
                    })
                    if (placing) add(CustomAccessibilityAction("Cancel leaf selection") {
                                    selected = null
                                    accessibilityPage = 0
                                    true
                    })
                }
            }
            .pointerInput(safe, arrange, selected) {
                detectTapGestures { p ->
                    val hit =
                        safe
                            .minByOrNull {
                                val a = points[it.anchor]
                                hypot(p.x - a.x * size.width, p.y - a.y * size.height)
                            }
                            ?.takeIf {
                                val a = points[it.anchor]
                                hypot(p.x - a.x * size.width, p.y - a.y * size.height) <
                                    28.dp.toPx()
                            }
                    if (arrange && selected != null) {
                        TreeLayout.snap(p.x / size.width, p.y / size.height)?.let {
                            onMove(selected!!, it)
                        }
                        selected = null
                        accessibilityPage = 0
                    } else if (hit != null) {
                        if (arrange) selected = hit.id else onOpen(hit)
                        accessibilityPage = 0
                    }
                }
            }
            .then(
                if (arrange)
                    Modifier.pointerInput(safe) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { p ->
                                selected =
                                    safe
                                        .minByOrNull {
                                            val a = points[it.anchor]
                                            hypot(p.x - a.x * size.width, p.y - a.y * size.height)
                                        }
                                        ?.takeIf {
                                            val a = points[it.anchor]
                                            hypot(p.x - a.x * size.width, p.y - a.y * size.height) <
                                                28.dp.toPx()
                                        }
                                        ?.id
                                if (selected != null) drag = p
                            },
                            onDrag = { change, amount ->
                                if (selected != null) {
                                    change.consume()
                                    drag = (drag ?: change.position) + amount
                                }
                            },
                            onDragEnd = {
                                val p = drag
                                val id = selected
                                if (p != null && id != null)
                                    TreeLayout.snap(p.x / size.width, p.y / size.height)?.let {
                                        onMove(id, it)
                                    }
                                drag = null
                                selected = null
                            },
                            onDragCancel = {
                                drag = null
                                selected = null
                            },
                        )
                    }
                else Modifier
            )
    ) {
        val w = size.width
        val h = size.height
        val bark = Color(0xFFA18C64)
        drawOval(
            Color(0xFF65815B).copy(alpha = .10f),
            Offset(w * .05f, h * .08f),
            Size(w * .9f, h * .72f),
        )
        drawOval(
            Color(0xFF739366).copy(alpha = .16f),
            Offset(w * .2f, h * .89f),
            Size(w * .6f, h * .09f),
        )
        val trunk =
            Path().apply {
                moveTo(w * .5f, h * .94f)
                cubicTo(w * .44f, h * .7f, w * .55f, h * .53f, w * .49f, h * .19f)
            }
        drawPath(trunk, bark, style = Stroke(11.dp.toPx(), cap = StrokeCap.Round))
        // Six structural limbs give each leaf a real twig, without an animated background.
        val limbs =
            listOf(
                Triple(Offset(.5f, .76f), Offset(.32f, .65f), listOf(17, 18, 23, 24)),
                Triple(Offset(.5f, .74f), Offset(.68f, .63f), listOf(21, 22, 25, 26, 31)),
                Triple(Offset(.5f, .60f), Offset(.28f, .44f), listOf(5, 6, 11, 12, 30)),
                Triple(Offset(.51f, .59f), Offset(.74f, .42f), listOf(9, 10, 15, 16, 29)),
                Triple(Offset(.5f, .46f), Offset(.35f, .25f), listOf(0, 1, 7, 27)),
                Triple(Offset(.5f, .43f), Offset(.64f, .25f), listOf(3, 4, 8, 28)),
            )
        limbs.forEach { (base, tip, slots) ->
            val limb =
                Path().apply {
                    moveTo(w * base.x, h * base.y)
                    cubicTo(
                        w * base.x,
                        h * (base.y - .10f),
                        w * tip.x,
                        h * (tip.y + .10f),
                        w * tip.x,
                        h * tip.y,
                    )
                }
            drawPath(limb, bark, style = Stroke(4.5.dp.toPx(), cap = StrokeCap.Round))
            slots.forEach { i ->
                val a = points[i]
                val twig =
                    Path().apply {
                        moveTo(w * tip.x, h * tip.y)
                        quadraticTo(w * a.x, h * tip.y, w * a.x, h * a.y)
                    }
                drawPath(
                    twig,
                    bark.copy(alpha = .85f),
                    style = Stroke(1.7.dp.toPx(), cap = StrokeCap.Round),
                )
            }
        }
        listOf(2, 13, 14, 19, 20).forEach { i ->
            val a = points[i]
            val twig =
                Path().apply {
                    moveTo(w * .5f, h * (a.y + .09f))
                    quadraticTo(w * .5f, h * a.y, w * a.x, h * a.y)
                }
            drawPath(twig, bark, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
        }
        if (arrange)
            points.forEach { a ->
                drawCircle(
                    Gold.copy(alpha = if (selected == null) .4f else .8f),
                    7.dp.toPx(),
                    Offset(w * a.x, h * a.y),
                )
            }
        listOf(-1, 1).forEach { d ->
            drawLine(
                bark,
                Offset(w * .5f, h * .92f),
                Offset(w * (.5f + d * .15f), h * .95f),
                4.dp.toPx(),
                StrokeCap.Round,
            )
        }
        safe.forEach { m ->
            val a = points[m.anchor]
            val pos = if (m.id == selected && drag != null) drag!! else Offset(w * a.x, h * a.y)
            if (selected == m.id) drawCircle(Gold.copy(alpha = .3f), 23.dp.toPx(), pos)
            rotate(if (a.x < .5f) -38f else 38f, pivot = pos) {
                val leaf =
                    Path().apply {
                        moveTo(pos.x - 14.dp.toPx(), pos.y)
                        cubicTo(
                            pos.x - 6.dp.toPx(),
                            pos.y - 19.dp.toPx(),
                            pos.x + 12.dp.toPx(),
                            pos.y - 13.dp.toPx(),
                            pos.x + 16.dp.toPx(),
                            pos.y,
                        )
                        cubicTo(
                            pos.x + 8.dp.toPx(),
                            pos.y + 15.dp.toPx(),
                            pos.x - 8.dp.toPx(),
                            pos.y + 13.dp.toPx(),
                            pos.x - 14.dp.toPx(),
                            pos.y,
                        )
                    }
                drawPath(leaf, leafColor(m.category))
                drawLine(
                    Color(0xFF476642).copy(alpha = .4f),
                    Offset(pos.x - 10.dp.toPx(), pos.y),
                    Offset(pos.x + 10.dp.toPx(), pos.y),
                    1.dp.toPx(),
                )
            }
        }
    }
}
