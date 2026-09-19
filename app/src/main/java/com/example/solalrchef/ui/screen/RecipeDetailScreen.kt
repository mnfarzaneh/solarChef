package com.mnfarzaneh.solalrchef.ui.screen

import android.content.Intent
import android.content.Context
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mnfarzaneh.solalrchef.R
import com.mnfarzaneh.solalrchef.model.Recipe
import com.mnfarzaneh.solalrchef.ui.components.IngredientCalculatorSheet
import com.mnfarzaneh.solalrchef.ui.components.IngredientCalculatorButton
import com.mnfarzaneh.solalrchef.ui.navigation.NavGraph
import com.mnfarzaneh.solalrchef.ui.theme.AppText
import com.mnfarzaneh.solalrchef.ui.theme.GlassColors
import com.mnfarzaneh.solalrchef.viewmodel.RecipeDetailViewModel
import com.mnfarzaneh.solalrchef.ui.theme.GlassIconButton
import com.mnfarzaneh.solalrchef.ui.theme.GlassActionButton
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
fun RecipeDetailScreen(
    recipeId: String,
    navController: NavController
) {
    val context = LocalContext.current
    val hazeState = remember { HazeState() }   // ← اضافه شد

    val viewModel: RecipeDetailViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(recipeId) {
        viewModel.loadRecipe(recipeId)
    }

    LaunchedEffect(uiState.notFound) {
        if (uiState.notFound) navController.popBackStack()
    }

    if (uiState.isLoading || uiState.recipe == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = GlassColors.AccentOrange)
        }
        return
    }

    val recipe = uiState.recipe!!
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val titleRevealThreshold = remember(density) {
        with(density) { 285.dp.roundToPx() }
    }
    val scrolledPastHero by remember {
        derivedStateOf { scrollState.value >= titleRevealThreshold }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassColors.BgLight)
            .hazeSource(state = hazeState)   // ← این خط اضافه شد: هرچی داخل این Box هست، منبع بلوره

    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            GlassHeroSection(recipe = recipe, scrollValue = scrollState.value)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-28).dp)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(GlassColors.BgLight)
            ) {
                Column {
                    GlassTitleSection(recipe = recipe)
                    if (recipe.calories > 0) {
                        GlassCalorieCard(recipe = recipe)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    GlassMacroRow(recipe = recipe)
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = GlassColors.Divider, thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 20.dp))

                    GlassActionRow(
                        navController   = navController,
                        recipe          = recipe,
                        isFavorite      = uiState.isFavorite,
                        onFavoriteClick = { viewModel.toggleFavorite() }
                    )

                    Divider(color = GlassColors.Divider, thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 20.dp))
                    GlassStoryCard(
                        recipe = recipe,
                        onClick = { viewModel.showShoppingList()}
                    )
                    if (recipe.description.isNotBlank()) {
                        RecipeDetailSection(title = stringResource(R.string.recipe_description)) {
                            AppText(
                                text = recipe.description,
                                color = GlassColors.TextMid,
                                fontSize = 15.sp,
                                lineHeight = 24.sp
                            )
                        }
                    }
                    if (recipe.ingredients.isNotEmpty()) {
                        RecipeDetailSection(title = stringResource(R.string.recipe_ingredients)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IngredientCalculatorButton(onClick = { viewModel.toggleCalculator() })
                            }
                            recipe.ingredients.forEachIndexed { index, ingredient ->
                                RecipeDetailListRow(
                                    leading = ingredient.name,
                                    trailing = listOf(ingredient.amount, ingredient.unit)
                                        .filter { it.isNotBlank() }
                                        .joinToString(" ")
                                )
                                if (index < recipe.ingredients.lastIndex) {
                                    Divider(color = GlassColors.Divider, thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                    if (recipe.equipment.isNotEmpty()) {
                        RecipeDetailSection(title = stringResource(R.string.recipe_equipment)) {
                            recipe.equipment.forEach { item ->
                                AppText(
                                    text = "• $item",
                                    color = GlassColors.TextDark,
                                    fontSize = 15.sp,
                                    lineHeight = 24.sp,
                                    modifier = Modifier.padding(vertical = 5.dp)
                                )
                            }
                        }
                    }
                    if (recipe.steps.isNotEmpty()) {
                        RecipeDetailSection(title = stringResource(R.string.recipe_steps)) {
                            recipe.steps.forEachIndexed { index, step ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 9.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(GlassColors.AccentOrange.copy(alpha = 0.14f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AppText(
                                            text = (index + 1).toString(),
                                            color = GlassColors.AccentOrange,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    AppText(
                                        text = step.instruction,
                                        color = GlassColors.TextDark,
                                        fontSize = 15.sp,
                                        lineHeight = 24.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        GlassTopBar(
            navController    = navController,
            scrolledPastHero = scrolledPastHero,
            title            = recipe.title,
            hazeState        = hazeState   // ← این پارامتر جدید رو پاس بده

        )

        if (uiState.showCalculator) {
            IngredientCalculatorSheet(
                ingredients = recipe.ingredients,
                baseYield   = recipe.yield.toIntOrNull() ?: 1,
                onDismiss   = { viewModel.toggleCalculator() }
            )
        }
        if (uiState.showShoppingList) {
            ShoppingListSheet(
                recipe = recipe,
                onDismiss = {
                    viewModel.hideShoppingList()
                }
            )
        }
    }
}

@Composable
fun GlassHeroSection(recipe: Recipe, scrollValue: Int) {
    val imageHeight = 320.dp
    val density = LocalDensity.current
    val parallaxOffset = with(density) { (scrollValue * 0.3f).toDp() }
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(imageHeight)
    ) {
        if (recipe.imagePath.isNotEmpty()) {
            AsyncImage(
                model = Uri.parse(recipe.imagePath),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight + 80.dp)
                    .offset(y = -parallaxOffset)
            )
        } else if (recipe.image != 0) {
            Image(
                painter = painterResource(recipe.image),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight + 80.dp)
                    .offset(y = -parallaxOffset)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight + 80.dp)
                    .background(Color(0xFFE0C9B0)),
                contentAlignment = Alignment.Center
            ) {
                AppText("🍽️", fontSize = 64.sp)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(statusBarHeight + 40.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.35f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.72f to Color.Transparent,
                            1.0f to GlassColors.BgLight
                        )
                    )
                )
        )
    }
}

@Composable
fun GlassTopBar(
    navController: NavController,
    scrolledPastHero: Boolean,
    title: String,
    hazeState: HazeState   // ← پارامتر جدید
) {
    val barBackground by animateColorAsState(
        targetValue = if (scrolledPastHero) GlassColors.BgLight.copy(alpha = 0.98f) else Color.Transparent,
        animationSpec = tween(durationMillis = 180),
        label = "recipeTopBarBackground"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(barBackground)
            .statusBarsPadding()
            .height(64.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
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
        AnimatedVisibility(
            visible = scrolledPastHero,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(120)),
            modifier = Modifier.weight(1f)
        ) {
            AppText(
                text = title,
                color = GlassColors.TextDark, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
            )
        }
        if (!scrolledPastHero) Spacer(modifier = Modifier.weight(1f))
    }
}



@Composable
fun GlassTitleSection(recipe: Recipe) {
    Column(modifier = Modifier.fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 16.dp)) {
        AppText(recipe.title, color = GlassColors.TextDark, fontSize = 28.sp,
            fontWeight = FontWeight.Bold, lineHeight = 34.sp)
        Spacer(modifier = Modifier.height(4.dp))
        AppText("${recipe.source} • ${recipe.author}",
            color = GlassColors.TextLight, fontSize = 13.sp)
    }
}

@Composable
fun GlassCalorieCard(recipe: Recipe) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp)).background(GlassColors.GlassCard)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AppText(stringResource(R.string.recipe_kilocalories, recipe.calories), color = GlassColors.TextDark,
            fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape)
                .background(GlassColors.AccentOrange.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) { AppText("🔥", fontSize = 16.sp) }
    }
}

@Composable
fun GlassMacroRow(recipe: Recipe) {
    val values = buildList {
        if (recipe.totalTime.isNotBlank()) add(Triple(R.string.recipe_total_time, recipe.totalTime, GlassColors.AccentOrange))
        if (recipe.cookTime.isNotBlank()) add(Triple(R.string.recipe_cook_time, recipe.cookTime, GlassColors.AccentBlue))
        if (recipe.yield.isNotBlank()) add(Triple(R.string.recipe_output_count, recipe.yield, GlassColors.AccentGreen))
    }
    if (values.isEmpty()) return
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        values.forEach { (label, value, color) ->
            GlassMacroCard(stringResource(label), value, color, Modifier.weight(1f))
        }
    }
}

@Composable
fun GlassMacroCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(14.dp))
            .background(GlassColors.GlassCard).padding(12.dp)
    ) {
        AppText(label, color = GlassColors.TextLight, fontSize = 10.sp, letterSpacing = 0.3.sp)
        Spacer(modifier = Modifier.height(4.dp))
        AppText(value, color = GlassColors.TextDark, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RecipeDetailSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(GlassColors.GlassCard)
            .padding(16.dp)
    ) {
        AppText(
            text = title,
            color = GlassColors.AccentOrange,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun RecipeDetailListRow(leading: String, trailing: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppText(leading, color = GlassColors.TextDark, fontSize = 15.sp, modifier = Modifier.weight(1f))
        if (trailing.isNotBlank()) {
            AppText(trailing, color = GlassColors.AccentOrange, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun buildRecipeShareText(context: Context, recipe: Recipe): String {
    val ingredients = recipe.ingredients.joinToString("\n") {
        "• ${it.amount} ${it.unit} ${it.name}"
    }

    val steps = recipe.steps.mapIndexed { index, step ->
        "${index + 1}. ${step.instruction}"
    }.joinToString("\n")

    return """
🍽 ${recipe.title}

${context.getString(R.string.share_description_heading)}
${recipe.description}

${context.getString(R.string.share_total_time, recipe.totalTime)}
${context.getString(R.string.share_cook_time, recipe.cookTime)}
${context.getString(R.string.share_servings, recipe.yield)}

${context.getString(R.string.share_ingredients_heading)}
$ingredients

${context.getString(R.string.share_method_heading)}
$steps

${context.getString(R.string.share_sent_from_app)}

${context.getString(R.string.share_more_tutorials)}
${context.getString(R.string.share_instagram)}
https://www.instagram.com/diy.by.farzaneh?utm_source=qr

    """.trimIndent()
}

@Composable
fun GlassActionRow(
    navController: NavController,
    recipe: Recipe,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        GlassActionButton(
            icon      = Icons.Default.MenuBook,
            label     = stringResource(R.string.recipe_start_cooking),
            modifier  = Modifier.fillMaxWidth(),
            isPrimary = true,
            tint      = GlassColors.AccentOrange,
            onClick   = { navController.navigate(NavGraph.Screen.Cooking.createRoute(recipe.id)) }
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GlassActionButton(
                icon      = if (isFavorite) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                label     = stringResource(if (isFavorite) R.string.recipe_favorite else R.string.recipe_add_favorite),
                modifier  = Modifier.weight(1f),
                isPrimary = false,
                tint      = if (isFavorite) Color(0xFFE53935) else GlassColors.TextMid,
                onClick   = onFavoriteClick
            )
            GlassActionButton(
                icon    = Icons.Default.Share,
                label   = stringResource(R.string.action_share),
                modifier = Modifier.weight(1f),
                isPrimary = false,
                onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, buildRecipeShareText(context, recipe))
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, context.getString(R.string.recipe_share_chooser)))
                }
            )
        }
    }
}



@Composable
fun GlassStoryCard(
    recipe: Recipe,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(16.dp)).background(GlassColors.GlassCard).clickable {onClick()  }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (recipe.imagePath.isNotEmpty()) {
                AsyncImage(
                    model = Uri.parse(recipe.imagePath), contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(52.dp).clip(RoundedCornerShape(12.dp))
                )
            } else if (recipe.image != 0) {
                Image(
                    painter = painterResource(recipe.image), contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(52.dp).clip(RoundedCornerShape(12.dp))
                )
            } else {
                Box(
                    modifier = Modifier.size(52.dp).clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE0C9B0)),
                    contentAlignment = Alignment.Center
                ) { AppText("🍽️", fontSize = 24.sp) }
            }

            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                AppText(stringResource(R.string.shopping_list), color = GlassColors.AccentOrange, fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
                Spacer(modifier = Modifier.height(2.dp))
                AppText(recipe.title, color = GlassColors.TextDark, fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold, maxLines = 2,
                    overflow = TextOverflow.Ellipsis)
            }
            Icon(Icons.Default.ChevronLeft, contentDescription = null,
                tint = GlassColors.TextLight, modifier = Modifier.size(20.dp))
        }
    }
}

private fun buildShoppingListText(
    context: Context,
    recipe: Recipe,
    selectedIngredients: List<String>,
    selectedEquipment: List<String>
): String {

    return buildString {

        appendLine(context.getString(R.string.shopping_list_share_title))
        appendLine()
        appendLine(recipe.title)
        appendLine()

        if (selectedIngredients.isNotEmpty()) {

            appendLine(context.getString(R.string.shopping_ingredients_heading))

            selectedIngredients.forEach {
                appendLine("• $it")
            }

            appendLine()
        }

        if (selectedEquipment.isNotEmpty()) {

            appendLine(context.getString(R.string.shopping_equipment_heading))

            selectedEquipment.forEach {
                appendLine("• $it")
            }
        }

        appendLine()
        appendLine(context.getString(R.string.shopping_sent_from))
        appendLine(context.getString(R.string.shopping_more_tutorials))
        appendLine("https://www.instagram.com/diy.by.farzaneh?utm_source=qr")    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListSheet(
    recipe: Recipe,
    onDismiss: () -> Unit
) {

    val context = LocalContext.current

    val clipboardManager =
        LocalClipboardManager.current

    val selectedIngredients = remember {
        mutableStateMapOf<String, Boolean>()
    }

    val selectedEquipment = remember {
        mutableStateMapOf<String, Boolean>()
    }

    val selectedIngredientNames =
        selectedIngredients
            .filterValues { it }
            .keys
            .toList()

    val selectedEquipmentNames =
        selectedEquipment
            .filterValues { it }
            .keys
            .toList()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = GlassColors.BgLight
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            AppText(
                text = stringResource(R.string.shopping_list),
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppText(
                text = stringResource(R.string.shopping_list_hint)
            )

            Spacer(modifier = Modifier.height(8.dp))

            recipe.ingredients.forEach { ingredient ->

                val checked =
                    selectedIngredients[ingredient.name] ?: false

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedIngredients[ingredient.name] =
                                !checked
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                if (checked)
                                    GlassColors.AccentOrange.copy(alpha = 0.15f)
                                else
                                    GlassColors.GlassWhite
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (checked) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = GlassColors.AccentOrange,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    AppText(
                        text = ingredient.name
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            AppText(
                text = stringResource(R.string.shopping_equipment)
            )

            Spacer(modifier = Modifier.height(8.dp))

            recipe.equipment.forEach { equipment ->

                val checked =
                    selectedEquipment[equipment] ?: false

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedEquipment[equipment] =
                                !checked
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                if (checked)
                                    GlassColors.AccentOrange.copy(alpha = 0.15f)
                                else
                                    GlassColors.GlassWhite
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (checked) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = GlassColors.AccentOrange,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.width(16.dp)
                    )

                    AppText(
                        text = equipment,
                        color = GlassColors.TextDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                GlassActionButton(
                    icon = Icons.Default.ContentCopy,
                    label = stringResource(R.string.action_copy),
                    modifier = Modifier.weight(1f),
                    onClick = {

                        val text = buildShoppingListText(
                            context = context,
                            recipe = recipe,
                            selectedIngredients = selectedIngredientNames,
                            selectedEquipment = selectedEquipmentNames
                        )

                        clipboardManager.setText(
                            AnnotatedString(text)
                        )
                    }
                )

                GlassActionButton(
                    icon = Icons.Default.Share,
                    label = stringResource(R.string.shopping_share),
                    modifier = Modifier.weight(1f),
                    isPrimary = true,
                    onClick = {

                        val text = buildShoppingListText(
                            context = context,
                            recipe = recipe,
                            selectedIngredients = selectedIngredientNames,
                            selectedEquipment = selectedEquipmentNames
                        )

                        val intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, text)
                        }

                        context.startActivity(
                            Intent.createChooser(
                                intent,
                                context.getString(R.string.shopping_share_chooser)
                            )
                        )
                    }
                )
            }


        }
    }
}
