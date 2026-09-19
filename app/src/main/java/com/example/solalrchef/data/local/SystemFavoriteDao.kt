package com.mnfarzaneh.solalrchef.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SystemFavoriteDao {
    @Query("SELECT recipeId FROM system_favorites WHERE ownerKey = :ownerKey")
    fun observeIds(ownerKey: String): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM system_favorites WHERE recipeId = :recipeId AND ownerKey = :ownerKey)")
    suspend fun isFavorite(recipeId: String, ownerKey: String): Boolean

    @Upsert
    suspend fun upsert(value: SystemFavoriteEntity)

    @Query("DELETE FROM system_favorites WHERE recipeId = :recipeId AND ownerKey = :ownerKey")
    suspend fun delete(recipeId: String, ownerKey: String)
}
