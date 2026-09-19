package com.mnfarzaneh.solalrchef.data.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mnfarzaneh.solalrchef.data.ContentRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ArticleNotificationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val contentRepository: ContentRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val heroResult = contentRepository.getHero()
        if (heroResult.isFailure) {
            Log.w(TAG, "Could not check latest hero article", heroResult.exceptionOrNull())
            return Result.retry()
        }

        val article = heroResult.getOrNull() ?: return Result.success()
        val preferences = applicationContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        val previousId = preferences.getString(KEY_LAST_ARTICLE_ID, null)

        if (previousId == null) {
            preferences.edit().putString(KEY_LAST_ARTICLE_ID, article.id).apply()
            return Result.success()
        }

        if (previousId != article.id) {
            ArticleNotificationScheduler.showArticleNotification(applicationContext, article)
            preferences.edit().putString(KEY_LAST_ARTICLE_ID, article.id).apply()
        }

        return Result.success()
    }

    private companion object {
        const val TAG = "ArticleNotification"
        const val PREFERENCES = "article_notification_preferences"
        const val KEY_LAST_ARTICLE_ID = "last_notified_article_id"
    }
}
