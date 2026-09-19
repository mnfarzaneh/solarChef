package com.mnfarzaneh.solalrchef.ui.screen

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.Switch
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.platform.LocalContext
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
import com.mnfarzaneh.solalrchef.R
import androidx.compose.ui.res.stringResource
import com.mnfarzaneh.solalrchef.data.remote.dto.HeroArticleDto
import com.mnfarzaneh.solalrchef.model.Category
import com.mnfarzaneh.solalrchef.model.Recipe
import com.mnfarzaneh.solalrchef.ui.navigation.NavGraph
import com.mnfarzaneh.solalrchef.ui.theme.AppText
import com.mnfarzaneh.solalrchef.ui.theme.GlassColors
import com.mnfarzaneh.solalrchef.ui.theme.AppAccent
import android.content.Context
import com.mnfarzaneh.solalrchef.viewmodel.CategoryViewModel
import com.mnfarzaneh.solalrchef.viewmodel.CategoryWithCount
import com.mnfarzaneh.solalrchef.viewmodel.HomeViewModel
import com.mnfarzaneh.solalrchef.viewmodel.MyRecipesViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private sealed class BottomTab(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object AllRecipes :
        BottomTab("home_all", "خانه", Icons.Default.Home)

    data object Favorites :
        BottomTab("home_favorites", "علاقه‌مندی‌ها", Icons.Default.Favorite)

    data object MyRecipes :
        BottomTab("home_my_recipes", "دستورهای من", Icons.Default.MenuBook)


    data object Profile :
        BottomTab("home_profile", "پروفایل", Icons.Default.Person)
}



// همون گرادیان نارنجی-قهوه‌ای که روی HomeRecipeCard استفاده شده — حالا مرکزی شد
// تا هم روی کارت‌ها هم روی نویگیشن‌بار یکسان اعمال بشه
val WarmGlassGradient: Brush
    get() = Brush.linearGradient(
        colors = if (GlassColors.darkMode) {
            listOf(
                GlassColors.GlassWhite,
                GlassColors.AccentSecondary.copy(alpha = 0.48f),
                GlassColors.AccentOrange.copy(alpha = 0.36f)
            )
        } else {
            listOf(
                Color.White.copy(alpha = 0.94f),
                GlassColors.AccentSecondary.copy(alpha = 0.62f),
                GlassColors.AccentOrange.copy(alpha = 0.48f)
            )
        },
        start = Offset.Zero,
        end = Offset(500f, 500f)
    )

private val HomeBackgroundGradient: Brush
    get() = Brush.verticalGradient(
        colors = if (GlassColors.darkMode) {
            listOf(Color(0xFF1D1815), GlassColors.BgLight, Color(0xFF110E0C))
        } else {
            listOf(Color(0xFFFFFBF8), Color(0xFFFFF6F0), Color(0xFFF8F1ED))
        }
    )

private val NavBarHeight = 66.dp
private val NavBarVerticalMargin = 14.dp

@Composable
fun HomeScreen(
    rootNavController: NavController,
    revealContent: Boolean = true
) {
    val innerNavController = rememberNavController()
    val hazeState = rememberHazeState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showGuide by rememberSaveable { mutableStateOf(false) }

    val currentBackStack by innerNavController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route
    val bottomSystemInset = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()
    val navigationReservedSpace = NavBarHeight + (NavBarVerticalMargin * 2) + bottomSystemInset

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            SolarChefDrawer(
                onArticlesClick = {
                    scope.launch { drawerState.close() }
                    rootNavController.navigate(NavGraph.Screen.Articles.route)
                },
                onGuideClick = {
                    scope.launch { drawerState.close() }
                    showGuide = true
                },
                onProfileClick = {
                    scope.launch { drawerState.close() }
                    innerNavController.navigate(BottomTab.Profile.route) { launchSingleTop = true }
                }
            )
        }
    ) {
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
                .padding(bottom = navigationReservedSpace)
                .hazeSource(state = hazeState)
        ) {
            composable(BottomTab.AllRecipes.route) {
                AllRecipesTab(
                    rootNavController = rootNavController,
                    revealContent = revealContent,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onSeeAllRecipes = {
                        innerNavController.navigate(BottomTab.MyRecipes.route) {
                            popUpTo(innerNavController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(BottomTab.Favorites.route) {
                FavoritesTab(rootNavController = rootNavController)
            }
            composable(BottomTab.MyRecipes.route) {
                MyRecipesScreen(navController = rootNavController)
            }
            composable(BottomTab.Profile.route) {
                ProfileScreen(
                    onLoginClick = {
                        rootNavController.navigate(NavGraph.Screen.Login.route)
                    }
                )
            }
        }

        // ── لایه‌ی محوشونده: قبل از رسیدن محتوا به نویگیشن‌بار،
        // به‌آرومی محو می‌شه به‌جای این‌که یهو زیر شیشه قطع بشه ──
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(navigationReservedSpace + 18.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            GlassColors.BgLight.copy(alpha = 0.72f),
                            GlassColors.BgLight.copy(alpha = 0.94f)
                        )
                    )
                )
        )

        // ── نویگیشن‌بار شیشه‌ای شناور با دکمه‌ی وسط برجسته ──
        GlassBottomNavBar(
            currentRoute = currentRoute,
            hazeState = hazeState,
            onHomeClick = {
                // خانه همیشه باید واقعاً به مقصد ریشه برگردد؛ بازیابی state قبلی
                // در بعضی دستگاه‌ها باعث ماندن روی Profile می‌شد.
                val returnedHome = innerNavController.popBackStack(
                    BottomTab.AllRecipes.route,
                    inclusive = false
                )
                if (!returnedHome && currentRoute != BottomTab.AllRecipes.route) {
                    innerNavController.navigate(BottomTab.AllRecipes.route) {
                        launchSingleTop = true
                    }
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
                rootNavController.navigate(
                    NavGraph.Screen.AddRecipe.createRoute()
                )
            },
            onProfileClick = {
                innerNavController.navigate(BottomTab.Profile.route) {
                    popUpTo(innerNavController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )
    }
    }

    if (showGuide) {
        FirstUseGuide(onFinished = { showGuide = false })
    }
}

@Composable
private fun SolarChefDrawer(
    onArticlesClick: () -> Unit,
    onGuideClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val context = LocalContext.current
    ModalDrawerSheet(
        drawerContainerColor = GlassColors.BgLight,
        drawerShape = RoundedCornerShape(topEnd = 0.dp, bottomEnd = 0.dp, topStart = 28.dp, bottomStart = 28.dp),
        modifier = Modifier.widthIn(max = 310.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 20.dp)
                .padding(horizontal = 16.dp)
        ) {
            AppText(
                "SolarChef",
                color = GlassColors.TextDark,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )
            AppText(
                stringResource(R.string.drawer_tagline),
                color = GlassColors.TextLight,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(28.dp))

            NavigationDrawerItem(
                label = { AppText(stringResource(R.string.drawer_articles), color = GlassColors.TextDark) },
                selected = false,
                icon = { Icon(Icons.Default.Description, null, tint = GlassColors.AccentOrange) },
                onClick = onArticlesClick
            )
            NavigationDrawerItem(
                label = { AppText(stringResource(R.string.drawer_guide), color = GlassColors.TextDark) },
                selected = false,
                icon = { Icon(Icons.Default.HelpOutline, null, tint = GlassColors.AccentOrange) },
                onClick = onGuideClick
            )
            NavigationDrawerItem(
                label = { AppText(stringResource(R.string.drawer_profile_backup), color = GlassColors.TextDark) },
                selected = false,
                icon = { Icon(Icons.Default.Person, null, tint = GlassColors.AccentOrange) },
                onClick = onProfileClick
            )
            Spacer(Modifier.height(18.dp))
            AppearancePicker(
                onAppearanceChanged = { accent, dark ->
                    GlassColors.setAppearance(accent = accent, isDark = dark)
                    context.getSharedPreferences("solarchef_appearance", Context.MODE_PRIVATE)
                        .edit()
                        .putString("accent", accent.storageKey)
                        .putBoolean("dark_mode", dark)
                        .apply()
                }
            )
        }
    }
}

@Composable
private fun AppearancePicker(
    onAppearanceChanged: (AppAccent, Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(GlassColors.GlassCard)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Palette, contentDescription = null, tint = GlassColors.AccentOrange)
            Spacer(Modifier.width(9.dp))
            AppText(
                stringResource(R.string.appearance_title),
                color = GlassColors.TextDark,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AppAccent.entries.forEach { accent ->
                val selected = accent == GlassColors.activeAccent
                Box(
                    modifier = Modifier
                        .size(if (selected) 42.dp else 38.dp)
                        .clip(CircleShape)
                        .background(accent.color)
                        .border(
                            width = if (selected) 3.dp else 1.dp,
                            color = if (selected) GlassColors.TextDark else Color.White.copy(alpha = 0.7f),
                            shape = CircleShape
                        )
                        .clickable { onAppearanceChanged(accent, GlassColors.darkMode) },
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.DarkMode, contentDescription = null, tint = GlassColors.TextMid)
            Spacer(Modifier.width(9.dp))
            AppText(
                stringResource(R.string.appearance_dark_mode),
                color = GlassColors.TextDark,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = GlassColors.darkMode,
                onCheckedChange = { onAppearanceChanged(GlassColors.activeAccent, it) }
            )
        }
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
    onMyRecipesClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
){
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
                .graphicsLayer {
                    shadowElevation = 14f
                    shape = RoundedCornerShape(28.dp)
                    ambientShadowColor = GlassColors.AccentOrange.copy(alpha = 0.38f)
                    spotShadowColor = GlassColors.AccentOrange.copy(alpha = 0.38f)
                }
                .clip(RoundedCornerShape(28.dp))
                .background(WarmGlassGradient)
                .hazeEffect(state = hazeState) {
                    blurRadius = 25.dp
                    tints = listOf(
                        HazeTint(Color.White.copy(alpha = 0.22f)),
                        HazeTint(GlassColors.AccentOrange.copy(alpha = 0.42f))
                    )
                    noiseFactor = 0.06f
                }
                .border(1.4.dp, Color.White.copy(alpha = 0.82f), RoundedCornerShape(28.dp)),
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

            Spacer(modifier = Modifier.weight(1f))

            NavBarItem(
                icon = Icons.Default.MenuBook,
                label = "دستورهای من",
                selected = currentRoute == BottomTab.MyRecipes.route,
                onClick = onMyRecipesClick,
                modifier = Modifier.weight(1f)
            )

            NavBarItem(
                icon = Icons.Default.Person,
                label = "پروفایل",
                selected = currentRoute == BottomTab.Profile.route,
                onClick = onProfileClick,
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
                        listOf(GlassColors.AccentOrange, GlassColors.AccentSecondary)
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
    val inactiveContentColor = if (GlassColors.darkMode) {
        GlassColors.TextMid
    } else {
        GlassColors.TextLight
    }

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
            tint = if (selected) GlassColors.AccentOrange else inactiveContentColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.height(3.dp))
        AppText(
            text = label,
            fontSize = 9.5.sp,
            color = if (selected) GlassColors.AccentOrange else inactiveContentColor,
            fontWeight = when {
                selected -> FontWeight.Bold
                GlassColors.darkMode -> FontWeight.Medium
                else -> FontWeight.Normal
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AllRecipesTab(
    rootNavController: NavController,
    revealContent: Boolean,
    onMenuClick: () -> Unit,
    onSeeAllRecipes: () -> Unit
) {
    val categoryViewModel: CategoryViewModel = hiltViewModel()
    val homeViewModel: HomeViewModel = hiltViewModel()
    val myRecipesViewModel: MyRecipesViewModel = hiltViewModel()

    val categoriesWithCounts by
    categoryViewModel.categoriesWithCounts.collectAsState()

    val allRecipes by homeViewModel.allRecipes.collectAsState()
    val myRecipes by myRecipesViewModel.recipes.collectAsState()
    val pendingSyncCount by
    homeViewModel.pendingSyncCount.collectAsState()
    val heroArticle by homeViewModel.heroArticle.collectAsState()
    val isRefreshing by categoryViewModel.isRefreshing.collectAsState()

    // دریافت محتوای عمومی را بعد از پایان Oven نیز تکرار می‌کنیم؛ این مسیر
    // به نشست کاربر وابسته نیست و برای مهمان هم باید کار کند.
    LaunchedEffect(revealContent) {
        if (revealContent) homeViewModel.refreshHero()
    }

    CategoryGrid(
        categoriesWithCounts = categoriesWithCounts,
        allRecipes = allRecipes,
        myRecipes = myRecipes,
        pendingSyncCount = pendingSyncCount,
        heroArticle = heroArticle,
        revealContent = revealContent,
        isRefreshing = isRefreshing,
        onRefresh = {
            categoryViewModel.refreshFromCloud()
            homeViewModel.refreshFromCloud()
        },
        onCategoryClick = { category ->
            rootNavController.navigate(
                NavGraph.Screen.CategoryRecipes.createRoute(
                    category.id,
                    category.name
                )
            )
        },
        onRecipeClick = { recipe ->
            rootNavController.navigate(
                NavGraph.Screen.RecipeDetail.createRoute(recipe.id)
            )
        },
        onAddRecipe = {
            rootNavController.navigate(
                NavGraph.Screen.AddRecipe.createRoute()
            )
        },
        onHeroClick = { slug ->
            rootNavController.navigate(NavGraph.Screen.Article.createRoute(slug))
        },
        onMenuClick = onMenuClick,
        onSeeAllRecipes = onSeeAllRecipes,
        onAddCategory = { name, emoji ->
            categoryViewModel.addCategory(name, emoji)
        },
        onUpdateCategory = { id, name, emoji ->
            categoryViewModel.updateCategory(id, name, emoji)
        },
        onDeleteCategory = { id ->
            categoryViewModel.deleteCategory(id)
        }
    )
}
private fun Modifier.quickHomeReveal(
    progress: Float,
    distance: Float = 14f
): Modifier {
    return graphicsLayer {
        alpha = progress
        translationY = distance * (1f - progress)
    }
}
// ─── گرید دسته‌بندی‌ها (صفحه‌ی اصلی خانه) ──────────────────
@OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class
)@Composable
fun CategoryGrid(
    categoriesWithCounts: List<CategoryWithCount>,
    allRecipes: List<Recipe>,
    myRecipes: List<Recipe>,
    pendingSyncCount: Int,
    heroArticle: HeroArticleDto?,
    onCategoryClick: (Category) -> Unit,
    revealContent: Boolean,
    onRecipeClick: (Recipe) -> Unit,
    onAddRecipe: () -> Unit,
    onHeroClick: (String) -> Unit,
    onMenuClick: () -> Unit,
    onSeeAllRecipes: () -> Unit,
    onAddCategory: (String, String) -> Unit,
    onUpdateCategory: (String, String, String) -> Unit,
    onDeleteCategory: (String) -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<Category?>(null) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var homeRevealCompleted by remember {
        mutableStateOf(false)
    }

    val homeReveal = remember {
        Animatable(0f)
    }

    LaunchedEffect(revealContent) {
        if (
            revealContent &&
            !homeRevealCompleted
        ) {
            homeReveal.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 420,
                    easing = LinearOutSlowInEasing
                )
            )

            homeRevealCompleted = true
        }
    }

    fun revealPart(start: Float, end: Float): Float {
        return ((homeReveal.value - start) / (end - start))
            .coerceIn(0f, 1f)
    }

    val searchReveal = revealPart(0f, 0.24f)
    val heroReveal = revealPart(0.10f, 0.42f)
    val categoryTitleReveal = revealPart(0.26f, 0.52f)
    val categoryCardsReveal = revealPart(0.36f, 0.68f)
    val recipeTitleReveal = revealPart(0.56f, 0.80f)
    val recipesReveal = revealPart(0.68f, 1f)
    val visibleCategories = remember(categoriesWithCounts, searchQuery) {
        if (searchQuery.isBlank()) {
            categoriesWithCounts
        } else {
            categoriesWithCounts.filter {
                it.category.name.contains(searchQuery.trim(), ignoreCase = true)
            }
        }
    }
    val normalizedQuery = searchQuery.trim()

    val visibleRecipes = remember(
        allRecipes,
        myRecipes,
        normalizedQuery
    ) {
        if (normalizedQuery.isBlank()) {
            myRecipes
                .sortedByDescending { it.updatedAt }
                .take(3)
        } else {
            allRecipes.filter { recipe ->
                recipe.title.contains(normalizedQuery, ignoreCase = true) ||
                        recipe.description.contains(
                            normalizedQuery,
                            ignoreCase = true
                        ) ||
                        recipe.ingredients.any { ingredient ->
                            ingredient.name.contains(
                                normalizedQuery,
                                ignoreCase = true
                            )
                        }
            }.take(6)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        HomeBrandHeader(onMenuClick = onMenuClick)

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.weight(1f)
        ) {
            LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = NavBarHeight + NavBarVerticalMargin * 2 + 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                SyncStatusChip(
                    pendingCount = pendingSyncCount,
                    modifier = Modifier.quickHomeReveal(searchReveal)
                )
            }
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .quickHomeReveal(searchReveal),
                    placeholder = {
                        AppText(
                            "جست‌وجوی دستور یا دسته‌بندی",
                            color = GlassColors.TextLight
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = GlassColors.AccentOrange
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(22.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = GlassColors.GlassCard.copy(alpha = 0.92f),
                        unfocusedContainerColor = GlassColors.GlassCard.copy(alpha = 0.78f),
                        focusedBorderColor =
                            GlassColors.AccentOrange.copy(alpha = 0.55f),
                        unfocusedBorderColor =
                            GlassColors.Divider.copy(alpha = 0.85f),
                        cursorColor = GlassColors.AccentOrange,
                        focusedTextColor = GlassColors.TextDark,
                        unfocusedTextColor = GlassColors.TextDark
                    )
                )
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .quickHomeReveal(
                            progress = heroReveal,
                            distance = 18f
                        )
                ) {
                    SolarChefHero(
                        article = heroArticle,
                        animationStarted = heroReveal > 0.04f,
                        onAddRecipe = onAddRecipe,
                        onArticleClick = onHeroClick
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .quickHomeReveal(categoryTitleReveal),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppText(
                        text = "دسته‌بندی‌ها",
                        color = GlassColors.TextDark,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    AppText(
                        text = "${categoriesWithCounts.size} دسته",
                        color = GlassColors.TextLight,
                        fontSize = 12.sp
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .quickHomeReveal(
                            progress = categoryCardsReveal,
                            distance = 18f
                        )
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CompactAddCategoryCard(
                        onClick = { showAddDialog = true }
                    )

                    visibleCategories.forEach { item ->
                        CompactCategoryCard(
                            item = item,
                            onClick = {
                                onCategoryClick(item.category)
                            },
                            onLongClick = {
                                if (
                                    item.category.id !in setOf(
                                        "cat_bread",
                                        "cat_cake",
                                        "cat_pizza"
                                    )
                                ) {
                                    categoryToEdit = item.category
                                }
                            }
                        )
                    }
                }
            }
            item {
                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .quickHomeReveal(recipeTitleReveal),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppText(
                        text = if (normalizedQuery.isBlank()) {
                            "دستورهای اخیر"
                        } else {
                            "نتایج دستورها"
                        },
                        color = GlassColors.TextDark,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    if (normalizedQuery.isBlank() && myRecipes.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(onClick = onSeeAllRecipes)
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            AppText(
                                text = "مشاهده همه",
                                color = GlassColors.AccentOrange,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(Modifier.width(3.dp))

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = GlassColors.AccentOrange,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            if (visibleRecipes.isNotEmpty()) {
                items(
                    items = visibleRecipes,
                    key = { recipe -> "home_${recipe.id}" }
                ) { recipe ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .quickHomeReveal(
                                progress = recipesReveal,
                                distance = 20f
                            )
                    ) {
                        HomeRecipeCard(
                            recipe = recipe,
                            onClick = {
                                onRecipeClick(recipe)
                            }
                        )
                    }
                }
            } else {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .quickHomeReveal(
                                progress = recipesReveal,
                                distance = 24f
                            )
                    ) {
                        HomeRecipesEmptyState(
                            isSearching = normalizedQuery.isNotBlank(),
                            onAddRecipe = onAddRecipe
                        )
                    }
                }
            }
            }
        }
    }

    if (showAddDialog) {
        AddCategoryDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, emoji ->
                onAddCategory(name, emoji)
                showAddDialog = false
            }
        )
    }

    // ← جدید: دیالوگ ویرایش/حذف (با long-press روی کارت باز می‌شه)
    categoryToEdit?.let { category ->
        EditCategoryDialog(
            category = category,
            onDismiss = { categoryToEdit = null },
            onConfirm = { name, emoji ->
                onUpdateCategory(category.id, name, emoji)
                categoryToEdit = null
            },
            onDeleteRequest = {
                categoryToDelete = category
                categoryToEdit = null
            }
        )
    }

    // ← جدید: تأییدیه‌ی حذف
    categoryToDelete?.let { category ->
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = GlassColors.GlassWhite,
            tonalElevation = 0.dp,
            titleContentColor = GlassColors.TextDark,
            textContentColor = GlassColors.TextDark,
            title = { AppText(stringResource(R.string.category_delete), fontWeight = FontWeight.Bold) },
            text = {
                AppText(stringResource(R.string.category_delete_message, category.name))
            },
            confirmButton = {
                Button(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    onClick = {
                        onDeleteCategory(category.id)
                        categoryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    AppText(stringResource(R.string.action_delete), color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    onClick = { categoryToDelete = null },
                    border = BorderStroke(1.dp, GlassColors.AccentOrange)
                ) {
                    AppText(stringResource(R.string.action_cancel), color = GlassColors.AccentOrange)
                }
            }
        )
    }
}

@Composable
private fun SolarChefHero(
    article: HeroArticleDto?,
    animationStarted: Boolean,
    onAddRecipe: () -> Unit,
    onArticleClick: (String) -> Unit
) {
    val imageScale = remember(article?.id) { Animatable(1f) }
    val titleAlpha = remember(article?.id) { Animatable(0f) }
    val titleOffset = remember(article?.id) { Animatable(38f) }
    val summaryAlpha = remember(article?.id) { Animatable(0f) }
    val summaryOffset = remember(article?.id) { Animatable(38f) }
    val actionAlpha = remember(article?.id) { Animatable(0f) }
    val actionOffset = remember(article?.id) { Animatable(38f) }
    val layoutDirection = LocalLayoutDirection.current

    LaunchedEffect(article?.id, animationStarted) {
        imageScale.snapTo(1f)
        titleAlpha.snapTo(0f)
        titleOffset.snapTo(38f)
        summaryAlpha.snapTo(0f)
        summaryOffset.snapTo(38f)
        actionAlpha.snapTo(0f)
        actionOffset.snapTo(38f)

        // ساخت Composable زودتر از دیده‌شدن Hero انجام می‌شود؛ انیمیشن تنها
        // وقتی خود کارت وارد محدودهٔ نمایش شد شروع می‌شود.
        if (!animationStarted) return@LaunchedEffect

        launch {
            while (true) {
                imageScale.animateTo(
                    targetValue = 1.15f,
                    animationSpec = tween(durationMillis = 5_500, easing = LinearOutSlowInEasing)
                )
                imageScale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 5_500, easing = FastOutSlowInEasing)
                )
            }
        }

        launch {
            titleAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing)
            )
        }
        launch {
            titleOffset.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 620, easing = FastOutSlowInEasing)
            )
        }
        launch {
            delay(480)
            summaryAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing)
            )
        }
        launch {
            delay(480)
            summaryOffset.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 620, easing = FastOutSlowInEasing)
            )
        }
        launch {
            delay(960)
            actionAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing)
            )
        }
        launch {
            delay(960)
            actionOffset.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 620, easing = FastOutSlowInEasing)
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(168.dp)
            .clip(RoundedCornerShape(28.dp))
    ) {
        if (!article?.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = article?.imageUrl,
                contentDescription = article?.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = imageScale.value
                        scaleY = imageScale.value
                    }
            )
        } else {
            Image(
                painter = painterResource(R.drawable.mainbg),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = imageScale.value
                        scaleY = imageScale.value
                    }
            )
        }

        // لایهٔ تیره برای خوانایی متن
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xE62A160E),
                            Color(0x992A160E),
                            Color.Transparent
                        )
                    )
                )
        )

        // درخشش شیشه‌ای ملایم
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.48f),
                    shape = RoundedCornerShape(28.dp)
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(horizontal = 20.dp, vertical = 18.dp)
                .widthIn(max = 260.dp)
        ) {
            AppText(
                text = article?.title ?: "ایده‌ات را به یک دستور تبدیل کن",
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                modifier = Modifier.graphicsLayer {
                    alpha = titleAlpha.value
                    translationX = if (layoutDirection == LayoutDirection.Rtl) titleOffset.value else -titleOffset.value
                }
            )

            Spacer(Modifier.height(5.dp))

            AppText(
                text = article?.summary ?: "دستور جدید بساز یا متن آماده را هوشمند استخراج کن",
                color = Color.White.copy(alpha = 0.84f),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.graphicsLayer {
                    alpha = summaryAlpha.value
                    translationX = if (layoutDirection == LayoutDirection.Rtl) summaryOffset.value else -summaryOffset.value
                }
            )

            Spacer(Modifier.height(13.dp))

            Row(
                modifier = Modifier
                    .widthIn(max = 210.dp)
                    .graphicsLayer {
                        alpha = actionAlpha.value
                        translationX = if (layoutDirection == LayoutDirection.Rtl) actionOffset.value else -actionOffset.value
                    }
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.92f))
                    .clickable {
                        if (article != null) onArticleClick(article.slug) else onAddRecipe()
                    }
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (article == null) Icons.Default.Add else Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = GlassColors.AccentOrange,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(Modifier.width(6.dp))

                AppText(
                    text = article?.callToAction
                        ?.takeIf { it.length <= 20 }
                        ?: if (article == null) "افزودن دستور" else "مشاهده آموزش",
                    color = Color(0xFF44281E),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun CompactCategoryCard(
    item: CategoryWithCount,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(128.dp)
            .height(112.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(WarmGlassGradient)
            .border(
                width = 1.dp,
                color = if (GlassColors.darkMode) {
                    GlassColors.Divider.copy(alpha = 0.85f)
                } else {
                    Color.Transparent
                },
                shape = RoundedCornerShape(22.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        AppText(
            text = item.category.emoji,
            fontSize = 27.sp
        )

        Column {
            AppText(
                text = item.category.name,
                color = GlassColors.TextDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            AppText(
                text = "${item.recipeCount} دستور",
                color = GlassColors.TextLight,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun CompactAddCategoryCard(
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(108.dp)
            .height(112.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(GlassColors.GlassCard.copy(alpha = 0.72f))
            .border(
                width = 1.dp,
                color = GlassColors.AccentOrange.copy(alpha = 0.38f),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable { onClick() }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(
                    GlassColors.AccentOrange.copy(alpha = 0.14f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                tint = GlassColors.AccentOrange
            )
        }

        Spacer(Modifier.height(8.dp))

        AppText(
            text = "دسته جدید",
            color = GlassColors.AccentOrange,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun CategoryCard(item: CategoryWithCount, onClick: () -> Unit, onLongClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(WarmGlassGradient)
            .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        AppText(item.category.emoji, fontSize = 32.sp)
        Column {
            AppText(
                text = item.category.name,
                color = GlassColors.TextDark,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            AppText(
                text = "${item.recipeCount} دستور",
                color = GlassColors.TextLight,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun AddCategoryCard(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(GlassColors.GlassCard)
            .border(1.dp, GlassColors.AccentOrange.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(GlassColors.AccentOrange.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = GlassColors.AccentOrange)
        }
        Spacer(modifier = Modifier.height(8.dp))
        AppText(stringResource(R.string.category_new), color = GlassColors.AccentOrange, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

private val categoryEmojiOptions = listOf("🍞", "🍰", "🍕", "🍲", "🥗", "🍜", "🍗", "🍮", "🥘", "🍳")

@Composable
private fun CategoryNameEmojiFields(
    name: String,
    onNameChange: (String) -> Unit,
    selectedEmoji: String,
    onEmojiChange: (String) -> Unit
) {
    Column {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { AppText(stringResource(R.string.category_name_example), color = GlassColors.TextLight) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = GlassColors.GlassCard,
                unfocusedContainerColor = GlassColors.GlassCard,
                focusedBorderColor = GlassColors.AccentOrange,
                unfocusedBorderColor = GlassColors.Divider,
                focusedTextColor = GlassColors.TextDark,
                unfocusedTextColor = GlassColors.TextDark,
                cursorColor = GlassColors.AccentOrange
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = selectedEmoji,
            onValueChange = onEmojiChange,
            modifier = Modifier.fillMaxWidth(),
            label = { AppText(stringResource(R.string.category_custom_emoji), color = GlassColors.TextMid) },
            placeholder = { AppText(stringResource(R.string.category_emoji_example), color = GlassColors.TextLight) },
            supportingText = { AppText(stringResource(R.string.category_emoji_help), color = GlassColors.TextLight) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = GlassColors.GlassCard,
                unfocusedContainerColor = GlassColors.GlassCard,
                focusedBorderColor = GlassColors.AccentOrange,
                unfocusedBorderColor = GlassColors.Divider,
                focusedTextColor = GlassColors.TextDark,
                unfocusedTextColor = GlassColors.TextDark,
                focusedLabelColor = GlassColors.TextMid,
                unfocusedLabelColor = GlassColors.TextLight,
                cursorColor = GlassColors.AccentOrange
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categoryEmojiOptions.forEach { emoji ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (emoji == selectedEmoji) GlassColors.AccentOrange.copy(alpha = 0.25f)
                            else Color.White
                        )
                        .border(
                            width = if (emoji == selectedEmoji) 1.5.dp else 1.dp,
                            color = if (emoji == selectedEmoji) GlassColors.AccentOrange else Color(0xFFFFC107),
                            shape = CircleShape
                        )
                        .clickable { onEmojiChange(emoji) },
                    contentAlignment = Alignment.Center
                ) {
                    AppText(emoji, fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun AddCategoryDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf(categoryEmojiOptions.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = GlassColors.GlassWhite,
        tonalElevation = 0.dp,
        titleContentColor = GlassColors.TextDark,
        textContentColor = GlassColors.TextDark,
        title = { AppText(stringResource(R.string.category_new), fontWeight = FontWeight.Bold) },
        text = {
            CategoryNameEmojiFields(
                name = name,
                onNameChange = { name = it },
                selectedEmoji = selectedEmoji,
                onEmojiChange = { selectedEmoji = it }
            )
        },
        confirmButton = {
            Button(
                modifier = Modifier.padding(horizontal = 4.dp),
                onClick = { onConfirm(name.trim(), selectedEmoji.trim()) },
                enabled = name.isNotBlank() && selectedEmoji.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = GlassColors.AccentOrange)
            ) {
                AppText(stringResource(R.string.category_create), color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            OutlinedButton(
                modifier = Modifier.padding(horizontal = 4.dp),
                onClick = onDismiss,
                border = BorderStroke(1.dp, GlassColors.AccentOrange)
            ) {
                AppText(stringResource(R.string.action_cancel), color = GlassColors.AccentOrange)
            }
        }
    )
}

// ← جدید: دیالوگ ویرایش (با long-press روی کارت دسته‌بندی باز می‌شه)
@Composable
fun EditCategoryDialog(
    category: Category,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
    onDeleteRequest: () -> Unit
) {
    var name by remember { mutableStateOf(category.name) }
    var selectedEmoji by remember { mutableStateOf(category.emoji) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = GlassColors.GlassWhite,
        tonalElevation = 0.dp,
        titleContentColor = GlassColors.TextDark,
        textContentColor = GlassColors.TextDark,
        title = { AppText(stringResource(R.string.category_edit), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                CategoryNameEmojiFields(
                    name = name,
                    onNameChange = { name = it },
                    selectedEmoji = selectedEmoji,
                    onEmojiChange = { selectedEmoji = it }
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Red.copy(alpha = 0.08f))
                        .clickable { onDeleteRequest() }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    AppText(stringResource(R.string.category_delete_this), color = Color(0xFFD32F2F), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                modifier = Modifier.padding(horizontal = 4.dp),
                onClick = { onConfirm(name.trim(), selectedEmoji.trim()) },
                enabled = name.isNotBlank() && selectedEmoji.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = GlassColors.AccentOrange)
            ) {
                AppText(stringResource(R.string.action_save), color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            OutlinedButton(
                modifier = Modifier.padding(horizontal = 4.dp),
                onClick = onDismiss,
                border = BorderStroke(1.dp, GlassColors.AccentOrange)
            ) {
                AppText(stringResource(R.string.action_cancel), color = GlassColors.AccentOrange)
            }
        }
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
private fun string(): String = "دستورهای"

@Composable
fun HomeListHeader(title: String, onProfileClick: (() -> Unit)? = null) {

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
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        if (onProfileClick != null) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(GlassColors.GlassCard)
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "پروفایل",
                    tint = GlassColors.AccentOrange,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun HomeRecipeList(
    recipes: List<Recipe>,
    emptyMessage: String,
    rootNavController: NavController,
    title: String,
    onProfileClick: (() -> Unit)? = null
) {
    // فاصله‌ی پایین: فقط به اندازه‌ی ارتفاع نویگیشن‌بار شناور + حاشیه‌هاش
    // (همون بخشی که زیرش محو و شیشه‌ای می‌شه)، به‌علاوه‌ی یه فضای اضافه‌ی
    // قابل‌تنظیم (extraScrollSpace) تا آخرین کارت بتونه بیشتر بیاد بالا.
    // ← همین یه عدد رو برای آزمون‌وخطا تغییر بده (الان: دو برابر ارتفاع نویگیشن‌بار)
    val extraScrollSpace = (NavBarHeight + NavBarVerticalMargin * 2)
    val bottomInset = NavBarHeight + NavBarVerticalMargin * 2 + extraScrollSpace

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        HomeListHeader(
            title = title,
            onProfileClick = onProfileClick
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
private fun HomeRecipesEmptyState(
    isSearching: Boolean,
    onAddRecipe: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(GlassColors.GlassCard.copy(alpha = 0.78f))
            .border(
                1.dp,
                GlassColors.Divider.copy(alpha = 0.88f),
                RoundedCornerShape(22.dp)
            )
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppText(
            text = if (isSearching) "🔎" else "🍽️",
            fontSize = 34.sp
        )

        Spacer(Modifier.height(10.dp))

        AppText(
            text = if (isSearching) {
                "دستوری با این عبارت پیدا نشد"
            } else {
                "هنوز دستور شخصی اضافه نکردی"
            },
            color = GlassColors.TextMid,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )

        if (!isSearching) {
            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onAddRecipe,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GlassColors.AccentOrange
                )
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(Modifier.width(6.dp))

                AppText(
                    text = "افزودن اولین دستور",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
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
@Composable
private fun SyncStatusChip(
    pendingCount: Int,
    modifier: Modifier = Modifier
) {
    val isSynced = pendingCount == 0

    val backgroundColor =
        if (isSynced) {
            Color(0xFF3FA66A).copy(alpha = 0.10f)
        } else {
            GlassColors.AccentOrange.copy(alpha = 0.12f)
        }

    val contentColor =
        if (isSynced) {
            Color(0xFF287A4A)
        } else {
            Color(0xFFB85C28)
        }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = contentColor.copy(alpha = 0.18f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        AppText(
            text = if (isSynced) "✓" else "↻",
            color = contentColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        AppText(
            text = if (isSynced) {
                "همه تغییرات پشتیبان‌گیری شده‌اند"
            } else {
                stringResource(R.string.sync_waiting, pendingCount)
            },
            color = contentColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun HomeBrandHeader(onMenuClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(GlassColors.BgLight.copy(alpha = 0.96f))
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = 10.dp
            )
    ) {
        // در RTL، Start سمت راست و End سمت چپ است.
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = stringResource(R.string.content_description_open_menu),
            tint = GlassColors.TextDark,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(44.dp)
                .clip(CircleShape)
                .clickable(onClick = onMenuClick)
                .padding(10.dp)
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(54.dp)
                .clip(RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.solarchef_logo),
                contentDescription = stringResource(R.string.content_description_logo),
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppText(
                text = "SolarChef",
                color = GlassColors.TextDark,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(Modifier.height(3.dp))
            AppText(
                text = "سلام، امروز چی می‌پزی؟",
                color = GlassColors.TextMid,
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    }
}
