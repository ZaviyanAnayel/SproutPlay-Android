package com.zaviyanllc.sproutplay.ui.game

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zaviyanllc.sproutplay.domain.model.AgeTier
import com.zaviyanllc.sproutplay.games.engine.PromptBannerCard
import com.zaviyanllc.sproutplay.games.match3.CandyColor
import com.zaviyanllc.sproutplay.games.match3.CandyMode
import com.zaviyanllc.sproutplay.games.match3.GoalProgress
import com.zaviyanllc.sproutplay.games.match3.Match3Board
import com.zaviyanllc.sproutplay.games.match3.Pos
import com.zaviyanllc.sproutplay.games.match3.Special
import com.zaviyanllc.sproutplay.games.match3.applyGoal
import com.zaviyanllc.sproutplay.games.match3.goalProgressText
import com.zaviyanllc.sproutplay.games.match3.isGoalComplete
import com.zaviyanllc.sproutplay.games.match3.levelFor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun candyColor(color: CandyColor): Color = when (color) {
    CandyColor.RED -> Color(0xFFFF4757)
    CandyColor.PINK -> Color(0xFFFF6B9D)
    CandyColor.ORANGE -> Color(0xFFFF9F43)
    CandyColor.YELLOW -> Color(0xFFFFD32A)
    CandyColor.GREEN -> Color(0xFF2ED573)
    CandyColor.BLUE -> Color(0xFF1E90FF)
    CandyColor.PURPLE -> Color(0xFFA55EEA)
}

private fun modeBackground(mode: CandyMode): Brush = when (mode) {
    CandyMode.MATH -> Brush.verticalGradient(listOf(Color(0xFFDBEAFE), Color(0xFFEDE9FE)))
    CandyMode.WORDS -> Brush.verticalGradient(listOf(Color(0xFFFCE7F3), Color(0xFFFFEDD5)))
    CandyMode.LOGIC -> Brush.verticalGradient(listOf(Color(0xFFEDE9FE), Color(0xFFFCE7F3)))
    CandyMode.SCIENCE -> Brush.verticalGradient(listOf(Color(0xFFD1FAE5), Color(0xFFCFFAFE)))
    CandyMode.ARCADE -> Brush.verticalGradient(listOf(Color(0xFFFCE7F3), Color(0xFFEDE9FE), Color(0xFFFFEDD5)))
}

private fun comboWord(cascade: Int): String = when {
    cascade >= 5 -> "DIVINE! 🌈"
    cascade == 4 -> "SUGAR CRUSH! 🍭"
    cascade == 3 -> "TASTY! 😋"
    else -> "SWEET! 🍬"
}

@Composable
fun Match3GameScreen(
    mode: CandyMode,
    level: Int,
    tier: AgeTier,
    onLevelComplete: (stars: Int, score: Int) -> Unit,
    onSpeak: (String) -> Unit,
    onTapSound: () -> Unit,
    onPlaySuccess: () -> Unit,
    onPlayCelebration: () -> Unit,
    onPlayNote: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val config = remember(mode, level, tier) { levelFor(mode, level, tier) }
    var resetTick by remember(mode, level) { mutableIntStateOf(0) }
    val board = remember(mode, level, tier, resetTick) { Match3Board(colorCount = config.colors, faceFactory = config.faceFactory) }
    var boardVersion by remember(mode, level) { mutableIntStateOf(0) }
    var selected by remember(mode, level) { mutableStateOf<Pos?>(null) }
    var movesLeft by remember(mode, level) { mutableIntStateOf(config.moves) }
    var score by remember(mode, level) { mutableIntStateOf(0) }
    var progress by remember(mode, level) { mutableStateOf(GoalProgress()) }
    var comboText by remember(mode, level) { mutableStateOf<String?>(null) }
    var resolving by remember(mode, level) { mutableStateOf(false) }
    var failed by remember(mode, level) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(mode, level, resetTick) {
        onSpeak(config.hint)
    }
    LaunchedEffect(comboText) {
        if (comboText != null) {
            delay(1100)
            comboText = null
        }
    }

    fun resetLevel() {
        resetTick++
        movesLeft = config.moves
        score = 0
        progress = GoalProgress()
        comboText = null
        resolving = false
        failed = false
        selected = null
        boardVersion++
    }

    fun handleTileClick(pos: Pos) {
        if (resolving || failed) return
        onTapSound()
        val current = selected
        if (current == null) {
            selected = pos
            return
        }
        if (current == pos) {
            selected = null
            return
        }
        if (!current.isAdjacentTo(pos)) {
            selected = pos
            return
        }
        selected = null
        scope.launch {
            resolving = true
            val ok = board.attemptSwap(current, pos)
            boardVersion++
            if (!ok) {
                delay(200)
                resolving = false
                return@launch
            }
            movesLeft--
            // Queue any special that was part of the swap so it fires first.
            val pending = mutableListOf<Pos>()
            for (p in listOf(current, pos)) {
                if ((board.tileAt(p)?.special ?: Special.NONE) != Special.NONE) pending.add(p)
            }
            var cascade = 0
            var won = false
            while (true) {
                val step = board.stepResolve(pending) ?: break
                cascade++
                val gained = step.cleared.size * 10 * cascade
                score += gained
                progress = applyGoal(config.goal, progress, step.cleared.map { it.tile }, gained)
                onPlayNote(cascade.coerceAtMost(8))
                boardVersion++
                if (isGoalComplete(config.goal, progress)) {
                    won = true
                    break
                }
                delay(320)
            }
            if (cascade >= 2) {
                comboText = comboWord(cascade)
                onPlaySuccess()
            }
            if (won) {
                val stars = when {
                    movesLeft * 5 >= config.moves * 2 -> 3
                    movesLeft * 5 >= config.moves -> 2
                    else -> 1
                }
                onPlayCelebration()
                onSpeak("Sweet win! You earned $stars stars!")
                delay(700)
                onLevelComplete(stars, score)
            } else if (movesLeft <= 0) {
                onSpeak("So close! Try again, you can do it!")
                failed = true
            } else if (!board.hasPossibleMove()) {
                board.shuffle()
                boardVersion++
                comboText = "Shuffled! 🔀"
            }
            resolving = false
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Goal + moves + score HUD
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.White,
                shadowElevation = 3.dp,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = "🎯 ${goalProgressText(config.goal, progress)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    maxLines = 1,
                )
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFFFF6B9D),
                shadowElevation = 3.dp,
            ) {
                Text(
                    text = "❤️ $movesLeft",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFFFFD32A),
                shadowElevation = 3.dp,
            ) {
                Text(
                    text = "⭐ $score",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color(0xFF543300),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        PromptBannerCard(
            promptText = config.hint,
            accentColor = candyColor(CandyColor.PINK),
            onSpeakClick = { onSpeak(config.hint) },
            emoji = mode.emoji,
            subHint = "Level $level of 10 • ${mode.title}",
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Combo praise pop
        val comboScale by animateFloatAsState(
            targetValue = if (comboText != null) 1f else 0.6f,
            animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow),
            label = "combo_scale",
        )
        if (comboText != null) {
            Text(
                text = comboText!!,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFFF6B9D),
                modifier = Modifier.scale(comboScale),
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        if (failed) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("🍬", fontSize = 52.sp)
                    Text(
                        text = "Out of moves!",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "So close — the candies believe in you. Try again!",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { onTapSound(); resetLevel() },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B9D)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                    ) {
                        Text("Try Again 🔄", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        } else {
            // The candy board
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(modeBackground(mode), RoundedCornerShape(28.dp))
                    .border(3.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(28.dp))
                    .padding(10.dp),
                contentAlignment = Alignment.Center,
            ) {
                val boardSize = minOf(maxWidth, 520.dp)
                key(boardVersion, resetTick) {
                    CandyBoard(
                        board = board,
                        size = boardSize,
                        selected = selected,
                        onTileClick = ::handleTileClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun CandyBoard(
    board: Match3Board,
    size: Dp,
    selected: Pos?,
    onTileClick: (Pos) -> Unit,
) {
    val cell = size / 8
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        for (r in 0 until board.rows) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                for (c in 0 until board.cols) {
                    val pos = Pos(r, c)
                    val tile = board.tileAt(pos)
                    key(tile?.id ?: "empty-$r-$c") {
                        CandyTileView(
                            tile = tile,
                            size = cell,
                            isSelected = selected == pos,
                            onClick = { onTileClick(pos) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CandyTileView(
    tile: com.zaviyanllc.sproutplay.games.match3.CandyTile?,
    size: Dp,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    var appeared by remember(tile?.id) { mutableStateOf(false) }
    LaunchedEffect(tile?.id) { appeared = true }
    val scale by animateFloatAsState(
        targetValue = when {
            !appeared -> 0.2f
            isSelected -> 1.18f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "tile_scale",
    )
    val offsetY by animateFloatAsState(
        targetValue = if (appeared) 0f else -46f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow),
        label = "tile_fall",
    )

    Box(
        modifier = Modifier
            .size(size)
            .offset(y = offsetY.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = if (tile == null) {
                    Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                } else if (tile.special == Special.BOMB) {
                    Brush.linearGradient(listOf(Color(0xFF2D2A4A), Color(0xFF6C5CE7)))
                } else {
                    val base = candyColor(tile.color)
                    Brush.linearGradient(listOf(Color.White.copy(alpha = 0.55f), base, base))
                },
            )
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) Color.White else Color.Transparent,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(enabled = tile != null) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        if (tile != null) {
            if (tile.special == Special.BOMB) {
                Text(text = "🌈", fontSize = 24.sp)
            } else {
                if (tile.special == Special.STRIPED_ROW || tile.special == Special.STRIPED_COL) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp)
                            .background(Color.White.copy(alpha = 0.45f), RoundedCornerShape(50)),
                    )
                }
                Text(
                    text = tile.face,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
