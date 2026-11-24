package com.zaviyanllc.sproutplay.domain.model

import androidx.compose.ui.graphics.Color
import com.zaviyanllc.sproutplay.ui.theme.ArcadeColor
import com.zaviyanllc.sproutplay.ui.theme.ArcadeSoft
import com.zaviyanllc.sproutplay.ui.theme.LogicColor
import com.zaviyanllc.sproutplay.ui.theme.LogicSoft
import com.zaviyanllc.sproutplay.ui.theme.MathColor
import com.zaviyanllc.sproutplay.ui.theme.MathSoft
import com.zaviyanllc.sproutplay.ui.theme.ReadingColor
import com.zaviyanllc.sproutplay.ui.theme.ReadingSoft
import com.zaviyanllc.sproutplay.ui.theme.ScienceColor
import com.zaviyanllc.sproutplay.ui.theme.ScienceSoft

enum class GameCategory(
    val id: String,
    val title: String,
    val shortTitle: String,
    val emoji: String,
    val accentColor: Color,
    val softColor: Color,
    val tagline: String,
) {
    MATH_NUMBERS(
        id = "math",
        title = "Math & Numbers",
        shortTitle = "Math",
        emoji = "🔢",
        accentColor = MathColor,
        softColor = MathSoft,
        tagline = "Counting, fractions, time & number quests",
    ),
    READING_PHONICS(
        id = "reading",
        title = "Reading, Phonics & Words",
        shortTitle = "Reading",
        emoji = "📖",
        accentColor = ReadingColor,
        softColor = ReadingSoft,
        tagline = "Letter sounds, rhymes, spelling & stories",
    ),
    LOGIC_MEMORY(
        id = "logic",
        title = "Logic, Memory & Puzzles",
        shortTitle = "Logic",
        emoji = "🧩",
        accentColor = LogicColor,
        softColor = LogicSoft,
        tagline = "Patterns, mazes, coding & brain teasers",
    ),
    SCIENCE_WORLD(
        id = "science",
        title = "Science, Nature & World",
        shortTitle = "Science",
        emoji = "🌍",
        accentColor = ScienceColor,
        softColor = ScienceSoft,
        tagline = "Habitats, planets, weather & music labs",
    ),
    CREATIVE_ARCADE(
        id = "arcade",
        title = "Creative, Reflex & Arcade",
        shortTitle = "Arcade",
        emoji = "🎨",
        accentColor = ArcadeColor,
        softColor = ArcadeSoft,
        tagline = "Pixel art, rhythm, gravity & daily challenges",
    );

    companion object {
        fun fromId(id: String): GameCategory = entries.firstOrNull { it.id == id } ?: MATH_NUMBERS
    }
}

data class TierScaling(
    val preschool: String,
    val k1: String,
    val grade23: String,
    val grade45: String,
) {
    fun forTier(tier: AgeTier): String = when (tier) {
        AgeTier.PRESCHOOL -> preschool
        AgeTier.K_1 -> k1
        AgeTier.GRADE_2_3 -> grade23
        AgeTier.GRADE_4_5 -> grade45
    }
}

data class MiniGameSpec(
    val id: String,
    val number: Int,
    val title: String,
    val emoji: String,
    val category: GameCategory,
    val coreSkill: String,
    val howItPlays: String,
    val voicePrompt: String,
    val scaling: TierScaling,
    val bestForTiers: Set<AgeTier>,
)
