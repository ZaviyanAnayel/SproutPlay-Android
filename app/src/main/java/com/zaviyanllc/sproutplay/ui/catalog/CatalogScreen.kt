package com.zaviyanllc.sproutplay.ui.catalog

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zaviyanllc.sproutplay.domain.model.AgeTier
import com.zaviyanllc.sproutplay.domain.model.GameCatalog
import com.zaviyanllc.sproutplay.domain.model.GameCategory
import com.zaviyanllc.sproutplay.domain.model.MiniGameSpec
import com.zaviyanllc.sproutplay.ui.SproutUiState
import com.zaviyanllc.sproutplay.ui.theme.StarGold

@Composable
fun CatalogScreen(
    state: SproutUiState,
    initialCategory: GameCategory?,
    onPlayGame: (String, AgeTier) -> Unit,
    onTapSound: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedCategory by remember(initialCategory) { mutableStateOf(initialCategory) }
    var selectedTierFilter by remember { mutableStateOf<AgeTier?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var detailGame by remember { mutableStateOf<MiniGameSpec?>(null) }

    val filteredGames = remember(selectedCategory, selectedTierFilter, searchQuery) {
        GameCatalog.allGames.filter { g ->
            val catOk = selectedCategory == null || g.category == selectedCategory
            val tierOk = selectedTierFilter == null || selectedTierFilter!! in g.bestForTiers
            val q = searchQuery.trim().lowercase()
            val queryOk = q.isEmpty() ||
                g.title.lowercase().contains(q) ||
                g.coreSkill.lowercase().contains(q) ||
                g.howItPlays.lowercase().contains(q)
            catOk && tierOk && queryOk
        }
    }

    val effectiveTier = selectedTierFilter ?: state.activeTier

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp)
                .padding(top = 12.dp),
        ) {
            Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                Text(
                    text = "50 Mini-Game Library",
                    style = MaterialTheme.typography.headlineLarge,
                )
                Text(
                    text = "Playing at ${effectiveTier.title} level • Tap any game to start!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search math, phonics, planets, coding...") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("catalog_search"),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Category Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { onTapSound(); selectedCategory = null },
                        label = { Text("All 50") },
                    )
                }
                items(GameCategory.entries) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { onTapSound(); selectedCategory = cat },
                        label = { Text("${cat.emoji} ${cat.shortTitle} (10)") },
                    )
                }
            }

            // Age Tier Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    FilterChip(
                        selected = selectedTierFilter == null,
                        onClick = { onTapSound(); selectedTierFilter = null },
                        label = { Text("My Level (${state.activeTier.shortTitle})") },
                    )
                }
                items(AgeTier.entries) { t ->
                    FilterChip(
                        selected = selectedTierFilter == t,
                        onClick = { onTapSound(); selectedTierFilter = t },
                        label = { Text("${t.emoji} ${t.shortTitle}") },
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(filteredGames, key = { it.id }) { game ->
                    val stars = state.progressMap[game.id]?.starsEarned ?: 0
                    CatalogGameRow(
                        game = game,
                        starsEarned = stars,
                        activeTier = effectiveTier,
                        onInfoClick = { onTapSound(); detailGame = game },
                        onPlayClick = { onTapSound(); onPlayGame(game.id, effectiveTier) },
                    )
                }
            }
        }
    }

    detailGame?.let { g ->
        AlertDialog(
            onDismissRequest = { detailGame = null },
            title = { Text("#${g.number} ${g.emoji} ${g.title}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Category: ${g.category.title}", fontWeight = FontWeight.Bold)
                    Text("Core Skill: ${g.coreSkill}")
                    Text(g.howItPlays)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Age Scaling:", fontWeight = FontWeight.Bold)
                    Text("🐣 Pre-School: ${g.scaling.preschool}", style = MaterialTheme.typography.bodyMedium)
                    Text("🦊 K–1st: ${g.scaling.k1}", style = MaterialTheme.typography.bodyMedium)
                    Text("🦉 2nd–3rd: ${g.scaling.grade23}", style = MaterialTheme.typography.bodyMedium)
                    Text("🚀 4th–5th: ${g.scaling.grade45}", style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                Button(onClick = {
                    val id = g.id
                    detailGame = null
                    onPlayGame(id, effectiveTier)
                }) {
                    Text("Play Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { detailGame = null }) {
                    Text("Close")
                }
            },
        )
    }
}

@Composable
private fun CatalogGameRow(
    game: MiniGameSpec,
    starsEarned: Int,
    activeTier: AgeTier,
    onInfoClick: () -> Unit,
    onPlayClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlayClick)
            .testTag("catalog_game_${game.id}"),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.2.dp, game.category.accentColor.copy(alpha = 0.25f)),
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = game.category.softColor,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(game.emoji, fontSize = 28.sp)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${game.number} ${game.title}",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Text(
                    text = "${game.coreSkill} • ${game.scaling.forTier(activeTier)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(3) { i ->
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = if (i < starsEarned) StarGold else Color(0xFFE2E8F0),
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
            IconButton(onClick = onInfoClick) {
                Icon(Icons.Rounded.Info, contentDescription = "View scaling guide")
            }
            Button(
                onClick = onPlayClick,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = game.category.accentColor),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = "Play ${game.title}")
            }
        }
    }
}
