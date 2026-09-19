package com.mnfarzaneh.solalrchef.data.local

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "system_favorites",
    primaryKeys = ["recipeId", "ownerKey"],
    indices = [Index("ownerKey")]
)
data class SystemFavoriteEntity(
    val recipeId: String,
    val ownerKey: String,
    val createdAt: Long = System.currentTimeMillis()
)
