package com.example.solalrchef.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// ─── مدل ذخیره‌سازی در دیتابیس ──────────────────────────
@Entity(tableName = "user_recipes")
data class RecipeEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val imagePath: String,      // مسیر عکس در حافظه گوشی
    val author: String,
    val source: String,
    val totalTime: String,
    val cookTime: String,
    val yield: String,
    val calories: Int,
    val difficulty: String,
    val rating: Float,
    val isFavorite: Boolean = false,   // ← اضافه شد
    // مواد لازم و مراحل به صورت JSON ذخیره میشن
    val ingredientsJson: String,
    val stepsJson: String
)