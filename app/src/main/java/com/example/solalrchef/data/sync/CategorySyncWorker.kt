package com.mnfarzaneh.solalrchef.data.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mnfarzaneh.solalrchef.data.CategoryRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class CategorySyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val categoryRepository: CategoryRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("CategorySyncWorker", "Worker started")

        return try {
            if (categoryRepository.syncPendingOperations()) {
                Log.d(
                    "CategorySyncWorker",
                    "Sync completed successfully"
                )
                Result.success()
            } else {
                Log.e(
                    "CategorySyncWorker",
                    "Sync failed; retry requested"
                )
                retryOrStop()
            }
        } catch (e: Exception) {
            Log.e(
                "CategorySyncWorker",
                "Worker crashed",
                e
            )
            retryOrStop()
        }
    }

    private fun retryOrStop(): Result =
        if (runAttemptCount >= MAX_RETRY_ATTEMPTS) {
            Log.e("CategorySyncWorker", "Retry limit reached; operation remains pending")
            Result.failure()
        } else {
            Result.retry()
        }

    private companion object {
        const val MAX_RETRY_ATTEMPTS = 8
    }
}
