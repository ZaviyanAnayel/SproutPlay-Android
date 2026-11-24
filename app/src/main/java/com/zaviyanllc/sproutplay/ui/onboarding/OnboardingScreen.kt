package com.zaviyanllc.sproutplay.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zaviyanllc.sproutplay.R
import com.zaviyanllc.sproutplay.domain.model.AgeTier
import com.zaviyanllc.sproutplay.domain.model.ChildAvatar
import com.zaviyanllc.sproutplay.ui.theme.StarGold

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    isAddingAdditionalProfile: Boolean,
    onSpeak: (String) -> Unit,
    onTapSound: () -> Unit,
    onCancelAdd: (() -> Unit)?,
    onComplete: (name: String, age: Int, avatarId: String) -> Unit,
) {
    var step by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var selectedAvatar by remember { mutableStateOf(ChildAvatar.OWL) }
    var selectedAge by remember { mutableIntStateOf(5) }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(step) {
        if (step == 0) {
            onSpeak("Welcome to SproutPlay! What is your name, and which buddy do you like?")
        } else {
            val clean = name.trim().ifEmpty { "friend" }
            onSpeak("Hi $clean! How old are you? We'll pick games just right for you!")
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
                .widthIn(max = 600.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (step > 0) {
                    IconButton(
                        onClick = { onTapSound(); step = 0 },
                        modifier = Modifier.testTag("onboarding_back"),
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                } else if (isAddingAdditionalProfile && onCancelAdd != null) {
                    IconButton(onClick = { onTapSound(); onCancelAdd() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Cancel")
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        text = if (step == 0) "Step 1 of 2 • Meet Your Buddy" else "Step 2 of 2 • Your Age",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    )
                }

                FilledTonalIconButton(
                    onClick = {
                        val clean = name.trim().ifEmpty { "Explorer" }
                        onSpeak(
                            if (step == 0) "Enter your name and pick a fun avatar!"
                            else "Hi $clean! Tap your age from 3 to 11!",
                        )
                    },
                ) {
                    Icon(Icons.Rounded.VolumeUp, contentDescription = "Hear instructions")
                }
            }

            // Storybook Hero Art
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp),
                shape = RoundedCornerShape(28.dp),
                shadowElevation = 4.dp,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_onboarding_welcome),
                    contentDescription = "Sunny SproutPlay learning meadow",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            AnimatedContent(targetState = step, label = "onboarding_step") { currentStep ->
                if (currentStep == 0) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Welcome to SproutPlay!",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = "Let's make your own learning adventure.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { if (it.length <= 18) name = it },
                            label = { Text("Child's Name") },
                            placeholder = { Text("e.g. Maya or Leo") },
                            singleLine = true,
                            shape = RoundedCornerShape(20.dp),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                keyboard?.hide()
                                val previewName = name.trim().ifEmpty { "Explorer" }
                                onSpeak("Hi $previewName! Let's play and learn!")
                            }),
                            trailingIcon = {
                                IconButton(onClick = {
                                    val previewName = name.trim().ifEmpty { "Explorer" }
                                    onSpeak("Hi $previewName! Let's play and learn!")
                                }) {
                                    Icon(Icons.Rounded.VolumeUp, contentDescription = "Test voice greeting")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_name_input"),
                        )

                        Text(
                            text = "Pick your Explorer Buddy:",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.align(Alignment.Start),
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            ChildAvatar.entries.forEach { avatar ->
                                val isSelected = avatar == selectedAvatar
                                Surface(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            onTapSound()
                                            selectedAvatar = avatar
                                        }
                                        .testTag("avatar_${avatar.id}"),
                                    shape = CircleShape,
                                    color = avatar.bgColor,
                                    border = BorderStroke(
                                        width = if (isSelected) 3.5.dp else 1.5.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFCBD5E1),
                                    ),
                                    shadowElevation = if (isSelected) 6.dp else 1.dp,
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(avatar.emoji, fontSize = 34.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                onTapSound()
                                keyboard?.hide()
                                step = 1
                            },
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .testTag("onboarding_next_btn"),
                        ) {
                            Text("Next: Pick Age", style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null)
                        }
                    }
                } else {
                    val mappedTier = AgeTier.fromAge(selectedAge)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "How old is ${name.trim().ifEmpty { "your explorer" }}?",
                            style = MaterialTheme.typography.headlineLarge,
                            textAlign = TextAlign.Center,
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            for (age in 3..11) {
                                val chosen = age == selectedAge
                                Surface(
                                    modifier = Modifier
                                        .size(width = 96.dp, height = 68.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable {
                                            onTapSound()
                                            selectedAge = age
                                            val t = AgeTier.fromAge(age)
                                            onSpeak("Age $age! ${t.title}!")
                                        }
                                        .testTag("age_chip_$age"),
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (chosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(
                                        2.dp,
                                        if (chosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    ),
                                    shadowElevation = if (chosen) 5.dp else 1.dp,
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                    ) {
                                        Text(
                                            text = "$age",
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = if (chosen) Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        Text(
                                            text = "years",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (chosen) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }

                        // Live Grade Tier Preview Card
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            border = BorderStroke(1.5.dp, StarGold),
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(mappedTier.emoji, fontSize = 38.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "${mappedTier.title} (${mappedTier.ageRangeLabel})",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = mappedTier.summary,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f),
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                onComplete(
                                    name.trim().ifEmpty { "Explorer" },
                                    selectedAge,
                                    selectedAvatar.id,
                                )
                            },
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .testTag("onboarding_start_btn"),
                        ) {
                            Icon(Icons.Rounded.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Let's Play & Learn!", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }
        }
    }
}
