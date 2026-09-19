package com.mnfarzaneh.solalrchef

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mnfarzaneh.solalrchef.ui.navigation.NavGraph
import com.mnfarzaneh.solalrchef.ui.screen.AddRecipeScreen
import com.mnfarzaneh.solalrchef.ui.screen.CookingScreen
import com.mnfarzaneh.solalrchef.ui.screen.CategoryRecipesScreen
import com.mnfarzaneh.solalrchef.ui.screen.HomeScreen
import com.mnfarzaneh.solalrchef.ui.screen.LoginScreen
import com.mnfarzaneh.solalrchef.ui.screen.MyRecipesScreen
import com.mnfarzaneh.solalrchef.ui.screen.OvenScreen
import com.mnfarzaneh.solalrchef.ui.screen.ProfileScreen
import com.mnfarzaneh.solalrchef.ui.screen.RecipeDetailScreen
import com.mnfarzaneh.solalrchef.ui.screen.RegisterScreen
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.navigation.navDeepLink
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import com.mnfarzaneh.solalrchef.ui.screen.CustomSplashScreen
import com.mnfarzaneh.solalrchef.ui.screen.ArticleScreen
import com.mnfarzaneh.solalrchef.ui.screen.FirstUseGuide
import com.mnfarzaneh.solalrchef.ui.screen.ArticlesScreen
import com.mnfarzaneh.solalrchef.data.sync.ArticleNotificationScheduler
import com.mnfarzaneh.solalrchef.ui.theme.AppAccent
import com.mnfarzaneh.solalrchef.ui.theme.GlassColors
import com.mnfarzaneh.solalrchef.ui.theme.SolalrChefTheme
import androidx.compose.runtime.SideEffect
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onStart() {
        super.onStart()
        // علاوه بر بررسی روزانه، با هر بار برگشت واقعی کاربر به اپ نیز بررسی می‌کنیم.
        ArticleNotificationScheduler.checkNow(this)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()   // ← این خط باید قبل از super.onCreate یا بلافاصله بعدش باشه
        super.onCreate(savedInstanceState)

        // جهت خود Window را قبل از ساخت هر Compose Dialog مشخص می‌کنیم.
        // در غیر این صورت، روی گوشی‌هایی با زبان سیستم LTR ممکن است AlertDialog
        // در اولین فریم چپ‌چین ساخته شود و سپس با CompositionLocal به RTL بپرد.
        window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL

        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)

        window.statusBarColor = android.graphics.Color.TRANSPARENT
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val appearancePreferences = getSharedPreferences("solarchef_appearance", MODE_PRIVATE)
        GlassColors.setAppearance(
            accent = AppAccent.fromStorageKey(appearancePreferences.getString("accent", null)),
            isDark = appearancePreferences.getBoolean("dark_mode", false)
        )

        setContent {
            val navController = rememberNavController()
            val darkMode = GlassColors.darkMode

            SideEffect {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !darkMode
                    isAppearanceLightNavigationBars = !darkMode
                }
            }

            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { }

            // ← لاگین اختیاریه: بدون توجه به وضعیت لاگین، همیشه از Oven شروع می‌کنیم.
            // کاربرای قبلی (بدون حساب) هیچ تغییری توی تجربه‌شون نمی‌بینن.
            // Login/Register فقط از طریق دکمه‌ی «پشتیبان‌گیری» توی پروفایل قابل‌دسترسیه.

            SolalrChefTheme(darkTheme = darkMode, dynamicColor = false) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                val introPreferences = remember {
                    getSharedPreferences("solarchef_intro_transition", MODE_PRIVATE)
                }
                val shouldPlayFullIntro = remember {
                    !introPreferences.getBoolean("full_intro_completed", false)
                }
                var showCustomSplash by remember {
                    mutableStateOf(true)
                }

                LaunchedEffect(shouldPlayFullIntro) {
                    delay(if (shouldPlayFullIntro) 900 else 360)
                    showCustomSplash = false
                }

                if (showCustomSplash) {
                    CustomSplashScreen()
                } else {
                    LaunchedEffect(Unit) {
                        val permissionPreferences = getSharedPreferences(
                            "article_notification_preferences",
                            MODE_PRIVATE
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED &&
                            !permissionPreferences.getBoolean("notification_permission_asked", false)
                        ) {
                            permissionPreferences.edit()
                                .putBoolean("notification_permission_asked", true)
                                .apply()
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                    NavHost(
                        navController = navController,
                        startDestination = NavGraph.Screen.Home.route,
                        modifier = Modifier.fillMaxSize()
                    ) {
                    composable(NavGraph.Screen.Login.route) {
                        LoginScreen(
                            onLoginSuccess = {
                                // ← مستقیم برمی‌گردیم صفحه‌ی خانه و اونجا می‌مونیم
                                // (Profile و Login از استک پاک می‌شن)
                                navController.popBackStack(NavGraph.Screen.Home.route, false)
                            },
                            onNavigateToRegister = {
                                navController.navigate(NavGraph.Screen.Register.route)
                            }
                        )
                    }
                    composable(NavGraph.Screen.Register.route) {
                        RegisterScreen(
                            onRegisterSuccess = {
                                navController.popBackStack(NavGraph.Screen.Home.route, false)
                            },
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable(NavGraph.Screen.Profile.route) {
                        ProfileScreen(
                            onLoginClick = { navController.navigate(NavGraph.Screen.Login.route) }
                        )
                    }
                    composable(
                        route = NavGraph.Screen.Home.route,
                        enterTransition = {
                            EnterTransition.None
                        }
                    ) {
                        val context = LocalContext.current
                        val guidePreferences = remember {
                            context.getSharedPreferences("solarchef_first_use", MODE_PRIVATE)
                        }
                        var showOvenOverlay by rememberSaveable {
                            mutableStateOf(shouldPlayFullIntro)
                        }

                        var revealHomeContent by rememberSaveable {
                            mutableStateOf(!shouldPlayFullIntro)
                        }
                        var showFirstUseGuide by rememberSaveable {
                            mutableStateOf(!guidePreferences.getBoolean("guide_completed", false))
                        }

                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            HomeScreen(
                                rootNavController = navController,
                                revealContent = revealHomeContent
                            )

                            if (showOvenOverlay) {
                                CompositionLocalProvider(
                                    LocalLayoutDirection provides LayoutDirection.Ltr
                                ) {
                                    OvenScreen(
                                        onFinished = {
                                            introPreferences.edit()
                                                .putBoolean("full_intro_completed", true)
                                                .apply()
                                            revealHomeContent = true
                                            showOvenOverlay = false
                                        }
                                    )
                                }
                            }

                            if (!showOvenOverlay && revealHomeContent && showFirstUseGuide) {
                                FirstUseGuide(
                                    onFinished = {
                                        guidePreferences.edit()
                                            .putBoolean("guide_completed", true)
                                            .apply()
                                        showFirstUseGuide = false
                                    }
                                )
                            }
                        }
                    }

                    composable(
                        route = NavGraph.Screen.CategoryRecipes.route,
                        arguments = listOf(
                            navArgument("categoryId") { type = NavType.StringType },
                            navArgument("categoryName") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
                        CategoryRecipesScreen(categoryName = categoryName, navController = navController)
                    }
                    composable(route = NavGraph.Screen.RecipeDetail.route) { backStackEntry ->
                        val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
                        RecipeDetailScreen(recipeId = recipeId, navController = navController)
                    }
                    composable(route = NavGraph.Screen.Cooking.route) { backStackEntry ->
                        val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
                        CookingScreen(recipeId = recipeId, navController = navController)
                    }
                    composable(
                        route = NavGraph.Screen.Articles.route
                    ) {
                        ArticlesScreen(navController = navController)
                    }
                    composable(
                        route = NavGraph.Screen.Article.route,
                        arguments = listOf(navArgument("slug") { type = NavType.StringType }),
                        deepLinks = listOf(navDeepLink { uriPattern = NavGraph.Screen.Article.deepLink })
                    ) { backStackEntry ->
                        ArticleScreen(
                            slug = backStackEntry.arguments?.getString("slug").orEmpty(),
                            navController = navController
                        )
                    }
                    composable(NavGraph.Screen.MyRecipes.route) {
                        MyRecipesScreen(navController = navController)
                    }
                    composable(NavGraph.Screen.EditRecipe.route) { backStackEntry ->
                        val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
                        AddRecipeScreen(navController = navController, editRecipeId = recipeId)
                    }
                    composable(
                        route = NavGraph.Screen.AddRecipe.route,
                        arguments = listOf(
                            navArgument("autoParse") {
                                type = NavType.BoolType
                                defaultValue = false
                            }
                        )
                    ) { backStackEntry ->
                        val autoParse = backStackEntry.arguments?.getBoolean("autoParse") ?: false
                        AddRecipeScreen(
                            navController = navController,
                            autoOpenParseDialog = autoParse   // ← پارامتر جدید
                        )
                    }
                    }
                }
            }
            }
        }
    }
}
