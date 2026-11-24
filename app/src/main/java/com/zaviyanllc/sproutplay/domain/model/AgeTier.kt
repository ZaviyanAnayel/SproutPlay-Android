package com.zaviyanllc.sproutplay.domain.model

import androidx.compose.ui.graphics.Color

enum class AgeTier(
    val levelIndex: Int,
    val title: String,
    val shortTitle: String,
    val ageRangeLabel: String,
    val ages: IntRange,
    val emoji: String,
    val summary: String,
    val roundsPerGame: Int,
    val prefersMinimalText: Boolean,
) {
    PRESCHOOL(
        levelIndex = 0,
        title = "Pre-School",
        shortTitle = "Pre-K",
        ageRangeLabel = "Ages 3–4",
        ages = 3..4,
        emoji = "🐣",
        summary = "Big tap targets, colors, shapes, animal sounds & counting 1–10",
        roundsPerGame = 4,
        prefersMinimalText = true,
    ),
    K_1(
        levelIndex = 1,
        title = "Kindergarten – 1st Grade",
        shortTitle = "K–1st",
        ageRangeLabel = "Ages 5–7",
        ages = 5..7,
        emoji = "🦊",
        summary = "Phonics, sight words, adding & subtracting within 20, patterns",
        roundsPerGame = 5,
        prefersMinimalText = false,
    ),
    GRADE_2_3(
        levelIndex = 2,
        title = "2nd – 3rd Grade",
        shortTitle = "2nd–3rd",
        ageRangeLabel = "Ages 8–9",
        ages = 8..9,
        emoji = "🦉",
        summary = "Multiplication, telling time, fractions, spelling & science labs",
        roundsPerGame = 6,
        prefersMinimalText = false,
    ),
    GRADE_4_5(
        levelIndex = 3,
        title = "4th – 5th Grade",
        shortTitle = "4th–5th",
        ageRangeLabel = "Ages 10–11",
        ages = 10..11,
        emoji = "🚀",
        summary = "Decimals, percentages, logic grids, coding & world discovery",
        roundsPerGame = 6,
        prefersMinimalText = false,
    );

    companion object {
        fun fromAge(age: Int): AgeTier = when {
            age <= 4 -> PRESCHOOL
            age <= 7 -> K_1
            age <= 9 -> GRADE_2_3
            else -> GRADE_4_5
        }
    }
}

enum class ChildAvatar(
    val id: String,
    val label: String,
    val emoji: String,
    val bgColor: Color,
) {
    OWL("owl", "Sunny Owl", "🦉", Color(0xFFFEF3C7)),
    FOX("fox", "Clever Fox", "🦊", Color(0xFFFFEDD5)),
    BEAR("bear", "Astro Bear", "🐻", Color(0xFFDBEAFE)),
    DINO("dino", "Sprout Dino", "🦕", Color(0xFFD1FAE5)),
    CAT("cat", "Cosmic Cat", "🐱", Color(0xFFEDE9FE)),
    WHALE("whale", "Splash Whale", "🐳", Color(0xFFCFFAFE)),
    BUNNY("bunny", "Hop Bunny", "🐰", Color(0xFFFCE7F3)),
    PANDA("panda", "Bamboo Panda", "🐼", Color(0xFFE2E8F0));

    companion object {
        fun fromId(id: String): ChildAvatar =
            entries.firstOrNull { it.id == id } ?: OWL
    }
}
