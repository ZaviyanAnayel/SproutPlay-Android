package com.zaviyanllc.sproutplay.domain.model

data class StickerSpec(
    val id: String,
    val title: String,
    val emoji: String,
    val unlockHint: String,
    val requiredTotalStars: Int = 0,
    val requiredCategory: GameCategory? = null,
    val requiredCategoryGames: Int = 0,
)

object StickerCatalog {
    val allStickers: List<StickerSpec> = listOf(
        StickerSpec("stk_first_star", "First Sprout", "🌱", "Earn your very first star!", requiredTotalStars = 1),
        StickerSpec("stk_sunny", "Sunny Smile", "☀️", "Collect 5 total stars", requiredTotalStars = 5),
        StickerSpec("stk_rainbow", "Rainbow Leap", "🌈", "Collect 10 total stars", requiredTotalStars = 10),
        StickerSpec("stk_rocket", "Star Rocket", "🚀", "Collect 20 total stars", requiredTotalStars = 20),
        StickerSpec("stk_crown", "Gold Crown", "👑", "Collect 35 total stars", requiredTotalStars = 35),
        StickerSpec("stk_galaxy", "Galaxy Hero", "🌌", "Collect 50 total stars", requiredTotalStars = 50),
        StickerSpec("stk_unicorn", "Wonder Unicorn", "🦄", "Collect 75 total stars", requiredTotalStars = 75),
        StickerSpec("stk_trophy", "Grand Champion", "🏆", "Collect 100 total stars", requiredTotalStars = 100),

        StickerSpec("stk_math_1", "Bubble Counter", "🫧", "Play 1 Math game", requiredCategory = GameCategory.MATH_NUMBERS, requiredCategoryGames = 1),
        StickerSpec("stk_math_3", "Number Wizard", "🧮", "Play 3 Math games", requiredCategory = GameCategory.MATH_NUMBERS, requiredCategoryGames = 3),
        StickerSpec("stk_math_5", "Pizza Fraction Pro", "🍕", "Play 5 Math games", requiredCategory = GameCategory.MATH_NUMBERS, requiredCategoryGames = 5),

        StickerSpec("stk_read_1", "Safari Reader", "🦁", "Play 1 Reading game", requiredCategory = GameCategory.READING_PHONICS, requiredCategoryGames = 1),
        StickerSpec("stk_read_3", "Story Owl", "🦉", "Play 3 Reading games", requiredCategory = GameCategory.READING_PHONICS, requiredCategoryGames = 3),
        StickerSpec("stk_read_5", "Word Knight", "🏰", "Play 5 Reading games", requiredCategory = GameCategory.READING_PHONICS, requiredCategoryGames = 5),

        StickerSpec("stk_logic_1", "Puzzle Cub", "🧩", "Play 1 Logic game", requiredCategory = GameCategory.LOGIC_MEMORY, requiredCategoryGames = 1),
        StickerSpec("stk_logic_3", "Maze Fox", "🧭", "Play 3 Logic games", requiredCategory = GameCategory.LOGIC_MEMORY, requiredCategoryGames = 3),
        StickerSpec("stk_logic_5", "Robo Coder", "🤖", "Play 5 Logic games", requiredCategory = GameCategory.LOGIC_MEMORY, requiredCategoryGames = 5),

        StickerSpec("stk_sci_1", "Earth Friend", "🌍", "Play 1 Science game", requiredCategory = GameCategory.SCIENCE_WORLD, requiredCategoryGames = 1),
        StickerSpec("stk_sci_3", "Space Cadet", "🪐", "Play 3 Science games", requiredCategory = GameCategory.SCIENCE_WORLD, requiredCategoryGames = 3),
        StickerSpec("stk_sci_5", "Lab Explorer", "🔬", "Play 5 Science games", requiredCategory = GameCategory.SCIENCE_WORLD, requiredCategoryGames = 5),

        StickerSpec("stk_arc_1", "Pixel Painter", "🎨", "Play 1 Arcade game", requiredCategory = GameCategory.CREATIVE_ARCADE, requiredCategoryGames = 1),
        StickerSpec("stk_arc_3", "Beat Drummer", "🥁", "Play 3 Arcade games", requiredCategory = GameCategory.CREATIVE_ARCADE, requiredCategoryGames = 3),
        StickerSpec("stk_arc_5", "Kindness Star", "💛", "Play 5 Arcade games", requiredCategory = GameCategory.CREATIVE_ARCADE, requiredCategoryGames = 5),
        StickerSpec("stk_daily", "Daily Explorer", "🌟", "Earn 15 total stars", requiredTotalStars = 15),
    )
}
