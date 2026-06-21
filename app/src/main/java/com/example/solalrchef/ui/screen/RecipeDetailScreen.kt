package com.example.solalrchef.ui.screen

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.solalrchef.model.Recipe
import com.example.solalrchef.ui.navigation.NavGraph
import com.example.solalrchef.viewmodel.RecipeDetailViewModel

// ─── رنگ‌ها ───────────────────────────────────────────────
private val BgLight      = Color(0xFFF5EFE6)
private val GlassWhite   = Color(0xCCFFFFFF)
private val GlassCard    = Color(0xAAFFFFFF)
private val AccentOrange = Color(0xFFFF6B35)
private val AccentGreen  = Color(0xFF4CAF50)
private val AccentBlue   = Color(0xFF4A90D9)
private val TextDark     = Color(0xFF2C1810)
private val TextMid      = Color(0xFF6B4C3B)
private val TextLight    = Color(0xFF9E7B6A)
private val DivGlass     = Color(0x33000000)

@Composable
fun RecipeDetailScreen(
    recipeId: String,
    navController: NavController
) {
    val context = LocalContext.current

    // ── ViewModel جای مستقیم‌خوانی از Repository ─────────
    val viewModel: RecipeDetailViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(recipeId) {
        viewModel.loadRecipe(recipeId)
    }

    // اگه پیدا نشد برگرد
    LaunchedEffect(uiState.notFound) {
        if (uiState.notFound) navController.popBackStack()
    }

    // Loading
    if (uiState.isLoading || uiState.recipe == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AccentOrange)
        }
        return
    }

    val recipe = uiState.recipe!!
    val scrollState = rememberScrollState()
    val heroHeight = 320
    val scrolledPastHero by remember {
        derivedStateOf { scrollState.value > heroHeight * 2 }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
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
                    .background(BgLight)
            ) {
                Column {
                    GlassTitleSection(recipe = recipe)
                    GlassCalorieCard(recipe = recipe)
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassMacroRow(recipe = recipe)
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = DivGlass, thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 20.dp))

                    // ── Action Row با isFavorite از ViewModel ─
                    GlassActionRow(
                        navController   = navController,
                        recipe          = recipe,
                        isFavorite      = uiState.isFavorite,
                        onFavoriteClick = { viewModel.toggleFavorite() }
                    )

                    Divider(color = DivGlass, thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 20.dp))
                    GlassStoryCard(recipe = recipe)
                    Text(
                        text = recipe.description,
                        color = TextMid,
                        fontSize = 15.sp,
                        lineHeight = 24.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        GlassTopBar(
            navController    = navController,
            scrolledPastHero = scrolledPastHero,
            title            = recipe.title
        )

        // ── ماشین حساب (اگه داری) ────────────────────────
        if (uiState.showCalculator) {
            IngredientCalculatorSheet(
                ingredients = recipe.ingredients,
                baseYield   = recipe.yield.toIntOrNull() ?: 4,
                onDismiss   = { viewModel.toggleCalculator() }
            )
        }
    }
}

// ─── Hero ─────────────────────────────────────────────────
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
                Text("🍽️", fontSize = 64.sp)
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
                            1.0f to BgLight
                        )
                    )
                )
        )
    }
}

// ─── TopBar ───────────────────────────────────────────────
@Composable
fun GlassTopBar(
    navController: NavController,
    scrolledPastHero: Boolean,
    title: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 52.dp, start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        GlassIconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back",
                tint = TextDark, modifier = Modifier.size(20.dp))
        }
        if (scrolledPastHero) {
            Text(
                text = title,
                color = TextDark, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
        GlassIconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.Default.Close, contentDescription = "Close",
                tint = TextDark, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun GlassIconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.size(40.dp).clip(CircleShape)
            .background(GlassWhite).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) { content() }
}

// ─── عنوان ────────────────────────────────────────────────
@Composable
fun GlassTitleSection(recipe: Recipe) {
    Column(modifier = Modifier.fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text(recipe.title, color = TextDark, fontSize = 28.sp,
            fontWeight = FontWeight.Bold, lineHeight = 34.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text("${recipe.source} • ${recipe.author}",
            color = TextLight, fontSize = 13.sp)
    }
}

// ─── کالری ────────────────────────────────────────────────
@Composable
fun GlassCalorieCard(recipe: Recipe) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp)).background(GlassCard)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Total ${recipe.calories} kcal", color = TextDark,
            fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape)
                .background(AccentOrange.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) { Text("🔥", fontSize = 16.sp) }
    }
}

// ─── ماکرو ────────────────────────────────────────────────
@Composable
fun GlassMacroRow(recipe: Recipe) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        GlassMacroCard("زمان کل",    recipe.totalTime, AccentOrange, Modifier.weight(1f))
        GlassMacroCard("زمان پخت",   recipe.cookTime,  AccentBlue,   Modifier.weight(1f))
        GlassMacroCard("تعداد خروجی", recipe.yield,    AccentGreen,  Modifier.weight(1f))
    }
}

@Composable
fun GlassMacroCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(14.dp))
            .background(GlassCard).padding(12.dp)
    ) {
        Text(label, color = TextLight, fontSize = 10.sp, letterSpacing = 0.3.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, color = TextDark, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().height(4.dp)
            .clip(RoundedCornerShape(2.dp)).background(color.copy(alpha = 0.2f))) {
            Box(modifier = Modifier.fillMaxWidth(0.7f).height(4.dp)
                .clip(RoundedCornerShape(2.dp)).background(color))
        }
    }
}

// ─── Action Row — isFavorite و onFavoriteClick اضافه شد ──
@Composable
fun GlassActionRow(
    navController: NavController,
    recipe: Recipe,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        GlassActionButton(
            icon      = Icons.Default.MenuBook,
            label     = "طرز تهیه",
            modifier  = Modifier.weight(1f),
            isPrimary = true,
            onClick   = { navController.navigate(NavGraph.Screen.Cooking.createRoute(recipe.id)) }
        )
        // دکمه علاقه‌مندی با state واقعی
        GlassActionButton(
            icon      = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            label     = "مورد علاقه",
            modifier  = Modifier.weight(1f),
            isPrimary = false,
            tint      = if (isFavorite) Color(0xFFE53935) else TextMid,
            onClick   = onFavoriteClick
        )
        GlassActionButton(
            icon    = Icons.Default.Share,
            label   = "اشتراک‌گذاری",
            modifier = Modifier.weight(1f),
            isPrimary = false,
            onClick = {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "${recipe.title}\n${recipe.description}")
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "اشتراک‌گذاری دستور پخت"))
            }
        )
    }
}

@Composable
fun GlassActionButton(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
    tint: Color = TextMid,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier.height(58.dp).clip(RoundedCornerShape(14.dp))
            .background(
                if (isPrimary)
                    Brush.linearGradient(listOf(Color(0xFFFF6B35), Color(0xFFFF8C42)))
                else
                    Brush.linearGradient(listOf(GlassCard, GlassCard))
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = label,
                tint = if (isPrimary) Color.White else tint,
                modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(3.dp))
            Text(label, color = if (isPrimary) Color.White else TextMid,
                fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// ─── کارت مواد لازم ───────────────────────────────────────
@Composable
fun GlassStoryCard(recipe: Recipe) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(16.dp)).background(GlassCard).clickable { }
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
                ) { Text("🍽️", fontSize = 24.sp) }
            }

            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("مواد لازم", color = AccentOrange, fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(recipe.title, color = TextDark, fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold, maxLines = 2,
                    overflow = TextOverflow.Ellipsis)
            }
            Icon(Icons.Default.ChevronLeft, contentDescription = null,
                tint = TextLight, modifier = Modifier.size(20.dp))
        }
    }
}