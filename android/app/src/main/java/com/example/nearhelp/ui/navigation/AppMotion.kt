package com.example.nearhelp.ui.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

/** Shared, non-bouncy motion. Compose applies the system animation duration scale. */
internal object AppMotion {
    const val DURATION = 220

    fun fade() = ContentTransform(
        targetContentEnter = fadeIn(tween(DURATION, easing = FastOutSlowInEasing)),
        initialContentExit = fadeOut(tween(DURATION, easing = FastOutSlowInEasing)),
        sizeTransform = null,
    )

    fun tab(forward: Boolean): ContentTransform = horizontal(forward, distanceDivisor = 24)

    fun page(forward: Boolean): ContentTransform = horizontal(forward, distanceDivisor = 12)

    private fun horizontal(forward: Boolean, distanceDivisor: Int): ContentTransform {
        val direction = if (forward) 1 else -1
        return ContentTransform(
            targetContentEnter = slideInHorizontally(tween(DURATION, easing = FastOutSlowInEasing)) {
                direction * (it / distanceDivisor)
            } + fadeIn(tween(DURATION, easing = FastOutSlowInEasing)),
            initialContentExit = slideOutHorizontally(tween(DURATION, easing = FastOutSlowInEasing)) {
                -direction * (it / distanceDivisor)
            } + fadeOut(tween(DURATION, easing = FastOutSlowInEasing)),
            // Do not animate measurement while two destinations overlap.
            sizeTransform = null,
        )
    }
}
