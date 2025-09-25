package com.corapana.swipematch

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.collections.asReversed
import kotlin.collections.contains
import kotlin.collections.set
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.text.get

@Composable
fun <T : SwipeableCardItem> SwipeCardStack(
    viewModel: GenericCardViewModel<T>,
    swipeConfig: SwipeConfig = SwipeConfig(),
    onSwipe: (item: T, direction: SwipeDirection?) -> Unit,
    onChangeFilters: () -> Unit,
    cardContent: @Composable (card: T, isTop: Boolean, swipeDirection: MutableState<SwipeDirection?>) -> Unit
) {
    val cards by viewModel.cards.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val swipeThreshold = 100f

    // Keep offsets per card to avoid multiple cards moving together
    val cardOffsets =
        remember { mutableStateMapOf<Int, Pair<Animatable<Float, *>, Animatable<Float, *>>>() }

    // Initialize offsets for all cards
    cards.forEach { card ->
        if (!cardOffsets.contains(card.id)) {
            cardOffsets.set(
                card.id, Pair(
                    Animatable(0f),
                    Animatable(0f)
                )
            )
        }
    }

    // Rotation only for top card
    val rotation: State<Float> = remember {
        derivedStateOf {
            val topCard = cards.firstOrNull()
            val offset = topCard?.let { cardOffsets[it.id]?.first?.value ?: 0f } ?: 0f
            offset / 60f
        }
    }


    // Swipe direction for overlay
    val swipeDirection = remember { mutableStateOf<SwipeDirection?>(null) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (cards.isEmpty()) {
            EmptyProfilesState(
                onChangeFilters = onChangeFilters,
                lottieRes = swipeConfig.lottieRes,
                imageRes = swipeConfig.imageRes
            )
        } else {
            // Draw bottom to top
            cards.asReversed().forEachIndexed { index, card ->
                val isTop = index == cards.lastIndex
                val (offsetX, offsetY) = cardOffsets[card.id]!!

                val cardModifier = if (isTop) {
                    Modifier
                        .offset {
                            IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt())
                        }
                        .rotate(rotation.value)
                        .pointerInput(card.id) {
                            detectDragGestures(
                                onDragEnd = {
                                    coroutineScope.launch {
                                        val shouldDismiss =
                                            abs(offsetX.value) > swipeThreshold || offsetY.value < -swipeThreshold

                                        if (shouldDismiss) {
                                            // Determine swipe direction for overlay
                                            val direction = when {
                                                offsetX.value > swipeThreshold -> SwipeDirection.RIGHT
                                                offsetX.value < -swipeThreshold -> SwipeDirection.LEFT
                                                offsetY.value < -swipeThreshold -> SwipeDirection.UP
                                                offsetY.value > swipeThreshold -> SwipeDirection.DOWN
                                                else -> null
                                            }
                                            swipeDirection.value = if (direction != null &&
                                                swipeConfig.enabledDirections.contains(direction)
                                            ) direction else null

                                            // Use animateDecay for natural inertia
                                            val velocityX = offsetX.value * 8f
                                            val velocityY = offsetY.value * 8f

                                            val dirX = offsetX.value
                                            val dirY = offsetY.value
                                            val magnitude =
                                                sqrt(dirX * dirX + dirY * dirY)
                                            val unitX = dirX / magnitude
                                            val unitY = dirY / magnitude

                                            val speed = 25f

                                            val decay = exponentialDecay<Float>()

                                            launch { offsetX.animateDecay(velocityX, decay) }
                                            launch { offsetY.animateDecay(velocityY, decay) }

                                            // Wait until card is offscreen
                                            while (
                                                abs(offsetX.value) < screenWidth.value * 1.5f &&
                                                abs(offsetY.value) < screenHeight.value * 1.5f
                                            ) {
                                                offsetX.snapTo(offsetX.value + unitX * speed)
                                                offsetY.snapTo(offsetY.value + unitY * speed)
                                                delay(16)
                                            }

                                            // Remove top card and cleanup offsets\
                                            onSwipe(card, swipeDirection.value)
                                            viewModel.removeTopAndAddNew()
                                            cardOffsets.remove(card.id)

                                            offsetX.snapTo(0f)
                                            offsetY.snapTo(0f)
                                            swipeDirection.value = null
                                        } else {
                                            // Not enough swipe → bounce back
                                            offsetX.animateTo(0f, tween(300))
                                            offsetY.animateTo(0f, tween(300))
                                            swipeDirection.value = null
                                        }
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    coroutineScope.launch {
                                        offsetX.snapTo(offsetX.value + dragAmount.x)
                                        offsetY.snapTo(offsetY.value + dragAmount.y)

                                        // Update overlay during drag
                                        val direction = when {
                                            offsetX.value > swipeThreshold -> SwipeDirection.RIGHT
                                            offsetX.value < -swipeThreshold -> SwipeDirection.LEFT
                                            offsetY.value < -swipeThreshold -> SwipeDirection.UP
                                            offsetY.value > swipeThreshold -> SwipeDirection.DOWN
                                            else -> null
                                        }
                                        swipeDirection.value = if (direction != null &&
                                            swipeConfig.enabledDirections.contains(direction)
                                        ) direction else null
                                    }
                                }
                            )
                        }
                } else {
                    Modifier.offset(y = ((cards.size - index - 1) * 8).dp)
                }

                Box(
                    modifier = cardModifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .shadow(16.dp, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    cardContent(card, isTop, swipeDirection)
                }
            }
        }
    }
}

@Composable
fun EmptyProfilesState(
    onChangeFilters: () -> Unit,
    // Optional Lottie raw resource
    lottieRes: Int? = null,
    // Optional image resource
    imageRes: Int? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            // ✅ Show Lottie if given
            lottieRes != null -> {
                val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieRes))
                val progress by animateLottieCompositionAsState(composition)

                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(180.dp)
                )
            }

            // ✅ Show Image if given
            imageRes != null -> {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = "Empty state",
                    modifier = Modifier.size(120.dp)
                )
            }

            // ✅ Fallback to default icon
            else -> {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = "No profiles",
                    tint = Color(0xFFE91E63),
                    modifier = Modifier.size(96.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "You’re all caught up 🎉",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Looks like you’ve seen everyone nearby.\nTry changing your filters to discover more matches.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = Color.Gray,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onChangeFilters,
            shape = RoundedCornerShape(50), // pill style
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(50.dp)
        ) {
            Text(
                "Change Filters",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

