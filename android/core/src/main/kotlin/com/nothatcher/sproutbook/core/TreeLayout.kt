package com.nothatcher.sproutbook.core

import kotlin.math.*

data class BranchPoint(val x: Float, val y: Float)

object TreeLayout {
    val anchors =
        listOf(
                .22f to .22f,
                .35f to .14f,
                .49f to .10f,
                .63f to .15f,
                .76f to .23f,
                .14f to .34f,
                .27f to .32f,
                .41f to .27f,
                .55f to .25f,
                .69f to .32f,
                .84f to .35f,
                .19f to .46f,
                .33f to .43f,
                .47f to .40f,
                .61f to .41f,
                .75f to .45f,
                .89f to .46f,
                .12f to .57f,
                .26f to .57f,
                .39f to .55f,
                .54f to .54f,
                .68f to .56f,
                .81f to .58f,
                .22f to .69f,
                .36f to .68f,
                .63f to .67f,
                .77f to .69f,
                .31f to .23f,
                .57f to .17f,
                .82f to .28f,
                .16f to .25f,
                .88f to .57f,
            )
            .map { BranchPoint(it.first, it.second) }

    fun next(slots: List<Pair<Int, Int>>): Pair<Int, Int> {
        val occupied = slots.toSet()
        for (chapter in 0..((slots.maxOfOrNull { it.first } ?: 0) + 1)) for (anchor in
            anchors.indices) if ((chapter to anchor) !in occupied) return chapter to anchor
        error("No available branch")
    }

    fun move(slots: Map<String, Int>, id: String, target: Int): Map<String, Int> {
        if (id !in slots || target !in anchors.indices) return slots
        val old = slots.getValue(id)
        val other = slots.entries.firstOrNull { it.key != id && it.value == target }?.key
        return slots.toMutableMap().apply {
            this[id] = target
            if (other != null) this[other] = old
        }
    }

    fun snap(x: Float, y: Float): Int? {
        val nearest = anchors.indices.minBy { hypot(anchors[it].x - x, anchors[it].y - y) }
        return nearest.takeIf { hypot(anchors[it].x - x, anchors[it].y - y) <= .13f }
    }
}
