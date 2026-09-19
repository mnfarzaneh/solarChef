package com.mnfarzaneh.solalrchef.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface PendingCategorySyncDao {

    @Query(
        """
        SELECT * FROM pending_category_sync
        WHERE ownerId = :ownerId
        ORDER BY createdAt ASC
        """
    )
    suspend fun getForOwner(ownerId: String): List<PendingCategorySyncEntity>

    @Query(
        """
        SELECT * FROM pending_category_sync
        WHERE categoryId = :categoryId
        LIMIT 1
        """
    )
    suspend fun getByCategoryId(categoryId: String): PendingCategorySyncEntity?

    @Upsert
    suspend fun upsert(item: PendingCategorySyncEntity)

    @Query("DELETE FROM pending_category_sync WHERE categoryId = :categoryId")
    suspend fun deleteByCategoryId(categoryId: String)

    @Query("SELECT COUNT(*) FROM pending_category_sync WHERE ownerId = :ownerId")
    suspend fun countForOwner(ownerId: String): Int

    @Query(
        """
    SELECT COUNT(*) FROM pending_category_sync
    WHERE ownerId = :ownerId
    """
    )
    fun observeCountForOwner(
        ownerId: String
    ): kotlinx.coroutines.flow.Flow<Int>
}