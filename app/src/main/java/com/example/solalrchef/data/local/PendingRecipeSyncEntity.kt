package com.mnfarzaneh.solalrchef.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pending_recipe_sync",
    indices = [
        Index(value = ["ownerId"])
    ]
)
data class PendingRecipeSyncEntity(
    @PrimaryKey
    val recipeId: String,
    val ownerId: String,
    val operation: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val UPSERT = "UPSERT"
        const val DELETE = "DELETE"
    }
}