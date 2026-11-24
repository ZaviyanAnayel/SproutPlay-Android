package com.zaviyanllc.sproutplay.games.match3

import kotlin.random.Random

/**
 * Pure-Kotlin match-3 engine for SproutPlay's Candy Crush-style learning games.
 * No Android dependencies — the board logic is fully testable on its own.
 *
 * Rules (kid-friendly, Candy Crush-like):
 * - 8x8 board of glossy candy tiles. Swap two adjacent tiles.
 * - 3+ same-color tiles in a row/column pop. 4 in a row makes a striped candy
 *   (clears its whole row/column when matched). 5 in a row makes a color bomb
 *   (clears every tile of one color when matched).
 * - After a pop, tiles fall down (gravity) and new tiles drop in; chained pops
 *   are cascades and multiply the score.
 * - If no move is possible the board reshuffles itself.
 */

enum class CandyColor {
    RED, PINK, ORANGE, YELLOW, GREEN, BLUE, PURPLE
}

enum class Special {
    NONE, STRIPED_ROW, STRIPED_COL, BOMB
}

data class Pos(val row: Int, val col: Int) {
    fun isAdjacentTo(other: Pos): Boolean =
        (row == other.row && kotlin.math.abs(col - other.col) == 1) ||
            (col == other.col && kotlin.math.abs(row - other.row) == 1)
}

/**
 * One candy tile. [face] is what the child sees on the tile (a digit, a letter,
 * an emoji) and [value] is its learning value (e.g. the digit for math sums).
 * [bornTick] marks which resolve step created the tile, so the UI can animate
 * freshly dropped tiles falling in.
 */
data class CandyTile(
    val id: Long,
    val color: CandyColor,
    val face: String,
    val value: Int = 1,
    val special: Special = Special.NONE,
    val bornTick: Int = 0,
)

data class ClearedTile(val pos: Pos, val tile: CandyTile)

/** One cascade step: what popped, what special was created, what dropped in. */
data class ResolveStep(
    val cleared: List<ClearedTile>,
    val createdSpecial: Pair<Pos, CandyTile>?,
    val spawned: List<Pair<Pos, CandyTile>>,
    val cascadeIndex: Int,
)

/** A face factory: the learning mode decides what each new tile shows. */
typealias FaceFactory = (color: CandyColor, random: Random) -> Pair<String, Int>

class Match3Board(
    val rows: Int = 8,
    val cols: Int = 8,
    private val colorCount: Int = 6,
    private val faceFactory: FaceFactory,
    private val random: Random = Random.Default,
) {
    private var nextId = 1L
    private var tick = 0

    /** grid[row][col] — null only transiently inside resolve steps. */
    var grid: Array<Array<CandyTile?>> = Array(rows) { Array<CandyTile?>(cols) { null } }
        private set

    init {
        fillInitial()
    }

    private fun newTile(color: CandyColor, bornTick: Int = tick): CandyTile {
        val (face, value) = faceFactory(color, random)
        return CandyTile(id = nextId++, color = color, face = face, value = value, bornTick = bornTick)
    }

    private fun randomColor(): CandyColor =
        CandyColor.entries[random.nextInt(colorCount)]

    /** Fill the board with no pre-existing matches, so the child starts clean. */
    private fun fillInitial() {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                var color: CandyColor
                do {
                    color = randomColor()
                } while (
                    (c >= 2 && grid[r][c - 1]?.color == color && grid[r][c - 2]?.color == color) ||
                        (r >= 2 && grid[r - 1][c]?.color == color && grid[r - 2][c]?.color == color)
                )
                grid[r][c] = newTile(color)
            }
        }
    }

    fun tileAt(pos: Pos): CandyTile? =
        if (pos.row in 0 until rows && pos.col in 0 until cols) grid[pos.row][pos.col] else null

    /** Find every position that is part of a 3+ run of the same color. Bombs are wild. */
    fun findMatches(): Set<Pos> {
        val matched = mutableSetOf<Pos>()
        // Horizontal runs
        for (r in 0 until rows) {
            var c = 0
            while (c < cols) {
                val tile = grid[r][c]
                if (tile == null) {
                    c++
                    continue
                }
                if (tile.special == Special.BOMB) {
                    // A bomb joins whatever run touches it; handled by the run scan below.
                    c++
                    continue
                }
                val color = tile.color
                var end = c + 1
                while (end < cols) {
                    val t = grid[r][end] ?: break
                    if (t.color == color || t.special == Special.BOMB) end++ else break
                }
                if (end - c >= 3) {
                    for (i in c until end) matched.add(Pos(r, i))
                }
                c = end
            }
        }
        // Vertical runs
        for (c in 0 until cols) {
            var r = 0
            while (r < rows) {
                val tile = grid[r][c]
                if (tile == null) {
                    r++
                    continue
                }
                if (tile.special == Special.BOMB) {
                    r++
                    continue
                }
                val color = tile.color
                var end = r + 1
                while (end < rows) {
                    val t = grid[end][c] ?: break
                    if (t.color == color || t.special == Special.BOMB) end++ else break
                }
                if (end - r >= 3) {
                    for (i in r until end) matched.add(Pos(i, c))
                }
                r = end
            }
        }
        // Bombs that sit adjacent to any matched tile also pop.
        val extra = matched.flatMap { pos ->
            listOf(Pos(pos.row - 1, pos.col), Pos(pos.row + 1, pos.col), Pos(pos.row, pos.col - 1), Pos(pos.row, pos.col + 1))
        }.filter { p ->
            p.row in 0 until rows && p.col in 0 until cols && tileAt(p)?.special == Special.BOMB
        }
        matched.addAll(extra)
        return matched
    }

    /**
     * Try to swap two adjacent tiles. Returns true when the swap creates a match
     * or triggers a special; otherwise the tiles swap back and it returns false.
     */
    fun attemptSwap(a: Pos, b: Pos): Boolean {
        if (!a.isAdjacentTo(b)) return false
        val tileA = tileAt(a) ?: return false
        val tileB = tileAt(b) ?: return false
        grid[a.row][a.col] = tileB
        grid[b.row][b.col] = tileA
        val specialTriggered = tileA.special != Special.NONE || tileB.special != Special.NONE
        if (specialTriggered) return true
        if (findMatches().isEmpty()) {
            grid[a.row][a.col] = tileA
            grid[b.row][b.col] = tileB
            return false
        }
        return true
    }

    /**
     * Run ONE cascade step: pop matches (or fire a queued special), drop tiles,
     * refill. Returns null when the board is settled. The UI calls this in a loop
     * with a short delay between steps so the child sees each pop.
     */
    fun stepResolve(pendingSpecials: MutableList<Pos> = mutableListOf()): ResolveStep? {
        val toClear: MutableSet<Pos>
        var createdSpecial: Pair<Pos, CandyTile>? = null
        if (pendingSpecials.isNotEmpty()) {
            val origin = pendingSpecials.removeAt(0)
            toClear = fireSpecial(origin).toMutableSet()
        } else {
            val matches = findMatches()
            if (matches.isEmpty()) return null
            toClear = matches.toMutableSet()
            // A 4-run makes a striped candy; a 5+-run makes a color bomb.
            createdSpecial = maybeCreateSpecial(matches)
            if (createdSpecial != null) toClear.remove(createdSpecial.first)
            // Striped / bomb tiles caught inside a match fire as well.
            val firing = toClear.mapNotNull { tileAt(it) }
                .filter { it.special != Special.NONE }
                .map { posOf(it) }
            for (pos in firing) {
                toClear.addAll(fireSpecial(pos))
            }
        }
        tick++
        val cleared = toClear.mapNotNull { pos -> tileAt(pos)?.let { ClearedTile(pos, it) } }
        for (pos in toClear) grid[pos.row][pos.col] = null
        val spawned = applyGravityAndRefill()
        return ResolveStep(
            cleared = cleared,
            createdSpecial = createdSpecial,
            spawned = spawned,
            cascadeIndex = tick,
        )
    }

    private fun posOf(tile: CandyTile): Pos {
        for (r in 0 until rows) for (c in 0 until cols) {
            if (grid[r][c]?.id == tile.id) return Pos(r, c)
        }
        return Pos(0, 0)
    }

    /** Work out the positions a special at [pos] clears. */
    private fun fireSpecial(pos: Pos): Set<Pos> {
        val tile = tileAt(pos) ?: return emptySet()
        return when (tile.special) {
            Special.STRIPED_ROW -> (0 until cols).map { Pos(pos.row, it) }.toSet()
            Special.STRIPED_COL -> (0 until rows).map { Pos(it, pos.col) }.toSet()
            Special.BOMB -> {
                val target = (0 until rows).flatMap { r -> (0 until cols).map { c -> Pos(r, c) } }
                    .mapNotNull { p -> tileAt(p) }
                    .filter { it.special == Special.NONE }
                    .groupBy { it.color }
                    .maxByOrNull { it.value.size }
                    ?.key
                if (target == null) setOf(pos)
                else (0 until rows).flatMap { r -> (0 until cols).map { c -> Pos(r, c) } }
                    .filter { tileAt(it)?.color == target }.toSet() + pos
            }
            Special.NONE -> setOf(pos)
        }
    }

    /** A run of 4+ creates a special at the middle of its longest run. */
    private fun maybeCreateSpecial(matches: Set<Pos>): Pair<Pos, CandyTile>? {
        // Find the longest horizontal or vertical run inside the matches.
        var bestRun: List<Pos> = emptyList()
        for (r in 0 until rows) {
            var run = mutableListOf<Pos>()
            for (c in 0 until cols) {
                if (Pos(r, c) in matches) run.add(Pos(r, c)) else {
                    if (run.size > bestRun.size) bestRun = run
                    run = mutableListOf()
                }
            }
            if (run.size > bestRun.size) bestRun = run
        }
        for (c in 0 until cols) {
            var run = mutableListOf<Pos>()
            for (r in 0 until rows) {
                if (Pos(r, c) in matches) run.add(Pos(r, c)) else {
                    if (run.size > bestRun.size) bestRun = run
                    run = mutableListOf()
                }
            }
            if (run.size > bestRun.size) bestRun = run
        }
        if (bestRun.size < 4) return null
        val at = bestRun[bestRun.size / 2]
        val base = tileAt(at) ?: return null
        val special = if (bestRun.size >= 5) Special.BOMB
        else if (bestRun.all { it.row == bestRun.first().row }) Special.STRIPED_ROW
        else Special.STRIPED_COL
        val upgraded = base.copy(special = special)
        grid[at.row][at.col] = upgraded
        return at to upgraded
    }

    /** Collapse each column down and drop fresh tiles in from the top. */
    private fun applyGravityAndRefill(): List<Pair<Pos, CandyTile>> {
        val spawned = mutableListOf<Pair<Pos, CandyTile>>()
        for (c in 0 until cols) {
            var write = rows - 1
            for (r in rows - 1 downTo 0) {
                val tile = grid[r][c]
                if (tile != null) {
                    grid[write][c] = tile
                    if (write != r) grid[r][c] = null
                    write--
                }
            }
            for (r in write downTo 0) {
                val tile = newTile(randomColor(), bornTick = tick)
                grid[r][c] = tile
                spawned.add(Pos(r, c) to tile)
            }
        }
        return spawned
    }

    /** Is there any legal swap left? If not, the UI reshuffles. */
    fun hasPossibleMove(): Boolean {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val a = Pos(r, c)
                for (b in listOf(Pos(r, c + 1), Pos(r + 1, c))) {
                    if (b.row >= rows || b.col >= cols) continue
                    val tileA = tileAt(a) ?: continue
                    val tileB = tileAt(b) ?: continue
                    if (tileA.special != Special.NONE || tileB.special != Special.NONE) return true
                    grid[a.row][a.col] = tileB
                    grid[b.row][b.col] = tileA
                    val ok = findMatches().isNotEmpty()
                    grid[a.row][a.col] = tileA
                    grid[b.row][b.col] = tileB
                    if (ok) return true
                }
            }
        }
        return false
    }

    /** Shuffle until a board with a possible move and no instant matches appears. */
    fun shuffle() {
        val tiles = (0 until rows).flatMap { r -> (0 until cols).map { c -> grid[r][c]!! } }.shuffled(random)
        var guard = 0
        do {
            val shuffled = tiles.shuffled(random)
            var i = 0
            for (r in 0 until rows) for (c in 0 until cols) grid[r][c] = shuffled[i++]
            guard++
        } while ((findMatches().isNotEmpty() || !hasPossibleMove()) && guard < 40)
    }
}
