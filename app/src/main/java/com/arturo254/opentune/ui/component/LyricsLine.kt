package com.arturo254.opentune.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arturo254.opentune.lyrics.LyricsEntry
import com.arturo254.opentune.ui.screens.settings.LyricsPosition

@Composable
fun LyricsLine(
    entry: LyricsEntry,
    index: Int,
    currentLineIndex: Int,
    isSynced: Boolean,
    lyricsTextPosition: LyricsPosition,
    textColor: Color,
    textSize: Float,
    lineSpacing: Float,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isSelected: Boolean,
    isSelectionModeActive: Boolean,
    modifier: Modifier = Modifier
) {
    // 1. Determine the exact state of this line
    val isActive = index == currentLineIndex && isSynced
    val isPast = index < currentLineIndex && isSynced
    // Future is implicitly anything else (index > currentLineIndex or unsynced)

    val targetScale = if (isActive) 1f else 0.85f

    // Alpha controls the "grey/transparent" look from the video:
    // Active = 1.0 (Bright White/Color)
    // Past = 0.25 (Very grey/transparent)
    // Future = 0.5 (Grey, but more visible than past)
    val targetAlpha = when {
        isSelectionModeActive && !isSelected -> 0.3f
        isActive -> 1f
        isPast -> 0.25f
        else -> 0.5f
    }

    // Smooth animations for transitions
    val animatedScale by animateFloatAsState(targetValue = targetScale, label = "scale", animationSpec = tween(400))
    val animatedAlpha by animateFloatAsState(targetValue = targetAlpha, label = "alpha", animationSpec = tween(400))

    // Determine scale origin so text shrinks towards the correct alignment
    val scaleOrigin = remember(lyricsTextPosition) {
        when (lyricsTextPosition) {
            LyricsPosition.LEFT -> TransformOrigin(0f, 0.5f)
            LyricsPosition.CENTER -> TransformOrigin(0.5f, 0.5f)
            LyricsPosition.RIGHT -> TransformOrigin(1f, 0.5f)
        }
    }

    val itemModifier = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
        .background(
            if (isSelected && isSelectionModeActive)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            else Color.Transparent
        )
        .padding(
            horizontal = 8.dp,
            vertical = lineSpacing.dp
        )
        .graphicsLayer {
            transformOrigin = scaleOrigin
            scaleX = animatedScale
            scaleY = animatedScale
            alpha = animatedAlpha
        }

    Column(
        modifier = itemModifier,
        horizontalAlignment = when (lyricsTextPosition) {
            LyricsPosition.LEFT -> Alignment.Start
            LyricsPosition.CENTER -> Alignment.CenterHorizontally
            LyricsPosition.RIGHT -> Alignment.End
        }
    ) {
        val staticFontSize = (textSize + 2).sp
        val staticLineHeight = (textSize + 8).sp

        Text(
            text = entry.text,
            fontSize = staticFontSize,
            lineHeight = staticLineHeight,
            textAlign = when (lyricsTextPosition) {
                LyricsPosition.LEFT -> TextAlign.Left
                LyricsPosition.CENTER -> TextAlign.Center
                LyricsPosition.RIGHT -> TextAlign.Right
            },
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.fillMaxWidth()
        )
    }
}