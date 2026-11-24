package com.zaviyanllc.sproutplay.games.engine

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zaviyanllc.sproutplay.domain.model.AgeTier

data class GameSessionCallbacks(
    val onRoundSuccess: (points: Int, praise: String) -> Unit,
    val onGentleMistake: (hint: String) -> Unit,
    val onSpeak: (String) -> Unit,
    val onTapSound: () -> Unit,
    val onPlayNote: (Int) -> Unit,
)

@Composable
fun PromptBannerCard(
    promptText: String,
    accentColor: Color,
    onSpeakClick: () -> Unit,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    subHint: String? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = accentColor.copy(alpha = 0.12f),
        border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.28f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!emoji.isNullOrEmpty()) {
                Text(
                    text = emoji,
                    fontSize = 32.sp,
                    modifier = Modifier.padding(end = 12.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = promptText,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (!subHint.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subHint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            FilledTonalIconButton(
                onClick = onSpeakClick,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("speak_prompt_button"),
                shape = CircleShape,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = accentColor,
                    contentColor = Color.White,
                ),
            ) {
                Icon(
                    imageVector = Icons.Rounded.VolumeUp,
                    contentDescription = "Listen to prompt",
                )
            }
        }
    }
}

@Composable
fun BigKidChoiceButton(
    label: String,
    onClick: () -> Unit,
    containerColor: Color,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    enabled: Boolean = true,
    contentColor: Color = Color.White,
    testTag: String = "choice_button",
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "choice_scale",
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 1.dp),
        modifier = modifier
            .scale(scale)
            .minimumInteractiveComponentSize()
            .testTag(testTag),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (!emoji.isNullOrEmpty()) {
                Text(text = emoji, fontSize = 24.sp)
                if (label.isNotEmpty()) Spacer(modifier = Modifier.width(8.dp))
            }
            if (label.isNotEmpty()) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
fun VisualEmojiGroup(
    emoji: String,
    count: Int,
    modifier: Modifier = Modifier,
) {
    val safeCount = count.coerceIn(0, 20)
    val rows = (safeCount + 4) / 5
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        for (r in 0 until rows) {
            val inRow = minOf(5, safeCount - r * 5)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(inRow) {
                    Text(text = emoji, fontSize = 24.sp)
                }
            }
        }
    }
}

fun defaultTierPraise(tier: AgeTier): String = when (tier) {
    AgeTier.PRESCHOOL -> listOf("Yay! You did it!", "Super star!", "Awesome job!").random()
    AgeTier.K_1 -> listOf("Great thinking!", "Way to go!", "Gold star effort!").random()
    AgeTier.GRADE_2_3 -> listOf("Sharp mind!", "Nailed it!", "Brilliant work!").random()
    AgeTier.GRADE_4_5 -> listOf("Masterful!", "Spot on!", "Top-tier logic!").random()
}
