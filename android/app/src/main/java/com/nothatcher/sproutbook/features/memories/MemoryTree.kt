package com.nothatcher.sproutbook.features.memories

import androidx.compose.foundation.Canvas
import androidx.compose.ui.semantics.clearAndSetSemantics
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

internal fun leafColor(category: String) =
    when (category) {
        "First",
        "Achievement" -> Gold
        "Milestone" -> Sky
        "Funny moment",
        "Family moment" -> Rose
        "Health" -> Color(0xFFB6A6CF)
        else -> Moss
    }

/** Leaf colours as a reader would name them, in a stable legend order. */
internal fun leafGroup(category: String) =
    when (category) {
        "First", "Achievement" -> "Firsts & wins"
        "Milestone" -> "Milestones"
        "Funny moment", "Family moment" -> "Fun & family"
        "Health" -> "Health"
        else -> "Everyday"
    }

internal val leafGroups = listOf("Firsts & wins" to Gold, "Milestones" to Sky, "Fun & family" to Rose,
    "Health" to Color(0xFFB6A6CF), "Everyday" to Moss)

/** A memory leaf centred on [pos], pointing right; callers rotate it into place. */
internal fun DrawScope.drawMemoryLeaf(pos: Offset, color: Color, outline: Color, scale: Float = 1f) {
    fun d(v: Float) = v.dp.toPx() * scale
    val leaf = Path().apply {
        moveTo(pos.x - d(13f), pos.y)
        cubicTo(pos.x - d(7f), pos.y - d(14f), pos.x + d(9f), pos.y - d(13f), pos.x + d(17f), pos.y - d(1f))
        cubicTo(pos.x + d(9f), pos.y + d(12f), pos.x - d(7f), pos.y + d(13f), pos.x - d(13f), pos.y)
        close()
    }
    // Stem first, so the blade sits on top of it.
    drawLine(outline.copy(alpha = .7f), Offset(pos.x - d(12f), pos.y), Offset(pos.x - d(18f), pos.y + d(2f)),
        d(1.4f), StrokeCap.Round)
    translate(d(1f), d(2f)) { drawPath(leaf, Color(0xFF10251E).copy(alpha = .2f)) }
    drawPath(leaf, Brush.linearGradient(
        listOf(lerp(color, Color.White, .28f), color, lerp(color, outline, .14f)),
        start = pos - Offset(d(6f), d(11f)), end = pos + Offset(d(6f), d(11f)),
    ))
    drawPath(leaf, outline.copy(alpha = .85f), style = Stroke(d(1f)))
    val veins = Path().apply {
        moveTo(pos.x - d(11f), pos.y)
        quadraticTo(pos.x + d(2f), pos.y - d(1.5f), pos.x + d(14f), pos.y - d(1f))
        listOf(-5f, 1f, 7f).forEach { x ->
            moveTo(pos.x + d(x), pos.y - d(.8f)); lineTo(pos.x + d(x + 4f), pos.y - d(6f))
            moveTo(pos.x + d(x), pos.y - d(.6f)); lineTo(pos.x + d(x + 4f), pos.y + d(5f))
        }
    }
    drawPath(veins, outline.copy(alpha = .32f), style = Stroke(d(.8f), cap = StrokeCap.Round))
    // A soft sheen on the upper blade.
    drawOval(Color.White.copy(alpha = .28f), Offset(pos.x - d(5f), pos.y - d(8.5f)), androidx.compose.ui.geometry.Size(d(9f), d(3.2f)))
}

@Composable
fun MemoryTree(
    memories: List<Memory>,
    arrange: Boolean = false,
    onOpen: (Memory) -> Unit = {},
    onMove: (String, Int) -> Unit = { _, _ -> },
    treeStyle: String = "Summer",
    enabled: Boolean = true,
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
                if (!enabled) disabled()
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
                customActions = if (!enabled) emptyList() else pageActions + buildList {
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
            .pointerInput(safe, arrange, selected, enabled) {
                if (!enabled) return@pointerInput
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
                if (arrange && enabled)
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
        val occupied = safe.map { it.anchor }.toSet()
        val newest = safe.maxByOrNull { it.occurredOn }?.id
        // Little buds on empty branches show where the next moments will grow.
        if (!arrange) points.forEachIndexed { i, a ->
            if (i !in occupied) {
                val bud = Offset(w * a.x, h * a.y)
                drawCircle(palette.light.copy(alpha = .55f), 3.4.dp.toPx(), bud)
                drawCircle(palette.leafOutline.copy(alpha = .35f), 3.4.dp.toPx(), bud, style = Stroke(.8.dp.toPx()))
            }
        }
        safe.forEach { m ->
            val a = points[m.anchor]
            val pos = if (m.id == selected && drag != null) drag!! else Offset(w * a.x, h * a.y)
            if (selected == m.id) {
                drawCircle(Gold.copy(alpha = .25f), 23.dp.toPx(), pos)
                drawCircle(Gold.copy(alpha = .85f), 23.dp.toPx(), pos, style = Stroke(1.5.dp.toPx()))
            } else if (m.id == newest && !arrange) {
                drawCircle(Brush.radialGradient(listOf(palette.light.copy(alpha = .75f), Color.Transparent), pos, 26.dp.toPx()),
                    26.dp.toPx(), pos)
            }
            rotate(if (a.x < .5f) -38f else 38f, pivot = pos) {
                drawMemoryLeaf(pos, leafColor(m.category), palette.leafOutline, if (m.id == newest) 1.12f else 1f)
            }
        }
    }
}

@Composable
internal fun LeafMarker(category: String, size: androidx.compose.ui.unit.Dp = 32.dp) {
    val outline = MaterialTheme.colorScheme.primary
    Canvas(Modifier.size(size).clearAndSetSemantics {}) {
        rotate(-35f) { drawMemoryLeaf(center + Offset(1.dp.toPx(), 0f), leafColor(category), outline, this.size.width / 40.dp.toPx()) }
    }
}
