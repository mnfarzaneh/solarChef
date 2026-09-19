package com.mnfarzaneh.solalrchef.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface PendingRecipeSyncDao {

    @Query(
        """
        SELECT * FROM pending_recipe_sync
        WHERE ownerId = :ownerId
        ORDER BY createdAt ASC
        """
    )
    suspend fun getForOwner(ownerId: String): List<PendingRecipeSyncEntity>

    @Upsert
    suspend fun upsert(operation: PendingRecipeSyncEntity)

    @Query("DELETE FROM pending_recipe_sync WHERE recipeId = :recipeId")
    suspend fun deleteByRecipeId(recipeId: String)

    @Query("DELETE FROM pending_recipe_sync WHERE ownerId = :ownerId")
    suspend fun deleteForOwner(ownerId: String)

    @Query(
        """
    SELECT COUNT(*) FROM pending_recipe_sync
    WHERE ownerId = :ownerId
    """
    )
    fun observeCountForOwner(ownerId: String): kotlinx.coroutines.flow.Flow<Int>
}