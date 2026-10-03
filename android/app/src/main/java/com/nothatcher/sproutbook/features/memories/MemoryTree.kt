package com.nothatcher.sproutbook.features.memories

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
    treeStyle: String = "Summer",
) {
    var selected by remember { mutableStateOf<String?>(null) }
    var drag by remember { mutableStateOf<Offset?>(null) }
    var accessibilityPage by remember { mutableIntStateOf(0) }
    val points = remember { TreeLayout.anchors }
    val dark = MaterialTheme.colorScheme.surface.luminance() < .35f
    val palette = remember(treeStyle, dark) { memoryTreePalette(treeStyle, dark) }
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
        drawMemoryTreeScenery(points, palette)
        if (arrange)
            points.forEach { a ->
                val center = Offset(w * a.x, h * a.y)
                drawCircle(
                    Gold.copy(alpha = if (selected == null) .45f else .9f),
                    7.dp.toPx(),
                    center,
                    style = Stroke(1.5.dp.toPx()),
                )
                drawCircle(Gold.copy(alpha = .7f), 2.dp.toPx(), center)
            }
        safe.forEach { m ->
            val a = points[m.anchor]
            val pos = if (m.id == selected && drag != null) drag!! else Offset(w * a.x, h * a.y)
            if (selected == m.id) {
                drawCircle(Gold.copy(alpha = .25f), 23.dp.toPx(), pos)
                drawCircle(Gold.copy(alpha = .85f), 23.dp.toPx(), pos, style = Stroke(1.5.dp.toPx()))
            }
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
                        close()
                    }
                translate(1.dp.toPx(), 2.dp.toPx()) {
                    drawPath(leaf, Color(0xFF10251E).copy(alpha = .18f))
                }
                val color = leafColor(m.category)
                drawPath(leaf, Brush.linearGradient(
                    listOf(lerp(color, Color.White, .18f), color, lerp(color, palette.leafOutline, .10f)),
                    start = pos - Offset(8.dp.toPx(), 12.dp.toPx()),
                    end = pos + Offset(8.dp.toPx(), 12.dp.toPx()),
                ))
                drawPath(leaf, palette.leafOutline, style = Stroke(1.dp.toPx()))
                val vein = Path().apply {
                    moveTo(pos.x - 11.dp.toPx(), pos.y)
                    quadraticTo(pos.x, pos.y - 2.dp.toPx(), pos.x + 12.dp.toPx(), pos.y)
                    moveTo(pos.x - 3.dp.toPx(), pos.y - .8.dp.toPx())
                    quadraticTo(pos.x - 3.dp.toPx(), pos.y - 4.dp.toPx(), pos.x - 1.dp.toPx(), pos.y - 7.dp.toPx())
                    moveTo(pos.x + 4.dp.toPx(), pos.y - .5.dp.toPx())
                    quadraticTo(pos.x + 3.dp.toPx(), pos.y + 3.dp.toPx(), pos.x + 2.dp.toPx(), pos.y + 6.dp.toPx())
                }
                drawPath(vein, palette.leafOutline.copy(alpha = .4f),
                    style = Stroke(.8.dp.toPx(), cap = StrokeCap.Round))
            }
        }
    }
}
