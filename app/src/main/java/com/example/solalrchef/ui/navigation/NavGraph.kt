package com.example.solalrchef.ui.navigation

class NavGraph {
    sealed class Screen(val route: String) {
        data object Oven : Screen("oven")
        data object MyRecipes : Screen("my_recipes")       // ← جدید
        data object AddRecipe : Screen("add_recipe")

        data object EditRecipe : Screen("edit_recipe/{recipeId}") {  // ← جدید
            fun createRoute(recipeId: String) = "edit_recipe/$recipeId"
        }

        data object RecipeDetail : Screen("recipe_detail/{recipeId}") {
            fun createRoute(recipeId: String) = "recipe_detail/$recipeId"
        }
        data object Cooking : Screen("cooking/{recipeId}") {
            fun createRoute(recipeId: String) = "cooking/$recipeId"
        }
    }
}