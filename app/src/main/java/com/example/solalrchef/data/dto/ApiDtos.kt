package com.mnfarzaneh.solalrchef.data.remote.dto

import com.google.gson.annotations.SerializedName

data class HeroArticleDto(
    val id: String,
    val slug: String,
    val title: String,
    val summary: String,
    val imageUrl: String,
    val callToAction: String,
    val publishedAt: Long?
)

data class ArticleDto(
    val id: String,
    val slug: String,
    val title: String,
    val summary: String,
    val body: String,
    val imageUrl: String,
    val publishedAt: Long?,
    // Older articles were published before structured content existed.
    val content: ArticleContentDto? = null
)
data class ArticleIngredientDto(val name: String = "", val amount: String = "", val unit: String = "")
data class ArticleStepDto(val title: String = "", val text: String = "", val imageKey: String = "", val imageUrl: String = "")
data class ArticleSectionDto(val heading: String = "", val text: String = "", val imageKey: String = "", val imageUrl: String = "")
data class ArticleContentDto(
    val prepTime: String = "", val cookTime: String = "", val servings: String = "", val difficulty: String = "",
    val ingredients: List<ArticleIngredientDto> = emptyList(), val equipment: List<String> = emptyList(),
    val steps: List<ArticleStepDto> = emptyList(), val sections: List<ArticleSectionDto> = emptyList()
)

// ── Auth ──────────────────────────────────────────────────
// ── Auth ──────────────────────────────────────────────────
data class RegisterRequestDto(
    val email: String,
    val password: String
)

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class RefreshRequestDto(
    val refreshToken: String
)

data class LogoutRequestDto(
    val refreshToken: String
)

data class ForgotPasswordRequestDto(
    val email: String
)

data class ResetPasswordRequestDto(
    val email: String,
    val code: String,
    val newPassword: String
)
data class AuthResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val email: String
)

// ── Recipe ────────────────────────────────────────────────
data class RecipeDto(
    val id: String,
    val title: String,
    val description: String = "",
    val imagePath: String? = "",
    val imageKey: String? = "",
    val imageUrl: String? = "",
    val author: String = "من",
    val source: String = "دستور شخصی",
    val totalTime: String = "",
    val cookTime: String = "",
    val yield: String = "",
    val calories: Int = 0,
    val difficulty: String = "متوسط",
    val rating: Float = 5f,
    val isFavorite: Boolean = false,
    val ingredientsJson: String? = "[]",
    val stepsJson: String? = "[]",
    val equipmentJson: String? = "[]",
    val categoryIds: List<String>? = emptyList(),
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

// ── Category ──────────────────────────────────────────────
data class CategoryDto(
    val id: String,
    val name: String,
    val emoji: String = "🍽️",
    val sortOrder: Int = 0
)
data class ImageUploadResponseDto(
    val imageKey: String
)
