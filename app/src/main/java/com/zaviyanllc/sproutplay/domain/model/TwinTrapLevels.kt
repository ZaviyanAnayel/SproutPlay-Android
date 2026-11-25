package com.zaviyanllc.sproutplay.domain.model

import androidx.compose.ui.graphics.Color

/** The three balls a player can be asked to follow. */
enum class BallColor(
    val id: String,
    val label: String,
    val color: Color,
    val light: Color,
    val dark: Color,
) {
    GOLD("gold", "GOLD", Color(0xFFFFD700), Color(0xFFFFF3B0), Color(0xFFB8860B)),
    PINK("pink", "PINK", Color(0xFFFF2E88), Color(0xFFFFB3D1), Color(0xFFB3125E)),
    LIME("lime", "LIME", Color(0xFFA3E635), Color(0xFFDFFFB0), Color(0xFF4D7C0F)),
}

enum class CupLayout { GRID, TWO_ROWS, CIRCLE }

enum class CupStyle { SOLID, STRIPED, RIDGED }

/**
 * One Twin Trap level. Cup colors match the @spotquest reference videos
 * (teal / blue / green / rose / purple / red / dark purple cups on a dark
 * navy background) — the reference look, drawn by us, never their watermark.
 */
data class TwinTrapLevel(
    val number: Int,
    val title: String,
    /** Subtitle split so the keyword can be tinted: before + keyword + after. */
    val subtitleBefore: String,
    val keyword: String,
    val keywordColor: Color?,
    val subtitleAfter: String,
    val cupCount: Int,
    val balls: List<BallColor>,
    val swaps: Int,
    val swapIntervalMs: Long,
    val dimmed: Boolean,
    val layout: CupLayout,
    val style: CupStyle,
    val cupBase: Color,
    val cupLight: Color,
    val cupDark: Color,
    val stripeColor: Color?,
    val background: Color,
    val backgroundCenter: Color,
    /** Trophy name awarded on completion, or null. */
    val trophy: String?,
)

object TwinTrapLevels {
    private val navy = Color(0xFF0A1628)
    private val navyCenter = Color(0xFF122A4A)
    private val blueBase = Color(0xFF2E6BFF)
    private val blueLight = Color(0xFF7FB0FF)
    private val blueDark = Color(0xFF12275E)

    val all: List<TwinTrapLevel> = listOf(
        TwinTrapLevel(
            number = 1, title = "FIRST LOOK",
            subtitleBefore = "FOLLOW THE ", keyword = "GOLD", keywordColor = BallColor.GOLD.color,
            subtitleAfter = " BALL",
            cupCount = 6, balls = listOf(BallColor.GOLD), swaps = 14, swapIntervalMs = 700,
            dimmed = false, layout = CupLayout.GRID, style = CupStyle.SOLID,
            cupBase = blueBase, cupLight = blueLight, cupDark = blueDark, stripeColor = null,
            background = navy, backgroundCenter = navyCenter, trophy = null,
        ),
        TwinTrapLevel(
            number = 2, title = "TWIN TROUBLE",
            subtitleBefore = "FOLLOW ", keyword = "GOLD", keywordColor = BallColor.GOLD.color,
            subtitleAfter = " AND PINK",
            cupCount = 6, balls = listOf(BallColor.GOLD, BallColor.PINK), swaps = 16,
            swapIntervalMs = 600, dimmed = false, layout = CupLayout.GRID, style = CupStyle.SOLID,
            cupBase = blueBase, cupLight = blueLight, cupDark = blueDark, stripeColor = null,
            background = navy, backgroundCenter = navyCenter, trophy = null,
        ),
        TwinTrapLevel(
            number = 3, title = "LIGHTS OUT",
            subtitleBefore = "FOLLOW ", keyword = "PINK", keywordColor = BallColor.PINK.color,
            subtitleAfter = " IN THE DARK",
            cupCount = 6, balls = listOf(BallColor.PINK), swaps = 14, swapIntervalMs = 600,
            dimmed = true, layout = CupLayout.GRID, style = CupStyle.RIDGED,
            cupBase = Color(0xFF4ADE80), cupLight = Color(0xFFB8F58A), cupDark = Color(0xFF14532D),
            stripeColor = null,
            background = Color(0xFF071507), backgroundCenter = Color(0xFF12351A), trophy = null,
        ),
        TwinTrapLevel(
            number = 4, title = "SPEED RUN",
            subtitleBefore = "FOLLOW THE ", keyword = "GOLD", keywordColor = BallColor.GOLD.color,
            subtitleAfter = " BALL",
            cupCount = 6, balls = listOf(BallColor.GOLD), swaps = 20, swapIntervalMs = 450,
            dimmed = false, layout = CupLayout.GRID, style = CupStyle.SOLID,
            cupBase = blueBase, cupLight = blueLight, cupDark = blueDark, stripeColor = null,
            background = navy, backgroundCenter = navyCenter, trophy = null,
        ),
        TwinTrapLevel(
            number = 5, title = "NINE CUPS",
            subtitleBefore = "NINE CUPS, ONE ", keyword = "GOLD", keywordColor = BallColor.GOLD.color,
            subtitleAfter = " BALL",
            cupCount = 9, balls = listOf(BallColor.GOLD), swaps = 22, swapIntervalMs = 500,
            dimmed = false, layout = CupLayout.GRID, style = CupStyle.SOLID,
            cupBase = Color(0xFF14B8A6), cupLight = Color(0xFF7DE8DC), cupDark = Color(0xFF0B4F47),
            stripeColor = null,
            background = Color(0xFF06231F), backgroundCenter = Color(0xFF0C3B34),
            trophy = "Halfway Hero",
        ),
        TwinTrapLevel(
            number = 6, title = "SPLIT BRAIN",
            subtitleBefore = "TRACK ", keyword = "GOLD", keywordColor = BallColor.GOLD.color,
            subtitleAfter = " AND PINK",
            cupCount = 6, balls = listOf(BallColor.GOLD, BallColor.PINK), swaps = 18,
            swapIntervalMs = 550, dimmed = false, layout = CupLayout.TWO_ROWS, style = CupStyle.STRIPED,
            cupBase = Color(0xFF14B8A6), cupLight = Color(0xFF7DE8DC), cupDark = Color(0xFF0B4F47),
            stripeColor = Color(0xFFE0F7F4),
            background = Color(0xFF06231F), backgroundCenter = Color(0xFF0C3B34), trophy = null,
        ),
        TwinTrapLevel(
            number = 7, title = "ORBIT",
            subtitleBefore = "NINE CUPS, ", keyword = "NEVER", keywordColor = Color.White,
            subtitleAfter = " STOP SPINNING",
            cupCount = 9, balls = listOf(BallColor.GOLD), swaps = 24, swapIntervalMs = 450,
            dimmed = false, layout = CupLayout.CIRCLE, style = CupStyle.SOLID,
            cupBase = Color(0xFFF43F5E), cupLight = Color(0xFFFDA4AF), cupDark = Color(0xFF7F1D2D),
            stripeColor = null,
            background = Color(0xFF260A12), backgroundCenter = Color(0xFF3D1220), trophy = null,
        ),
        TwinTrapLevel(
            number = 8, title = "BRAIN BREAKER",
            subtitleBefore = "TWELVE CUPS, ONE ", keyword = "GOLD", keywordColor = BallColor.GOLD.color,
            subtitleAfter = " BALL",
            cupCount = 12, balls = listOf(BallColor.GOLD), swaps = 26, swapIntervalMs = 400,
            dimmed = false, layout = CupLayout.GRID, style = CupStyle.STRIPED,
            cupBase = Color(0xFF7C3AED), cupLight = Color(0xFFC4B5FD), cupDark = Color(0xFF3B1470),
            stripeColor = BallColor.GOLD.color,
            background = Color(0xFF150829), backgroundCenter = Color(0xFF221040), trophy = null,
        ),
        TwinTrapLevel(
            number = 9, title = "BOSS LEVEL",
            subtitleBefore = "TWELVE CUPS, ONE ", keyword = "LIME", keywordColor = BallColor.LIME.color,
            subtitleAfter = " BALL",
            cupCount = 12, balls = listOf(BallColor.LIME), swaps = 30, swapIntervalMs = 350,
            dimmed = false, layout = CupLayout.GRID, style = CupStyle.RIDGED,
            cupBase = Color(0xFFDC2626), cupLight = Color(0xFFFCA5A5), cupDark = Color(0xFF5B0E0E),
            stripeColor = null,
            background = Color(0xFF220808), backgroundCenter = Color(0xFF3A0E0E), trophy = null,
        ),
        TwinTrapLevel(
            number = 10, title = "IMPOSSIBLE",
            subtitleBefore = "TWELVE CUPS, ", keyword = "TWO BALLS", keywordColor = BallColor.PINK.color,
            subtitleAfter = ", NO MERCY",
            cupCount = 12, balls = listOf(BallColor.GOLD, BallColor.PINK), swaps = 36,
            swapIntervalMs = 300, dimmed = true, layout = CupLayout.GRID, style = CupStyle.SOLID,
            cupBase = Color(0xFF4C1D95), cupLight = Color(0xFFA78BFA), cupDark = Color(0xFF1E0A3C),
            stripeColor = null,
            background = Color(0xFF0D0618), backgroundCenter = Color(0xFF1A0B30),
            trophy = "Top 10% Club",
        ),
    )

    fun get(number: Int): TwinTrapLevel = all.firstOrNull { it.number == number } ?: all.first()

    /** 10 coins per level per ball found. */
    fun coinsFor(levelNumber: Int, ballsFound: Int): Int = 10 * levelNumber * ballsFound

    /** 0 strikes = 3 stars, 1 = 2 stars, 2 = 1 star. */
    fun starsForStrikes(strikes: Int): Int = when (strikes) {
        0 -> 3
        1 -> 2
        2 -> 1
        else -> 0
    }

    fun gameId(levelNumber: Int): String = "twintrap_$levelNumber"

    fun trophyFor(levelNumber: Int): String? = get(levelNumber).trophy
}
