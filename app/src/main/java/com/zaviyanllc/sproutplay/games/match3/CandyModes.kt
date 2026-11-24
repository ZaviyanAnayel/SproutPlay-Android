package com.zaviyanllc.sproutplay.games.match3

import com.zaviyanllc.sproutplay.domain.model.AgeTier
import com.zaviyanllc.sproutplay.domain.model.GameCategory
import com.zaviyanllc.sproutplay.domain.model.MiniGameSpec
import com.zaviyanllc.sproutplay.domain.model.TierScaling
import kotlin.random.Random

/**
 * The five Candy Crush-style learning games. One shared match-3 board; each mode
 * teaches through what the tiles show and what the level asks the child to collect.
 *
 * - MATH "Number Crunch": tiles are digits — every pop adds its digits to your sum.
 * - WORDS "Word Pop": tiles are letters — pop the letters to spell the word.
 * - LOGIC "Pattern Pop": collect the exact color pattern the level shows.
 * - SCIENCE "Science Pop": collect the nature set (water, sun, leaves...).
 * - ARCADE "Candy Blast": pure juicy score attack, combos and cascades.
 * Each game has 10 levels, so the app still has 50 levels to beat.
 */

enum class CandyMode(
    val id: String,
    val title: String,
    val emoji: String,
    val category: GameCategory,
    val tagline: String,
    val howItPlays: String,
    val voicePrompt: String,
) {
    MATH(
        id = "candy_math",
        title = "Number Crunch",
        emoji = "🔢",
        category = GameCategory.MATH_NUMBERS,
        tagline = "Pop candies to add up to the target number!",
        howItPlays = "Swap candies to match 3 or more. Every candy you pop adds its number to your total — reach the target before your moves run out!",
        voicePrompt = "Match the candies and add up the numbers to reach the target!",
    ),
    WORDS(
        id = "candy_words",
        title = "Word Pop",
        emoji = "🔤",
        category = GameCategory.READING_PHONICS,
        tagline = "Pop the letters to spell the magic word!",
        howItPlays = "Swap candies to match 3 or more. Pop the letters of the magic word — collect every letter to win the level!",
        voicePrompt = "Pop the letters and spell the magic word!",
    ),
    LOGIC(
        id = "candy_logic",
        title = "Pattern Pop",
        emoji = "🧩",
        category = GameCategory.LOGIC_MEMORY,
        tagline = "Complete the color pattern to win!",
        howItPlays = "Swap candies to match 3 or more. Pop exactly the colors the pattern asks for — count carefully!",
        voicePrompt = "Follow the pattern and pop the right colors!",
    ),
    SCIENCE(
        id = "candy_science",
        title = "Science Pop",
        emoji = "🌍",
        category = GameCategory.SCIENCE_WORLD,
        tagline = "Collect water, sunshine and leaves!",
        howItPlays = "Swap candies to match 3 or more. Collect the nature set the level asks for — water drops, suns and leaves!",
        voicePrompt = "Collect the sunshine, water and leaves!",
    ),
    ARCADE(
        id = "candy_arcade",
        title = "Candy Blast",
        emoji = "🍬",
        category = GameCategory.CREATIVE_ARCADE,
        tagline = "Big combos, huge scores, pure candy fun!",
        howItPlays = "Swap candies to match 3 or more. Chain big combos and cascades to smash the target score before your moves run out!",
        voicePrompt = "Make the biggest, sweetest combos you can!",
    );

    companion object {
        fun fromId(id: String): CandyMode = entries.firstOrNull { it.id == id } ?: MATH
    }
}

/** What the level asks the child to do. */
sealed interface CandyGoal {
    fun title(): String
}

data class SumGoal(val target: Int) : CandyGoal {
    override fun title(): String = "Reach $target"
}

data class WordGoal(val word: String) : CandyGoal {
    override fun title(): String = "Spell $word"
}

data class CollectGoal(val needs: Map<String, Int>) : CandyGoal {
    override fun title(): String = needs.entries.joinToString("  ") { "${it.key}×${it.value}" }
}

data class ScoreGoal(val target: Int) : CandyGoal {
    override fun title(): String = "Score $target"
}

/** Live progress toward a goal, updated after every pop. */
data class GoalProgress(
    val sum: Int = 0,
    val letters: Set<Char> = emptySet(),
    val faces: Map<String, Int> = emptyMap(),
    val score: Int = 0,
)

fun applyGoal(goal: CandyGoal, progress: GoalProgress, cleared: List<CandyTile>, gainedScore: Int): GoalProgress {
    return when (goal) {
        is SumGoal -> progress.copy(sum = progress.sum + cleared.sumOf { it.value }, score = progress.score + gainedScore)
        is WordGoal -> {
            val letters = progress.letters + cleared.mapNotNull { it.face.singleOrNull() }
                .filter { it in goal.word }
            progress.copy(letters = letters, score = progress.score + gainedScore)
        }
        is CollectGoal -> {
            val faces = progress.faces.toMutableMap()
            for (tile in cleared) {
                if (tile.face in goal.needs) {
                    faces[tile.face] = (faces[tile.face] ?: 0) + 1
                }
            }
            progress.copy(faces = faces, score = progress.score + gainedScore)
        }
        is ScoreGoal -> progress.copy(score = progress.score + gainedScore)
    }
}

fun isGoalComplete(goal: CandyGoal, progress: GoalProgress): Boolean {
    return when (goal) {
        is SumGoal -> progress.sum >= goal.target
        is WordGoal -> goal.word.toSet().all { it in progress.letters }
        is CollectGoal -> goal.needs.all { (face, need) -> (progress.faces[face] ?: 0) >= need }
        is ScoreGoal -> progress.score >= goal.target
    }
}

fun goalProgressText(goal: CandyGoal, progress: GoalProgress): String {
    return when (goal) {
        is SumGoal -> "${progress.sum} / ${goal.target}"
        is WordGoal -> goal.word.map { if (it in progress.letters) it else '_' }.joinToString(" ")
        is CollectGoal -> goal.needs.entries.joinToString("  ") { (face, need) ->
            "$face ${(progress.faces[face] ?: 0)}/$need"
        }
        is ScoreGoal -> "${progress.score} / ${goal.target}"
    }
}

data class CandyLevel(
    val level: Int,
    val moves: Int,
    val goal: CandyGoal,
    val colors: Int,
    val faceFactory: FaceFactory,
    val hint: String,
)

private val WORD_BANK: Map<AgeTier, List<String>> = mapOf(
    AgeTier.PRESCHOOL to listOf("CAT", "DOG", "SUN", "PIG", "BEE", "EGG", "CUP", "BUS", "HEN", "FOX"),
    AgeTier.K_1 to listOf("FISH", "BIRD", "CAKE", "STAR", "MOON", "FROG", "LION", "BEAR", "KITE", "DUCK"),
    AgeTier.GRADE_2_3 to listOf("TIGER", "OCEAN", "MUSIC", "ROBOT", "APPLE", "HORSE", "CLOUD", "PIZZA", "DANCE", "MAGIC"),
    AgeTier.GRADE_4_5 to listOf("PLANET", "RAINBOW", "SCIENCE", "DRAGON", "PUZZLE", "GARDEN", "ROCKET", "CASTLE", "BANANA", "GUITAR"),
)

private val SCIENCE_FACES = listOf("💧", "☀️", "🌱", "🌙", "⭐", "🪐")
private val ARCADE_FACES = listOf("🍬", "🍭", "🍩", "🧁", "🍪", "🍫")
private val LOGIC_FACES = listOf("●", "▲", "■", "★", "◆", "♥")

private val COLOR_FACES: Map<CandyColor, String> = mapOf(
    CandyColor.RED to "●",
    CandyColor.PINK to "♥",
    CandyColor.ORANGE to "▲",
    CandyColor.YELLOW to "★",
    CandyColor.GREEN to "◆",
    CandyColor.BLUE to "■",
    CandyColor.PURPLE to "●",
)

/** Build the level config for a mode + level (1..10) + age tier. */
fun levelFor(mode: CandyMode, level: Int, tier: AgeTier): CandyLevel {
    val t = tier.levelIndex // 0..3
    val colors = when {
        level <= 3 -> 5
        level <= 7 -> 6
        else -> 6
    }
    return when (mode) {
        CandyMode.MATH -> {
            val maxDigit = if (tier == AgeTier.PRESCHOOL) 5 else 9
            val target = 25 + level * 12 + t * 25
            CandyLevel(
                level = level,
                moves = 22 + level,
                goal = SumGoal(target),
                colors = colors,
                faceFactory = { _, random ->
                    val d = random.nextInt(1, maxDigit + 1)
                    d.toString() to d
                },
                hint = "Pop candies to add up to $target!",
            )
        }
        CandyMode.WORDS -> {
            val bank = WORD_BANK[tier] ?: WORD_BANK[AgeTier.K_1]!!
            val word = bank[(level - 1) % bank.size]
            CandyLevel(
                level = level,
                moves = 24 + level,
                goal = WordGoal(word),
                colors = colors,
                faceFactory = { _, random ->
                    val letter = if (random.nextFloat() < 0.6f) {
                        word[random.nextInt(word.length)]
                    } else {
                        ('A'..'Z').random(random)
                    }
                    letter.toString() to 1
                },
                hint = "Spell the word: $word",
            )
        }
        CandyMode.LOGIC -> {
            val base = 3 + level + t
            val needs = mapOf("●" to base, "▲" to base, "■" to base)
            CandyLevel(
                level = level,
                moves = 24 + level,
                goal = CollectGoal(needs),
                colors = colors,
                faceFactory = { color, _ ->
                    (COLOR_FACES[color] ?: "●") to 1
                },
                hint = "Collect the pattern: ●×$base  ▲×$base  ■×$base",
            )
        }
        CandyMode.SCIENCE -> {
            val drops = 5 + level + t
            val suns = 4 + level + t
            val leaves = 4 + level + t
            val needs = mapOf("💧" to drops, "☀️" to suns, "🌱" to leaves)
            CandyLevel(
                level = level,
                moves = 24 + level,
                goal = CollectGoal(needs),
                colors = colors,
                faceFactory = { _, random ->
                    val face = SCIENCE_FACES[random.nextInt(SCIENCE_FACES.size)]
                    face to 1
                },
                hint = "Collect 💧×$drops  ☀️×$suns  🌱×$leaves",
            )
        }
        CandyMode.ARCADE -> {
            val target = 800 + level * 400 + t * 500
            CandyLevel(
                level = level,
                moves = 20 + level,
                goal = ScoreGoal(target),
                colors = colors,
                faceFactory = { _, random ->
                    val face = ARCADE_FACES[random.nextInt(ARCADE_FACES.size)]
                    face to 1
                },
                hint = "Score $target points!",
            )
        }
    }
}

/** Catalog entry for one candy game (MiniGameSpec kept so Home/Catalog keep working). */
fun CandyMode.toSpec(number: Int): MiniGameSpec = MiniGameSpec(
    id = id,
    number = number,
    title = title,
    emoji = emoji,
    category = category,
    coreSkill = tagline,
    howItPlays = howItPlays,
    voicePrompt = voicePrompt,
    scaling = TierScaling(
        preschool = "10 sweet levels, big candies, easy goals",
        k1 = "10 sweet levels, more colors, bigger goals",
        grade23 = "10 sweet levels, tricky goals, more moves to plan",
        grade45 = "10 sweet levels, master goals, candy combos",
    ),
    bestForTiers = AgeTier.entries.toSet(),
)

/** Per-level progress key stored in Room (e.g. "candy_math_3"). */
fun levelGameId(mode: CandyMode, level: Int): String = "${mode.id}_$level"
