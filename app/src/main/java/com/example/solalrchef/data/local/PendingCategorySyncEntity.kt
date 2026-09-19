package com.mnfarzaneh.solalrchef.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pending_category_sync",
    indices = [Index(value = ["ownerId"])]
)
data class PendingCategorySyncEntity(
    @PrimaryKey
    val categoryId: String,
    val ownerId: String,
    val operation: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val CREATE = "CREATE"
        const val UPDATE = "UPDATE"
        const val DELETE = "DELETE"
    }
}