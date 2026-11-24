package com.zaviyanllc.sproutplay.ui.stickers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zaviyanllc.sproutplay.R
import com.zaviyanllc.sproutplay.domain.model.StickerCatalog
import com.zaviyanllc.sproutplay.ui.SproutUiState
import com.zaviyanllc.sproutplay.ui.theme.StarGold

@Composable
fun StickersScreen(
    state: SproutUiState,
    modifier: Modifier = Modifier,
) {
    val profile = state.activeProfile ?: return
    val unlockedCount = state.unlockedStickerIds.size

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 148.dp),
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = PaddingValues(18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "${profile.name}'s Sticker Chest",
                        style = MaterialTheme.typography.headlineLarge,
                    )
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(172.dp),
                        shape = RoundedCornerShape(26.dp),
                        shadowElevation = 3.dp,
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_sticker_chest),
                            contentDescription = "Sticker treasure chest",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Rounded.Star, contentDescription = null, tint = StarGold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("${profile.totalStars} Stars", style = MaterialTheme.typography.titleMedium)
                                    Text("$unlockedCount / ${StickerCatalog.allStickers.size} Stickers", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = Color(0xFFEA580C))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("${profile.streakDays}-Day Streak", style = MaterialTheme.typography.titleMedium)
                                    Text("Keep exploring!", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }

            items(StickerCatalog.allStickers, key = { it.id }) { sticker ->
                val isUnlocked = sticker.id in state.unlockedStickerIds
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = if (isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = BorderStroke(
                        width = if (isUnlocked) 2.dp else 1.dp,
                        color = if (isUnlocked) StarGold else MaterialTheme.colorScheme.outline,
                    ),
                    shadowElevation = if (isUnlocked) 3.dp else 0.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .padding(14.dp)
                            .alpha(if (isUnlocked) 1f else 0.58f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = if (isUnlocked) sticker.emoji else "🔒",
                            fontSize = 38.sp,
                        )
                        Text(
                            text = sticker.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = if (isUnlocked) "Unlocked!" else sticker.unlockHint,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}
