package com.zaviyanllc.sproutplay.ui.parents

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zaviyanllc.sproutplay.data.local.ChildProfileEntity
import com.zaviyanllc.sproutplay.domain.model.AgeTier
import com.zaviyanllc.sproutplay.domain.model.ChildAvatar
import com.zaviyanllc.sproutplay.domain.model.GameCatalog
import com.zaviyanllc.sproutplay.domain.model.GameCategory
import com.zaviyanllc.sproutplay.ui.SproutUiState
import kotlin.random.Random

@Composable
fun ParentZoneScreen(
    state: SproutUiState,
    onBack: () -> Unit,
    onSwitchProfile: (Long) -> Unit,
    onAddNewProfile: () -> Unit,
    onUpdateProfileAge: (ChildProfileEntity, Int) -> Unit,
    onDeleteProfile: (Long) -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleVoice: (Boolean) -> Unit,
    onToggleMusic: (Boolean) -> Unit,
    onSetTimeLimit: (Int) -> Unit,
) {
    var unlocked by remember { mutableStateOf(false) }
    val gateChallenge = remember {
        val a = Random.nextInt(4, 9)
        val b = Random.nextInt(3, 9)
        Pair("$a × $b = ?", (a * b).toString())
    }
    var gateInput by remember { mutableStateOf("") }
    var gateError by remember { mutableStateOf(false) }
    var showAllCurriculum by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("parent_back_btn")) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text("Parent Controls & Progress", style = MaterialTheme.typography.headlineMedium)
            }

            if (!unlocked) {
                // Kid-Safe Parent Math Gate
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                        shadowElevation = 4.dp,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LockOpen,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp),
                            )
                            Text("Grown-Ups Only", style = MaterialTheme.typography.headlineMedium)
                            Text(
                                text = "Solve to open Parent Controls: ${gateChallenge.first}",
                                style = MaterialTheme.typography.titleLarge,
                            )
                            OutlinedTextField(
                                value = gateInput,
                                onValueChange = {
                                    gateInput = it.filter { ch -> ch.isDigit() }
                                    gateError = false
                                },
                                label = { Text("Answer") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = gateError,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("parent_gate_input"),
                            )
                            if (gateError) {
                                Text(
                                    text = "Not quite — please try again.",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                            Button(
                                onClick = {
                                    if (gateInput.trim() == gateChallenge.second) {
                                        unlocked = true
                                    } else {
                                        gateError = true
                                    }
                                },
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("parent_gate_unlock"),
                            ) {
                                Text("Unlock Controls")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Child Profiles Section
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Child Profiles", style = MaterialTheme.typography.titleLarge)
                            OutlinedButton(
                                onClick = onAddNewProfile,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.testTag("add_child_profile_btn"),
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Child")
                            }
                        }
                    }

                    items(state.profiles, key = { it.id }) { profile ->
                        val isActive = profile.id == state.activeProfile?.id
                        val avatar = ChildAvatar.fromId(profile.avatarId)
                        val tier = AgeTier.fromAge(profile.age)
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                if (isActive) 2.dp else 1.dp,
                                if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            ),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        modifier = Modifier.size(46.dp),
                                        shape = CircleShape,
                                        color = avatar.bgColor,
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(avatar.emoji, fontSize = 24.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${profile.name} (${tier.shortTitle})", style = MaterialTheme.typography.titleMedium)
                                        Text("Age ${profile.age} • ${profile.totalStars} Stars", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    if (!isActive) {
                                        Button(onClick = { onSwitchProfile(profile.id) }) {
                                            Text("Switch")
                                        }
                                    } else {
                                        Text("Active ✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }
                                    if (state.profiles.size > 1) {
                                        IconButton(onClick = { onDeleteProfile(profile.id) }) {
                                            Icon(Icons.Rounded.Delete, contentDescription = "Delete profile")
                                        }
                                    }
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text("Adjust Age:", style = MaterialTheme.typography.bodyMedium)
                                    OutlinedButton(
                                        onClick = { if (profile.age > 3) onUpdateProfileAge(profile, profile.age - 1) },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    ) { Text("−") }
                                    Text("${profile.age}", fontWeight = FontWeight.Bold)
                                    OutlinedButton(
                                        onClick = { if (profile.age < 11) onUpdateProfileAge(profile, profile.age + 1) },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    ) { Text("+") }
                                }
                            }
                        }
                    }

                    // Audio & Screen-Time Controls
                    item {
                        Text("Audio & Kid-Safe Settings", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                SettingSwitchRow("Sound Effects & Chimes", state.settings.soundEnabled, onToggleSound)
                                SettingSwitchRow("Spoken Voice Prompts (TTS)", state.settings.voicePromptsEnabled, onToggleVoice)
                                SettingSwitchRow("Musical Note Cues", state.settings.musicCuesEnabled, onToggleMusic)

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Daily Gentle Break Reminder (${state.minutesUsedToday}m played today)",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(0 to "Off", 15 to "15m", 30 to "30m", 45 to "45m").forEach { (mins, label) ->
                                        FilterChip(
                                            selected = state.settings.dailyTimeLimitMinutes == mins,
                                            onClick = { onSetTimeLimit(mins) },
                                            label = { Text(label) },
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Category Progress Overview
                    item {
                        Text("Skill Progress Overview", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                GameCategory.entries.forEach { cat ->
                                    val games = GameCatalog.byCategory(cat)
                                    val played = games.count { (state.progressMap[it.id]?.timesPlayed ?: 0) > 0 }
                                    val catStars = games.sumOf { state.progressMap[it.id]?.starsEarned ?: 0 }
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            Text("${cat.emoji} ${cat.title}", style = MaterialTheme.typography.titleMedium)
                                            Text("$played/10 • $catStars ★", style = MaterialTheme.typography.bodyMedium)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { played / 10f },
                                            color = cat.accentColor,
                                            trackColor = cat.softColor,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(50)),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 50-Game Curriculum Guide Toggle
                    item {
                        OutlinedButton(
                            onClick = { showAllCurriculum = !showAllCurriculum },
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(if (showAllCurriculum) "Hide Full 50-Game Curriculum ▲" else "View Full 50-Game Curriculum & Scaling ▼")
                        }
                    }

                    if (showAllCurriculum) {
                        items(GameCatalog.allGames, key = { it.id }) { g ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text("#${g.number} ${g.emoji} ${g.title} (${g.category.shortTitle})", fontWeight = FontWeight.Bold)
                                    Text("Skill: ${g.coreSkill}", style = MaterialTheme.typography.bodyMedium)
                                    Text("Pre-K: ${g.scaling.preschool}", style = MaterialTheme.typography.bodyMedium)
                                    Text("K–1st: ${g.scaling.k1}", style = MaterialTheme.typography.bodyMedium)
                                    Text("2nd–3rd: ${g.scaling.grade23}", style = MaterialTheme.typography.bodyMedium)
                                    Text("4th–5th: ${g.scaling.grade45}", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
