package com.zaviyanllc.sproutplay.ui.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zaviyanllc.sproutplay.audio.TrapAudioManager
import com.zaviyanllc.sproutplay.domain.model.BallColor
import com.zaviyanllc.sproutplay.domain.model.CupLayout
import com.zaviyanllc.sproutplay.domain.model.TwinTrapLevels
import com.zaviyanllc.sproutplay.games.cup.CupGameEngine
import com.zaviyanllc.sproutplay.games.cup.CupView
import com.zaviyanllc.sproutplay.ui.theme.StarGold
import com.zaviyanllc.sproutplay.ui.theme.TrapGold
import com.zaviyanllc.sproutplay.ui.theme.TrapMuted
import com.zaviyanllc.sproutplay.ui.theme.TrapPink
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

private enum class CupPhase { SHOW, COUNTDOWN, SHUFFLE, GUESS, REVEAL, RESULT }

private sealed interface LevelOutcome {
    data class Win(val stars: Int, val coins: Int) : LevelOutcome
    data object Fail : LevelOutcome
}

private const val ANSWER_TIME_MS = 15_000L

private data class BoardLayout(
    val cupW: Float,
    val cupH: Float,
    val slotCenters: List<Offset>,
    val orbitCenter: Offset,
    val orbitRadius: Float,
)

private fun computeLayout(
    cupCount: Int,
    layout: CupLayout,
    maxW: Float,
    maxH: Float,
    gapPx: Float,
): BoardLayout {
    return when (layout) {
        CupLayout.GRID -> {
            val cols = 3
            val rows = (cupCount + cols - 1) / cols
            var cupW = (maxW - gapPx * (cols + 1)) / cols
            var cupH = cupW * 1.16f
            var totalH = rows * cupH + gapPx * (rows - 1)
            if (totalH > maxH) {
                val s = maxH / totalH
                cupW *= s
                cupH *= s
                totalH = maxH
            }
            val x0 = (maxW - (cols * cupW + (cols - 1) * gapPx)) / 2
            val y0 = (maxH - totalH) / 2
            val centers = CupGameEngine.gridSlots(cupCount, cols).map { (c, r) ->
                Offset(x0 + c * (cupW + gapPx) + cupW / 2, y0 + r * (cupH + gapPx) + cupH / 2)
            }
            BoardLayout(cupW, cupH, centers, Offset(maxW / 2, maxH / 2), 0f)
        }
        CupLayout.TWO_ROWS -> {
            val cols = 3
            var cupW = (maxW - gapPx * (cols + 1)) / cols
            var cupH = cupW * 1.16f
            var rowGap = cupH * 0.55f
            var totalH = 2 * cupH + rowGap
            if (totalH > maxH) {
                val s = maxH / totalH
                cupW *= s
                cupH *= s
                rowGap *= s
                totalH = maxH
            }
            val x0 = (maxW - (cols * cupW + (cols - 1) * gapPx)) / 2
            val y0 = (maxH - totalH) / 2
            val centers = CupGameEngine.twoRowSlots(cupCount).map { (c, r) ->
                Offset(x0 + c * (cupW + gapPx) + cupW / 2, y0 + r * (cupH + rowGap) + cupH / 2)
            }
            BoardLayout(cupW, cupH, centers, Offset(maxW / 2, maxH / 2), 0f)
        }
        CupLayout.CIRCLE -> {
            val cupW = minOf(maxW / 4.6f, maxH / 5.4f)
            val cupH = cupW * 1.16f
            val center = Offset(maxW / 2, maxH * 0.46f)
            val radius = minOf(maxW, maxH) * 0.33f
            val centers = CupGameEngine.circleAngles(cupCount, 0f).map { a ->
                val (ux, uy) = CupGameEngine.circlePoint(a)
                Offset(center.x + ux * radius, center.y + uy * radius)
            }
            BoardLayout(cupW, cupH, centers, center, radius)
        }
    }
}

/** Dark numbers on bright cups, white numbers on dark blue/purple cups (reference). */
private fun numberColorFor(levelNumber: Int): Color =
    if (levelNumber in setOf(1, 2, 4, 7, 8, 10)) Color.White else Color(0xFF0A1628)

/**
 * The cup game: watch the balls (2s) → countdown 3-2-1 → shuffle with music
 * → tap the cup(s) hiding the ball(s) within 15s → reveal with glow ring.
 * 3 wrong picks (or timeouts) = retry the level.
 */
@Composable
fun CupGameScreen(
    level: Int,
    audio: TrapAudioManager,
    onBack: () -> Unit,
    onLevelComplete: (stars: Int, coins: Int) -> Unit,
    onNextLevel: (() -> Unit)?,
    onOpenLevels: () -> Unit,
) {
    var roundId by remember(level) { mutableIntStateOf(0) }
    key(level, roundId) {
        CupGameRound(
            level = level,
            audio = audio,
            onBack = onBack,
            onLevelComplete = onLevelComplete,
            onNextLevel = onNextLevel,
            onOpenLevels = onOpenLevels,
            onReplay = { roundId++ },
        )
    }
}

@Composable
private fun CupGameRound(
    level: Int,
    audio: TrapAudioManager,
    onBack: () -> Unit,
    onLevelComplete: (stars: Int, coins: Int) -> Unit,
    onNextLevel: (() -> Unit)?,
    onOpenLevels: () -> Unit,
    onReplay: () -> Unit,
) {
    val spec = TwinTrapLevels.get(level)
    val random = remember { Random(System.nanoTime()) }

    var phase by remember { mutableStateOf(CupPhase.SHOW) }
    var countdown by remember { mutableIntStateOf(0) }
    var remainingMs by remember { mutableLongStateOf(ANSWER_TIME_MS) }
    var strikes by remember { mutableIntStateOf(0) }
    var foundCups by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var wrongCups by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var shakeKeys by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }
    var result by remember { mutableStateOf<LevelOutcome?>(null) }

    val ballCups = remember {
        if (spec.layout == CupLayout.TWO_ROWS) {
            // Split Brain: gold only in the top row, pink only in the bottom.
            CupGameEngine.assignBallsConstrained(
                balls = spec.balls,
                allowedCups = listOf(setOf(0, 1, 2), setOf(3, 4, 5)),
                random = random,
            )
        } else {
            CupGameEngine.assignBalls(spec.cupCount, spec.balls, random)
        }
    }
    val swaps = remember { CupGameEngine.generateSwaps(spec.cupCount, spec.swaps, random) }

    DisposableEffect(Unit) { onDispose { audio.stopMusic() } }

    fun failLevel() {
        result = LevelOutcome.Fail
        audio.playLose()
        phase = CupPhase.REVEAL
    }

    fun onCupTap(cupIndex: Int) {
        if (phase != CupPhase.GUESS) return
        if (cupIndex in foundCups || cupIndex in wrongCups) return
        val ball = ballCups[cupIndex]
        if (ball != null) {
            audio.playPop()
            val found = foundCups + cupIndex
            foundCups = found
            if (found.size == spec.balls.size) {
                val stars = TwinTrapLevels.starsForStrikes(strikes)
                val coins = TwinTrapLevels.coinsFor(spec.number, found.size)
                result = LevelOutcome.Win(stars, coins)
                audio.playWin()
                phase = CupPhase.REVEAL
            }
        } else {
            audio.playWrong()
            wrongCups = wrongCups + cupIndex
            shakeKeys = shakeKeys + (cupIndex to ((shakeKeys[cupIndex] ?: 0) + 1))
            strikes++
            if (strikes >= 3) failLevel()
        }
    }

    fun onTimeUp() {
        if (phase != CupPhase.GUESS) return
        audio.playWrong()
        strikes++
        if (strikes >= 3) failLevel() else remainingMs = ANSWER_TIME_MS
    }

    // Record a win exactly once.
    LaunchedEffect(result) {
        val win = result as? LevelOutcome.Win ?: return@LaunchedEffect
        onLevelComplete(win.stars, win.coins)
    }

    // Answer timer.
    LaunchedEffect(phase, strikes) {
        if (phase != CupPhase.GUESS) return@LaunchedEffect
        while (remainingMs > 0) {
            delay(100)
            remainingMs -= 100
        }
        onTimeUp()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(spec.backgroundCenter, spec.background),
                    radius = 900f,
                ),
            ),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ---- Header ----
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { audio.playTap(); onBack() }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "LEVEL ${spec.number} / 10",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TrapMuted,
                    )
                    Text(
                        text = spec.title,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                    )
                }
                Text(
                    text = "✕".repeat(strikes) + "○".repeat((3 - strikes).coerceAtLeast(0)),
                    fontSize = 16.sp,
                    color = if (strikes > 0) Color(0xFFFF4D4D) else TrapMuted,
                    modifier = Modifier.padding(end = 8.dp),
                )
            }

            // Subtitle with the colored keyword
            Text(
                text = buildAnnotatedString {
                    append(spec.subtitleBefore)
                    withStyle(
                        SpanStyle(
                            color = spec.keywordColor ?: Color.White,
                            fontWeight = FontWeight.ExtraBold,
                        ),
                    ) { append(spec.keyword) }
                    append(spec.subtitleAfter)
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            // ---- Board ----
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                val density = LocalDensity.current
                val maxW = with(density) { maxWidth.toPx() }
                val maxH = with(density) { maxHeight.toPx() }
                val gapPx = with(density) { 12.dp.toPx() }
                val layout = remember(spec, maxW, maxH) {
                    computeLayout(spec.cupCount, spec.layout, maxW, maxH, gapPx)
                }
                val cupAnims = remember(spec, layout) {
                    layout.slotCenters.map { center ->
                        Animatable(
                            Offset(center.x - layout.cupW / 2, center.y - layout.cupH / 2),
                            Offset.VectorConverter,
                        )
                    }
                }

                fun centersFor(angleDeg: Float): List<Offset> =
                    if (spec.layout == CupLayout.CIRCLE) {
                        CupGameEngine.circleAngles(spec.cupCount, angleDeg).map { a ->
                            val (ux, uy) = CupGameEngine.circlePoint(a)
                            Offset(
                                layout.orbitCenter.x + ux * layout.orbitRadius,
                                layout.orbitCenter.y + uy * layout.orbitRadius,
                            )
                        }
                    } else {
                        layout.slotCenters
                    }

                fun topLeft(center: Offset): Offset =
                    Offset(center.x - layout.cupW / 2, center.y - layout.cupH / 2)

                // Phase driver: show -> countdown -> shuffle -> guess -> reveal -> result.
                LaunchedEffect(phase) {
                    when (phase) {
                        CupPhase.SHOW -> {
                            delay(2000)
                            phase = CupPhase.COUNTDOWN
                        }
                        CupPhase.COUNTDOWN -> {
                            for (n in 3 downTo 1) {
                                countdown = n
                                audio.playTick()
                                delay(600)
                            }
                            countdown = 0
                            phase = CupPhase.SHUFFLE
                        }
                        CupPhase.SHUFFLE -> {
                            audio.startMusic()
                            var slots = List(spec.cupCount) { it }
                            var angle = 0f
                            for ((a, b) in swaps) {
                                audio.playWhoosh()
                                val targetAngle = if (spec.layout == CupLayout.CIRCLE) angle + 40f else angle
                                val centers = centersFor(targetAngle)
                                coroutineScope {
                                    launch {
                                        cupAnims[a].animateTo(
                                            topLeft(centers[slots[b]]),
                                            tween(durationMillis = spec.swapIntervalMs.toInt(), easing = LinearEasing),
                                        )
                                    }
                                    launch {
                                        cupAnims[b].animateTo(
                                            topLeft(centers[slots[a]]),
                                            tween(durationMillis = spec.swapIntervalMs.toInt(), easing = LinearEasing),
                                        )
                                    }
                                }
                                slots = CupGameEngine.applySwap(slots, a to b)
                                angle = targetAngle
                            }
                            audio.stopMusic()
                            phase = CupPhase.GUESS
                        }
                        CupPhase.GUESS -> Unit
                        CupPhase.REVEAL -> {
                            delay(1900)
                            phase = CupPhase.RESULT
                        }
                        CupPhase.RESULT -> Unit
                    }
                }

                // Cups (animated positions)
                for (cupIndex in 0 until spec.cupCount) {
                    val anim = cupAnims[cupIndex]
                    val isBallCup = ballCups.containsKey(cupIndex)
                    val showBall = phase == CupPhase.SHOW && isBallCup
                    val revealed = phase == CupPhase.REVEAL || phase == CupPhase.RESULT
                    val lifted = showBall || (revealed && isBallCup)
                    val glow = if (revealed && isBallCup) ballCups[cupIndex]?.color else null
                    val showNumber = phase == CupPhase.GUESS || phase == CupPhase.REVEAL
                    val dimmed = spec.dimmed && phase == CupPhase.SHUFFLE
                    Box(
                        modifier = Modifier
                            .size(
                                width = with(density) { layout.cupW.toDp() },
                                height = with(density) { layout.cupH.toDp() },
                            )
                            .offset { IntOffset(anim.value.x.roundToInt(), anim.value.y.roundToInt()) }
                            .clickable { onCupTap(cupIndex) }
                            .testTag("cup_${cupIndex + 1}"),
                    ) {
                        CupView(
                            style = spec.style,
                            base = spec.cupBase,
                            light = spec.cupLight,
                            dark = spec.cupDark,
                            stripeColor = spec.stripeColor,
                            number = if (showNumber) cupIndex + 1 else null,
                            numberColor = numberColorFor(spec.number),
                            ball = if (isBallCup) ballCups[cupIndex] else null,
                            lifted = lifted,
                            glowColor = glow,
                            dimmed = dimmed,
                            shakeKey = shakeKeys[cupIndex] ?: 0,
                            modifier = Modifier.fillMaxSize(),
                        )
                        // Picked markers
                        if (cupIndex in foundCups) {
                            Text(
                                text = "✓",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF22C55E),
                                modifier = Modifier.align(Alignment.TopEnd),
                            )
                        }
                        if (cupIndex in wrongCups) {
                            Text(
                                text = "✕",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFF4D4D),
                                modifier = Modifier.align(Alignment.TopEnd),
                            )
                        }
                    }
                }

                // Dimmed shuffle scrim
                if (spec.dimmed && phase == CupPhase.SHUFFLE) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.30f)),
                    )
                }

                // Countdown overlay
                if (phase == CupPhase.COUNTDOWN && countdown > 0) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "$countdown",
                            fontSize = 120.sp,
                            fontWeight = FontWeight.Black,
                            color = TrapGold,
                            modifier = Modifier.testTag("countdown_text"),
                        )
                    }
                }
            }

            // ---- Bottom status / prompt ----
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                when (phase) {
                    CupPhase.SHOW -> Text(
                        text = "WATCH CLOSELY…",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                    CupPhase.COUNTDOWN -> Text(
                        text = "GET READY…",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                    CupPhase.SHUFFLE -> Text(
                        text = "FOLLOW THE BALL${if (spec.balls.size > 1) "S" else ""}…",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                    CupPhase.GUESS -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (spec.balls.size > 1) "WHERE ARE BOTH?" else "WHICH CUP?",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = if (spec.balls.size > 1) "Tap two cups" else "Tap the cup",
                            fontSize = 14.sp,
                            color = TrapMuted,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFF22345A))
                                .testTag("guess_timer_bar"),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth((remainingMs / ANSWER_TIME_MS.toFloat()).coerceIn(0f, 1f))
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(spec.balls.first().color),
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${(remainingMs / 1000)}s  •  ${foundCups.size}/${spec.balls.size} found",
                            fontSize = 13.sp,
                            color = TrapMuted,
                        )
                    }
                    CupPhase.REVEAL, CupPhase.RESULT -> {
                        val revealText = if (spec.balls.size == 1) {
                            val cup = ballCups.entries.first { it.value == spec.balls.first() }.key + 1
                            "IT WAS CUP $cup"
                        } else {
                            spec.balls.joinToString(" • ") { ball ->
                                val cup = ballCups.entries.first { it.value == ball }.key + 1
                                "${ball.label} $cup"
                            }
                        }
                        Text(
                            text = revealText,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TrapGold,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }

        // ---- Result overlay ----
        if (phase == CupPhase.RESULT) {
            val win = result as? LevelOutcome.Win
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = Color(0xFF101B31),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp)
                        .testTag("result_card"),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        if (win != null) {
                            Text(text = "🎉", fontSize = 48.sp)
                            Text(
                                text = "LEVEL COMPLETE!",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row {
                                repeat(3) { idx ->
                                    Icon(
                                        imageVector = Icons.Rounded.Star,
                                        contentDescription = null,
                                        tint = if (idx < win.stars) StarGold else Color(0xFF334155),
                                        modifier = Modifier.size(44.dp),
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "+${win.coins} coins",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TrapGold,
                            )
                            if (spec.trophy != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "🏆 ${spec.trophy} UNLOCKED!",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TrapGold,
                                    textAlign = TextAlign.Center,
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            if (onNextLevel != null) {
                                Button(
                                    onClick = { audio.playTap(); onNextLevel() },
                                    shape = RoundedCornerShape(18.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = TrapGold, contentColor = Color(0xFF1A1400)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                ) {
                                    Text("NEXT LEVEL", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        } else {
                            Text(text = "💥", fontSize = 48.sp)
                            Text(
                                text = "SO CLOSE!",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "3 wrong picks — the balls were under cups " +
                                    spec.balls.joinToString(" and ") { ball ->
                                        "${ballCups.entries.first { it.value == ball }.key + 1}"
                                    } + ".",
                                fontSize = 15.sp,
                                color = TrapMuted,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp),
                        ) {
                            OutlinedButton(
                                onClick = { audio.playTap(); onReplay() },
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                            ) {
                                Icon(Icons.Rounded.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (win != null) "REPLAY" else "RETRY")
                            }
                            Button(
                                onClick = { audio.playTap(); onOpenLevels() },
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22345A)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                            ) {
                                Icon(Icons.Rounded.Home, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("LEVELS")
                            }
                        }
                    }
                }
            }
        }
    }
}
