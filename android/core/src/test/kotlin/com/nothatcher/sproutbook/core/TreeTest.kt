package com.nothatcher.sproutbook.core

import org.junit.Assert.*
import org.junit.Test

class TreeTest {
    @Test
    fun firstLeafStartsAtFirstAnchor() {
        assertEquals(0 to 0, TreeLayout.next(emptyList()))
    }

    @Test
    fun fullCanopyGetsAnotherChapter() {
        assertEquals(1 to 0, TreeLayout.next((0..31).map { 0 to it }))
    }

    @Test
    fun vacantAnchorReused() {
        assertEquals(0 to 1, TreeLayout.next(listOf(0 to 0, 0 to 2)))
    }

    @Test
    fun occupiedAnchorSwapsBothLeaves() {
        assertEquals(mapOf("a" to 4, "b" to 2), TreeLayout.move(mapOf("a" to 2, "b" to 4), "a", 4))
    }

    @Test
    fun farDropDoesNotMoveLeaf() {
        assertNull(TreeLayout.snap(.5f, 1.5f))
    }

    @Test
    fun exactBranchSnaps() {
        val p = TreeLayout.anchors[8]
        assertEquals(8, TreeLayout.snap(p.x, p.y))
    }
}
