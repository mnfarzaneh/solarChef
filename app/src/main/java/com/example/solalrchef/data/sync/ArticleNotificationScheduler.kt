package com.mnfarzaneh.solalrchef.data.sync

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.mnfarzaneh.solalrchef.MainActivity
import com.mnfarzaneh.solalrchef.R
import com.mnfarzaneh.solalrchef.data.remote.dto.HeroArticleDto
import java.util.concurrent.TimeUnit

object ArticleNotificationScheduler {
    private const val CHANNEL_ID = "new_articles"
    private const val DAILY_WORK_NAME = "daily_article_notification_check"
    private const val IMMEDIATE_WORK_NAME = "immediate_article_notification_check"

    private val connectedConstraint = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun scheduleDaily(context: Context) {
        val request = PeriodicWorkRequestBuilder<ArticleNotificationWorker>(24, TimeUnit.HOURS)
            .setConstraints(connectedConstraint)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DAILY_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun checkNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<ArticleNotificationWorker>()
            .setConstraints(connectedConstraint)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "مقاله‌های جدید",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "اعلان انتشار آموزش‌ها و مقاله‌های جدید سولارشف"
            }
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    fun showArticleNotification(context: Context, article: HeroArticleDto) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return

        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("solarchef://article/${Uri.encode(article.slug)}"),
            context,
            MainActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            article.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_solarchef)
            .setContentTitle("مقالهٔ جدید سولارشف")
            .setContentText(article.title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(article.summary.ifBlank { article.title }))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(article.id.hashCode(), notification)
    }
}
