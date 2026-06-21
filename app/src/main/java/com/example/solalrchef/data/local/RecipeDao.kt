package com.example.solalrchef.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

    // ── خواندن همه دستورات کاربر ──
    @Query("SELECT * FROM user_recipes ORDER BY title ASC")
    fun getAllRecipes(): Flow<List<RecipeEntity>>

    // ── خواندن یک دستور با id ──
    @Query("SELECT * FROM user_recipes WHERE id = :id")
    suspend fun getRecipeById(id: String): RecipeEntity?

    // ── اضافه کردن / آپدیت ──
    @Upsert
    suspend fun upsertRecipe(recipe: RecipeEntity)

    // ── حذف ──
    @Delete
    suspend fun deleteRecipe(recipe: RecipeEntity)

    // ── حذف با id ──
    @Query("DELETE FROM user_recipes WHERE id = :id")
    suspend fun deleteRecipeById(id: String)

    // ── آپدیت علاقه‌مندی ──
    @Query("UPDATE user_recipes SET isFavorite = :isFavorite WHERE id = :recipeId")
    suspend fun updateFavorite(recipeId: String, isFavorite: Boolean)
}