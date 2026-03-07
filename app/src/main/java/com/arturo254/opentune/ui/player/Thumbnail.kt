package com.arturo254.opentune.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.media3.common.C
import androidx.media3.common.Player
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.arturo254.opentune.LocalPlayerConnection
import com.arturo254.opentune.R
import com.arturo254.opentune.constants.*
import com.arturo254.opentune.ui.component.AppConfig
import com.arturo254.opentune.utils.rememberEnumPreference
import com.arturo254.opentune.utils.rememberPreference
import kotlinx.coroutines.delay
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Thumbnail(
    sliderPositionProvider: () -> Long?,
    onOpenFullscreenLyrics: () -> Unit,
    modifier: Modifier = Modifier,
    isPlayerExpanded: Boolean = true,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val context = LocalContext.current

    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val error by playerConnection.error.collectAsState()
    val queueTitle by playerConnection.queueTitle.collectAsState()

    val swipeThumbnail by rememberPreference(SwipeThumbnailKey, true)
    val canSkipPrevious by playerConnection.canSkipPrevious.collectAsState()
    val canSkipNext by playerConnection.canSkipNext.collectAsState()

    val playerBackground by rememberEnumPreference(
        key = PlayerBackgroundStyleKey,
        defaultValue = PlayerBackgroundStyle.DEFAULT
    )

    val isAppleMusicStyle = playerBackground == PlayerBackgroundStyle.APPLE_MUSIC

    var thumbnailCornerRadius by remember { mutableStateOf(16f) }
    LaunchedEffect(Unit) {
        thumbnailCornerRadius = AppConfig.getThumbnailCornerRadius(context)
    }

    val textBackgroundColor = when (playerBackground) {
        PlayerBackgroundStyle.DEFAULT -> MaterialTheme.colorScheme.onBackground
        else -> Color.White
    }

    val queueWindows by playerConnection.queueWindows.collectAsState()
    val currentWindowIndex by playerConnection.currentWindowIndex.collectAsState()

    val pagerState = rememberPagerState(
        initialPage = Math.max(0, currentWindowIndex),
        pageCount = { queueWindows.size }
    )

    LaunchedEffect(currentWindowIndex) {
        if (currentWindowIndex != -1 && pagerState.currentPage != currentWindowIndex) {
            pagerState.animateScrollToPage(currentWindowIndex)
        }
    }

    LaunchedEffect(pagerState.settledPage) {
        if (pagerState.settledPage != currentWindowIndex && currentWindowIndex != -1) {
            if (pagerState.settledPage > currentWindowIndex) {
                playerConnection.seekToNext()
            } else {
                playerConnection.skipToPrevious()
            }
        }
    }

    Box(modifier = modifier) {

        AnimatedVisibility(
            visible = error == null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize().statusBarsPadding()
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isAppleMusicStyle) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.playing_from),
                            style = MaterialTheme.typography.titleMedium,
                            color = textBackgroundColor
                        )

                        val playingFrom = queueTitle ?: mediaMetadata?.album?.title
                        if (!playingFrom.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = playingFrom,
                                style = MaterialTheme.typography.titleMedium,
                                color = textBackgroundColor.copy(alpha = 0.8f),
                                maxLines = 1,
                                modifier = Modifier.basicMarquee()
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(24.dp))
                }
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val size = maxWidth - PlayerHorizontalPadding * 2

                    HorizontalPager(
                        state = pagerState,
                        userScrollEnabled = swipeThumbnail && isPlayerExpanded,
                        modifier = Modifier.fillMaxSize(),
                        beyondViewportPageCount = 1
                    ) { page ->
                        val window = queueWindows.getOrNull(page)
                        val item = window?.mediaItem

                        Box(
                            modifier = Modifier
                                .width(maxWidth)
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = { onOpenFullscreenLyrics() },
                                        onDoubleTap = { offset ->
                                            if (offset.x < size.toPx() / 2) {
                                                playerConnection.player.seekBack()
                                            } else {
                                                playerConnection.player.seekForward()
                                            }
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {

                            if (isAppleMusicStyle) {
                                // CARÁTULA OCULTA
                                Box(modifier = Modifier.size(size))
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(size)
                                        .clip(RoundedCornerShape(thumbnailCornerRadius.dp * 2))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(item?.mediaMetadata?.artworkUri?.toString())
                                            .memoryCachePolicy(CachePolicy.ENABLED)
                                            .diskCachePolicy(CachePolicy.ENABLED)
                                            .networkCachePolicy(CachePolicy.ENABLED)
                                            .build(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        var showSeekEffect by remember { mutableStateOf(false) }
        var seekDirection by remember { mutableStateOf("") }

        LaunchedEffect(showSeekEffect) {
            if (showSeekEffect) {
                delay(1000)
                showSeekEffect = false
            }
        }

        AnimatedVisibility(
            visible = showSeekEffect,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(
                text = seekDirection,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            )
        }
    }
}
