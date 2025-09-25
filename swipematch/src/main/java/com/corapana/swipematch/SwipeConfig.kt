package com.corapana.swipematch

import kotlin.Int

/**
 * Configuration options for the swipeable card stack.
 *
 * @property enabledDirections The set of directions the user can swipe.
 * By default: LEFT, RIGHT, and UP are enabled.
 *
 * @property directionLabels Text labels shown during swipe for each direction.
 * Example: LEFT = "NOPE", RIGHT = "LIKE", UP = "SUPER LIKE".
 *
 * @property lottieRes (Optional) Raw resource ID of a Lottie animation to show
 * when the stack is empty.
 * ⚠️ Takes **highest priority**: if this is set, it will be used
 * instead of [imageRes] or the default icon.
 *
 * @property imageRes (Optional) Drawable resource ID of a static image to show
 * when the stack is empty.
 * ⚠️ Used **only if [lottieRes] is null**. If both are null,
 * a default icon will be displayed.
 */
data class SwipeConfig(
    val enabledDirections: Set<SwipeDirection> = setOf(
        SwipeDirection.LEFT,
        SwipeDirection.RIGHT,
        SwipeDirection.UP
    ),
    val directionLabels: Map<SwipeDirection, String> = mapOf(
        SwipeDirection.LEFT to "NOPE",
        SwipeDirection.RIGHT to "LIKE",
        SwipeDirection.UP to "SUPER LIKE"
    ),
    val lottieRes: Int? = null,
    val imageRes: Int? = null,
)

