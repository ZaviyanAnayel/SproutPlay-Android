package com.zaviyanllc.sproutplay.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zaviyanllc.sproutplay.data.local.GameProgressEntity
import com.zaviyanllc.sproutplay.domain.model.AgeTier
import com.zaviyanllc.sproutplay.domain.model.GameCatalog
import com.zaviyanllc.sproutplay.domain.model.StickerSpec
import com.zaviyanllc.sproutplay.games.match3.CandyMode
import com.zaviyanllc.sproutplay.games.match3.levelGameId
import com.zaviyanllc.sproutplay.ui.theme.StarGold
import com.zaviyanllc.sproutplay.ui.theme.SuccessGreen
import kotlinx.coroutines.delay

/**
 * Host for the Candy Crush-style games: level picker (10 levels, unlocked in
 * order), the match-3 board, and the win celebration. Progress is stored per
 * level in Room (gameId "candy_math_3" etc.) so stars survive restarts.
 */
@Composable
fun GameHostScreen(
    gameId: String,
    initialTier: AgeTier,
    childName: String,
    onBack: () -> Unit,
    onSpeak: (String) -> Unit,
    onTapSound: () -> Unit,
    onPlaySuccess: () -> Unit,
    onPlayCelebration: () -> Unit,
    onPlayGentleTryAgain: () -> Unit,
    onPlayNote: (Int) -> Unit,
    onRecordWin: (gameId: String, stars: Int, score: Int, tier: AgeTier, onStickers: (List<StickerSpec>) -> Unit) -> Unit,
    progressMap: Map<String, GameProgressEntity> = emptyMap(),
) {
    val game = remember(gameId) { GameCatalog.getById(gameId) }
    val mode = remember(gameId) { CandyMode.fromId(gameId) }
    var activeTier by remember(gameId, initialTier) { mutableStateOf(initialTier) }
    var selectedLevel by remember(gameId) {
        mutableStateOf(
            (1..10).firstOrNull { level ->
                (progressMap[levelGameId(mode, level)]?.starsEarned ?: 0) == 0
            } ?: 1,
        )
    }
    var completed by remember(gameId, selectedLevel) { mutableStateOf(false) }
    var earnedStars by remember(gameId, selectedLevel) { mutableIntStateOf(0) }
    var finalScore by remember(gameId, selectedLevel) { mutableIntStateOf(0) }
    var unlockedStickers by remember(gameId, selectedLevel) { mutableStateOf<List<StickerSpec>>(emptyList()) }
    var feedbackMessage by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var showTierMenu by remember { mutableStateOf(false) }

    fun starsFor(level: Int): Int = progressMap[levelGameId(mode, level)]?.starsEarned ?: 0
    fun isUnlocked(level: Int): Boolean = level == 1 || starsFor(level - 1) > 0
    val levelsBeaten = (1..10).count { starsFor(it) > 0 }

    LaunchedEffect(feedbackMessage) {
        if (feedbackMessage != null) {
            delay(1700)
            feedbackMessage = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp)
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            // Top Kid-Safe Game Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = { onTapSound(); onBack() },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("game_back_btn"),
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back Home")
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${game.emoji} ${game.title}",
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                    )
                    Text(
                        text = "Level $selectedLevel of 10 • $levelsBeaten levels beaten 🌟",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Live Age-Tier Switcher Pill
                Box {
                    Surface(
                        onClick = { onTapSound(); showTierMenu = true },
                        shape = RoundedCornerShape(50),
                        color = game.category.softColor,
                        border = BorderStroke(1.dp, game.category.accentColor.copy(alpha = 0.4f)),
                        modifier = Modifier.testTag("game_tier_pill"),
                    ) {
                        Text(
                            text = "${activeTier.emoji} ${activeTier.shortTitle} ▾",
                            style = MaterialTheme.typography.labelLarge,
                            color = game.category.accentColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        )
                    }
                    DropdownMenu(
                        expanded = showTierMenu,
                        onDismissRequest = { showTierMenu = false },
                    ) {
                        AgeTier.entries.forEach { t ->
                            DropdownMenuItem(
                                text = { Text("${t.emoji} ${t.title} (${t.ageRangeLabel})") },
                                onClick = {
                                    onTapSound()
                                    activeTier = t
                                    completed = false
                                    showTierMenu = false
                                },
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { (levelsBeaten / 10f).coerceIn(0.05f, 1f) },
                color = game.category.accentColor,
                trackColor = game.category.softColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50)),
            )

            // Level picker — 10 glossy level bubbles
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                for (level in 1..10) {
                    val stars = starsFor(level)
                    val unlocked = isUnlocked(level)
                    val isCurrent = level == selectedLevel
                    Surface(
                        onClick = {
                            if (!unlocked) return@Surface
                            onTapSound()
                            selectedLevel = level
                            completed = false
                        },
                        shape = RoundedCornerShape(50),
                        color = when {
                            isCurrent -> game.category.accentColor
                            stars > 0 -> StarGold
                            unlocked -> game.category.softColor
                            else -> Color(0xFFE2E8F0)
                        },
                        border = BorderStroke(
                            1.5.dp,
                            if (isCurrent) StarGold else game.category.accentColor.copy(alpha = 0.35f),
                        ),
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("level_bubble_$level"),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (!unlocked) {
                                Icon(
                                    Icons.Rounded.Lock,
                                    contentDescription = "Locked",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(18.dp),
                                )
                            } else {
                                Text(
                                    text = if (stars > 0) "★" else "$level",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = if (isCurrent) Color.White else game.category.accentColor,
                                )
                            }
                        }
                    }
                }
            }

            // Gentle Feedback Banner
            AnimatedVisibility(
                visible = feedbackMessage != null,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                feedbackMessage?.let { (isGood, msg) ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = SuccessGreen.copy(alpha = 0.16f),
                        border = BorderStroke(1.5.dp, SuccessGreen),
                    ) {
                        Text(
                            text = "🌟 $msg",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (completed) {
                GameCelebrationCard(
                    childName = childName,
                    gameTitle = "${game.title} — Level $selectedLevel",
                    stars = earnedStars,
                    score = finalScore,
                    unlockedStickers = unlockedStickers,
                    accentColor = game.category.accentColor,
                    onPlayAgain = {
                        onTapSound()
                        completed = false
                    },
                    onBackHome = { onTapSound(); onBack() },
                    onNextLevel = if (selectedLevel < 10 && isUnlocked(selectedLevel + 1)) {
                        {
                            onTapSound()
                            selectedLevel += 1
                            completed = false
                        }
                    } else {
                        null
                    },
                )
            } else {
                Box(modifier = Modifier.weight(1f)) {
                    Match3GameScreen(
                        mode = mode,
                        level = selectedLevel,
                        tier = activeTier,
                        onLevelComplete = { stars, score ->
                            earnedStars = stars
                            finalScore = score
                            completed = true
                            feedbackMessage = true to "Level $selectedLevel complete — $stars stars!"
                            onRecordWin(levelGameId(mode, selectedLevel), stars, score, activeTier) { stickers ->
                                unlockedStickers = stickers
                            }
                        },
                        onSpeak = onSpeak,
                        onTapSound = onTapSound,
                        onPlaySuccess = onPlaySuccess,
                        onPlayCelebration = onPlayCelebration,
                        onPlayNote = onPlayNote,
                    )
                }
            }
        }
    }
}

@Composable
private fun GameCelebrationCard(
    childName: String,
    gameTitle: String,
    stars: Int,
    score: Int,
    unlockedStickers: List<StickerSpec>,
    accentColor: Color,
    onPlayAgain: () -> Unit,
    onBackHome: () -> Unit,
    onNextLevel: (() -> Unit)? = null,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(30.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(2.dp, StarGold),
        shadowElevation = 6.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("🎉🌟🎉", fontSize = 54.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Awesome Job, $childName!",
                style = MaterialTheme.typography.displayMedium,
                color = accentColor,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "You completed $gameTitle!",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(3) { idx ->
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = if (idx < stars) StarGold else Color(0xFFE2E8F0),
                        modifier = Modifier.size(54.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Score: $score points",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (unlockedStickers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("New Sticker Unlocked!", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        unlockedStickers.forEach { stk ->
                            Text("${stk.emoji} ${stk.title}", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            if (onNextLevel != null) {
                Button(
                    onClick = onNextLevel,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StarGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                ) {
                    Text("Next Level", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Rounded.ArrowForward, contentDescription = null)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onPlayAgain,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp),
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Play Again")
                }
                Button(
                    onClick = onBackHome,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp)
                        .testTag("celebration_home_btn"),
                ) {
                    Icon(Icons.Rounded.Home, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Home Hub")
                }
            }
        }
    }
}
