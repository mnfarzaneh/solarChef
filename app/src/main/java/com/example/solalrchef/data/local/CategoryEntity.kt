package com.mnfarzaneh.solalrchef.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val emoji: String = "🍽️",
    val sortOrder: Int = 0,
    // ← همون منطق ownerId که برای دستورها داریم؛ دسته‌بندی‌های هر کاربر جدا می‌مونه
    val ownerId: String? = null
)