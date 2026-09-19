package com.mnfarzaneh.solalrchef.data.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mnfarzaneh.solalrchef.data.UserRecipeRepository
import com.mnfarzaneh.solalrchef.data.CategoryRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class RecipeSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val userRecipeRepository: UserRecipeRepository,
    private val categoryRepository: CategoryRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("RecipeSyncWorker", "Worker started")

        return try {
            // دستور ممکن است به دستهٔ شخصی تازه‌ساخته‌شده وابسته باشد؛ بنابراین
            // دسته‌ها باید پیش از خود دستور روی سرور موجود باشند.
            val categoriesSuccessful = categoryRepository.syncPendingOperations()
            val successful = categoriesSuccessful &&
                userRecipeRepository.syncPendingOperations()

            if (successful) {
                Log.d("RecipeSyncWorker", "Sync completed successfully")
                Result.success()
            } else {
                Log.e("RecipeSyncWorker", "Sync failed; retry requested")
                retryOrStop()
            }
        } catch (e: Exception) {
            Log.e("RecipeSyncWorker", "Worker crashed", e)
            retryOrStop()
        }
    }

    private fun retryOrStop(): Result =
        if (runAttemptCount >= MAX_RETRY_ATTEMPTS) {
            Log.e("RecipeSyncWorker", "Retry limit reached; operation remains pending")
            Result.failure()
        } else {
            Result.retry()
        }

    private companion object {
        const val MAX_RETRY_ATTEMPTS = 8
    }
}
