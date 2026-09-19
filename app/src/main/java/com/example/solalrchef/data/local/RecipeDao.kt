package com.mnfarzaneh.solalrchef.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

    // ── خواندن همه دستورات (بدون فیلتر مالک — فعلاً جایی استفاده نمی‌شه،
    // ولی برای سازگاری با کد قدیمی نگهش می‌داریم) ──
    @Query("SELECT * FROM user_recipes ORDER BY title ASC")
    fun getAllRecipes(): Flow<List<RecipeEntity>>

    // ── جدید: فقط دستورهای مال یه مالک خاص (یا مهمان‌ها با ownerId=null) ──
    // از عملگر IS استفاده می‌کنیم چون در SQLite، برخلاف =، با NULL هم درست کار می‌کنه
    @Query("SELECT * FROM user_recipes WHERE ownerId IS :ownerId ORDER BY updatedAt DESC, title ASC")
    fun getRecipesForOwner(ownerId: String?): Flow<List<RecipeEntity>>

    // ── جدید: همه دستورهای «مهمان» (بدون مالک) رو یه‌جا claim می‌کنه ──
    @Query("UPDATE user_recipes SET ownerId = :ownerId WHERE ownerId IS NULL")
    suspend fun claimUnclaimedRecipes(ownerId: String)

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
    @Query("UPDATE user_recipes SET isFavorite = :isFavorite, updatedAt = :updatedAt WHERE id = :recipeId")
    suspend fun updateFavorite(recipeId: String, isFavorite: Boolean, updatedAt: Long)

    @Query("DELETE FROM user_recipes WHERE ownerId = :ownerId")
    suspend fun deleteAllRecipesForOwner(ownerId: String)
}
