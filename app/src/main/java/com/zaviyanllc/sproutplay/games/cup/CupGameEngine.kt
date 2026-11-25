package com.zaviyanllc.sproutplay.games.cup

import com.zaviyanllc.sproutplay.domain.model.BallColor
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Pure cup-game logic — no Android/Compose dependencies, unit-testable.
 *
 * A cup has a fixed identity (0..cupCount-1). A ball is hidden under a cup.
 * Shuffling swaps the POSITIONS of two cups; the ball travels with its cup,
 * so [ballCups] (cup index -> ball) never changes during a shuffle — only
 * [slotOfCup] (cup index -> slot index) does.
 */
object CupGameEngine {

    /** Randomly hide each ball under a distinct cup. */
    fun assignBalls(
        cupCount: Int,
        balls: List<BallColor>,
        random: Random = Random.Default,
    ): Map<Int, BallColor> {
        require(balls.size <= cupCount) { "More balls than cups" }
        val cups = (0 until cupCount).shuffled(random)
        return balls.mapIndexed { i, ball -> cups[i] to ball }.toMap()
    }

    /**
     * Hide each ball under a cup from its own allowed set (Split Brain: gold
     * only in the top row, pink only in the bottom row).
     */
    fun assignBallsConstrained(
        balls: List<BallColor>,
        allowedCups: List<Set<Int>>,
        random: Random = Random.Default,
    ): Map<Int, BallColor> {
        require(balls.size == allowedCups.size) { "Balls and allowed sets must match" }
        val used = mutableSetOf<Int>()
        val result = mutableMapOf<Int, BallColor>()
        balls.forEachIndexed { i, ball ->
            val free = (allowedCups[i] - used).toList()
            require(free.isNotEmpty()) { "No free cup for ball $ball" }
            val cup = free.random(random)
            used += cup
            result[cup] = ball
        }
        return result
    }

    /**
     * A shuffle is a list of cup-index pairs to swap, in order. Never swaps
     * the same pair twice in a row (that would undo the previous swap).
     */
    fun generateSwaps(
        cupCount: Int,
        count: Int,
        random: Random = Random.Default,
    ): List<Pair<Int, Int>> {
        require(cupCount >= 2) { "Need at least 2 cups to shuffle" }
        val swaps = ArrayList<Pair<Int, Int>>(count)
        var previous: Pair<Int, Int>? = null
        repeat(count) {
            var pair: Pair<Int, Int>
            do {
                val a = random.nextInt(cupCount)
                var b = random.nextInt(cupCount)
                while (b == a) b = random.nextInt(cupCount)
                pair = a to b
            } while (pair == previous || pair == previous?.let { it.second to it.first })
            swaps += pair
            previous = pair
        }
        return swaps
    }

    /** Apply one swap to a cup->slot mapping (returns a new list). */
    fun applySwap(slotOfCup: List<Int>, swap: Pair<Int, Int>): List<Int> {
        val next = slotOfCup.toMutableList()
        val (a, b) = swap
        next[a] = slotOfCup[b]
        next[b] = slotOfCup[a]
        return next
    }

    /** Slot positions for a grid: [cols] columns, rows filled top-to-bottom. */
    fun gridSlots(cupCount: Int, cols: Int = 3): List<Pair<Int, Int>> {
        return List(cupCount) { i -> (i % cols) to (i / cols) }
    }

    /** Slot positions for two rows of three (Split Brain). */
    fun twoRowSlots(cupCount: Int): List<Pair<Int, Int>> {
        return List(cupCount) { i -> (i % 3) to (i / 3) }
    }

    /** Angles (radians) for slots on a circle, top slot first, clockwise. */
    fun circleAngles(cupCount: Int, rotationDeg: Float = 0f): List<Float> {
        val step = (2 * PI / cupCount).toFloat()
        val base = (-PI / 2 + Math.toRadians(rotationDeg.toDouble())).toFloat()
        return List(cupCount) { i -> base + i * step }
    }

    /** Unit-circle position for an angle. */
    fun circlePoint(angleRad: Float): Pair<Float, Float> =
        cos(angleRad) to sin(angleRad)
}
