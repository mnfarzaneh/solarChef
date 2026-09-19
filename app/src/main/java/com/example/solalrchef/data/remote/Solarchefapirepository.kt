package com.mnfarzaneh.solalrchef.data.remote

import com.mnfarzaneh.solalrchef.data.remote.dto.CategoryDto
import com.mnfarzaneh.solalrchef.data.remote.dto.LoginRequestDto
import com.mnfarzaneh.solalrchef.data.remote.dto.LogoutRequestDto
import com.mnfarzaneh.solalrchef.data.remote.dto.RecipeDto
import com.mnfarzaneh.solalrchef.data.remote.dto.RefreshRequestDto
import com.mnfarzaneh.solalrchef.data.remote.dto.RegisterRequestDto
import com.mnfarzaneh.solalrchef.data.secure.SecureSessionStorage
import com.mnfarzaneh.solalrchef.data.secure.StoredSession
import com.mnfarzaneh.solalrchef.model.Recipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton
import com.mnfarzaneh.solalrchef.data.remote.dto.ForgotPasswordRequestDto
import com.mnfarzaneh.solalrchef.data.remote.dto.ResetPasswordRequestDto
import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import com.mnfarzaneh.solalrchef.data.remote.dto.ImageUploadResponseDto
import com.mnfarzaneh.solalrchef.R

sealed class ApiResult {
    object Success : ApiResult()
    data class Error(val message: String) : ApiResult()
}

@Singleton
class SolarChefApiRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionStorage: SecureSessionStorage,
    @ApplicationContext private val appContext: Context
){
    private val refreshMutex = Mutex()

    private val _authState = MutableStateFlow<UserInfo?>(currentUser)
    val authState: Flow<UserInfo?> = _authState

    data class UserInfo(
        val userId: String,
        val email: String,
        val token: String
    )

    val currentUser: UserInfo?
        get() = sessionStorage.read()?.let { session ->
            UserInfo(
                userId = session.userId,
                email = session.email,
                token = session.accessToken
            )
        }

    val isLoggedIn: Boolean
        get() = currentUser != null

    val userId: String?
        get() = currentUser?.userId

    suspend fun register(email: String, password: String): ApiResult {
        return try {
            val response = apiService.register(
                RegisterRequestDto(email.trim(), password)
            )

            if (response.isSuccessful) {
                val auth = response.body()
                    ?: return ApiResult.Error("پاسخ سرور ناقص است")

                saveSession(
                    accessToken = auth.accessToken,
                    refreshToken = auth.refreshToken,
                    userId = auth.userId,
                    email = auth.email
                )
                ApiResult.Success
            } else {
                ApiResult.Error(parseError(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            ApiResult.Error(friendlyError(e))
        }
    }

    suspend fun login(email: String, password: String): ApiResult {
        return try {
            val response = apiService.login(
                LoginRequestDto(email.trim(), password)
            )

            if (response.isSuccessful) {
                val auth = response.body()
                    ?: return ApiResult.Error("پاسخ سرور ناقص است")

                saveSession(
                    accessToken = auth.accessToken,
                    refreshToken = auth.refreshToken,
                    userId = auth.userId,
                    email = auth.email
                )
                ApiResult.Success
            } else {
                ApiResult.Error(parseError(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            ApiResult.Error(friendlyError(e))
        }
    }

    suspend fun logout() {
        val refreshToken = sessionStorage.read()?.refreshToken

        try {
            if (refreshToken != null) {
                apiService.logout(LogoutRequestDto(refreshToken))
            }
        } catch (_: Exception) {
            // حتی در صورت قطع اینترنت، خروج محلی باید انجام شود.
        } finally {
            sessionStorage.clear()
            _authState.value = null
        }
    }

    suspend fun uploadRecipeImage(
        recipeId: String,
        imagePath: String
    ): Result<String> {
        val preparedFile = prepareImageFile(imagePath)
            ?: return Result.failure(Exception("فایل تصویر قابل خواندن نیست"))

        return try {
            val contentType = appContext.contentResolver
                .getType(Uri.parse(imagePath))
                ?.toMediaTypeOrNull()
                ?: "image/jpeg".toMediaType()

            val imagePart = MultipartBody.Part.createFormData(
                name = "image",
                filename = preparedFile.file.name,
                body = preparedFile.file.asRequestBody(contentType)
            )

            val recipeIdBody = recipeId.toRequestBody("text/plain".toMediaType())

            val response = authorizedRequest { token ->
                apiService.uploadImage(
                    token = token,
                    recipeId = recipeIdBody,
                    image = imagePart
                )
            }

            when {
                response == null ->
                    Result.failure(Exception("نشست شما منقضی شده؛ دوباره وارد شوید"))

                response.isSuccessful && response.body() != null ->
                    Result.success(response.body()!!.imageKey)

                else ->
                    Result.failure(Exception(parseError(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            preparedFile.isTemporary?.let { tempFile ->
                tempFile.delete()
            }
        }
    }

    private data class PreparedImageFile(
        val file: File,
        val isTemporary: File? = null
    )

    private fun prepareImageFile(imagePath: String): PreparedImageFile? {
        val localFile = File(imagePath)

        if (localFile.isFile) {
            return PreparedImageFile(file = localFile)
        }

        return try {
            val uri = Uri.parse(imagePath)
            val tempFile = File.createTempFile(
                "recipe-upload-",
                ".jpg",
                appContext.cacheDir
            )

            appContext.contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return null

            PreparedImageFile(
                file = tempFile,
                isTemporary = tempFile
            )
        } catch (_: Exception) {
            null
        }
    }

    suspend fun requestPasswordReset(email: String): ApiResult {
        return try {
            val response = apiService.forgotPassword(
                ForgotPasswordRequestDto(email.trim())
            )

            if (response.isSuccessful) {
                ApiResult.Success
            } else {
                ApiResult.Error(parseError(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            ApiResult.Error(friendlyError(e))
        }
    }

    suspend fun resetPassword(
        email: String,
        code: String,
        newPassword: String
    ): ApiResult {
        return try {
            val response = apiService.resetPassword(
                ResetPasswordRequestDto(
                    email = email.trim(),
                    code = code.trim(),
                    newPassword = newPassword
                )
            )

            if (response.isSuccessful) {
                ApiResult.Success
            } else {
                ApiResult.Error(parseError(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            ApiResult.Error(friendlyError(e))
        }
    }

    suspend fun uploadRecipe(recipe: Recipe): ApiResult {
        return try {
            val response = authorizedRequest { token ->
                apiService.saveRecipe(token, recipe.toDto())
            }

            when {
                response == null ->
                    ApiResult.Error("نشست شما منقضی شده؛ دوباره وارد شوید")

                response.isSuccessful ->
                    ApiResult.Success

                else ->
                    ApiResult.Error(parseError(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            ApiResult.Error(friendlyError(e))
        }
    }

    suspend fun deleteRecipe(recipeId: String): ApiResult {
        return try {
            val response = authorizedRequest { token ->
                apiService.deleteRecipe(token, recipeId)
            }

            when {
                response == null ->
                    ApiResult.Error("نشست شما منقضی شده؛ دوباره وارد شوید")

                response.isSuccessful ->
                    ApiResult.Success

                else ->
                    ApiResult.Error(parseError(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            ApiResult.Error(friendlyError(e))
        }
    }

    suspend fun downloadAllRecipes(): Result<List<Recipe>> {
        return try {
            val response = authorizedRequest { token ->
                apiService.getRecipes(token)
            }

            when {
                response == null ->
                    Result.failure(Exception("نشست شما منقضی شده؛ دوباره وارد شوید"))

                response.isSuccessful ->
                    Result.success(
                        response.body()
                            ?.map { it.toRecipe() }
                            ?: emptyList()
                    )

                else ->
                    Result.failure(
                        Exception(parseError(response.errorBody()?.string()))
                    )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadAllCategories(): Result<List<CategoryDto>> {
        return try {
            val response = authorizedRequest { token ->
                apiService.getCategories(token)
            }

            when {
                response == null ->
                    Result.failure(
                        Exception("نشست شما منقضی شده؛ دوباره وارد شوید")
                    )

                response.isSuccessful ->
                    Result.success(response.body() ?: emptyList())

                else ->
                    Result.failure(
                        Exception(parseError(response.errorBody()?.string()))
                    )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadCategory(
        id: String,
        name: String,
        emoji: String,
        sortOrder: Int
    ): ApiResult {
        return try {
            val response = authorizedRequest { token ->
                apiService.saveCategory(
                    token,
                    CategoryDto(id, name, emoji, sortOrder)
                )
            }

            when {
                response == null ->
                    ApiResult.Error("نشست شما منقضی شده؛ دوباره وارد شوید")

                response.isSuccessful ->
                    ApiResult.Success

                else ->
                    ApiResult.Error(parseError(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            ApiResult.Error(friendlyError(e))
        }
    }

    suspend fun updateCategory(
        id: String,
        name: String,
        emoji: String,
        sortOrder: Int
    ): ApiResult {
        return try {
            val response = authorizedRequest { token ->
                apiService.updateCategory(
                    token,
                    id,
                    CategoryDto(id, name, emoji, sortOrder)
                )
            }

            when {
                response == null ->
                    ApiResult.Error("نشست شما منقضی شده؛ دوباره وارد شوید")

                response.isSuccessful ->
                    ApiResult.Success

                else ->
                    ApiResult.Error(parseError(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            ApiResult.Error(friendlyError(e))
        }
    }

    suspend fun deleteCategory(id: String): ApiResult {
        return try {
            val response = authorizedRequest { token ->
                apiService.deleteCategory(token, id)
            }

            when {
                response == null ->
                    ApiResult.Error("نشست شما منقضی شده؛ دوباره وارد شوید")

                response.isSuccessful ->
                    ApiResult.Success

                else ->
                    ApiResult.Error(parseError(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            ApiResult.Error(friendlyError(e))
        }
    }

    private suspend fun <T> authorizedRequest(
        request: suspend (bearerToken: String) -> Response<T>
    ): Response<T>? {
        val oldSession = sessionStorage.read() ?: return null

        val firstResponse =
            request("Bearer ${oldSession.accessToken}")

        if (firstResponse.code() != 401) {
            return firstResponse
        }

        android.util.Log.d(
            "SolarChefAuth",
            "Access token expired; refreshing session"
        )

        val refreshed =
            refreshSession(oldSession.accessToken)

        if (!refreshed) {
            android.util.Log.e(
                "SolarChefAuth",
                "Session refresh failed"
            )
            return null
        }

        val newAccessToken =
            sessionStorage.read()?.accessToken ?: return null

        android.util.Log.d(
            "SolarChefAuth",
            "Session refreshed; retrying original request"
        )

        return request("Bearer $newAccessToken")
    }

    private suspend fun refreshSession(
        expiredAccessToken: String
    ): Boolean {
        return refreshMutex.withLock {
            val latestSession =
                sessionStorage.read() ?: return@withLock false

            // ممکن است درخواست دیگری زودتر Refresh را انجام داده باشد.
            if (latestSession.accessToken != expiredAccessToken) {
                android.util.Log.d(
                    "SolarChefAuth",
                    "Session already refreshed by another request"
                )
                return@withLock true
            }

            try {
                val response = apiService.refresh(
                    RefreshRequestDto(latestSession.refreshToken)
                )

                if (!response.isSuccessful) {
                    android.util.Log.e(
                        "SolarChefAuth",
                        "Refresh failed with HTTP ${response.code()}: " +
                                parseError(response.errorBody()?.string())
                    )

                    // فقط در خطاهای قطعی احراز هویت نشست حذف شود.
                    // خطاهای موقت سرور نباید کاربر را خارج کنند.
                    if (response.code() == 400 || response.code() == 401) {
                        clearSession()
                    }

                    return@withLock false
                }

                val auth = response.body()

                if (auth == null) {
                    android.util.Log.e(
                        "SolarChefAuth",
                        "Refresh response body was empty"
                    )
                    return@withLock false
                }

                saveSession(
                    accessToken = auth.accessToken,
                    refreshToken = auth.refreshToken,
                    userId = auth.userId,
                    email = auth.email
                )

                android.util.Log.d(
                    "SolarChefAuth",
                    "Session refresh completed"
                )

                true
            } catch (e: Exception) {
                android.util.Log.e(
                    "SolarChefAuth",
                    "Temporary refresh error",
                    e
                )

                // خطای اینترنت نباید نشست ذخیره‌شده را پاک کند.
                false
            }
        }
    }

    private fun saveSession(
        accessToken: String,
        refreshToken: String,
        userId: String,
        email: String
    ) {
        val session = StoredSession(
            accessToken = accessToken,
            refreshToken = refreshToken,
            userId = userId,
            email = email
        )

        sessionStorage.save(session)
        _authState.value = UserInfo(
            userId = userId,
            email = email,
            token = accessToken
        )
    }

    private fun clearSession() {
        sessionStorage.clear()
        _authState.value = null
    }

    private fun parseError(body: String?): String {
        if (body == null) return "خطای ناشناخته"

        return try {
            val json = org.json.JSONObject(body)
            when (json.optString("code")) {
                "DAILY_UPLOAD_LIMIT_REACHED" ->
                    appContext.getString(R.string.error_daily_upload_limit)
                "RECIPE_LIMIT_REACHED" ->
                    appContext.getString(R.string.error_recipe_limit)
                "CATEGORY_LIMIT_REACHED" ->
                    appContext.getString(R.string.error_category_limit)
                "IMAGE_TOO_LARGE" ->
                    appContext.getString(R.string.error_image_too_large)
                "UNSUPPORTED_IMAGE_FORMAT" ->
                    appContext.getString(R.string.error_image_format)
                "LOGIN_RATE_LIMITED" ->
                    appContext.getString(R.string.error_login_rate_limit)
                "REGISTER_RATE_LIMITED", "PASSWORD_RECOVERY_RATE_LIMITED" ->
                    appContext.getString(R.string.error_request_rate_limit)
                else -> json.optString("error", "خطای ناشناخته")
            }
        } catch (_: Exception) {
            "خطای ناشناخته"
        }
    }

    private fun friendlyError(e: Exception): String = when {
        e.message?.contains("Unable to resolve host") == true ->
            "اتصال به سرور برقرار نشد. اینترنت را بررسی کنید"

        e.message?.contains("timeout", ignoreCase = true) == true ->
            "سرور پاسخ نداد. دوباره تلاش کنید"

        else ->
            "خطا در ارتباط با سرور"
    }
}

private fun Recipe.toDto(): RecipeDto = RecipeDto(
    id = id,
    title = title,
    description = description,
    imagePath = if (imageKey.isBlank()) imagePath else "",
    imageKey = imageKey,
    author = author,
    source = source,
    totalTime = totalTime,
    cookTime = cookTime,
    yield = yield,
    calories = calories,
    difficulty = difficulty,
    rating = rating,
    isFavorite = isFavorite,
    ingredientsJson = com.mnfarzaneh.solalrchef.data.local
        .ingredientsToJsonPublic(ingredients),
    stepsJson = com.mnfarzaneh.solalrchef.data.local
        .stepsToJsonPublic(steps),
    equipmentJson = com.mnfarzaneh.solalrchef.data.local
        .equipmentToJsonPublic(equipment),
    categoryIds = categoryIds,
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun RecipeDto.toRecipe(): Recipe = Recipe(
    id = id.orEmpty(),
    title = title.orEmpty(),
    description = description.orEmpty(),

    imagePath = imageUrl.orEmpty().ifBlank { imagePath.orEmpty() },
    imageKey = imageKey.orEmpty(),

    author = author.orEmpty(),
    source = source.orEmpty(),
    totalTime = totalTime.orEmpty(),
    cookTime = cookTime.orEmpty(),
    yield = yield.orEmpty(),

    calories = calories,
    difficulty = difficulty.orEmpty(),
    rating = rating,
    isFavorite = isFavorite,

    ingredients = com.mnfarzaneh.solalrchef.data.local
        .parseIngredientsPublic(ingredientsJson.orEmpty()),

    steps = com.mnfarzaneh.solalrchef.data.local
        .parseStepsPublic(stepsJson.orEmpty()),

    equipment = com.mnfarzaneh.solalrchef.data.local
        .parseEquipmentPublic(equipmentJson.orEmpty()),

    categoryIds = categoryIds.orEmpty(),
    createdAt = createdAt,
    updatedAt = updatedAt
)
