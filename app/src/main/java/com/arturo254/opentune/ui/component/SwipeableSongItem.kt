package com.arturo254.opentune.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.arturo254.opentune.R
import com.arturo254.opentune.constants.SwipeToQueueKey
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.input.pointer.PointerEventPass
import com.arturo254.opentune.utils.rememberPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableSongItem(
    onSwipeToQueue: () -> Unit,
    songTitle: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val swipeToQueueEnabled by rememberPreference(SwipeToQueueKey, defaultValue = true)

    if (!swipeToQueueEnabled) {
        Box(modifier = modifier) {
            content()
        }
        return
    }

    var isTouching by remember { mutableStateOf(false) }
    var dragX by remember { mutableStateOf(0f) }
    var width by remember { mutableStateOf(0) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            false // Always snap back, the action is handled on release
        }
    )

    LaunchedEffect(isTouching) {
        if (!isTouching) {
            val isPastThreshold = dragX > width / 2 && width > 0
            if (isPastThreshold) {
                onSwipeToQueue()
                val toastMessage = if (songTitle != null) {
                    context.getString(R.string.added_to_queue) + ": $songTitle"
                } else {
                    context.getString(R.string.added_to_queue)
                }
                Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
            }
            dragX = 0f
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromEndToStart = false,
        backgroundContent = {
            val isPastThreshold = dragX > width / 2 && width > 0
            val isVisible = isPastThreshold || Math.abs(dragX) > 10f
            
            val color by animateColorAsState(
                if (isPastThreshold) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                label = "backgroundColor"
            )
            val scale by animateFloatAsState(
                if (isPastThreshold) 1f else 0.75f,
                label = "iconScale"
            )

            Box(
                Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (isVisible) {
                    Icon(
                        painter = painterResource(R.drawable.queue_music),
                        contentDescription = null,
                        modifier = Modifier.scale(scale),
                        tint = if (isPastThreshold) 
                            MaterialTheme.colorScheme.onPrimaryContainer 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        },
        modifier = modifier
            .onSizeChanged { width = it.width }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        isTouching = event.changes.any { it.pressed }
                        if (isTouching) {
                            event.changes.forEach { change ->
                                dragX += change.position.x - change.previousPosition.x
                            }
                        }
                    }
                }
            },
        content = { content() }
    )
}
