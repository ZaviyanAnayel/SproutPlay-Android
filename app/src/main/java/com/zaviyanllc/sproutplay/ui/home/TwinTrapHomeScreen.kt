package com.zaviyanllc.sproutplay.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.MusicOff
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zaviyanllc.sproutplay.audio.TrapAudioManager
import com.zaviyanllc.sproutplay.domain.model.TwinTrapLevels
import com.zaviyanllc.sproutplay.games.cup.GlowBall
import com.zaviyanllc.sproutplay.ui.TrapUiState
import com.zaviyanllc.sproutplay.ui.theme.TrapGold
import com.zaviyanllc.sproutplay.ui.theme.TrapMuted
import com.zaviyanllc.sproutplay.ui.theme.TrapNavy
import com.zaviyanllc.sproutplay.ui.theme.TrapNavyCenter
import com.zaviyanllc.sproutplay.ui.theme.TrapPink

/**
 * Twin Trap home: big title, hype tagline, coins, Tap to Play, and the
 * 10-level grid with lock/star states and earned trophies.
 */
@Composable
fun TwinTrapHomeScreen(
    state: TrapUiState,
    audio: TrapAudioManager,
    onPlayLevel: (Int) -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleMusic: (Boolean) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(TrapNavyCenter, TrapNavy),
                    radius = 900f,
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            // Top row: coins + sound/music toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFF101B31),
                    modifier = Modifier.testTag("coins_pill"),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = "🪙", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${state.totalCoins}",
                            fontWeight = FontWeight.ExtraBold,
                            color = TrapGold,
                            fontSize = 17.sp,
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { audio.playTap(); onToggleSound(!state.settings.soundEnabled) },
                    modifier = Modifier.testTag("toggle_sound"),
                ) {
                    Icon(
                        imageVector = if (state.settings.soundEnabled) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeOff,
                        contentDescription = "Sound",
                        tint = TrapMuted,
                    )
                }
                IconButton(
                    onClick = { audio.playTap(); onToggleMusic(!state.settings.musicCuesEnabled) },
                    modifier = Modifier.testTag("toggle_music"),
                ) {
                    Icon(
                        imageVector = if (state.settings.musicCuesEnabled) Icons.Rounded.MusicNote else Icons.Rounded.MusicOff,
                        contentDescription = "Music",
                        tint = TrapMuted,
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Two glowing balls flanking the title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GlowBall(ball = com.zaviyanllc.sproutplay.domain.model.BallColor.GOLD, size = 52.dp)
                Spacer(modifier = Modifier.width(18.dp))
                Text(
                    text = "TWIN TRAP",
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.sp,
                )
                Spacer(modifier = Modifier.width(18.dp))
                GlowBall(ball = com.zaviyanllc.sproutplay.domain.model.BallColor.PINK, size = 52.dp)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = buildAnnotatedString {
                    append("FOLLOW ")
                    withStyle(SpanStyle(color = TrapGold, fontWeight = FontWeight.ExtraBold)) { append("GOLD") }
                    append(" AND ")
                    withStyle(SpanStyle(color = TrapPink, fontWeight = FontWeight.ExtraBold)) { append("PINK") }
                    append(".")
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "Only 10% can solve all 10 levels.",
                fontSize = 15.sp,
                color = TrapMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { audio.playTap(); onPlayLevel(state.nextLevel) },
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TrapGold, contentColor = Color(0xFF1A1400)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .testTag("tap_to_play_btn"),
            ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "TAP TO PLAY — LEVEL ${state.nextLevel}",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "LEVELS",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TrapMuted,
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "${state.levelsCompleted}/10 complete  •  ${state.totalStars}★",
                    fontSize = 14.sp,
                    color = TrapMuted,
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f),
            ) {
                items(TwinTrapLevels.all) { level ->
                    val stars = state.starsFor(level.number)
                    val unlocked = state.isUnlocked(level.number)
                    LevelCard(
                        level = level,
                        stars = stars,
                        unlocked = unlocked,
                        onClick = { if (unlocked) { audio.playTap(); onPlayLevel(level.number) } },
                    )
                }
            }

            if (state.trophies.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF101B31),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "🏆 " + state.trophies.joinToString("  •  "),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TrapGold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun LevelCard(
    level: com.zaviyanllc.sproutplay.domain.model.TwinTrapLevel,
    stars: Int,
    unlocked: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF101B31),
        modifier = Modifier.testTag("level_card_${level.number}"),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = level.cupBase,
                    modifier = Modifier.size(40.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (!unlocked) {
                            Icon(
                                Icons.Rounded.Lock,
                                contentDescription = "Locked",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp),
                            )
                        } else {
                            Text(
                                text = "${level.number}",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 18.sp,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = level.title,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 15.sp,
                    )
                    Text(
                        text = "${level.cupCount} cups • ${level.balls.size} ball${if (level.balls.size > 1) "s" else ""}",
                        fontSize = 12.sp,
                        color = TrapMuted,
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                repeat(3) { idx ->
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = if (idx < stars) com.zaviyanllc.sproutplay.ui.theme.StarGold else Color(0xFF334155),
                        modifier = Modifier.size(20.dp),
                    )
                }
                if (level.trophy != null && stars > 0) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = "🏆", fontSize = 16.sp)
                }
            }
        }
    }
}
