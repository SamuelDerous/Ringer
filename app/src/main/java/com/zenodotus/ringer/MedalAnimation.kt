package com.zenodotus.ringer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MedalAnimation(
    visible: Boolean,
    onAnimationEnd: () -> Unit
) {
    // schaal en alpha animaties
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(visible) {
        if (visible) {
            // zoom in
            alpha.snapTo(0f)
            scale.snapTo(0f)

            launch {
                scale.animateTo(
                    targetValue = 1.5f,
                    animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
                )
                scale.animateTo(
                    targetValue = 1.5f,
                    animationSpec = tween(durationMillis = 700)
                )
            }

            launch {
                alpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 700)
                )
                delay(500) // even zichtbaar
                alpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 700)
                )
            }

            delay(1100) // totale animatie tijd
            onAnimationEnd()
        }
    }

    if (alpha.value > 0f) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    this.alpha = alpha.value
                },
            contentAlignment = Alignment.Center
        ) {
            val medalPainter = rememberAsyncImagePainter("file:///android_asset/medal.svg")
            Image(
                painter = medalPainter,
                contentDescription = "Medal",
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale.value)      // voor zoom
                    .alpha(alpha.value)      // voor vervagen
            )
        }
    }
}
