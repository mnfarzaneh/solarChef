package com.example.solalrchef.model

data class Ingredient(
    val amount: String,   // "12"
    val unit: String,     // "ounces"
    val name: String      // "broccoli florets"
)

data class CookingStep(
    val instruction: String  // "Preheat the oven to 400F"
)

data class Recipe(
    val id: String,
    val title: String,
    val description: String = "",
    val image: Int = 0,           // ← عکس روی سولاردام (آیکون کوچک)
    val imagePath: String = "",
    val detailImage: Int = image,  // ← عکس صفحه جزئیات (بزرگ)
    val author: String = "Solar Chef",
    val source: String = "Solar Chef",
    val totalTime: String = "45 min",
    val cookTime: String = "30 min",
    val yield: String = "4",
    val rating: Float = 4.5f,
    val difficulty: String = "Easy",
    val calories: Int = 360,
    val isFavorite: Boolean = false,   // ← اضافه شد
    val ingredients: List<Ingredient> = emptyList(),
    val steps: List<CookingStep> = emptyList(),
    val equipment: List<String> = emptyList()  // ← جدید

)