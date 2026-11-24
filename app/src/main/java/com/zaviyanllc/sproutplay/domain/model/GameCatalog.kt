package com.zaviyanllc.sproutplay.domain.model

object GameCatalog {
    val allGames: List<MiniGameSpec> = CandyCatalog.allGames

    private val byId: Map<String, MiniGameSpec> = allGames.associateBy { it.id }

    fun getById(id: String): MiniGameSpec = byId[id] ?: allGames.first()

    fun byCategory(category: GameCategory): List<MiniGameSpec> =
        allGames.filter { it.category == category }

    /** All five candy games suit every age tier — levels scale inside each game. */
    fun recommendedForTier(tier: AgeTier): List<MiniGameSpec> = allGames
}
