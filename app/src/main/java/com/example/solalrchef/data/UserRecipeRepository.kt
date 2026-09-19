package com.mnfarzaneh.solalrchef.data

import android.util.Log
import androidx.room.withTransaction
import com.mnfarzaneh.solalrchef.data.local.PendingRecipeSyncDao
import com.mnfarzaneh.solalrchef.data.local.PendingRecipeSyncEntity
import com.mnfarzaneh.solalrchef.data.local.RecipeDao
import com.mnfarzaneh.solalrchef.data.local.RecipeDatabase
import com.mnfarzaneh.solalrchef.data.local.SystemFavoriteDao
import com.mnfarzaneh.solalrchef.data.local.SystemFavoriteEntity
import com.mnfarzaneh.solalrchef.data.local.toEntity
import com.mnfarzaneh.solalrchef.data.local.toRecipe
import com.mnfarzaneh.solalrchef.data.remote.ApiResult
import com.mnfarzaneh.solalrchef.data.remote.SolarChefApiRepository
import com.mnfarzaneh.solalrchef.data.sync.RecipeSyncScheduler
import com.mnfarzaneh.solalrchef.model.Recipe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlinx.coroutines.flow.flowOf

class UserRecipeRepository @Inject constructor(
    private val database: RecipeDatabase,
    private val dao: RecipeDao,
    private val pendingSyncDao: PendingRecipeSyncDao,
    private val apiRepository: SolarChefApiRepository,
    private val syncScheduler: RecipeSyncScheduler,
    private val systemFavoriteDao: SystemFavoriteDao
) {
    private fun favoriteOwnerKey(): String = apiRepository.userId ?: GUEST_FAVORITES_OWNER

    @OptIn(ExperimentalCoroutinesApi::class)
    val systemFavoriteIds: Flow<Set<String>> = apiRepository.authState.flatMapLatest { user ->
        systemFavoriteDao.observeIds(user?.userId ?: GUEST_FAVORITES_OWNER).map { it.toSet() }
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    val allRecipes: Flow<List<Recipe>> = apiRepository.authState.flatMapLatest { user ->
        dao.getRecipesForOwner(user?.userId).map { entities ->
            entities.map { it.toRecipe() }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val pendingSyncCount: Flow<Int> =
        apiRepository.authState.flatMapLatest { user ->
            if (user == null) {
                flowOf(0)
            } else {
                pendingSyncDao.observeCountForOwner(user.userId)
            }
        }

    suspend fun saveRecipe(recipe: Recipe) {
        val ownerId = apiRepository.userId
        val existing = dao.getRecipeById(recipe.id)
        val now = System.currentTimeMillis()

        Log.d(
            "RecipeRepository",
            "saveRecipe id=${recipe.id}, ownerId=$ownerId"
        )

        database.withTransaction {
            dao.upsertRecipe(
                recipe.toEntity().copy(
                    ownerId = ownerId,
                    createdAt = existing?.createdAt
                        ?: recipe.createdAt.takeIf { it > 0L }
                        ?: now,
                    updatedAt = now
                )
            )

            if (ownerId != null) {
                pendingSyncDao.upsert(
                    PendingRecipeSyncEntity(
                        recipeId = recipe.id,
                        ownerId = ownerId,
                        operation = PendingRecipeSyncEntity.UPSERT
                    )
                )

                Log.d(
                    "RecipeRepository",
                    "Pending UPSERT inserted for ${recipe.id}"
                )
            } else {
                Log.w(
                    "RecipeRepository",
                    "Recipe saved only locally because ownerId is null"
                )
            }
        }

        if (ownerId != null) {
            Log.d("RecipeRepository", "Calling syncScheduler.enqueue()")
            syncScheduler.enqueue()
        }
    }
    suspend fun deleteRecipe(recipeId: String) {
        val ownerId = dao.getRecipeById(recipeId)?.ownerId ?: apiRepository.userId

        database.withTransaction {
            dao.deleteRecipeById(recipeId)

            if (ownerId != null) {
                // Tombstone: حتی بعد از حذف رکورد محلی، حذف باید به سرور برسد.
                pendingSyncDao.upsert(
                    PendingRecipeSyncEntity(
                        recipeId = recipeId,
                        ownerId = ownerId,
                        operation = PendingRecipeSyncEntity.DELETE
                    )
                )
            }
        }

        if (ownerId != null) {
            syncScheduler.enqueue()
        }
    }

    suspend fun getRecipeById(id: String): Recipe? =
        dao.getRecipeById(id)?.toRecipe()

    suspend fun updateFavorite(recipeId: String, isFavorite: Boolean) {
        val ownerId = dao.getRecipeById(recipeId)?.ownerId ?: apiRepository.userId

        database.withTransaction {
            dao.updateFavorite(recipeId, isFavorite, System.currentTimeMillis())

            if (ownerId != null) {
                pendingSyncDao.upsert(
                    PendingRecipeSyncEntity(
                        recipeId = recipeId,
                        ownerId = ownerId,
                        operation = PendingRecipeSyncEntity.UPSERT
                    )
                )
            }
        }

        if (ownerId != null) {
            syncScheduler.enqueue()
        }
    }

    suspend fun isSystemFavorite(recipeId: String): Boolean =
        systemFavoriteDao.isFavorite(recipeId, favoriteOwnerKey())

    suspend fun updateSystemFavorite(recipeId: String, isFavorite: Boolean) {
        val ownerKey = favoriteOwnerKey()
        if (isFavorite) {
            systemFavoriteDao.upsert(SystemFavoriteEntity(recipeId = recipeId, ownerKey = ownerKey))
        } else {
            systemFavoriteDao.delete(recipeId, ownerKey)
        }
    }

    suspend fun syncFromCloud() {
        val currentUserId = apiRepository.userId ?: return

        // تا وقتی تغییر محلیِ ارسال‌نشده داریم، دادهٔ سرور نباید آن را overwrite کند.
        if (pendingSyncDao.getForOwner(currentUserId).isNotEmpty()) return

        apiRepository.downloadAllRecipes().onSuccess { cloudRecipes ->
            // نسخه‌های قدیمی API یا پاسخ ناقص ممکن است زمان را صفر برگردانند.
            // قبل از جایگزینی جدول، زمان معتبر محلی را نگه می‌داریم تا ترتیب
            // «دستورهای اخیر» پس از Sync ناگهان به حالت قبل برنگردد.
            val localRecipesById = dao
                .getRecipesForOwner(currentUserId)
                .first()
                .associateBy { it.id }

            database.withTransaction {
                dao.deleteAllRecipesForOwner(currentUserId)

                cloudRecipes.forEach { cloudRecipe ->
                    val localRecipe = localRecipesById[cloudRecipe.id]
                    val fallbackTime = System.currentTimeMillis()
                    val mergedRecipe = cloudRecipe.copy(
                        createdAt = cloudRecipe.createdAt.takeIf { it > 0L }
                            ?: localRecipe?.createdAt?.takeIf { it > 0L }
                            ?: fallbackTime,
                        updatedAt = cloudRecipe.updatedAt.takeIf { it > 0L }
                            ?: localRecipe?.updatedAt?.takeIf { it > 0L }
                            ?: fallbackTime
                    )

                    Log.d(
                        "RecipeSyncRepository",
                        "Cloud recipe=${cloudRecipe.id}, " +
                                "createdAt=${cloudRecipe.createdAt}, " +
                                "updatedAt=${cloudRecipe.updatedAt}, " +
                                "mergedUpdatedAt=${mergedRecipe.updatedAt}"
                    )

                    dao.upsertRecipe(
                        mergedRecipe.toEntity().copy(ownerId = currentUserId)
                    )
                }
            }
        }
    }

    suspend fun pushAllLocalRecipesToCloud() {
        val userId = apiRepository.userId ?: return
        val guestRecipes = dao.getRecipesForOwner(null).first()

        database.withTransaction {
            if (guestRecipes.isNotEmpty()) {
                dao.claimUnclaimedRecipes(userId)

                guestRecipes.forEach { recipe ->
                    pendingSyncDao.upsert(
                        PendingRecipeSyncEntity(
                            recipeId = recipe.id,
                            ownerId = userId,
                            operation = PendingRecipeSyncEntity.UPSERT
                        )
                    )
                }
            }
        }

        // حتی اگر دستور مهمان نداشتیم، ممکن است عملیات قدیمیِ در انتظار وجود داشته باشد.
        syncScheduler.enqueue()
    }

    /**
     * فقط Worker این تابع را اجرا می‌کند.
     * true = صف کامل ارسال شد؛ false = خطای موقت و نیاز به تلاش مجدد.
     */
    suspend fun syncPendingOperations(): Boolean {
        val currentUserId = apiRepository.userId
            ?: return true // کاربر خارج شده؛ عملیات برای ورود بعدی باقی می‌ماند.

        while (true) {
            val operation = pendingSyncDao
                .getForOwner(currentUserId)
                .firstOrNull()
                ?: break

            val result = when (operation.operation) {
                PendingRecipeSyncEntity.UPSERT -> {
                    val recipe = dao.getRecipeById(operation.recipeId)

                    if (recipe == null || recipe.ownerId != currentUserId) {
                        // اگر رکورد محلی دیگر وجود ندارد، حالت نهایی باید «حذف‌شده» باشد.
                        apiRepository.deleteRecipe(operation.recipeId)
                    } else {
                        syncRecipeToCloud(recipe.toRecipe())
                    }
                }

                PendingRecipeSyncEntity.DELETE -> {
                    apiRepository.deleteRecipe(operation.recipeId)
                }

                else -> {
                    ApiResult.Error("نوع عملیات sync نامعتبر است")
                }
            }

            val wasSuccessful = result is ApiResult.Success ||
                    (
                            operation.operation == PendingRecipeSyncEntity.DELETE &&
                                    result is ApiResult.Error &&
                                    result.message.contains("یافت نشد")
                            )

            if (!wasSuccessful) {
                val errorMessage =
                    (result as? ApiResult.Error)?.message
                        ?: "خطای نامشخص"

                android.util.Log.e(
                    "RecipeSyncRepository",
                    "Sync failed: " +
                            "operation=${operation.operation}, " +
                            "recipeId=${operation.recipeId}, " +
                            "error=$errorMessage"
                )

                return false
            }

            pendingSyncDao.deleteByRecipeId(operation.recipeId)
        }

        // پس از خالی‌شدن صف، نسخهٔ سرور را می‌خوانیم تا گوشی دقیقاً همگام شود.
        syncFromCloud()
        return true
    }

    private suspend fun syncRecipeToCloud(recipe: Recipe): ApiResult {
        var cloudRecipe = recipe
        android.util.Log.d(
            "RecipeSyncRepository",
            "syncRecipeToCloud: id=${recipe.id}, " +
                    "imagePath=${recipe.imagePath}, " +
                    "imageKey=${recipe.imageKey}"
        )
        if (
            cloudRecipe.imagePath.isImageUploadCandidate() &&
            cloudRecipe.imageKey.isBlank()
        ) {
            android.util.Log.d(
                "RecipeSyncRepository",
                "Uploading image for ${cloudRecipe.id}"
            )
            val imageKey = apiRepository
                .uploadRecipeImage(
                    recipeId = cloudRecipe.id,
                    imagePath = cloudRecipe.imagePath
                )
                .getOrElse { error ->
                    return ApiResult.Error(
                        error.message ?: "آپلود عکس ناموفق بود"
                    )
                }

            cloudRecipe = cloudRecipe.copy(imageKey = imageKey)

            database.withTransaction {
                dao.upsertRecipe(
                    cloudRecipe.toEntity().copy(
                        ownerId = apiRepository.userId
                    )
                )
            }
        }
        Log.d(
            "RecipeSyncRepository",
            "syncRecipeToCloud: id=${recipe.id}, imagePath=${recipe.imagePath}, imageKey=${recipe.imageKey}"
        )
        Log.d(
            "RecipeSyncRepository",
            "Uploading recipe=${recipe.id}, categoryIds=${recipe.categoryIds}"
        )
        return apiRepository.uploadRecipe(cloudRecipe)
    }

    private fun String.isImageUploadCandidate(): Boolean =
        isNotBlank() &&
                !startsWith("http://") &&
                !startsWith("https://")

    private companion object {
        const val GUEST_FAVORITES_OWNER = "__guest__"
    }
}
