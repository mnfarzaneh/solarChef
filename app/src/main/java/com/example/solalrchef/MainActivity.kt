package com.example.solalrchef

import android.os.Build
import android.os.Bundle
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.solalrchef.ui.navigation.NavGraph
import com.example.solalrchef.ui.screen.OvenScreen
import com.example.solalrchef.ui.screen.RecipeDetailScreen
import com.example.solalrchef.ui.screen.CookingScreen
import androidx.compose.ui.unit.LayoutDirection
import com.example.solalrchef.ui.screen.AddRecipeScreen
import com.example.solalrchef.ui.screen.MyRecipesScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.insetsController?.let {
            it.hide(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE)
            it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        window.statusBarColor = android.graphics.Color.TRANSPARENT
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val navController = rememberNavController()

            // ← بدون RTL wrapper اینجا
            NavHost(
                navController = navController,
                startDestination = NavGraph.Screen.Oven.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(NavGraph.Screen.Oven.route) {
                    // ← OvenScreen بدون RTL
                    OvenScreen(navController = navController)
                }

                composable(route = NavGraph.Screen.RecipeDetail.route) { backStackEntry ->
                    val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
                    // ← فقط اینجا RTL
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        RecipeDetailScreen(recipeId = recipeId, navController = navController)
                    }
                }

                composable(route = NavGraph.Screen.Cooking.route) { backStackEntry ->
                    val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
                    // ← فقط اینجا RTL
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        CookingScreen(recipeId = recipeId, navController = navController)
                    }
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
            }
        }
    }
}

