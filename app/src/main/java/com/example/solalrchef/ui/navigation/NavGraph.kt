package com.mnfarzaneh.solalrchef.ui.navigation

class NavGraph {
    sealed class Screen(val route: String) {
        data object Oven : Screen("oven")
        data object MyRecipes : Screen("my_recipes")       // ← جدید
        // ← تغییر کرد: یه پارامتر اختیاری autoParse اضافه شد
        data object AddRecipe : Screen("add_recipe?autoParse={autoParse}") {
            fun createRoute(autoParse: Boolean = false) = "add_recipe?autoParse=$autoParse"
        }
        data object Home : Screen("home")                    // ← جدید: صفحه‌ی اصلی با Bottom Nav

        data object Favorites : Screen("favorites")           // ← جدید: تب علاقه‌مندی‌ها

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