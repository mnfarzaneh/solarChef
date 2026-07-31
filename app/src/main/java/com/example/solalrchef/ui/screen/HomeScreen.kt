package com.mnfarzaneh.solalrchef.ui.screen

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.mnfarzaneh.solalrchef.model.Recipe
import com.mnfarzaneh.solalrchef.ui.navigation.NavGraph
import com.mnfarzaneh.solalrchef.ui.theme.AppText
import com.mnfarzaneh.solalrchef.ui.theme.GlassColors
import com.mnfarzaneh.solalrchef.viewmodel.HomeViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

private sealed class BottomTab(val route: String, val label: String, val icon: ImageVector) {
    data object AllRecipes : BottomTab("home_all", "خانه", Icons.Default.Home)
    data object Favorites : BottomTab("home_favorites", "علاقه‌مندی‌ها", Icons.Default.Favorite)
    data object MyRecipes : BottomTab("home_my_recipes", "دستورهای من", Icons.Default.Person)
}

// همون گرادیان نارنجی-قهوه‌ای که روی HomeRecipeCard استفاده شده — حالا مرکزی شد
// تا هم روی کارت‌ها هم روی نویگیشن‌بار یکسان اعمال بشه
private val WarmGlassGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFFFB74D).copy(alpha = 0.30f),
        Color(0xFFFFCC80).copy(alpha = 0.20f),
        Color(0xFFFFF3E0).copy(alpha = 0.25f)
    ),
    start = Offset(0f, 0f),
    end = Offset(500f, 500f)
)

// پس‌زمینه‌ی گرادیانی — چون رنگ یکدست بلور نمی‌خوره، اینجا یه گرادیان ملایم می‌ذاریم
// که بلور واقعاً قابل دیدنه (شبیه پس‌زمینه‌ی رنگی عکس رفرنس)
private val HomeBackgroundGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFE9D6),
        Color(0xFFFFF3E9),
        Color(0xFFFDEFEA),
        GlassColors.BgLight
    )
)

private val NavBarHeight = 66.dp
private val NavBarVerticalMargin = 14.dp

@Composable
fun HomeScreen(rootNavController: NavController) {
    val innerNavController = rememberNavController()
    val hazeState = rememberHazeState()

    val currentBackStack by innerNavController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    Box(modifier = Modifier.fillMaxSize()) {

        // ── لایه‌ی پس‌زمینه: این چیزیه که پشت نویگیشن‌بار و کارت‌ها بلور می‌شه ──
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HomeBackgroundGradient)
        )

        // ── محتوای هر تب. hazeSource روش می‌ذاریم تا هرچی پشت نویگیشن‌بار
        // رد می‌شه (لیست دستورها هنگام اسکرول) واقعاً بلور بشه ──
        NavHost(
            navController = innerNavController,
            startDestination = BottomTab.AllRecipes.route,
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState)
        ) {
            composable(BottomTab.AllRecipes.route) {
                AllRecipesTab(rootNavController = rootNavController)
            }
            composable(BottomTab.Favorites.route) {
                FavoritesTab(rootNavController = rootNavController)
            }
            composable(BottomTab.MyRecipes.route) {
                MyRecipesScreen(navController = rootNavController)
            }
        }

        // ── لایه‌ی محوشونده: قبل از رسیدن محتوا به نویگیشن‌بار،
        // به‌آرومی محو می‌شه به‌جای این‌که یهو زیر شیشه قطع بشه ──
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            GlassColors.BgLight.copy(alpha = 2f),
                            GlassColors.BgLight.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // ── نویگیشن‌بار شیشه‌ای شناور با دکمه‌ی وسط برجسته ──
        GlassBottomNavBar(
            currentRoute = currentRoute,
            hazeState = hazeState,
            onHomeClick = {
                innerNavController.navigate(BottomTab.AllRecipes.route) {
                    popUpTo(innerNavController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            onFavoritesClick = {
                innerNavController.navigate(BottomTab.Favorites.route) {
                    popUpTo(innerNavController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            onMyRecipesClick = {
                innerNavController.navigate(BottomTab.MyRecipes.route) {
                    popUpTo(innerNavController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            onAddClick = {
                rootNavController.navigate(NavGraph.Screen.AddRecipe.route)
            },
            onAiExtractClick = {
                // فعلاً همون صفحه‌ی افزودن دستور رو باز می‌کنه (بالاش دکمه‌ی استخراج هوشمند هست)
                rootNavController.navigate(NavGraph.Screen.AddRecipe.route)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )
    }
}

// ─── نویگیشن‌بار شیشه‌ای با ۵ آیتم + دکمه‌ی وسط برجسته ─────
@Composable
private fun GlassBottomNavBar(
    currentRoute: String?,
    hazeState: HazeState,
    onHomeClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onAddClick: () -> Unit,
    onAiExtractClick: () -> Unit,
    onMyRecipesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = NavBarVerticalMargin)
    ) {
        // ── خود نوار شیشه‌ای — با همون گرادیان کارت‌ها ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(NavBarHeight)
                .clip(RoundedCornerShape(28.dp))
                .background(WarmGlassGradient)
                .hazeEffect(state = hazeState) {
                    blurRadius = 25.dp
                    tints = listOf(
                        HazeTint(Color.White.copy(alpha = 0.18f)),
                        HazeTint(Color(0xFFFFA726).copy(alpha = 0.35f))
                    )
                    noiseFactor = 0.06f
                }
                .border(1.2.dp, Color.White.copy(alpha = 0.65f), RoundedCornerShape(28.dp)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavBarItem(
                icon = Icons.Default.Home,
                label = "خانه",
                selected = currentRoute == BottomTab.AllRecipes.route,
                onClick = onHomeClick,
                modifier = Modifier.weight(1f)
            )
            NavBarItem(
                icon = Icons.Default.Favorite,
                label = "علاقه‌مندی‌ها",
                selected = currentRoute == BottomTab.Favorites.route,
                onClick = onFavoritesClick,
                modifier = Modifier.weight(1f)
            )
            // ── جای خالی وسط برای دکمه‌ی شناور ──
            Spacer(modifier = Modifier.weight(1f))
            NavBarItem(
                icon = Icons.Default.AutoAwesome,
                label = "استخراج",
                selected = false,
                onClick = onAiExtractClick,
                modifier = Modifier.weight(1f)
            )
            NavBarItem(
                icon = Icons.Default.Person,
                label = "دستورهای من",
                selected = currentRoute == BottomTab.MyRecipes.route,
                onClick = onMyRecipesClick,
                modifier = Modifier.weight(1f)
            )
        }

        // ── دکمه‌ی رنگی و برجسته‌ی وسط (Add) — بالاتر از نوار قرار می‌گیره ──
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-8).dp)
                .size(60.dp)
                .graphicsLayer {
                    shadowElevation = 18f
                    shape = CircleShape
                    clip = true
                    ambientShadowColor = GlassColors.AccentOrange
                    spotShadowColor = GlassColors.AccentOrange
                }
                .background(
                    Brush.linearGradient(
                        listOf(GlassColors.AccentOrange, Color(0xFFFF8C42))
                    )
                )
                .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                .clickable { onAddClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "افزودن دستور جدید",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun NavBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 6.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (selected) GlassColors.AccentOrange else GlassColors.TextLight,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.height(3.dp))
        AppText(
            text = label,
            fontSize = 9.sp,
            color = if (selected) GlassColors.AccentOrange else GlassColors.TextLight,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AllRecipesTab(rootNavController: NavController) {
    val viewModel: HomeViewModel = hiltViewModel()
    val recipes by viewModel.allRecipes.collectAsState()

    HomeRecipeList(
        recipes = recipes,
        emptyMessage = "هنوز دستوری وجود ندارد",
        rootNavController = rootNavController,
        title = "همه دستورها 🍽️"
    )
}

@Composable
fun FavoritesTab(rootNavController: NavController) {
    val viewModel: HomeViewModel = hiltViewModel()
    val recipes by viewModel.favoriteRecipes.collectAsState()

    HomeRecipeList(
        recipes = recipes,
        emptyMessage = "هنوز چیزی به علاقه‌مندی‌ها اضافه نکردید",
        rootNavController = rootNavController,
        title = "دستورهای محبوب ❤️"
    )
}

@Composable
fun HomeListHeader(title: String) {

    val statusBarHeight =
        WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = statusBarHeight + 8.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        AppText(
            text = title,
            color = GlassColors.TextDark,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun HomeRecipeList(
    recipes: List<Recipe>,
    emptyMessage: String,
    rootNavController: NavController,
    title: String
) {
    // فاصله‌ی پایین به اندازه‌ی ارتفاع نویگیشن‌بار شناور + حاشیه‌هاش، تا آخرین
    // آیتم لیست زیر نویگیشن‌بار گم نشه ولی همچنان موقع اسکرول از پشتش رد بشه و بلور بخوره
    val bottomInset = NavBarHeight + NavBarVerticalMargin * 2

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        HomeListHeader(
            title = title
        )

        if (recipes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AppText("🍽️", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    AppText(emptyMessage, color = GlassColors.TextLight)
                }
            }
        } else {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = bottomInset
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                items(recipes, key = { it.id }) { recipe ->

                    HomeRecipeCard(
                        recipe = recipe,
                        onClick = {
                            rootNavController.navigate(
                                NavGraph.Screen.RecipeDetail.createRoute(recipe.id)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeRecipeCard(recipe: Recipe, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(WarmGlassGradient)
            .border(
                1.dp,
                Color.White.copy(alpha = 0.35f),
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(GlassColors.Divider),
            contentAlignment = Alignment.Center
        ) {
            if (recipe.imagePath.isNotEmpty()) {
                AsyncImage(
                    model = Uri.parse(recipe.imagePath),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (recipe.image != 0) {
                Image(
                    painter = painterResource(recipe.image),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                AppText("🍽️", fontSize = 24.sp)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            AppText(
                recipe.title, color = GlassColors.TextDark, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            AppText(recipe.author, fontSize = 12.sp, color = GlassColors.TextLight)
        }
        if (recipe.isFavorite) {
            Icon(
                Icons.Filled.Favorite,
                contentDescription = null,
                tint = Color(0xFFE53935),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
