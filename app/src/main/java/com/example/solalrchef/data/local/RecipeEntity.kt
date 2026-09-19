package com.mnfarzaneh.solalrchef.data.local

import androidx.room.ColumnInfo
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
    val imageKey: String = "",
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
    val stepsJson: String,
    val equipmentJson: String = "[]",  // ← جدید

    // ← مشخص می‌کنه این دستور مال کدوم حساب کاربریه (uid فایربیس).
    // null یعنی دستور «مهمان»ه — قبل از لاگین ساخته شده و هنوز claim نشده.
    @ColumnInfo(defaultValue = "NULL")
    val ownerId: String? = null,

    // ← جدید: این دستور می‌تونه هم‌زمان توی چند دسته‌بندی باشه، پس به‌جای
    // یه categoryId تکی، یه لیست JSON از idهای دسته‌بندی ذخیره می‌کنیم
    // (دقیقاً مثل الگوی equipmentJson/ingredientsJson)
    @ColumnInfo(defaultValue = "'[]'")
    val categoryIdsJson: String = "[]",

    @ColumnInfo(defaultValue = "0")
    val createdAt: Long = 0L,

    @ColumnInfo(defaultValue = "0")
    val updatedAt: Long = 0L
)
