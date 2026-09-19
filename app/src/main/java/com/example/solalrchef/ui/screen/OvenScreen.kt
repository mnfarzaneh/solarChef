package com.mnfarzaneh.solalrchef.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mnfarzaneh.solalrchef.R
import com.mnfarzaneh.solalrchef.ui.navigation.NavGraph
import com.mnfarzaneh.solalrchef.ui.theme.AppText
import com.mnfarzaneh.solalrchef.ui.theme.Vazirmatn
import com.mnfarzaneh.solalrchef.viewmodel.CategoryViewModel
import com.mnfarzaneh.solalrchef.viewmodel.CategoryWithCount
import com.mnfarzaneh.solalrchef.viewmodel.OvenViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.zIndex
import androidx.compose.animation.core.LinearEasing
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.drawscope.clipRect

private val doorFrames = listOf(
    R.drawable.closed0, R.drawable.open10, R.drawable.open30,
    R.drawable.open45, R.drawable.open75, R.drawable.open85, R.drawable.open90,
)
private val doorFrameDelays = listOf(80L, 70L, 60L, 60L, 80L, 120L)

// ─── state هر کارت ───────────────────────────────────────
private class CardAnimState {

    val translationY = Animatable(-26f)

    val alpha = Animatable(0f)

    val scale = Animatable(0.94f)

}

// ─── انیمیشن پرتاب کارت (مثل ویندوز سولیتر) ─────────────
private suspend fun revealCard(
    state: CardAnimState,
    index: Int
) = coroutineScope {

    state.alpha.snapTo(0f)
    state.translationY.snapTo((-26 + index * 4).toFloat())
    state.scale.snapTo(0.94f)

    launch {

        state.alpha.animateTo(
            1f,
            tween(
                durationMillis = 180,
                easing = FastOutSlowInEasing
            )
        )

    }

    launch {

        state.translationY.animateTo(
            0f,
            tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            )
        )

    }

    launch {

        state.scale.animateTo(
            1f,
            keyframes {

                durationMillis = 280

                1.015f at 180

                1f at 280

            }

        )

    }

}

private fun navigateToCategoryFromOven(
    navController: NavController,
    viewModel: OvenViewModel,
    categoryId: String,
    categoryName: String
) {
    viewModel.markAnimationCompleted()
    navController.navigate(NavGraph.Screen.Home.route) {
        popUpTo(NavGraph.Screen.Oven.route) { inclusive = true }
    }
    navController.navigate(NavGraph.Screen.CategoryRecipes.createRoute(categoryId, categoryName))
}

private fun navigateToHomeFromOven(navController: NavController, viewModel: OvenViewModel) {
    viewModel.markAnimationCompleted()
    navController.navigate(NavGraph.Screen.Home.route) {
        popUpTo(NavGraph.Screen.Oven.route) { inclusive = true }
    }
}

@Composable
fun OvenScreen(
    onFinished: () -> Unit,
    viewModel: OvenViewModel = viewModel()
) {
    var currentDoorFrame by remember { mutableIntStateOf(0) }

    val morphProgress = remember {
        Animatable(0f)
    }

    fun sectionProgress(
        start: Float,
        end: Float
    ): Float {
        return ((morphProgress.value - start) / (end - start))
            .coerceIn(0f, 1f)
    }

    LaunchedEffect(Unit) {
        if (viewModel.animationCompleted) {
            onFinished()
            return@LaunchedEffect
        }

        delay(200)

        // بازشدن در فر
        for (frame in 1..6) {
            currentDoorFrame = frame
            delay(doorFrameDelays[frame - 1])
        }

        // خروج برند، بزرگ‌شدن و پوشاندن صفحه
        morphProgress.animateTo(
            targetValue = 0.78f,
            animationSpec = tween(
                durationMillis = 930,
                easing = LinearEasing
            )
        )

        viewModel.markAnimationCompleted()
        onFinished()
    }
    val ovenOverlayAlpha =
        1f - sectionProgress(
            start = 0.38f,
            end = 0.74f
        )
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = ovenOverlayAlpha
            }
    ) {
        val density = LocalDensity.current


        val cardEntrance = sectionProgress(0f, 0.38f)
        val surfaceAppearance = sectionProgress(0.08f, 0.20f)
        val surfaceExpansion = sectionProgress(0.14f, 0.78f)
        val cardToHeaderProgress = sectionProgress(0.28f, 0.78f)
        val ovenFade = 1f - sectionProgress(0.16f, 0.72f)
        val headerTravelY = with(density) {
            (maxHeight / 2 - 62.dp).toPx()
        }

        val headerTravelX = with(density) {
            maxOf(
                0f,
                maxWidth.value / 2f - 135f
            ).dp.toPx()
        }

        // تصویر آشپزخانه
        Image(
            painter = painterResource(R.drawable.mainbg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = 1f
                    scaleX = 1f + (morphProgress.value * 0.04f)
                    scaleY = 1f + (morphProgress.value * 0.04f)
                }
        )

        // روشنایی بالای Status bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xEFFFFAF6),
                            Color.Transparent
                        )
                    )
                )
                .graphicsLayer {
                    alpha = ovenFade
                }
        )

        // فر
        Image(
            painter = painterResource(doorFrames[currentDoorFrame]),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.78f)
                .graphicsLayer {
                    alpha = 1f
                    scaleX = 1f - morphProgress.value * 0.05f
                    scaleY = 1f - morphProgress.value * 0.05f
                }
        )

        /*
         * کارت اولیه برند که از فر بیرون می‌آید.
         */
        CompositionLocalProvider(
            LocalLayoutDirection provides LayoutDirection.Rtl
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .zIndex(3f)
                    .graphicsLayer {
                        val initialScale = 0.16f
                        val finalScale = 1f

                        val softEntrance = cardEntrance *
                                cardEntrance *
                                (3f - 2f * cardEntrance)

                        scaleX = initialScale +
                                softEntrance * (finalScale - initialScale)

                        scaleY = initialScale +
                                softEntrance * (finalScale - initialScale)

                        translationY =
                            with(density) {
                                (58.dp * (1f - softEntrance)).toPx()
                            } - headerTravelY * cardToHeaderProgress

                        translationX =
                            headerTravelX * cardToHeaderProgress

                        alpha = sectionProgress(0f, 0.12f)
                    }
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Color.White.copy(
                            alpha = 0.88f * (1f - cardToHeaderProgress)
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(
                            alpha = 0.94f * (1f - cardToHeaderProgress)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(
                        horizontal = (16f * (1f - cardToHeaderProgress)).dp,
                        vertical = (10f * (1f - cardToHeaderProgress)).dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.solarchef_logo),
                    contentDescription = "لوگوی SolarChef",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(17.dp))
                )

                Spacer(Modifier.width(12.dp))
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppText(
                            text = "SolarChef",
                            color = Color(0xFF341D15),
                            fontSize = 27.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.width(7.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Color(0xFFFF7043).copy(
                                        alpha = 0.12f * cardToHeaderProgress
                                    )
                                )
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                                .graphicsLayer {
                                    alpha = cardToHeaderProgress
                                }
                        ) {
                            AppText(
                                text = "آشپزخانه من",
                                color = Color(0xFFFF7043),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(Modifier.height(3.dp))

                    Box {
                        AppText(
                            text = "آشپزخانه همیشه همراه تو",
                            color = Color(0xFF856E63),
                            fontSize = 11.sp,
                            modifier = Modifier.graphicsLayer {
                                alpha = 1f - cardToHeaderProgress
                            }
                        )

                        AppText(
                            text = "سلام، امروز چی می‌پزی؟",
                            color = Color(0xFF856E63),
                            fontSize = 13.sp,
                            modifier = Modifier.graphicsLayer {
                                alpha = cardToHeaderProgress
                            }
                        )
                    }
                }
            }
        }

        // سه نقطه کوچک هنگام تکمیل تبدیل
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 28.dp)
                .zIndex(4f)
                .graphicsLayer {
                    alpha = cardToHeaderProgress                },
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == 1) 7.dp else 5.dp)
                        .clip(CircleShape)
                        .background(
                            Color(0xFFFF7043).copy(
                                alpha = if (index == 1) 0.9f else 0.35f
                            )
                        )
                )
            }
        }
    }
}

// ─── کارت نارنجی مات با انیمیشن پرتاب ───────────────────
@Composable
private fun OvenGlassCard(
    item: CategoryWithCount,
    state: CardAnimState,
    density: androidx.compose.ui.unit.Density,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = tween(80),
        label = "cardPress"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationY = state.translationY.value

                scaleX = state.scale.value * pressScale

                scaleY = state.scale.value * pressScale

                alpha = state.alpha.value
                alpha = state.alpha.value
                shadowElevation = 16f
                shape = RoundedCornerShape(18.dp)
                clip = true
                ambientShadowColor = Color(0xFF8B3A00).copy(alpha = 0.4f)
                spotShadowColor = Color(0xFF8B3A00).copy(alpha = 0.4f)
            }
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFE39A).copy(alpha = 0.65f),
                        Color(0xFFFFB347).copy(alpha = 0.55f),
                        Color(0xFFFF7A00).copy(alpha = 0.42f)
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .drawWithContent {

                drawContent()

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.18f),
                            Color.Transparent
                        )
                    ),
                    cornerRadius = CornerRadius(36f)
                )

            }
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF2C7).copy(alpha = 0.55f),
                        Color(0xFFFFB35C).copy(alpha = 0.22f)
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFD56A).copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            AppText(item.category.emoji, fontSize = 22.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            AppText(
                text = item.category.name,
                style = TextStyle(
                    fontFamily = Vazirmatn,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3E1F0A)
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            AppText(
                text = "${item.recipeCount} دستور",
                style = TextStyle(
                    fontFamily = Vazirmatn,
                    fontSize = 12.sp,
                    color = Color(0xFF6B3410)
                )
            )
        }

        Icon(
            Icons.Default.ChevronLeft,
            contentDescription = null,
            tint = Color(0xFF3E1F0A),
            modifier = Modifier.size(22.dp)
        )
    }
}