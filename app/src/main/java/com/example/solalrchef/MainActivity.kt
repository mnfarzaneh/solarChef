package com.mnfarzaneh.solalrchef

import android.os.Build
import android.os.Bundle
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mnfarzaneh.solalrchef.ui.navigation.NavGraph
import com.mnfarzaneh.solalrchef.ui.screen.AddRecipeScreen
import com.mnfarzaneh.solalrchef.ui.screen.CookingScreen
import com.mnfarzaneh.solalrchef.ui.screen.HomeScreen
import com.mnfarzaneh.solalrchef.ui.screen.MyRecipesScreen
import com.mnfarzaneh.solalrchef.ui.screen.OvenScreen
import com.mnfarzaneh.solalrchef.ui.screen.RecipeDetailScreen
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.navArgument
import androidx.navigation.NavType

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()   // ← این خط باید قبل از super.onCreate یا بلافاصله بعدش باشه
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        window.statusBarColor = android.graphics.Color.TRANSPARENT
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val navController = rememberNavController()

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                NavHost(
                    navController = navController,
                    startDestination = NavGraph.Screen.Oven.route,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(NavGraph.Screen.Oven.route) {
                        // ← استثنا: این صفحه رو صریحاً به LTR برمی‌گردونیم
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            OvenScreen(navController = navController)
                        }
                    }
                    composable(NavGraph.Screen.Home.route) {
                        HomeScreen(rootNavController = navController)
                    }
                    composable(route = NavGraph.Screen.RecipeDetail.route) { backStackEntry ->
                        val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
                        RecipeDetailScreen(recipeId = recipeId, navController = navController)
                    }
                    composable(route = NavGraph.Screen.Cooking.route) { backStackEntry ->
                        val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
                        CookingScreen(recipeId = recipeId, navController = navController)
                    }
                    composable(NavGraph.Screen.AddRecipe.route) {
                        AddRecipeScreen(navController = navController)
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

