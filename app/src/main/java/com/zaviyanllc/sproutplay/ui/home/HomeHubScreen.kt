package com.zaviyanllc.sproutplay.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zaviyanllc.sproutplay.R
import com.zaviyanllc.sproutplay.domain.model.ChildAvatar
import com.zaviyanllc.sproutplay.domain.model.GameCatalog
import com.zaviyanllc.sproutplay.domain.model.GameCategory
import com.zaviyanllc.sproutplay.domain.model.MiniGameSpec
import com.zaviyanllc.sproutplay.ui.SproutUiState
import com.zaviyanllc.sproutplay.ui.theme.CandyGrapeContainer
import com.zaviyanllc.sproutplay.ui.theme.CandyPinkContainer
import com.zaviyanllc.sproutplay.ui.theme.CandySkySoft
import com.zaviyanllc.sproutplay.ui.theme.StarGold

@Composable
fun HomeHubScreen(
    state: SproutUiState,
    onPlayGame: (String) -> Unit,
    onOpenCategory: (GameCategory) -> Unit,
    onOpenAllGames: () -> Unit,
    onOpenParents: () -> Unit,
    onSpeak: (String) -> Unit,
    onTapSound: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val profile = state.activeProfile ?: return
    val avatar = ChildAvatar.fromId(profile.avatarId)
    val tier = state.activeTier
    val recommended = remember(tier) { GameCatalog.recommendedForTier(tier).take(10) }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            // Personalized Greeting Header — candy gradient card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(CandyPinkContainer, CandyGrapeContainer, CandySkySoft),
                            ),
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                    Surface(
                        modifier = Modifier.size(58.dp),
                        shape = CircleShape,
                        color = avatar.bgColor,
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(avatar.emoji, fontSize = 30.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hi, ${profile.name}!",
                            style = MaterialTheme.typography.headlineMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Text(
                                text = "${tier.emoji} ${tier.title} • Age ${profile.age}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            )
                        }
                    }

                    // Stars Pill
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = "Stars earned",
                                tint = StarGold,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${profile.totalStars}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = { onTapSound(); onOpenParents() },
                        modifier = Modifier.testTag("parent_zone_btn"),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = "Parent Controls",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    }
                }
            }

            // Hero Adventure Banner + Daily Star Challenge
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onTapSound()
                            onPlayGame("candy_arcade")
                        }
                        .testTag("daily_challenge_banner"),
                    shape = RoundedCornerShape(28.dp),
                    shadowElevation = 4.dp,
                ) {
                    Box(modifier = Modifier.height(188.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.img_home_banner),
                            contentDescription = "SproutPlay adventure world",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color(0xCC0F172A)),
                                    ),
                                ),
                        )
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Daily Star Challenge 🏆",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                )
                                Text(
                                    text = "10 sweet levels • Big candy combos!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.9f),
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { onTapSound(); onPlayGame("candy_arcade") },
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StarGold, contentColor = Color(0xFF1E293B)),
                            ) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                                Text("Play", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Picked Just For Your Age Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Picked Just For You",
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        Text(
                            text = "Tailored for ${tier.title} (${tier.ageRangeLabel})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    FilledTonalIconButton(
                        onClick = {
                            onSpeak("Hi ${profile.name}! Here are fun games picked for ${tier.title}. Tap any card to play!")
                        },
                    ) {
                        Icon(Icons.Rounded.VolumeUp, contentDescription = "Read recommendations")
                    }
                }
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(recommended, key = { it.id }) { game ->
                        RecommendedGameCard(
                            game = game,
                            starsEarned = state.progressMap[game.id]?.starsEarned ?: 0,
                            onClick = { onTapSound(); onPlayGame(game.id) },
                        )
                    }
                }
            }

            // 5 Learning Worlds
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "5 Learning Worlds (50 Games)",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Button(
                        onClick = { onTapSound(); onOpenAllGames() },
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("see_all_50_btn"),
                    ) {
                        Text("All 50")
                    }
                }
            }

            items(GameCategory.entries, key = { it.id }) { category ->
                val catGames = remember(category) { GameCatalog.byCategory(category) }
                val playedCount = catGames.count { (state.progressMap[it.id]?.timesPlayed ?: 0) > 0 }
                CategoryWorldCard(
                    category = category,
                    playedCount = playedCount,
                    totalCount = catGames.size,
                    onClick = { onTapSound(); onOpenCategory(category) },
                )
            }
        }
    }
}

@Composable
private fun RecommendedGameCard(
    game: MiniGameSpec,
    starsEarned: Int,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .width(172.dp)
            .clickable(onClick = onClick)
            .testTag("rec_game_${game.id}"),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.5.dp, game.category.accentColor.copy(alpha = 0.25f)),
        shadowElevation = 3.dp,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = game.category.softColor,
                    modifier = Modifier.size(46.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(game.emoji, fontSize = 24.sp)
                    }
                }
                Row {
                    repeat(3) { idx ->
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = if (idx < starsEarned) StarGold else Color(0xFFE2E8F0),
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
            Text(
                text = game.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = game.coreSkill,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Surface(
                shape = RoundedCornerShape(50),
                color = game.category.accentColor,
            ) {
                Text(
                    text = "Play Now",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                )
            }
        }
    }
}

@Composable
private fun CategoryWorldCard(
    category: GameCategory,
    playedCount: Int,
    totalCount: Int,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("world_card_${category.id}"),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.5.dp, category.accentColor.copy(alpha = 0.25f)),
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(58.dp),
                shape = RoundedCornerShape(18.dp),
                color = category.softColor,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(category.emoji, fontSize = 30.sp)
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(category.title, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = category.tagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { (playedCount / totalCount.toFloat()).coerceIn(0f, 1f) },
                        color = category.accentColor,
                        trackColor = category.softColor,
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(50)),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "$playedCount/$totalCount",
                        style = MaterialTheme.typography.labelMedium,
                        color = category.accentColor,
                    )
                }
            }
        }
    }
}
