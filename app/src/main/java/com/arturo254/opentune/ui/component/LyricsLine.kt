package com.arturo254.opentune.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arturo254.opentune.lyrics.LyricsEntry
import com.arturo254.opentune.ui.screens.settings.LyricsPosition
import kotlin.math.absoluteValue

@Composable
fun LyricsLine(
    entry: LyricsEntry,
    listState: LazyListState,
    index: Int,
    isActive: Boolean, // True if this is the line currently being sung
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
    // 1. Calculate screen position for the "Wheel" scaling effect
    val layoutInfo by remember { derivedStateOf { listState.layoutInfo } }

    val projection by remember(index, listState.firstVisibleItemIndex) {
        derivedStateOf {
            val visibleInfo = layoutInfo.visibleItemsInfo.find { it.index == index }
            if (visibleInfo != null) {
                val viewportHeight = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
                val viewportCenter = layoutInfo.viewportStartOffset + (viewportHeight / 2f)
                val itemCenter = visibleInfo.offset + (visibleInfo.size / 2f)
                val distanceFromCenter = (viewportCenter - itemCenter).absoluteValue

                // 0.0 = Center, 1.0 = Edge
                val normalizedDistance = (distanceFromCenter / (viewportHeight / 2.2f)).coerceIn(0f, 1f)
                normalizedDistance
            } else {
                1f // Off screen
            }
        }
    }

    // 2. Visual Logic

    // Scale: Active line is always full size. Inactive lines shrink as they move away.
    val targetScale = if (isActive) 1f else (1f - (projection * 0.15f))

    // Alpha: Active line is ALWAYS 1.0 (unless selecting). Inactive lines fade out at edges.
    val targetAlpha = if (isActive && !isSelectionModeActive) 1f else (1f - (projection * 0.7f)).coerceAtLeast(0.2f)

    val animatedScale by animateFloatAsState(targetValue = targetScale, label = "scale", animationSpec = tween(400))
    val animatedAlpha by animateFloatAsState(targetValue = targetAlpha, label = "alpha", animationSpec = tween(400))

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
            horizontal = 24.dp,
            vertical = (lineSpacing + if(isActive) 4f else 1f).dp
        )
        .graphicsLayer {
            scaleX = animatedScale
            scaleY = animatedScale
            alpha = animatedAlpha
            // No Blur here anymore
        }

    Column(
        modifier = itemModifier,
        horizontalAlignment = when (lyricsTextPosition) {
            LyricsPosition.LEFT -> Alignment.Start
            LyricsPosition.CENTER -> Alignment.CenterHorizontally
            LyricsPosition.RIGHT -> Alignment.End
        }
    ) {
        val fontSize = if(isActive) (textSize + 4).sp else textSize.sp
        Text(
            text = entry.text,
            fontSize = fontSize,
            lineHeight = fontSize,
            textAlign = when (lyricsTextPosition) {
                LyricsPosition.LEFT -> TextAlign.Left
                LyricsPosition.CENTER -> TextAlign.Center
                LyricsPosition.RIGHT -> TextAlign.Right
            },
            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
            // Active = Color, Inactive = Color with opacity (handled by graphicsLayer alpha mostly, but added here for depth)
            color = if (isActive) textColor else textColor.copy(alpha = 0.8f),
            modifier = Modifier.fillMaxWidth()
        )
    }
}