package com.example.clarimind.notification

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.clarimind.data.NotificationEventEntity
import com.example.clarimind.data.UsageDatabase
import com.example.clarimind.data.FirebaseSyncHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClariMindNotificationListenerService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val postTime = sbn.postTime
        val notificationTitle = sbn.notification.extras.getString("android.title") ?: ""
        val notificationText = sbn.notification.extras.getString("android.text") ?: ""

        CoroutineScope(Dispatchers.IO).launch {
            val db = UsageDatabase.getInstance(applicationContext)
            val event = NotificationEventEntity(
                packageName = packageName,
                title = notificationTitle,
                text = notificationText,
                postTime = postTime
            )
            db.notificationEventDao().insert(event)
            FirebaseSyncHelper.uploadNotificationEvent(event)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Optional: handle notification removal
    }
} 