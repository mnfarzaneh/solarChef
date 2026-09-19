package com.mnfarzaneh.solalrchef.ui.screen

import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mnfarzaneh.solalrchef.R
import com.mnfarzaneh.solalrchef.model.Ingredient
import com.mnfarzaneh.solalrchef.model.UnitCategory
import com.mnfarzaneh.solalrchef.model.findUnit
import com.mnfarzaneh.solalrchef.ui.components.IngredientCalculatorButton
import com.mnfarzaneh.solalrchef.ui.components.IngredientCalculatorSheet
import com.mnfarzaneh.solalrchef.ui.theme.AppText
import com.mnfarzaneh.solalrchef.viewmodel.CookingViewModel
import com.mnfarzaneh.solalrchef.ui.theme.GlassColors
import com.mnfarzaneh.solalrchef.ui.theme.GlassIconButton
import com.mnfarzaneh.solalrchef.ui.theme.GlassActionButton
import kotlin.math.abs
// ─── رنگ‌ها ───────────────────────────────────────────────


@Composable
fun CookingScreen(
    recipeId: String,
    navController: NavController
) {
    CookingModeScreen(recipeId = recipeId, navController = navController)
    return

    val context = LocalContext.current
    val viewModel: CookingViewModel = hiltViewModel()

    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // لودینگ
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = GlassColors.AccentOrange)
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
        } else if (recipe.detailImage != 0) {
            Image(
                painter = painterResource(recipe.detailImage),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.45f)
            )
        } else {

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
                                colors = listOf(Color.Transparent, GlassColors.BgLight)
                            )
                        )
                )
            }
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
                    .background(GlassColors.BgLight)
            ) {
                // هندل اسکرول
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 10.dp)
                        .width(40.dp).height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(GlassColors.TextLight.copy(alpha = 0.4f))
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

                Divider(color = GlassColors.Divider, thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 20.dp))

                // وعده + کالری
                CkServingsRow(
                    servings = uiState.servings,
                    calories = recipe.calories,
                    onMinus  = { viewModel.decrementServings() },
                    onPlus   = { viewModel.incrementServings() }
                )

                Divider(color = GlassColors.Divider, thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 20.dp))

                // تب‌ها
                TabRow(
                    selectedTabIndex = uiState.selectedTab,
                    containerColor   = GlassColors.BgLight,
                    contentColor     = GlassColors.AccentOrange,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab]),
                            color    = GlassColors.AccentOrange
                        )
                    }
                ) {
                    Tab(
                        selected = uiState.selectedTab == 0,
                        onClick  = { viewModel.selectTab(0) },
                        text = {
                            AppText(
                                stringResource(R.string.recipe_ingredients),
                                color = if (uiState.selectedTab == 0) GlassColors.AccentOrange else GlassColors.TextLight,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    )
                    Tab(
                        selected = uiState.selectedTab == 1,
                        onClick  = { viewModel.selectTab(1) },
                        text = {
                            AppText(
                                stringResource(R.string.recipe_equipment),
                                color = if (uiState.selectedTab == 1) GlassColors.AccentOrange else GlassColors.TextLight,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    )
                    Tab(
                        selected = uiState.selectedTab == 2,
                        onClick  = { viewModel.selectTab(2) },
                        text = {
                            AppText(
                                stringResource(R.string.cooking_tab_method),
                                color = if (uiState.selectedTab == 2) GlassColors.AccentOrange else GlassColors.TextLight,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    )
                }

                // محتوا
                when (uiState.selectedTab) {

                    0 -> {
                        CkIngredientsSection(
                            ingredients = recipe.ingredients,
                            servingsMultiplier =
                                uiState.servings.toFloat() /
                                        (recipe.yield.toIntOrNull() ?: 1).toFloat()
                        )
                    }

                    1 -> {
                        CkEquipmentSection(
                            equipment = recipe.equipment
                        )
                    }

                    2 -> {
                        CkStepsSection(
                            steps = recipe.steps,
                            completedSteps = uiState.completedSteps,
                            onStepToggle = { viewModel.toggleStep(it) }
                        )
                    }
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
            GlassIconButton(
                onClick = {
                    navController.popBackStack()
                }
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_description_back),
                    tint = GlassColors.TextDark, modifier = Modifier.size(20.dp))
            }
        }

        // ── ماشین حساب ───────────────────────────────────
        if (uiState.showCalculator) {
            IngredientCalculatorSheet(
                ingredients = recipe.ingredients,
                baseYield   = recipe.yield.toIntOrNull() ?: 1,
                onDismiss   = { viewModel.hideCalculator() }
            )
        }
    }
}

// ─── عنوان + ستاره ────────────────────────────────────────
@Composable
fun CkTitleSection(recipe: com.mnfarzaneh.solalrchef.model.Recipe) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        AppText(stringResource(R.string.cooking_level, recipe.difficulty),
            color = GlassColors.AccentOrange, fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
        Spacer(modifier = Modifier.height(4.dp))
        AppText(recipe.title, color = GlassColors.TextDark, fontSize = 26.sp,
            fontWeight = FontWeight.Bold, lineHeight = 32.sp)
        Spacer(modifier = Modifier.height(4.dp))
        AppText(stringResource(R.string.cooking_personal_recipe, recipe.author), color = GlassColors.TextLight, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(8.dp))
        CkRatingBar(rating = recipe.rating)
    }
}

@Composable
fun CkRatingBar(rating: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(5) { index ->
            AppText(
                text  = if (index < rating.toInt()) "★" else "☆",
                color = if (index < rating.toInt()) Color(0xFFF59E0B) else GlassColors.TextLight,
                fontSize = 18.sp
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        AppText(rating.toString(), color = GlassColors.TextLight, fontSize = 13.sp)
    }
}

// ─── آمار سریع ───────────────────────────────────────────
@Composable
fun CkStatsRow(recipe: com.mnfarzaneh.solalrchef.model.Recipe) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CkStatCard(stringResource(R.string.recipe_total_time), recipe.totalTime, GlassColors.AccentOrange, Modifier.weight(1f))
        CkStatCard(stringResource(R.string.recipe_cook_time), recipe.cookTime, GlassColors.AccentBlue, Modifier.weight(1f))
    }
}

@Composable
fun CkStatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(GlassColors.GlassCard)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppText(value, color = GlassColors.TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        AppText(label, color = GlassColors.TextLight, fontSize = 10.sp)
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
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(GlassColors.GlassCard)
                    .clickable { onMinus() },
                contentAlignment = Alignment.Center
            ) {
                AppText(
                    "−",
                    color = GlassColors.TextDark,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            AppText(
                "$servings",
                color = GlassColors.TextDark,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 14.dp)
            )

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(GlassColors.AccentOrange)
                    .clickable { onPlus() },
                contentAlignment = Alignment.Center
            ) {
                AppText(
                    "+",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            AppText(
                stringResource(R.string.cooking_serving),
                color = GlassColors.TextDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            AppText("$calories", color = GlassColors.TextDark, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            AppText(stringResource(R.string.cooking_calories_per_serving), color = GlassColors.TextLight, fontSize = 11.sp)
        }
    }
}

// ─── فرمت مقدار به‌صورت کسری برای واحدهای حجمی ─────────────
// ← ۰.۶۶۷ پیمانه رو به‌جای «۰.۷» به‌صورت «⅔» نشون می‌ده.
// فقط برای واحدهای حجمی (پیمانه، قاشق و...) فعاله؛ برای وزنی/شمارشی
// (گرم، عدد) همون عدد اعشاری معمولی نشون داده می‌شه.
private val commonFractions = listOf(
    0.25f to "¼",
    0.333f to "⅓",
    0.5f to "½",
    0.667f to "⅔",
    0.75f to "¾"
)

private fun formatAsFraction(value: Float): String {
    val whole = value.toInt()
    val frac = value - whole

    if (frac < 0.05f) return whole.toString().ifBlank { "0" }

    val closest = commonFractions.minByOrNull { abs(it.first - frac) }
    val isCloseEnough = closest != null && abs(closest.first - frac) < 0.06f

    return when {
        !isCloseEnough -> String.format("%.1f", value)
        whole == 0 -> closest!!.second
        else -> "$whole${closest!!.second}"
    }
}

private fun formatIngredientAmount(rawAmount: String, unit: String, multiplier: Float): String {
    val value = rawAmount.toFloatOrNull()?.let { it * multiplier } ?: return rawAmount
    val isVolume = findUnit(unit)?.category == UnitCategory.VOLUME
    return if (isVolume) formatAsFraction(value) else {
        if (value == value.toLong().toFloat()) value.toLong().toString()
        else String.format("%.1f", value)
    }
}

// ─── مواد لازم ────────────────────────────────────────────
@Composable
fun CkIngredientsSection(ingredients: List<Ingredient>, servingsMultiplier: Float) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
        ingredients.forEach { ingredient ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                val adjusted = formatIngredientAmount(
                    rawAmount = ingredient.amount,
                    unit = ingredient.unit,
                    multiplier = servingsMultiplier
                )

                AppText(
                    ingredient.name,
                    color = GlassColors.TextDark,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                AppText(
                    "$adjusted ${ingredient.unit}",
                    color = GlassColors.AccentOrange,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.width(110.dp),
                    textAlign = TextAlign.Start
                )
            }
            Divider(color = GlassColors.Divider, thickness = 0.5.dp)
        }
    }
}

// ─── طرز تهیه ─────────────────────────────────────────────
@Composable
fun CkStepsSection(
    steps: List<com.mnfarzaneh.solalrchef.model.CookingStep>,
    completedSteps: List<Boolean>,
    onStepToggle: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
        steps.forEachIndexed { index, step ->
            val bgColor by animateColorAsState(
                targetValue = if (completedSteps.getOrElse(index) { false })
                    GlassColors.AccentGreen.copy(alpha = 0.12f) else Color.Transparent,
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
                            if (completedSteps.getOrElse(index) { false }) GlassColors.AccentGreen
                            else GlassColors.GlassCard
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (completedSteps.getOrElse(index) { false }) {
                        Icon(Icons.Default.Check, null, tint = Color.White,
                            modifier = Modifier.size(16.dp))
                    } else {
                        AppText((index + 1).toString(), color = GlassColors.TextDark,
                            fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                AppText(
                    step.instruction,
                    color = if (completedSteps.getOrElse(index) { false }) GlassColors.TextLight else GlassColors.TextDark,
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
@Composable
fun CkEquipmentSection(
    equipment: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {

        equipment.forEach { item ->

            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                AppText(
                    text = "🔸",
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.width(8.dp))

                AppText(
                    text = item,
                    color = GlassColors.TextDark,
                    fontSize = 16.sp
                )
            }
        }
    }
}
