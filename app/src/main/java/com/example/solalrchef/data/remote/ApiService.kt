package com.mnfarzaneh.solalrchef.data.remote

import com.mnfarzaneh.solalrchef.data.remote.dto.AuthResponseDto
import com.mnfarzaneh.solalrchef.data.remote.dto.CategoryDto
import com.mnfarzaneh.solalrchef.data.remote.dto.LoginRequestDto
import com.mnfarzaneh.solalrchef.data.remote.dto.LogoutRequestDto
import com.mnfarzaneh.solalrchef.data.remote.dto.RecipeDto
import com.mnfarzaneh.solalrchef.data.remote.dto.RefreshRequestDto
import com.mnfarzaneh.solalrchef.data.remote.dto.RegisterRequestDto
import retrofit2.Response
import retrofit2.http.*
import com.mnfarzaneh.solalrchef.data.remote.dto.ForgotPasswordRequestDto
import com.mnfarzaneh.solalrchef.data.remote.dto.ResetPasswordRequestDto
import com.mnfarzaneh.solalrchef.data.remote.dto.ImageUploadResponseDto
import com.mnfarzaneh.solalrchef.data.remote.dto.HeroArticleDto
import com.mnfarzaneh.solalrchef.data.remote.dto.ArticleDto

interface ApiService {

    // محتوای عمومی؛ برای این مسیرها ورود کاربر لازم نیست.
    @GET("content/hero")
    suspend fun getHeroArticle(): Response<HeroArticleDto>

    @GET("content/articles")
    suspend fun getArticles(
        @Query("limit") limit: Int = 20
    ): Response<List<HeroArticleDto>>

    @GET("content/articles/{slug}")
    suspend fun getArticle(
        @Path("slug") slug: String
    ): Response<ArticleDto>

    // ── احراز هویت ───────────────────────────────────────
// ── احراز هویت ───────────────────────────────────────────
    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequestDto
    ): Response<AuthResponseDto>

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): Response<AuthResponseDto>

    @POST("auth/refresh")
    suspend fun refresh(
        @Body request: RefreshRequestDto
    ): Response<AuthResponseDto>

    @POST("auth/logout")
    suspend fun logout(
        @Body request: LogoutRequestDto
    ): Response<Unit>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequestDto
    ): Response<Unit>

    @POST("auth/reset-password")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequestDto
    ): Response<Unit>

    @Multipart
    @POST("images")
    suspend fun uploadImage(
        @Header("Authorization") token: String,
        @Part("recipeId") recipeId: okhttp3.RequestBody,
        @Part image: okhttp3.MultipartBody.Part
    ): Response<ImageUploadResponseDto>
    // ── دستورها ──────────────────────────────────────────
    @GET("recipes")
    suspend fun getRecipes(@Header("Authorization") token: String): Response<List<RecipeDto>>

    @POST("recipes")
    suspend fun saveRecipe(
        @Header("Authorization") token: String,
        @Body recipe: RecipeDto
    ): Response<Map<String, String>>

    @DELETE("recipes/{id}")
    suspend fun deleteRecipe(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Map<String, String>>

    // ── دسته‌بندی‌ها ──────────────────────────────────────
    @GET("categories")
    suspend fun getCategories(@Header("Authorization") token: String): Response<List<CategoryDto>>

    @POST("categories")
    suspend fun saveCategory(
        @Header("Authorization") token: String,
        @Body category: CategoryDto
    ): Response<Map<String, String>>

    @PUT("categories/{id}")
    suspend fun updateCategory(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body category: CategoryDto
    ): Response<Map<String, String>>

    @DELETE("categories/{id}")
    suspend fun deleteCategory(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Map<String, String>>
}
