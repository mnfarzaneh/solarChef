package com.mnfarzaneh.solalrchef

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import com.mnfarzaneh.solalrchef.data.sync.ArticleNotificationScheduler
import com.mnfarzaneh.solalrchef.data.sync.CategorySyncScheduler
import com.mnfarzaneh.solalrchef.data.sync.RecipeSyncScheduler

@HiltAndroidApp
class SolarChefApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var categorySyncScheduler: CategorySyncScheduler

    @Inject
    lateinit var recipeSyncScheduler: RecipeSyncScheduler

    override fun onCreate() {
        super.onCreate()
        ArticleNotificationScheduler.createNotificationChannel(this)
        ArticleNotificationScheduler.scheduleDaily(this)

        // اگر اجرای قبلی به‌دلیل اینترنت ضعیف یا بسته‌شدن برنامه نیمه‌کاره
        // مانده باشد، با شروع بعدی اپ صف پایدار Room دوباره بررسی می‌شود.
        categorySyncScheduler.schedule()
        recipeSyncScheduler.enqueue()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()
}
