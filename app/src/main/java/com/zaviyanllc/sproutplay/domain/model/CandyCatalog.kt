package com.zaviyanllc.sproutplay.domain.model

import com.zaviyanllc.sproutplay.games.match3.CandyMode
import com.zaviyanllc.sproutplay.games.match3.toSpec

/**
 * The five Candy Crush-style learning games. The old 50 quiz games were removed
 * (Nov 24, 2026 rebuild) — every game is now match-3 that teaches.
 */
object CandyCatalog {
    val allGames: List<MiniGameSpec> =
        CandyMode.entries.mapIndexed { index, mode -> mode.toSpec(index + 1) }

    fun getById(id: String): MiniGameSpec? = allGames.firstOrNull { it.id == id }
}
