package com.zaviyanllc.sproutplay

import com.zaviyanllc.sproutplay.domain.model.AgeTier
import com.zaviyanllc.sproutplay.domain.model.GameCatalog
import com.zaviyanllc.sproutplay.domain.model.GameCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun catalogHasExactly50UniqueGamesAcross5Categories() {
        val games = GameCatalog.allGames
        assertEquals(50, games.size)
        assertEquals(50, games.map { it.id }.toSet().size)
        assertEquals((1..50).toList(), games.map { it.number })

        for (category in GameCategory.entries) {
            assertEquals(10, GameCatalog.byCategory(category).size)
        }
    }

    @Test
    fun ageTiersMapAges3Through11Correctly() {
        assertEquals(AgeTier.PRESCHOOL, AgeTier.fromAge(3))
        assertEquals(AgeTier.PRESCHOOL, AgeTier.fromAge(4))
        assertEquals(AgeTier.K_1, AgeTier.fromAge(5))
        assertEquals(AgeTier.K_1, AgeTier.fromAge(7))
        assertEquals(AgeTier.GRADE_2_3, AgeTier.fromAge(8))
        assertEquals(AgeTier.GRADE_2_3, AgeTier.fromAge(9))
        assertEquals(AgeTier.GRADE_4_5, AgeTier.fromAge(10))
        assertEquals(AgeTier.GRADE_4_5, AgeTier.fromAge(11))
    }

    @Test
    fun recommendationsExistForEveryAgeTier() {
        for (tier in AgeTier.entries) {
            val recs = GameCatalog.recommendedForTier(tier)
            assertTrue("Expected recommendations for $tier", recs.size >= 5)
        }
    }
}
