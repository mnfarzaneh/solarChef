package com.mnfarzaneh.solalrchef.data

import com.mnfarzaneh.solalrchef.data.local.RecipeDao
import com.mnfarzaneh.solalrchef.data.local.toEntity
import com.mnfarzaneh.solalrchef.data.local.toRecipe
import com.mnfarzaneh.solalrchef.model.Recipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// ─── مخزن دستورات کاربر ──────────────────────────────────
class UserRecipeRepository @Inject constructor(
    private val dao: RecipeDao
){
    // ── همه دستورات کاربر (Flow = live update) ──
    val allRecipes: Flow<List<Recipe>> = dao.getAllRecipes().map { entities ->
        entities.map { it.toRecipe() }
    }

    // ── ذخیره یا آپدیت ──
    suspend fun saveRecipe(recipe: Recipe) {
        dao.upsertRecipe(recipe.toEntity())
    }

    // ── حذف ──
    suspend fun deleteRecipe(recipeId: String) {
        dao.deleteRecipeById(recipeId)
    }

    // ── خواندن یک دستور ──
    suspend fun getRecipeById(id: String): Recipe? {
        return dao.getRecipeById(id)?.toRecipe()
    }

    // ── آپدیت علاقه‌مندی ──
    suspend fun updateFavorite(recipeId: String, isFavorite: Boolean) {
        dao.updateFavorite(recipeId, isFavorite)
    }
}