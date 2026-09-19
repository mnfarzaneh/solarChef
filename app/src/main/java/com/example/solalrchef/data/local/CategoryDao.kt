package com.mnfarzaneh.solalrchef.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query(
        """
        SELECT * FROM categories
        WHERE ownerId IS NULL OR ownerId = :ownerId
        ORDER BY sortOrder ASC, name ASC
        """
    )
    fun getCategoriesForOwner(ownerId: String?): Flow<List<CategoryEntity>>

    @Upsert
    suspend fun upsertCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategory(id: String)

    @Query("DELETE FROM categories WHERE ownerId = :ownerId")
    suspend fun deleteAllCategoriesForOwner(ownerId: String)

    @Query(
        """
        SELECT * FROM categories
        WHERE ownerId IS NULL
          AND id NOT IN ('cat_bread', 'cat_cake', 'cat_pizza')
        ORDER BY sortOrder ASC, name ASC
        """
    )
    suspend fun getUnclaimedCustomCategories(): List<CategoryEntity>

    @Query(
        """
        UPDATE categories
        SET ownerId = :ownerId
        WHERE ownerId IS NULL
          AND id NOT IN ('cat_bread', 'cat_cake', 'cat_pizza')
        """
    )
    suspend fun claimUnclaimedCategories(ownerId: String)

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getCategoryById(id: String): CategoryEntity?
}