package com.mnfarzaneh.solalrchef.data.sync

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeSyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun enqueue() {
        Log.d("RecipeSyncScheduler", "enqueue() called")
        val workManager = WorkManager.getInstance(context)

        val request = OneTimeWorkRequestBuilder<RecipeSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                10,
                TimeUnit.SECONDS
            )
            .build()

        Log.d("RecipeSyncScheduler", "Enqueuing request id=${request.id}")

        workManager.enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            request
        )
    }

    private companion object {
        const val UNIQUE_WORK_NAME = "recipe_cloud_sync_v2"
    }
}
