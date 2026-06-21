package com.example.solalrchef.ui.screen

import android.app.Application
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.solalrchef.model.Ingredient
import com.example.solalrchef.ui.navigation.NavGraph
import com.example.solalrchef.viewmodel.CookingViewModel

// ─── رنگ‌ها ───────────────────────────────────────────────
private val CkBgLight      = Color(0xFFF5EFE6)
private val CkGlassCard    = Color(0xAAFFFFFF)
private val CkGlassWhite   = Color(0xCCFFFFFF)
private val CkAccentOrange = Color(0xFFFF6B35)
private val CkAccentGreen  = Color(0xFF4CAF50)
private val CkAccentBlue   = Color(0xFF4A90D9)
private val CkTextDark     = Color(0xFF2C1810)
private val CkTextLight    = Color(0xFF9E7B6A)
private val CkDivider      = Color(0x33000000)

@Composable
fun CookingScreen(
    recipeId: String,
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: CookingViewModel = hiltViewModel()

    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // لودینگ
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = CkAccentOrange)
        }
        return
    }

    // دستور پیدا نشد
    val recipe = uiState.recipe ?: run {
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── عکس ثابت ─────────────────────────────────────
        if (recipe.imagePath.isNotEmpty()) {
            AsyncImage(
                model = Uri.parse(recipe.imagePath),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.45f)
            )
        } else {
            Image(
                painter = painterResource(recipe.detailImage),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.45f)
            )
        }

        // gradient پایین عکس
        Box(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.45f),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, CkBgLight)
                        )
                    )
            )
        }

        // ── کارت اسکرول‌شونده ────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(340.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(CkBgLight)
            ) {
                // هندل اسکرول
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 10.dp)
                        .width(40.dp).height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(CkTextLight.copy(alpha = 0.4f))
                )

                Spacer(modifier = Modifier.height(8.dp))

                // عنوان + رتبه
                CkTitleSection(recipe = recipe)

                // آمار سریع
                CkStatsRow(recipe = recipe)

                // دکمه ماشین حساب
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IngredientCalculatorButton(
                        onClick = { viewModel.showCalculator() }
                    )
                }

                Divider(color = CkDivider, thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 20.dp))

                // وعده + کالری
                CkServingsRow(
                    servings = uiState.servings,
                    calories = recipe.calories,
                    onMinus  = { viewModel.decrementServings() },
                    onPlus   = { viewModel.incrementServings() }
                )

                Divider(color = CkDivider, thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 20.dp))

                // تب‌ها
                TabRow(
                    selectedTabIndex = uiState.selectedTab,
                    containerColor   = CkBgLight,
                    contentColor     = CkAccentOrange,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab]),
                            color    = CkAccentOrange
                        )
                    }
                ) {
                    Tab(
                        selected = uiState.selectedTab == 0,
                        onClick  = { viewModel.selectTab(0) },
                        text = {
                            Text(
                                "مواد لازم",
                                color = if (uiState.selectedTab == 0) CkAccentOrange else CkTextLight,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    )
                    Tab(
                        selected = uiState.selectedTab == 1,
                        onClick  = { viewModel.selectTab(1) },
                        text = {
                            Text(
                                "طرز تهیه",
                                color = if (uiState.selectedTab == 1) CkAccentOrange else CkTextLight,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    )
                }

                // محتوا
                if (uiState.selectedTab == 0) {
                    CkIngredientsSection(
                        ingredients = recipe.ingredients,
                        servingsMultiplier = uiState.servings.toFloat() /
                                (recipe.yield.toIntOrNull() ?: 4).toFloat()
                    )
                } else {
                    CkStepsSection(
                        steps = recipe.steps,
                        completedSteps = uiState.completedSteps,
                        onStepToggle = { viewModel.toggleStep(it) }
                    )
                }

                Spacer(modifier = Modifier.height(60.dp))
            }
        }

        // ── دکمه‌های Back و Close ─────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 52.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            GlassIconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back",
                    tint = CkTextDark, modifier = Modifier.size(20.dp))
            }
            GlassIconButton(
                onClick = {
                    while (navController.currentDestination?.route != NavGraph.Screen.Oven.route) {
                        navController.popBackStack()
                    }
                }
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close",
                    tint = CkTextDark, modifier = Modifier.size(20.dp))
            }
        }

        // ── ماشین حساب ───────────────────────────────────
        if (uiState.showCalculator) {
            IngredientCalculatorSheet(
                ingredients = recipe.ingredients,
                baseYield   = recipe.yield.toIntOrNull() ?: 4,
                onDismiss   = { viewModel.hideCalculator() }
            )
        }
    }
}

// ─── عنوان + ستاره ────────────────────────────────────────
@Composable
fun CkTitleSection(recipe: com.example.solalrchef.model.Recipe) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text("${recipe.source} • ${recipe.difficulty}",
            color = CkAccentOrange, fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(recipe.title, color = CkTextDark, fontSize = 26.sp,
            fontWeight = FontWeight.Bold, lineHeight = 32.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text("recipe by ${recipe.author}", color = CkTextLight, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(8.dp))
        CkRatingBar(rating = recipe.rating)
    }
}

@Composable
fun CkRatingBar(rating: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(5) { index ->
            Text(
                text  = if (index < rating.toInt()) "★" else "☆",
                color = if (index < rating.toInt()) Color(0xFFF59E0B) else CkTextLight,
                fontSize = 18.sp
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(rating.toString(), color = CkTextLight, fontSize = 13.sp)
    }
}

// ─── آمار سریع ───────────────────────────────────────────
@Composable
fun CkStatsRow(recipe: com.example.solalrchef.model.Recipe) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CkStatCard("زمان کل",  recipe.totalTime, CkAccentOrange, Modifier.weight(1f))
        CkStatCard("زمان پخت", recipe.cookTime,  CkAccentBlue,   Modifier.weight(1f))
    }
}

@Composable
fun CkStatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CkGlassCard)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = CkTextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = CkTextLight, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth().height(3.dp)
            .clip(RoundedCornerShape(2.dp)).background(color.copy(alpha = 0.2f))) {
            Box(modifier = Modifier.fillMaxWidth(0.7f).height(3.dp).background(color))
        }
    }
}

// ─── وعده + کالری ─────────────────────────────────────────
@Composable
fun CkServingsRow(servings: Int, calories: Int, onMinus: () -> Unit, onPlus: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(36.dp).clip(CircleShape)
                .background(CkGlassCard).clickable { onMinus() },
                contentAlignment = Alignment.Center) {
                Text("−", color = CkTextDark, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Text("$servings\nوعده", color = CkTextDark, fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 14.dp), lineHeight = 20.sp)
            Box(modifier = Modifier.size(36.dp).clip(CircleShape)
                .background(CkAccentOrange).clickable { onPlus() },
                contentAlignment = Alignment.Center) {
                Text("+", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("$calories", color = CkTextDark, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("کالری در هر وعده", color = CkTextLight, fontSize = 11.sp)
        }
    }
}

// ─── مواد لازم ────────────────────────────────────────────
@Composable
fun CkIngredientsSection(ingredients: List<Ingredient>, servingsMultiplier: Float) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text("INGREDIENTS", color = CkTextLight, fontSize = 11.sp,
            fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp))
        ingredients.forEach { ingredient ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically) {
                val adjusted = ingredient.amount.toFloatOrNull()
                    ?.let { it * servingsMultiplier }
                    ?.let {
                        if (it == it.toLong().toFloat()) it.toLong().toString()
                        else String.format("%.1f", it)
                    } ?: ingredient.amount
                Text("$adjusted ${ingredient.unit}", color = CkAccentOrange,
                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.width(110.dp))
                Text(ingredient.name, color = CkTextDark, fontSize = 14.sp)
            }
            Divider(color = CkDivider, thickness = 0.5.dp)
        }
    }
}

// ─── طرز تهیه ─────────────────────────────────────────────
@Composable
fun CkStepsSection(
    steps: List<com.example.solalrchef.model.CookingStep>,
    completedSteps: List<Boolean>,
    onStepToggle: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text("DIRECTIONS", color = CkTextLight, fontSize = 11.sp,
            fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp))

        steps.forEachIndexed { index, step ->
            val bgColor by animateColorAsState(
                targetValue = if (completedSteps.getOrElse(index) { false })
                    CkAccentGreen.copy(alpha = 0.12f) else Color.Transparent,
                label = "stepBg"
            )
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor)
                    .clickable { onStepToggle(index) }
                    .padding(vertical = 12.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier.size(30.dp).clip(CircleShape)
                        .background(
                            if (completedSteps.getOrElse(index) { false }) CkAccentGreen
                            else CkGlassCard
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (completedSteps.getOrElse(index) { false }) {
                        Icon(Icons.Default.Check, null, tint = Color.White,
                            modifier = Modifier.size(16.dp))
                    } else {
                        Text((index + 1).toString(), color = CkTextDark,
                            fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    step.instruction,
                    color = if (completedSteps.getOrElse(index) { false }) CkTextLight else CkTextDark,
                    fontSize = 15.sp, lineHeight = 23.sp,
                    textDecoration = if (completedSteps.getOrElse(index) { false })
                        TextDecoration.LineThrough else TextDecoration.None,
                    modifier = Modifier.weight(1f)
                )
            }
            if (index < steps.lastIndex) Spacer(modifier = Modifier.height(4.dp))
        }
    }
}