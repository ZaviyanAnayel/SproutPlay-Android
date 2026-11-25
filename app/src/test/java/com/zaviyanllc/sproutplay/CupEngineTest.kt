package com.zaviyanllc.sproutplay

import com.zaviyanllc.sproutplay.domain.model.BallColor
import com.zaviyanllc.sproutplay.domain.model.TwinTrapLevels
import com.zaviyanllc.sproutplay.games.cup.CupGameEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class CupEngineTest {

    @Test
    fun tenLevelsDefinedInOrder() {
        assertEquals(10, TwinTrapLevels.all.size)
        assertEquals((1..10).toList(), TwinTrapLevels.all.map { it.number })
    }

    @Test
    fun ballAssignmentUsesDistinctCups() {
        val assigned = CupGameEngine.assignBalls(6, listOf(BallColor.GOLD, BallColor.PINK), Random(42))
        assertEquals(2, assigned.size)
        assertEquals(2, assigned.keys.toSet().size)
        assertTrue(assigned.values.containsAll(listOf(BallColor.GOLD, BallColor.PINK)))
    }

    @Test
    fun constrainedAssignmentRespectsRows() {
        val assigned = CupGameEngine.assignBallsConstrained(
            balls = listOf(BallColor.GOLD, BallColor.PINK),
            allowedCups = listOf(setOf(0, 1, 2), setOf(3, 4, 5)),
            random = Random(7),
        )
        val goldCup = assigned.entries.first { it.value == BallColor.GOLD }.key
        val pinkCup = assigned.entries.first { it.value == BallColor.PINK }.key
        assertTrue(goldCup in 0..2)
        assertTrue(pinkCup in 3..5)
    }

    @Test
    fun swapSequenceNeverRepeatsSamePair() {
        val swaps = CupGameEngine.generateSwaps(12, 30, Random(123))
        assertEquals(30, swaps.size)
        for (i in 1 until swaps.size) {
            val prev = swaps[i - 1]
            val cur = swaps[i]
            assertNotEquals(prev, cur)
            assertNotEquals(prev.second to prev.first, cur)
            assertNotEquals(cur.first, cur.second)
        }
    }

    @Test
    fun applySwapMovesBallsWithCups() {
        // Cup 0 sits in slot 0, cup 1 in slot 1. Swap them.
        val slots = CupGameEngine.applySwap(listOf(0, 1, 2), 0 to 1)
        assertEquals(listOf(1, 0, 2), slots)
        val back = CupGameEngine.applySwap(slots, 0 to 1)
        assertEquals(listOf(0, 1, 2), back)
    }

    @Test
    fun starsFallWithStrikes() {
        assertEquals(3, TwinTrapLevels.starsForStrikes(0))
        assertEquals(2, TwinTrapLevels.starsForStrikes(1))
        assertEquals(1, TwinTrapLevels.starsForStrikes(2))
        assertEquals(0, TwinTrapLevels.starsForStrikes(3))
    }

    @Test
    fun coinsScaleWithLevelAndBalls() {
        assertEquals(10, TwinTrapLevels.coinsFor(1, 1))
        assertEquals(100, TwinTrapLevels.coinsFor(5, 2))
        assertEquals(200, TwinTrapLevels.coinsFor(10, 2))
    }

    @Test
    fun circleAnglesCoverFullRing() {
        val angles = CupGameEngine.circleAngles(9)
        assertEquals(9, angles.size)
        // First slot at the top (-90 deg), steps of 40 deg.
        assertTrue(kotlin.math.abs(angles[0] - (-Math.PI / 2).toFloat()) < 0.01f)
        assertTrue(kotlin.math.abs(angles[1] - angles[0] - (2 * Math.PI / 9).toFloat()) < 0.01f)
    }
}
