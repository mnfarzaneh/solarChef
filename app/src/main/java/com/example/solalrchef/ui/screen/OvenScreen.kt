package com.example.solalrchef.ui.screen

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.solalrchef.R
import com.example.solalrchef.ui.navigation.NavGraph
import com.example.solalrchef.viewmodel.OvenViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import com.example.solalrchef.ui.theme.IranSans
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.remember
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ripple


// تابع کمکی برای رسم متن با outline
// ─── تابع outline نرم ────────────────────────────────────
private fun DrawScope.drawTextWithOutline(
    textMeasurer: TextMeasurer,
    text: String,
    style: TextStyle,
    topLeft: Offset,
    outlineColor: Color,
    outlineWidth: Float = 5f
) {
    // ۳۶۰ درجه دور متن میچرخیم - کاملاً نرم و بدون نقطه
    val steps = 24  // هر چی بیشتر، نرم‌تر (24 کافیه)
    for (i in 0 until steps) {
        val angle = (i * 2 * Math.PI / steps).toFloat()
        val ox = cos(angle) * outlineWidth
        val oy = sin(angle) * outlineWidth
        drawText(
            textLayoutResult = textMeasurer.measure(
                text, style.copy(color = outlineColor)
            ),
            topLeft = Offset(topLeft.x + ox, topLeft.y + oy)
        )
    }
    // متن اصلی روی outline
    drawText(
        textLayoutResult = textMeasurer.measure(text, style),
        topLeft = topLeft
    )
}
// ─── فریم‌های در ─────────────────────────────────────────
private val doorFrames = listOf(
    R.drawable.closed0,
    R.drawable.open10,
    R.drawable.open30,
    R.drawable.open45,
    R.drawable.open75,
    R.drawable.open85,
    R.drawable.open90,
)
private val doorFrameDelays = listOf(
    80L,
    70L,
    60L,
    60L,
    80L,
    120L
)

// ─── موقعیت‌های غذا ──────────────────────────────────────
private data class FoodPosition(val x: Dp, val y: Dp)
private val POS_INSIDE = FoodPosition(23.dp,      17.dp)
private val POS_LEFT   = FoodPosition((-130).dp, (-120).dp)
private val POS_CENTER = FoodPosition(0.dp,      (-180).dp)
private val POS_RIGHT  = FoodPosition(130.dp,    (-120).dp)

// ─── ذره آتش ─────────────────────────────────────────────
private data class FireParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,          // 1.0 → 0.0
    val decay: Float,
    var size: Float,
    val hue: Float            // 0–60 (قرمز تا زرد)
)

// ─── مراحل آتش‌بازی ──────────────────────────────────────
private enum class FirePhase {
    IDLE,        // هنوز شروع نشده
    ROTATING,    // دور دایره میچرخه
    TO_CENTER,   // داره میاد مرکز
    CENTER,      // توی مرکز میسوزه
    DONE         // متن نشون داده شد
}

@Composable
fun OvenScreen(
    navController: NavController,
    viewModel: OvenViewModel = viewModel()
) {
    // ── state انیمیشن در ──────────────────────────────────
    var currentDoorFrame by remember { mutableIntStateOf(0) }

    // ── state غذاها ───────────────────────────────────────
    val interactionSource = remember { MutableInteractionSource() }

    val pressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = tween(80),
        label = "pressScale"
    )

    var fireAlpha by remember { mutableFloatStateOf(1f) }

    val symbols = listOf(
        "✦",
        "✧",
        "✶",
        "✷",
        "✹"
    )

    var showBread by remember { mutableStateOf(false) }
    var showCake  by remember { mutableStateOf(false) }
    var showPizza by remember { mutableStateOf(false) }

    var breadPos  by remember { mutableStateOf(POS_INSIDE) }
    var cakePos   by remember { mutableStateOf(POS_INSIDE) }
    var pizzaPos  by remember { mutableStateOf(POS_INSIDE) }

    var scaleBread by remember { mutableFloatStateOf(0.8f) }
    var scaleCake  by remember { mutableFloatStateOf(0.8f) }
    var scalePizza by remember { mutableFloatStateOf(0.8f) }

    val springSpec = spring<Dp>(dampingRatio = 0.5f, stiffness = Spring.StiffnessVeryLow)

    val breadX     by animateDpAsState(breadPos.x, springSpec, label = "bX")
    val breadY     by animateDpAsState(breadPos.y, springSpec, label = "bY")
    val breadScale by animateFloatAsState(scaleBread, label = "bS")

    val cakeX      by animateDpAsState(cakePos.x, springSpec, label = "cX")
    val cakeY      by animateDpAsState(cakePos.y, springSpec, label = "cY")
    val cakeScale  by animateFloatAsState(scaleCake, label = "cS")

    val pizzaX     by animateDpAsState(pizzaPos.x, springSpec, label = "pX")
    val pizzaY     by animateDpAsState(pizzaPos.y, springSpec, label = "pY")
    val pizzaScale by animateFloatAsState(scalePizza, label = "pS")

    // ── state آتش ─────────────────────────────────────────
    var firePhase     by remember { mutableStateOf(FirePhase.IDLE) }
    var fireAngle     by remember { mutableFloatStateOf((-Math.PI / 2).toFloat()) }
    var fireHeadX     by remember { mutableFloatStateOf(0f) }
    var fireHeadY     by remember { mutableFloatStateOf(0f) }
    var showText      by remember { mutableStateOf(false) }
    var textAlpha     by remember { mutableFloatStateOf(0f) }
    val particles     = remember { mutableStateListOf<FireParticle>() }

    val textMeasurer  = rememberTextMeasurer()
    val scope         = rememberCoroutineScope()

    // ── ریست کامل ─────────────────────────────────────────
    fun resetAnimation() {
        currentDoorFrame = 0
        showBread = false; showCake = false; showPizza = false
        breadPos = POS_INSIDE; cakePos = POS_INSIDE; pizzaPos = POS_INSIDE
        scaleBread = 0.8f; scaleCake = 0.8f; scalePizza = 0.8f
        firePhase = FirePhase.IDLE
        fireAngle = (-Math.PI / 2).toFloat()
        particles.clear()
        showText = false
        textAlpha = 0f
    }

    // ── انیمیشن اصلی ──────────────────────────────────────
    suspend fun startAnimation() {

        // باز شدن در
        delay(200)

        for (frame in 1..6) {
            currentDoorFrame = frame
            delay(doorFrameDelays[frame - 1])
        }

        delay(120)

        // ── نان ──────────────────
        showBread = true
        scaleBread = 0.8f
        breadPos = POS_INSIDE

        delay(80)
        scaleBread = 1.12f

        delay(60)
        breadPos = POS_LEFT

        delay(220)
        scaleBread = 1f

        delay(120)

        // ── کیک ──────────────────
        showCake = true
        scaleCake = 0.8f
        cakePos = POS_INSIDE

        delay(80)
        scaleCake = 1.12f

        delay(60)

        breadPos = POS_CENTER
        cakePos = POS_LEFT

        delay(220)

        scaleCake = 1f
        scaleBread = 1f

        delay(120)

        // ── پیتزا ────────────────
        showPizza = true
        scalePizza = 0.8f
        pizzaPos = POS_INSIDE

        delay(80)
        scalePizza = 1.12f

        delay(60)

        breadPos = POS_RIGHT
        cakePos = POS_CENTER
        pizzaPos = POS_LEFT

        delay(220)

        scalePizza = 1f
        scaleCake = 1f
        scaleBread = 1f

        // کمی مکث قبل از آتش
        delay(250)

        firePhase = FirePhase.ROTATING
    }

    fun showFinalState() {
        currentDoorFrame = 6

        showBread = true
        showCake = true
        showPizza = true

        breadPos = POS_RIGHT
        cakePos = POS_CENTER
        pizzaPos = POS_LEFT

        scaleBread = 1f
        scaleCake = 1f
        scalePizza = 1f

        firePhase = FirePhase.DONE
        showText = true
        textAlpha = 1f
        fireAlpha = 0f
    }

    LaunchedEffect(Unit) {
        if (viewModel.animationCompleted) {
            showFinalState()
        } else {
            startAnimation()
        }
    }

    // ── loop آتش با withFrameMillis ───────────────────────
    LaunchedEffect(firePhase) {
        if (firePhase == FirePhase.IDLE || firePhase == FirePhase.DONE) return@LaunchedEffect

        // شعاع دایره (px) - تقریباً برابر فاصله غذاها از مرکز

        val radius = 340f
        var totalRotated = 0f
        var centerX = 0f
        var centerY = 0f

        while (firePhase != FirePhase.DONE) {
            withFrameMillis { _ ->

                when (firePhase) {

                    FirePhase.ROTATING -> {
                        // سر آتش روی دایره
                        val hx = radius * cos(fireAngle)
                        val hy = radius * sin(fireAngle)
                        fireHeadX = hx
                        fireHeadY = hy

                        // spawn ذرات آتش
                        repeat(5) {
                            val spread = (Random.nextFloat() - 0.5f) * 0.8f
                            val speed  = 2f + Random.nextFloat() * 3f
                            particles.add(
                                FireParticle(
                                    x = hx, y = hy,
                                    vx = cos(fireAngle + spread + Math.PI.toFloat()) * speed * 0.4f,
                                    vy = sin(fireAngle + spread + Math.PI.toFloat()) * speed * 0.4f,
                                    life = 1f,
                                    decay = 0.025f + Random.nextFloat() * 0.035f,
                                    size = 6f + Random.nextFloat() * 10f,
                                    hue = Random.nextFloat() * 50f
                                )
                            )
                        }

                        fireAngle += 0.12f
                        totalRotated += 0.12f

                        if (totalRotated >= Math.PI.toFloat() * 2f) {
                            firePhase = FirePhase.TO_CENTER
                            centerX = 0f
                            centerY = 0f
                        }
                    }

                    FirePhase.TO_CENTER -> {
                        val dx = centerX - fireHeadX
                        val dy = centerY - fireHeadY
                        val dist = sqrt(dx * dx + dy * dy)

                        fireHeadX += dx * 0.1f
                        fireHeadY += dy * 0.1f

                        repeat(6) {
                            val a = Random.nextFloat() * Math.PI.toFloat() * 2f
                            particles.add(
                                FireParticle(
                                    x = fireHeadX, y = fireHeadY,
                                    vx = cos(a) * (1f + Random.nextFloat() * 2f),
                                    vy = sin(a) * (1f + Random.nextFloat() * 2f),
                                    life = 1f,
                                    decay = 0.02f + Random.nextFloat() * 0.03f,
                                    size = 7f + Random.nextFloat() * 12f,
                                    hue = Random.nextFloat() * 55f
                                )
                            )
                        }

                        if (dist < 8f) {
                            firePhase = FirePhase.CENTER
                        }
                    }

                    FirePhase.CENTER -> {
                        repeat(8) {
                            val a = Random.nextFloat() * Math.PI.toFloat() * 2f
                            val spd = 1f + Random.nextFloat() * 3f
                            particles.add(
                                FireParticle(
                                    x = 0f, y = 0f,
                                    vx = cos(a) * spd,
                                    vy = sin(a) * spd - 1f,
                                    life = 1f,
                                    decay = 0.015f + Random.nextFloat() * 0.025f,
                                    size = 8f + Random.nextFloat() * 14f,
                                    hue = Random.nextFloat() * 55f
                                )
                            )
                        }

                        // بعد از ۶۰ فریم متن نشون میده
                        if (!showText) {
                            showText = true
                            scope.launch {

                                repeat(20) {
                                    textAlpha = it / 20f
                                    fireAlpha = 1f - (it / 20f)

                                    delay(16)
                                }

                                textAlpha = 1f
                                fireAlpha = 0f

                                particles.clear()

                                delay(800)

                                firePhase = FirePhase.DONE
                                viewModel.markAnimationCompleted()
                            }
                        }
                    }

                    else -> {}
                }

                // آپدیت همه ذرات
                val iter = particles.iterator()
                while (iter.hasNext()) {
                    val p = iter.next()
                    p.x    += p.vx
                    p.y    += p.vy
                    p.vy   -= 0.06f          // شناوری رو به بالا
                    p.life -= p.decay
                    p.size *= 0.97f
                    if (p.life <= 0f) iter.remove()
                }
            }
        }
    }

    // ── UI ────────────────────────────────────────────────
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // بک‌گراند
        Image(
            painter = painterResource(R.drawable.mainbg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // در فر
        Image(
            painter = painterResource(doorFrames[currentDoorFrame]),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(0.8f)
        )

        // ── Canvas آتش (زیر غذاها) ───────────────────────
        if (firePhase != FirePhase.IDLE) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f

                // رسم ذرات
                particles.forEach { p ->
                    val alpha = p.life.coerceIn(0f, 1f) * fireAlpha
                    val lightness = 0.5f + p.life * 0.3f
                    drawCircle(
                        color = Color.hsv(p.hue, 1f, lightness, alpha),
                        radius = p.size.coerceAtLeast(0.5f),
                        center = Offset(cx + p.x, cy + p.y)
                    )
                }

                // هسته درخشان سر آتش (فاز چرخش و حرکت به مرکز)
                if (firePhase == FirePhase.ROTATING || firePhase == FirePhase.TO_CENTER) {
                    drawCircle(
                        color = Color(1f, 0.95f, 0.6f, 0.95f),
                        radius = 10f,
                        center = Offset(cx + fireHeadX, cy + fireHeadY)
                    )
                    drawCircle(
                        color = Color(1f, 0.7f, 0.2f, 0.5f),
                        radius = 20f,
                        center = Offset(cx + fireHeadX, cy + fireHeadY)
                    )
                }

                // متن مرکز
                if (showText && textAlpha > 0f) {
                    val measured = textMeasurer.measure(
                        text = "غذای خود را انتخاب کنید",
                        style = TextStyle(
                            fontFamily = IranSans,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(1f, 0.9f, 0.4f, textAlpha)
                        )
                    )
                    // توی Canvas، قبل از drawText اضافه کن:

                    if (showText && textAlpha > 0f) {

                        // ── لایه ۱: هاله نارنجی بزرگ و محو ──
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0.85f, 0.25f, 0.0f, 0.45f * textAlpha),  // نارنجی-قرمز
                                    Color(0.7f,  0.15f, 0.0f, 0.25f * textAlpha),  // قرمز تیره
                                    Color.Transparent
                                ),
                                center = Offset(cx, cy),
                                radius = 280f
                            ),
                            radius = 280f,
                            center = Offset(cx, cy)
                        )

                        // ── لایه ۲: هسته درخشان‌تر ──
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(1f, 0.45f, 0.0f, 0.35f * textAlpha),
                                    Color.Transparent
                                ),
                                center = Offset(cx, cy),
                                radius = 160f
                            ),
                            radius = 160f,
                            center = Offset(cx, cy)
                        )

                        // ── تزیین ✦ ✦ ✦ ──
                        val deco = textMeasurer.measure(
                            text = "✦  ✦  ✦",
                            style = TextStyle(
                                fontFamily = IranSans,
                                fontSize = 12.sp,
                                color = Color(1f, 0.7f, 0.2f, textAlpha * 0.8f)
                            )
                        )
                        drawText(
                            textLayoutResult = deco,
                            topLeft = Offset(cx - deco.size.width / 2f, cy + measured.size.height / 2f)
                        )
                    }
                    // جای style متن اصلی
                    val gradientStyle = TextStyle(
                        fontFamily = IranSans,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(1f, 0.95f, 0.3f, textAlpha),   // زرد طلایی
                                Color(1f, 0.55f, 0.1f, textAlpha),   // نارنجی
                                Color(1f, 0.85f, 0.2f, textAlpha),   // طلایی روشن
                            )
                        )
                    )

                    drawTextWithOutline(
                        textMeasurer = textMeasurer,
                        text = "غذای خود را انتخاب کنید",
                        style = gradientStyle,
                        topLeft = Offset(
                            cx - measured.size.width / 2f,
                            cy - measured.size.height / 2f - 20f
                        ),
                        outlineColor = Color(0.3f, 0.05f, 0f, textAlpha * 0.9f),
                        outlineWidth = 4f
                    )
                }
            }
        }

        // ── نان ───────────────────────────────────────────
        if (showBread) {
            val interactionSource = remember { MutableInteractionSource() }
            val pressed by interactionSource.collectIsPressedAsState()
            val pressScale by animateFloatAsState(
                targetValue = if (pressed) 0.88f else 1f,
                animationSpec = tween(80),
                label = "breadPress"
            )
            Image(
                painter = painterResource(R.drawable.bread1),
                contentDescription = null,
                modifier = Modifier
                    .size(90.dp)
                    .offset(x = breadX, y = breadY)
                    .graphicsLayer {
                        scaleX = breadScale * pressScale
                        scaleY = breadScale * pressScale
                    }
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null  // ← بدون ripple
                    ) {
                        navController.navigate(
                            NavGraph.Screen.RecipeDetail.createRoute("bread")
                        )
                    }
            )
        }

        // ── کیک ───────────────────────────────────────────
        if (showCake) {
            val interactionSource = remember { MutableInteractionSource() }
            val pressed by interactionSource.collectIsPressedAsState()
            val pressScale by animateFloatAsState(
                targetValue = if (pressed) 0.88f else 1f,
                animationSpec = tween(80),
                label = "breadPress"
            )
            Image(
                painter = painterResource(R.drawable.cake1),
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .offset(x = cakeX, y = cakeY)
                    .graphicsLayer {
                        scaleX = cakeScale * pressScale
                        scaleY = cakeScale * pressScale
                    }
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null  // ← بدون ripple
                    ) {
                        navController.navigate(
                            NavGraph.Screen.RecipeDetail.createRoute("cake")
                        )
                    }
            )
        }

        // ── پیتزا ─────────────────────────────────────────
        if (showPizza) {
            val interactionSource = remember { MutableInteractionSource() }
            val pressed by interactionSource.collectIsPressedAsState()
            val pressScale by animateFloatAsState(
                targetValue = if (pressed) 0.88f else 1f,
                animationSpec = tween(80),
                label = "breadPress"
            )
            Image(
                painter = painterResource(R.drawable.pizza1),
                contentDescription = null,
                modifier = Modifier
                    .size(93.dp)
                    .offset(x = pizzaX, y = pizzaY)
                    .graphicsLayer {
                        scaleX = pizzaScale * pressScale
                        scaleY = pizzaScale * pressScale
                    }
                    .clip(CircleShape)  // ← اضافه کن تا ripple گرد بشه
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null  // ← بدون ripple
                    ){
                        navController.navigate(
                            NavGraph.Screen.RecipeDetail.createRoute("pizza")
                        )
                    }
            )
        }

        // ── دکمه Replay ───────────────────────────────────
        // ── دکمه Glassmorphism SolarChef ─────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 52.dp, end = 16.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0x663A1700))
                .clickable { navController.navigate(NavGraph.Screen.MyRecipes.route) },
            contentAlignment = Alignment.Center
        ) {
            Text("+", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 160.dp)
                .width(200.dp)
                .height(54.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0x663A1700), // تیره‌تر
                            Color(0x55200000)
                        )
                    )
                )
                // ← بوردر رو حذف کردیم، جاش graphicsLayer با shadow
                .graphicsLayer {
                    shadowElevation = 24f
                    shape = RoundedCornerShape(28.dp)
                    clip = true
                    ambientShadowColor = Color(
                        android.graphics.Color.argb(180, 255, 100, 0)
                    )

                    spotShadowColor = Color(
                        android.graphics.Color.argb(200, 255, 60, 0)
                    )
                }
                .clickable {
                    scope.launch {
                        resetAnimation()
                        delay(200)
                        startAnimation()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(1f, 0.5f, 0.1f, 0.2f),
                                Color.Transparent
                            ),
                            radius = 300f
                        )
                    )
            )

            Text(
                text = "Solar Chef",
                style = TextStyle(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFF8C6),
                            Color(0xFFFFD56A),
                            Color(0xFFFFF1A8)
                        )
                    ),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = IranSans,
                    // ← سایه پشت متن برای خوانایی
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.8f),
                        offset = Offset(0f, 2f),
                        blurRadius = 12f
                    )
                )
            )
        }
    }
}