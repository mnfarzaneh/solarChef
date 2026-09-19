package com.mnfarzaneh.solalrchef.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.mnfarzaneh.solalrchef.R

@Composable
fun CustomSplashScreen() {
    val logoScale = remember { Animatable(0.35f) }

    LaunchedEffect(Unit) {
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 320,
                easing = FastOutSlowInEasing
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3DFC8)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(
                id = R.drawable.solarchef_splash_logo
            ),
            contentDescription = "SolarChef",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth(0.86f)
                .aspectRatio(1f)
                .graphicsLayer {
                    scaleX = logoScale.value
                    scaleY = logoScale.value
                }
        )
    }
}
