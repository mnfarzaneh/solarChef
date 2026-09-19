package com.mnfarzaneh.solalrchef.data

import androidx.room.withTransaction
import com.mnfarzaneh.solalrchef.data.local.CategoryDao
import com.mnfarzaneh.solalrchef.data.local.CategoryEntity
import com.mnfarzaneh.solalrchef.data.local.PendingCategorySyncDao
import com.mnfarzaneh.solalrchef.data.local.PendingCategorySyncEntity
import com.mnfarzaneh.solalrchef.data.local.RecipeDatabase
import com.mnfarzaneh.solalrchef.data.remote.ApiResult
import com.mnfarzaneh.solalrchef.data.remote.SolarChefApiRepository
import com.mnfarzaneh.solalrchef.data.remote.dto.CategoryDto
import com.mnfarzaneh.solalrchef.data.sync.CategorySyncScheduler
import com.mnfarzaneh.solalrchef.model.Category
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.flowOf

class CategoryRepository @Inject constructor(
    private val database: RecipeDatabase,
    private val dao: CategoryDao,
    private val pendingSyncDao: PendingCategorySyncDao,
    private val apiRepository: SolarChefApiRepository,
    private val syncScheduler: CategorySyncScheduler
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    val categories: Flow<List<Category>> =
        apiRepository.authState.flatMapLatest { user ->
            dao.getCategoriesForOwner(user?.userId).map { entities ->
                entities.map { entity ->
                    Category(
                        id = entity.id,
                        name = entity.name,
                        emoji = entity.emoji
                    )
                }
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

    suspend fun addCategory(name: String, emoji: String = "🍽️"): String {
        val ownerId = apiRepository.userId
        android.util.Log.d(
            "CategorySyncRepository",
            "addCategory ownerId=$ownerId"
        )
        val entity = CategoryEntity(
            id = "cat_${UUID.randomUUID()}",
            name = name.trim(),
            emoji = emoji,
            sortOrder = 0,
            ownerId = ownerId
        )

        database.withTransaction {
            dao.upsertCategory(entity)

            // اگر کاربر وارد حسابش باشد، این ساختن وارد صف ارسال می‌شود.
            if (ownerId != null) {
                enqueueOperation(
                    categoryId = entity.id,
                    ownerId = ownerId,
                    requestedOperation = PendingCategorySyncEntity.CREATE
                )
                android.util.Log.d(
                    "CategorySyncRepository",
                    "Pending CREATE inserted: ${entity.id}"
                )
            }
        }

        if (ownerId != null) {
            syncScheduler.schedule()
        }
        return entity.id
    }

    suspend fun updateCategory(id: String, name: String, emoji: String) {
        if (id in defaultCategoryIds) return

        val old = dao.getCategoryById(id) ?: return
        val ownerId = old.ownerId

        val updated = old.copy(
            name = name.trim(),
            emoji = emoji
        )

        database.withTransaction {
            dao.upsertCategory(updated)

            if (ownerId != null) {
                enqueueOperation(
                    categoryId = id,
                    ownerId = ownerId,
                    requestedOperation = PendingCategorySyncEntity.UPDATE
                )
            }
        }

        if (ownerId != null) {
            syncScheduler.schedule()
        }
    }

    suspend fun deleteCategory(id: String) {
        // دسته‌های اصلی برنامه حذف نمی‌شوند.
        if (id in defaultCategoryIds) return

        val category = dao.getCategoryById(id) ?: return
        val ownerId = category.ownerId

        database.withTransaction {
            dao.deleteCategory(id)

            if (ownerId != null) {
                enqueueOperation(
                    categoryId = id,
                    ownerId = ownerId,
                    requestedOperation = PendingCategorySyncEntity.DELETE
                )
            }
        }

        if (ownerId != null) {
            syncScheduler.schedule()
        }
    }

    // دسته‌های مهمانِ ساخته‌شده پیش از ورود را به کاربر فعلی منتقل می‌کند.
    suspend fun claimUnclaimedCategories(ownerId: String) {
        val guestCategories = dao.getUnclaimedCustomCategories()

        database.withTransaction {
            dao.claimUnclaimedCategories(ownerId)

            guestCategories.forEach { category ->
                enqueueOperation(
                    categoryId = category.id,
                    ownerId = ownerId,
                    requestedOperation = PendingCategorySyncEntity.CREATE
                )
            }
        }

        if (guestCategories.isNotEmpty()) {
            syncScheduler.schedule()
        }
    }

    // دریافت دسته‌ها از سرور؛ وقتی تغییر ارسال‌نشده نداریم.
    suspend fun syncFromCloud() {
        val ownerId = apiRepository.userId ?: return

        // دادهٔ ارسال‌نشدهٔ محلی نباید با پاسخ قدیمی سرور overwrite شود.
        if (pendingSyncDao.countForOwner(ownerId) > 0) return

        apiRepository.downloadAllCategories()
            .onSuccess { cloudCategories ->
                database.withTransaction {
                    dao.deleteAllCategoriesForOwner(ownerId)

                    cloudCategories.forEach { dto ->
                        dao.upsertCategory(dto.toEntity(ownerId))
                    }
                }
            }
    }

    // عملیات صف را به‌ترتیب انجام می‌دهد.
    // true یعنی تمام شد؛ false یعنی اینترنت/سرور مشکل دارد و WorkManager دوباره تلاش کند.
    suspend fun syncPendingOperations(): Boolean {
        val ownerId = apiRepository.userId ?: return true
        val operations = pendingSyncDao.getForOwner(ownerId)

        for (pending in operations) {
            val succeeded = when (pending.operation) {
                PendingCategorySyncEntity.CREATE -> {
                    val category = dao.getCategoryById(pending.categoryId)

                    if (category == null) {
                        pendingSyncDao.deleteByCategoryId(pending.categoryId)
                        true
                    } else {
                        when (
                            val result = apiRepository.uploadCategory(
                                id = category.id,
                                name = category.name,
                                emoji = category.emoji,
                                sortOrder = category.sortOrder
                            )
                        ) {
                            is ApiResult.Success -> true

                            is ApiResult.Error ->
                                result.message.contains("قبلاً") ||
                                        result.message.contains("already", ignoreCase = true)
                        }
                    }
                }

                PendingCategorySyncEntity.UPDATE -> {
                    val category = dao.getCategoryById(pending.categoryId)

                    if (category == null) {
                        pendingSyncDao.deleteByCategoryId(pending.categoryId)
                        true
                    } else {
                        apiRepository.updateCategory(
                            id = category.id,
                            name = category.name,
                            emoji = category.emoji,
                            sortOrder = category.sortOrder
                        ) is ApiResult.Success
                    }
                }

                PendingCategorySyncEntity.DELETE -> {
                    when (val result = apiRepository.deleteCategory(pending.categoryId)) {
                        is ApiResult.Success -> true

                        is ApiResult.Error ->
                            result.message.contains("یافت نشد") ||
                                    result.message.contains("not found", ignoreCase = true)
                    }
                }

                else -> true
            }

            if (!succeeded) {
                android.util.Log.e(
                    "CategorySyncRepository",
                    "Operation failed: " +
                            "type=${pending.operation}, " +
                            "categoryId=${pending.categoryId}"
                )
                return false
            }

            pendingSyncDao.deleteByCategoryId(pending.categoryId)
            android.util.Log.d(
                "CategorySyncRepository",
                "Pending operations count=${operations.size}, ownerId=$ownerId"
            )
        }

        syncFromCloud()
        return true
    }


    suspend fun ensureDefaultCategoriesExist() {
        defaultCategories.forEach { category ->
            if (dao.getCategoryById(category.id) == null) {
                dao.upsertCategory(category)
            }
        }
    }

    private suspend fun enqueueOperation(
        categoryId: String,
        ownerId: String,
        requestedOperation: String
    ) {
        val existing = pendingSyncDao.getByCategoryId(categoryId)

        // اگر CREATE هنوز ارسال نشده و کاربر فقط آن را ویرایش کرده،
        // باید همچنان CREATE بماند؛ نه UPDATE.
        val finalOperation = when {
            requestedOperation == PendingCategorySyncEntity.DELETE ->
                PendingCategorySyncEntity.DELETE

            existing?.operation == PendingCategorySyncEntity.CREATE ->
                PendingCategorySyncEntity.CREATE

            else -> requestedOperation
        }

        pendingSyncDao.upsert(
            PendingCategorySyncEntity(
                categoryId = categoryId,
                ownerId = ownerId,
                operation = finalOperation,
                createdAt = existing?.createdAt ?: System.currentTimeMillis()
            )
        )
    }

    private fun CategoryDto.toEntity(ownerId: String): CategoryEntity {
        return CategoryEntity(
            id = id,
            name = name,
            emoji = emoji,
            sortOrder = sortOrder,
            ownerId = if (id in defaultCategoryIds) null else ownerId
        )
    }

    private companion object {
        val defaultCategoryIds = setOf(
            "cat_bread",
            "cat_cake",
            "cat_pizza"
        )

        val defaultCategories = listOf(
            CategoryEntity("cat_bread", "نانها", "🍞", 0, null),
            CategoryEntity("cat_cake", "شیرینی‌جات", "🍰", 1, null),
            CategoryEntity("cat_pizza", "پیتزا", "🍕", 2, null)
        )
    }
}
