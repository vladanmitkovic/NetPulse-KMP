package me.mitkovic.kmp.netpulse.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun VerticalProgressIndicator(
    progress: Float,
    color: Color,
    fromBottom: Boolean = false,
    modifier: Modifier = Modifier,
    animate: Boolean = false,
    durationMillis: Int = 0,
    reset: Boolean = false,
    complete: Boolean = false,
) {
    val animatedProgress = remember { Animatable(progress) }

    LaunchedEffect(animate) {
        if (animate) {
            animatedProgress.snapTo(0f)
            animatedProgress.animateTo(
                1f,
                animationSpec =
                    tween(
                        durationMillis = durationMillis,
                        easing = LinearEasing,
                    ),
            )
        }
    }

    LaunchedEffect(reset) {
        if (reset) {
            animatedProgress.snapTo(0f)
        }
    }

    LaunchedEffect(complete) {
        if (complete) {
            animatedProgress.stop()
            animatedProgress.snapTo(1f)
        }
    }

    val useProgress = if (animate || complete || reset) animatedProgress.value else progress

    Box(
        modifier = modifier.background(Color.Gray.copy(alpha = 0.3f)),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(useProgress)
                    .background(color)
                    .align(if (fromBottom) Alignment.BottomCenter else Alignment.TopCenter),
        )
    }
}
